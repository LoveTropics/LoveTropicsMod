package com.lovetropics.minigames.common.minigames;

import com.lovetropics.minigames.common.map.MapRegions;
import com.lovetropics.minigames.common.minigames.behaviours.IMinigameBehavior;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.command.CommandSource;
import net.minecraft.command.ICommandSource;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.server.ServerWorld;

import java.util.EnumMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;

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

    private final MutablePlayerSet allPlayers;
    private final EnumMap<PlayerRole, MutablePlayerSet> roles = new EnumMap<>(PlayerRole.class);

    private final MapRegions mapRegions = new MapRegions();

    private CommandSource commandSource;

    private final Map<String, Consumer<CommandSource>> controlCommands = new Object2ObjectOpenHashMap<>();

    public MinigameInstance(IMinigameDefinition definition, MinecraftServer server) {
        this.definition = definition;
        this.server = server;

        this.allPlayers = new MutablePlayerSet(server);

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
            public void onRemovePlayer(UUID id) {
                MinigameInstance.this.onRemovePlayer(id);
            }
        });
    }

    private void onRemovePlayer(UUID id) {
        ServerPlayerEntity player = this.server.getPlayerList().getPlayerByUUID(id);
        if (player == null) {
            return;
        }

        for (MutablePlayerSet rolePlayers : roles.values()) {
            rolePlayers.remove(id);
        }

        for (IMinigameBehavior behavior : definition.getAllBehaviours()) {
            behavior.onPlayerLeave(MinigameInstance.this, player);
        }
    }

    private void onAddPlayerToRole(ServerPlayerEntity player, PlayerRole role) {
        boolean hadRole = false;

        // remove the player from any other roles
        for (PlayerRole otherRole : PlayerRole.ROLES) {
            if (otherRole != role) {
                roles.get(role).remove(player);
                hadRole = true;
            }
        }

        if (hadRole) {
            for (IMinigameBehavior behavior : definition.getAllBehaviours()) {
                behavior.onPlayerChangeRole(this, player, role);
            }
        }
    }

    @Override
    public IMinigameDefinition getDefinition() {
        return this.definition;
    }

    @Override
    public void addPlayer(ServerPlayerEntity player, PlayerRole role) {
        if (!allPlayers.contains(player)) {
            allPlayers.add(player);

            for (IMinigameBehavior behavior : definition.getAllBehaviours()) {
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
    public void addControlCommand(String name, Consumer<CommandSource> task) {
        this.controlCommands.put(name, task);
    }

    @Override
    public void invokeControlCommand(String name, CommandSource source) {
        Consumer<CommandSource> task = this.controlCommands.get(name);
        if (task != null) {
            task.accept(source);
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
            ServerWorld world = server.getWorld(definition.getDimension());
            this.commandSource = new CommandSource(ICommandSource.DUMMY, Vec3d.ZERO, Vec2f.ZERO, world, 2, unlocalizedName, text, this.server, null);
        }

        return this.commandSource;
    }

    @Override
    public ServerWorld getWorld() {
        return server.getWorld(definition.getDimension());
    }
}
