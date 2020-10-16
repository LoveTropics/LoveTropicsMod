package com.lovetropics.minigames.common.minigames.behaviours.instances.conservation_exploration;

import com.lovetropics.lib.entity.FireworkUtil;
import com.lovetropics.minigames.common.item.MinigameItems;
import com.lovetropics.minigames.common.minigames.IMinigameInstance;
import com.lovetropics.minigames.common.minigames.MinigameManager;
import com.lovetropics.minigames.common.minigames.PlayerRole;
import com.lovetropics.minigames.common.minigames.PlayerSet;
import com.lovetropics.minigames.common.minigames.behaviours.IMinigameBehavior;
import com.mojang.authlib.GameProfile;
import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.scoreboard.ServerScoreboard;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.PlayerProfileCache;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.util.text.event.ClickEvent;
import net.minecraft.util.text.event.HoverEvent;
import net.minecraft.world.BossInfo;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerBossInfo;

import java.util.*;
import java.util.stream.Collectors;

public final class RecordCreaturesBehavior implements IMinigameBehavior {
	private static final long CLOSE_TIME = 20 * 10;

	private final Map<UUID, PlayerData> playerData = new Object2ObjectOpenHashMap<>();
	private final Set<EntityType<?>> discoveredEntities = new ReferenceOpenHashSet<>();

	private final RecordList globalRecords = new RecordList();

	// TODO: keep track of the actual entities so that we can maybe give hints / handle if the entities die?
	private int totalEntityCount;

	private long closeTime;

	private ScorePlayerTeam firstDiscoveryTeam;

	private final ServerBossInfo progressBar = new ServerBossInfo(
			new StringTextComponent("Creatures Recorded"),
			BossInfo.Color.GREEN,
			BossInfo.Overlay.PROGRESS
	);

	private boolean closing;

	public static <T> RecordCreaturesBehavior parse(Dynamic<T> root) {
		return new RecordCreaturesBehavior();
	}

	public void setTotalEntityCount(int totalEntityCount) {
		this.totalEntityCount = totalEntityCount;
		this.updateRecordedProgressBar();
	}

	@Override
	public void onStart(IMinigameInstance minigame) {
		ServerScoreboard scoreboard = minigame.getServer().getScoreboard();
		firstDiscoveryTeam = scoreboard.createTeam("first_discovery");
		firstDiscoveryTeam.setColor(TextFormatting.GREEN);

		minigame.addControlCommand("teleport_back", source -> {
			ServerPlayerEntity player = source.asPlayer();
			PlayerData data = getDataFor(player);

			Vec3d lastPosition = data.lastPosition;
			data.lastPosition = null;

			if (lastPosition != null) {
				player.teleport(minigame.getWorld(), lastPosition.x, lastPosition.y, lastPosition.z, player.rotationYaw, player.rotationPitch);
			}
		});
	}

	@Override
	public void onPlayerJoin(IMinigameInstance minigame, ServerPlayerEntity player, PlayerRole role) {
		player.addItemStackToInventory(new ItemStack(MinigameItems.RECORD_CREATURE.get()));

		this.progressBar.addPlayer(player);
	}

	@Override
	public void onPlayerLeave(IMinigameInstance minigame, ServerPlayerEntity player) {
		this.progressBar.removePlayer(player);
	}

	@Override
	public void worldUpdate(IMinigameInstance minigame, World world) {
		long ticks = minigame.ticks();

		if (this.closing && ticks >= this.closeTime) {
			MinigameManager.getInstance().finish();
		}
	}

	private void displayReport(IMinigameInstance minigame) {
		PlayerSet players = minigame.getPlayers();

		players.sendMessage(new StringTextComponent("All creatures have been found! Here are the top recorders for this round:").applyTextStyle(TextFormatting.GREEN));

		List<Pair<GameProfile, Integer>> leaderboard = buildLeaderboard(minigame);

		for (int i = 0; i < leaderboard.size(); i++) {
			Pair<GameProfile, Integer> entry = leaderboard.get(i);
			GameProfile profile = entry.getFirst();
			int count = entry.getSecond();

			players.sendMessage(
					new StringTextComponent("- " + (i + 1) + ". ")
							.appendSibling(new StringTextComponent(profile.getName()).applyTextStyle(TextFormatting.BLUE))
							.appendText(": " + count)
							.applyTextStyle(TextFormatting.GRAY)
			);
		}
	}

	private List<Pair<GameProfile, Integer>> buildLeaderboard(IMinigameInstance minigame) {
		MinecraftServer server = minigame.getServer();
		PlayerProfileCache profileCache = server.getPlayerProfileCache();

		return playerData.entrySet().stream()
				.map(entry -> {
					GameProfile profile = profileCache.getProfileByUUID(entry.getKey());
					return profile != null ? Pair.of(profile, entry.getValue().records.totalCount) : null;
				})
				.filter(Objects::nonNull)
				.sorted(Comparator.comparingInt(Pair::getSecond))
				.limit(5)
				.collect(Collectors.toList());
	}

	@Override
	public void onPlayerInteractEntity(IMinigameInstance minigame, ServerPlayerEntity player, Entity entity, Hand hand) {
		ItemStack heldItem = player.getHeldItem(hand);
		if (heldItem.isEmpty()) {
			return;
		}

		if (heldItem.getItem() == MinigameItems.RECORD_CREATURE.get()) {
			if (entity instanceof LivingEntity) {
				this.recordEntity(minigame, player, (LivingEntity) entity);
			}
		}
	}

