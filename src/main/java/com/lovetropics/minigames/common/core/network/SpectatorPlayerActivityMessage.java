package com.lovetropics.minigames.common.core.network;

import com.lovetropics.minigames.client.game.handler.spectate.ClientSpectatingManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.UUID;
import java.util.function.Supplier;

public record SpectatorPlayerActivityMessage(UUID player, int color) {
    public void encode(FriendlyByteBuf buffer) {
        buffer.writeUUID(player);
        buffer.writeInt(color);
    }

    public static SpectatorPlayerActivityMessage decode(FriendlyByteBuf buffer) {
        UUID player = buffer.readUUID();
        int color = buffer.readInt();
        return new SpectatorPlayerActivityMessage(player, color);
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ClientSpectatingManager.INSTANCE.onPlayerActivity(player, color);
    }
}
