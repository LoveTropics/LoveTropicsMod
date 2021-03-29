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
			.then(literal("resetIslandChests").requires(s -> s.hasPermissionLevel(4))
			.executes(c -> {
				World world = c.getSource().getWorld();

				if (!world.getDimensionKey().getLocation().getNamespace().equals(Constants.MODID)) {
					c.getSource().sendFeedback(new StringTextComponent("Must use this command in workspace dimension"), true);
					return 0;
				}

				for (TileEntity te : world.loadedTileEntityList) {
					if (te instanceof ChestTileEntity) {
						ChestTileEntity cte = (ChestTileEntity)te;
						cte.clear();

						CompoundNBT tag = new CompoundNBT();
						cte.write(tag);

						tag.putString("LootTable", "lt20:stt1/chest");

						cte.read(te.getBlockState(), tag);
						cte.markDirty();
					}
				}
				return 1;
			}))
		);
	}
}
