package com.lovetropics.minigames.client;

import com.lovetropics.minigames.Constants;
import com.lovetropics.minigames.LoveTropics;
import com.lovetropics.minigames.common.core.diguise.DisguiseType;
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

import java.util.UUID;

@Mod.EventBusSubscriber(modid = Constants.MODID, value = Dist.CLIENT)
public final class ClientPlayerDisguises {
	private static final Minecraft CLIENT = Minecraft.getInstance();

	@SubscribeEvent
	public static void onRenderPlayer(RenderPlayerEvent.Pre event) {
		PlayerEntity player = event.getPlayer();
		Entity disguise = PlayerDisguise.getDisguiseEntity(player);
		if (disguise == null) return;

		EntityRenderer<? super Entity> renderer = CLIENT.getEntityRenderDispatcher().getRenderer(disguise);
		if (renderer != null) {
			try {
				copyDisguiseState(disguise, player);

				float partialTicks = event.getPartialRenderTick();
				MatrixStack transform = event.getMatrixStack();
				IRenderTypeBuffer buffers = event.getBuffers();
				int packedLight = event.getLight();

				float yaw = MathHelper.lerp(partialTicks, player.yRotO, player.yRot);
				renderer.render(disguise, yaw, partialTicks, transform, buffers, packedLight);
			} catch (Exception e) {
				PlayerDisguise.get(player).ifPresent(PlayerDisguise::clearDisguise);
				LoveTropics.LOGGER.error("Failed to render player disguise", e);
			}

			event.setCanceled(true);
		}
	}

	private static void copyDisguiseState(Entity disguise, PlayerEntity player) {
		disguise.setPos(player.getX(), player.getY(), player.getZ());
		disguise.xo = player.xo;
		disguise.yo = player.yo;
		disguise.zo = player.zo;

		disguise.yRot = player.yRot;
		disguise.yRotO = player.yRotO;
		disguise.xRot = player.xRot;
		disguise.xRotO = player.xRotO;

		disguise.setShiftKeyDown(player.isShiftKeyDown());
		disguise.setPose(player.getPose());
		disguise.setInvisible(player.isInvisible());
		disguise.setSprinting(player.isSprinting());
		disguise.setSwimming(player.isSwimming());

		if (disguise instanceof LivingEntity) {
			LivingEntity livingDisguise = (LivingEntity) disguise;
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
		PlayerEntity player = Minecraft.getInstance().level.getPlayerByUUID(uuid);
		if (player != null) {
			PlayerDisguise.get(player).ifPresent(playerDisguise -> playerDisguise.setDisguise(disguiseType));
		}
	}
}
