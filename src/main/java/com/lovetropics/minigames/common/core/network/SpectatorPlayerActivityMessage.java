package com.lovetropics.minigames.common.core.network;

import java.util.UUID;
import java.util.function.Supplier;

import com.lovetropics.minigames.client.game.handler.spectate.ClientSpectatingManager;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.TextColor;
import net.minecraftforge.network.NetworkEvent;

public record SpectatorPlayerActivityMessage(UUID player, TextColor style) {
    public void encode(FriendlyByteBuf buffer) {
        buffer.writeUUID(player);
        // The longest colour name is 12 characters, so only leave a small bit of headway in our length assumption
        buffer.writeUtf(style.serialize(), 16);
    }

    public static SpectatorPlayerActivityMessage decode(FriendlyByteBuf buffer) {
        UUID player = buffer.readUUID();
        TextColor style = TextColor.parseColor(buffer.readUtf(16));
        return new SpectatorPlayerActivityMessage(player, style);
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ClientSpectatingManager.INSTANCE.onPlayerActivity(player, style);
        });
        ctx.get().setPacketHandled(true);
    }
}
