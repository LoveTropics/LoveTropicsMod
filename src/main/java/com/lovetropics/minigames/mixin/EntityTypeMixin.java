package com.lovetropics.minigames.mixin;

import com.lovetropics.minigames.common.core.game.IGameManager;
import com.lovetropics.minigames.common.core.game.IGamePhase;
import com.lovetropics.minigames.common.core.game.behavior.event.GameLivingEntityEvents;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.server.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EntityType.class)
public class EntityTypeMixin<T extends Entity> {
	@Inject(
			method = "spawn(Lnet/minecraft/world/server/ServerWorld;Lnet/minecraft/nbt/CompoundNBT;Lnet/minecraft/util/text/ITextComponent;Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/entity/SpawnReason;ZZ)Lnet/minecraft/entity/Entity;",
			at = @At(value = "RETURN")
	)
	private void spawnEntity(ServerWorld world, CompoundNBT nbt, ITextComponent customName, PlayerEntity player, BlockPos pos, SpawnReason reason, boolean p_220342_7_, boolean p_220342_8_, CallbackInfoReturnable<T> ci) {
		T entity = ci.getReturnValue();
		if (entity instanceof LivingEntity) {
			IGamePhase game = IGameManager.get().getGamePhaseFor(entity);
			if (game != null) {
				game.invoker(GameLivingEntityEvents.SPAWNED).onSpawn((LivingEntity) entity, reason, (ServerPlayerEntity) player);
			}
		}
	}
}
