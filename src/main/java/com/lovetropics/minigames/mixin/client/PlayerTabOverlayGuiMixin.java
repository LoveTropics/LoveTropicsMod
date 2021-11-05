package com.lovetropics.minigames.mixin.client;

import com.lovetropics.minigames.client.lobby.state.ClientLobbyManager;
import com.lovetropics.minigames.client.lobby.state.ClientLobbyState;
import net.minecraft.client.gui.overlay.PlayerTabOverlayGui;
import net.minecraft.client.network.play.NetworkPlayerInfo;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.GameType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.UUID;

@Mixin(PlayerTabOverlayGui.class)
public class PlayerTabOverlayGuiMixin {
	@Inject(method = "func_238524_a_", at = @At("HEAD"), cancellable = true)
	private void getDisplayName(NetworkPlayerInfo info, IFormattableTextComponent displayName, CallbackInfoReturnable<ITextComponent> ci) {
		ClientLobbyState lobby = ClientLobbyManager.getJoined();
		if (lobby != null) {
			UUID id = info.getGameProfile().getId();
			if (lobby.getPlayers().contains(id)) {
				if (info.getGameType() != GameType.SPECTATOR) {
					ci.setReturnValue(new StringTextComponent("\uD83D\uDDE1 ").appendSibling(displayName));
				} else {
					ci.setReturnValue(displayName);
				}
			} else {
				ci.setReturnValue(displayName.mergeStyle(TextFormatting.DARK_GRAY));
			}
		}
	}
}
