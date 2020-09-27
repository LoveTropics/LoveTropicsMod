package com.lovetropics.minigames.common.minigames.behaviours.instances;

import com.lovetropics.minigames.common.map.MapExportReader;
import com.lovetropics.minigames.common.map.MapMetadata;
import com.lovetropics.minigames.common.map.MapWorldInfo;
import com.lovetropics.minigames.common.minigames.IMinigameDefinition;
import com.lovetropics.minigames.common.minigames.IMinigameInstance;
import com.lovetropics.minigames.common.minigames.behaviours.IMinigameBehavior;
import com.mojang.datafixers.Dynamic;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.dimension.DimensionType;
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

	public static <T> LoadMapMinigameBehaviour parse(Dynamic<T> root) {
		ResourceLocation loadFrom = new ResourceLocation(root.get("load_from").asString(""));
		return new LoadMapMinigameBehaviour(loadFrom);
	}

	@Override
	public ActionResult<ITextComponent> canStartMinigame(final IMinigameDefinition definition, final MinecraftServer server) {
		ServerWorld world = DimensionManager.getWorld(server, definition.getDimension(), false, false);

		if (world != null) {
			final ITextComponent minigameName = new TranslationTextComponent(definition.getUnlocalizedName());
			if (world.getPlayers().size() <= 0) {
				DimensionManager.unloadWorld(world);
				return new ActionResult<>(ActionResultType.FAIL, new StringTextComponent("The ").appendSibling(minigameName).appendText(" dimension was not unloaded. Begun unloading, please try again in a few seconds.").applyTextStyle(
						TextFormatting.RED));
			}

			return new ActionResult<>(ActionResultType.FAIL, new StringTextComponent("Cannot start minigame as players are in ").appendSibling(minigameName).appendText(" dimension. Make them teleport out first.").applyTextStyle(TextFormatting.RED));
		}

		return new ActionResult<>(ActionResultType.SUCCESS, new StringTextComponent(""));
	}

	@Override
	public void onConstruct(IMinigameInstance minigame, MinecraftServer server) {
		try (MapExportReader reader = MapExportReader.open(server, loadFrom)) {
			MapMetadata metadata = reader.loadInto(server, minigame.getDimension());

			minigame.getMapRegions().addAll(metadata.regions);

			ServerWorld world = minigame.getWorld();
			ServerWorld overworld = world.getServer().getWorld(DimensionType.OVERWORLD);

			world.worldInfo = new MapWorldInfo(overworld.getWorldInfo(), metadata.settings);
		} catch (IOException e) {
			LOGGER.error("Failed to load map from {}", loadFrom, e);
		}
	}

	@Override
	public void onPostFinish(final IMinigameInstance minigame) {
		ServerWorld world = minigame.getWorld();
		DimensionManager.unloadWorld(world);
	}
}
