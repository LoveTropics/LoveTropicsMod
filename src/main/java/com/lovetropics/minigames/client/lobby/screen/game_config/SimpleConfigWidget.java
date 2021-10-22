package com.lovetropics.minigames.client.lobby.screen.game_config;

import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.math.NumberUtils;

import com.lovetropics.minigames.client.screen.LayoutGui;
import com.lovetropics.minigames.client.screen.LayoutTree;
import com.lovetropics.minigames.client.screen.flex.FlexSolver;
import com.lovetropics.minigames.client.screen.flex.Layout;
import com.lovetropics.minigames.common.core.game.behavior.config.ConfigData.SimpleConfigData;
import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.IGuiEventListener;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.util.Util;
import net.minecraft.util.text.StringTextComponent;

public abstract class SimpleConfigWidget extends LayoutGui implements IConfigWidget {
	
	protected final SimpleConfigData config;
	private Widget control;
	
	public static SimpleConfigWidget from(LayoutTree ltree, SimpleConfigData data) {
		switch (data.type()) {
		case BOOLEAN:
			return new BooleanConfigWidget(ltree, data);
		case NUMBER:
			return new NumericConfigWidget(ltree, data);
		case STRING:
			return new StringConfigWidget(ltree, data);
		case ENUM:
			return new EnumConfigWidget(ltree, data);
		default:
			throw new IllegalArgumentException("Invalid config type " + data.type() + " for simple config widget");	
		}
	}
	
	protected SimpleConfigWidget(LayoutTree ltree, SimpleConfigData config) {
		super();
		this.config = config;
		ltree.definiteChild(-1, getHeight());
		this.control = createControl(ltree.pop());
		this.mainLayout = ltree.pop();
	}
	
	@Override
	public List<? extends IGuiEventListener> getEventListeners() {
		return Collections.singletonList(control);
	}
	
	@Override
	public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
		control.render(matrixStack, mouseX, mouseY, partialTicks);
		super.render(matrixStack, mouseX, mouseY, partialTicks);
	}
	
	protected abstract Widget createControl(Layout ltree);

	@Override
	public int getHeight() {
		return 20;
	}

	private static final class BooleanConfigWidget extends SimpleConfigWidget {

		BooleanConfigWidget(LayoutTree ltree, SimpleConfigData config) {
			super(ltree, config);
		}
		
		@Override
		protected Widget createControl(Layout mainLayout) {
			return Util.make(new TextFieldWidget(Minecraft.getInstance().fontRenderer, mainLayout.background().left(), mainLayout.background().top(), mainLayout.background().width(), mainLayout.background().height(), new StringTextComponent("")), w -> {
				w.setText(config.value().toString());
			});
		}
	}
	
	private static final class NumericConfigWidget extends SimpleConfigWidget {

		NumericConfigWidget(LayoutTree ltree, SimpleConfigData config) {
			super(ltree, config);
		}

		@Override
		protected Widget createControl(Layout mainLayout) {
			return Util.make(new TextFieldWidget(Minecraft.getInstance().fontRenderer, mainLayout.background().left(), mainLayout.background().top(), mainLayout.background().width(), mainLayout.background().height(), new StringTextComponent("")), w -> {
				w.setText(config.value().toString());
				w.setValidator(NumberUtils::isCreatable);
			});
		}
	}
	
	private static final class StringConfigWidget extends SimpleConfigWidget {

		StringConfigWidget(LayoutTree ltree, SimpleConfigData config) {
			super(ltree, config);
		}
		
		@Override
		protected Widget createControl(Layout mainLayout) {
			
			return Util.make(new TextFieldWidget(Minecraft.getInstance().fontRenderer, mainLayout.background().left(), mainLayout.background().top(), mainLayout.background().width(), mainLayout.background().height(), new StringTextComponent("")), w -> {
				w.setText(config.value().toString());
			});
		}
	}
	
	private static final class EnumConfigWidget extends SimpleConfigWidget {

		EnumConfigWidget(LayoutTree ltree, SimpleConfigData config) {
			super(ltree, config);
		}
		
		@Override
		protected Widget createControl(Layout mainLayout) {
			return Util.make(new TextFieldWidget(Minecraft.getInstance().fontRenderer, mainLayout.background().left(), mainLayout.background().top(), mainLayout.background().width(), mainLayout.background().height(), new StringTextComponent("")), w -> {
				w.setText(config.value().toString());
			});
		}
	}
}
