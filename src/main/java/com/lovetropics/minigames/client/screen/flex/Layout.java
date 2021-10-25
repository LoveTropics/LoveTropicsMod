package com.lovetropics.minigames.client.screen.flex;

import java.util.Objects;

import com.lovetropics.minigames.LoveTropics;
import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.client.gui.AbstractGui;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.loading.FMLEnvironment;

public final class Layout extends AbstractGui {
	private final Box content;
	private final Box padding;
	private final Box margin;

	public Layout(Box content, Box padding, Box margin) {
		this.content = content;
		this.padding = padding;
		this.margin = margin;
	}

	public Box content() {
		return this.content;
	}

	public Box padding() {
		return this.padding;
	}

	public Box margin() {
		return this.margin;
	}

	public Box background() {
		return this.padding;
	}

	public Layout clip(Box bounds) {
		Box origArea = margin();
		Box newArea = origArea.intersect(bounds);
		int dl = newArea.left() - origArea.left();
		int dr = newArea.right() - origArea.right();
		int dt = newArea.top() - origArea.top();
		int db = newArea.bottom() - origArea.bottom();
		return new Layout(content().grow(-dl, -dt, dr, db), padding().grow(-dl, -dt, dr, db), newArea);
	}

	public Layout shrinkTo(Box bounds) {
		Box origArea = content();
		Box newArea = bounds;
		int dl = newArea.left() - origArea.left();
		int dr = newArea.right() - origArea.right();
		int dt = newArea.top() - origArea.top();
		int db = newArea.bottom() - origArea.bottom();
		return new Layout(newArea, padding().grow(-dl, -dt, dr, db), margin().grow(-dl, -dt, dr, db));
	}

	public Layout moveY(int y) {
		int dy = y - margin().top();
		return new Layout(content().shift(0, dy), padding().shift(0, dy), margin().shift(0, dy));
	}

	public Layout unboundedY() {
		int unbounded = 1_000_000;
		return new Layout(content().grow(0, 0, 0, unbounded), padding().grow(0, 0, 0, unbounded), margin().grow(0, 0, 0, unbounded));
	}

	@Override
	public String toString() {
		return "Layout [content=" + content + ", padding=" + padding + ", margin=" + margin + "]";
	}

	@Override
	public int hashCode() {
		return Objects.hash(content, margin, padding);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Layout other = (Layout) obj;
		return Objects.equals(content, other.content) && Objects.equals(margin, other.margin)
				&& Objects.equals(padding, other.padding);
	}

	private static final boolean inDev = !FMLEnvironment.naming.equals("srg");
	public void debugRender(MatrixStack mStack) {
		if (!inDev) return;
		vLine(mStack, margin().left(), margin().top(), margin().bottom() - 1, 0xFFF9CC9D);
		vLine(mStack, margin().right() - 1, margin().top(), margin().bottom() - 1, 0xFFF9CC9D);
		hLine(mStack, margin().left(), margin().right() - 1, margin().top(), 0xFFF9CC9D);
		hLine(mStack, margin().left(), margin().right() - 1, margin().bottom() - 1, 0xFFF9CC9D);
		
		vLine(mStack, padding().left(), padding().top(), padding().bottom() - 1, 0xFFC3D08B);
		vLine(mStack, padding().right() - 1, padding().top(), padding().bottom() - 1, 0xFFC3D08B);
		hLine(mStack, padding().left(), padding().right() - 1, padding().top(), 0xFFC3D08B);
		hLine(mStack, padding().left(), padding().right() - 1, padding().bottom() - 1, 0xFFC3D08B);
		
		vLine(mStack, content().left(), content().top(), content().bottom() - 1, 0xFF8CB6C0);
		vLine(mStack, content().right() - 1, content().top(), content().bottom() - 1, 0xFF8CB6C0);
		hLine(mStack, content().left(), content().right() - 1, content().top(), 0xFF8CB6C0);
		hLine(mStack, content().left(), content().right() - 1, content().bottom() - 1, 0xFF8CB6C0);
	}
}
