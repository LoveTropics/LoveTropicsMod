package com.lovetropics.minigames.common.core.network;

import com.lovetropics.minigames.LoveTropics;
import com.lovetropics.minigames.client.tweaks.ClientGameTweaksState;
import com.lovetropics.minigames.common.core.game.client_tweak.GameClientTweak;
import com.lovetropics.minigames.common.core.game.client_tweak.GameClientTweakType;
import com.lovetropics.minigames.common.core.game.client_tweak.GameClientTweakTypes;
import com.mojang.serialization.DataResult;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.NBTDynamicOps;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import javax.annotation.Nullable;
import java.util.function.Supplier;

public class SetGameClientTweakMessage {
	private final GameClientTweakType<?> type;
	@Nullable
	private final GameClientTweak tweak;

	private SetGameClientTweakMessage(GameClientTweakType<?> type, @Nullable GameClientTweak tweak) {
		this.type = type;
		this.tweak = tweak;
	}

	public static <T extends GameClientTweak> SetGameClientTweakMessage set(T tweak) {
		return new SetGameClientTweakMessage(tweak.getType(), tweak);
	}

	public static <T extends GameClientTweak> SetGameClientTweakMessage remove(GameClientTweakType<T> type) {
		return new SetGameClientTweakMessage(type, null);
	}

	public void encode(PacketBuffer buffer) {
		buffer.writeRegistryIdUnsafe(GameClientTweakTypes.REGISTRY.get(), this.type);

		CompoundNBT tweakNbt = tweak != null ? tryEncodeUnchecked(type, tweak) : null;
		buffer.writeCompoundTag(tweakNbt);
	}

	public static SetGameClientTweakMessage decode(PacketBuffer buffer) {
		GameClientTweakType<?> type = buffer.readRegistryIdUnsafe(GameClientTweakTypes.REGISTRY.get());
		CompoundNBT tweakNbt = buffer.readCompoundTag();
		GameClientTweak tweak = tweakNbt != null ? tryDecode(type, tweakNbt) : null;
		return new SetGameClientTweakMessage(type, tweak);
	}

	@SuppressWarnings("unchecked")
	@Nullable
	private static <T extends GameClientTweak> CompoundNBT tryEncodeUnchecked(GameClientTweakType<T> type, GameClientTweak tweak) {
		DataResult<INBT> result = type.getCodec().encodeStart(NBTDynamicOps.INSTANCE, (T) tweak)
				.flatMap(nbt -> nbt instanceof CompoundNBT ? DataResult.success(nbt) : DataResult.error("Encoded tweak was not a compound!"));

		result.error().ifPresent(error -> {
			LoveTropics.LOGGER.warn("Failed to encode client tweak of type '{}': {}", type.getRegistryName(), error);
		});

		return (CompoundNBT) result.result().orElse(null);
	}

	@Nullable
	private static <T extends GameClientTweak> T tryDecode(GameClientTweakType<T> type, CompoundNBT nbt) {
		DataResult<T> result = type.getCodec().parse(NBTDynamicOps.INSTANCE, nbt);
		result.error().ifPresent(error -> {
			LoveTropics.LOGGER.warn("Failed to decode client tweak of type '{}': {}", type.getRegistryName(), error);
		});

		return result.result().orElse(null);
	}

	public void handle(Supplier<NetworkEvent.Context> ctx) {
		ctx.get().enqueueWork(() -> {
			if (tweak != null) {
				ClientGameTweaksState.set(tweak);
			} else {
				ClientGameTweaksState.remove(type);
			}
		});
		ctx.get().setPacketHandled(true);
	}
}
