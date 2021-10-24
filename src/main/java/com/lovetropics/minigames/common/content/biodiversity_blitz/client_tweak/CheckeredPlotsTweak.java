package com.lovetropics.minigames.common.content.biodiversity_blitz.client_tweak;

import com.lovetropics.lib.BlockBox;
import com.lovetropics.lib.codec.MoreCodecs;
import com.lovetropics.minigames.common.content.biodiversity_blitz.BiodiversityBlitz;
import com.lovetropics.minigames.common.core.game.client_tweak.GameClientTweak;
import com.lovetropics.minigames.common.core.game.client_tweak.GameClientTweakType;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.math.BlockPos;

public final class CheckeredPlotsTweak implements GameClientTweak {
	public static final Codec<CheckeredPlotsTweak> CODEC = RecordCodecBuilder.create(instance -> {
		return instance.group(
				MoreCodecs.arrayOrUnit(BlockBox.CODEC, BlockBox[]::new).fieldOf("plots").forGetter(c -> c.plots)
		).apply(instance, CheckeredPlotsTweak::new);
	});

	private final BlockBox[] plots;
	private final BlockBox global;

	public CheckeredPlotsTweak(BlockBox[] plots) {
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
					BlockBox.min(global.min, plot.min),
					BlockBox.max(global.max, plot.max)
			);
		}

		return global;
	}

	@Override
	public GameClientTweakType<?> getType() {
		return BiodiversityBlitz.CHECKERED_PLOTS_TWEAK.get();
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
