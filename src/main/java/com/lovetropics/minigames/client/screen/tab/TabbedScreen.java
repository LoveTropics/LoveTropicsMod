package com.lovetropics.minigames.client.screen.tab;

import com.lovetropics.minigames.client.screen.LayoutScreen;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ComponentPath;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.client.gui.components.events.AbstractContainerEventHandler;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.layouts.HeaderAndFooterLayout;
import net.minecraft.client.gui.layouts.Layout;
import net.minecraft.client.gui.layouts.LayoutElement;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.navigation.FocusNavigationEvent;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.CommonColors;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.Nullable;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;

public abstract class TabbedScreen<T extends TabbedScreen.Tab> extends LayoutScreen<HeaderAndFooterLayout> {
	private static final int TABS_HEIGHT = 24;
	private static final int HEADER_HEIGHT = 36;

	protected final List<T> tabs;

	private final TabBar tabBar;

	private int selectedTabIndex;

	protected TabbedScreen(Component title, List<T> tabs) {
		super(title);
		if (tabs.isEmpty()) {
			throw new IllegalStateException("TabbedScreen must have at least one tab");
		}
		this.tabs = tabs;
		tabBar = new TabBar();
	}

	protected abstract void populateLayout(HeaderAndFooterLayout layout, T tab);

	@Override
	protected final HeaderAndFooterLayout initializeLayout() {
		return createLayout(selectedTab());
	}

	private HeaderAndFooterLayout createLayout(T tab) {
		HeaderAndFooterLayout layout = new HeaderAndFooterLayout(this);
		layout.setHeaderHeight(HEADER_HEIGHT);
		layout.addToHeader(tabBar);
		populateLayout(layout, tab);
		return layout;
	}

	public void selectTab(int index) {
		if (index == selectedTabIndex) {
			return;
		}
		selectedTabIndex = Mth.clamp(index, 0, tabs.size() - 1);
		updateLayout(createLayout(selectedTab()));
		if (tabBar.isFocused()) {
			tabBar.setFocused(tabBar.buttons.get(selectedTabIndex));
		}
	}

	public void selectTab(T tab) {
		int index = tabs.indexOf(tab);
		if (index == -1) {
			throw new IllegalArgumentException("Tab '" + tab + "' does not exist in this screen");
		}
		selectTab(index);
	}

	public T selectedTab() {
		return tabs.get(selectedTabIndex);
	}

	public interface Tab {
		Component title();
	}

	protected class TabBar extends AbstractContainerEventHandler implements Layout, Renderable, GuiEventListener, NarratableEntry {
		private static final int MAX_WIDTH = 400;
		private static final int MARGIN = 14;
		private static final Component USAGE_NARRATION = Component.translatable("narration.tab_navigation.usage");

		private final List<TabButton<T>> buttons;

		private TabBar() {
			buttons = tabs.stream().map(tab -> new TabButton<>(TabbedScreen.this, tab, 0, TABS_HEIGHT)).toList();
		}

		@Override
		public void setFocused(boolean focused) {
			super.setFocused(focused);
			if (getFocused() != null) {
				getFocused().setFocused(focused);
			}
		}

		@Override
		@SuppressWarnings("unchecked")
		public void setFocused(@Nullable GuiEventListener listener) {
			super.setFocused(listener);
			if (listener instanceof TabButton<?> button) {
				selectTab((T) button.tab);
			}
		}

		@Override
		@Nullable
		public ComponentPath nextFocusPath(FocusNavigationEvent event) {
			if (!isFocused()) {
				TabButton<T> button = selectedTabButton();
				return ComponentPath.path(this, ComponentPath.leaf(button));
			}
			return event instanceof FocusNavigationEvent.TabNavigation ? null : super.nextFocusPath(event);
		}

		@Override
		public List<? extends GuiEventListener> children() {
			return buttons;
		}

		@Override
		public NarratableEntry.NarrationPriority narrationPriority() {
			return buttons.stream().map(AbstractWidget::narrationPriority).max(Comparator.naturalOrder()).orElse(NarratableEntry.NarrationPriority.NONE);
		}

		@Override
		public void updateNarration(NarrationElementOutput output) {
			Optional<TabButton<T>> selectedButton = buttons.stream()
					.filter(AbstractWidget::isHovered)
					.findFirst()
					.or(() -> Optional.of(selectedTabButton()));
			selectedButton.ifPresent(button -> {
				narrateListElementPosition(output.nest(), button);
				button.updateNarration(output);
			});
			if (isFocused()) {
				output.add(NarratedElementType.USAGE, USAGE_NARRATION);
			}
		}

		protected void narrateListElementPosition(NarrationElementOutput output, TabButton<T> button) {
			if (tabs.size() > 1) {
				int index = buttons.indexOf(button);
				if (index != -1) {
					output.add(NarratedElementType.POSITION, Component.translatable("narrator.position.tab", index + 1, tabs.size()));
				}
			}
		}

