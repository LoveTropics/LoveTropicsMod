package com.lovetropics.minigames.common.content.biodiversity_blitz.behavior;

import com.lovetropics.lib.BlockBox;
import com.lovetropics.lib.codec.MoreCodecs;
import com.lovetropics.minigames.common.content.biodiversity_blitz.BiodiversityBlitzTexts;
import com.lovetropics.minigames.common.content.biodiversity_blitz.behavior.event.BbEvents;
import com.lovetropics.minigames.common.content.biodiversity_blitz.merchant.BbMerchant;
import com.lovetropics.minigames.common.content.biodiversity_blitz.plot.Plot;
import com.lovetropics.minigames.common.content.biodiversity_blitz.plot.plant.PlantItemType;
import com.lovetropics.minigames.common.core.game.IGamePhase;
import com.lovetropics.minigames.common.core.game.behavior.IGameBehavior;
import com.lovetropics.minigames.common.core.game.behavior.event.EventRegistrar;
import com.lovetropics.minigames.common.core.game.behavior.event.GamePlayerEvents;
import com.lovetropics.minigames.common.util.Util;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;
import net.minecraft.world.InteractionResult;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerLevel;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;

public final class BbMerchantBehavior implements IGameBehavior {
	public static final Codec<BbMerchantBehavior> CODEC = RecordCodecBuilder.create(instance -> {
		return instance.group(
				Codec.STRING.fieldOf("plot_region").forGetter(c -> c.plotRegion),
				Registry.ENTITY_TYPE.fieldOf("entity").forGetter(c -> c.entity),
				MoreCodecs.TEXT.optionalFieldOf("name", TextComponent.EMPTY).forGetter(c -> c.name),
				Offer.CODEC.listOf().fieldOf("offers").forGetter(c -> c.offers)
		).apply(instance, BbMerchantBehavior::new);
	});

	private final String plotRegion;
	private final EntityType<?> entity;
	private final Component name;
	private final List<Offer> offers;

	private final Set<UUID> merchants = new ObjectOpenHashSet<>();

	private IGamePhase game;

	public BbMerchantBehavior(String plotRegion, EntityType<?> entity, Component name, List<Offer> offers) {
		this.plotRegion = plotRegion;
		this.entity = entity;
		this.name = name;
		this.offers = offers;
	}

	@Override
	public void register(IGamePhase game, EventRegistrar events) {
		this.game = game;

		events.listen(BbEvents.ASSIGN_PLOT, this::onAssignPlot);
		events.listen(GamePlayerEvents.INTERACT_ENTITY, this::interactWithEntity);
	}

	private void onAssignPlot(ServerPlayer player, Plot plot) {
		ServerLevel world = this.game.getWorld();

		BlockBox region = plot.regionByName(this.plotRegion);
		if (region == null) return;

		Vec3 center = region.getCenter();

		Entity merchant = this.createMerchant(world);
		if (merchant == null) return;

		Direction direction = Util.getDirectionBetween(region, plot.spawn);
		float yaw = direction.toYRot();

		merchant.moveTo(center.x(), center.y() - 0.5, center.z(), yaw, 0);
		merchant.setYHeadRot(yaw);

		world.getChunk(region.getCenterBlock());
		world.addFreshEntity(merchant);

		if (merchant instanceof Mob) {
			Mob mob = (Mob) merchant;
			mob.finalizeSpawn(world, world.getCurrentDifficultyAt(new BlockPos(center)), MobSpawnType.MOB_SUMMONED, null, null);
			mob.setNoAi(true);
			mob.setBaby(false);
			mob.setInvulnerable(true);
		}

		merchants.add(merchant.getUUID());
	}

	@Nullable
	private Entity createMerchant(ServerLevel world) {
		Entity merchant = this.entity.create(world);
		if (merchant != null) {
			if (this.name != TextComponent.EMPTY) {
				merchant.setCustomName(this.name);
				merchant.setCustomNameVisible(true);
			}

			return merchant;
		}

		return null;
	}

	private InteractionResult interactWithEntity(ServerPlayer player, Entity target, InteractionHand hand) {
		if (this.merchants.contains(target.getUUID())) {
			MerchantOffers builtOffers = new MerchantOffers();
			for (Offer offer : this.offers) {
				builtOffers.add(offer.build(this.game));
			}

			BbMerchant merchant = new BbMerchant(player, builtOffers);
			merchant.openTradingScreen(player, BiodiversityBlitzTexts.trading(), 1);

			return InteractionResult.SUCCESS;
		}

		return InteractionResult.PASS;
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
				return game.invoker(BbEvents.CREATE_PLANT_ITEM).createPlantItem(this.plant);
			}
		}
	}
}
