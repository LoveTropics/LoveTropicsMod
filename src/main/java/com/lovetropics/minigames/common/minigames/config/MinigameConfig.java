package com.lovetropics.minigames.common.minigames.config;

import com.lovetropics.minigames.Constants;
import com.lovetropics.minigames.common.minigames.IMinigameDefinition;
import com.lovetropics.minigames.common.minigames.behaviours.BehaviorMap;
import com.lovetropics.minigames.common.minigames.map.IMinigameMapProvider;
import com.lovetropics.minigames.common.minigames.map.MinigameMapProviders;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.ResourceLocation;

import java.util.List;
import java.util.Optional;

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

	public static Codec<MinigameConfig> codec(BehaviorReferenceReader reader, ResourceLocation id) {
		return RecordCodecBuilder.create(instance -> {
			return instance.group(
					Codec.STRING.optionalFieldOf("display_id").forGetter(c -> Optional.of(c.displayId.getPath())),
					Codec.STRING.optionalFieldOf("telemetry_key").forGetter(c -> Optional.of(c.telemetryKey)),
					Codec.STRING.fieldOf("translation_key").forGetter(c -> c.translationKey),
					MinigameMapProviders.CODEC.fieldOf("map_provider").forGetter(c -> c.mapProvider),
					Codec.INT.optionalFieldOf("minimum_participants", 1).forGetter(c -> c.minimumParticipants),
					Codec.INT.optionalFieldOf("maximum_participants", 100).forGetter(c -> c.maximumParticipants),
					reader.listOf().fieldOf("behaviors").forGetter(c -> c.behaviors)
			).apply(instance, (displayIdOpt, telemetryKeyOpt, translationKey, mapProvider, minimumParticipants, maximumParticipants, behaviors) -> {
				ResourceLocation displayId = displayIdOpt.map(string -> new ResourceLocation(id.getNamespace(), string)).orElse(id);
				String telemetryKey = telemetryKeyOpt.orElse(id.getPath());
				return new MinigameConfig(id, displayId, telemetryKey, translationKey, mapProvider, minimumParticipants, maximumParticipants, behaviors);
			});
		});
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
		return Constants.MODID + ".minigame." + translationKey;
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
