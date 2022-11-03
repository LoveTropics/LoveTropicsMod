package com.lovetropics.minigames.common.content.biodiversity_blitz.behavior.tutorial;

import com.lovetropics.minigames.common.content.biodiversity_blitz.behavior.event.BbEvents;
import com.lovetropics.minigames.common.content.biodiversity_blitz.entity.BbTutorialHuskEntity;
import com.lovetropics.minigames.common.content.biodiversity_blitz.plot.Plot;
import com.lovetropics.minigames.common.content.biodiversity_blitz.plot.PlotsState;
import com.lovetropics.minigames.common.content.biodiversity_blitz.plot.plant.Plant;
import com.lovetropics.minigames.common.content.biodiversity_blitz.plot.plant.PlantType;
import com.lovetropics.minigames.common.core.game.GameException;
import com.lovetropics.minigames.common.core.game.IGamePhase;
import com.lovetropics.minigames.common.core.game.behavior.IGameBehavior;
import com.lovetropics.minigames.common.core.game.behavior.event.EventRegistrar;
import com.lovetropics.minigames.common.core.game.behavior.event.GameActionEvents;
import com.lovetropics.minigames.common.core.game.behavior.event.GamePhaseEvents;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Reference2ObjectMap;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FarmBlock;
import net.minecraft.world.level.block.LevelEvent;
import net.minecraft.world.level.block.state.BlockState;

// TODO: this is extremely hardcoded for now, need to split up!
public class BbTutorialAction implements IGameBehavior {
    public static final Codec<BbTutorialAction> CODEC = RecordCodecBuilder.create(i -> i.group(
            PlantType.CODEC.fieldOf("diffusa").forGetter(c -> c.diffusa),
            PlantType.CODEC.fieldOf("grass").forGetter(c -> c.grass),
            PlantType.CODEC.fieldOf("wheat").forGetter(c -> c.wheat)
    ).apply(i, BbTutorialAction::new));

    private final PlantType diffusa;
    private final PlantType grass;
    private final PlantType wheat;

    private Reference2ObjectMap<ServerPlayer, Long2ObjectMap<Runnable>> tutorialActions;

    public BbTutorialAction(PlantType diffusa, PlantType grass, PlantType wheat) {
        this.diffusa = diffusa;
        this.grass = grass;
        this.wheat = wheat;
    }

    @Override
    public void register(IGamePhase game, EventRegistrar events) throws GameException {
        PlotsState plots = game.getState().getOrThrow(PlotsState.KEY);
        tutorialActions = new Reference2ObjectOpenHashMap<>();

        events.listen(GameActionEvents.APPLY_TO_PLAYER, (context, target) -> {
            Plot playerPlot = plots.getPlotFor(target);
            if (playerPlot == null) {
                return false;
            }

            Long2ObjectMap<Runnable> actions = new Long2ObjectOpenHashMap<>();
            tutorialActions.put(target, actions);

            BlockPos sample = playerPlot.plantBounds.centerBlock();

            long ticks = game.ticks() + 4;

            Direction cw = playerPlot.forward.getClockWise();

            ticks = placeBlocks(game, target, playerPlot, sample, ticks, cw, actions);

            ticks += 40;

            // Spawn mob
            actions.put(ticks, () -> {
                BlockPos pos = sample.relative(playerPlot.forward, 12);

                Mob entity = new BbTutorialHuskEntity(EntityType.HUSK, target.level, playerPlot);

                Direction direction = playerPlot.forward.getOpposite();
                entity.moveTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, direction.toYRot(), 0);

                target.level.addFreshEntity(entity);

                entity.finalizeSpawn(target.getLevel(), target.level.getCurrentDifficultyAt(pos), MobSpawnType.MOB_SUMMONED, null, null);
            });

            ticks += 240;

            ticks = breakBlocks(game, target, playerPlot, sample, ticks, cw, actions);

            actions.put(ticks, () -> {
//                target.sendMessage(new TextComponent("Tutorial done!"), ChatType.SYSTEM, Util.NIL_UUID);
                game.getState().getOrThrow(TutorialState.KEY).finishTutorial();
            });

            return true;
        });

