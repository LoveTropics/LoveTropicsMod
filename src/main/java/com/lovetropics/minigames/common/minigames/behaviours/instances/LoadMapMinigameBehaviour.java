package com.lovetropics.minigames.common.minigames.behaviours.instances;

import com.lovetropics.minigames.common.map.MapExportReader;
import com.lovetropics.minigames.common.map.MapMetadata;
import com.lovetropics.minigames.common.minigames.IMinigameDefinition;
import com.lovetropics.minigames.common.minigames.IMinigameInstance;
import com.lovetropics.minigames.common.minigames.behaviours.IMinigameBehavior;
import net.minecraft.resources.IResource;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.DimensionManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;

public class LoadMapMinigameBehaviour implements IMinigameBehavior {
	private static final Logger LOGGER = LogManager.getLogger(LoadMapMinigameBehaviour.class);

	private final ResourceLocation loadFrom;

	public LoadMapMinigameBehaviour(final ResourceLocation loadFrom) {
		this.loadFrom = loadFrom;
	}

	@Override
	public void onPreStart(final IMinigameDefinition definition, MinecraftServer server) {
		ResourceLocation path = new ResourceLocation(loadFrom.getNamespace(), "maps/" + loadFrom.getPath());
		try (IResource resource = server.getResourceManager().getResource(path)) {
			try (MapExportReader reader = MapExportReader.open(resource.getInputStream())) {
				MapMetadata metadata = reader.loadInto(server, definition.getDimension());
				// TODO: how do we pass metadata to things that need it?
			}
		} catch (IOException e) {
			LOGGER.error("Failed to load map from {}", path, e);
		}
	}

	@Override
	public void onPostFinish(final IMinigameInstance minigame) {
		ServerWorld world = minigame.getWorld();
		DimensionManager.unloadWorld(world);
	}
}
