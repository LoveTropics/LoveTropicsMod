package com.lovetropics.minigames.common.core.game.behavior.instances;

import com.lovetropics.minigames.common.core.game.GameException;
import com.lovetropics.minigames.common.core.game.IGamePhase;
import com.lovetropics.minigames.common.core.game.behavior.IGameBehavior;
import com.lovetropics.minigames.common.core.game.behavior.event.EventRegistrar;
import com.lovetropics.minigames.common.core.game.behavior.event.GamePhaseEvents;
import com.lovetropics.minigames.common.core.game.behavior.event.GamePlayerEvents;
import com.lovetropics.minigames.common.core.game.lobby.IGameLobbyPlayers;
import com.lovetropics.minigames.common.core.game.player.PlayerRole;
import com.lovetropics.minigames.common.core.game.util.SelectorItems;
import com.mojang.serialization.Codec;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.IItemProvider;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.UUID;

// TODO: it is weird that we have both the lobby role system and this which resets it! how can we consolidate them?
public final class ReadyUpBehavior implements IGameBehavior {
	public static final Codec<ReadyUpBehavior> CODEC = Codec.unit(ReadyUpBehavior::new);

	private final Map<UUID, PlayerRole> requestedRoles = new Object2ObjectOpenHashMap<>();

	private SelectorItems<PlayerRole> selectors;

	// TODO: lower countdown when everyone has readied-up

	@Override
	public void register(IGamePhase game, EventRegistrar events) throws GameException {
		SelectorItems.Handlers<PlayerRole> handlers = new SelectorItems.Handlers<PlayerRole>() {
			@Override
			public void onPlayerSelected(ServerPlayerEntity player, PlayerRole role) {
				requestedRoles.put(player.getUniqueID(), role);

				if (role == PlayerRole.PARTICIPANT) {
					player.sendStatusMessage(new StringTextComponent("You are queued to participate in the next game.").mergeStyle(TextFormatting.AQUA), true);
				} else {
					player.sendStatusMessage(new StringTextComponent("You will spectate the next game.").mergeStyle(TextFormatting.WHITE), true);
				}
			}

			@Override
			public String getIdFor(PlayerRole role) {
				return role.getKey();
			}

			@Override
			public ITextComponent getNameFor(PlayerRole role) {
				if (role == PlayerRole.PARTICIPANT) {
					return new StringTextComponent("Ready-up to participate!").mergeStyle(TextFormatting.AQUA);
				} else {
					return new StringTextComponent("Spectate the game").mergeStyle(TextFormatting.WHITE);
				}
			}

			@Override
			public IItemProvider getItemFor(PlayerRole role) {
				return role == PlayerRole.PARTICIPANT ? Blocks.CYAN_STAINED_GLASS : Blocks.WHITE_STAINED_GLASS;
			}
		};

		selectors = new SelectorItems<>(handlers, PlayerRole.values());
		selectors.applyTo(events);

		events.listen(GamePlayerEvents.ADD, player -> {
			player.sendStatusMessage(new StringTextComponent("Ready-up before the game!").mergeStyle(TextFormatting.GOLD, TextFormatting.BOLD), false);
			player.sendStatusMessage(new StringTextComponent("Right-click with the relevant item in your inventory to be apart of the next game!").mergeStyle(TextFormatting.GRAY), false);
			player.playSound(SoundEvents.ENTITY_ARROW_HIT_PLAYER, SoundCategory.MASTER, 1.0F, 1.0F);

			selectors.giveSelectorsTo(player);
		});

		events.listen(GamePhaseEvents.STOP, reason -> updateRoleAssignments(game));
	}

	private void updateRoleAssignments(IGamePhase game) {
		IGameLobbyPlayers players = game.getLobby().getPlayers();
		for (ServerPlayerEntity player : players) {
			PlayerRole newRole = getNewRegisteredRole(game, player);
			players.changeRole(player, newRole);
		}
	}

	// TODO: we should probably split into two enums for registered player role & actual player role
	@Nullable
	private PlayerRole getNewRegisteredRole(IGamePhase game, ServerPlayerEntity player) {
		PlayerRole requestedRole = requestedRoles.get(player.getUniqueID());
		if (requestedRole == PlayerRole.PARTICIPANT) {
			if (this.isForceJoined(game, player)) {
				return PlayerRole.PARTICIPANT;
			}
			return null;
		} else {
			return PlayerRole.SPECTATOR;
		}
	}

	private boolean isForceJoined(IGamePhase game, ServerPlayerEntity player) {
		PlayerRole registeredRole = game.getLobby().getPlayers().getRegisteredRoleFor(player);
		return registeredRole == PlayerRole.PARTICIPANT;
	}
}
