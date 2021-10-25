package com.lovetropics.minigames.common.core.game.config;

import com.lovetropics.minigames.Constants;
import com.lovetropics.minigames.common.core.game.IGameDefinition;
import com.lovetropics.minigames.common.core.game.IGamePhaseDefinition;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nullable;
import java.util.Optional;

public final class GameConfig implements IGameDefinition {
	public final ResourceLocation id;
	public final ResourceLocation displayId;
	public final String telemetryKey;
	public final String translationKey;
	public final int minimumParticipants;
	public final int maximumParticipants;
	@Nullable
	public final GamePhaseConfig waiting;
	public final GamePhaseConfig playing;

	public GameConfig(
			ResourceLocation id,
			ResourceLocation displayId,
			String telemetryKey,
			String translationKey,
			int minimumParticipants,
			int maximumParticipants,
			@Nullable GamePhaseConfig waiting,
			GamePhaseConfig playing
	) {
		this.id = id;
		this.displayId = displayId;
		this.telemetryKey = telemetryKey;
		this.translationKey = translationKey;
		this.minimumParticipants = minimumParticipants;
		this.maximumParticipants = maximumParticipants;
		this.waiting = waiting;
		this.playing = playing;
	}

	public static Codec<GameConfig> codec(BehaviorReferenceReader reader, ResourceLocation id) {
		MapCodec<GamePhaseConfig> phaseCodec = GamePhaseConfig.mapCodec(reader);

		return RecordCodecBuilder.create(instance -> {
			return instance.group(
					Codec.STRING.optionalFieldOf("display_id").forGetter(c -> Optional.of(c.displayId.getPath())),
					Codec.STRING.optionalFieldOf("telemetry_key").forGetter(c -> Optional.of(c.telemetryKey)),
					Codec.STRING.fieldOf("translation_key").forGetter(c -> c.translationKey),
					Codec.INT.optionalFieldOf("minimum_participants", 1).forGetter(c -> c.minimumParticipants),
					Codec.INT.optionalFieldOf("maximum_participants", 100).forGetter(c -> c.maximumParticipants),
					phaseCodec.codec().optionalFieldOf("waiting").forGetter(c -> Optional.ofNullable(c.waiting)),
					phaseCodec.forGetter(c -> c.playing)
			).apply(instance, (displayIdOpt, telemetryKeyOpt, translationKey, minimumParticipants, maximumParticipants, waitingOpt, active) -> {
				ResourceLocation displayId = displayIdOpt.map(string -> new ResourceLocation(id.getNamespace(), string)).orElse(id);
				String telemetryKey = telemetryKeyOpt.orElse(id.getPath());
				GamePhaseConfig waiting = waitingOpt.orElse(null);
				return new GameConfig(id, displayId, telemetryKey, translationKey, minimumParticipants, maximumParticipants, waiting, active);
			});
		});
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
	public IGamePhaseDefinition getPlayingPhase() {
		return playing;
	}

	@Override
	public Optional<IGamePhaseDefinition> getWaitingPhase() {
		return Optional.ofNullable(waiting);
	}
}
