package com.lovetropics.minigames.common.core.game;

import com.google.common.collect.Lists;
import com.lovetropics.minigames.LoveTropics;
import com.lovetropics.minigames.client.data.TropicraftLangKeys;
import com.lovetropics.minigames.client.minigame.ClientMinigameMessage;
import com.lovetropics.minigames.client.minigame.ClientRoleMessage;
import com.lovetropics.minigames.client.minigame.PlayerCountsMessage;
import com.lovetropics.minigames.common.core.game.behavior.event.*;
import com.lovetropics.minigames.common.core.game.config.GameConfigs;
import com.lovetropics.minigames.common.core.game.polling.PollingGameInstance;
import com.lovetropics.minigames.common.core.game.statistics.PlayerKey;
import com.lovetropics.minigames.common.core.integration.Telemetry;
import com.lovetropics.minigames.common.core.network.LoveTropicsNetwork;
import com.lovetropics.minigames.common.util.Scheduler;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.command.CommandSource;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.PlayerList;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Util;
import net.minecraft.util.text.*;
import net.minecraft.util.text.event.ClickEvent;
import net.minecraft.util.text.event.HoverEvent;
import net.minecraft.world.IServerWorld;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraft.world.chunk.IChunk;
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

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

/**
 * Standard implementation of a minigame manager. Would prefer to do something other
 * than singleton-style implementation to allow for multiple managers to run multiple
 * minigames at once.
 */
public class GameManager implements IGameManager {
	/**
	 * Singleton instance that persists throughout server lifecycle.
	 */
	private static IGameManager instance;

	/**
	 * Current instance of the minigame. Agnostic from minigame definition and used to store
	 * the players and spectators that are a part of the current minigame.
	 * <p>
	 * Is null when there is no minigame running. Isn't set until a game has finished polling
	 * and has started.
	 */
	private GameInstance currentInstance;

	/**
	 * Currently polling game. Is null when there is no minigame polling or a minigame is running.
	 */
	private PollingGameInstance polling;

	/**
	 * Server reference to fetch players from player list.
	 */
	private final MinecraftServer server;

	private GameManager(MinecraftServer server) {
		this.server = server;
	}

	/**
	 * Initialize the MinigameManager singleton. Registers itself for forge events
	 * and registers some minigame definitions.
	 *
	 * @param server The minecraft server used for fetching player list.
	 */
	public static void init(MinecraftServer server) {
		if (instance != null) {
			MinecraftForge.EVENT_BUS.unregister(instance);
		}
		instance = new GameManager(server);
		MinecraftForge.EVENT_BUS.register(instance);
	}

	/**
	 * Returns null if init() has not been called yet. Shouldn't be called
	 * before the server has started.
	 *
	 * @return The global instance of the minigame manager.
	 */
	public static IGameManager get() {
		return instance;
	}

	@Override
	public Collection<IGameDefinition> getAllMinigames() {
		return Collections.unmodifiableCollection(GameConfigs.GAME_CONFIGS.values());
	}

	@Override
	public IGameInstance getActiveMinigame() {
		return this.currentInstance;
	}

	private GameResult<ITextComponent> close() {
		GameInstance game = this.currentInstance;
		this.currentInstance = null;

		if (game == null) {
			return GameResult.error(new TranslationTextComponent(TropicraftLangKeys.COMMAND_NO_MINIGAME));
		}

		try {
			IGameDefinition definition = game.getDefinition();

			try {
				game.events().invoker(GameLifecycleEvents.FINISH).stop(game);
			} catch (Exception e) {
				return GameResult.fromException("Failed to dispatch finish event", e);
			}

			List<ServerPlayerEntity> players = Lists.newArrayList(game.getPlayers());
			for (ServerPlayerEntity player : players) {
				game.removePlayer(player);
			}

			// Send all players a message letting them know the minigame has finished
			for (ServerPlayerEntity player : this.server.getPlayerList().getPlayers()) {
				IFormattableTextComponent message = new TranslationTextComponent(TropicraftLangKeys.COMMAND_FINISHED_MINIGAME,
						definition.getName().deepCopy().mergeStyle(TextFormatting.ITALIC)).mergeStyle(TextFormatting.AQUA)
						.mergeStyle(TextFormatting.GOLD);
				player.sendStatusMessage(message, false);
			}

			try {
				game.events().invoker(GameLifecycleEvents.POST_FINISH).stop(game);
			} catch (Exception e) {
				return GameResult.fromException("Failed to dispatch post finish event", e);
			}

			ITextComponent minigameName = definition.getName().deepCopy().mergeStyle(TextFormatting.AQUA);
			return GameResult.ok(new TranslationTextComponent(TropicraftLangKeys.COMMAND_STOPPED_MINIGAME, minigameName).mergeStyle(TextFormatting.GREEN));
		} catch (Exception e) {
			return GameResult.fromException("Unknown error finishing minigame", e);
		} finally {
			game.close();
			LoveTropicsNetwork.CHANNEL.send(PacketDistributor.ALL.noArg(), new ClientMinigameMessage());
		}
	}

