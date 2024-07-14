package com.lovetropics.minigames.client.lobby.screen.game_config;

import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;
import com.lovetropics.minigames.client.lobby.manage.state.ClientLobbyQueuedGame;
import com.lovetropics.minigames.client.lobby.state.ClientBehaviorList;
import com.lovetropics.minigames.client.lobby.state.ClientConfigList;
import com.lovetropics.minigames.client.screen.LayoutTree;
import com.lovetropics.minigames.client.screen.flex.Box;
import com.lovetropics.minigames.client.screen.flex.Layout;
import com.lovetropics.minigames.common.core.game.behavior.config.ConfigData;
import com.lovetropics.minigames.common.core.game.behavior.config.ConfigData.CompositeConfigData;
import com.lovetropics.minigames.common.core.game.behavior.config.ConfigData.ListConfigData;
import com.lovetropics.minigames.common.core.game.behavior.config.ConfigData.SimpleConfigData;
import com.mojang.blaze3d.vertex.Tesselator;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.client.gui.widget.ScrollPanel;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class GameConfig extends ScrollPanel {
	private final Layout mainLayout;
	private Layout content;
	private final Screen screen;

	private final Handlers handlers;
	
	private ClientLobbyQueuedGame configuring;
	private final List<ClientBehaviorList> configData = new ArrayList<>();
	private final Multimap<ResourceLocation, BehaviorConfigUI> configMenus = LinkedHashMultimap.create();

	private final List<GuiEventListener> children = new ArrayList<>();

	private final Button saveButton;

	public GameConfig(Screen screen, Layout main, Handlers handlers) {
		super(screen.getMinecraft(), main.background().width(), main.background().height(), main.background().top(),
				main.background().left());
		this.mainLayout = main;
		this.screen = screen;
		this.handlers = handlers;
		this.content = main;

		children.add(this.saveButton = Button.builder(Component.literal("Save"), $ -> handlers.saveConfigs())
				.bounds(main.content().right() - 46, main.content().bottom() - 20, 40, 20).build());
		this.saveButton.active = false;
	}

	@Override
	public NarrationPriority narrationPriority() {
		return NarrationPriority.NONE;
	}

	@Override
	public void updateNarration(final NarrationElementOutput output) {
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
		for (ClientBehaviorList configSet : configData) {
			for (ClientConfigList behavior : configSet.behaviors()) {
				if (!behavior.configs().isEmpty()) {
					BehaviorConfigUI menu = new BehaviorConfigUI(this, ltree.child(0, 3), behavior);
					this.configMenus.put(behavior.id(), menu);
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
        return switch (value) {
            case SimpleConfigData simpleConfigData -> SimpleConfigWidget.from(ltree, simpleConfigData);
            case ListConfigData objects -> ListConfigWidget.from(this, ltree, objects);
            case CompositeConfigData compositeConfigData -> CompositeConfigWidget.from(this, ltree, compositeConfigData);
            default -> throw new IllegalArgumentException("Unknown config type: " + value);
        };
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
	protected void drawPanel(GuiGraphics graphics, int entryRight, int relativeY, Tesselator tess, int mouseX, int mouseY) {
		this.mainLayout.debugRender(graphics);
		graphics.pose().pushPose();
		graphics.pose().translate(0, relativeY - this.top - this.border, 0);
		this.content.debugRender(graphics);
		configMenus.values().forEach(ui -> ui.render(graphics, mouseX, mouseY + (int) this.scrollDistance, 0));
		graphics.pose().popPose();
		this.saveButton.render(graphics, mouseX, mouseY, mouseY);
	}

	@Override
	protected void drawGradientRect(GuiGraphics graphics, int left, int top, int right, int bottom, int color1, int color2) {}

	@Override
	public Optional<GuiEventListener> getChildAt(double mouseX, double mouseY) {
		Optional<GuiEventListener> ret = super.getChildAt(mouseX, mouseY);
		if (ret.isEmpty() || ret.get() != saveButton) {
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
