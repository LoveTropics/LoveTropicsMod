package com.lovetropics.minigames.client.render;

import com.lovetropics.minigames.Constants;
import com.lovetropics.minigames.client.game.ClientGameStateManager;
import com.lovetropics.minigames.common.content.biodiversity_blitz.BiodiversityBlitz;
import com.lovetropics.minigames.common.content.biodiversity_blitz.client_state.ClientMobSpawnState;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.RenderBuffers;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

@Mod.EventBusSubscriber(modid = Constants.MODID, value = Dist.CLIENT)
public class GameRendering {
    @SubscribeEvent
    public static void render(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_WEATHER) {
            return;
        }

        ClientMobSpawnState state = ClientGameStateManager.getOrNull(BiodiversityBlitz.MOB_SPAWN);
        if (state == null) {
            return;
        }

        AABB b = state.plot().asAabb();
        PoseStack matrices = event.getPoseStack();
        Vec3 vec3 = event.getCamera().getPosition();
        RenderBuffers buffers = Minecraft.getInstance().renderBuffers();
        VertexConsumer cons = buffers.bufferSource().getBuffer(GameRenderTypes.TRANSLUCENT_NO_TEX);

        PoseStack mv = RenderSystem.getModelViewStack();
        mv.pushPose(); mv.setIdentity(); RenderSystem.applyModelViewMatrix();

        buildBox(cons, matrices, (float)(b.minX - vec3.x), (float)(b.maxX - vec3.x), (float)(b.minY - vec3.y), (float)(b.maxY - vec3.y), (float)(b.minZ - vec3.z), (float)(b.maxZ - vec3.z), 255, 0, 0, 160);

        // Flush vertices
        buffers.bufferSource().endLastBatch();

        mv.popPose(); RenderSystem.applyModelViewMatrix();
    }

    public static void buildBox(VertexConsumer buffer, PoseStack matrices, float x1, float x2, float y1, float y2, float z1, float z2, int r, int g, int b, int a) {
        PoseStack.Pose entry = matrices.last();

        buildNorthFacing(buffer, entry, x1, x2, y1, y2, z1, r, g, b, a, 0);
        buildNorthFacing(buffer, entry, x1, x2, y1, y2, z2, r, g, b, a, 0);

        buildEastFacing(buffer, entry, y1, y2, z1, z2, x1, r, g, b, a, 0);
        buildEastFacing(buffer, entry, y1, y2, z1, z2, x2, r, g, b, a, 0);
    }

    public static void buildNorthFacing(VertexConsumer buffer, PoseStack.Pose entry, float x1, float x2, float y1, float y2, float z, int r, int g, int b, int a, int a2) {
        Matrix4f model = entry.pose();
        Matrix3f normal = entry.normal();

        buffer.vertex(model, x1, y2, z).color(r, g, b, a2)
                .uv(0.0F, 1.0F).uv2(LightTexture.FULL_BRIGHT).normal(normal, 0.0F, 1.0F, 0.0F).endVertex();
        buffer.vertex(model, x2, y2, z).color(r, g, b, a2)
                .uv(1.0F, 1.0F).uv2(LightTexture.FULL_BRIGHT).normal(normal, 0.0F, 1.0F, 0.0F).endVertex();
        buffer.vertex(model, x2, y1, z).color(r, g, b, a)
                .uv(1.0F, 0.0F).uv2(LightTexture.FULL_BRIGHT).normal(normal, 0.0F, 1.0F, 0.0F).endVertex();
        buffer.vertex(model, x1, y1, z).color(r, g, b, a)
                .uv(0.0F, 0.0F).uv2(LightTexture.FULL_BRIGHT).normal(normal, 0.0F, 1.0F, 0.0F).endVertex();

        // Reverse

        buffer.vertex(model, x1, y2, z).color(r, g, b, a2)
                .uv(0.0F, 1.0F).uv2(LightTexture.FULL_BRIGHT).normal(normal, 0.0F, 1.0F, 0.0F).endVertex();
        buffer.vertex(model, x1, y1, z).color(r, g, b, a)
                .uv(0.0F, 0.0F).uv2(LightTexture.FULL_BRIGHT).normal(normal, 0.0F, 1.0F, 0.0F).endVertex();
        buffer.vertex(model, x2, y1, z).color(r, g, b, a)
                .uv(1.0F, 0.0F).uv2(LightTexture.FULL_BRIGHT).normal(normal, 0.0F, 1.0F, 0.0F).endVertex();
        buffer.vertex(model, x2, y2, z).color(r, g, b, a2)
                .uv(1.0F, 1.0F).uv2(LightTexture.FULL_BRIGHT).normal(normal, 0.0F, 1.0F, 0.0F).endVertex();
    }

    public static void buildEastFacing(VertexConsumer buffer, PoseStack.Pose entry, float y1, float y2, float z1, float z2, float x, int r, int g, int b, int a, int a2) {
        Matrix4f model = entry.pose();
        Matrix3f normal = entry.normal();

        buffer.vertex(model, x, y1, z2).color(r, g, b, a)
                .uv(0.0F, 1.0F).uv2(LightTexture.FULL_BRIGHT).normal(normal, 0.0F, 1.0F, 0.0F).endVertex();
        buffer.vertex(model, x, y2, z2).color(r, g, b, a2)
                .uv(1.0F, 1.0F).uv2(LightTexture.FULL_BRIGHT).normal(normal, 0.0F, 1.0F, 0.0F).endVertex();
        buffer.vertex(model, x, y2, z1).color(r, g, b, a2)
                .uv(1.0F, 0.0F).uv2(LightTexture.FULL_BRIGHT).normal(normal, 0.0F, 1.0F, 0.0F).endVertex();
        buffer.vertex(model, x, y1, z1).color(r, g, b, a)
                .uv(0.0F, 0.0F).uv2(LightTexture.FULL_BRIGHT).normal(normal, 0.0F, 1.0F, 0.0F).endVertex();

        // Reverse

        buffer.vertex(model, x, y1, z2).color(r, g, b, a)
                .uv(0.0F, 1.0F).uv2(LightTexture.FULL_BRIGHT).normal(normal, 0.0F, 1.0F, 0.0F).endVertex();
        buffer.vertex(model, x, y1, z1).color(r, g, b, a)
                .uv(0.0F, 0.0F).uv2(LightTexture.FULL_BRIGHT).normal(normal, 0.0F, 1.0F, 0.0F).endVertex();
        buffer.vertex(model, x, y2, z1).color(r, g, b, a2)
                .uv(1.0F, 0.0F).uv2(LightTexture.FULL_BRIGHT).normal(normal, 0.0F, 1.0F, 0.0F).endVertex();
        buffer.vertex(model, x, y2, z2).color(r, g, b, a2)
                .uv(1.0F, 1.0F).uv2(LightTexture.FULL_BRIGHT).normal(normal, 0.0F, 1.0F, 0.0F).endVertex();
    }
}
