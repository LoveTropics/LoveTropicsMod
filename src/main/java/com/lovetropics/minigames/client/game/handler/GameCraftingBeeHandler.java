package com.lovetropics.minigames.client.game.handler;

import com.lovetropics.minigames.LoveTropics;
import com.lovetropics.minigames.client.game.ClientGameStateManager;
import com.lovetropics.minigames.common.content.crafting_bee.CraftingBeeTexts;
import com.lovetropics.minigames.common.content.crafting_bee.SelectedRecipe;
import com.lovetropics.minigames.common.core.game.client_state.GameClientStateTypes;
import com.lovetropics.minigames.common.core.game.client_state.instance.CraftingBeeCraftsClientState;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.ChatFormatting;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.CraftingScreen;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterClientTooltipComponentFactoriesEvent;
import net.neoforged.neoforge.client.event.ScreenEvent;
import org.jetbrains.annotations.Nullable;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;
import java.util.function.Predicate;

@EventBusSubscriber(modid = LoveTropics.ID, value = Dist.CLIENT)
public class GameCraftingBeeHandler {
    private static int hintsRemaining;
    private static UUID lastKnownGame;
    private static Map<ResourceLocation, RecipeHint> hintGrids;

    static final ClientGameStateHandler<CraftingBeeCraftsClientState> HANDLER = new ClientGameStateHandler<CraftingBeeCraftsClientState>() {
        @Override
        public void accept(CraftingBeeCraftsClientState state) {
            lastKnownGame = null;
        }

        @Override
        public void disable(CraftingBeeCraftsClientState state) {

        }
    };

    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath("ltminigames", "textures/gui/minigames/crafting_bee/items_bar.png");
    private static final ResourceLocation GRID_TEXTURE = ResourceLocation.fromNamespaceAndPath("ltminigames", "textures/gui/minigames/crafting_bee/crafting_grid.png");

    @EventBusSubscriber(modid = LoveTropics.ID, value = Dist.CLIENT, bus = EventBusSubscriber.Bus.MOD)
    public static class ModSubscriber {
        @SubscribeEvent
        static void onRegisterTooltips(final RegisterClientTooltipComponentFactoriesEvent event) {
            event.register(RecipeHint.class, recipeHint -> new ClientTooltipComponent() {
                @Override
                public int getHeight() {
                    return 58;
                }

                @Override
                public int getWidth(Font font) {
                    return 54;
                }

                @Override
                public void renderImage(Font font, int x, int y, GuiGraphics guiGraphics) {
                    guiGraphics.blit(GRID_TEXTURE, x, y, 0, 0, 54, 54, 54, 54);
                    for (int i = 0; i < recipeHint.grid().size(); i++) {
                        var ingredient = recipeHint.grid.get(i);
                        if (ingredient.isEmpty()) continue;

                        var size = recipeHint.grid.size() == 4 ? 2 : 3;

                        guiGraphics.renderFakeItem(
                                resolveIngredient(ingredient),
                                x + 1 + 18 * (i % size),
                                y + 1 + 18 * (i / size)
                        );
                    }
                }
            });
        }
    }

