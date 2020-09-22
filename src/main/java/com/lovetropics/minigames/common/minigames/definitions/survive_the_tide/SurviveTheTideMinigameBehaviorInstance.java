package com.lovetropics.minigames.common.minigames.definitions.survive_the_tide;

import com.lovetropics.minigames.client.data.TropicraftLangKeys;
import com.lovetropics.minigames.common.config.ConfigLT;
import com.lovetropics.minigames.common.dimension.DimensionUtils;
import com.lovetropics.minigames.common.donations.FireworkUtil;
import com.lovetropics.minigames.common.minigames.IMinigameInstance;
import com.lovetropics.minigames.common.minigames.MinigameManager;
import com.lovetropics.minigames.common.minigames.behaviours.IMinigameBehavior;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.CampfireBlock;
import net.minecraft.block.FenceBlock;
import net.minecraft.block.IWaterLoggable;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.LightningBoltEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.server.MinecraftServer;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EntityPredicates;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.BossInfo;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkSection;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.gen.Heightmap;
import net.minecraft.world.server.ServerBossInfo;
import net.minecraft.world.server.ServerChunkProvider;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Iterator;
import java.util.Random;
import java.util.UUID;

public class SurviveTheTideMinigameBehaviorInstance implements IMinigameBehavior
{
	public static final Logger LOGGER = LogManager.getLogger();
	public static boolean debug = true;

	private MinigamePhase phase = MinigamePhase.PHASE0;

	private long minigameTime = 0;
	private long phaseTime = 0;
	private long ticksSincePhase4Start = 0;
	private boolean borderCollapseMessageSent = false;

	private int waterLevel;

	private BlockPos waterLevelMin = new BlockPos(5722, 0, 6782);
	private BlockPos waterLevelMax = new BlockPos(6102, 0, 7162);

	private BlockPos worldBorderCenter = new BlockPos(5912, 145, 6972);

	private boolean minigameEnded;
	private int minigameEndedTimer = 0;
	private UUID winningPlayer;
	private ITextComponent winningPlayerName;

	private Random rand = new Random();

	private final ServerBossInfo bossInfo = (ServerBossInfo)(new ServerBossInfo(new StringTextComponent("Explosive Storm"), BossInfo.Color.WHITE, BossInfo.Overlay.PROGRESS)).setDarkenSky(false);

	public enum MinigamePhase {
		PHASE0,
		PHASE1,
		PHASE2,
		PHASE3,
		PHASE4,
	}
	
	public SurviveTheTideMinigameBehaviorInstance()
	{
	}

	private DimensionType getDimension() {
		return DimensionUtils.SURVIVE_THE_TIDE_DIMENSION;
	}
	
