package com.lovetropics.minigames.common.core.command;

import com.lovetropics.minigames.Constants;
import com.lovetropics.minigames.LoveTropics;
import com.lovetropics.minigames.common.core.command.argument.DimensionArgument;
import com.lovetropics.minigames.common.core.command.argument.MapWorkspaceArgument;
import com.lovetropics.minigames.common.core.dimension.DimensionUtils;
import com.lovetropics.minigames.common.core.map.*;
import com.lovetropics.minigames.common.core.map.workspace.MapWorkspace;
import com.lovetropics.minigames.common.core.map.workspace.MapWorkspaceManager;
import com.lovetropics.minigames.common.core.map.workspace.WorkspaceDimensionConfig;
import com.lovetropics.minigames.common.core.map.workspace.WorkspacePositionTracker;
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
import net.minecraft.util.RegistryKey;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.event.ClickEvent;
import net.minecraft.util.text.event.HoverEvent;
import net.minecraft.world.Dimension;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.storage.SaveFormat;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;

import static net.minecraft.command.Commands.argument;
import static net.minecraft.command.Commands.literal;

public final class MapCommand {
	private static final DynamicCommandExceptionType WORKSPACE_ALREADY_EXISTS = new DynamicCommandExceptionType(o -> {
		return new StringTextComponent("Workspace already exists with id '" + o + "'");
	});

	private static final SimpleCommandExceptionType NOT_IN_WORKSPACE = new SimpleCommandExceptionType(new StringTextComponent("You are not in a workspace!"));

	public static void register(CommandDispatcher<CommandSource> dispatcher) {
		// @formatter:off
        dispatcher.register(
            literal("map")
				.requires(source -> source.hasPermissionLevel(2))
                .then(literal("open")
                    .then(argument("id", StringArgumentType.string())
						.then(DimensionArgument.argument("dimension")
						.executes(context ->{
							Dimension dimension = DimensionArgument.get(context, "dimension");
							return openMap(context, dimension);
						})
					)
						.executes(context -> {
							MinecraftServer server = context.getSource().getServer();
							Dimension dimension = new Dimension(DimensionUtils.overworld(server), new VoidChunkGenerator(server));
							return openMap(context, dimension);
						})
                ))
				.then(literal("delete")
					.then(MapWorkspaceArgument.argument("id")
					.executes(MapCommand::deleteMap)
				))
				.then(literal("join")
					.then(MapWorkspaceArgument.argument("id")
					.executes(MapCommand::joinMap)
				))
				.then(literal("leave").executes(MapCommand::leaveMap))
				.then(literal("export")
					.then(MapWorkspaceArgument.argument("id")
					.executes(MapCommand::exportMap)
				))
				.then(literal("import")
					.then(argument("location", ResourceLocationArgument.resourceLocation())
							.then(DimensionArgument.argument("dimension")
							.executes(context ->{
								Dimension dimension = DimensionArgument.get(context, "dimension");
								return importMap(context, dimension);
							})
						)
							.executes(context -> {
								MinecraftServer server = context.getSource().getServer();
								Dimension dimension = new Dimension(DimensionUtils.overworld(server), new VoidChunkGenerator(server));
								return importMap(context, dimension);
							})
					)
				)
				.then(literal("region")
					.then(literal("add")
						.then(argument("key", StringArgumentType.string())
								.then(argument("min", BlockPosArgument.blockPos())
								.then(argument("max", BlockPosArgument.blockPos())
								.executes(MapCommand::addRegion)
							))
								.executes(MapCommand::addRegionHere)
						)
					)
					.then(literal("hide")
							.executes(MapCommand::showHideRegions))
				)
        );
        // @formatter:on
	}

