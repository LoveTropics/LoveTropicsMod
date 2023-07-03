package com.lovetropics.minigames.client;

import com.lovetropics.minigames.Constants;
import com.lovetropics.minigames.LoveTropics;
import com.lovetropics.minigames.common.core.diguise.DisguiseType;
import com.lovetropics.minigames.common.core.diguise.PlayerDisguise;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.WalkAnimationState;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
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
    public static void onRenderPlayer(RenderPlayerEvent.Pre event) {
        Player player = event.getEntity();
        PlayerDisguise disguise = PlayerDisguise.get(event.getEntity());
        DisguiseType disguiseType = disguise.type();
        Entity disguiseEntity = disguise.entity();
        if (disguiseType == null || disguiseEntity == null) return;

        EntityRenderer<? super Entity> renderer = CLIENT.getEntityRenderDispatcher().getRenderer(disguiseEntity);
        if (renderer != null) {
            PoseStack transform = event.getPoseStack();
            int capturedTransformState = PoseStackCapture.get(transform);

            try {
                copyDisguiseState(disguiseEntity, player);

                float partialTicks = event.getPartialTick();
                MultiBufferSource buffers = event.getMultiBufferSource();
                int packedLight = event.getPackedLight();

                transform.pushPose();
                if (disguiseType.scale() != 1.0f) {
                    transform.scale(disguiseType.scale(), disguiseType.scale(), disguiseType.scale());
                }

                float yaw = Mth.lerp(partialTicks, player.yRotO, player.getYRot());
                renderer.render(disguiseEntity, yaw, partialTicks, transform, buffers, packedLight);

                transform.popPose();
            } catch (Exception e) {
                PlayerDisguise.get(player).clear();
                LoveTropics.LOGGER.error("Failed to render player disguise", e);
                PoseStackCapture.restore(transform, capturedTransformState);
            }

            event.setCanceled(true);
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

    private static void copyWalkAnimation(WalkAnimationState from, WalkAnimationState to) {
        to.update(from.position() - to.position() - from.speed(), 1.0f);
        to.setSpeed(from.speed(0.0f));
        to.update(from.speed(), 1.0f);
    }

    public static void updateClientDisguise(UUID uuid, DisguiseType disguiseType) {
        Player player = Minecraft.getInstance().level.getPlayerByUUID(uuid);
        if (player != null) {
            PlayerDisguise.get(player).set(disguiseType);
        }
    }

    @SubscribeEvent
    public static void onPositionCamera(ViewportEvent.ComputeCameraAngles event) {
        Camera camera = event.getCamera();
        if (!camera.isDetached()) {
            return;
        }

        if (Minecraft.getInstance().cameraEntity instanceof Player player) {
            PlayerDisguise disguise = PlayerDisguise.get(player);
            if (disguise.type() == null || disguise.type().scale() == 1.0f) {
                return;
            }

            Vec3 eyePosition = player.getEyePosition((float) event.getPartialTick());
            camera.setPosition(eyePosition.x, eyePosition.y, eyePosition.z);
            camera.move(-camera.getMaxZoom(4.0 * disguise.type().scale()), 0.0, 0.0);
        }
    }
}
