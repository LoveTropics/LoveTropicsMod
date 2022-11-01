package com.lovetropics.minigames.common.core.game.behavior.instances.action;

import com.lovetropics.minigames.common.core.game.GameException;
import com.lovetropics.minigames.common.core.game.IGamePhase;
import com.lovetropics.minigames.common.core.game.behavior.IGameBehavior;
import com.lovetropics.minigames.common.core.game.behavior.event.EventRegistrar;
import com.lovetropics.minigames.common.core.game.behavior.event.GameActionEvents;
import com.lovetropics.minigames.common.util.Util;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraftforge.fml.InterModComms;

public record TransformPlayerTornadoAction(int timeTicks) implements IGameBehavior {
	public static final Codec<TransformPlayerTornadoAction> CODEC = RecordCodecBuilder.create(i -> i.group(
			Codec.INT.fieldOf("time_ticks").forGetter(c -> c.timeTicks)
	).apply(i, TransformPlayerTornadoAction::new));

	@Override
	public void register(IGamePhase game, EventRegistrar events) throws GameException {
		events.listen(GameActionEvents.APPLY_TO_PLAYER, (context, player) -> transformPlayer(player));
	}

	private boolean transformPlayer(final ServerPlayer player) {

		InterModComms.sendTo("weather2", "player_tornado", () -> {
			CompoundTag tag = new CompoundTag();
			tag.putString("uuid", player.getUUID().toString());
			tag.putInt("time_ticks", 800);
			tag.putString("dimension", player.getLevel().dimension().location().toString());
			return tag;
		});

		return true;
	}
}
