package com.lovetropics.minigames.common.minigames.behaviours.instances;

import com.lovetropics.minigames.common.minigames.IMinigameInstance;
import com.lovetropics.minigames.common.minigames.behaviours.IMinigameBehavior;
import com.mojang.serialization.Codec;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.TNTBlock;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.event.world.BlockEvent;

public class TNTAutoFuseBehavior implements IMinigameBehavior {
	public static final Codec<TNTAutoFuseBehavior> CODEC = Codec.unit(TNTAutoFuseBehavior::new);

	@Override
	public void onEntityPlaceBlock(IMinigameInstance minigame, Entity entity, BlockPos pos, BlockState state, BlockEvent.EntityPlaceEvent event) {
		if (event.getState().getBlock() instanceof TNTBlock && event.getWorld() instanceof World) {
			event.getState().getBlock().catchFire(event.getState(), (World)event.getWorld(), event.getPos(), null, null);
			event.getWorld().setBlockState(event.getPos(), Blocks.AIR.getDefaultState(), 11);
		}
	}
}
