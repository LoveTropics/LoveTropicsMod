package com.lovetropics.minigames.common.util;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.MapLike;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class DynamicTemplate {
	private final Node node;
	private final Set<ParameterPath> parameters;

	private DynamicTemplate(final Node node, Set<ParameterPath> parameters) {
		this.node = node;
		this.parameters = parameters;
	}

	public static <T> DynamicTemplate parse(final Dynamic<T> template) {
		final Node node = parseNode(template.getOps(), template.getValue());
		final Set<ParameterPath> parameters = node.parameters().collect(Collectors.toSet());
		return new DynamicTemplate(node, parameters);
	}

	private static <T> Node parseNode(final DynamicOps<T> ops, final T value) {
		final Optional<ParameterPath> reference = ops.getStringValue(value).result().map(DynamicTemplate::parseParameterReference);
		if (reference.isPresent()) {
			return new Substituted(reference.get());
		}

		final Static<T> staticNode = new Static<>(new Dynamic<>(ops, value));

		final Optional<MapLike<T>> map = ops.getMap(value).result();
		if (map.isPresent()) {
			final MapNode<T> node = new MapNode<>(ops, map.get().entries().collect(Collectors.toMap(
					Pair::getFirst,
					entry -> parseNode(ops, entry.getSecond())
			)));
			return node.hasParameters() ? node : staticNode;
		}

		final Optional<Stream<T>> list = ops.getStream(value).result();
		if (list.isPresent()) {
			final ListNode node = new ListNode(list.get().map(entry -> parseNode(ops, entry)).toList());
			return node.hasParameters() ? node : staticNode;
		}

		return staticNode;
	}

	@Nullable
	private static ParameterPath parseParameterReference(String key) {
		if (key.startsWith("$")) {
			return new ParameterPath(key.substring(1).split("\\."));
		} else {
			return null;
		}
	}

	public <U> Dynamic<U> substitute(final DynamicOps<U> ops, final MapLike<U> parameters) {
		final U result = node.substitute(ops, path -> resolveParameter(ops, parameters, path));
		return new Dynamic<>(ops, Objects.requireNonNullElseGet(result, ops::emptyMap));
	}

	@Nullable
	private static <T> T resolveParameter(final DynamicOps<T> ops, final MapLike<T> root, final ParameterPath path) {
		if (path.segments.length == 0) {
			return null;
		}
		T value = null;
		MapLike<T> map = root;
		for (final String key : path.segments) {
			if (map == null) {
				return null;
			}
			value = map.get(key);
			map = value != null ? ops.getMap(value).result().orElse(null) : null;
		}
		return value;
	}

	public Set<ParameterPath> parameters() {
		return parameters;
	}

	private sealed interface Node permits MapNode, ListNode, Substituted, Static {
		@Nullable
		<U> U substitute(DynamicOps<U> ops, Function<ParameterPath, U> resolver);

		Stream<ParameterPath> parameters();

		default boolean hasParameters() {
			return parameters().findAny().isPresent();
		}
	}

	private record MapNode<T>(DynamicOps<T> ops, Map<T, Node> nodes) implements Node {
		@Override
		public <U> U substitute(final DynamicOps<U> ops, final Function<ParameterPath, U> resolver) {
			return ops.createMap(nodes.entrySet().stream()
					.map(entry -> {
						final U substituted = entry.getValue().substitute(ops, resolver);
						if (substituted != null) {
							final U key = Dynamic.convert(this.ops, ops, entry.getKey());
							return Map.entry(key, substituted);
						}
						return null;
					})
					.filter(Objects::nonNull)
					.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue))
			);
		}

		@Override
		public Stream<ParameterPath> parameters() {
			return nodes.values().stream().flatMap(Node::parameters);
		}
	}

	private record ListNode(List<Node> nodes) implements Node {
		@Override
		public <U> U substitute(final DynamicOps<U> ops, final Function<ParameterPath, U> resolver) {
			return ops.createList(nodes.stream().map(node -> node.substitute(ops, resolver)).filter(Objects::nonNull));
		}

		@Override
		public Stream<ParameterPath> parameters() {
			return nodes.stream().flatMap(Node::parameters);
		}
	}

	private record Substituted(ParameterPath path) implements Node {
		@Override
		@Nullable
		public <U> U substitute(final DynamicOps<U> ops, final Function<ParameterPath, U> resolver) {
			return resolver.apply(path);
		}

		@Override
		public Stream<ParameterPath> parameters() {
			return Stream.of(path);
		}
	}

	private record Static<T>(Dynamic<T> dynamic) implements Node {
		@Override
		public <U> U substitute(final DynamicOps<U> ops, final Function<ParameterPath, U> resolver) {
			return dynamic.convert(ops).getValue();
		}

		@Override
		public Stream<ParameterPath> parameters() {
			return Stream.empty();
		}
	}

	public record ParameterPath(String[] segments) {
		@Override
		public boolean equals(Object obj) {
			return obj instanceof final ParameterPath parameter && Arrays.equals(segments, parameter.segments);
		}

		@Override
		public int hashCode() {
			return Arrays.hashCode(segments);
		}

		@Override
		public String toString() {
			return String.join(".", segments);
		}
	}
}
