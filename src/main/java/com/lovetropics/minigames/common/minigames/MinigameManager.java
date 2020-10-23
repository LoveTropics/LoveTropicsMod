package com.lovetropics.minigames.common.minigames;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.lovetropics.minigames.Constants;
import com.lovetropics.minigames.client.data.TropicraftLangKeys;
import com.lovetropics.minigames.common.minigames.behaviours.IMinigameBehavior;
import com.lovetropics.minigames.common.minigames.behaviours.IPollingMinigameBehavior;
import com.lovetropics.minigames.common.minigames.polling.PollingMinigameInstance;
import com.lovetropics.minigames.common.techstack.TechStack;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.command.CommandSource;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Unit;
import net.minecraft.util.text.*;
import net.minecraft.world.IWorld;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.dimension.DimensionType;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.world.ChunkDataEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.apache.logging.log4j.util.TriConsumer;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;

/**
 * Standard implementation of a minigame manager. Would prefer to do something other
 * than singleton-style implementation to allow for multiple managers to run multiple
 * minigames at once.
 */
public class MinigameManager implements IMinigameManager {
	/**
	 * Singleton instance that persists throughout server lifecycle.
	 */
	private static IMinigameManager INSTANCE;

	/**
	 * Registry map for minigame definitions. Used to fetch and start requested minigames.
	 */
	private Map<ResourceLocation, IMinigameDefinition> registeredMinigames = Maps.newHashMap();

	/**
	 * Current instance of the minigame. Agnostic from minigame definition and used to store
	 * the players and spectators that are a part of the current minigame.
	 * <p>
	 * Is null when there is no minigame running. Isn't set until a game has finished polling
	 * and has started.
	 */
	private MinigameInstance currentInstance;

	/**
	 * Currently polling game. Is null when there is no minigame polling or a minigame is running.
	 */
	private PollingMinigameInstance polling;

	/**
	 * Server reference to fetch players from player list.
	 */
	private MinecraftServer server;

	private MinigameManager(MinecraftServer server) {
		this.server = server;
	}

	/**
	 * Initialize the MinigameManager singleton. Registers itself for forge events
	 * and registers some minigame definitions.
	 *
	 * @param server The minecraft server used for fetching player list.
	 */
	public static void init(MinecraftServer server) {
		INSTANCE = new MinigameManager(server);
		MinecraftForge.EVENT_BUS.register(INSTANCE);
	}

	/**
	 * Returns null if init() has not been called yet. Shouldn't be called
	 * before the server has started.
	 *
	 * @return The global instance of the minigame manager.
	 */
	public static IMinigameManager getInstance() {
		return INSTANCE;
	}

	@Override
	public void register(IMinigameDefinition minigame) {
		if (this.registeredMinigames.containsKey(minigame.getID())) {
			throw new IllegalArgumentException("Minigame already registered with the following ID: " + minigame.getID());
		}

		this.registeredMinigames.put(minigame.getID(), minigame);
	}

	@Override
	public void unregister(ResourceLocation minigameID) {
		if (!this.registeredMinigames.containsKey(minigameID)) {
			TranslationTextComponent msg = new TranslationTextComponent(TropicraftLangKeys.COMMAND_MINIGAME_NOT_REGISTERED, minigameID);
			throw new IllegalArgumentException(msg.getFormattedText());
		}

		this.registeredMinigames.remove(minigameID);
	}

	@Override
	public Collection<IMinigameDefinition> getAllMinigames() {
		return Collections.unmodifiableCollection(this.registeredMinigames.values());
	}

	@Override
	public IMinigameInstance getActiveMinigame() {
		return this.currentInstance;
	}

	private MinigameResult<ITextComponent> close() {
		MinigameInstance minigame = this.currentInstance;
		this.currentInstance = null;

		if (minigame == null) {
			return MinigameResult.error(new TranslationTextComponent(TropicraftLangKeys.COMMAND_NO_MINIGAME));
		}

		try {
			IMinigameDefinition definition = minigame.getDefinition();

			MinigameResult<Unit> result = minigame.dispatchToBehaviors(IMinigameBehavior::onFinish);
			if (result.isError()) {
				return result.castError();
			}

			for (ServerPlayerEntity player : minigame.getPlayers()) {
				minigame.removePlayer(player);
			}

			// Send all players a message letting them know the minigame has finished
			for (ServerPlayerEntity player : this.server.getPlayerList().getPlayers()) {
				player.sendMessage(new TranslationTextComponent(TropicraftLangKeys.COMMAND_FINISHED_MINIGAME,
						definition.getName().applyTextStyle(TextFormatting.ITALIC).applyTextStyle(TextFormatting.AQUA))
						.applyTextStyle(TextFormatting.GOLD), ChatType.CHAT);
			}

			result = minigame.dispatchToBehaviors(IMinigameBehavior::onPostFinish);
			if (result.isError()) {
				return result.castError();
			}

			ITextComponent minigameName = definition.getName().applyTextStyle(TextFormatting.AQUA);
			return MinigameResult.ok(new TranslationTextComponent(TropicraftLangKeys.COMMAND_STOPPED_MINIGAME, minigameName).applyTextStyle(TextFormatting.GREEN));
		} catch (Exception e) {
			return MinigameResult.fromException("Unknown error finishing minigame", e);
		} finally {
			minigame.getDefinition().getMapProvider().close(minigame);
		}
	}

