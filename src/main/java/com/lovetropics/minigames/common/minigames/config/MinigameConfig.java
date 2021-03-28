package com.lovetropics.minigames.common.minigames.config;

import com.lovetropics.minigames.Constants;
import com.lovetropics.minigames.common.minigames.IMinigameDefinition;
import com.lovetropics.minigames.common.minigames.behaviours.BehaviorMap;
import com.lovetropics.minigames.common.minigames.map.IMinigameMapProvider;
import com.mojang.serialization.Dynamic;
import net.minecraft.util.ResourceLocation;

import java.util.List;

public final class MinigameConfig implements IMinigameDefinition {
	public final ResourceLocation id;
	public final ResourceLocation displayId;
	public final String telemetryKey;
	public final String translationKey;
	public final IMinigameMapProvider mapProvider;
	public final int minimumParticipants;
	public final int maximumParticipants;
	public final List<BehaviorReference> behaviors;

	public MinigameConfig(
			ResourceLocation id,
			ResourceLocation displayId,
			String telemetryKey,
			String translationKey,
			IMinigameMapProvider mapProvider,
			int minimumParticipants,
			int maximumParticipants,
			List<BehaviorReference> behaviors
	) {
		this.id = id;
		this.displayId = displayId;
		this.telemetryKey = telemetryKey;
		this.translationKey = translationKey;
		this.mapProvider = mapProvider;
		this.minimumParticipants = minimumParticipants;
		this.maximumParticipants = maximumParticipants;
		this.behaviors = behaviors;
	}

	public static <T> MinigameConfig read(BehaviorReferenceReader reader, ResourceLocation id, Dynamic<T> root) {
		ResourceLocation displayId = root.get("display_id").asString()
				.map(path -> new ResourceLocation(id.getNamespace(), path))
				.orElse(id);
		String telemetryKey = root.get("telemetry_key").asString(id.getPath());
		String translationKey = root.get("translation_key").asString("");

		IMinigameMapProvider mapProvider = IMinigameMapProvider.parse(root.get("map_provider").orElseEmptyMap());

		int minimumParticipants = root.get("minimum_participants").asInt(1);
		int maximumParticipants = root.get("maximum_participants").asInt(100);

		List<BehaviorReference> behaviors = reader.readList(root.get("behaviors").orElseEmptyList());

		return new MinigameConfig(
				id,
				displayId,
				telemetryKey,
				translationKey,
				mapProvider,
				minimumParticipants,
				maximumParticipants,
				behaviors
		);
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
	public ResourceLocation getDisplayID() {
		return displayId;
	}

	@Override
	public String getTelemetryKey() {
		return telemetryKey;
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
