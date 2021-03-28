package com.lovetropics.minigames.common.minigames.behaviours.instances;

import com.lovetropics.minigames.common.minigames.IMinigameInstance;
import com.lovetropics.minigames.common.minigames.behaviours.IMinigameBehavior;
import com.mojang.serialization.Codec;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.FoodStats;

public final class DisableHungerBehavior implements IMinigameBehavior {
	public static final Codec<DisableHungerBehavior> CODEC = Codec.unit(DisableHungerBehavior::new);

	private final CompoundNBT foodStats = new CompoundNBT();

	public DisableHungerBehavior() {
		FoodStats foodStats = new FoodStats();
		foodStats.write(this.foodStats);
	}

	@Override
	public void onParticipantUpdate(IMinigameInstance minigame, ServerPlayerEntity player) {
		if (player.ticksExisted % 20 == 0) {
			player.getFoodStats().read(this.foodStats);
		}
	}
}
