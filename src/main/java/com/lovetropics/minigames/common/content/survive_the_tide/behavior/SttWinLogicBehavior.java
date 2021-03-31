package com.lovetropics.minigames.common.content.survive_the_tide.behavior;

import com.lovetropics.minigames.common.core.game.GameException;
import com.lovetropics.minigames.common.core.game.IGameInstance;
import com.lovetropics.minigames.common.core.game.IGameManager;
import com.lovetropics.minigames.common.core.game.behavior.IGameBehavior;
import com.lovetropics.minigames.common.core.game.behavior.event.EventRegistrar;
import com.lovetropics.minigames.common.core.game.behavior.event.GameLifecycleEvents;
import com.lovetropics.minigames.common.core.game.behavior.event.GameLogicEvents;
import com.lovetropics.minigames.common.core.game.util.TemplatedText;
import com.lovetropics.minigames.common.util.MoreCodecs;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.effect.LightningBoltEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;
import net.minecraft.world.gen.Heightmap;

public class SttWinLogicBehavior implements IGameBehavior {
	public static final Codec<SttWinLogicBehavior> CODEC = RecordCodecBuilder.create(instance -> {
		return instance.group(
				Codec.LONG.optionalFieldOf("game_finish_tick_delay", 0L).forGetter(c -> c.gameFinishTickDelay),
				MoreCodecs.long2Object(TemplatedText.CODEC).fieldOf("scheduled_game_finish_messages").forGetter(c -> c.scheduledGameFinishMessages),
				Codec.BOOL.optionalFieldOf("spawn_lightning_bolts_on_finish", false).forGetter(c -> c.spawnLightningBoltsOnFinish),
				Codec.INT.optionalFieldOf("lightning_bolt_spawn_tick_rate", 60).forGetter(c -> c.lightningBoltSpawnTickRate)
		).apply(instance, SttWinLogicBehavior::new);
	});

	protected boolean minigameEnded;
	private long minigameEndedTimer = 0;
	protected final long gameFinishTickDelay;
	private ITextComponent winner;
	protected final Long2ObjectMap<TemplatedText> scheduledGameFinishMessages;
	protected final boolean spawnLightningBoltsOnFinish;
	protected final int lightningBoltSpawnTickRate;

	public SttWinLogicBehavior(final long gameFinishTickDelay, final Long2ObjectMap<TemplatedText> scheduledGameFinishMessages, final boolean spawnLightningBoltsOnFinish, final int lightningBoltSpawnTickRate) {
		this.gameFinishTickDelay = gameFinishTickDelay;
		this.scheduledGameFinishMessages = scheduledGameFinishMessages;
		this.spawnLightningBoltsOnFinish = spawnLightningBoltsOnFinish;
		this.lightningBoltSpawnTickRate = lightningBoltSpawnTickRate;
	}

	@Override
	public void register(IGameInstance registerGame, EventRegistrar events) throws GameException {
		events.listen(GameLogicEvents.WIN_TRIGGERED, (game, winnerName) -> {
			this.winner = winnerName;
			this.minigameEnded = true;
			this.minigameEndedTimer = 0;
		});

		events.listen(GameLifecycleEvents.TICK, game -> this.checkForGameEndCondition(game, game.getWorld()));

		events.listen(GameLifecycleEvents.FINISH, game -> {
			this.minigameEnded = false;
			this.minigameEndedTimer = 0;
		});
	}

	private void checkForGameEndCondition(final IGameInstance game, final World world) {
		if (this.minigameEnded) {
			if (spawnLightningBoltsOnFinish) {
				spawnLightningBoltsEverywhere(game, world);
			}

			sendGameFinishMessages(game);

			if (this.minigameEndedTimer >= gameFinishTickDelay) {
				IGameManager.get().finish(game);
			}

			this.minigameEndedTimer++;
		}
	}

	private void spawnLightningBoltsEverywhere(IGameInstance minigame, final World world) {
		if (this.minigameEndedTimer % lightningBoltSpawnTickRate == 0) {
			for (ServerPlayerEntity player : minigame.getParticipants()) {
				int xOffset = (7 + world.rand.nextInt(5)) * (world.rand.nextBoolean() ? 1 : -1);
				int zOffset = (7 + world.rand.nextInt(5)) * (world.rand.nextBoolean() ? 1 : -1);

				int posX = MathHelper.floor(player.getPosX()) + xOffset;
				int posZ = MathHelper.floor(player.getPosZ()) + zOffset;

				int posY = world.getHeight(Heightmap.Type.MOTION_BLOCKING, posX, posZ);

				LightningBoltEntity lightning = new LightningBoltEntity(EntityType.LIGHTNING_BOLT, world);
				lightning.forceSetPosition(posX, posY, posZ);
				lightning.setEffectOnly(true);

				world.addEntity(lightning);
			}
		}
	}

	private void sendGameFinishMessages(final IGameInstance game) {
		TemplatedText message = scheduledGameFinishMessages.remove(minigameEndedTimer);
		if (message != null) {
			game.getAllPlayers().sendMessage(message.apply(winner));
		}
	}
}
