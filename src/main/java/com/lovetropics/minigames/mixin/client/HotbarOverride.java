package com.lovetropics.minigames.mixin.client;

import com.lovetropics.minigames.Constants;
import com.lovetropics.minigames.client.lobby.state.ClientGameDefinition;
import com.lovetropics.minigames.client.lobby.state.ClientLobbyManager;
import com.lovetropics.minigames.client.lobby.state.ClientLobbyState;
import net.minecraft.client.gui.IngameGui;
import net.minecraft.util.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(IngameGui.class)
public class HotbarOverride {

	private static final ResourceLocation TARGET = new ResourceLocation("lt20:signature_run");
	private static final ResourceLocation TEXTURE = new ResourceLocation(Constants.MODID, "textures/gui/widgets.png");

	@ModifyArg(
			method = "renderHotbar",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/client/renderer/texture/TextureManager;bindTexture(Lnet/minecraft/util/ResourceLocation;)V"),
			index = 0)
	public ResourceLocation getHotbarTexture(ResourceLocation loc) {
		ClientLobbyState state = ClientLobbyManager.getJoined();
		if (state != null) {
			ClientGameDefinition currentGame = state.getCurrentGame();
			if (currentGame != null && currentGame.id.equals(TARGET)) {
				return TEXTURE;
			}
		}
		return loc;
	}
}
