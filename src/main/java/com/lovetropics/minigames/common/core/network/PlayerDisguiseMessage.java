package com.lovetropics.minigames.common.core.network;

import com.lovetropics.minigames.Constants;
import com.lovetropics.minigames.client.ClientPlayerDisguises;
import com.lovetropics.minigames.common.core.diguise.DisguiseType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.Optional;

public record PlayerDisguiseMessage(int entityId, Optional<DisguiseType> disguise) implements CustomPacketPayload {
    public static final Type<PlayerDisguiseMessage> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(Constants.MODID, "player_disguise"));

    public static final StreamCodec<RegistryFriendlyByteBuf, PlayerDisguiseMessage> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, PlayerDisguiseMessage::entityId,
            DisguiseType.STREAM_CODEC.apply(ByteBufCodecs::optional), PlayerDisguiseMessage::disguise,
            PlayerDisguiseMessage::new
    );

    public static void handle(PlayerDisguiseMessage message, IPayloadContext context) {
        ClientPlayerDisguises.updateClientDisguise(message.entityId, message.disguise.orElse(null));
    }

    @Override
    public Type<PlayerDisguiseMessage> type() {
        return TYPE;
    }
}
