package net.tropicraft.lovetropics.client.entity.render;

import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.util.ResourceLocation;
import net.tropicraft.lovetropics.client.TropicraftRenderUtils;
import net.tropicraft.lovetropics.client.entity.model.TreeFrogModel;
import net.tropicraft.lovetropics.common.entity.neutral.TreeFrogEntity;

public class TreeFrogRenderer extends MobRenderer<TreeFrogEntity, TreeFrogModel> {

    public TreeFrogRenderer(final EntityRendererManager rendererManager) {
        super(rendererManager, new TreeFrogModel(), 0.5F);
        shadowOpaque = 0.5f;
        shadowSize = 0.3f;
    }
    
    @Override
	protected ResourceLocation getEntityTexture(TreeFrogEntity entity) {
        return TropicraftRenderUtils.getTextureEntity("treefrog/treefrog" + entity.getColor());
    }
}
