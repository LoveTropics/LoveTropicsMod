package com.lovetropics.minigames.common.block;

import java.util.List;

import com.lovetropics.minigames.common.block.tileentity.DonationTileEntity;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.IBlockReader;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class DonationBlock extends Block {

	public DonationBlock(Block.Properties properties) {
		super(properties);
	}
	
	@Override
	public boolean hasTileEntity(BlockState state) {
	    return true;
	}

	@Override
	public TileEntity createTileEntity(BlockState state, IBlockReader world) {
		return LoveTropicsBlocks.DONATION_TILE.create();
	}

	@Override
	@OnlyIn(Dist.CLIENT)
    public void addInformation(ItemStack stack, IBlockReader worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
	    super.addInformation(stack, worldIn, tooltip, flagIn);
	    tooltip.add(new TranslationTextComponent(this.getTranslationKey() + ".desc").applyTextStyle(TextFormatting.GRAY));
	}
}
