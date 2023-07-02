package com.lovetropics.minigames.common.core.game.client_state.instance;

import com.lovetropics.minigames.common.core.game.client_state.GameClientState;
import com.lovetropics.minigames.common.core.game.client_state.GameClientStateType;
import com.lovetropics.minigames.common.core.game.client_state.GameClientStateTypes;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.chat.Component;
import net.minecraft.util.ExtraCodecs;

import java.util.List;

public record SidebarClientState(Component title, List<Component> lines) implements GameClientState {
	public static final Codec<SidebarClientState> CODEC = RecordCodecBuilder.create(i -> i.group(
			ExtraCodecs.COMPONENT.fieldOf("title").forGetter(SidebarClientState::title),
			ExtraCodecs.COMPONENT.listOf().fieldOf("lines").forGetter(SidebarClientState::lines)
	).apply(i, SidebarClientState::new));

	@Override
	public GameClientStateType<?> getType() {
		return GameClientStateTypes.SIDEBAR.get();
	}
}
