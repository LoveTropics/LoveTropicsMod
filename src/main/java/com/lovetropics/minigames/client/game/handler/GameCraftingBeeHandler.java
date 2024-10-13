package com.lovetropics.minigames.client.game.handler;

import com.lovetropics.minigames.LoveTropics;
import com.lovetropics.minigames.client.game.ClientGameStateManager;
import com.lovetropics.minigames.common.content.crafting_bee.CraftingBeeTexts;
import com.lovetropics.minigames.common.core.game.client_state.GameClientStateTypes;
import com.lovetropics.minigames.common.core.game.client_state.instance.CraftingBeeCrafts;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.ChatFormatting;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.CraftingScreen;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ScreenEvent;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;

@EventBusSubscriber(modid = LoveTropics.ID, value = Dist.CLIENT)
public class GameCraftingBeeHandler {
    // TODO - hints
    private static int hintsRemaining = 3;
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath("ltminigames", "textures/gui/minigames/crafting_bee/items_bar.png");

    @SubscribeEvent
    static void onGuiInit(ScreenEvent.Init.Post event) {
        if (getState() == null || !(event.getScreen() instanceof CraftingScreen screen)) return;

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
                        var tooltipLines = new ArrayList<>(Screen.getTooltipFromItem(Minecraft.getInstance(), craft.output()));
                        if (craft.done()) {
                            tooltipLines.set(0, tooltipLines.get(0).copy().withStyle(ChatFormatting.GREEN));
                        } else {
                            tooltipLines.add(CraftingBeeTexts.HINT);
                            tooltipLines.add(CraftingBeeTexts.HINTS_LEFT.apply(Component.literal(String.valueOf(hintsRemaining)).withStyle(ChatFormatting.AQUA)));
                        }
                        guiGraphics.renderTooltip(Minecraft.getInstance().font, tooltipLines, craft.output().getTooltipImage(), mouseX, mouseY);
                    }
                }
            }

            @Override
            protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {

            }
        });
    }

    @Nullable
    private static CraftingBeeCrafts getState() {
        return ClientGameStateManager.getOrNull(GameClientStateTypes.CRAFTING_BEE_CRAFTS);
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
                CrashReport crashreport = CrashReport.forThrowable(throwable, "Rendering item");
                CrashReportCategory crashreportcategory = crashreport.addCategory("Item being rendered");
                crashreportcategory.setDetail("Item Type", () -> String.valueOf(stack.getItem()));
                crashreportcategory.setDetail("Item Components", () -> String.valueOf(stack.getComponents()));
                crashreportcategory.setDetail("Item Foil", () -> String.valueOf(stack.hasFoil()));
                throw new ReportedException(crashreport);
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
}
