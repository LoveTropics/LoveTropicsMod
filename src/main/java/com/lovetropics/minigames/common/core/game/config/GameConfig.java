package com.lovetropics.minigames.common.core.game.config;

import com.lovetropics.lib.codec.MoreCodecs;
import com.lovetropics.minigames.common.core.game.IGameDefinition;
import com.lovetropics.minigames.common.core.game.IGamePhaseDefinition;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;

import javax.annotation.Nullable;
import java.util.Optional;

public final class GameConfig implements IGameDefinition {
	public final ResourceLocation id;
	public final ResourceLocation backendId;
	public final String statisticsKey;
	public final ITextComponent name;
	@Nullable
	public final ITextComponent subtitle;
	public final int minimumParticipants;
	public final int maximumParticipants;
	@Nullable
	public final GamePhaseConfig waiting;
	public final GamePhaseConfig playing;

	public GameConfig(
			ResourceLocation id,
			ResourceLocation backendId,
			String statisticsKey,
			ITextComponent name,
			@Nullable ITextComponent subtitle,
			int minimumParticipants,
			int maximumParticipants,
			@Nullable GamePhaseConfig waiting,
			GamePhaseConfig playing
	) {
		this.id = id;
		this.backendId = backendId;
		this.statisticsKey = statisticsKey;
		this.name = name;
		this.subtitle = subtitle;
		this.minimumParticipants = minimumParticipants;
		this.maximumParticipants = maximumParticipants;
		this.waiting = waiting;
		this.playing = playing;
	}

	public static Codec<GameConfig> codec(BehaviorReferenceReader reader, ResourceLocation id) {
		MapCodec<GamePhaseConfig> phaseCodec = GamePhaseConfig.mapCodec(reader);

		return RecordCodecBuilder.create(instance -> {
			return instance.group(
					ResourceLocation.CODEC.optionalFieldOf("backend_id").forGetter(c -> Optional.of(c.backendId)),
					Codec.STRING.optionalFieldOf("statistics_key").forGetter(c -> Optional.of(c.statisticsKey)),
					MoreCodecs.TEXT.fieldOf("name").forGetter(c -> c.name),
					MoreCodecs.TEXT.optionalFieldOf("subtitle").forGetter(c -> Optional.ofNullable(c.subtitle)),
					Codec.INT.optionalFieldOf("minimum_participants", 1).forGetter(c -> c.minimumParticipants),
					Codec.INT.optionalFieldOf("maximum_participants", 100).forGetter(c -> c.maximumParticipants),
					phaseCodec.codec().optionalFieldOf("waiting").forGetter(c -> Optional.ofNullable(c.waiting)),
					phaseCodec.forGetter(c -> c.playing)
			).apply(instance, (backendIdOpt, statisticsKeyOpt, name, subtitleOpt, minimumParticipants, maximumParticipants, waitingOpt, active) -> {
				ResourceLocation backendId = backendIdOpt.orElse(id);
				String telemetryKey = statisticsKeyOpt.orElse(id.getPath());
				ITextComponent subtitle = subtitleOpt.orElse(null);
				GamePhaseConfig waiting = waitingOpt.orElse(null);
				return new GameConfig(id, backendId, telemetryKey, name, subtitle, minimumParticipants, maximumParticipants, waiting, active);
			});
		});
	}

	@Override
	public ResourceLocation getId() {
		return id;
	}

	@Override
	public ResourceLocation getBackendId() {
		return backendId;
	}

	@Override
	public String getStatisticsKey() {
		return statisticsKey;
	}

	@Override
	public ITextComponent getName() {
		return name;
	}

	@Nullable
	@Override
	public ITextComponent getSubtitle() {
		return subtitle;
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
