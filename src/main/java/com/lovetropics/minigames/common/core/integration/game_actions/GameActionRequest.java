package com.lovetropics.minigames.common.core.integration.game_actions;

import com.lovetropics.lib.codec.MoreCodecs;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

public record GameActionRequest(GameActionType type, UUID uuid, LocalDateTime triggerTime, GameAction action) {
	private static final Codec<LocalDateTime> TIME_CODEC = MoreCodecs.localDateTime(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSSSSS"));

	public static Codec<GameActionRequest> codec(GameActionType type, MapCodec<GameAction> codec) {
		return RecordCodecBuilder.create(i -> i.group(
				MoreCodecs.UUID_STRING.fieldOf("uuid").forGetter(GameActionRequest::uuid),
				TIME_CODEC.fieldOf("trigger_time").forGetter(GameActionRequest::triggerTime),
				codec.forGetter(GameActionRequest::action)
		).apply(i, (uuid, triggerTime, action) -> new GameActionRequest(type, uuid, triggerTime, action)));
	}

	@Override
	public boolean equals(Object obj) {
		return obj instanceof GameActionRequest request && uuid.equals(request.uuid());
	}

	@Override
	public int hashCode() {
		return uuid.hashCode();
	}
}
