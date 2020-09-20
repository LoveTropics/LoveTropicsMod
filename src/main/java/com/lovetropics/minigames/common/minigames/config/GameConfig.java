package com.lovetropics.minigames.common.minigames.config;

import com.mojang.datafixers.Dynamic;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.GameType;
import net.minecraft.world.dimension.DimensionType;

public final class GameConfig {
    public final ResourceLocation id;
    public final String translationKey;
    public final DimensionType dimension;
    public final GameType participantGameType;
    public final GameType spectatorGameType;
    public final BlockPos spectatorPosition;
    public final BlockPos respawnPosition;
    public final int minimumParticipants;
    public final int maximumParticipants;
//    public final GameBehavior[] behavior;

    public GameConfig(
            ResourceLocation id,
            String translationKey,
            DimensionType dimension,
            GameType participantGameType,
            GameType spectatorGameType,
            BlockPos spectatorPosition,
            BlockPos respawnPosition,
            int minimumParticipants,
            int maximumParticipants
//            GameBehavior[] behavior
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
//        this.behavior = behavior;
    }

    public static <T> GameConfig deserialize(ResourceLocation id, Dynamic<T> root) {
        String translationKey = root.get("translation_key").asString("");
        DimensionType dimension = DimensionType.byName(new ResourceLocation(root.get("dimension").asString("")));
        GameType participantGameType = GameType.getByName(root.get("participant_game_type").asString(""));
        GameType spectatorGameType = GameType.getByName(root.get("spectator_game_type").asString(""));

        BlockPos spectatorPosition = BlockPos.deserialize(root.get("spectator_position").orElseEmptyMap());
        BlockPos respawnPosition = BlockPos.deserialize(root.get("respawn_position").orElseEmptyMap());

        int minimumParticipants = root.get("minimum_participants").asInt(1);
        int maximumParticipants = root.get("maximum_participants").asInt(100);

//        GameBehavior[] behavior = root.get("behavior").asList(GameConfig::deserializeBehavior).toArray(new GameBehavior[0]);

        return new GameConfig(
                id,
                translationKey,
                dimension,
                participantGameType,
                spectatorGameType,
                spectatorPosition,
                respawnPosition,
                minimumParticipants,
                maximumParticipants
//                behavior
        );
    }

    // TODO: implement retrieval and parsing from registry
    // private static <T> GameBehavior deserializeBehavior(Dynamic<T> root) {
    //     ResourceLocation type = new ResourceLocation(root.get("type").asString(""));
    // }
}
