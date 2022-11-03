package com.lovetropics.minigames.common.core.integration.game_actions;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record Donation(String name, double amount, String comments, boolean anonymous, double total) {
	public static final MapCodec<Donation> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
			Codec.STRING.fieldOf("name").forGetter(Donation::name),
			Codec.DOUBLE.fieldOf("amount").forGetter(Donation::amount),
			Codec.STRING.optionalFieldOf("comments", "").forGetter(Donation::comments),
			Codec.BOOL.optionalFieldOf("anonymous", false).forGetter(Donation::anonymous),
			Codec.DOUBLE.fieldOf("total").forGetter(Donation::total)
	).apply(i, Donation::new));
}
