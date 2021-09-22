package com.lovetropics.minigames.client.screen.flex;

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.util.text.ITextComponent;

public final class Layout {
	private final Box content;
	private final Box padding;
	private final Box margin;

	public Layout(Box content, Box padding, Box margin) {
		this.content = content;
		this.padding = padding;
		this.margin = margin;
	}

	public Box content() {
		return this.content;
	}

	public Box padding() {
		return this.padding;
	}

	public Box margin() {
		return this.margin;
	}

	public Box background() {
		return this.padding;
	}

	public Button createButton(ITextComponent title, Button.IPressable action) {
		Box background = this.background();
		return new Button(background.left(), background.top(), background.width(), background.height(), title, action);
	}

	public TextFieldWidget createTextField(FontRenderer font, ITextComponent title) {
		Box background = this.background();
		return new TextFieldWidget(font, background.left(), background.top(), background.width(), background.height(), title);
	}
}
