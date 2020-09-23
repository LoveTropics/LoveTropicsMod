package com.lovetropics.minigames.common.item.map;

import com.lovetropics.minigames.client.map.MapWorkspaceTracer;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.world.World;

public final class EditRegionItem extends Item {
	public EditRegionItem(Properties properties) {
		super(properties);
	}

	@Override
	public ActionResult<ItemStack> onItemRightClick(World world, PlayerEntity player, Hand hand) {
		ItemStack stack = player.getHeldItem(hand);
		if (world.isRemote) {
			if (MapWorkspaceTracer.isEditing()) {
				MapWorkspaceTracer.stopEditing();
				return ActionResult.resultSuccess(stack);
			} else if (MapWorkspaceTracer.tryStartEditing()) {
				return ActionResult.resultSuccess(stack);
			}
		}

		return ActionResult.resultPass(stack);
	}
}
