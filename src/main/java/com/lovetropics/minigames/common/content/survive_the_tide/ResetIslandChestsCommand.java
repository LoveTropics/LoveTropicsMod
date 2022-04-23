package com.lovetropics.minigames.common.content.survive_the_tide;

import com.lovetropics.minigames.Constants;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;

import static net.minecraft.commands.Commands.literal;

public class ResetIslandChestsCommand {
	public static void register(final CommandDispatcher<CommandSourceStack> dispatcher) {
		dispatcher.register(
			literal("game")
			.then(literal("resetIslandChests").requires(s -> s.hasPermission(4))
			.executes(c -> {
				Level world = c.getSource().getLevel();

				if (!world.dimension().location().getNamespace().equals(Constants.MODID)) {
					c.getSource().sendSuccess(new TextComponent("Must use this command in workspace dimension"), true);
					return 0;
				}

				for (BlockEntity te : world.blockEntityList) {
					if (te instanceof ChestBlockEntity) {
						ChestBlockEntity cte = (ChestBlockEntity)te;
						cte.clearContent();

						CompoundTag tag = new CompoundTag();
						cte.save(tag);

						tag.putString("LootTable", "lt20:stt1/chest");

						cte.load(te.getBlockState(), tag);
						cte.setChanged();
					}
				}
				return 1;
			}))
		);
	}
}
