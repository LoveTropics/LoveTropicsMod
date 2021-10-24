package com.lovetropics.minigames.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.client.gui.FocusableGui;
import net.minecraftforge.client.gui.ScrollPanel;

@Mixin(ScrollPanel.class)
public abstract class ScrollPanelMixin extends FocusableGui {

	@Shadow
	private float scrollDistance;

	@ModifyArg(method = {"mouseClicked", "mouseReleased"},
			   at = @At(value = "INVOKE", ordinal = 0),
			   index = 1)
	public double moveMouseForScrolling(double mouseY) {
		return mouseY + this.scrollDistance;
	}

	@Inject(method = { "mouseScrolled", "mouseDragged" }, at = @At("HEAD"))
	public void mouseScrolledAddSuper(double mouseX, double mouseY, double delta, CallbackInfoReturnable<Boolean> callbackInfo) {
		super.mouseScrolled(mouseX, mouseY + this.scrollDistance, delta);
	}
	
	@Inject(method = { "mouseDragged" }, at = @At("HEAD"))
	public void mouseDraggedAddSuper(double mouseX, double mouseY, int button, double dragX, double dragY, CallbackInfoReturnable<Boolean> callbackInfo) {
		super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
	}
}
