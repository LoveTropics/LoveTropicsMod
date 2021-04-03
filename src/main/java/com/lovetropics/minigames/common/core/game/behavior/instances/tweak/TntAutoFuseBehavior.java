package com.lovetropics.minigames.common.core.game.behavior.instances.tweak;

import com.lovetropics.minigames.common.core.game.IActiveGame;
import com.lovetropics.minigames.common.core.game.behavior.IGameBehavior;
import com.lovetropics.minigames.common.core.game.behavior.event.EventRegistrar;
import com.lovetropics.minigames.common.core.game.behavior.event.GamePlayerEvents;
import com.mojang.serialization.Codec;
import net.minecraft.block.Blocks;
import net.minecraft.block.TNTBlock;
import net.minecraft.util.ActionResultType;

public class TntAutoFuseBehavior implements IGameBehavior {
	public static final Codec<TntAutoFuseBehavior> CODEC = Codec.unit(TntAutoFuseBehavior::new);

	@Override
	public void register(IActiveGame registerGame, EventRegistrar events) {
		events.listen(GamePlayerEvents.PLACE_BLOCK, (game, player, pos, placed, placedOn) -> {
			if (placed.getBlock() instanceof TNTBlock) {
				placed.getBlock().catchFire(placed, player.world, pos, null, null);
				player.world.setBlockState(pos, Blocks.AIR.getDefaultState(), 11);
			}
			return ActionResultType.PASS;
		});
	}
}