        events.listen(GamePhaseEvents.TICK, () -> {
            for (Long2ObjectMap<Runnable> actions : tutorialActions.values()) {
                Runnable run = actions.remove(game.ticks());
                if (run != null) {
                    run.run();
                }
            }
        });
    }

    private long placeBlocks(IGamePhase game, ServerPlayer target, Plot playerPlot, BlockPos sample, long ticks, Direction cw, Long2ObjectMap<Runnable> actions) {
        // Place diffusa
        for (int i = 0; i < 3; i++) {
            BlockPos pos = sample.relative(playerPlot.forward, 6).relative(cw, i * 2 - 2);
            actions.put(ticks, new SetPlant(game, target, playerPlot, pos, this.diffusa, SoundEvents.GRASS_BREAK));
            ticks += 8;
        }

        // First wall of grass
        for (int i = -1; i < 13; i++) {
            BlockPos pos = sample.relative(playerPlot.forward, 8).relative(cw, i - 5);
            if (playerPlot.plantBounds.contains(pos)) {
                actions.put(ticks, new SetPlant(game, target, playerPlot, pos, this.grass, SoundEvents.GRASS_STEP));
                ticks += 5;
            }
        }

        // Second wall of grass
        for (int i = -1; i < 13; i++) {
            BlockPos pos = sample.relative(playerPlot.forward, 9).relative(cw, i - 5);
            if (playerPlot.plantBounds.contains(pos)) {
                actions.put(ticks, new SetPlant(game, target, playerPlot, pos, this.grass, SoundEvents.GRASS_STEP));
                ticks += 5;
            }
        }

        // Farmland row
        for (int i = -1; i < 13; i++) {
            BlockPos pos = sample.relative(playerPlot.forward, -5).relative(cw, i - 5);
            if (playerPlot.plantBounds.contains(pos) && target.level.getBlockState(pos.below()).getBlock() == Blocks.GRASS_BLOCK) {
                actions.put(ticks, new SetFarmland(target, pos.below()));
                ticks += 5;
            }
        }

        // Farmland row
        for (int i = -1; i < 13; i++) {
            BlockPos pos = sample.relative(playerPlot.forward, -4).relative(cw, i - 5);
            if (playerPlot.plantBounds.contains(pos) && target.level.getBlockState(pos.below()).getBlock() == Blocks.GRASS_BLOCK) {
                actions.put(ticks, new SetFarmland(target, pos.below()));
                ticks += 5;
            }
        }

        // Add wheat

        for (int i = -1; i < 13; i++) {
            BlockPos pos = sample.relative(playerPlot.forward, -5).relative(cw, i - 5);

            if (!playerPlot.plantBounds.contains(pos) || isNotGrass(target, pos)) {
                continue;
            }

            actions.put(ticks, new SetPlant(game, target, playerPlot, pos, this.wheat, SoundEvents.GRASS_STEP));
            ticks += 5;
        }

        for (int i = -1; i < 13; i++) {
            BlockPos pos = sample.relative(playerPlot.forward, -4).relative(cw, i - 5);

            if (!playerPlot.plantBounds.contains(pos) || isNotGrass(target, pos)) {
                continue;
            }

            actions.put(ticks, new SetPlant(game, target, playerPlot, pos, this.wheat, SoundEvents.GRASS_STEP));
            ticks += 5;
        }

        return ticks;
    }

    private long breakBlocks(IGamePhase game, ServerPlayer target, Plot playerPlot, BlockPos sample, long ticks, Direction cw, Long2ObjectMap<Runnable> actions) {
        // Remove wheat

        for (int i = -1; i < 13; i++) {
            BlockPos pos = sample.relative(playerPlot.forward, -4).relative(cw, i - 5);

            if (!playerPlot.plantBounds.contains(pos) || isNotGrass(target, pos)) {
                continue;
            }

            actions.put(ticks, new BreakPlant(game, target, playerPlot, pos, this.wheat, SoundEvents.GRASS_STEP));
            ticks += 3;
        }

        for (int i = -1; i < 13; i++) {
            BlockPos pos = sample.relative(playerPlot.forward, -5).relative(cw, i - 5);

            if (!playerPlot.plantBounds.contains(pos) || isNotGrass(target, pos)) {
                continue;
            }

            actions.put(ticks, new BreakPlant(game, target, playerPlot, pos, this.wheat, SoundEvents.GRASS_STEP));
            ticks += 3;
        }

        // Farmland row
        for (int i = -1; i < 13; i++) {
            BlockPos pos = sample.relative(playerPlot.forward, -4).relative(cw, i - 5);
            // how does this work??? there's farmland here!! but removing this breaks it?!?!
            if (playerPlot.plantBounds.contains(pos) && target.level.getBlockState(pos.below()).getBlock() == Blocks.GRASS_BLOCK) {
                actions.put(ticks, new SetGrass(target, pos.below()));
                ticks += 3;
            }
        }

        // Farmland row
        for (int i = -1; i < 13; i++) {
            BlockPos pos = sample.relative(playerPlot.forward, -5).relative(cw, i - 5);
            if (playerPlot.plantBounds.contains(pos) && target.level.getBlockState(pos.below()).getBlock() == Blocks.GRASS_BLOCK) {
                actions.put(ticks, new SetGrass(target, pos.below()));
                ticks += 3;
            }
        }

        // Second wall of grass
        for (int i = -1; i < 13; i++) {
            BlockPos pos = sample.relative(playerPlot.forward, 9).relative(cw, i - 5);
            if (playerPlot.plantBounds.contains(pos)) {
                actions.put(ticks, new BreakPlant(game, target, playerPlot, pos, this.grass, SoundEvents.GRASS_STEP));
                ticks += 3;
            }
        }

        // First wall of grass
        for (int i = -1; i < 13; i++) {
            BlockPos pos = sample.relative(playerPlot.forward, 8).relative(cw, i - 5);
            if (playerPlot.plantBounds.contains(pos)) {
                actions.put(ticks, new BreakPlant(game, target, playerPlot, pos, this.grass, SoundEvents.GRASS_STEP));
                ticks += 3;
            }
        }

        // Place diffusa
        for (int i = 0; i < 3; i++) {
            BlockPos pos = sample.relative(playerPlot.forward, 6).relative(cw, i * 2 - 2);
            actions.put(ticks, new BreakPlant(game, target, playerPlot, pos, this.diffusa, SoundEvents.GRASS_BREAK));
            ticks += 3;
        }

        return ticks;
    }

    private boolean isNotGrass(ServerPlayer target, BlockPos pos) {
        BlockState grass = target.level.getBlockState(pos.below());
        return !grass.is(BlockTags.DIRT) && !grass.is(Blocks.FARMLAND);
    }

    public record SetPlant(IGamePhase game, ServerPlayer target, Plot playerPlot, BlockPos sample, PlantType type, SoundEvent sound) implements Runnable {
        public void run() {
            Plant plant = game.invoker(BbEvents.PLACE_PLANT).placePlant(target, playerPlot, sample, type).getObject();
            if (plant != null) {
                target.level.levelEvent(null, LevelEvent.PARTICLES_DESTROY_BLOCK, sample, Block.getId(target.level.getBlockState(plant.coverage().getOrigin())));
                target.level.playSound(null, sample, sound, SoundSource.BLOCKS, 0.4F, 1.0F);
            }
        }
    }

    public record BreakPlant(IGamePhase game, ServerPlayer target, Plot playerPlot, BlockPos sample, PlantType type, SoundEvent sound) implements Runnable {
        public void run() {
            Plant plant = playerPlot.plants.getPlantAt(sample);
            if (plant == null) {
                return;
            }

            boolean placed = game.invoker(BbEvents.BREAK_PLANT).breakPlant(target, playerPlot, plant);
            if (placed) {
                target.level.levelEvent(null, LevelEvent.PARTICLES_DESTROY_BLOCK, sample, Block.getId(target.level.getBlockState(plant.coverage().getOrigin())));
                target.level.playSound(null, sample, sound, SoundSource.BLOCKS, 0.4F, 1.0F);
            }
        }
    }

    public record SetFarmland(ServerPlayer target, BlockPos pos) implements Runnable {
        public void run() {
            target.level.setBlock(pos, Blocks.FARMLAND.defaultBlockState().setValue(FarmBlock.MOISTURE, 7), 3);
            target.level.playSound(null, pos, SoundEvents.HOE_TILL, SoundSource.BLOCKS, 1.0F, 1.0F);
        }
    }

    public record SetGrass(ServerPlayer target, BlockPos pos) implements Runnable {
        public void run() {
            target.level.setBlock(pos, Blocks.GRASS_BLOCK.defaultBlockState(), 3);
            target.level.playSound(null, pos, SoundEvents.HOE_TILL, SoundSource.BLOCKS, 1.0F, 1.0F);
        }
    }
}
