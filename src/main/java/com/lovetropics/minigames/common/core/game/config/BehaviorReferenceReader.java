package com.lovetropics.minigames.common.core.game.config;

import com.google.common.collect.ImmutableList;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.lovetropics.minigames.common.core.game.behavior.GameBehaviorType;
import com.lovetropics.minigames.common.core.game.behavior.GameBehaviorTypes;
import com.mojang.datafixers.util.Pair;
import com.mojang.datafixers.util.Unit;
import com.mojang.serialization.*;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.resources.ResourceLocation;
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
	private static final JsonParser PARSER = new JsonParser();

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

	private <T> void decodeEntry(DynamicOps<T> ops, T input, Consumer<DataResult<BehaviorReference>> yield) {
		Optional<MapLike<T>> mapInput = ops.getMap(input).result();
		if (mapInput.isPresent()) {
			this.decodeMap(ops, input, mapInput.get(), yield);
			return;
		}

		Optional<String> stringInput = Codec.STRING.parse(ops, input).result();
		if (stringInput.isPresent()) {
			this.decodeReference(ops, stringInput.get(), ops.emptyMap(), yield);
			return;
		}

		yield.accept(DataResult.error("Behavior reference must be either a map or a string!"));
	}

	private <T> void decodeMap(DynamicOps<T> ops, T input, MapLike<T> mapInput, Consumer<DataResult<BehaviorReference>> yield) {
		DataResult<String> typeResult = Codec.STRING.fieldOf("type").decode(ops, mapInput);

		typeResult.result().ifPresent(idString -> {
			this.decodeReference(ops, idString, input, yield);
		});

		typeResult.error().ifPresent(error -> {
			yield.accept(DataResult.error(error.message()));
		});
	}

	private <T> void decodeReference(DynamicOps<T> ops, String idString, T input, Consumer<DataResult<BehaviorReference>> yield) {
		ResourceLocation id = new ResourceLocation(idString);
		if (!this.currentlyReading.add(id)) {
			yield.accept(DataResult.error("Tried to recursively add behavior of " + id));
			return;
		}

		try {
			DataResult<BehaviorReference> staticResult = this.tryDecodeStatic(id, ops, input);
			if (staticResult != null) {
				yield.accept(staticResult);
				return;
			}

			DataResult<List<BehaviorReference>> setResult = this.tryDecodeSet(id, ops, input);
			if (setResult != null) {
				setResult.result().ifPresent(references -> {
					for (BehaviorReference reference : references) {
						yield.accept(DataResult.success(reference));
					}
				});

				setResult.error().ifPresent(error -> {
					yield.accept(DataResult.error(error.message()));
				});

				return;
			}

			yield.accept(DataResult.error("Invalid reference to behavior: " + id));
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

	@Nullable
	private <T> DataResult<List<BehaviorReference>> tryDecodeSet(ResourceLocation id, DynamicOps<T> ops, T input) {
		ResourceLocation path = new ResourceLocation(id.getNamespace(), "behaviors/" + id.getPath() + ".json");
		if (!resources.hasResource(path)) {
			return null;
		}

		BehaviorParameterReplacer<JsonElement> parameterReplacer = BehaviorParameterReplacer.from(new Dynamic<>(ops, input));

		try (
				Resource resource = resources.getResource(path);
				InputStream stream = resource.getInputStream()
		) {
			JsonElement json = PARSER.parse(new BufferedReader(new InputStreamReader(stream)));
			Dynamic<JsonElement> data = new Dynamic<>(JsonOps.INSTANCE, json);

			data = parameterReplacer.apply(data);

			return this.parse(data);
		} catch (IOException e) {
			return DataResult.error("Failed to load behavior set at " + path);
		}
	}

	@Override
	public <T> DataResult<T> encode(List<BehaviorReference> input, DynamicOps<T> ops, T prefix) {
		return DataResult.error("Encoding unsupported");
	}
}
