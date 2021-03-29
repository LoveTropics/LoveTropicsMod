package com.lovetropics.minigames.common.minigames;

import com.google.common.collect.Lists;
import com.lovetropics.minigames.client.data.TropicraftLangKeys;
import com.lovetropics.minigames.client.minigame.ClientMinigameMessage;
import com.lovetropics.minigames.client.minigame.ClientRoleMessage;
import com.lovetropics.minigames.client.minigame.PlayerCountsMessage;
import com.lovetropics.minigames.common.Scheduler;
import com.lovetropics.minigames.common.minigames.behaviours.IMinigameBehavior;
import com.lovetropics.minigames.common.minigames.behaviours.IPollingMinigameBehavior;
import com.lovetropics.minigames.common.minigames.config.MinigameConfigs;
import com.lovetropics.minigames.common.minigames.polling.PollingMinigameInstance;
import com.lovetropics.minigames.common.minigames.statistics.PlayerKey;
import com.lovetropics.minigames.common.network.LTNetwork;
import com.lovetropics.minigames.common.telemetry.Telemetry;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.command.CommandSource;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.PlayerList;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Unit;
import net.minecraft.util.Util;
import net.minecraft.util.text.*;
import net.minecraft.util.text.event.ClickEvent;
import net.minecraft.util.text.event.HoverEvent;
import net.minecraft.world.IServerWorld;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraft.world.chunk.IChunk;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.PlayerLoggedInEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.event.world.ChunkEvent;
import net.minecraftforge.event.world.ExplosionEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.network.PacketDistributor;
import org.apache.logging.log4j.util.TriConsumer;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;
import java.util.stream.Stream;

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
		if (INSTANCE != null) {
			MinecraftForge.EVENT_BUS.unregister(INSTANCE);
		}
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
	public Collection<IMinigameDefinition> getAllMinigames() {
		return Collections.unmodifiableCollection(MinigameConfigs.GAME_CONFIGS.values());
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

			List<ServerPlayerEntity> players = Lists.newArrayList(minigame.getPlayers());
			for (ServerPlayerEntity player : players) {
				minigame.removePlayer(player);
			}

			// Send all players a message letting them know the minigame has finished
			for (ServerPlayerEntity player : this.server.getPlayerList().getPlayers()) {
				IFormattableTextComponent message = new TranslationTextComponent(TropicraftLangKeys.COMMAND_FINISHED_MINIGAME,
						definition.getName().deepCopy().mergeStyle(TextFormatting.ITALIC)).mergeStyle(TextFormatting.AQUA)
						.mergeStyle(TextFormatting.GOLD);
				player.sendStatusMessage(message, false);
			}

			result = minigame.dispatchToBehaviors(IMinigameBehavior::onPostFinish);
			if (result.isError()) {
				return result.castError();
			}

			ITextComponent minigameName = definition.getName().deepCopy().mergeStyle(TextFormatting.AQUA);
			return MinigameResult.ok(new TranslationTextComponent(TropicraftLangKeys.COMMAND_STOPPED_MINIGAME, minigameName).mergeStyle(TextFormatting.GREEN));
		} catch (Exception e) {
			return MinigameResult.fromException("Unknown error finishing minigame", e);
		} finally {
			minigame.close();
			LTNetwork.CHANNEL.send(PacketDistributor.ALL.noArg(), new ClientMinigameMessage());
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
			minigame.getTelemetry().finish(minigame.getStatistics());
		} else {
			minigame.getTelemetry().cancel();
		}

		return result;
	}

	@Override
	public MinigameResult<ITextComponent> cancel() {
		MinigameInstance minigame = this.currentInstance;
		if (minigame == null) {
			return MinigameResult.error(new TranslationTextComponent(TropicraftLangKeys.COMMAND_NO_MINIGAME));
		}

		minigame.getTelemetry().cancel();

		MinigameResult<Unit> result = minigame.dispatchToBehaviors(IMinigameBehavior::onCancel);
		if (result.isError()) {
			return result.castError();
		}

		return close();
	}

	@Override
	public MinigameResult<ITextComponent> startPolling(ResourceLocation minigameId, PlayerKey initiator) {
		// Make sure minigame is registered with provided id
		if (!MinigameConfigs.GAME_CONFIGS.containsKey(minigameId)) {
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

		IMinigameDefinition definition = MinigameConfigs.GAME_CONFIGS.get(minigameId);

		MinigameResult<PollingMinigameInstance> pollResult = PollingMinigameInstance.create(server, definition, initiator);
		if (pollResult.isError()) {
			return pollResult.castError();
		}

		PollingMinigameInstance polling = pollResult.getOk();

		MinigameResult<Unit> result = polling.dispatchToBehaviors(IPollingMinigameBehavior::onStartPolling);
		if (result.isError()) {
			return result.castError();
		}

		ITextComponent name = definition.getName().deepCopy().mergeStyle(TextFormatting.ITALIC, TextFormatting.AQUA);
		Style linkStyle = Style.EMPTY
				.setUnderlined(true)
				.setFormatting(TextFormatting.BLUE)
				.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/minigame join"))
				.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new StringTextComponent("Join this minigame")));

		ITextComponent link = new StringTextComponent("/minigame join").setStyle(linkStyle);
		ITextComponent message = new TranslationTextComponent(TropicraftLangKeys.COMMAND_MINIGAME_POLLING, name, link)
				.mergeStyle(TextFormatting.GOLD);

		server.getPlayerList().func_232641_a_(message, ChatType.SYSTEM, Util.DUMMY_UUID);

		this.polling = polling;
		LTNetwork.CHANNEL.send(PacketDistributor.ALL.noArg(), new ClientMinigameMessage(this.polling));

		if (!Telemetry.INSTANCE.isConnected()) {
			ITextComponent warning = new StringTextComponent("Warning: Minigame telemetry websocket is not connected!")
					.mergeStyle(TextFormatting.RED, TextFormatting.BOLD);
			sendWarningToOperators(warning);
		}

		return MinigameResult.ok(new TranslationTextComponent(TropicraftLangKeys.COMMAND_MINIGAME_POLLED));
	}

	private void sendWarningToOperators(ITextComponent warning) {
		PlayerList playerList = server.getPlayerList();
		for (ServerPlayerEntity player : playerList.getPlayers()) {
			if (playerList.canSendCommands(player.getGameProfile())) {
				player.sendStatusMessage(warning, false);
			}
		}
	}

	@Override
	public MinigameResult<ITextComponent> stopPolling() {
		PollingMinigameInstance polling = this.polling;

		// Check if a minigame is polling
		if (polling == null) {
			return MinigameResult.error(new TranslationTextComponent(TropicraftLangKeys.COMMAND_NO_MINIGAME_POLLING));
		}

		this.polling = null;

		ITextComponent name = polling.getDefinition().getName().deepCopy().mergeStyle(TextFormatting.ITALIC, TextFormatting.AQUA);
		ITextComponent message = new TranslationTextComponent(TropicraftLangKeys.COMMAND_MINIGAME_STOPPED_POLLING, name).mergeStyle(TextFormatting.RED);
		server.getPlayerList().func_232641_a_(message, ChatType.SYSTEM, Util.DUMMY_UUID);

		LTNetwork.CHANNEL.send(PacketDistributor.ALL.noArg(), new ClientMinigameMessage());
		return MinigameResult.ok(new TranslationTextComponent(TropicraftLangKeys.COMMAND_STOP_POLL).mergeStyle(TextFormatting.GREEN));
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
				LTNetwork.CHANNEL.send(PacketDistributor.ALL.noArg(), new ClientMinigameMessage(this.currentInstance));
				return MinigameResult.ok(new TranslationTextComponent(TropicraftLangKeys.COMMAND_MINIGAME_STARTED).mergeStyle(TextFormatting.GREEN));
			} else {
				return result.castError();
			}
		});
	}

	@Override
	public MinigameResult<ITextComponent> joinPlayerAs(ServerPlayerEntity player, @Nullable PlayerRole requestedRole) {
		MinigameInstance minigame = this.currentInstance;
		if (minigame != null && !minigame.getPlayers().contains(player)) {
			minigame.addPlayer(player, PlayerRole.SPECTATOR);
			LTNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), new ClientRoleMessage(PlayerRole.SPECTATOR));
			LTNetwork.CHANNEL.send(PacketDistributor.ALL.noArg(), new PlayerCountsMessage(PlayerRole.SPECTATOR, minigame.getMemberCount(PlayerRole.SPECTATOR)));
			return MinigameResult.ok(new StringTextComponent("You have joined the game as a spectator!").mergeStyle(TextFormatting.GREEN));
		}

		PollingMinigameInstance polling = this.polling;
		if (polling == null) {
			return MinigameResult.error(new TranslationTextComponent(TropicraftLangKeys.COMMAND_NO_MINIGAME_POLLING));
		}

		// Client state is updated within this method to allow preconditions to be checked there
		return polling.joinPlayerAs(player, requestedRole);
	}

	@Override
	public MinigameResult<ITextComponent> removePlayer(ServerPlayerEntity player) {
		MinigameInstance minigame = this.currentInstance;
		if (minigame != null && minigame.removePlayer(player)) {
			ITextComponent minigameName = minigame.getDefinition().getName();
			return MinigameResult.ok(new TranslationTextComponent(TropicraftLangKeys.COMMAND_UNREGISTERED_MINIGAME, minigameName).mergeStyle(TextFormatting.RED));
		}

		PollingMinigameInstance polling = this.polling;
		if (polling != null) {
			// Client state is updated within this method to allow preconditions to be checked there
			return polling.removePlayer(player);
		}

		return MinigameResult.error(new TranslationTextComponent(TropicraftLangKeys.COMMAND_NO_MINIGAME));
	}

	@SubscribeEvent
	public void onChunkLoad(ChunkEvent.Load event) {
		MinigameInstance minigame = this.currentInstance;
		if (minigame != null) {
			IWorld world = event.getWorld();
			if (world instanceof IServerWorld) {
				RegistryKey<World> dimensionType = ((IServerWorld) world).getWorld().getDimensionKey();
				if (dimensionType == minigame.getDimension()) {
					IChunk chunk = event.getChunk();
					Scheduler.INSTANCE.submit(s -> {
						if (this.currentInstance == minigame) {
							minigame.dispatchToBehaviors(IMinigameBehavior::onChunkLoad, chunk);
						}
					});
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
			if (entity instanceof ServerPlayerEntity && minigame.getParticipants().contains(entity)) {
				minigame.dispatchToBehaviors(IMinigameBehavior::onParticipantUpdate, (ServerPlayerEntity) entity);
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
			if (minigame != null && event.world.getDimensionKey() == minigame.getDimension() && !event.world.isRemote) {
				dispatchOrCancel(minigame, IMinigameBehavior::worldUpdate, (ServerWorld) event.world);
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
			polling.removePlayer(player);
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

	@SubscribeEvent
	public void onPlayerBreakBlock(BlockEvent.BreakEvent event) {
		MinigameInstance minigame = getMinigameFor(event.getPlayer());
		if (minigame != null) {
			ServerPlayerEntity player = (ServerPlayerEntity) event.getPlayer();
			dispatchOrCancel(minigame, (b, m) -> b.onPlayerBreakBlock(m, player, event.getPos(), event.getState(), event));
		}
	}

	@SubscribeEvent
	public void onEntityPlaceBlock(BlockEvent.EntityPlaceEvent event) {
		MinigameInstance minigame = getMinigameFor(event.getEntity());
		if (minigame != null) {
			dispatchOrCancel(minigame, (b, m) -> b.onEntityPlaceBlock(m, event.getEntity(), event.getPos(), event.getState(), event));
		}
	}

	@SubscribeEvent
	public void onExplosion(ExplosionEvent.Detonate event) {
		MinigameInstance minigame = this.currentInstance;
		if (minigame != null) {
			dispatchOrCancel(minigame, (b, m) -> b.onExplosionDetonate(m, event));
		}
	}

	@SubscribeEvent
	public void onPlayerLoggedIn(PlayerLoggedInEvent event) {
		ProtoMinigame minigame = polling;
		if (minigame == null) {
			minigame = getActiveMinigame();
		}
		if (minigame != null) {
			PacketDistributor.PacketTarget target = PacketDistributor.PLAYER.with(() -> (ServerPlayerEntity) event.getPlayer());
			LTNetwork.CHANNEL.send(target, new ClientMinigameMessage(minigame));
			for (PlayerRole role : PlayerRole.values()) {
				LTNetwork.CHANNEL.send(target, new PlayerCountsMessage(role, minigame.getMemberCount(role)));
			}
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
			return entity.world.getDimensionKey() == minigame.getDimension() ? minigame : null;
		}
	}

	@Override
	public void addControlCommand(String name, ControlCommand command) {
		if (currentInstance != null) {
			currentInstance.addControlCommand(name, command);
		}

		if (polling != null) {
			polling.addControlCommand(name, command);
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
	public Stream<String> controlCommandsFor(CommandSource source) {
		if (currentInstance != null) {
			return currentInstance.controlCommandsFor(source);
		}

		if (polling != null) {
			return polling.controlCommandsFor(source);
		}

		return Stream.empty();
	}

	@Override
	public PlayerKey getInitiator() {
		if (currentInstance != null) {
			return currentInstance.getInitiator();
		}

		if (polling != null) {
			return polling.getInitiator();
		}

		return null;
	}
}
