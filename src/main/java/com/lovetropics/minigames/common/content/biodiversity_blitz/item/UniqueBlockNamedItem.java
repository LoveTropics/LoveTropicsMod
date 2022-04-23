package com.lovetropics.minigames.common.content.biodiversity_blitz.item;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.item.ItemNameBlockItem;
import net.minecraft.world.item.Item;

import java.util.Map;

import net.minecraft.world.item.Item.Properties;

public final class UniqueBlockNamedItem extends ItemNameBlockItem {
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
