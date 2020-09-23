package com.lovetropics.minigames.common.minigames.behaviours.instances;

import com.google.common.collect.ImmutableList;
import com.lovetropics.minigames.common.minigames.IMinigameInstance;
import com.lovetropics.minigames.common.minigames.behaviours.IMinigameBehavior;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.datafixers.Dynamic;
import net.minecraft.command.CommandSource;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.Map;

public final class CommandInvokeBehavior implements IMinigameBehavior {
	private static final Logger LOGGER = LogManager.getLogger(CommandInvokeBehavior.class);

	private final Map<String, List<String>> commands;

	private CommandDispatcher<CommandSource> dispatcher;
	private CommandSource source;

	public CommandInvokeBehavior(Map<String, List<String>> commands) {
		this.commands = commands;
	}

	public static <T> CommandInvokeBehavior parse(Dynamic<T> root) {
		Map<String, List<String>> commands = root.asMap(
				key -> key.asString(""),
				value -> value.asListOpt(CommandInvokeBehavior::parseCommand)
						.orElseGet(() -> ImmutableList.of(parseCommand(value)))
		);

		return new CommandInvokeBehavior(commands);
	}

	private static <T> String parseCommand(Dynamic<T> value) {
		String command = value.asString("");
		if (command.startsWith("/")) {
			command = command.substring(1);
		}
		return command;
	}

	private void invoke(String key) {
		this.invoke(key, this.source);
	}

	private void invoke(String key, CommandSource source) {
		List<String> commands = this.commands.get(key);
		if (commands == null || commands.isEmpty()) {
			return;
		}

		for (String command : commands) {
			try {
				this.dispatcher.execute(command, source);
			} catch (CommandSyntaxException e) {
				LOGGER.error("Failed to execute command `{}` for {}", command, key, e);
			}
		}
	}

	@Override
	public void onConstruct(IMinigameInstance minigame, MinecraftServer server) {
		this.dispatcher = server.getCommandManager().getDispatcher();
	}

	@Override
	public void onStart(IMinigameInstance minigame) {
		this.source = minigame.getCommandSource();

		this.invoke("start");
	}

	@Override
	public void worldUpdate(IMinigameInstance minigame, World world) {
		this.invoke("update");
	}

	@Override
	public void onPlayerDeath(IMinigameInstance minigame, ServerPlayerEntity player) {
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
	public void onPlayerHurt(IMinigameInstance minigame, LivingHurtEvent event) {
		this.invoke("player_hurt", sourceForEntity(event.getEntity()));
	}

	@Override
	public void onPlayerAttackEntity(IMinigameInstance minigame, AttackEntityEvent event) {
		this.invoke("player_attack", sourceForEntity(event.getTarget()));
	}

	private CommandSource sourceForEntity(Entity entity) {
		return this.source.withEntity(entity).withPos(entity.getPositionVec());
	}
}
