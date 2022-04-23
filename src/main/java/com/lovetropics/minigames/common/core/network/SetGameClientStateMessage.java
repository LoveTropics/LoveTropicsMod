package com.lovetropics.minigames.common.core.network;

import com.lovetropics.minigames.LoveTropics;
import com.lovetropics.minigames.client.game.ClientGameStateManager;
import com.lovetropics.minigames.common.core.game.client_state.GameClientState;
import com.lovetropics.minigames.common.core.game.client_state.GameClientStateType;
import com.lovetropics.minigames.common.core.game.client_state.GameClientStateTypes;
import com.mojang.serialization.DataResult;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.FriendlyByteBuf;
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

	public void encode(FriendlyByteBuf buffer) {
		buffer.writeRegistryIdUnsafe(GameClientStateTypes.REGISTRY.get(), this.type);

		CompoundTag stateNbt = state != null ? tryEncodeUnchecked(type, state) : null;
		buffer.writeNbt(stateNbt);
	}

	public static SetGameClientStateMessage decode(FriendlyByteBuf buffer) {
		GameClientStateType<?> type = buffer.readRegistryIdUnsafe(GameClientStateTypes.REGISTRY.get());
		CompoundTag stateNbt = buffer.readNbt();
		GameClientState state = stateNbt != null ? tryDecode(type, stateNbt) : null;
		return new SetGameClientStateMessage(type, state);
	}

	@SuppressWarnings("unchecked")
	@Nullable
	private static <T extends GameClientState> CompoundTag tryEncodeUnchecked(GameClientStateType<T> type, GameClientState state) {
		DataResult<Tag> result = type.getCodec().encodeStart(NbtOps.INSTANCE, (T) state)
				.flatMap(nbt -> nbt instanceof CompoundTag ? DataResult.success(nbt) : DataResult.error("Encoded state was not a compound!"));

		result.error().ifPresent(error -> {
			LoveTropics.LOGGER.warn("Failed to encode client state of type '{}': {}", type.getRegistryName(), error);
		});

		return (CompoundTag) result.result().orElse(null);
	}

	@Nullable
	private static <T extends GameClientState> T tryDecode(GameClientStateType<T> type, CompoundTag nbt) {
		DataResult<T> result = type.getCodec().parse(NbtOps.INSTANCE, nbt);
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
