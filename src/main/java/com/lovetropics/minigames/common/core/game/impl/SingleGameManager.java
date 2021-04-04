package com.lovetropics.minigames.common.core.game.impl;

import com.google.common.collect.ImmutableList;
import com.lovetropics.minigames.Constants;
import com.lovetropics.minigames.client.data.LoveTropicsLangKeys;
import com.lovetropics.minigames.client.minigame.ClientMinigameMessage;
import com.lovetropics.minigames.client.minigame.PlayerCountsMessage;
import com.lovetropics.minigames.common.core.game.*;
import com.lovetropics.minigames.common.core.game.behavior.event.GamePollingEvents;
import com.lovetropics.minigames.common.core.game.control.ControlCommandInvoker;
import com.lovetropics.minigames.common.core.game.statistics.PlayerKey;
import com.lovetropics.minigames.common.core.integration.Telemetry;
import com.lovetropics.minigames.common.core.network.LoveTropicsNetwork;
import net.minecraft.command.CommandSource;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.PlayerList;
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
import net.minecraftforge.fml.event.server.FMLServerStoppingEvent;
import net.minecraftforge.fml.network.PacketDistributor;

import javax.annotation.Nullable;
import java.util.Collection;

/**
 * Standard implementation of a game manager. Would prefer to do something other
 * than singleton-style implementation to allow for multiple managers to run multiple
 * games at once.
 */
// TODO: support concurrent minigames
@Mod.EventBusSubscriber(modid = Constants.MODID)
public class SingleGameManager implements IGameManager {
	public static final SingleGameManager INSTANCE = new SingleGameManager();

	/**
	 * Current instance of the game. Agnostic from game definition and used to store
	 * the players and spectators that are a part of the current game.
	 * <p>
	 * Is null when there is no game running.
	 */
	private GameInstance currentGame;

	@Override
	public GameResult<PollingGame> startPolling(IGameDefinition game, ServerPlayerEntity initiator) {
		// Make sure there isn't a currently running game
		if (currentGame != null) {
			if (currentGame.isPolling()) {
				return GameResult.error(new TranslationTextComponent(LoveTropicsLangKeys.COMMAND_ANOTHER_MINIGAME_POLLING));
			} else {
				return GameResult.error(new TranslationTextComponent(LoveTropicsLangKeys.COMMAND_MINIGAME_ALREADY_STARTED));
			}
		}

		GameResult<PollingGame> pollResult = GameInstance.createPolling(this, initiator.server, game, PlayerKey.from(initiator));
		if (pollResult.isError()) {
			return pollResult.castError();
		}

		PollingGame polling = pollResult.getOk();

		try {
			polling.getEvents().invoker(GamePollingEvents.START).start(polling);
		} catch (Exception e) {
			return GameResult.fromException("Failed to dispatch polling start event", e);
		}

		PlayerSet.ofServer(initiator.server).sendMessage(GameMessages.forGame(game).startPolling());

		this.currentGame = polling.getInstance();

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

	@Nullable
	@Override
	public GameInstance getGameFor(PlayerEntity player) {
		GameInstance game = this.currentGame;
		if (game == null || player.world.isRemote) return null;

		return game.getAllPlayers().contains(player) ? game : null;
	}

	@Nullable
	@Override
	public GameInstance getGameFor(Entity entity) {
		if (entity instanceof PlayerEntity) {
			return getGameFor((PlayerEntity) entity);
		}
		return getGameForWorld(entity.world);
	}

	@Nullable
	@Override
	public GameInstance getGameAt(World world, BlockPos pos) {
		return getGameForWorld(world);
	}

	@Nullable
	public GameInstance getGameForWorld(World world) {
		GameInstance game = this.currentGame;
		if (game == null || world.isRemote) return null;

		IActiveGame active = game.asActive();
		if (active != null) {
			return world.getDimensionKey() == active.getDimension() ? game : null;
		} else {
			return null;
		}
	}

	@Override
	public Collection<IGameInstance> getAllGames() {
		GameInstance game = this.currentGame;
		return game != null ? ImmutableList.of(game) : ImmutableList.of();
	}

	@Nullable
	@Override
	public GameInstance getGameByCommandId(String id) {
		GameInstance game = this.currentGame;
		if (game.getInstanceId().commandId.equals(id)) {
			return game;
		}
		return null;
	}

	@Override
	public ControlCommandInvoker getControlInvoker(CommandSource source) {
		IGameInstance game = getGameFor(source);
		if (game != null) {
			return game.getControlCommands();
		}
		return ControlCommandInvoker.EMPTY;
	}

	// TODO: should these be separated from the specific game manager instance?
	void stop(GameInstance game) {
		if (INSTANCE.currentGame == game) {
			INSTANCE.currentGame = null;
		}
	}

	@SubscribeEvent
	public static void onServerStopping(FMLServerStoppingEvent event) {
		GameInstance game = INSTANCE.currentGame;
		if (game != null) {
			game.cancel();
		}
	}

	@SubscribeEvent
	public static void onServerTick(TickEvent.ServerTickEvent event) {
		if (event.phase == TickEvent.Phase.START) return;

		GameInstance game = INSTANCE.currentGame;
		if (game != null && !game.tick()) {
			game.cancel();
		}
	}

	@SubscribeEvent
	public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
		IGameInstance game = INSTANCE.currentGame;
		if (game != null) {
			PacketDistributor.PacketTarget target = PacketDistributor.PLAYER.with(() -> (ServerPlayerEntity) event.getPlayer());
			LoveTropicsNetwork.CHANNEL.send(target, new ClientMinigameMessage(game));

			int networkId = game.getInstanceId().networkId;
			for (PlayerRole role : PlayerRole.ROLES) {
				LoveTropicsNetwork.CHANNEL.send(target, new PlayerCountsMessage(networkId, role, game.getMemberCount(role)));
			}
		}
	}

	/**
	 * When a player logs out, remove them from the currently running game instance
	 * if they are inside, and teleport back them to their original state.
	 * <p>
	 * Also if they have registered for a game poll, they will be removed from the
	 * list of registered players.
	 */
	@SubscribeEvent
	public static void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
		GameInstance game = INSTANCE.currentGame;
		if (game != null) {
			game.removePlayer((ServerPlayerEntity) event.getPlayer());
		}
	}
}
