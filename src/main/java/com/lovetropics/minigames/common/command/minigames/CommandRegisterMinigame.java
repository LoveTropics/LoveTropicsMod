package com.lovetropics.minigames.common.command.minigames;

import com.lovetropics.minigames.common.minigames.MinigameManager;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.command.CommandSource;
import net.minecraft.entity.player.ServerPlayerEntity;

import static net.minecraft.command.Commands.literal;

public class CommandRegisterMinigame {
	public static void register(final CommandDispatcher<CommandSource> dispatcher) {
		dispatcher.register(
			literal("minigame")
			.then(literal("register")
			.requires(s -> s.getEntity() instanceof ServerPlayerEntity)
			.executes(c -> CommandMinigame.executeMinigameAction(() ->
					MinigameManager.getInstance().registerFor((ServerPlayerEntity) c.getSource().getEntity()), c.getSource())))
		);
	}
}
