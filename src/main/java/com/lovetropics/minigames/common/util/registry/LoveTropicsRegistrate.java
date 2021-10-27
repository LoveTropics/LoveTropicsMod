package com.lovetropics.minigames.common.util.registry;

import com.lovetropics.minigames.common.core.game.client_state.GameClientState;
import com.lovetropics.minigames.common.core.game.behavior.IGameBehavior;
import com.mojang.serialization.Codec;
import com.tterrag.registrate.AbstractRegistrate;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

public final class LoveTropicsRegistrate extends AbstractRegistrate<LoveTropicsRegistrate> {
	private LoveTropicsRegistrate(String modid) {
		super(modid);
	}

	public static LoveTropicsRegistrate create(String modid) {
		return new LoveTropicsRegistrate(modid).registerEventListeners(FMLJavaModLoadingContext.get().getModEventBus());
	}

	public <T extends IGameBehavior> GameBehaviorBuilder<T, LoveTropicsRegistrate> behavior(Codec<T> codec) {
		return behavior(this, codec);
	}

	public <T extends IGameBehavior, P> GameBehaviorBuilder<T, P> behavior(P parent, Codec<T> codec) {
		return behavior(parent, currentName(), codec);
	}

	public <T extends IGameBehavior, P> GameBehaviorBuilder<T, P> behavior(P parent, String name, Codec<T> codec) {
		return entry(name, callback -> new GameBehaviorBuilder<>(this, parent, name, callback, codec));
	}

	public <T extends GameClientState> GameClientTweakBuilder<T, LoveTropicsRegistrate> clientState(Codec<T> codec) {
		return clientState(this, codec);
	}

	public <T extends GameClientState, P> GameClientTweakBuilder<T, P> clientState(P parent, Codec<T> codec) {
		return clientState(parent, currentName(), codec);
	}

	public <T extends GameClientState, P> GameClientTweakBuilder<T, P> clientState(P parent, String name, Codec<T> codec) {
		return entry(name, callback -> new GameClientTweakBuilder<>(this, parent, name, callback, codec));
	}
}
