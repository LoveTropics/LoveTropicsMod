package com.lovetropics.minigames.common.content.paint_party;

import com.lovetropics.lib.BlockBox;
import com.lovetropics.lib.codec.MoreCodecs;
import com.lovetropics.minigames.common.content.river_race.behaviour.CollectablesBehaviour;
import com.lovetropics.minigames.common.core.game.GameException;
import com.lovetropics.minigames.common.core.game.IGamePhase;
import com.lovetropics.minigames.common.core.game.behavior.IGameBehavior;
import com.lovetropics.minigames.common.core.game.behavior.event.EventRegistrar;
import com.lovetropics.minigames.common.core.game.behavior.event.GamePhaseEvents;
import com.lovetropics.minigames.common.core.game.behavior.event.GamePlayerEvents;
import com.lovetropics.minigames.common.core.game.player.PlayerRole;
import com.lovetropics.minigames.common.core.game.state.statistics.StatisticKey;
import com.lovetropics.minigames.common.core.game.state.team.GameTeam;
import com.lovetropics.minigames.common.core.game.state.team.GameTeamKey;
import com.lovetropics.minigames.common.core.game.state.team.TeamState;
import com.lovetropics.minigames.common.core.network.SetForcedPoseMessage;
import com.lovetropics.minigames.common.core.network.trivia.TriviaAnswerResponseMessage;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.world.RandomizableContainer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.storage.loot.LootTable;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.network.PacketDistributor;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Predicate;

