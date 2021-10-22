package com.lovetropics.minigames.client.screen;

import java.util.BitSet;
import java.util.LinkedList;

import com.google.common.collect.TreeTraverser;
import com.lovetropics.minigames.client.screen.flex.Axis;
import com.lovetropics.minigames.client.screen.flex.Box;
import com.lovetropics.minigames.client.screen.flex.Layout;

public class LayoutTree {

	private static class LayoutNode {
		final LayoutNode parent;
		Layout bounds;
		final LinkedList<LayoutNode> children = new LinkedList<>();
		boolean contracted = false;
		final BitSet definite = new BitSet();

		LayoutNode(LayoutNode parent, Layout self, Axis... definite) {
			this.parent = parent;
			this.bounds = self;
			for (Axis axis : definite) {
				this.definite.set(axis.ordinal());
			}
		}

		LayoutNode addChild(Layout layout, Axis... definite) {
			if (contracted) throw new IllegalArgumentException("Cannot add child to contracted node");
			LayoutNode child = new LayoutNode(this, layout, definite);
			this.children.descendingIterator().forEachRemaining(n -> {
				if (n.bounds.margin().contains(child.bounds.margin().left(), child.bounds.margin().top())) {
					child.bounds = child.bounds.moveY(n.bounds.margin().bottom());
				}
			});
			this.children.add(child);
			return child;
		}

		void fitToChildren() {
			if (contracted || definite.cardinality() == Axis.values().length) return;
			Layout orig = bounds;
			if (children.isEmpty()) {
				bounds = bounds.shrinkTo(new Box(bounds.content().left(), bounds.content().top(), bounds.content().left(), bounds.content().top()));
			} else {
				Box minContent = null;
				for (LayoutNode node : children) {
					Box childArea = node.bounds.margin();
					minContent = minContent == null ? childArea : minContent.union(childArea);
				}
				this.bounds = this.bounds.shrinkTo(minContent);
			}
			if (definite.get(Axis.X.ordinal())) {
				bounds = new Layout(new Box(orig.content().left(), bounds.content().top(), orig.content().right(), bounds.content().bottom()),
						new Box(orig.padding().left(), bounds.padding().top(), orig.padding().right(), bounds.padding().bottom()),
						new Box(orig.margin().left(), bounds.margin().top(), orig.margin().right(), bounds.margin().bottom()));
			}
			if (definite.get(Axis.Y.ordinal())) {
				bounds = new Layout(new Box(bounds.content().left(), orig.content().top(), bounds.content().right(), orig.content().bottom()),
						new Box(bounds.padding().left(), orig.padding().top(), bounds.padding().right(), orig.padding().bottom()),
						new Box(bounds.margin().left(), orig.margin().top(), bounds.margin().right(), orig.margin().bottom()));
			}
			contracted = true;
		}
	}
	
	private LayoutNode head;
	
	public LayoutTree(Layout root) {
		this.head = new LayoutNode(null, root);
	}

	public Layout head() {
		return head.bounds;
	}

	private LayoutTree child(Layout layout, Axis... definite) {
		head = head.addChild(layout, definite);
		return this;
	}

	public LayoutTree child(Box margin, Box padding) {
		return child(get(margin, padding));
	}

	private Layout get(Box area, Box margin, Box padding) {
		Box insideMargin = area.contract(margin);
		Box insidePadding = insideMargin.contract(padding);
		Layout layout = new Layout(insidePadding, insideMargin, area);
		return layout;
	}

	public Layout get(Box margin, Box padding) {
		return get(head().content(), margin, padding);
	}
	
	public LayoutTree child(int margin, int padding) {
		return child(get(margin, padding));
	}

	public Layout get(int margin, int padding) {
		return get(new Box(margin, margin, margin, margin), new Box(padding, padding, padding, padding));
	}

	public Layout get(float amount, Axis axis) {
		Box area = head().content();
		Box contract = new Box()
				.left(	(int) (axis == Axis.X && amount < 0 ? -amount * area.width() : 0))
				.right(	(int) (axis == Axis.X && amount > 0 ? (1 - amount) * area.width() : 0))
				.top(	(int) (axis == Axis.Y && amount < 0 ? -amount * area.height() : 0))
				.bottom((int) (axis == Axis.Y && amount > 0 ? (1 - amount) * area.height() : 0));
		return get(area.contract(contract), new Box(), new Box());
	}

	public LayoutTree child(float amount, Axis axis) {
		return child(get(amount, axis), axis);
	}

	public LayoutTree child() {
		return child(get());
	}

	public Layout get() {
		return get(0, 0);
	}

	public Layout pop() {
		contract();
		Layout ret = head.bounds;
		head = head.parent;
		return ret;
	}

	private void contract() {
		TreeTraverser.<LayoutNode>using(n -> n.children)
			.postOrderTraversal(head)
			.forEach(LayoutNode::fitToChildren);
	}

	public void definiteChild(int width, int height, Box margin, Box padding) {
		Box area = head().content();
		Box newMargin = area.withDimensions(width, height);
		Box newPadding = newMargin.contract(margin);
		Box newContent = newPadding.contract(padding);
		child(new Layout(newContent, newPadding, newMargin), Axis.values());
	}

	public void definiteChild(int width, int height) {
		definiteChild(width, height, new Box(), new Box());
	}
}
