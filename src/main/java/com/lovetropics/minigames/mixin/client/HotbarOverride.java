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

	@ModifyArg(
			method = "renderHotbar",
			at = @At(
					value = "INVOKE",
					target = "Lcom/mojang/blaze3d/systems/RenderSystem;setShaderTexture(ILnet/minecraft/resources/ResourceLocation;)V"),
			index = 1)
	public ResourceLocation getHotbarTexture(ResourceLocation loc) {
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
