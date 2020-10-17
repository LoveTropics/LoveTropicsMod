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
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.client.resources.SkinManager;
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
	private static final double CHASE_DISTANCE = 16.0;

	private static final Minecraft CLIENT = Minecraft.getInstance();

	private static Session session;
	private static double accumulatedScroll;

	public static void start(List<UUID> players) {
		session = new Session(players);
		startSpectating(players.get(0));

		CLIENT.gameSettings.thirdPersonView = 2;
		CLIENT.gameSettings.smoothCamera = true;
	}

	public static void stop() {
		session = null;
		stopSpectating();

		CLIENT.gameSettings.thirdPersonView = 0;
		CLIENT.gameSettings.smoothCamera = false;
	}

	private static void startSpectating(UUID entity) {
		LTNetwork.CHANNEL.sendToServer(new ChaseSpectatePlayerMessage(entity));
	}

	private static void stopSpectating() {
		LTNetwork.CHANNEL.sendToServer(new ChaseSpectatePlayerMessage(CLIENT.player.getUniqueID()));
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

		if (world.getGameTime() % 10 == 0) {
			UUID spectatedId = session.players.get(session.selectedPlayerIndex);
			PlayerEntity spectatedPlayer = world.getPlayerByUuid(spectatedId);

			// we need to send position updates to the server or we won't properly track chunks
			if (spectatedPlayer != null) {
				ClientPlayerEntity player = CLIENT.player;
				player.setPosition(spectatedPlayer.getPosX(), spectatedPlayer.getPosY(), spectatedPlayer.getPosZ());
			}
		}
	}

	@SubscribeEvent
	public static void onPositionCamera(EntityViewRenderEvent.CameraSetup event) {
		if (session == null) {
			return;
		}

		CLIENT.gameSettings.thirdPersonView = 2;

		float partialTicks = (float) event.getRenderPartialTicks();

		ClientPlayerEntity player = CLIENT.player;
		Entity focusEntity = CLIENT.getRenderViewEntity();
		focusEntity = focusEntity != null ? focusEntity : player;

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

		camera.movePosition(-camera.calcCameraDistance(CHASE_DISTANCE), 0.0, 0.0);
	}

	@SubscribeEvent
	public static void onMouseScroll(InputEvent.MouseScrollEvent event) {
		Session session = ChaseCameraManager.session;
		if (session == null) {
			return;
		}

		double delta = event.getScrollDelta();

		if (accumulatedScroll != 0.0 && Math.signum(delta) != Math.signum(accumulatedScroll)) {
			accumulatedScroll = 0.0;
		}

		accumulatedScroll += delta;

		int scrollAmount = (int) accumulatedScroll;
		if (scrollAmount != 0) {
			int newIndex = session.selectedPlayerIndex - scrollAmount;
			newIndex = MathHelper.clamp(newIndex, 0, session.players.size() - 1);

			session.selectedPlayerIndex = newIndex;
			startSpectating(session.players.get(newIndex));
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

		int entryPadding = 2;
		int entrySize = 12 + entryPadding;

		int listHeight = entrySize * session.players.size();

		FontRenderer fontRenderer = CLIENT.fontRenderer;
		TextureManager textureManager = CLIENT.getTextureManager();

		int minY = (window.getScaledHeight() - listHeight) / 2;

		RenderSystem.disableTexture();

		UUID selectedPlayer = session.players.get(session.selectedPlayerIndex);
		GameProfile selectedProfile = session.getPlayerProfile(selectedPlayer);
		if (selectedProfile != null) {
			String name = selectedProfile.getName();

			int width = entrySize + entryPadding + fontRenderer.getStringWidth(name);
			int y = minY + session.selectedPlayerIndex * entrySize;
			AbstractGui.fill(0, y - 1, width + 1, y + entrySize - 1, 0x80000000);
		}

		RenderSystem.enableTexture();

		int y = minY;

		for (UUID player : session.players) {
			GameProfile profile = session.getPlayerProfile(player);
			if (profile == null) {
				continue;
			}

			String name = profile.getName();
			ResourceLocation skin = session.getPlayerSkin(profile);

			textureManager.bindTexture(skin);
			AbstractGui.blit(entryPadding, y, 12, 12, 8.0F, 8.0F, 8, 8, 64, 64);
			AbstractGui.blit(entryPadding, y, 12, 12, 40.0F, 8.0F, 8, 8, 64, 64);

			fontRenderer.drawString(name, entrySize + entryPadding, y + entryPadding, 0xFFFFFFFF);

			y += entrySize;
		}

		RenderSystem.popMatrix();
	}

	static class Session {
		final List<UUID> players;
		private final Map<UUID, GameProfile> playerProfiles = new HashMap<>();
		private final Map<UUID, ResourceLocation> playerSkins = new HashMap<>();
		int selectedPlayerIndex;

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
	}
}
