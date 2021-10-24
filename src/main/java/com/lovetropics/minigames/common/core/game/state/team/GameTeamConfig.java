package com.lovetropics.minigames.common.core.game.state.team;

import com.lovetropics.lib.codec.MoreCodecs;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.item.DyeColor;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class GameTeamConfig {
	public static final MapCodec<GameTeamConfig> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> {
		return instance.group(
				MoreCodecs.TEXT.fieldOf("name").forGetter(GameTeamConfig::name),
				MoreCodecs.DYE_COLOR.optionalFieldOf("dye", DyeColor.WHITE).forGetter(GameTeamConfig::dye),
				MoreCodecs.FORMATTING.optionalFieldOf("text", TextFormatting.WHITE).forGetter(GameTeamConfig::formatting),
				MoreCodecs.UUID_STRING.listOf().optionalFieldOf("assign", Collections.emptyList()).forGetter(GameTeamConfig::assignedPlayers),
				Codec.INT.optionalFieldOf("max_size", Integer.MAX_VALUE).forGetter(GameTeamConfig::maxSize)
		).apply(instance, GameTeamConfig::new);
	});

	public static final Codec<GameTeamConfig> CODEC = MAP_CODEC.codec();

	private final ITextComponent name;
	private final DyeColor dye;
	private final TextFormatting formatting;

	private final List<UUID> assignedPlayers;
	private final int maxSize;

	public GameTeamConfig(ITextComponent name, DyeColor dye, TextFormatting formatting, List<UUID> assignedPlayers, int maxSize) {
		this.name = name;
		this.dye = dye;
		this.formatting = formatting;
		this.assignedPlayers = assignedPlayers;
		this.maxSize = maxSize;
	}

	public ITextComponent name() {
		return name;
	}

	public DyeColor dye() {
		return dye;
	}

	public TextFormatting formatting() {
		return formatting;
	}

	public List<UUID> assignedPlayers() {
		return assignedPlayers;
	}

	public int maxSize() {
		return maxSize;
	}
}
