package com.lovetropics.minigames.common.core.game.config;

import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import org.apache.commons.lang3.mutable.MutableBoolean;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

record BehaviorParameters<T>(Dynamic<?> source) {
	@Nonnull
	public Dynamic<T> substitute(Dynamic<T> target) {
		Dynamic<T> result = trySubstituteInAny(target);
		return Objects.requireNonNullElse(result, target);
	}

	@Nullable
	private Dynamic<T> trySubstituteInAny(Dynamic<T> target) {
		Optional<String> string = target.asString().result();
		if (string.isPresent()) {
			String[] parameterRef = parseParameterRef(string.get());
			if (parameterRef != null) {
				return lookupParameter(source, target.getOps(), parameterRef);
			} else {
				return null;
			}
		}

		Optional<Map<Dynamic<T>, Dynamic<T>>> map = target.getMapValues().result();
		if (map.isPresent()) {
			return substituteInMap(target, map.get());
		}

		return target.asStreamOpt().result()
				.map(stream -> substituteInList(target, stream))
				.orElse(null);
	}

	@Nullable
	private Dynamic<T> substituteInMap(Dynamic<T> target, Map<Dynamic<T>, Dynamic<T>> targetMap) {
		Dynamic<T> result = null;

		for (Map.Entry<Dynamic<T>, Dynamic<T>> entry : targetMap.entrySet()) {
			Optional<String> key = entry.getKey().asString().result();
			if (key.isPresent()) {
				Dynamic<T> substitutedValue = trySubstituteInAny(entry.getValue());
				if (substitutedValue != null) {
					if (result == null) {
						result = target;
					}
					result = result.set(key.get(), substitutedValue);
				}
			}
		}

		return result;
	}

	@Nullable
	private Dynamic<T> substituteInList(Dynamic<T> target, Stream<Dynamic<T>> targetStream) {
		MutableBoolean substituted = new MutableBoolean();

		List<T> replacedList = targetStream.map(element -> {
			Dynamic<T> substitutedElement = trySubstituteInAny(element);
			if (substitutedElement != null) {
				substituted.setTrue();
				return substitutedElement.getValue();
			} else {
				return element.getValue();
			}
		}).toList();

		if (substituted.isTrue()) {
			DynamicOps<T> ops = target.getOps();
			return new Dynamic<>(ops, ops.createList(replacedList.stream()));
		} else {
			return null;
		}
	}

	@Nullable
	private String[] parseParameterRef(String key) {
		if (key.startsWith("$")) {
			return key.substring(1).split("\\.");
		} else {
			return null;
		}
	}

	@Nullable
	private static <S, T> Dynamic<T> lookupParameter(Dynamic<S> source, DynamicOps<T> ops, String[] parameter) {
		for (String key : parameter) {
			Optional<Dynamic<S>> next = source.get(key).result();
			if (next.isPresent()) {
				source = next.get();
			} else {
				return null;
			}
		}
		return source.convert(ops);
	}
}
