package com.lovetropics.minigames.client.lobby.screen.game_config;

import java.util.ArrayList;
import java.util.List;

import com.lovetropics.minigames.client.screen.LayoutGui;
import com.lovetropics.minigames.client.screen.LayoutTree;
import com.lovetropics.minigames.client.screen.flex.Box;
import com.lovetropics.minigames.client.screen.flex.Layout;
import com.lovetropics.minigames.common.core.game.behavior.config.ConfigData;
import com.lovetropics.minigames.common.core.game.behavior.config.ConfigData.ListConfigData;
import com.lovetropics.minigames.common.core.game.behavior.config.ConfigData.SimpleConfigData;
import com.lovetropics.minigames.common.core.game.behavior.config.ConfigType;
import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.client.gui.IGuiEventListener;
import net.minecraft.client.gui.INestedGuiEventHandler;
import net.minecraft.client.gui.IRenderable;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.fml.client.gui.widget.ExtendedButton;

public class ListConfigWidget extends LayoutGui implements IConfigWidget {

	private final GameConfig parent;
	private final ListConfigData config;
	private final Layout btnLayout;

	public ListConfigWidget(GameConfig parent, LayoutTree ltree, ListConfigData config) {
		super();
		this.parent = parent;
		this.config = config;

		if (config.componentType() == ConfigType.COMPOSITE) {
			for (Object val : config.value()) {
				ltree.child(new Box(5, 0, 0, 0), new Box(3, 3, 3, 3));
				children.add(parent.createWidget(ltree, (ConfigData) val));
			}
		} else {
			for (Object val : config.value()) {
				ltree.child(new Box(5, 0, 0, 0), new Box(3, 3, 3, 3));
				children.add(parent.createWidget(ltree, new ConfigData.SimpleConfigData(config.componentType(), val)));
			}
		}
		int width = ltree.head().content().width();
		btnLayout = ltree.definiteChild(10, 10, new Box(width - 10, 0, 0, 0), new Box()).pop();
		this.children.add(new ExtendedButton(btnLayout.content().left(), btnLayout.content().top(), 10, 10, new StringTextComponent("+"), b -> addDefault()));
		this.mainLayout = ltree.pop();
	}

	private final List<IGuiEventListener> children = new ArrayList<>();
	
	public static ListConfigWidget from(GameConfig parent, LayoutTree ltree, ListConfigData data) {
		return new ListConfigWidget(parent, ltree, data);
	}

	private void addDefault() {
		this.config.addDefault();
		this.parent.reflow();
	}

	@Override
	public List<? extends IGuiEventListener> getEventListeners() {
		return children;
	}

	@Override
	public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
		super.render(matrixStack, mouseX, mouseY, partialTicks);
		btnLayout.debugRender(matrixStack);
		for (IGuiEventListener child : children) {
			if (child instanceof IRenderable) {
				((IRenderable)child).render(matrixStack, mouseX, mouseY, partialTicks);
			}
		}
	}

	@Override
	public int getHeight() {
		return this.mainLayout.margin().height();
	}
}
