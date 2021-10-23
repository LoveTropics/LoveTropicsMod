package com.lovetropics.minigames.common.core.game.client_tweak;

import com.lovetropics.minigames.LoveTropics;
import com.lovetropics.minigames.common.core.game.state.GameStateKey;
import com.lovetropics.minigames.common.core.game.state.IGameState;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.DataResult;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.NBTDynamicOps;
import net.minecraft.network.PacketBuffer;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class GameClientTweakMap implements IGameState {
	public static final GameStateKey.Defaulted<GameClientTweakMap> STATE_KEY = GameStateKey.create("Game Client Tweaks", GameClientTweakMap::new);

	private final Map<GameClientTweakType<?>, GameClientTweak> map = new Reference2ObjectOpenHashMap<>();

	public static GameClientTweakMap empty() {
		return new GameClientTweakMap();
	}

	public <T extends GameClientTweak> void add(T tweak) {
		this.map.put(tweak.getType(), tweak);
	}

	@SuppressWarnings("unchecked")
	@Nullable
	public <T extends GameClientTweak> T getOrNull(GameClientTweakType<T> type) {
		return (T) this.map.get(type);
	}

	public void encode(PacketBuffer buffer) {
		List<Pair<GameClientTweakType<?>, CompoundNBT>> encoded = new ArrayList<>(this.map.size());
		for (Map.Entry<GameClientTweakType<?>, GameClientTweak> entry : this.map.entrySet()) {
			GameClientTweakType<?> type = entry.getKey();
			GameClientTweak tweak = entry.getValue();

			CompoundNBT nbt = tryEncodeUnchecked(type, tweak);
			if (nbt != null) {
				encoded.add(Pair.of(type, nbt));
			}
		}

		buffer.writeVarInt(encoded.size());

		for (Pair<GameClientTweakType<?>, CompoundNBT> pair : encoded) {
			buffer.writeRegistryIdUnsafe(GameClientTweakTypes.REGISTRY.get(), pair.getFirst());
			buffer.writeCompoundTag(pair.getSecond());
		}
	}

	public static GameClientTweakMap decode(PacketBuffer buffer) {
		GameClientTweakMap result = new GameClientTweakMap();

		int encodedCount = buffer.readVarInt();
		for (int i = 0; i < encodedCount; i++) {
			GameClientTweakType<?> type = buffer.readRegistryIdUnsafe(GameClientTweakTypes.REGISTRY.get());
			CompoundNBT nbt = buffer.readCompoundTag();
			if (type == null) {
				LoveTropics.LOGGER.warn("Encountered unknown tweak type reference with data: {}", nbt);
				continue;
			}

			GameClientTweak tweak = tryDecode(type, nbt);
			if (tweak != null) {
				result.add(tweak);
			}
		}

		return result;
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
}
