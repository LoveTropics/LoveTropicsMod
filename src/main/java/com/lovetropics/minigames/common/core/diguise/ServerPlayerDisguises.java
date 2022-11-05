package com.lovetropics.minigames.common.core.diguise;

import com.lovetropics.minigames.Constants;
import com.lovetropics.minigames.common.core.network.LoveTropicsNetwork;
import com.lovetropics.minigames.common.core.network.PlayerDisguiseMessage;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntitySummonArgument;
import net.minecraft.commands.synchronization.SuggestionProviders;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.Registry;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;

import javax.annotation.Nullable;

import static com.mojang.brigadier.arguments.FloatArgumentType.*;
import static com.mojang.brigadier.arguments.FloatArgumentType.getFloat;
import static net.minecraft.commands.arguments.CompoundTagArgument.*;

@Mod.EventBusSubscriber(modid = Constants.MODID)
public final class ServerPlayerDisguises {
	@SubscribeEvent
	public static void onRegisterCommands(RegisterCommandsEvent event) {
		// @formatter:off
		event.getDispatcher().register(
			Commands.literal("disguise").requires(source -> source.hasPermission(2))
				.then(Commands.literal("as")
					.then(Commands.argument("entity", EntitySummonArgument.id())
						.suggests(SuggestionProviders.SUMMONABLE_ENTITIES)
						.executes(context -> disguiseAs(context, 1.0f, null))
							.then(Commands.argument("scale", floatArg(0.1f, 20.0f))
								.executes(context -> disguiseAs(context, getFloat(context, "scale"), null))
								.then(Commands.argument("nbt", compoundTag())
									.executes(context -> disguiseAs(context, getFloat(context, "scale"), getCompoundTag(context, "nbt")))
								)
							)
					))
				.then(Commands.literal("clear")
					.executes(ServerPlayerDisguises::clearDisguise)
				)
		);
		// @formatter:on
	}

	@SubscribeEvent
	public static void onEntityTrack(PlayerEvent.StartTracking event) {
		if (!(event.getPlayer() instanceof ServerPlayer player)) {
			return;
		}

		if (event.getTarget() instanceof Player tracked) {
			DisguiseType disguise = PlayerDisguise.getDisguiseType(tracked);
			if (disguise != null) {
				LoveTropicsNetwork.CHANNEL.send(
						PacketDistributor.PLAYER.with(() -> player),
						new PlayerDisguiseMessage(tracked.getUUID(), disguise)
				);
			}
		}
	}

	@SubscribeEvent
	public static void onPlayerClone(PlayerEvent.Clone event) {
		if (event.isWasDeath()) {
			return;
		}

		Player newPlayer = event.getPlayer();
		Player oldPlayer = event.getOriginal();
		if (newPlayer instanceof ServerPlayer && oldPlayer instanceof ServerPlayer) {
			PlayerDisguise.get(oldPlayer).ifPresent(oldDisguise -> {
				PlayerDisguise.get(newPlayer).ifPresent(newDisguise -> {
					newDisguise.copyFrom(oldDisguise);
					onSetDisguise((ServerPlayer) newPlayer, newDisguise);
				});
			});
		}
	}

	private static int disguiseAs(CommandContext<CommandSourceStack> context, float scale, @Nullable CompoundTag nbt) throws CommandSyntaxException {
		ServerPlayer player = context.getSource().getPlayerOrException();
		ResourceLocation entityId = EntitySummonArgument.getSummonableEntity(context, "entity");
		EntityType<?> entityType = Registry.ENTITY_TYPE.get(entityId);

		ServerPlayerDisguises.set(player, DisguiseType.create(entityType, scale, nbt));

		return Command.SINGLE_SUCCESS;
	}

	private static int clearDisguise(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
		ServerPlayer player = context.getSource().getPlayerOrException();
		ServerPlayerDisguises.set(player, null);

		return Command.SINGLE_SUCCESS;
	}

	public static void set(ServerPlayer player, @Nullable DisguiseType disguiseType) {
		PlayerDisguise.get(player).ifPresent(playerDisguise -> {
			playerDisguise.setDisguise(disguiseType);
			onSetDisguise(player, playerDisguise);
		});
	}

	public static void clear(ServerPlayer player) {
		set(player, null);
	}

	public static void clear(ServerPlayer player, DisguiseType disguiseType) {
		PlayerDisguise.get(player).ifPresent(playerDisguise -> {
			playerDisguise.clearDisguise(disguiseType);
			onSetDisguise(player, playerDisguise);
		});
	}

	private static void onSetDisguise(ServerPlayer player, PlayerDisguise disguise) {
		LoveTropicsNetwork.CHANNEL.send(
				PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> player),
				new PlayerDisguiseMessage(player.getUUID(), disguise.getDisguiseType())
		);

		PlayerDisguiseBehavior.clearAttributes(player);

		DisguiseType disguiseType = disguise.getDisguiseType();
		if (disguiseType != null && disguiseType.applyAttributes) {
			Entity entity = disguise.getDisguiseEntity();
			if (entity instanceof LivingEntity) {
				PlayerDisguiseBehavior.applyAttributes(player, (LivingEntity) entity);
			}
		}
	}
}
