package com.lovetropics.minigames.common.minigames;

import com.lovetropics.minigames.common.map.MapRegions;
import com.lovetropics.minigames.common.minigames.behaviours.IMinigameBehavior;
import com.lovetropics.minigames.common.minigames.behaviours.IMinigameBehaviorType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.command.CommandSource;
import net.minecraft.command.ICommandSource;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.server.ServerWorld;

import javax.annotation.Nullable;
import java.util.*;

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
    private DimensionType dimension;

    private final MutablePlayerSet allPlayers;
    private final EnumMap<PlayerRole, MutablePlayerSet> roles = new EnumMap<>(PlayerRole.class);

    private final MapRegions mapRegions = new MapRegions();

    private CommandSource commandSource;

    private final Map<String, ControlCommandHandler> controlCommands = new Object2ObjectOpenHashMap<>();
    private long ticks;

    private final Map<IMinigameBehaviorType<?>, IMinigameBehavior> behaviors;

    public MinigameInstance(IMinigameDefinition definition, MinecraftServer server) {
        this.definition = definition;
        this.server = server;

        this.allPlayers = new MutablePlayerSet(server);

        this.behaviors = definition.createBehaviors();

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

    private void onRemovePlayer(UUID id, @Nullable ServerPlayerEntity player) {
        for (MutablePlayerSet rolePlayers : roles.values()) {
            rolePlayers.remove(id);
        }

        if (player != null) {
            for (IMinigameBehavior behavior : behaviors.values()) {
                behavior.onPlayerLeave(MinigameInstance.this, player);
            }
        }
    }

    private void onAddPlayerToRole(ServerPlayerEntity player, PlayerRole role) {
        boolean hadRole = false;

        // remove the player from any other roles
        for (PlayerRole otherRole : PlayerRole.ROLES) {
            if (otherRole != role) {
                roles.get(otherRole).remove(player);
                hadRole = true;
            }
        }

        if (hadRole) {
            for (IMinigameBehavior behavior : behaviors.values()) {
                behavior.onPlayerChangeRole(this, player, role);
            }
        }
    }

    @Override
    public IMinigameDefinition getDefinition() {
        return this.definition;
    }

    @Override
    public Collection<IMinigameBehavior> getAllBehaviours() {
        return behaviors.values();
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends IMinigameBehavior> Optional<T> getBehavior(IMinigameBehaviorType<T> type) {
        return Optional.ofNullable((T) behaviors.get(type));
    }

    @Override
    public void addPlayer(ServerPlayerEntity player, PlayerRole role) {
        if (!allPlayers.contains(player)) {
            allPlayers.add(player);

            for (IMinigameBehavior behavior : behaviors.values()) {
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

    public void setDimension(DimensionType dimension) {
        this.dimension = dimension;
    }
}
