package com.lovetropics.minigames.common.core.map.item;

import com.lovetropics.minigames.client.map.MapWorkspaceTracer;
import com.lovetropics.minigames.client.map.RegionEditOperator;
import com.lovetropics.minigames.client.map.RegionTraceTarget;
import com.lovetropics.minigames.common.core.network.LoveTropicsNetwork;
import com.lovetropics.minigames.common.core.network.workspace.UpdateWorkspaceRegionMessage;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;

public final class EditRegionItem extends Item {
	private static Mode mode = Mode.RESIZE;
	private static int useTick;

	public EditRegionItem(Properties properties) {
		super(properties);
	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand hand) {
		ItemStack stack = player.getItemInHand(hand);

		if (world.isClientSide && isClientPlayer(player)) {
			RegionTraceTarget traceResult = MapWorkspaceTracer.trace(player);

			useTick = player.tickCount;

			if (traceResult != null && mode == Mode.REMOVE) {
				LoveTropicsNetwork.CHANNEL.sendToServer(new UpdateWorkspaceRegionMessage(traceResult.entry().id, null));
				return InteractionResultHolder.success(stack);
			}

			if (MapWorkspaceTracer.select(player, traceResult, target -> mode.createEdit(target))) {
				return InteractionResultHolder.success(stack);
			} else {
				return InteractionResultHolder.pass(stack);
			}
		}

		return InteractionResultHolder.pass(stack);
	}

	@Override
	public boolean onEntitySwing(ItemStack stack, LivingEntity entity) {
		if (entity.level().isClientSide && entity.tickCount != useTick && isClientPlayer(entity)) {
			mode = mode.getNext();

			MapWorkspaceTracer.stopEditing();

			if (entity instanceof Player) {
				Player player = (Player) entity;
				Component message = Component.literal("Changed mode to: ")
						.append(Component.literal(mode.key).withStyle(mode.color));
				player.displayClientMessage(message, true);
			}
		}

		return false;
	}

	private static boolean isClientPlayer(LivingEntity entity) {
		return Minecraft.getInstance().player == entity;
	}

	enum Mode {
		RESIZE("resize", ChatFormatting.BLUE),
		MOVE("move", ChatFormatting.BLUE),
		// TODO: Provide commands that can operate on selected regions
		SELECT("select", ChatFormatting.BLUE),
		REMOVE("remove", ChatFormatting.RED);

		static final Mode[] MODES = values();

		final String key;
		final ChatFormatting color;

		Mode(String key, ChatFormatting color) {
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
