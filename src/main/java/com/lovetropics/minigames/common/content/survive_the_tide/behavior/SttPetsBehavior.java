package com.lovetropics.minigames.common.content.survive_the_tide.behavior;

import com.lovetropics.minigames.common.core.game.IGamePhase;
import com.lovetropics.minigames.common.core.game.behavior.IGameBehavior;
import com.lovetropics.minigames.common.core.game.behavior.event.EventRegistrar;
import com.lovetropics.minigames.common.core.game.behavior.event.GameLivingEntityEvents;
import com.lovetropics.minigames.common.core.game.behavior.event.GamePhaseEvents;
import com.lovetropics.minigames.common.core.game.behavior.event.GamePlayerEvents;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ReferenceArrayList;
import net.minecraft.entity.CreatureEntity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.pathfinding.Path;
import net.minecraft.pathfinding.PathNavigator;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.DamageSource;
import net.minecraft.util.registry.Registry;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class SttPetsBehavior implements IGameBehavior {
	public static final Codec<SttPetsBehavior> CODEC = RecordCodecBuilder.create(instance -> {
		return instance.group(
				Codec.unboundedMap(Registry.ENTITY_TYPE, PetConfig.CODEC).fieldOf("entities").forGetter(c -> c.petTypes)
		).apply(instance, SttPetsBehavior::new);
	});

	private static final double FOLLOW_DISTANCE = 5.0;

	private final Map<EntityType<?>, PetConfig> petTypes;

	private final Object2ObjectMap<UUID, List<Pet>> petsByPlayer = new Object2ObjectOpenHashMap<>();

	private IGamePhase game;

	public SttPetsBehavior(Map<EntityType<?>, PetConfig> petTypes) {
		this.petTypes = petTypes;
	}

	@Override
	public void register(IGamePhase game, EventRegistrar events) {
		this.game = game;

		events.listen(GamePhaseEvents.TICK, this::tick);

		events.listen(GameLivingEntityEvents.SPAWNED, (entity, reason, player) -> {
			if (reason == SpawnReason.SPAWN_EGG && player != null && entity instanceof CreatureEntity) {
				this.onCreatureSpawnedFromEgg((CreatureEntity) entity, player);
			}
		});

		events.listen(GameLivingEntityEvents.DEATH, (entity, damageSource) -> {
			if (entity instanceof CreatureEntity) {
				this.onCreatureDeath((CreatureEntity) entity);
			}
			return ActionResultType.PASS;
		});

		events.listen(GamePlayerEvents.REMOVE, player -> {
			List<Pet> pets = this.petsByPlayer.remove(player.getUUID());
			if (pets != null) {
				pets.forEach(Pet::remove);
			}
		});
	}

	private void tick() {
		this.petsByPlayer.values().removeIf(pets -> {
			pets.removeIf(pet -> pet.tick(game));
			return pets.isEmpty();
		});
	}

	private void onCreatureSpawnedFromEgg(CreatureEntity entity, ServerPlayerEntity player) {
		PetConfig config = this.petTypes.get(entity.getType());
		if (config != null) {
			this.stopPetFromGettingSidetracked(entity);

			this.addPet(new Pet(player, entity, config));
		}
	}

	private void stopPetFromGettingSidetracked(CreatureEntity entity) {
		entity.goalSelector.disableControlFlag(Goal.Flag.MOVE);
		entity.targetSelector.disableControlFlag(Goal.Flag.TARGET);
	}

	private void onCreatureDeath(CreatureEntity entity) {
		Pet pet = this.findPet(entity);
		if (pet != null) {
			this.removePet(pet);
		}
	}

	@Nullable
	private Pet findPet(CreatureEntity entity) {
		for (List<Pet> pets : this.petsByPlayer.values()) {
			for (Pet pet : pets) {
				if (pet.entity == entity) {
					return pet;
				}
			}
		}
		return null;
	}

	private void addPet(Pet pet) {
		List<Pet> petsByPlayer = this.petsByPlayer.computeIfAbsent(pet.player.getUUID(), u -> new ReferenceArrayList<>());
		petsByPlayer.add(pet);
	}

	private void removePet(Pet pet) {
		List<Pet> pets = this.petsByPlayer.get(pet.player.getUUID());
		pets.remove(pet);

		if (pets.isEmpty()) {
			this.petsByPlayer.remove(pet.player.getUUID());
		}
	}

	static final class PetConfig {
		public static final Codec<PetConfig> CODEC = RecordCodecBuilder.create(instance -> {
			return instance.group(
					Codec.FLOAT.fieldOf("attack_damage").forGetter(c -> c.attackDamage),
					Codec.FLOAT.optionalFieldOf("speed", 1.0F).forGetter(c -> c.speed)
			).apply(instance, PetConfig::new);
		});

		final float attackDamage;
		final float speed;

		PetConfig(float attackDamage, float speed) {
			this.attackDamage = attackDamage;
			this.speed = speed;
		}
	}

	static final class Pet {
		private static final int ATTACK_COOLDOWN = 10;

		final ServerPlayerEntity player;
		final CreatureEntity entity;
		final PetConfig config;

		private int attackCooldown;

		Pet(ServerPlayerEntity player, CreatureEntity entity, PetConfig config) {
			this.player = player;
			this.entity = entity;
			this.config = config;
		}

		boolean tick(IGamePhase game) {
			if (!this.player.isAlive() || !this.entity.isAlive() || !game.getParticipants().contains(this.player)) {
				return true;
			}

			this.tickTargetSelection();

			LivingEntity target = this.entity.getTarget();
			if (target != null) {
				this.tickAttacking(target);
			} else {
				this.tickFollowingPlayer();
			}

			return false;
		}

		private void tickTargetSelection() {
			LivingEntity target = this.player.getLastHurtByMob();
			if (target != null && !target.isAlive()) {
				target = null;
			}

			if (this.entity.getTarget() != target) {
				this.entity.setTarget(target);
			}
		}

		private void tickAttacking(LivingEntity target) {
			if (this.attackCooldown > 0) {
				this.attackCooldown--;
			}

			double attackDistance = (this.entity.getBbWidth() + target.getBbWidth()) / 2.0 + 0.5;
			if (this.entity.distanceToSqr(target) <= attackDistance * attackDistance) {
				this.tickAttackTarget(target);
			} else {
				this.tickMoveToTarget(target);
			}
		}

		private void tickAttackTarget(LivingEntity target) {
			if (this.attackCooldown > 0) {
				return;
			}

			this.attackCooldown = ATTACK_COOLDOWN;

			DamageSource source = DamageSource.mobAttack(this.entity);
			if (target.hurt(source, this.config.attackDamage)) {
				this.entity.setLastHurtMob(target);
			}
		}

		private void tickMoveToTarget(LivingEntity target) {
			if (!this.isMovingTowards(target, 2.0)) {
				this.entity.getNavigation().moveTo(target, this.config.speed);
			}
		}

		private void tickFollowingPlayer() {
			if (this.isMovingTowards(this.player, 3.0)) {
				return;
			}

			double distance2 = this.entity.distanceToSqr(this.player);
			if (distance2 > FOLLOW_DISTANCE * FOLLOW_DISTANCE) {
				PathNavigator navigator = this.entity.getNavigation();
				Path path = navigator.createPath(this.player, 3);
				if (path != null) {
					navigator.moveTo(path, this.config.speed);
				}
			}
		}

		private boolean isMovingTowards(LivingEntity target, double threshold) {
			Path path = this.entity.getNavigation().getPath();
			return path != null && path.getTarget().closerThan(target.position(), threshold);
		}

		void remove() {
			this.entity.hurt(DamageSource.OUT_OF_WORLD, Float.MAX_VALUE);
		}
	}
}
