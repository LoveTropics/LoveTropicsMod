package com.lovetropics.minigames.client.lobby.manage.screen;

import com.lovetropics.minigames.client.screen.flex.*;
import net.minecraft.client.gui.screen.Screen;

final class ManageLobbyLayout {
	static final int PADDING = 8;
	static final int FOOTER_HEIGHT = 20;

	final Layout header;

	final Layout leftColumn;
	final Layout leftFooter;

	final Layout gameList;

	final Layout centerColumn;
	final Layout centerFooter;

	final Layout centerHeader;
	final Layout edit;

	final Layout play;
	final Layout skip;

	final Layout rightColumn;
	final Layout rightFooter;

	final Layout properties;
	final Layout name;
	final Layout playerList;

	final Layout done;

	final Layout[] marginals;

	ManageLobbyLayout(Screen screen) {
		int fontHeight = screen.getMinecraft().fontRenderer.FONT_HEIGHT;

		Flex root = new Flex().columns();

		Flex header = root.child().rows()
				.width(1.0F, Flex.Unit.PERCENT).height(fontHeight).padding(PADDING)
				.alignMain(Align.Main.START);

		Flex body = root.child().rows()
				.width(1.0F, Flex.Unit.PERCENT).grow(1.0F);

		Flex leftColumn = body.child()
				.size(0.25F, 1.0F, Flex.Unit.PERCENT)
				.alignMain(Align.Main.START);

		Flex gameList = leftColumn.child()
				.width(1.0F, Flex.Unit.PERCENT).grow(1.0F)
				.alignMain(Align.Main.START);

		Flex leftFooter = leftColumn.child()
				.width(1.0F, Flex.Unit.PERCENT).height(FOOTER_HEIGHT).padding(PADDING)
				.alignMain(Align.Main.END);

		Flex centerColumn = body.child().columns()
				.height(1.0F, Flex.Unit.PERCENT).grow(1.0F);

		Flex centerHeader = centerColumn.child()
				.width(1.0F, Flex.Unit.PERCENT).height(fontHeight).padding(3)
				.alignMain(Align.Main.START);

		Flex edit = centerColumn.child()
				.width(1.0F, Flex.Unit.PERCENT).grow(1.0F)
				.padding(PADDING);

		Flex centerFooter = centerColumn.child().columns()
				.width(1.0F, Flex.Unit.PERCENT).height(FOOTER_HEIGHT).padding(PADDING)
				.alignMain(Align.Main.END);

		Flex controls = centerFooter.child().rows()
				.alignCross(Align.Cross.CENTER);

		Flex play = controls.child().size(20, 20).margin(2, 0);
		Flex stop = controls.child().size(20, 20).margin(2, 0);

		Flex rightColumn = body.child().columns()
				.size(0.25F, 1.0F, Flex.Unit.PERCENT)
				.alignMain(Align.Main.END);

		Flex properties = rightColumn.child().columns()
				.width(1.0F, Flex.Unit.PERCENT).grow(1.0F).padding(PADDING);

		Flex name = properties.child()
				.width(1.0F, Flex.Unit.PERCENT).height(20)
				.margin(2).marginTop(fontHeight);

		Flex playerList = properties.child()
				.width(1.0F, Flex.Unit.PERCENT).grow(1.0F)
				.marginTop(PADDING);

		Flex rightFooter = rightColumn.child()
				.width(1.0F, Flex.Unit.PERCENT).height(FOOTER_HEIGHT).padding(PADDING)
				.alignMain(Align.Main.END);

		Flex done = rightFooter.child()
				.width(1.0F, Flex.Unit.PERCENT).height(20)
				.alignMain(Align.Main.END);

		FlexSolver.Results solve = new FlexSolver(new Box(screen)).apply(root);

		this.header = solve.layout(header);

		this.leftColumn = solve.layout(leftColumn);
		this.leftFooter = solve.layout(leftFooter);
		this.gameList = solve.layout(gameList);

		this.centerColumn = solve.layout(centerColumn);
		this.centerHeader = solve.layout(centerHeader);
		this.centerFooter = solve.layout(centerFooter);
		this.edit = solve.layout(edit);

		this.play = solve.layout(play);
		this.skip = solve.layout(stop);

		this.rightColumn = solve.layout(rightColumn);
		this.rightFooter = solve.layout(rightFooter);
		this.properties = solve.layout(properties);
		this.name = solve.layout(name);
		this.playerList = solve.layout(playerList);
		this.done = solve.layout(done);

		this.marginals = new Layout[] { this.header, this.leftFooter, this.centerFooter, this.rightFooter };
	}
}
