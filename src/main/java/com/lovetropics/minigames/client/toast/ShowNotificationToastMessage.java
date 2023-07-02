package com.lovetropics.minigames.client.toast;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record ShowNotificationToastMessage(Component message, NotificationStyle style) {
	public void encode(FriendlyByteBuf buffer) {
		buffer.writeComponent(this.message);
		this.style.encode(buffer);
	}

	public static ShowNotificationToastMessage decode(FriendlyByteBuf buffer) {
		Component message = buffer.readComponent();
		NotificationStyle style = NotificationStyle.decode(buffer);
		return new ShowNotificationToastMessage(message, style);
	}

	public void handle(Supplier<NetworkEvent.Context> ctx) {
		NotificationToasts.display(this.message, this.style);
	}
}
