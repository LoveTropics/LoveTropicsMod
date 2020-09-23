package com.lovetropics.minigames.common.item.map;

import com.lovetropics.minigames.client.map.MapWorkspaceTracer;
import com.lovetropics.minigames.client.map.RegionEditOperator;
import com.lovetropics.minigames.client.map.RegionTraceTarget;
import com.lovetropics.minigames.common.network.LTNetwork;
import com.lovetropics.minigames.common.network.map.UpdateWorkspaceRegionMessage;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;

import javax.annotation.Nullable;

public final class EditRegionItem extends Item {
	private static Mode mode = Mode.RESIZE;
	private static int useTick;

	public EditRegionItem(Properties properties) {
		super(properties);
	}

	@Override
	public ActionResult<ItemStack> onItemRightClick(World world, PlayerEntity player, Hand hand) {
		ItemStack stack = player.getHeldItem(hand);

		if (world.isRemote) {
			RegionTraceTarget traceResult = MapWorkspaceTracer.trace(player);
			if (traceResult == null) {
				return ActionResult.resultPass(stack);
			}

			useTick = player.ticksExisted;

			if (mode == Mode.REMOVE) {
				LTNetwork.CHANNEL.sendToServer(new UpdateWorkspaceRegionMessage(traceResult.entry.id, null));
				return ActionResult.resultSuccess(stack);
			}

			MapWorkspaceTracer.select(player, traceResult, target -> mode.createEdit(target));
			return ActionResult.resultSuccess(stack);
		}

		return ActionResult.resultPass(stack);
	}

	@Override
	public boolean onEntitySwing(ItemStack stack, LivingEntity entity) {
		if (entity.world.isRemote && entity.ticksExisted != useTick) {
			mode = mode.getNext();

			MapWorkspaceTracer.stopEditing();

			if (entity instanceof PlayerEntity) {
				PlayerEntity player = (PlayerEntity) entity;
				ITextComponent message = new StringTextComponent("Changed mode to: ")
						.appendSibling(new StringTextComponent(mode.key).applyTextStyle(mode.color));
				player.sendStatusMessage(message, true);
			}
		}

		return false;
	}

	enum Mode {
		RESIZE("resize", TextFormatting.BLUE),
		MOVE("move", TextFormatting.BLUE),
		// TODO: Provide commands that can operate on selected regions
		SELECT("select", TextFormatting.BLUE),
		REMOVE("remove", TextFormatting.RED);

		static final Mode[] MODES = values();

		final String key;
		final TextFormatting color;

		Mode(String key, TextFormatting color) {
			this.key = key;
			this.color = color;
		}

		@Nullable
		RegionEditOperator createEdit(RegionTraceTarget target) {
			switch (this) {
				case RESIZE: return new RegionEditOperator.Resize(target);
				case MOVE: return new RegionEditOperator.Move(target);
				case SELECT: return new RegionEditOperator.Select(target);
				default: return null;
			}
		}

		static Mode byIndex(int index) {
			return MODES[index % MODES.length];
		}

		Mode getNext() {
			return byIndex(ordinal() + 1);
		}
	}
}
