package com.lovetropics.minigames.common.core.game.config;

import com.lovetropics.lib.codec.MoreCodecs;
import com.lovetropics.minigames.Constants;
import com.lovetropics.minigames.common.core.game.IGameDefinition;
import com.lovetropics.minigames.common.core.game.behavior.BehaviorMap;
import com.lovetropics.minigames.common.core.game.map.GameMapProviders;
import com.lovetropics.minigames.common.core.game.map.IGameMapProvider;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraftforge.common.extensions.IForgeTileEntity;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;

public final class GameConfig implements IGameDefinition {
	public final ResourceLocation id;
	public final ResourceLocation displayId;
	public final String telemetryKey;
	public final String translationKey;
	public final IGameMapProvider map;
	public final int minimumParticipants;
	public final int maximumParticipants;
	public final AxisAlignedBB area;
	public final WaitingLobbyConfig waitingLobby;
	public final List<BehaviorReference> behaviors;

	public GameConfig(
			ResourceLocation id,
			ResourceLocation displayId,
			String telemetryKey,
			String translationKey,
			IGameMapProvider map,
			int minimumParticipants,
			int maximumParticipants,
			AxisAlignedBB area,
			WaitingLobbyConfig waitingLobby,
			List<BehaviorReference> behaviors
	) {
		this.id = id;
		this.displayId = displayId;
		this.telemetryKey = telemetryKey;
		this.translationKey = translationKey;
		this.map = map;
		this.minimumParticipants = minimumParticipants;
		this.maximumParticipants = maximumParticipants;
		this.area = area;
		this.waitingLobby = waitingLobby;
		this.behaviors = behaviors;
	}

	public static Codec<GameConfig> codec(BehaviorReferenceReader reader, ResourceLocation id) {
		return RecordCodecBuilder.create(instance -> {
			return instance.group(
					Codec.STRING.optionalFieldOf("display_id").forGetter(c -> Optional.of(c.displayId.getPath())),
					Codec.STRING.optionalFieldOf("telemetry_key").forGetter(c -> Optional.of(c.telemetryKey)),
					Codec.STRING.fieldOf("translation_key").forGetter(c -> c.translationKey),
					GameMapProviders.CODEC.fieldOf("map").forGetter(c -> c.map),
					Codec.INT.optionalFieldOf("minimum_participants", 1).forGetter(c -> c.minimumParticipants),
					Codec.INT.optionalFieldOf("maximum_participants", 100).forGetter(c -> c.maximumParticipants),
					MoreCodecs.AABB.optionalFieldOf("area", IForgeTileEntity.INFINITE_EXTENT_AABB).forGetter(c -> c.area),
					WaitingLobbyConfig.CODEC.optionalFieldOf("waiting_lobby").forGetter(c -> Optional.ofNullable(c.waitingLobby)),
					reader.fieldOf("behaviors").forGetter(c -> c.behaviors)
			).apply(instance, (displayIdOpt, telemetryKeyOpt, translationKey, mapProvider, minimumParticipants, maximumParticipants, area, waitingLobbyOpt, behaviors) -> {
				ResourceLocation displayId = displayIdOpt.map(string -> new ResourceLocation(id.getNamespace(), string)).orElse(id);
				String telemetryKey = telemetryKeyOpt.orElse(id.getPath());
				WaitingLobbyConfig waitingLobby = waitingLobbyOpt.orElse(null);
				return new GameConfig(id, displayId, telemetryKey, translationKey, mapProvider, minimumParticipants, maximumParticipants, area, waitingLobby, behaviors);
			});
		});
	}

	@Override
	public IGameMapProvider getMap() {
		return map;
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

	@Nullable
	@Override
	public WaitingLobbyConfig getWaitingLobby() {
		return waitingLobby;
	}
}
