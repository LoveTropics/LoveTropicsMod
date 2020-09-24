package com.lovetropics.minigames.common.minigames.behaviours.instances.survive_the_tide;

import com.lovetropics.minigames.client.data.TropicraftLangKeys;
import com.lovetropics.minigames.common.minigames.IMinigameInstance;
import com.lovetropics.minigames.common.minigames.MinigameManager;
import com.lovetropics.minigames.common.minigames.behaviours.IMinigameBehavior;
import com.mojang.datafixers.Dynamic;
import net.minecraft.entity.effect.LightningBoltEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraft.world.gen.Heightmap;
import net.minecraft.world.server.ServerWorld;

import java.util.Iterator;
import java.util.UUID;

public class SurviveTheTideWinConditionBehavior implements IMinigameBehavior
{
	private boolean minigameEnded;
	private int minigameEndedTimer = 0;
	private UUID winningPlayer;
	private ITextComponent winningPlayerName;

	public SurviveTheTideWinConditionBehavior() {
	}

	public static <T> SurviveTheTideWinConditionBehavior parse(Dynamic<T> root) {
		return new SurviveTheTideWinConditionBehavior();
	}

	@Override
	public void worldUpdate(final IMinigameInstance minigame, World world) {
		this.checkForGameEndCondition(minigame, world);
	}

	@Override
	public void onPlayerDeath(final IMinigameInstance minigame, ServerPlayerEntity player) {
		final MinecraftServer server = player.getServer();

		if (minigame.getParticipants().size() == 2) {
			Iterator<ServerPlayerEntity> it = minigame.getParticipants().iterator();

			ServerPlayerEntity p1 = it.next();
			ServerPlayerEntity p2 = it.next();

			if (p1 != null && p2 != null) {
				ITextComponent p1text = p1.getDisplayName().deepCopy().applyTextStyle(TextFormatting.AQUA);
				ITextComponent p2text = p2.getDisplayName().deepCopy().applyTextStyle(TextFormatting.AQUA);

				minigame.getPlayers().sendMessage(new TranslationTextComponent(TropicraftLangKeys.SURVIVE_THE_TIDE_DOWN_TO_TWO, p1text, p2text).applyTextStyle(TextFormatting.GOLD));
			}
		}

		if (minigame.getParticipants().size() == 1) {
			this.minigameEnded = true;

			this.winningPlayer = minigame.getParticipants().iterator().next().getUniqueID();
			this.winningPlayerName = server.getPlayerList().getPlayerByUUID(this.winningPlayer).getDisplayName().deepCopy();
		}
	}

	@Override
	public void onFinish(final IMinigameInstance minigame) {
		this.minigameEnded = false;
		this.minigameEndedTimer = 0;
		this.winningPlayer = null;
	}

	private void checkForGameEndCondition(final IMinigameInstance minigame, final World world) {
		if (this.minigameEnded) {
			spawnLightningBoltsEverywhere(world);
			sendFinishMessages(minigame);

			if (this.minigameEndedTimer == 20 * 38) {
				MinigameManager.getInstance().finishCurrentMinigame();
			}

			this.minigameEndedTimer++;
		}
	}

	private void spawnLightningBoltsEverywhere(final World world) {
		if (this.minigameEndedTimer % 60 == 0) {
			ServerPlayerEntity winning = world.getServer().getPlayerList().getPlayerByUUID(this.winningPlayer);

			if (winning != null) {
				int xOffset = (7 + world.rand.nextInt(5)) * (world.rand.nextBoolean() ? 1 : -1);
				int zOffset =  (7 + world.rand.nextInt(5)) * (world.rand.nextBoolean() ? 1 : -1);

				int posX = MathHelper.floor(winning.getPosX()) + xOffset;
				int posZ = MathHelper.floor(winning.getPosZ()) + zOffset;

				int posY = world.getHeight(Heightmap.Type.MOTION_BLOCKING, posX, posZ);

				((ServerWorld)world).addLightningBolt(new LightningBoltEntity(world, posX, posY, posZ, true));
			}
		}
	}

	private void sendFinishMessages(final IMinigameInstance minigame) {
		if (this.minigameEndedTimer == 0) {
			minigame.getPlayers().sendMessage(new TranslationTextComponent(TropicraftLangKeys.SURVIVE_THE_TIDE_FINISH1, this.winningPlayerName).applyTextStyle(TextFormatting.GRAY));
		} else if (this.minigameEndedTimer == 20 * 7){
			minigame.getPlayers().sendMessage(new TranslationTextComponent(TropicraftLangKeys.SURVIVE_THE_TIDE_FINISH2, this.winningPlayerName).applyTextStyle(TextFormatting.GRAY));
		} else if (this.minigameEndedTimer == 20 * 14){
			minigame.getPlayers().sendMessage(new TranslationTextComponent(TropicraftLangKeys.SURVIVE_THE_TIDE_FINISH3, this.winningPlayerName).applyTextStyle(TextFormatting.GRAY));
		} else if (this.minigameEndedTimer == 20 * 21){
			minigame.getPlayers().sendMessage(new TranslationTextComponent(TropicraftLangKeys.SURVIVE_THE_TIDE_FINISH4, this.winningPlayerName).applyTextStyle(TextFormatting.GRAY));
		} else if (this.minigameEndedTimer == 20 * 28) {
			minigame.getPlayers().sendMessage(new TranslationTextComponent(TropicraftLangKeys.MINIGAME_FINISH).applyTextStyle(TextFormatting.GOLD));
		}
	}
}
