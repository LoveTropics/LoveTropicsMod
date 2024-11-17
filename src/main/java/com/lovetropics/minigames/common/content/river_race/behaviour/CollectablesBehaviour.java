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
import com.lovetropics.minigames.common.core.game.behavior.action.GameActionParameter;
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
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
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

public final class CollectablesBehaviour implements IGameBehavior, IGameState {
    public static final MapCodec<CollectablesBehaviour> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
            ExtraCodecs.nonEmptyList(Collectable.CODEC.listOf()).fieldOf("collectables").forGetter(CollectablesBehaviour::collectables)
    ).apply(i, CollectablesBehaviour::new));

    public static final GameStateKey<CollectablesBehaviour> COLLECTABLES = GameStateKey.create("collectables");
    private final List<Collectable> collectables;

    public CollectablesBehaviour(List<Collectable> collectables) {
        this.collectables = collectables;
    }

    private final Map<BlockPos, Collectable> monumentSlots = new HashMap<>();
    private final LongSet placedCollectables = new LongOpenHashSet();

    private final Map<String, GameTeamKey> firstTeamToCollect = new HashMap<>();

    @Nullable
    public Collectable getCollectableForZone(String zone) {
        for (Collectable collectable : collectables) {
            if (collectable.zone.equals(zone)) {
                return collectable;
            }
        }
        return null;
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

        return onCollectablePlaced(game, player, team, placedCollectable, pos);
    }

    private InteractionResult onCollectablePlaced(IGamePhase game, ServerPlayer player, GameTeam team, Collectable collectable, BlockPos slotPos) {
        if (firstTeamToCollect.putIfAbsent(collectable.zone, team.key()) == null) {
            GameActionContext context = GameActionContext.builder()
                    .set(GameActionParameter.TEAM, team)
                    .set(GameActionParameter.NAME, Component.literal(collectable.zoneDisplayName()))
                    .build();
            collectable.onCompleteAction.apply(game, context, game.allPlayers());
        }
        game.invoker(RiverRaceEvents.COLLECTABLE_PLACED).onCollectablePlaced(player, team, slotPos);
        FireworkPalette.DYE_COLORS.spawn(slotPos.above(), game.level());
        return InteractionResult.PASS;
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

    public record Collectable(String zone, String zoneDisplayName, ItemStack collectable,
                              List<String> monumentSlotRegions,
                              GameActionList<ServerPlayer> onCompleteAction) {
        public static final Codec<Collectable> CODEC = RecordCodecBuilder.create(i -> i.group(
                Codec.STRING.fieldOf("zone").forGetter(Collectable::zone),
                Codec.STRING.fieldOf("zone_display_name").forGetter(Collectable::zoneDisplayName),
                MoreCodecs.ITEM_STACK.fieldOf("item").forGetter(Collectable::collectable),
                ExtraCodecs.nonEmptyList(Codec.STRING.listOf()).fieldOf("monument_slot_region").forGetter(Collectable::monumentSlotRegions),
                GameActionList.PLAYER_CODEC.fieldOf("on_complete").forGetter(Collectable::onCompleteAction)
        ).apply(i, Collectable::new));
    }
}