	@Override
	public MinigameResult<ITextComponent> finish() {
		MinigameInstance minigame = currentInstance;
		if (minigame == null) {
			return MinigameResult.error(new TranslationTextComponent(TropicraftLangKeys.COMMAND_NO_MINIGAME));
		}

		MinigameResult<ITextComponent> result = close();
		if (result.isOk()) {
			ITextComponent name = minigame.getDefinition().getName();
			TechStack.uploadMinigameResults(name.getString(), minigame.getStatistics());
		}

		return result;
	}

	@Override
	public MinigameResult<ITextComponent> cancel() {
		MinigameInstance minigame = this.currentInstance;
		if (minigame == null) {
			return MinigameResult.error(new TranslationTextComponent(TropicraftLangKeys.COMMAND_NO_MINIGAME));
		}

		MinigameResult<Unit> result = minigame.dispatchToBehaviors(IMinigameBehavior::onCancel);
		if (result.isError()) {
			return result.castError();
		}

		return close();
	}

	@Override
	public MinigameResult<ITextComponent> startPolling(ResourceLocation minigameId) {
		// Make sure minigame is registered with provided id
		if (!this.registeredMinigames.containsKey(minigameId)) {
			return MinigameResult.error(new TranslationTextComponent(TropicraftLangKeys.COMMAND_MINIGAME_ID_INVALID));
		}

		// Make sure there isn't a currently running minigame
		if (this.currentInstance != null) {
			return MinigameResult.error(new TranslationTextComponent(TropicraftLangKeys.COMMAND_MINIGAME_ALREADY_STARTED));
		}

		// Make sure another minigame isn't already polling
		if (this.polling != null) {
			return MinigameResult.error(new TranslationTextComponent(TropicraftLangKeys.COMMAND_ANOTHER_MINIGAME_POLLING));
		}

		IMinigameDefinition definition = registeredMinigames.get(minigameId);

		MinigameResult<PollingMinigameInstance> pollResult = PollingMinigameInstance.create(server, definition);
		if (pollResult.isError()) {
			return pollResult.castError();
		}

		PollingMinigameInstance polling = pollResult.getOk();

		MinigameResult<Unit> result = polling.dispatchToBehaviors(IPollingMinigameBehavior::onStartPolling);
		if (result.isError()) {
			return result.castError();
		}

		for (ServerPlayerEntity player : this.server.getPlayerList().getPlayers()) {
			player.sendMessage(new TranslationTextComponent(TropicraftLangKeys.COMMAND_MINIGAME_POLLING,
					definition.getName().applyTextStyle(TextFormatting.ITALIC).applyTextStyle(TextFormatting.AQUA),
					new StringTextComponent("/minigame register").applyTextStyle(TextFormatting.ITALIC).applyTextStyle(TextFormatting.GRAY))
					.applyTextStyle(TextFormatting.GOLD), ChatType.CHAT);
		}

		this.polling = polling;

		return MinigameResult.ok(new TranslationTextComponent(TropicraftLangKeys.COMMAND_MINIGAME_POLLED));
	}

	@Override
	public MinigameResult<ITextComponent> stopPolling() {
		PollingMinigameInstance polling = this.polling;

		// Check if a minigame is polling
		if (polling == null) {
			return MinigameResult.error(new TranslationTextComponent(TropicraftLangKeys.COMMAND_NO_MINIGAME_POLLING));
		}

		this.polling = null;

		for (ServerPlayerEntity player : this.server.getPlayerList().getPlayers()) {
			player.sendMessage(new TranslationTextComponent(Constants.MODID + ".minigame.minigame_stopped_polling",
					polling.getDefinition().getName().applyTextStyle(TextFormatting.ITALIC).applyTextStyle(TextFormatting.AQUA))
					.applyTextStyle(TextFormatting.RED), ChatType.CHAT);
		}

		return MinigameResult.ok(new TranslationTextComponent(TropicraftLangKeys.COMMAND_STOP_POLL).applyTextStyle(TextFormatting.GREEN));
	}