	private void recordEntity(IMinigameInstance minigame, ServerPlayerEntity reporter, LivingEntity entity) {
		if (this.tryRecordEntity(minigame, reporter, entity)) {
			int recordedCount = this.getDataFor(reporter).records.getRecordedCount(entity.getType());
			ITextComponent entityName = new TranslationTextComponent(entity.getType().getTranslationKey());

			reporter.sendMessage(
					new StringTextComponent("You have recorded " + recordedCount + " of ")
							.appendSibling(entityName.applyTextStyle(TextFormatting.BOLD))
							.appendText("!")
							.applyTextStyle(TextFormatting.GREEN)
			);
		} else {
			reporter.sendMessage(new StringTextComponent("This creature has already been recorded!").applyTextStyle(TextFormatting.RED));
		}
	}

	private PlayerData getDataFor(PlayerEntity player) {
		return this.playerData.computeIfAbsent(player.getUniqueID(), uuid -> new PlayerData());
	}

	private boolean tryRecordEntity(IMinigameInstance minigame, ServerPlayerEntity reporter, LivingEntity entity) {
		if (!this.globalRecords.tryRecord(entity)) {
			return false;
		}

		PlayerData data = this.getDataFor(reporter);
		data.records.tryRecord(entity);

		entity.addPotionEffect(new EffectInstance(Effects.GLOWING, Integer.MAX_VALUE, 10, false, false));
		this.updateRecordedProgressBar();

		if (globalRecords.totalCount >= totalEntityCount) {
			this.closing = true;
			this.closeTime = minigame.ticks() + CLOSE_TIME;
			this.displayReport(minigame);
		}

		if (this.discoveredEntities.add(entity.getType())) {
			this.onDiscoverNewEntity(minigame, reporter, entity);
		}

		return true;
	}

	private void updateRecordedProgressBar() {
		this.progressBar.setPercent((float) globalRecords.totalCount / totalEntityCount);
		this.progressBar.setName(new StringTextComponent("Creatures recorded: " + globalRecords.totalCount + "/" + totalEntityCount));
	}

	private void onDiscoverNewEntity(IMinigameInstance minigame, ServerPlayerEntity reporter, LivingEntity entity) {
		String teleportCommand = "teleport_" + entity.getType().getTranslationKey();

		Vec3d position = reporter.getPositionVec();
		float yaw = reporter.rotationYaw;
		float pitch = reporter.rotationPitch;

		// highlight and stop the reported entity from moving for a bit
		entity.addPotionEffect(new EffectInstance(Effects.SLOWNESS, 20 * 20, 10, false, false));

		minigame.getServer().getScoreboard().addPlayerToTeam(entity.getScoreboardName(), firstDiscoveryTeam);

		FireworkUtil.spawnFirework(new BlockPos(entity), entity.world, FireworkUtil.Palette.ISLAND_ROYALE.getPalette());

		minigame.addControlCommand(teleportCommand, source -> {
			ServerPlayerEntity player = source.asPlayer();

			PlayerData data = getDataFor(player);
			if (data.lastPosition == null) {
				data.lastPosition = player.getPositionVec();
			}

			player.sendMessage(new StringTextComponent("Click here to teleport back")
					.applyTextStyles(TextFormatting.BLUE, TextFormatting.UNDERLINE)
					.applyTextStyle(style -> {
						style.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new StringTextComponent("Teleport to your previous location")));
						style.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/minigame teleport_back"));
					})
			);

			player.teleport(minigame.getWorld(), position.x, position.y, position.z, yaw, pitch);
		});

		ITextComponent message = reporter.getDisplayName()
				.appendText(" discovered a ")
				.appendSibling(new TranslationTextComponent(entity.getType().getTranslationKey()).applyTextStyle(TextFormatting.BOLD))
				.appendText("! ")
				.applyTextStyle(TextFormatting.GOLD);

		ITextComponent teleportLink = new StringTextComponent("Click here to teleport")
				.applyTextStyles(TextFormatting.BLUE, TextFormatting.UNDERLINE)
				.applyTextStyle(style -> {
					style.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new StringTextComponent("Teleport to this entity")));
					style.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/minigame " + teleportCommand));
				});

		minigame.getPlayers().sendMessage(message.appendSibling(teleportLink));
	}

	@Override
	public void onFinish(IMinigameInstance minigame) {
		this.progressBar.setVisible(false);
		this.progressBar.removeAllPlayers();

		ServerScoreboard scoreboard = minigame.getServer().getScoreboard();
		scoreboard.removeTeam(firstDiscoveryTeam);
	}

	static class PlayerData {
		final RecordList records = new RecordList();
		Vec3d lastPosition;
	}

	static class RecordList {
		final Object2IntOpenHashMap<EntityType<?>> recordedCount = new Object2IntOpenHashMap<>();
		final Set<UUID> recordedEntities = new ObjectOpenHashSet<>();
		int totalCount;

		boolean tryRecord(LivingEntity entity) {
			if (this.recordedEntities.add(entity.getUniqueID())) {
				this.recordedCount.addTo(entity.getType(), 1);
				this.totalCount += 1;
				return true;
			}
			return false;
		}

		int getRecordedCount(EntityType<?> type) {
			return this.recordedCount.getInt(type);
		}
	}
}
