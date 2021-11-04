package com.lovetropics.minigames.client.game.handler;

import com.lovetropics.minigames.Constants;
import com.lovetropics.minigames.common.core.game.client_state.instance.ResourcePackClientState;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourcePackInfo;
import net.minecraft.resources.ResourcePackList;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Mod.EventBusSubscriber(modid = Constants.MODID, value = Dist.CLIENT)
public final class GameResourcePackHandler implements ClientGameStateHandler<ResourcePackClientState> {
	public static final GameResourcePackHandler INSTANCE = new GameResourcePackHandler();

	private static final Minecraft CLIENT = Minecraft.getInstance();

	private final Map<String, UpdateType> packUpdates = new Object2ObjectOpenHashMap<>();

	private GameResourcePackHandler() {
	}

	@SubscribeEvent
	public static void onClientTick(TickEvent.ClientTickEvent event) {
		if (event.phase == TickEvent.Phase.END) {
			GameResourcePackHandler handler = INSTANCE;
			if (!handler.packUpdates.isEmpty()) {
				handler.processUpdates();
			}
		}
	}

	private void processUpdates() {
		for (Map.Entry<String, UpdateType> entry : this.packUpdates.entrySet()) {
			String pack = entry.getKey();
			UpdateType updateType = entry.getValue();
			if (updateType == UpdateType.ADD) {
				this.add(pack);
			} else {
				this.remove(pack);
			}
		}

		this.packUpdates.clear();
	}

	@Override
	public void accept(ResourcePackClientState state) {
		INSTANCE.packUpdates.put(state.getPackName(), UpdateType.ADD);
	}

	@Override
	public void disable(ResourcePackClientState state) {
		INSTANCE.packUpdates.put(state.getPackName(), UpdateType.REMOVE);
	}

	private void add(String pack) {
		if (this.packExists(pack)) {
			this.updatePacks(enabledPacks -> {
				if (!enabledPacks.contains(pack)) {
					enabledPacks.add(pack);
					return true;
				} else {
					return false;
				}
			});
		}
	}

	private void remove(String pack) {
		if (this.packExists(pack)) {
			this.updatePacks(enabledPacks -> enabledPacks.remove(pack));
		}
	}

	private void updatePacks(Predicate<List<String>> apply) {
		ResourcePackList packList = CLIENT.getResourcePackList();

		List<String> enabledPacks = packList.getEnabledPacks().stream()
				.map(ResourcePackInfo::getName)
				.collect(Collectors.toList());

		if (apply.test(enabledPacks)) {
			packList.setEnabledPacks(enabledPacks);

			CLIENT.scheduleResourcesRefresh();
		}
	}

	private boolean packExists(String packName) {
		return CLIENT.getResourcePackList().func_232617_b_(packName);
	}

	enum UpdateType {
		ADD,
		REMOVE,
	}
}
