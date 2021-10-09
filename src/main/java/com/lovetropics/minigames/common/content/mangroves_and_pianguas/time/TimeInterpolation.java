package com.lovetropics.minigames.common.content.mangroves_and_pianguas.time;

import net.minecraft.client.Minecraft;
import net.minecraft.client.world.ClientWorld;

public final class TimeInterpolation {
    private static int speed = 0;

    public static void updateSpeed(int newSpeed) {
        speed = newSpeed;
    }

    public static void handleTick(ClientWorld world) {
        if (speed != 0 && !(Minecraft.getInstance().isGamePaused() && Minecraft.getInstance().isSingleplayer())) {
            world.setDayTime(world.getDayTime() + speed);
        }
    }
}
