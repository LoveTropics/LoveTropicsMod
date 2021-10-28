package com.lovetropics.minigames.common.core.network;

import com.lovetropics.minigames.LoveTropics;
import com.lovetropics.minigames.client.game.ClientGameStateManager;
import com.lovetropics.minigames.common.core.game.client_state.GameClientState;
import com.lovetropics.minigames.common.core.game.client_state.GameClientStateType;
import com.lovetropics.minigames.common.core.game.client_state.GameClientStateTypes;
import com.mojang.serialization.DataResult;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.NBTDynamicOps;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import javax.annotation.Nullable;
import java.util.function.Supplier;

public class SetGameClientStateMessage {
	private final GameClientStateType<?> type;
	@Nullable
	private final GameClientState state;

	private SetGameClientStateMessage(GameClientStateType<?> type, @Nullable GameClientState state) {
		this.type = type;
		this.state = state;
	}

	public static <T extends GameClientState> SetGameClientStateMessage set(T state) {
		return new SetGameClientStateMessage(state.getType(), state);
	}

	public static <T extends GameClientState> SetGameClientStateMessage remove(GameClientStateType<T> type) {
		return new SetGameClientStateMessage(type, null);
	}

	public void encode(PacketBuffer buffer) {
		buffer.writeRegistryIdUnsafe(GameClientStateTypes.REGISTRY.get(), this.type);

		CompoundNBT stateNbt = state != null ? tryEncodeUnchecked(type, state) : null;
		buffer.writeCompoundTag(stateNbt);
	}

	public static SetGameClientStateMessage decode(PacketBuffer buffer) {
		GameClientStateType<?> type = buffer.readRegistryIdUnsafe(GameClientStateTypes.REGISTRY.get());
		CompoundNBT stateNbt = buffer.readCompoundTag();
		GameClientState state = stateNbt != null ? tryDecode(type, stateNbt) : null;
		return new SetGameClientStateMessage(type, state);
	}

	@SuppressWarnings("unchecked")
	@Nullable
	private static <T extends GameClientState> CompoundNBT tryEncodeUnchecked(GameClientStateType<T> type, GameClientState state) {
		DataResult<INBT> result = type.getCodec().encodeStart(NBTDynamicOps.INSTANCE, (T) state)
				.flatMap(nbt -> nbt instanceof CompoundNBT ? DataResult.success(nbt) : DataResult.error("Encoded state was not a compound!"));

		result.error().ifPresent(error -> {
			LoveTropics.LOGGER.warn("Failed to encode client state of type '{}': {}", type.getRegistryName(), error);
		});

		return (CompoundNBT) result.result().orElse(null);
	}

	@Nullable
	private static <T extends GameClientState> T tryDecode(GameClientStateType<T> type, CompoundNBT nbt) {
		DataResult<T> result = type.getCodec().parse(NBTDynamicOps.INSTANCE, nbt);
		result.error().ifPresent(error -> {
			LoveTropics.LOGGER.warn("Failed to decode client state of type '{}': {}", type.getRegistryName(), error);
		});

		return result.result().orElse(null);
	}

	public void handle(Supplier<NetworkEvent.Context> ctx) {
		ctx.get().enqueueWork(() -> {
			if (state != null) {
				ClientGameStateManager.set(state);
			} else {
				ClientGameStateManager.remove(type);
			}
		});
		ctx.get().setPacketHandled(true);
	}
}
