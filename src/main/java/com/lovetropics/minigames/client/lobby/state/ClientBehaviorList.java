package com.lovetropics.minigames.client.lobby.state;

import com.lovetropics.minigames.common.core.game.behavior.BehaviorList;
import com.lovetropics.minigames.common.core.game.behavior.IGameBehavior;
import net.minecraft.network.FriendlyByteBuf;

import java.util.List;
import java.util.Objects;

public record ClientBehaviorList(List<ClientConfigList> behaviors) {
	public static ClientBehaviorList from(BehaviorList behaviors) {
		return new ClientBehaviorList(behaviors.stream().map(IGameBehavior::getConfigurables).filter(Objects::nonNull).map(ClientConfigList::from).toList());
	}

	public static ClientBehaviorList decode(FriendlyByteBuf buffer) {
		return new ClientBehaviorList(buffer.readList(ClientConfigList::decode));
	}

	public void encode(FriendlyByteBuf buffer) {
		buffer.writeCollection(behaviors, (buf, list) -> list.encode(buf));
	}
}
