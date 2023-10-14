package com.lovetropics.minigames.common.core.game.config;

import com.lovetropics.minigames.common.core.game.IGameDefinition;
import com.lovetropics.minigames.common.core.game.IGamePhaseDefinition;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ExtraCodecs;

import javax.annotation.Nullable;
import java.util.Optional;

public final class GameConfig implements IGameDefinition {
	public final ResourceLocation id;
	public final ResourceLocation backendId;
	public final String statisticsKey;
	public final Component name;
	@Nullable
	public final Component subtitle;
	@Nullable
	public final ResourceLocation icon;
	public final int minimumParticipants;
	public final int maximumParticipants;
	@Nullable
	public final GamePhaseConfig waiting;
	public final GamePhaseConfig playing;

	public GameConfig(
			ResourceLocation id,
			ResourceLocation backendId,
			String statisticsKey,
			Component name,
			@Nullable Component subtitle,
			@Nullable ResourceLocation icon,
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
		this.icon = icon;
		this.minimumParticipants = minimumParticipants;
		this.maximumParticipants = maximumParticipants;
		this.waiting = waiting;
		this.playing = playing;
	}

	public static Codec<GameConfig> codec(ResourceLocation id) {
		return RecordCodecBuilder.create(i -> i.group(
				ResourceLocation.CODEC.optionalFieldOf("backend_id").forGetter(c -> Optional.of(c.backendId)),
				Codec.STRING.optionalFieldOf("statistics_key").forGetter(c -> Optional.of(c.statisticsKey)),
				ExtraCodecs.COMPONENT.fieldOf("name").forGetter(c -> c.name),
				ExtraCodecs.COMPONENT.optionalFieldOf("subtitle").forGetter(c -> Optional.ofNullable(c.subtitle)),
				ResourceLocation.CODEC.optionalFieldOf("icon").forGetter(c -> Optional.ofNullable(c.icon)),
				Codec.INT.optionalFieldOf("minimum_participants", 1).forGetter(c -> c.minimumParticipants),
				Codec.INT.optionalFieldOf("maximum_participants", 100).forGetter(c -> c.maximumParticipants),
				GamePhaseConfig.CODEC.optionalFieldOf("waiting").forGetter(c -> Optional.ofNullable(c.waiting)),
				GamePhaseConfig.MAP_CODEC.forGetter(c -> c.playing)
		).apply(i, (backendIdOpt, statisticsKeyOpt, name, subtitleOpt, iconOpt, minimumParticipants, maximumParticipants, waitingOpt, active) -> {
			ResourceLocation backendId = backendIdOpt.orElse(id);
			String statisticsKey = statisticsKeyOpt.orElse(id.getPath());
			Component subtitle = subtitleOpt.orElse(null);
			ResourceLocation icon = iconOpt.orElse(null);
			GamePhaseConfig waiting = waitingOpt.orElse(null);
			return new GameConfig(id, backendId, statisticsKey, name, subtitle, icon, minimumParticipants, maximumParticipants, waiting, active);
		}));
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
	public Component getName() {
		return name;
	}

	@Nullable
	@Override
	public Component getSubtitle() {
		return subtitle;
	}

	@Nullable
	@Override
	public ResourceLocation getIcon() {
		return icon;
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
