package com.lovetropics.minigames.common.content.survive_the_tide.item;

import com.lovetropics.minigames.common.content.survive_the_tide.entity.LightningArrowEntity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ArrowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class LightningArrowItem extends ArrowItem {
	public LightningArrowItem(final Properties properties) {
		super(properties);
	}

	@Override
	public AbstractArrow createArrow(final Level level, final ItemStack stack, final LivingEntity shooter) {
		return new LightningArrowEntity(level, shooter);
	}
}
