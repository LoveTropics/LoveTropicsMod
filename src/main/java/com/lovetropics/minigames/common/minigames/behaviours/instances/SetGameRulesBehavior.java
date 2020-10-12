package com.lovetropics.minigames.common.minigames.behaviours.instances;

import com.lovetropics.minigames.common.minigames.IMinigameInstance;
import com.lovetropics.minigames.common.minigames.behaviours.IMinigameBehavior;
import com.mojang.datafixers.Dynamic;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.world.GameRules;

import java.util.Map;

public final class SetGameRulesBehavior implements IMinigameBehavior {
	private final Map<String, String> rules;
	private CompoundNBT rulesSnapshot;

	public SetGameRulesBehavior(Map<String, String> rules) {
		this.rules = rules;
	}

	public static <T> SetGameRulesBehavior parse(Dynamic<T> root) {
		Map<String, String> rules = root.get("rules").asMap(
				key -> key.asString(""),
				value -> value.asString("")
		);
		return new SetGameRulesBehavior(rules);
	}

	@Override
	public void onMapReady(IMinigameInstance minigame) {
		GameRules gameRules = minigame.getWorld().getGameRules();
		this.rulesSnapshot = gameRules.write();

		CompoundNBT nbt = new CompoundNBT();
		for (Map.Entry<String, String> entry : this.rules.entrySet()) {
			nbt.putString(entry.getKey(), entry.getValue());
		}

		gameRules.read(nbt);
	}

	@Override
	public void onFinish(IMinigameInstance minigame) {
		if (this.rulesSnapshot != null) {
			GameRules gameRules = minigame.getWorld().getGameRules();
			gameRules.read(this.rulesSnapshot);
		}
	}
}
