package com.lovetropics.minigames.common.core.game.impl;

import com.lovetropics.minigames.Constants;
import com.lovetropics.minigames.client.lobby.state.message.LobbyUpdateMessage;
import com.lovetropics.minigames.common.core.game.GameResult;
import com.lovetropics.minigames.common.core.game.IGameManager;
import com.lovetropics.minigames.common.core.game.IGamePhase;
import com.lovetropics.minigames.common.core.game.lobby.GameLobbyId;
import com.lovetropics.minigames.common.core.game.lobby.GameLobbyMetadata;
import com.lovetropics.minigames.common.core.game.lobby.IGameLobby;
import com.lovetropics.minigames.common.core.game.player.PlayerOps;
import com.lovetropics.minigames.common.core.game.player.PlayerSet;
import com.lovetropics.minigames.common.core.game.state.control.ControlCommandInvoker;
import com.lovetropics.minigames.common.core.game.state.statistics.PlayerKey;
import com.lovetropics.minigames.common.core.network.LoveTropicsNetwork;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import net.minecraft.command.CommandSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.PlayerList;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.server.FMLServerStoppingEvent;
import net.minecraftforge.fml.network.PacketDistributor;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Predicate;

/**
 * Standard implementation of a game manager. Would prefer to do something other
 * than singleton-style implementation to allow for multiple managers to run multiple
 * games at once.
 */
@Mod.EventBusSubscriber(modid = Constants.MODID)
public class MultiGameManager implements IGameManager {
	public static final MultiGameManager INSTANCE = new MultiGameManager();

	private final LobbyCommandIdManager commandIds = new LobbyCommandIdManager();

	private final List<GameLobby> lobbies = new ArrayList<>();

	private final Map<UUID, GameLobby> lobbiesByPlayer = new Object2ObjectOpenHashMap<>();
	private final Map<RegistryKey<World>, List<GamePhase>> gamesByDimension = new Reference2ObjectOpenHashMap<>();

	@Override
	public GameResult<IGameLobby> createGameLobby(String name, ServerPlayerEntity initiator) {
		GameLobbyId id = GameLobbyId.next();
		String commandId = commandIds.acquire(name);
		GameLobbyMetadata metadata = new GameLobbyMetadata(id, PlayerKey.from(initiator), name, commandId);

		GameLobby lobby = new GameLobby(this, initiator.server, metadata);
		lobbies.add(lobby);

		return GameResult.ok(lobby);
	}

	// TODO: ?
	/*@Override
	public GameResult<WaitingGame> startPolling(IGameDefinition game, ServerPlayerEntity initiator) {
		if (lobbies.stream().map(GameLobby::getDefinition)
				.filter(d -> d.getGameArea().intersects(game.getGameArea()))
				.anyMatch(d -> !Collections.disjoint(d.getMapProvider().getPossibleDimensions(), game.getMap().getPossibleDimensions()))) {
			return GameResult.error(new TranslationTextComponent(LoveTropicsLangKeys.COMMAND_MINIGAMES_INTERSECT));
		}

		GameLobbyId id = ids.acquire(name);

		GameResult<WaitingGame> pollResult = GameLobby.createPolling(this, initiator.server, id, game, PlayerKey.from(initiator));
		if (pollResult.isError()) {
			return pollResult.castError();
		}

		WaitingGame polling = pollResult.getOk();

		try {
			polling.invoker(GamePollingEvents.START).start(polling);
		} catch (Exception e) {
			return GameResult.fromException("Failed to dispatch polling start event", e);
		}

		PlayerSet.ofServer(initiator.server).sendMessage(GameMessages.forLobby(game).startPolling());

		lobbies.add(polling.getLobby());

		if (!Telemetry.INSTANCE.isConnected()) {
			ITextComponent warning = new StringTextComponent("Warning: Telemetry websocket is not connected!")
					.mergeStyle(TextFormatting.RED, TextFormatting.BOLD);
			operators(initiator.server).sendMessage(warning);
		}

		return GameResult.ok(polling);
	}*/

	private static PlayerOps operators(MinecraftServer server) {
		PlayerList playerList = server.getPlayerList();
		return PlayerSet.of(playerList)
				.filter(player -> playerList.canSendCommands(player.getGameProfile()));
	}