	@Override
	public void worldUpdate(final IMinigameInstance minigame, World world) {
		if (world.getDimension().getType() == getDimension()) {
			this.checkForGameEndCondition(minigame, world);

			minigameTime++;
			phaseTime++;

			this.processWaterLevel(world);

			if (ConfigLT.MINIGAME_SURVIVE_THE_TIDE.minigame_SurviveTheTide_worlderBorderEnabled.get()) {
				this.tickWorldBorder(world, minigame);
			}

			if (phase == MinigamePhase.PHASE0) {
				if (phaseTime == 20 * 7) {
					minigame.getPlayers().sendMessage(new TranslationTextComponent(TropicraftLangKeys.SURVIVE_THE_TIDE_INTRO2).applyTextStyle(TextFormatting.GRAY));
				} else if (phaseTime == 20 * 14) {
					minigame.getPlayers().sendMessage(new TranslationTextComponent(TropicraftLangKeys.SURVIVE_THE_TIDE_INTRO3).applyTextStyle(TextFormatting.GRAY));
				} else if (phaseTime == 20 * 21) {
					minigame.getPlayers().sendMessage(new TranslationTextComponent(TropicraftLangKeys.SURVIVE_THE_TIDE_INTRO4).applyTextStyle(TextFormatting.GRAY));
				} else if (phaseTime == 20 * 28) {
					minigame.getPlayers().sendMessage(new TranslationTextComponent(TropicraftLangKeys.SURVIVE_THE_TIDE_INTRO5).applyTextStyle(TextFormatting.GRAY));
				}

				if (phaseTime >= ConfigLT.MINIGAME_SURVIVE_THE_TIDE.phase0Length.get()) {
					nextPhase();

					BlockPos spawnStart = ConfigLT.minigame_SurviveTheTide_spawnAreaP1;
					BlockPos spawnEnd = ConfigLT.minigame_SurviveTheTide_spawnAreaP2;

					// Destroy all fences blocking players from getting out of spawn area for phase 0
					for (BlockPos p : BlockPos.getAllInBoxMutable(spawnStart, spawnEnd)) {
						if (world.getBlockState(p).getBlock() instanceof FenceBlock) {
							world.setBlockState(p, Blocks.AIR.getDefaultState(), 2);
						}
					}

					int minutes = ConfigLT.MINIGAME_SURVIVE_THE_TIDE.phase1Length.get() / 20 / 60;
					minigame.getPlayers().sendMessage(new TranslationTextComponent(TropicraftLangKeys.SURVIVE_THE_TIDE_PVP_DISABLED, new StringTextComponent(String.valueOf(minutes))).applyTextStyle(TextFormatting.YELLOW));

					// So players can drop down without fall damage
					minigame.getPlayers().addPotionEffect(new EffectInstance(Effects.SLOW_FALLING, 10 * 20));
				}
			} else if (phase == MinigamePhase.PHASE1) {
				if (phaseTime >= ConfigLT.MINIGAME_SURVIVE_THE_TIDE.phase1Length.get()) {
					nextPhase();

					minigame.getPlayers().sendMessage(new TranslationTextComponent(TropicraftLangKeys.SURVIVE_THE_TIDE_PVP_ENABLED).applyTextStyle(TextFormatting.RED));
				}
			} else if (phase == MinigamePhase.PHASE2) {
				if (phaseTime >= ConfigLT.MINIGAME_SURVIVE_THE_TIDE.phase2Length.get()) {
					nextPhase();
				}
			} else if (phase == MinigamePhase.PHASE3) {
				if (phaseTime >= ConfigLT.MINIGAME_SURVIVE_THE_TIDE.phase3Length.get()) {
					nextPhase();
				}
			} else if (phase == MinigamePhase.PHASE4) {
				if (this.minigameTime % 100 == 0) {
					this.growIcebergs(world);
				}

				ticksSincePhase4Start++;
			}
		}
	}

	@Override
	public void onPlayerDeath(final IMinigameInstance minigame, ServerPlayerEntity player) {
		final MinecraftServer server = player.getServer();
		BlockPos fireworkPos = player.world.getHeight(Heightmap.Type.MOTION_BLOCKING, player.getPosition());
		FireworkUtil.spawnFirework(fireworkPos, player.world, FireworkUtil.Palette.ISLAND_ROYALE.getPalette());

		destroyVanishingCursedItems(player.inventory);
		player.inventory.dropAllItems();

		if (minigame.getParticipants().size() == 2) {
			Iterator<ServerPlayerEntity> it = minigame.getParticipants().iterator();

			ServerPlayerEntity p1 = it.next();
			ServerPlayerEntity p2 = it.next();

			if (p1 != null && p2 != null) {
				ITextComponent p1text = p1.getDisplayName().deepCopy().applyTextStyle(TextFormatting.AQUA);
				ITextComponent p2text = p2.getDisplayName().deepCopy().applyTextStyle(TextFormatting.AQUA);

				minigame.getPlayers().sendMessage(new TranslationTextComponent(TropicraftLangKeys.SURVIVE_THE_TIDE_DOWN_TO_TWO, p1text, p2text).applyTextStyle(TextFormatting.GOLD));
			}
		}

		if (minigame.getParticipants().size() == 1) {
			this.minigameEnded = true;

			this.winningPlayer = minigame.getParticipants().iterator().next().getUniqueID();
			this.winningPlayerName = server.getPlayerList().getPlayerByUUID(this.winningPlayer).getDisplayName().deepCopy();
		}
	}

	@Override
	public void onPlayerHurt(final IMinigameInstance minigame, LivingHurtEvent event) {
		if (event.getSource().getTrueSource() instanceof PlayerEntity && isSafePhase(this.phase)) {
			event.setCanceled(true);
		}
	}

	@Override
	public void onPlayerAttackEntity(final IMinigameInstance minigame, AttackEntityEvent event) {
		if (event.getTarget() instanceof PlayerEntity && isSafePhase(this.phase)) {
			event.setCanceled(true);
		}
	}

