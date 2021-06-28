package com.lovetropics.minigames.client;

import com.lovetropics.minigames.Constants;
import com.lovetropics.minigames.common.core.diguise.PlayerDisguise;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Constants.MODID, value = Dist.CLIENT)
public final class ClientPlayerDisguises {
	private static final Minecraft CLIENT = Minecraft.getInstance();

	@SubscribeEvent
	public static void onRenderPlayer(RenderPlayerEvent.Pre event) {
		PlayerEntity player = event.getPlayer();
		Entity disguise = PlayerDisguise.getDisguiseEntity(player);

		if (disguise != null) {
			EntityRenderer<? super Entity> renderer = CLIENT.getRenderManager().getRenderer(disguise);
			if (renderer != null) {
				copyDisguiseState(disguise, player);

				float partialTicks = event.getPartialRenderTick();
				MatrixStack transform = event.getMatrixStack();
				IRenderTypeBuffer buffers = event.getBuffers();
				int packedLight = event.getLight();

				float yaw = MathHelper.lerp(partialTicks, player.prevRotationYaw, player.rotationYaw);
				renderer.render(disguise, yaw, partialTicks, transform, buffers, packedLight);

				event.setCanceled(true);
			}
		}
	}

	private static void copyDisguiseState(Entity disguise, PlayerEntity player) {
		disguise.setPosition(player.getPosX(), player.getPosY(), player.getPosZ());
		disguise.prevPosX = player.prevPosX;
		disguise.prevPosY = player.prevPosY;
		disguise.prevPosZ = player.prevPosZ;

		disguise.rotationYaw = player.rotationYaw;
		disguise.prevRotationYaw = player.prevRotationYaw;
		disguise.rotationPitch = player.rotationPitch;
		disguise.prevRotationPitch = player.prevRotationPitch;

		disguise.setSneaking(player.isSneaking());
		disguise.setPose(player.getPose());
		disguise.setInvisible(player.isInvisible());
		disguise.setSprinting(player.isSprinting());
		disguise.setSwimming(player.isSwimming());

		disguise.setCustomName(player.getCustomName());
		disguise.setCustomNameVisible(player.isCustomNameVisible());

		if (disguise instanceof LivingEntity) {
			LivingEntity livingDisguise = (LivingEntity) disguise;
			livingDisguise.renderYawOffset = player.renderYawOffset;
			livingDisguise.prevRenderYawOffset = player.prevRenderYawOffset;

			livingDisguise.rotationYawHead = player.rotationYawHead;
			livingDisguise.prevRotationYawHead = player.prevRotationYawHead;

			livingDisguise.limbSwing = player.limbSwing;
			livingDisguise.limbSwingAmount = player.limbSwingAmount;
			livingDisguise.prevLimbSwingAmount = player.prevLimbSwingAmount;

			livingDisguise.swingingHand = player.swingingHand;
			livingDisguise.swingProgress = player.swingProgress;
			livingDisguise.swingProgressInt = player.swingProgressInt;
			livingDisguise.prevSwingProgress = player.prevSwingProgress;
			livingDisguise.isSwingInProgress = player.isSwingInProgress;

			livingDisguise.setOnGround(player.isOnGround());
		}

		disguise.ticksExisted = player.ticksExisted;
	}
}
