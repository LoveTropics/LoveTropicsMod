package com.lovetropics.minigames.common.content.qottott.behavior;

import com.lovetropics.lib.BlockBox;
import com.lovetropics.minigames.common.core.game.GameException;
import com.lovetropics.minigames.common.core.game.IGamePhase;
import com.lovetropics.minigames.common.core.game.behavior.IGameBehavior;
import com.lovetropics.minigames.common.core.game.behavior.event.EventRegistrar;
import com.lovetropics.minigames.common.core.game.behavior.event.GamePlayerEvents;
import com.lovetropics.minigames.common.core.game.player.PlayerRole;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.minecraft.Util;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Set;
import java.util.UUID;

public record LobbyWithPortalBehavior(String portalRegion, String targetRegion) implements IGameBehavior {
	public static final MapCodec<LobbyWithPortalBehavior> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
			Codec.STRING.fieldOf("portal_region").forGetter(LobbyWithPortalBehavior::portalRegion),
			Codec.STRING.fieldOf("target_region").forGetter(LobbyWithPortalBehavior::targetRegion)
	).apply(i, LobbyWithPortalBehavior::new));

	@Override
	public void register(final IGamePhase game, final EventRegistrar events) {
		final BlockBox portal = game.getMapRegions().getOrThrow(portalRegion);
		final List<BlockBox> targets = game.getMapRegions().getAll(targetRegion);
		if (targets.isEmpty()) {
			throw new GameException(Component.literal("No targets for portal"));
		}

		final Set<UUID> playersInLobby = new ObjectOpenHashSet<>();
		events.listen(GamePlayerEvents.SPAWN, (playerId, spawn, role) -> {
			if (role == PlayerRole.PARTICIPANT) {
				playersInLobby.add(playerId);
			}
		});

		events.listen(GamePlayerEvents.TICK, player -> {
			if (portal.contains(player.position()) && playersInLobby.remove(player.getUUID())) {
				final BlockBox target = Util.getRandom(targets, game.getRandom());
				final Vec3 center = target.center();
				player.teleportTo(center.x, center.y, center.z);
				player.level().playSound(null, center.x, center.y, center.z, SoundEvents.CHORUS_FRUIT_TELEPORT, SoundSource.PLAYERS, 1.0f, 1.0f);
			}
		});

		events.listen(GamePlayerEvents.ATTACK, (player, target) -> checkInLobby(player, playersInLobby));
		events.listen(GamePlayerEvents.DAMAGE, (player, damageSource, amount) -> checkInLobby(player, playersInLobby));
	}

	private static InteractionResult checkInLobby(final ServerPlayer player, final Set<UUID> playersInLobby) {
		if (playersInLobby.contains(player.getUUID())) {
			return InteractionResult.FAIL;
		}
		return InteractionResult.PASS;
	}
}
