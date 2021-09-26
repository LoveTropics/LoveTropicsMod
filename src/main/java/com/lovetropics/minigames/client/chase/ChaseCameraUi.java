package com.lovetropics.minigames.client.chase;

import com.lovetropics.minigames.Constants;
import com.lovetropics.minigames.client.screen.PlayerFaces;
import com.mojang.authlib.GameProfile;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MainWindow;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.util.InputMappings;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;

@Mod.EventBusSubscriber(modid = Constants.MODID, value = Dist.CLIENT)
public final class ChaseCameraUi {
	private static final Minecraft CLIENT = Minecraft.getInstance();

	private static final int ENTRY_PADDING = 2;
	private static final int ENTRY_SIZE = 12 + ENTRY_PADDING;

	private final ChaseCameraSession session;
	private List<Entry> entries;

	private int selectedEntryIndex;
	private int highlightedEntryIndex;

	private double accumulatedScroll;

	ChaseCameraUi(ChaseCameraSession session) {
		this.session = session;
		this.entries = createEntriesFor(session.players);
	}

	@SubscribeEvent
	public static void onMouseScroll(InputEvent.MouseScrollEvent event) {
		ChaseCameraSession session = ChaseCameraManager.session;
		if (session == null) {
			return;
		}

		double delta = event.getScrollDelta();

		boolean zoom = InputMappings.isKeyDown(CLIENT.getMainWindow().getHandle(), GLFW.GLFW_KEY_LEFT_CONTROL);
		if (zoom) {
			session.ui.onScrollZoom(delta);
		} else {
			session.ui.onScrollSelection(delta);
		}
	}

	private void onScrollZoom(double delta) {
		session.targetZoom = MathHelper.clamp(session.targetZoom - delta * 0.05, 0.0, 1.0);
	}

	private void onScrollSelection(double delta) {
		if (accumulatedScroll != 0.0 && Math.signum(delta) != Math.signum(accumulatedScroll)) {
			accumulatedScroll = 0.0;
		}

		accumulatedScroll += delta;

		int scrollAmount = (int) accumulatedScroll;
		if (scrollAmount != 0) {
			scrollSelection(-MathHelper.clamp(scrollAmount, -1, 1));
		}
	}

	@SubscribeEvent
	public static void onKeyInput(InputEvent.KeyInputEvent event) {
		ChaseCameraSession session = ChaseCameraManager.session;
		if (session == null || event.getAction() == GLFW.GLFW_RELEASE) {
			return;
		}

		int key = event.getKey();

		if (key == GLFW.GLFW_KEY_DOWN) {
			session.ui.scrollSelection(1);
		} else if (key == GLFW.GLFW_KEY_UP) {
			session.ui.scrollSelection(-1);
		} else if (key == GLFW.GLFW_KEY_RIGHT || key == GLFW.GLFW_KEY_ENTER || key == GLFW.GLFW_KEY_KP_ENTER) {
			session.ui.selectEntry(session.ui.highlightedEntryIndex);
		}
	}

	@SubscribeEvent
	public static void onMouseInput(InputEvent.MouseInputEvent event) {
		ChaseCameraSession session = ChaseCameraManager.session;
		if (session == null || event.getAction() == GLFW.GLFW_RELEASE) {
			return;
		}

		int key = event.getButton();
		if (key == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
			session.ui.selectEntry(session.ui.highlightedEntryIndex);
		}
	}

	private void scrollSelection(int shift) {
		int newIndex = highlightedEntryIndex + shift;
		newIndex = MathHelper.clamp(newIndex, 0, entries.size() - 1);

		highlightedEntryIndex = newIndex;
	}

	private void selectEntry(int index) {
		Entry entry = entries.get(index);

		session.applyState(entry.selectionState);

		selectedEntryIndex = index;
		highlightedEntryIndex = index;
	}

	@SubscribeEvent
	public static void onRenderOverlay(RenderGameOverlayEvent.Post event) {
		ChaseCameraSession session = ChaseCameraManager.session;
		if (session == null) {
			return;
		}

		if (event.getType() == RenderGameOverlayEvent.ElementType.ALL) {
			MainWindow window = event.getWindow();
			session.ui.renderChasePlayerList(event.getMatrixStack(), window);
		}
	}

