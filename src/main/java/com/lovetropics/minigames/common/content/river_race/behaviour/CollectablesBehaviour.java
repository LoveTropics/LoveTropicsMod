package com.lovetropics.minigames.common.content.river_race.behaviour;

import com.lovetropics.lib.BlockBox;
import com.lovetropics.lib.codec.MoreCodecs;
import com.lovetropics.lib.entity.FireworkPalette;
import com.lovetropics.minigames.common.content.river_race.RiverRaceTexts;
import com.lovetropics.minigames.common.content.river_race.state.RiverRaceState;
import com.lovetropics.minigames.common.core.game.GameException;
import com.lovetropics.minigames.common.core.game.IGamePhase;
import com.lovetropics.minigames.common.core.game.behavior.IGameBehavior;
import com.lovetropics.minigames.common.core.game.behavior.event.EventRegistrar;
import com.lovetropics.minigames.common.core.game.behavior.event.GamePhaseEvents;
import com.lovetropics.minigames.common.core.game.behavior.event.GamePlayerEvents;
import com.lovetropics.minigames.common.core.game.impl.MultiGamePhase;
import com.lovetropics.minigames.common.core.game.state.GameStateKey;
import com.lovetropics.minigames.common.core.game.state.IGameState;
import com.lovetropics.minigames.common.core.game.state.team.GameTeam;
import com.lovetropics.minigames.common.core.game.state.team.GameTeamKey;
import com.lovetropics.minigames.common.core.game.state.team.TeamState;
import com.lovetropics.minigames.common.util.BlockStatePredicate;
import com.mojang.math.Transformation;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.ChatFormatting;
import net.minecraft.SharedConstants;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Display;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import org.joml.Vector3f;

