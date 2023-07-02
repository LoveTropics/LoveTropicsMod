package com.lovetropics.minigames.common.core.game.config;

import com.google.common.collect.ImmutableList;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.lovetropics.minigames.common.core.game.behavior.GameBehaviorType;
import com.lovetropics.minigames.common.core.game.behavior.GameBehaviorTypes;
import com.mojang.datafixers.util.Pair;
import com.mojang.datafixers.util.Unit;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.MapLike;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import org.apache.commons.lang3.mutable.MutableObject;

import javax.annotation.Nullable;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Stream;

public final class BehaviorReferenceReader implements Codec<List<BehaviorReference>> {
	private final ResourceManager resources;
	private final Set<ResourceLocation> currentlyReading = new ObjectOpenHashSet<>();

	public BehaviorReferenceReader(ResourceManager resources) {
		this.resources = resources;
	}

	@Override
	public <T> DataResult<Pair<List<BehaviorReference>, T>> decode(DynamicOps<T> ops, T input) {
		return ops.getList(input).flatMap(stream -> {
			ImmutableList.Builder<BehaviorReference> references = ImmutableList.builder();
			Stream.Builder<T> errors = Stream.builder();

			MutableObject<DataResult<Unit>> result = new MutableObject<>(DataResult.success(Unit.INSTANCE));

			stream.accept(entry -> {
				this.decodeEntry(ops, entry, referenceResult -> {
					referenceResult.error().ifPresent(e -> errors.add(entry));
					result.setValue(result.getValue().apply2stable((u, r) -> {
						references.add(r);
						return u;
					}, referenceResult));
				});
			});

			Pair<List<BehaviorReference>, T> pair = Pair.of(references.build(), ops.createList(errors.build()));
			return result.getValue().map(unit -> pair).setPartial(pair);
		});
	}

	private <T> void decodeEntry(DynamicOps<T> ops, T input, Consumer<DataResult<BehaviorReference>> consumer) {
		Optional<MapLike<T>> mapInput = ops.getMap(input).result();
		if (mapInput.isPresent()) {
			this.decodeMap(ops, input, mapInput.get(), consumer);
			return;
		}

		Optional<String> stringInput = Codec.STRING.parse(ops, input).result();
		if (stringInput.isPresent()) {
			this.decodeReference(ops, stringInput.get(), ops.emptyMap(), consumer);
			return;
		}

		consumer.accept(DataResult.error(() -> "Behavior reference must be either a map or a string!"));
	}

	private <T> void decodeMap(DynamicOps<T> ops, T input, MapLike<T> mapInput, Consumer<DataResult<BehaviorReference>> consumer) {
		DataResult<String> typeResult = Codec.STRING.fieldOf("type").decode(ops, mapInput);

		typeResult.result().ifPresent(idString -> {
			this.decodeReference(ops, idString, ops.remove(input, "type"), consumer);
		});

		typeResult.error().ifPresent(error -> consumer.accept(DataResult.error(error::message)));
	}

	private <T> void decodeReference(DynamicOps<T> ops, String idString, T input, Consumer<DataResult<BehaviorReference>> consumer) {
		ResourceLocation id = new ResourceLocation(idString);
		if (!this.currentlyReading.add(id)) {
			consumer.accept(DataResult.error(() -> "Tried to recursively add behavior of " + id));
			return;
		}

		try {
			DataResult<BehaviorReference> staticResult = this.tryDecodeStatic(id, ops, input);
			if (staticResult != null) {
				consumer.accept(staticResult);
				return;
			}

			DataResult<List<BehaviorReference>> setResult = this.tryDecodeSet(id, ops, input);
			if (setResult != null) {
				setResult.result().ifPresent(references -> {
					for (BehaviorReference reference : references) {
						consumer.accept(DataResult.success(reference));
					}
				});

				setResult.error().ifPresent(error -> {
					consumer.accept(DataResult.error(error::message));
				});

				return;
			}

			consumer.accept(DataResult.error(() -> "Invalid reference to behavior: " + id));
		} finally {
			this.currentlyReading.remove(id);
		}
	}

	@Nullable
	private <T> DataResult<BehaviorReference> tryDecodeStatic(ResourceLocation id, DynamicOps<T> ops, T input) {
		GameBehaviorType<?> type = GameBehaviorTypes.REGISTRY.get().getValue(id);
		if (type != null) {
			return DataResult.success(new BehaviorReference(type, new Dynamic<>(ops, input)));
		}
		return null;
	}

	// TODO: Actually list these rather than doing Resource lookups. Replacing these BehaviorReferences with Holders (and converting these into a dynamic registry) will probably cover this.
	@Nullable
	private <T> DataResult<List<BehaviorReference>> tryDecodeSet(ResourceLocation id, DynamicOps<T> ops, T input) {
		ResourceLocation path = new ResourceLocation(id.getNamespace(), "behaviors/" + id.getPath() + ".json");
		Optional<Resource> resource = resources.getResource(path);
		if (resource.isEmpty()) {
			return null;
		}

		BehaviorParameterReplacer<JsonElement> parameterReplacer = BehaviorParameterReplacer.from(new Dynamic<>(ops, input));

		try (InputStream stream = resource.get().open()) {
			JsonElement json = JsonParser.parseReader(new BufferedReader(new InputStreamReader(stream)));
			Dynamic<JsonElement> data = new Dynamic<>(JsonOps.INSTANCE, json);

			data = parameterReplacer.apply(data);

			return this.parse(data);
		} catch (IOException e) {
			return DataResult.error(() -> "Failed to load behavior set at " + path);
		}
	}

	@Override
	public <T> DataResult<T> encode(List<BehaviorReference> input, DynamicOps<T> ops, T prefix) {
		return DataResult.error(() -> "Encoding unsupported");
	}
}
