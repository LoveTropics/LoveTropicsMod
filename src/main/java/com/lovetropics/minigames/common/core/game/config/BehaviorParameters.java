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

record BehaviorParameters(Dynamic<?> source) {
	public <T> Dynamic<T> substitute(Dynamic<T> target) {
		Dynamic<T> result = trySubstituteInAny(target);
		return Objects.requireNonNullElse(result, target);
	}

	@Nullable
	private <T> Dynamic<T> trySubstituteInAny(Dynamic<T> target) {
		Optional<String> string = target.asString().result();
		if (string.isPresent()) {
			String[] parameterRef = parseParameterRef(string.get());
			if (parameterRef != null) {
				return lookupParameter(source, target.getOps(), parameterRef);
			} else {
				return target;
			}
		}

		Optional<Map<Dynamic<T>, Dynamic<T>>> map = target.getMapValues().result();
		if (map.isPresent()) {
			return substituteInMap(target, map.get());
		}

		return target.asStreamOpt().result()
				.map(stream -> substituteInList(target, stream))
				.orElse(target);
	}

	private <T> Dynamic<T> substituteInMap(Dynamic<T> target, Map<Dynamic<T>, Dynamic<T>> targetMap) {
		Dynamic<T> result = target;

		for (Map.Entry<Dynamic<T>, Dynamic<T>> entry : targetMap.entrySet()) {
			Optional<String> key = entry.getKey().asString().result();
			if (key.isPresent()) {
				Dynamic<T> value = entry.getValue();
				Dynamic<T> substitutedValue = trySubstituteInAny(value);
				if (substitutedValue != null) {
					if (value != substitutedValue) {
						result = result.set(key.get(), substitutedValue);
					}
				} else {
					result = result.remove(key.get());
				}
			}
		}

		return result;
	}

	private <T> Dynamic<T> substituteInList(Dynamic<T> target, Stream<Dynamic<T>> targetStream) {
		MutableBoolean substituted = new MutableBoolean();

		List<T> replacedList = targetStream.map(element -> {
			Dynamic<T> substitutedElement = trySubstituteInAny(element);
			if (substitutedElement != null) {
				if (element != substitutedElement) {
					substituted.setTrue();
				}
				return substitutedElement.getValue();
			}
			return null;
		}).filter(Objects::nonNull).toList();

		if (substituted.isTrue()) {
			DynamicOps<T> ops = target.getOps();
			return new Dynamic<>(ops, ops.createList(replacedList.stream()));
		} else {
			return target;
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
