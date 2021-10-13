package com.lovetropics.minigames.common.content.mangroves_and_pianguas.behavior;

import com.lovetropics.lib.BlockBox;
import com.lovetropics.minigames.common.content.MinigameTexts;
import com.lovetropics.minigames.common.content.mangroves_and_pianguas.FriendlyExplosion;
import com.lovetropics.minigames.common.content.mangroves_and_pianguas.behavior.event.MpEvents;
import com.lovetropics.minigames.common.content.mangroves_and_pianguas.entity.MpHuskEntity;
import com.lovetropics.minigames.common.content.mangroves_and_pianguas.entity.MpPillagerEntity;
import com.lovetropics.minigames.common.content.mangroves_and_pianguas.plot.Plot;
import com.lovetropics.minigames.common.content.mangroves_and_pianguas.plot.PlotsState;
import com.lovetropics.minigames.common.content.mangroves_and_pianguas.plot.plant.Plant;
import com.lovetropics.minigames.common.content.mangroves_and_pianguas.plot.plant.PlantCoverage;
import com.lovetropics.minigames.common.content.mangroves_and_pianguas.plot.plant.PlantType;
import com.lovetropics.minigames.common.content.mangroves_and_pianguas.plot.plant.state.JackOLantern;
import com.lovetropics.minigames.common.content.mangroves_and_pianguas.plot.plant.state.Pumpkin;
import com.lovetropics.minigames.common.core.dimension.DimensionUtils;
import com.lovetropics.minigames.common.core.game.IGamePhase;
import com.lovetropics.minigames.common.core.game.behavior.IGameBehavior;
import com.lovetropics.minigames.common.core.game.behavior.event.*;
import com.lovetropics.minigames.common.core.game.player.PlayerRole;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.minecraft.block.*;
import net.minecraft.block.trees.BirchTree;
import net.minecraft.entity.*;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.ai.attributes.ModifiableAttributeInstance;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.merchant.villager.VillagerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.play.server.SExplosionPacket;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.*;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.Difficulty;
import net.minecraft.world.Explosion;
import net.minecraft.world.GameType;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.storage.ServerWorldInfo;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nullable;
import java.util.*;

// TODO: needs to be split up!
public final class MpBehavior implements IGameBehavior {
    private static final Codec<Difficulty> DIFFICULTY_CODEC = Codec.STRING.xmap(Difficulty::byName, Difficulty::getTranslationKey);