	@Override
	public void onLivingEntityUpdate(final IMinigameInstance minigame, LivingEntity entity) {
		if (entity.getPosY() <= this.waterLevel + 1 && entity.isInWater() && entity.ticksExisted % 20 == 0) {
			entity.attackEntityFrom(DamageSource.DROWN, 2.0F);
		}
	}

	@Override
	public void onPlayerRespawn(final IMinigameInstance minigame, ServerPlayerEntity player) {

	}

	@Override
	public void onFinish(final IMinigameInstance minigame) {
		this.minigameEnded = false;
		this.minigameEndedTimer = 0;
		this.winningPlayer = null;
		phase = MinigamePhase.PHASE0;
		phaseTime = 0;
		ticksSincePhase4Start = 0;
		borderCollapseMessageSent = false;

		bossInfo.removeAllPlayers();
	}

	@Override
	public void onStart(final IMinigameInstance minigame) {
		minigameTime = 0;
		minigameEndedTimer = 0;
		waterLevel = 120;
		phase = MinigamePhase.PHASE0;

		minigame.getPlayers().sendMessage(new TranslationTextComponent(TropicraftLangKeys.SURVIVE_THE_TIDE_INTRO1).applyTextStyle(TextFormatting.GRAY));
	}

	public MinigamePhase getPhase() {
		return phase;
	}

	public void setPhase(MinigamePhase phase) {
		this.phase = phase;
	}

	public void nextPhase() {
		if (phase == MinigamePhase.PHASE0) {
			phase = MinigamePhase.PHASE1;
		} else if (phase == MinigamePhase.PHASE1) {
			phase = MinigamePhase.PHASE2;
		} else if (phase == MinigamePhase.PHASE2) {
			phase = MinigamePhase.PHASE3;
		} else if (phase == MinigamePhase.PHASE3) {
			phase = MinigamePhase.PHASE4;
		}
		LOGGER.info("Starting minigame phase " + phase);
		phaseTime = 0;
	}

	public long getMinigameTime() {
		return minigameTime;
	}

	public void setMinigameTime(long minigameTime) {
		this.minigameTime = minigameTime;
	}

	public long getPhaseTime() {
		return phaseTime;
	}

	public void setPhaseTime(long phaseTime) {
		this.phaseTime = phaseTime;
	}

	public void dbg(String str) {
		if (debug) {
			LOGGER.info(str);
		}
	}

	public static boolean isSafePhase(MinigamePhase phase) {
		return phase == MinigamePhase.PHASE0 || phase == MinigamePhase.PHASE1;
	}

	private void checkForGameEndCondition(IMinigameInstance instance, World world) {
		if (this.minigameEnded) {
			if (this.minigameEndedTimer % 60 == 0) {
				ServerPlayerEntity winning = world.getServer().getPlayerList().getPlayerByUUID(this.winningPlayer);

				if (winning != null) {
					int xOffset = (7 + this.rand.nextInt(5)) * (this.rand.nextBoolean() ? 1 : -1);
					int zOffset =  (7 + this.rand.nextInt(5)) * (this.rand.nextBoolean() ? 1 : -1);

					int posX = MathHelper.floor(winning.getPosX()) + xOffset;
					int posZ = MathHelper.floor(winning.getPosZ()) + zOffset;

					int posY = world.getHeight(Heightmap.Type.MOTION_BLOCKING, posX, posZ);

					((ServerWorld)world).addLightningBolt(new LightningBoltEntity(world, posX, posY, posZ, true));
				}
			}

			if (this.minigameEndedTimer == 0) {
				instance.getPlayers().sendMessage(new TranslationTextComponent(TropicraftLangKeys.SURVIVE_THE_TIDE_FINISH1, this.winningPlayerName).applyTextStyle(TextFormatting.GRAY));
			} else if (this.minigameEndedTimer == 20 * 7){
				instance.getPlayers().sendMessage(new TranslationTextComponent(TropicraftLangKeys.SURVIVE_THE_TIDE_FINISH2, this.winningPlayerName).applyTextStyle(TextFormatting.GRAY));
			} else if (this.minigameEndedTimer == 20 * 14){
				instance.getPlayers().sendMessage(new TranslationTextComponent(TropicraftLangKeys.SURVIVE_THE_TIDE_FINISH3, this.winningPlayerName).applyTextStyle(TextFormatting.GRAY));
			} else if (this.minigameEndedTimer == 20 * 21){
				instance.getPlayers().sendMessage(new TranslationTextComponent(TropicraftLangKeys.SURVIVE_THE_TIDE_FINISH4, this.winningPlayerName).applyTextStyle(TextFormatting.GRAY));
			} else if (this.minigameEndedTimer == 20 * 28) {
				instance.getPlayers().sendMessage(new TranslationTextComponent(TropicraftLangKeys.MINIGAME_FINISH).applyTextStyle(TextFormatting.GOLD));
			} else if (this.minigameEndedTimer == 20 * 38) {
				MinigameManager.getInstance().finishCurrentMinigame();
			}

			this.minigameEndedTimer++;
		}
	}

