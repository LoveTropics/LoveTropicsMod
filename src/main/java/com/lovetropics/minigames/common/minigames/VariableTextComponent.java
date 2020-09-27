package com.lovetropics.minigames.common.minigames;

import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponent;

import java.util.List;
import java.util.Map;

public class VariableTextComponent extends TextComponent
{
	private final ITextComponent decorate;
	private final Map<String, String> variableToText;
	private final String text;

	public VariableTextComponent(final ITextComponent decorate, final Map<String, String> variableToText) {
		this.decorate = decorate;
		this.variableToText = variableToText;

		String unformatted = decorate.getUnformattedComponentText();
		for (Map.Entry<String, String> entry : variableToText.entrySet()) {
			final String variable = entry.getKey();
			final String text = entry.getValue();

			unformatted = unformatted.replace(variable, text);
		}

		this.text = unformatted;
	}

	@Override
	public List<ITextComponent> getSiblings() {
		return decorate.getSiblings();
	}

	@Override
	public ITextComponent setStyle(Style style) {
		return decorate.setStyle(style);
	}

	@Override
	public Style getStyle() {
		return decorate.getStyle();
	}

	@Override
	public boolean equals(Object p_equals_1_) {
		if (this == p_equals_1_) {
			return true;
		} else if (!(p_equals_1_ instanceof VariableTextComponent)) {
			return false;
		} else {
			VariableTextComponent stringtextcomponent = (VariableTextComponent)p_equals_1_;
			return this.text.equals(stringtextcomponent.text) && super.equals(p_equals_1_);
		}
	}

	@Override
	public String toString() {
		return "TextComponent{text='" + text + '\'' + ", siblings=" + this.siblings + ", style=" + this.getStyle() + '}';
	}

	@Override
	public String getUnformattedComponentText()
	{
		return text;
	}

	@Override
	public ITextComponent shallowCopy()
	{
		return new VariableTextComponent(decorate, variableToText);
	}
}