	@Override
	public GameResult<ITextComponent> finish() {
		GameInstance game = currentInstance;
		if (game == null) {
			return GameResult.error(new TranslationTextComponent(TropicraftLangKeys.COMMAND_NO_MINIGAME));
		}

		GameResult<ITextComponent> result = close();
		if (result.isOk()) {
			game.getTelemetry().finish(game.getStatistics());
		} else {
			game.getTelemetry().cancel();
		}

		return result;
	}

	@Override
	public GameResult<ITextComponent> cancel() {
		GameInstance game = this.currentInstance;
		if (game == null) {
			return GameResult.error(new TranslationTextComponent(TropicraftLangKeys.COMMAND_NO_MINIGAME));
		}

		game.getTelemetry().cancel();

		try {
			game.events().invoker(GameLifecycleEvents.CANCEL).stop(game);
		} catch (Exception e) {
			return GameResult.fromException("Failed to dispatch cancel event", e);
		}

		return close();
	}

	@Override
	public GameResult<ITextComponent> startPolling(ResourceLocation gameId, PlayerKey initiator) {
		// Make sure minigame is registered with provided id
		if (!GameConfigs.GAME_CONFIGS.containsKey(gameId)) {
			return GameResult.error(new TranslationTextComponent(TropicraftLangKeys.COMMAND_MINIGAME_ID_INVALID));
		}

		// Make sure there isn't a currently running minigame
		if (this.currentInstance != null) {
			return GameResult.error(new TranslationTextComponent(TropicraftLangKeys.COMMAND_MINIGAME_ALREADY_STARTED));
		}

		// Make sure another minigame isn't already polling
		if (this.polling != null) {
			return GameResult.error(new TranslationTextComponent(TropicraftLangKeys.COMMAND_ANOTHER_MINIGAME_POLLING));
		}

		IGameDefinition definition = GameConfigs.GAME_CONFIGS.get(gameId);

		GameResult<PollingGameInstance> pollResult = PollingGameInstance.create(server, definition, initiator);
		if (pollResult.isError()) {
			return pollResult.castError();
		}

		PollingGameInstance polling = pollResult.getOk();

		try {
			polling.events().invoker(GamePollingEvents.START).start(polling);
		} catch (Exception e) {
			return GameResult.fromException("Failed to dispatch polling start event", e);
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
		LoveTropicsNetwork.CHANNEL.send(PacketDistributor.ALL.noArg(), new ClientMinigameMessage(this.polling));

		if (!Telemetry.INSTANCE.isConnected()) {
			ITextComponent warning = new StringTextComponent("Warning: Minigame telemetry websocket is not connected!")
					.mergeStyle(TextFormatting.RED, TextFormatting.BOLD);
			sendWarningToOperators(warning);
		}

		return GameResult.ok(new TranslationTextComponent(TropicraftLangKeys.COMMAND_MINIGAME_POLLED));
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
	public GameResult<ITextComponent> stopPolling() {
		PollingGameInstance polling = this.polling;

		// Check if a minigame is polling
		if (polling == null) {
			return GameResult.error(new TranslationTextComponent(TropicraftLangKeys.COMMAND_NO_MINIGAME_POLLING));
		}

		this.polling = null;

		ITextComponent name = polling.getDefinition().getName().deepCopy().mergeStyle(TextFormatting.ITALIC, TextFormatting.AQUA);
		ITextComponent message = new TranslationTextComponent(TropicraftLangKeys.COMMAND_MINIGAME_STOPPED_POLLING, name).mergeStyle(TextFormatting.RED);
		server.getPlayerList().func_232641_a_(message, ChatType.SYSTEM, Util.DUMMY_UUID);

		LoveTropicsNetwork.CHANNEL.send(PacketDistributor.ALL.noArg(), new ClientMinigameMessage());
		return GameResult.ok(new TranslationTextComponent(TropicraftLangKeys.COMMAND_STOP_POLL).mergeStyle(TextFormatting.GREEN));
	}

	@Override
	public CompletableFuture<GameResult<ITextComponent>> start() {
		PollingGameInstance polling = this.polling;

		// Check if any minigame is polling, can only start if so
		if (polling == null) {
			return CompletableFuture.completedFuture(GameResult.error(new TranslationTextComponent(TropicraftLangKeys.COMMAND_NO_MINIGAME_POLLING)));
		}

		return polling.start().thenApply(result -> {
			if (result.isOk()) {
				this.polling = null;
				this.currentInstance = result.getOk();
				LoveTropicsNetwork.CHANNEL.send(PacketDistributor.ALL.noArg(), new ClientMinigameMessage(this.currentInstance));
				return GameResult.ok(new TranslationTextComponent(TropicraftLangKeys.COMMAND_MINIGAME_STARTED).mergeStyle(TextFormatting.GREEN));
			} else {
				return result.castError();
			}
		});
	}

	@Override
	public GameResult<ITextComponent> joinPlayerAs(ServerPlayerEntity player, @Nullable PlayerRole requestedRole) {
		GameInstance minigame = this.currentInstance;
		if (minigame != null && !minigame.getPlayers().contains(player)) {
			minigame.addPlayer(player, PlayerRole.SPECTATOR);
			LoveTropicsNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), new ClientRoleMessage(PlayerRole.SPECTATOR));
			LoveTropicsNetwork.CHANNEL.send(PacketDistributor.ALL.noArg(), new PlayerCountsMessage(PlayerRole.SPECTATOR, minigame.getMemberCount(PlayerRole.SPECTATOR)));
			return GameResult.ok(new StringTextComponent("You have joined the game as a spectator!").mergeStyle(TextFormatting.GREEN));
		}

		PollingGameInstance polling = this.polling;
		if (polling == null) {
			return GameResult.error(new TranslationTextComponent(TropicraftLangKeys.COMMAND_NO_MINIGAME_POLLING));
		}

		// Client state is updated within this method to allow preconditions to be checked there
		return polling.joinPlayerAs(player, requestedRole);
	}

