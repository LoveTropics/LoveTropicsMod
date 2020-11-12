package com.lovetropics.minigames.common.minigames.behaviours.instances.conservation_exploration;

import com.lovetropics.lib.entity.FireworkUtil;
import com.lovetropics.minigames.common.Scheduler;
import com.lovetropics.minigames.common.item.MinigameItems;
import com.lovetropics.minigames.common.map.MapRegion;
import com.lovetropics.minigames.common.minigames.ControlCommand;
import com.lovetropics.minigames.common.minigames.IMinigameInstance;
import com.lovetropics.minigames.common.minigames.MinigameManager;
import com.lovetropics.minigames.common.minigames.PlayerRole;
import com.lovetropics.minigames.common.minigames.behaviours.IMinigameBehavior;
import com.mojang.datafixers.Dynamic;
import net.minecraft.entity.*;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.scoreboard.ServerScoreboard;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.util.text.event.ClickEvent;
import net.minecraft.util.text.event.HoverEvent;
import net.minecraft.world.BossInfo;
import net.minecraft.world.chunk.IChunk;
import net.minecraft.world.server.ServerBossInfo;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public final class ConservationExplorationBehavior implements IMinigameBehavior {
	private final List<CreatureType> creatures;
	private CreatureType[] searchOrder;
	private int searchIndex = -1;

	private boolean creatureDiscovered;

	private ScorePlayerTeam discoveryTeam;

	private final ServerBossInfo progressBar = new ServerBossInfo(
			new StringTextComponent("Conservation Exploration"),
			BossInfo.Color.GREEN,
			BossInfo.Overlay.PROGRESS
	);

	public ConservationExplorationBehavior(List<CreatureType> creatures) {
		this.creatures = creatures;
	}

	public static <T> ConservationExplorationBehavior parse(Dynamic<T> root) {
		List<CreatureType> creatures = root.get("creatures").asList(CreatureType::parse);
		return new ConservationExplorationBehavior(creatures);
	}

	@Override
	public void onStart(IMinigameInstance minigame) {
		List<CreatureType> searchOrder = new ArrayList<>(creatures);
		Collections.shuffle(searchOrder);
		this.searchOrder = searchOrder.toArray(new CreatureType[0]);

		nextCreature(minigame);

		ServerScoreboard scoreboard = minigame.getServer().getScoreboard();
		discoveryTeam = scoreboard.createTeam("first_discovery");
		discoveryTeam.setColor(TextFormatting.GREEN);

		minigame.addControlCommand("next_creature", ControlCommand.forInitiator(source -> {
			Scheduler.INSTANCE.submit(server -> {
				if (!nextCreature(minigame)) {
					MinigameManager.getInstance().finish();
				}
			});
		}));
	}

	@Override
	public void onPlayerJoin(IMinigameInstance minigame, ServerPlayerEntity player, PlayerRole role) {
		player.addItemStackToInventory(new ItemStack(MinigameItems.RECORD_CREATURE.get()));
		progressBar.addPlayer(player);
	}

	@Override
	public void onPlayerLeave(IMinigameInstance minigame, ServerPlayerEntity player) {
		progressBar.removePlayer(player);
	}

	@Override
	public void onPlayerInteractEntity(IMinigameInstance minigame, ServerPlayerEntity player, Entity entity, Hand hand) {
		ItemStack heldItem = player.getHeldItem(hand);
		if (!heldItem.isEmpty() && heldItem.getItem() == MinigameItems.RECORD_CREATURE.get()) {
			if (entity instanceof LivingEntity) {
				this.recordEntity(minigame, player, (LivingEntity) entity);
			}
		}
	}

	private void recordEntity(IMinigameInstance minigame, ServerPlayerEntity reporter, LivingEntity entity) {
		if (!creatureDiscovered && entity.getType() == searchOrder[searchIndex].entity) {
			onDiscoverNewEntity(minigame, reporter, entity);
			creatureDiscovered = true;
		}
	}

	private void onDiscoverNewEntity(IMinigameInstance minigame, ServerPlayerEntity reporter, LivingEntity entity) {
		Vec3d position = reporter.getPositionVec();
		float yaw = reporter.rotationYaw;
		float pitch = reporter.rotationPitch;

		// highlight and stop the reported entity from moving for a bit
		entity.addPotionEffect(new EffectInstance(Effects.SLOWNESS, 20 * 20, 10, false, false));

		minigame.getServer().getScoreboard().addPlayerToTeam(entity.getScoreboardName(), discoveryTeam);

		FireworkUtil.spawnFirework(new BlockPos(entity), entity.world, FireworkUtil.Palette.ISLAND_ROYALE.getPalette());

		String teleportCommand = "teleport_" + entity.getType().getTranslationKey();
		minigame.addControlCommand(teleportCommand, ControlCommand.forEveryone(source -> {
			ServerPlayerEntity player = source.asPlayer();
			player.teleport(minigame.getWorld(), position.x, position.y, position.z, yaw, pitch);
		}));

		ITextComponent message = reporter.getDisplayName()
				.appendText(" discovered a ")
				.appendSibling(entity.getType().getName().shallowCopy().applyTextStyle(TextFormatting.BOLD))
				.appendText("! ")
				.applyTextStyle(TextFormatting.GOLD);

		ITextComponent teleportLink = new StringTextComponent("Click here to teleport")
				.applyTextStyles(TextFormatting.BLUE, TextFormatting.UNDERLINE)
				.applyTextStyle(style -> {
					style.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new StringTextComponent("Teleport to this entity")));
					style.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/minigame " + teleportCommand));
				});

		minigame.getPlayers().sendMessage(message.appendSibling(teleportLink));
		minigame.getPlayers().sendMessage(new StringTextComponent("How is this creature being impacted by human activities?").applyTextStyles(TextFormatting.GRAY, TextFormatting.ITALIC));

		ITextComponent nextLink = new StringTextComponent("click here")
				.applyTextStyles(TextFormatting.BLUE, TextFormatting.UNDERLINE)
				.applyTextStyle(style -> {
					style.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new StringTextComponent("Move on to the next creature")));
					style.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/minigame next_creature"));
				});

		ITextComponent nextMessage = new StringTextComponent("Host: Once we are finished viewing and discussing this entity, ")
				.appendSibling(nextLink)
				.appendText(" to move onto the next creature")
				.applyTextStyles(TextFormatting.AQUA, TextFormatting.ITALIC);

		ServerPlayerEntity initiator = minigame.getPlayers().getPlayerBy(minigame.getInitiator());
		if (initiator != null) {
			initiator.sendMessage(nextMessage);
		}
	}

	private boolean nextCreature(IMinigameInstance minigame) {
		int searchIndex = ++this.searchIndex;
		if (searchIndex >= searchOrder.length) {
			return false;
		}

		creatureDiscovered = false;

		CreatureType creature = searchOrder[searchIndex];
		ITextComponent creatureName = creature.getName().applyTextStyle(TextFormatting.AQUA);

		progressBar.setName(new StringTextComponent("Looking for: ").appendSibling(creatureName));
		progressBar.setPercent((float) searchIndex / searchOrder.length);

		minigame.getPlayers().sendMessage(new StringTextComponent("We are looking for a ").appendSibling(creatureName).appendText("!").applyTextStyle(TextFormatting.GOLD));

		spawnCreaturesFor(minigame, creature);

		return true;
	}

	@Override
	public void onFinish(IMinigameInstance minigame) {
		this.progressBar.setVisible(false);
		this.progressBar.removeAllPlayers();

		ServerScoreboard scoreboard = minigame.getServer().getScoreboard();
		scoreboard.removeTeam(discoveryTeam);
	}

	private void spawnCreaturesFor(IMinigameInstance minigame, CreatureType creature) {
		ServerWorld world = minigame.getWorld();
		Random random = world.rand;

		String regionKey = creature.region;
		List<MapRegion> regions = new ArrayList<>(minigame.getMapRegions().get(regionKey));
		if (regions.isEmpty()) {
			return;
		}

		int groupCount = Math.max(regions.size() / 6, 1);

		for (int i = 0; i < groupCount; i++) {
			MapRegion region = regions.get(random.nextInt(regions.size()));

			EntityType<?> entityType = creature.entity;
			for (int j = 0; j < creature.count; j++) {
				BlockPos pos = region.sample(random);
				pos = findSurface(world, pos.getX(), region.min.getY(), pos.getZ());

				Entity entity = entityType.spawn(world, null, null, null, pos, SpawnReason.SPAWN_EGG, true, false);
				if (entity instanceof MobEntity) {
					((MobEntity) entity).enablePersistence();
					entity.setInvulnerable(true);
				}
			}
		}
	}

	private BlockPos findSurface(ServerWorld world, int x, int y, int z) {
		IChunk chunk = world.getChunk(x >> 4, z >> 4);

		BlockPos.Mutable mutablePos = new BlockPos.Mutable(x, y, z);
		while (!chunk.getBlockState(mutablePos).isAir()) {
			mutablePos.move(Direction.UP);
		}

		return mutablePos.toImmutable().up();
	}

	static class CreatureType {
		final String region;
		final EntityType<?> entity;
		final int count;

		CreatureType(String region, EntityType<?> entity, int count) {
			this.region = region;
			this.entity = entity;
			this.count = count;
		}

		static <T> CreatureType parse(Dynamic<T> root) {
			String region = root.get("region").asString("");
			EntityType<?> entityType = ForgeRegistries.ENTITIES.getValue(new ResourceLocation(root.get("entity").asString("")));
			int count = root.get("count").asInt(0);
			return new CreatureType(region, entityType, count);
		}

		ITextComponent getName() {
			return new TranslationTextComponent(entity.getTranslationKey());
		}
	}
}
