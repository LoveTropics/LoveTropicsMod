package com.lovetropics.minigames.common.content.river_race.render;

import com.lovetropics.minigames.LoveTropics;
import com.lovetropics.minigames.client.game.ClientGameStateManager;
import com.lovetropics.minigames.common.content.river_race.RiverRace;
import com.lovetropics.minigames.common.content.river_race.client_state.RiverRaceClientBarState;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor;
import net.minecraft.world.item.DyeColor;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.event.RenderGuiLayerEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;

@EventBusSubscriber(modid = LoveTropics.ID, value = Dist.CLIENT)
public final class RiverRaceBarRenderer {
	private static final int MAP_TOP = 3;

	private static final int MAP_WIDTH = 300;
	private static final int MAP_HEIGHT = 24;
	private static final int MAP_MARGIN_X = 2;
	private static final int MAP_MARGIN_Y = 4;

	private static final int BAR_HEIGHT = 2;

	private static final int POINTER_SIZE = 5;

	private static final ResourceLocation MAP_SPRITE = LoveTropics.location("minigames/river_race/map");
	private static final ResourceLocation BAR_SPRITE = LoveTropics.location("minigames/river_race/bar");
	private static final ResourceLocation POINTER_TOP_SPRITE = LoveTropics.location("minigames/river_race/pointer_top");
	private static final ResourceLocation POINTER_BOTTOM_SPRITE = LoveTropics.location("minigames/river_race/pointer_bottom");
	private static final ResourceLocation LOCKED_SPRITE = LoveTropics.location("minigames/river_race/locked");

	public static void registerOverlays(RegisterGuiLayersEvent event) {
		event.registerAbove(VanillaGuiLayers.BOSS_OVERLAY, LoveTropics.location("river_race_bar"), (graphics, deltaTracker) -> {
			if (Minecraft.getInstance().options.hideGui) {
				return;
			}
			RiverRaceClientBarState barState = ClientGameStateManager.getOrNull(RiverRace.BAR_STATE);
			if (barState != null) {
				render(graphics, barState);
			}
		});
	}

	@SubscribeEvent
	public static void onRenderLayerPre(RenderGuiLayerEvent.Pre event) {
		if (event.getName().equals(VanillaGuiLayers.BOSS_OVERLAY) && ClientGameStateManager.getOrNull(RiverRace.BAR_STATE) != null) {
			PoseStack pose = event.getGuiGraphics().pose();
			pose.pushPose();
			pose.translate(0.0f, MAP_HEIGHT + POINTER_SIZE, 0.0f);
		}
	}

	@SubscribeEvent
	public static void onRenderLayerPost(RenderGuiLayerEvent.Post event) {
		if (event.getName().equals(VanillaGuiLayers.BOSS_OVERLAY) && ClientGameStateManager.getOrNull(RiverRace.BAR_STATE) != null) {
			event.getGuiGraphics().pose().popPose();
		}
	}

	private static void render(GuiGraphics graphics, RiverRaceClientBarState barState) {
		RenderSystem.enableBlend();

		int mapLeft = (graphics.guiWidth() - MAP_WIDTH) / 2;
		int left = mapLeft + MAP_MARGIN_X;
		renderTeamMarkers(graphics, barState.topTeam(), left, true);
		renderTeamMarkers(graphics, barState.bottomTeam(), left, false);

		graphics.blitSprite(MAP_SPRITE, mapLeft, MAP_TOP, MAP_WIDTH, MAP_HEIGHT);

		for (RiverRaceClientBarState.Zone zone : barState.lockedZones()) {
			setColor(graphics, zone.color());
			graphics.blitSprite(LOCKED_SPRITE, left + zone.start(), MAP_TOP + MAP_MARGIN_Y, zone.length(), MAP_HEIGHT - MAP_MARGIN_Y * 2);
		}
		graphics.setColor(1.0f, 1.0f, 1.0f, 1.0f);
		RenderSystem.disableBlend();
	}

	private static void renderTeamMarkers(GuiGraphics graphics, RiverRaceClientBarState.Team team, int left, boolean top) {
		int barY = top ? MAP_TOP + MAP_MARGIN_Y - BAR_HEIGHT - 1 : MAP_TOP + MAP_HEIGHT - MAP_MARGIN_Y + 1;
		int pointerY = top ? 0 : MAP_TOP + MAP_HEIGHT - MAP_MARGIN_Y + BAR_HEIGHT;

		setColor(graphics, team.color());
		graphics.blitSprite(BAR_SPRITE, left, barY, 0, team.progress(), BAR_HEIGHT);

		IntList playerPositions = team.players();
		for (int i = 0; i < playerPositions.size(); i++) {
			int x = playerPositions.getInt(i);
			graphics.blitSprite(top ? POINTER_TOP_SPRITE : POINTER_BOTTOM_SPRITE, left + x - POINTER_SIZE / 2 - 1, pointerY, POINTER_SIZE, POINTER_SIZE);
		}

		graphics.setColor(1.0f, 1.0f, 1.0f, 1.0f);
	}

	private static void setColor(GuiGraphics graphics, DyeColor dyeColor) {
		int color = dyeColor.getTextureDiffuseColor();
		graphics.setColor(
				FastColor.ARGB32.red(color) / 255.0f,
				FastColor.ARGB32.green(color) / 255.0f,
				FastColor.ARGB32.blue(color) / 255.0f,
				1.0f
		);
	}
}
