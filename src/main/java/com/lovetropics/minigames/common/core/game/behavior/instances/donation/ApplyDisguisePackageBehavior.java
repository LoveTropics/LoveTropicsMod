package com.lovetropics.minigames.common.core.game.behavior.instances.donation;

import com.lovetropics.minigames.common.core.diguise.DisguiseType;
import com.lovetropics.minigames.common.core.diguise.ServerPlayerDisguises;
import com.lovetropics.minigames.common.core.game.IGamePhase;
import com.lovetropics.minigames.common.core.game.behavior.IGameBehavior;
import com.lovetropics.minigames.common.core.game.behavior.event.EventRegistrar;
import com.lovetropics.minigames.common.core.game.behavior.event.GamePackageEvents;
import com.lovetropics.minigames.common.core.game.behavior.event.GamePhaseEvents;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.entity.player.ServerPlayerEntity;

public final class ApplyDisguisePackageBehavior implements IGameBehavior {
	public static final Codec<ApplyDisguisePackageBehavior> CODEC = RecordCodecBuilder.create(instance -> {
		return instance.group(
				DisguiseType.CODEC.fieldOf("disguise").forGetter(c -> c.disguise),
				Codec.INT.fieldOf("seconds").forGetter(c -> c.durationTicks / 20)
		).apply(instance, ApplyDisguisePackageBehavior::new);
	});

	private final DisguiseType disguise;
	private final int durationTicks;

	private long finishTime = -1;

	public ApplyDisguisePackageBehavior(DisguiseType disguise, int durationSeconds) {
		this.disguise = disguise;
		this.durationTicks = durationSeconds * 20;
	}

	@Override
	public void register(IGamePhase game, EventRegistrar events) {
		events.listen(GamePackageEvents.APPLY_PACKAGE, (player, sendingPlayer) -> {
			if (this.finishTime == -1) {
				this.apply(game);
				this.finishTime = game.ticks() + this.durationTicks;
				return true;
			} else {
				return false;
			}
		});
		events.listen(GamePhaseEvents.TICK, () -> tick(game));
	}

	private void apply(IGamePhase game) {
		for (ServerPlayerEntity player : game.getParticipants()) {
			ServerPlayerDisguises.set(player, this.disguise);
		}
	}

	private void disable(IGamePhase game) {
		for (ServerPlayerEntity player : game.getParticipants()) {
			ServerPlayerDisguises.clear(player, this.disguise);
		}
	}

	private void tick(IGamePhase game) {
		if (finishTime != -1 && game.ticks() >= finishTime) {
			disable(game);
			finishTime = -1;
		}
	}
}
