package com.lovetropics.minigames.client.toast;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public final class ShowNotificationToastMessage {
	private final Component message;
	private final NotificationDisplay display;

	public ShowNotificationToastMessage(Component message, NotificationDisplay display) {
		this.message = message;
		this.display = display;
	}

	public void encode(FriendlyByteBuf buffer) {
		buffer.writeComponent(this.message);
		this.display.encode(buffer);
	}

	public static ShowNotificationToastMessage decode(FriendlyByteBuf buffer) {
		Component message = buffer.readComponent();
		NotificationDisplay display = NotificationDisplay.decode(buffer);
		return new ShowNotificationToastMessage(message, display);
	}

	public void handle(Supplier<NetworkEvent.Context> ctx) {
		ctx.get().enqueueWork(() -> {
			NotificationToasts.display(this.message, this.display);
		});
		ctx.get().setPacketHandled(true);
	}
}