public record PaintPartyBehaviour(Map<GameTeamKey, TeamConfig> teamConfigs, BlockState neutralBlock, int startAmmo, int ammoRechargeTicks) implements IGameBehavior {
    public static final MapCodec<PaintPartyBehaviour> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
            Codec.unboundedMap(GameTeamKey.CODEC, TeamConfig.CODEC).optionalFieldOf("teams", Map.of()).forGetter(b -> b.teamConfigs),
            MoreCodecs.BLOCK_STATE.optionalFieldOf("neutral_block", Blocks.WHITE_CONCRETE.defaultBlockState()).forGetter(PaintPartyBehaviour::neutralBlock),
            Codec.INT.optionalFieldOf("starting_ammo", 32).forGetter(PaintPartyBehaviour::startAmmo),
            Codec.INT.optionalFieldOf("ammo_recharge_ticks", 5).forGetter(PaintPartyBehaviour::ammoRechargeTicks)
    ).apply(i, PaintPartyBehaviour::new));

    @Override
    public void register(IGamePhase game, EventRegistrar events) throws GameException {
        ServerLevel level = game.level();
        TeamState teams = game.instanceState().getOrNull(TeamState.KEY);
        Map<GameTeamKey, BlockBox> spawnRegions = new HashMap<>();
        for (Map.Entry<GameTeamKey, TeamConfig> teamEntry : PaintPartyBehaviour.this.teamConfigs.entrySet()) {
            spawnRegions.put(teamEntry.getKey(), game.mapRegions().getOrThrow(teamEntry.getValue().spawnRegion()));
        }
        events.listen(GamePlayerEvents.SPAWN, (player, spawnBuilder, playerRole) -> {
            if(playerRole == PlayerRole.PARTICIPANT){
                GameTeamKey teamKey = teams.getTeamForPlayer(player);
                if(teamKey == null){
                    return;
                }
                TeamConfig teamConfig = teamConfigs.get(teamKey);
                spawnBuilder.run(serverPlayer -> {
                    ItemStack copy = teamConfig.ammoItem.copy();
                    copy.setCount(startAmmo);
                    serverPlayer.getInventory().clearContent();
                    serverPlayer.getInventory().setItem(0, copy);
                });
            }
        });
        events.listen(GamePlayerEvents.TICK, (player) -> {
            GameTeamKey teamKey = teams.getTeamForPlayer(player);
            if(teamKey == null){
                return;
            }
            BlockPos playerPos = player.getOnPos();
            for (Map.Entry<GameTeamKey, BlockBox> entry : spawnRegions.entrySet()) {
                if(!entry.getKey().equals(teamKey)){
                    if(entry.getValue().contains(playerPos)){
                        return;
                    }
                }
            }
            TeamConfig teamConfig = getTeamConfig(teamKey);
            BlockState blockState = game.level().getBlockState(playerPos);
            if(!blockState.isAir() && blockState.is(Tags.Blocks.DYED)){
                if(!blockState.is(teamConfig.blockTag())){
                    if(player.isSwimming()){
                        player.setSwimming(false);
                        player.setForcedPose(null);
                        PacketDistributor.sendToPlayer(player, new SetForcedPoseMessage(Optional.empty()));
                        return;
                    }
                    boolean canPlace = false;
                    for(Direction d : Direction.Plane.HORIZONTAL)
                    {
                        if(game.level().getBlockState(playerPos.relative(d)).is(teamConfig.blockTag())){
                            canPlace = true;
                            break;
                        }
                    }
                    if(!canPlace){
                        return;
                    }
                    // Check if player has any blocks to place
                    if(player.getInventory().hasAnyMatching(itemStack -> itemStack.is(teamConfig.itemTag()))) {
                        // Player has blocks to place
                        if (!blockState.equals(neutralBlock)) {
                            // Enemy team block, remove score from enemy
                            GameTeamKey teamFromBlockState = getTeamFromBlockState(blockState);
                            if(teamFromBlockState != null) {
                                game.statistics().forTeam(teamFromBlockState).incrementInt(StatisticKey.POINTS, -1);
                            }
                        }
                        game.level().setBlock(playerPos, teamConfig.blockType(), Block.UPDATE_CLIENTS);
                        player.getInventory().removeItem(0, 1);
                        game.statistics().forTeam(teamKey).incrementInt(StatisticKey.POINTS, 1);
                    }
                } else {
                    // If we're swimming, reload.
                    if(player.getForcedPose() == Pose.SWIMMING){
                        if(!player.isShiftKeyDown()){
                            player.setForcedPose(null);
                            PacketDistributor.sendToPlayer(player, new SetForcedPoseMessage(Optional.empty()));
                        } else {
                            if (player.getInventory().countItem(teamConfig.ammoItem.getItem()) < startAmmo) {
                                if (game.ticks() % ammoRechargeTicks() == 0) {
                                    player.getInventory().add(teamConfig.ammoItem.copy());
                                }
                            }
                        }
                    } else if (player.isShiftKeyDown()) {
                        if(player.getForcedPose() != Pose.SWIMMING) {
                            player.setForcedPose(Pose.SWIMMING);
                            PacketDistributor.sendToPlayer(player, new SetForcedPoseMessage(Optional.of(Pose.SWIMMING)));
                        }
                    }
                }
            }
        });
    }

    public TeamConfig getTeamConfig(GameTeamKey teamKey){
        return teamConfigs.get(teamKey);
    }

    @Nullable
    public GameTeamKey getTeamFromBlockState(BlockState blockState){
        for (Map.Entry<GameTeamKey, TeamConfig> entry : teamConfigs.entrySet()) {
            if(entry.getValue().blockType.equals(blockState)){
                return entry.getKey();
            }
        }
        return null;
    }
    private int calculateTeamScore(){
        int score = 0;
        return score;
    }

    public record TeamConfig(String spawnRegion,
                             BlockState blockType,
                             ItemStack ammoItem,
                             TagKey<Block> blockTag, TagKey<Item> itemTag){
        public static final Codec<TeamConfig> CODEC = RecordCodecBuilder.create(i -> i.group(
                Codec.STRING.fieldOf("spawn_region").forGetter(TeamConfig::spawnRegion),
                MoreCodecs.BLOCK_STATE.fieldOf("block").forGetter(TeamConfig::blockType),
                MoreCodecs.ITEM_STACK.fieldOf("ammo_item").forGetter(TeamConfig::ammoItem),
                TagKey.hashedCodec(Registries.BLOCK).fieldOf("block_tag").forGetter(TeamConfig::blockTag),
                TagKey.hashedCodec(Registries.ITEM).fieldOf("item_tag").forGetter(TeamConfig::itemTag)
        ).apply(i, TeamConfig::new));
    }
}
