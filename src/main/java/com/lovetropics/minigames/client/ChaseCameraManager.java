package com.lovetropics.minigames.client;

import com.lovetropics.minigames.Constants;
import com.lovetropics.minigames.common.network.ChaseSpectatePlayerMessage;
import com.lovetropics.minigames.common.network.LTNetwork;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MainWindow;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.client.resources.SkinManager;
import net.minecraft.client.util.InputMappings;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityViewRenderEvent;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Mod.EventBusSubscriber(modid = Constants.MODID, value = Dist.CLIENT)
public final class ChaseCameraManager {
	private static final double MAX_CHASE_DISTANCE = 16.0;

	private static final int ENTRY_PADDING = 2;
	private static final int ENTRY_SIZE = 12 + ENTRY_PADDING;

	private static final Minecraft CLIENT = Minecraft.getInstance();

	private static final int ZOOM_KEY = InputMappings.getInputByName("key.keyboard.left.control").getKeyCode();

	private static Session session;
	private static double accumulatedScroll;

	public static void update(List<UUID> players) {
		Session session = ChaseCameraManager.session;
		if (session == null) {
			ChaseCameraManager.session = new Session(players);
		} else {
			session.update(players);
		}
	}

	public static void stop() {
		Session session = ChaseCameraManager.session;
		ChaseCameraManager.session = null;

		if (session != null) {
			session.stopSpectating();
			CLIENT.gameSettings.thirdPersonView = 0;
			CLIENT.gameSettings.smoothCamera = false;
		}
	}

	@SubscribeEvent
	public static void onWorldLoad(WorldEvent.Load event) {
		if (event.getWorld().isRemote()) {
			session = null;
		}
	}

	@SubscribeEvent
	public static void onClientTick(TickEvent.ClientTickEvent event) {
		Session session = ChaseCameraManager.session;
		if (session == null || event.phase == TickEvent.Phase.START) {
			return;
		}

		ClientWorld world = CLIENT.world;
		if (world == null) {
			return;
		}

		// reset to free camera if the player is spectating themselves
		if (CLIENT.getRenderViewEntity() == CLIENT.player) {
			session.selectedPlayerIndex = -1;
		}

		session.prevZoom = session.zoom;

		if (session.isSpectating()) {
			if (world.getGameTime() % 10 == 0) {
				// we need to send position updates to the server or we won't properly track chunks
				UUID spectatedId = session.players.get(session.selectedPlayerIndex);
				PlayerEntity spectatedPlayer = world.getPlayerByUuid(spectatedId);

				if (spectatedPlayer != null) {
					ClientPlayerEntity player = CLIENT.player;
					player.setPosition(spectatedPlayer.getPosX(), spectatedPlayer.getPosY(), spectatedPlayer.getPosZ());
				}
			}
		} else {
			// force player to maximum flying speed since scrollwheel has been stolen
			CLIENT.player.abilities.setFlySpeed(0.2F);
		}
	}

	@SubscribeEvent
	public static void onRenderTick(TickEvent.RenderTickEvent event) {
		Session session = ChaseCameraManager.session;
		if (session == null || event.phase == TickEvent.Phase.END) {
			return;
		}

		if (session.isSpectating()) {
			ClientPlayerEntity player = CLIENT.player;
			Entity focusEntity = CLIENT.getRenderViewEntity();
			focusEntity = focusEntity != null ? focusEntity : player;

			boolean firstPerson = session.zoom <= 1e-2;

			if (!firstPerson) {
				CLIENT.gameSettings.smoothCamera = true;
				CLIENT.gameSettings.thirdPersonView = 2;
			} else {
				CLIENT.gameSettings.smoothCamera = false;
				CLIENT.gameSettings.thirdPersonView = 0;

				player.rotationYaw = focusEntity.rotationYaw;
				player.rotationPitch = focusEntity.rotationPitch;
			}
		} else {
			CLIENT.gameSettings.smoothCamera = false;
			CLIENT.gameSettings.thirdPersonView = 0;
		}
	}

