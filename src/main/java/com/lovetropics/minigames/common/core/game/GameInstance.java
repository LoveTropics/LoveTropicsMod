package com.lovetropics.minigames.common.core.game;

import com.lovetropics.minigames.client.minigame.ClientRoleMessage;
import com.lovetropics.minigames.common.util.Scheduler;
import com.lovetropics.minigames.common.core.game.map.GameMap;
import com.lovetropics.minigames.common.core.map.MapRegions;
import com.lovetropics.minigames.common.core.game.behavior.BehaviorMap;
import com.lovetropics.minigames.common.core.game.behavior.IGameBehavior;
import com.lovetropics.minigames.common.core.game.behavior.GameBehaviorType;
import com.lovetropics.minigames.common.core.game.polling.MinigameRegistrations;
import com.lovetropics.minigames.common.core.game.statistics.MinigameStatistics;
import com.lovetropics.minigames.common.core.game.statistics.PlayerKey;
import com.lovetropics.minigames.common.core.game.statistics.StatisticKey;
import com.lovetropics.minigames.common.core.network.LoveTropicsNetwork;
import com.lovetropics.minigames.common.core.integration.GameInstanceTelemetry;
import com.lovetropics.minigames.common.core.integration.Telemetry;
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
import net.minecraft.util.text.StringTextComponent;
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
    private final MinigameStatistics statistics = new MinigameStatistics();

    private CommandSource commandSource;

    private final Map<String, ControlCommand> controlCommands = new Object2ObjectOpenHashMap<>();
    private long ticks;

    private final BehaviorMap behaviors;
    private final PlayerKey initiator;

    private final GameInstanceTelemetry telemetry;

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
        GameInstance minigame = new GameInstance(definition, server, map, behaviors, initiator);

        GameResult<Unit> result = minigame.validateBehaviors();
        if (result.isError()) {
            return CompletableFuture.completedFuture(result.castError());
        }

        result = minigame.dispatchToBehaviors(IGameBehavior::onConstruct);
        if (result.isError()) {
            return CompletableFuture.completedFuture(result.castError());
        }

        List<ServerPlayerEntity> participants = new ArrayList<>();
        List<ServerPlayerEntity> spectators = new ArrayList<>();

        registrations.collectInto(server, participants, spectators, definition.getMaximumParticipantCount());

        minigame.dispatchToBehaviors((b, m) -> b.assignPlayerRoles(m, participants, spectators));

        for (ServerPlayerEntity player : participants) {
            minigame.addPlayer(player, PlayerRole.PARTICIPANT);
    		LoveTropicsNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), new ClientRoleMessage(PlayerRole.PARTICIPANT));
        }

        for (ServerPlayerEntity player : spectators) {
            minigame.addPlayer(player, PlayerRole.SPECTATOR);
            LoveTropicsNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), new ClientRoleMessage(PlayerRole.SPECTATOR));
        }

        map.getName().ifPresent(name -> minigame.getStatistics().getGlobal().set(StatisticKey.MAP, name));
        minigame.telemetry.start(minigame.getParticipants());

        return Scheduler.INSTANCE.submit(s -> {
            GameResult<Unit> startResult = minigame.dispatchToBehaviors(IGameBehavior::onStart);
            if (startResult.isError()) {
                return startResult.castError();
            }

            return GameResult.ok(minigame);
		}, 1);
	}

	@Override
	public GameStatus getStatus() {
		return GameStatus.ACTIVE;
	}

    private GameResult<Unit> validateBehaviors() {
        for (IGameBehavior behavior : getBehaviors()) {
            for (GameBehaviorType<? extends IGameBehavior> dependency : behavior.dependencies()) {
                if (getBehaviors(dependency).isEmpty()) {
                    return GameResult.error(new StringTextComponent(behavior + " is missing dependency on " + dependency + "!"));
                }
            }
        }
        return dispatchToBehaviors(IGameBehavior::validateBehavior);
    }

    private void onRemovePlayer(UUID id, @Nullable ServerPlayerEntity player) {
        for (MutablePlayerSet rolePlayers : roles.values()) {
            rolePlayers.remove(id);
        }

        if (player != null) {
            for (IGameBehavior behavior : behaviors.getBehaviors()) {
                behavior.onPlayerLeave(GameInstance.this, player);
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
        for (IGameBehavior behavior : behaviors.getBehaviors()) {
            behavior.onPlayerChangeRole(this, player, role, lastRole);
        }
    }

    @Override
    public IGameDefinition getDefinition() {
        return this.definition;
    }

    @Override
    public Collection<IGameBehavior> getBehaviors() {
        return behaviors.getBehaviors();
    }

    @Override
    public <T extends IGameBehavior> Collection<T> getBehaviors(GameBehaviorType<T> type) {
        return behaviors.getBehaviors(type);
    }

    @Override
    public void addPlayer(ServerPlayerEntity player, PlayerRole role) {
        if (!allPlayers.contains(player)) {
            allPlayers.add(player);

            for (IGameBehavior behavior : behaviors.getBehaviors()) {
                behavior.onPlayerJoin(this, player, role);
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
    public PlayerSet getPlayers() {
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
            String unlocalizedName = definition.getUnlocalizedName();
            ITextComponent text = new StringTextComponent(unlocalizedName);
            ServerWorld world = getWorld();
            this.commandSource = new CommandSource(ICommandSource.DUMMY, Vector3d.ZERO, Vector2f.ZERO, world, 4, unlocalizedName, text, this.server, null);
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

    @Override
    public void update() {
        ticks++;
    }

    @Override
    public long ticks()
    {
        return ticks;
    }

    @Override
    public MinigameStatistics getStatistics() {
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
