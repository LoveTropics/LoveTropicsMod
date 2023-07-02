package com.lovetropics.minigames.mixin.client;

import com.lovetropics.minigames.client.game.ClientGameStateManager;
import com.lovetropics.minigames.common.core.game.client_state.GameClientStateTypes;
import com.lovetropics.minigames.common.core.game.client_state.instance.ReplaceTexturesClientState;
import net.minecraft.client.gui.components.BossHealthOverlay;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(BossHealthOverlay.class)
public class BossOverlayGuiMixin {
	@ModifyArg(
			method = "drawBar(Lnet/minecraft/client/gui/GuiGraphics;IILnet/minecraft/world/BossEvent;II)V",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/client/gui/GuiGraphics;blit(Lnet/minecraft/resources/ResourceLocation;IIIIII)V"
			),
			index = 0
	)
	public ResourceLocation getBossBarTexture(ResourceLocation loc) {
		ReplaceTexturesClientState textures = ClientGameStateManager.getOrNull(GameClientStateTypes.REPLACE_TEXTURES);
		if (textures != null) {
			ResourceLocation bossBars = textures.getTexture(ReplaceTexturesClientState.TextureType.BOSS_BARS);
			if (bossBars != null) {
				return bossBars;
			}
		}
		return loc;
	}
}
