package com.lovetropics.minigames.common.command;

import com.lovetropics.minigames.Constants;
import com.lovetropics.minigames.LoveTropics;
import com.lovetropics.minigames.common.dimension.DimensionUtils;
import com.lovetropics.minigames.common.map.MapExportWriter;
import com.lovetropics.minigames.common.map.MapMetadata;
import com.lovetropics.minigames.common.map.MapRegion;
import com.lovetropics.minigames.common.map.MapRegions;
import com.lovetropics.minigames.common.map.workspace.MapWorkspace;
import com.lovetropics.minigames.common.map.workspace.MapWorkspaceManager;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.minecraft.command.CommandSource;
import net.minecraft.command.arguments.BlockPosArgument;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.event.ClickEvent;
import net.minecraft.util.text.event.HoverEvent;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.storage.SessionLockException;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;

import static net.minecraft.command.Commands.argument;
import static net.minecraft.command.Commands.literal;

public final class CommandMap {
	private static final DynamicCommandExceptionType WORKSPACE_ALREADY_EXISTS = new DynamicCommandExceptionType(o -> {
		return new StringTextComponent("Workspace already exists with id '" + o + "'");
	});

	private static final DynamicCommandExceptionType WORKSPACE_DOES_NOT_EXIST = new DynamicCommandExceptionType(o -> {
		return new StringTextComponent("Workspace does not exist with id '" + o + "'");
	});

	private static final SimpleCommandExceptionType NOT_IN_WORKSPACE = new SimpleCommandExceptionType(new StringTextComponent("You are not in a workspace!"));

	public static void register(CommandDispatcher<CommandSource> dispatcher) {
		// @formatter:off
        dispatcher.register(
            literal("minigame").then(literal("map")
                .then(literal("open")
                    .then(argument("id", StringArgumentType.word())
                    .executes(CommandMap::openMap)
                ))
				.then(literal("delete")
					.then(argument("id", StringArgumentType.word())
					.executes(CommandMap::deleteMap)
				))
				.then(literal("join")
					.then(argument("id", StringArgumentType.word()) // TODO: suggestions
					.executes(CommandMap::joinMap)
				))
				.then(literal("export")
					.then(argument("id", StringArgumentType.word())
					.executes(CommandMap::exportMap)
				))
				.then(literal("region")
					.then(literal("add")
						.then(argument("key", StringArgumentType.word())
						.then(argument("min", BlockPosArgument.blockPos())
						.then(argument("max", BlockPosArgument.blockPos())
						.executes(CommandMap::addRegion)
					))))
				)
            )
        );
        // @formatter:on
	}

