package com.lovetropics.minigames.common.content.treasure_dig;

import com.lovetropics.lib.BlockBox;
import com.lovetropics.lib.codec.MoreCodecs;
import com.lovetropics.minigames.common.content.de_a_coudre.DeACoudreBehavior;
import com.lovetropics.minigames.common.core.game.GameException;
import com.lovetropics.minigames.common.core.game.IGamePhase;
import com.lovetropics.minigames.common.core.game.behavior.IGameBehavior;
import com.lovetropics.minigames.common.core.game.behavior.event.EventRegistrar;
import com.lovetropics.minigames.common.core.game.behavior.event.GameActionEvents;
import com.lovetropics.minigames.common.core.game.behavior.event.GamePhaseEvents;
import com.lovetropics.minigames.common.core.game.behavior.event.GamePlayerEvents;
import com.lovetropics.minigames.common.core.game.behavior.instances.world.FillChestsByMarkerBehavior;
import com.lovetropics.minigames.common.core.game.player.PlayerRole;
import com.lovetropics.minigames.common.core.game.state.statistics.StatisticKey;
import com.lovetropics.minigames.common.core.game.state.team.GameTeam;
import com.lovetropics.minigames.common.core.game.state.team.GameTeamKey;
import com.lovetropics.minigames.common.core.game.state.team.TeamState;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.RandomizableContainer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.storage.loot.LootTable;
import org.apache.commons.lang3.mutable.MutableBoolean;

import java.util.Map;

public record TreasureDigBehaviour(String chestsRegion, Map<BlockState,
        ResourceKey<LootTable>> lootTables, Map<ResourceKey<Item>, Integer> pointsMap) implements IGameBehavior {
    public static final MapCodec<TreasureDigBehaviour> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
            Codec.STRING.fieldOf("chests_region").forGetter(b -> b.chestsRegion),
            Codec.unboundedMap(MoreCodecs.BLOCK_STATE, ResourceKey.codec(Registries.LOOT_TABLE)).optionalFieldOf("loot_tables", Map.of()).forGetter(b -> b.lootTables),
            Codec.unboundedMap(ResourceKey.codec(Registries.ITEM), Codec.INT).optionalFieldOf("points_map", Map.of()).forGetter(b -> b.pointsMap)
    ).apply(i, TreasureDigBehaviour::new));

    @Override
    public void register(IGamePhase game, EventRegistrar events) throws GameException {
        ServerLevel level = game.level();
        TeamState teams = game.instanceState().getOrNull(TeamState.KEY);
        BlockBox chestRegion = game.mapRegions().getOrThrow(chestsRegion);

        // Rushes dodgy code for filling the chests
        for (Long chunk : chestRegion.asChunks()) {
            LevelChunk levelChunk = level.getChunk(ChunkPos.getX(chunk), ChunkPos.getZ(chunk));
            ObjectArrayList<Chest> chests = collectChests(levelChunk);
            for (Chest chest : chests) {
                if (level.getBlockEntity(chest.pos()) instanceof RandomizableContainer blockEntity) {
                    blockEntity.setLootTable(lootTables().get(chest.blockState), level.random.nextLong());
                }
            }
        }

        // This is not good, but there doesn't seem to be an event for when a players inventory changes?
        events.listen(GamePhaseEvents.TICK, () -> {
            for (GameTeam team : teams) {
                int teamScore = 0;
                for (ServerPlayer serverPlayer : teams.getPlayersForTeam(team.key())) {
                    teamScore += calculatePlayerScore(serverPlayer);
                }
                if(game.statistics().forTeam(team).getOr(StatisticKey.POINTS, 0) != teamScore) {
                    game.statistics().forTeam(team).set(StatisticKey.POINTS, teamScore);
                }
            }
        });
    }

    private int calculatePlayerScore(ServerPlayer serverPlayer){
        int score = 0;
        for(ItemStack itemStack : serverPlayer.getInventory().items) {
            if(itemStack.isEmpty()){
                continue;
            }
            score += pointsMap().getOrDefault(itemStack.getItemHolder().getKey(), 0) * itemStack.getCount();
        }
        return score;
    }

    private ObjectArrayList<Chest> collectChests(LevelChunk chunk) {
        ObjectArrayList<Chest> chestPositions = new ObjectArrayList<>();
        for (BlockPos pos : chunk.getBlockEntitiesPos()) {
            if (chunk.getBlockEntity(pos) instanceof ChestBlockEntity blockEntity) {
                if(lootTables().containsKey(blockEntity.getBlockState())){
                    chestPositions.add(new Chest(pos, blockEntity.getBlockState()));
                }
            }
        }
        return chestPositions;
    }

    private record Chest(BlockPos pos, BlockState blockState) {
    }
}