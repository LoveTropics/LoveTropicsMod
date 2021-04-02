package com.lovetropics.minigames.common.core.game;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.lovetropics.minigames.Constants;
import com.lovetropics.minigames.LoveTropics;
import com.lovetropics.minigames.client.data.LoveTropicsLangKeys;
import com.lovetropics.minigames.client.minigame.ClientMinigameMessage;
import com.lovetropics.minigames.client.minigame.PlayerCountsMessage;
import com.lovetropics.minigames.common.core.game.behavior.event.GameLifecycleEvents;
import com.lovetropics.minigames.common.core.game.behavior.event.GamePollingEvents;
import com.lovetropics.minigames.common.core.game.control.ControlCommandInvoker;
import com.lovetropics.minigames.common.core.game.polling.PollingGameInstance;
import com.lovetropics.minigames.common.core.game.statistics.PlayerKey;
import com.lovetropics.minigames.common.core.integration.Telemetry;
import com.lovetropics.minigames.common.core.network.LoveTropicsNetwork;
import net.minecraft.command.CommandSource;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.PlayerList;
import net.minecraft.util.Unit;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.network.PacketDistributor;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Standard implementation of a minigame manager. Would prefer to do something other
 * than singleton-style implementation to allow for multiple managers to run multiple
 * minigames at once.
 */
// TODO: support multiple concurrent minigames
// TODO: a wrapper type for game instances where changing polling->active just means changing the inner mutable state?
@Mod.EventBusSubscriber(modid = Constants.MODID)
public class SingleGameManager implements IGameManager {
	static final SingleGameManager INSTANCE = new SingleGameManager();

	/**
	 * Current instance of the minigame. Agnostic from minigame definition and used to store
	 * the players and spectators that are a part of the current minigame.
	 * <p>
	 * Is null when there is no minigame running. Isn't set until a game has finished polling
	 * and has started.
	 */
	private GameInstance activeGame;
	/**
	 * Currently polling game. Is null when there is no minigame polling or a minigame is running.
	 */
	private PollingGameInstance pollingGame;

	private GameResult<Unit> stop(IGameInstance game) {
		if (this.activeGame != game) {
			return GameResult.error(new TranslationTextComponent(LoveTropicsLangKeys.COMMAND_NO_MINIGAME));
		}
		this.activeGame = null;

		try {
			try {
				game.invoker(GameLifecycleEvents.STOP).stop(game);
			} catch (Exception e) {
				return GameResult.fromException("Failed to dispatch stop event", e);
			}

			List<ServerPlayerEntity> players = Lists.newArrayList(game.getAllPlayers());
			for (ServerPlayerEntity player : players) {
				game.removePlayer(player);
			}

			PlayerSet.ofServer(game.getServer()).sendMessage(GameMessages.forGame(game).finished());

			try {
				game.invoker(GameLifecycleEvents.POST_STOP).stop(game);
			} catch (Exception e) {
				return GameResult.fromException("Failed to dispatch post stop event", e);
			}

			return GameResult.ok();
		} catch (Exception e) {
			return GameResult.fromException("Unknown error stopping game", e);
		} finally {
			game.close();
			LoveTropicsNetwork.CHANNEL.send(PacketDistributor.ALL.noArg(), new ClientMinigameMessage());
		}
	}

	@Override
	public GameResult<Unit> finish(IGameInstance game) {
		GameResult<Unit> result = stop(game);
		if (result.isOk()) {
			game.getTelemetry().finish(game.getStatistics());
		} else {
			game.getTelemetry().cancel();
		}

		try {
			game.invoker(GameLifecycleEvents.FINISH).stop(game);
		} catch (Exception e) {
			return GameResult.fromException("Failed to dispatch finish event", e);
		}

		return result;
	}

	@Override
	public GameResult<Unit> cancel(IGameInstance game) {
		game.getTelemetry().cancel();

		try {
			game.invoker(GameLifecycleEvents.CANCEL).stop(game);
		} catch (Exception e) {
			return GameResult.fromException("Failed to dispatch cancel event", e);
		}

		return stop(game);
	}

	@Override
	public GameResult<PollingGameInstance> startPolling(IGameDefinition game, ServerPlayerEntity initiator) {
		// Make sure there isn't a currently running minigame
		if (this.activeGame != null) {
			return GameResult.error(new TranslationTextComponent(LoveTropicsLangKeys.COMMAND_MINIGAME_ALREADY_STARTED));
		}

		// Make sure another minigame isn't already polling
		if (this.pollingGame != null) {
			return GameResult.error(new TranslationTextComponent(LoveTropicsLangKeys.COMMAND_ANOTHER_MINIGAME_POLLING));
		}

		GameResult<PollingGameInstance> pollResult = PollingGameInstance.create(initiator.server, game, PlayerKey.from(initiator));
		if (pollResult.isError()) {
			return pollResult.castError();
		}

		PollingGameInstance polling = pollResult.getOk();

		try {
			polling.getEvents().invoker(GamePollingEvents.START).start(polling);
		} catch (Exception e) {
			return GameResult.fromException("Failed to dispatch polling start event", e);
		}

		PlayerSet.ofServer(initiator.server).sendMessage(GameMessages.forGame(game).startPolling());

		this.pollingGame = polling;
		LoveTropicsNetwork.CHANNEL.send(PacketDistributor.ALL.noArg(), new ClientMinigameMessage(this.pollingGame));

		if (!Telemetry.INSTANCE.isConnected()) {
			ITextComponent warning = new StringTextComponent("Warning: Minigame telemetry websocket is not connected!")
					.mergeStyle(TextFormatting.RED, TextFormatting.BOLD);
			sendWarningToOperators(initiator.server, warning);
		}

		return GameResult.ok(polling);
	}

