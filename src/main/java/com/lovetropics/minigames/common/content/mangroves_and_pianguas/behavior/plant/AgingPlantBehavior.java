package com.lovetropics.minigames.common.content.mangroves_and_pianguas.behavior.plant;

import com.lovetropics.minigames.common.content.mangroves_and_pianguas.behavior.event.MpEvents;
import com.lovetropics.minigames.common.content.mangroves_and_pianguas.plot.plant.Plant;
import com.lovetropics.minigames.common.core.game.IGamePhase;
import com.lovetropics.minigames.common.core.game.behavior.IGameBehavior;
import com.lovetropics.minigames.common.core.game.behavior.event.EventRegistrar;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.block.BlockState;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.server.ServerWorld;

public final class AgingPlantBehavior implements IGameBehavior {
	public static final Codec<AgingPlantBehavior> CODEC = RecordCodecBuilder.create(instance -> {
		return instance.group(
				Codec.INT.fieldOf("interval").forGetter(c -> c.interval)
		).apply(instance, AgingPlantBehavior::new);
	});

	private final int interval;

	public AgingPlantBehavior(int interval) {
		this.interval = interval;
	}

	@Override
	public void register(IGamePhase game, EventRegistrar events) {
		events.listen(MpEvents.TICK_PLANTS, (player, plot, plants) -> {
			long ticks = game.ticks();
			if (ticks % this.interval != 0) return;

			ServerWorld world = game.getWorld();

			for (Plant plant : plants) {
				for (BlockPos pos : plant.coverage()) {
					BlockState state = world.getBlockState(pos);
					BlockState agedState = ageUp(state);
					if (state != agedState) {
						world.setBlockState(pos, agedState);
					}
				}
			}
		});
	}

	private static BlockState ageUp(BlockState state) {
		if (state.hasProperty(BlockStateProperties.AGE_0_3)) {
			int age = state.get(BlockStateProperties.AGE_0_3);
			if (age < 3) {
				return state.with(BlockStateProperties.AGE_0_3, age + 1);
			}
		} else if (state.hasProperty(BlockStateProperties.AGE_0_7)) {
			int age = state.get(BlockStateProperties.AGE_0_7);
			if (age < 7) {
				return state.with(BlockStateProperties.AGE_0_7, age + 1);
			}
		}

		return state;
	}
}
