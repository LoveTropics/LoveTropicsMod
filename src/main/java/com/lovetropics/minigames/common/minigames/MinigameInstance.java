package com.lovetropics.minigames.common.minigames;

import com.lovetropics.minigames.client.minigame.ClientRoleMessage;
import com.lovetropics.minigames.common.Scheduler;
import com.lovetropics.minigames.common.map.MapRegions;
import com.lovetropics.minigames.common.minigames.behaviours.BehaviorMap;
import com.lovetropics.minigames.common.minigames.behaviours.IMinigameBehavior;
import com.lovetropics.minigames.common.minigames.behaviours.IMinigameBehaviorType;
import com.lovetropics.minigames.common.minigames.polling.MinigameRegistrations;
import com.lovetropics.minigames.common.minigames.statistics.MinigameStatistics;
import com.lovetropics.minigames.common.minigames.statistics.PlayerKey;
import com.lovetropics.minigames.common.minigames.statistics.StatisticKey;
import com.lovetropics.minigames.common.network.LTNetwork;
import com.lovetropics.minigames.common.telemetry.MinigameInstanceTelemetry;
import com.lovetropics.minigames.common.telemetry.Telemetry;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.command.CommandSource;
import net.minecraft.command.ICommandSource;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Unit;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.fml.network.PacketDistributor;

import javax.annotation.Nullable;
import java.util.*;
import java.util.concurrent.CompletableFuture;

/**
 * Default implementation of a minigame instance. Simple and naive
 * solution to holding state of players that are currently part
 * of the minigame instance, as well as the definition that is being
 * used to specify the rulesets for the minigame.
 */
public class MinigameInstance implements IMinigameInstance
{
    private final MinecraftServer server;
    private final IMinigameDefinition definition;
    private final DimensionType dimension;

    private final MutablePlayerSet allPlayers;
    private final EnumMap<PlayerRole, MutablePlayerSet> roles = new EnumMap<>(PlayerRole.class);

    private final MapRegions mapRegions;
    private final MinigameStatistics statistics = new MinigameStatistics();

    private CommandSource commandSource;

    private final Map<String, ControlCommandHandler> controlCommands = new Object2ObjectOpenHashMap<>();
    private long ticks;

    private final BehaviorMap behaviors;

    private final MinigameInstanceTelemetry telemetry;

    private MinigameInstance(IMinigameDefinition definition, MinecraftServer server, MinigameMap map, BehaviorMap behaviors, PlayerKey initiator) {
        this.definition = definition;
        this.server = server;
        this.dimension = map.getDimension();
        this.mapRegions = map.getMapRegions();

        this.allPlayers = new MutablePlayerSet(server);

        this.behaviors = behaviors;

        this.telemetry = Telemetry.INSTANCE.openMinigame(definition, initiator);

        for (PlayerRole role : PlayerRole.ROLES) {
            MutablePlayerSet rolePlayers = new MutablePlayerSet(server);
            roles.put(role, rolePlayers);

            rolePlayers.addListener(new PlayerSet.Listeners() {
                @Override
                public void onAddPlayer(ServerPlayerEntity player) {
                    MinigameInstance.this.onAddPlayerToRole(player, role);
                }
            });
        }

        allPlayers.addListener(new PlayerSet.Listeners() {
            @Override
            public void onRemovePlayer(UUID id, @Nullable ServerPlayerEntity player) {
                MinigameInstance.this.onRemovePlayer(id, player);
            }
        });
    }

