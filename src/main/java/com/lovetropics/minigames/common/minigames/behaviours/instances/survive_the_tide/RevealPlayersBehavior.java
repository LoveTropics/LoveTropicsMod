package com.lovetropics.minigames.common.minigames.behaviours.instances.survive_the_tide;

import com.google.common.collect.Lists;
import com.lovetropics.minigames.common.Util;
import com.lovetropics.minigames.common.game_actions.DonationPackageGameAction;
import com.lovetropics.minigames.common.minigames.IMinigameInstance;
import com.lovetropics.minigames.common.minigames.behaviours.IMinigameBehavior;
import com.lovetropics.minigames.common.minigames.behaviours.instances.donations.DonationPackageData;
import com.mojang.datafixers.Dynamic;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.potion.EffectInstance;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.*;

public class RevealPlayersBehavior implements IMinigameBehavior
{
	private final DonationPackageData data;

	private final int playersLeftRequired;
	private final int glowOnTime;
	private final int glowOffTime;

	private int curGlowOnTime;
	private int curGlowOffTime;

	//prevent messing with spectral arrows
	private HashSet<UUID> playerToWasGlowingAlready = new HashSet<>();

	public RevealPlayersBehavior(final DonationPackageData data, final int playersLeftRequired, final int glowOnTime, final int glowOffTime) {
		this.data = data;
		this.playersLeftRequired = playersLeftRequired;
		this.glowOnTime = glowOnTime;
		this.glowOffTime = glowOffTime;
		this.curGlowOffTime = this.glowOffTime;
	}

	public static <T> RevealPlayersBehavior parse(Dynamic<T> root) {
		final DonationPackageData data = DonationPackageData.parse(root);
			final int playersLeftRequired = root.get("players_left_required").asInt(2);
		final int glowOnTime = root.get("glow_on_time").asInt(20);
		final int glowOffTime = root.get("glow_off_time").asInt(80);

		return new RevealPlayersBehavior(data, playersLeftRequired, glowOnTime, glowOffTime);
	}

	@Override
	public void worldUpdate(IMinigameInstance minigame, World world) {
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