	@Override
	public CompletableFuture<MinigameResult<ITextComponent>> start() {
		PollingMinigameInstance polling = this.polling;

		// Check if any minigame is polling, can only start if so
		if (polling == null) {
			return CompletableFuture.completedFuture(MinigameResult.error(new TranslationTextComponent(TropicraftLangKeys.COMMAND_NO_MINIGAME_POLLING)));
		}

		return polling.start().thenApply(result -> {
			if (result.isOk()) {
				this.polling = null;
				this.currentInstance = result.getOk();
				return MinigameResult.ok(new TranslationTextComponent(TropicraftLangKeys.COMMAND_MINIGAME_STARTED).applyTextStyle(TextFormatting.GREEN));
			} else {
				return result.castError();
			}
		});
	}

	@Override
	public MinigameResult<ITextComponent> registerFor(ServerPlayerEntity player, @Nullable PlayerRole requestedRole) {
		// Check if minigame has already started
		if (this.currentInstance != null) {
			return MinigameResult.error(new TranslationTextComponent(TropicraftLangKeys.COMMAND_MINIGAME_ALREADY_STARTED));
		}

		PollingMinigameInstance polling = this.polling;
		if (polling == null) {
			return MinigameResult.error(new TranslationTextComponent(TropicraftLangKeys.COMMAND_NO_MINIGAME_POLLING));
		}

		return polling.registerPlayerAs(player, requestedRole);
	}

	@Override
	public MinigameResult<ITextComponent> unregisterFor(ServerPlayerEntity player) {
		PollingMinigameInstance polling = this.polling;
		if (polling == null) {
			return MinigameResult.error(new TranslationTextComponent(TropicraftLangKeys.COMMAND_NO_MINIGAME_POLLING));
		}

		return polling.unregisterPlayer(player);
	}

	@SubscribeEvent
	public void onChunkLoad(ChunkDataEvent.Load event) {
		// TODO: WE DON'T NEED THIS PAST 1.15, HACKY DATA FIXING
		if (event.getStatus() == ChunkStatus.Type.LEVELCHUNK && this.currentInstance != null) {
			final IWorld world = event.getWorld();

			if (world != null) {
				final DimensionType dimensionType = world.getDimension().getType();

				if (dimensionType == this.currentInstance.getDimension()) {
					final ListNBT entities = event.getData().getList("Entities", 10);

					for (int i1 = 0; i1 < entities.size(); ++i1) {
						CompoundNBT entityNBT = entities.getCompound(i1);
						entityNBT.putInt("Dimension", dimensionType.getId());
					}
				}
			}
		}
	}

	@SubscribeEvent
	public void onPlayerHurt(LivingHurtEvent event) {
		Entity entity = event.getEntity();
		if (entity instanceof ServerPlayerEntity) {
			MinigameInstance minigame = getMinigameFor(entity);
			if (minigame != null) {
				minigame.dispatchToBehaviors(IMinigameBehavior::onPlayerHurt, event);
			}
		}
	}

	@SubscribeEvent
	public void onAttackEntity(AttackEntityEvent event) {
		MinigameInstance minigame = getMinigameFor(event.getPlayer());
		if (minigame != null) {
			minigame.dispatchToBehaviors(IMinigameBehavior::onPlayerAttackEntity, event);
		}
	}

	@SubscribeEvent
	public void onLivingUpdate(LivingEvent.LivingUpdateEvent event) {
		LivingEntity entity = event.getEntityLiving();
		MinigameInstance minigame = getMinigameFor(entity);
		if (minigame != null) {
			if (entity instanceof ServerPlayerEntity) {
				minigame.dispatchToBehaviors(IMinigameBehavior::onPlayerUpdate, (ServerPlayerEntity) entity);
			}
			minigame.dispatchToBehaviors(IMinigameBehavior::onLivingEntityUpdate, entity);
		}
	}

	/**
	 * Funnel into minigame definition onPlayerDeath() for convenience
	 */
	@SubscribeEvent
	public void onPlayerDeath(LivingDeathEvent event) {
		LivingEntity entity = event.getEntityLiving();
		if (entity instanceof ServerPlayerEntity) {
			MinigameInstance minigame = getMinigameFor(entity);
			if (minigame != null) {
				minigame.dispatchToBehaviors((b, m) -> b.onPlayerDeath(m, (ServerPlayerEntity) entity, event));
			}
		}
	}

