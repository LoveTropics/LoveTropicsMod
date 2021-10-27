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
		String packName = state.getPackName();
		this.updatePacks(enabledPacks -> {
			if (!enabledPacks.contains(packName)) {
				enabledPacks.add(packName);
				return true;
			} else {
				return false;
			}
		});
	}

	@Override
	public void disable(ResourcePackClientState state) {
		String packName = state.getPackName();
		this.updatePacks(enabledPacks -> enabledPacks.remove(packName));
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
}
