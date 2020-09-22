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
    private final MutablePlayerSet participants;
    private final MutablePlayerSet spectators;

    private CommandSource commandSource;

    private final Map<String, Consumer<CommandSource>> controlCommands = new Object2ObjectOpenHashMap<>();

    public MinigameInstance(IMinigameDefinition definition, ServerWorld world) {
        this.definition = definition;
        this.world = world;

        MinecraftServer server = world.getServer();
        this.participants = new MutablePlayerSet(server);
        this.spectators = new MutablePlayerSet(server);
        this.allPlayers = new MutablePlayerSet(server);
        
        this.participants.addListener(new PlayerSet.Listeners() {
            @Override
            public void onAddPlayer(ServerPlayerEntity player) {
                spectators.remove(player);
                for (IMinigameBehavior behavior : definition.getAllBehaviours()) {
                    behavior.onAddParticipant(MinigameInstance.this, player);
                }
            }

            @Override
            public void onRemovePlayer(UUID id) {
                ServerPlayerEntity player = server.getPlayerList().getPlayerByUUID(id);
                if (player != null) {
                    for (IMinigameBehavior behavior : definition.getAllBehaviours()) {
                        behavior.onRemoveParticipant(MinigameInstance.this, player);
                    }
                }
            }
        });
        
        this.spectators.addListener(new PlayerSet.Listeners() {
            @Override
            public void onAddPlayer(ServerPlayerEntity player) {
                participants.remove(player);
                for (IMinigameBehavior behavior : definition.getAllBehaviours()) {
                    behavior.onAddSpectator(MinigameInstance.this, player);
                }
            }

            @Override
            public void onRemovePlayer(UUID id) {
                ServerPlayerEntity player = server.getPlayerList().getPlayerByUUID(id);
                if (player != null) {
                    for (IMinigameBehavior behavior : definition.getAllBehaviours()) {
                        behavior.onRemoveSpectator(MinigameInstance.this, player);
                    }
                }
            }
        });

        this.allPlayers.addListener(new PlayerSet.Listeners() {
            @Override
            public void onAddPlayer(ServerPlayerEntity player) {
                MinigameInstance.this.onAddPlayer(player);
            }

            @Override
            public void onRemovePlayer(UUID id) {
                MinigameInstance.this.onRemovePlayer(id);
                participants.remove(id);
                spectators.remove(id);
            }
        });
    }

    private void onAddPlayer(ServerPlayerEntity player) {
        for (IMinigameBehavior behavior : definition.getAllBehaviours()) {
            behavior.onAddPlayer(MinigameInstance.this, player);
        }
    }

    private void onRemovePlayer(UUID id) {
        ServerPlayerEntity player = this.world.getServer().getPlayerList().getPlayerByUUID(id);
        if (player == null) {
            return;
        }

        for (IMinigameBehavior behavior : definition.getAllBehaviours()) {
            behavior.onRemovePlayer(MinigameInstance.this, player);
        }
    }

    @Override
    public IMinigameDefinition getDefinition() {
        return this.definition;
    }

    @Override
    public void makeParticipant(ServerPlayerEntity player) throws IllegalArgumentException {
        if (!allPlayers.contains(player)) {
            throw new IllegalArgumentException("Player does not exist in this minigame instance! "
                    + player.getDisplayName().getFormattedText());
        }

        participants.add(player);
        spectators.remove(player);
    }

    @Override
    public void makeSpectator(ServerPlayerEntity player) throws IllegalArgumentException {
        if (!allPlayers.contains(player)) {
            throw new IllegalArgumentException("Player does not exist in this minigame instance! "
                    + player.getDisplayName().getFormattedText());
        }

        spectators.add(player);
        participants.remove(player);
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
    public MutablePlayerSet getAllPlayers() {
        return this.allPlayers;
    }

    @Override
    public PlayerSet getParticipants() {
        return this.participants;
    }

    @Override
    public PlayerSet getSpectators() {
        return this.spectators;
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
