package com.lovetropics.minigames.common.content.mangroves_and_pianguas.behavior.event;

import com.lovetropics.minigames.common.content.mangroves_and_pianguas.state.MpPlot;
import com.lovetropics.minigames.common.core.game.behavior.event.GameEventType;
import net.minecraft.entity.player.ServerPlayerEntity;

public final class MpEvents {
	public static final GameEventType<AssignPlot> ASSIGN_PLOT = GameEventType.create(AssignPlot.class, listeners -> (player, plot) -> {
		for (AssignPlot listener : listeners) {
			listener.onAssignPlot(player, plot);
		}
	});

	private MpEvents() {
	}

	public interface AssignPlot {
		void onAssignPlot(ServerPlayerEntity player, MpPlot plot);
	}
}
