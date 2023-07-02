package com.lovetropics.minigames.common.core.game.behavior.instances.tweak;

import com.lovetropics.minigames.common.core.game.IGamePhase;
import com.lovetropics.minigames.common.core.game.behavior.IGameBehavior;
import com.lovetropics.minigames.common.core.game.behavior.event.EventRegistrar;
import com.lovetropics.minigames.common.core.game.behavior.event.GamePhaseEvents;
import com.lovetropics.minigames.common.core.map.MapWorldInfo;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.Difficulty;

public record SetDifficultyBehavior(Difficulty difficulty) implements IGameBehavior {
	public static final Codec<SetDifficultyBehavior> CODEC = RecordCodecBuilder.create(i -> i.group(
			Difficulty.CODEC.fieldOf("difficulty").forGetter(c -> c.difficulty)
	).apply(i, SetDifficultyBehavior::new));

	@Override
	public void register(IGamePhase game, EventRegistrar events) {
		events.listen(GamePhaseEvents.START, () -> {
			if (game.getWorld().getLevelData() instanceof MapWorldInfo worldInfo) {
				worldInfo.setDifficulty(difficulty);
			}
		});
	}
}
