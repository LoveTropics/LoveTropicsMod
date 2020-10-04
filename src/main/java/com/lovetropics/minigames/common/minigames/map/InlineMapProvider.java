package com.lovetropics.minigames.common.minigames.map;

import com.lovetropics.minigames.common.minigames.IMinigameDefinition;
import com.lovetropics.minigames.common.minigames.IMinigameInstance;
import com.mojang.datafixers.Dynamic;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.dimension.DimensionType;

import java.util.concurrent.CompletableFuture;

public final class InlineMapProvider implements IMinigameMapProvider{
	private final DimensionType dimension;

	public InlineMapProvider(DimensionType dimension) {
		this.dimension = dimension;
	}

	public static <T> InlineMapProvider parse(Dynamic<T> root) {
		DimensionType dimension = DimensionType.byName(new ResourceLocation(root.get("dimension").asString("")));
		return new InlineMapProvider(dimension);
	}

	@Override
	public ActionResult<ITextComponent> canOpen(IMinigameDefinition definition, MinecraftServer server) {
		return ActionResult.resultSuccess(new StringTextComponent(""));
	}

	@Override
	public CompletableFuture<DimensionType> open(IMinigameInstance minigame, MinecraftServer server) {
		return CompletableFuture.completedFuture(dimension);
	}

	@Override
	public void close(IMinigameInstance minigame) {
	}
}
