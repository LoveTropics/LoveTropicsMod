package net.tropicraft.lovetropics.client.entity.render;

import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.tropicraft.lovetropics.Constants;
import net.tropicraft.lovetropics.client.entity.model.TropiCreeperModel;
import net.tropicraft.lovetropics.common.entity.passive.TropiCreeperEntity;

@OnlyIn(Dist.CLIENT)
public class TropiCreeperRenderer extends MobRenderer<TropiCreeperEntity, TropiCreeperModel> {
    private static final ResourceLocation CREEPER_TEXTURE = new ResourceLocation(Constants.MODID, "textures/entity/tropicreeper.png");

    public TropiCreeperRenderer(EntityRendererManager rendererManager) {
        super(rendererManager, new TropiCreeperModel(), 0.5F);
    }

    protected ResourceLocation getEntityTexture(TropiCreeperEntity e) {
        return CREEPER_TEXTURE;
    }
}
