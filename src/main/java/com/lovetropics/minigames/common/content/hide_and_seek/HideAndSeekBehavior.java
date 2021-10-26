package com.lovetropics.minigames.common.content.hide_and_seek;

import com.lovetropics.lib.BlockBox;
import com.lovetropics.lib.codec.MoreCodecs;
import com.lovetropics.minigames.common.core.diguise.DisguiseType;
import com.lovetropics.minigames.common.core.diguise.PlayerDisguise;
import com.lovetropics.minigames.common.core.diguise.ServerPlayerDisguises;
import com.lovetropics.minigames.common.core.dimension.DimensionUtils;
import com.lovetropics.minigames.common.core.game.GameException;
import com.lovetropics.minigames.common.core.game.IGamePhase;
import com.lovetropics.minigames.common.core.game.behavior.IGameBehavior;
import com.lovetropics.minigames.common.core.game.behavior.event.EventRegistrar;
import com.lovetropics.minigames.common.core.game.behavior.event.GamePhaseEvents;
import com.lovetropics.minigames.common.core.game.behavior.event.GamePlayerEvents;
import com.lovetropics.minigames.common.core.game.player.PlayerRole;
import com.lovetropics.minigames.common.core.game.player.PlayerSet;
import com.lovetropics.minigames.common.core.game.state.team.TeamKey;
import com.lovetropics.minigames.common.core.game.state.team.TeamState;
import com.lovetropics.minigames.common.core.game.util.PlayerSnapshot;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.text.StringTextComponent;

import java.util.List;
import java.util.Random;

// TODO: clean up and split up
public final class HideAndSeekBehavior implements IGameBehavior {
	public static final Codec<HideAndSeekBehavior> CODEC = RecordCodecBuilder.create(instance -> {
		return instance.group(
				Codec.STRING.fieldOf("spawn").forGetter(c -> c.spawnRegionKey),
				Codec.INT.fieldOf("initial_hide_seconds").forGetter(c -> c.initialHideSeconds),
				DisguiseType.CODEC.listOf().fieldOf("disguises").forGetter(c -> c.disguises),
				Creature.CODEC.listOf().fieldOf("creatures").forGetter(c -> c.creatures)
		).apply(instance, HideAndSeekBehavior::new);
	});

	private final String spawnRegionKey;
	private final int initialHideSeconds;
	private final List<DisguiseType> disguises;
	private final List<Creature> creatures;

	private IGamePhase game;
	private TeamState teams;
	private TeamKey hiders;
	private TeamKey seekers;

	private BlockBox spawnRegion;

	public HideAndSeekBehavior(String spawnRegionKey, int initialHideSeconds, List<DisguiseType> disguises, List<Creature> creatures) {
		this.spawnRegionKey = spawnRegionKey;
		this.initialHideSeconds = initialHideSeconds;
		this.disguises = disguises;
		this.creatures = creatures;
	}

	@Override
	public void register(IGamePhase game, EventRegistrar events) {
		this.game = game;

		teams = game.getState().getOrThrow(TeamState.KEY);

		if (disguises.isEmpty()) {
			throw new GameException(new StringTextComponent("No possible disguises!"));
		}

		hiders = teams.getTeamByKey("hiders");
		seekers = teams.getTeamByKey("seekers");

		if (hiders == null || seekers == null) {
			throw new GameException(new StringTextComponent("Missing hiders or seekers team!"));
		}

		spawnRegion = game.getMapRegions().getOrThrow(spawnRegionKey);

		events.listen(GamePhaseEvents.START, this::start);

		events.listen(GamePlayerEvents.SET_ROLE, this::onPlayerSetRole);
		events.listen(GamePlayerEvents.ATTACK, this::onPlayerAttack);
		events.listen(GamePlayerEvents.DEATH, this::onPlayerDeath);
	}

