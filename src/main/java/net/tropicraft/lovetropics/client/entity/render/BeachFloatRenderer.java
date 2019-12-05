package net.tropicraft.lovetropics.client.entity.render;

import com.mojang.blaze3d.platform.GlStateManager;

import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.tropicraft.lovetropics.client.entity.model.BeachFloatModel;
import net.tropicraft.lovetropics.common.entity.placeable.BeachFloatEntity;

public class BeachFloatRenderer extends FurnitureRenderer<BeachFloatEntity> {

    public BeachFloatRenderer(EntityRendererManager rendererManager) {
        super(rendererManager, "beach_float", new BeachFloatModel());
        shadowSize = .5F;
    }
    
    @Override
    protected double getYOffset() {
        return super.getYOffset() + 1.2;
    }
    
    @Override
    protected void setupTransforms() {
        GlStateManager.rotatef(-180f, 0, 1, 0);
    }
    
    @Override
    protected boolean rockOnZAxis() {
        return true;
    }
}
