package com.lovetropics.minigames.client.map;

import com.lovetropics.lib.BlockBox;
import com.lovetropics.minigames.Constants;
import com.lovetropics.minigames.common.core.map.workspace.ClientWorkspaceRegions;
import net.minecraft.world.level.LevelAccessor;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import javax.annotation.Nullable;

@Mod.EventBusSubscriber(modid = Constants.MODID, value = Dist.CLIENT)
public final class ClientMapWorkspace {
	public static final ClientMapWorkspace INSTANCE = new ClientMapWorkspace();

	private ClientWorkspaceRegions regions = new ClientWorkspaceRegions();

	private ClientMapWorkspace() {
	}

	public void setRegions(ClientWorkspaceRegions regions) {
		this.regions = regions;
	}

	public void addRegion(int id, String key, BlockBox region) {
		regions.add(id, key, region);
	}

	public void updateRegion(int id, @Nullable BlockBox region) {
		regions.set(id, region);
	}

	public ClientWorkspaceRegions getRegions() {
		return regions;
	}

	private void reset() {
		regions = new ClientWorkspaceRegions();
	}

	@SubscribeEvent
	public static void onChangeDimension(LevelEvent.Load event) {
		LevelAccessor level = event.getLevel();
		if (level.isClientSide()) {
			INSTANCE.reset();
		}
	}
}
