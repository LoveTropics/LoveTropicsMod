package com.lovetropics.minigames.client.game.handler;

import com.lovetropics.minigames.client.game.ClientGameStateManager;
import com.lovetropics.minigames.common.core.game.client_state.GameClientStateTypes;
import com.lovetropics.minigames.common.core.game.client_state.instance.FogClientState;
import com.mojang.blaze3d.shaders.FogShape;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ViewportEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(Dist.CLIENT)
public class GameFogModifier {
    @SubscribeEvent
    static void onModifyFog(final ViewportEvent.ComputeFogColor event) {
        FogClientState state = ClientGameStateManager.getOrNull(GameClientStateTypes.FOG);
        if (state == null) {
            return;
        }

        event.setBlue(state.blue());
        event.setGreen(state.green());
        event.setRed(state.red());
    }

    @SubscribeEvent
    static void onRenderFog(final ViewportEvent.RenderFog event) {
        FogClientState state = ClientGameStateManager.getOrNull(GameClientStateTypes.FOG);
        if (state == null) {
            return;
        }

        state.fogType().ifPresent(type -> {
            if (type == FogClientState.FogType.SKY) {
                event.setNearPlaneDistance(0.0f);
                event.setFarPlaneDistance(event.getRenderer().getRenderDistance());
                event.setFogShape(FogShape.CYLINDER);
            } else {
                event.setNearPlaneDistance(event.getRenderer().getRenderDistance() * 0.05f);
                event.setFarPlaneDistance(Math.min(event.getRenderer().getRenderDistance(), 192f) * .5f);
                event.setFogShape(FogShape.SPHERE);
            }
        });
        state.fogShape().ifPresent(shape -> event.setFogShape(shape.fogShape));
    }
}