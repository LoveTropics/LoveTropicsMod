package com.lovetropics.minigames.common.util.registry;

import com.lovetropics.minigames.common.core.game.behavior.IGameBehavior;
import com.lovetropics.minigames.common.core.game.client_state.GameClientState;
import com.lovetropics.minigames.common.core.game.predicate.entity.EntityPredicate;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.tterrag.registrate.AbstractRegistrate;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

public final class LoveTropicsRegistrate extends AbstractRegistrate<LoveTropicsRegistrate> {
	private LoveTropicsRegistrate(String modid) {
		super(modid);
	}

	public static LoveTropicsRegistrate create(String modid) {
		return new LoveTropicsRegistrate(modid).registerEventListeners(FMLJavaModLoadingContext.get().getModEventBus());
	}

	public <T extends IGameBehavior> GameBehaviorBuilder<T, LoveTropicsRegistrate> behavior(MapCodec<T> codec) {
		return behavior(this, codec);
	}

	public <T extends IGameBehavior, P> GameBehaviorBuilder<T, P> behavior(P parent, MapCodec<T> codec) {
		return behavior(parent, currentName(), codec);
	}

	public <T extends IGameBehavior, P> GameBehaviorBuilder<T, P> behavior(P parent, String name, MapCodec<T> codec) {
		return entry(name, callback -> new GameBehaviorBuilder<>(this, parent, name, callback, codec));
	}

	public <T extends EntityPredicate> EntityPredicateBuilder<T, LoveTropicsRegistrate> entityPredicate(Codec<T> codec) {
		return entityPredicate(this, currentName(), codec);
	}

	public <T extends EntityPredicate> EntityPredicateBuilder<T, LoveTropicsRegistrate> entityPredicate(String name, Codec<T> codec) {
		return entityPredicate(this, name, codec);
	}

	public <T extends EntityPredicate, P> EntityPredicateBuilder<T, P> entityPredicate(P parent, String name, Codec<T> codec) {
		return entry(name, callback -> new EntityPredicateBuilder<>(this, parent, name, callback, codec));
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