	@Override
	public GameResult<ITextComponent> removePlayer(ServerPlayerEntity player) {
		GameInstance minigame = this.currentInstance;
		if (minigame != null && minigame.removePlayer(player)) {
			ITextComponent minigameName = minigame.getDefinition().getName();
			return GameResult.ok(new TranslationTextComponent(TropicraftLangKeys.COMMAND_UNREGISTERED_MINIGAME, minigameName).mergeStyle(TextFormatting.RED));
		}

		PollingGameInstance polling = this.polling;
		if (polling != null) {
			// Client state is updated within this method to allow preconditions to be checked there
			return polling.removePlayer(player);
		}

		return GameResult.error(new TranslationTextComponent(TropicraftLangKeys.COMMAND_NO_MINIGAME));
	}

	@SubscribeEvent
	public void onChunkLoad(ChunkEvent.Load event) {
		GameInstance game = this.currentInstance;
		if (game == null) return;

		IWorld world = event.getWorld();
		if (world instanceof IServerWorld) {
			RegistryKey<World> dimensionType = ((IServerWorld) world).getWorld().getDimensionKey();
			if (dimensionType == game.getDimension()) {
				IChunk chunk = event.getChunk();
				Scheduler.INSTANCE.submit(s -> {
					if (this.currentInstance != game) return;
					game.events().invoker(GameWorldEvents.CHUNK_LOAD).onChunkLoad(game, chunk);
				});
			}
		}
	}

