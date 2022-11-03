package com.lovetropics.minigames.common.core.game.client_state;

import com.lovetropics.lib.codec.MoreCodecs;
import com.lovetropics.minigames.Constants;
import com.lovetropics.minigames.LoveTropics;
import com.lovetropics.minigames.common.core.game.client_state.instance.BeaconClientState;
import com.lovetropics.minigames.common.core.game.client_state.instance.HealthTagClientState;
import com.lovetropics.minigames.common.core.game.client_state.instance.ReplaceTexturesClientState;
import com.lovetropics.minigames.common.core.game.client_state.instance.ResourcePackClientState;
import com.lovetropics.minigames.common.core.game.client_state.instance.SidebarClientState;
import com.lovetropics.minigames.common.core.game.client_state.instance.SpectatingClientState;
import com.lovetropics.minigames.common.core.game.client_state.instance.TimeInterpolationClientState;
import com.lovetropics.minigames.common.util.registry.GameClientTweakEntry;
import com.lovetropics.minigames.common.util.registry.LoveTropicsRegistrate;
import com.mojang.serialization.Codec;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegistryBuilder;

import java.util.function.Supplier;

public final class GameClientStateTypes {
	public static final ResourceKey<Registry<GameClientStateType<?>>> REGISTRY_KEY = ResourceKey.createRegistryKey(new ResourceLocation(Constants.MODID, "game_client_state"));

	public static final DeferredRegister<GameClientStateType<?>> REGISTER = DeferredRegister.create(REGISTRY_KEY, Constants.MODID);

	public static final Supplier<IForgeRegistry<GameClientStateType<?>>> REGISTRY = REGISTER.makeRegistry(GameClientStateType.type(), () -> {
		return new RegistryBuilder<GameClientStateType<?>>().disableSaving();
	});

	private static final LoveTropicsRegistrate REGISTRATE = LoveTropics.registrate();

	public static final Codec<GameClientStateType<?>> TYPE_CODEC = MoreCodecs.ofForgeRegistry(REGISTRY);

	public static final GameClientTweakEntry<ReplaceTexturesClientState> REPLACE_TEXTURES = register("replace_textures", ReplaceTexturesClientState.CODEC);
	public static final GameClientTweakEntry<TimeInterpolationClientState> TIME_INTERPOLATION = register("time_interpolation", TimeInterpolationClientState.CODEC);
	public static final GameClientTweakEntry<SpectatingClientState> SPECTATING = register("spectating", SpectatingClientState.CODEC);
	public static final GameClientTweakEntry<ResourcePackClientState> RESOURCE_PACK = register("resource_pack", ResourcePackClientState.CODEC);
	public static final GameClientTweakEntry<HealthTagClientState> HEALTH_TAG = register("health_tag", HealthTagClientState.CODEC);
	public static final GameClientTweakEntry<SidebarClientState> SIDEBAR = register("sidebar", SidebarClientState.CODEC);
	public static final GameClientTweakEntry<BeaconClientState> BEACON = register("beacon", BeaconClientState.CODEC);

	public static <T extends GameClientState> GameClientTweakEntry<T> register(final String name, final Codec<T> codec) {
		return REGISTRATE.object(name)
				.clientState(codec)
				.register();
	}

	public static void init(IEventBus modBus) {
		REGISTER.register(modBus);
	}
}
