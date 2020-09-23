package com.lovetropics.minigames.common.map.workspace;

import com.lovetropics.minigames.Constants;
import com.lovetropics.minigames.common.map.VoidChunkGenerator;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.dimension.Dimension;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.gen.ChunkGenerator;
import net.minecraft.world.gen.ChunkGeneratorType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.ModDimension;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public final class MapWorkspaceDimension extends Dimension {
	public static final DeferredRegister<ModDimension> REGISTER = DeferredRegister.create(ForgeRegistries.MOD_DIMENSIONS, Constants.MODID);

	public static final RegistryObject<ModDimension> MOD_DIMENSION = REGISTER.register("map_workspace", () -> {
		return ModDimension.withFactory(MapWorkspaceDimension::new);
	});

	public MapWorkspaceDimension(World world, DimensionType dimensionType) {
		super(world, dimensionType, 0.0F);
	}

	@Override
	public ChunkGenerator<?> createChunkGenerator() {
		// TODO: allow specifying chunk generator
		ChunkGeneratorType<VoidChunkGenerator.Settings, VoidChunkGenerator> type = VoidChunkGenerator.TYPE.get();

		VoidChunkGenerator.Settings settings = type.createSettings();
		return type.create(world, VoidChunkGenerator.biomeProvider(world), settings);
	}

	@Override
	public BlockPos findSpawn(ChunkPos chunkPos, boolean checkValid) {
		return new BlockPos(chunkPos.getXStart() + 8, 64, chunkPos.getZStart() + 8);
	}

	@Override
	public BlockPos findSpawn(int posX, int posZ, boolean checkValid) {
		return new BlockPos(posX, 64, posZ);
	}

	@Override
	public float calculateCelestialAngle(long worldTime, float partialTicks) {
		double timeOfDay = MathHelper.frac(worldTime / 24000.0 - 0.25);
		double offset = 0.5 - Math.cos(timeOfDay * Math.PI) / 2.0;
		return (float) (timeOfDay * 2.0 + offset) / 3.0F;
	}

	@Override
	public boolean isSurfaceWorld() {
		return true;
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public Vec3d getFogColor(float celestialAngle, float partialTicks) {
		float pi2 = (float) Math.PI * 2;
		float delta = MathHelper.cos(celestialAngle * pi2) * 2.0F + 0.5F;
		delta = MathHelper.clamp(delta, 0.0F, 1.0F);

		float red = 0.75F * (delta * 0.94F + 0.06F);
		float green = 0.85F * (delta * 0.94F + 0.06F);
		float blue = 1.0F * (delta * 0.91F + 0.09F);

		return new Vec3d(red, green, blue);
	}

	@OnlyIn(Dist.CLIENT)
	@Override
	public float getCloudHeight() {
		return 192;
	}

	@Override
	public boolean canRespawnHere() {
		return false;
	}

	@Override
	public boolean doesXZShowFog(int x, int z) {
		return false;
	}
}