	private void sendWarningToOperators(MinecraftServer server, ITextComponent warning) {
		PlayerList playerList = server.getPlayerList();
		for (ServerPlayerEntity player : playerList.getPlayers()) {
			if (playerList.canSendCommands(player.getGameProfile())) {
				player.sendStatusMessage(warning, false);
			}
		}
	}

	@Override
	public GameResult<Unit> stopPolling(PollingGameInstance game) {
		if (this.pollingGame != game) {
			return GameResult.error(new TranslationTextComponent(LoveTropicsLangKeys.COMMAND_NO_MINIGAME_POLLING));
		}
		this.pollingGame = null;

		GameMessages gameMessages = GameMessages.forGame(game);
		PlayerSet.ofServer(game.getServer()).sendMessage(gameMessages.stopPolling());

		LoveTropicsNetwork.CHANNEL.send(PacketDistributor.ALL.noArg(), new ClientMinigameMessage());

		return GameResult.ok();
	}

	@Override
	public CompletableFuture<GameResult<IGameInstance>> start(PollingGameInstance game) {
		if (this.pollingGame != game) {
			return CompletableFuture.completedFuture(GameResult.error(new TranslationTextComponent(LoveTropicsLangKeys.COMMAND_NO_MINIGAME_POLLING)));
		}

		return game.start().thenApply(result -> {
			if (result.isOk()) {
				GameInstance resultGame = result.getOk();
				this.pollingGame = null;
				this.activeGame = resultGame;
				LoveTropicsNetwork.CHANNEL.send(PacketDistributor.ALL.noArg(), new ClientMinigameMessage(this.activeGame));
				return GameResult.ok(resultGame);
			} else {
				return result.castError();
			}
		});
	}

	@Nullable
	@Override
	public IGameInstance getGameFor(PlayerEntity player) {
		GameInstance game = this.activeGame;
		if (game == null || player.world.isRemote) return null;

		return game.getAllPlayers().contains(player) ? game : null;
	}

	@Nullable
	@Override
	public IGameInstance getGameFor(Entity entity) {
		if (entity instanceof PlayerEntity) {
			return getGameFor((PlayerEntity) entity);
		}

		GameInstance game = this.activeGame;
		if (game == null || entity.world.isRemote) return null;

		return entity.world.getDimensionKey() == game.getDimension() ? game : null;
	}

	@Nullable
	@Override
	public IGameInstance getGameAt(World world, BlockPos pos) {
		GameInstance game = this.activeGame;
		if (game != null && !world.isRemote && game.getDimension() == world.getDimensionKey()) {
			return game;
		}
		return null;
	}

	@Override
	public Collection<ProtoGameInstance> getAllGames() {
		if (activeGame != null) {
			return ImmutableList.of(activeGame);
		} else if (pollingGame != null) {
			return ImmutableList.of(pollingGame);
		}
		return null;
	}

	@Nullable
	@Override
	public ProtoGameInstance getGameById(String id) {
		if (activeGame.getInstanceId().equals(id)) {
			return activeGame;
		} else if (pollingGame.getInstanceId().equals(id)) {
			return pollingGame;
		}
		return null;
	}

	@Override
	public ControlCommandInvoker getControlInvoker(CommandSource source) {
		IGameInstance game = getGameFor(source);
		if (game != null) {
			return game.getControlCommands();
		} else if (pollingGame != null) {
			// TODO
			return pollingGame.getControlCommands();
		}
		return ControlCommandInvoker.EMPTY;
	}

	@Override
	public void close() {
		if (activeGame != null) {
			cancel(activeGame);
		}
		if (pollingGame != null) {
			stopPolling(pollingGame);
		}
	}

	// TODO: should these be separated from the specific game manager instance?
	@SubscribeEvent
	public static void onServerTick(TickEvent.ServerTickEvent event) {
		if (event.phase == TickEvent.Phase.END) {
			GameInstance game = INSTANCE.activeGame;
			if (game != null) {
				try {
					game.invoker(GameLifecycleEvents.TICK).tick(game);
				} catch (Exception e) {
					LoveTropics.LOGGER.warn("Failed to dispatch world tick event", e);
					INSTANCE.cancel(game);
				}
			}
		}
	}

	@SubscribeEvent
	public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
		ProtoGameInstance game = INSTANCE.pollingGame;
		if (game == null) {
			game = INSTANCE.activeGame;
		}

		if (game != null) {
			PacketDistributor.PacketTarget target = PacketDistributor.PLAYER.with(() -> (ServerPlayerEntity) event.getPlayer());
			LoveTropicsNetwork.CHANNEL.send(target, new ClientMinigameMessage(game));
			for (PlayerRole role : PlayerRole.ROLES) {
				LoveTropicsNetwork.CHANNEL.send(target, new PlayerCountsMessage(role, game.getMemberCount(role)));
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
	public static void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
		ServerPlayerEntity player = (ServerPlayerEntity) event.getPlayer();

		GameInstance game = INSTANCE.activeGame;
		if (game != null) {
			game.removePlayer(player);
		}

		PollingGameInstance polling = INSTANCE.pollingGame;
		if (polling != null) {
			polling.removePlayer(player);
		}
	}
}
