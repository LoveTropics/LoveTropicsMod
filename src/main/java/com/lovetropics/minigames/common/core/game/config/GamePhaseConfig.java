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
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraftforge.common.extensions.IForgeTileEntity;

import java.util.List;

public final class GamePhaseConfig implements IGamePhaseDefinition {
	public final AxisAlignedBB area;
	public final IGameMapProvider map;
	public final List<BehaviorReference> behaviors;

	public GamePhaseConfig(IGameMapProvider map, AxisAlignedBB area, List<BehaviorReference> behaviors) {
		this.map = map;
		this.area = area;
		this.behaviors = behaviors;
	}

	public static Codec<GamePhaseConfig> codec(BehaviorReferenceReader reader) {
		return mapCodec(reader).codec();
	}

	public static MapCodec<GamePhaseConfig> mapCodec(BehaviorReferenceReader reader) {
		return RecordCodecBuilder.mapCodec(instance -> {
			return instance.group(
					GameMapProviders.CODEC.fieldOf("map").forGetter(c -> c.map),
					MoreCodecs.AABB.optionalFieldOf("area", IForgeTileEntity.INFINITE_EXTENT_AABB).forGetter(c -> c.area),
					reader.fieldOf("behaviors").forGetter(c -> c.behaviors)
			).apply(instance, GamePhaseConfig::new);
		});
	}

	@Override
	public IGameMapProvider getMap() {
		return map;
	}

	@Override
	public AxisAlignedBB getGameArea() {
		return area;
	}

	@Override
	public BehaviorMap createBehaviors(MinecraftServer server) {
		return BehaviorMap.create(server, behaviors);
	}
}
