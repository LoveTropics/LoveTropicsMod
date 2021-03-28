package com.lovetropics.minigames.common.minigames;

import com.lovetropics.minigames.common.MoreCodecs;
import com.lovetropics.minigames.common.minigames.statistics.PlayerKey;
import com.mojang.brigadier.LiteralMessage;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.serialization.Codec;
import net.minecraft.command.CommandSource;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.ServerPlayerEntity;

public final class ControlCommand {
	private static final SimpleCommandExceptionType NO_PERMISSION = new SimpleCommandExceptionType(new LiteralMessage("You do not have permission to use this command!"));

	private final Scope scope;
	private final Handler handler;

	public ControlCommand(Scope scope, Handler handler) {
		this.scope = scope;
		this.handler = handler;
	}

	public static ControlCommand forEveryone(Handler handler) {
		return new ControlCommand(Scope.EVERYONE, handler);
	}

	public static ControlCommand forAdmins(Handler handler) {
		return new ControlCommand(Scope.ADMINS, handler);
	}

	public static ControlCommand forInitiator(Handler handler) {
		return new ControlCommand(Scope.INITIATOR, handler);
	}

	public void invoke(MinigameControllable minigame, CommandSource source) throws CommandSyntaxException {
		if (!canUse(minigame, source)) {
			throw NO_PERMISSION.create();
		}

		handler.run(source);
	}

	public boolean canUse(MinigameControllable minigame, CommandSource source) {
		return scope.testSource(minigame, source);
	}

	public enum Scope {
		EVERYONE("everyone") {
			@Override
			public boolean testSource(MinigameControllable minigame, CommandSource source) {
				return true;
			}
		},
		ADMINS("admins") {
			@Override
			public boolean testSource(MinigameControllable minigame, CommandSource source) {
				return source.hasPermissionLevel(2);
			}
		},
		INITIATOR("initiator") {
			@Override
			public boolean testSource(MinigameControllable minigame, CommandSource source) {
				if (source.hasPermissionLevel(2)) {
					return true;
				}

				Entity entity = source.getEntity();
				if (entity instanceof ServerPlayerEntity) {
					PlayerKey initiator = minigame.getInitiator();
					return initiator != null && initiator.matches(entity);
				}

				return false;
			}
		};

		public static final Codec<Scope> CODEC = MoreCodecs.stringVariants(Scope.values(), s -> s.key);

		public final String key;

		Scope(String key) {
			this.key = key;
		}

		public abstract boolean testSource(MinigameControllable minigame, CommandSource source);
	}

	public interface Handler {
		void run(CommandSource source) throws CommandSyntaxException;
	}
}
