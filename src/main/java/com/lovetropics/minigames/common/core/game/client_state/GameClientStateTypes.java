package com.lovetropics.minigames.common.core.game.client_state;

import com.lovetropics.lib.codec.MoreCodecs;
import com.lovetropics.minigames.Constants;
import com.lovetropics.minigames.LoveTropics;
import com.lovetropics.minigames.common.core.game.client_state.instance.HotbarTextureClientState;
import com.lovetropics.minigames.common.core.game.client_state.instance.SpectatingClientState;
import com.lovetropics.minigames.common.core.game.client_state.instance.TimeInterpolationClientState;
import com.lovetropics.minigames.common.util.registry.GameClientTweakEntry;
import com.lovetropics.minigames.common.util.registry.LoveTropicsRegistrate;
import com.mojang.serialization.Codec;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegistryBuilder;

import java.util.function.Supplier;

public final class GameClientStateTypes {
	public static final DeferredRegister<GameClientStateType<?>> REGISTER = DeferredRegister.create(GameClientStateType.type(), Constants.MODID);

	public static final Supplier<IForgeRegistry<GameClientStateType<?>>> REGISTRY = REGISTER.makeRegistry("game_client_state", () -> {
		return new RegistryBuilder<GameClientStateType<?>>().disableSaving();
	});

	private static final LoveTropicsRegistrate REGISTRATE = LoveTropics.registrate();

	public static final Codec<GameClientStateType<?>> TYPE_CODEC = MoreCodecs.ofForgeRegistry(REGISTRY);

	public static final GameClientTweakEntry<HotbarTextureClientState> HOTBAR_TEXTURE = register("hotbar_texture", HotbarTextureClientState.CODEC);
	public static final GameClientTweakEntry<TimeInterpolationClientState> TIME_INTERPOLATION = register("time_interpolation", TimeInterpolationClientState.CODEC);
	public static final GameClientTweakEntry<SpectatingClientState> SPECTATING = register("spectating", SpectatingClientState.CODEC);

	public static <T extends GameClientState> GameClientTweakEntry<T> register(final String name, final Codec<T> codec) {
		return REGISTRATE.object(name)
				.clientState(codec)
				.register();
	}

	public static void init(IEventBus modBus) {
		REGISTER.register(modBus);
	}
}
