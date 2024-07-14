package com.lovetropics.minigames.common.util.registry;

import com.lovetropics.minigames.common.core.game.predicate.entity.EntityPredicate;
import com.lovetropics.minigames.common.core.game.predicate.entity.EntityPredicates;
import com.mojang.serialization.MapCodec;
import com.tterrag.registrate.builders.AbstractBuilder;
import com.tterrag.registrate.builders.BuilderCallback;

public final class EntityPredicateBuilder<T extends EntityPredicate, P> extends AbstractBuilder<MapCodec<? extends EntityPredicate>, MapCodec<T>, P, EntityPredicateBuilder<T, P>> {
	private final MapCodec<T> codec;

	public EntityPredicateBuilder(LoveTropicsRegistrate owner, P parent, String name, BuilderCallback callback, MapCodec<T> codec) {
		super(owner, parent, name, callback, EntityPredicates.REGISTRY_KEY);
		this.codec = codec;
	}

	@Override
	protected MapCodec<T> createEntry() {
		return codec;
	}
}
