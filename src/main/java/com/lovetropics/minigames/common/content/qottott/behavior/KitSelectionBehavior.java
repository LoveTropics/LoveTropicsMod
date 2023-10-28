package com.lovetropics.minigames.common.content.qottott.behavior;

import com.lovetropics.lib.BlockBox;
import com.lovetropics.minigames.common.core.diguise.DisguiseType;
import com.lovetropics.minigames.common.core.game.IGamePhase;
import com.lovetropics.minigames.common.core.game.behavior.IGameBehavior;
import com.lovetropics.minigames.common.core.game.behavior.action.GameActionContext;
import com.lovetropics.minigames.common.core.game.behavior.action.GameActionList;
import com.lovetropics.minigames.common.core.game.behavior.event.EventRegistrar;
import com.lovetropics.minigames.common.core.game.behavior.event.GamePhaseEvents;
import com.lovetropics.minigames.common.core.game.behavior.event.GamePlayerEvents;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.slf4j.Logger;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public record KitSelectionBehavior(List<Kit> kits) implements IGameBehavior {
	private static final Logger LOGGER = LogUtils.getLogger();

	public static final MapCodec<KitSelectionBehavior> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
			Kit.CODEC.listOf().fieldOf("kits").forGetter(KitSelectionBehavior::kits)
	).apply(i, KitSelectionBehavior::new));

	@Override
	public void register(final IGamePhase game, final EventRegistrar events) {
		for (final Kit kit : kits) {
			kit.apply.register(game, events);
		}

		final Map<UUID, Kit> kitEntities = new Object2ObjectOpenHashMap<>();

		events.listen(GamePhaseEvents.START, () -> {
			for (final Kit kit : kits) {
				final BlockBox region = game.getMapRegions().getAny(kit.region());
				if (region == null) {
					LOGGER.error("Missing region for kit: {}", kit);
					continue;
				}
				final Entity entity = kit.entity().createEntity(game.getLevel());
				if (entity == null) {
					LOGGER.error("Unable to create entity for kit: {}", kit);
					continue;
				}
				final Vec3 center = region.center();
				entity.moveTo(center.x, region.min().getY(), center.z, kit.angle, 0.0f);
				game.getLevel().addFreshEntity(entity);
				kitEntities.put(entity.getUUID(), kit);
			}
		});

		events.listen(GamePlayerEvents.INTERACT_ENTITY, (player, target, hand) -> {
			final Kit kit = kitEntities.get(target.getUUID());
			if (kit != null) {
				kit.apply.apply(game, GameActionContext.EMPTY, player);
				return InteractionResult.CONSUME;
			}
			return InteractionResult.PASS;
		});
	}

	private record Kit(String region, float angle, DisguiseType.EntityConfig entity, GameActionList<ServerPlayer> apply) {
		public static final Codec<Kit> CODEC = RecordCodecBuilder.create(i -> i.group(
				Codec.STRING.fieldOf("region").forGetter(Kit::region),
				Codec.FLOAT.optionalFieldOf("angle", 0.0f).forGetter(Kit::angle),
				DisguiseType.EntityConfig.CODEC.fieldOf("entity").forGetter(Kit::entity),
				GameActionList.PLAYER_CODEC.fieldOf("apply").forGetter(Kit::apply)
		).apply(i, Kit::new));
	}
}
