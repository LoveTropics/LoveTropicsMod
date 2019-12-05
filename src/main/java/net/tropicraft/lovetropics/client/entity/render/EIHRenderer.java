package net.tropicraft.lovetropics.client.entity.render;

import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.util.ResourceLocation;
import net.tropicraft.lovetropics.Constants;
import net.tropicraft.lovetropics.client.entity.model.EIHModel;
import net.tropicraft.lovetropics.common.entity.neutral.EIHEntity;

import javax.annotation.Nullable;

public class EIHRenderer extends MobRenderer<EIHEntity, EIHModel> {

    private static final ResourceLocation TEXTURE_SLEEP = new ResourceLocation(Constants.MODID, "textures/entity/eih/headtext.png");
    private static final ResourceLocation TEXTURE_AWARE = new ResourceLocation(Constants.MODID, "textures/entity/eih/headawaretext.png");
    private static final ResourceLocation TEXTURE_ANGRY = new ResourceLocation(Constants.MODID, "textures/entity/eih/headangrytext.png");

    public EIHRenderer(final EntityRendererManager rendererManager) {
        super(rendererManager, new EIHModel(), 1.2F);
    }

    @Override
    protected void preRenderCallback(final EIHEntity eih, final float partialTickTime) {
        GlStateManager.scalef(2.0F, 1.75F, 2.0F);
    }

    @Nullable
    @Override
    protected ResourceLocation getEntityTexture(final EIHEntity eih) {
        if (eih.isAware()) {
            return TEXTURE_AWARE;
        } else if (eih.isAngry()) {
            return TEXTURE_ANGRY;
        } else {
            return TEXTURE_SLEEP;
        }
    }
}
