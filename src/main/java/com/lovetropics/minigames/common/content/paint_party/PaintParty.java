package com.lovetropics.minigames.common.content.paint_party;

import com.lovetropics.minigames.LoveTropics;
import com.lovetropics.minigames.client.render.entity.PaintBallRenderer;
import com.lovetropics.minigames.common.content.paint_party.entity.PaintBallEntity;
import com.lovetropics.minigames.common.util.registry.GameBehaviorEntry;
import com.lovetropics.minigames.common.util.registry.LoveTropicsRegistrate;
import com.tterrag.registrate.util.entry.RegistryEntry;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;

public class PaintParty {
    private static final LoveTropicsRegistrate REGISTRATE = LoveTropics.registrate();

    public static final GameBehaviorEntry<PaintPartyBehaviour> BEHAVIOR = REGISTRATE.object("paint_party")
            .behavior(PaintPartyBehaviour.CODEC)
            .register();

    public static final RegistryEntry<EntityType<?>, EntityType<PaintBallEntity>> PAINTBALL = REGISTRATE.entity("paintball", (EntityType.EntityFactory<PaintBallEntity>) PaintBallEntity::new, MobCategory.MISC)
            .properties(properties -> properties.sized(0.25F, 0.25F).setShouldReceiveVelocityUpdates(true).setUpdateInterval(3))
            .defaultLang()
            .renderer(() -> PaintBallRenderer::new)
            .register();

    public static void init() {
    }
}