	@SubscribeEvent
	public static void onPositionCamera(EntityViewRenderEvent.CameraSetup event) {
		Session session = ChaseCameraManager.session;
		if (session == null) {
			return;
		}

		if (session.isSpectating()) {
			ClientPlayerEntity player = CLIENT.player;
			Entity focusEntity = CLIENT.getRenderViewEntity();
			focusEntity = focusEntity != null ? focusEntity : player;

			float partialTicks = (float) event.getRenderPartialTicks();
			double zoom = MathHelper.lerp(partialTicks, session.prevZoom, session.zoom);
			boolean firstPerson = zoom <= 1e-2;

			if (!firstPerson) {
				ActiveRenderInfo camera = event.getInfo();

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

				double distance = zoom * MAX_CHASE_DISTANCE;
				camera.movePosition(-camera.calcCameraDistance(distance), 0.0, 0.0);
			}
		}
	}

	@SubscribeEvent
	public static void onMouseScroll(InputEvent.MouseScrollEvent event) {
		Session session = ChaseCameraManager.session;
		if (session == null) {
			return;
		}

		double delta = event.getScrollDelta();

		boolean zoom = InputMappings.isKeyDown(CLIENT.getMainWindow().getHandle(), ZOOM_KEY);
		if (zoom) {
			onScrollZoom(session, delta);
		} else {
			onScrollSelection(session, delta);
		}
	}

	private static void onScrollZoom(Session session, double delta) {
		session.zoom = MathHelper.clamp(session.zoom - delta * 0.1, 0.0, 1.0);
	}

	private static void onScrollSelection(Session session, double delta) {
		if (accumulatedScroll != 0.0 && Math.signum(delta) != Math.signum(accumulatedScroll)) {
			accumulatedScroll = 0.0;
		}

		accumulatedScroll += delta;

		int scrollAmount = (int) accumulatedScroll;
		if (scrollAmount != 0) {
			int newIndex = session.selectedPlayerIndex - scrollAmount;
			newIndex = MathHelper.clamp(newIndex, -1, session.players.size() - 1);

			if (newIndex != -1) {
				session.startSpectating(newIndex);
			} else {
				session.stopSpectating();
			}
		}
	}

	@SubscribeEvent
	public static void onRenderOverlay(RenderGameOverlayEvent.Post event) {
		Session session = ChaseCameraManager.session;
		if (session == null) {
			return;
		}

		if (event.getType() == RenderGameOverlayEvent.ElementType.ALL) {
			MainWindow window = event.getWindow();
			renderChasePlayerList(session, window);
		}
	}

	private static void renderChasePlayerList(Session session, MainWindow window) {
		RenderSystem.pushMatrix();
		RenderSystem.enableAlphaTest();
		RenderSystem.enableBlend();
		RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);

		int listHeight = (session.players.size() + 1) * ENTRY_SIZE;

		int minY = (window.getScaledHeight() - listHeight) / 2;

		RenderSystem.disableTexture();

		GameProfile localProfile = CLIENT.player.getGameProfile();

		// TODO: could make this code good and have a list of generic entries that aren't specifically players..
		//       but i don't feel like it. :)

		// draw the selection box
		String selectedName;
		if (session.selectedPlayerIndex != -1) {
			GameProfile profile = session.getPlayerProfile(session.players.get(session.selectedPlayerIndex));
			selectedName = profile != null ? profile.getName() : "";
		} else {
			selectedName = "Free Camera";
		}

		int selectionWidth = ENTRY_SIZE + ENTRY_PADDING + CLIENT.fontRenderer.getStringWidth(selectedName);
		int selectionY = minY + (session.selectedPlayerIndex + 1) * ENTRY_SIZE;
		AbstractGui.fill(0, selectionY - 1, selectionWidth + 1, selectionY + ENTRY_SIZE - 1, 0x80000000);

