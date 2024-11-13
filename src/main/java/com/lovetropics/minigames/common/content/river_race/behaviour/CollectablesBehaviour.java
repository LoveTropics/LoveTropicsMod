package com.lovetropics.minigames.common.content.river_race.behaviour;

import com.lovetropics.lib.BlockBox;
import com.lovetropics.lib.codec.MoreCodecs;
import com.lovetropics.lib.entity.FireworkPalette;
import com.lovetropics.minigames.common.content.river_race.RiverRaceTexts;
import com.lovetropics.minigames.common.content.river_race.state.RiverRaceState;
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
import com.lovetropics.minigames.common.core.game.state.team.GameTeam;
import com.lovetropics.minigames.common.core.game.state.team.GameTeamKey;
import com.lovetropics.minigames.common.core.game.state.team.TeamState;
import com.mojang.math.Transformation;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
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
        RiverRaceState riverRaceState = game.state().getOrThrow(RiverRaceState.KEY);
        TeamState teams = game.instanceState().getOrThrow(TeamState.KEY);

        for (Collectable collectable : collectables) {
            collectable.onCompleteAction.register(game, events);
        }

        events.listen(GamePhaseEvents.TICK, () -> {
            if (countdown != null){
                countdown.tick(game);
            }
        });
        events.listen(GamePhaseEvents.CREATE, () -> {
            for (Collectable collectable : collectables) {
                for (String monumentSlotRegion : collectable.monumentSlotRegions()) {
                    BlockBox region = game.mapRegions().getOrThrow(monumentSlotRegion);
                    spawnCollectableDisplay(game, collectable, region.center());
                }
            }
        });
        events.listen(GamePlayerEvents.PLACE_BLOCK, (player, pos, placed, placedOn, placedItemStack) -> {
            Collectable placedCollectable = getMatchingCollectable(placedItemStack);
            if (placedCollectable == null) {
                return InteractionResult.PASS;
            }
            return tryPlaceCollectable(game, player, pos, placedCollectable, teams, riverRaceState);
        });
    }

    private void spawnCollectableDisplay(IGamePhase game, Collectable collectable, Vec3 position) {
        Display.ItemDisplay itemDisplay = EntityType.ITEM_DISPLAY.create(game.level());
        itemDisplay.setPos(position);
        itemDisplay.setItemStack(collectable.collectable().copy());
        itemDisplay.setTransformation(new Transformation(null, null, new Vector3f(0.2f, 0.2f, 0.2f), null));
        game.level().addFreshEntity(itemDisplay);
    }

    private InteractionResult tryPlaceCollectable(IGamePhase game, ServerPlayer player, BlockPos pos, Collectable placedCollectable, TeamState teams, RiverRaceState riverRaceState) {
        BlockPos slot = getMonumentSlotAt(placedCollectable, game, pos);
        if (slot == null) {
            player.playNotifySound(SoundEvents.VILLAGER_NO, SoundSource.PLAYERS, 1.0f, 1.0f);
            player.sendSystemMessage(RiverRaceTexts.CANT_PLACE_COLLECTABLE, true);
            return InteractionResult.FAIL;
        }
        return tryPlaceIntoMonumentSlot(game, teams, riverRaceState, player, placedCollectable, slot);
    }

    private InteractionResult tryPlaceIntoMonumentSlot(IGamePhase game, TeamState teams, RiverRaceState riverRaceState, ServerPlayer player, Collectable collectable, BlockPos slotCenterPos) {
        GameTeamKey teamKey = teams.getTeamForPlayer(player);
        if (teamKey == null) {
            return InteractionResult.FAIL;
        }
        GameTeam team = teams.getTeamByKey(teamKey);
        if (firstTeamToCollect.putIfAbsent(collectable.zone, teamKey) == null) {
            addEffectsForPlacedCollectable(game, collectable, team);
            // Start microgames countdown
            countdown = new Countdown(game.ticks() + (SharedConstants.TICKS_PER_SECOND * COUNTDOWN_SECONDS), (unused) -> {
                collectable.onCompleteAction.apply(game, GameActionContext.EMPTY);
                countdown = null;
            });
        }
        riverRaceState.addPointsToTeam(teamKey, collectable.victoryPoints);
        FireworkPalette.DYE_COLORS.spawn(slotCenterPos.above(), game.level());
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

    @Nullable
    private BlockPos getMonumentSlotAt(Collectable collectable, IGamePhase game, BlockPos pos) {
        for (BlockBox region : game.mapRegions().getAll(collectable.monumentSlotRegions)) {
            if (region.contains(pos)) {
                return region.centerBlock();
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

    public record Collectable(String zone, String zoneDisplayName, ItemStack collectable,
                              List<String> monumentSlotRegions,
                              int victoryPoints, GameActionList<Void> onCompleteAction) {
        public static final Codec<Collectable> CODEC = RecordCodecBuilder.create(i -> i.group(
                Codec.STRING.fieldOf("zone").forGetter(Collectable::zone),
                Codec.STRING.fieldOf("zone_display_name").forGetter(Collectable::zoneDisplayName),
                MoreCodecs.ITEM_STACK.fieldOf("item").forGetter(Collectable::collectable),
                ExtraCodecs.nonEmptyList(Codec.STRING.listOf()).fieldOf("monument_slot_region").forGetter(Collectable::monumentSlotRegions),
                Codec.INT.fieldOf("victory_points").forGetter(Collectable::victoryPoints),
                GameActionList.VOID_CODEC.fieldOf("on_complete").forGetter(Collectable::onCompleteAction)
        ).apply(i, Collectable::new));
    }
}
