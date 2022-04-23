package com.lovetropics.minigames.common.core.game.behavior.instances.effect;

import com.lovetropics.lib.entity.FireworkPalette;
import com.lovetropics.minigames.common.core.game.IGamePhase;
import com.lovetropics.minigames.common.core.game.behavior.IGameBehavior;
import com.lovetropics.minigames.common.core.game.behavior.event.EventRegistrar;
import com.lovetropics.minigames.common.core.game.behavior.event.GamePlayerEvents;
import com.mojang.serialization.Codec;
import net.minecraft.world.InteractionResult;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.levelgen.Heightmap;

public final class FireworksOnDeathBehavior implements IGameBehavior {
	public static final Codec<FireworksOnDeathBehavior> CODEC = Codec.unit(FireworksOnDeathBehavior::new);

	@Override
	public void register(IGamePhase game, EventRegistrar events) {
		events.listen(GamePlayerEvents.DEATH, (player, damageSource) -> {
			BlockPos fireworkPos = player.level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, player.blockPosition());
			FireworkPalette.ISLAND_ROYALE.spawn(fireworkPos, player.level);
			return InteractionResult.PASS;
		});
	}
}
