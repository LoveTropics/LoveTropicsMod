package com.lovetropics.minigames.common.minigames.behaviours.instances;

import com.lovetropics.minigames.common.minigames.IMinigameDefinition;
import com.lovetropics.minigames.common.minigames.behaviours.IMinigameBehavior;
import net.minecraft.command.CommandSource;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.DimensionManager;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;

public class LoadMapMinigameBehaviour implements IMinigameBehavior
{
	public static final Logger LOGGER = LogManager.getLogger();

	private final String loadFrom;

	public LoadMapMinigameBehaviour(final String loadFrom) {
		this.loadFrom = loadFrom;
	}

	@Override
	public void onPreStart(final IMinigameDefinition definition, MinecraftServer server) {
		File worldDirectory = server.getWorld(DimensionType.OVERWORLD).getSaveHandler().getWorldDirectory();
		loadMap(worldDirectory, loadFrom, definition.getDimension().getDirectory(worldDirectory));
	}

	@Override
	public void onPostFinish(final IMinigameDefinition definition, CommandSource commandSource) {
		ServerWorld world = commandSource.getServer().getWorld(definition.getDimension());
		DimensionManager.unloadWorld(world);
	}

	private static void saveMapTo(File from, File to) {
		try {
			if (from.exists()) {
				FileUtils.deleteDirectory(to);

				if (to.mkdirs()) {
					FileUtils.copyDirectory(from, to);
				}
			} else {
				LOGGER.info("Requested map to load doesn't exist in " + to.getPath() + ", add first before it can copy and replace each game start.");
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void saveBaseMap(File worldDirectory) {
		File baseMapsFile = new File(worldDirectory, "minigame_base_maps");

		File islandRoyaleBase = new File(baseMapsFile, "hunger_games");
		File islandRoyaleCurrent = new File(worldDirectory, "lovetropics/hunger_games");

		saveMapTo(islandRoyaleCurrent, islandRoyaleBase);
	}

	public static void loadMap(File worldDirectory, final String loadMap, final File saveTo) {
		File mapDirectory = new File(worldDirectory, "minigame_base_maps");
		File loadMapFrom = new File(mapDirectory, loadMap);

		saveMapTo(loadMapFrom, saveTo);
	}
}
