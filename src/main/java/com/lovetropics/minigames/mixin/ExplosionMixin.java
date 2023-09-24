package com.lovetropics.minigames.mixin;

import com.lovetropics.minigames.common.core.game.IGameManager;
import com.lovetropics.minigames.common.core.game.IGamePhase;
import com.lovetropics.minigames.common.core.game.behavior.event.GameLivingEntityEvents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.enchantment.ProtectionEnchantment;
import net.minecraft.world.level.Explosion;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value = Explosion.class, priority = 1050)
public class ExplosionMixin {
    @Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/enchantment/ProtectionEnchantment;getExplosionKnockbackAfterDampener(Lnet/minecraft/world/entity/LivingEntity;D)D"), method = "explode()V")
    private double ltminigames$modifyKnockback(LivingEntity entity, double amount) {
        amount = ProtectionEnchantment.getExplosionKnockbackAfterDampener(entity, amount);
        IGamePhase game = IGameManager.get().getGamePhaseFor(entity);
        if (game != null) {
            amount = game.invoker(GameLivingEntityEvents.MODIFY_EXPLOSION_KNOCKBACK).getKnockback(entity, (Explosion) (Object) this, amount, amount);
        }
        return amount;
    }
}