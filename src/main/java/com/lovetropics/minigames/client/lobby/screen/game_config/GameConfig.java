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
import com.lovetropics.minigames.client.screen.flex.Box;
import com.lovetropics.minigames.client.screen.flex.Flex;
import com.lovetropics.minigames.client.screen.flex.FlexSolver;
import com.lovetropics.minigames.client.screen.flex.Layout;
import com.lovetropics.minigames.client.screen.flex.Flex.Unit;
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
	private final Screen screen;

	private final Handlers handlers;
	
	private ClientLobbyQueuedGame configuring;
	private ClientBehaviorMap configData = new ClientBehaviorMap(LinkedHashMultimap.create());
	private final Multimap<GameBehaviorType<?>, BehaviorConfigUI> children = LinkedHashMultimap.create();
	private int height;

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
		
		Flex flex = new Flex().column();
		for (Entry<GameBehaviorType<?>, ClientConfigList> e : configData.behaviors.entries()) {
			if (!e.getValue().configs.isEmpty()) {
				Flex basis = flex.child().column().padding(3).width(1, Unit.PERCENT);
				this.children.put(e.getKey(), new BehaviorConfigUI((ManageLobbyScreen) screen, basis, e.getKey(), e.getValue()));
			}
		}
		FlexSolver solver = new FlexSolver(this.mainLayout.content());
		FlexSolver.Results results = solver.apply(flex);
		this.children.values().forEach(b -> b.bake(results));

		this.height = this.children.values().stream().mapToInt(BehaviorConfigUI::getHeight).sum();
	}

	@Override
	public List<? extends IGuiEventListener> getEventListeners() {
		return Lists.newArrayList(children.values());
	}

	public static IConfigWidget createWidget(Screen screen, Flex basis, ConfigData value) {
		if (value instanceof SimpleConfigData) {
			return SimpleConfigWidget.from(basis, (SimpleConfigData) value);
		} else if (value instanceof ListConfigData) {
			return ListConfigWidget.from(screen, basis, (ListConfigData) value);
		} else if (value instanceof CompositeConfigData) {
			return CompositeConfigWidget.from(screen, basis, (CompositeConfigData) value);
		}
		throw new IllegalArgumentException("Unknown config type: " + value);
	}
	
	@Override
	public boolean isMouseOver(double mouseX, double mouseY) {
		return mainLayout.content().contains(mouseX, mouseY);
	}

	@Override
	protected int getContentHeight() {
		return this.height;
	}

	@Override
	protected void drawPanel(MatrixStack mStack, int entryRight, int relativeY, Tessellator tess, int mouseX,
			int mouseY) {
		Box outline = mainLayout.background();
		vLine(mStack, outline.left(), outline.top(), outline.bottom(), 0xFFFF0000);
		vLine(mStack, outline.right() - 1, outline.top(), outline.bottom(), 0xFFFF0000);
		hLine(mStack, outline.left(), outline.right(), outline.top(), 0xFFFF0000);
		hLine(mStack, outline.left(), outline.right(), outline.bottom() - 1, 0xFFFF0000);
		children.values().forEach(ui -> ui.render(mStack, mouseX, mouseY, 0));
	}
}
