package com.lovetropics.minigames.common.content.mangroves_and_pianguas.behavior;

import com.lovetropics.lib.codec.MoreCodecs;
import com.lovetropics.minigames.common.content.MinigameTexts;
import com.lovetropics.minigames.common.content.mangroves_and_pianguas.behavior.event.MpEvents;
import com.lovetropics.minigames.common.content.mangroves_and_pianguas.plot.Plot;
import com.lovetropics.minigames.common.content.mangroves_and_pianguas.plot.plant.PlantItemType;
import com.lovetropics.minigames.common.core.game.IGamePhase;
import com.lovetropics.minigames.common.core.game.behavior.IGameBehavior;
import com.lovetropics.minigames.common.core.game.behavior.event.EventRegistrar;
import com.lovetropics.minigames.common.core.game.behavior.event.GamePlayerEvents;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.MerchantOffer;
import net.minecraft.item.MerchantOffers;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.server.ServerWorld;

import java.util.List;
import java.util.function.Function;

public final class MpMerchantBehavior implements IGameBehavior {
	public static final Codec<MpMerchantBehavior> CODEC = RecordCodecBuilder.create(instance -> {
		return instance.group(
				Registry.ENTITY_TYPE.fieldOf("entity").forGetter(c -> c.entity),
				Offer.CODEC.listOf().fieldOf("offers").forGetter(c -> c.offers)
		).apply(instance, MpMerchantBehavior::new);
	});

	private final EntityType<?> entity;
	private final List<Offer> offers;

	private IGamePhase game;

	public MpMerchantBehavior(EntityType<?> entity, List<Offer> offers) {
		this.entity = entity;
		this.offers = offers;
	}

	@Override
	public void register(IGamePhase game, EventRegistrar events) {
		this.game = game;

		events.listen(MpEvents.ASSIGN_PLOT, this::onAssignPlot);
		events.listen(GamePlayerEvents.INTERACT_ENTITY, this::interactWithEntity);
	}

	private void onAssignPlot(ServerPlayerEntity player, Plot plot) {
		ServerWorld world = this.game.getWorld();

		Vector3d center = plot.shop.getCenter();

		Entity merchant = this.entity.create(world);
		if (merchant == null) {
			return;
		}

		merchant.setLocationAndAngles(center.getX(), center.getY() - 0.5, center.getZ(), 0, 0);

		world.addEntity(merchant);

		if (merchant instanceof MobEntity) {
			MobEntity mob = (MobEntity) merchant;
			mob.onInitialSpawn(world, world.getDifficultyForLocation(new BlockPos(center)), SpawnReason.MOB_SUMMONED, null, null);
			mob.setNoAI(true);
		}
	}

	private ActionResultType interactWithEntity(ServerPlayerEntity player, Entity target, Hand hand) {
		if (this.entity == target.getType()) {
			MerchantOffers builtOffers = new MerchantOffers();
			for (Offer offer : this.offers) {
				builtOffers.add(offer.build(this.game));
			}

			MpMerchant merchant = new MpMerchant(player, builtOffers);
			merchant.openMerchantContainer(player, MinigameTexts.mpTrading(), 1);

			return ActionResultType.SUCCESS;
		}

		return ActionResultType.PASS;
	}

	public static final class Offer {
		public static final Codec<Offer> CODEC = RecordCodecBuilder.create(instance -> instance.group(
				MoreCodecs.ITEM_STACK.fieldOf("input").forGetter(c -> c.input),
				Output.CODEC.fieldOf("output").forGetter(c -> c.output)
		).apply(instance, Offer::new));

		private final ItemStack input;
		private final Output output;

		public Offer(ItemStack input, Output output) {
			this.input = input;
			this.output = output;
		}

		public MerchantOffer build(IGamePhase game) {
			return new MerchantOffer(
					this.input, this.output.build(game),
					Integer.MAX_VALUE,
					0,
					0
			);
		}
	}

	public static final class Output {
		private static final Codec<Output> ITEM_CODEC = MoreCodecs.ITEM_STACK.xmap(Output::item, output -> output.item);
		private static final Codec<Output> PLANT_CODEC = RecordCodecBuilder.create(instance -> {
			return instance.group(
					PlantItemType.CODEC.fieldOf("plant").forGetter(c -> c.plant)
			).apply(instance, Output::plant);
		});

		public static final Codec<Output> CODEC = Codec.either(ITEM_CODEC, PLANT_CODEC)
				.xmap(
						either -> either.map(Function.identity(), Function.identity()),
						output -> output.item != null ? Either.left(output) : Either.right(output)
				);

		private final ItemStack item;
		private final PlantItemType plant;

		private Output(ItemStack item, PlantItemType plant) {
			this.item = item;
			this.plant = plant;
		}

		private static Output item(ItemStack item) {
			return new Output(item, null);
		}

		private static Output plant(PlantItemType plant) {
			return new Output(null, plant);
		}

		private ItemStack build(IGamePhase game) {
			if (this.item != null) {
				return this.item;
			} else {
				return game.invoker(MpEvents.CREATE_PLANT_ITEM).createPlantItem(this.plant);
			}
		}
	}
}
