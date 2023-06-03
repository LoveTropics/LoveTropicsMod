package com.lovetropics.minigames.client.game.handler.spectate;

import com.lovetropics.minigames.common.core.network.LoveTropicsNetwork;
import com.lovetropics.minigames.common.core.network.SpectatePlayerAndTeleportMessage;
import net.minecraft.SharedConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.Camera;
import net.minecraft.client.CameraType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.util.Mth;
import net.minecraftforge.client.event.ViewportEvent;

import java.util.UUID;
import java.util.function.BooleanSupplier;

interface SpectatingState {
	FreeCamera FREE_CAMERA = new FreeCamera();

	StateApplicator apply(Minecraft client, SpectatingSession session);

	default SpectatingState tick(Minecraft client, SpectatingSession session, LocalPlayer player) {
		return this;
	}

	default void renderTick(Minecraft client, SpectatingSession session, LocalPlayer player) {
	}

	default void applyToCamera(Minecraft client, SpectatingSession session, LocalPlayer player, Camera camera, float partialTicks, ViewportEvent.ComputeCameraAngles event) {
	}

	boolean allowsZoom();

	class FreeCamera implements SpectatingState {
		FreeCamera() {
		}

		@Override
		public StateApplicator apply(Minecraft client, SpectatingSession session) {
			client.options.smoothCamera = false;
			client.options.setCameraType(CameraType.FIRST_PERSON);

			return new StateApplicator(
					() -> LoveTropicsNetwork.CHANNEL.sendToServer(new SpectatePlayerAndTeleportMessage(client.player.getUUID())),
					() -> client.getCameraEntity() == client.player
			);
		}

		@Override
		public SpectatingState tick(Minecraft client, SpectatingSession session, LocalPlayer player) {
			// force player to maximum flying speed
			player.getAbilities().setFlyingSpeed(0.2F);

			if (client.getCameraEntity() != player) {
				return new SelectedPlayer(client.getCameraEntity().getUUID());
			}

			return this;
		}

		@Override
		public boolean allowsZoom() {
			return false;
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
		public SpectatingState tick(Minecraft client, SpectatingSession session, LocalPlayer player) {
			if (player.level.getGameTime() % 10 == 0) {
				// we need to send position updates to the server or we won't properly track chunks
				Player spectatedPlayer = player.level.getPlayerByUUID(spectatedId);
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
		public void renderTick(Minecraft client, SpectatingSession session, LocalPlayer player) {
			Entity focusEntity = client.getCameraEntity();
			focusEntity = focusEntity != null ? focusEntity : player;

			boolean firstPerson = session.zoom <= 1e-2;

			if (!firstPerson) {
				client.options.smoothCamera = true;
				client.options.setCameraType(CameraType.THIRD_PERSON_BACK);
			} else {
				client.options.smoothCamera = false;
				client.options.setCameraType(CameraType.FIRST_PERSON);

				player.setYRot(focusEntity.getYRot());
				player.setXRot(focusEntity.getXRot());
			}
		}

		@Override
		public void applyToCamera(Minecraft client, SpectatingSession session, LocalPlayer player, Camera camera, float partialTicks, ViewportEvent.ComputeCameraAngles event) {
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
						Mth.lerp(partialTicks, focusEntity.xo, focusEntity.getX()),
						Mth.lerp(partialTicks, focusEntity.yo, focusEntity.getY()) + focusEntity.getEyeHeight(),
						Mth.lerp(partialTicks, focusEntity.zo, focusEntity.getZ())
				);

				double distance = zoom * ClientSpectatingManager.MAX_CHASE_DISTANCE;
				camera.move(-camera.getMaxZoom(distance), 0.0, 0.0);
			}
		}

		@Override
		public boolean allowsZoom() {
			return true;
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
		private static final long APPLY_INTERVAL_TICKS = SharedConstants.TICKS_PER_SECOND;

		final Runnable apply;
		final BooleanSupplier condition;

		long lastApplyTime;

		StateApplicator(Runnable apply, BooleanSupplier condition) {
			this.apply = apply;
			this.condition = condition;
		}

		boolean tryApply(Minecraft client) {
			long time = client.level.getGameTime();
			if (time - lastApplyTime >= APPLY_INTERVAL_TICKS) {
				apply.run();
				lastApplyTime = time;
			}

			return condition.getAsBoolean();
		}

		boolean isApplied() {
			return condition.getAsBoolean();
		}
	}
}
