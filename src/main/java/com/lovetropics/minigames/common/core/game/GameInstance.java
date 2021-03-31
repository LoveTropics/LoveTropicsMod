package com.lovetropics.minigames.common.core.game;

import com.lovetropics.minigames.LoveTropics;
import com.lovetropics.minigames.client.minigame.ClientRoleMessage;
import com.lovetropics.minigames.common.core.game.behavior.BehaviorMap;
import com.lovetropics.minigames.common.core.game.behavior.IGameBehavior;
import com.lovetropics.minigames.common.core.game.behavior.event.GameEventListeners;
import com.lovetropics.minigames.common.core.game.behavior.event.GameLifecycleEvents;
import com.lovetropics.minigames.common.core.game.behavior.event.GamePlayerEvents;
import com.lovetropics.minigames.common.core.game.map.GameMap;
import com.lovetropics.minigames.common.core.game.polling.MinigameRegistrations;
import com.lovetropics.minigames.common.core.game.state.GameStateMap;
import com.lovetropics.minigames.common.core.game.statistics.GameStatistics;
import com.lovetropics.minigames.common.core.game.statistics.PlayerKey;
import com.lovetropics.minigames.common.core.game.statistics.StatisticKey;
import com.lovetropics.minigames.common.core.integration.GameInstanceTelemetry;
import com.lovetropics.minigames.common.core.integration.Telemetry;
import com.lovetropics.minigames.common.core.map.MapRegions;
import com.lovetropics.minigames.common.core.network.LoveTropicsNetwork;
import com.lovetropics.minigames.common.util.Scheduler;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
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
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

/**
 * Default implementation of a minigame instance. Simple and naive
 * solution to holding state of players that are currently part
 * of the minigame instance, as well as the definition that is being
 * used to specify the rulesets for the minigame.
 */
public class GameInstance implements IGameInstance
{
    private final MinecraftServer server;
    private final IGameDefinition definition;
    private final RegistryKey<World> dimension;

    private final MutablePlayerSet allPlayers;
    private final EnumMap<PlayerRole, MutablePlayerSet> roles = new EnumMap<>(PlayerRole.class);

    private final GameMap map;
    private final GameStatistics statistics = new GameStatistics();

    private CommandSource commandSource;

    private final Map<String, ControlCommand> controlCommands = new Object2ObjectOpenHashMap<>();
    private long ticks;

    private final BehaviorMap behaviors;
    private final PlayerKey initiator;

    private final GameInstanceTelemetry telemetry;

    private final GameEventListeners events = new GameEventListeners();
    private final GameStateMap state = new GameStateMap();

    private GameInstance(IGameDefinition definition, MinecraftServer server, GameMap map, BehaviorMap behaviors, PlayerKey initiator) {
        this.definition = definition;
        this.server = server;
        this.dimension = map.getDimension();
        this.map = map;

        this.allPlayers = new MutablePlayerSet(server);

        this.behaviors = behaviors;
        this.initiator = initiator;

        this.telemetry = Telemetry.INSTANCE.openMinigame(this);

        for (PlayerRole role : PlayerRole.ROLES) {
            MutablePlayerSet rolePlayers = new MutablePlayerSet(server);
            roles.put(role, rolePlayers);

            rolePlayers.addListener(new PlayerSet.Listeners() {
                @Override
                public void onAddPlayer(ServerPlayerEntity player) {
                    GameInstance.this.onAddPlayerToRole(player, role);
                }
            });
        }

        allPlayers.addListener(new PlayerSet.Listeners() {
            @Override
            public void onRemovePlayer(UUID id, @Nullable ServerPlayerEntity player) {
                GameInstance.this.onRemovePlayer(id, player);
            }
        });
    }

    public static CompletableFuture<GameResult<GameInstance>> start(
            IGameDefinition definition, MinecraftServer server, GameMap map, BehaviorMap behaviors,
            PlayerKey initiator, MinigameRegistrations registrations
    ) {
        GameInstance game = new GameInstance(definition, server, map, behaviors, initiator);

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

        for (ServerPlayerEntity player : participants) {
            game.addPlayer(player, PlayerRole.PARTICIPANT);
    		LoveTropicsNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), new ClientRoleMessage(PlayerRole.PARTICIPANT));
        }

        for (ServerPlayerEntity player : spectators) {
            game.addPlayer(player, PlayerRole.SPECTATOR);
            LoveTropicsNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), new ClientRoleMessage(PlayerRole.SPECTATOR));
        }

        map.getName().ifPresent(name -> game.getStatistics().getGlobal().set(StatisticKey.MAP, name));
        game.telemetry.start(game.getParticipants());

        return Scheduler.INSTANCE.submit(s -> {
            try {
                game.invoker(GameLifecycleEvents.START).start(game);
                return GameResult.ok(game);
            } catch (Exception e) {
                return GameResult.fromException("Failed to dispatch game start event", e);
            }
		}, 1);
	}

	@Override
	public GameStatus getStatus() {
		return GameStatus.ACTIVE;
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

    private void onRemovePlayer(UUID id, @Nullable ServerPlayerEntity player) {
        for (MutablePlayerSet rolePlayers : roles.values()) {
            rolePlayers.remove(id);
        }

        if (player != null) {
            try {
                invoker(GamePlayerEvents.LEAVE).onLeave(this, player);
            } catch (Exception e) {
                LoveTropics.LOGGER.warn("Failed to dispatch player leave event", e);
            }
        }
    }

    private void onAddPlayerToRole(ServerPlayerEntity player, PlayerRole role) {
        PlayerRole lastRole = null;

        // remove the player from any other roles
        for (PlayerRole otherRole : PlayerRole.ROLES) {
            if (otherRole != role && roles.get(otherRole).remove(player)) {
                lastRole = otherRole;
                break;
            }
        }

        if (lastRole != null) {
            onPlayerChangeRole(player, role, lastRole);
        }
    }

    private void onPlayerChangeRole(ServerPlayerEntity player, PlayerRole role, PlayerRole lastRole) {
        try {
            invoker(GamePlayerEvents.CHANGE_ROLE).onChangeRole(this, player, role, lastRole);
        } catch (Exception e) {
            LoveTropics.LOGGER.warn("Failed to dispatch player change role event", e);
        }
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

        roles.get(role).add(player);
    }

    @Override
    public boolean removePlayer(ServerPlayerEntity player) {
        return allPlayers.remove(player);
    }

    @Override
    public void addControlCommand(String name, ControlCommand command) {
        this.controlCommands.put(name, command);
    }

    @Override
    public void invokeControlCommand(String name, CommandSource source) throws CommandSyntaxException {
        ControlCommand command = this.controlCommands.get(name);
        if (command != null) {
            command.invoke(this, source);
        }
    }

    @Override
    public Stream<String> controlCommandsFor(CommandSource source) {
        return this.controlCommands.entrySet().stream()
                .filter(entry -> entry.getValue().canUse(this, source))
                .map(Map.Entry::getKey);
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
        return dimension;
    }

    @Override
    public MinecraftServer getServer() {
        return server;
    }

    @Override
    public ServerWorld getWorld() {
        return server.getWorld(dimension);
    }

    public void update() {
        ticks++;
    }

    @Override
    public long ticks()
    {
        return ticks;
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
    public GameStateMap getState() {
        return state;
    }

    @Override
    public PlayerKey getInitiator() {
        return initiator;
    }

    @Override
    public GameEventListeners getEvents() {
        return events;
    }

    @Override
    public void close() {
        map.close(this);
    }
}
