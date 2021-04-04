package com.lovetropics.minigames.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import com.lovetropics.minigames.Constants;
import com.lovetropics.minigames.client.minigame.ClientMinigameState;
import com.lovetropics.minigames.common.core.game.GameStatus;
import com.lovetropics.minigames.common.core.game.PlayerRole;

import net.minecraft.client.gui.IngameGui;
import net.minecraft.util.ResourceLocation;

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
		ClientMinigameState state = ClientMinigameState.getCurrent();
		if (state != null) {
			if (state.getStatus() == GameStatus.ACTIVE && state.getRole() == PlayerRole.PARTICIPANT && state.getMinigame().equals(TARGET)) {
				return TEXTURE;
			}
		}
		return loc;
	}
}
