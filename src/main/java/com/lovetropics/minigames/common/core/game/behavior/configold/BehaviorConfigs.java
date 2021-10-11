package com.lovetropics.minigames.common.core.game.behavior.configold;

import com.lovetropics.minigames.common.core.game.state.team.TeamKey;
import com.mojang.serialization.Codec;

import java.util.HashMap;
import java.util.Map;

public class BehaviorConfigs {

	private static final Map<Class<?>, ConfigType<?>> types = new HashMap<>();

	public static final ConfigType<TeamKey> TEAM = register(TeamKey.class, TeamKey.CODEC);

	private static <T> ConfigType<T> register(Class<? super T> cls, Codec<T> codec) {
		ConfigType<T> type = () -> codec;
		types.put(cls, type);
		return type;
	}

	@SuppressWarnings("unchecked")
	static <T> Codec<T> decode(Class<? super T> configType) {
		return (Codec<T>) types.get(configType).codec();
	}

	/*public static <T> Codec<T> orConfig(Codec<T> source, Class<? super T> configType) {
		return MoreCodecs.tryFirst(decode(configType), source);
	}*/
}
