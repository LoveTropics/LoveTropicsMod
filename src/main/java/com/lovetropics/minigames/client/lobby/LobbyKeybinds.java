package com.lovetropics.minigames.client.lobby;

import static org.lwjgl.glfw.GLFW.*;

import java.util.ArrayList;
import java.util.List;

import com.lovetropics.minigames.Constants;

import net.minecraft.client.settings.KeyBinding;
import net.minecraft.client.util.InputMappings;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.client.settings.KeyModifier;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@EventBusSubscriber(bus = Bus.MOD, value = Dist.CLIENT, modid = Constants.MODID)
public class LobbyKeybinds {

	private static final List<KeyBinding> ALL = new ArrayList<>();

	public static final KeyBinding JOIN = makeMinigameKeybind("join", GLFW_KEY_J);
	public static final KeyBinding SPECTATE = makeMinigameKeybind("spectate", GLFW_KEY_K);
	public static final KeyBinding LEAVE = makeMinigameKeybind("leave", GLFW_KEY_L);

	private static final KeyBinding makeMinigameKeybind(String id, int key) {
		KeyBinding ret = new KeyBinding("key." + Constants.MODID + "." + id, KeyConflictContext.IN_GAME, KeyModifier.CONTROL, InputMappings.Type.KEYSYM.getOrMakeInput(key), "key.categories." + Constants.MODID + ".lobby");
		ALL.add(ret);
		return ret;
	}

	@SubscribeEvent
	public static void clientSetup(FMLClientSetupEvent event) {
		ALL.forEach(ClientRegistry::registerKeyBinding);
	}
}
