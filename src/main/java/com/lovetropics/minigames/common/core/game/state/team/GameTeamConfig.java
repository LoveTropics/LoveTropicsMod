package com.lovetropics.minigames.common.core.game.state.team;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.ChatFormatting;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.DyeColor;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static com.lovetropics.lib.codec.MoreCodecs.inputOptionalFieldOf;

public record GameTeamConfig(Component name, DyeColor dye, ChatFormatting formatting, List<UUID> assignedPlayers, int maxSize) {
	public static final MapCodec<GameTeamConfig> MAP_CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
			ExtraCodecs.COMPONENT.fieldOf("name").forGetter(GameTeamConfig::name),
			inputOptionalFieldOf(DyeColor.CODEC, "dye", DyeColor.WHITE).forGetter(GameTeamConfig::dye),
			inputOptionalFieldOf(ChatFormatting.CODEC, "text", ChatFormatting.WHITE).forGetter(GameTeamConfig::formatting),
			inputOptionalFieldOf(UUIDUtil.STRING_CODEC.listOf(), "assign", Collections.emptyList()).forGetter(GameTeamConfig::assignedPlayers),
			inputOptionalFieldOf(Codec.INT, "max_size", Integer.MAX_VALUE).forGetter(GameTeamConfig::maxSize)
	).apply(i, GameTeamConfig::new));

	public static final Codec<GameTeamConfig> CODEC = MAP_CODEC.codec();

	public MutableComponent styledName() {
		return name.copy().withStyle(formatting);
	}
}
