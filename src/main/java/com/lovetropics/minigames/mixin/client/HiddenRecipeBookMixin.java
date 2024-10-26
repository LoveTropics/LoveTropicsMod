package com.lovetropics.minigames.mixin.client;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.lovetropics.minigames.client.game.ClientGameStateManager;
import com.lovetropics.minigames.common.core.game.client_state.GameClientStateTypes;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.client.gui.screens.inventory.CraftingScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.gui.screens.recipebook.RecipeBookComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.RecipeBookType;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({CraftingScreen.class, InventoryScreen.class})
public class HiddenRecipeBookMixin {
    @Final
    @Shadow(remap = false)
    private RecipeBookComponent recipeBookComponent;

    @Inject(method = "init", at = @At("HEAD"))
    private void hideBookIfOpen(CallbackInfo ci) {
        if (ClientGameStateManager.getOrNull(GameClientStateTypes.HIDE_RECIPE_BOOK) != null && recipeBookComponent.isVisible()) {
            Minecraft.getInstance().player.getRecipeBook().setBookSetting(RecipeBookType.CRAFTING, false, false);
        }
    }

    @WrapOperation(method = "init", at = @At(value = "NEW", target = "net/minecraft/client/gui/components/ImageButton"))
    private ImageButton respectHiddenBook(int x, int y, int width, int height, WidgetSprites sprites, Button.OnPress onPress, Operation<ImageButton> original) {
        var disabled = ResourceLocation.fromNamespaceAndPath("ltminigames", "recipe_book/button_disabled");
        var org = original.call(x, y, width, height, new WidgetSprites(
                sprites.enabled(), disabled, sprites.enabledFocused(), disabled
        ), onPress);
        var hidden = ClientGameStateManager.getOrNull(GameClientStateTypes.HIDE_RECIPE_BOOK);
        if (hidden != null) {
            org.active = false;
            org.setTooltip(Tooltip.create(hidden.message()));
        }
        return org;
    }
}
