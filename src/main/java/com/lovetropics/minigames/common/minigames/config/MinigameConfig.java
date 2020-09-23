package com.lovetropics.minigames.common.minigames.config;

import com.lovetropics.minigames.common.minigames.behaviours.IMinigameBehavior;
import com.lovetropics.minigames.common.minigames.behaviours.IMinigameBehaviorType;
import com.lovetropics.minigames.common.minigames.behaviours.MinigameBehaviorTypes;
import com.mojang.datafixers.Dynamic;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.dimension.DimensionType;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public final class MinigameConfig {
    public final ResourceLocation id;
    public final String translationKey;
    public final DimensionType dimension;
    public final int minimumParticipants;
    public final int maximumParticipants;
    public final Map<IMinigameBehaviorType<?>, IMinigameBehavior> behaviors;

    public MinigameConfig(
            ResourceLocation id,
            String translationKey,
            DimensionType dimension,
            int minimumParticipants,
            int maximumParticipants,
            Map<IMinigameBehaviorType<?>, IMinigameBehavior> behaviors
    ) {
        this.id = id;
        this.translationKey = translationKey;
        this.dimension = dimension;
        this.minimumParticipants = minimumParticipants;
        this.maximumParticipants = maximumParticipants;
        this.behaviors = behaviors;
    }

    public static <T> MinigameConfig deserialize(ResourceLocation id, Dynamic<T> root) {
        String translationKey = root.get("translation_key").asString("");
        DimensionType dimension = DimensionType.byName(new ResourceLocation(root.get("dimension").asString("")));

        int minimumParticipants = root.get("minimum_participants").asInt(1);
        int maximumParticipants = root.get("maximum_participants").asInt(100);

        Map<IMinigameBehaviorType<?>, IMinigameBehavior> behaviors = root.get("behaviors")
        		.asList(MinigameConfig::deserializeBehavior)
        		.stream()
                .filter(Objects::nonNull)
        		.collect(Collectors.toMap(Pair::getLeft, Pair::getRight));

        return new MinigameConfig(
                id,
                translationKey,
                dimension,
                minimumParticipants,
                maximumParticipants,
                behaviors
        );
    }

    private static <T extends IMinigameBehavior, D> Pair<IMinigameBehaviorType<T>, T> deserializeBehavior(Dynamic<D> root) {
        ResourceLocation type = new ResourceLocation(root.get("type").asString(""));
        @SuppressWarnings("unchecked")
		IMinigameBehaviorType<T> behavior = (IMinigameBehaviorType<T>) MinigameBehaviorTypes.MINIGAME_BEHAVIOURS_REGISTRY.get().getValue(type);

        if (behavior != null) {
            return Pair.of(behavior, behavior.create(root));
        }

        System.out.println("Type '" + type + "' is not valid!");
        return null;
    }
}
