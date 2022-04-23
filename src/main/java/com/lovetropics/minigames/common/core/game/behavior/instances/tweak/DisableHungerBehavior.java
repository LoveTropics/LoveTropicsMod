package com.lovetropics.minigames.common.core.game.behavior.instances.tweak;

import com.lovetropics.minigames.common.core.game.IGamePhase;
import com.lovetropics.minigames.common.core.game.behavior.IGameBehavior;
import com.lovetropics.minigames.common.core.game.behavior.event.EventRegistrar;
import com.lovetropics.minigames.common.core.game.behavior.event.GamePlayerEvents;
import com.mojang.serialization.Codec;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.FoodStats;

public final class DisableHungerBehavior implements IGameBehavior {
	public static final Codec<DisableHungerBehavior> CODEC = Codec.unit(DisableHungerBehavior::new);

	private final CompoundNBT foodStats = new CompoundNBT();

	public DisableHungerBehavior() {
		FoodStats foodStats = new FoodStats();
		foodStats.addAdditionalSaveData(this.foodStats);
	}

	@Override
	public void register(IGamePhase game, EventRegistrar events) {
		events.listen(GamePlayerEvents.TICK, (player) -> {
			if (player.tickCount % 20 == 0) {
				player.getFoodData().readAdditionalSaveData(this.foodStats);
			}
		});
	}
}
