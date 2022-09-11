package com.lovetropics.minigames.client;

import com.lovetropics.minigames.Constants;
import com.lovetropics.minigames.LoveTropics;
import com.lovetropics.minigames.client.game.handler.spectate.ClientSpectatingManager;
import com.lovetropics.minigames.common.core.diguise.DisguiseType;
import com.lovetropics.minigames.common.core.diguise.PlayerDisguise;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityViewRenderEvent;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.UUID;

@Mod.EventBusSubscriber(modid = Constants.MODID, value = Dist.CLIENT)
public final class ClientPlayerDisguises {
    private static final Minecraft CLIENT = Minecraft.getInstance();

    @SubscribeEvent
    public static void onRenderPlayer(RenderPlayerEvent.Pre event) {
        Player player = event.getPlayer();
        DisguiseType disguiseType = PlayerDisguise.getDisguiseType(event.getPlayer());
        Entity disguiseEntity = PlayerDisguise.getDisguiseEntity(player);
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
                if (disguiseType.scale != 1.0f) {
                    transform.scale(disguiseType.scale, disguiseType.scale, disguiseType.scale);
                }

                float yaw = Mth.lerp(partialTicks, player.yRotO, player.getYRot());
                renderer.render(disguiseEntity, yaw, partialTicks, transform, buffers, packedLight);

                transform.popPose();
            } catch (Exception e) {
                PlayerDisguise.get(player).ifPresent(PlayerDisguise::clearDisguise);
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

            livingDisguise.animationPosition = player.animationPosition;
            livingDisguise.animationSpeed = player.animationSpeed;
            livingDisguise.animationSpeedOld = player.animationSpeedOld;

            livingDisguise.swingingArm = player.swingingArm;
            livingDisguise.attackAnim = player.attackAnim;
            livingDisguise.swingTime = player.swingTime;
            livingDisguise.oAttackAnim = player.oAttackAnim;
            livingDisguise.swinging = player.swinging;

            livingDisguise.setOnGround(player.isOnGround());
        }

        disguise.tickCount = player.tickCount;
    }

    public static void updateClientDisguise(UUID uuid, DisguiseType disguiseType) {
        Player player = Minecraft.getInstance().level.getPlayerByUUID(uuid);
        if (player != null) {
            PlayerDisguise.get(player).ifPresent(playerDisguise -> playerDisguise.setDisguise(disguiseType));
        }
    }

    @SubscribeEvent
    public static void onPositionCamera(EntityViewRenderEvent.CameraSetup event) {
        Camera camera = event.getCamera();
        if (!camera.isDetached()) {
            return;
        }

        if (Minecraft.getInstance().cameraEntity instanceof Player player) {
            DisguiseType disguiseType = PlayerDisguise.getDisguiseType(player);
            if (disguiseType == null || disguiseType.scale == 1.0f) {
                return;
            }

            Vec3 eyePosition = player.getEyePosition((float) event.getPartialTicks());
            camera.setPosition(eyePosition.x, eyePosition.y, eyePosition.z);
            camera.move(-camera.getMaxZoom(4.0 * disguiseType.scale), 0.0, 0.0);
        }
    }
}
