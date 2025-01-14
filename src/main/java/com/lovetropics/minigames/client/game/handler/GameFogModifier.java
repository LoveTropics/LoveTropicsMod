package com.lovetropics.minigames.client.game.handler;

import com.lovetropics.minigames.client.game.ClientGameStateManager;
import com.lovetropics.minigames.common.core.game.client_state.GameClientStateTypes;
import com.lovetropics.minigames.common.core.game.client_state.instance.FogClientState;
import com.mojang.blaze3d.shaders.FogShape;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ViewportEvent;

@EventBusSubscriber(Dist.CLIENT)
public class GameFogModifier {
    @SubscribeEvent(priority = EventPriority.HIGH)
    static void onModifyFog(final ViewportEvent.ComputeFogColor event) {
        FogClientState state = ClientGameStateManager.getOrNull(GameClientStateTypes.FOG);
        if (state == null) {
            return;
        }

        event.setRed(state.red());
        event.setGreen(state.green());
        event.setBlue(state.blue());
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    static void onRenderFog(final ViewportEvent.RenderFog event) {
        FogClientState state = ClientGameStateManager.getOrNull(GameClientStateTypes.FOG);
        if (state == null) {
            return;
        }

        event.setCanceled(true);

        state.fogType().ifPresent(type -> {
            if (type == FogClientState.FogType.SKY) {
                event.setNearPlaneDistance(0.0f);
                event.setFarPlaneDistance(event.getRenderer().getRenderDistance());
                event.setFogShape(FogShape.CYLINDER);
            } else {
                event.setNearPlaneDistance(Math.min(state.nearDistance(), event.getNearPlaneDistance()));
                event.setFarPlaneDistance(Math.min(state.farDistance(), event.getFarPlaneDistance()));
                event.setFogShape(FogShape.SPHERE);
            }
        });
        state.fogShape().ifPresent(shape -> event.setFogShape(switch (shape) {
            case SPHERE -> FogShape.SPHERE;
            case CYLINDER -> FogShape.CYLINDER;
        }));
    }
}
