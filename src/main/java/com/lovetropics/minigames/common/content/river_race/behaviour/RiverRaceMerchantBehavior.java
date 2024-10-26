package com.lovetropics.minigames.common.content.river_race.behaviour;

import com.lovetropics.lib.BlockBox;
import com.lovetropics.lib.codec.MoreCodecs;
import com.lovetropics.minigames.common.content.biodiversity_blitz.BiodiversityBlitzTexts;
import com.lovetropics.minigames.common.content.biodiversity_blitz.merchant.BbMerchant;
import com.lovetropics.minigames.common.core.game.GameException;
import com.lovetropics.minigames.common.core.game.IGamePhase;
import com.lovetropics.minigames.common.core.game.behavior.IGameBehavior;
import com.lovetropics.minigames.common.core.game.behavior.event.EventRegistrar;
import com.lovetropics.minigames.common.core.game.behavior.event.GamePhaseEvents;
import com.lovetropics.minigames.common.core.game.behavior.event.GamePlayerEvents;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.ItemCost;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Set;
import java.util.UUID;

// TODO Merge with BbMerchantBehavior?
public class RiverRaceMerchantBehavior implements IGameBehavior {

    public static final MapCodec<RiverRaceMerchantBehavior> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
            Codec.STRING.fieldOf("zone").forGetter(c -> c.zone),
            BuiltInRegistries.ENTITY_TYPE.byNameCodec().fieldOf("entity").forGetter(c -> c.entity),
            ComponentSerialization.CODEC.optionalFieldOf("name", CommonComponents.EMPTY).forGetter(c -> c.name),
            Offer.CODEC.listOf().fieldOf("offers").forGetter(c -> c.offers)
    ).apply(i, RiverRaceMerchantBehavior::new));

    private final String zone;
    private final EntityType<?> entity;
    private final Component name;
    private final List<Offer> offers;

    private final Set<UUID> merchants = new ObjectOpenHashSet<>();

    private IGamePhase game;

    public RiverRaceMerchantBehavior(String zone, EntityType<?> entity, Component name, List<Offer> offers) {
        this.zone = zone;
        this.entity = entity;
        this.name = name;
        this.offers = offers;
    }

    @Override
    public void register(IGamePhase game, EventRegistrar events) throws GameException {
        this.game = game;

        events.listen(GamePhaseEvents.CREATE, this::onGameStarted);
        events.listen(GamePlayerEvents.INTERACT_ENTITY, this::interactWithEntity);
    }

    /**
     * When the game loads, load this merchant into its proper section
     */
    private void onGameStarted() {
        ServerLevel world = game.level();

        BlockBox region = game.mapRegions().getOrThrow(zone);
       // if (region == null) return;

        Vec3 center = region.center();

        Entity merchant = createMerchant(world);
        if (merchant == null) return;

//        Direction direction = Util.getDirectionBetween(region, plot.spawn);
//        float yaw = direction.toYRot();
//
        merchant.moveTo(center.x(), center.y() - 0.5, center.z(), 0 /*yaw*/, 0);
//        merchant.setYHeadRot(yaw);

        world.getChunk(region.centerBlock());
        world.addFreshEntity(merchant);

        if (merchant instanceof Mob mob) {
            mob.finalizeSpawn(world, world.getCurrentDifficultyAt(BlockPos.containing(center)), MobSpawnType.MOB_SUMMONED, null);
            mob.setNoAi(true);
            mob.setBaby(false);
            mob.setInvulnerable(true);
        }

        merchants.add(merchant.getUUID());
    }

    private InteractionResult interactWithEntity(ServerPlayer player, Entity target, InteractionHand hand) {
        if (merchants.contains(target.getUUID())) {
            MerchantOffers builtOffers = new MerchantOffers();
            for (Offer offer : offers) {
                builtOffers.add(offer.build(game));
            }

            // TODO need a different screen?
            BbMerchant merchant = new BbMerchant(player, builtOffers);
            merchant.openTradingScreen(player, BiodiversityBlitzTexts.TRADING, 1);

            return InteractionResult.SUCCESS;
        }

        return InteractionResult.PASS;
    }

    @Nullable
    private Entity createMerchant(ServerLevel world) {
        Entity merchant = entity.create(world);
        if (merchant != null) {
            if (name != CommonComponents.EMPTY) {
                merchant.setCustomName(name);
                merchant.setCustomNameVisible(true);
            }

            merchant.setSilent(true);

            return merchant;
        }

        return null;
    }

    public static final class Offer {
        public static final Codec<Offer> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                ItemCost.CODEC.fieldOf("input").forGetter(c -> c.input),
                Output.CODEC.fieldOf("output").forGetter(c -> c.output)
        ).apply(instance, Offer::new));

        private final ItemCost input;
        private final Output output;

        public Offer(ItemCost input, Output output) {
            this.input = input;
            this.output = output;
        }

        public MerchantOffer build(IGamePhase game) {
            return new MerchantOffer(
                    input, output.build(game),
                    Integer.MAX_VALUE,
                    0,
                    0
            );
        }
    }

    public static final class Output {
        private static final Codec<Output> CODEC = MoreCodecs.ITEM_STACK.xmap(Output::item, output -> output.item);

        @Nullable
        private final ItemStack item;

        private Output(@Nullable ItemStack item) {
            this.item = item;
        }

        private static Output item(ItemStack item) {
            return new Output(item);
        }

        private ItemStack build(IGamePhase game) {
            if (item != null) {
                return item;
            }
            throw new IllegalStateException();
        }
    }
}
