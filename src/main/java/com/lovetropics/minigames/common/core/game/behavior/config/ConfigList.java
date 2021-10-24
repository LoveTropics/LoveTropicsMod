package com.lovetropics.minigames.common.core.game.behavior.config;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.DataResult;

import java.util.*;

public class ConfigList {

	private final Map<BehaviorConfig<?>, ConfigData> configs;

	private ConfigList(Map<BehaviorConfig<?>, ConfigData> configs) {
		this.configs = configs;
	}

	public ConfigData getData(BehaviorConfig<?> key) {
		return configs.get(key);
	}
	
	public Set<BehaviorConfig<?>> keySet() {
		return Collections.unmodifiableSet(configs.keySet());
	}

	public <T> DataResult<Pair<T, ConfigData>> decode(BehaviorConfig<T> key) {
		return key.getCodec().decode(ConfigDataOps.INSTANCE, getData(key));
	}
	
	@Override
	public String toString() {
		return "ConfigList" + Objects.toString(configs);
	}

	private static final ConfigList empty = new ConfigList(Collections.emptyMap());

	public static ConfigList empty() {
		return empty;
	}

	public static Builder builder() {
		return new Builder();
	}

	public static class Builder {

		private final Map<BehaviorConfig<?>, ConfigData> configs = new LinkedHashMap<>();

		Builder() {
		}

		public <T> Builder with(BehaviorConfig<T> key, T defVal) {
			ConfigData data = key.getCodec().encodeStart(ConfigDataOps.INSTANCE, defVal).getOrThrow(false, s -> { throw new IllegalArgumentException(s); });
			this.configs.put(key, data);
			return this;
		}

		public ConfigList build() {
			return new ConfigList(configs);
		}
	}
}
