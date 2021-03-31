package com.lovetropics.minigames.common.core.game;

import com.lovetropics.minigames.LoveTropics;
import com.lovetropics.minigames.client.minigame.ClientRoleMessage;
import com.lovetropics.minigames.common.core.game.behavior.BehaviorMap;
import com.lovetropics.minigames.common.core.game.behavior.IGameBehavior;
import com.lovetropics.minigames.common.core.game.behavior.event.GameEventListeners;
import com.lovetropics.minigames.common.core.game.behavior.event.GameLifecycleEvents;
import com.lovetropics.minigames.common.core.game.behavior.event.GamePlayerEvents;
import com.lovetropics.minigames.common.core.game.control.GameControlCommands;
import com.lovetropics.minigames.common.core.game.map.GameMap;
import com.lovetropics.minigames.common.core.game.polling.GameRegistrations;
import com.lovetropics.minigames.common.core.game.state.GameStateMap;
import com.lovetropics.minigames.common.core.game.statistics.GameStatistics;
import com.lovetropics.minigames.common.core.game.statistics.PlayerKey;
import com.lovetropics.minigames.common.core.game.statistics.StatisticKey;
import com.lovetropics.minigames.common.core.integration.GameInstanceTelemetry;
import com.lovetropics.minigames.common.core.integration.Telemetry;
import com.lovetropics.minigames.common.core.map.MapRegions;
import com.lovetropics.minigames.common.core.network.LoveTropicsNetwork;
import com.lovetropics.minigames.common.util.Scheduler;
import net.minecraft.command.CommandSource;
import net.minecraft.command.ICommandSource;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.Unit;
import net.minecraft.util.math.vector.Vector2f;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.fml.network.PacketDistributor;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Default implementation of a minigame instance. Simple and naive
 * solution to holding state of players that are currently part
 * of the minigame instance, as well as the definition that is being
 * used to specify the rulesets for the minigame.
 */
public class GameInstance implements IGameInstance {
    private final MinecraftServer server;
    private final IGameDefinition definition;
    private final GameMap map;
    private final BehaviorMap behaviors;
    private final PlayerKey initiator;

    private final MutablePlayerSet allPlayers;
    private final EnumMap<PlayerRole, MutablePlayerSet> roles = new EnumMap<>(PlayerRole.class);

    private final GameInstanceTelemetry telemetry;

    private final GameEventListeners events = new GameEventListeners();
    private final GameStateMap state = new GameStateMap();
    private final GameStatistics statistics = new GameStatistics();

    private final GameControlCommands controlCommands;

    private CommandSource commandSource;

    private long startTime;

    private GameInstance(MinecraftServer server, IGameDefinition definition, GameMap map, BehaviorMap behaviors, PlayerKey initiator) {
        this.server = server;
        this.definition = definition;
        this.map = map;
        this.behaviors = behaviors;
        this.initiator = initiator;

        this.allPlayers = new MutablePlayerSet(server);

        for (PlayerRole role : PlayerRole.ROLES) {
            MutablePlayerSet rolePlayers = new MutablePlayerSet(server);
            this.roles.put(role, rolePlayers);
        }

        this.telemetry = Telemetry.INSTANCE.openGame(this);

        this.controlCommands = new GameControlCommands(initiator);
    }

    public static CompletableFuture<GameResult<GameInstance>> start(
            MinecraftServer server, IGameDefinition definition, GameMap map, BehaviorMap behaviors,
            PlayerKey initiator, GameRegistrations registrations
    ) {
        GameInstance game = new GameInstance(server, definition, map, behaviors, initiator);

        GameResult<Unit> result = game.registerBehaviors();
        if (result.isError()) {
            return CompletableFuture.completedFuture(result.castError());
        }

        List<ServerPlayerEntity> participants = new ArrayList<>();
        List<ServerPlayerEntity> spectators = new ArrayList<>();

        registrations.collectInto(server, participants, spectators, definition.getMaximumParticipantCount());

        try {
            game.invoker(GameLifecycleEvents.ASSIGN_ROLES).assignRoles(game, participants, spectators);
        } catch (Exception e) {
            return CompletableFuture.completedFuture(GameResult.fromException("Failed to dispatch assign roles event", e));
        }

        game.addPlayers(participants, spectators);

        map.getName().ifPresent(mapName -> game.getStatistics().getGlobal().set(StatisticKey.MAP, mapName));
        game.telemetry.start();

        return Scheduler.INSTANCE.submit(s -> {
            return game.start().map(u -> game);
		}, 1);
	}

