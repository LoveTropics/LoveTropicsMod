package com.lovetropics.minigames.common.core.game.state.team;

import com.lovetropics.lib.codec.MoreCodecs;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.DyeColor;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static com.lovetropics.lib.codec.MoreCodecs.inputOptionalFieldOf;

public record GameTeamConfig(Component name, DyeColor dye, ChatFormatting formatting, List<UUID> assignedPlayers, int maxSize) {
	public static final MapCodec<GameTeamConfig> MAP_CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
			MoreCodecs.TEXT.fieldOf("name").forGetter(GameTeamConfig::name),
			inputOptionalFieldOf(MoreCodecs.DYE_COLOR, "dye", DyeColor.WHITE).forGetter(GameTeamConfig::dye),
			inputOptionalFieldOf(MoreCodecs.FORMATTING, "text", ChatFormatting.WHITE).forGetter(GameTeamConfig::formatting),
			inputOptionalFieldOf(MoreCodecs.UUID_STRING.listOf(), "assign", Collections.emptyList()).forGetter(GameTeamConfig::assignedPlayers),
			inputOptionalFieldOf(Codec.INT, "max_size", Integer.MAX_VALUE).forGetter(GameTeamConfig::maxSize)
	).apply(i, GameTeamConfig::new));

	public static final Codec<GameTeamConfig> CODEC = MAP_CODEC.codec();
}
