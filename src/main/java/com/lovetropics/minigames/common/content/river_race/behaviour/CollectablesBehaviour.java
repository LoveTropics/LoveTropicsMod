package com.lovetropics.minigames.common.content.river_race.behaviour;

import com.lovetropics.lib.codec.MoreCodecs;
import com.lovetropics.lib.entity.FireworkPalette;
import com.lovetropics.minigames.common.content.river_race.RiverRaceTexts;
import com.lovetropics.minigames.common.content.river_race.event.RiverRaceEvents;
import com.lovetropics.minigames.common.core.game.GameException;
import com.lovetropics.minigames.common.core.game.IGamePhase;
import com.lovetropics.minigames.common.core.game.behavior.IGameBehavior;
import com.lovetropics.minigames.common.core.game.behavior.action.GameActionContext;
import com.lovetropics.minigames.common.core.game.behavior.action.GameActionList;
import com.lovetropics.minigames.common.core.game.behavior.event.EventRegistrar;
import com.lovetropics.minigames.common.core.game.behavior.event.GamePhaseEvents;
import com.lovetropics.minigames.common.core.game.behavior.event.GamePlayerEvents;
import com.lovetropics.minigames.common.core.game.state.GameStateKey;
import com.lovetropics.minigames.common.core.game.state.GameStateMap;
import com.lovetropics.minigames.common.core.game.state.IGameState;
import com.lovetropics.minigames.common.core.game.state.statistics.StatisticKey;
import com.lovetropics.minigames.common.core.game.state.team.GameTeam;
import com.lovetropics.minigames.common.core.game.state.team.GameTeamKey;
import com.lovetropics.minigames.common.core.game.state.team.TeamState;
import com.mojang.math.Transformation;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.minecraft.ChatFormatting;
import net.minecraft.SharedConstants;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Display;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;

