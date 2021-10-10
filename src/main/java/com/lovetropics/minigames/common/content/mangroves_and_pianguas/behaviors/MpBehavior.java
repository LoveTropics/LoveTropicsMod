package com.lovetropics.minigames.common.content.mangroves_and_pianguas.behaviors;

import com.lovetropics.lib.BlockBox;
import com.lovetropics.lib.codec.MoreCodecs;
import com.lovetropics.minigames.common.content.mangroves_and_pianguas.FriendlyExplosion;
import com.lovetropics.minigames.common.content.mangroves_and_pianguas.entity.MpHuskEntity;
import com.lovetropics.minigames.common.content.mangroves_and_pianguas.entity.MpPillagerEntity;
import com.lovetropics.minigames.common.content.mangroves_and_pianguas.time.TimeInterpolationMessage;
import com.lovetropics.minigames.common.core.dimension.DimensionUtils;
import com.lovetropics.minigames.common.core.game.IGamePhase;
import com.lovetropics.minigames.common.core.game.behavior.IGameBehavior;
import com.lovetropics.minigames.common.core.game.behavior.event.*;
import com.lovetropics.minigames.common.core.game.player.PlayerRole;
import com.lovetropics.minigames.common.core.map.MapRegions;
import com.lovetropics.minigames.common.core.network.LoveTropicsNetwork;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
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
import net.minecraftforge.fml.network.PacketDistributor;

import javax.annotation.Nullable;
import java.util.*;

// TODO: needs to be split up!
public final class MpBehavior implements IGameBehavior {
    private static final Codec<Difficulty> DIFFICULTY_CODEC = Codec.STRING.xmap(Difficulty::byName, Difficulty::getTranslationKey);

