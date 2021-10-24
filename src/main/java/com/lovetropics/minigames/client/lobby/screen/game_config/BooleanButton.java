package com.lovetropics.minigames.client.lobby.screen.game_config;

import com.lovetropics.minigames.client.screen.flex.Layout;

import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.util.text.StringTextComponent;

public class BooleanButton extends Button {

	private boolean value;

	public BooleanButton(Layout layout, boolean def) {
		super(layout.content().left(), layout.content().top(), layout.content().width(), layout.content().height(),
				new StringTextComponent(Boolean.toString(def)), BooleanButton::toggle, EMPTY_TOOLTIP);
	}

	private static void toggle(Button button) {
		BooleanButton b = (BooleanButton) button;
		b.value = !b.value;
		b.setMessage(new StringTextComponent(Boolean.toString(b.value)));
	}
}
