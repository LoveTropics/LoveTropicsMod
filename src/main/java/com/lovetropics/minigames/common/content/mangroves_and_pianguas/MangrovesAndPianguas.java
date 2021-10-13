package com.lovetropics.minigames.common.content.mangroves_and_pianguas;

import com.lovetropics.minigames.Constants;
import com.lovetropics.minigames.LoveTropics;
import com.lovetropics.minigames.common.content.mangroves_and_pianguas.behavior.MpAssignPlotsBehavior;
import com.lovetropics.minigames.common.content.mangroves_and_pianguas.behavior.MpBehavior;
import com.lovetropics.minigames.common.content.mangroves_and_pianguas.behavior.MpMerchantBehavior;
import com.lovetropics.minigames.common.content.mangroves_and_pianguas.behavior.MpWaveSpawnerBehavior;
import com.lovetropics.minigames.common.util.GameBehaviorEntry;
import com.lovetropics.minigames.common.util.LoveTropicsRegistrate;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Constants.MODID)
public final class MangrovesAndPianguas {
    private static final LoveTropicsRegistrate REGISTRATE = LoveTropics.registrate();

    public static final GameBehaviorEntry<MpBehavior> MANGROVES_AND_PIANGUAS = REGISTRATE.object("mangroves_and_pianguas")
            .behavior(MpBehavior.CODEC)
            .register();

    public static final GameBehaviorEntry<MpAssignPlotsBehavior> ASSIGN_PLOTS = REGISTRATE.object("mangroves_and_pianguas_assign_plots")
            .behavior(MpAssignPlotsBehavior.CODEC)
            .register();

    public static final GameBehaviorEntry<MpMerchantBehavior> MERCHANT = REGISTRATE.object("mangroves_and_pianguas_merchant")
            .behavior(MpMerchantBehavior.CODEC)
            .register();

    public static final GameBehaviorEntry<MpWaveSpawnerBehavior> WAVE_SPAWNER = REGISTRATE.object("mangroves_and_pianguas_wave_spawner")
            .behavior(MpWaveSpawnerBehavior.CODEC)
            .register();

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {

    }
}
