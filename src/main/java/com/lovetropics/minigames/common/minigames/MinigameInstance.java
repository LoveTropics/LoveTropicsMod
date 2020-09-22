package com.lovetropics.minigames.common.minigames;

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
    private final ServerWorld world;
    private final IMinigameDefinition definition;

    private final MutablePlayerSet allPlayers;
    private final EnumMap<PlayerRole, MutablePlayerSet> roles = new EnumMap<>(PlayerRole.class);

    private CommandSource commandSource;

    private final Map<String, Consumer<CommandSource>> controlCommands = new Object2ObjectOpenHashMap<>();

    public MinigameInstance(IMinigameDefinition definition, ServerWorld world) {
        this.definition = definition;
        this.world = world;

        MinecraftServer server = world.getServer();
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
        ServerPlayerEntity player = this.world.getServer().getPlayerList().getPlayerByUUID(id);
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
        // remove the player from any other roles
        for (PlayerRole otherRole : PlayerRole.ROLES) {
            if (otherRole != role) {
                roles.get(role).remove(player);
            }
        }

        for (IMinigameBehavior behavior : definition.getAllBehaviours()) {
            behavior.onPlayerJoin(this, player, role);
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
    public PlayerSet getPlayersForRule(PlayerRole role) {
        return roles.get(role);
    }

    @Override
    public CommandSource getCommandSource() {
        if (this.commandSource == null) {
            String s = this.getDefinition().getUnlocalizedName();
            ITextComponent text = new StringTextComponent(s);
            this.commandSource = new CommandSource(ICommandSource.DUMMY, Vec3d.ZERO, Vec2f.ZERO, this.world, 2, s, text, this.world.getServer(), null);
        }

        return this.commandSource;
    }
}
