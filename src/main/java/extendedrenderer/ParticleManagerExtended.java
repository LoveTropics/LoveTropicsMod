package extendedrenderer;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.particle.ParticleManager;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.culling.ClippingHelper;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.world.ClientWorld;

import javax.annotation.Nullable;

public class ParticleManagerExtended extends ParticleManager {

    public ParticleManagerExtended(ClientWorld world, TextureManager textureManager) {
        super(world, textureManager);
    }

    @Override
    public void renderParticles(MatrixStack matrixStackIn, IRenderTypeBuffer.Impl bufferIn, LightTexture lightTextureIn, ActiveRenderInfo activeRenderInfoIn, float partialTicks, @Nullable ClippingHelper clippingHelper) {
        System.out.println("particle manager hooked!");
        super.renderParticles(matrixStackIn, bufferIn, lightTextureIn, activeRenderInfoIn, partialTicks, clippingHelper);
    }
}
