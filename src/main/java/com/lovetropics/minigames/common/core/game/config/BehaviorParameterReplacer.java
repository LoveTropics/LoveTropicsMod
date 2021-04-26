package com.lovetropics.minigames.common.core.game.config;

import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import org.apache.commons.lang3.mutable.MutableBoolean;
import org.apache.commons.lang3.mutable.MutableObject;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

final class BehaviorParameterReplacer<T> {
	private final Dynamic<?> source;

	private BehaviorParameterReplacer(Dynamic<?> source) {
		this.source = source;
	}

	public static <T> BehaviorParameterReplacer<T> from(Dynamic<?> source) {
		return new BehaviorParameterReplacer<>(source);
	}

	@Nonnull
	public Dynamic<T> apply(Dynamic<T> target) {
		Dynamic<T> result = this.replaceInAny(target);
		return result != null ? result : target;
	}

	@Nullable
	private Dynamic<T> replaceInAny(Dynamic<T> target) {
		Optional<String> string = target.asString().result();
		if (string.isPresent()) {
			String[] parameterRef = this.parseParameterRef(string.get());
			if (parameterRef != null) {
				Dynamic<T> parameter = this.lookupParameter(target.getOps(), parameterRef);
				if (parameter != null) {
					return parameter;
				}
			}
		}

		Optional<Map<Dynamic<T>, Dynamic<T>>> map = target.getMapValues().result();
		if (map.isPresent()) {
			return this.replaceInMap(target, map.get());
		}

		Optional<Stream<Dynamic<T>>> stream = target.asStreamOpt().result();
		if (stream.isPresent()) {
			return this.replaceInStream(target, stream.get());
		}

		return null;
	}

	@Nullable
	private Dynamic<T> replaceInMap(Dynamic<T> target, Map<Dynamic<T>, Dynamic<T>> targetMap) {
		MutableObject<Dynamic<T>> result = new MutableObject<>();

		for (Map.Entry<Dynamic<T>, Dynamic<T>> entry : targetMap.entrySet()) {
			entry.getKey().asString().result().ifPresent(key -> {
				Dynamic<T> replacedValue = this.replaceInAny(entry.getValue());
				if (replacedValue != null) {
					if (result.getValue() == null) {
						result.setValue(target);
					}
					result.setValue(result.getValue().set(key, replacedValue));
				}
			});
		}

		return result.getValue();
	}

	@Nullable
	private Dynamic<T> replaceInStream(Dynamic<T> target, Stream<Dynamic<T>> targetStream) {
		MutableBoolean replaced = new MutableBoolean();

		List<T> replacedList = targetStream.map(element -> {
			Dynamic<T> replacedElement = this.replaceInAny(element);
			if (replacedElement != null) {
				replaced.setTrue();
				return replacedElement.getValue();
			} else {
				return element.getValue();
			}
		}).collect(Collectors.toList());

		if (replaced.isTrue()) {
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
	@SuppressWarnings("unchecked")
	private <S> Dynamic<T> lookupParameter(DynamicOps<T> ops, String[] parameter) {
		Dynamic<S> source = (Dynamic<S>) this.source;
		for (String key : parameter) {
			Optional<Dynamic<S>> next = source.get(key).result();
			if (next.isPresent()) {
				source = next.get();
			} else {
				return null;
			}
		}

		if (source.getOps() == ops) {
			return (Dynamic<T>) source;
		} else {
			return source.castTyped(ops);
		}
	}
}