    public static CompletableFuture<MinigameResult<MinigameInstance>> start(
            IMinigameDefinition definition, MinecraftServer server, MinigameMap map, BehaviorMap behaviors,
            PlayerKey initiator, MinigameRegistrations registrations
    ) {
        MinigameInstance minigame = new MinigameInstance(definition, server, map, behaviors, initiator);

        MinigameResult<Unit> result = minigame.validateBehaviors();
        if (result.isError()) {
            return CompletableFuture.completedFuture(result.castError());
        }

        result = minigame.dispatchToBehaviors(IMinigameBehavior::onConstruct);
        if (result.isError()) {
            return CompletableFuture.completedFuture(result.castError());
        }

        List<ServerPlayerEntity> participants = new ArrayList<>();
        List<ServerPlayerEntity> spectators = new ArrayList<>();

        registrations.collectInto(server, participants, spectators, definition.getMaximumParticipantCount());

        minigame.dispatchToBehaviors((b, m) -> b.assignPlayerRoles(m, participants, spectators));

        for (ServerPlayerEntity player : participants) {
            minigame.addPlayer(player, PlayerRole.PARTICIPANT);
    		LTNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), new ClientRoleMessage(PlayerRole.PARTICIPANT));
        }

        for (ServerPlayerEntity player : spectators) {
            minigame.addPlayer(player, PlayerRole.SPECTATOR);
            LTNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), new ClientRoleMessage(PlayerRole.SPECTATOR));
        }

        map.getName().ifPresent(name -> minigame.getStatistics().getGlobal().set(StatisticKey.MAP, name));
        minigame.telemetry.start(minigame.getParticipants());

        return Scheduler.INSTANCE.submit(s -> {
            MinigameResult<Unit> startResult = minigame.dispatchToBehaviors(IMinigameBehavior::onStart);
            if (startResult.isError()) {
                return startResult.castError();
            }

            return MinigameResult.ok(minigame);
		}, 1);
	}

	@Override
	public MinigameStatus getStatus() {
		return MinigameStatus.ACTIVE;
	}

    private MinigameResult<Unit> validateBehaviors() {
        for (IMinigameBehavior behavior : getBehaviors()) {
            for (IMinigameBehaviorType<? extends IMinigameBehavior> dependency : behavior.dependencies()) {
                if (getBehaviors(dependency).isEmpty()) {
                    return MinigameResult.error(new StringTextComponent(behavior + " is missing dependency on " + dependency + "!"));
                }
            }
        }
        return dispatchToBehaviors(IMinigameBehavior::validateBehavior);
    }

    private void onRemovePlayer(UUID id, @Nullable ServerPlayerEntity player) {
        for (MutablePlayerSet rolePlayers : roles.values()) {
            rolePlayers.remove(id);
        }

        if (player != null) {
            for (IMinigameBehavior behavior : behaviors.getBehaviors()) {
                behavior.onPlayerLeave(MinigameInstance.this, player);
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
        for (IMinigameBehavior behavior : behaviors.getBehaviors()) {
            behavior.onPlayerChangeRole(this, player, role, lastRole);
        }
    }

    @Override
    public IMinigameDefinition getDefinition() {
        return this.definition;
    }

    @Override
    public Collection<IMinigameBehavior> getBehaviors() {
        return behaviors.getBehaviors();
    }

    @Override
    public <T extends IMinigameBehavior> Collection<T> getBehaviors(IMinigameBehaviorType<T> type) {
        return behaviors.getBehaviors(type);
    }

    @Override
    public void addPlayer(ServerPlayerEntity player, PlayerRole role) {
        if (!allPlayers.contains(player)) {
            allPlayers.add(player);

            for (IMinigameBehavior behavior : behaviors.getBehaviors()) {
                behavior.onPlayerJoin(this, player, role);
            }
        }

        roles.get(role).add(player);
    }

    @Override
    public void removePlayer(ServerPlayerEntity player) {
        allPlayers.remove(player);
    }

    @Override
    public void addControlCommand(String name, ControlCommandHandler task) {
        this.controlCommands.put(name, task);
    }

    @Override
    public void invokeControlCommand(String name, CommandSource source) throws CommandSyntaxException {
        ControlCommandHandler task = this.controlCommands.get(name);
        if (task != null) {
            task.run(source);
        }
    }

    @Override
    public Set<String> getControlCommands() {
        return this.controlCommands.keySet();
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
        return mapRegions;
    }

    @Override
    public CommandSource getCommandSource() {
        if (this.commandSource == null) {
            String unlocalizedName = definition.getUnlocalizedName();
            ITextComponent text = new StringTextComponent(unlocalizedName);
            ServerWorld world = getWorld();
            this.commandSource = new CommandSource(ICommandSource.DUMMY, Vec3d.ZERO, Vec2f.ZERO, world, 2, unlocalizedName, text, this.server, null);
        }

        return this.commandSource;
    }

    @Override
    public DimensionType getDimension() {
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
    public MinigameInstanceTelemetry getTelemetry() {
        return telemetry;
    }
}
