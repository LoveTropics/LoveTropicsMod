package com.lovetropics.minigames.common.core.command.game;

import com.lovetropics.minigames.common.core.game.GameManager;
import com.lovetropics.minigames.common.core.game.IGameInstance;
import com.lovetropics.minigames.common.core.game.behavior.IGamePackageBehavior;
import com.lovetropics.minigames.common.core.game.behavior.event.GamePackageEvents;
import com.lovetropics.minigames.common.core.integration.game_actions.GamePackage;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.command.CommandSource;
import net.minecraft.command.ISuggestionProvider;
import net.minecraft.command.arguments.EntityArgument;
import net.minecraft.entity.player.ServerPlayerEntity;

import java.util.concurrent.CompletableFuture;

import static net.minecraft.command.Commands.argument;
import static net.minecraft.command.Commands.literal;

public class GamePackageCommand {
	public static void register(final CommandDispatcher<CommandSource> dispatcher) {
		dispatcher.register(
			literal("game")
			.then(literal("package").requires(s -> s.hasPermissionLevel(2))
				.then(argument("id", StringArgumentType.word())
					.suggests(GamePackageCommand::suggestPackages)
						.executes(ctx -> GamePackageCommand.spawnPackage(ctx, null))
						.then(argument("target", EntityArgument.player())
							.executes(ctx -> GamePackageCommand.spawnPackage(ctx, EntityArgument.getPlayer(ctx, "target"))))))
		);
	}

	private static CompletableFuture<Suggestions> suggestPackages(final CommandContext<CommandSource> context, final SuggestionsBuilder builder) {
		IGameInstance active = GameManager.get().getActiveMinigame();
		if (active != null) {
			return ISuggestionProvider.suggest(active.getBehaviors().values().stream()
					.filter(b -> b instanceof IGamePackageBehavior)
					.map(b -> ((IGamePackageBehavior)b).getPackageType()), builder);
		}
		return Suggestions.empty();
	}

	private static int spawnPackage(CommandContext<CommandSource> ctx, ServerPlayerEntity target) throws CommandSyntaxException {
		IGameInstance active = GameManager.get().getActiveMinigame();
		if (active != null) {
			String type = StringArgumentType.getString(ctx, "id");
			GamePackage gamePackage = new GamePackage(type, "LoveTropics", target == null ? null : target.getUniqueID());
			active.invoker(GamePackageEvents.RECEIVE_PACKAGE).onReceivePackage(active, gamePackage);
		}
		return Command.SINGLE_SUCCESS;
	}
}
