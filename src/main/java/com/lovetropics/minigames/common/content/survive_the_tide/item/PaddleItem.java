package com.lovetropics.minigames.common.content.survive_the_tide.item;

import com.lovetropics.minigames.LoveTropics;
import com.lovetropics.minigames.common.content.survive_the_tide.entity.DriftwoodEntity;
import com.lovetropics.minigames.common.content.survive_the_tide.entity.DriftwoodRider;
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
	public void appendHoverText(ItemStack stack, @Nullable World world, List<ITextComponent> tooltip, ITooltipFlag flag) {
		tooltip.add(new StringTextComponent("This might come in handy").withStyle(TextFormatting.AQUA));
	}

	@Override
	public ActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
		ItemStack stack = player.getItemInHand(hand);
		if (world.isClientSide) {
			return ActionResult.pass(stack);
		}

		DriftwoodRider rider = player.getCapability(LoveTropics.driftwoodRiderCap()).orElse(null);
		if (rider != null) {
			DriftwoodEntity driftwood = rider.getRiding();
			if (driftwood != null) {
				if (driftwood.paddle(player.yRot)) {
					player.swing(hand, true);
					return ActionResult.success(stack);
				} else {
					return ActionResult.fail(stack);
				}
			}
		}

		return ActionResult.pass(stack);
	}
}
