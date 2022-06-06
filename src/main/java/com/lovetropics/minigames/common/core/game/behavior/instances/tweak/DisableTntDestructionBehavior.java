package com.lovetropics.minigames.common.core.game.behavior.instances.tweak;

import com.lovetropics.minigames.common.core.game.IGamePhase;
import com.lovetropics.minigames.common.core.game.behavior.IGameBehavior;
import com.lovetropics.minigames.common.core.game.behavior.event.EventRegistrar;
import com.lovetropics.minigames.common.core.game.behavior.event.GameWorldEvents;
import com.lovetropics.minigames.common.util.BlockStatePredicate;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.server.level.ServerLevel;

public record DisableTntDestructionBehavior(BlockStatePredicate blockPredicate) implements IGameBehavior {
	public static final Codec<DisableTntDestructionBehavior> CODEC = RecordCodecBuilder.create(i -> i.group(
			BlockStatePredicate.CODEC.optionalFieldOf("block_predicate", BlockStatePredicate.ANY).forGetter(c -> c.blockPredicate)
	).apply(i, DisableTntDestructionBehavior::new));

	@Override
	public void register(IGamePhase game, EventRegistrar events) {
		events.listen(GameWorldEvents.EXPLOSION_DETONATE, (explosion, affectedBlocks, affectedEntities) -> {
			ServerLevel world = game.getWorld();
			affectedBlocks.removeIf(pos -> blockPredicate.test(world.getBlockState(pos)));
		});
	}
}
