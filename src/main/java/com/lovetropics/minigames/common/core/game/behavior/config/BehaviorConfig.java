package com.lovetropics.minigames.common.core.game.behavior.config;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.UnaryOperator;

import com.lovetropics.minigames.common.core.game.behavior.config.ConfigData.CompositeConfigData;
import com.lovetropics.minigames.common.core.game.behavior.config.ConfigData.ListConfigData;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public final class BehaviorConfig<T> {

	private final String name;
	private final Codec<T> codec;
	private final Map<String, UnaryOperator<ConfigData>> hints = new HashMap<>();

	public BehaviorConfig(String name, Codec<T> codec) {
		this.name = name;
		this.codec = codec;
	}

	public <E extends Enum<E>> BehaviorConfig<T> enumHint(String key, Function<String, E> fromName) {
		return hint(key, data -> new ConfigData.SimpleConfigData(ConfigType.ENUM, fromName.apply((String) data.value())));
	}
	
	private BehaviorConfig<T> hint(String key, UnaryOperator<ConfigData> hint) {
		this.hints.put(key, hint);
		return this;
	}

	final ConfigData postProcess(ConfigData data) {
		return postProcessRecursive("", data);
	}

	private final ConfigData postProcessRecursive(String path, ConfigData data) {
		if (data instanceof CompositeConfigData) {
			for (Map.Entry<String, ConfigData> e : ((CompositeConfigData) data).value().entrySet()) {
				e.setValue(postProcessRecursive(path.isEmpty() ? e.getKey() : "." + e.getKey(), e.getValue()));
			}
		} else if (data instanceof ListConfigData) {
			ListConfigData list = (ListConfigData) data;
			if (list.componentType() == ConfigType.COMPOSITE) {
				List<Object> values = list.value();
				for (int i = 0; i < values.size(); i++) {
					values.set(i, postProcessRecursive(path, (ConfigData) values.get(i)));
				}
			}
		}

		UnaryOperator<ConfigData> hint = this.hints.get(path);
		if (hint != null) {
			return hint.apply(data);
		}
		return data;
	}
	
	public String getName() {
		return name;
	}
	
	public Codec<T> getCodec() {
		return codec;
	}

	public MapCodec<T> fieldOf() {
		return codec.fieldOf(getName());
	}
	
	public MapCodec<T> optionalFieldOf(T def) {
		return codec.optionalFieldOf(getName(), def);
	}

	public <O> RecordCodecBuilder<O, T> forGetter(Function<O, T> getter) {
		return fieldOf().forGetter(getter);
	}
	
	public <O> RecordCodecBuilder<O, T> forGetterOptional(Function<O, T> getter, T def) {
		return optionalFieldOf(def).forGetter(getter);
	}

	@Override
	public String toString() {
		return name;
	}
}
