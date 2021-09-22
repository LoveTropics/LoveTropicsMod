package com.lovetropics.minigames.common.core.game.behavior.configold;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class ConfigList {

	private final Map<Config<?>, Configurable<?>> configs;

	private ConfigList(Map<Config<?>, Configurable<?>> configs) {
		this.configs = configs;
	}
	
	@SuppressWarnings("unchecked")
	public <T> Configurable<T> get(Config<T> key) {
		return (Configurable<T>) configs.get(key);
	}

	private static final ConfigList empty = new ConfigList(Collections.emptyMap());
	public static ConfigList empty() {
		return empty;
	}

	public static Builder builder() {
		return new Builder();
	}

	public static class Builder {
		
		private final Map<Config<?>, Configurable<?>> configs = new HashMap<>();
		
		Builder() {}
		
		public <T> Builder with(Config<T> key, Configurable<T> config) {
			configs.put(key, config);
			return this;
		}
		
		public ConfigList build() {
			return new ConfigList(configs);
		}
	}
}
