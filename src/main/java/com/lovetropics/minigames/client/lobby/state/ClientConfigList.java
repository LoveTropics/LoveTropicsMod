package com.lovetropics.minigames.client.lobby.state;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.lovetropics.minigames.common.core.game.behavior.config.BehaviorConfig;
import com.lovetropics.minigames.common.core.game.behavior.config.ConfigData;
import com.lovetropics.minigames.common.core.game.behavior.config.ConfigDataOps;
import com.lovetropics.minigames.common.core.game.behavior.config.ConfigList;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.NBTDynamicOps;
import net.minecraft.network.PacketBuffer;

public class ClientConfigList {
	
	public final Map<BehaviorConfig<?>, ConfigData> configs;
	
	public ClientConfigList(Map<BehaviorConfig<?>, ConfigData> configs) {
		this.configs = configs;
	}

	public static ClientConfigList from(ConfigList list) {
		return new ClientConfigList(
				list.keySet().stream().collect(Collectors.toMap(Function.identity(), list::getData, (u, v) -> {
					throw new IllegalStateException(String.format("Duplicate key %s", u));
				}, LinkedHashMap::new)));
	}
	
	public static ClientConfigList decode(PacketBuffer buffer) {
		Map<BehaviorConfig<?>, ConfigData> values = new LinkedHashMap<>();
		int size = buffer.readVarInt();
		for (int i = 0; i < size; i++) {
			values.put(BehaviorConfig.TEMP_REGISTRY.get(buffer.readString(255)), NBTDynamicOps.INSTANCE.convertTo(ConfigDataOps.INSTANCE, buffer.readCompoundTag().get("configs")));
		}
		values.forEach((t, d) -> t.postProcess(d));
		return new ClientConfigList(values);
	}
	
	public void encode(PacketBuffer buffer) {
		buffer.writeVarInt(configs.size());
		for (Map.Entry<BehaviorConfig<?>, ConfigData> e : configs.entrySet()) {
			buffer.writeString(e.getKey().getName(), 255);
			CompoundNBT tag = new CompoundNBT();
			tag.put("configs", ConfigDataOps.INSTANCE.convertTo(NBTDynamicOps.INSTANCE, e.getValue()));
			buffer.writeCompoundTag(tag);
		}
	}

	@Override
	public String toString() {
		return configs.toString();
	}
}
