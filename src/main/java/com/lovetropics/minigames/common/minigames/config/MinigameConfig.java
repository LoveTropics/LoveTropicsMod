package com.lovetropics.minigames.common.minigames.config;

import com.lovetropics.minigames.common.minigames.behaviours.IMinigameBehavior;
import com.lovetropics.minigames.common.minigames.behaviours.IMinigameBehaviorType;
import com.lovetropics.minigames.common.minigames.behaviours.MinigameBehaviorTypes;
import com.lovetropics.minigames.common.minigames.map.IMinigameMapProvider;
import com.mojang.datafixers.Dynamic;
import net.minecraft.util.ResourceLocation;
import org.apache.commons.lang3.tuple.Pair;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public final class MinigameConfig {
    public final ResourceLocation id;
    public final String translationKey;
    public final IMinigameMapProvider mapProvider;
    public final int minimumParticipants;
    public final int maximumParticipants;
    public final Map<IMinigameBehaviorType<?>, IMinigameBehavior> behaviors;

    public MinigameConfig(
            ResourceLocation id,
            String translationKey,
            IMinigameMapProvider mapProvider,
            int minimumParticipants,
            int maximumParticipants,
            Map<IMinigameBehaviorType<?>, IMinigameBehavior> behaviors
    ) {
        this.id = id;
        this.translationKey = translationKey;
        this.mapProvider = mapProvider;
        this.minimumParticipants = minimumParticipants;
        this.maximumParticipants = maximumParticipants;
        this.behaviors = behaviors;
    }

    public static <T> MinigameConfig deserialize(ResourceLocation id, Dynamic<T> root) {
        String translationKey = root.get("translation_key").asString("");

        IMinigameMapProvider mapProvider = IMinigameMapProvider.parse(root.get("map").orElseEmptyMap());

        int minimumParticipants = root.get("minimum_participants").asInt(1);
        int maximumParticipants = root.get("maximum_participants").asInt(100);

        Map<IMinigameBehaviorType<?>, IMinigameBehavior> behaviors = root.get("behaviors")
        		.asList(MinigameConfig::deserializeBehavior)
        		.stream()
                .filter(Objects::nonNull)
        		.collect(Collectors.toMap(
        		        Pair::getLeft,
                        Pair::getRight,
                        (a, b) -> { throw new IllegalArgumentException("duplicate key"); },
                        LinkedHashMap::new
                ));

        final MinigameConfig config = new MinigameConfig(
                id,
                translationKey,
                mapProvider,
                minimumParticipants,
                maximumParticipants,
                behaviors
        );

        validateConfig(config);

        return config;
    }

    private static void validateConfig(final MinigameConfig config) {
        boolean isInvalid = false;
        final StringBuilder exceptionMsg = new StringBuilder();
        exceptionMsg.append("The following issues were found while loading the following minigame config: ")
                .append(config.id).append(System.lineSeparator());

        for (final Map.Entry<IMinigameBehaviorType<?>, IMinigameBehavior> behaviorEntry : config.behaviors.entrySet()) {
            final IMinigameBehaviorType<?> behaviorType = behaviorEntry.getKey();
            final IMinigameBehavior behavior = behaviorEntry.getValue();

            for (IMinigameBehaviorType<?> dependency : behavior.dependencies()) {
                if (!config.behaviors.containsKey(dependency)) {
                    isInvalid = true;
                    exceptionMsg.append("Behavior ").append(behaviorType.getRegistryName()).append(" has a missing dependency: ")
                            .append(dependency.getRegistryName()).append(System.lineSeparator());
                }
            }
        }

        if (isInvalid) {
            throw new RuntimeException(exceptionMsg.toString());
        }
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
