package com.lovetropics.minigames.client.lobby.state;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

import com.lovetropics.minigames.common.core.game.behavior.config.ConfigData;
import com.lovetropics.minigames.common.core.game.behavior.config.ConfigDataOps;
import com.lovetropics.minigames.common.core.game.behavior.config.ConfigList;
import com.mojang.serialization.Dynamic;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.NBTDynamicOps;
import net.minecraft.network.PacketBuffer;

public class ClientConfigList {
	
	public final Map<String, ConfigData> configs;
	
	public ClientConfigList(Map<String, ConfigData> configs) {
		this.configs = configs;
	}

	public static ClientConfigList from(ConfigList list) {
		return new ClientConfigList(
				list.keySet().stream().collect(Collectors.toMap(k -> k.getName(), list::getData, (u, v) -> {
					throw new IllegalStateException(String.format("Duplicate key %s", u));
				}, LinkedHashMap::new)));
	}
	
	public static ClientConfigList decode(PacketBuffer buffer) {
		Map<String, ConfigData> values = new LinkedHashMap<>();
		int size = buffer.readVarInt();
		for (int i = 0; i < size; i++) {
			values.put(buffer.readString(255), NBTDynamicOps.INSTANCE.convertTo(ConfigDataOps.INSTANCE, buffer.readCompoundTag().get("configs")));
		}
		return new ClientConfigList(values);
	}
	
	public void encode(PacketBuffer buffer) {
		buffer.writeVarInt(configs.size());
		for (Map.Entry<String, ConfigData> e : configs.entrySet()) {
			buffer.writeString(e.getKey(), 255);
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
