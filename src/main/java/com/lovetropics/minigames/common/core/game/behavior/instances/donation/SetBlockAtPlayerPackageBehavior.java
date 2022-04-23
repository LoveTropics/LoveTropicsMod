package com.lovetropics.minigames.common.core.game.behavior.instances.donation;

import com.lovetropics.lib.codec.MoreCodecs;
import com.lovetropics.minigames.common.core.game.GameException;
import com.lovetropics.minigames.common.core.game.IGamePhase;
import com.lovetropics.minigames.common.core.game.behavior.IGameBehavior;
import com.lovetropics.minigames.common.core.game.behavior.event.EventRegistrar;
import com.lovetropics.minigames.common.core.game.behavior.event.GamePackageEvents;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.gen.blockstateprovider.BlockStateProvider;

public final class SetBlockAtPlayerPackageBehavior implements IGameBehavior {
	public static final Codec<SetBlockAtPlayerPackageBehavior> CODEC = RecordCodecBuilder.create(instance -> {
		return instance.group(
				MoreCodecs.BLOCK_STATE_PROVIDER.fieldOf("block").forGetter(c -> c.block)
		).apply(instance, SetBlockAtPlayerPackageBehavior::new);
	});

	private final BlockStateProvider block;

	public SetBlockAtPlayerPackageBehavior(BlockStateProvider block) {
		this.block = block;
	}

	@Override
	public void register(IGamePhase game, EventRegistrar events) throws GameException {
		events.listen(GamePackageEvents.APPLY_PACKAGE_TO_PLAYER, (player, sendingPlayer) -> {
			BlockPos pos = player.blockPosition();
			BlockState state = block.getState(player.level.random, pos);
			player.level.setBlockAndUpdate(pos, state);
			return true;
		});
	}
}
