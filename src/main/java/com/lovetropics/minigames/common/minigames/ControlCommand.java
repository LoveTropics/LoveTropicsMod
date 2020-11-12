package com.lovetropics.minigames.common.minigames;

import com.lovetropics.minigames.common.minigames.statistics.PlayerKey;
import com.mojang.brigadier.LiteralMessage;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.minecraft.command.CommandSource;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.ServerPlayerEntity;

import javax.annotation.Nullable;

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
		EVERYONE {
			@Override
			public boolean testSource(MinigameControllable minigame, CommandSource source) {
				return true;
			}
		},
		ADMINS {
			@Override
			public boolean testSource(MinigameControllable minigame, CommandSource source) {
				return source.hasPermissionLevel(4);
			}
		},
		INITIATOR {
			@Override
			public boolean testSource(MinigameControllable minigame, CommandSource source) {
				if (source.hasPermissionLevel(4)) {
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

		public abstract boolean testSource(MinigameControllable minigame, CommandSource source);

		@Nullable
		public static Scope byKey(String key) {
			switch (key) {
				case "everyone": return EVERYONE;
				case "admins": return ADMINS;
				case "initiator": return INITIATOR;
			}
			return null;
		}
	}

	public interface Handler {
		void run(CommandSource source) throws CommandSyntaxException;
	}
}
