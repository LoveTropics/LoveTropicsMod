package com.lovetropics.minigames.client.lobby.screen.game_config;

import java.util.List;
import java.util.Map.Entry;

import javax.annotation.Nullable;

import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.lovetropics.minigames.client.lobby.manage.screen.ManageLobbyScreen;
import com.lovetropics.minigames.client.lobby.manage.state.ClientLobbyQueuedGame;
import com.lovetropics.minigames.client.lobby.state.ClientBehaviorMap;
import com.lovetropics.minigames.client.lobby.state.ClientConfigList;
import com.lovetropics.minigames.client.screen.LayoutGui;
import com.lovetropics.minigames.client.screen.flex.Layout;
import com.lovetropics.minigames.common.core.game.behavior.GameBehaviorType;
import com.lovetropics.minigames.common.core.game.behavior.config.ConfigData;
import com.lovetropics.minigames.common.core.game.behavior.config.ConfigData.CompositeConfigData;
import com.lovetropics.minigames.common.core.game.behavior.config.ConfigData.ListConfigData;
import com.lovetropics.minigames.common.core.game.behavior.config.ConfigData.SimpleConfigData;
import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.client.gui.IGuiEventListener;
import net.minecraft.client.gui.INestedGuiEventHandler;
import net.minecraft.client.gui.IRenderable;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.renderer.Tessellator;
import net.minecraftforge.client.gui.ScrollPanel;

public final class GameConfig extends ScrollPanel {
	private final Layout mainLayout;
	private final Screen screen;

	private final Handlers handlers;
	
	private ClientLobbyQueuedGame configuring;
	private ClientBehaviorMap configData = new ClientBehaviorMap(LinkedHashMultimap.create());
	private final Multimap<GameBehaviorType<?>, BehaviorConfigUI> children = LinkedHashMultimap.create();

	public GameConfig(Screen screen, Layout main, Handlers handlers) {
		super(screen.getMinecraft(), main.background().width(), main.background().height(), main.background().top(),
				main.background().left());
		this.mainLayout = main;
		this.screen = screen;
		this.handlers = handlers;
	}

	public interface Handlers {

		void saveConfigs();
	}
	
	public void setGame(@Nullable ClientLobbyQueuedGame game) {
		this.configuring = game;
		this.configData = game == null ? new ClientBehaviorMap(LinkedHashMultimap.create()) : game.configs();
		this.children.clear();
		for (Entry<GameBehaviorType<?>, ClientConfigList> e : configData.behaviors.entries()) {
			// TODO narrow layout
			this.children.put(e.getKey(), new BehaviorConfigUI((ManageLobbyScreen) screen, mainLayout, e.getKey(), e.getValue()));
		}
	}

	@Override
	public List<? extends IGuiEventListener> getEventListeners() {
		return Lists.newArrayList(children.values());
	}

	@SuppressWarnings("unchecked")
	public static <T extends INestedGuiEventHandler & IRenderable> T createWidget(Layout layout, ConfigData value) {
		if (value instanceof SimpleConfigData) {
			return (T) SimpleConfigWidget.from(layout, (SimpleConfigData) value);
		} else if (value instanceof ListConfigData) {
			return (T) ListConfigWidget.from(layout, (ListConfigData) value);
		} else if (value instanceof CompositeConfigData) {
			return (T) CompositeConfigWidget.from(layout, (CompositeConfigData) value);
		}
		throw new IllegalArgumentException("Unknown config type: " + value);
	}
	
	@Override
	public boolean isMouseOver(double mouseX, double mouseY) {
		return mainLayout.content().contains(mouseX, mouseY);
	}

	@Override
	protected int getContentHeight() {
		return 1000;
	}

	@Override
	protected void drawPanel(MatrixStack mStack, int entryRight, int relativeY, Tessellator tess, int mouseX,
			int mouseY) {
		children.values().forEach(ui -> ui.render(mStack, mouseX, mouseY, 0));
	}
}
