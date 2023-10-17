package com.lovetropics.minigames.common.util;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.util.Pair;
import com.mojang.datafixers.util.Unit;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.Lifecycle;
import com.mojang.serialization.MapLike;
import com.mojang.serialization.RecordBuilder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.Item;

import java.util.Map;
import java.util.function.Function;

public class Codecs {
    public static final Codec<HolderSet<Item>> ITEMS = RegistryCodecs.homogeneousList(Registries.ITEM);

    public static <K, V> Codec<Map<K, V>> mapValueBasedOnKey(Codec<K> keyCodec, Function<K, Codec<V>> valueCodec) {
        return new Codec<>() {
            @Override
            public <T> DataResult<Pair<Map<K, V>, T>> decode(final DynamicOps<T> ops, final T input) {
                return ops.getMap(input).setLifecycle(Lifecycle.stable()).flatMap(map -> decode(ops, map)).map(r -> Pair.of(r, input));
            }

            @Override
            public <T> DataResult<T> encode(final Map<K, V> input, final DynamicOps<T> ops, final T prefix) {
                return encode(input, ops, ops.mapBuilder()).build(prefix);
            }

            private <T> DataResult<Map<K, V>> decode(final DynamicOps<T> ops, final MapLike<T> input) {
                final ImmutableMap.Builder<K, V> read = ImmutableMap.builder();
                final ImmutableList.Builder<Pair<T, T>> failed = ImmutableList.builder();

                final DataResult<Unit> result = input.entries().reduce(
                        DataResult.success(Unit.INSTANCE, Lifecycle.stable()),
                        (r, pair) -> {
                            final DataResult<K> k = keyCodec.parse(ops, pair.getFirst());

                            final DataResult<Pair<K, V>> entry = k.flatMap(key ->
                                    valueCodec.apply(key).parse(ops, pair.getSecond()).map(value -> Pair.of(key, value)))
                                    .setLifecycle(Lifecycle.stable());
                            entry.error().ifPresent(e -> failed.add(pair));

                            return r.apply2stable((u, p) -> {
                                read.put(p.getFirst(), p.getSecond());
                                return u;
                            }, entry);
                        },
                        (r1, r2) -> r1.apply2stable((u1, u2) -> u1, r2)
                );

                final Map<K, V> elements = read.build();
                final T errors = ops.createMap(failed.build().stream());

                return result.map(unit -> elements).setPartial(elements).mapError(e -> e + " missed input: " + errors);
            }

            private <T> RecordBuilder<T> encode(final Map<K, V> input, final DynamicOps<T> ops, final RecordBuilder<T> prefix) {
                for (final Map.Entry<K, V> entry : input.entrySet()) {
                    prefix.add(keyCodec.encodeStart(ops, entry.getKey()), valueCodec.apply(entry.getKey()).encodeStart(ops, entry.getValue()));
                }
                return prefix;
            }

            @Override
            public String toString() {
                return "ValueBasedOnKeyMapCodec[" + keyCodec + " -> " + valueCodec + ']';
            }
        };
    }
}
