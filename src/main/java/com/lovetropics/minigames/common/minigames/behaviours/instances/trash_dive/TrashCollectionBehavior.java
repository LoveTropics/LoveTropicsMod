package com.lovetropics.minigames.common.minigames.behaviours.instances.trash_dive;

import com.google.common.collect.ImmutableList;
import com.lovetropics.minigames.common.minigames.IMinigameInstance;
import com.lovetropics.minigames.common.minigames.MinigameSidebar;
import com.lovetropics.minigames.common.minigames.PlayerSet;
import com.lovetropics.minigames.common.minigames.behaviours.IMinigameBehavior;
import com.lovetropics.minigames.common.minigames.behaviours.IMinigameBehaviorType;
import com.lovetropics.minigames.common.minigames.behaviours.MinigameBehaviorTypes;
import com.lovetropics.minigames.common.techstack.ParticipantEntry;
import com.mojang.authlib.GameProfile;
import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.PlayerProfileCache;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class TrashCollectionBehavior implements IMinigameBehavior {
	private int totalTrash;
	private final LongSet remainingTrash = new LongOpenHashSet();

	private final Object2IntOpenHashMap<UUID> pointsByPlayer = new Object2IntOpenHashMap<>();
	private int totalPoints;

	private boolean gameOver;

	private MinigameSidebar sidebar;

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
		totalTrash = remainingTrash.size();

		ITextComponent sidebarTitle = new StringTextComponent("Trash Dive")
				.applyTextStyles(TextFormatting.BLUE, TextFormatting.BOLD);

		sidebar = MinigameSidebar.open(sidebarTitle, minigame.getPlayers());
		sidebar.set(renderSidebar(minigame));

		minigame.getBehavior(MinigameBehaviorTypes.TIMED.get()).ifPresent(timed -> {
			timed.onFinish(this::triggerGameOver);
		});

		PlayerSet players = minigame.getPlayers();
		players.addPotionEffect(new EffectInstance(Effects.NIGHT_VISION, Integer.MAX_VALUE, 1, false, false));

		for (ServerPlayerEntity player : players) {
			player.setItemStackToSlot(EquipmentSlotType.HEAD, new ItemStack(Items.TURTLE_HELMET));
		}
	}

	@Override
	public void onFinish(IMinigameInstance minigame) {
		triggerGameOver(minigame);

		sidebar.close();
	}

	@Override
	public void onPlayerLeftClickBlock(IMinigameInstance minigame, ServerPlayerEntity player, BlockPos pos, PlayerInteractEvent.LeftClickBlock event) {
		if (!remainingTrash.remove(pos.toLong())) {
			event.setCanceled(true);
			return;
		}

		minigame.getWorld().removeBlock(pos, false);
		player.playSound(SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, SoundCategory.PLAYERS, 1.0F, 1.0F);

		totalPoints += 1;
		pointsByPlayer.addTo(player.getUniqueID(), 1);

		sidebar.set(renderSidebar(minigame));

		if (remainingTrash.isEmpty()) {
			triggerGameOver(minigame);
		}
	}

	private void triggerGameOver(IMinigameInstance minigame) {
		if (gameOver) return;

		gameOver = true;

		ITextComponent finishMessage;
		if (remainingTrash.isEmpty()) {
			finishMessage = new StringTextComponent("We collected all the trash!");
		} else {
			finishMessage = new StringTextComponent("We ran out of time!");
		}

		PlayerSet players = minigame.getPlayers();
		players.sendMessage(finishMessage.applyTextStyles(TextFormatting.GREEN));

		minigame.sendMinigameResults(buildResults(minigame));
	}

	private String[] renderSidebar(IMinigameInstance minigame) {
		int collectedTrash = totalTrash - remainingTrash.size();

		List<String> sidebar = new ArrayList<>(10);
		sidebar.add(TextFormatting.GREEN + "Pick up trash! " + TextFormatting.GRAY + collectedTrash + "/" + totalTrash);

		List<Pair<GameProfile, Integer>> leaderboard = leaderboardFor(minigame).limit(5).collect(Collectors.toList());
		if (!leaderboard.isEmpty()) {
			sidebar.add("");
			sidebar.add(TextFormatting.GREEN + "Top players:");

			for (Pair<GameProfile, Integer> entry : leaderboard) {
				sidebar.add(" - " + TextFormatting.AQUA + entry.getFirst().getName() + ": " + TextFormatting.GOLD + entry.getSecond());
			}
		}

		return sidebar.toArray(new String[0]);
	}

	private List<ParticipantEntry> buildResults(IMinigameInstance minigame) {
		// TODO: this code is duplicated: can we extract utilities for building minigame results?
		List<Pair<GameProfile, Integer>> leaderboard = leaderboardFor(minigame).collect(Collectors.toList());

		List<ParticipantEntry> participantResults = new ArrayList<>();

		int place = 0;
		int lastPoints = -1;

		for (Pair<GameProfile, Integer> entry : leaderboard) {
			GameProfile profile = entry.getFirst();
			int points = entry.getSecond();

			if (points != lastPoints) place++;
			lastPoints = points;

			participantResults.add(ParticipantEntry.withPoints(profile, place, points));
		}

		return participantResults;
	}

	private Stream<Pair<GameProfile, Integer>> leaderboardFor(IMinigameInstance minigame) {
		MinecraftServer server = minigame.getServer();
		PlayerProfileCache profileCache = server.getPlayerProfileCache();

		return pointsByPlayer.object2IntEntrySet().stream()
				.map(entry -> {
					GameProfile profile = profileCache.getProfileByUUID(entry.getKey());
					return profile != null ? Pair.of(profile, entry.getIntValue()) : null;
				})
				.filter(Objects::nonNull)
				.sorted(Comparator.<Pair<GameProfile, Integer>>comparingInt(Pair::getSecond).reversed());
	}
}
