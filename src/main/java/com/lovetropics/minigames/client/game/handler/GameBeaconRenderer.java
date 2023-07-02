package com.lovetropics.minigames.client.game.handler;

import com.lovetropics.minigames.Constants;
import com.lovetropics.minigames.client.game.ClientGameStateManager;
import com.lovetropics.minigames.common.core.game.client_state.GameClientStateTypes;
import com.lovetropics.minigames.common.core.game.client_state.instance.BeaconClientState;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BeaconRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.List;

@Mod.EventBusSubscriber(modid = Constants.MODID, value = Dist.CLIENT)
public final class GameBeaconRenderer {
	private static final Minecraft CLIENT = Minecraft.getInstance();
	private static final float[] COLOR = DyeColor.WHITE.getTextureDiffuseColors();

	@SubscribeEvent
	public static void onRenderLevel(RenderLevelStageEvent event) {
		if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_LEVEL) {
			return;
		}

		BeaconClientState state = ClientGameStateManager.getOrNull(GameClientStateTypes.BEACON);
		if (state == null || state.positions().isEmpty()) {
			return;
		}

		List<BlockPos> positions = state.positions();

		ClientLevel level = CLIENT.level;
		Camera camera = CLIENT.gameRenderer.getMainCamera();
		if (level == null || !camera.isInitialized()) {
			return;
		}

		MultiBufferSource.BufferSource bufferSource = MultiBufferSource.immediate(Tesselator.getInstance().getBuilder());

		Vec3 cameraPosition = camera.getPosition();
		PoseStack poseStack = event.getPoseStack();

		long gameTime = level.getGameTime();
		float partialTick = event.getPartialTick();

		for (BlockPos position : positions) {
			poseStack.pushPose();
			poseStack.translate(position.getX() - cameraPosition.x, position.getY() - cameraPosition.y, position.getZ() - cameraPosition.z);
			BeaconRenderer.renderBeaconBeam(poseStack, bufferSource, BeaconRenderer.BEAM_LOCATION, partialTick, 0.0f, gameTime, 0, 256, COLOR, 0.15F, 0.175F);
			poseStack.popPose();
		}

		bufferSource.endBatch();
	}
}
