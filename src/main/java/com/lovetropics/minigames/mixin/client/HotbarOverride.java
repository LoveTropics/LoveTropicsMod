package com.lovetropics.minigames.mixin.client;

import com.lovetropics.minigames.client.game.ClientGameStateManager;
import com.lovetropics.minigames.common.core.game.client_state.GameClientStateTypes;
import com.lovetropics.minigames.common.core.game.client_state.instance.ReplaceTexturesClientState;
import net.minecraft.client.gui.Gui;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(Gui.class)
public class HotbarOverride {
	private static final ResourceLocation WIDGETS_LOCATION = new ResourceLocation("textures/gui/widgets.png");

	@ModifyArg(
			method = "renderHotbar",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/client/gui/GuiGraphics;blit(Lnet/minecraft/resources/ResourceLocation;IIIIII)V"),
			index = 0)
	public ResourceLocation getHotbarTexture(ResourceLocation loc) {
		if (!loc.equals(WIDGETS_LOCATION)) {
			return loc;
		}
		ReplaceTexturesClientState textures = ClientGameStateManager.getOrNull(GameClientStateTypes.REPLACE_TEXTURES);
		if (textures != null) {
			ResourceLocation hotbar = textures.getTexture(ReplaceTexturesClientState.TextureType.HOTBAR);
			if (hotbar != null) {
				return hotbar;
			}
		}
		return loc;
	}
}
