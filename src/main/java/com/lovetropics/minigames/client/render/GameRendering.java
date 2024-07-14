package com.lovetropics.minigames.client.render;

import com.lovetropics.lib.BlockBox;
import com.lovetropics.minigames.Constants;
import com.lovetropics.minigames.client.game.ClientGameStateManager;
import com.lovetropics.minigames.common.content.biodiversity_blitz.BiodiversityBlitz;
import com.lovetropics.minigames.common.content.biodiversity_blitz.client_state.ClientBbMobSpawnState;
import com.lovetropics.minigames.common.content.biodiversity_blitz.client_state.ClientBbScoreboardState;
import com.lovetropics.minigames.common.core.game.client_state.GameClientStateTypes;
import com.lovetropics.minigames.common.core.game.client_state.instance.PointTagClientState;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderBuffers;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextColor;
import net.minecraft.util.CommonColors;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.ClientHooks;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.client.event.RenderNameTagEvent;

import java.util.List;

@EventBusSubscriber(modid = Constants.MODID, value = Dist.CLIENT)
public class GameRendering {
    @SubscribeEvent
    public static void render(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_WEATHER) {
            return;
        }

        PoseStack matrices = event.getPoseStack();
        Vec3 cameraPos = event.getCamera().getPosition();
        RenderBuffers buffers = Minecraft.getInstance().renderBuffers();
        VertexConsumer cons = buffers.bufferSource().getBuffer(GameRenderTypes.TRANSLUCENT_NO_TEX);

        ClientBbMobSpawnState state = ClientGameStateManager.getOrNull(BiodiversityBlitz.MOB_SPAWN);
        if (state != null) {
            for (BlockBox box : state.spawns()) {
                BlockPos min = box.min();
                BlockPos max = box.max();
                float x0 = (float) (min.getX() - cameraPos.x);
                float x1 = (float) (max.getX() + 1.0 - cameraPos.x);
                float y0 = (float) (min.getY() - cameraPos.y);
                float y1 = (float) (max.getY() + 1.0 - cameraPos.y);
                float z0 = (float) (min.getZ() - cameraPos.z);
                float z1 = (float) (max.getZ() + 1.0 - cameraPos.z);
                buildBox(cons, matrices, x0, x1, y0, y1, z0, z1, 255, 0, 0, 160);
            }
        }

        ClientBbScoreboardState scoreboardState = ClientGameStateManager.getOrNull(BiodiversityBlitz.SCOREBOARD);
        if (scoreboardState != null) {
            renderScoreboardState(scoreboardState, matrices, cameraPos, buffers.bufferSource(), cons);
        }