    public static final Codec<MpBehavior> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            DIFFICULTY_CODEC.fieldOf("difficulty").forGetter(c -> c.difficulty)
    ).apply(instance, MpBehavior::new));

    private static final AttributeModifier GRASS_SLOW = new AttributeModifier(UUID.fromString("0b5baa42-2576-11ec-9621-0242ac130002"), "Slowness from tall grass", -0.65F, AttributeModifier.Operation.MULTIPLY_TOTAL);
    private static final RegistryObject<EntityType<?>> KOA = RegistryObject.of(new ResourceLocation("tropicraft","koa"), ForgeRegistries.ENTITIES);

    private static final ResourceLocation IRIS = new ResourceLocation("tropicraft", "iris");
    private static final Map<Difficulty, Double> DEATH_DECREASE = new HashMap<>();
    private static final Map<Difficulty, Double> MOB_SCALAR = new HashMap<>();

    private static final Direction[] DIRECTIONS = Direction.values();

    private static final BirchTree BIRCH_TREE = new BirchTree();

    static {
        DEATH_DECREASE.put(Difficulty.EASY, 0.9);
        DEATH_DECREASE.put(Difficulty.NORMAL, 0.8);
        DEATH_DECREASE.put(Difficulty.HARD, 0.5);

        MOB_SCALAR.put(Difficulty.EASY, 0.5);
        MOB_SCALAR.put(Difficulty.NORMAL, 1.0);
        MOB_SCALAR.put(Difficulty.HARD, 1.5);
    }

    private final Difficulty difficulty;

    private IGamePhase game;
    private PlotsState plots;

    private long gameStartTime = 0;
    private int sentWaves = 0;

    public MpBehavior(Difficulty difficulty) {
        this.difficulty = difficulty;
    }

    @Override
    public void register(IGamePhase game, EventRegistrar events) {
        this.game = game;
        this.plots = game.getState().getOrThrow(PlotsState.KEY);

        events.listen(GamePlayerEvents.ADD, player -> setupPlayerAsRole(player, null));
        events.listen(GamePlayerEvents.SET_ROLE, (player, role, lastRole) -> setupPlayerAsRole(player, role));
        events.listen(MpEvents.ASSIGN_PLOT, this::onAssignPlot);
        events.listen(GamePhaseEvents.TICK, () -> tick(game));
        events.listen(GamePhaseEvents.START, () -> start(game));
        events.listen(GamePlayerEvents.INTERACT_ENTITY, this::interactWithEntity);
        events.listen(GamePlayerEvents.PLACE_BLOCK, this::onPlaceBlock);
        events.listen(GamePlayerEvents.BREAK_BLOCK, this::onBreakBlock);
        events.listen(GameLivingEntityEvents.TICK, this::entityTick);
        events.listen(GamePlayerEvents.DEATH, this::onPlayerDeath);
        events.listen(GameWorldEvents.EXPLOSION_DETONATE, this::onExplosion);
        // Don't grow any trees- we handle that ourselves
        events.listen(GameWorldEvents.SAPLING_GROW, (w, p) -> ActionResultType.FAIL);
        events.listen(GamePlayerEvents.ATTACK, this::onAttack);
        // No mob drops
        events.listen(GameLivingEntityEvents.MOB_DROP, (e, d, r) -> ActionResultType.FAIL);
        events.listen(GameLivingEntityEvents.FARMLAND_TRAMPLE, this::onFarmlandTrample);
    }

    private void setupPlayerAsRole(ServerPlayerEntity player, @Nullable PlayerRole role) {
        if (role == PlayerRole.SPECTATOR) {
            this.spawnSpectator(player);
        }
    }

    private void spawnSpectator(ServerPlayerEntity player) {
        // Teleport players to center for now
        // TODO: hardcoded region
        teleportToRegion(player, BlockBox.of(new BlockPos(0, 105, 0)));

        player.setGameType(GameType.SPECTATOR);
    }

    private void onAssignPlot(ServerPlayerEntity player, Plot plot) {
        teleportToRegion(player, plot.spawn);

        if (KOA.isPresent()) {
            ServerWorld world = game.getWorld();

            Vector3d center = plot.shop.getCenter();

            VillagerEntity koa = (VillagerEntity) KOA.get().create(world);
            koa.setLocationAndAngles(center.getX(), center.getY(), center.getZ(), 0, 0);
            koa.setPosition(center.getX(), center.getY() - 0.5, center.getZ());

            world.addEntity(koa);

            koa.onInitialSpawn(world, world.getDifficultyForLocation(new BlockPos(center)), SpawnReason.MOB_SUMMONED, null, null);

            koa.setNoAI(true);
        }
    }

    private void start(IGamePhase game) {
        this.gameStartTime = game.ticks();

        if (game.getWorld().getWorldInfo() instanceof ServerWorldInfo) {
            ((ServerWorldInfo)(game.getWorld().getWorldInfo())).setDifficulty(this.difficulty);
        }
    }

    private void onExplosion(Explosion explosion, List<BlockPos> affectedBlocks, List<Entity> affectedEntities) {
        // Remove players from friendly explosions
        if (explosion instanceof FriendlyExplosion) {
            affectedEntities.removeIf(e -> e instanceof ServerPlayerEntity);
        }

        affectedEntities.removeIf(e -> e instanceof VillagerEntity);

        // Blocks should not explode
        affectedBlocks.clear();
    }

    private void interactWithEntity(ServerPlayerEntity player, Entity target, Hand hand) {
        if (KOA.isPresent() && KOA.get() == target.getType()) {
            MpMerchant merchant = new MpMerchant(player);
            merchant.openMerchantContainer(player, new TranslationTextComponent("ltminigames.minigame.mp_trading"), 1);
        }
    }

    private ActionResultType onAttack(ServerPlayerEntity player, Entity target) {
        // disable pvp and pvv (player vs villager)
        if (target instanceof VillagerEntity || target instanceof PlayerEntity) {
            return ActionResultType.FAIL;
        }

        return ActionResultType.PASS;
    }

    private ActionResultType onPlaceBlock(ServerPlayerEntity player, BlockPos pos, BlockState placed, BlockState placedOn) {
        Plot plot = plots.getPlotFor(player);
        if (plot == null) {
            return ActionResultType.PASS;
        }

        // Check to see if the farmland block is below the plot
        if (placed.getBlock() == Blocks.FARMLAND && plot.bounds.contains(pos.up())) {
            return ActionResultType.PASS;
        }

        if (!plot.bounds.contains(pos)) {
            return ActionResultType.FAIL;
        }

        if (placed.getBlock() == Blocks.MELON) {
            plot.plants.addPlant(PlantType.MELON, PlantCoverage.of(pos));
        }

        if (placed.getBlock() == Blocks.JACK_O_LANTERN) {
            // Jack o lanterns live for 2 minutes
            plot.plants.addPlant(PlantType.JACK_O_LANTERN, PlantCoverage.of(pos), new JackOLantern(2 * 60 * 20));
        }

        if (placed.getBlock() == Blocks.PUMPKIN) {
            plot.plants.addPlant(PlantType.PUMPKIN, PlantCoverage.of(pos), new Pumpkin(30));
        }

        return ActionResultType.PASS;
    }

    private ActionResultType onBreakBlock(ServerPlayerEntity player, BlockPos pos, BlockState state) {
        Plot plot = plots.getPlotFor(player);
        if (plot == null) {
            return ActionResultType.PASS;
        }

        if (!plot.bounds.contains(pos)) {
            return ActionResultType.FAIL;
        }

        Plant<?> plant = plot.plants.getPlantAt(pos);
        if (plant != null) {
            // TODO: people can perpetually replace jack o' lanterns to reset their timer. Is there a better way to do this?

            ActionResultType result = this.onBreakPlant(pos, plant);
            plot.plants.removePlant(plant);
            return result;
        }

        return ActionResultType.PASS;
    }

    // TODO: hardcoded handling
    private ActionResultType onBreakPlant(BlockPos pos, Plant<?> plant) {
        ServerWorld world = game.getWorld();

        if (plant.type() == PlantType.BIRCH_TREE) {
            for (BlockPos treePos : plant.coverage()) {
                world.removeBlock(treePos, false);
            }

            ItemEntity sapling = new ItemEntity(world, pos.getX(), pos.getY(), pos.getZ(), new ItemStack(Blocks.BIRCH_SAPLING));
            world.addEntity(sapling);

            return ActionResultType.FAIL;
        }

        if (plant.type() == PlantType.MELON) {
            // TODO: removing a plant should always remove its blocks- we want to extract this logic out
            world.removeBlock(pos, false);

            ItemEntity melon = new ItemEntity(world, pos.getX(), pos.getY(), pos.getZ(), new ItemStack(Blocks.MELON));
            world.addEntity(melon);

            return ActionResultType.FAIL;
        }

        return ActionResultType.PASS;
    }

    private ActionResultType onFarmlandTrample(Entity entity, BlockPos pos, BlockState state) {
        if (entity instanceof ServerPlayerEntity) {
            // Don't trample farmland if it's not found in the player's plot. Normalizes with up() as farmland is below the plot
            Plot plot = plots.getPlotFor(entity);
            if (plot != null && !plot.bounds.contains(pos.up())) {
                return ActionResultType.FAIL;
            }
        }

        return ActionResultType.PASS;
    }

    private void entityTick(LivingEntity entity) {
        if (!(entity instanceof ServerPlayerEntity)) {
            BlockState state = entity.getBlockState();
            ModifiableAttributeInstance speed = entity.getAttribute(Attributes.MOVEMENT_SPEED);

            if (state.getBlock() == Blocks.GRASS) {
                speed.applyNonPersistentModifier(GRASS_SLOW);
            } else {
                if (speed.hasModifier(GRASS_SLOW)) {
                    speed.removeModifier(GRASS_SLOW);
                }
            }
        }
    }

    private ActionResultType onPlayerDeath(ServerPlayerEntity player, DamageSource damageSource) {
        Plot plot = plots.getPlotFor(player);
        if (plot == null) {
            return ActionResultType.PASS;
        }

        Vector3d center = plot.spawn.getCenter();

        player.teleport(player.getServerWorld(), center.x, center.y, center.z, player.rotationYaw, player.rotationPitch);
        player.setHealth(20.0F);

        // Resets all currency from the player's inventory and adds a new stack with 80% of the amount.
        // A better way of just removing 20% of the existing stacks could be done but this was chosen for the time being to save time
        int totalCount = 0;

        for (ItemStack stack : player.inventory.mainInventory) {
            if (stack.getItem() == Items.SUNFLOWER) {
                totalCount += stack.getCount();
                player.inventory.deleteStack(stack);
            }
        }

        for (ItemStack stack : player.inventory.offHandInventory) {
            if (stack.getItem() == Items.SUNFLOWER) {
                totalCount += stack.getCount();
                player.inventory.deleteStack(stack);
            }
        }

        int targetCount = (int) (totalCount * DEATH_DECREASE.get(this.difficulty));

        // First insert all the full stacks
        int stacks = targetCount / 64;
        for (int i = 0; i < stacks; i++) {
            player.addItemStackToInventory(new ItemStack(Items.SUNFLOWER, 64));
            // Reduce the target by 64 as we just inserted a full stack
            targetCount -= 64;
        }

        // Add the remaining items
        player.addItemStackToInventory(new ItemStack(Items.SUNFLOWER, targetCount));
        player.playSound(SoundEvents.ENTITY_ARROW_HIT_PLAYER, 0.18F, 0.1F);
        player.sendStatusMessage(MinigameTexts.mpDeathDecrease(totalCount - targetCount), false);

        return ActionResultType.FAIL;
    }

    // TODO: staggered tick per player because this much logic in a single tick is not ideal
    private void tick(IGamePhase game) {
        ServerWorld world = game.getWorld();
        Random random = world.getRandom();
        long ticks = this.gameStartTime + game.ticks();

        for (Plot plot : plots) {
            this.tickPlot(world, plot, ticks);
        }

        // Tick every second
        if (ticks % 20 == 0) {
            for (ServerPlayerEntity player : game.getParticipants()) {
                Plot plot = plots.getPlotFor(player);
                if (plot == null) continue;

                for (BlockPos pos : plot.bounds) {
                    BlockState state = world.getBlockState(pos);

                    if (Registry.BLOCK.getKey(state.getBlock()).equals(IRIS)) {
                        List<MobEntity> entities = world.getEntitiesWithinAABB(MobEntity.class, new AxisAlignedBB(pos.add(-5, -5, -5), pos.add(5, 5, 5)));

                        entities.removeIf(e -> e instanceof VillagerEntity);

                        for (MobEntity entity : entities) {
                            // Add glowing effect
                            // The duration is > 20 ticks to ensure that the glowing doesn't flicker due to ticking time discrepancies
                            entity.addPotionEffect(new EffectInstance(Effects.GLOWING, 25, 1));
                        }
                    }
                }
            }
        }

        // Tick growables every 15 seconds
        if (ticks % 300 == 0) {
            for (ServerPlayerEntity player : game.getParticipants()) {
                Plot plot = plots.getPlotFor(player);
                if (plot == null) continue;

                for (BlockPos pos : plot.bounds) {
                    BlockState state = world.getBlockState(pos);

                    if (state.hasProperty(CropsBlock.AGE)) {
                        int age = state.get(CropsBlock.AGE);

                        if (age < 7) {
                            world.setBlockState(pos, state.with(CropsBlock.AGE, age + 1));
                        }
                    } else if (state.hasProperty(BeetrootBlock.BEETROOT_AGE)) {
                        // Also affects sweet berry bushes!
                        // TODO: should sweet berries grow slower? might be too op!
                        int age = state.get(BeetrootBlock.BEETROOT_AGE);

                        if (state.getBlock() == Blocks.SWEET_BERRY_BUSH) {
                            if (age < 1) {
                                world.setBlockState(pos, state.with(BeetrootBlock.BEETROOT_AGE, age + 1));
                            } else if (world.rand.nextInt(128) == 0 && age < 3) {
                                world.setBlockState(pos, state.with(BeetrootBlock.BEETROOT_AGE, age + 1));
                            }
                        } else {
                            if (age < 3) {
                                world.setBlockState(pos, state.with(BeetrootBlock.BEETROOT_AGE, age + 1));
                            }
                        }
                    } else if (state.getBlock() == Blocks.BIRCH_SAPLING) {
                        boolean grew = BIRCH_TREE.attemptGrowTree(world, world.getChunkProvider().getChunkGenerator(), pos, state, world.rand);

                        if (grew) {
                            // iterate all new tree blocks
                            LongSet treeBlocks = new LongOpenHashSet();

                            Deque<BlockPos> queue = new LinkedList<>();
                            queue.add(pos.toImmutable());

                            // DFS new blocks from trees
                            while (!queue.isEmpty()) {
                                BlockPos poll = queue.poll();

                                // TODO: prioritize trunk blocks so trunks are never cut off

                                // DFS more if this is a tree block and it's not an already globally tracked tree or a part of this current tree that we've already seen
                                if (isTreeBlock(world.getBlockState(poll)) && !plot.plants.hasPlantAt(poll) && !treeBlocks.contains(poll.toLong())) {
                                    treeBlocks.add(poll.toLong());

                                    // Go forth in all directions
                                    for (Direction value : DIRECTIONS) {
                                        queue.add(poll.offset(value));
                                    }
                                }
                            }

                            plot.plants.addPlant(PlantType.BIRCH_TREE, PlantCoverage.of(treeBlocks));
                        } else {
                            // Place red particles to indicate tree didn't grow
                        }
                    }
                }
            }
        }

        // Drop currency every 30 seconds
        if (ticks % 600 == 0) {
            for (ServerPlayerEntity player : game.getParticipants()) {
                Plot plot = plots.getPlotFor(player);
                if (plot == null) continue;

                double value = 2;

                // TODO: calculate crops, plants, and trees separately
                Set<Block> uniqueBlocks = new HashSet<>();

                for (BlockPos pos : plot.bounds) {
                    BlockState state = world.getBlockState(pos);
                    uniqueBlocks.add(state.getBlock());

                    if (state.getBlock() instanceof CropsBlock) {
                        value += 0.05;
                    } else if (state.getBlock() == Blocks.GRASS) {
                        value += 0.025;
                    } else if (state.getBlock() == Blocks.WITHER_ROSE) {
                        value += 0.075;
                    } else if (state.getBlock() == Blocks.BIRCH_LOG) {
                        value += 0.85;
                    }

                    // ...
                }

                // TODO: temp math equation
                value += (uniqueBlocks.size() / 4.0) * value;
                int count = (int) value;

                if (world.rand.nextDouble() < value - count) {
                    count++;
                }

                player.playSound(SoundEvents.ENTITY_ARROW_HIT_PLAYER, 0.18F, 1.0F);
                player.sendStatusMessage(MinigameTexts.mpCurrencyAddition(count), false);
                player.addItemStackToInventory(new ItemStack(Items.SUNFLOWER, count));
            }
        }

        // Spawn mobs every 2 minutes (in daytime)
        long timeTilNextWave = ticks % 2400;

        // Warn players of an impending wave
        // Idea: upgrade that allows you to predict waves in the future?
        if (timeTilNextWave == 1800) {
            game.getParticipants().sendMessage(MinigameTexts.mpWaveWarning());
        }

        if (timeTilNextWave == 0) {
            for (ServerPlayerEntity player : game.getParticipants()) {
                Plot plot = plots.getPlotFor(player);
                if (plot == null) continue;

                // Temp wave scaling equation- seems to work fine?
                int x = this.sentWaves / 2;
                int amount = (int) (MOB_SCALAR.get(this.difficulty) * (Math.pow(x, 1.2) + x) + 2 + random.nextInt(3));

                for (int i = 0; i < amount; i++) {
                    BlockPos pos = plot.mobSpawn.sample(random);

                    MobEntity entity = selectEntityForWave(random, world);

                    entity.setLocationAndAngles(pos.getX(), pos.getY(), pos.getZ(), 0, 0);
                    entity.setPosition(pos.getX(), pos.getY(), pos.getZ());

                    world.addEntity(entity);

                    entity.onInitialSpawn(world, world.getDifficultyForLocation(pos), SpawnReason.MOB_SUMMONED, null, null);
                }
            }

            this.sentWaves++;
        }
    }

    private void tickPlot(ServerWorld world, Plot plot, long ticks) {
        List<Plant<?>> addedPlants = new ArrayList<>();
        List<Plant<?>> removedPlants = new ArrayList<>();

        Random random = world.rand;

        if (ticks % 5 == 0) {
            for (Plant<Unit> melon : plot.plants.getPlantsByType(PlantType.MELON)) {
                AxisAlignedBB pushBounds = melon.coverage().asBounds().grow(2.0);
                List<MobEntity> entities = world.getEntitiesWithinAABB(MobEntity.class, pushBounds, entity -> !(entity instanceof VillagerEntity));
                if (!entities.isEmpty()) {
                    removedPlants.add(melon);
                    explodeMelon(world, melon.coverage());
                }
            }

            for (Plant<JackOLantern> lantern : plot.plants.getPlantsByType(PlantType.JACK_O_LANTERN)) {
                AxisAlignedBB bounds = lantern.coverage().asBounds();
                AxisAlignedBB pushBounds = bounds.grow(3.0, 2.0, 3.0);
                List<MobEntity> entities = world.getEntitiesWithinAABB(MobEntity.class, pushBounds, entity -> !(entity instanceof VillagerEntity));

                Vector3d pushFrom = bounds.getCenter();

                for (MobEntity entity : entities) {
                    Vector3d entityPos = entity.getPositionVec();

                    // Scaled so that closer values are higher, with a max of 10
                    double dist = 2.0 / (0.1 + entityPos.distanceTo(pushFrom));

                    // Angle between entity and center of lantern
                    double theta = Math.atan2(entityPos.z - pushFrom.z, entityPos.x - pushFrom.x);

                    // zoooooom
                    entity.addVelocity(dist * Math.cos(theta), 0.25, dist * Math.sin(theta));

                    // Prevent mobs from flying to the moon due to too much motion
                    Vector3d motion = entity.getMotion();
                    entity.setMotion(Math.min(motion.x, 10), Math.min(motion.y, 0.25), Math.min(motion.z, 10));

                    // Make it so that using a jack o lantern a lot will reduce alive time
                    if (random.nextInt(6) == 0) {
                        lantern.state().aliveTicks--;
                    }
                }
            }
        }

        // Reduce lantern times
        for (Plant<JackOLantern> lantern : plot.plants.getPlantsByType(PlantType.JACK_O_LANTERN)) {
            if (--lantern.state().aliveTicks <= 0) {
                removedPlants.add(lantern);

                for (BlockPos pos : lantern.coverage()) {
                    world.setBlockState(pos, Blocks.PUMPKIN.getDefaultState());
                    this.spawnBlockPoof(world, random, pos);
                }

                addedPlants.add(Plant.create(PlantType.PUMPKIN, lantern.coverage(), new Pumpkin(30)));
            }
        }

        for (Plant<Pumpkin> pumpkin : plot.plants.getPlantsByType(PlantType.PUMPKIN)) {
            AxisAlignedBB bounds = pumpkin.coverage().asBounds();
            AxisAlignedBB damageBounds = bounds.grow(1.0, 5.0, 1.0);

            List<MobEntity> entities = world.getEntitiesWithinAABB(MobEntity.class, damageBounds, entity -> !(entity instanceof VillagerEntity));
            if (entities.isEmpty()) {
                continue;
            }

            int newHealth = pumpkin.state().health - entities.size();
            if (newHealth <= 0) {
                removedPlants.add(pumpkin);

                for (BlockPos pos : pumpkin.coverage()) {
                    world.removeBlock(pos, false);
                    this.spawnBlockPoof(world, random, pos);
                }
            } else {
                pumpkin.state().health = newHealth;
            }
        }

        for (Plant<?> plant : removedPlants) {
            plot.plants.removePlant(plant);
        }

        for (Plant<?> plant : addedPlants) {
            plot.plants.addPlant(plant);
        }
    }

    private void spawnBlockPoof(ServerWorld world, Random random, BlockPos pos) {
        for (int i = 0; i < 20; i++) {
            double vx = random.nextGaussian() * 0.02;
            double vy = random.nextGaussian() * 0.02;
            double vz = random.nextGaussian() * 0.02;
            world.spawnParticle(ParticleTypes.POOF, pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, 1, vx, vy, vz, 0.15F);
        }
    }

    private static MobEntity selectEntityForWave(Random random, World world) {
        return random.nextBoolean() ? new MpPillagerEntity(EntityType.PILLAGER, world) : new MpHuskEntity(EntityType.HUSK, world);
    }


    private static boolean isTreeBlock(BlockState state) {
        // Add stuff like vines and propagules as needed

        // TODO: beehives are above the floor and have a leaves block above them
        return BlockTags.LOGS.contains(state.getBlock()) || BlockTags.LEAVES.contains(state.getBlock());
    }

    // Kaboom!
    private static void explodeMelon(ServerWorld world, PlantCoverage coverage) {
        for (BlockPos pos : coverage) {
            world.removeBlock(pos, true);

            double x = pos.getX() + 0.5;
            double y = pos.getY() + 0.5;
            double z = pos.getZ() + 0.5;

            Explosion explosion = new FriendlyExplosion(world, null, null, null, x, y, z, 4.0f, false, Explosion.Mode.BREAK);
            explosion.doExplosionA();
            explosion.doExplosionB(false);

            for (ServerPlayerEntity player : world.getPlayers()) {
                if (player.getDistanceSq(x, y, z) < 4096.0) {
                    player.connection.sendPacket(new SExplosionPacket(x, y, z, 4.0f, explosion.getAffectedBlockPositions(), explosion.getPlayerKnockbackMap().get(player)));
                }
            }
        }
    }

    private void teleportToRegion(ServerPlayerEntity player, BlockBox region) {
        BlockPos pos = region.sample(player.getRNG());
        DimensionUtils.teleportPlayerNoPortal(player, game.getDimension(), pos);
    }
}