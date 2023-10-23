package com.lovetropics.minigames.mixin;

import com.lovetropics.minigames.common.core.game.IGameManager;
import com.lovetropics.minigames.common.core.game.IGamePhase;
import com.lovetropics.minigames.common.core.game.lobby.IGameLobby;
import com.mojang.authlib.GameProfile;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.util.ITeleporter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Objects;

@Mixin(ServerPlayer.class)
public abstract class ServerPlayerMixin extends Player {
	private ServerPlayerMixin(Level level, BlockPos pos, float yRot, GameProfile gameProfile) {
		super(level, pos, yRot, gameProfile);
	}

	@Inject(method = "teleportTo(Lnet/minecraft/server/level/ServerLevel;DDDFF)V", at = @At("HEAD"), cancellable = true)
	private void onTeleport(ServerLevel targetLevel, double x, double y, double z, float yaw, float pitch, CallbackInfo ci) {
		if (level() != targetLevel && !tryTeleportTo(targetLevel)) {
			ci.cancel();
		}
	}

	@Inject(method = "changeDimension", at = @At("HEAD"), cancellable = true, remap = false)
	private void onMoveWorld(ServerLevel targetLevel, ITeleporter teleporter, CallbackInfoReturnable<Entity> ci) {
		if (level() != targetLevel && !tryTeleportTo(targetLevel)) {
			ci.setReturnValue(this);
		}
	}

	private boolean tryTeleportTo(ServerLevel targetLevel) {
		IGameManager gameManager = IGameManager.get();
		IGameLobby lobby = gameManager.getLobbyFor(this);
		IGamePhase targetGame = gameManager.getGamePhaseInDimension(targetLevel);

		if (targetGame != null) {
			return Objects.equals(targetGame.getLobby(), lobby);
		}

		if (lobby != null) {
			lobby.getPlayers().remove((ServerPlayer) (Object) this);
		}
		return true;
	}
}