	@SubscribeEvent
	public void onPlayerHurt(LivingHurtEvent event) {
		Entity entity = event.getEntity();
		if (entity instanceof ServerPlayerEntity) {
			GameInstance game = getGameFor(entity);
			if (game != null) {
				try {
					ActionResultType result = game.events().invoker(GamePlayerEvents.DAMAGE).onDamage(game, (ServerPlayerEntity) entity, event.getSource(), event.getAmount());
					if (result == ActionResultType.FAIL) {
						event.setCanceled(true);
					}
				} catch (Exception e) {
					LoveTropics.LOGGER.warn("Failed to dispatch player hurt event", e);
				}
			}
		}
	}

	@SubscribeEvent
	public void onAttackEntity(AttackEntityEvent event) {
		GameInstance game = getGameFor(event.getPlayer());
		if (game != null && event.getPlayer() instanceof ServerPlayerEntity) {
			try {
				ActionResultType result = game.events().invoker(GamePlayerEvents.ATTACK).onAttack(game, (ServerPlayerEntity) event.getPlayer(), event.getTarget());
				if (result == ActionResultType.FAIL) {
					event.setCanceled(true);
				}
			} catch (Exception e) {
				LoveTropics.LOGGER.warn("Failed to dispatch player attack event", e);
			}
		}
	}

	@SubscribeEvent
	public void onLivingUpdate(LivingEvent.LivingUpdateEvent event) {
		LivingEntity entity = event.getEntityLiving();
		GameInstance game = getGameFor(entity);
		if (game != null) {
			if (entity instanceof ServerPlayerEntity && game.getParticipants().contains(entity)) {
				try {
					game.events().invoker(GamePlayerEvents.TICK).tick(game, (ServerPlayerEntity) entity);
				} catch (Exception e) {
					LoveTropics.LOGGER.warn("Failed to dispatch player tick event", e);
				}
			}

			try {
				game.events().invoker(GameLivingEntityEvents.TICK).tick(game, entity);
			} catch (Exception e) {
				LoveTropics.LOGGER.warn("Failed to dispatch living tick event", e);
			}
		}
	}

	@SubscribeEvent
	public void onPlayerDeath(LivingDeathEvent event) {
		LivingEntity entity = event.getEntityLiving();
		if (entity instanceof ServerPlayerEntity) {
			GameInstance game = getGameFor(entity);
			if (game != null) {
				try {
					ActionResultType result = game.events().invoker(GamePlayerEvents.DEATH).onDeath(game, (ServerPlayerEntity) entity, event.getSource());
					if (result == ActionResultType.FAIL) {
						event.setCanceled(true);
					}
				} catch (Exception e) {
					LoveTropics.LOGGER.warn("Failed to dispatch player death event", e);
				}
			}
		}
	}

