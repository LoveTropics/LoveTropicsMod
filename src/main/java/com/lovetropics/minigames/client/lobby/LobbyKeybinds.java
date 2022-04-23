package com.lovetropics.minigames.client.lobby;

import com.lovetropics.minigames.Constants;
import net.minecraft.client.KeyMapping;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.client.settings.KeyModifier;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.glfw.GLFW.GLFW_KEY_J;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_L;

@EventBusSubscriber(bus = Bus.MOD, value = Dist.CLIENT, modid = Constants.MODID)
public class LobbyKeybinds {

	private static final List<KeyMapping> ALL = new ArrayList<>();

	public static final KeyMapping JOIN = makeMinigameKeybind("join", GLFW_KEY_J);
	public static final KeyMapping LEAVE = makeMinigameKeybind("leave", GLFW_KEY_L);

	private static final KeyMapping makeMinigameKeybind(String id, int key) {
		KeyMapping ret = new KeyMapping("key." + Constants.MODID + "." + id, KeyConflictContext.IN_GAME, KeyModifier.CONTROL, InputConstants.Type.KEYSYM.getOrCreate(key), "key.categories." + Constants.MODID + ".lobby");
		ALL.add(ret);
		return ret;
	}

	@SubscribeEvent
	public static void clientSetup(FMLClientSetupEvent event) {
		ALL.forEach(ClientRegistry::registerKeyBinding);
	}
}