    public static final Codec<MpBehavior> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            MoreCodecs.arrayOrUnit(PlayerRegionKey.CODEC, PlayerRegionKey[]::new).optionalFieldOf("participants", new PlayerRegionKey[0]).forGetter(c -> c.participantSpawnKeys),
            DIFFICULTY_CODEC.fieldOf("difficulty").forGetter(c -> c.difficulty)
    ).apply(instance, MpBehavior::new));

    private static final AttributeModifier GRASS_SLOW = new AttributeModifier(UUID.fromString("0b5baa42-2576-11ec-9621-0242ac130002"), "Slowness from tall grass", -0.65F, AttributeModifier.Operation.MULTIPLY_TOTAL);
    private static final ResourceLocation KOA_LOCATION = new ResourceLocation("tropicraft", "koa");
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

    private final PlayerRegionKey[] participantSpawnKeys;
    private final Difficulty difficulty;

    // TODO: reset all this info!!!!!
    private final List<PlayerRegions> freeRegions = new ArrayList<>();
    private final Map<ServerPlayerEntity, PlayerRegions> allocatedRegions = new HashMap<>();
    private final List<BlockPos> trackedMelons = new ArrayList<>();
    private final List<BlockPos> trackedJackOLanterns = new ArrayList<>();
    private final List<BlockPos> trackedPumpkins = new ArrayList<>();
    private final Set<BlockPos> globallyTrackedTrees = new HashSet<>();
    private final Map<BlockPos, Set<BlockPos>> treesPerTrunk = new HashMap<>();
    private final Map<BlockPos, Integer> jackOLanternAliveSeconds = new HashMap<>();
    private final Map<BlockPos, Integer> pumpkinHealth = new HashMap<>();

    private int participantSpawnIndex;
    private boolean firstTick = true;

    private long gameStartTime = 0;
    private int sentWaves = 0;

    public MpBehavior(PlayerRegionKey[] participantSpawnKeys, Difficulty difficulty) {
        this.participantSpawnKeys = participantSpawnKeys;
        this.difficulty = difficulty;
    }

    @Override
    public void register(IGamePhase game, EventRegistrar events) {
        MapRegions regions = game.getMapRegions();

        for (PlayerRegionKey key : this.participantSpawnKeys) {
            this.freeRegions.add(PlayerRegions.associate(key, regions));
        }

        events.listen(GamePlayerEvents.ADD, player -> setupPlayerAsRole(game, player, null));
        events.listen(GamePlayerEvents.SET_ROLE, (player, role, lastRole) -> setupPlayerAsRole(game, player, role));
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
        events.listen(GamePhaseEvents.STOP, (reason) -> LoveTropicsNetwork.CHANNEL.send(PacketDistributor.DIMENSION.with(game::getDimension), new TimeInterpolationMessage(1)));
        events.listen(GamePlayerEvents.ATTACK, this::onAttack);
        // No mob drops
        events.listen(GameLivingEntityEvents.MOB_DROP, (e, d, r) -> ActionResultType.FAIL);
        events.listen(GameLivingEntityEvents.FARMLAND_TRAMPLE, this::onFarmlandTrample);
    }

    private void setupPlayerAsRole(IGamePhase game, ServerPlayerEntity player, @Nullable PlayerRole role) {
        if (role == PlayerRole.SPECTATOR) {
            // Teleport players to center for now
            teleportToRegion(game, player, BlockBox.of(new BlockPos(0, 105, 0)));

            player.setGameType(GameType.SPECTATOR);
        } else if (role == PlayerRole.PARTICIPANT) {
            // Get next region
            PlayerRegions regions = this.freeRegions.get(this.participantSpawnIndex++ % this.freeRegions.size());

            this.allocatedRegions.put(player, regions);

            // Unbreakable hoe
            ItemStack stack = new ItemStack(Items.DIAMOND_HOE);
            stack.getOrCreateTag().putBoolean("Unbreakable", true);
            player.addItemStackToInventory(stack);

            stack = new ItemStack(Items.STONE_SWORD);
            stack.getOrCreateTag().putBoolean("Unbreakable", true);
            player.addItemStackToInventory(stack);

            player.addItemStackToInventory(new ItemStack(Items.SUNFLOWER, 32));

            teleportToRegion(game, player, regions.spawn);
        }
    }

    private void start(IGamePhase game) {
        this.gameStartTime = game.ticks();

        if (game.getWorld().getWorldInfo() instanceof ServerWorldInfo) {
            ((ServerWorldInfo)(game.getWorld().getWorldInfo())).setDifficulty(this.difficulty);
        }

        Optional<EntityType<?>> optional = Registry.ENTITY_TYPE.getOptional(KOA_LOCATION);
        if (optional.isPresent()) {
            EntityType<?> type = optional.get();

            for (Map.Entry<ServerPlayerEntity, PlayerRegions> entry : this.allocatedRegions.entrySet()) {
                ServerWorld world = game.getWorld();
                VillagerEntity koa = (VillagerEntity) type.create(world);

                Vector3d center = entry.getValue().shop.getCenter();

                koa.setLocationAndAngles(center.getX(), center.getY(), center.getZ(), 0, 0);
                koa.setPosition(center.getX(), center.getY() - 0.5, center.getZ());

                world.addEntity(koa);

                koa.onInitialSpawn(world, world.getDifficultyForLocation(new BlockPos(center)), SpawnReason.MOB_SUMMONED, null, null);

                koa.setNoAI(true);
            }
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
        ResourceLocation location = Registry.ENTITY_TYPE.getKey(target.getType());

        if (location.equals(KOA_LOCATION)) {
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
        PlayerRegions regions = this.allocatedRegions.get(player);

        // Check to see if the farmland block is below the plot
        if (placed.getBlock() == Blocks.FARMLAND && regions.plot.contains(pos.up())) {
            return ActionResultType.PASS;
        }

        if (regions != null) {
            if (!regions.plot.contains(pos)) {
                return ActionResultType.FAIL;
            }
        }

        if (placed.getBlock() == Blocks.MELON) {
            this.trackedMelons.add(pos);
        }

        if (placed.getBlock() == Blocks.JACK_O_LANTERN) {
            this.trackedJackOLanterns.add(pos);
            this.jackOLanternAliveSeconds.put(pos, 120); // Jack o lanterns live for 2 minutes
        }

        if (placed.getBlock() == Blocks.PUMPKIN) {
            this.trackedPumpkins.add(pos);
            this.pumpkinHealth.put(pos, 30);
        }

        return ActionResultType.PASS;
    }

    private ActionResultType onBreakBlock(ServerPlayerEntity player, BlockPos pos, BlockState state) {
        PlayerRegions regions = this.allocatedRegions.get(player);

        if (regions != null) {
            if (!regions.plot.contains(pos)) {
                return ActionResultType.FAIL;
            }
        }

        ServerWorld world = player.getServerWorld();

        if (state.getBlock() == Blocks.BIRCH_LOG) {
            Set<BlockPos> tree = this.treesPerTrunk.remove(pos);
            this.globallyTrackedTrees.removeAll(tree);

            for (BlockPos treePos : tree) {
                world.removeBlock(treePos, false);
            }

            ItemEntity sapling = new ItemEntity(world, pos.getX(), pos.getY(), pos.getZ(), new ItemStack(Blocks.BIRCH_SAPLING));
            world.addEntity(sapling);

            return ActionResultType.FAIL;
        }

        if (state.getBlock() == Blocks.MELON) {
            world.removeBlock(pos, false);

            ItemEntity melon = new ItemEntity(world, pos.getX(), pos.getY(), pos.getZ(), new ItemStack(Blocks.MELON));
            world.addEntity(melon);

            this.trackedMelons.remove(pos);

            return ActionResultType.FAIL;
        }

        if (state.getBlock() == Blocks.JACK_O_LANTERN) {
            this.trackedJackOLanterns.remove(pos);
            // TODO: this allows people to perpetually replace jack o' lanterns to reset their timer. Is there a better way to do this?
            this.jackOLanternAliveSeconds.remove(pos);
        }

        if (state.getBlock() == Blocks.PUMPKIN) {
            this.trackedPumpkins.remove(pos);
            this.pumpkinHealth.remove(pos);
        }

        return ActionResultType.PASS;
    }

    private ActionResultType onFarmlandTrample(Entity entity, BlockPos pos, BlockState state) {
        if (entity instanceof ServerPlayerEntity) {
            PlayerRegions regions = this.allocatedRegions.get((ServerPlayerEntity) entity);

            // Don't trample farmland if it's not found in the player's plot. Normalizes with up() as farmland is below the plot
            if (!regions.plot.contains(pos.up())) {
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
        Vector3d center = this.allocatedRegions.get(player).spawn.getCenter();

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
        player.sendStatusMessage(new TranslationTextComponent("ltminigames.minigame.mp_death_decrease", (totalCount - targetCount)), false);

        return ActionResultType.FAIL;
    }

    // TODO: staggered tick per player because this much logic in a single tick is not ideal
    private void tick(IGamePhase game) {
        // Send time data
        if (this.firstTick) {
            LoveTropicsNetwork.CHANNEL.send(PacketDistributor.DIMENSION.with(game::getDimension), new TimeInterpolationMessage(5));
            this.firstTick = false;
        }

        ServerWorld world = game.getWorld();
        Random random = world.getRandom();
        long ticks = this.gameStartTime + game.ticks();

        // Tick melons and jack o' lanterns every 5 ticks
        if (ticks % 5 == 0) {
            List<BlockPos> removed = new ArrayList<>();

            for (BlockPos melon : this.trackedMelons) {
                List<MobEntity> entities = world.getEntitiesWithinAABB(MobEntity.class, new AxisAlignedBB(melon.add(-2, -2, -2), melon.add(2, 2, 2)));

                entities.removeIf(e -> e instanceof VillagerEntity);

                if (entities.size() > 0) {
                    removed.add(melon);

                    explodeMelon(world, melon);
                }
            }

            this.trackedMelons.removeAll(removed);

            // Push away mobs from jack o' lanterns
            for (BlockPos lantern : this.trackedJackOLanterns) {
                List<MobEntity> entities = world.getEntitiesWithinAABB(MobEntity.class, new AxisAlignedBB(lantern.add(-3, -2, -3), lantern.add(3, 2, 3)));

                entities.removeIf(e -> e instanceof VillagerEntity);

                // Center of lantern block
                Vector3d lanternPos = new Vector3d(lantern.getX() + 0.5, lantern.getY() + 0.5, lantern.getZ() + 0.5);

                for (MobEntity entity : entities) {
                    Vector3d entityPos = entity.getPositionVec();

                    // Scaled so that closer values are higher, with a max of 10
                    double dist = 2.0 / (0.1 + entityPos.distanceTo(lanternPos));

                    // Angle between entity and center of lantern
                    double theta = Math.atan2(entityPos.z - lanternPos.z, entityPos.x - lanternPos.x);

                    // zoooooom
                    entity.addVelocity(dist * Math.cos(theta), 0.25, dist * Math.sin(theta));

                    // Prevent mobs from flying to the moon due to too much motion
                    Vector3d motion = entity.getMotion();
                    entity.setMotion(Math.min(motion.x, 10), Math.min(motion.y, 0.25), Math.min(motion.z, 10));

                    // Make it so that using a jack o lantern a lot will reduce alive time
                    if (random.nextInt(6) == 0) {
                        this.jackOLanternAliveSeconds.put(lantern, this.jackOLanternAliveSeconds.get(lantern) - 1);
                    }
                }
            }
        }

        // Tick every second
        if (ticks % 20 == 0) {
            for (Map.Entry<ServerPlayerEntity, PlayerRegions> entry : this.allocatedRegions.entrySet()) {
                for (BlockPos pos : entry.getValue().plot) {
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

            // Reduce lantern times
            List<BlockPos> toRemove = new ArrayList<>();
            for (Map.Entry<BlockPos, Integer> entry : this.jackOLanternAliveSeconds.entrySet()) {
                BlockPos pos = entry.getKey();
                int newTime = entry.getValue() - 1;

                if (newTime <= 0) {
                    toRemove.add(pos);
                    world.setBlockState(pos, Blocks.PUMPKIN.getDefaultState());
                    this.pumpkinHealth.put(pos, 30);

                    // Spawn poof when jack o' lanterns downgrade to pumpkins
                    for(int i = 0; i < 20; ++i) {
                        double d3 = random.nextGaussian() * 0.02;
                        double d1 = random.nextGaussian() * 0.02;
                        double d2 = random.nextGaussian() * 0.02;
                        world.spawnParticle(ParticleTypes.POOF, pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, 1, d3, d1, d2, 0.15F);
                    }
                } else {
                    this.jackOLanternAliveSeconds.put(pos, newTime);
                }
            }

            for (BlockPos pos : toRemove) {
                this.jackOLanternAliveSeconds.remove(pos);
                this.trackedJackOLanterns.remove(pos);
            }

            toRemove.clear();

            for (Map.Entry<BlockPos, Integer> entry : this.pumpkinHealth.entrySet()) {
                BlockPos pos = entry.getKey();

                List<MobEntity> entities = world.getEntitiesWithinAABB(MobEntity.class, new AxisAlignedBB(pos.add(-1, -5, -1), pos.add(1, 5, 1)));
                entities.removeIf(e -> e instanceof VillagerEntity);

                int newHealth = this.pumpkinHealth.get(pos) - entities.size();
                if (newHealth <= 0) {
                    toRemove.add(pos);
                    world.removeBlock(pos, false);

                    for(int i = 0; i < 20; ++i) {
                        double d3 = random.nextGaussian() * 0.02;
                        double d1 = random.nextGaussian() * 0.02;
                        double d2 = random.nextGaussian() * 0.02;
                        world.spawnParticle(ParticleTypes.POOF, pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, 1, d3, d1, d2, 0.15F);
                    }
                } else {
                    this.pumpkinHealth.put(pos, newHealth);
                }
            }

            for (BlockPos pos : toRemove) {
                this.pumpkinHealth.remove(pos);
                this.trackedPumpkins.remove(pos);
            }
        }

        // Tick growables every 15 seconds
        if (ticks % 300 == 0) {
            for (Map.Entry<ServerPlayerEntity, PlayerRegions> entry : this.allocatedRegions.entrySet()) {
                for (BlockPos pos : entry.getValue().plot) {
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
                            Set<BlockPos> treeBlocks = new HashSet<>();

                            Deque<BlockPos> queue = new LinkedList<>();
                            queue.add(pos.toImmutable());

                            // DFS new blocks from trees
                            while (!queue.isEmpty()) {
                                BlockPos poll = queue.poll();

                                // TODO: prioritize trunk blocks so trunks are never cut off

                                // DFS more if this is a tree block and it's not an already globally tracked tree or a part of this current tree that we've already seen
                                if (isTreeBlock(world.getBlockState(poll)) && !this.globallyTrackedTrees.contains(poll) && !treeBlocks.contains(poll)) {
                                    treeBlocks.add(poll);

                                    // Go forth in all directions
                                    for (Direction value : DIRECTIONS) {
                                        queue.add(poll.offset(value));
                                    }
                                }
                            }

                            this.globallyTrackedTrees.addAll(treeBlocks);
                            this.treesPerTrunk.put(pos.toImmutable(), treeBlocks);
                        } else {
                            // Place red particles to indicate tree didn't grow
                        }
                    }
                }
            }
        }

        // Drop currency every 30 seconds
        if (ticks % 600 == 0) {
            for (Map.Entry<ServerPlayerEntity, PlayerRegions> entry : this.allocatedRegions.entrySet()) {
                double value = 2;

                // TODO: calculate crops, plants, and trees separately
                Set<Block> blocks = new HashSet<>();

                for (BlockPos pos : entry.getValue().plot) {
                    BlockState state = world.getBlockState(pos);
                    blocks.add(state.getBlock());

                    if (state.getBlock() instanceof CropsBlock) {
                        value += 0.05;
                    } else if (state.getBlock() == Blocks.GRASS) {
                        value += 0.025;
                    } else if (state.getBlock() == Blocks.WITHER_ROSE) {
                        value += 0.075;
                    }else if (state.getBlock() == Blocks.BIRCH_LOG) {
                        value += 0.85;
                    }

                    // ...
                }

                // TODO: temp math equation
                value += (blocks.size() / 4.0) * value;
                int count = (int) value;

                if (world.rand.nextDouble() < value - count) {
                    count++;
                }

                ServerPlayerEntity player = entry.getKey();
                player.playSound(SoundEvents.ENTITY_ARROW_HIT_PLAYER, 0.18F, 1.0F);
                player.sendStatusMessage(new TranslationTextComponent("ltminigames.minigame.mp_currency_addition", count), false);
                player.addItemStackToInventory(new ItemStack(Items.SUNFLOWER, count));
            }
        }

        // Spawn mobs every 2 minutes (in daytime)
        long timeTilNextWave = ticks % 2400;

        // Warn players of an impending wave
        // Idea: upgrade that allows you to predict waves in the future?
        if (timeTilNextWave == 1800) {
            for (Map.Entry<ServerPlayerEntity, PlayerRegions> entry : this.allocatedRegions.entrySet()) {
                entry.getKey().sendStatusMessage(new TranslationTextComponent("ltminigames.minigame.mp_wave_warning"), false);
            }
        }

        if (timeTilNextWave == 0) {
            for (Map.Entry<ServerPlayerEntity, PlayerRegions> entry : this.allocatedRegions.entrySet()) {
                // Temp wave scaling equation- seems to work fine?
                int x = this.sentWaves / 2;
                int amount = (int) (MOB_SCALAR.get(this.difficulty) * (Math.pow(x, 1.2) + x) + 2 + random.nextInt(3));

                for (int i = 0; i < amount; i++) {
                    BlockPos pos = entry.getValue().mobSpawn.sample(random);

                    MobEntity entity = selectEntityForWave(random, world);

                    entity.setLocationAndAngles(pos.getX(), pos.getY(), pos.getZ(), 0, 0);
                    entity.setPosition(pos.getX(), pos.getY(), pos.getZ());

                    world.addEntity(entity);

                    entity.onInitialSpawn(world, world.getDifficultyForLocation(pos), SpawnReason.MOB_SUMMONED, null, null);
                }
            }

            this.sentWaves++;
        }

        // 4x faster ticking
        world.setDayTime(world.getDayTime() + 5);
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
    private static void explodeMelon(ServerWorld world, BlockPos pos) {
        world.removeBlock(pos, true);

        double x = pos.getX() + 0.5;
        double y = pos.getY() + 0.5;
        double z = pos.getZ() + 0.5;

        Explosion explosion = new FriendlyExplosion(world, null, null, null, x, y, z, 4.0f, false, Explosion.Mode.BREAK);
        explosion.doExplosionA();
        explosion.doExplosionB(false);

        for(ServerPlayerEntity serverplayerentity : world.getPlayers()) {
            if (serverplayerentity.getDistanceSq(x, y, z) < 4096.0D) {
                serverplayerentity.connection.sendPacket(new SExplosionPacket(x, y, z, 4.0f, explosion.getAffectedBlockPositions(), explosion.getPlayerKnockbackMap().get(serverplayerentity)));
            }
        }
    }

    private void teleportToRegion(IGamePhase game, ServerPlayerEntity player, BlockBox region) {
        BlockPos pos = region.sample(player.getRNG());
        DimensionUtils.teleportPlayerNoPortal(player, game.getDimension(), pos);
    }

    private static final class PlayerRegionKey {
        public static final Codec<PlayerRegionKey> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.STRING.fieldOf("plot").forGetter(k -> k.plot),
                Codec.STRING.fieldOf("spawn").forGetter(k -> k.spawn),
                Codec.STRING.fieldOf("shop").forGetter(k -> k.shop),
                Codec.STRING.fieldOf("mob_spawn").forGetter(k -> k.mobSpawn)
        ).apply(instance, PlayerRegionKey::new));
        private final String plot;
        private final String spawn;
        private final String shop;
        private final String mobSpawn;

        private PlayerRegionKey(String plot, String spawn, String shop, String mobSpawn) {
            this.plot = plot;
            this.spawn = spawn;
            this.shop = shop;
            this.mobSpawn = mobSpawn;
        }
    }

    private static final class PlayerRegions {
        private final BlockBox plot;
        private final BlockBox spawn;
        private final BlockBox shop;
        private final BlockBox mobSpawn;

        private PlayerRegions(BlockBox plot, BlockBox spawn, BlockBox shop, BlockBox mobSpawn) {
            this.plot = plot;
            this.spawn = spawn;
            this.shop = shop;
            this.mobSpawn = mobSpawn;
        }

        // Gathers all the regions out of the provided key. Assumes that each region is defined once.
        private static PlayerRegions associate(PlayerRegionKey key, MapRegions regions) {
            return new PlayerRegions(
                    regions.getAny(key.plot),
                    regions.getAny(key.spawn),
                    regions.getAny(key.shop),
                    regions.getAny(key.mobSpawn)
            );
        }
    }
}
