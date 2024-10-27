package com.lovetropics.minigames.common.content.river_race.behaviour;

import com.lovetropics.lib.BlockBox;
import com.lovetropics.lib.codec.MoreCodecs;
import com.lovetropics.minigames.common.content.river_race.RiverRaceTexts;
import com.lovetropics.minigames.common.core.game.GameException;
import com.lovetropics.minigames.common.core.game.IGamePhase;
import com.lovetropics.minigames.common.core.game.behavior.IGameBehavior;
import com.lovetropics.minigames.common.core.game.behavior.event.EventRegistrar;
import com.lovetropics.minigames.common.core.game.behavior.event.GamePlayerEvents;
import com.lovetropics.minigames.common.core.game.behavior.instances.action.GiveRewardAction;
import com.lovetropics.minigames.common.core.game.state.GameStateKey;
import com.lovetropics.minigames.common.core.game.state.IGameState;
import com.lovetropics.minigames.common.core.game.state.team.GameTeam;
import com.lovetropics.minigames.common.core.game.state.team.GameTeamKey;
import com.lovetropics.minigames.common.core.game.state.team.TeamState;
import com.lovetropics.minigames.common.util.BlockStatePredicate;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.List;

public class CollectablesBehaviour implements IGameBehavior, IGameState {
    public static final MapCodec<CollectablesBehaviour> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
            ExtraCodecs.nonEmptyList(Collectable.CODEC.listOf()).fieldOf("collectables").forGetter(CollectablesBehaviour::getCollectables)
    ).apply(i, CollectablesBehaviour::new));

    public static final GameStateKey<CollectablesBehaviour> COLLECTABLES = GameStateKey.create("collectables");

    private final List<Collectable> collectables;

    public CollectablesBehaviour(List<Collectable> collectables) {
        this.collectables = collectables;
    }

    public List<Collectable> getCollectables() {
        return collectables;
    }

    @Nullable
    public Collectable getCollectableForZone(String zone){
        for (Collectable collectable : collectables) {
            if(collectable.zone.equals(zone)){
                return collectable;
            }
        }
        return null;
    }

    @Override
    public void register(IGamePhase game, EventRegistrar events) throws GameException {
        game.instanceState().register(COLLECTABLES, this);
        TeamState teams = game.instanceState().getOrNull(TeamState.KEY);
        events.listen(GamePlayerEvents.PLACE_BLOCK, ((player, pos, placed, placedOn) -> {
            for (Collectable collectable : collectables) {
                for (String monumentSlotRegion : collectable.monumentSlotRegions()) {
                    BlockBox region = game.mapRegions().getAny(monumentSlotRegion);
                    if(region != null && region.contains(pos)){
                        if(!collectable.monumentPredicate.test(placed)){
                            return InteractionResult.FAIL;
                        } else {
                            GameTeamKey teamKey = teams.getTeamForPlayer(player);
                            GameTeam teamByKey = teams.getTeamByKey(teamKey);
                            game.allPlayers().showTitle(RiverRaceTexts.COLLECTABLE_PLACED.apply(teamByKey.config().styledName(), collectable.zoneDisplayName()), 20, 40, 20);
                            // Sound Effect
                            game.allPlayers().playSound(SoundEvents.RAID_HORN.value(), SoundSource.NEUTRAL, 0.5f, 1);
                            // Victory Points
                            // Start microgames countdown
                            return InteractionResult.PASS;
                        }
                    }
                }
            }
            return InteractionResult.PASS;
        }));
    }

    public record Collectable(String zone, String zoneDisplayName, ItemStack collectable, List<String> monumentSlotRegions, BlockStatePredicate monumentPredicate, int victoryPoints) {
        public static final Codec<Collectable> CODEC = RecordCodecBuilder.create(i -> i.group(
                Codec.STRING.fieldOf("zone").forGetter(Collectable::zone),
                Codec.STRING.fieldOf("zone_display_name").forGetter(Collectable::zoneDisplayName),
                MoreCodecs.ITEM_STACK.fieldOf("item").forGetter(Collectable::collectable),
                ExtraCodecs.nonEmptyList(Codec.STRING.listOf()).fieldOf("monument_slot_region").forGetter(Collectable::monumentSlotRegions),
                BlockStatePredicate.CODEC.fieldOf("monument_predicate").forGetter(Collectable::monumentPredicate),
                Codec.INT.fieldOf("victory_points").forGetter(Collectable::victoryPoints)
        ).apply(i, Collectable::new));
    }
}
