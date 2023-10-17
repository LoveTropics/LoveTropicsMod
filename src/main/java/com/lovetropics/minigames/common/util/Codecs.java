package com.lovetropics.minigames.common.util;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.MapLike;
import com.mojang.serialization.RecordBuilder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.Item;

import java.util.stream.Stream;

public class Codecs {
    public static final Codec<HolderSet<Item>> ITEMS = RegistryCodecs.homogeneousList(Registries.ITEM);

    // For debugging
    public static <A> Codec<A> hook(final Codec<A> codec) {
        return new Codec<>() {
			@Override
			public <T> DataResult<Pair<A, T>> decode(DynamicOps<T> ops, T input) {
				return codec.decode(ops, input);
			}

			@Override
			public <T> DataResult<T> encode(A input, DynamicOps<T> ops, T prefix) {
				return codec.encode(input, ops, prefix);
			}
		};
    }
}
