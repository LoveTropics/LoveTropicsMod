package com.lovetropics.minigames.common.core.game.behavior.instances.tweak;

import com.lovetropics.minigames.common.core.game.IGamePhase;
import com.lovetropics.minigames.common.core.game.behavior.GameBehaviorType;
import com.lovetropics.minigames.common.core.game.behavior.GameBehaviorTypes;
import com.lovetropics.minigames.common.core.game.behavior.IGameBehavior;
import com.lovetropics.minigames.common.core.game.behavior.event.EventRegistrar;
import com.lovetropics.minigames.common.core.game.behavior.event.GamePlayerEvents;
import com.mojang.serialization.MapCodec;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.food.FoodData;

import java.util.function.Supplier;

public final class DisableHungerBehavior implements IGameBehavior {
	public static final MapCodec<DisableHungerBehavior> CODEC = MapCodec.unit(DisableHungerBehavior::new);

	private final CompoundTag foodStats = new CompoundTag();

	public DisableHungerBehavior() {
		FoodData foodStats = new FoodData();
		foodStats.addAdditionalSaveData(this.foodStats);
	}

	@Override
	public void register(IGamePhase game, EventRegistrar events) {
		events.listen(GamePlayerEvents.TICK, (player) -> {
			if (player.tickCount % 20 == 0) {
				player.getFoodData().readAdditionalSaveData(foodStats);
			}
		});
	}

	@Override
	public Supplier<? extends GameBehaviorType<?>> behaviorType() {
		return GameBehaviorTypes.DISABLE_HUNGER;
	}
}
