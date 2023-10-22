package com.lovetropics.minigames.common.core.game.util;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import java.util.Map;
import java.util.function.BiConsumer;

public class TranslationCollector {
	private final String prefix;
	private final Map<String, String> values = new Object2ObjectOpenHashMap<>();

	public TranslationCollector(String prefix) {
		this.prefix = prefix;
	}

	public Component add(String key, String value) {
		put(key, value);
		return Component.translatable(key);
	}

	public Fun1 add1(String key, String value) {
		put(key, value);
		return arg -> Component.translatable(key, arg);
	}

	public Fun2 add2(String key, String value) {
		put(key, value);
		return (a, b) -> Component.translatable(key, a, b);
	}

	public Fun3 add3(String key, String value) {
		put(key, value);
		return (a, b, c) -> Component.translatable(key, a, b, c);
	}

	private void put(String key, String value) {
		key = prefix + key;
		if (values.putIfAbsent(key, value) != null) {
			throw new IllegalArgumentException("Duplicate translation for key: " + key);
		}
	}

	public void forEach(BiConsumer<String, String> consumer) {
		values.forEach(consumer);
	}

	public interface Fun1 {
		MutableComponent apply(Object arg);
	}

	public interface Fun2 {
		MutableComponent apply(Object a, Object b);
	}

	public interface Fun3 {
		MutableComponent apply(Object a, Object b, Object c);
	}
}
