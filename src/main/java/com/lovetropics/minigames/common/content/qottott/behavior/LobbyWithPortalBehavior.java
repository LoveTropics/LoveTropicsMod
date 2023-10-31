package com.lovetropics.minigames.common.content.qottott.behavior;

import com.lovetropics.lib.BlockBox;
import com.lovetropics.minigames.common.core.game.GameException;
import com.lovetropics.minigames.common.core.game.IGamePhase;
import com.lovetropics.minigames.common.core.game.behavior.IGameBehavior;
import com.lovetropics.minigames.common.core.game.behavior.event.EventRegistrar;
import com.lovetropics.minigames.common.core.game.behavior.event.GamePhaseEvents;
import com.lovetropics.minigames.common.core.game.behavior.event.GamePlayerEvents;
import com.lovetropics.minigames.common.core.game.player.PlayerRole;
import com.lovetropics.minigames.common.core.game.state.ProgressionPoint;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.NetherPortalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.apache.commons.lang3.mutable.MutableBoolean;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.function.BooleanSupplier;

public record LobbyWithPortalBehavior(String portalRegion, String targetRegion, String pointTowardsRegion, ProgressionPoint openAt) implements IGameBehavior {
	public static final MapCodec<LobbyWithPortalBehavior> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
			Codec.STRING.fieldOf("portal_region").forGetter(LobbyWithPortalBehavior::portalRegion),
			Codec.STRING.fieldOf("target_region").forGetter(LobbyWithPortalBehavior::targetRegion),
			Codec.STRING.fieldOf("point_towards_region").forGetter(LobbyWithPortalBehavior::pointTowardsRegion),
			ProgressionPoint.CODEC.fieldOf("open_at").forGetter(LobbyWithPortalBehavior::openAt)
	).apply(i, LobbyWithPortalBehavior::new));

	@Override
	public void register(final IGamePhase game, final EventRegistrar events) {
		final BlockBox portal = game.getMapRegions().getOrThrow(portalRegion);
		final List<BlockBox> targets = game.getMapRegions().getAll(targetRegion);
		if (targets.isEmpty()) {
			throw new GameException(Component.literal("No targets for portal"));
		}

		final Vec3 pointTowards = game.getMapRegions().getOrThrow(pointTowardsRegion).center();

		final BooleanSupplier predicate = openAt.createPredicate(game);
		final MutableBoolean openedPortal = new MutableBoolean();
		events.listen(GamePhaseEvents.TICK, () -> {
			if (!openedPortal.getValue() && predicate.getAsBoolean()) {
				setPortal(game.getLevel(), portal);
				openedPortal.setTrue();
			}
		});

		final Set<UUID> playersInLobby = new ObjectOpenHashSet<>();
		events.listen(GamePlayerEvents.SPAWN, (playerId, spawn, role) -> {
			if (role == PlayerRole.PARTICIPANT) {
				playersInLobby.add(playerId);
			}
		});

		events.listen(GamePlayerEvents.TICK, player -> {
			if (!predicate.getAsBoolean()) {
				return;
			}
			if (portal.contains(player.position()) && playersInLobby.remove(player.getUUID())) {
				final BlockBox target = Util.getRandom(targets, game.getRandom());
				final Vec3 center = target.center();
				player.teleportTo(player.serverLevel(), center.x, center.y, center.z, computeAngle(center, pointTowards), 0.0f);
				player.level().playSound(null, center.x, center.y, center.z, SoundEvents.CHORUS_FRUIT_TELEPORT, SoundSource.PLAYERS, 1.0f, 1.0f);
			}
		});

		events.listen(GamePlayerEvents.ATTACK, (player, target) -> checkInLobby(player, playersInLobby));
		events.listen(GamePlayerEvents.DAMAGE, (player, damageSource, amount) -> checkInLobby(player, playersInLobby));
	}

	private float computeAngle(final Vec3 pos, final Vec3 target) {
		final double deltaX = target.x - pos.x;
		final double deltaZ = target.z - pos.z;
		return (float) Math.atan2(-deltaX, deltaZ) * Mth.RAD_TO_DEG;
	}

	private static void setPortal(final ServerLevel level, final BlockBox portal) {
		final Direction.Axis portalAxis = portal.size().getX() > 1 ? Direction.Axis.X : Direction.Axis.Z;
		final BlockState portalState = Blocks.NETHER_PORTAL.defaultBlockState().setValue(NetherPortalBlock.AXIS, portalAxis);
		for (final BlockPos pos : portal) {
			level.setBlock(pos, portalState, Block.UPDATE_CLIENTS);
		}
	}

	private static InteractionResult checkInLobby(final ServerPlayer player, final Set<UUID> playersInLobby) {
		if (playersInLobby.contains(player.getUUID())) {
			return InteractionResult.FAIL;
		}
		return InteractionResult.PASS;
	}
}