	private static int openMap(CommandContext<CommandSource> context) throws CommandSyntaxException {
		CommandSource source = context.getSource();
		MapWorkspaceManager workspaceManager = MapWorkspaceManager.get(source.getServer());

		String id = StringArgumentType.getString(context, "id");
		if (workspaceManager.hasWorkspace(id)) {
			throw WORKSPACE_ALREADY_EXISTS.create(id);
		}

		workspaceManager.openWorkspace(id);

		ITextComponent message = new StringTextComponent("Opened workspace with id '" + id + "'. ").applyTextStyles(TextFormatting.AQUA);
		ITextComponent join = new StringTextComponent("Click here to join")
				.applyTextStyle(style -> {
					style.setColor(TextFormatting.BLUE)
							.setUnderlined(true)
							.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/minigame map join " + id))
							.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new StringTextComponent("Join this map workspace")));
				});

		source.sendFeedback(message.appendSibling(join), false);

		return Command.SINGLE_SUCCESS;
	}

	private static int deleteMap(CommandContext<CommandSource> context) throws CommandSyntaxException {
		CommandSource source = context.getSource();
		MapWorkspaceManager workspaceManager = MapWorkspaceManager.get(source.getServer());

		String id = StringArgumentType.getString(context, "id");
		if (!workspaceManager.deleteWorkspace(id)) {
			throw WORKSPACE_DOES_NOT_EXIST.create(id);
		}

		source.sendFeedback(new StringTextComponent("Deleted workspace with id '" + id + "'. ").applyTextStyles(TextFormatting.GOLD), false);

		return Command.SINGLE_SUCCESS;
	}

	private static int joinMap(CommandContext<CommandSource> context) throws CommandSyntaxException {
		ServerPlayerEntity player = context.getSource().asPlayer();

		MapWorkspace workspace = getGivenWorkspace(context);

		DimensionType dimension = workspace.getDimension();
		DimensionUtils.teleportPlayerNoPortal(player, dimension, new BlockPos(0, 64, 0));

		if (player.abilities.allowFlying) {
			player.abilities.isFlying = true;
			player.sendPlayerAbilities();
		}

		return Command.SINGLE_SUCCESS;
	}

	private static int addRegion(CommandContext<CommandSource> context) throws CommandSyntaxException {
		MapWorkspace workspace = getCurrentWorkspace(context);

		String key = StringArgumentType.getString(context, "key");
		BlockPos min = BlockPosArgument.getBlockPos(context, "min");
		BlockPos max = BlockPosArgument.getBlockPos(context, "max");

		workspace.getRegions().add(key, MapRegion.of(min, max));

		return Command.SINGLE_SUCCESS;
	}

	private static int exportMap(CommandContext<CommandSource> context) throws CommandSyntaxException {
		CommandSource source = context.getSource();
		MapWorkspace workspace = getGivenWorkspace(context);

		MinecraftServer server = source.getServer();
		ServerWorld overworld = server.getWorld(DimensionType.OVERWORLD);

		CompletableFuture<Void> saveAll = saveWorkspace(server, workspace);

		saveAll.thenRunAsync(() -> {
			File worldDirectory = overworld.getSaveHandler().getWorldDirectory();
			File dimensionDirectory = workspace.getDimension().getDirectory(worldDirectory);

			ResourceLocation id = new ResourceLocation(Constants.MODID, workspace.getId());
			Path exportPath = MapExportWriter.pathFor(id);

			try {
				Files.createDirectories(exportPath.getParent());

				try (MapExportWriter writer = MapExportWriter.open(exportPath)) {
					MapRegions regions = workspace.getRegions().compile();
					writer.writeMetadata(new MapMetadata(id, workspace.getWorldSettings(), regions));
					writer.writeWorldData(dimensionDirectory.toPath());

					source.sendFeedback(new StringTextComponent("Successfully exported map!"), false);
				}
			} catch (Exception e) {
				source.sendErrorMessage(new StringTextComponent("Failed to export map!"));
				LoveTropics.LOGGER.error("Failed to export map", e);
			}
		}, Util.getServerExecutor());

		return Command.SINGLE_SUCCESS;
	}

	private static CompletableFuture<Void> saveWorkspace(MinecraftServer server, MapWorkspace workspace) {
		// TODO: accessing internal forge api: how can we check if the dimension is currently loaded?
		if (server.forgeGetWorldMap().containsKey(workspace.getDimension())) {
			return server.runAsync(() -> {
				ServerWorld workspaceWorld = server.getWorld(workspace.getDimension());
				try {
					workspaceWorld.save(null, true, false);
				} catch (SessionLockException e) {
					LoveTropics.LOGGER.warn("Could not save workspace world", e);
				}
			});
		}

		return CompletableFuture.completedFuture(null);
	}

	private static MapWorkspace getGivenWorkspace(CommandContext<CommandSource> context) throws CommandSyntaxException {
		CommandSource source = context.getSource();
		MapWorkspaceManager workspaceManager = MapWorkspaceManager.get(source.getServer());

		String id = StringArgumentType.getString(context, "id");
		MapWorkspace workspace = workspaceManager.getWorkspace(id);
		if (workspace == null) {
			throw WORKSPACE_DOES_NOT_EXIST.create(id);
		}

		return workspace;
	}

	private static MapWorkspace getCurrentWorkspace(CommandContext<CommandSource> context) throws CommandSyntaxException {
		CommandSource source = context.getSource();
		MapWorkspaceManager workspaceManager = MapWorkspaceManager.get(source.getServer());

		ServerPlayerEntity player = source.asPlayer();
		MapWorkspace workspace = workspaceManager.getWorkspace(player.dimension);
		if (workspace == null) {
			throw NOT_IN_WORKSPACE.create();
		}

		return workspace;
	}
}
