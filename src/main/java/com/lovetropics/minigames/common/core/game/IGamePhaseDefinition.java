package com.lovetropics.minigames.common.core.game;

import com.lovetropics.minigames.common.core.game.behavior.BehaviorMap;
import com.lovetropics.minigames.common.core.game.map.IGameMapProvider;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.AxisAlignedBB;

public interface IGamePhaseDefinition {
	IGameMapProvider getMap();

	default AxisAlignedBB getGameArea() {
		return TileEntity.INFINITE_EXTENT_AABB;
	}

	BehaviorMap createBehaviors(MinecraftServer server);
}
