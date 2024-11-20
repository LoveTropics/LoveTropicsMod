package com.lovetropics.minigames.common.core.integration.game_actions;

import com.lovetropics.lib.codec.MoreCodecs;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.UUIDUtil;

import java.time.Instant;
import java.util.UUID;

public record GameActionRequest(GameActionType type, UUID uuid, Instant time, GameAction action) {
	public static Codec<GameActionRequest> codec(GameActionType type, MapCodec<GameAction> codec) {
		return RecordCodecBuilder.create(i -> i.group(
				UUIDUtil.STRING_CODEC.fieldOf("uuid").forGetter(GameActionRequest::uuid),
				MoreCodecs.TIME_CODEC.fieldOf(type.getTimeFieldName()).forGetter(GameActionRequest::time),
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
