package com.lovetropics.minigames.client.lobby.screen.game_config;

import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.math.NumberUtils;

import com.lovetropics.minigames.common.core.game.behavior.config.ConfigData.SimpleConfigData;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FocusableGui;
import net.minecraft.client.gui.IGuiEventListener;
import net.minecraft.client.gui.INestedGuiEventHandler;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.util.Util;
import net.minecraft.util.text.StringTextComponent;

public abstract class SimpleConfigWidget extends FocusableGui {
	
	private final SimpleConfigData config;
	private final Widget control;
	
	public static INestedGuiEventHandler from(SimpleConfigData data) {
		switch (data.type()) {
		case BOOLEAN:
			return new BooleanConfigWidget(data);
		case NUMBER:
			return new NumericConfigWidget(data);
		case STRING:
			return new StringConfigWidget(data);
		case ENUM:
			return new EnumConfigWidget(data);
		default:
			throw new IllegalArgumentException("Invalid config type " + data.type() + " for simple config widget");	
		}
	}
	
	protected SimpleConfigWidget(SimpleConfigData config, Widget control) {
		this.config = config;
		this.control = control;
	}
	
	@Override
	public List<? extends IGuiEventListener> getEventListeners() {
		return Collections.singletonList(control);
	}

	private static final class BooleanConfigWidget extends SimpleConfigWidget {

		BooleanConfigWidget(SimpleConfigData config) {
			super(config, Util.make(new TextFieldWidget(Minecraft.getInstance().fontRenderer, 0, 0, 200, 20, new StringTextComponent("")), w -> {
				w.setText(config.value().toString());
			}));
		}
	}
	
	private static final class NumericConfigWidget extends SimpleConfigWidget {

		NumericConfigWidget(SimpleConfigData config) {
			super(config, Util.make(new TextFieldWidget(Minecraft.getInstance().fontRenderer, 0, 0, 200, 20, new StringTextComponent("")), w -> {
				w.setText(config.value().toString());
				w.setValidator(NumberUtils::isCreatable);
			}));
		}
	}
	
	private static final class StringConfigWidget extends SimpleConfigWidget {

		StringConfigWidget(SimpleConfigData config) {
			super(config, Util.make(new TextFieldWidget(Minecraft.getInstance().fontRenderer, 0, 0, 200, 20, new StringTextComponent("")), w -> {
				w.setText(config.value().toString());
			}));
		}
	}
	
	private static final class EnumConfigWidget extends SimpleConfigWidget {

		EnumConfigWidget(SimpleConfigData config) {
			super(config, Util.make(new TextFieldWidget(Minecraft.getInstance().fontRenderer, 0, 0, 200, 20, new StringTextComponent("")), w -> {
				w.setText(config.value().toString());
			}));
		}
	}
}
