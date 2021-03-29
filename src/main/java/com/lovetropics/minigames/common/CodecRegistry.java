package com.lovetropics.minigames.common;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Iterator;
import java.util.Set;

public final class CodecRegistry<K, V> implements Codec<V>, Iterable<V> {
    private final Codec<K> keyCodec;
    private final BiMap<K, V> map = HashBiMap.create();

    private CodecRegistry(Codec<K> keyCodec) {
        this.keyCodec = keyCodec;
    }

    public static <V> CodecRegistry<String, V> stringKeys() {
        return new CodecRegistry<>(Codec.STRING);
    }

    public static <V> CodecRegistry<ResourceLocation, V> resourceLocationKeys() {
        return new CodecRegistry<>(ResourceLocation.CODEC);
    }

    public void register(K key, V value) {
        this.map.put(key, value);
    }

    @Nullable
    public V get(K key) {
        return this.map.get(key);
    }

    @Nullable
    public K getKey(V value) {
        return this.map.inverse().get(value);
    }

    public boolean containsKey(K key) {
        return this.map.containsKey(key);
    }

    @Override
    public <U> DataResult<Pair<V, U>> decode(DynamicOps<U> ops, U input) {
        return this.keyCodec.decode(ops, input)
                .flatMap(pair -> {
                    if (!this.containsKey(pair.getFirst())) {
                        return DataResult.error("Unknown registry key: " + pair.getFirst());
                    }
                    return DataResult.success(pair.mapFirst(this::get));
                });
    }

    @Override
    public <U> DataResult<U> encode(V input, DynamicOps<U> ops, U prefix) {
        K key = this.getKey(input);
        if (key == null) {
            return DataResult.error("Unknown registry element " + input);
        }
        return this.keyCodec.encodeStart(ops, key).flatMap(keyData -> {
            return ops.mergeToPrimitive(prefix, keyData);
        });
    }

    public Set<K> keySet() {
        return this.map.keySet();
    }

    @Nonnull
    @Override
    public Iterator<V> iterator() {
        return this.map.values().iterator();
    }
}
