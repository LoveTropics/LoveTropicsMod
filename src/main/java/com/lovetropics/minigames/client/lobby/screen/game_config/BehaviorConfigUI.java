package com.lovetropics.minigames.client.lobby.screen.game_config;

import com.google.common.collect.Lists;
import com.lovetropics.minigames.client.lobby.state.ClientConfigList;
import com.lovetropics.minigames.client.screen.LayoutTree;
import com.lovetropics.minigames.client.screen.flex.Align;
import com.lovetropics.minigames.client.screen.flex.Axis;
import com.lovetropics.minigames.common.core.game.behavior.GameBehaviorType;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.components.events.AbstractContainerEventHandler;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.components.Widget;
import net.minecraft.network.chat.TranslatableComponent;

import java.util.List;

public class BehaviorConfigUI extends AbstractContainerEventHandler implements Widget {
	
	private final GameBehaviorType<?> behavior;
	private final ClientConfigList configs;
	
	private final TextLabel title;
	private final BehaviorConfigList list;

	public BehaviorConfigUI(GameConfig parent, LayoutTree ltree, GameBehaviorType<?> behavior, ClientConfigList configs) {
		super();
		this.behavior = behavior;
		this.configs = configs;
		
		this.title = new TextLabel(ltree.child(1, Axis.X), 10, new TranslatableComponent(behavior.getRegistryName().toString()), Align.Cross.CENTER, Align.Cross.START);
		this.list = new BehaviorConfigList(parent, ltree.child(), configs);
		ltree.pop(); // This UI doesn't have a layout, but we still need to pop our layout from the tree
	}

	@Override
	public List<? extends GuiEventListener> children() {
		return Lists.newArrayList(list);
	}
	
	@Override
	public void render(PoseStack matrixStack, int mouseX, int mouseY, float partialTicks) {
		title.render(matrixStack, mouseX, mouseY, partialTicks);
		list.render(matrixStack, mouseX, mouseY, partialTicks);
	}

	public int getHeight() {
		return list.getHeight();
	}
}
