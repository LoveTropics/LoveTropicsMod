package com.lovetropics.minigames.client.lobby.screen.game_config;

import com.google.common.collect.LinkedHashMultimap;
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
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.components.Button;
import com.mojang.blaze3d.vertex.Tesselator;
import net.minecraft.network.chat.TextComponent;
import net.minecraftforge.client.gui.ScrollPanel;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Optional;

public final class GameConfig extends ScrollPanel {
	private final Layout mainLayout;
	private Layout content;
	private final Screen screen;

	private final Handlers handlers;
	
	private ClientLobbyQueuedGame configuring;
	private final List<ClientBehaviorMap> configData = new ArrayList<>();
	private final Multimap<GameBehaviorType<?>, BehaviorConfigUI> configMenus = LinkedHashMultimap.create();

	private final List<GuiEventListener> children = new ArrayList<>();

	private final Button saveButton;

	public GameConfig(Screen screen, Layout main, Handlers handlers) {
		super(screen.getMinecraft(), main.background().width(), main.background().height(), main.background().top(),
				main.background().left());
		this.mainLayout = main;
		this.screen = screen;
		this.handlers = handlers;
		this.content = main;

		children.add(this.saveButton = new Button(main.content().right() - 46, main.content().bottom() - 20, 40, 20, new TextComponent("Save"), $ -> handlers.saveConfigs()));
		this.saveButton.active = false;
	}

	public interface Handlers {

		void saveConfigs();
	}

	public void setGame(@Nullable ClientLobbyQueuedGame game) {
		this.saveButton.active = game != null;

		this.configuring = game;
		this.configData.clear();
		if (game != null) {
			if (game.waitingConfigs() != null) {
				this.configData.add(game.waitingConfigs());
			}
			this.configData.add(game.playingConfigs());
		}
		reflow();
	}

	public void reflow() {
		this.configMenus.values().forEach(children::remove);
		this.configMenus.clear();
		LayoutTree ltree = new LayoutTree(this.mainLayout.unboundedY());
		ltree.child(new Box(0, 0, 6, 0), new Box()); // Add margin where the scroll bar is
		for (ClientBehaviorMap configSet : configData) {
			for (Entry<GameBehaviorType<?>, ClientConfigList> e : configSet.behaviors.entries()) {
				if (!e.getValue().configs.isEmpty()) {
					BehaviorConfigUI menu = new BehaviorConfigUI(this, ltree.child(0, 3), e.getKey(), e.getValue());
					this.configMenus.put(e.getKey(), menu);
					children.add(menu);
				}
			}
		}
		this.content = ltree.pop();
	}

	@Override
	public List<? extends GuiEventListener> children() {
		return children;
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
	protected void drawPanel(PoseStack mStack, int entryRight, int relativeY, Tesselator tess, int mouseX, int mouseY) {
		this.mainLayout.debugRender(mStack);
		mStack.pushPose();
		mStack.translate(0, relativeY - this.top - this.border, 0);
		this.content.debugRender(mStack);
		configMenus.values().forEach(ui -> ui.render(mStack, mouseX, mouseY + (int) this.scrollDistance, 0));
		mStack.popPose();
		this.saveButton.render(mStack, mouseX, mouseY, mouseY);
	}

	@Override
	protected void drawGradientRect(PoseStack mStack, int left, int top, int right, int bottom, int color1, int color2) {}

	@Override
	public Optional<GuiEventListener> getChildAt(double mouseX, double mouseY) {
		Optional<GuiEventListener> ret = super.getChildAt(mouseX, mouseY);
		if (!ret.isPresent() || ret.get() != saveButton) {
			// Can't allow the save button to activate here, the mouse position is wrong for it
			ret = super.getChildAt(mouseX, mouseY + this.scrollDistance).filter(g -> g != saveButton);
		}
		return ret;
	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		if (this.saveButton.isMouseOver(mouseX, mouseY)) {
			return saveButton.mouseClicked(mouseX, mouseY, button);
		}
		// Can't allow the save button to activate here, the mouse position is wrong for it
		this.saveButton.visible = false;
		boolean ret = super.mouseClicked(mouseX, mouseY, button);
		this.saveButton.visible = true;
		return ret;
	}
}
