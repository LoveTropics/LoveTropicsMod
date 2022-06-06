package com.lovetropics.minigames.common.content.biodiversity_blitz.client_state;

import com.lovetropics.lib.BlockBox;
import com.lovetropics.lib.codec.MoreCodecs;
import com.lovetropics.minigames.common.content.biodiversity_blitz.BiodiversityBlitz;
import com.lovetropics.minigames.common.core.game.client_state.GameClientState;
import com.lovetropics.minigames.common.core.game.client_state.GameClientStateType;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;

public final class CheckeredPlotsState implements GameClientState {
	public static final Codec<CheckeredPlotsState> CODEC = RecordCodecBuilder.create(i -> i.group(
			MoreCodecs.arrayOrUnit(BlockBox.CODEC, BlockBox[]::new).fieldOf("plots").forGetter(c -> c.plots)
	).apply(i, CheckeredPlotsState::new));

	private final BlockBox[] plots;
	private final BlockBox global;

	public CheckeredPlotsState(BlockBox[] plots) {
		this.plots = plots;
		this.global = this.computeGlobalBounds(plots);
	}

	private BlockBox computeGlobalBounds(BlockBox[] plots) {
		if (plots.length == 0) {
			return BlockBox.of(BlockPos.ZERO);
		}

		BlockBox global = plots[0];
		for (int i = 1; i < plots.length; i++) {
			BlockBox plot = plots[i];
			global = BlockBox.of(
					BlockBox.min(global.min(), plot.min()),
					BlockBox.max(global.max(), plot.max())
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
