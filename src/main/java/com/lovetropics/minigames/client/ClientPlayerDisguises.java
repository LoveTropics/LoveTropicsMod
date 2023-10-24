package com.lovetropics.minigames.client;

import com.lovetropics.minigames.Constants;
import com.lovetropics.minigames.LoveTropics;
import com.lovetropics.minigames.common.core.diguise.DisguiseType;
import com.lovetropics.minigames.common.core.diguise.PlayerDisguise;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.WalkAnimationState;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.scores.Team;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.client.event.ViewportEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.UUID;

@Mod.EventBusSubscriber(modid = Constants.MODID, value = Dist.CLIENT)
public final class ClientPlayerDisguises {
    private static final Minecraft CLIENT = Minecraft.getInstance();

    @SubscribeEvent
    public static void onRenderPlayerPre(RenderPlayerEvent.Pre event) {
        Player player = event.getEntity();
        PlayerDisguise disguise = PlayerDisguise.getOrNull(player);
        if (disguise == null || !disguise.isDisguised()) {
            return;
        }

        DisguiseType disguiseType = disguise.type();
        Entity disguiseEntity = disguise.entity();
        EntityRenderDispatcher dispatcher = CLIENT.getEntityRenderDispatcher();
        PoseStack poseStack = event.getPoseStack();

        if (disguiseEntity != null) {
            int capturedTransformState = PoseStackCapture.get(poseStack);

            try {
                copyDisguiseState(disguiseEntity, player);
                disguiseEntity.setCustomNameVisible(shouldShowName(dispatcher, player));

                float partialTicks = event.getPartialTick();
                MultiBufferSource buffers = event.getMultiBufferSource();
                int packedLight = event.getPackedLight();

                poseStack.pushPose();
                if (disguiseType.scale() != 1.0f) {
                    poseStack.scale(disguiseType.scale(), disguiseType.scale(), disguiseType.scale());
                }

                float yaw = Mth.lerp(partialTicks, player.yRotO, player.getYRot());
                EntityRenderer<? super Entity> renderer = dispatcher.getRenderer(disguiseEntity);
                if (renderer != null) {
                    renderer.render(disguiseEntity, yaw, partialTicks, poseStack, buffers, packedLight);
                }

                poseStack.popPose();
            } catch (Exception e) {
                disguise.clear();
                LoveTropics.LOGGER.error("Failed to render player disguise", e);
                PoseStackCapture.restore(poseStack, capturedTransformState);
            }

            event.setCanceled(true);
        } else {
            poseStack.pushPose();
            if (disguiseType.scale() != 1.0f) {
                poseStack.scale(disguiseType.scale(), disguiseType.scale(), disguiseType.scale());
            }
        }
    }

    @SubscribeEvent
    public static void onRenderPlayerPost(RenderPlayerEvent.Post event) {
        Player player = event.getEntity();
        PlayerDisguise disguise = PlayerDisguise.getOrNull(player);
        if (disguise == null || !disguise.isDisguised()) {
            return;
        }

        if (disguise.type().entity() == null) {
            event.getPoseStack().popPose();
        }
    }

    private static void copyDisguiseState(Entity disguise, Player player) {
        disguise.setPos(player.getX(), player.getY(), player.getZ());
        disguise.xo = player.xo;
        disguise.yo = player.yo;
        disguise.zo = player.zo;

        disguise.setYRot(player.getYRot());
        disguise.yRotO = player.yRotO;
        disguise.setXRot(player.getXRot());
        disguise.xRotO = player.xRotO;

        disguise.setShiftKeyDown(player.isShiftKeyDown());
        disguise.setPose(player.getPose());
        disguise.setInvisible(player.isInvisible());
        disguise.setSprinting(player.isSprinting());
        disguise.setSwimming(player.isSwimming());

        disguise.setCustomName(player.getDisplayName());

        if (disguise instanceof LivingEntity livingDisguise) {
            livingDisguise.yBodyRot = player.yBodyRot;
            livingDisguise.yBodyRotO = player.yBodyRotO;

            livingDisguise.yHeadRot = player.yHeadRot;
            livingDisguise.yHeadRotO = player.yHeadRotO;

            copyWalkAnimation(player.walkAnimation, livingDisguise.walkAnimation);

            livingDisguise.swingingArm = player.swingingArm;
            livingDisguise.attackAnim = player.attackAnim;
            livingDisguise.swingTime = player.swingTime;
            livingDisguise.oAttackAnim = player.oAttackAnim;
            livingDisguise.swinging = player.swinging;

            livingDisguise.setOnGround(player.onGround());
        }

        disguise.tickCount = player.tickCount;
    }

    // TODO: Shameless code duplication
    private static boolean shouldShowName(EntityRenderDispatcher entityRenderDispatcher, Player player) {
        double distanceSq = entityRenderDispatcher.distanceToSqr(player);
        float maximumDistance = player.isDiscrete() ? 32.0f : 64.0f;
        if (distanceSq >= maximumDistance * maximumDistance) {
            return false;
        }

        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer localPlayer = minecraft.player;
        boolean visible = !player.isInvisibleTo(localPlayer);
        if (player != localPlayer) {
            Team playerTeam = player.getTeam();
            Team localPlayerTeam = localPlayer.getTeam();
            if (playerTeam != null) {
                return switch (playerTeam.getNameTagVisibility()) {
                    case ALWAYS -> visible;
                    case NEVER -> false;
                    case HIDE_FOR_OTHER_TEAMS ->
                            localPlayerTeam == null ? visible : playerTeam.isAlliedTo(localPlayerTeam) && (playerTeam.canSeeFriendlyInvisibles() || visible);
                    case HIDE_FOR_OWN_TEAM ->
                            localPlayerTeam == null ? visible : !playerTeam.isAlliedTo(localPlayerTeam) && visible;
                };
            }
        }

        return Minecraft.renderNames() && player != minecraft.getCameraEntity() && visible && !player.isVehicle();
    }

    private static void copyWalkAnimation(WalkAnimationState from, WalkAnimationState to) {
        to.update(from.position() - to.position() - from.speed(), 1.0f);
        to.setSpeed(from.speed(0.0f));
        to.update(from.speed(), 1.0f);
    }

    public static void updateClientDisguise(UUID uuid, DisguiseType disguiseType) {
        Player player = Minecraft.getInstance().level.getPlayerByUUID(uuid);
        if (player != null) {
            PlayerDisguise disguise = PlayerDisguise.getOrNull(player);
            if (disguise != null) {
                disguise.set(disguiseType);
            }
        }
    }

    @SubscribeEvent
    public static void onPositionCamera(ViewportEvent.ComputeCameraAngles event) {
        Camera camera = event.getCamera();
        if (!camera.isDetached()) {
            return;
        }

        if (camera.getEntity() instanceof Player player) {
            PlayerDisguise disguise = PlayerDisguise.getOrNull(player);
            if (disguise == null) {
                return;
            }

            float scale = Math.max(disguise.getEffectiveScale(), 0.5f);
            if (scale == 1.0f) {
                return;
            }

            Vec3 eyePosition = player.getEyePosition((float) event.getPartialTick());
            camera.setPosition(eyePosition.x, eyePosition.y, eyePosition.z);
            camera.move(-camera.getMaxZoom(4.0 * scale), 0.0, 0.0);
        }
    }
}
