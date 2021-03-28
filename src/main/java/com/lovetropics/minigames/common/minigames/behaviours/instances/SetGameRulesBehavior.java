package com.lovetropics.minigames.common.minigames.behaviours.instances;

import com.lovetropics.minigames.common.minigames.IMinigameInstance;
import com.lovetropics.minigames.common.minigames.behaviours.IMinigameBehavior;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.world.GameRules;

import java.util.Map;

public final class SetGameRulesBehavior implements IMinigameBehavior {
	public static final Codec<SetGameRulesBehavior> CODEC = RecordCodecBuilder.create(instance -> {
		return instance.group(
				Codec.unboundedMap(Codec.STRING, Codec.STRING).fieldOf("rules").forGetter(c -> c.rules)
		).apply(instance, SetGameRulesBehavior::new);
	});

	private final Map<String, String> rules;
	private CompoundNBT rulesSnapshot;

	public SetGameRulesBehavior(Map<String, String> rules) {
		this.rules = rules;
	}

	@Override
	public void onConstruct(IMinigameInstance minigame) {
		GameRules gameRules = minigame.getWorld().getGameRules();
		this.rulesSnapshot = gameRules.write();

		CompoundNBT nbt = new CompoundNBT();
		for (Map.Entry<String, String> entry : this.rules.entrySet()) {
			nbt.putString(entry.getKey(), entry.getValue());
		}

		gameRules.decode(nbt);
	}

	@Override
	public void onFinish(IMinigameInstance minigame) {
		if (this.rulesSnapshot != null) {
			GameRules gameRules = minigame.getWorld().getGameRules();
			gameRules.decode(this.rulesSnapshot);
		}
	}
}
