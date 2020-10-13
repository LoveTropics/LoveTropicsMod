package com.lovetropics.minigames.common.minigames.behaviours.instances.collaborative_conservation;

import com.lovetropics.lib.entity.FireworkUtil;
import com.lovetropics.minigames.common.item.MinigameItems;
import com.lovetropics.minigames.common.minigames.IMinigameInstance;
import com.lovetropics.minigames.common.minigames.MinigameManager;
import com.lovetropics.minigames.common.minigames.PlayerRole;
import com.lovetropics.minigames.common.minigames.PlayerSet;
import com.lovetropics.minigames.common.minigames.behaviours.IMinigameBehavior;
import com.mojang.datafixers.Dynamic;
import it.unimi.dsi.fastutil.objects.*;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
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

import java.util.Map;
import java.util.Set;
import java.util.UUID;

public final class RecordCreaturesBehavior implements IMinigameBehavior {
	private static final long CLOSE_TIME = 20 * 10;

	private final Map<UUID, PlayerData> playerData = new Object2ObjectOpenHashMap<>();
	private final Set<EntityType<?>> discoveredEntities = new ReferenceOpenHashSet<>();

	private final RecordList globalRecords = new RecordList();

	private final long time;
	private final long closeTime;

	private final ServerBossInfo timerBar = new ServerBossInfo(
			new StringTextComponent("Time Remaining"),
			BossInfo.Color.GREEN,
			BossInfo.Overlay.PROGRESS
	);

	private boolean closing;

	RecordCreaturesBehavior(long time) {
		this.time = time;
		this.closeTime = time + CLOSE_TIME;
	}

	public static <T> RecordCreaturesBehavior parse(Dynamic<T> root) {
		long time = root.get("time").asLong(20 * 60 * 10);
		return new RecordCreaturesBehavior(time);
	}

	@Override
	public void onStart(IMinigameInstance minigame) {
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

		this.timerBar.addPlayer(player);
	}

	@Override
	public void onPlayerLeave(IMinigameInstance minigame, ServerPlayerEntity player) {
		this.timerBar.removePlayer(player);
	}

	@Override
	public void worldUpdate(IMinigameInstance minigame, World world) {
		long ticks = minigame.ticks();

		if (ticks < this.time) {
			if (ticks % 20 == 0) {
				long ticksRemaining = this.time - ticks;
				this.timerBar.setName(this.getTimeRemainingText(ticksRemaining));
				this.timerBar.setPercent((float) ticksRemaining / this.time);
			}
		} else if (!this.closing) {
			this.closing = true;
			this.displayReport(minigame);
		}

		if (this.closing && ticks >= this.closeTime) {
			MinigameManager.getInstance().finish();
		}
	}

	private ITextComponent getTimeRemainingText(long ticksRemaining) {
		long secondsRemaining = ticksRemaining / 20;

		long minutes = secondsRemaining / 60;
		long seconds = secondsRemaining % 60;
		String time = String.format("%02d:%02d", minutes, seconds);

		return new StringTextComponent("Time Remaining: " + time + "...");
	}

	private void displayReport(IMinigameInstance minigame) {
		PlayerSet players = minigame.getPlayers();

		players.sendMessage(new StringTextComponent("Time is up! Here's the statistics for this round:").applyTextStyle(TextFormatting.GREEN));

		for (Object2IntMap.Entry<EntityType<?>> entry : this.globalRecords.recordedCount.object2IntEntrySet()) {
			EntityType<?> entityType = entry.getKey();
			int count = entry.getIntValue();

			players.sendMessage(
					new StringTextComponent(" - ")
							.appendSibling(new TranslationTextComponent(entityType.getTranslationKey()).applyTextStyle(TextFormatting.BLUE))
							.appendText(": " + count)
							.applyTextStyle(TextFormatting.GRAY)
			);
		}
	}

	@Override
	public void onPlayerInteractEntity(IMinigameInstance minigame, ServerPlayerEntity player, Entity entity, Hand hand) {
		if (this.closing) {
			return;
		}

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

	private void recordEntity(IMinigameInstance minigame, ServerPlayerEntity player, LivingEntity entity) {
		PlayerData data = this.getDataFor(player);

		if (data.records.tryRecord(entity)) {
			this.onRecordEntity(minigame, player, entity);

			int recordedCount = data.records.getRecordedCount(entity.getType());
			ITextComponent entityName = new TranslationTextComponent(entity.getType().getTranslationKey());

			player.sendMessage(
					new StringTextComponent("You have recorded " + recordedCount + " of ")
							.appendSibling(entityName.applyTextStyle(TextFormatting.BOLD))
							.appendText("!")
							.applyTextStyle(TextFormatting.GREEN)
			);
		} else {
			player.sendMessage(new StringTextComponent("You've already recorded this entity!").applyTextStyle(TextFormatting.RED));
		}
	}

	private PlayerData getDataFor(PlayerEntity player) {
		return this.playerData.computeIfAbsent(player.getUniqueID(), uuid -> new PlayerData());
	}

	private void onRecordEntity(IMinigameInstance minigame, ServerPlayerEntity reporter, LivingEntity entity) {
		this.globalRecords.tryRecord(entity);
		if (this.discoveredEntities.add(entity.getType())) {
			this.onDiscoverNewEntity(minigame, reporter, entity);
		}
	}

	private void onDiscoverNewEntity(IMinigameInstance minigame, ServerPlayerEntity reporter, LivingEntity entity) {
		String teleportCommand = "teleport_" + entity.getType().getTranslationKey();

		Vec3d position = reporter.getPositionVec();
		float yaw = reporter.rotationYaw;
		float pitch = reporter.rotationPitch;

		// highlight and stop the reported entity from moving for a bit
		entity.addPotionEffect(new EffectInstance(Effects.SLOWNESS, 20 * 20, 10, false, false));
		entity.addPotionEffect(new EffectInstance(Effects.GLOWING, 20 * 20, 10, false, false));

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
		this.timerBar.setVisible(false);
		this.timerBar.removeAllPlayers();
	}

	static class PlayerData {
		final RecordList records = new RecordList();
		Vec3d lastPosition;
	}

	static class RecordList {
		final Object2IntOpenHashMap<EntityType<?>> recordedCount = new Object2IntOpenHashMap<>();
		final Set<UUID> recordedEntities = new ObjectOpenHashSet<>();

		boolean tryRecord(LivingEntity entity) {
			if (this.recordedEntities.add(entity.getUniqueID())) {
				this.recordedCount.addTo(entity.getType(), 1);
				return true;
			}
			return false;
		}

		int getRecordedCount(EntityType<?> type) {
			return this.recordedCount.getInt(type);
		}
	}
}