	private void renderChasePlayerList(MatrixStack transform, MainWindow window) {
		RenderSystem.enableAlphaTest();
		RenderSystem.enableBlend();
		RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);

		int listHeight = entries.size() * ENTRY_SIZE;

		int minY = (window.getScaledHeight() - listHeight) / 2;

		int x = ENTRY_PADDING;
		int y = minY;

		RenderSystem.disableTexture();

		drawSelection(transform, x, minY, selectedEntryIndex, 0x80000000);

		boolean highlighting = highlightedEntryIndex != selectedEntryIndex;
		if (highlighting) {
			drawSelection(transform, x, minY, highlightedEntryIndex, 0xA0A0A0A0);
		}

		RenderSystem.enableTexture();

		if (highlighting) {
			CLIENT.fontRenderer.drawStringWithShadow(transform, "Click or press ENTER to select", x, minY - CLIENT.fontRenderer.FONT_HEIGHT - 2, 0xFFFFFFFF);
		}

		for (Entry entry : entries) {
			entry.render(transform, session, x, y);
			y += ENTRY_SIZE;
		}
	}

	void drawSelection(MatrixStack transform, int minX, int minY, int selectedEntryIndex, int color) {
		int selectionWidth = minX + entries.get(selectedEntryIndex).getWidth(session);
		int selectionY = minY + selectedEntryIndex * ENTRY_SIZE;
		AbstractGui.fill(transform, 0, selectionY - 1, selectionWidth + 1, selectionY + ENTRY_SIZE - 1, color);
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
	}

	void updateState(ChaseCameraState state) {
		int index = getSelectedEntryIndex(state);
		if (index != -1) {
			selectedEntryIndex = index;
		} else {
			selectEntry(0);
		}
	}

	private int getSelectedEntryIndex(ChaseCameraState state) {
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
		entries.add(new Entry(CLIENT.player.getUniqueID(), s -> "Free Camera", TextFormatting.RESET, ChaseCameraState.FREE_CAMERA));

		players.sort(Comparator.comparing(player -> {
			ScorePlayerTeam team = getTeamFor(player);
			return team != null ? team.getName() : "";
		}));

		for (UUID player : players) {
			Function<ChaseCameraSession, String> nameFunction = s -> {
				GameProfile profile = s.getPlayerProfile(player);
				return profile != null ? profile.getName() : "...";
			};

			ScorePlayerTeam team = getTeamFor(player);
			TextFormatting color = team != null ? team.getColor() : TextFormatting.RESET;

			entries.add(new Entry(player, nameFunction, color, new ChaseCameraState.SelectedPlayer(player)));
		}

		return entries;
	}

	@Nullable
	private static ScorePlayerTeam getTeamFor(UUID playerId) {
		ClientWorld world = CLIENT.world;
		if (world == null) {
			return null;
		}

		PlayerEntity player = world.getPlayerByUuid(playerId);
		if (player != null) {
			return world.getScoreboard().getPlayersTeam(player.getScoreboardName());
		} else {
			return null;
		}
	}

	static class Entry {
		final UUID playerIcon;
		final Function<ChaseCameraSession, String> nameFunction;
		final int color;
		final ChaseCameraState selectionState;

		Entry(
				UUID playerIcon,
				Function<ChaseCameraSession, String> name,
				TextFormatting color,
				ChaseCameraState selectionState
		) {
			this.playerIcon = playerIcon;
			this.nameFunction = name;
			this.color = color.getColor() != null ? color.getColor() | (0xFF << 24) : 0xFFFFFFFF;
			this.selectionState = selectionState;
		}

		void render(MatrixStack transform, ChaseCameraSession session, int x, int y) {
			PlayerFaces.render(playerIcon, transform, x, y, 12);

			String name = nameFunction.apply(session);
			CLIENT.fontRenderer.drawString(transform, name, x + ENTRY_SIZE, y + ENTRY_PADDING, color);
		}

		int getWidth(ChaseCameraSession session) {
			String name = nameFunction.apply(session);
			return ENTRY_SIZE + CLIENT.fontRenderer.getStringWidth(name);
		}
	}
}
