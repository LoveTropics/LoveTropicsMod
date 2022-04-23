package com.lovetropics.minigames.common.core.game.behavior.instances.tweak;

import com.lovetropics.minigames.common.core.game.IGamePhase;
import com.lovetropics.minigames.common.core.game.behavior.IGameBehavior;
import com.lovetropics.minigames.common.core.game.behavior.event.EventRegistrar;
import com.lovetropics.minigames.common.core.game.behavior.event.GamePhaseEvents;
import com.lovetropics.minigames.common.core.game.behavior.event.GamePlayerEvents;
import com.mojang.serialization.Codec;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.protocol.game.ClientboundGameEventPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.GameRules;

import java.util.Map;

public final class SetGameRulesBehavior implements IGameBehavior {
	public static final Codec<SetGameRulesBehavior> CODEC = RecordCodecBuilder.create(instance -> {
		return instance.group(
				Codec.unboundedMap(Codec.STRING, Codec.STRING).fieldOf("rules").forGetter(c -> c.rules)
		).apply(instance, SetGameRulesBehavior::new);
	});

	private final Map<String, String> rules;

	public SetGameRulesBehavior(Map<String, String> rules) {
		this.rules = rules;
	}

	@Override
	public void register(IGamePhase game, EventRegistrar events) {
		MinecraftServer server = game.getServer();
		GameRules gameRules = game.getWorld().getGameRules();
		CompoundTag rulesSnapshot = applyRules(gameRules);

		events.listen(GamePlayerEvents.ADD, player -> sendRuleUpdatesTo(gameRules, player));
		events.listen(GamePlayerEvents.REMOVE, player -> sendRuleUpdatesTo(server.getGameRules(), player));

		events.listen(GamePhaseEvents.DESTROY, () -> {
			resetRules(gameRules, rulesSnapshot);
		});
	}

	private CompoundTag applyRules(GameRules gameRules) {
		CompoundTag snapshot = gameRules.createTag();

		CompoundTag nbt = new CompoundTag();
		for (Map.Entry<String, String> entry : this.rules.entrySet()) {
			nbt.putString(entry.getKey(), entry.getValue());
		}

		gameRules.loadFromTag(new Dynamic<>(NbtOps.INSTANCE, nbt));

		return snapshot;
	}

	private void resetRules(GameRules gameRules, CompoundTag snapshot) {
		gameRules.loadFromTag(new Dynamic<>(NbtOps.INSTANCE, snapshot));
	}

	private void sendRuleUpdatesTo(GameRules gameRules, ServerPlayer player) {
		boolean immediateRespawn = gameRules.getRule(GameRules.RULE_DO_IMMEDIATE_RESPAWN).get();
		player.connection.send(new ClientboundGameEventPacket(ClientboundGameEventPacket.IMMEDIATE_RESPAWN, immediateRespawn ? 1.0F : 0.0F));
	}
}