	@Nullable
	@Override
	public GameLobby getLobbyFor(PlayerEntity player) {
		if (player.world.isRemote) return null;

		return lobbiesByPlayer.get(player.getUniqueID());
	}

	@Nullable
	@Override
	public IGamePhase getGamePhaseFor(PlayerEntity player) {
		GameLobby lobby = getLobbyFor(player);
		return lobby != null ? lobby.getCurrentPhase() : null;
	}

	@Nullable
	@Override
	public IGamePhase getGamePhaseAt(World world, Vector3d pos) {
		return getGamePhaseForWorld(world, phase -> phase.getPhaseDefinition().getGameArea().contains(pos));
	}

	public List<GamePhase> getGamePhaseForWorld(World world) {
		if (world.isRemote) {
			return Collections.emptyList();
		}

		return gamesByDimension.getOrDefault(world.getDimensionKey(), Collections.emptyList());
	}

	@Nullable
	public GamePhase getGamePhaseForWorld(World world, Predicate<GamePhase> pred) {
		return getGamePhaseForWorld(world).stream().filter(pred).findFirst().orElse(null);
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
	public GameLobby getLobbyByCommandId(String id) {
		for (GameLobby lobby : lobbies) {
			if (lobby.getMetadata().commandId().equals(id)) {
				return lobby;
			}
		}
		return null;
	}

	@Override
	public ControlCommandInvoker getControlInvoker(CommandSource source) {
		IGamePhase phase = getGamePhaseFor(source);
		return phase != null ? phase.getControlInvoker() : ControlCommandInvoker.EMPTY;
	}

	void addGamePhaseToDimension(RegistryKey<World> dimension, GamePhase game) {
		gamesByDimension.computeIfAbsent(dimension, k -> new ArrayList<>())
				.add(game);
	}

	void removeGamePhaseFromDimension(RegistryKey<World> dimension, GamePhase game) {
		List<GamePhase> games = gamesByDimension.get(dimension);
		if (games == null) return;

		if (games.remove(game) && games.isEmpty()) {
			gamesByDimension.remove(dimension, games);
		}
	}

	void addPlayerToLobby(ServerPlayerEntity player, GameLobby lobby) {
		lobbiesByPlayer.put(player.getUniqueID(), lobby);
	}

	void removePlayerFromLobby(ServerPlayerEntity player, GameLobby lobby) {
		lobbiesByPlayer.remove(player.getUniqueID(), lobby);
	}

	void removeLobby(GameLobby lobby) {
		if (lobbies.remove(lobby)) {
			commandIds.release(lobby.getMetadata().commandId());
		}
	}

	@SubscribeEvent
	public static void onServerStopping(FMLServerStoppingEvent event) {
		List<GameLobby> lobbies = new ArrayList<>(INSTANCE.lobbies);
		for (GameLobby lobby : lobbies) {
			lobby.cancel();
		}
	}

	@SubscribeEvent
	public static void onServerTick(TickEvent.ServerTickEvent event) {
		if (event.phase == TickEvent.Phase.START) return;

		List<GameLobby> canceled = null;

		for (GameLobby lobby : INSTANCE.lobbies) {
			if (!lobby.tick()) {
				if (canceled == null) {
					canceled = new ArrayList<>();
				}
				canceled.add(lobby);
			}
		}

		if (canceled != null) {
			canceled.forEach(GameLobby::cancel);
		}
	}

	@SubscribeEvent
	public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
		for (GameLobby lobby : INSTANCE.lobbies) {
			// TODO: watcher
			PacketDistributor.PacketTarget target = PacketDistributor.PLAYER.with(() -> (ServerPlayerEntity) event.getPlayer());
			LoveTropicsNetwork.CHANNEL.send(target, LobbyUpdateMessage.update(lobby));

			/*int networkId = lobby.getMetadata().id().networkId();
			for (PlayerRole role : PlayerRole.ROLES) {
				LoveTropicsNetwork.CHANNEL.send(target, new ClientLobbyPlayersMessage(networkId, role, lobby.getMemberCount(role)));
			}*/
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
		for (GameLobby lobby : INSTANCE.lobbies) {
			ServerPlayerEntity player = (ServerPlayerEntity) event.getPlayer();
			lobby.getPlayers().remove(player);
		}
	}
}
