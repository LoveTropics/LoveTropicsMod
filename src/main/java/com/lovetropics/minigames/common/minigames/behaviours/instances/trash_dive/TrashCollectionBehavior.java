package com.lovetropics.minigames.common.minigames.behaviours.instances.trash_dive;

import com.google.common.collect.ImmutableList;
import com.lovetropics.minigames.common.minigames.IMinigameInstance;
import com.lovetropics.minigames.common.minigames.PlayerSet;
import com.lovetropics.minigames.common.minigames.behaviours.IMinigameBehavior;
import com.lovetropics.minigames.common.minigames.behaviours.IMinigameBehaviorType;
import com.lovetropics.minigames.common.minigames.behaviours.MinigameBehaviorTypes;
import com.mojang.datafixers.Dynamic;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.util.Direction;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;

import java.util.UUID;

public final class TrashCollectionBehavior implements IMinigameBehavior {
	private final LongSet remainingTrash = new LongOpenHashSet();
	private final Object2IntOpenHashMap<UUID> pointsByPlayer = new Object2IntOpenHashMap<>();
	private int totalPoints;

	private boolean gameOver;

	public static <T> TrashCollectionBehavior parse(Dynamic<T> root) {
		return new TrashCollectionBehavior();
	}

	@Override
	public ImmutableList<IMinigameBehaviorType<?>> dependencies() {
		return ImmutableList.of(MinigameBehaviorTypes.PLACE_TRASH.get());
	}

	@Override
	public void onStart(IMinigameInstance minigame) {
		PlaceTrashBehavior trash = minigame.getBehaviorOrThrow(MinigameBehaviorTypes.PLACE_TRASH.get());
		remainingTrash.addAll(trash.getTrashBlocks());

		minigame.getBehavior(MinigameBehaviorTypes.TIMED.get()).ifPresent(timed -> {
			timed.onFinish(this::onGameOver);
		});

		PlayerSet players = minigame.getPlayers();
		players.addPotionEffect(new EffectInstance(Effects.NIGHT_VISION, Integer.MAX_VALUE, 1, false, true));
		players.addPotionEffect(new EffectInstance(Effects.WATER_BREATHING, Integer.MAX_VALUE, 1, false, true));
	}

	@Override
	public void onFinish(IMinigameInstance minigame) {
		this.onGameOver(minigame);
	}

	@Override
	public void onPlayerLeftClickBlock(IMinigameInstance minigame, ServerPlayerEntity player, BlockPos pos, Direction face) {
		if (remainingTrash.remove(pos.toLong())) {
			minigame.getWorld().removeBlock(pos, false);
			givePointsTo(player, 1);

			if (remainingTrash.isEmpty()) {
				this.onGameOver(minigame);
			}
		}
	}

	private void onGameOver(IMinigameInstance minigame) {
		if (gameOver) return;

		gameOver = true;
		minigame.getPlayers().sendMessage(new StringTextComponent("Game finished!").applyTextStyles(TextFormatting.GREEN));
	}

	private void givePointsTo(ServerPlayerEntity player, int points) {
		totalPoints += points;
		pointsByPlayer.addTo(player.getUniqueID(), points);

		player.playSound(SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, SoundCategory.PLAYERS, 1.0F, 1.0F);
	}
}
