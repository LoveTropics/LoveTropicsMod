package net.tropicraft.core.common.block.tileentity;

import com.google.common.collect.Sets;

import net.minecraft.tileentity.TileEntityType;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.tropicraft.Constants;
import net.tropicraft.core.common.block.TropicraftBlocks;

public class TropicraftTileEntityTypes {
    
    public static final DeferredRegister<TileEntityType<?>> TILE_ENTITIES = new DeferredRegister<>(ForgeRegistries.TILE_ENTITIES, Constants.MODID);

    public static final RegistryObject<TileEntityType<DonationTileEntity>> DONATION = TILE_ENTITIES.register(
            "donation", () -> new TileEntityType<>(DonationTileEntity::new, Sets.newHashSet(TropicraftBlocks.DONATION.get()), null));
}
