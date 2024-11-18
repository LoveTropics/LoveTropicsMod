package com.lovetropics.minigames.common.content.columns_of_chaos;

import com.lovetropics.lib.BlockBox;
import com.lovetropics.lib.entity.FireworkPalette;
import com.lovetropics.minigames.common.content.MinigameTexts;
import com.lovetropics.minigames.common.core.game.GameException;
import com.lovetropics.minigames.common.core.game.GameStopReason;
import com.lovetropics.minigames.common.core.game.IGamePhase;
import com.lovetropics.minigames.common.core.game.behavior.IGameBehavior;
import com.lovetropics.minigames.common.core.game.behavior.event.*;
import com.lovetropics.minigames.common.core.game.player.PlayerRole;
import com.lovetropics.minigames.common.core.game.player.PlayerSet;
import com.lovetropics.minigames.common.core.game.state.team.GameTeam;
import com.lovetropics.minigames.common.core.game.state.team.GameTeamKey;
import com.lovetropics.minigames.common.core.game.state.team.TeamState;
import com.lovetropics.minigames.common.core.game.util.CycledSpawner;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.ChatFormatting;
import net.minecraft.SharedConstants;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Predicate;

public final class ColumnsOfChaosBehavior implements IGameBehavior {
    public static final MapCodec<ColumnsOfChaosBehavior> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
            Codec.INT.fieldOf("pillar_height").forGetter(b -> b.pillarHeight),
            Codec.INT.optionalFieldOf("item_interval", 40).forGetter(b -> b.itemInterval),
            Codec.INT.optionalFieldOf("decrease_over_rounds", 20).forGetter(b -> b.decreaseOverRounds),
            Codec.INT.optionalFieldOf("max_countdown_ticks", (10 * 20)).forGetter(b -> b.decreaseOverRounds),
            Codec.INT.optionalFieldOf("min_countdown_ticks", (2 * 20)).forGetter(b -> b.decreaseOverRounds),
            TagKey.hashedCodec(Registries.ITEM).listOf().fieldOf("exclusion_tags").forGetter(b -> b.exclusionTags),
            Codec.STRING.fieldOf("floor_region").forGetter(b -> b.floorRegionName)
    ).apply(i, ColumnsOfChaosBehavior::new));
    private final int pillarHeight;
    private final int itemInterval;
    private final int decreaseOverRounds;
    private final int maxCountdownTicks;
    private final int minCountdownTicks;
    private final String floorRegionName;
    private final List<TagKey<Item>> exclusionTags;
    private List<ItemStack> filteredItems;
    private BlockBox floorRegion;
    @Nullable
    private State state;

    public ColumnsOfChaosBehavior(int pillarHeight, int itemInterval, int decreaseOverRounds, int maxCountdownTicks, int minCountdownTicks, List<TagKey<Item>> exclusionTags, String floorRegionName) {
        this.pillarHeight = pillarHeight;
        this.itemInterval = itemInterval;
        this.decreaseOverRounds = decreaseOverRounds;
        this.maxCountdownTicks = maxCountdownTicks;
        this.minCountdownTicks = minCountdownTicks;
        this.exclusionTags = exclusionTags;
        this.floorRegionName = floorRegionName;
    }

    @Override
    public void register(IGamePhase game, EventRegistrar events) throws GameException {
        ServerLevel level = game.level();
        floorRegion = game.mapRegions().getOrThrow(floorRegionName);
        TeamState teams = game.instanceState().getOrNull(TeamState.KEY);
        Map<GameTeamKey, CycledSpawner> teamSpawners = new HashMap<>();
        events.listen(GameTeamEvents.TEAMS_ALLOCATED, () -> {
            for (GameTeam team : teams) {
                PlayerSet teamPlayers = teams.getPlayersForTeam(team.key());
                List<BlockBox> spawnRegions = new ArrayList<>();
                for (int i = 1; i <= teamPlayers.size(); i++) {
                    String regionKey = team.key().id() + "_" + i;
                    BlockBox pillarBox = game.mapRegions().getOrThrow(regionKey);
                    level.setBlock(pillarBox.centerBlock(), Blocks.BEDROCK.defaultBlockState(), Block.UPDATE_CLIENTS);
                    for (int x = 1; x <= pillarHeight; x++) {
                        level.setBlock(pillarBox.centerBlock().offset(0, x, 0), Blocks.BEDROCK.defaultBlockState(), Block.UPDATE_CLIENTS);
                    }
                    spawnRegions.add(pillarBox.offset(0, pillarHeight + 1, 0));
                }
                teamSpawners.put(team.key(), new CycledSpawner(spawnRegions));
            }
        });
        events.listen(GamePlayerEvents.SPAWN, (playerId, spawn, role) -> {
            if (role == PlayerRole.PARTICIPANT) {
                BlockBox spawnForPlayer = getSpawnForPlayer(playerId, teams, teamSpawners);
                if (spawnForPlayer != null) {
                    spawn.teleportTo(game.level(), spawnForPlayer.centerBlock());
                }
            } else {
                spawn.teleportTo(game.level(), game.mapRegions().getOrThrow("spectator_spawn").centerBlock());
            }
        });
        events.listen(GamePhaseEvents.START, () -> {
            state = startCountingDown(game, 0);
        });
        Predicate<Holder<Item>> blackList = stack -> {
            for (TagKey<Item> exclusionTag : exclusionTags) {
                if (stack.is(exclusionTag)) {
                    return false;
                }
            }
            return true;
        };
       filteredItems = new ArrayList<>();
        CreativeModeTab.ItemDisplayParameters parameters = new CreativeModeTab.ItemDisplayParameters(game.level().enabledFeatures(), true, game.level().registryAccess());
        BuiltInRegistries.CREATIVE_MODE_TAB.holders().forEach(holder -> {
            CreativeModeTab tab = holder.value();
            if (tab.getType() != CreativeModeTab.Type.SEARCH) {
                tab.buildContents(parameters);
                tab.getDisplayItems().forEach(itemStack -> {
                    if (blackList.test(itemStack.getItemHolder())) {
                        filteredItems.add(itemStack);
                    }
                });
            }
        });
        events.listen(GamePhaseEvents.TICK, () -> {
            this.tick(game);
        });
    }

    private void tick(IGamePhase game) {
        if (state == null) {
            return;
        }

        State newState = state.tick(game);
        state = newState;

        if (newState == null) {
            game.requestStop(GameStopReason.finished());
            return;
        }

        PlayerSet participants = game.participants();
        for (ServerPlayer player : participants) {
            double y = player.getY();
            if (y < player.level().getMinBuildHeight() || y < floorRegion.min().getY() - 10) {
                game.setPlayerRole(player, PlayerRole.SPECTATOR);
                game.allPlayers().playSound(SoundEvents.ARROW_HIT_PLAYER, SoundSource.PLAYERS, 1.0f, 1.0f);
                game.allPlayers().sendMessage(MinigameTexts.ELIMINATED.apply(player.getDisplayName()));
            }
        }
    }

    private ItemStack getItemToGive(List<ItemStack> filteredItems, ServerLevel level) {
        Util.shuffle(filteredItems, level.getRandom());
        return filteredItems.getFirst().copy();
    }

    @Nullable
    private BlockBox getSpawnForPlayer(UUID playerId, TeamState teams, Map<GameTeamKey, CycledSpawner> teamSpawners) {
        GameTeamKey team = teams.getTeamForPlayer(playerId);
        if (team != null) {
            CycledSpawner teamSpawner = teamSpawners.get(team);
            if (teamSpawner != null) {
                return teamSpawner.next();
            }
        }
        return null;
    }

    CountingDown startCountingDown(IGamePhase game, int round) {
        float lerp = (float) round / decreaseOverRounds;
        long duration = Mth.floor(Mth.clampedLerp(maxCountdownTicks, minCountdownTicks, lerp));
        return new CountingDown(round + 1, game.ticks() + duration);
    }

    Interval startInterval(IGamePhase game, int round) {
        for (ServerPlayer participant : game.participants()) {
            ItemStack item = getItemToGive(filteredItems, game.level());
            participant.addItem(item.copy());
            participant.playNotifySound(SoundEvents.ARROW_HIT_PLAYER, SoundSource.NEUTRAL, 1, 1);
        }
        return new Interval(round, game.ticks() + itemInterval);
    }

    public int pillarHeight() {
        return pillarHeight;
    }

    public int itemInterval() {
        return itemInterval;
    }

    public List<TagKey<Item>> exclusionTags() {
        return exclusionTags;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (ColumnsOfChaosBehavior) obj;
        return this.pillarHeight == that.pillarHeight &&
                this.itemInterval == that.itemInterval &&
                Objects.equals(this.exclusionTags, that.exclusionTags);
    }

    @Override
    public int hashCode() {
        return Objects.hash(pillarHeight, itemInterval, exclusionTags);
    }

    @Override
    public String toString() {
        return "PillarsOfWealthBehaviour[" +
                "pillarHeight=" + pillarHeight + ", " +
                "itemInterval=" + itemInterval + ", " +
                "exclusionTags=" + exclusionTags + ']';
    }


    interface State {
        @Nullable
        State tick(IGamePhase game);
    }

    final class CountingDown implements State {

        private final int round;
        private final long breakAt;

        CountingDown(int round, long breakAt) {
            this.round = round;
            this.breakAt = breakAt;
        }

        @Override
        public State tick(IGamePhase game) {
            PlayerSet players = game.allPlayers();
            long time = game.ticks();

            long ticksLeft = breakAt - time;
            if (ticksLeft <= 0) {
                return startInterval(game, round);
            }

            long secondsLeft = ticksLeft / SharedConstants.TICKS_PER_SECOND;
            if (ticksLeft % 10 == 0) {
                Component message = ColumnsOfChaosTexts.NEW_ITEM_IN.apply(secondsLeft).withStyle(ChatFormatting.GOLD);
                players.sendMessage(message, true);
            }

            return this;
        }
    }

    final class Interval implements State {
        private final int round;
        private final long nextAt;

        Interval(int round, long nextAt) {
            this.round = round;
            this.nextAt = nextAt;
        }

        @Override
        public State tick(IGamePhase game) {
            long time = game.ticks();
            if (time > nextAt) {
                return startCountingDown(game, round + 1);
            }
            return this;
        }

    }

    record Ending(long endAt) implements State {
        @Override
        public State tick(IGamePhase game) {
            if (game.ticks() > endAt) {
                return null;
            }
            for (ServerPlayer player : game.participants()) {
                if (!player.isSpectator() && game.random().nextInt(10) == 0) {
                    BlockPos fireworksPos = BlockPos.containing(player.getEyePosition()).above();
                    FireworkPalette.DYE_COLORS.spawn(fireworksPos, game.level());
                }
            }
            return this;
        }
    }

}
