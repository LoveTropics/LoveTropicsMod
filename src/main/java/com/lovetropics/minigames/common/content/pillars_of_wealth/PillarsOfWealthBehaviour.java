package com.lovetropics.minigames.common.content.pillars_of_wealth;

import com.lovetropics.lib.BlockBox;
import com.lovetropics.lib.codec.MoreCodecs;
import com.lovetropics.minigames.common.core.game.GameException;
import com.lovetropics.minigames.common.core.game.IGamePhase;
import com.lovetropics.minigames.common.core.game.behavior.IGameBehavior;
import com.lovetropics.minigames.common.core.game.behavior.event.EventRegistrar;
import com.lovetropics.minigames.common.core.game.behavior.event.GamePhaseEvents;
import com.lovetropics.minigames.common.core.game.behavior.event.GamePlayerEvents;
import com.lovetropics.minigames.common.core.game.behavior.event.GameTeamEvents;
import com.lovetropics.minigames.common.core.game.behavior.instances.PositionPlayersBehavior;
import com.lovetropics.minigames.common.core.game.player.PlayerRole;
import com.lovetropics.minigames.common.core.game.player.PlayerSet;
import com.lovetropics.minigames.common.core.game.state.statistics.StatisticKey;
import com.lovetropics.minigames.common.core.game.state.team.GameTeam;
import com.lovetropics.minigames.common.core.game.state.team.GameTeamKey;
import com.lovetropics.minigames.common.core.game.state.team.TeamState;
import com.lovetropics.minigames.common.core.game.util.CycledSpawner;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.RandomizableContainer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackLinkedSet;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.storage.loot.LootTable;
import org.apache.commons.lang3.mutable.MutableInt;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public record PillarsOfWealthBehaviour(int pillarHeight, int itemInterval, List<TagKey<Item>> exclusionTags) implements IGameBehavior {
    public static final MapCodec<PillarsOfWealthBehaviour> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
            Codec.INT.fieldOf("pillar_height").forGetter(b -> b.pillarHeight),
            Codec.INT.fieldOf("item_interval").forGetter(b -> b.itemInterval),
            TagKey.hashedCodec(Registries.ITEM).listOf().fieldOf("exclusion_tags").forGetter(b -> b.exclusionTags)
    ).apply(i, PillarsOfWealthBehaviour::new));

    @Override
    public void register(IGamePhase game, EventRegistrar events) throws GameException {
        ServerLevel level = game.level();
        TeamState teams = game.instanceState().getOrNull(TeamState.KEY);
        Map<GameTeamKey, CycledSpawner> teamSpawners = new HashMap<>();
        events.listen(GameTeamEvents.TEAMS_ALLOCATED, () -> {
            for (GameTeam team : teams) {
                PlayerSet teamPlayers = teams.getPlayersForTeam(team.key());
                List<BlockBox> spawnRegions = new ArrayList<>();
                for(int i = 1; i <= teamPlayers.size(); i++){
                    String regionKey = team.key().id() + "_" + i;
                    BlockBox pillarBox = game.mapRegions().getOrThrow(regionKey);
                    level.setBlock(pillarBox.centerBlock(), Blocks.BEDROCK.defaultBlockState(), Block.UPDATE_CLIENTS);
                    for(int x = 1; x <= pillarHeight; x++){
                        level.setBlock(pillarBox.centerBlock().offset(0, x, 0), Blocks.BEDROCK.defaultBlockState(), Block.UPDATE_CLIENTS);
                    }
                    spawnRegions.add(pillarBox.offset(0, pillarHeight + 1, 0));
                }
                teamSpawners.put(team.key(), new CycledSpawner(spawnRegions));
            }
        });
        events.listen(GamePlayerEvents.SPAWN, (playerId, spawn, role) -> {
            if(role == PlayerRole.PARTICIPANT){
                BlockBox spawnForPlayer = getSpawnForPlayer(playerId, teams, teamSpawners);
                if(spawnForPlayer != null){
                    spawn.teleportTo(game.level(), spawnForPlayer.centerBlock());
                }
            } else {
                spawn.teleportTo(game.level(), game.mapRegions().getOrThrow("spectator_spawn").centerBlock());
            }
        });
        Predicate<Holder<Item>> blackList = stack -> {
            for (TagKey<Item> exclusionTag : exclusionTags) {
                if(stack.is(exclusionTag)){
                    return false;
                }
            }
            return true;
        };
        List<Holder.Reference<Item>> filteredItems =  new ArrayList<>(BuiltInRegistries.ITEM.holders().filter(blackList).toList());
        MutableInt ticks = new MutableInt(0);
        events.listen(GamePhaseEvents.TICK, () -> {
            ticks.increment();
            if(ticks.getValue() == (itemInterval * 20)){
                ticks.setValue(0);
                for (ServerPlayer participant : game.participants()) {
                    ItemStack item = getItemToGive(filteredItems, game.level());
                    participant.addItem(item.copy());
                }
            }
            // Check if we need to give item, pick item to give
        });
    }

    private ItemStack getItemToGive(List<Holder.Reference<Item>> filteredItems, ServerLevel level){
        Util.shuffle(filteredItems, level.getRandom());
        return new ItemStack(filteredItems.getFirst());
    }

    @Nullable
    private BlockBox getSpawnForPlayer(UUID playerId, TeamState teams, Map<GameTeamKey, CycledSpawner> teamSpawners){
        GameTeamKey team = teams.getTeamForPlayer(playerId);
        if (team != null) {
            CycledSpawner teamSpawner = teamSpawners.get(team);
            if (teamSpawner != null) {
                return teamSpawner.next();
            }
        }
        return null;
    }
}
