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

public record SetDifficultyBehavior(Difficulty difficulty) implements IGameBehavior {
	public static final Codec<SetDifficultyBehavior> CODEC = RecordCodecBuilder.create(i -> i.group(
			MoreCodecs.DIFFICULTY.fieldOf("difficulty").forGetter(c -> c.difficulty)
	).apply(i, SetDifficultyBehavior::new));

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
