package com.lovetropics.minigames.common.core.game.impl;

import com.lovetropics.minigames.Constants;
import com.lovetropics.minigames.common.core.game.GameResult;
import com.lovetropics.minigames.common.core.game.IGameManager;
import com.lovetropics.minigames.common.core.game.IGamePhase;
import com.lovetropics.minigames.common.core.game.IGamePhaseDefinition;
import com.lovetropics.minigames.common.core.game.lobby.GameLobbyId;
import com.lovetropics.minigames.common.core.game.lobby.GameLobbyMetadata;
import com.lovetropics.minigames.common.core.game.lobby.IGameLobby;
import com.lovetropics.minigames.common.core.game.lobby.LobbyVisibility;
import com.lovetropics.minigames.common.core.game.map.IGameMapProvider;
import com.lovetropics.minigames.common.core.game.state.control.ControlCommandInvoker;
import com.lovetropics.minigames.common.core.game.state.statistics.PlayerKey;
import com.lovetropics.minigames.common.core.game.util.GameTexts;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Unit;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityTravelToDimensionEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Predicate;

/**
 * Standard implementation of a game manager. Would prefer to do something other
 * than singleton-style implementation to allow for multiple managers to run multiple
 * games at once.
 */
@Mod.EventBusSubscriber(modid = Constants.MODID)
public class MultiGameManager implements IGameManager {
	public static final MultiGameManager INSTANCE = new MultiGameManager();

	private final List<GameLobby> lobbies = new ArrayList<>();

	private final Map<UUID, GameLobby> lobbiesByPlayer = new Object2ObjectOpenHashMap<>();
	private final Map<ResourceKey<Level>, List<GamePhase>> gamesByDimension = new Reference2ObjectOpenHashMap<>();

	private GameLobby focusedLiveLobby;

	@Override
	public GameResult<IGameLobby> createGameLobby(String name, ServerPlayer initiator) {
		GameLobby currentLobby = lobbiesByPlayer.get(initiator.getUUID());
		if (currentLobby != null) {
			return GameResult.error(GameTexts.Commands.ALREADY_IN_LOBBY);
		}

		GameLobbyId id = GameLobbyId.next();
		GameLobbyMetadata metadata = new GameLobbyMetadata(id, PlayerKey.from(initiator), name);

		GameLobby lobby = new GameLobby(this, initiator.server, metadata);
		lobbies.add(lobby);

		return GameResult.ok(lobby);
	}

	GameResult<Unit> canStartGamePhase(IGamePhaseDefinition definition) {
		IGameMapProvider map = definition.getMap();
		List<ResourceKey<Level>> possibleDimensions = map.getPossibleDimensions();
		AABB area = definition.getGameArea();

		for (ResourceKey<Level> dimension : possibleDimensions) {
			List<GamePhase> games = gamesByDimension.getOrDefault(dimension, Collections.emptyList());
			for (GamePhase game : games) {
				if (game.getPhaseDefinition().getGameArea().intersects(area)) {
					return GameResult.error(GameTexts.Commands.GAMES_INTERSECT);
				}
			}
		}

		return GameResult.ok();
	}

	@Nullable
	@Override
	public GameLobby getLobbyFor(Player player) {
		if (player.level().isClientSide) return null;

		return lobbiesByPlayer.get(player.getUUID());
	}

	@Nullable
	@Override
	public IGamePhase getGamePhaseFor(Player player) {
		GameLobby lobby = getLobbyFor(player);
		return lobby != null ? lobby.getCurrentPhase() : null;
	}

	@Nullable
	@Override
	public IGamePhase getGamePhaseAt(Level level, Vec3 pos) {
		return getGamePhaseForWorld(level, phase -> phase.getPhaseDefinition().getGameArea().contains(pos));
	}

	@Nullable
	@Override
	public IGamePhase getGamePhaseInDimension(Level level) {
		List<GamePhase> games = gamesByDimension.get(level.dimension());
		if (games != null && games.size() == 1) {
			return games.get(0);
		}
		return null;
	}

	public List<GamePhase> getGamePhasesForWorld(Level level) {
		if (level.isClientSide) {
			return Collections.emptyList();
		}

		return gamesByDimension.getOrDefault(level.dimension(), Collections.emptyList());
	}

	@Nullable
	public GamePhase getGamePhaseForWorld(Level level, Predicate<GamePhase> pred) {
		return getGamePhasesForWorld(level).stream().filter(pred).findFirst().orElse(null);
	}

	@Nullable
	public GameLobby getLobby(Predicate<GameLobby> pred) {
		return lobbies.stream().filter(pred).findFirst().orElse(null);
	}

	@Override
	public Collection<? extends IGameLobby> getAllLobbies() {
		return lobbies;
	}

	@Nullable
	@Override
	public IGameLobby getLobbyByNetworkId(int id) {
		for (GameLobby lobby : lobbies) {
			if (lobby.getMetadata().id().networkId() == id) {
				return lobby;
			}
		}
		return null;
	}

