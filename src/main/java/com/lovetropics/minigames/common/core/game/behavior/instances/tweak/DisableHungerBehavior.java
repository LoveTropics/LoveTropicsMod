package com.lovetropics.minigames.common.core.game.behavior.instances.tweak;

import com.lovetropics.minigames.common.core.game.IGameInstance;
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
		foodStats.write(this.foodStats);
	}

	@Override
	public void register(IGameInstance registerGame, EventRegistrar events) {
		events.listen(GamePlayerEvents.TICK, (game, player) -> {
			if (player.ticksExisted % 20 == 0) {
				player.getFoodStats().read(this.foodStats);
			}
		});
	}
}
