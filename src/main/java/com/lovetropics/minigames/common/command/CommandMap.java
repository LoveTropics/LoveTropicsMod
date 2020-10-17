package com.lovetropics.minigames.common.command;

import com.lovetropics.minigames.Constants;
import com.lovetropics.minigames.LoveTropics;
import com.lovetropics.minigames.common.dimension.DimensionUtils;
import com.lovetropics.minigames.common.map.*;
import com.lovetropics.minigames.common.map.generator.ConfiguredGenerator;
import com.lovetropics.minigames.common.map.generator.ConfiguredGenerators;
import com.lovetropics.minigames.common.map.workspace.MapWorkspace;
import com.lovetropics.minigames.common.map.workspace.MapWorkspaceManager;
import com.lovetropics.minigames.common.map.workspace.WorkspacePositionTracker;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.minecraft.command.CommandSource;
import net.minecraft.command.arguments.BlockPosArgument;
import net.minecraft.command.arguments.ResourceLocationArgument;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.event.ClickEvent;
import net.minecraft.util.text.event.HoverEvent;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.storage.SessionLockException;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;

import static net.minecraft.command.Commands.argument;
import static net.minecraft.command.Commands.literal;

public final class CommandMap {
	private static final DynamicCommandExceptionType WORKSPACE_ALREADY_EXISTS = new DynamicCommandExceptionType(o -> {
		return new StringTextComponent("Workspace already exists with id '" + o + "'");
	});

	private static final SimpleCommandExceptionType NOT_IN_WORKSPACE = new SimpleCommandExceptionType(new StringTextComponent("You are not in a workspace!"));

	public static void register(CommandDispatcher<CommandSource> dispatcher) {
		// @formatter:off
        dispatcher.register(
            literal("minigame").then(literal("map")
				.requires(source -> source.hasPermissionLevel(2))
                .then(literal("open")
                    .then(argument("id", StringArgumentType.word())
						.then(ConfiguredGeneratorArgument.argument("generator")
						.executes(context ->{
							ConfiguredGenerator generator = ConfiguredGeneratorArgument.get(context, "generator");
							return openMap(context, generator);
						})
					)
						.executes(context -> openMap(context, ConfiguredGenerators.VOID))
                ))
				.then(literal("delete")
					.then(MapWorkspaceArgument.argument("id")
					.executes(CommandMap::deleteMap)
				))
				.then(literal("join")
					.then(MapWorkspaceArgument.argument("id")
					.executes(CommandMap::joinMap)
				))
				.then(literal("leave").executes(CommandMap::leaveMap)
				)
				.then(literal("export")
					.then(MapWorkspaceArgument.argument("id")
					.executes(CommandMap::exportMap)
				))
				.then(literal("import")
					.then(argument("location", ResourceLocationArgument.resourceLocation())
							.then(ConfiguredGeneratorArgument.argument("generator")
							.executes(context ->{
								ConfiguredGenerator generator = ConfiguredGeneratorArgument.get(context, "generator");
								return importMap(context, generator);
							})
						)
							.executes(context -> importMap(context, ConfiguredGenerators.VOID))
					)
				)
				.then(literal("region")
					.then(literal("add")
						.then(argument("key", StringArgumentType.word())
								.then(argument("min", BlockPosArgument.blockPos())
								.then(argument("max", BlockPosArgument.blockPos())
								.executes(CommandMap::addRegion)
							))
								.executes(CommandMap::addRegionHere)
						)
					)
				)
            )
        );
        // @formatter:on
	}

