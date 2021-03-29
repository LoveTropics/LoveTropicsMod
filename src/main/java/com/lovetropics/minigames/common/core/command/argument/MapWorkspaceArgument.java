package com.lovetropics.minigames.common.core.command.argument;

import com.lovetropics.minigames.common.core.map.workspace.MapWorkspace;
import com.lovetropics.minigames.common.core.map.workspace.MapWorkspaceManager;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.ISuggestionProvider;
import net.minecraft.util.text.StringTextComponent;

public final class MapWorkspaceArgument {
	private static final DynamicCommandExceptionType WORKSPACE_DOES_NOT_EXIST = new DynamicCommandExceptionType(id -> {
		return new StringTextComponent("Workspace does not exist with id '" + id + "'");
	});

	public static RequiredArgumentBuilder<CommandSource, String> argument(String name) {
		return Commands.argument(name, StringArgumentType.string())
				.suggests((context, builder) -> {
					CommandSource source = context.getSource();
					MapWorkspaceManager workspaceManager = MapWorkspaceManager.get(source.getServer());

					return ISuggestionProvider.suggest(
							workspaceManager.getWorkspaceIds().stream(),
							builder
					);
				});
	}

	public static MapWorkspace get(CommandContext<CommandSource> context, String name) throws CommandSyntaxException {
		CommandSource source = context.getSource();
		MapWorkspaceManager workspaceManager = MapWorkspaceManager.get(source.getServer());

		String id = StringArgumentType.getString(context, name);
		MapWorkspace workspace = workspaceManager.getWorkspace(id);
		if (workspace == null) {
			throw WORKSPACE_DOES_NOT_EXIST.create(id);
		}

		return workspace;
	}
}