		// draw all the player entries
		RenderSystem.enableTexture();

		int y = minY;

		renderPlayerEntry(session, "Free Camera", localProfile, y);
		y += ENTRY_SIZE;

		for (UUID player : session.players) {
			GameProfile profile = session.getPlayerProfile(player);
			if (profile == null) {
				continue;
			}

			String name = profile.getName();
			renderPlayerEntry(session, name, profile, y);

			y += ENTRY_SIZE;
		}

		RenderSystem.popMatrix();
	}

	private static void renderPlayerEntry(Session session, String name, GameProfile profile, int y) {
		ResourceLocation skin = session.getPlayerSkin(profile);

		CLIENT.getTextureManager().bindTexture(skin);
		AbstractGui.blit(ENTRY_PADDING, y, 12, 12, 8.0F, 8.0F, 8, 8, 64, 64);
		AbstractGui.blit(ENTRY_PADDING, y, 12, 12, 40.0F, 8.0F, 8, 8, 64, 64);

		CLIENT.fontRenderer.drawString(name, ENTRY_SIZE + ENTRY_PADDING, y + ENTRY_PADDING, 0xFFFFFFFF);
	}

	static class Session {
		List<UUID> players;
		private final Map<UUID, GameProfile> playerProfiles = new HashMap<>();
		private final Map<UUID, ResourceLocation> playerSkins = new HashMap<>();
		int selectedPlayerIndex = -1;

		double zoom = 1.0;
		double prevZoom = 1.0;

		Session(List<UUID> players) {
			this.players = players;
		}

		ResourceLocation getPlayerSkin(GameProfile profile) {
			ResourceLocation existingSkin = playerSkins.get(profile.getId());
			if (existingSkin != null) {
				return existingSkin;
			}

			ResourceLocation skin = loadPlayerSkin(profile);
			playerSkins.put(profile.getId(), skin);

			return skin;
		}

		@Nullable
		GameProfile getPlayerProfile(UUID id) {
			GameProfile existingProfile = playerProfiles.get(id);
			if (existingProfile != null) {
				return existingProfile;
			}

			PlayerEntity player = CLIENT.world.getPlayerByUuid(id);
			if (player == null) {
				return null;
			}

			GameProfile profile = player.getGameProfile();
			playerProfiles.put(id, profile);

			return profile;
		}

		private ResourceLocation loadPlayerSkin(GameProfile profile) {
			SkinManager skinManager = CLIENT.getSkinManager();

			Map<MinecraftProfileTexture.Type, MinecraftProfileTexture> textures = skinManager.loadSkinFromCache(profile);
			if (textures.containsKey(MinecraftProfileTexture.Type.SKIN)) {
				return skinManager.loadSkin(textures.get(MinecraftProfileTexture.Type.SKIN), MinecraftProfileTexture.Type.SKIN);
			} else {
				return DefaultPlayerSkin.getDefaultSkin(PlayerEntity.getUUID(profile));
			}
		}

		boolean isSpectating() {
			return selectedPlayerIndex != -1;
		}

		void startSpectating(int index) {
			selectedPlayerIndex = index;
			LTNetwork.CHANNEL.sendToServer(new ChaseSpectatePlayerMessage(players.get(index)));
		}

		void stopSpectating() {
			selectedPlayerIndex = -1;
			LTNetwork.CHANNEL.sendToServer(new ChaseSpectatePlayerMessage(CLIENT.player.getUniqueID()));
		}

		void update(List<UUID> players) {
			List<UUID> previousPlayers = session.players;
			UUID selectedPlayer = session.selectedPlayerIndex != -1 ? previousPlayers.get(session.selectedPlayerIndex) : null;

			session.players = players;

			int index = selectedPlayer != null ? players.indexOf(selectedPlayer) : -1;
			if (index != -1) {
				startSpectating(index);
			} else {
				stopSpectating();
			}
		}
	}
}
