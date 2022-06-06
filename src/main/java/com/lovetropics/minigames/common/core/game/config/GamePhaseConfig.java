package com.lovetropics.minigames.common.core.game.config;

import com.lovetropics.lib.codec.MoreCodecs;
import com.lovetropics.minigames.common.core.game.IGamePhaseDefinition;
import com.lovetropics.minigames.common.core.game.behavior.BehaviorMap;
import com.lovetropics.minigames.common.core.game.map.GameMapProviders;
import com.lovetropics.minigames.common.core.game.map.IGameMapProvider;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.common.extensions.IForgeBlockEntity;

import java.util.List;

public record GamePhaseConfig(IGameMapProvider map, AABB area, List<BehaviorReference> behaviors) implements IGamePhaseDefinition {
	public static Codec<GamePhaseConfig> codec(BehaviorReferenceReader reader) {
		return mapCodec(reader).codec();
	}

	public static MapCodec<GamePhaseConfig> mapCodec(BehaviorReferenceReader reader) {
		return RecordCodecBuilder.mapCodec(i -> i.group(
				GameMapProviders.CODEC.fieldOf("map").forGetter(c -> c.map),
				MoreCodecs.AABB.optionalFieldOf("area", IForgeBlockEntity.INFINITE_EXTENT_AABB).forGetter(c -> c.area),
				reader.fieldOf("behaviors").forGetter(c -> c.behaviors)
		).apply(i, GamePhaseConfig::new));
	}

	@Override
	public IGameMapProvider getMap() {
		return map;
	}

	@Override
	public AABB getGameArea() {
		return area;
	}

	@Override
	public BehaviorMap createBehaviors(MinecraftServer server) {
		return BehaviorMap.create(server, behaviors);
	}
}
