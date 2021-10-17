package com.lovetropics.minigames.common.core.command;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.command.CommandSource;
import net.minecraft.command.arguments.GameProfileArgument;
import net.minecraft.command.arguments.ResourceLocationArgument;
import net.minecraft.command.impl.BossBarCommand;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.server.CustomServerBossInfo;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.PlayerList;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;

import javax.annotation.Nullable;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.UUID;

import static net.minecraft.command.Commands.argument;
import static net.minecraft.command.Commands.literal;

public final class ExtendedBossBarCommand {
	public static void register(CommandDispatcher<CommandSource> dispatcher) {
		// @formatter:off
		dispatcher.register(
			literal("bossbar")
				.then(literal("players").requires(source -> source.hasPermissionLevel(2))
					.then(literal("add")
					.then(argument("id", ResourceLocationArgument.resourceLocation()).suggests(BossBarCommand.SUGGESTIONS_PROVIDER)
					.then(argument("players", GameProfileArgument.gameProfile())
						.executes(ExtendedBossBarCommand::addPlayers)
					)))
					.then(literal("remove")
					.then(argument("id", ResourceLocationArgument.resourceLocation()).suggests(BossBarCommand.SUGGESTIONS_PROVIDER)
					.then(argument("players", GameProfileArgument.gameProfile())
						.executes(ExtendedBossBarCommand::removePlayers)
					)))
			)
		);
		// @formatter:on
	}

	private static int addPlayers(CommandContext<CommandSource> context) throws CommandSyntaxException {
		return updatePlayers(context, (bossBar, profile, player) -> {
			if (player != null) {
				bossBar.addPlayer(player);
			} else {
				bossBar.addPlayer(profile.getId());
			}
		});
	}

	private static int removePlayers(CommandContext<CommandSource> context) throws CommandSyntaxException {
		return updatePlayers(context, (bossBar, profile, player) -> {
			if (player != null) {
				bossBar.removePlayer(player);
			} else {
				Set<UUID> playerIdSet = PlayerSetAccessor.getPlayerIdSet(bossBar);
				playerIdSet.remove(profile.getId());
			}
		});
	}

	private static int updatePlayers(CommandContext<CommandSource> context, PlayerUpdateFunction update) throws CommandSyntaxException {
		CustomServerBossInfo bossBar = BossBarCommand.getBossbar(context);
		Collection<GameProfile> profiles = GameProfileArgument.getGameProfiles(context, "players");

		MinecraftServer server = context.getSource().getServer();
		PlayerList players = server.getPlayerList();

		for (GameProfile profile : profiles) {
			ServerPlayerEntity player = players.getPlayerByUUID(profile.getId());
			update.apply(bossBar, profile, player);
		}

		return Command.SINGLE_SUCCESS;
	}

	interface PlayerUpdateFunction {
		void apply(CustomServerBossInfo bossBar, GameProfile profile, @Nullable ServerPlayerEntity player);
	}

	static final class PlayerSetAccessor {
		private static final MethodHandle GET_PLAYER_ID_SET;

		static {
			try {
				Field field = ObfuscationReflectionHelper.findField(CustomServerBossInfo.class, "field_201374_i");
				field.setAccessible(true);

				GET_PLAYER_ID_SET = MethodHandles.lookup().unreflectGetter(field);
			} catch (ReflectiveOperationException e) {
				throw new RuntimeException("Failed to get method handle to player set", e);
			}
		}

		@SuppressWarnings("unchecked")
		static Set<UUID> getPlayerIdSet(CustomServerBossInfo bossBar) {
			try {
				return (Set<UUID>) GET_PLAYER_ID_SET.invokeExact(bossBar);
			} catch (Throwable e) {
				return Collections.emptySet();
			}
		}
	}
}