	@SubscribeEvent
	public void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
		GameInstance game = getGameFor(event.getPlayer());
		if (game != null) {
			try {
				game.events().invoker(GamePlayerEvents.RESPAWN).onRespawn(game, (ServerPlayerEntity) event.getPlayer());
			} catch (Exception e) {
				LoveTropics.LOGGER.warn("Failed to dispatch player respawn event", e);
			}
		}
	}

	@SubscribeEvent
	public void onWorldTick(TickEvent.WorldTickEvent event) {
		if (event.phase == TickEvent.Phase.END) {
			GameInstance game = this.currentInstance;
			if (game != null && event.world.getDimensionKey() == game.getDimension() && !event.world.isRemote) {
				try {
					game.events().invoker(GameLifecycleEvents.TICK).tick(game);
					game.update();
				} catch (Exception e) {
					LoveTropics.LOGGER.warn("Failed to dispatch world tick event", e);
					cancel();
				}
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

		GameInstance game = this.currentInstance;
		if (game != null) {
			game.removePlayer(player);
		}

		PollingGameInstance polling = this.polling;
		if (polling != null) {
			polling.removePlayer(player);
		}
	}

	@SubscribeEvent
	public void onPlayerInteractEntity(PlayerInteractEvent.EntityInteract event) {
		GameInstance game = getGameFor(event.getPlayer());
		if (game != null) {
			ServerPlayerEntity player = (ServerPlayerEntity) event.getPlayer();

			try {
				game.events().invoker(GamePlayerEvents.INTERACT_ENTITY).onInteract(game, player, event.getTarget(), event.getHand());
			} catch (Exception e) {
				LoveTropics.LOGGER.warn("Failed to dispatch player interact entity event", e);
			}
		}
	}

	@SubscribeEvent
	public void onPlayerLeftClickBlock(PlayerInteractEvent.LeftClickBlock event) {
		GameInstance game = getGameFor(event.getPlayer());
		if (game != null) {
			ServerPlayerEntity player = (ServerPlayerEntity) event.getPlayer();

			try {
				game.events().invoker(GamePlayerEvents.LEFT_CLICK_BLOCK).onLeftClickBlock(game, player, event.getPos());
			} catch (Exception e) {
				LoveTropics.LOGGER.warn("Failed to dispatch player left click block event", e);
			}
		}
	}

	@SubscribeEvent
	public void onPlayerBreakBlock(BlockEvent.BreakEvent event) {
		GameInstance game = getGameFor(event.getPlayer());
		if (game != null) {
			try {
				ServerPlayerEntity player = (ServerPlayerEntity) event.getPlayer();
				ActionResultType result = game.events().invoker(GamePlayerEvents.BREAK_BLOCK).onBreakBlock(game, player, event.getPos(), event.getState());
				if (result == ActionResultType.FAIL) {
					event.setCanceled(true);
				}
			} catch (Exception e) {
				LoveTropics.LOGGER.warn("Failed to dispatch player break block event", e);
			}
		}
	}

	@SubscribeEvent
	public void onEntityPlaceBlock(BlockEvent.EntityPlaceEvent event) {
		GameInstance game = getGameFor(event.getEntity());
		if (game != null && event.getEntity() instanceof ServerPlayerEntity) {
			try {
				ServerPlayerEntity player = (ServerPlayerEntity) event.getEntity();
				ActionResultType result = game.events().invoker(GamePlayerEvents.PLACE_BLOCK).onPlaceBlock(game, player, event.getPos(), event.getPlacedBlock(), event.getPlacedAgainst());
				if (result == ActionResultType.FAIL) {
					event.setCanceled(true);
				}
			} catch (Exception e) {
				LoveTropics.LOGGER.warn("Failed to dispatch player place block event", e);
			}
		}
	}

	@SubscribeEvent
	public void onExplosion(ExplosionEvent.Detonate event) {
		GameInstance game = this.currentInstance;
		if (game != null && event.getWorld() == game.getWorld()) {
			try {
				game.events().invoker(GameWorldEvents.EXPLOSION_DETONATE).onExplosionDetonate(game, event.getExplosion(), event.getAffectedBlocks(), event.getAffectedEntities());
			} catch (Exception e) {
				LoveTropics.LOGGER.warn("Failed to dispatch explosion event", e);
			}
		}
	}

	@SubscribeEvent
	public void onPlayerLoggedIn(PlayerLoggedInEvent event) {
		ProtoGame game = polling;
		if (game == null) {
			game = getActiveMinigame();
		}
		if (game != null) {
			PacketDistributor.PacketTarget target = PacketDistributor.PLAYER.with(() -> (ServerPlayerEntity) event.getPlayer());
			LoveTropicsNetwork.CHANNEL.send(target, new ClientMinigameMessage(game));
			for (PlayerRole role : PlayerRole.values()) {
				LoveTropicsNetwork.CHANNEL.send(target, new PlayerCountsMessage(role, game.getMemberCount(role)));
			}
		}
	}

	@Nullable
	private GameInstance getGameFor(Entity entity) {
		GameInstance game = this.currentInstance;
		if (game == null) return null;

		if (entity.world.isRemote) return null;

		if (entity instanceof ServerPlayerEntity) {
			return game.getPlayers().contains(entity) ? game : null;
		} else {
			return entity.world.getDimensionKey() == game.getDimension() ? game : null;
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
