package com.lovetropics.minigames.mixin.client;

import com.lovetropics.minigames.client.tweaks.ClientGameTweaksState;
import com.lovetropics.minigames.common.core.game.client_tweak.GameClientTweakTypes;
import com.lovetropics.minigames.common.core.game.client_tweak.instance.HotbarTextureTweak;
import net.minecraft.client.gui.IngameGui;
import net.minecraft.util.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(IngameGui.class)
public class HotbarOverride {

	@ModifyArg(
			method = "renderHotbar",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/client/renderer/texture/TextureManager;bindTexture(Lnet/minecraft/util/ResourceLocation;)V"),
			index = 0)
	public ResourceLocation getHotbarTexture(ResourceLocation loc) {
		HotbarTextureTweak hotbarTexture = ClientGameTweaksState.getOrNull(GameClientTweakTypes.HOTBAR_TEXTURE);
		return hotbarTexture != null ? hotbarTexture.getTexture() : loc;
	}
}
