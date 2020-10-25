package com.lovetropics.minigames.client.chase;

import com.lovetropics.minigames.common.network.ChaseSpectatePlayerMessage;
import com.lovetropics.minigames.common.network.LTNetwork;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.client.event.EntityViewRenderEvent;

import java.util.UUID;
import java.util.function.BooleanSupplier;

public interface ChaseCameraState {
	FreeCamera FREE_CAMERA = new FreeCamera();

	StateApplicator apply(Minecraft client, ChaseCameraSession session);

	default ChaseCameraState tick(Minecraft client, ChaseCameraSession session, ClientPlayerEntity player) {
		return this;
	}

	default void renderTick(Minecraft client, ChaseCameraSession session, ClientPlayerEntity player) {
	}

	default void applyToCamera(Minecraft client, ChaseCameraSession session, ClientPlayerEntity player, ActiveRenderInfo camera, float partialTicks, EntityViewRenderEvent.CameraSetup event) {
	}

	class FreeCamera implements ChaseCameraState {
		FreeCamera() {
		}

		@Override
		public StateApplicator apply(Minecraft client, ChaseCameraSession session) {
			return new StateApplicator(
					() -> LTNetwork.CHANNEL.sendToServer(new ChaseSpectatePlayerMessage(client.player.getUniqueID())),
					() -> client.getRenderViewEntity() == client.player
			);
		}

		@Override
		public ChaseCameraState tick(Minecraft client, ChaseCameraSession session, ClientPlayerEntity player) {
			// force player to maximum flying speed
			player.abilities.setFlySpeed(0.2F);

			if (client.getRenderViewEntity() != player) {
				return new SelectedPlayer(client.getRenderViewEntity().getUniqueID());
			}

			return this;
		}

		@Override
		public void renderTick(Minecraft client, ChaseCameraSession session, ClientPlayerEntity player) {
			client.gameSettings.smoothCamera = false;
			client.gameSettings.thirdPersonView = 0;
		}
	}

	class SelectedPlayer implements ChaseCameraState {
		final UUID spectatedId;

		public SelectedPlayer(UUID spectatedId) {
			this.spectatedId = spectatedId;
		}

		@Override
		public StateApplicator apply(Minecraft client, ChaseCameraSession session) {
			return new StateApplicator(
					() -> LTNetwork.CHANNEL.sendToServer(new ChaseSpectatePlayerMessage(spectatedId)),
					() -> spectatedId.equals(client.getRenderViewEntity().getUniqueID())
			);
		}

		@Override
		public ChaseCameraState tick(Minecraft client, ChaseCameraSession session, ClientPlayerEntity player) {
			if (player.world.getGameTime() % 10 == 0) {
				// we need to send position updates to the server or we won't properly track chunks
				PlayerEntity spectatedPlayer = player.world.getPlayerByUuid(spectatedId);
				if (spectatedPlayer != null) {
					player.setPosition(spectatedPlayer.getPosX(), spectatedPlayer.getPosY(), spectatedPlayer.getPosZ());
				}
			}

			if (!spectatedId.equals(client.getRenderViewEntity().getUniqueID())) {
				return ChaseCameraState.FREE_CAMERA;
			}

			return this;
		}

		@Override
		public void renderTick(Minecraft client, ChaseCameraSession session, ClientPlayerEntity player) {
			Entity focusEntity = client.getRenderViewEntity();
			focusEntity = focusEntity != null ? focusEntity : player;

			boolean firstPerson = session.zoom <= 1e-2;

			if (!firstPerson) {
				client.gameSettings.smoothCamera = true;
				client.gameSettings.thirdPersonView = 2;
			} else {
				client.gameSettings.smoothCamera = false;
				client.gameSettings.thirdPersonView = 0;

				player.rotationYaw = focusEntity.rotationYaw;
				player.rotationPitch = focusEntity.rotationPitch;
			}
		}

		@Override
		public void applyToCamera(Minecraft client, ChaseCameraSession session, ClientPlayerEntity player, ActiveRenderInfo camera, float partialTicks, EntityViewRenderEvent.CameraSetup event) {
			Entity focusEntity = client.getRenderViewEntity();
			focusEntity = focusEntity != null ? focusEntity : player;

			double zoom = session.getZoom(partialTicks);
			boolean firstPerson = zoom <= 1e-2;

			if (!firstPerson) {
				float yaw = player.getYaw(partialTicks);
				float pitch = player.getPitch(partialTicks);

				event.setYaw(yaw);
				event.setPitch(pitch);
				camera.setDirection(yaw, pitch);

				camera.setPosition(
						MathHelper.lerp(partialTicks, focusEntity.prevPosX, focusEntity.getPosX()),
						MathHelper.lerp(partialTicks, focusEntity.prevPosY, focusEntity.getPosY()) + focusEntity.getEyeHeight(),
						MathHelper.lerp(partialTicks, focusEntity.prevPosZ, focusEntity.getPosZ())
				);

				double distance = zoom * ChaseCameraManager.MAX_CHASE_DISTANCE;
				camera.movePosition(-camera.calcCameraDistance(distance), 0.0, 0.0);
			}
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) return true;

			if (obj instanceof SelectedPlayer) {
				SelectedPlayer state = (SelectedPlayer) obj;
				return spectatedId.equals(state.spectatedId);
			}

			return false;
		}
	}

	final class StateApplicator {
		private static final long APPLY_INTERVAL = 20;

		final Runnable apply;
		final BooleanSupplier condition;

		long lastApplyTime;

		StateApplicator(Runnable apply, BooleanSupplier condition) {
			this.apply = apply;
			this.condition = condition;
		}

		boolean tryApply(Minecraft client) {
			long time = client.world.getGameTime();
			if (time - lastApplyTime >= APPLY_INTERVAL) {
				apply.run();
				lastApplyTime = time;
			}

			return condition.getAsBoolean();
		}
	}
}
