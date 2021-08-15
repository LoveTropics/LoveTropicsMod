package com.lovetropics.minigames.client.minigame;

import com.lovetropics.minigames.client.toast.DonationPackageToast;
import com.lovetropics.minigames.common.core.game.behavior.instances.donation.DonationPackageData;
import net.minecraft.client.Minecraft;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public final class NotifyDonationPackageMessage {
	private final ITextComponent message;
	private final DonationPackageData.Icon icon;

	public NotifyDonationPackageMessage(ITextComponent message, DonationPackageData.Icon icon) {
		this.message = message;
		this.icon = icon;
	}

	public void encode(PacketBuffer buffer) {
		buffer.writeTextComponent(this.message);
		this.icon.encode(buffer);
	}

	public static NotifyDonationPackageMessage decode(PacketBuffer buffer) {
		ITextComponent message = buffer.readTextComponent();
		DonationPackageData.Icon icon = DonationPackageData.Icon.decode(buffer);
		return new NotifyDonationPackageMessage(message, icon);
	}

	public void handle(Supplier<NetworkEvent.Context> ctx) {
		ctx.get().enqueueWork(() -> {
			displayToast(this.message, this.icon);
		});
		ctx.get().setPacketHandled(true);
	}

	private static void displayToast(ITextComponent message, DonationPackageData.Icon icon) {
		Minecraft client = Minecraft.getInstance();
		client.getToastGui().add(new DonationPackageToast(message, icon));
	}
}
