package com.lovetropics.minigames.common.content.biodiversity_blitz.entity;

import com.lovetropics.minigames.common.content.biodiversity_blitz.entity.ai.BbMobBrain;
import com.lovetropics.minigames.common.content.biodiversity_blitz.plot.Plot;
import net.minecraft.entity.MobEntity;

public interface BbMobEntity {
	BbMobBrain getMobBrain();

	MobEntity asMob();

	Plot getPlot();
}
