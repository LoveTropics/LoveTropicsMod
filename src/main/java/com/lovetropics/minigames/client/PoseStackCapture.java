package com.lovetropics.minigames.client;

import com.lovetropics.minigames.mixin.client.PoseStackAccessor;
import com.mojang.blaze3d.vertex.PoseStack;

import java.util.Deque;

public class PoseStackCapture {
    private PoseStackCapture() {
    }

    public static int get(PoseStack poseStack) {
        Deque<PoseStack.Pose> state = ((PoseStackAccessor) poseStack).getPoseStackState();
        return state.size();
    }

    public static void restore(PoseStack poseStack, int size) {
        Deque<PoseStack.Pose> state = ((PoseStackAccessor) poseStack).getPoseStackState();
        while (state.size() > size) {
            state.removeLast();
        }
    }
}
