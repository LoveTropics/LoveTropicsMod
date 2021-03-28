package com.lovetropics.minigames.common.item.minigame;

import com.lovetropics.minigames.LoveTropics;
import com.lovetropics.minigames.common.entity.DriftwoodEntity;
import com.lovetropics.minigames.common.entity.DriftwoodRider;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nullable;
import java.util.List;

public class PaddleItem extends Item {
	public PaddleItem(Properties properties) {
		super(properties);
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void addInformation(ItemStack stack, @Nullable World world, List<ITextComponent> tooltip, ITooltipFlag flag) {
		tooltip.add(new StringTextComponent("This might come in handy").mergeStyle(TextFormatting.AQUA));
	}

	@Override
	public ActionResult<ItemStack> onItemRightClick(World world, PlayerEntity player, Hand hand) {
		ItemStack stack = player.getHeldItem(hand);
		if (world.isRemote) {
			return ActionResult.resultPass(stack);
		}

		DriftwoodRider rider = player.getCapability(LoveTropics.driftwoodRiderCap()).orElse(null);
		if (rider != null) {
			DriftwoodEntity driftwood = rider.getRiding();
			if (driftwood != null) {
				if (driftwood.paddle(player.rotationYaw)) {
					player.swing(hand, true);
					return ActionResult.resultSuccess(stack);
				} else {
					return ActionResult.resultFail(stack);
				}
			}
		}

		return ActionResult.resultPass(stack);
	}
}
