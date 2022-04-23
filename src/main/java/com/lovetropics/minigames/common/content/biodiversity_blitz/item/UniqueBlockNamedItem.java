package com.lovetropics.minigames.common.content.biodiversity_blitz.item;

import net.minecraft.block.Block;
import net.minecraft.item.BlockNamedItem;
import net.minecraft.item.Item;

import java.util.Map;

public final class UniqueBlockNamedItem extends BlockNamedItem {
    public UniqueBlockNamedItem(Block blockIn, Properties properties) {
        super(blockIn, properties);
    }

    @Override
    public void registerBlocks(Map<Block, Item> blockToItemMap, Item itemIn) {

    }

    @Override
    public void removeFromBlockToItemMap(Map<Block, Item> blockToItemMap, Item itemIn) {

    }
}
