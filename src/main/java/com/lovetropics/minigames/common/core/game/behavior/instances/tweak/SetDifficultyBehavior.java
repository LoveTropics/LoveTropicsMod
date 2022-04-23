package com.lovetropics.minigames.common.core.game.behavior.instances.tweak;

import com.lovetropics.lib.codec.MoreCodecs;
import com.lovetropics.minigames.common.core.game.IGamePhase;
import com.lovetropics.minigames.common.core.game.behavior.IGameBehavior;
import com.lovetropics.minigames.common.core.game.behavior.event.EventRegistrar;
import com.lovetropics.minigames.common.core.game.behavior.event.GamePhaseEvents;
import com.lovetropics.minigames.common.core.map.MapWorldInfo;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.Difficulty;
import net.minecraft.server.level.ServerLevel;

public class SetDifficultyBehavior implements IGameBehavior {
	public static final Codec<SetDifficultyBehavior> CODEC = RecordCodecBuilder.create(instance -> {
		return instance.group(
				MoreCodecs.DIFFICULTY.fieldOf("difficulty").forGetter(c -> c.difficulty)
		).apply(instance, SetDifficultyBehavior::new);
	});

	private final Difficulty difficulty;

	public SetDifficultyBehavior(Difficulty difficulty) {
		this.difficulty = difficulty;
	}

	@Override
	public void register(IGamePhase game, EventRegistrar events) {
		events.listen(GamePhaseEvents.START, () -> {
			ServerLevel world = game.getWorld();
			if (world.getLevelData() instanceof MapWorldInfo) {
				MapWorldInfo worldInfo = (MapWorldInfo) world.getLevelData();
				worldInfo.setDifficulty(difficulty);
			}
		});
	}
}
