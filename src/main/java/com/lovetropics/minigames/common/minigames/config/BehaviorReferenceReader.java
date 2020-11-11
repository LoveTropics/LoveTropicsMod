package com.lovetropics.minigames.common.minigames.config;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.lovetropics.minigames.common.minigames.behaviours.IMinigameBehaviorType;
import com.lovetropics.minigames.common.minigames.behaviours.MinigameBehaviorTypes;
import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.types.JsonOps;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.minecraft.resources.IResource;
import net.minecraft.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nullable;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public final class BehaviorReferenceReader {
	private static final Logger LOGGER = LogManager.getLogger(BehaviorReferenceReader.class);
	private static final JsonParser PARSER = new JsonParser();

	private final IResourceManager resources;
	private final Set<ResourceLocation> currentlyReading = new ObjectOpenHashSet<>();

	public BehaviorReferenceReader(IResourceManager resources) {
		this.resources = resources;
	}

	public <T> List<BehaviorReference> readList(Dynamic<T> list) {
		return list.asList(this::read).stream()
				.filter(Objects::nonNull)
				.collect(Collectors.toList());
	}

	@Nullable
	public <T> BehaviorReference read(Dynamic<T> root) {
		ResourceLocation id = new ResourceLocation(root.get("type").asString(""));
		if (!this.currentlyReading.add(id)) {
			LOGGER.error("Tried to recursively add behavior of: {}!", id);
			return null;
		}

		try {
			// try parse as a statically registered behavior type
			IMinigameBehaviorType<?> type = MinigameBehaviorTypes.MINIGAME_BEHAVIOURS_REGISTRY.get().getValue(id);
			if (type != null) {
				return new BehaviorReference.Static(type, root);
			}

			// try parse as a reference to a behavior set json
			ResourceLocation path = new ResourceLocation(id.getNamespace(), "behaviors/" + id.getPath() + ".json");
			if (resources.hasResource(path)) {
				return this.readSetReference(path);
			}

			LOGGER.error("Invalid reference to behavior: {} - ignoring!", id);

			return null;
		} finally {
			this.currentlyReading.remove(id);
		}
	}

	private BehaviorReference readSetReference(ResourceLocation path) {
		try (IResource resource = resources.getResource(path)) {
			try (InputStream input = resource.getInputStream()) {
				JsonElement json = PARSER.parse(new BufferedReader(new InputStreamReader(input)));
				List<BehaviorReference> behaviors = this.readList(new Dynamic<>(JsonOps.INSTANCE, json));
				return new BehaviorReference.Set(behaviors);
			}
		} catch (IOException e) {
			LOGGER.error("Failed to load behavior set at {}", path);
			return null;
		}
	}
}
