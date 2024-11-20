package com.lovetropics.minigames.mixin;

import com.lovetropics.minigames.common.core.game.impl.GameEventDispatcher;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.AbstractContainerMenu;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayer.class)
public class ServerPlayerMixin {
	@Inject(method = "initMenu", at = @At("HEAD"))
	private void initMenu(AbstractContainerMenu menu, CallbackInfo ci) {
		menu.addSlotListener(GameEventDispatcher.instance.createInventoryListener((ServerPlayer) (Object) this));
	}
}
