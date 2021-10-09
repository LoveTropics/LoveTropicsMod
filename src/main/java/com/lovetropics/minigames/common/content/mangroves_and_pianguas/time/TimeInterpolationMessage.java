package com.lovetropics.minigames.common.content.mangroves_and_pianguas.time;

import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public final class TimeInterpolationMessage {
    private final int speed;

    public TimeInterpolationMessage(int speed) {
        this.speed = speed;
    }

    public void encode(PacketBuffer buffer) {
        buffer.writeVarInt(this.speed);
    }

    public static TimeInterpolationMessage decode(PacketBuffer buffer) {
        int speed = buffer.readVarInt();

        return new TimeInterpolationMessage(speed);
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            TimeInterpolation.updateSpeed(this.speed);
        });
        ctx.get().setPacketHandled(true);
    }
}
