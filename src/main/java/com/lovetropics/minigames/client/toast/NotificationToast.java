package com.lovetropics.minigames.client.toast;

import com.lovetropics.minigames.LoveTropics;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.toasts.Toast;
import net.minecraft.client.gui.components.toasts.ToastComponent;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;

import java.util.ArrayList;
import java.util.List;

public final class NotificationToast implements Toast {
	private static final int ICON_SIZE = 18;
	private static final int TEXT_LEFT = ICON_SIZE + 8;
	private static final int MAX_WIDTH = 160 - TEXT_LEFT;

	private static final int LINE_HEIGHT = 12;

	private static final Minecraft CLIENT = Minecraft.getInstance();

	private final List<FormattedCharSequence> lines;
	private final NotificationStyle style;
	private final ResourceLocation backgroundSprite;

	private final int width;
	private final int height;

	public NotificationToast(Component message, NotificationStyle style) {
		Font fontRenderer = CLIENT.font;

		List<FormattedCharSequence> lines = new ArrayList<>(2);
		lines.addAll(fontRenderer.split(message, MAX_WIDTH));

		int textWidth = Math.max(lines.stream().mapToInt(fontRenderer::width).max().orElse(MAX_WIDTH), MAX_WIDTH);
		this.width = TEXT_LEFT + textWidth + 4;
		this.height = Math.max(lines.size() * LINE_HEIGHT + 8, 26);

		this.lines = lines;
		this.style = style;
		backgroundSprite = LoveTropics.location("toast/" + style.color().getName() + "_" + style.sentiment().getName());
	}

	@Override
	public Visibility render(GuiGraphics graphics, ToastComponent gui, long time) {
		graphics.blitSprite(backgroundSprite, 0, 0, width, height);

		this.drawText(graphics);
		this.drawIcon(graphics);

		return time >= this.style.visibleTimeMs() ? Toast.Visibility.HIDE : Toast.Visibility.SHOW;
	}

	private void drawText(GuiGraphics graphics) {
		List<FormattedCharSequence> lines = this.lines;
		for (int i = 0; i < lines.size(); i++) {
			FormattedCharSequence line = lines.get(i);
			graphics.drawString(CLIENT.font, line, TEXT_LEFT, 7 + (i * 12), style.color() == NotificationStyle.Color.LIGHT ? 0xFF000000 : 0xFFFFFFFF, false);
		}
	}

	private void drawIcon(GuiGraphics graphics) {
		int y = (this.height - ICON_SIZE) / 2;

		NotificationIcon icon = this.style.icon();
		if (icon.item != null) {
			graphics.renderFakeItem(icon.item, 6, y);
		} else if (icon.effect != null) {
			TextureAtlasSprite sprite = CLIENT.getMobEffectTextures().get(icon.effect);
			graphics.blit(5, y, 0, 18, 18, sprite);
		}
	}

	@Override
	public int width() {
		return this.width;
	}

	@Override
	public int height() {
		return this.height;
	}
}