import javax.annotation.Nullable;
import java.util.*;
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

    private final Map<String, GameTeamKey> COLLECTED_COLLECTABLES = new HashMap<>();
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

    public void queueMicrogames(IGamePhase game, Collectable collectable) {
        if (game instanceof MultiGamePhase multiGamePhase) {
            multiGamePhase.clearQueuedGames();

            final List<ResourceLocation> configs = new ArrayList<>(collectable.gamePool);
            Collections.shuffle(configs);
            multiGamePhase.queueGames(configs.subList(0, Math.min(configs.size(), collectable.gamesPerRound)));
        }
    }

    public void startQueuedMicrogame(final IGamePhase game) {
        if (game instanceof MultiGamePhase multiGamePhase) {
            multiGamePhase.startNextQueuedMicrogame(true);
        }
    }

    public void givePlayerCollectable(IGamePhase game, Collectable collectable, ServerPlayer player) {
        TeamState teams = game.instanceState().getOrNull(TeamState.KEY);
        GameTeamKey teamKey = teams.getTeamForPlayer(player);

        player.addItem(collectable.collectable().copy());
    }

    @Override
    public void register(IGamePhase game, EventRegistrar events) throws GameException {
        game.instanceState().register(COLLECTABLES, this);
        RiverRaceState riverRaceState = game.state().getOrNull(RiverRaceState.KEY);
        TeamState teams = game.instanceState().getOrNull(TeamState.KEY);
        events.listen(GamePhaseEvents.TICK, () -> {
            if(countdown != null){
                countdown.tick(game);
            }
        });
        events.listen(GamePhaseEvents.CREATE, () -> {
            for (Collectable collectable : collectables) {
                for (String monumentSlotRegion : collectable.monumentSlotRegions()) {
                    BlockBox region = game.mapRegions().getAny(monumentSlotRegion);
                    Display.BlockDisplay blockDisplay = EntityType.BLOCK_DISPLAY.create(game.level());
                    blockDisplay.setPos(region.center());
                    blockDisplay.setBlockState(collectable.blockState());
                    blockDisplay.setTransformation(new Transformation(new Vector3f(-0.1f, -0.1f, -0.1f), null, new Vector3f(0.2f, 0.2f, 0.2f), null));
                    game.level().addFreshEntity(blockDisplay);
                }
            }
        });
        events.listen(GamePlayerEvents.PLACE_BLOCK, ((player, pos, placed, placedOn) -> {
            for (Collectable collectable : collectables) {
                for (String monumentSlotRegion : collectable.monumentSlotRegions()) {
                    BlockBox region = game.mapRegions().getAny(monumentSlotRegion);
                    if (region != null && region.contains(pos)) {
                        if (!collectable.monumentPredicate.test(placed)) {
                            return InteractionResult.FAIL;
                        } else {
                            // Victory Points
                            GameTeamKey teamKey = teams.getTeamForPlayer(player);
                            GameTeam teamByKey = teams.getTeamByKey(teamKey);
                            if(!COLLECTED_COLLECTABLES.containsKey(collectable.zone)) {
                                COLLECTED_COLLECTABLES.put(collectable.zone, teamKey);
                                // Message
                                MutableComponent teamName = teamByKey.config().styledName();
                                game.allPlayers().showTitle(null, RiverRaceTexts.COLLECTABLE_PLACED_TITLE.apply(teamName, collectable.zoneDisplayName()), 20, 40, 20);
                                game.allPlayers().sendMessage(RiverRaceTexts.COLLECTABLE_PLACED.apply(teamName, collectable.zoneDisplayName(), COUNTDOWN_SECONDS));
                                // Sound Effect
                                game.allPlayers().playSound(SoundEvents.RAID_HORN.value(), SoundSource.NEUTRAL, 1f, 1);
                                queueMicrogames(game, collectable);
                                // Start microgames countdown
                                countdown = new Countdown(game.ticks() + (20 * COUNTDOWN_SECONDS), (unused) -> {
									startQueuedMicrogame(game);
									countdown = null;
								});
                            }
                            riverRaceState.addPointsToTeam(teamKey, collectable.victoryPoints);
                            FireworkPalette.DYE_COLORS.spawn(region.centerBlock().above(), game.level());
                            return InteractionResult.PASS;
                        }
                    }
                }
            }
            return InteractionResult.PASS;
        }));
    }

    public List<Collectable> collectables() {
        return collectables;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (CollectablesBehaviour) obj;
        return Objects.equals(this.collectables, that.collectables);
    }

    @Override
    public int hashCode() {
        return Objects.hash(collectables);
    }

    @Override
    public String toString() {
        return "CollectablesBehaviour[" +
                "collectables=" + collectables + ']';
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
                              List<String> monumentSlotRegions, BlockStatePredicate monumentPredicate,
                              BlockState blockState,
                              int victoryPoints, List<ResourceLocation> gamePool, int gamesPerRound) {
        public static final Codec<Collectable> CODEC = RecordCodecBuilder.create(i -> i.group(
                Codec.STRING.fieldOf("zone").forGetter(Collectable::zone),
                Codec.STRING.fieldOf("zone_display_name").forGetter(Collectable::zoneDisplayName),
                MoreCodecs.ITEM_STACK.fieldOf("item").forGetter(Collectable::collectable),
                ExtraCodecs.nonEmptyList(Codec.STRING.listOf()).fieldOf("monument_slot_region").forGetter(Collectable::monumentSlotRegions),
                BlockStatePredicate.CODEC.fieldOf("monument_predicate").forGetter(Collectable::monumentPredicate),
                MoreCodecs.BLOCK_STATE.fieldOf("monument_block_state").forGetter(Collectable::blockState),
                Codec.INT.fieldOf("victory_points").forGetter(Collectable::victoryPoints),
                ExtraCodecs.nonEmptyList(ResourceLocation.CODEC.listOf()).fieldOf("game_pool").forGetter(Collectable::gamePool),
                Codec.INT.optionalFieldOf("games_per_round", 1).forGetter(c -> c.gamesPerRound)
        ).apply(i, Collectable::new));
    }
}
