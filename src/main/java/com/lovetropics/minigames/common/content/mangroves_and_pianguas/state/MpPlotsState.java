package com.lovetropics.minigames.common.content.mangroves_and_pianguas.state;

import com.lovetropics.minigames.common.core.game.state.GameStateKey;
import com.lovetropics.minigames.common.core.game.state.IGameState;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.ServerPlayerEntity;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.UUID;

public final class MpPlotsState implements IGameState {
	public static final GameStateKey<MpPlotsState> KEY = GameStateKey.create("Mangroves & Pianguas Plots");

	private final Map<UUID, MpPlot> plotsByPlayer = new Object2ObjectOpenHashMap<>();

	public void addPlayer(ServerPlayerEntity player, MpPlot plot) {
		this.plotsByPlayer.put(player.getUniqueID(), plot);
	}

	@Nullable
	public MpPlot removePlayer(ServerPlayerEntity player) {
		return this.plotsByPlayer.remove(player.getUniqueID());
	}

	@Nullable
	public MpPlot getPlotFor(Entity entity) {
		return this.plotsByPlayer.get(entity.getUniqueID());
	}
}
