package com.lovetropics.minigames.mixin;

import com.lovetropics.minigames.common.core.diguise.DisguiseType;
import com.lovetropics.minigames.common.core.diguise.PlayerDisguise;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(Player.class)
public abstract class PlayerMixin extends LivingEntity {
	private PlayerMixin(EntityType<? extends LivingEntity> type, Level level) {
		super(type, level);
	}

	@Override
	public boolean canBeCollidedWith() {
		DisguiseType disguise = PlayerDisguise.get((Player) (Object) this).type();
		if (disguise.entityType() == EntityType.FALLING_BLOCK) {
			return true;
		}
		return super.canBeCollidedWith();
	}
}
