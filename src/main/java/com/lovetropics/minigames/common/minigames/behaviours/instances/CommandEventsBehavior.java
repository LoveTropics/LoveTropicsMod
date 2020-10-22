package com.lovetropics.minigames.common.minigames.behaviours.instances;

import com.lovetropics.minigames.common.minigames.IMinigameInstance;
import com.lovetropics.minigames.common.minigames.PlayerRole;
import com.mojang.datafixers.Dynamic;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;

import java.util.List;
import java.util.Map;

public final class CommandEventsBehavior extends CommandInvokeBehavior {
	public CommandEventsBehavior(Map<String, List<String>> commands) {
		super(commands);
	}

	public static <T> CommandEventsBehavior parse(Dynamic<T> root) {
		return new CommandEventsBehavior(parseCommands(root));
	}

	@Override
	public void onMapReady(IMinigameInstance minigame) {
		this.invoke("ready");
	}

	@Override
	public void onStart(IMinigameInstance minigame) {
		super.onStart(minigame);
		this.invoke("start");
	}

	@Override
	public void worldUpdate(IMinigameInstance minigame, World world) {
		this.invoke("update");
	}

	@Override
	public void onPlayerJoin(IMinigameInstance minigame, ServerPlayerEntity player, PlayerRole role) {
		if (role == PlayerRole.PARTICIPANT) {
			this.invoke("player_join", sourceForEntity(player));
		} else {
			this.invoke("player_spectate", sourceForEntity(player));
		}
	}

	@Override
	public void onPlayerChangeRole(IMinigameInstance minigame, ServerPlayerEntity player, PlayerRole role) {
		this.onPlayerJoin(minigame, player, role);
	}

	@Override
	public void onPlayerLeave(IMinigameInstance minigame, ServerPlayerEntity player) {
		this.invoke("player_leave", sourceForEntity(player));
	}

	@Override
	public void onPlayerDeath(IMinigameInstance minigame, ServerPlayerEntity player, LivingDeathEvent event) {
		this.invoke("player_death", sourceForEntity(player));
	}

	@Override
	public void onLivingEntityUpdate(IMinigameInstance minigame, LivingEntity entity) {
		this.invoke("entity_update", sourceForEntity(entity));
	}

	@Override
	public void onPlayerUpdate(IMinigameInstance minigame, ServerPlayerEntity player) {
		this.invoke("player_update", sourceForEntity(player));
	}

	@Override
	public void onPlayerRespawn(IMinigameInstance minigame, ServerPlayerEntity player) {
		this.invoke("player_respawn", sourceForEntity(player));
	}

	@Override
	public void onFinish(IMinigameInstance minigame) {
		this.invoke("finish");
	}

	@Override
	public void onPostFinish(IMinigameInstance minigame) {
		this.invoke("post_finish");
	}

	@Override
	public void onCancel(IMinigameInstance minigame) {
		this.invoke("cancel");
	}

	@Override
	public void onPlayerHurt(IMinigameInstance minigame, LivingHurtEvent event) {
		this.invoke("player_hurt", sourceForEntity(event.getEntity()));
	}

	@Override
	public void onPlayerAttackEntity(IMinigameInstance minigame, AttackEntityEvent event) {
		this.invoke("player_attack", sourceForEntity(event.getTarget()));
	}
}
