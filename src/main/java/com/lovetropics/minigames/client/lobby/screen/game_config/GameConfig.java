package com.lovetropics.minigames.client.lobby.screen.game_config;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Optional;

import javax.annotation.Nullable;

import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.lovetropics.minigames.client.lobby.manage.state.ClientLobbyQueuedGame;
import com.lovetropics.minigames.client.lobby.state.ClientBehaviorMap;
import com.lovetropics.minigames.client.lobby.state.ClientConfigList;
import com.lovetropics.minigames.client.screen.LayoutTree;
import com.lovetropics.minigames.client.screen.flex.Box;
import com.lovetropics.minigames.client.screen.flex.Layout;
import com.lovetropics.minigames.common.core.game.behavior.GameBehaviorType;
import com.lovetropics.minigames.common.core.game.behavior.config.ConfigData;
import com.lovetropics.minigames.common.core.game.behavior.config.ConfigData.CompositeConfigData;
import com.lovetropics.minigames.common.core.game.behavior.config.ConfigData.ListConfigData;
import com.lovetropics.minigames.common.core.game.behavior.config.ConfigData.SimpleConfigData;
import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.client.gui.IGuiEventListener;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.renderer.Tessellator;
import net.minecraftforge.client.gui.ScrollPanel;

public final class GameConfig extends ScrollPanel {
	private final Layout mainLayout;
	private Layout content;
	private final Screen screen;

	private final Handlers handlers;
	
	private ClientLobbyQueuedGame configuring;
	private final List<ClientBehaviorMap> configData = new ArrayList<>();
	private final Multimap<GameBehaviorType<?>, BehaviorConfigUI> children = LinkedHashMultimap.create();

	public GameConfig(Screen screen, Layout main, Handlers handlers) {
		super(screen.getMinecraft(), main.background().width(), main.background().height(), main.background().top(),
				main.background().left());
		this.mainLayout = main;
		this.screen = screen;
		this.handlers = handlers;
		this.content = main;
	}

	public interface Handlers {

		void saveConfigs();
	}
	
	public void setGame(@Nullable ClientLobbyQueuedGame game) {
		this.configuring = game;
		this.configData.clear();
		if (game != null) {
			this.configData.add(game.waitingConfigs());
			this.configData.add(game.playingConfigs());
		}
		reflow();
	}

	public void reflow() {
		this.children.clear();
		LayoutTree ltree = new LayoutTree(this.mainLayout.unboundedY());
		ltree.child(new Box(0, 0, 6, 0), new Box()); // Add margin where the scroll bar is
		for (ClientBehaviorMap configSet : configData) {
			for (Entry<GameBehaviorType<?>, ClientConfigList> e : configSet.behaviors.entries()) {
				if (!e.getValue().configs.isEmpty()) {
					this.children.put(e.getKey(), new BehaviorConfigUI(this, ltree.child(0, 3), e.getKey(), e.getValue()));
				}
			}
		}
		this.content = ltree.pop();
	}

	@Override
	public List<? extends IGuiEventListener> getEventListeners() {
		return Lists.newArrayList(children.values());
	}

	public IConfigWidget createWidget(LayoutTree ltree, ConfigData value) {
		if (value instanceof SimpleConfigData) {
			return SimpleConfigWidget.from(ltree, (SimpleConfigData) value);
		} else if (value instanceof ListConfigData) {
			return ListConfigWidget.from(this, ltree, (ListConfigData) value);
		} else if (value instanceof CompositeConfigData) {
			return CompositeConfigWidget.from(this, ltree, (CompositeConfigData) value);
		}
		throw new IllegalArgumentException("Unknown config type: " + value);
	}
	
	@Override
	public boolean isMouseOver(double mouseX, double mouseY) {
		return mainLayout.content().contains(mouseX, mouseY);
	}

	@Override
	protected int getContentHeight() {
		return this.content.margin().height();
	}

	@Override
	protected void drawPanel(MatrixStack mStack, int entryRight, int relativeY, Tessellator tess, int mouseX, int mouseY) {
		this.mainLayout.debugRender(mStack);
		mStack.push();
		mStack.translate(0, relativeY - this.top - this.border, 0);
		this.content.debugRender(mStack);
		children.values().forEach(ui -> ui.render(mStack, mouseX, mouseY + (int) this.scrollDistance, 0));
		mStack.pop();
	}

	@Override
	protected void drawGradientRect(MatrixStack mStack, int left, int top, int right, int bottom, int color1, int color2) {}

	@Override
	public Optional<IGuiEventListener> getEventListenerForPos(double mouseX, double mouseY) {
		return super.getEventListenerForPos(mouseX, mouseY + this.scrollDistance);
	}
}
