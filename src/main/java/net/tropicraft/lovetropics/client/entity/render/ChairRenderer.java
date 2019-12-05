package net.tropicraft.lovetropics.client.entity.render;

import com.mojang.blaze3d.platform.GlStateManager;

import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.tropicraft.lovetropics.client.entity.model.ChairModel;
import net.tropicraft.lovetropics.common.entity.placeable.ChairEntity;

public class ChairRenderer extends FurnitureRenderer<ChairEntity> {

    public ChairRenderer(EntityRendererManager rendererManager) {
        super(rendererManager, "chair", new ChairModel(), 0.0625f);
        this.shadowSize = 0.65f;
    }
    
    @Override
    protected void setupTransforms() {
        GlStateManager.translated(0, 0, -0.15);
    }
}
