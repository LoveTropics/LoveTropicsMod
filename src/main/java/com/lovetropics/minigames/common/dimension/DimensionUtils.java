package com.lovetropics.minigames.common.dimension;

import com.lovetropics.minigames.LoveTropics;
import com.lovetropics.minigames.common.minigames.dimensions.ConservationExplorationDimension;
import com.lovetropics.minigames.common.minigames.dimensions.SignatureRunDimension;
import com.lovetropics.minigames.common.minigames.dimensions.SurviveTheTideDimension;
import com.lovetropics.minigames.common.minigames.dimensions.TrashDiveDimension;
import com.tterrag.registrate.Registrate;
import com.tterrag.registrate.util.entry.RegistryEntry;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.ModDimension;

public class DimensionUtils {
    
    public static final Registrate REGISTRATE = LoveTropics.registrate();
    
    public static DimensionType SURVIVE_THE_TIDE_DIMENSION;

	public static final RegistryEntry<ModDimension> SURVIVE_THE_TIDE_MOD_DIMENSION = REGISTRATE
			.dimension("hunger_games", SurviveTheTideDimension::new)
			.keepLoaded(false)
			.hasSkyLight(true)
			.dimensionTypeCallback(t -> SURVIVE_THE_TIDE_DIMENSION = t)
			.register();

	public static final RegistryEntry<ModDimension> CONSERVATION_EXPLORATION_MOD_DIMENSION = REGISTRATE
			.dimension("conservation_exploration_game", ConservationExplorationDimension::new)
			.keepLoaded(false)
			.hasSkyLight(true)
			.register();

	public static final RegistryEntry<ModDimension> TRASH_DIVE_MOD_DIMENSION = REGISTRATE
			.dimension("trash_dive_game", TrashDiveDimension::new)
			.keepLoaded(false)
			.hasSkyLight(true)
			.register();

	public static final RegistryEntry<ModDimension> SIGNATURE_RUN_MOD_DIMENSION = REGISTRATE
			.dimension("signature_run_game", SignatureRunDimension::new)
			.keepLoaded(false)
			.hasSkyLight(true)
			.register();

	public static void init() {
	}

    public static void teleportPlayerNoPortal(ServerPlayerEntity player, DimensionType destination, BlockPos pos) {
		if (!net.minecraftforge.common.ForgeHooks.onTravelToDimension(player, destination)) return;

		ServerWorld serverworld = player.server.getWorld(destination);
		player.teleport(serverworld, pos.getX() + 0.5D, pos.getY() + 1.0D, pos.getZ() + 0.5D, player.rotationYaw, player.rotationPitch);

		net.minecraftforge.fml.hooks.BasicEventHooks.firePlayerChangedDimensionEvent(player, destination, destination);
	}
}
