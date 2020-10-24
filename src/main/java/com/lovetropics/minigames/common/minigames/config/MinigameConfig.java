package com.lovetropics.minigames.common.minigames.config;

import com.lovetropics.minigames.Constants;
import com.lovetropics.minigames.common.minigames.IMinigameDefinition;
import com.lovetropics.minigames.common.minigames.behaviours.*;
import com.lovetropics.minigames.common.minigames.map.IMinigameMapProvider;
import com.mojang.datafixers.Dynamic;
import net.minecraft.util.ResourceLocation;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public final class MinigameConfig implements IMinigameDefinition {
	public final ResourceLocation id;
	public final String translationKey;
	public final IMinigameMapProvider mapProvider;
	public final int minimumParticipants;
	public final int maximumParticipants;
	public final List<ConfiguredBehavior<?>> behaviors;

	public MinigameConfig(
			ResourceLocation id,
			String translationKey,
			IMinigameMapProvider mapProvider,
			int minimumParticipants,
			int maximumParticipants,
			List<ConfiguredBehavior<?>> behaviors
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

		IMinigameMapProvider mapProvider = IMinigameMapProvider.parse(root.get("map_provider").orElseEmptyMap());

		int minimumParticipants = root.get("minimum_participants").asInt(1);
		int maximumParticipants = root.get("maximum_participants").asInt(100);

		List<ConfiguredBehavior<?>> behaviors = root.get("behaviors")
				.asList(MinigameConfig::deserializeBehavior)
				.stream()
				.filter(Objects::nonNull)
				.collect(Collectors.toList());

		return new MinigameConfig(
				id,
				translationKey,
				mapProvider,
				minimumParticipants,
				maximumParticipants,
				behaviors
		);
	}

	private static <T extends IMinigameBehavior, D> ConfiguredBehavior<T> deserializeBehavior(Dynamic<D> root) {
		ResourceLocation id = new ResourceLocation(root.get("type").asString(""));

		@SuppressWarnings("unchecked")
		IMinigameBehaviorType<T> type = (IMinigameBehaviorType<T>) MinigameBehaviorTypes.MINIGAME_BEHAVIOURS_REGISTRY.get().getValue(id);
		if (type != null) {
			return ConfiguredBehavior.of(type, root);
		}

		System.out.println("Type '" + id + "' is not valid!");
		return null;
	}

	@Override
	public IMinigameMapProvider getMapProvider() {
		return mapProvider;
	}

	@Override
	public BehaviorMap createBehaviors() {
		return BehaviorMap.create(behaviors);
	}

	@Override
	public ResourceLocation getID() {
		return id;
	}

	@Override
	public String getUnlocalizedName() {
		return Constants.MODID +  ".minigame." + translationKey;
	}

	@Override
	public int getMinimumParticipantCount() {
		return minimumParticipants;
	}

	@Override
	public int getMaximumParticipantCount() {
		return maximumParticipants;
	}
}
