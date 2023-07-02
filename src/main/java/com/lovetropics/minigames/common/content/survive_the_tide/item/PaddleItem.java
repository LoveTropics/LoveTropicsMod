package com.lovetropics.minigames.common.content.survive_the_tide.item;

import com.lovetropics.minigames.LoveTropics;
import com.lovetropics.minigames.common.content.survive_the_tide.entity.DriftwoodEntity;
import com.lovetropics.minigames.common.content.survive_the_tide.entity.DriftwoodRider;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
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
	public void appendHoverText(ItemStack stack, @Nullable Level world, List<Component> tooltip, TooltipFlag flag) {
		tooltip.add(Component.literal("This might come in handy").withStyle(ChatFormatting.AQUA));
	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand hand) {
		ItemStack stack = player.getItemInHand(hand);
		if (world.isClientSide) {
			return InteractionResultHolder.pass(stack);
		}

		DriftwoodRider rider = player.getCapability(LoveTropics.DRIFTWOOD_RIDER).orElse(null);
		if (rider != null) {
			DriftwoodEntity driftwood = rider.getRiding();
			if (driftwood != null) {
				if (driftwood.paddle(player.getYRot())) {
					player.swing(hand, true);
					return InteractionResultHolder.success(stack);
				} else {
					return InteractionResultHolder.fail(stack);
				}
			}
		}

		return InteractionResultHolder.pass(stack);
	}
}
