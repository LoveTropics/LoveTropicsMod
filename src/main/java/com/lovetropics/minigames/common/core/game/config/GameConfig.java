package com.lovetropics.minigames.common.core.game.config;

import java.util.List;
import java.util.Optional;

import com.lovetropics.minigames.Constants;
import com.lovetropics.minigames.common.core.game.IGameDefinition;
import com.lovetropics.minigames.common.core.game.behavior.BehaviorMap;
import com.lovetropics.minigames.common.core.game.map.GameMapProviders;
import com.lovetropics.minigames.common.core.game.map.IGameMapProvider;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.extensions.IForgeTileEntity;

public final class GameConfig implements IGameDefinition {
	public final ResourceLocation id;
	public final ResourceLocation displayId;
	public final String telemetryKey;
	public final String translationKey;
	public final IGameMapProvider mapProvider;
	public final int minimumParticipants;
	public final int maximumParticipants;
	public final AxisAlignedBB area;
	public final List<BehaviorReference> behaviors;

	public GameConfig(
			ResourceLocation id,
			ResourceLocation displayId,
			String telemetryKey,
			String translationKey,
			IGameMapProvider mapProvider,
			int minimumParticipants,
			int maximumParticipants,
			AxisAlignedBB area,
			List<BehaviorReference> behaviors
	) {
		this.id = id;
		this.displayId = displayId;
		this.telemetryKey = telemetryKey;
		this.translationKey = translationKey;
		this.mapProvider = mapProvider;
		this.minimumParticipants = minimumParticipants;
		this.maximumParticipants = maximumParticipants;
		this.area = area;
		this.behaviors = behaviors;
	}

	private static final Codec<AxisAlignedBB> AREA_CODEC = RecordCodecBuilder.create(instance ->
			instance.group(
					BlockPos.CODEC.fieldOf("start").forGetter(aabb -> new BlockPos(aabb.minX, aabb.minY, aabb.minZ)),
					BlockPos.CODEC.fieldOf("end").forGetter(aabb -> new BlockPos(aabb.maxX, aabb.maxY, aabb.maxZ)))
			.apply(instance, (min, max) -> new AxisAlignedBB(min, max)));

	public static Codec<GameConfig> codec(BehaviorReferenceReader reader, ResourceLocation id) {
		return RecordCodecBuilder.create(instance -> {
			return instance.group(
					Codec.STRING.optionalFieldOf("display_id").forGetter(c -> Optional.of(c.displayId.getPath())),
					Codec.STRING.optionalFieldOf("telemetry_key").forGetter(c -> Optional.of(c.telemetryKey)),
					Codec.STRING.fieldOf("translation_key").forGetter(c -> c.translationKey),
					GameMapProviders.CODEC.fieldOf("map_provider").forGetter(c -> c.mapProvider),
					Codec.INT.optionalFieldOf("minimum_participants", 1).forGetter(c -> c.minimumParticipants),
					Codec.INT.optionalFieldOf("maximum_participants", 100).forGetter(c -> c.maximumParticipants),
					AREA_CODEC.optionalFieldOf("area", IForgeTileEntity.INFINITE_EXTENT_AABB).forGetter(c -> c.area),
					reader.fieldOf("behaviors").forGetter(c -> c.behaviors)
			).apply(instance, (displayIdOpt, telemetryKeyOpt, translationKey, mapProvider, minimumParticipants, maximumParticipants, area, behaviors) -> {
				ResourceLocation displayId = displayIdOpt.map(string -> new ResourceLocation(id.getNamespace(), string)).orElse(id);
				String telemetryKey = telemetryKeyOpt.orElse(id.getPath());
				return new GameConfig(id, displayId, telemetryKey, translationKey, mapProvider, minimumParticipants, maximumParticipants, area, behaviors);
			});
		});
	}

	@Override
	public IGameMapProvider getMapProvider() {
		return mapProvider;
	}

	@Override
	public BehaviorMap createBehaviors() {
		return BehaviorMap.create(behaviors);
	}

	@Override
	public ResourceLocation getId() {
		return id;
	}

	@Override
	public ResourceLocation getDisplayId() {
		return displayId;
	}

	@Override
	public String getTelemetryKey() {
		return telemetryKey;
	}

	@Override
	public String getTranslationKey() {
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

	@Override
	public AxisAlignedBB getGameArea() {
		return area;
	}
}
