package com.lovetropics.minigames.common.core.diguise;

import com.lovetropics.minigames.Constants;
import com.lovetropics.minigames.common.core.network.LoveTropicsNetwork;
import com.lovetropics.minigames.common.core.network.PlayerDisguiseMessage;
import com.mojang.brigadier.Command;
import net.minecraft.command.Commands;
import net.minecraft.command.arguments.EntitySummonArgument;
import net.minecraft.command.arguments.SuggestionProviders;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Registry;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.network.PacketDistributor;

import javax.annotation.Nullable;

@Mod.EventBusSubscriber(modid = Constants.MODID)
public final class ServerPlayerDisguises {
	@SubscribeEvent
	public static void onRegisterCommands(RegisterCommandsEvent event) {
		// @formatter:off
		event.getDispatcher().register(
			Commands.literal("disguise").requires(source -> source.hasPermissionLevel(3))
				.then(Commands.literal("as")
					.then(Commands.argument("entity", EntitySummonArgument.entitySummon())
						.suggests(SuggestionProviders.SUMMONABLE_ENTITIES)
						.executes(context -> {
							ServerPlayerEntity player = context.getSource().asPlayer();
							ResourceLocation entityId = EntitySummonArgument.getEntityId(context, "entity");
							EntityType<?> entityType = Registry.ENTITY_TYPE.getOrDefault(entityId);

							ServerPlayerDisguises.set(player, entityType);

							return Command.SINGLE_SUCCESS;
						})
					))
				.then(Commands.literal("clear")
					.executes(context -> {
						ServerPlayerEntity player = context.getSource().asPlayer();
						ServerPlayerDisguises.set(player, null);

						return Command.SINGLE_SUCCESS;
					})
				)
		);
		// @formatter:on
	}

	@SubscribeEvent
	public static void onEntityTrack(PlayerEvent.StartTracking event) {
		if (!(event.getPlayer() instanceof ServerPlayerEntity)) {
			return;
		}

		ServerPlayerEntity player = (ServerPlayerEntity) event.getPlayer();
		Entity tracked = event.getTarget();

		if (tracked instanceof PlayerEntity) {
			Entity disguise = PlayerDisguise.getDisguiseEntity((PlayerEntity) tracked);
			if (disguise != null) {
				LoveTropicsNetwork.CHANNEL.send(
						PacketDistributor.PLAYER.with(() -> player),
						new PlayerDisguiseMessage(player.getUniqueID(), disguise.getType())
				);
			}
		}
	}

	public static void set(ServerPlayerEntity player, @Nullable EntityType<?> disguiseType) {
		PlayerDisguise.get(player).ifPresent(playerDisguise -> {
			Entity disguise = disguiseType != null ? createDisguiseEntity(player, disguiseType) : null;

			playerDisguise.setDisguiseEntity(disguise);
			onSetDisguise(player, disguise);

			LoveTropicsNetwork.CHANNEL.send(
					PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> player),
					new PlayerDisguiseMessage(player.getUniqueID(), disguiseType)
			);
		});
	}

	@Nullable
	private static Entity createDisguiseEntity(ServerPlayerEntity player, EntityType<?> type) {
		return type.create(player.world);
	}

	private static void onSetDisguise(ServerPlayerEntity player, @Nullable Entity disguise) {
		PlayerDisguiseBehavior.clearAttributes(player);

		if (disguise instanceof LivingEntity) {
			PlayerDisguiseBehavior.applyAttributes(player, (LivingEntity) disguise);
		}
	}
}
