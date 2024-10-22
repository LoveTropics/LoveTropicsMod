package com.lovetropics.minigames.client.render.block;

import com.lovetropics.minigames.LoveTropics;
import com.lovetropics.minigames.common.content.river_race.block.TriviaChestBlockEntity;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.ChestRenderer;
import net.minecraft.client.resources.model.Material;
import net.minecraft.world.level.block.state.properties.ChestType;

public class TriviaChestRenderer extends ChestRenderer<TriviaChestBlockEntity> {

    public static final Material TRIVIA_CHEST_MATERIAL = getChestMaterial("trivia");
    public static final Material TRIVIA_CHEST_LEFT_MATERIAL = getChestMaterial("trivia_left");
    public static final Material TRIVIA_CHEST_RIGHT_MATERIAL = getChestMaterial("trivia_right");

    private static Material getChestMaterial(ChestType chestType) {
        return switch (chestType) {
            case LEFT -> TriviaChestRenderer.TRIVIA_CHEST_LEFT_MATERIAL;
            case RIGHT -> TriviaChestRenderer.TRIVIA_CHEST_RIGHT_MATERIAL;
            default -> TriviaChestRenderer.TRIVIA_CHEST_MATERIAL;
        };
    }

    private static Material getChestMaterial(String chestName) {
        return new Material(Sheets.CHEST_SHEET, LoveTropics.location("entity/chest/" + chestName));
    }
    public TriviaChestRenderer(BlockEntityRendererProvider.Context context) {
        super(context);
    }

    protected Material getMaterial(TriviaChestBlockEntity tileEntity, ChestType chestType) {
        return getChestMaterial(chestType);
    }
}
