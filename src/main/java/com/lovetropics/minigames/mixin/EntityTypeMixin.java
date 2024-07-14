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
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EntityType.class)
public class EntityTypeMixin<T extends Entity> {
	@Inject(
			method = "spawn(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/entity/MobSpawnType;ZZ)Lnet/minecraft/world/entity/Entity;",
			at = @At(value = "RETURN")
	)
	private void spawnEntity(ServerLevel level, ItemStack stack, Player player, BlockPos pos, MobSpawnType spawnType, boolean shouldOffsetY, boolean shouldOffsetYMore, CallbackInfoReturnable<T> ci) {
		T entity = ci.getReturnValue();
		if (entity instanceof LivingEntity) {
			IGamePhase game = IGameManager.get().getGamePhaseFor(entity);
			if (game != null) {
				game.invoker(GameLivingEntityEvents.SPAWNED).onSpawn((LivingEntity) entity, spawnType, (ServerPlayer) player);
			}
		}
	}
}
