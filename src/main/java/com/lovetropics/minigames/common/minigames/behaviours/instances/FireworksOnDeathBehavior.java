package com.lovetropics.minigames.common.minigames.behaviours.instances;

import com.lovetropics.lib.entity.FireworkUtil;
import com.lovetropics.minigames.common.minigames.IMinigameInstance;
import com.lovetropics.minigames.common.minigames.behaviours.IMinigameBehavior;
import com.mojang.datafixers.Dynamic;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.gen.Heightmap;
import net.minecraftforge.event.entity.living.LivingDeathEvent;

public final class FireworksOnDeathBehavior implements IMinigameBehavior {
	public FireworksOnDeathBehavior() {
	}

	public static <T> FireworksOnDeathBehavior parse(Dynamic<T> root) {
		return new FireworksOnDeathBehavior();
	}

	@Override
	public void onPlayerDeath(final IMinigameInstance minigame, ServerPlayerEntity player, LivingDeathEvent event) {
		BlockPos fireworkPos = player.world.getHeight(Heightmap.Type.MOTION_BLOCKING, player.getPosition());
		FireworkUtil.spawnFirework(fireworkPos, player.world, FireworkUtil.Palette.ISLAND_ROYALE.getPalette());
	}
}
