package com.lovetropics.minigames.client.map;

import com.lovetropics.minigames.Constants;
import com.lovetropics.minigames.common.map.MapRegionSet;
import net.minecraft.world.IWorld;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Constants.MODID, value = Dist.CLIENT)
public final class ClientMapWorkspace {
	public static final ClientMapWorkspace INSTANCE = new ClientMapWorkspace();

	private MapRegionSet regions = new MapRegionSet();

	private ClientMapWorkspace() {
	}

	public void setRegions(MapRegionSet regions) {
		this.regions = regions;
	}

	public MapRegionSet getRegions() {
		return regions;
	}

	private void reset() {
		regions = new MapRegionSet();
	}

	@SubscribeEvent
	public static void onChangeDimension(WorldEvent.Load event) {
		IWorld world = event.getWorld();
		if (!world.isRemote()) {
			return;
		}

		INSTANCE.reset();
	}
}