        // Flush vertices
        buffers.bufferSource().endBatch();
    }

    private static void renderScoreboardState(ClientBbScoreboardState state, PoseStack matrices, Vec3 camera, MultiBufferSource.BufferSource buffers, VertexConsumer cons) {
        AABB b = new AABB(state.start(), state.end());
        PoseStack.Pose entry = matrices.last();

        if (state.side()) {
            buildEastFacing(cons, entry,  (float)(b.minY - camera.y), (float)(b.maxY - camera.y), (float)(b.minZ - camera.z), (float)(b.maxZ - camera.z), (float)(b.minX - camera.x), 0, 0, 0, 30, 60);

            int diff = (int) (b.maxZ - b.minZ) * 25;

            matrices.pushPose();

            matrices.translate(b.minX - camera.x(), b.maxY - camera.y(), b.maxZ - camera.z());
            matrices.mulPose(Axis.XN.rotationDegrees(180));
            // TODO: this must be -Y or +Y based on map. Make it configurable!
            matrices.mulPose(Axis.YN.rotationDegrees(90));
            matrices.scale(0.04f, 0.04f, 0.04f);
            int voff = 1;
            Component header = state.header();
            drawComponent(matrices, buffers, (diff - Minecraft.getInstance().font.width(header)) / 2, voff, header.getStyle().getColor().getValue(), header);

            List<Component> content = state.content();
            for (int i = 0; i < content.size(); i++) {
                Component comp = content.get(i);
                int di = i + 2 >> 1; // di = (i + 2) / 2;
                int mi = i & 1; // mi = i % 2;

                voff = 10 * di + 1;

                int hoff = 1;
                if (mi == 1) {
                    hoff = diff - Minecraft.getInstance().font.width(comp) - 1;
                }

                TextColor col = comp.getStyle().getColor();
                drawComponent(matrices, buffers, hoff, voff, col == null ? 0xFFFFFF : col.getValue(), comp);
            }

            matrices.popPose();
        } else {
            buildNorthFacing(cons, entry, (float)(b.minX - camera.x), (float)(b.maxX - camera.x), (float)(b.minY - camera.y), (float)(b.maxY - camera.y), (float)(b.minZ - camera.z), 0, 0, 0, 30, 60);
        }
    }

    private static void drawComponent(PoseStack matrices, MultiBufferSource buffers, int hoff, int voff, int color, Component comp) {
        Minecraft.getInstance().font.drawInBatch(
                comp,
                hoff,
                voff,
                color,
                false,
                matrices.last().pose(),
                buffers,
                Font.DisplayMode.POLYGON_OFFSET,
                0,
                LightTexture.FULL_BRIGHT
        );
    }

    public static void buildBox(VertexConsumer buffer, PoseStack matrices, float x1, float x2, float y1, float y2, float z1, float z2, int r, int g, int b, int a) {
        PoseStack.Pose entry = matrices.last();

        buildNorthFacing(buffer, entry, x1, x2, y1, y2, z1, r, g, b, a, 0);
        buildNorthFacing(buffer, entry, x1, x2, y1, y2, z2, r, g, b, a, 0);

        buildEastFacing(buffer, entry, y1, y2, z1, z2, x1, r, g, b, a, 0);
        buildEastFacing(buffer, entry, y1, y2, z1, z2, x2, r, g, b, a, 0);
    }

    public static void buildNorthFacing(VertexConsumer buffer, PoseStack.Pose pose, float x1, float x2, float y1, float y2, float z, int r, int g, int b, int a, int a2) {
        buffer.addVertex(pose, x1, y2, z).setColor(r, g, b, a2)
                .setUv(0.0F, 1.0F).setLight(LightTexture.FULL_BRIGHT).setNormal(pose, 0.0F, 1.0F, 0.0F);
        buffer.addVertex(pose, x2, y2, z).setColor(r, g, b, a2)
                .setUv(1.0F, 1.0F).setLight(LightTexture.FULL_BRIGHT).setNormal(pose, 0.0F, 1.0F, 0.0F);
        buffer.addVertex(pose, x2, y1, z).setColor(r, g, b, a)
                .setUv(1.0F, 0.0F).setLight(LightTexture.FULL_BRIGHT).setNormal(pose, 0.0F, 1.0F, 0.0F);
        buffer.addVertex(pose, x1, y1, z).setColor(r, g, b, a)
                .setUv(0.0F, 0.0F).setLight(LightTexture.FULL_BRIGHT).setNormal(pose, 0.0F, 1.0F, 0.0F);

        // Reverse

        buffer.addVertex(pose, x1, y2, z).setColor(r, g, b, a2)
                .setUv(0.0F, 1.0F).setLight(LightTexture.FULL_BRIGHT).setNormal(pose, 0.0F, 1.0F, 0.0F);
        buffer.addVertex(pose, x1, y1, z).setColor(r, g, b, a)
                .setUv(0.0F, 0.0F).setLight(LightTexture.FULL_BRIGHT).setNormal(pose, 0.0F, 1.0F, 0.0F);
        buffer.addVertex(pose, x2, y1, z).setColor(r, g, b, a)
                .setUv(1.0F, 0.0F).setLight(LightTexture.FULL_BRIGHT).setNormal(pose, 0.0F, 1.0F, 0.0F);
        buffer.addVertex(pose, x2, y2, z).setColor(r, g, b, a2)
                .setUv(1.0F, 1.0F).setLight(LightTexture.FULL_BRIGHT).setNormal(pose, 0.0F, 1.0F, 0.0F);
    }

    public static void buildEastFacing(VertexConsumer buffer, PoseStack.Pose pose, float y1, float y2, float z1, float z2, float x, int r, int g, int b, int a, int a2) {
        buffer.addVertex(pose, x, y1, z2).setColor(r, g, b, a)
                .setUv(0.0F, 1.0F).setLight(LightTexture.FULL_BRIGHT).setNormal(pose, 0.0F, 1.0F, 0.0F);
        buffer.addVertex(pose, x, y2, z2).setColor(r, g, b, a2)
                .setUv(1.0F, 1.0F).setLight(LightTexture.FULL_BRIGHT).setNormal(pose, 0.0F, 1.0F, 0.0F);
        buffer.addVertex(pose, x, y2, z1).setColor(r, g, b, a2)
                .setUv(1.0F, 0.0F).setLight(LightTexture.FULL_BRIGHT).setNormal(pose, 0.0F, 1.0F, 0.0F);
        buffer.addVertex(pose, x, y1, z1).setColor(r, g, b, a)
                .setUv(0.0F, 0.0F).setLight(LightTexture.FULL_BRIGHT).setNormal(pose, 0.0F, 1.0F, 0.0F);

        // Reverse

        buffer.addVertex(pose, x, y1, z2).setColor(r, g, b, a)
                .setUv(0.0F, 1.0F).setLight(LightTexture.FULL_BRIGHT).setNormal(pose, 0.0F, 1.0F, 0.0F);
        buffer.addVertex(pose, x, y1, z1).setColor(r, g, b, a)
                .setUv(0.0F, 0.0F).setLight(LightTexture.FULL_BRIGHT).setNormal(pose, 0.0F, 1.0F, 0.0F);
        buffer.addVertex(pose, x, y2, z1).setColor(r, g, b, a2)
                .setUv(1.0F, 0.0F).setLight(LightTexture.FULL_BRIGHT).setNormal(pose, 0.0F, 1.0F, 0.0F);
        buffer.addVertex(pose, x, y2, z2).setColor(r, g, b, a2)
                .setUv(1.0F, 1.0F).setLight(LightTexture.FULL_BRIGHT).setNormal(pose, 0.0F, 1.0F, 0.0F);
    }

    @SubscribeEvent
    public static void onRenderPlayerName(RenderNameTagEvent event) {
        if (event.getEntity() instanceof Player player) {
            PointTagClientState state = ClientGameStateManager.getOrNull(GameClientStateTypes.POINT_TAGS);
            Component points = state != null ? state.getPointsTextFor(player.getUUID()) : null;
            if (points != null) {
                renderPlayerPoints(event, player, state.icon(), points);
            }
        }
    }

    private static void renderPlayerPoints(RenderNameTagEvent event, Entity entity, ItemStack icon, Component points) {
        final Minecraft client = Minecraft.getInstance();
        final EntityRenderDispatcher renderDispatcher = client.getEntityRenderDispatcher();
        double distance2 = renderDispatcher.distanceToSqr(entity);
        if (!ClientHooks.isNameplateInRenderDistance(entity, distance2) || entity.isDiscrete()) {
            return;
        }

        if (entity == client.cameraEntity || !Minecraft.renderNames()) {
            return;
        }

        final float itemSize = 16.0F;
        final float spacing = 4.0f;
        final float textScale = 1.0F / 2.5F;

        PoseStack poseStack = event.getPoseStack();

        poseStack.pushPose();
        poseStack.translate(0.0, entity.getBbHeight() + 0.75, 0.0);
        poseStack.mulPose(renderDispatcher.cameraOrientation());
        poseStack.scale(-0.0625F * textScale, 0.0625F * textScale, 0.0625F * textScale);

        MultiBufferSource buffer = event.getMultiBufferSource();
        int packedLight = event.getPackedLight();

        Font font = event.getEntityRenderer().getFont();
        ItemRenderer items = client.getItemRenderer();

        float width = itemSize + spacing + font.width(points);
        float left = -width / 2.0F;

        poseStack.pushPose();
        poseStack.scale(1.0F, -1.0F, 1.0F);

        float textX = left + itemSize + spacing;
        float textY = -font.lineHeight / 2.0F;
        font.drawInBatch(points, textX, textY, CommonColors.WHITE, false, poseStack.last().pose(), buffer, Font.DisplayMode.NORMAL, 0, packedLight);
        poseStack.popPose();

        poseStack.pushPose();
        poseStack.translate(left + (itemSize / 2.0f), 0.0F, 0.0F);
        poseStack.scale(itemSize, itemSize, -itemSize);
        items.renderStatic(icon, ItemDisplayContext.GUI, packedLight, OverlayTexture.NO_OVERLAY, poseStack, buffer, client.level, 0);
        poseStack.popPose();

        poseStack.popPose();
    }
}
