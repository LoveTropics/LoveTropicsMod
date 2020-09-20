package com.lovetropics.minigames.common.minigames.config;

import com.google.gson.JsonElement;
import com.lovetropics.minigames.common.minigames.behaviours.IMinigameBehaviorType;
import com.lovetropics.minigames.common.minigames.behaviours.IMinigameBehavior;
import com.lovetropics.minigames.common.minigames.behaviours.MinigameBehaviorTypes;
import com.mojang.datafixers.Dynamic;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.GameType;
import net.minecraft.world.dimension.DimensionType;

import java.util.List;

public final class MinigameConfig {
    public final ResourceLocation id;
    public final String translationKey;
    public final DimensionType dimension;
    public final GameType participantGameType;
    public final GameType spectatorGameType;
    public final BlockPos spectatorPosition;
    public final BlockPos respawnPosition;
    public final int minimumParticipants;
    public final int maximumParticipants;
    public final List<IMinigameBehavior> behaviors;

    public MinigameConfig(
            ResourceLocation id,
            String translationKey,
            DimensionType dimension,
            GameType participantGameType,
            GameType spectatorGameType,
            BlockPos spectatorPosition,
            BlockPos respawnPosition,
            int minimumParticipants,
            int maximumParticipants,
            List<IMinigameBehavior> behaviors
    ) {
        this.id = id;
        this.translationKey = translationKey;
        this.dimension = dimension;
        this.participantGameType = participantGameType;
        this.spectatorGameType = spectatorGameType;
        this.spectatorPosition = spectatorPosition;
        this.respawnPosition = respawnPosition;
        this.minimumParticipants = minimumParticipants;
        this.maximumParticipants = maximumParticipants;
        this.behaviors = behaviors;
    }

    public static MinigameConfig deserialize(ResourceLocation id, Dynamic<JsonElement> root) {
        String translationKey = root.get("translation_key").asString("");
        DimensionType dimension = DimensionType.byName(new ResourceLocation(root.get("dimension").asString("")));
        GameType participantGameType = GameType.getByName(root.get("participant_game_type").asString(""));
        GameType spectatorGameType = GameType.getByName(root.get("spectator_game_type").asString(""));

        BlockPos spectatorPosition = BlockPos.deserialize(root.get("spectator_position").orElseEmptyMap());
        BlockPos respawnPosition = BlockPos.deserialize(root.get("respawn_position").orElseEmptyMap());

        int minimumParticipants = root.get("minimum_participants").asInt(1);
        int maximumParticipants = root.get("maximum_participants").asInt(100);

        List<IMinigameBehavior> behaviors = root.get("behaviors").asList(MinigameConfig::deserializeBehavior);

        return new MinigameConfig(
                id,
                translationKey,
                dimension,
                participantGameType,
                spectatorGameType,
                spectatorPosition,
                respawnPosition,
                minimumParticipants,
                maximumParticipants,
                behaviors
        );
    }

    // TODO: implement retrieval and parsing from registry
    private static IMinigameBehavior deserializeBehavior(Dynamic<JsonElement> root) {
        ResourceLocation type = new ResourceLocation(root.get("type").asString(""));
        IMinigameBehaviorType behaviour = MinigameBehaviorTypes.MINIGAME_BEHAVIOURS_REGISTRY.get().getValue(type);

        if (behaviour != null) {
            return behaviour.getInstanceFactory().apply(root);
        }

        System.out.println("Type is not valid!");

        return null;
    }
}
