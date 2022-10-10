package com.lovetropics.minigames.mixin;

import com.lovetropics.minigames.Constants;
import net.minecraft.util.datafix.fixes.ChunkHeightAndBiomeFix;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

// I am sorry for what I must do.
// We import our maps with new overworld dimension type settings, but the datafixer does not update the heights because it is not actually the overworld.
// Let's pretend to be the overworld.
@Mixin(ChunkHeightAndBiomeFix.class)
public class ChunkHeightAndBiomeFixMixin {
    @Redirect(method = "lambda$makeRule$10", remap = false, at = @At(value = "INVOKE", target = "Ljava/lang/String;equals(Ljava/lang/Object;)Z"))
    private boolean shouldUpgradeHeight(String overworld, Object dimension) {
        if (((String) dimension).startsWith(Constants.MODID + ":")) {
            return true;
        }
        return overworld.equals(dimension);
    }
}