	private void processWaterLevel(World world) {
		if (phase == MinigamePhase.PHASE2 || phase == MinigamePhase.PHASE3) {
			int waterChangeInterval;

			if (phase == MinigamePhase.PHASE2) {
				waterChangeInterval = this.calculateWaterChangeInterval(
						ConfigLT.MINIGAME_SURVIVE_THE_TIDE.phase2TargetWaterLevel.get(),
						126,
						ConfigLT.MINIGAME_SURVIVE_THE_TIDE.phase2Length.get()
				);
			}
			else {
				waterChangeInterval = this.calculateWaterChangeInterval(
						ConfigLT.MINIGAME_SURVIVE_THE_TIDE.phase3TargetWaterLevel.get(),
						ConfigLT.MINIGAME_SURVIVE_THE_TIDE.phase2TargetWaterLevel.get(),
						ConfigLT.MINIGAME_SURVIVE_THE_TIDE.phase3Length.get());
			}

			if (minigameTime % waterChangeInterval == 0) {
				this.waterLevel++;
				BlockPos min = this.waterLevelMin.add(0, this.waterLevel, 0);
				BlockPos max = this.waterLevelMax.add(0, this.waterLevel, 0);
				ChunkPos minChunk = new ChunkPos(min);
				ChunkPos maxChunk = new ChunkPos(max);

				long startTime = System.currentTimeMillis();
				long updatedBlocks = 0;

				BlockPos.Mutable localStart = new BlockPos.Mutable();
				BlockPos.Mutable localEnd = new BlockPos.Mutable();
				BlockPos.Mutable realPos = new BlockPos.Mutable();

				for (int x = minChunk.x; x <= maxChunk.x; x++) {
					for (int z = minChunk.z; z <= maxChunk.z; z++) {
						ChunkPos chunkPos = new ChunkPos(x, z);
						BlockPos chunkStart = chunkPos.asBlockPos();
						// Extract current chunk section
						Chunk chunk = world.getChunk(x, z);
						ChunkSection[] sectionArray = chunk.getSections();
						ChunkSection section = sectionArray[this.waterLevel >> 4];
						int localY = this.waterLevel & 0xF;
						// Calculate start/end within the current section
						localStart.setPos(min.subtract(chunkStart));
						localStart.setPos(Math.max(0, localStart.getX()), localY, Math.max(0, localStart.getZ()));
						localEnd.setPos(max.subtract(chunkStart));
						localEnd.setPos(Math.min(15, localEnd.getX()), localY, Math.min(15, localEnd.getZ()));
						// If this section is empty, we must add a new one
						if (section == Chunk.EMPTY_SECTION) {
							// This constructor expects the "base y" which means the real Y-level floored to the nearest multiple of 16
							// This is accomplished by removing the last 4 bits of the coordinate
							section = new ChunkSection(this.waterLevel & ~0xF);
							sectionArray[this.waterLevel >> 4] = section;
						}
						Heightmap heightmapSurface = chunk.getHeightmap(Heightmap.Type.WORLD_SURFACE);
						Heightmap heightmapMotionBlocking = chunk.getHeightmap(Heightmap.Type.MOTION_BLOCKING);
						boolean anyChanged = false;
						for (BlockPos pos : BlockPos.getAllInBoxMutable(localStart, localEnd)) {
							BlockState existing = section.getBlockState(pos.getX(), pos.getY(), pos.getZ());
							realPos.setPos(chunkStart.getX() + pos.getX(), this.waterLevel, chunkStart.getZ() + pos.getZ());
							BlockState toSet = null;
							if (existing.isAir(world, pos) || !existing.getMaterial().blocksMovement() || existing.getBlock() == Blocks.BAMBOO) {
								// If air or a replaceable block, just set to water
								toSet = Blocks.WATER.getDefaultState();
							} else if (existing.getBlock() instanceof IWaterLoggable) {
								// If waterloggable, set the waterloggable property to true
								toSet = existing.with(BlockStateProperties.WATERLOGGED, true);
								if (existing.getBlock() == Blocks.CAMPFIRE) {
									toSet = toSet.with(CampfireBlock.LIT, false);
								}
							}
							if (toSet != null) {
								anyChanged = true;
								if (existing.getBlock() == Blocks.BAMBOO) {
									world.setBlockState(realPos, toSet, Constants.BlockFlags.NO_RERENDER | Constants.BlockFlags.BLOCK_UPDATE);
								} else {
									section.setBlockState(pos.getX(), pos.getY(), pos.getZ(), toSet);
								}
								// Tell the client about the change
								((ServerChunkProvider)world.getChunkProvider()).markBlockChanged(realPos);
								// Update heightmap
								heightmapSurface.update(pos.getX(), realPos.getY(), pos.getZ(), toSet);
								heightmapMotionBlocking.update(pos.getX(), realPos.getY(), pos.getZ(), toSet);
								updatedBlocks++;
								// FIXES LIGHTING AT THE COST OF PERFORMANCE - TODO ask fry?
								// world.getChunkProvider().getLightManager().checkBlock(realPos);
							}
						}
						if (anyChanged) {
							// Make sure this chunk gets saved
							chunk.markDirty();
						}
					}
				}

				long endTime = System.currentTimeMillis();
				LogManager.getLogger().info("Updated {} blocks in {}ms", updatedBlocks, endTime - startTime);
			}
		}
	}

