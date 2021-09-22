package com.lovetropics.minigames.common.core.game.behavior.instances.tweak;

import com.lovetropics.minigames.common.core.game.IActiveGame;
import com.lovetropics.minigames.common.core.game.behavior.IGameBehavior;
import com.lovetropics.minigames.common.core.game.behavior.event.EventRegistrar;
import com.lovetropics.minigames.common.core.game.behavior.event.GameLifecycleEvents;
import com.mojang.serialization.Codec;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.NBTDynamicOps;
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
	public void register(IActiveGame registerGame, EventRegistrar events) {
		GameRules gameRules = registerGame.getWorld().getGameRules();
		CompoundNBT rulesSnapshot = applyRules(gameRules);

		events.listen(GameLifecycleEvents.STOP, (game, reason) -> {
			resetRules(gameRules, rulesSnapshot);
		});
	}

	private CompoundNBT applyRules(GameRules gameRules) {
		CompoundNBT snapshot = gameRules.write();

		CompoundNBT nbt = new CompoundNBT();
		for (Map.Entry<String, String> entry : this.rules.entrySet()) {
			nbt.putString(entry.getKey(), entry.getValue());
		}

		gameRules.decode(new Dynamic<>(NBTDynamicOps.INSTANCE, nbt));

		return snapshot;
	}

	private void resetRules(GameRules gameRules, CompoundNBT snapshot) {
		gameRules.decode(new Dynamic<>(NBTDynamicOps.INSTANCE, snapshot));
	}
}
