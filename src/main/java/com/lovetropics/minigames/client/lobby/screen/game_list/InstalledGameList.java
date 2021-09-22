package com.lovetropics.minigames.client.lobby.screen.game_list;

import com.lovetropics.minigames.client.lobby.ClientGameDefinition;
import com.lovetropics.minigames.client.screen.flex.Layout;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.IntConsumer;

public final class InstalledGameList extends AbstractGameList {
	private static final ITextComponent TITLE = new StringTextComponent("Installed Games")
			.mergeStyle(TextFormatting.UNDERLINE, TextFormatting.BOLD);

	private final IntConsumer select;

	public InstalledGameList(Screen screen, Layout layout, IntConsumer select) {
		super(screen, layout, TITLE);
		this.select = select;
	}

	public void setEntries(List<ClientGameDefinition> games) {
		this.clearEntries();
		for (ClientGameDefinition entry : games) {
			this.addEntry(new GameEntry(this, entry));
		}
	}

	@Override
	public void setSelected(@Nullable Entry entry) {
		int index = this.getEventListeners().indexOf(entry);
		this.select.accept(index);

		super.setSelected(entry);
	}
}
