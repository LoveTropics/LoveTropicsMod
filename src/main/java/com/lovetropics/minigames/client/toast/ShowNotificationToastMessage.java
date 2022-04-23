package com.lovetropics.minigames.client.toast;

import net.minecraft.network.PacketBuffer;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public final class ShowNotificationToastMessage {
	private final ITextComponent message;
	private final NotificationDisplay display;

	public ShowNotificationToastMessage(ITextComponent message, NotificationDisplay display) {
		this.message = message;
		this.display = display;
	}

	public void encode(PacketBuffer buffer) {
		buffer.writeComponent(this.message);
		this.display.encode(buffer);
	}

	public static ShowNotificationToastMessage decode(PacketBuffer buffer) {
		ITextComponent message = buffer.readComponent();
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
