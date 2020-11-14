package com.lovetropics.minigames.common.command.minigames;

import static net.minecraft.command.Commands.argument;
import static net.minecraft.command.Commands.literal;

import java.util.concurrent.CompletableFuture;

import com.lovetropics.minigames.common.game_actions.GamePackage;
import com.lovetropics.minigames.common.minigames.IMinigameInstance;
import com.lovetropics.minigames.common.minigames.MinigameManager;
import com.lovetropics.minigames.common.minigames.MinigameResult;
import com.lovetropics.minigames.common.minigames.behaviours.IMinigamePackageBehavior;
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

public class CommandMinigamePackage {
	public static void register(final CommandDispatcher<CommandSource> dispatcher) {
		dispatcher.register(
			literal("minigame")
			.then(literal("package").requires(s -> s.hasPermissionLevel(2))
				.then(argument("id", StringArgumentType.word())
					.suggests(CommandMinigamePackage::suggestPackages)
						.executes(ctx -> CommandMinigamePackage.spawnPackage(ctx, null))
						.then(argument("target", EntityArgument.player())
							.executes(ctx -> CommandMinigamePackage.spawnPackage(ctx, EntityArgument.getPlayer(ctx, "target"))))))
		);
	}

	private static CompletableFuture<Suggestions> suggestPackages(final CommandContext<CommandSource> context, final SuggestionsBuilder builder) {
		IMinigameInstance active = MinigameManager.getInstance().getActiveMinigame();
		if (active != null) {
			return ISuggestionProvider.suggest(active.getBehaviors().stream()
					.filter(b -> b instanceof IMinigamePackageBehavior)
					.map(b -> ((IMinigamePackageBehavior)b).getPackageType()), builder);
		}
		return Suggestions.empty();
	}

	private static int spawnPackage(CommandContext<CommandSource> ctx, ServerPlayerEntity target) throws CommandSyntaxException {
		IMinigameInstance active = MinigameManager.getInstance().getActiveMinigame();
		if (active != null) {
			String type = StringArgumentType.getString(ctx, "id");
			active.dispatchToBehaviors(b -> {
				b.onGamePackageReceived(active, new GamePackage(type, "LoveTropics", target == null ? null : target.getUniqueID()));
				return MinigameResult.ok();
			});
		}
		return Command.SINGLE_SUCCESS;
	}
}
