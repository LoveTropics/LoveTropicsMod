package com.lovetropics.minigames.client.game.handler.spectate;

import com.lovetropics.minigames.common.core.network.LoveTropicsNetwork;
import com.lovetropics.minigames.common.core.network.SpectatePlayerAndTeleportMessage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.settings.PointOfView;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.client.event.EntityViewRenderEvent;

import java.util.UUID;
import java.util.function.BooleanSupplier;

interface SpectatingState {
	FreeCamera FREE_CAMERA = new FreeCamera();

	StateApplicator apply(Minecraft client, SpectatingSession session);

	default SpectatingState tick(Minecraft client, SpectatingSession session, ClientPlayerEntity player) {
		return this;
	}

	default void renderTick(Minecraft client, SpectatingSession session, ClientPlayerEntity player) {
	}

	default void applyToCamera(Minecraft client, SpectatingSession session, ClientPlayerEntity player, ActiveRenderInfo camera, float partialTicks, EntityViewRenderEvent.CameraSetup event) {
	}

	class FreeCamera implements SpectatingState {
		FreeCamera() {
		}

		@Override
		public StateApplicator apply(Minecraft client, SpectatingSession session) {
			client.options.smoothCamera = false;
			client.options.setCameraType(PointOfView.FIRST_PERSON);

			return new StateApplicator(
					() -> LoveTropicsNetwork.CHANNEL.sendToServer(new SpectatePlayerAndTeleportMessage(client.player.getUUID())),
					() -> client.getCameraEntity() == client.player
			);
		}

		@Override
		public SpectatingState tick(Minecraft client, SpectatingSession session, ClientPlayerEntity player) {
			// force player to maximum flying speed
			player.abilities.setFlyingSpeed(0.2F);

			if (client.getCameraEntity() != player) {
				return new SelectedPlayer(client.getCameraEntity().getUUID());
			}

			return this;
		}
	}

	class SelectedPlayer implements SpectatingState {
		final UUID spectatedId;

		public SelectedPlayer(UUID spectatedId) {
			this.spectatedId = spectatedId;
		}

		@Override
		public StateApplicator apply(Minecraft client, SpectatingSession session) {
			return new StateApplicator(
					() -> LoveTropicsNetwork.CHANNEL.sendToServer(new SpectatePlayerAndTeleportMessage(spectatedId)),
					() -> spectatedId.equals(client.getCameraEntity().getUUID())
			);
		}

		@Override
		public SpectatingState tick(Minecraft client, SpectatingSession session, ClientPlayerEntity player) {
			if (player.level.getGameTime() % 10 == 0) {
				// we need to send position updates to the server or we won't properly track chunks
				PlayerEntity spectatedPlayer = player.level.getPlayerByUUID(spectatedId);
				if (spectatedPlayer != null) {
					player.setPos(spectatedPlayer.getX(), spectatedPlayer.getY(), spectatedPlayer.getZ());
				}
			}

			if (!spectatedId.equals(client.getCameraEntity().getUUID())) {
				return SpectatingState.FREE_CAMERA;
			}

			return this;
		}

		@Override
		public void renderTick(Minecraft client, SpectatingSession session, ClientPlayerEntity player) {
			Entity focusEntity = client.getCameraEntity();
			focusEntity = focusEntity != null ? focusEntity : player;

			boolean firstPerson = session.zoom <= 1e-2;

			if (!firstPerson) {
				client.options.smoothCamera = true;
				client.options.setCameraType(PointOfView.THIRD_PERSON_BACK);
			} else {
				client.options.smoothCamera = false;
				client.options.setCameraType(PointOfView.FIRST_PERSON);

				player.yRot = focusEntity.yRot;
				player.xRot = focusEntity.xRot;
			}
		}

		@Override
		public void applyToCamera(Minecraft client, SpectatingSession session, ClientPlayerEntity player, ActiveRenderInfo camera, float partialTicks, EntityViewRenderEvent.CameraSetup event) {
			Entity focusEntity = client.getCameraEntity();
			focusEntity = focusEntity != null ? focusEntity : player;

			double zoom = session.getZoom(partialTicks);
			boolean firstPerson = zoom <= 1e-2;

			if (!firstPerson) {
				float yaw = player.getViewYRot(partialTicks);
				float pitch = player.getViewXRot(partialTicks);

				event.setYaw(yaw);
				event.setPitch(pitch);
				camera.setRotation(yaw, pitch);

				camera.setPosition(
						MathHelper.lerp(partialTicks, focusEntity.xo, focusEntity.getX()),
						MathHelper.lerp(partialTicks, focusEntity.yo, focusEntity.getY()) + focusEntity.getEyeHeight(),
						MathHelper.lerp(partialTicks, focusEntity.zo, focusEntity.getZ())
				);

				double distance = zoom * ClientSpectatingManager.MAX_CHASE_DISTANCE;
				camera.move(-camera.getMaxZoom(distance), 0.0, 0.0);
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
			long time = client.level.getGameTime();
			if (time - lastApplyTime >= APPLY_INTERVAL) {
				apply.run();
				lastApplyTime = time;
			}

			return condition.getAsBoolean();
		}
	}
}
