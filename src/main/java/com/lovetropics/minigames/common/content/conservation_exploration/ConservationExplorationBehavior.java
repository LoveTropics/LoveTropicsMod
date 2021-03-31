package com.lovetropics.minigames.common.content.conservation_exploration;

import com.lovetropics.lib.entity.FireworkPalette;
import com.lovetropics.minigames.common.core.game.*;
import com.lovetropics.minigames.common.core.game.behavior.IGameBehavior;
import com.lovetropics.minigames.common.core.game.behavior.event.EventRegistrar;
import com.lovetropics.minigames.common.core.game.behavior.event.GameLifecycleEvents;
import com.lovetropics.minigames.common.core.game.behavior.event.GamePlayerEvents;
import com.lovetropics.minigames.common.core.game.util.GameBossBar;
import com.lovetropics.minigames.common.core.map.MapRegion;
import com.lovetropics.minigames.common.util.Scheduler;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.entity.*;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.scoreboard.ServerScoreboard;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.text.*;
import net.minecraft.util.text.event.ClickEvent;
import net.minecraft.util.text.event.HoverEvent;
import net.minecraft.world.BossInfo;
import net.minecraft.world.chunk.IChunk;
import net.minecraft.world.server.ServerWorld;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public final class ConservationExplorationBehavior implements IGameBehavior {
	public static final Codec<ConservationExplorationBehavior> CODEC = RecordCodecBuilder.create(instance -> {
		return instance.group(
				CreatureType.CODEC.listOf().fieldOf("creatures").forGetter(c -> c.creatures)
		).apply(instance, ConservationExplorationBehavior::new);
	});

	private final List<CreatureType> creatures;
	private CreatureType[] searchOrder;
	private int searchIndex = -1;

	private boolean creatureDiscovered;

	private ScorePlayerTeam discoveryTeam;

	private GameBossBar progressBar;

	public ConservationExplorationBehavior(List<CreatureType> creatures) {
		this.creatures = creatures;
	}

	@Override
	public void register(IGameInstance game, EventRegistrar events) throws GameException {
		events.listen(GameLifecycleEvents.START, this::onStart);
		events.listen(GameLifecycleEvents.FINISH, this::onFinish);
		
		events.listen(GamePlayerEvents.JOIN, this::onPlayerJoin);
		events.listen(GamePlayerEvents.INTERACT_ENTITY, this::onPlayerInteractEntity);

		this.progressBar = GameBossBar.openGlobal(game,
				new StringTextComponent("Conservation Exploration"),
				BossInfo.Color.GREEN,
				BossInfo.Overlay.PROGRESS
		);;
	}

	private void onStart(IGameInstance game) {
		List<CreatureType> searchOrder = new ArrayList<>(creatures);
		Collections.shuffle(searchOrder);
		this.searchOrder = searchOrder.toArray(new CreatureType[0]);

		nextCreature(game);

		ServerScoreboard scoreboard = game.getServer().getScoreboard();
		discoveryTeam = scoreboard.createTeam("first_discovery");
		discoveryTeam.setColor(TextFormatting.GREEN);

		game.addControlCommand("next_creature", ControlCommand.forInitiator(source -> {
			Scheduler.INSTANCE.submit(server -> {
				if (!nextCreature(game)) {
					SingleGameManager.INSTANCE.finish(game);
				}
			});
		}));
	}

	private void onPlayerJoin(IGameInstance game, ServerPlayerEntity player, PlayerRole role) {
		player.addItemStackToInventory(new ItemStack(ConservationExploration.RECORD_CREATURE.get()));
	}

	private void onPlayerInteractEntity(IGameInstance game, ServerPlayerEntity player, Entity entity, Hand hand) {
		ItemStack heldItem = player.getHeldItem(hand);
		if (!heldItem.isEmpty() && heldItem.getItem() == ConservationExploration.RECORD_CREATURE.get()) {
			if (entity instanceof LivingEntity) {
				this.recordEntity(game, player, (LivingEntity) entity);
			}
		}
	}

	private void recordEntity(IGameInstance minigame, ServerPlayerEntity reporter, LivingEntity entity) {
		if (!creatureDiscovered && entity.getType() == searchOrder[searchIndex].entity) {
			onDiscoverNewEntity(minigame, reporter, entity);
			creatureDiscovered = true;
		}
	}

	private void onDiscoverNewEntity(IGameInstance minigame, ServerPlayerEntity reporter, LivingEntity entity) {
		Vector3d position = reporter.getPositionVec();
		float yaw = reporter.rotationYaw;
		float pitch = reporter.rotationPitch;

		// highlight and stop the reported entity from moving for a bit
		entity.addPotionEffect(new EffectInstance(Effects.SLOWNESS, 20 * 20, 10, false, false));

		minigame.getServer().getScoreboard().addPlayerToTeam(entity.getScoreboardName(), discoveryTeam);

		FireworkPalette.ISLAND_ROYALE.spawn(entity.getPosition(), entity.world);

		String teleportCommand = "teleport_" + entity.getType().getTranslationKey();
		minigame.addControlCommand(teleportCommand, ControlCommand.forEveryone(source -> {
			ServerPlayerEntity player = source.asPlayer();
			player.teleport(minigame.getWorld(), position.x, position.y, position.z, yaw, pitch);
		}));

		IFormattableTextComponent message = reporter.getDisplayName().deepCopy()
				.appendString(" discovered a ")
				.appendSibling(entity.getType().getName().deepCopy().mergeStyle(TextFormatting.BOLD))
				.appendString("! ")
				.mergeStyle(TextFormatting.GOLD);

		ITextComponent teleportLink = new StringTextComponent("Click here to teleport")
				.mergeStyle(TextFormatting.BLUE, TextFormatting.UNDERLINE)
				.modifyStyle(style -> {
					return style
							.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new StringTextComponent("Teleport to this entity")))
							.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/minigame " + teleportCommand));
				});

		minigame.getAllPlayers().sendMessage(message.appendSibling(teleportLink));
		minigame.getAllPlayers().sendMessage(new StringTextComponent("How is this creature being impacted by human activities?").mergeStyle(TextFormatting.GRAY, TextFormatting.ITALIC));

		ITextComponent nextLink = new StringTextComponent("click here")
				.mergeStyle(TextFormatting.BLUE, TextFormatting.UNDERLINE)
				.modifyStyle(style -> {
					return style
							.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new StringTextComponent("Move on to the next creature")))
							.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/minigame next_creature"));
				});

		ITextComponent nextMessage = new StringTextComponent("Host: Once we are finished viewing and discussing this entity, ")
				.appendSibling(nextLink)
				.appendString(" to move onto the next creature")
				.mergeStyle(TextFormatting.AQUA, TextFormatting.ITALIC);

		ServerPlayerEntity initiator = minigame.getAllPlayers().getPlayerBy(minigame.getInitiator());
		if (initiator != null) {
			initiator.sendStatusMessage(nextMessage, false);
		}
	}

	private boolean nextCreature(IGameInstance minigame) {
		int searchIndex = ++this.searchIndex;
		if (searchIndex >= searchOrder.length) {
			return false;
		}

		creatureDiscovered = false;

		CreatureType creature = searchOrder[searchIndex];
		ITextComponent creatureName = creature.getName().deepCopy().mergeStyle(TextFormatting.AQUA);

		progressBar.setTitle(new StringTextComponent("Looking for: ").appendSibling(creatureName));
		progressBar.setProgress((float) searchIndex / searchOrder.length);

		minigame.getAllPlayers().sendMessage(new StringTextComponent("We are looking for a ").appendSibling(creatureName).appendString("!").mergeStyle(TextFormatting.GOLD));

		spawnCreaturesFor(minigame, creature);

		return true;
	}

	private void onFinish(IGameInstance game) {
		ServerScoreboard scoreboard = game.getServer().getScoreboard();
		scoreboard.removeTeam(discoveryTeam);
	}

	private void spawnCreaturesFor(IGameInstance minigame, CreatureType creature) {
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
		static final Codec<CreatureType> CODEC = RecordCodecBuilder.create(instance -> {
			return instance.group(
					Codec.STRING.fieldOf("region").forGetter(c -> c.region),
					Registry.ENTITY_TYPE.fieldOf("entity").forGetter(c -> c.entity),
					Codec.INT.fieldOf("count").forGetter(c -> c.count)
			).apply(instance, CreatureType::new);
		});

		final String region;
		final EntityType<?> entity;
		final int count;

		CreatureType(String region, EntityType<?> entity, int count) {
			this.region = region;
			this.entity = entity;
			this.count = count;
		}

		ITextComponent getName() {
			return new TranslationTextComponent(entity.getTranslationKey());
		}
	}
}
