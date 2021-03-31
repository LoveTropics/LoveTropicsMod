package com.lovetropics.minigames.common.core.game;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.lovetropics.minigames.Constants;
import com.lovetropics.minigames.client.data.TropicraftLangKeys;
import com.lovetropics.minigames.client.minigame.ClientMinigameMessage;
import com.lovetropics.minigames.client.minigame.ClientRoleMessage;
import com.lovetropics.minigames.client.minigame.PlayerCountsMessage;
import com.lovetropics.minigames.common.core.game.behavior.event.GameLifecycleEvents;
import com.lovetropics.minigames.common.core.game.behavior.event.GamePollingEvents;
import com.lovetropics.minigames.common.core.game.control.ControlCommandInvoker;
import com.lovetropics.minigames.common.core.game.polling.PollingGameInstance;
import com.lovetropics.minigames.common.core.game.statistics.PlayerKey;
import com.lovetropics.minigames.common.core.integration.Telemetry;
import com.lovetropics.minigames.common.core.network.LoveTropicsNetwork;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.PlayerList;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.*;
import net.minecraft.util.text.event.ClickEvent;
import net.minecraft.util.text.event.HoverEvent;
import net.minecraft.world.World;
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

	@Nullable
	@Override
	public IGameInstance getActiveGame() {
		return this.activeGame;
	}

	@Nullable
	@Override
	public PollingGameInstance getPollingGame() {
		return pollingGame;
	}

	private GameResult<ITextComponent> close(IGameInstance game) {
		if (this.activeGame != game) {
			return GameResult.error(new TranslationTextComponent(TropicraftLangKeys.COMMAND_NO_MINIGAME));
		}
		this.activeGame = null;

		try {
			IGameDefinition definition = game.getDefinition();

			try {
				game.invoker(GameLifecycleEvents.FINISH).stop(game);
			} catch (Exception e) {
				return GameResult.fromException("Failed to dispatch finish event", e);
			}

			List<ServerPlayerEntity> players = Lists.newArrayList(game.getAllPlayers());
			for (ServerPlayerEntity player : players) {
				game.removePlayer(player);
			}

			// Send all players a message letting them know the minigame has finished
			for (ServerPlayerEntity player : game.getServer().getPlayerList().getPlayers()) {
				IFormattableTextComponent message = new TranslationTextComponent(TropicraftLangKeys.COMMAND_FINISHED_MINIGAME,
						definition.getName().deepCopy().mergeStyle(TextFormatting.ITALIC)).mergeStyle(TextFormatting.AQUA)
						.mergeStyle(TextFormatting.GOLD);
				player.sendStatusMessage(message, false);
			}

			try {
				game.invoker(GameLifecycleEvents.POST_FINISH).stop(game);
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
	public GameResult<ITextComponent> finish(IGameInstance game) {
		GameResult<ITextComponent> result = close(game);
		if (result.isOk()) {
			game.getTelemetry().finish(game.getStatistics());
		} else {
			game.getTelemetry().cancel();
		}

		return result;
	}

	@Override
	public GameResult<ITextComponent> cancel(IGameInstance game) {
		game.getTelemetry().cancel();

		try {
			game.invoker(GameLifecycleEvents.CANCEL).stop(game);
		} catch (Exception e) {
			return GameResult.fromException("Failed to dispatch cancel event", e);
		}

		return close(game);
	}

	@Override
	public GameResult<ITextComponent> startPolling(IGameDefinition game, ServerPlayerEntity initiator) {
		// Make sure there isn't a currently running minigame
		if (this.activeGame != null) {
			return GameResult.error(new TranslationTextComponent(TropicraftLangKeys.COMMAND_MINIGAME_ALREADY_STARTED));
		}

		// Make sure another minigame isn't already polling
		if (this.pollingGame != null) {
			return GameResult.error(new TranslationTextComponent(TropicraftLangKeys.COMMAND_ANOTHER_MINIGAME_POLLING));
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

		ITextComponent name = game.getName().deepCopy().mergeStyle(TextFormatting.ITALIC, TextFormatting.AQUA);
		Style linkStyle = Style.EMPTY
				.setUnderlined(true)
				.setFormatting(TextFormatting.BLUE)
				.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/minigame join"))
				.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new StringTextComponent("Join this minigame")));

		ITextComponent link = new StringTextComponent("/minigame join").setStyle(linkStyle);
		ITextComponent message = new TranslationTextComponent(TropicraftLangKeys.COMMAND_MINIGAME_POLLING, name, link)
				.mergeStyle(TextFormatting.GOLD);

		initiator.server.getPlayerList().func_232641_a_(message, ChatType.SYSTEM, Util.DUMMY_UUID);

		this.pollingGame = polling;
		LoveTropicsNetwork.CHANNEL.send(PacketDistributor.ALL.noArg(), new ClientMinigameMessage(this.pollingGame));

		if (!Telemetry.INSTANCE.isConnected()) {
			ITextComponent warning = new StringTextComponent("Warning: Minigame telemetry websocket is not connected!")
					.mergeStyle(TextFormatting.RED, TextFormatting.BOLD);
			sendWarningToOperators(initiator.server, warning);
		}

		return GameResult.ok(new TranslationTextComponent(TropicraftLangKeys.COMMAND_MINIGAME_POLLED));
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
	public GameResult<ITextComponent> stopPolling(PollingGameInstance game) {
		if (this.pollingGame != game) {
			return GameResult.error(new TranslationTextComponent(TropicraftLangKeys.COMMAND_NO_MINIGAME_POLLING));
		}
		this.pollingGame = null;

		ITextComponent name = game.getDefinition().getName().deepCopy().mergeStyle(TextFormatting.ITALIC, TextFormatting.AQUA);
		ITextComponent message = new TranslationTextComponent(TropicraftLangKeys.COMMAND_MINIGAME_STOPPED_POLLING, name).mergeStyle(TextFormatting.RED);
		game.getServer().getPlayerList().func_232641_a_(message, ChatType.SYSTEM, Util.DUMMY_UUID);

		LoveTropicsNetwork.CHANNEL.send(PacketDistributor.ALL.noArg(), new ClientMinigameMessage());
		return GameResult.ok(new TranslationTextComponent(TropicraftLangKeys.COMMAND_STOP_POLL).mergeStyle(TextFormatting.GREEN));
	}

	@Override
	public CompletableFuture<GameResult<ITextComponent>> start(PollingGameInstance game) {
		if (this.pollingGame != game) {
			return CompletableFuture.completedFuture(GameResult.error(new TranslationTextComponent(TropicraftLangKeys.COMMAND_NO_MINIGAME_POLLING)));
		}

		return game.start().thenApply(result -> {
			if (result.isOk()) {
				this.pollingGame = null;
				this.activeGame = result.getOk();
				LoveTropicsNetwork.CHANNEL.send(PacketDistributor.ALL.noArg(), new ClientMinigameMessage(this.activeGame));
				return GameResult.ok(new TranslationTextComponent(TropicraftLangKeys.COMMAND_MINIGAME_STARTED).mergeStyle(TextFormatting.GREEN));
			} else {
				return result.castError();
			}
		});
	}

	@Override
	public GameResult<ITextComponent> joinPlayerAs(ServerPlayerEntity player, @Nullable PlayerRole requestedRole) {
		GameInstance game = this.activeGame;
		if (game != null && !game.getAllPlayers().contains(player)) {
			game.addPlayer(player, PlayerRole.SPECTATOR);
			LoveTropicsNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), new ClientRoleMessage(PlayerRole.SPECTATOR));
			LoveTropicsNetwork.CHANNEL.send(PacketDistributor.ALL.noArg(), new PlayerCountsMessage(PlayerRole.SPECTATOR, game.getMemberCount(PlayerRole.SPECTATOR)));
			return GameResult.ok(new StringTextComponent("You have joined the game as a spectator!").mergeStyle(TextFormatting.GREEN));
		}

		PollingGameInstance polling = this.pollingGame;
		if (polling == null) {
			return GameResult.error(new TranslationTextComponent(TropicraftLangKeys.COMMAND_NO_MINIGAME_POLLING));
		}

		// Client state is updated within this method to allow preconditions to be checked there
		return polling.joinPlayerAs(player, requestedRole);
	}

	@Override
	public GameResult<ITextComponent> removePlayer(ServerPlayerEntity player) {
		GameInstance game = this.activeGame;
		if (game != null && game.removePlayer(player)) {
			ITextComponent name = game.getDefinition().getName();
			return GameResult.ok(new TranslationTextComponent(TropicraftLangKeys.COMMAND_UNREGISTERED_MINIGAME, name).mergeStyle(TextFormatting.RED));
		}

		PollingGameInstance polling = this.pollingGame;
		if (polling != null) {
			// Client state is updated within this method to allow preconditions to be checked there
			return polling.removePlayer(player);
		}

		return GameResult.error(new TranslationTextComponent(TropicraftLangKeys.COMMAND_NO_MINIGAME));
	}

	@SubscribeEvent
	public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
		ProtoGame game = INSTANCE.pollingGame;
		if (game == null) {
			game = INSTANCE.getActiveGame();
		}
		if (game != null) {
			PacketDistributor.PacketTarget target = PacketDistributor.PLAYER.with(() -> (ServerPlayerEntity) event.getPlayer());
			LoveTropicsNetwork.CHANNEL.send(target, new ClientMinigameMessage(game));
			for (PlayerRole role : PlayerRole.values()) {
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
	public Collection<IGameInstance> getAllGames() {
		return activeGame != null ? ImmutableList.of(activeGame) : ImmutableList.of();
	}

	@Override
	public ControlCommandInvoker getControlInvoker() {
		if (activeGame != null) {
			return activeGame.getControlCommands();
		} else if (pollingGame != null) {
			return pollingGame.getControlCommands();
		}
		return ControlCommandInvoker.EMPTY;
	}
}
