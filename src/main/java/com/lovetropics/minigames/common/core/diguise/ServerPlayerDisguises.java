package com.lovetropics.minigames.common.core.diguise;

import com.lovetropics.minigames.Constants;
import com.lovetropics.minigames.common.core.network.LoveTropicsNetwork;
import com.lovetropics.minigames.common.core.network.PlayerDisguiseMessage;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.ResourceArgument;
import net.minecraft.commands.synchronization.SuggestionProviders;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;

import javax.annotation.Nullable;
import java.util.function.Consumer;

import static com.mojang.brigadier.arguments.FloatArgumentType.floatArg;
import static com.mojang.brigadier.arguments.FloatArgumentType.getFloat;
import static net.minecraft.commands.arguments.CompoundTagArgument.compoundTag;
import static net.minecraft.commands.arguments.CompoundTagArgument.getCompoundTag;

@Mod.EventBusSubscriber(modid = Constants.MODID)
public final class ServerPlayerDisguises {
	@SubscribeEvent
	public static void onRegisterCommands(RegisterCommandsEvent event) {
		// @formatter:off
		event.getDispatcher().register(
			Commands.literal("disguise").requires(source -> source.hasPermission(Commands.LEVEL_GAMEMASTERS))
				.then(Commands.literal("as")
					.then(Commands.argument("entity", ResourceArgument.resource(event.getBuildContext(), Registries.ENTITY_TYPE))
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
		if (!(event.getEntity() instanceof ServerPlayer player)) {
			return;
		}

		if (event.getTarget() instanceof Player tracked) {
			PlayerDisguise disguise = PlayerDisguise.get(tracked);
			DisguiseType type = disguise.type();
			if (type != null) {
				LoveTropicsNetwork.CHANNEL.send(
						PacketDistributor.PLAYER.with(() -> player),
						new PlayerDisguiseMessage(tracked.getUUID(), type)
				);
			}
		}
	}

	@SubscribeEvent
	public static void onPlayerClone(PlayerEvent.Clone event) {
		if (event.isWasDeath()) {
			return;
		}

		if (event.getEntity() instanceof ServerPlayer newPlayer && event.getOriginal() instanceof ServerPlayer oldPlayer) {
			PlayerDisguise newDisguise = PlayerDisguise.get(newPlayer);
			newDisguise.copyFrom(PlayerDisguise.get(oldPlayer));
			onSetDisguise(newPlayer);
		}
	}

	private static int disguiseAs(CommandContext<CommandSourceStack> context, float scale, @Nullable CompoundTag nbt) throws CommandSyntaxException {
		Holder.Reference<EntityType<?>> entityType = ResourceArgument.getSummonableEntityType(context, "entity");
		set(context.getSource().getPlayerOrException(), new DisguiseType(entityType.value(), scale, nbt));
		return Command.SINGLE_SUCCESS;
	}

	private static int clearDisguise(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
		ServerPlayer player = context.getSource().getPlayerOrException();
		ServerPlayerDisguises.set(player, null);

		return Command.SINGLE_SUCCESS;
	}

	public static void set(ServerPlayer player, DisguiseType type) {
		PlayerDisguise.get(player).set(type);
		onSetDisguise(player);
	}

	public static void update(ServerPlayer player, Consumer<PlayerDisguise> consumer) {
		consumer.accept(PlayerDisguise.get(player));
		onSetDisguise(player);
	}

	public static void clear(ServerPlayer player) {
		update(player, PlayerDisguise::clear);
	}

	public static void clear(ServerPlayer player, DisguiseType type) {
		update(player, disguise -> disguise.clear(type));
	}

	private static void onSetDisguise(ServerPlayer player) {
		PlayerDisguise disguise = PlayerDisguise.get(player);
		LoveTropicsNetwork.CHANNEL.send(
				PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> player),
				new PlayerDisguiseMessage(player.getUUID(), disguise.type())
		);

		PlayerDisguiseBehavior.clearAttributes(player);

		DisguiseType disguiseType = disguise.type();
		if (disguiseType != null && disguiseType.applyAttributes()) {
			if (disguise.entity() instanceof LivingEntity living) {
				PlayerDisguiseBehavior.applyAttributes(player, living);
			}
		}
	}
}
