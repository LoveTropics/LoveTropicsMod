package com.lovetropics.minigames.common.core.game;

import com.lovetropics.minigames.common.core.game.behavior.BehaviorMap;
import com.lovetropics.minigames.common.core.game.map.IGameMapProvider;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.AABB;

public interface IGamePhaseDefinition {
	IGameMapProvider getMap();

	default AABB getGameArea() {
		return BlockEntity.INFINITE_EXTENT_AABB;
	}

	BehaviorMap createBehaviors(MinecraftServer server);
}
