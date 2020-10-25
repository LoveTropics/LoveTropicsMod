package com.lovetropics.minigames.common.telemetry;

import com.google.common.collect.Maps;
import com.google.common.collect.Queues;
import com.lovetropics.minigames.Constants;
import com.lovetropics.minigames.common.game_actions.GameAction;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.server.ServerLifecycleHooks;

import java.util.Map;
import java.util.Queue;

@Mod.EventBusSubscriber(modid = Constants.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class GameActionHandler
{
    public static final Map<BackendRequest, PollingGameActions> GAME_ACTION_QUEUES = Maps.newHashMap();

    @SubscribeEvent
    public static void tick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        final MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server == null) {
            return;
        }
        final int tick = server.getTickCounter();

        pollGameActions(server, tick);
    }

    private static void pollGameActions(final MinecraftServer server, final int tick) {
        for (final Map.Entry<BackendRequest, PollingGameActions> entry : GAME_ACTION_QUEUES.entrySet()) {
            final BackendRequest request = entry.getKey();
            final PollingGameActions polling = entry.getValue();

            if (tick >= polling.getLastPolledTick() + (request.getPollingIntervalSeconds() * 20))
            {
                if (!polling.getQueue().isEmpty())
                {
                    final GameAction action = polling.getQueue().poll();
                    if (action == null)
                    {
                        continue;
                    }

                    // If we resolved the action, send acknowledgement to the backend
                    if (action.resolve(server))
                    {
                        Telemetry.INSTANCE.acknowledgeActionDelivery(request, action);
                    }
                }

                polling.setLastPolledTick(tick);
            }
        }
    }

    public static void queueGameAction(final BackendRequest requestType, final GameAction action) {
        if (GAME_ACTION_QUEUES.containsKey(requestType)) {
            GAME_ACTION_QUEUES.get(requestType).getQueue().offer(action);
        } else {
            GAME_ACTION_QUEUES.put(requestType, new PollingGameActions());
        }
    }

    public static class PollingGameActions
    {
        private int lastPolledTick = 0;
        private final Queue<GameAction> queue;

        public PollingGameActions()
        {
            queue = Queues.newPriorityBlockingQueue();
        }

        public void setLastPolledTick(int tick) {
            lastPolledTick = tick;
        }

        public int getLastPolledTick()
        {
            return lastPolledTick;
        }

        public Queue<GameAction> getQueue()
        {
            return queue;
        }
    }

}
