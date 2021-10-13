package com.lovetropics.minigames.common.content.mangroves_and_pianguas.behavior;

import com.lovetropics.lib.codec.MoreCodecs;
import com.lovetropics.minigames.common.content.MinigameTexts;
import com.lovetropics.minigames.common.content.mangroves_and_pianguas.behavior.event.MpEvents;
import com.lovetropics.minigames.common.content.mangroves_and_pianguas.plot.Plot;
import com.lovetropics.minigames.common.core.game.IGamePhase;
import com.lovetropics.minigames.common.core.game.behavior.IGameBehavior;
import com.lovetropics.minigames.common.core.game.behavior.event.EventRegistrar;
import com.lovetropics.minigames.common.core.game.behavior.event.GamePlayerEvents;
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
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.server.ServerWorld;

import java.util.List;

public final class MpMerchantBehavior implements IGameBehavior {
	public static final Codec<MpMerchantBehavior> CODEC = RecordCodecBuilder.create(instance -> {
		return instance.group(
				Registry.ENTITY_TYPE.fieldOf("entity").forGetter(c -> c.entity),
				Offer.CODEC.listOf().fieldOf("offers").forGetter(c -> c.offers)
		).apply(instance, MpMerchantBehavior::new);
	});

	private final EntityType<?> entity;
	private final List<Offer> offers;
	private final MerchantOffers builtOffers;

	private IGamePhase game;

	public MpMerchantBehavior(EntityType<?> entity, List<Offer> offers) {
		this.entity = entity;
		this.offers = offers;

		this.builtOffers = new MerchantOffers();
		for (Offer offer : this.offers) {
			this.builtOffers.add(offer.build());
		}
	}

	@Override
	public void register(IGamePhase game, EventRegistrar events) {
		this.game = game;

		events.listen(MpEvents.ASSIGN_PLOT, this::onAssignPlot);
		events.listen(GamePlayerEvents.INTERACT_ENTITY, this::interactWithEntity);
	}

	private void onAssignPlot(ServerPlayerEntity player, Plot plot) {
		ServerWorld world = game.getWorld();

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

	private void interactWithEntity(ServerPlayerEntity player, Entity target, Hand hand) {
		if (this.entity == target.getType()) {
			MpMerchant merchant = new MpMerchant(player, this.builtOffers);
			merchant.openMerchantContainer(player, MinigameTexts.mpTrading(), 1);
		}
	}

	public static final class Offer {
		public static final Codec<Offer> CODEC = RecordCodecBuilder.create(instance -> {
			return instance.group(
					MoreCodecs.ITEM_STACK.fieldOf("input").forGetter(c -> c.input),
					MoreCodecs.ITEM_STACK.fieldOf("output").forGetter(c -> c.output)
			).apply(instance, Offer::new);
		});

		final ItemStack input;
		final ItemStack output;

		public Offer(ItemStack input, ItemStack output) {
			this.input = input;
			this.output = output;
		}

		public MerchantOffer build() {
			return new MerchantOffer(
					this.input, this.output,
					Integer.MAX_VALUE,
					0,
					0
			);
		}
	}
}
