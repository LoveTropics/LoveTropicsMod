package com.lovetropics.minigames.common.core.game.client_tweak;

import com.lovetropics.lib.codec.MoreCodecs;
import com.lovetropics.minigames.Constants;
import com.lovetropics.minigames.LoveTropics;
import com.lovetropics.minigames.common.core.game.client_tweak.instance.HotbarTextureTweak;
import com.lovetropics.minigames.common.core.game.client_tweak.instance.TimeInterpolationTweak;
import com.lovetropics.minigames.common.util.registry.GameClientTweakEntry;
import com.lovetropics.minigames.common.util.registry.LoveTropicsRegistrate;
import com.mojang.serialization.Codec;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegistryBuilder;

import java.util.function.Supplier;

public final class GameClientTweakTypes {
	public static final DeferredRegister<GameClientTweakType<?>> REGISTER = DeferredRegister.create(GameClientTweakType.type(), Constants.MODID);

	public static final Supplier<IForgeRegistry<GameClientTweakType<?>>> REGISTRY = REGISTER.makeRegistry("game_client_tweaks", () -> {
		return new RegistryBuilder<GameClientTweakType<?>>().disableSaving();
	});

	private static final LoveTropicsRegistrate REGISTRATE = LoveTropics.registrate();

	public static final Codec<GameClientTweakType<?>> TYPE_CODEC = MoreCodecs.ofForgeRegistry(REGISTRY);

	public static final GameClientTweakEntry<HotbarTextureTweak> HOTBAR_TEXTURE = register("hotbar_texture", HotbarTextureTweak.CODEC);
	public static final GameClientTweakEntry<TimeInterpolationTweak> TIME_INTERPOLATION = register("time_interpolation", TimeInterpolationTweak.CODEC);

	public static <T extends GameClientTweak> GameClientTweakEntry<T> register(final String name, final Codec<T> codec) {
		return REGISTRATE.object(name)
				.clientTweak(codec)
				.register();
	}

	public static void init(IEventBus modBus) {
		REGISTER.register(modBus);
	}
}