public final class CollectablesBehaviour implements IGameBehavior, IGameState {
    public static final MapCodec<CollectablesBehaviour> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
            ExtraCodecs.nonEmptyList(Collectable.CODEC.listOf()).fieldOf("collectables").forGetter(CollectablesBehaviour::collectables)
    ).apply(i, CollectablesBehaviour::new));

    public static final GameStateKey<CollectablesBehaviour> COLLECTABLES = GameStateKey.create("collectables");
    public static final int COUNTDOWN_SECONDS = 45;
    private final List<Collectable> collectables;

    public CollectablesBehaviour(List<Collectable> collectables) {
        this.collectables = collectables;
    }

    private final Map<BlockPos, Collectable> monumentSlots = new HashMap<>();
    private final LongSet placedCollectables = new LongOpenHashSet();

    private final Map<String, GameTeamKey> firstTeamToCollect = new HashMap<>();
    @Nullable
    private Countdown countdown;

    @Nullable
    public Collectable getCollectableForZone(String zone) {
        for (Collectable collectable : collectables) {
            if (collectable.zone.equals(zone)) {
                return collectable;
            }
        }
        return null;
    }

    public void spawnCollectableItem(IGamePhase game, Collectable collectable, Vec3 pos) {
        game.level().addFreshEntity(new ItemEntity(game.level(), pos.x, pos.y, pos.z, collectable.collectable.copy()));
    }

    @Override
    public void registerState(IGamePhase game, GameStateMap phaseState, GameStateMap instanceState) {
        phaseState.register(COLLECTABLES, this);
    }

    @Override
    public void register(IGamePhase game, EventRegistrar events) throws GameException {
        TeamState teams = game.instanceState().getOrThrow(TeamState.KEY);

        for (Collectable collectable : collectables) {
            collectable.onCompleteAction.register(game, events);
            for (String region : collectable.monumentSlotRegions) {
                for (BlockPos pos : game.mapRegions().getOrThrow(region)) {
                    monumentSlots.put(pos.immutable(), collectable);
                }
            }
        }

        events.listen(GamePhaseEvents.TICK, () -> {
            if (countdown != null){
                countdown.tick(game);
            }
        });
        events.listen(GamePhaseEvents.CREATE, () ->
                monumentSlots.forEach((pos, collectable) -> spawnCollectableDisplay(game, collectable, pos.getCenter()))
        );
        events.listen(GamePlayerEvents.PLACE_BLOCK, (player, pos, placed, placedOn, placedItemStack) -> {
            Collectable expectedCollectable = monumentSlots.get(pos);
            Collectable placedCollectable = getMatchingCollectable(placedItemStack);
			if (expectedCollectable != null || placedCollectable != null) {
				return tryPlaceCollectable(game, teams, player, pos, expectedCollectable, placedCollectable);
			}
			return InteractionResult.PASS;
		});
        events.listen(GamePlayerEvents.BREAK_BLOCK, (player, pos, state, hand) -> {
			if (placedCollectables.contains(pos.asLong())) {
                return InteractionResult.FAIL;
			}
            return InteractionResult.PASS;
        });
    }

    private void spawnCollectableDisplay(IGamePhase game, Collectable collectable, Vec3 position) {
        Display.ItemDisplay itemDisplay = EntityType.ITEM_DISPLAY.create(game.level());
        itemDisplay.setPos(position);
        itemDisplay.setItemStack(collectable.collectable().copy());
        itemDisplay.setTransformation(new Transformation(null, null, new Vector3f(0.2f, 0.2f, 0.2f), null));
        game.level().addFreshEntity(itemDisplay);
    }

    private InteractionResult tryPlaceCollectable(IGamePhase game, TeamState teams, ServerPlayer player, BlockPos pos, @Nullable Collectable expectedCollectable, @Nullable Collectable placedCollectable) {
        if (placedCollectable == null || !Objects.equals(expectedCollectable, placedCollectable)) {
            player.playNotifySound(SoundEvents.VILLAGER_NO, SoundSource.PLAYERS, 1.0f, 1.0f);
            player.sendSystemMessage(RiverRaceTexts.CANT_PLACE_COLLECTABLE, true);
            return InteractionResult.FAIL;
        }

        GameTeamKey teamKey = teams.getTeamForPlayer(player);
        GameTeam team = teamKey != null ? teams.getTeamByKey(teamKey) : null;
        if (team == null) {
            return InteractionResult.FAIL;
        }

        // If the players somehow broke the old one - don't let them continue to trigger it and gain points
        if (!placedCollectables.add(pos.asLong())) {
            return InteractionResult.FAIL;
        }

        return onCollectablePlaced(game, team, placedCollectable, pos);
    }

    private InteractionResult onCollectablePlaced(IGamePhase game, GameTeam team, Collectable collectable, BlockPos slotPos) {
        if (firstTeamToCollect.putIfAbsent(collectable.zone, team.key()) == null) {
            addEffectsForPlacedCollectable(game, collectable, team);
            // Start microgames countdown
            countdown = new Countdown(game.ticks() + (SharedConstants.TICKS_PER_SECOND * COUNTDOWN_SECONDS), (unused) -> {
                collectable.onCompleteAction.apply(game, GameActionContext.EMPTY);
                countdown = null;
            });
        }
        collectable.unlocksZone.ifPresent(zone -> game.invoker(RiverRaceEvents.UNLOCK_ZONE).onUnlockZone(team, zone));
        game.statistics().forTeam(team.key()).incrementInt(StatisticKey.VICTORY_POINTS, collectable.victoryPoints);
        FireworkPalette.DYE_COLORS.spawn(slotPos.above(), game.level());
        return InteractionResult.PASS;
    }

    private static void addEffectsForPlacedCollectable(IGamePhase game, Collectable collectable, GameTeam team) {
        Component teamName = team.config().styledName();
        game.allPlayers().showTitle(null, RiverRaceTexts.COLLECTABLE_PLACED_TITLE.apply(teamName, collectable.zoneDisplayName()), 20, 40, 20);
        game.allPlayers().sendMessage(RiverRaceTexts.COLLECTABLE_PLACED.apply(teamName, collectable.zoneDisplayName(), COUNTDOWN_SECONDS));

        game.allPlayers().playSound(SoundEvents.RAID_HORN.value(), SoundSource.NEUTRAL, 1f, 1);
    }

    @Nullable
    private Collectable getMatchingCollectable(ItemStack itemStack) {
        for (Collectable collectable : collectables) {
            if (ItemStack.isSameItemSameComponents(collectable.collectable, itemStack)) {
                return collectable;
            }
        }
        return null;
    }

    public List<Collectable> collectables() {
        return collectables;
    }

    private static class Countdown {

        private final long endTicks;
        private final Consumer<Void> end;

        public Countdown(long endTicks, Consumer<Void> end) {
            this.endTicks = endTicks;
            this.end = end;
        }

        public void tick(IGamePhase game){
            long ticks = game.ticks();
            if(ticks >= endTicks){
                end.accept(null);
            } else if(ticks % 20 == 0) {
                long remainingTicks = endTicks - ticks;
                long remainingSeconds = (remainingTicks / SharedConstants.TICKS_PER_SECOND);
                if(remainingSeconds == 30 || remainingSeconds == 15 || remainingSeconds == 10) {
                    game.allPlayers().sendMessage(RiverRaceTexts.GAMES_START_IN.apply(remainingSeconds).withStyle(ChatFormatting.GREEN));
                } else if((remainingTicks / SharedConstants.TICKS_PER_SECOND) <= 5) {
                    game.allPlayers().playSound(SoundEvents.UI_BUTTON_CLICK.value(), SoundSource.NEUTRAL, 0.2f, 1f);
                    game.allPlayers().showTitle(Component.literal(remainingTicks / SharedConstants.TICKS_PER_SECOND + "").withStyle(ChatFormatting.GREEN), 10, 20, 10);
                }
            }
        }
    }

    public record Collectable(String zone, Optional<String> unlocksZone, String zoneDisplayName, ItemStack collectable,
                              List<String> monumentSlotRegions,
                              int victoryPoints, GameActionList<Void> onCompleteAction) {
        public static final Codec<Collectable> CODEC = RecordCodecBuilder.create(i -> i.group(
                Codec.STRING.fieldOf("zone").forGetter(Collectable::zone),
                Codec.STRING.optionalFieldOf("unlocks_zone").forGetter(Collectable::unlocksZone),
                Codec.STRING.fieldOf("zone_display_name").forGetter(Collectable::zoneDisplayName),
                MoreCodecs.ITEM_STACK.fieldOf("item").forGetter(Collectable::collectable),
                ExtraCodecs.nonEmptyList(Codec.STRING.listOf()).fieldOf("monument_slot_region").forGetter(Collectable::monumentSlotRegions),
                Codec.INT.fieldOf("victory_points").forGetter(Collectable::victoryPoints),
                GameActionList.VOID_CODEC.fieldOf("on_complete").forGetter(Collectable::onCompleteAction)
        ).apply(i, Collectable::new));
    }
}
