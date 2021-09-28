package com.lovetropics.minigames.common.core.game.behavior.config;

import com.lovetropics.minigames.common.core.game.behavior.config.ConfigData.CompositeConfigData;
import com.lovetropics.minigames.common.core.game.behavior.config.ConfigData.ListConfigData;
import com.mojang.serialization.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;

public final class BehaviorConfig<A> extends MapCodec<A> {
	private final String name;
	private final Map<String, UnaryOperator<ConfigData>> hints = new HashMap<>();
	private final Codec<A> codec;
	private final MapCodec<A> mapCodec;

	public BehaviorConfig(String name, Codec<A> codec) {
		this.name = name;
		this.codec = codec;
		this.mapCodec = codec.fieldOf(name);
	}

	public <E extends Enum<E>> BehaviorConfig<A> enumHint(String key, Function<String, E> fromName) {
		return hint(key, data -> new ConfigData.SimpleConfigData(ConfigType.ENUM, fromName.apply((String) data.value())));
	}
	
	private BehaviorConfig<A> hint(String key, UnaryOperator<ConfigData> hint) {
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
	
	public Codec<A> getCodec() {
		return codec;
	}

	@Override
	public String toString() {
		return name;
	}

	@Override
	public <T> Stream<T> keys(DynamicOps<T> ops) {
		return mapCodec.keys(ops);
	}

	@Override
	public <T> DataResult<A> decode(DynamicOps<T> ops, MapLike<T> input) {
		return mapCodec.decode(ops, input);
	}

	@Override
	public <T> RecordBuilder<T> encode(A input, DynamicOps<T> ops, RecordBuilder<T> prefix) {
		return mapCodec.encode(input, ops, prefix);
	}
}
