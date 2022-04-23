package com.lovetropics.minigames.common.core.map.item;

import com.lovetropics.minigames.client.map.MapWorkspaceTracer;
import com.lovetropics.minigames.client.map.RegionEditOperator;
import com.lovetropics.minigames.client.map.RegionTraceTarget;
import com.lovetropics.minigames.common.core.network.LoveTropicsNetwork;
import com.lovetropics.minigames.common.core.network.workspace.UpdateWorkspaceRegionMessage;
import net.minecraft.client.Minecraft;
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
	public ActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
		ItemStack stack = player.getItemInHand(hand);

		if (world.isClientSide && isClientPlayer(player)) {
			RegionTraceTarget traceResult = MapWorkspaceTracer.trace(player);

			useTick = player.tickCount;

			if (traceResult != null && mode == Mode.REMOVE) {
				LoveTropicsNetwork.CHANNEL.sendToServer(new UpdateWorkspaceRegionMessage(traceResult.entry.id, null));
				return ActionResult.success(stack);
			}

			if (MapWorkspaceTracer.select(player, traceResult, target -> mode.createEdit(target))) {
				return ActionResult.success(stack);
			} else {
				return ActionResult.pass(stack);
			}
		}

		return ActionResult.pass(stack);
	}

	@Override
	public boolean onEntitySwing(ItemStack stack, LivingEntity entity) {
		if (entity.level.isClientSide && entity.tickCount != useTick && isClientPlayer(entity)) {
			mode = mode.getNext();

			MapWorkspaceTracer.stopEditing();

			if (entity instanceof PlayerEntity) {
				PlayerEntity player = (PlayerEntity) entity;
				ITextComponent message = new StringTextComponent("Changed mode to: ")
						.append(new StringTextComponent(mode.key).withStyle(mode.color));
				player.displayClientMessage(message, true);
			}
		}

		return false;
	}

	private static boolean isClientPlayer(LivingEntity entity) {
		return Minecraft.getInstance().player == entity;
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
