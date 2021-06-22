package com.lovetropics.minigames.common.core.game.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;

import javax.annotation.Nullable;

import com.google.common.base.Predicates;
import com.lovetropics.minigames.Constants;
import com.lovetropics.minigames.client.data.LoveTropicsLangKeys;
import com.lovetropics.minigames.client.minigame.ClientMinigameMessage;
import com.lovetropics.minigames.client.minigame.PlayerCountsMessage;
import com.lovetropics.minigames.common.core.game.GameMessages;
import com.lovetropics.minigames.common.core.game.GameResult;
import com.lovetropics.minigames.common.core.game.IActiveGame;
import com.lovetropics.minigames.common.core.game.IGameDefinition;
import com.lovetropics.minigames.common.core.game.IGameInstance;
import com.lovetropics.minigames.common.core.game.IGameManager;
import com.lovetropics.minigames.common.core.game.PlayerRole;
import com.lovetropics.minigames.common.core.game.PlayerSet;
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
import net.minecraft.util.math.vector.Vector3d;
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
		if (currentGames.stream().map(GameInstance::getDefinition)
				.filter(d -> d.getGameArea().intersects(game.getGameArea()))
				.anyMatch(d -> !Collections.disjoint(d.getMapProvider().getPossibleDimensions(), game.getMapProvider().getPossibleDimensions()))) {
			return GameResult.error(new TranslationTextComponent(LoveTropicsLangKeys.COMMAND_MINIGAMES_INTERSECT));
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
		return getGame(g -> g.getAllPlayers().contains(player));
	}

	@Nullable
	@Override
	public GameInstance getGameFor(Entity entity) {
		if (entity instanceof PlayerEntity) {
			return getGameFor((PlayerEntity) entity);
		}
		return getGameForWorld(entity.world, g -> g.getDefinition().getGameArea().contains(entity.getPositionVec()));
	}

	@Nullable
	@Override
	public GameInstance getGameAt(World world, BlockPos pos) {
		return getGameForWorld(world, g -> g.getDefinition().getGameArea().contains(Vector3d.copyCentered(pos)));
	}

	public List<GameInstance> getGamesForWorld(World world) {
		return getGamesForWorld(world, Predicates.alwaysTrue());
	}

	public List<GameInstance> getGamesForWorld(World world, Predicate<GameInstance> pred) {
		if (world.isRemote) return Collections.emptyList();

		return getGames(pred.and(g -> {
			IActiveGame active = g.asActive();
			return active != null && active.getDimension() == world.getDimensionKey();
		}));
	}

	public List<GameInstance> getGames(Predicate<GameInstance> pred) {
		List<GameInstance> ret = new ArrayList<>();
		for (GameInstance game : currentGames) {
			if (pred.test(game)) {
				ret.add(game);
			}
		}

		return ret;
	}

	@Nullable
	public GameInstance getGameForWorld(World world, Predicate<GameInstance> pred) {
		return getGamesForWorld(world, pred).stream().findFirst().orElse(null);
	}

	@Nullable
	public GameInstance getGame(Predicate<GameInstance> pred) {
		return getGames(pred).stream().findFirst().orElse(null);
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
