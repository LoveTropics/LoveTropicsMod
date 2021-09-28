package com.lovetropics.minigames.common.core.game.state.team;

import com.lovetropics.lib.codec.MoreCodecs;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.item.DyeColor;
import net.minecraft.util.text.TextFormatting;

public class TeamKey {
	public static final Codec<TeamKey> CODEC = RecordCodecBuilder.create(instance -> {
		return instance.group(
				Codec.STRING.fieldOf("key").forGetter(c -> c.key),
				Codec.STRING.fieldOf("name").forGetter(c -> c.name),
				MoreCodecs.DYE_COLOR.optionalFieldOf("dye", DyeColor.WHITE).forGetter(c -> c.dye),
				MoreCodecs.FORMATTING.optionalFieldOf("text", TextFormatting.WHITE).forGetter(c -> c.text)
		).apply(instance, TeamKey::new);
	});

	public final String key;
	public final String name;
	public final DyeColor dye;
	public final TextFormatting text;

	public TeamKey(String key, String name, DyeColor dye, TextFormatting text) {
		this.key = key;
		this.name = name;
		this.dye = dye;
		this.text = text;
	}

	@Override
	public String toString() {
		return name;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;

		if (obj instanceof TeamKey) {
			TeamKey team = (TeamKey) obj;
			return key.equals(team.key);
		}

		return false;
	}

	@Override
	public int hashCode() {
		return key.hashCode();
	}
}
