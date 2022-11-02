package com.lovetropics.minigames.common.core.game.behavior.instances.action;

import com.lovetropics.lib.entity.FireworkPalette;
import com.lovetropics.minigames.common.core.game.IGamePhase;
import com.lovetropics.minigames.common.core.game.behavior.IGameBehavior;
import com.lovetropics.minigames.common.core.game.behavior.event.EventRegistrar;
import com.lovetropics.minigames.common.core.game.behavior.event.GameActionEvents;
import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.levelgen.Heightmap;

public final class SpawnFireworksAction implements IGameBehavior {
	public static final Codec<SpawnFireworksAction> CODEC = Codec.unit(SpawnFireworksAction::new);

	@Override
	public void register(IGamePhase game, EventRegistrar events) {
		events.listen(GameActionEvents.APPLY_TO_PLAYER, (context, player) -> {
			BlockPos fireworkPos = player.level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, player.blockPosition());
			FireworkPalette.DYE_COLORS.spawn(fireworkPos, player.level);
			return true;
		});
	}
}
