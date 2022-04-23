package com.lovetropics.minigames.client.lobby.screen.game_config;

import com.lovetropics.minigames.client.screen.flex.Layout;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.TextComponent;

public class BooleanButton extends Button {

	private boolean value;

	public BooleanButton(Layout layout, boolean def) {
		super(layout.content().left(), layout.content().top(), layout.content().width(), layout.content().height(),
				new TextComponent(Boolean.toString(def)), BooleanButton::toggle, NO_TOOLTIP);
	}

	private static void toggle(Button button) {
		BooleanButton b = (BooleanButton) button;
		b.value = !b.value;
		b.setMessage(new TextComponent(Boolean.toString(b.value)));
	}
}
