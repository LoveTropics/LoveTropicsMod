package com.lovetropics.minigames.client.toast;

import net.minecraft.client.Minecraft;
import net.minecraft.util.text.ITextComponent;

final class NotificationToasts {
	static void display(ITextComponent message, NotificationDisplay display) {
		Minecraft client = Minecraft.getInstance();
		client.getToastGui().add(new NotificationToast(message, display));
	}
}
