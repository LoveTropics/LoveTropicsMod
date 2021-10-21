package com.lovetropics.minigames.client.lobby.screen.game_config;

import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.math.NumberUtils;

import com.lovetropics.minigames.client.screen.DynamicLayoutGui;
import com.lovetropics.minigames.client.screen.flex.Flex;
import com.lovetropics.minigames.client.screen.flex.Flex.Unit;
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

public abstract class SimpleConfigWidget extends DynamicLayoutGui implements IConfigWidget {
	
	protected final SimpleConfigData config;
	private Widget control;
	
	public static SimpleConfigWidget from(Flex basis, SimpleConfigData data) {
		Flex widgetBasis = basis.child().margin(3);
		switch (data.type()) {
		case BOOLEAN:
			return new BooleanConfigWidget(widgetBasis, data);
		case NUMBER:
			return new NumericConfigWidget(widgetBasis, data);
		case STRING:
			return new StringConfigWidget(widgetBasis, data);
		case ENUM:
			return new EnumConfigWidget(widgetBasis, data);
		default:
			throw new IllegalArgumentException("Invalid config type " + data.type() + " for simple config widget");	
		}
	}
	
	protected SimpleConfigWidget(Flex basis, SimpleConfigData config) {
		super(basis);
		basis.height(20, Unit.PX).width(getHeight());
		this.config = config;
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
	
	@Override
	public void bake(FlexSolver.Results solve) {
		super.bake(solve);
		this.control = createControl(this.mainLayout);
	}
	
	protected abstract Widget createControl(Layout mainLayout);

	@Override
	public int getHeight() {
		return this.control.getHeight();
	}

	private static final class BooleanConfigWidget extends SimpleConfigWidget {

		BooleanConfigWidget(Flex basis, SimpleConfigData config) {
			super(basis, config);
		}
		
		@Override
		protected Widget createControl(Layout mainLayout) {
			return Util.make(new TextFieldWidget(Minecraft.getInstance().fontRenderer, mainLayout.background().left(), mainLayout.background().top(), mainLayout.background().width(), mainLayout.background().height(), new StringTextComponent("")), w -> {
				w.setText(config.value().toString());
			});
		}
	}
	
	private static final class NumericConfigWidget extends SimpleConfigWidget {

		NumericConfigWidget(Flex basis, SimpleConfigData config) {
			super(basis, config);
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

		StringConfigWidget(Flex basis, SimpleConfigData config) {
			super(basis, config);
		}
		
		@Override
		protected Widget createControl(Layout mainLayout) {
			
			return Util.make(new TextFieldWidget(Minecraft.getInstance().fontRenderer, mainLayout.background().left(), mainLayout.background().top(), mainLayout.background().width(), mainLayout.background().height(), new StringTextComponent("")), w -> {
				w.setText(config.value().toString());
			});
		}
	}
	
	private static final class EnumConfigWidget extends SimpleConfigWidget {

		EnumConfigWidget(Flex basis, SimpleConfigData config) {
			super(basis, config);
		}
		
		@Override
		protected Widget createControl(Layout mainLayout) {
			return Util.make(new TextFieldWidget(Minecraft.getInstance().fontRenderer, mainLayout.background().left(), mainLayout.background().top(), mainLayout.background().width(), mainLayout.background().height(), new StringTextComponent("")), w -> {
				w.setText(config.value().toString());
			});
		}
	}
}
