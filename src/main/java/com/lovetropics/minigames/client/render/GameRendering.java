package com.lovetropics.minigames.client.render;

import com.lovetropics.minigames.Constants;
import com.lovetropics.minigames.client.game.ClientGameStateManager;
import com.lovetropics.minigames.common.content.biodiversity_blitz.BiodiversityBlitz;
import com.lovetropics.minigames.common.content.biodiversity_blitz.client_state.ClientBbMobSpawnState;
import com.lovetropics.minigames.common.content.biodiversity_blitz.client_state.ClientBbScoreboardState;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderBuffers;
import net.minecraft.network.chat.Component;
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

        ClientBbMobSpawnState state = ClientGameStateManager.getOrNull(BiodiversityBlitz.MOB_SPAWN);
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

        // TODO: logic bug- scoreboard state only ever renders when mob spawn is also active!
        ClientBbScoreboardState scoreboardState = ClientGameStateManager.getOrNull(BiodiversityBlitz.SCOREBOARD);
        if (scoreboardState != null) {
            renderScoreboardState(scoreboardState, matrices, vec3, buffers.bufferSource(), cons);
        }

        // Flush vertices
        buffers.bufferSource().endLastBatch();

        mv.popPose(); RenderSystem.applyModelViewMatrix();
    }

    private static void renderScoreboardState(ClientBbScoreboardState state, PoseStack matrices, Vec3 camera, MultiBufferSource buffers, VertexConsumer cons) {
        AABB b = new AABB(state.start(), state.end());
        PoseStack.Pose entry = matrices.last();

        if (state.side()) {
            buildEastFacing(cons, entry,  (float)(b.minY - camera.y), (float)(b.maxY - camera.y), (float)(b.minZ - camera.z), (float)(b.maxZ - camera.z), (float)(b.minX - camera.x), 0, 0, 0, 30, 60);

            matrices.pushPose();

            matrices.translate(b.minX - camera.x(), b.maxY - camera.y(), b.maxZ - camera.z());
            matrices.mulPose(Axis.XN.rotationDegrees(180));
            matrices.mulPose(Axis.YN.rotationDegrees(90));
            matrices.scale(0.04f, 0.04f, 0.04f);
            int off = 1;
            for (Component comp : state.content()) {
                Minecraft.getInstance().font.drawInBatch(
                        comp,
                        1,
                        off,
                        0xFFFFFF,
                        false,
                        matrices.last().pose(),
                        buffers,
                        Font.DisplayMode.NORMAL,
                        0,
                        LightTexture.FULL_BRIGHT
                );

                off += 10;
            }

            matrices.popPose();
        } else {
            buildNorthFacing(cons, entry, (float)(b.minX - camera.x), (float)(b.maxX - camera.x), (float)(b.minY - camera.y), (float)(b.maxY - camera.y), (float)(b.minZ - camera.z), 0, 0, 0, 30, 60);
        }
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
