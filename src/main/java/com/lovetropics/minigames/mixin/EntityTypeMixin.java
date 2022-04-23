package com.lovetropics.minigames.mixin;

import com.lovetropics.minigames.common.core.game.IGameManager;
import com.lovetropics.minigames.common.core.game.IGamePhase;
import com.lovetropics.minigames.common.core.game.behavior.event.GameLivingEntityEvents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
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
	private void spawnEntity(ServerLevel world, CompoundTag nbt, Component customName, Player player, BlockPos pos, MobSpawnType reason, boolean p_220342_7_, boolean p_220342_8_, CallbackInfoReturnable<T> ci) {
		T entity = ci.getReturnValue();
		if (entity instanceof LivingEntity) {
			IGamePhase game = IGameManager.get().getGamePhaseFor(entity);
			if (game != null) {
				game.invoker(GameLivingEntityEvents.SPAWNED).onSpawn((LivingEntity) entity, reason, (ServerPlayer) player);
			}
		}
	}
}
