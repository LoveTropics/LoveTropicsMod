package com.lovetropics.minigames.common.content.qottott.behavior;

import com.lovetropics.minigames.common.content.qottott.Qottott;
import com.lovetropics.minigames.common.core.game.IGamePhase;
import com.lovetropics.minigames.common.core.game.behavior.GameBehaviorType;
import com.lovetropics.minigames.common.core.game.behavior.IGameBehavior;
import com.lovetropics.minigames.common.core.game.behavior.action.GameActionContext;
import com.lovetropics.minigames.common.core.game.behavior.action.GameActionList;
import com.lovetropics.minigames.common.core.game.behavior.action.GameActionParameter;
import com.lovetropics.minigames.common.core.game.behavior.event.EventRegistrar;
import com.lovetropics.minigames.common.core.game.behavior.event.GamePhaseEvents;
import com.lovetropics.minigames.common.core.game.behavior.event.GamePlayerEvents;
import com.lovetropics.minigames.common.core.game.client_state.GameClientState;
import com.lovetropics.minigames.common.core.game.client_state.GameClientStateTypes;
import com.lovetropics.minigames.common.core.game.client_state.instance.PointTagClientState;
import com.lovetropics.minigames.common.core.game.state.statistics.Placed;
import com.lovetropics.minigames.common.core.game.state.statistics.PlacementOrder;
import com.lovetropics.minigames.common.core.game.state.statistics.PlayerKey;
import com.lovetropics.minigames.common.core.game.state.statistics.PlayerPlacement;
import com.lovetropics.minigames.common.core.game.state.statistics.StatisticKey;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nullable;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;

public record KillBonusBehavior(
		StatisticKey<Integer> statistic,
		PlacementOrder order,
		int maxPlacement,
		float chancePerTick,
		GameActionList<ServerPlayer> assignAnnouncement,
		float pointPercent,
		GameActionList<ServerPlayer> claimAnnouncement,
		Item tagIcon,
		String tagTranslationKey
) implements IGameBehavior {
	public static final MapCodec<KillBonusBehavior> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
			StatisticKey.INT_CODEC.fieldOf("statistic").forGetter(KillBonusBehavior::statistic),
			PlacementOrder.CODEC.optionalFieldOf("order", PlacementOrder.MAX).forGetter(KillBonusBehavior::order),
			Codec.INT.fieldOf("max_placement").forGetter(KillBonusBehavior::maxPlacement),
			Codec.FLOAT.fieldOf("chance_per_tick").forGetter(KillBonusBehavior::chancePerTick),
			GameActionList.PLAYER_CODEC.fieldOf("assign_announcement").forGetter(KillBonusBehavior::assignAnnouncement),
			Codec.floatRange(0.0f, 1.0f).fieldOf("point_percent").forGetter(KillBonusBehavior::pointPercent),
			GameActionList.PLAYER_CODEC.fieldOf("claim_announcement").forGetter(KillBonusBehavior::claimAnnouncement),
			ForgeRegistries.ITEMS.getCodec().fieldOf("tag_icon").forGetter(KillBonusBehavior::tagIcon),
			Codec.STRING.fieldOf("tag_translation_key").forGetter(KillBonusBehavior::tagTranslationKey)
	).apply(i, KillBonusBehavior::new));

	@Override
	public void register(final IGamePhase game, final EventRegistrar events) {
		assignAnnouncement.register(game, events);
		claimAnnouncement.register(game, events);

		final Object2IntMap<UUID> playersWithKillBonus = new Object2IntOpenHashMap<>();
		events.listen(GamePhaseEvents.TICK, () -> {
			if (assignKillBonuses(game, playersWithKillBonus)) {
				GameClientState.sendToPlayers(createTagState(playersWithKillBonus), game.getAllPlayers());
			}
		});
		events.listen(GamePlayerEvents.DEATH, (player, damageSource) -> {
			if (onPlayerKilled(game, player, damageSource, playersWithKillBonus)) {
				GameClientState.sendToPlayers(createTagState(playersWithKillBonus), game.getAllPlayers());
			}
			return InteractionResult.PASS;
		});

		events.listen(GamePlayerEvents.ADD, player -> GameClientState.sendToPlayer(createTagState(playersWithKillBonus), player));
		events.listen(GamePlayerEvents.REMOVE, player -> GameClientState.removeFromPlayer(GameClientStateTypes.POINT_TAGS.get(), player));
	}

	private boolean assignKillBonuses(final IGamePhase game, final Object2IntMap<UUID> playersWithKillBonus) {
		boolean assigned = false;
		for (final Placed<PlayerKey> place : PlayerPlacement.fromScore(order, game, statistic, true)) {
			if (place.placement() > maxPlacement) {
				break;
			}
			final float chanceForPlacement = chancePerTick / place.placement();
			if (game.getRandom().nextFloat() > chanceForPlacement) {
				continue;
			}
			final ServerPlayer player = game.getParticipants().getPlayerBy(place.value());
			if (player != null) {
				assigned |= assignKillBonus(game, player, playersWithKillBonus);
			}
		}
		return assigned;
	}

	private boolean assignKillBonus(final IGamePhase game, final ServerPlayer player, final Object2IntMap<UUID> playersWithKillBonus) {
		final int amount = computeKillBonus(game, player);
		if (amount > 0 && playersWithKillBonus.putIfAbsent(player.getUUID(), amount) == 0) {
			final GameActionContext context = GameActionContext.builder().set(GameActionParameter.COUNT, amount).build();
			assignAnnouncement.apply(game, context, player);
			return true;
		}
		return false;
	}

	private int computeKillBonus(final IGamePhase game, final ServerPlayer player) {
		final int points = game.getStatistics().forPlayer(player).getOr(statistic, 0);
		return Mth.floor(pointPercent * points);
	}

	private boolean onPlayerKilled(IGamePhase game, ServerPlayer player, DamageSource damageSource, Object2IntMap<UUID> playersWithKillBonus) {
		final int killBonus = playersWithKillBonus.removeInt(player.getUUID());
		if (killBonus == 0) {
			return false;
		}
		final ServerPlayer killer = getKiller(player, damageSource);
		if (killer != null) {
			claimKillBonus(game, player, killer, killBonus);
		}
		return true;
	}

	@Nullable
	private static ServerPlayer getKiller(final ServerPlayer player, final DamageSource damageSource) {
		if (damageSource.getEntity() instanceof final ServerPlayer killer) {
			return killer;
		}
		if (player.getKillCredit() instanceof final ServerPlayer killer) {
			return killer;
		}
		return null;
	}

	private void claimKillBonus(final IGamePhase game, final ServerPlayer player, final ServerPlayer killer, final int killBonus) {
		final GameActionContext context = GameActionContext.builder()
				.set(GameActionParameter.KILLER, killer)
				.set(GameActionParameter.KILLED, player)
				.set(GameActionParameter.COUNT, killBonus)
				.build();
		claimAnnouncement.apply(game, context, killer);
		game.getStatistics().forPlayer(killer).incrementInt(statistic, killBonus);
	}

	private PointTagClientState createTagState(final Object2IntMap<UUID> playersWithKillBonus) {
		return new PointTagClientState(new ItemStack(tagIcon), Optional.of(tagTranslationKey), new Object2IntOpenHashMap<>(playersWithKillBonus));
	}

	@Override
	public Supplier<? extends GameBehaviorType<?>> behaviorType() {
		return Qottott.KILL_BONUS;
	}
}
