package com.lovetropics.minigames.client.render;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;

import static net.minecraft.client.renderer.RenderType.create;

// LTMinigames Render Types
// Extends RenderStateShard to access protected fields
public class GameRenderTypes extends RenderStateShard {
    public static final RenderType TRANSLUCENT_NO_TEX = create(
            "translucent_broken_depth", DefaultVertexFormat.BLOCK,
            VertexFormat.Mode.QUADS, 2097152, true, true, of(RENDERTYPE_TRANSLUCENT_SHADER)
    );

    public GameRenderTypes(String pName, Runnable pSetupState, Runnable pClearState) {
        super(pName, pSetupState, pClearState);

        throw new IllegalStateException("Don't call this");
    }

    public static RenderType.CompositeState of(RenderStateShard.ShaderStateShard program) {
        return RenderType.CompositeState.builder()
                .setLightmapState(LIGHTMAP)
                .setShaderState(program)
                .setTextureState(new TextureStateShard(ResourceLocation.withDefaultNamespace("textures/misc/white.png"), false, false))
                .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
                .setOutputState(TRANSLUCENT_TARGET)
                .setCullState(NO_CULL)
                .setLayeringState(POLYGON_OFFSET_LAYERING)
                .setWriteMaskState(COLOR_WRITE)
                .createCompositeState(true);
    }
}
