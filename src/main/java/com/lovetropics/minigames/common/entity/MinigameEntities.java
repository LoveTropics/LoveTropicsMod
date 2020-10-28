package com.lovetropics.minigames.common.entity;

import com.lovetropics.minigames.LoveTropics;
import com.lovetropics.minigames.client.render.entity.DriftwoodRenderer;
import com.tterrag.registrate.Registrate;
import com.tterrag.registrate.util.entry.RegistryEntry;
import net.minecraft.entity.EntityClassification;
import net.minecraft.entity.EntityType;

public final class MinigameEntities {
	private static final Registrate REGISTRATE = LoveTropics.registrate();

	public static final RegistryEntry<EntityType<DriftwoodEntity>> DRIFTWOOD = REGISTRATE.entity("driftwood", DriftwoodEntity::new, EntityClassification.MISC)
			.properties(properties -> properties.size(2.0F, 1.0F).setShouldReceiveVelocityUpdates(true).setUpdateInterval(3))
			.defaultLang()
			.renderer(() -> DriftwoodRenderer::new)
			.register();

	public static void init() {}
}
