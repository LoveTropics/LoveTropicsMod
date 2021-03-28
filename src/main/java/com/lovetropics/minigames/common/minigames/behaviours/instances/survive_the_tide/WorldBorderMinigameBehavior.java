package com.lovetropics.minigames.common.minigames.behaviours.instances.survive_the_tide;

import com.lovetropics.minigames.common.MoreCodecs;
import com.lovetropics.minigames.common.map.MapRegion;
import com.lovetropics.minigames.common.minigames.IMinigameInstance;
import com.lovetropics.minigames.common.minigames.behaviours.IMinigameBehavior;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.particles.IParticleData;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EntityPredicates;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.BossInfo;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerBossInfo;
import net.minecraft.world.server.ServerWorld;

import java.util.ArrayList;
import java.util.List;

public class WorldBorderMinigameBehavior implements IMinigameBehavior
{
	public static final Codec<WorldBorderMinigameBehavior> CODEC = RecordCodecBuilder.create(instance -> {
		return instance.group(
				MoreCodecs.TEXT.fieldOf("name").forGetter(c -> c.name),
				Codec.STRING.fieldOf("world_border_center").forGetter(c -> c.worldBorderCenterKey),
				MoreCodecs.TEXT.fieldOf("collapse_message").forGetter(c -> c.collapseMessage),
				Codec.LONG.fieldOf("ticks_until_start").forGetter(c -> c.ticksUntilStart),
				Codec.LONG.fieldOf("delay_until_collapse").forGetter(c -> c.delayUntilCollapse),
				Codec.INT.fieldOf("particle_rate_delay").forGetter(c -> c.particleRateDelay),
				Codec.INT.fieldOf("particle_height").forGetter(c -> c.particleHeight),
				Codec.INT.fieldOf("damage_rate_delay").forGetter(c -> c.damageRateDelay),
				Codec.INT.fieldOf("damage_amount").forGetter(c -> c.damageAmount),
				ParticleTypes.CODEC.optionalFieldOf("border_particle", ParticleTypes.EXPLOSION).forGetter(c -> c.borderParticle)
		).apply(instance, WorldBorderMinigameBehavior::new);
	});

	private final ITextComponent name;
	private final String worldBorderCenterKey;
	private final ITextComponent collapseMessage;
	private final long ticksUntilStart;
	private final long delayUntilCollapse;
	private final int particleRateDelay;
	private final int particleHeight;
	private final int damageRateDelay;
	private final int damageAmount;
	private final IParticleData borderParticle;

	private BlockPos worldBorderCenter = BlockPos.ZERO;

	private boolean borderCollapseMessageSent = false;
	private final ServerBossInfo bossInfo;

	public WorldBorderMinigameBehavior(final ITextComponent name, final String worldBorderCenterKey, final ITextComponent collapseMessage, final long ticksUntilStart,
			final long delayUntilCollapse, final int particleRateDelay, final int particleHeight, final int damageRateDelay, final int damageAmount, final IParticleData borderParticle) {
		this.name = name;
		this.worldBorderCenterKey = worldBorderCenterKey;
		this.collapseMessage = collapseMessage;
		this.ticksUntilStart = ticksUntilStart;
		this.delayUntilCollapse = delayUntilCollapse;
		this.particleRateDelay = particleRateDelay;
		this.particleHeight = particleHeight;
		this.damageRateDelay = damageRateDelay;
		this.damageAmount = damageAmount;
		this.borderParticle = borderParticle;

		this.bossInfo = (ServerBossInfo)(new ServerBossInfo(
				name,
				BossInfo.Color.WHITE,
				BossInfo.Overlay.PROGRESS))
				.setDarkenSky(false);
	}

	@Override
	public void onConstruct(IMinigameInstance minigame) {
		List<MapRegion> regions = new ArrayList<>(minigame.getMapRegions().get(worldBorderCenterKey));

		if (!regions.isEmpty()) {
			MapRegion centerRegion = regions.get(minigame.getWorld().getRandom().nextInt(regions.size()));
			worldBorderCenter = centerRegion.getCenterBlock();
		} else {
			worldBorderCenter = BlockPos.ZERO;
		}
	}

	@Override
	public void onFinish(final IMinigameInstance minigame) {
		borderCollapseMessageSent = false;
		bossInfo.removeAllPlayers();
	}

	@Override
	public void worldUpdate(final IMinigameInstance minigame, ServerWorld world) {
		tickWorldBorder(world, minigame);
	}

	// TODO: Clean up this mess
	private void tickWorldBorder(final World world, final IMinigameInstance minigame) {
		if (minigame.ticks() >= ticksUntilStart) {
			if (!borderCollapseMessageSent) {
				borderCollapseMessageSent = true;
				minigame.getPlayers().sendMessage(collapseMessage);
			}

			long ticksSinceStart = minigame.ticks() - ticksUntilStart;

			boolean isCollapsing = minigame.ticks() >= ticksUntilStart + delayUntilCollapse;
			float borderPercent = 0.01F;
			if (!isCollapsing) {
				borderPercent = 1F - ((float) (ticksSinceStart + 1) / (float) delayUntilCollapse);
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
			int yStepAmount = 5;

			int randSpawn = 30;

			//particle spawning
			if (world.getGameTime() % particleRateDelay == 0) {

				for (float step = 0; step <= 360; step += stepAmount) {
					for (int yStep = yMin; yStep < particleHeight; yStep += yStepAmount) {
						if (world.rand.nextInt(randSpawn/*yMax - yMin*/) == 0) {
							float xVec = (float) -Math.sin(Math.toRadians(step)) * currentRadius;
							float zVec = (float) Math.cos(Math.toRadians(step)) * currentRadius;
							//world.addParticle(borderParticle, worldBorderCenter.getX() + xVec, worldBorderCenter.getY() + yStep, worldBorderCenter.getZ() + zVec, 0, 0, 0);
							if (world instanceof ServerWorld) {
								ServerWorld serverWorld = (ServerWorld) world;
								//IParticleData data = ForgeRegistries.PARTICLE_TYPES.getValue(new ResourceLocation("heart"));
								serverWorld.spawnParticle(borderParticle, worldBorderCenter.getX() + xVec, worldBorderCenter.getY() + yStep, worldBorderCenter
										.getZ() + zVec, 1, 0, 0, 0, 1D);
							}
						}

					}
				}

				if (world instanceof ServerWorld) {
					ServerWorld serverWorld = (ServerWorld) world;
					//serverWorld.spawnParticle(borderParticle, worldBorderCenter.getX(), worldBorderCenter.getY(), worldBorderCenter.getZ(), 1, 0, 0, 0, 1D);
				}
			}

			//player damage
			if (world.getGameTime() % damageRateDelay == 0) {
				for (PlayerEntity playerentity : world.getPlayers()) {
					if (EntityPredicates.NOT_SPECTATING.test(playerentity) && EntityPredicates.IS_LIVING_ALIVE.test(playerentity)) {
						//needs moar predicates
						if (!playerentity.isCreative()) {
							//ignore Y val, only do X Z dist compare
							double d0 = playerentity.getDistanceSq(worldBorderCenter.getX(), playerentity.getPosY(), worldBorderCenter.getZ());
							if (isCollapsing || !(currentRadius < 0.0D || d0 < currentRadius * currentRadius)) {
								//System.out.println("hurt: " + playerentity);
								playerentity.attackEntityFrom(DamageSource.causeExplosionDamage((LivingEntity) null), damageAmount);
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

			//world.addParticle(borderParticle, );
		}
	}
}
