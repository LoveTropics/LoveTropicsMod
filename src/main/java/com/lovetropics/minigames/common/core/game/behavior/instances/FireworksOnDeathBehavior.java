package com.lovetropics.minigames.common.core.game.behavior.instances;

import com.lovetropics.lib.entity.FireworkPalette;
import com.lovetropics.minigames.common.core.game.IGameInstance;
import com.lovetropics.minigames.common.core.game.behavior.IGameBehavior;
import com.mojang.serialization.Codec;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.gen.Heightmap;
import net.minecraftforge.event.entity.living.LivingDeathEvent;

public final class FireworksOnDeathBehavior implements IGameBehavior {
	public static final Codec<FireworksOnDeathBehavior> CODEC = Codec.unit(FireworksOnDeathBehavior::new);

	@Override
	public void onPlayerDeath(final IGameInstance minigame, ServerPlayerEntity player, LivingDeathEvent event) {
		BlockPos fireworkPos = player.world.getHeight(Heightmap.Type.MOTION_BLOCKING, player.getPosition());
		FireworkPalette.ISLAND_ROYALE.spawn(fireworkPos, player.world);
	}
}
