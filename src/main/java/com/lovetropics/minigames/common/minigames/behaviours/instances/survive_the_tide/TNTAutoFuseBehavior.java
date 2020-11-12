package com.lovetropics.minigames.common.minigames.behaviours.instances.survive_the_tide;

import com.lovetropics.minigames.common.Util;
import com.lovetropics.minigames.common.map.MapRegion;
import com.lovetropics.minigames.common.minigames.IMinigameInstance;
import com.lovetropics.minigames.common.minigames.MinigameManager;
import com.lovetropics.minigames.common.minigames.behaviours.IMinigameBehavior;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.datafixers.Dynamic;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.TNTBlock;
import net.minecraft.command.arguments.ParticleArgument;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.particles.IParticleData;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EntityPredicates;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.BossInfo;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerBossInfo;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.event.world.BlockEvent;

import java.util.ArrayList;
import java.util.List;

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