	private static int openMap(CommandContext<CommandSource> context, Dimension dimension) throws CommandSyntaxException {
		CommandSource source = context.getSource();
		MinecraftServer server = source.getServer();
		MapWorkspaceManager workspaceManager = MapWorkspaceManager.get(server);

		String id = StringArgumentType.getString(context, "id");
		if (workspaceManager.hasWorkspace(id)) {
			throw WORKSPACE_ALREADY_EXISTS.create(id);
		}

		long seed = server.func_241755_D_().getSeed();
		WorkspaceDimensionConfig dimensionConfig = new WorkspaceDimensionConfig(dimension.getDimensionTypeSupplier(), dimension.getChunkGenerator(), seed);

		workspaceManager.openWorkspace(id, dimensionConfig).thenAcceptAsync(workspace -> {
			IFormattableTextComponent message = new StringTextComponent("Opened workspace with id '" + id + "'. ").mergeStyle(TextFormatting.AQUA);
			ITextComponent join = new StringTextComponent("Click here to join")
					.modifyStyle(style -> {
						String command = "/map join " + id;
						return style.setFormatting(TextFormatting.BLUE)
								.setUnderlined(true)
								.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, command))
								.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new StringTextComponent(command)));
					});

			source.sendFeedback(message.appendSibling(join), false);
		}, server);

		return Command.SINGLE_SUCCESS;
	}

	private static int deleteMap(CommandContext<CommandSource> context) throws CommandSyntaxException {
		CommandSource source = context.getSource();
		MapWorkspaceManager workspaceManager = MapWorkspaceManager.get(source.getServer());

		MapWorkspace workspace = MapWorkspaceArgument.get(context, "id");
		workspaceManager.deleteWorkspace(workspace.getId());

		source.sendFeedback(new StringTextComponent("Deleted workspace with id '" + workspace.getId() + "'. ").mergeStyle(TextFormatting.GOLD), false);

		return Command.SINGLE_SUCCESS;
	}

	private static int leaveMap(CommandContext<CommandSource> context) throws CommandSyntaxException {
		ServerPlayerEntity player = context.getSource().asPlayer();

		WorkspacePositionTracker.Position returnPosition = WorkspacePositionTracker.getReturnPositionFor(player);
		if (returnPosition != null) {
			returnPosition.applyTo(player);
		} else {
			DimensionUtils.teleportPlayerNoPortal(player, World.OVERWORLD, new BlockPos(0, 64, 0));
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
			RegistryKey<World> dimension = workspace.getDimension();
			ServerWorld world = context.getSource().getServer().getWorld(dimension);
			DimensionUtils.teleportPlayerNoPortal(player, dimension, world.getSpawnPoint());
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
		Vector3d pos = context.getSource().getPos();

		String key = StringArgumentType.getString(context, "key");

		workspace.getRegions().add(key, MapRegion.of(new BlockPos(pos)));

		return Command.SINGLE_SUCCESS;
	}

	private static int showHideRegions(CommandContext<CommandSource> context) throws CommandSyntaxException {
		MapWorkspace workspace = getCurrentWorkspace(context);

		workspace.getRegions().showHide(context.getSource().asPlayer());

		return Command.SINGLE_SUCCESS;
	}

	private static int exportMap(CommandContext<CommandSource> context) throws CommandSyntaxException {
		CommandSource source = context.getSource();
		MapWorkspace workspace = MapWorkspaceArgument.get(context, "id");

		MinecraftServer server = source.getServer();

		CompletableFuture<Void> saveAll = saveWorkspace(server, workspace);

		saveAll.thenRunAsync(() -> {
			SaveFormat.LevelSave save = server.anvilConverterForAnvilFile;
			File dimensionDirectory = save.getDimensionFolder(workspace.getDimension());

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
		return server.runAsync(() -> {
			ServerWorld workspaceWorld = server.getWorld(workspace.getDimension());
			workspaceWorld.save(null, true, false);
		});
	}

	private static MapWorkspace getCurrentWorkspace(CommandContext<CommandSource> context) throws CommandSyntaxException {
		CommandSource source = context.getSource();
		MapWorkspaceManager workspaceManager = MapWorkspaceManager.get(source.getServer());

		ServerPlayerEntity player = source.asPlayer();
		MapWorkspace workspace = workspaceManager.getWorkspace(player.world.getDimensionKey());
		if (workspace == null) {
			throw NOT_IN_WORKSPACE.create();
		}

		return workspace;
	}

	private static int importMap(CommandContext<CommandSource> context, Dimension dimension) throws CommandSyntaxException {
		ResourceLocation location = ResourceLocationArgument.getResourceLocation(context, "location");
		String id = location.getPath();

		CommandSource source = context.getSource();
		MapWorkspaceManager workspaceManager = MapWorkspaceManager.get(source.getServer());

		if (workspaceManager.hasWorkspace(id)) {
			throw WORKSPACE_ALREADY_EXISTS.create(id);
		}

		long seed = source.getServer().func_241755_D_().getSeed();
		WorkspaceDimensionConfig dimensionConfig = new WorkspaceDimensionConfig(dimension.getDimensionTypeSupplier(), dimension.getChunkGenerator(), seed);

		workspaceManager.openWorkspace(id, dimensionConfig).thenAcceptAsync(workspace -> {
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
