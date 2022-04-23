package com.lovetropics.minigames.common.core.game.behavior.instances.tweak;

import com.lovetropics.minigames.common.core.game.IGamePhase;
import com.lovetropics.minigames.common.core.game.behavior.IGameBehavior;
import com.lovetropics.minigames.common.core.game.behavior.event.EventRegistrar;
import com.lovetropics.minigames.common.core.game.behavior.event.GamePhaseEvents;
import com.lovetropics.minigames.common.core.game.behavior.event.GamePlayerEvents;
import com.mojang.serialization.Codec;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.NBTDynamicOps;
import net.minecraft.network.play.server.SChangeGameStatePacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.GameRules;

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
		CompoundNBT rulesSnapshot = applyRules(gameRules);

		events.listen(GamePlayerEvents.ADD, player -> sendRuleUpdatesTo(gameRules, player));
		events.listen(GamePlayerEvents.REMOVE, player -> sendRuleUpdatesTo(server.getGameRules(), player));

		events.listen(GamePhaseEvents.DESTROY, () -> {
			resetRules(gameRules, rulesSnapshot);
		});
	}

	private CompoundNBT applyRules(GameRules gameRules) {
		CompoundNBT snapshot = gameRules.createTag();

		CompoundNBT nbt = new CompoundNBT();
		for (Map.Entry<String, String> entry : this.rules.entrySet()) {
			nbt.putString(entry.getKey(), entry.getValue());
		}

		gameRules.loadFromTag(new Dynamic<>(NBTDynamicOps.INSTANCE, nbt));

		return snapshot;
	}

	private void resetRules(GameRules gameRules, CompoundNBT snapshot) {
		gameRules.loadFromTag(new Dynamic<>(NBTDynamicOps.INSTANCE, snapshot));
	}

	private void sendRuleUpdatesTo(GameRules gameRules, ServerPlayerEntity player) {
		boolean immediateRespawn = gameRules.getRule(GameRules.RULE_DO_IMMEDIATE_RESPAWN).get();
		player.connection.send(new SChangeGameStatePacket(SChangeGameStatePacket.IMMEDIATE_RESPAWN, immediateRespawn ? 1.0F : 0.0F));
	}
}
