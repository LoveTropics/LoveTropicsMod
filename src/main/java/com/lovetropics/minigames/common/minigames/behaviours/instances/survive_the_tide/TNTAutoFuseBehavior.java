package com.lovetropics.minigames.common.minigames.behaviours.instances.survive_the_tide;

import com.lovetropics.minigames.common.minigames.IMinigameInstance;
import com.lovetropics.minigames.common.minigames.behaviours.IMinigameBehavior;
import com.mojang.datafixers.Dynamic;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.TNTBlock;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.event.world.BlockEvent;

public class TNTAutoFuseBehavior implements IMinigameBehavior {

	public TNTAutoFuseBehavior() {
	}

	public static <T> TNTAutoFuseBehavior parse(Dynamic<T> root) {
		return new TNTAutoFuseBehavior();
	}

	@Override
	public void onEntityPlaceBlock(IMinigameInstance minigame, Entity entity, BlockPos pos, BlockState state, BlockEvent.EntityPlaceEvent event) {
		if (event.getState().getBlock() instanceof TNTBlock && event.getWorld() instanceof World) {
			event.getState().getBlock().catchFire(event.getState(), (World)event.getWorld(), event.getPos(), null, null);
			event.getWorld().setBlockState(event.getPos(), Blocks.AIR.getDefaultState(), 11);
		}
	}
}
