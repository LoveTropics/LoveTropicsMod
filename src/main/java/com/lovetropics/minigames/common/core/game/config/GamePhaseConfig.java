package com.lovetropics.minigames.common.core.game.config;

import com.lovetropics.lib.codec.MoreCodecs;
import com.lovetropics.minigames.common.core.game.IGamePhaseDefinition;
import com.lovetropics.minigames.common.core.game.behavior.BehaviorList;
import com.lovetropics.minigames.common.core.game.behavior.IGameBehavior;
import com.lovetropics.minigames.common.core.game.map.GameMapProviders;
import com.lovetropics.minigames.common.core.game.map.IGameMapProvider;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.common.extensions.IForgeBlockEntity;

import java.util.List;

public record GamePhaseConfig(IGameMapProvider map, AABB area, List<IGameBehavior> behaviors) implements IGamePhaseDefinition {
	public static final MapCodec<GamePhaseConfig> MAP_CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
			GameMapProviders.CODEC.fieldOf("map").forGetter(c -> c.map),
			MoreCodecs.AABB.optionalFieldOf("area", IForgeBlockEntity.INFINITE_EXTENT_AABB).forGetter(c -> c.area),
			IGameBehavior.LIST_CODEC.fieldOf("behaviors").forGetter(c -> c.behaviors)
	).apply(i, GamePhaseConfig::new));
	public static final Codec<GamePhaseConfig> CODEC = MAP_CODEC.codec();

	@Override
	public IGameMapProvider getMap() {
		return map;
	}

	@Override
	public AABB getGameArea() {
		return area;
	}

	@Override
	public BehaviorList getBehaviors() {
		return new BehaviorList(behaviors);
	}
}
