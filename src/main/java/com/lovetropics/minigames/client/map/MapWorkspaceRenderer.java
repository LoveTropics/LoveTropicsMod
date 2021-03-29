package com.lovetropics.minigames.client.map;

import com.lovetropics.minigames.Constants;
import com.lovetropics.minigames.common.core.map.MapRegion;
import com.lovetropics.minigames.common.core.map.workspace.ClientWorkspaceRegions;
import com.mojang.blaze3d.systems.RenderSystem;
import it.unimi.dsi.fastutil.HashCommon;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.debug.DebugRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.opengl.GL11;

import java.util.Set;

@Mod.EventBusSubscriber(modid = Constants.MODID, value = Dist.CLIENT)
public final class MapWorkspaceRenderer {
	@SubscribeEvent
	public static void onRenderWorld(RenderWorldLastEvent event) {
		ClientWorkspaceRegions regions = ClientMapWorkspace.INSTANCE.getRegions();
		if (regions.isEmpty()) {
			return;
		}

		Minecraft client = Minecraft.getInstance();
		ActiveRenderInfo renderInfo = client.gameRenderer.getActiveRenderInfo();
		if (!renderInfo.isValid()) {
			return;
		}

		Vector3d view = renderInfo.getProjectedView();

		RenderSystem.pushMatrix();
		RenderSystem.multMatrix(event.getMatrixStack().getLast().getMatrix());

		RenderSystem.disableTexture();
		RenderSystem.enableBlend();
		RenderSystem.defaultBlendFunc();
		RenderSystem.disableLighting();
		RenderSystem.enableDepthTest();

		RenderSystem.polygonOffset(-1.0F, -10.0F);
		RenderSystem.enablePolygonOffset();

		RenderSystem.depthMask(false);

		Set<ClientWorkspaceRegions.Entry> selectedRegions = MapWorkspaceTracer.getSelectedRegions();

		for (ClientWorkspaceRegions.Entry entry : regions) {
			int color = colorForKey(entry.key);
			float red = (color >> 16 & 0xFF) / 255.0F;
			float green = (color >> 8 & 0xFF) / 255.0F;
			float blue = (color & 0xFF) / 255.0F;
			float outlineRed = red;
			float outlineGreen = green;
			float outlineBlue = blue;

			float alpha = 0.3F;

			if (selectedRegions.contains(entry)) {
				double time = client.world.getGameTime() + event.getPartialTicks();
				float animation = (float) ((Math.sin(time * 0.1) + 1.0) / 2.0);

				alpha = 0.4F + animation * 0.15F;

				red = Math.min(red * 1.3F, 1.0F);
				green = Math.min(green * 1.3F, 1.0F);
				blue = Math.min(blue * 1.3F, 1.0F);
				outlineRed = outlineGreen = outlineBlue = 1.0F;
			}

			MapRegion region = entry.region;
			double minX = region.min.getX() - view.x;
			double minY = region.min.getY() - view.y;
			double minZ = region.min.getZ() - view.z;
			double maxX = region.max.getX() + 1.0 - view.x;
			double maxY = region.max.getY() + 1.0 - view.y;
			double maxZ = region.max.getZ() + 1.0 - view.z;

			DebugRenderer.renderBox(minX, minY, minZ, maxX, maxY, maxZ, red, green, blue, alpha);
			renderOutline(minX, minY, minZ, maxX, maxY, maxZ, outlineRed, outlineGreen, outlineBlue, 1.0F);
		}

		for (ClientWorkspaceRegions.Entry entry : regions) {
			Vector3d center = entry.region.getCenter();
			BlockPos size = entry.region.getSize();

			int minSize = Math.min(size.getX(), Math.min(size.getY(), size.getZ())) - 1;
			float scale = MathHelper.clamp(minSize * 0.03125F, 0.03125F, 0.125F);

			DebugRenderer.renderText(entry.key, center.x, center.y, center.z, 0xFFFFFFFF, scale);
		}

		RenderSystem.depthMask(true);

		RenderSystem.polygonOffset(0.0F, 0.0F);
		RenderSystem.disablePolygonOffset();

		RenderSystem.popMatrix();
	}

	private static int colorForKey(String key) {
		return HashCommon.mix(key.hashCode()) & 0xFFFFFF;
	}

	private static void renderOutline(double minX, double minY, double minZ, double maxX, double maxY, double maxZ, float red, float green, float blue, float alpha) {
		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder builder = tessellator.getBuffer();
		builder.begin(GL11.GL_LINES, DefaultVertexFormats.POSITION_COLOR);

		builder.pos(minX, minY, minZ).color(red, green, blue, alpha).endVertex();
		builder.pos(maxX, minY, minZ).color(red, green, blue, alpha).endVertex();
		builder.pos(minX, minY, minZ).color(red, green, blue, alpha).endVertex();
		builder.pos(minX, maxY, minZ).color(red, green, blue, alpha).endVertex();
		builder.pos(minX, minY, minZ).color(red, green, blue, alpha).endVertex();
		builder.pos(minX, minY, maxZ).color(red, green, blue, alpha).endVertex();
		builder.pos(maxX, minY, minZ).color(red, green, blue, alpha).endVertex();
		builder.pos(maxX, maxY, minZ).color(red, green, blue, alpha).endVertex();
		builder.pos(maxX, maxY, minZ).color(red, green, blue, alpha).endVertex();
		builder.pos(minX, maxY, minZ).color(red, green, blue, alpha).endVertex();
		builder.pos(minX, maxY, minZ).color(red, green, blue, alpha).endVertex();
		builder.pos(minX, maxY, maxZ).color(red, green, blue, alpha).endVertex();
		builder.pos(minX, maxY, maxZ).color(red, green, blue, alpha).endVertex();
		builder.pos(minX, minY, maxZ).color(red, green, blue, alpha).endVertex();
		builder.pos(minX, minY, maxZ).color(red, green, blue, alpha).endVertex();
		builder.pos(maxX, minY, maxZ).color(red, green, blue, alpha).endVertex();
		builder.pos(maxX, minY, maxZ).color(red, green, blue, alpha).endVertex();
		builder.pos(maxX, minY, minZ).color(red, green, blue, alpha).endVertex();
		builder.pos(minX, maxY, maxZ).color(red, green, blue, alpha).endVertex();
		builder.pos(maxX, maxY, maxZ).color(red, green, blue, alpha).endVertex();
		builder.pos(maxX, minY, maxZ).color(red, green, blue, alpha).endVertex();
		builder.pos(maxX, maxY, maxZ).color(red, green, blue, alpha).endVertex();
		builder.pos(maxX, maxY, minZ).color(red, green, blue, alpha).endVertex();
		builder.pos(maxX, maxY, maxZ).color(red, green, blue, alpha).endVertex();

		tessellator.draw();
	}
}