    private GameResult<Unit> registerBehaviors() {
        for (IGameBehavior behavior : getBehaviors()) {
            behavior.registerState(state);
        }

        for (IGameBehavior behavior : getBehaviors()) {
            try {
                behavior.register(this, events);
            } catch (GameException e) {
                return GameResult.error(e.getTextMessage());
            }
        }

        return GameResult.ok();
    }

    private void addPlayers(List<ServerPlayerEntity> participants, List<ServerPlayerEntity> spectators) {
        for (ServerPlayerEntity player : participants) {
            addPlayer(player, PlayerRole.PARTICIPANT);
        }

        for (ServerPlayerEntity player : spectators) {
            addPlayer(player, PlayerRole.SPECTATOR);
        }
    }

    private GameResult<Unit> start() {
        startTime = getWorld().getGameTime();
        try {
            invoker(GameLifecycleEvents.START).start(this);
            return GameResult.ok();
        } catch (Exception e) {
            return GameResult.fromException("Failed to dispatch game start event", e);
        }
    }

	@Override
	public GameStatus getStatus() {
		return GameStatus.ACTIVE;
	}

    @Override
    public IGameDefinition getDefinition() {
        return this.definition;
    }

    @Override
    public BehaviorMap getBehaviors() {
        return behaviors;
    }

    @Override
    public void addPlayer(ServerPlayerEntity player, PlayerRole role) {
        if (!allPlayers.contains(player)) {
            allPlayers.add(player);

            try {
                invoker(GamePlayerEvents.JOIN).onJoin(this, player, role);
            } catch (Exception e) {
                LoveTropics.LOGGER.warn("Failed to dispatch player join event", e);
            }
        }

        PlayerRole lastRole = getRoleFor(player);
        roles.get(role).add(player);

        if (lastRole != role) {
            roles.get(lastRole).remove(player);

            try {
                invoker(GamePlayerEvents.CHANGE_ROLE).onChangeRole(this, player, role, lastRole);
            } catch (Exception e) {
                LoveTropics.LOGGER.warn("Failed to dispatch player change role event", e);
            }
        }

        LoveTropicsNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), new ClientRoleMessage(role));
    }

    @Nullable
    private PlayerRole getRoleFor(ServerPlayerEntity player) {
        for (PlayerRole role : PlayerRole.ROLES) {
            if (roles.get(role).contains(player)) {
                return role;
            }
        }
        return null;
    }

    @Override
    public boolean removePlayer(ServerPlayerEntity player) {
        if (allPlayers.remove(player)) {
            for (MutablePlayerSet rolePlayers : roles.values()) {
                rolePlayers.remove(player);
            }

            try {
                invoker(GamePlayerEvents.LEAVE).onLeave(this, player);
            } catch (Exception e) {
                LoveTropics.LOGGER.warn("Failed to dispatch player leave event", e);
            }

            return true;
        }

        return false;
    }

    @Override
    public PlayerSet getAllPlayers() {
        return this.allPlayers;
    }

    @Override
    public PlayerSet getPlayersWithRole(PlayerRole role) {
        return roles.get(role);
    }

    @Override
    public MapRegions getMapRegions() {
        return map.getRegions();
    }

    @Override
    public CommandSource getCommandSource() {
        if (this.commandSource == null) {
            ITextComponent name = definition.getName();
            this.commandSource = new CommandSource(ICommandSource.DUMMY, Vector3d.ZERO, Vector2f.ZERO, getWorld(), 4, name.getString(), name, this.server, null);
        }

        return this.commandSource;
    }

    @Override
    public RegistryKey<World> getDimension() {
        return map.getDimension();
    }

    @Override
    public MinecraftServer getServer() {
        return server;
    }

    @Override
    public ServerWorld getWorld() {
        return server.getWorld(map.getDimension());
    }

    @Override
    public long ticks() {
        return getWorld().getGameTime() - startTime;
    }

    @Override
    public GameEventListeners getEvents() {
        return events;
    }

    @Override
    public GameStateMap getState() {
        return state;
    }

    @Override
    public GameControlCommands getControlCommands() {
        return this.controlCommands;
    }

    @Override
    public GameStatistics getStatistics() {
        return statistics;
    }

    @Override
    public GameInstanceTelemetry getTelemetry() {
        return telemetry;
    }

    @Override
    public PlayerKey getInitiator() {
        return initiator;
    }

    @Override
    public void close() {
        map.close(this);
    }
}
