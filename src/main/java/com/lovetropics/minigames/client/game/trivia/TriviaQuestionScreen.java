package com.lovetropics.minigames.client.game.trivia;

import com.lovetropics.minigames.common.content.river_race.behaviour.TriviaBehaviour;
import com.lovetropics.minigames.common.content.river_race.block.TriviaBlockEntity;
import com.lovetropics.minigames.common.core.game.util.GameTexts;
import com.lovetropics.minigames.common.core.network.trivia.RequestTriviaStateUpdateMessage;
import com.lovetropics.minigames.common.core.network.trivia.SelectTriviaAnswerMessage;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.*;
import net.minecraft.client.gui.layouts.FrameLayout;
import net.minecraft.client.gui.layouts.GridLayout;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;

public class TriviaQuestionScreen extends Screen {

    public static class AutoUpdatingTextWidget extends AbstractStringWidget {
        private float alignX;
        private Supplier<Component> messageSupplier;

        public AutoUpdatingTextWidget(Supplier<Component> message, Font font) {
            super(0, 0, 0, 0, Component.empty(), font);
            this.messageSupplier = message;
        }

        public AutoUpdatingTextWidget setColor(int pColor) {
            super.setColor(pColor);
            return this;
        }

        private AutoUpdatingTextWidget horizontalAlignment(float pHorizontalAlignment) {
            this.alignX = pHorizontalAlignment;
            return this;
        }

        public AutoUpdatingTextWidget alignLeft() {
            return this.horizontalAlignment(0.0F);
        }

        public AutoUpdatingTextWidget alignCenter() {
            return this.horizontalAlignment(0.5F);
        }

        public AutoUpdatingTextWidget alignRight() {
            return this.horizontalAlignment(1.0F);
        }

        @Override
        protected void renderWidget(GuiGraphics guiGraphics, int i, int i1, float v) {
            Component component = messageSupplier.get();
            Font font = this.getFont();
            MultiLineLabel label = MultiLineLabel.create(font, component);
            int x = this.getX();
            int y = this.getY();
            label.renderCentered(guiGraphics, x, y, 9, getColor());
//            guiGraphics.drawString(font, component, x, totalY, this.getColor());
        }
    }

    //TODO: make this a thing that actually looks like a good design
    public static class AnswerButton extends Button {
        protected AnswerButton(Builder builder) {
            super(builder);
        }

        public void setActive(boolean active){
            this.active = active;
            if(active){
                setAlpha(1);
            } else {
                setAlpha(0.5f);
            }
        }

        @Override
        protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
            super.renderWidget(guiGraphics, mouseX, mouseY, partialTick);
        }
    }

    private TriviaBehaviour.TriviaQuestion question;
    private final BlockPos triviaBlockPos;
    private TriviaBlockEntity.TriviaBlockState triviaBlockState;
    private final GridLayout layout = new GridLayout().spacing(25);
    private String selected = null;
    private Set<AnswerButton> buttons = new HashSet<>();

    public TriviaQuestionScreen(BlockPos triviaBlockPos, TriviaBehaviour.TriviaQuestion question, TriviaBlockEntity.TriviaBlockState blockState) {
        super(Component.literal("Answer Trivia Question"));
        this.triviaBlockPos = triviaBlockPos;
        this.question = question;
        this.triviaBlockState = blockState;
    }

    @Override
    protected void init() {
        buttons.clear();
        GridLayout.RowHelper helper = layout.createRowHelper(1);
        layout.defaultCellSetting().alignHorizontallyCenter();
        helper.addChild(new AutoUpdatingTextWidget(() -> {
            if(triviaBlockState.lockedOut()) {
                return Component.literal("LOCKED OUT!\n")
                        .append(Component.literal("Unlocks in " + (triviaBlockState.unlocksAt() - System.currentTimeMillis()) / 1000 + "s"))
                        .withStyle(ChatFormatting.RED);
            } else if(triviaBlockState.correctAnswer().isPresent()){
                return Component.literal("Answered Correctly!").withStyle(ChatFormatting.GREEN);
            }
            return Component.empty();
        }, font).alignCenter(), 1);
        helper.addChild(new MultiLineTextWidget(Component.literal(question.question()), font).setCentered(true), 1);
        for (TriviaBehaviour.TriviaQuestion.TriviaQuestionAnswer answer : question.answers()) {
            AnswerButton button = new AnswerButton(Button.builder(Component.literal(answer.text()), this::handleAnswerClick));
            button.setActive(getButtonState(answer.text()));
            buttons.add(button);
            helper.addChild(button, 1);
        }
        repositionElements();
        layout.visitWidgets(this::addRenderableWidget);
    }

    private boolean getButtonState(String answerText){
        boolean isEnabled = true;
        if(triviaBlockState.lockedOut()){
            isEnabled = false;
        } else {
            if (triviaBlockState.isAnswered() && triviaBlockState.correctAnswer().isPresent()) {
                if (!triviaBlockState.correctAnswer().get().equals(answerText)) {
                    isEnabled = false;
                }
            }
        }
        return isEnabled;
    }

    @Override
    public void tick() {
        super.tick();
        if(triviaBlockState.lockedOut() && triviaBlockState.unlocksAt() <= System.currentTimeMillis()){
            PacketDistributor.sendToServer(new RequestTriviaStateUpdateMessage(triviaBlockPos));
        }
    }

    private void handleAnswerClick(Button clickedButton){
        if(triviaBlockState.isAnswered()){
            return;
        }
        String selectedAnswer = clickedButton.getMessage().getString();
        selected = selectedAnswer;
        PacketDistributor.sendToServer(new SelectTriviaAnswerMessage(triviaBlockPos, selectedAnswer));
    }

    public void handleAnswerResponse(TriviaBlockEntity.TriviaBlockState triviaBlockState){
        this.triviaBlockState = triviaBlockState;
        for (AnswerButton button : buttons) {
            button.setActive(getButtonState(button.getMessage().getString()));
        }
    }

    @Override
    protected void repositionElements() {
        layout.arrangeElements();
        FrameLayout.centerInRectangle(layout, 0, 0, width, height);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
