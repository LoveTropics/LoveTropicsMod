package net.tropicraft.lovetropics.client.entity.render;

import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.tropicraft.lovetropics.Constants;
import net.tropicraft.lovetropics.client.entity.model.FailgullModel;
import net.tropicraft.lovetropics.common.entity.passive.FailgullEntity;
import net.tropicraft.lovetropics.common.entity.passive.TropiCreeperEntity;

@OnlyIn(Dist.CLIENT)
public class FailgullRenderer extends MobRenderer<FailgullEntity, FailgullModel> {
    private static final ResourceLocation FAILGULL_TEXTURE = new ResourceLocation(Constants.MODID, "textures/entity/failgull.png");

    public FailgullRenderer(EntityRendererManager rendererManager) {
        super(rendererManager, new FailgullModel(), 0.25F);
    }

    protected ResourceLocation getEntityTexture(FailgullEntity e) {
        return FAILGULL_TEXTURE;
    }
}
