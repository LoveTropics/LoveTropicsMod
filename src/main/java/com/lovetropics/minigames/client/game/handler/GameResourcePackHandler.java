package com.lovetropics.minigames.client.game.handler;

import com.lovetropics.minigames.common.core.game.client_state.instance.ResourcePackClientState;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourcePackInfo;
import net.minecraft.resources.ResourcePackList;

import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public final class GameResourcePackHandler implements ClientGameStateHandler<ResourcePackClientState> {
	public static final GameResourcePackHandler INSTANCE = new GameResourcePackHandler();

	private static final Minecraft CLIENT = Minecraft.getInstance();

	private GameResourcePackHandler() {
	}

	@Override
	public void accept(ResourcePackClientState state) {
		String pack = state.getPackName();
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

	@Override
	public void disable(ResourcePackClientState state) {
		String pack = state.getPackName();
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
}
