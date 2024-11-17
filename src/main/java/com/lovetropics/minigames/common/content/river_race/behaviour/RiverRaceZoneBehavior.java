package com.lovetropics.minigames.common.content.river_race.behaviour;

import com.lovetropics.lib.BlockBox;
import com.lovetropics.minigames.common.content.river_race.RiverRaceState;
import com.lovetropics.minigames.common.core.game.GameException;
import com.lovetropics.minigames.common.core.game.IGamePhase;
import com.lovetropics.minigames.common.core.game.behavior.IGameBehavior;
import com.lovetropics.minigames.common.core.game.behavior.event.EventRegistrar;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.world.item.DyeColor;

public record RiverRaceZoneBehavior(
		String id,
		String regionKey,
		Component displayName,
		DyeColor color
) implements IGameBehavior {
	public static final MapCodec<RiverRaceZoneBehavior> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
			Codec.STRING.fieldOf("id").forGetter(RiverRaceZoneBehavior::id),
			Codec.STRING.fieldOf("region").forGetter(RiverRaceZoneBehavior::regionKey),
			ComponentSerialization.CODEC.fieldOf("display_name").forGetter(RiverRaceZoneBehavior::displayName),
			DyeColor.CODEC.fieldOf("color").forGetter(RiverRaceZoneBehavior::color)
	).apply(i, RiverRaceZoneBehavior::new));

	@Override
	public void register(IGamePhase game, EventRegistrar events) throws GameException {
		RiverRaceState riverRace = game.state().get(RiverRaceState.KEY);
		BlockBox box = game.mapRegions().getOrThrow(regionKey);
		riverRace.addZone(id, box, displayName, color);
	}
}
