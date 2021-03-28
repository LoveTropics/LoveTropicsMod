package com.lovetropics.minigames.common.minigames.behaviours.instances.survive_the_tide;

import com.google.common.collect.Lists;
import com.lovetropics.minigames.common.minigames.IMinigameInstance;
import com.lovetropics.minigames.common.minigames.behaviours.IMinigameBehavior;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.world.server.ServerWorld;

import java.util.List;
import java.util.Set;
import java.util.UUID;

public class RevealPlayersBehavior implements IMinigameBehavior
{
	public static final Codec<RevealPlayersBehavior> CODEC = RecordCodecBuilder.create(instance -> {
		return instance.group(
				Codec.INT.optionalFieldOf("players_left_required", 2).forGetter(c -> c.playersLeftRequired),
				Codec.INT.optionalFieldOf("glow_on_time", 20).forGetter(c -> c.glowOnTime),
				Codec.INT.optionalFieldOf("glow_off_time", 80).forGetter(c -> c.glowOffTime)
		).apply(instance, RevealPlayersBehavior::new);
	});

	private final int playersLeftRequired;
	private final int glowOnTime;
	private final int glowOffTime;

	private int curGlowOnTime;
	private int curGlowOffTime;

	//prevent messing with spectral arrows
	private final Set<UUID> playerToWasGlowingAlready = new ObjectOpenHashSet<>();

	public RevealPlayersBehavior(final int playersLeftRequired, final int glowOnTime, final int glowOffTime) {
		this.playersLeftRequired = playersLeftRequired;
		this.glowOnTime = glowOnTime;
		this.glowOffTime = glowOffTime;
		this.curGlowOffTime = this.glowOffTime;
	}

	@Override
	public void worldUpdate(IMinigameInstance minigame, ServerWorld world) {
		final List<ServerPlayerEntity> players = Lists.newArrayList(minigame.getParticipants());
		if (players.size() <= playersLeftRequired) {
			if (curGlowOnTime > 0) {
				curGlowOnTime--;
				if (curGlowOnTime == 0) {
					curGlowOffTime = glowOffTime;
					for (ServerPlayerEntity player : players) {
						//prevent unsetting glow if something else was making them glow
						if (!playerToWasGlowingAlready.contains(player.getUniqueID())) {
							player.setGlowing(false);
						}
					}
				}
			}
			if (curGlowOffTime > 0) {
				curGlowOffTime--;
				if (curGlowOffTime == 0) {
					curGlowOnTime = glowOnTime;
					playerToWasGlowingAlready.clear();
					for (ServerPlayerEntity player : players) {
						if (player.isGlowing()) {
							playerToWasGlowingAlready.add(player.getUniqueID());
						}
						player.setGlowing(true);
					}
				}
			}
		}
	}


}
