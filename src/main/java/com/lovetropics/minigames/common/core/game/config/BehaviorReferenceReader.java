package com.lovetropics.minigames.common.core.game.config;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.lovetropics.minigames.common.core.game.behavior.GameBehaviorType;
import com.lovetropics.minigames.common.core.game.behavior.GameBehaviorTypes;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.*;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.minecraft.resources.IResource;
import net.minecraft.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Set;

public final class BehaviorReferenceReader implements Codec<BehaviorReference> {
	private static final JsonParser PARSER = new JsonParser();

	private final IResourceManager resources;
	private final Set<ResourceLocation> currentlyReading = new ObjectOpenHashSet<>();

	public BehaviorReferenceReader(IResourceManager resources) {
		this.resources = resources;
	}

	@Override
	public <T> DataResult<Pair<BehaviorReference, T>> decode(DynamicOps<T> ops, T input) {
		return ops.getMap(input).flatMap(mapInput -> {
			return Codec.STRING.fieldOf("type").decode(ops, mapInput).flatMap(idString -> {
				ResourceLocation id = new ResourceLocation(idString);
				if (!this.currentlyReading.add(id)) {
					return DataResult.error("Tried to recursively add behavior of" + id);
				}

				try {
					// try parse as a statically registered behavior type
					GameBehaviorType<?> type = GameBehaviorTypes.REGISTRY.get().getValue(id);
					if (type != null) {
						BehaviorReference reference = new BehaviorReference.Static(type, new Dynamic<>(ops, input));
						return DataResult.success(reference);
					}

					// try parse as a reference to a behavior set json
					ResourceLocation path = new ResourceLocation(id.getNamespace(), "behaviors/" + id.getPath() + ".json");
					if (resources.hasResource(path)) {
						return this.readSetReference(path);
					} else {
						return DataResult.error("Invalid reference to behavior: " + id);
					}
				} finally {
					this.currentlyReading.remove(id);
				}
			});
		}).map(reference -> Pair.of(reference, input));
	}

	@Override
	public <T> DataResult<T> encode(BehaviorReference input, DynamicOps<T> ops, T prefix) {
		return DataResult.error("Encoding unsupported");
	}

	private DataResult<BehaviorReference> readSetReference(ResourceLocation path) {
		try (IResource resource = resources.getResource(path)) {
			try (InputStream input = resource.getInputStream()) {
				JsonElement json = PARSER.parse(new BufferedReader(new InputStreamReader(input)));
				return this.listOf().parse(JsonOps.INSTANCE, json).map(BehaviorReference.Set::new);
			}
		} catch (IOException e) {
			return DataResult.error("Failed to load behavior set at " + path);
		}
	}
}