    @SubscribeEvent
    static void onGuiInit(ScreenEvent.Init.Post event) {
        if (getState() == null || !(event.getScreen() instanceof CraftingScreen screen)) return;

        var state = getState();
        if (!Objects.equals(state.gameId(), lastKnownGame)) {
            lastKnownGame = state.gameId();
            hintsRemaining = state.allowedHints();
            hintGrids = new HashMap<>();
        }

        event.addListener(new AbstractWidget(screen.getGuiLeft() + 22, screen.getGuiTop() - 21, 132, 21, Component.empty()) {
            @Override
            protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
                guiGraphics.blit(TEXTURE, this.getX(), this.getY(), 0, 0, 132, 21, 132, 21);
                var crafts = getState().crafts();
                for (int i = 0; i < crafts.size(); i++) {
                    var craft = crafts.get(i);
                    var x = this.getX() + 4 + i * 18;
                    renderItem(guiGraphics, craft.output(), x, this.getY() + 4, 0, 0, 1f, 1f, 1f, craft.done() ? .1f : 1f);

                    if (mouseX >= x && mouseX <= x + 16 && mouseY >= getY() + 4 && mouseY <= getY() + 20) {
                        var hint = hintGrids.get(craft.recipeId());

                        var tooltipLines = new ArrayList<>(Screen.getTooltipFromItem(Minecraft.getInstance(), craft.output()));
                        if (craft.done()) {
                            tooltipLines.set(0, tooltipLines.get(0).copy().withStyle(ChatFormatting.GREEN));
                        } else if (hint == null || hint.expectedIngredientCount() != hint.grid().stream().filter(Predicate.not(Ingredient::isEmpty)).count()) {
                            tooltipLines.add(CraftingBeeTexts.HINT);
                            tooltipLines.add(CraftingBeeTexts.HINTS_LEFT.apply(Component.literal(String.valueOf(hintsRemaining)).withStyle(ChatFormatting.AQUA)));
                        }
                        guiGraphics.renderTooltip(Minecraft.getInstance().font, tooltipLines, Optional.<TooltipComponent>ofNullable(hint).filter($ -> !craft.done()), mouseX, mouseY);
                    }
                }
            }

            @Override
            public void onClick(double mouseX, double mouseY, int button) {
                if (hintsRemaining <= 0) return;

                var crafts = getState().crafts();

                if (mouseY < getY() + 4 || mouseY > getY() + 4 + 16) return;
                if (mouseX < getX() + 4 || mouseX > getX() + 4 + (18 * crafts.size() - 1)) return;
                var index = (int)(mouseX - getX() - 4) / 18;

                var craft = crafts.get(index);
                if (craft.done()) return;

                var recipe = new SelectedRecipe(craft.recipeId(), Minecraft.getInstance().player.connection.getRecipeManager());
                var ingredients = recipe.decompose();

                var grid = hintGrids.computeIfAbsent(craft.recipeId(), k -> new RecipeHint(
                        NonNullList.withSize(
                                recipe.recipe().map(shaped -> shaped.getWidth() * shaped.getHeight(), shapeless -> shapeless.getIngredients().size() > 3 ? 9 : shapeless.getIngredients().size()),
                                Ingredient.EMPTY),
                        (int)ingredients.stream().filter(Predicate.not(Ingredient::isEmpty)).count()
                ));

                int filledGridAmount = (int) grid.grid().stream().filter(Predicate.not(Ingredient::isEmpty)).count();
                if (grid.expectedIngredientCount() == filledGridAmount) return;

                record PositionedIngredient(Ingredient ingredient, int position) {}

                List<PositionedIngredient> ingredientsToPick = new ArrayList<>();
                for (int i = 0; i < ingredients.size(); i++) {
                    var ingr = ingredients.get(i);
                    if (!ingr.isEmpty() && grid.grid().get(i).isEmpty()) {
                        ingredientsToPick.add(new PositionedIngredient(ingr, i));
                    }
                }

                Collections.shuffle(ingredientsToPick);
                // Make sure that we never show the full recipe in just one hint
                var ingredientsToShow = new Random().nextInt(filledGridAmount == 0 ? Math.max(1, ingredientsToPick.size() - 1) : ingredientsToPick.size());

                for (int i = 0; i <= ingredientsToShow; i++) {
                    var ingredient = ingredientsToPick.get(i);
                    grid.grid().set(ingredient.position(), ingredient.ingredient());
                }

                hintsRemaining--;
            }

            @Override
            protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {

            }
        });
    }

    @Nullable
    private static CraftingBeeCraftsClientState getState() {
        return ClientGameStateManager.getOrNull(GameClientStateTypes.CRAFTING_BEE_CRAFTS);
    }

    private static ItemStack resolveIngredient(Ingredient ingredient) {
        if (ingredient.isEmpty()) {
            return ItemStack.EMPTY;
        }
        for (ItemStack item : ingredient.getItems()) {
            // Prioritize vanilla items
            if (item.getItem().builtInRegistryHolder().key().location().getNamespace().equals(ResourceLocation.DEFAULT_NAMESPACE)) {
                return item;
            }
        }
        return ingredient.getItems()[0];
    }

    public static void reset() {

    }

    private static void renderItem(
            GuiGraphics graphics, ItemStack stack, int x, int y, int seed, int guiOffset,
            float redTint, float greenTint, float blueTint, float alphaTint
    ) {
        if (!stack.isEmpty()) {
            RenderSystem.enableBlend();
            RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

            BakedModel bakedmodel = Minecraft.getInstance().getItemRenderer().getModel(stack, null, null, seed);
            graphics.pose().pushPose();
            graphics.pose().translate((float) (x + 8), (float) (y + 8), (float) (150 + (bakedmodel.isGui3d() ? guiOffset : 0)));

            try {
                graphics.pose().scale(16.0F, -16.0F, 16.0F);
                RenderSystem.applyModelViewMatrix();

                boolean flag = !bakedmodel.usesBlockLight();
                if (flag) {
                    Lighting.setupForFlatItems();
                }

                Minecraft.getInstance()
                        .getItemRenderer()
                        .render(stack, ItemDisplayContext.GUI, false, graphics.pose(), renderType -> {
                            if (renderType instanceof RenderType.CompositeRenderType composite) {
                                if (composite.state().textureState instanceof RenderStateShard.TextureStateShard texture && texture.texture.isPresent()) {
                                    return new TintedVertexConsumer(
                                            graphics.bufferSource().getBuffer(RenderType.entityTranslucent(texture.texture.get())), redTint, greenTint, blueTint, alphaTint
                                    );
                                }
                            }
                            return new TintedVertexConsumer(
                                    graphics.bufferSource().getBuffer(renderType), redTint, greenTint, blueTint, alphaTint
                            );
                        }, LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY, bakedmodel);
                graphics.flush();
                RenderSystem.enableDepthTest();
                if (flag) {
                    Lighting.setupFor3DItems();
                }
            } catch (Throwable throwable) {
                LoggerFactory.getLogger(GameCraftingBeeHandler.class).error("error", throwable);
            }

            graphics.pose().popPose();
            RenderSystem.applyModelViewMatrix();
        }
    }

    public static final class TintedVertexConsumer implements VertexConsumer {
        private final VertexConsumer wrapped;

        @Override
        public VertexConsumer addVertex(float x, float y, float z) {
            return wrapped.addVertex(x, y, z);
        }

        @Override
        public VertexConsumer setColor(int red, int green, int blue, int alpha) {
            return wrapped.setColor((int)(red * this.red), (int)(green * this.green), (int)(blue * this.blue), (int)(alpha * this.alpha));
        }

        @Override
        public VertexConsumer setUv(float u, float v) {
            return wrapped.setUv(u, v);
        }

        @Override
        public VertexConsumer setUv1(int u, int v) {
            return wrapped.setUv1(u, v);
        }

        @Override
        public VertexConsumer setUv2(int u, int v) {
            return wrapped.setUv2(u, v);
        }

        @Override
        public VertexConsumer setNormal(float normalX, float normalY, float normalZ) {
            return wrapped.setNormal(normalX, normalY, normalZ);
        }

        private final float red;
        private final float green;
        private final float blue;
        private final float alpha;

        public TintedVertexConsumer(VertexConsumer wrapped, float red, float green, float blue, float alpha) {
            this.wrapped = wrapped;
            this.red = red;
            this.green = green;
            this.blue = blue;
            this.alpha = alpha;
        }
    }

    public record RecipeHint(
            NonNullList<Ingredient> grid,
            int expectedIngredientCount
    ) implements TooltipComponent {}

}