	private static int openMap(CommandContext<CommandSource> context, ConfiguredGenerator generator) throws CommandSyntaxException {
		CommandSource source = context.getSource();
		MapWorkspaceManager workspaceManager = MapWorkspaceManager.get(source.getServer());

		String id = StringArgumentType.getString(context, "id");
		if (workspaceManager.hasWorkspace(id)) {
			throw WORKSPACE_ALREADY_EXISTS.create(id);
		}

		workspaceManager.openWorkspace(id, generator);

		ITextComponent message = new StringTextComponent("Opened workspace with id '" + id + "'. ").applyTextStyles(TextFormatting.AQUA);
		ITextComponent join = new StringTextComponent("Click here to join")
				.applyTextStyle(style -> {
					String command = "/minigame map join " + id;
					style.setColor(TextFormatting.BLUE)
							.setUnderlined(true)
							.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, command))
							.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new StringTextComponent(command)));
				});

		source.sendFeedback(message.appendSibling(join), false);

		return Command.SINGLE_SUCCESS;
	}

	private static int deleteMap(CommandContext<CommandSource> context) throws CommandSyntaxException {
		CommandSource source = context.getSource();
		MapWorkspaceManager workspaceManager = MapWorkspaceManager.get(source.getServer());

		MapWorkspace workspace = MapWorkspaceArgument.get(context, "id");
		workspaceManager.deleteWorkspace(workspace.getId());

		source.sendFeedback(new StringTextComponent("Deleted workspace with id '" + workspace.getId() + "'. ").applyTextStyles(TextFormatting.GOLD), false);

		return Command.SINGLE_SUCCESS;
	}

	private static int leaveMap(CommandContext<CommandSource> context) throws CommandSyntaxException {
		ServerPlayerEntity player = context.getSource().asPlayer();

		WorkspacePositionTracker.Position returnPosition = WorkspacePositionTracker.getReturnPositionFor(player);
		if (returnPosition != null) {
			returnPosition.applyTo(player);
		} else {
			DimensionUtils.teleportPlayerNoPortal(player, DimensionType.OVERWORLD, new BlockPos(0, 64, 0));
		}

		return Command.SINGLE_SUCCESS;
	}

	private static int joinMap(CommandContext<CommandSource> context) throws CommandSyntaxException {
		ServerPlayerEntity player = context.getSource().asPlayer();

		MapWorkspace workspace = MapWorkspaceArgument.get(context, "id");

		WorkspacePositionTracker.Position position = WorkspacePositionTracker.getPositionFor(player, workspace);
		if (position != null) {
			position.applyTo(player);
		} else {
			DimensionType dimension = workspace.getDimension();
			ServerWorld world = context.getSource().getServer().getWorld(dimension);
			BlockPos spawn = world.getDimension().findSpawn(0, 0, false);
			DimensionUtils.teleportPlayerNoPortal(player, dimension, spawn);
		}

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

	private static int addRegionHere(CommandContext<CommandSource> context) throws CommandSyntaxException {
		MapWorkspace workspace = getCurrentWorkspace(context);
		Vec3d pos = context.getSource().getPos();

		String key = StringArgumentType.getString(context, "key");

		workspace.getRegions().add(key, MapRegion.of(new BlockPos(pos)));

		return Command.SINGLE_SUCCESS;
	}

	private static int exportMap(CommandContext<CommandSource> context) throws CommandSyntaxException {
		CommandSource source = context.getSource();
		MapWorkspace workspace = MapWorkspaceArgument.get(context, "id");

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

	private static int importMap(CommandContext<CommandSource> context, ConfiguredGenerator generator) throws CommandSyntaxException {
		ResourceLocation location = ResourceLocationArgument.getResourceLocation(context, "location");
		String id = location.getPath();

		CommandSource source = context.getSource();
		MapWorkspaceManager workspaceManager = MapWorkspaceManager.get(source.getServer());

		if (workspaceManager.hasWorkspace(id)) {
			throw WORKSPACE_ALREADY_EXISTS.create(id);
		}

		MapWorkspace workspace = workspaceManager.openWorkspace(id, generator);

		CompletableFuture.runAsync(() -> {
			try {
				MinecraftServer server = source.getServer();

				try (MapExportReader reader = MapExportReader.open(server, location)) {
					MapMetadata metadata = reader.loadInto(server, workspace.getDimension());
					workspace.importFrom(metadata);

					source.sendFeedback(new StringTextComponent("Successfully imported workspace into '" + id + "'"), false);
				}
			} catch (IOException e) {
				source.sendErrorMessage(new StringTextComponent("Failed to import workspace!"));
				e.printStackTrace();
			}
		}, Util.getServerExecutor());

		return Command.SINGLE_SUCCESS;
	}
}
