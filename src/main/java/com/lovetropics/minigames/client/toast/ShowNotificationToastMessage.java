package com.lovetropics.minigames.client.toast;

import net.minecraft.client.Minecraft;
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
		buffer.writeTextComponent(this.message);
		this.display.encode(buffer);
	}

	public static ShowNotificationToastMessage decode(PacketBuffer buffer) {
		ITextComponent message = buffer.readTextComponent();
		NotificationDisplay display = NotificationDisplay.decode(buffer);
		return new ShowNotificationToastMessage(message, display);
	}

	public void handle(Supplier<NetworkEvent.Context> ctx) {
		ctx.get().enqueueWork(() -> {
			displayToast(this.message, this.display);
		});
		ctx.get().setPacketHandled(true);
	}

	private static void displayToast(ITextComponent message, NotificationDisplay display) {
		Minecraft client = Minecraft.getInstance();
		client.getToastGui().add(new NotificationToast(message, display));
	}
}
