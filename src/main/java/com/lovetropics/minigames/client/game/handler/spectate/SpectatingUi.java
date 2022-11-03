package com.lovetropics.minigames.client.game.handler.spectate;

import com.lovetropics.minigames.Constants;
import com.lovetropics.minigames.client.screen.ClientPlayerInfo;
import com.lovetropics.minigames.client.screen.PlayerFaces;
import com.mojang.authlib.GameProfile;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;

import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextColor;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.util.Mth;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;

@Mod.EventBusSubscriber(modid = Constants.MODID, value = Dist.CLIENT)
public final class SpectatingUi {
	private static final Minecraft CLIENT = Minecraft.getInstance();

	private static final Component FREE_CAMERA_TEXT = new TextComponent("Free Camera").withStyle(ChatFormatting.ITALIC);
	private static final Component SELECT_PROMPT_TEXT = new TextComponent(" [Click to select]").withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC);

	private static final int FACE_SIZE = 16;
	private static final int ENTRY_PADDING = 2;
	private static final int ENTRY_WIDTH = FACE_SIZE + ENTRY_PADDING * 2;
	private static final int ENTRY_TAG_HEIGHT = 2;
	private static final int ENTRY_HEIGHT = ENTRY_WIDTH + ENTRY_TAG_HEIGHT;
	private static final int HIGHLIGHTED_ENTRY_HEIGHT = ENTRY_HEIGHT + ENTRY_TAG_HEIGHT;

	private static final int MAX_ENTRIES_ON_SCREEN = 16;

	private final SpectatingSession session;
	private List<Entry> entries;
	private Object2ObjectMap<UUID, PlayerEvent> events;

	private int selectedEntryIndex;
	private int highlightedEntryIndex;

	private int scrollViewIndex;

	private double accumulatedScroll;

	SpectatingUi(SpectatingSession session) {
		this.session = session;
		this.entries = createEntriesFor(session.players);
		this.events = new Object2ObjectOpenHashMap<>();
	}

	@SubscribeEvent
	public static void onMouseScroll(InputEvent.MouseScrollEvent event) {
		SpectatingSession session = ClientSpectatingManager.INSTANCE.session;
		if (session == null || CLIENT.screen != null) {
			return;
		}

		if (InputConstants.isKeyDown(CLIENT.getWindow().getWindow(), InputConstants.KEY_LSHIFT) && InputConstants.isKeyDown(CLIENT.getWindow().getWindow(), InputConstants.KEY_LCONTROL)) {
			return;
		}

		double delta = event.getScrollDelta();

		boolean zoom = InputConstants.isKeyDown(CLIENT.getWindow().getWindow(), InputConstants.KEY_LCONTROL);
		if (zoom) {
			session.ui.onScrollZoom(delta);
		} else {
			session.ui.onScrollSelection(delta);
		}

		// Prevent adjusting the spectator fly speed
		event.setCanceled(true);
	}

	private void onScrollZoom(double delta) {
		session.targetZoom = Mth.clamp(session.targetZoom - delta * 0.05, 0.0, 1.0);
	}

	private void onScrollSelection(double delta) {
		if (accumulatedScroll != 0.0 && Math.signum(delta) != Math.signum(accumulatedScroll)) {
			accumulatedScroll = 0.0;
		}

		accumulatedScroll += delta;

		int scrollAmount = (int) accumulatedScroll;
		if (scrollAmount != 0) {
			scrollSelection(-Mth.clamp(scrollAmount, -1, 1));
		}
	}

	@SubscribeEvent
	public static void onKeyInput(InputEvent.KeyInputEvent event) {
		SpectatingSession session = ClientSpectatingManager.INSTANCE.session;
		if (session == null || CLIENT.screen != null || event.getAction() == GLFW.GLFW_RELEASE) {
			return;
		}

		int key = event.getKey();

		if (key == GLFW.GLFW_KEY_LEFT) {
			session.ui.scrollSelection(-1);
		} else if (key == GLFW.GLFW_KEY_RIGHT) {
			session.ui.scrollSelection(1);
		} else if (key == GLFW.GLFW_KEY_UP || key == GLFW.GLFW_KEY_ENTER || key == GLFW.GLFW_KEY_KP_ENTER) {
			session.ui.selectEntry(session.ui.highlightedEntryIndex);
		}
	}

	@SubscribeEvent
	public static void onMouseInput(InputEvent.MouseInputEvent event) {
		SpectatingSession session = ClientSpectatingManager.INSTANCE.session;
		if (session == null || CLIENT.screen != null || event.getAction() == GLFW.GLFW_RELEASE) {
			return;
		}

		int key = event.getButton();
		if (key == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
			session.ui.selectEntry(session.ui.highlightedEntryIndex);
		}
	}

	private void scrollSelection(int shift) {
		int newIndex = highlightedEntryIndex + shift;
		newIndex = Mth.clamp(newIndex, 0, entries.size() - 1);

		highlightedEntryIndex = newIndex;
		scrollTo(newIndex);
	}

	private void selectEntry(int index) {
		Entry entry = entries.get(index);

		session.applyState(entry.selectionState);

		selectedEntryIndex = index;
		highlightedEntryIndex = index;
		scrollTo(index);
	}

	private void scrollTo(int index) {
		int scrollViewStart = scrollViewStart();
		int scrollViewEnd = scrollViewEnd();
		if (index < scrollViewStart) {
			scrollViewIndex += index - scrollViewStart;
		} else if (index > scrollViewEnd) {
			scrollViewIndex += index - scrollViewEnd;
		}
	}

	private int scrollViewStart() {
		return scrollViewIndex;
	}

	private int scrollViewEnd() {
		return scrollViewIndex + scrollViewSize() - 1;
	}

	private int scrollViewSize() {
		int padding = ENTRY_WIDTH * 4;
		int availableWidth = CLIENT.getWindow().getGuiScaledWidth() - padding;
		return Mth.clamp(availableWidth / ENTRY_WIDTH, 1, MAX_ENTRIES_ON_SCREEN);
	}

	@SubscribeEvent
	public static void onRenderOverlay(RenderGameOverlayEvent.Post event) {
		SpectatingSession session = ClientSpectatingManager.INSTANCE.session;
		if (session == null) {
			return;
		}

		if (event.getType() == RenderGameOverlayEvent.ElementType.ALL) {
			Window window = event.getWindow();
			session.ui.renderChasePlayerList(event.getMatrixStack(), window);
		}
	}

	private void renderChasePlayerList(PoseStack transform, Window window) {
		RenderSystem.enableBlend();
		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

		int viewStart = scrollViewStart();
		int viewEnd = scrollViewEnd();
		int width = Math.min(entries.size(), scrollViewSize()) * ENTRY_WIDTH;
		int left = (window.getGuiScaledWidth() - width) / 2;
		int right = left + width;
		int bottom = window.getGuiScaledHeight();

		Font font = CLIENT.font;
		int textY = bottom - (ENTRY_HEIGHT + font.lineHeight) / 2;
		if (viewStart > 0) {
			font.drawShadow(transform, "<", left - font.width("<") - 2, textY, 0xffffffff);
		}
		if (viewEnd < entries.size() - 1) {
			font.drawShadow(transform, ">", right + 2, textY, 0xffffffff);
		}

		int x = left;
		for (int i = viewStart; i <= viewEnd && i < entries.size(); i++) {
			boolean selected = i == selectedEntryIndex;
			boolean highlighted = i == highlightedEntryIndex;
			Entry entry = entries.get(i);
			entry.render(transform, x, bottom, selected, highlighted, events.get(entry.playerIcon));
			x += ENTRY_WIDTH;
		}
	}

	public void onPlayerActivity(UUID player, TextColor style) {
		events.put(player, new PlayerEvent(System.currentTimeMillis(), style));
	}

	void updatePlayers(List<UUID> players) {
		Entry selectedEntry = entries.get(selectedEntryIndex);
		Entry highlightedEntry = entries.get(highlightedEntryIndex);

		entries = createEntriesFor(players);

		int newSelectedEntry = getSelectedEntryIndex(selectedEntry.selectionState);
		newSelectedEntry = newSelectedEntry != -1 ? newSelectedEntry : 0;

		selectEntry(newSelectedEntry);

		int newHighlightedEntry = getSelectedEntryIndex(highlightedEntry.selectionState);
		highlightedEntryIndex = newHighlightedEntry != -1 ? newHighlightedEntry : selectedEntryIndex;

		updateEventsMap(players);
	}

	void updateState(SpectatingState state) {
		int index = getSelectedEntryIndex(state);
		if (index != -1) {
			selectedEntryIndex = index;
		} else {
			selectEntry(0);
		}
	}

	private int getSelectedEntryIndex(SpectatingState state) {
		for (int i = 0; i < entries.size(); i++) {
			Entry entry = entries.get(i);
			if (entry.selectionState.equals(state)) {
				return i;
			}
		}

		return -1;
	}

	List<Entry> createEntriesFor(List<UUID> players) {
		List<Entry> entries = new ArrayList<>(players.size() + 1);
		entries.add(new Entry(CLIENT.player.getUUID(), () -> FREE_CAMERA_TEXT, ChatFormatting.RESET, SpectatingState.FREE_CAMERA));

		for (UUID player : players) {
			Supplier<Component> name = () -> {
				GameProfile profile = ClientPlayerInfo.getPlayerProfile(player);
				return profile != null ? new TextComponent(profile.getName()) : new TextComponent("...");
			};

			PlayerTeam team = getTeamFor(player);
			ChatFormatting color = team != null ? team.getColor() : ChatFormatting.RESET;

			entries.add(new Entry(player, name, color, new SpectatingState.SelectedPlayer(player)));
		}

		return entries;
	}

	void updateEventsMap(List<UUID> players) {
		Object2ObjectMap<UUID, PlayerEvent> events = new Object2ObjectOpenHashMap();
		for (var entry : this.events.entrySet()) {
			if (players.contains(entry.getKey())) {
				events.put(entry.getKey(), entry.getValue());
			}
		}
		this.events = events;
	}

	@Nullable
	private static PlayerTeam getTeamFor(UUID playerId) {
		ClientPacketListener connection = CLIENT.getConnection();
		if (connection != null) {
			PlayerInfo player = connection.getPlayerInfo(playerId);
			return player != null ? player.getTeam() : null;
		}
		return null;
	}

	record Entry(UUID playerIcon, Supplier<Component> nameSupplier, ChatFormatting tagColor, SpectatingState selectionState) {
		private static final int SELECTED_OUTLINE_COLOR = 0xffffffff;
		private static final int HIGHLIGHTED_OUTLINE_COLOR = 0xa0000000;
		private static final int TAB_COLOR = 0xff404040;

		void render(PoseStack transform, int left, int screenBottom, boolean selected, boolean highlighted, @Nullable PlayerEvent lastEvent) {
			int top = screenBottom - (highlighted ? HIGHLIGHTED_ENTRY_HEIGHT : ENTRY_HEIGHT);
			int bottom = top + ENTRY_HEIGHT;
			int right = left + ENTRY_WIDTH;

			if (highlighted || selected) {
				GuiComponent.fill(transform, left, top, right, bottom, selected ? SELECTED_OUTLINE_COLOR : HIGHLIGHTED_OUTLINE_COLOR);
			}

			int color = tagColor.getColor() != null ? tagColor.getColor() | 0xff000000 : 0xffa0a0a0;
			GuiComponent.fill(transform, left, bottom - ENTRY_TAG_HEIGHT, right, bottom, color);
			GuiComponent.fill(transform, left, bottom, right, screenBottom, TAB_COLOR);

			PlayerFaces.render(playerIcon, transform, left + ENTRY_PADDING, top + ENTRY_PADDING, FACE_SIZE);

			long now = System.currentTimeMillis();
			if (lastEvent != null && (now - lastEvent.time) <= 500) {
				double fade = Mth.lerp((now - lastEvent.time) / 500.0d, .75d, 0d);
				int fadeAlpha = (int) (fade * 255);
				int colour = lastEvent.style.getValue() | (fadeAlpha << 24);
				GuiComponent.fill(transform, left + ENTRY_PADDING, top + ENTRY_PADDING, right - ENTRY_PADDING, top + ENTRY_PADDING + FACE_SIZE, colour);
			}

			if (highlighted) {
				renderName(transform, left, top, selected);
			}
		}

		private void renderName(PoseStack transform, int left, int top, boolean selected) {
			Font font = CLIENT.font;
			Component name = nameSupplier.get();
			if (!selected) {
				name = name.copy().append(SELECT_PROMPT_TEXT);
			}

			int nameLeft = left + (ENTRY_WIDTH - font.width(name)) / 2;
			font.drawShadow(transform, name, nameLeft, top - font.lineHeight - 1, 0xffffffff);
		}
	}

	record PlayerEvent(long time, TextColor style) {
	}
}
