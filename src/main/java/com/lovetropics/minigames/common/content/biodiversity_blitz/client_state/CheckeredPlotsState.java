package com.lovetropics.minigames.common.content.biodiversity_blitz.client_state;

import com.lovetropics.lib.BlockBox;
import com.lovetropics.minigames.common.content.biodiversity_blitz.BiodiversityBlitz;
import com.lovetropics.minigames.common.core.game.client_state.GameClientState;
import com.lovetropics.minigames.common.core.game.client_state.GameClientStateType;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;

import java.util.List;

public record CheckeredPlotsState(List<BlockBox> plots, BlockBox global) implements GameClientState {
	public static final MapCodec<CheckeredPlotsState> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
			BlockBox.CODEC.listOf().fieldOf("plots").forGetter(CheckeredPlotsState::plots)
	).apply(i, CheckeredPlotsState::new));

	public CheckeredPlotsState(final List<BlockBox> plots) {
		this(plots, computeGlobalBounds(plots));
	}

	private static BlockBox computeGlobalBounds(final List<BlockBox> plots) {
		if (plots.isEmpty()) {
			return BlockBox.of(BlockPos.ZERO);
		}

		BlockBox global = plots.get(0);
		for (int i = 1; i < plots.size(); i++) {
			final BlockBox plot = plots.get(i);
			global = BlockBox.of(
					BlockPos.min(global.min(), plot.min()),
					BlockPos.max(global.max(), plot.max())
			);
		}

		return global;
	}

	@Override
	public GameClientStateType<?> getType() {
		return BiodiversityBlitz.CHECKERED_PLOTS_STATE.get();
	}

	public boolean contains(BlockPos pos) {
		int x = pos.getX();
		int y = pos.getY();
		int z = pos.getZ();

		if (!this.global.contains(x, y, z)) {
			return false;
		}

		for (BlockBox plot : this.plots) {
			if (plot.contains(x, y, z)) {
				return true;
			}
		}

		return false;
	}
}
