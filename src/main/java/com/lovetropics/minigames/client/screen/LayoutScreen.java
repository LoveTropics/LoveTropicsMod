package com.lovetropics.minigames.client.screen;

import com.lovetropics.minigames.mixin.client.ScreenAccessor;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.layouts.Layout;
import net.minecraft.client.gui.layouts.LayoutElement;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Consumer;

public abstract class LayoutScreen<L extends Layout> extends Screen {
	@Nullable
	protected L layout;

	protected LayoutScreen(Component title) {
		super(title);
	}

	protected abstract L initializeLayout();

	@Override
	protected void init() {
		super.init();
		updateLayout(initializeLayout());
	}

	@Override
	protected void repositionElements() {
		if (layout != null) {
			layout.arrangeElements();
		}
	}

	protected void updateLayout(L newLayout) {
		if (layout != null) {
			visitTree(layout, this::removeLayoutElement);
		}
		layout = newLayout;
		visitTree(newLayout, this::addLayoutElement);
		repositionElements();
	}

	// Hack: LayoutElement.visitWidgets() isn't enough for us, as we want to be able to include non-widgets
	@SuppressWarnings("unchecked")
	private void addLayoutElement(LayoutElement element) {
		if (element instanceof GuiEventListener eventListener) {
			((List<GuiEventListener>) children()).add(eventListener);
		}
		if (element instanceof NarratableEntry narratable) {
			((ScreenAccessor) this).ltminigames$getNarratables().add(narratable);
		}
		if (element instanceof Renderable renderable) {
			renderables.add(renderable);
		}
	}

	private void removeLayoutElement(LayoutElement element) {
		if (element instanceof GuiEventListener eventListener) {
			removeWidget(eventListener);
		} else if (element instanceof Renderable renderable) {
			renderables.remove(renderable);
		}
	}

	private static void visitTree(Layout layout, Consumer<LayoutElement> consumer) {
		layout.visitChildren(child -> {
			consumer.accept(child);
			if (child instanceof Layout childLayout) {
				visitTree(childLayout, consumer);
			}
		});
	}
}
