package com.lovetropics.minigames.common.core.game.state.team;

import static com.lovetropics.lib.codec.MoreCodecs.inputOptionalFieldOf;

import com.lovetropics.lib.codec.MoreCodecs;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.item.DyeColor;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class GameTeamConfig {
	public static final MapCodec<GameTeamConfig> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> {
		return instance.group(
				MoreCodecs.TEXT.fieldOf("name").forGetter(GameTeamConfig::name),
				inputOptionalFieldOf(MoreCodecs.DYE_COLOR, "dye", DyeColor.WHITE).forGetter(GameTeamConfig::dye),
				inputOptionalFieldOf(MoreCodecs.FORMATTING, "text", ChatFormatting.WHITE).forGetter(GameTeamConfig::formatting),
				inputOptionalFieldOf(MoreCodecs.UUID_STRING.listOf(), "assign", Collections.emptyList()).forGetter(GameTeamConfig::assignedPlayers),
				inputOptionalFieldOf(Codec.INT, "max_size", Integer.MAX_VALUE).forGetter(GameTeamConfig::maxSize)
		).apply(instance, GameTeamConfig::new);
	});

	public static final Codec<GameTeamConfig> CODEC = MAP_CODEC.codec();

	private final Component name;
	private final DyeColor dye;
	private final ChatFormatting formatting;

	private final List<UUID> assignedPlayers;
	private final int maxSize;

	public GameTeamConfig(Component name, DyeColor dye, ChatFormatting formatting, List<UUID> assignedPlayers, int maxSize) {
		this.name = name;
		this.dye = dye;
		this.formatting = formatting;
		this.assignedPlayers = assignedPlayers;
		this.maxSize = maxSize;
	}

	public Component name() {
		return name;
	}

	public DyeColor dye() {
		return dye;
	}

	public ChatFormatting formatting() {
		return formatting;
	}

	public List<UUID> assignedPlayers() {
		return assignedPlayers;
	}

	public int maxSize() {
		return maxSize;
	}
}
