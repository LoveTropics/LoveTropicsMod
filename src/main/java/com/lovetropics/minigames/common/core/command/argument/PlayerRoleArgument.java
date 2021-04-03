package com.lovetropics.minigames.common.core.command.argument;

import com.lovetropics.minigames.common.core.game.PlayerRole;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.ISuggestionProvider;
import net.minecraft.util.text.StringTextComponent;

public final class PlayerRoleArgument {
	private static final DynamicCommandExceptionType ROLE_NOT_VALID = new DynamicCommandExceptionType(key -> {
		return new StringTextComponent("'" + key + "' is not a valid role");
	});

    public static RequiredArgumentBuilder<CommandSource, String> argument(String name) {
        return Commands.argument(name, StringArgumentType.string())
                .suggests((context, builder) -> {
                    return ISuggestionProvider.suggest(
							PlayerRole.stream().map(PlayerRole::getKey),
                            builder
                    );
                });
    }

	public static PlayerRole get(CommandContext<CommandSource> context, String name) throws CommandSyntaxException {
		String key = StringArgumentType.getString(context, name);

		PlayerRole role = PlayerRole.byKey(key);
		if (role == null) {
			throw ROLE_NOT_VALID.create(key);
		}

		return role;
	}
}
