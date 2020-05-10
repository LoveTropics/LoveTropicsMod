package net.tropicraft.lovetropics.common.dimension.biome.minigames;

import net.minecraft.entity.EntityClassification;
import net.minecraft.entity.EntityType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.DefaultBiomeFeatures;
import net.minecraft.world.gen.GenerationStage;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.SeaGrassConfig;
import net.minecraft.world.gen.feature.structure.MineshaftConfig;
import net.minecraft.world.gen.feature.structure.MineshaftStructure;
import net.minecraft.world.gen.feature.structure.OceanRuinConfig;
import net.minecraft.world.gen.feature.structure.ShipwreckConfig;
import net.minecraft.world.gen.placement.*;
import net.minecraft.world.gen.surfacebuilders.SurfaceBuilder;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class SurviveTheTideBiome extends Biome {
    public static final int WATER_COLOR = 0x417251;
    public static final int WATER_FOG_COLOR = 0x0f331b;

    public SurviveTheTideBiome() {
        super(new Builder()
                .surfaceBuilder(SurfaceBuilder.DEFAULT, SurfaceBuilder.GRASS_DIRT_GRAVEL_CONFIG)
                .precipitation(RainType.RAIN)
                .category(Category.OCEAN)
                .depth(-1.6F)
                .scale(0.4F)
                .temperature(1.5F)
                .downfall(1.25F)
                .temperature(2.0F)
                .parent(null).waterColor(WATER_COLOR).waterFogColor(WATER_FOG_COLOR)
        );

        this.addStructure(Feature.MINESHAFT.withConfiguration(new MineshaftConfig(0.004D, MineshaftStructure.Type.NORMAL)));
        this.addStructure(Feature.SHIPWRECK.withConfiguration(new ShipwreckConfig(false)));
        this.addStructure(Feature.OCEAN_RUIN.withConfiguration(new OceanRuinConfig(net.minecraft.world.gen.feature.structure.OceanRuinStructure.Type.COLD, 0.3F, 0.9F)));
        DefaultBiomeFeatures.addOceanCarvers(this);
        DefaultBiomeFeatures.addStructures(this);
        DefaultBiomeFeatures.addLakes(this);
        DefaultBiomeFeatures.addStoneVariants(this);
        DefaultBiomeFeatures.addOres(this);
        DefaultBiomeFeatures.addSedimentDisks(this);
        DefaultBiomeFeatures.addScatteredOakTrees(this);
        DefaultBiomeFeatures.addDefaultFlowers(this);
        DefaultBiomeFeatures.addSparseGrass(this);
        DefaultBiomeFeatures.addMushrooms(this);
        DefaultBiomeFeatures.addReedsAndPumpkins(this);
        DefaultBiomeFeatures.addSprings(this);
        this.addFeature(GenerationStage.Decoration.VEGETAL_DECORATION, Feature.SEAGRASS.withConfiguration(new SeaGrassConfig(48, 0.3D)).withPlacement(Placement.TOP_SOLID_HEIGHTMAP.configure(IPlacementConfig.NO_PLACEMENT_CONFIG)));
        DefaultBiomeFeatures.addSeagrass(this);
        DefaultBiomeFeatures.addExtraKelp(this);
        DefaultBiomeFeatures.addFreezeTopLayer(this);
        this.addSpawn(EntityClassification.WATER_CREATURE, new SpawnListEntry(EntityType.SQUID, 1, 1, 4));
        this.addSpawn(EntityClassification.WATER_CREATURE, new SpawnListEntry(EntityType.COD, 10, 3, 6));
        this.addSpawn(EntityClassification.WATER_CREATURE, new SpawnListEntry(EntityType.DOLPHIN, 1, 1, 2));
    }

    @OnlyIn(Dist.CLIENT)
    public int getSkyColorByTemp(float currentTemperature) {
        return 0x0f331b;//ConfigLT.BIOMES.surviveTheTideSkyColor.get();
    }

    @OnlyIn(Dist.CLIENT)
    public int getFoliageColor(BlockPos p_180625_1_) {
        return 0x208d2a;//ConfigLT.BIOMES.surviveTheTideFoliageColor.get();
    }

    @OnlyIn(Dist.CLIENT)
    public int getGrassColor(BlockPos p_180627_1_) {
        return 0x498551;//ConfigLT.BIOMES.surviveTheTideGrassColor.get();
    }
}