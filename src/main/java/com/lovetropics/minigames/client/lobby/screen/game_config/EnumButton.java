package com.lovetropics.minigames.client.lobby.screen.game_config;

import com.lovetropics.minigames.client.screen.flex.Layout;

import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.util.text.StringTextComponent;

public class EnumButton<E extends Enum<E>> extends Button {

	private E value;

	public EnumButton(Layout layout, E def) {
		super(layout.content().left(), layout.content().top(), layout.content().width(), layout.content().height(),
				new StringTextComponent(def.name()), EnumButton::toggle, EMPTY_TOOLTIP);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private static void toggle(Button button) {
		EnumButton b = (EnumButton) button;
		int ordinal = b.value.ordinal();
		Enum<?>[] values = b.value.getClass().getEnumConstants();
		b.value = values[(ordinal + 1) % values.length];
		b.setMessage(new StringTextComponent(b.value.name()));
	}
}
