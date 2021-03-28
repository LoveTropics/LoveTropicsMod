package com.lovetropics.minigames.common;

import com.google.gson.JsonElement;
import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.*;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.*;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.DyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.NBTDynamicOps;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.GameType;
import net.minecraft.world.gen.blockstateprovider.BlockStateProvider;
import net.minecraft.world.gen.blockstateprovider.SimpleBlockStateProvider;

import java.util.*;
import java.util.function.Function;
import java.util.function.IntFunction;

public final class MoreCodecs {
	public static final Codec<ItemStack> ITEM_STACK = Codec.either(ItemStack.CODEC, Registry.ITEM)
			.xmap(either -> either.map(Function.identity(), ItemStack::new), Either::left);

	public static final Codec<BlockState> BLOCK_STATE = Codec.either(BlockState.CODEC, Registry.BLOCK)
			.xmap(either -> either.map(Function.identity(), Block::getDefaultState), Either::left);

	public static final Codec<BlockStateProvider> BLOCK_STATE_PROVIDER = Codec.either(BlockStateProvider.CODEC, BLOCK_STATE)
			.xmap(either -> either.map(Function.identity(), SimpleBlockStateProvider::new), Either::left);

	public static final Codec<ITextComponent> TEXT = withJson(
			ITextComponent.Serializer::toJsonTree,
			json -> {
				ITextComponent text = ITextComponent.Serializer.getComponentFromJson(json);
				return text != null ? DataResult.success(text) : DataResult.error("Malformed text");
			}
	);

	public static final Codec<DyeColor> DYE_COLOR = stringVariants(DyeColor.values(), DyeColor::getString);

	public static final Codec<EquipmentSlotType> EQUIPMENT_SLOT = stringVariants(EquipmentSlotType.values(), EquipmentSlotType::getName);

	public static final Codec<TextFormatting> FORMATTING = stringVariants(TextFormatting.values(), TextFormatting::getFriendlyName);

	public static final Codec<GameType> GAME_TYPE = stringVariants(GameType.values(), GameType::getName);

	public static final Codec<UUID> UUID_STRING = Codec.STRING.comapFlatMap(
			string -> {
				try {
					return DataResult.success(UUID.fromString(string));
				} catch (IllegalArgumentException e) {
					return DataResult.error("Malformed UUID!");
				}
			},
			UUID::toString
	);

	public static <T> MapCodec<T> nullableFieldOf(Codec<T> codec, String name) {
		return codec.optionalFieldOf(name).xmap(opt -> opt.orElse(null), Optional::ofNullable);
	}

	public static <T> Codec<T[]> arrayOrUnit(Codec<T> codec, IntFunction<T[]> factory) {
		return listToArray(listOrUnit(codec), factory);
	}

	public static <T> Codec<List<T>> listOrUnit(Codec<T> codec) {
		return Codec.either(codec.listOf(), codec)
				.xmap(
						either -> either.map(Function.identity(), MoreCodecs::unitArrayList),
						list -> list.size() == 1 ? Either.right(list.get(0)) : Either.left(list)
				);
	}

	public static <T> Codec<T[]> listToArray(Codec<List<T>> codec, IntFunction<T[]> factory) {
		return codec.xmap(list -> list.toArray(factory.apply(0)), Arrays::asList);
	}

	public static <A> Codec<A> stringVariants(A[] values, Function<A, String> asName) {
		return keyedVariants(values, asName, Codec.STRING);
	}

	public static <A, K> Codec<A> keyedVariants(A[] values, Function<A, K> asKey, Codec<K> keyCodec) {
		Map<K, A> byKey = new Object2ObjectOpenHashMap<>();
		for (A value : values) {
			byKey.put(asKey.apply(value), value);
		}

		return keyCodec.comapFlatMap(key -> {
			A value = byKey.get(key);
			return value != null ? DataResult.success(value) : DataResult.error("No variant with key '" + key + "'");
		}, asKey);
	}

	public static <A> Codec<A> withJson(Function<A, JsonElement> encode, Function<JsonElement, DataResult<A>> decode) {
		return withOps(JsonOps.INSTANCE, encode, decode);
	}

	public static <A> Codec<A> withNbt(Function<A, INBT> encode, Function<INBT, DataResult<A>> decode) {
		return withOps(NBTDynamicOps.INSTANCE, encode, decode);
	}

	public static <A, T> Codec<A> withOps(DynamicOps<T> ops, Function<A, T> encode, Function<T, DataResult<A>> decode) {
		return new MappedOpsCodec<>(ops, encode, decode);
	}

	public static <N extends Number> Codec<N> numberAsString(Function<String, N> parse) {
		return Codec.STRING.comapFlatMap(
				s -> {
					try {
						return DataResult.success(parse.apply(s));
					} catch (NumberFormatException e) {
						return DataResult.error("Failed to parse number '" + s + "'");
					}
				},
				Object::toString
		);
	}

	public static <V> Codec<Long2ObjectMap<V>> long2Object(Codec<V> codec) {
		return Codec.unboundedMap(numberAsString(Long::parseLong), codec).xmap(Long2ObjectOpenHashMap::new, HashMap::new);
	}

	public static <K> Codec<Object2FloatMap<K>> object2Float(Codec<K> codec) {
		return Codec.unboundedMap(codec, Codec.FLOAT).xmap(Object2FloatOpenHashMap::new, HashMap::new);
	}

	public static <K> Codec<Object2DoubleMap<K>> object2Double(Codec<K> codec) {
		return Codec.unboundedMap(codec, Codec.DOUBLE).xmap(Object2DoubleOpenHashMap::new, HashMap::new);
	}

	private static <T> List<T> unitArrayList(T t) {
		List<T> list = new ArrayList<>(1);
		list.add(t);
		return list;
	}

	static final class MappedOpsCodec<A, S> implements Codec<A> {
		private final DynamicOps<S> sourceOps;
		private final Function<A, S> encode;
		private final Function<S, DataResult<A>> decode;

		MappedOpsCodec(DynamicOps<S> sourceOps, Function<A, S> encode, Function<S, DataResult<A>> decode) {
			this.sourceOps = sourceOps;
			this.encode = encode;
			this.decode = decode;
		}

		@Override
		@SuppressWarnings("unchecked")
		public <T> DataResult<T> encode(A input, DynamicOps<T> ops, T prefix) {
			S sourceData = this.encode.apply(input);
			T targetData = ops == this.sourceOps ? (T) sourceData : this.sourceOps.convertTo(ops, sourceData);
			return ops.getMap(targetData).flatMap(map -> {
				return ops.mergeToMap(prefix, map);
			});
		}

		@Override
		@SuppressWarnings("unchecked")
		public <T> DataResult<Pair<A, T>> decode(DynamicOps<T> ops, T input) {
			S sourceData = ops == this.sourceOps ? (S) input : ops.convertTo(this.sourceOps, input);
			return this.decode.apply(sourceData).map(output -> Pair.of(output, input));
		}
	}
}
