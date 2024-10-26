package com.lovetropics.minigames.mixin.client;

import com.lovetropics.minigames.client.game.ClientGameStateManager;
import com.lovetropics.minigames.common.core.game.client_state.GameClientStateTypes;
import net.minecraft.client.player.Input;
import net.minecraft.client.player.KeyboardInput;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(KeyboardInput.class)
public class KeyboardInputMixin extends Input {
    @Inject(method = "tick", at = @At("TAIL"))
    private void respectSwappedMovement(boolean isSneaking, float sneakingSpeedMultiplier, CallbackInfo ci) {
        if (ClientGameStateManager.getOrNull(GameClientStateTypes.SWAP_MOVEMENT) != null) {
            float oldLeft = leftImpulse;
            leftImpulse = forwardImpulse;
            forwardImpulse = oldLeft;
        }
    }
}
