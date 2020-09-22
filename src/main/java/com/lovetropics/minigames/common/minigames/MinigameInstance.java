package com.lovetropics.minigames.common.minigames;

import com.google.common.collect.Maps;
import com.lovetropics.minigames.common.dimension.DimensionUtils;
import com.lovetropics.minigames.common.minigames.behaviours.MinigameBehaviorTypes;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.command.CommandSource;
import net.minecraft.command.ICommandSource;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
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

    private int teleportedParticipantIndex;

    /**
     * Cache used to know what state the player was in before teleporting into a minigame.
     */
    private final Map<UUID, MinigamePlayerCache> playerCache = Maps.newHashMap();

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
                teleportPlayerIntoInstance(player);
            }
        });
        
        this.spectators.addListener(new PlayerSet.Listeners() {
            @Override
            public void onAddPlayer(ServerPlayerEntity player) {
                participants.remove(player);
                teleportSpectatorIntoInstance(player);
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
                spectators.remove(id);
                allPlayers.remove(id);
            }
        });
    }

    private void onAddPlayer(ServerPlayerEntity player) {
        MinigamePlayerCache playerCache = new MinigamePlayerCache(player);
        playerCache.resetPlayerStats(player);

        this.playerCache.put(player.getUniqueID(), playerCache);
    }

    private void onRemovePlayer(UUID id) {
        ServerPlayerEntity player = this.world.getServer().getPlayerList().getPlayerByUUID(id);
        if (player == null) {
            return;
        }

        // try to restore the player to their old state
        MinigamePlayerCache playerCache = this.playerCache.remove(id);
        if (playerCache != null) {
            playerCache.teleportBack(player);
        }
    }

    private void teleportPlayerIntoInstance(ServerPlayerEntity player) {
        // TODO temporary
        BlockPos[] positions = definition.getBehavior(MinigameBehaviorTypes.POSITION_PARTICIPANTS.get())
                .map(b -> b.getStartPositions())
                .orElseThrow(IllegalStateException::new);

        // Ensure length of participant positions matches the maximum participant count.
        if (positions.length != definition.getMaximumParticipantCount()) {
            throw new IllegalStateException("The participant positions length doesn't match the" +
                    "maximum participant count defined by the following minigame definition! " + definition.getID());
        }

        BlockPos teleportTo = positions[teleportedParticipantIndex++ % positions.length];

        DimensionUtils.teleportPlayerNoPortal(player, definition.getDimension(), teleportTo);
        player.setGameType(definition.getParticipantGameType());
    }

    /**
     * Teleports the spectator into the dimension specified by the minigame definition.
     * Will set the position of the player to the location specified by the definition
     * for spectators. Sets player GameType to SPECTATOR.
     * @param player The spectator to teleport into the instance.
     */
    private void teleportSpectatorIntoInstance(ServerPlayerEntity player) {
        BlockPos teleportTo = definition.getSpectatorPosition();

        DimensionUtils.teleportPlayerNoPortal(player, definition.getDimension(), teleportTo);
        player.setGameType(definition.getSpectatorGameType());
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