	/**
	 * Funnel into definition onPlayerRespawn() for convenience.
	 * <p>
	 * Also set player's respawn position defined by the minigame definition.
	 */
	@SubscribeEvent
	public void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
		MinigameInstance minigame = getMinigameFor(event.getPlayer());
		if (minigame != null) {
			minigame.dispatchToBehaviors(IMinigameBehavior::onPlayerRespawn, (ServerPlayerEntity) event.getPlayer());
		}
	}

	/**
	 * Funnel into definition worldUpdate() for convenience
	 */
	@SubscribeEvent
	public void onWorldTick(TickEvent.WorldTickEvent event) {
		if (event.phase == TickEvent.Phase.END) {
			MinigameInstance minigame = this.currentInstance;
			if (minigame != null && event.world.getDimension().getType() == minigame.getDimension()) {
				dispatchOrCancel(minigame, IMinigameBehavior::worldUpdate, event.world);
				minigame.update();
			}
		}
	}

	/**
	 * When a player logs out, remove them from the currently running minigame instance
	 * if they are inside, and teleport back them to their original state.
	 * <p>
	 * Also if they have registered for a minigame poll, they will be removed from the
	 * list of registered players.
	 */
	@SubscribeEvent
	public void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
		ServerPlayerEntity player = (ServerPlayerEntity) event.getPlayer();

		MinigameInstance minigame = this.currentInstance;
		if (minigame != null) {
			minigame.removePlayer(player);
		}

		PollingMinigameInstance polling = this.polling;
		if (polling != null) {
			polling.unregisterPlayer(player);
		}
	}

	@SubscribeEvent
	public void onPlayerInteractEntity(PlayerInteractEvent.EntityInteract event) {
		MinigameInstance minigame = getMinigameFor(event.getPlayer());
		if (minigame != null) {
			ServerPlayerEntity player = (ServerPlayerEntity) event.getPlayer();
			dispatchOrCancel(minigame, (b, m) -> b.onPlayerInteractEntity(m, player, event.getTarget(), event.getHand()));
		}
	}

	@SubscribeEvent
	public void onPlayerLeftClickBlock(PlayerInteractEvent.LeftClickBlock event) {
		MinigameInstance minigame = getMinigameFor(event.getPlayer());
		if (minigame != null) {
			ServerPlayerEntity player = (ServerPlayerEntity) event.getPlayer();
			dispatchOrCancel(minigame, (b, m) -> b.onPlayerLeftClickBlock(m, player, event.getPos(), event));
		}
	}

	private <A> void dispatchOrCancel(IMinigameInstance minigame, TriConsumer<IMinigameBehavior, IMinigameInstance, A> action, A argument) {
		MinigameResult<Unit> result = minigame.dispatchToBehaviors(action, argument);
		if (result.isError()) {
			cancel();
		}
	}

	private void dispatchOrCancel(IMinigameInstance minigame, BiConsumer<IMinigameBehavior, IMinigameInstance> action) {
		MinigameResult<Unit> result = minigame.dispatchToBehaviors(action);
		if (result.isError()) {
			cancel();
		}
	}

	@Nullable
	private MinigameInstance getMinigameFor(Entity entity) {
		MinigameInstance minigame = this.currentInstance;
		if (minigame == null) return null;

		if (entity.world.isRemote) return null;

		if (entity instanceof ServerPlayerEntity) {
			return minigame.getPlayers().contains(entity) ? minigame : null;
		} else {
			return entity.dimension == minigame.getDimension() ? minigame : null;
		}
	}

	@Override
	public void addControlCommand(String name, ControlCommandHandler task) {
		if (currentInstance != null) {
			currentInstance.addControlCommand(name, task);
		}

		if (polling != null) {
			polling.addControlCommand(name, task);
		}
	}

	@Override
	public void invokeControlCommand(String name, CommandSource source) throws CommandSyntaxException {
		if (currentInstance != null) {
			currentInstance.invokeControlCommand(name, source);
		}

		if (polling != null) {
			polling.invokeControlCommand(name, source);
		}
	}

	@Override
	public Set<String> getControlCommands() {
		if (currentInstance != null) {
			return currentInstance.getControlCommands();
		}

		if (polling != null) {
			return polling.getControlCommands();
		}

		return ImmutableSet.of();
	}
}