	private void start() {
		int hideTicks = initialHideSeconds * 20;

		PlayerSet seekers = teams.getPlayersForTeam(this.seekers);
		PlayerSet hiders = teams.getPlayersForTeam(this.hiders);

		seekers.addPotionEffect(new EffectInstance(Effects.SLOWNESS, hideTicks, 255, true, false));
		seekers.addPotionEffect(new EffectInstance(Effects.BLINDNESS, hideTicks, 255, true, false));

		seekers.sendMessage(new StringTextComponent("You will be let out to catch the hiders in " + initialHideSeconds + " seconds!"));
		hiders.sendMessage(new StringTextComponent("You have " + initialHideSeconds + " seconds to hide from the seekers!"));

		for (ServerPlayerEntity player : game.getParticipants()) {
			if (teams.isOnTeam(player, this.hiders)) {
				setHider(player);
			} else {
				setSeeker(player);
			}
		}
	}

	private void onPlayerSetRole(ServerPlayerEntity player, PlayerRole role, PlayerRole lastRole) {
		this.spawnPlayer(player);

		if (lastRole == PlayerRole.PARTICIPANT) {
			this.removeParticipant(player);
		}
	}

	private void spawnPlayer(ServerPlayerEntity player) {
		BlockPos spawnPos = spawnRegion.sample(player.getRNG());
		DimensionUtils.teleportPlayerNoPortal(player, game.getDimension(), spawnPos);
	}

	private ActionResultType onPlayerAttack(ServerPlayerEntity player, Entity target) {
		if (teams.isOnTeam(player, seekers) && player.getHeldItemMainhand().getItem() == HideAndSeek.NET.get()) {
			if (target instanceof ServerPlayerEntity && teams.isOnTeam((ServerPlayerEntity) target, hiders)) {
				return ActionResultType.PASS;
			}
		}
		return ActionResultType.FAIL;
	}

	private ActionResultType onPlayerDeath(ServerPlayerEntity player, DamageSource damageSource) {
		TeamKey team = teams.getTeamForPlayer(player);
		if (team == hiders) {
			this.onHiderCaptured(player);
		} else if (team == seekers) {
			this.onSeekerDied(player);
		}
		return ActionResultType.FAIL;
	}

	private void removeParticipant(ServerPlayerEntity player) {
		if (teams.isOnTeam(player, hiders)) {
			this.removeHider(player);
		}
	}

	private void setHider(ServerPlayerEntity player) {
		DisguiseType disguise = nextDisguiseType(player.world.rand);
		ServerPlayerDisguises.set(player, disguise);
	}

	private void setSeeker(ServerPlayerEntity player) {
		PlayerSnapshot.clearPlayer(player);
		player.inventory.addItemStackToInventory(new ItemStack(HideAndSeek.NET.get()));
	}

	private DisguiseType nextDisguiseType(Random random) {
		DisguiseType type = disguises.get(random.nextInt(disguises.size()));
		CompoundNBT nbt = type.nbt != null ? type.nbt.copy() : new CompoundNBT();
		nbt.putBoolean("CustomNameVisible", false);
		return DisguiseType.create(type.type, nbt, type.applyAttributes);
	}

	private void removeHider(ServerPlayerEntity player) {
		PlayerDisguise.get(player).ifPresent(PlayerDisguise::clearDisguise);
	}

	private void onHiderCaptured(ServerPlayerEntity player) {
		teams.addPlayerTo(player, seekers);

		this.spawnPlayer(player);
		this.setSeeker(player);
	}

	private void onSeekerDied(ServerPlayerEntity player) {
		this.spawnPlayer(player);
	}

	public static final class Creature {
		public static final Codec<Creature> CODEC = RecordCodecBuilder.create(instance -> {
			return instance.group(
					Registry.ENTITY_TYPE.fieldOf("entity").forGetter(c -> c.entity),
					MoreCodecs.arrayOrUnit(Codec.STRING, String[]::new).fieldOf("region").forGetter(c -> c.regionKeys)
			).apply(instance, Creature::new);
		});

		final EntityType<?> entity;
		final String[] regionKeys;

		Creature(EntityType<?> entity, String[] regionKeys) {
			this.entity = entity;
			this.regionKeys = regionKeys;
		}
	}
}
