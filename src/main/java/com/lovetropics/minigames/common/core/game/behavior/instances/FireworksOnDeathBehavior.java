package com.lovetropics.minigames.common.core.game.behavior.instances;

import com.lovetropics.lib.entity.FireworkPalette;
import com.lovetropics.minigames.common.core.game.IActiveGame;
import com.lovetropics.minigames.common.core.game.behavior.IGameBehavior;
import com.lovetropics.minigames.common.core.game.behavior.event.EventRegistrar;
import com.lovetropics.minigames.common.core.game.behavior.event.GamePlayerEvents;
import com.mojang.serialization.Codec;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.gen.Heightmap;

public final class FireworksOnDeathBehavior implements IGameBehavior {
	public static final Codec<FireworksOnDeathBehavior> CODEC = Codec.unit(FireworksOnDeathBehavior::new);

	@Override
	public void register(IActiveGame registerGame, EventRegistrar events) {
		events.listen(GamePlayerEvents.DEATH, (game, player, damageSource) -> {
			BlockPos fireworkPos = player.world.getHeight(Heightmap.Type.MOTION_BLOCKING, player.getPosition());
			FireworkPalette.ISLAND_ROYALE.spawn(fireworkPos, player.world);
			return ActionResultType.PASS;
		});
	}
}
