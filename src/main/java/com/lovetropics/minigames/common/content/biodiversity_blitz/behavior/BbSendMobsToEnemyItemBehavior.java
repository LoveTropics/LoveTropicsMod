package com.lovetropics.minigames.common.content.biodiversity_blitz.behavior;

import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import com.lovetropics.minigames.common.content.biodiversity_blitz.behavior.event.BbEvents;
import com.lovetropics.minigames.common.content.biodiversity_blitz.entity.BbMobSpawner.BbEntityTypes;
import com.lovetropics.minigames.common.content.biodiversity_blitz.plot.Plot;
import com.lovetropics.minigames.common.content.biodiversity_blitz.plot.PlotsState;
import com.lovetropics.minigames.common.core.game.GameException;
import com.lovetropics.minigames.common.core.game.IGamePhase;
import com.lovetropics.minigames.common.core.game.behavior.IGameBehavior;
import com.lovetropics.minigames.common.core.game.behavior.event.EventRegistrar;
import com.lovetropics.minigames.common.core.game.behavior.event.GamePhaseEvents;
import com.lovetropics.minigames.common.core.game.behavior.event.GamePlayerEvents;
import com.lovetropics.minigames.common.util.Codecs;
import com.lovetropics.minigames.common.util.StackData;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.minecraft.ChatFormatting;
import net.minecraft.core.HolderSet;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.Item;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public final class BbSendMobsToEnemyItemBehavior implements IGameBehavior {
    public static final MapCodec<BbSendMobsToEnemyItemBehavior> CODEC = Codecs.ITEMS.fieldOf("item")
            .xmap(BbSendMobsToEnemyItemBehavior::new, b -> b.items);

    public static final StackData<Map<BbEntityTypes, Integer>> ENEMIES_TO_SEND = StackData.unboundedMap("bb_mobs_to_send_to_enemies", BbEntityTypes.CODEC, Codec.intRange(0, Integer.MAX_VALUE));

    private final HolderSet<Item> items;

    public BbSendMobsToEnemyItemBehavior(HolderSet<Item> items) {
        this.items = items;
    }

    private Multimap<Plot, Entity> sentEnemies;

    @Override
    public void register(IGamePhase game, EventRegistrar events) throws GameException {
        events.listen(BbEvents.MODIFY_WAVE_MODS, (entities, random, world, plot, waveIndex) -> entities.addAll(sentEnemies.removeAll(plot)));
        events.listen(GamePhaseEvents.START, () -> sentEnemies = Multimaps.synchronizedMultimap(Multimaps.newListMultimap(new HashMap<>(), LinkedList::new)));
        events.listen(GamePhaseEvents.STOP, reason -> {
            sentEnemies.clear();
            sentEnemies = null;
        });

        final var plots = game.getState().getOrThrow(PlotsState.KEY);
        events.listen(GamePlayerEvents.USE_ITEM, (player, hand) -> {
            final var item = player.getItemInHand(hand);
            if (items.contains(item.getItemHolder())) {
                final var playerPlot = plots.getPlotFor(player);

                ENEMIES_TO_SEND.getIfSuccessful(item).ifPresent(entities -> StreamSupport.stream(plots.spliterator(), false).filter(p -> p != playerPlot)
                        .forEach(targetPlot -> {
                            plots.getPlayersInPlot(targetPlot).forEach(id -> Optional.ofNullable(game.getServer().getPlayerList().getPlayer(id))
                                    .ifPresent(affected -> affected.sendSystemMessage(Component.empty()
                                            .append(player.getName().copy().withStyle(ChatFormatting.AQUA))
                                            .append(" has sent you a few mobs as a gift! Next wave you will encounter the following mobs in addition: ")
                                            .append(buildMessage(entities)))));
                            sentEnemies.putAll(targetPlot, entities.entrySet().stream()
                                    .flatMap(entry -> repeat(() -> entry.getKey().create(player.level(), targetPlot), entry.getValue()))
                                    .toList());
                        }));

                item.shrink(1);
                return InteractionResult.CONSUME;
            }

            return InteractionResult.PASS;
        });
    }

    public Component buildMessage(Map<BbEntityTypes, Integer> entities) {
        MutableComponent component = Component.empty();
        final var itr = entities.entrySet().iterator();
        while (itr.hasNext()) {
            final var next = itr.next();
            component.append(String.valueOf(next.getValue())).append("x ").append(next.getKey().getName().withStyle(ChatFormatting.GOLD));

            if (itr.hasNext()) {
                component = component.append(", ");
            }
        }

        return component;
    }

    private static <T> Stream<T> repeat(Supplier<T> value, int amount) {
        final var builder = Stream.<T>builder();
        for (int i = 0; i < amount; i++) {
            builder.accept(value.get());
        }
        return builder.build();
    }

    @Mod.EventBusSubscriber(Dist.CLIENT)
    public static final class Client {
        @SubscribeEvent
        static void appendTooltips(final RenderTooltipEvent.GatherComponents event) {
            ENEMIES_TO_SEND.getIfSuccessful(event.getItemStack()).ifPresent(entities -> entities.forEach((entity, count) ->
                    event.getTooltipElements().add(Either.left(Component.literal(count.toString()).append("x ").append(entity.getName().withStyle(ChatFormatting.GOLD))))));
        }
    }
}
