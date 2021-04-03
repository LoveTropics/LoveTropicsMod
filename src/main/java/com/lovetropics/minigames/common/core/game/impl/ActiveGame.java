package com.lovetropics.minigames.common.core.game.impl;

import com.google.common.collect.Lists;
import com.lovetropics.minigames.LoveTropics;
import com.lovetropics.minigames.client.minigame.ClientRoleMessage;
import com.lovetropics.minigames.client.minigame.PlayerCountsMessage;
import com.lovetropics.minigames.common.core.game.*;
import com.lovetropics.minigames.common.core.game.behavior.BehaviorMap;
import com.lovetropics.minigames.common.core.game.behavior.IGameBehavior;
import com.lovetropics.minigames.common.core.game.behavior.event.GameEventListeners;
import com.lovetropics.minigames.common.core.game.behavior.event.GameLifecycleEvents;
import com.lovetropics.minigames.common.core.game.behavior.event.GamePlayerEvents;
import com.lovetropics.minigames.common.core.game.control.GameControlCommands;
import com.lovetropics.minigames.common.core.game.map.GameMap;
import com.lovetropics.minigames.common.core.game.state.GameStateMap;
import com.lovetropics.minigames.common.core.game.statistics.GameStatistics;
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
import java.util.EnumMap;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Default implementation of a minigame instance. Simple and naive
 * solution to holding state of players that are currently part
 * of the minigame instance, as well as the definition that is being
 * used to specify the rulesets for the minigame.
 */
public class ActiveGame implements IActiveGame {
    private final GameInstance instance;
    private final MinecraftServer server;
    private final GameMap map;
    private final BehaviorMap behaviors;

    private final MutablePlayerSet allPlayers;
    private final EnumMap<PlayerRole, MutablePlayerSet> roles = new EnumMap<>(PlayerRole.class);

    private final GameInstanceTelemetry telemetry;

    private final GameEventListeners events = new GameEventListeners();
    private final GameStateMap state = new GameStateMap();
    private final GameStatistics statistics = new GameStatistics();

    private final GameControlCommands controlCommands;

    private CommandSource commandSource;

    private long startTime;

    private ActiveGame(GameInstance instance, GameMap map, BehaviorMap behaviors) {
        this.instance = instance;
        this.server = instance.getServer();
        this.map = map;
        this.behaviors = behaviors;

        this.allPlayers = new MutablePlayerSet(server);

        for (PlayerRole role : PlayerRole.ROLES) {
            MutablePlayerSet rolePlayers = new MutablePlayerSet(server);
            this.roles.put(role, rolePlayers);
        }

        this.telemetry = Telemetry.INSTANCE.openGame(this);

        this.controlCommands = new GameControlCommands(instance.getInitiator());
    }

    static CompletableFuture<GameResult<ActiveGame>> start(
            GameInstance instance, GameMap map, BehaviorMap behaviors,
            List<ServerPlayerEntity> participants, List<ServerPlayerEntity> spectators
    ) {
        ActiveGame game = new ActiveGame(instance, map, behaviors);

        GameResult<Unit> result = game.registerBehaviors();
        if (result.isError()) {
            return CompletableFuture.completedFuture(result.castError());
        }

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
    public IGameInstance getInstance() {
        return instance;
    }

    @Override
    public BehaviorMap getBehaviors() {
        return behaviors;
    }

    @Override
    public boolean requestPlayerJoin(ServerPlayerEntity player, @Nullable PlayerRole requestedRole) {
        return addPlayer(player, PlayerRole.SPECTATOR);
    }

    public boolean addPlayer(ServerPlayerEntity player, PlayerRole role) {
        if (allPlayers.add(player)) {
            roles.get(role).add(player);

            try {
                invoker(GamePlayerEvents.JOIN).onJoin(this, player, role);
            } catch (Exception e) {
                LoveTropics.LOGGER.warn("Failed to dispatch player join event", e);
            }

            LoveTropicsNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), new ClientRoleMessage(role));
            sendPlayerCountUpdate(role);

            return true;
        } else {
            return setPlayerRole(player, role);
        }
    }

    @Override
    public boolean setPlayerRole(ServerPlayerEntity player, PlayerRole role) {
        PlayerRole lastRole = getRoleFor(player);
        if (lastRole == null) {
            return false;
        }

        if (lastRole != role) {
            roles.get(role).add(player);
            roles.get(lastRole).remove(player);

            try {
                invoker(GamePlayerEvents.CHANGE_ROLE).onChangeRole(this, player, role, lastRole);
            } catch (Exception e) {
                LoveTropics.LOGGER.warn("Failed to dispatch player change role event", e);
            }

            LoveTropicsNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), new ClientRoleMessage(role));
            sendPlayerCountUpdate(role);
            sendPlayerCountUpdate(lastRole);

            return true;
        } else {
            return false;
        }
    }

    private void sendPlayerCountUpdate(PlayerRole role) {
        LoveTropicsNetwork.CHANNEL.send(PacketDistributor.ALL.noArg(), new PlayerCountsMessage(role, getMemberCount(role)));
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
        return allPlayers;
    }

    private GameResult<Unit> stop() {
        try {
            try {
                invoker(GameLifecycleEvents.STOP).stop(this);
            } catch (Exception e) {
                return GameResult.fromException("Failed to dispatch stop event", e);
            }

            List<ServerPlayerEntity> players = Lists.newArrayList(getAllPlayers());
            for (ServerPlayerEntity player : players) {
                removePlayer(player);
            }

            PlayerSet.ofServer(server).sendMessage(GameMessages.forGame(this).finished());

            try {
                invoker(GameLifecycleEvents.POST_STOP).stop(this);
            } catch (Exception e) {
                return GameResult.fromException("Failed to dispatch post stop event", e);
            }

            return GameResult.ok();
        } catch (Exception e) {
            return GameResult.fromException("Unknown error stopping game", e);
        } finally {
            map.close(this);
            instance.stop();
        }
    }

    @Override
    public GameResult<Unit> finish() {
        try {
            invoker(GameLifecycleEvents.FINISH).stop(this);
        } catch (Exception e) {
            return GameResult.fromException("Failed to dispatch finish event", e);
        }

        GameResult<Unit> result = stop();
        if (result.isOk()) {
            telemetry.finish(statistics);
        } else {
            telemetry.cancel();
        }

        return result;
    }

    @Override
    public GameResult<Unit> cancel() {
        try {
            invoker(GameLifecycleEvents.CANCEL).stop(this);
        } catch (Exception e) {
            return GameResult.fromException("Failed to dispatch cancel event", e);
        }

        telemetry.cancel();

        return stop();
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
            ITextComponent name = instance.getDefinition().getName();
            this.commandSource = new CommandSource(ICommandSource.DUMMY, Vector3d.ZERO, Vector2f.ZERO, getWorld(), 4, name.getString(), name, this.server, null);
        }

        return this.commandSource;
    }

    @Override
    public RegistryKey<World> getDimension() {
        return map.getDimension();
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
}
