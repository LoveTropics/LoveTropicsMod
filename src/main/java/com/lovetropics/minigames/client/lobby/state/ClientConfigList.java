package com.lovetropics.minigames.client.lobby.state;

import com.lovetropics.minigames.common.core.game.behavior.config.BehaviorConfig;
import com.lovetropics.minigames.common.core.game.behavior.config.ConfigData;
import com.lovetropics.minigames.common.core.game.behavior.config.ConfigDataOps;
import com.lovetropics.minigames.common.core.game.behavior.config.ConfigList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public record ClientConfigList(ResourceLocation id, Map<BehaviorConfig<?>, ConfigData> configs) {
	public static ClientConfigList from(ConfigList list) {
		return new ClientConfigList(
				list.id(), list.keySet().stream().collect(Collectors.toMap(Function.identity(), list::getData, (u, v) -> {
			throw new IllegalStateException(String.format("Duplicate key %s", u));
		}, LinkedHashMap::new)));
	}

	public static ClientConfigList decode(FriendlyByteBuf buffer) {
		ResourceLocation id = buffer.readResourceLocation();
		Map<BehaviorConfig<?>, ConfigData> values = new LinkedHashMap<>();
		int size = buffer.readVarInt();
		for (int i = 0; i < size; i++) {
			values.put(BehaviorConfig.TEMP_REGISTRY.get(buffer.readUtf(255)), NbtOps.INSTANCE.convertTo(ConfigDataOps.INSTANCE, buffer.readNbt().get("configs")));
		}
		values.forEach((t, d) -> t.postProcess(d));
		return new ClientConfigList(id, values);
	}

	public void encode(FriendlyByteBuf buffer) {
		buffer.writeResourceLocation(id);
		buffer.writeVarInt(configs.size());
		for (Map.Entry<BehaviorConfig<?>, ConfigData> e : configs.entrySet()) {
			buffer.writeUtf(e.getKey().getName(), 255);
			CompoundTag tag = new CompoundTag();
			tag.put("configs", ConfigDataOps.INSTANCE.convertTo(NbtOps.INSTANCE, e.getValue()));
			buffer.writeNbt(tag);
		}
	}

	@Override
	public String toString() {
		return configs.toString();
	}
}
