package com.lovetropics.minigames.common.techstack;

import com.google.common.collect.Queues;
import com.lovetropics.minigames.Constants;
import com.lovetropics.minigames.common.config.ConfigLT;
import com.lovetropics.minigames.common.packages.CarePackage;
import com.lovetropics.minigames.common.packages.ChatEvent;
import com.lovetropics.minigames.common.packages.GameAction;
import com.lovetropics.minigames.common.packages.SabotagePackage;
import com.lovetropics.minigames.common.techstack.websockets.WebSocketHelper;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.server.ServerLifecycleHooks;

import java.util.Queue;

@Mod.EventBusSubscriber(modid = Constants.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ActionHandler {
    public static final Queue<CarePackage> CARE_PACKAGE_QUEUE = Queues.newPriorityBlockingQueue();
    public static final Queue<SabotagePackage> SABOTAGE_PACKAGE_QUEUE = Queues.newPriorityBlockingQueue();
    public static final Queue<ChatEvent> CHAT_EVENT_QUEUE = Queues.newPriorityBlockingQueue();
    private static int carePackageLastPolledTick;
    private static int sabotagePackageLastPolledTick;
    private static int chatEventLastPolledTick;

    @SubscribeEvent
    public static void tick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        final MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server == null) {
            return;
        }
        final int tick = server.getTickCounter();

        // This is good to have...but we shouldn't need it hopefully
        WebSocketHelper.checkAndCycleConnection();

        handleCarePackages(tick);
        handleSabotagePackages(tick);
        handleChatEvents(tick);
    }

    private static void handleCarePackages(final int tick) {
        if (tick >= carePackageLastPolledTick + ConfigLT.GENERAL.carePackageDelay.get() && carePackagesPending()) {
            final GameAction action = getCarePackage();
            if (action == null) {
                return;
            }

            // Do something with the care package

            // If we did something, call this below
            TechStack.acknowledgeActionDelivery(action);

            carePackageLastPolledTick = tick;
        }
    }

    private static void handleSabotagePackages(final int tick) {
        if (tick >= sabotagePackageLastPolledTick + ConfigLT.GENERAL.sabotagePackageDelay.get() && sabotagePackagesPending()) {
            final GameAction action = getSabotagePackage();
            if (action == null) {
                return;
            }

            // Do something with the sabotage package

            // If we did something, call this below
            TechStack.acknowledgeActionDelivery(action);

            sabotagePackageLastPolledTick = tick;
        }
    }

    private static void handleChatEvents(final int tick) {
        if (tick >= chatEventLastPolledTick + ConfigLT.GENERAL.chatEventDelay.get() && chatEventsPending()) {
            final GameAction action = getChatEventPackage();
            if (action == null) {
                return;
            }

            // Do something with the sabotage package

            // If we did something, call this below
            TechStack.acknowledgeActionDelivery(action);

            chatEventLastPolledTick = tick;
        }
    }

    public static void queueAction(final GameAction action) {
        if (action instanceof CarePackage) {
            CARE_PACKAGE_QUEUE.offer((CarePackage) action);
        } else if (action instanceof SabotagePackage) {
            SABOTAGE_PACKAGE_QUEUE.offer((SabotagePackage) action);
        } else if (action instanceof ChatEvent) {
            CHAT_EVENT_QUEUE.offer((ChatEvent) action);
        }
    }

    public static CarePackage getCarePackage() {
        return CARE_PACKAGE_QUEUE.poll();
    }

    public static boolean carePackagesPending() {
        return !CARE_PACKAGE_QUEUE.isEmpty();
    }

    public static SabotagePackage getSabotagePackage() {
        return SABOTAGE_PACKAGE_QUEUE.poll();
    }

    public static boolean sabotagePackagesPending() {
        return !SABOTAGE_PACKAGE_QUEUE.isEmpty();
    }

    public static ChatEvent getChatEventPackage() {
        return CHAT_EVENT_QUEUE.poll();
    }

    public static boolean chatEventsPending() {
        return !CHAT_EVENT_QUEUE.isEmpty();
    }
}