	@Nullable
	@Override
	public GameLobby getLobbyById(UUID id) {
		for (GameLobby lobby : lobbies) {
			if (lobby.getMetadata().id().uuid().equals(id)) {
				return lobby;
			}
		}
		return null;
	}

	@Override
	public ControlCommandInvoker getControlInvoker(CommandSourceStack source) {
		IGamePhase phase = getGamePhaseFor(source);
		return phase != null ? phase.getControlInvoker() : ControlCommandInvoker.EMPTY;
	}

	void addGamePhaseToDimension(ResourceKey<Level> dimension, GamePhase game) {
		gamesByDimension.computeIfAbsent(dimension, k -> new ArrayList<>())
				.add(game);
	}

	void removeGamePhaseFromDimension(ResourceKey<Level> dimension, GamePhase game) {
		List<GamePhase> games = gamesByDimension.get(dimension);
		if (games == null) return;

		if (games.remove(game) && games.isEmpty()) {
			gamesByDimension.remove(dimension, games);
		}
	}

	void addPlayerToLobby(ServerPlayer player, GameLobby lobby) {
		lobbiesByPlayer.put(player.getUUID(), lobby);
	}

	void removePlayerFromLobby(ServerPlayer player, GameLobby lobby) {
		lobbiesByPlayer.remove(player.getUUID(), lobby);
	}

	void removeLobby(GameLobby lobby) {
		lobbies.remove(lobby);

		if (focusedLiveLobby == lobby) {
			setFocusedLiveLobby(null);
		}
	}

	GameLobbyMetadata setVisibility(GameLobby lobby, LobbyVisibility visibility) {
		if (visibility.isFocusedLive()) {
			if (!this.setFocusedLive(lobby)) {
				return lobby.metadata;
			}
		}

		return lobby.metadata.withVisibility(visibility);
	}

	private boolean setFocusedLive(GameLobby lobby) {
		if (focusedLiveLobby == null) {
			setFocusedLiveLobby(lobby);
			return true;
		} else {
			return false;
		}
	}

	private void setFocusedLiveLobby(@Nullable GameLobby lobby) {
		focusedLiveLobby = lobby;

		for (GameLobby otherLobby : lobbies) {
			otherLobby.management.onFocusedLiveLobbyChanged();
		}
	}

	boolean hasFocusedLiveLobby() {
		return focusedLiveLobby != null;
	}

	@SubscribeEvent
	public static void onServerStopping(ServerStoppingEvent event) {
		List<GameLobby> lobbies = new ArrayList<>(INSTANCE.lobbies);
		for (GameLobby lobby : lobbies) {
			lobby.close();
		}
	}

	@SubscribeEvent
	public static void onServerTick(TickEvent.ServerTickEvent event) {
		if (event.phase == TickEvent.Phase.START) return;

		for (GameLobby lobby : INSTANCE.lobbies) {
			lobby.tick();
		}
	}

	@SubscribeEvent
	public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
		ServerPlayer player = (ServerPlayer) event.getEntity();

		for (GameLobby lobby : INSTANCE.lobbies) {
			lobby.onPlayerLoggedIn(player);
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
		ServerPlayer player = (ServerPlayer) event.getEntity();
		for (GameLobby lobby : INSTANCE.lobbies) {
			lobby.onPlayerLoggedOut(player);
		}
	}

	@SubscribeEvent
	public static void onPlayerTryChangeDimension(EntityTravelToDimensionEvent event) {
		Entity entity = event.getEntity();
		if (entity instanceof ServerPlayer) {
			ServerPlayer player = (ServerPlayer) entity;
			ServerLevel targetWorld = player.server.getLevel(event.getDimension());
			if (targetWorld == null) {
				return;
			}

			IGamePhase playerPhase = INSTANCE.getGamePhaseFor(player);
			IGamePhase targetPhase = INSTANCE.getGamePhaseAt(targetWorld, player.blockPosition());
			if (!canTravelBetweenPhases(playerPhase, targetPhase)) {
				player.displayClientMessage(GameTexts.Commands.cannotTeleportIntoGame(), true);

				event.setCanceled(true);
			}
		}
	}

	private static boolean canTravelBetweenPhases(@Nullable IGamePhase from, @Nullable IGamePhase to) {
		return to == null || from == to;
	}

	@SubscribeEvent
	public static void onPlayerChangedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
		if (event.getEntity() instanceof ServerPlayer player) {
			IGamePhase phase = INSTANCE.getGamePhaseFor(player);
			if (phase == null) return;

			ResourceKey<Level> dimension = phase.getDimension();
			if (event.getFrom() == dimension && event.getTo() != dimension) {
				if (phase.getLobby().getPlayers().remove(player)) {
					player.displayClientMessage(GameTexts.Status.leftGameDimension(), false);
				}
			}
		}
	}
}
