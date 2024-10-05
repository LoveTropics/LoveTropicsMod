package com.lovetropics.minigames.client.game.trivia;

import com.lovetropics.minigames.common.content.river_race.behaviour.TriviaBehaviour;
import com.lovetropics.minigames.common.core.network.trivia.SelectTriviaAnswerMessage;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.MultiLineTextWidget;
import net.minecraft.client.gui.layouts.FrameLayout;
import net.minecraft.client.gui.layouts.GridLayout;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.network.PacketDistributor;

public class TriviaQuestionScreen extends Screen {

    private TriviaBehaviour.TriviaQuestion question;
    private final BlockPos triviaBlockPos;
    private final GridLayout layout = new GridLayout().spacing(25);

    public TriviaQuestionScreen(BlockPos triviaBlockPos, TriviaBehaviour.TriviaQuestion question) {
        super(Component.literal("Answer Trivia Question"));
        this.triviaBlockPos = triviaBlockPos;
        this.question = question;
    }

    @Override
    protected void init() {
        GridLayout.RowHelper helper = layout.createRowHelper(1);
        layout.defaultCellSetting().alignHorizontallyCenter();
        helper.addChild(new MultiLineTextWidget(Component.literal(question.question()), font).setCentered(true), 1);

        for (TriviaBehaviour.TriviaQuestion.TriviaQuestionAnswer answer : question.answers()) {
            helper.addChild(Button.builder(Component.literal(answer.text()), this::handleAnswerClick).build(), 1);
        }
        repositionElements();
        layout.visitWidgets(this::addRenderableWidget);
    }


    private void handleAnswerClick(Button clickedButton){
        String selectedAnswer = clickedButton.getMessage().getString();
        PacketDistributor.sendToServer(new SelectTriviaAnswerMessage(triviaBlockPos, selectedAnswer));
    }

    @Override
    protected void repositionElements() {
        layout.arrangeElements();
        FrameLayout.centerInRectangle(layout, 0, 0, width, height);
    }
}
