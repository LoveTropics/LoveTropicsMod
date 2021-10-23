package com.lovetropics.minigames.mixin.client;

import com.lovetropics.minigames.client.lobby.state.ClientLobbyManager;
import com.lovetropics.minigames.common.content.mangroves_and_pianguas.MangrovesAndPianguas;
import com.lovetropics.minigames.common.content.mangroves_and_pianguas.client_tweak.CheckeredPlotsTweak;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockDisplayReader;
import net.minecraft.world.biome.BiomeColors;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BiomeColors.class)
public class BiomeColorsMixin {
	@Inject(method = "getGrassColor", at = @At("HEAD"), cancellable = true)
	private static void getGrassColor(IBlockDisplayReader world, BlockPos pos, CallbackInfoReturnable<Integer> ci) {
		CheckeredPlotsTweak checkeredPlots = ClientLobbyManager.getTweakOrNull(MangrovesAndPianguas.CHECKERED_PLOTS);
		if (checkeredPlots != null && checkeredPlots.contains(pos)) {
			boolean checkerboard = (pos.getX() + pos.getZ() & 1) == 0;
			ci.setReturnValue(checkerboard ? 0x4CCE35 : 0x2E9641);
		}
	}
}
