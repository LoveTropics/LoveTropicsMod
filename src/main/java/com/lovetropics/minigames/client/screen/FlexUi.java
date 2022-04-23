package com.lovetropics.minigames.client.screen;

import com.lovetropics.minigames.client.screen.flex.Box;
import com.lovetropics.minigames.client.screen.flex.Layout;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;

public final class FlexUi {
	public static void fill(Layout layout, PoseStack matrixStack, int color) {
		fill(layout.background(), matrixStack, color);
	}

	public static void fill(Box box, PoseStack matrixStack, int color) {
		GuiComponent.fill(matrixStack, box.left(), box.top(), box.right(), box.bottom(), color);
	}

	public static Button createButton(Layout layout, Component title, Button.OnPress action) {
		Box background = layout.background();
		return new Button(background.left(), background.top(), background.width(), background.height(), title, action);
	}

	public static EditBox createTextField(Layout layout, Font font, Component title) {
		Box background = layout.background();
		return new EditBox(font, background.left(), background.top(), background.width(), background.height(), title);
	}
}
