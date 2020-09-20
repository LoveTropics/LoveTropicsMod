package com.lovetropics.minigames.common.dimension;

import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Supplier;

import com.lovetropics.minigames.Constants;
import com.lovetropics.minigames.common.Util;
import com.lovetropics.minigames.common.minigames.dimensions.SurviveTheTideDimension;

import io.netty.buffer.Unpooled;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.dimension.Dimension;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.ModDimension;
import net.minecraftforge.event.world.RegisterDimensionsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class DimensionUtils {
    
    public static final DeferredRegister<ModDimension> DIMENSIONS = new DeferredRegister<>(ForgeRegistries.MOD_DIMENSIONS, Constants.MODID);

	public static DimensionType SURVIVE_THE_TIDE_DIMENSION;

	public static ResourceLocation SURVIVE_THE_TIDE_ID = Util.resource("hunger_games");

	public static final RegistryObject<ModDimension> SURVIVE_THE_TIDE_MOD_DIMENSION = register(
			SURVIVE_THE_TIDE_ID.getPath(), DimensionUtils::surviveTheTideDimFactory);

	private static ModDimension surviveTheTideDimFactory() {
		return new ModDimension() {
			@Override
			public BiFunction<World, DimensionType, ? extends Dimension> getFactory() {
				return SurviveTheTideDimension::new;
			}
		};
	}
	
	private static RegistryObject<ModDimension> register(final String name, final Supplier<ModDimension> sup) {
	    return DIMENSIONS.register(name, sup);
	}

	@Mod.EventBusSubscriber(modid = Constants.MODID)
	public static class EventDimensionType {
		@SubscribeEvent
		public static void onModDimensionRegister(final RegisterDimensionsEvent event) {
			postRegister(SURVIVE_THE_TIDE_ID, dimensionType -> SURVIVE_THE_TIDE_DIMENSION = dimensionType, () -> SURVIVE_THE_TIDE_DIMENSION, SURVIVE_THE_TIDE_MOD_DIMENSION);
		}

		public static void postRegister(ResourceLocation id, Consumer<DimensionType> dimSetter, Supplier<DimensionType> dimGetter, RegistryObject<ModDimension> modDimRegistry) {
			if (DimensionType.byName(id) == null) {
				dimSetter.accept(DimensionManager.registerDimension(id, modDimRegistry.get(), new PacketBuffer(Unpooled.buffer()), true));
				DimensionManager.keepLoaded(dimGetter.get(), false);
			} else {
				dimSetter.accept(DimensionType.byName(id));
			}
		}
	}

    public static void teleportPlayerNoPortal(ServerPlayerEntity player, DimensionType destination, BlockPos pos) {
		if (!net.minecraftforge.common.ForgeHooks.onTravelToDimension(player, destination)) return;

		ServerWorld serverworld = player.server.getWorld(destination);
		player.teleport(serverworld, pos.getX() + 0.5D, pos.getY() + 1.0D, pos.getZ() + 0.5D, player.rotationYaw, player.rotationPitch);

		net.minecraftforge.fml.hooks.BasicEventHooks.firePlayerChangedDimensionEvent(player, destination, destination);
	}
}