	private void growIcebergs(World world) {
		for (IcebergLine line : ConfigLT.minigame_SurviveTheTide_icebergLines) {
			line.generate(world, this.waterLevel);
		}
	}

	private int calculateWaterChangeInterval(int targetLevel, int prevLevel, int phaseLength) {
		int waterLevelDiff = prevLevel - targetLevel;
		return phaseLength / waterLevelDiff;
	}

	private void destroyVanishingCursedItems(IInventory inventory) {
		for(int i = 0; i < inventory.getSizeInventory(); ++i) {
			ItemStack itemstack = inventory.getStackInSlot(i);
			if (!itemstack.isEmpty() && EnchantmentHelper.hasVanishingCurse(itemstack)) {
				inventory.removeStackFromSlot(i);
			}
		}
	}

	private void tickWorldBorder(World world, IMinigameInstance instance) {

		//DEBUG
        /*if (phase != MinigamePhase.PHASE4) {
            setPhase(MinigamePhase.PHASE4);
        }*/

		long ticksToStartAt = ConfigLT.MINIGAME_SURVIVE_THE_TIDE.minigame_SurviveTheTide_worldBorder_ticksAfterPhase4.get();
		//DEBUG
		//ticksToStartAt = 20*5;

		if (phase == MinigamePhase.PHASE4 && ticksSincePhase4Start >= ticksToStartAt) {

			if (!borderCollapseMessageSent) {
				borderCollapseMessageSent = true;
				//cant update clients right now, so hardcode message on server
				//instance.getAllPlayers().sendMessage(new TranslationTextComponent(TropicraftLangKeys.SURVIVE_THE_TIDE_BORDER_COLLAPSE).applyTextStyle(TextFormatting.RED));
				instance.getPlayers().sendMessage(new TranslationTextComponent("THE EXPLOSIVE STORM HAS STARTED CLOSING IN!").applyTextStyle(TextFormatting.RED));
			}

			long ticksToCollapseBorder = ConfigLT.MINIGAME_SURVIVE_THE_TIDE.minigame_SurviveTheTide_worldBorder_ticksUtilFullyShrinked.get();
			//DEBUG
			//ticksToCollapseBorder = 20*15;

			long ticksSincePhase4StartAdj = ticksSincePhase4Start - ticksToStartAt;

			boolean fullCollapse = ticksSincePhase4StartAdj > ticksToCollapseBorder;
			float borderPercent = 0.01F;
			if (!fullCollapse) {
				borderPercent = 1F - ((float) (ticksSincePhase4StartAdj + 1) / (float) ticksToCollapseBorder);
			}

			//math safety
			if (borderPercent < 0.01F) {
				borderPercent = 0.01F;
			}

			bossInfo.setPercent(borderPercent);

			float maxRadius = 210;
			float currentRadius = maxRadius * borderPercent;
			float amountPerCircle = 4 * currentRadius;
			float stepAmount = 360F / amountPerCircle;

			int yMin = -10;
			int yMax = ConfigLT.MINIGAME_SURVIVE_THE_TIDE.minigame_SurviveTheTide_worldBorder_particleHeight.get();
			int yStepAmount = 5;

			int randSpawn = 30;
			int iterateRate = ConfigLT.MINIGAME_SURVIVE_THE_TIDE.minigame_SurviveTheTide_worldBorder_particleRateDelay.get();

			//particle spawning
			if (world.getGameTime() % iterateRate == 0) {

				for (float step = 0; step <= 360; step += stepAmount) {
					for (int yStep = yMin; yStep < yMax; yStep += yStepAmount) {
						if (rand.nextInt(randSpawn/*yMax - yMin*/) == 0) {
							float xVec = (float) -Math.sin(Math.toRadians(step)) * currentRadius;
							float zVec = (float) Math.cos(Math.toRadians(step)) * currentRadius;
							//world.addParticle(ParticleTypes.EXPLOSION, worldBorderCenter.getX() + xVec, worldBorderCenter.getY() + yStep, worldBorderCenter.getZ() + zVec, 0, 0, 0);
							if (world instanceof ServerWorld) {
								ServerWorld serverWorld = (ServerWorld) world;
								//IParticleData data = ForgeRegistries.PARTICLE_TYPES.getValue(new ResourceLocation("heart"));
								serverWorld.spawnParticle(ParticleTypes.EXPLOSION, worldBorderCenter.getX() + xVec, worldBorderCenter.getY() + yStep, worldBorderCenter.getZ() + zVec, 1, 0, 0, 0, 1D);
							}
						}

					}
				}

				if (world instanceof ServerWorld) {
					ServerWorld serverWorld = (ServerWorld) world;
					//serverWorld.spawnParticle(ParticleTypes.EXPLOSION, worldBorderCenter.getX(), worldBorderCenter.getY(), worldBorderCenter.getZ(), 1, 0, 0, 0, 1D);
				}
			}

			//player damage
			if (world.getGameTime() % ConfigLT.MINIGAME_SURVIVE_THE_TIDE.minigame_SurviveTheTide_worldBorder_damageRateDelay.get() == 0) {
				for (PlayerEntity playerentity : world.getPlayers()) {
					if (EntityPredicates.NOT_SPECTATING.test(playerentity) && EntityPredicates.IS_LIVING_ALIVE.test(playerentity)) {
						//needs moar predicates
						if (!playerentity.isCreative()) {
							//ignore Y val, only do X Z dist compare
							double d0 = playerentity.getDistanceSq(worldBorderCenter.getX(), playerentity.getPosY(), worldBorderCenter.getZ());
							if (fullCollapse || !(currentRadius < 0.0D || d0 < currentRadius * currentRadius)) {
								//System.out.println("hurt: " + playerentity);
								playerentity.attackEntityFrom(DamageSource.causeExplosionDamage((LivingEntity) null), ConfigLT.MINIGAME_SURVIVE_THE_TIDE.minigame_SurviveTheTide_worldBorder_damageAmount.get());
								playerentity.addPotionEffect(new EffectInstance(Effects.NAUSEA, 40, 0));
							} else {

							}
						}
					}

					//add boss bar info to everyone in dim if not already registered for it
					if (playerentity instanceof ServerPlayerEntity) {
						if (!bossInfo.getPlayers().contains(playerentity)) {
							bossInfo.addPlayer((ServerPlayerEntity) playerentity);
						}
					}
				}
			}

			//instance.getParticipants()
			//this.actionAllParticipants(instance, (p) -> p.addPotionEffect(new EffectInstance(Effects.SLOW_FALLING, 10 * 20)));

			//world.addParticle(ParticleTypes.EXPLOSION, );
		}
	}
}
