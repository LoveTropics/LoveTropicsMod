package com.lovetropics.minigames.common.core.game.impl;

import com.lovetropics.minigames.Constants;
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
import net.minecraft.world.World;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.server.FMLServerStoppingEvent;
import net.minecraftforge.fml.network.PacketDistributor;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Standard implementation of a game manager. Would prefer to do something other
 * than singleton-style implementation to allow for multiple managers to run multiple
 * games at once.
 */
@Mod.EventBusSubscriber(modid = Constants.MODID)
public class MultiGameManager implements IGameManager {
	public static final MultiGameManager INSTANCE = new MultiGameManager();

	private final List<GameInstance> currentGames = new ArrayList<>();

	@Override
	public GameResult<PollingGame> startPolling(IGameDefinition game, ServerPlayerEntity initiator) {
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

		this.currentGames.add(polling.getInstance());

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
		if (player.world.isRemote) return null;

		for (GameInstance game : currentGames) {
			if (game.getAllPlayers().contains(player)) {
				return game;
			}
		}

		return null;
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
		if (world.isRemote) return null;

		for (GameInstance game : currentGames) {
			IActiveGame active = game.asActive();
			if (active != null && active.getDimension() == world.getDimensionKey()) {
				return game;
			}
		}

		return null;
	}

	@Override
	public Collection<? extends IGameInstance> getAllGames() {
		return currentGames;
	}

	@Nullable
	@Override
	public GameInstance getGameByCommandId(String id) {
		for (GameInstance game : currentGames) {
			if (game.getInstanceId().commandId.equals(id)) {
				return game;
			}
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
		INSTANCE.currentGames.remove(game);
	}

	@SubscribeEvent
	public static void onServerStopping(FMLServerStoppingEvent event) {
		List<GameInstance> games = new ArrayList<>(INSTANCE.currentGames);
		for (GameInstance game : games) {
			game.cancel();
		}
	}

	@SubscribeEvent
	public static void onServerTick(TickEvent.ServerTickEvent event) {
		if (event.phase == TickEvent.Phase.START) return;

		List<GameInstance> canceled = null;

		for (GameInstance game : INSTANCE.currentGames) {
			if (!game.tick()) {
				if (canceled == null) {
					canceled = new ArrayList<>();
				}
				canceled.add(game);
			}
		}

		if (canceled != null) {
			canceled.forEach(IGameInstance::cancel);
		}
	}

	@SubscribeEvent
	public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
		for (GameInstance game : INSTANCE.currentGames) {
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
		for (GameInstance game : INSTANCE.currentGames) {
			game.removePlayer((ServerPlayerEntity) event.getPlayer());
		}
	}
}
