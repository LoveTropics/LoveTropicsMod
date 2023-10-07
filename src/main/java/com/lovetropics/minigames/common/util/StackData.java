package com.lovetropics.minigames.common.util;

import com.lovetropics.minigames.LoveTropics;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import net.minecraft.Util;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.Map;
import java.util.Optional;
import java.util.function.UnaryOperator;

public record StackData<T>(String id, Codec<T> codec) {
    public StackData(ResourceLocation id, Codec<T> codec) {
        this(id.toString(), codec);
    }

    public static <K, V> StackData<Map<K, V>> unboundedMap(String id, Codec<K> keyCodec, Codec<V> valueCodec) {
        return new StackData<>(new ResourceLocation("ltminigames", id), Codec.unboundedMap(keyCodec, valueCodec));
    }

    public Optional<DataResult<T>> get(ItemStack stack) {
        if (stack.getTag() == null) {
            return Optional.empty();
        }
        final Tag sub = stack.getTag().get(id);
        if (sub == null) {
            return Optional.empty();
        }
        return Optional.of(codec.decode(NbtOps.INSTANCE, sub).map(Pair::getFirst));
    }

    public Optional<T> getIfSuccessful(ItemStack stack) {
        return get(stack).flatMap(DataResult::result);
    }

    public void set(ItemStack stack, T value) {
        stack.getOrCreateTag().put(id, Util.getOrThrow(codec.encodeStart(NbtOps.INSTANCE, value), IllegalArgumentException::new));
    }

    public void modify(ItemStack stack, UnaryOperator<T> modifier) {
        getIfSuccessful(stack).ifPresent(value -> set(stack, modifier.apply(value)));
    }
}