		@Override
		public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
			int buttonLeft = buttons.getFirst().getX();
			int buttonRight = buttons.getLast().getRight();
			RenderSystem.enableBlend();
			graphics.blit(Screen.HEADER_SEPARATOR, 0, getY() + getHeight() - 2, 0.0f, 0.0f, buttonLeft, 2, 32, 2);
			graphics.blit(Screen.HEADER_SEPARATOR, buttonRight, getY() + getHeight() - 2, 0.0f, 0.0f, getWidth(), 2, 32, 2);
			RenderSystem.disableBlend();

			for (TabButton<T> button : buttons) {
				button.render(graphics, mouseX, mouseY, partialTick);
			}
		}

		@Override
		public void setX(int x) {
		}

		@Override
		public void setY(int y) {
		}

		@Override
		public int getX() {
			return 0;
		}

		@Override
		public int getY() {
			return 0;
		}

		@Override
		public int getWidth() {
			return width;
		}

		@Override
		public int getHeight() {
			return TABS_HEIGHT;
		}

		@Override
		public ScreenRectangle getRectangle() {
			return new ScreenRectangle(getX(), getY(), getWidth(), getHeight());
		}

		@Override
		public void visitChildren(Consumer<LayoutElement> consumer) {
		}

		@Override
		public void arrangeElements() {
			int availableWidth = Math.min(MAX_WIDTH, width) - MARGIN * 2;
			int buttonWidth = Mth.roundToward(availableWidth / tabs.size(), 2);

			int buttonLeft = Mth.roundToward((width - availableWidth) / 2, 2);
			for (TabButton<T> button : buttons) {
				button.setX(buttonLeft);
				button.setWidth(buttonWidth);
				buttonLeft += buttonWidth;
			}
		}

		private TabButton<T> selectedTabButton() {
			return buttons.get(selectedTabIndex);
		}
	}

	protected static class TabButton<T extends TabbedScreen.Tab> extends AbstractWidget {
		private static final WidgetSprites SPRITES = new WidgetSprites(
				ResourceLocation.withDefaultNamespace("widget/tab_selected"),
				ResourceLocation.withDefaultNamespace("widget/tab"),
				ResourceLocation.withDefaultNamespace("widget/tab_selected_highlighted"),
				ResourceLocation.withDefaultNamespace("widget/tab_highlighted")
		);
		private static final int SELECTED_OFFSET = 3;
		private static final int TEXT_MARGIN = 1;
		private static final int UNDERLINE_HEIGHT = 1;
		private static final int UNDERLINE_MARGIN_X = 4;
		private static final int UNDERLINE_MARGIN_BOTTOM = 2;

		private final TabbedScreen<T> screen;
		private final T tab;

		public TabButton(TabbedScreen<T> screen, T tab, int width, int height) {
			super(0, 0, width, height, tab.title());
			this.tab = tab;
			this.screen = screen;
		}

		@Override
		public void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
			RenderSystem.enableBlend();
			graphics.blitSprite(SPRITES.get(isSelected(), isHoveredOrFocused()), getX(), getY(), width, height);
			RenderSystem.disableBlend();

			Font font = Minecraft.getInstance().font;
			int textColor = active ? CommonColors.WHITE : CommonColors.LIGHT_GRAY;
			renderLabel(graphics, font, textColor);
			if (isSelected()) {
				renderMenuBackground(graphics, getX() + 2, getY() + 2, getRight() - 2, getBottom());
				renderFocusUnderline(graphics, font, textColor);
			}
		}

		private void renderMenuBackground(GuiGraphics graphics, int x0, int y0, int x1, int y1) {
			Screen.renderMenuBackgroundTexture(graphics, Screen.MENU_BACKGROUND, x0, y0, 0.0f, 0.0f, x1 - x0, y1 - y0);
		}

		protected void renderLabel(GuiGraphics graphics, Font font, int color) {
			int left = getX() + TEXT_MARGIN;
			int top = getY() + (isSelected() ? 0 : SELECTED_OFFSET);
			int right = getX() + getWidth() - TEXT_MARGIN;
			int bottom = getY() + getHeight();
			renderScrollingString(graphics, font, getMessage(), left, top, right, bottom, color);
		}

		private void renderFocusUnderline(GuiGraphics graphics, Font font, int color) {
			int labelWidth = Math.min(font.width(getMessage()), getWidth() - UNDERLINE_MARGIN_X);
			int left = getX() + (getWidth() - labelWidth) / 2;
			int top = getY() + getHeight() - UNDERLINE_MARGIN_BOTTOM;
			graphics.fill(left, top, left + labelWidth, top + UNDERLINE_HEIGHT, color);
		}

		@Override
		protected void updateWidgetNarration(NarrationElementOutput output) {
			output.add(NarratedElementType.TITLE, Component.translatable("gui.narrate.tab", tab.title()));
		}

		public boolean isSelected() {
			return Objects.equals(screen.selectedTab(), tab);
		}
	}
}
