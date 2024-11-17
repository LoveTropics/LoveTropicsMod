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
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.world.item.DyeColor;

import java.util.Map;

public final class RiverRaceZonesBehavior implements IGameBehavior {
	public static final MapCodec<RiverRaceZonesBehavior> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
			Codec.unboundedMap(Codec.STRING, ZoneConfig.CODEC).fieldOf("zones").forGetter(b -> b.zoneConfigs),
			Direction.CODEC.fieldOf("forward_direction").forGetter(b -> b.forwardDirection)
	).apply(i, RiverRaceZonesBehavior::new));

	private final Map<String, ZoneConfig> zoneConfigs;
	private final Direction forwardDirection;

	public RiverRaceZonesBehavior(Map<String, ZoneConfig> zoneConfigs, Direction forwardDirection) {
		this.zoneConfigs = zoneConfigs;
		this.forwardDirection = forwardDirection;
	}

	@Override
	public void register(IGamePhase game, EventRegistrar events) throws GameException {
		RiverRaceState riverRace = game.state().get(RiverRaceState.KEY);
		riverRace.setForwardDirection(forwardDirection);

		zoneConfigs.forEach((id, config) -> {
			BlockBox box = game.mapRegions().getOrThrow(config.regionKey);
			riverRace.addZone(id, box, config.displayName, config.color);
		});
	}

	public record ZoneConfig(
			String regionKey,
			Component displayName,
			DyeColor color
	) {
		public static final Codec<ZoneConfig> CODEC = RecordCodecBuilder.create(i -> i.group(
				Codec.STRING.fieldOf("region").forGetter(ZoneConfig::regionKey),
				ComponentSerialization.CODEC.fieldOf("display_name").forGetter(ZoneConfig::displayName),
				DyeColor.CODEC.fieldOf("color").forGetter(ZoneConfig::color)
		).apply(i, ZoneConfig::new));
	}
}
