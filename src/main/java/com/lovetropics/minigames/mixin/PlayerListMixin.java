package com.lovetropics.minigames.mixin;

import com.lovetropics.minigames.common.core.game.PlayerIsolation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerList.class)
public class PlayerListMixin {
	@Inject(method = "save", at = @At("HEAD"), cancellable = true)
	private void save(final ServerPlayer player, final CallbackInfo ci) {
		if (PlayerIsolation.INSTANCE.isIsolated(player)) {
			ci.cancel();
		}
	}
}
