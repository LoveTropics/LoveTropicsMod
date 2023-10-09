package com.lovetropics.minigames.common.core.game.behavior.instances.action;

import com.lovetropics.minigames.common.core.diguise.DisguiseType;
import com.lovetropics.minigames.common.core.diguise.ServerPlayerDisguises;
import com.lovetropics.minigames.common.core.game.IGamePhase;
import com.lovetropics.minigames.common.core.game.behavior.IGameBehavior;
import com.lovetropics.minigames.common.core.game.behavior.event.EventRegistrar;
import com.lovetropics.minigames.common.core.game.behavior.event.GameActionEvents;
import com.lovetropics.minigames.common.core.game.behavior.event.GamePhaseEvents;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.server.level.ServerPlayer;

public final class ApplyGlobalDisguiseAction implements IGameBehavior {
	public static final Codec<ApplyGlobalDisguiseAction> CODEC = RecordCodecBuilder.create(i -> i.group(
			DisguiseType.CODEC.fieldOf("disguise").forGetter(c -> c.disguise),
			Codec.INT.fieldOf("seconds").forGetter(c -> c.durationTicks / 20)
	).apply(i, ApplyGlobalDisguiseAction::new));

	private final DisguiseType disguise;
	private final int durationTicks;

	private long finishTime = -1;

	public ApplyGlobalDisguiseAction(DisguiseType disguise, int durationSeconds) {
		this.disguise = disguise;
		this.durationTicks = durationSeconds * 20;
	}

	@Override
	public void register(IGamePhase game, EventRegistrar events) {
		events.listen(GameActionEvents.APPLY, (context) -> {
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
		for (ServerPlayer player : game.getParticipants()) {
			ServerPlayerDisguises.set(player, this.disguise);
		}
	}

	private void disable(IGamePhase game) {
		for (ServerPlayer player : game.getParticipants()) {
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
