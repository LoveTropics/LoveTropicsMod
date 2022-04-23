package com.lovetropics.minigames.common.content.survive_the_tide;

import com.lovetropics.minigames.Constants;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.command.CommandSource;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.ChestTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.World;

import static net.minecraft.command.Commands.literal;

public class ResetIslandChestsCommand {
	public static void register(final CommandDispatcher<CommandSource> dispatcher) {
		dispatcher.register(
			literal("game")
			.then(literal("resetIslandChests").requires(s -> s.hasPermission(4))
			.executes(c -> {
				World world = c.getSource().getLevel();

				if (!world.dimension().location().getNamespace().equals(Constants.MODID)) {
					c.getSource().sendSuccess(new StringTextComponent("Must use this command in workspace dimension"), true);
					return 0;
				}

				for (TileEntity te : world.blockEntityList) {
					if (te instanceof ChestTileEntity) {
						ChestTileEntity cte = (ChestTileEntity)te;
						cte.clearContent();

						CompoundNBT tag = new CompoundNBT();
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
