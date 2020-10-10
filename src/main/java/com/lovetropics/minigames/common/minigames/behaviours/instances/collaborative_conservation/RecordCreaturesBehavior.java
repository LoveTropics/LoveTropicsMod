package com.lovetropics.minigames.common.minigames.behaviours.instances.collaborative_conservation;

import com.lovetropics.lib.entity.FireworkUtil;
import com.lovetropics.minigames.common.item.MinigameItems;
import com.lovetropics.minigames.common.minigames.IMinigameInstance;
import com.lovetropics.minigames.common.minigames.PlayerRole;
import com.lovetropics.minigames.common.minigames.behaviours.IMinigameBehavior;
import com.mojang.datafixers.Dynamic;
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
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.util.text.event.ClickEvent;
import net.minecraft.util.text.event.HoverEvent;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

public final class RecordCreaturesBehavior implements IMinigameBehavior {
	private final Map<UUID, PlayerData> playerData = new Object2ObjectOpenHashMap<>();
	private final Set<EntityType<?>> discoveredEntities = new ReferenceOpenHashSet<>();

	private final RecordList globalRecords = new RecordList();

	public static <T> RecordCreaturesBehavior parse(Dynamic<T> root) {
		return new RecordCreaturesBehavior();
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
		entity.addPotionEffect(new EffectInstance(Effects.SLOWNESS, 20 * 10, 10, false, false));
		entity.addPotionEffect(new EffectInstance(Effects.GLOWING, 20 * 10, 10, false, false));

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
