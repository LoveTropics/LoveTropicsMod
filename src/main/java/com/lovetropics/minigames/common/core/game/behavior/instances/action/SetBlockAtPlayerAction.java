package com.lovetropics.minigames.common.core.game.behavior.instances.action;

import com.lovetropics.lib.codec.MoreCodecs;
import com.lovetropics.minigames.common.core.game.GameException;
import com.lovetropics.minigames.common.core.game.IGamePhase;
import com.lovetropics.minigames.common.core.game.behavior.IGameBehavior;
import com.lovetropics.minigames.common.core.game.behavior.event.EventRegistrar;
import com.lovetropics.minigames.common.core.game.behavior.event.GameActionEvents;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;

public record SetBlockAtPlayerAction(BlockStateProvider block) implements IGameBehavior {
	public static final Codec<SetBlockAtPlayerAction> CODEC = RecordCodecBuilder.create(i -> i.group(
			MoreCodecs.BLOCK_STATE_PROVIDER.fieldOf("block").forGetter(c -> c.block)
	).apply(i, SetBlockAtPlayerAction::new));

	@Override
	public void register(IGamePhase game, EventRegistrar events) throws GameException {
		events.listen(GameActionEvents.APPLY_TO_PLAYER, (context, player) -> {
			BlockPos pos = player.blockPosition();
			BlockState state = block.getState(player.level().random, pos);
			player.level().setBlockAndUpdate(pos, state);
			return true;
		});
	}
}
