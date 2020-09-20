package com.lovetropics.minigames.common.minigames;

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
import java.util.function.Consumer;

/**
 * Default implementation of a minigame instance. Simple and naive
 * solution to holding state of players that are currently part
 * of the minigame instance, as well as the definition that is being
 * used to specify the rulesets for the minigame.
 */
public class MinigameInstance implements IMinigameInstance
{
    private IMinigameDefinition definition;

    private final PlayerSet participants;
    private final PlayerSet spectators;
    private final PlayerSet allPlayers;

    private CommandSource commandSource;

    private ServerWorld world;

    private final Map<String, Consumer<CommandSource>> controlCommands = new Object2ObjectOpenHashMap<>();

    public MinigameInstance(IMinigameDefinition definition, ServerWorld world) {
        this.definition = definition;
        this.world = world;

        MinecraftServer server = world.getServer();
        this.participants = new PlayerSet(server);
        this.spectators = new PlayerSet(server);
        this.allPlayers = new PlayerSet(server);
    }

    @Override
    public IMinigameDefinition getDefinition() {
        return this.definition;
    }

    @Override
    public void addParticipant(ServerPlayerEntity player) {
        if (this.spectators.contains(player)) {
            throw new IllegalArgumentException("Player already exists in this minigame instance as a spectator! "
                    + player.getDisplayName().getFormattedText());
        }

        if (this.participants.contains(player)) {
            throw new IllegalArgumentException("Player already exists in this minigame instance! "
                    + player.getDisplayName().getFormattedText());
        }

        this.participants.add(player);
        this.allPlayers.add(player);
    }

    @Override
    public void removeParticipant(ServerPlayerEntity player) {
        if (!this.participants.contains(player)) {
            throw new IllegalArgumentException("Player doesn't exist in this minigame instance! "
                    + player.getDisplayName().getFormattedText());
        }

        this.participants.remove(player);
        this.allPlayers.remove(player);
    }

    @Override
    public void addSpectator(ServerPlayerEntity player) {
        if (this.participants.contains(player.getUniqueID())) {
            throw new IllegalArgumentException("Player already exists in this minigame instance as a non-spectator! "
                    + player.getDisplayName().getFormattedText());
        }

        if (this.spectators.contains(player.getUniqueID())) {
            throw new IllegalArgumentException("Player already exists in this minigame instance as a spectator! "
                    + player.getDisplayName().getFormattedText());
        }

        this.spectators.add(player);
        this.allPlayers.add(player);
    }

    @Override
    public void removeSpectator(ServerPlayerEntity player) {
        if (!this.spectators.contains(player.getUniqueID())) {
            throw new IllegalArgumentException("Player doesn't exist in this minigame instance as a spectator! "
                    + player.getDisplayName().getFormattedText());
        }

        this.spectators.remove(player.getUniqueID());
        this.allPlayers.remove(player.getUniqueID());
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
    public PlayerSet getParticipants() {
        return this.participants;
    }

    @Override
    public PlayerSet getAllPlayers() {
        return this.allPlayers;
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
