package com.lovetropics.minigames.client.screen;

import com.lovetropics.minigames.client.screen.flex.Box;
import com.lovetropics.minigames.client.screen.flex.Layout;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.util.text.ITextComponent;

public final class FlexUi {
	public static void fill(Layout layout, MatrixStack matrixStack, int color) {
		fill(layout.background(), matrixStack, color);
	}

	public static void fill(Box box, MatrixStack matrixStack, int color) {
		AbstractGui.fill(matrixStack, box.left(), box.top(), box.right(), box.bottom(), color);
	}

	public static Button createButton(Layout layout, ITextComponent title, Button.IPressable action) {
		Box background = layout.background();
		return new Button(background.left(), background.top(), background.width(), background.height(), title, action);
	}

	public static TextFieldWidget createTextField(Layout layout, FontRenderer font, ITextComponent title) {
		Box background = layout.background();
		return new TextFieldWidget(font, background.left(), background.top(), background.width(), background.height(), title);
	}
}
