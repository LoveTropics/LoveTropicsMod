package com.lovetropics.minigames.common.core.game.state.team;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.DyeColor;

import java.util.List;

import static com.lovetropics.lib.codec.MoreCodecs.inputOptionalFieldOf;

public record GameTeamConfig(Component name, DyeColor dye, ChatFormatting formatting, List<String> assignedRoles, int maxSize) {
	public static final MapCodec<GameTeamConfig> MAP_CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
			ComponentSerialization.CODEC.fieldOf("name").forGetter(GameTeamConfig::name),
			inputOptionalFieldOf(DyeColor.CODEC, "dye", DyeColor.WHITE).forGetter(GameTeamConfig::dye),
			inputOptionalFieldOf(ChatFormatting.CODEC, "text", ChatFormatting.WHITE).forGetter(GameTeamConfig::formatting),
			inputOptionalFieldOf(Codec.STRING.listOf(), "assign_roles", List.of()).forGetter(GameTeamConfig::assignedRoles),
			inputOptionalFieldOf(Codec.INT, "max_size", Integer.MAX_VALUE).forGetter(GameTeamConfig::maxSize)
	).apply(i, GameTeamConfig::new));

	public static final Codec<GameTeamConfig> CODEC = MAP_CODEC.codec();

	public MutableComponent styledName() {
		return name.copy().withStyle(formatting);
	}
}
