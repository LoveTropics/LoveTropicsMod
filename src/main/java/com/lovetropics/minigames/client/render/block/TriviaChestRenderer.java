package com.lovetropics.minigames.client.render.block;

import com.lovetropics.minigames.LoveTropics;
import com.lovetropics.minigames.common.content.river_race.block.TriviaChestBlockEntity;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.ChestRenderer;
import net.minecraft.client.resources.model.Material;
import net.minecraft.world.level.block.state.properties.ChestType;

public class TriviaChestRenderer extends ChestRenderer<TriviaChestBlockEntity> {
	private static final Material MATERIAL = new Material(Sheets.CHEST_SHEET, LoveTropics.location("entity/chest/trivia"));

	public TriviaChestRenderer(BlockEntityRendererProvider.Context context) {
		super(context);
	}

	@Override
	protected Material getMaterial(TriviaChestBlockEntity tileEntity, ChestType chestType) {
		return MATERIAL;
	}
}
