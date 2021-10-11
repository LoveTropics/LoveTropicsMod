package com.lovetropics.minigames.common.core.game.behavior.configold;

import com.mojang.serialization.Codec;

public interface ConfigType<T> {

	Codec<T> codec();

}
