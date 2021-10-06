package com.lovetropics.minigames.client.lobby.screen.game_config;

import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.math.NumberUtils;

import com.lovetropics.minigames.client.screen.LayoutGui;
import com.lovetropics.minigames.client.screen.flex.Layout;
import com.lovetropics.minigames.common.core.game.behavior.config.ConfigData.SimpleConfigData;
import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.IGuiEventListener;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.util.Util;
import net.minecraft.util.text.StringTextComponent;

public abstract class SimpleConfigWidget extends LayoutGui {
	
	private final SimpleConfigData config;
	private final Widget control;
	
	public static SimpleConfigWidget from(Layout layout, SimpleConfigData data) {
		switch (data.type()) {
		case BOOLEAN:
			return new BooleanConfigWidget(layout, data);
		case NUMBER:
			return new NumericConfigWidget(layout, data);
		case STRING:
			return new StringConfigWidget(layout, data);
		case ENUM:
			return new EnumConfigWidget(layout, data);
		default:
			throw new IllegalArgumentException("Invalid config type " + data.type() + " for simple config widget");	
		}
	}
	
	protected SimpleConfigWidget(Layout layout, SimpleConfigData config, Widget control) {
		super(layout);
		this.config = config;
		this.control = control;
	}
	
	@Override
	public List<? extends IGuiEventListener> getEventListeners() {
		return Collections.singletonList(control);
	}
	
	@Override
	public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
		control.render(matrixStack, mouseX, mouseY, partialTicks);
	}

	private static final class BooleanConfigWidget extends SimpleConfigWidget {

		BooleanConfigWidget(Layout layout, SimpleConfigData config) {
			super(layout, config, Util.make(new TextFieldWidget(Minecraft.getInstance().fontRenderer, 0, 0, 200, 20, new StringTextComponent("")), w -> {
				w.setText(config.value().toString());
			}));
		}
	}
	
	private static final class NumericConfigWidget extends SimpleConfigWidget {

		NumericConfigWidget(Layout layout, SimpleConfigData config) {
			super(layout, config, Util.make(new TextFieldWidget(Minecraft.getInstance().fontRenderer, 0, 0, 200, 20, new StringTextComponent("")), w -> {
				w.setText(config.value().toString());
				w.setValidator(NumberUtils::isCreatable);
			}));
		}
	}
	
	private static final class StringConfigWidget extends SimpleConfigWidget {

		StringConfigWidget(Layout layout, SimpleConfigData config) {
			super(layout, config, Util.make(new TextFieldWidget(Minecraft.getInstance().fontRenderer, 0, 0, 200, 20, new StringTextComponent("")), w -> {
				w.setText(config.value().toString());
			}));
		}
	}
	
	private static final class EnumConfigWidget extends SimpleConfigWidget {

		EnumConfigWidget(Layout layout, SimpleConfigData config) {
			super(layout, config, Util.make(new TextFieldWidget(Minecraft.getInstance().fontRenderer, 0, 0, 200, 20, new StringTextComponent("")), w -> {
				w.setText(config.value().toString());
			}));
		}
	}
}
