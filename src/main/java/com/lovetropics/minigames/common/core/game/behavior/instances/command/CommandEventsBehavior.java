package com.lovetropics.minigames.common.core.game.behavior.instances.command;

import com.lovetropics.minigames.common.core.game.IGameInstance;
import com.lovetropics.minigames.common.core.game.PlayerRole;
import com.mojang.serialization.Codec;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;

import java.util.List;
import java.util.Map;

public final class CommandEventsBehavior extends CommandInvokeBehavior {
	public static final Codec<CommandEventsBehavior> CODEC = COMMANDS_CODEC.xmap(CommandEventsBehavior::new, c -> c.commands);

	public CommandEventsBehavior(Map<String, List<String>> commands) {
		super(commands);
	}

	@Override
	public void onConstruct(IGameInstance minigame) {
		super.onConstruct(minigame);
		this.invoke("ready");
	}

	@Override
	public void onStart(IGameInstance minigame) {
		this.invoke("start");
	}

	@Override
	public void worldUpdate(IGameInstance minigame, ServerWorld world) {
		this.invoke("update");
	}

	@Override
	public void onPlayerJoin(IGameInstance minigame, ServerPlayerEntity player, PlayerRole role) {
		if (role == PlayerRole.PARTICIPANT) {
			this.invoke("player_join", sourceForEntity(player));
		} else {
			this.invoke("player_spectate", sourceForEntity(player));
		}
	}

	@Override
	public void onPlayerChangeRole(IGameInstance minigame, ServerPlayerEntity player, PlayerRole role, PlayerRole lastRole) {
		this.onPlayerJoin(minigame, player, role);
	}

	@Override
	public void onPlayerLeave(IGameInstance minigame, ServerPlayerEntity player) {
		this.invoke("player_leave", sourceForEntity(player));
	}

	@Override
	public void onPlayerDeath(IGameInstance minigame, ServerPlayerEntity player, LivingDeathEvent event) {
		this.invoke("player_death", sourceForEntity(player));
	}

	@Override
	public void onLivingEntityUpdate(IGameInstance minigame, LivingEntity entity) {
		this.invoke("entity_update", sourceForEntity(entity));
	}

	@Override
	public void onParticipantUpdate(IGameInstance minigame, ServerPlayerEntity player) {
		this.invoke("player_update", sourceForEntity(player));
	}

	@Override
	public void onPlayerRespawn(IGameInstance minigame, ServerPlayerEntity player) {
		this.invoke("player_respawn", sourceForEntity(player));
	}

	@Override
	public void onFinish(IGameInstance minigame) {
		this.invoke("finish");
	}

	@Override
	public void onPostFinish(IGameInstance minigame) {
		this.invoke("post_finish");
	}

	@Override
	public void onCancel(IGameInstance minigame) {
		this.invoke("cancel");
	}

	@Override
	public void onPlayerHurt(IGameInstance minigame, LivingHurtEvent event) {
		this.invoke("player_hurt", sourceForEntity(event.getEntity()));
	}

	@Override
	public void onPlayerAttackEntity(IGameInstance minigame, AttackEntityEvent event) {
		this.invoke("player_attack", sourceForEntity(event.getTarget()));
	}
}
