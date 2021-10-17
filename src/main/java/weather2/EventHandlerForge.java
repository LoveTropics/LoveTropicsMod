package weather2;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.renderer.FogRenderer.FogType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.EntityViewRenderEvent.FogColors;
import net.minecraftforge.client.event.EntityViewRenderEvent.RenderFogEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import weather2.client.SceneEnhancer;
import weather2.weathersystem.wind.WindManager;

@Mod.EventBusSubscriber(modid = Weather.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class EventHandlerForge {

	@SubscribeEvent
	@OnlyIn(Dist.CLIENT)
    public void worldRender(RenderWorldLastEvent event)
    {
		ClientTickHandler.checkClientWeather();
    }

	@SubscribeEvent
	@OnlyIn(Dist.CLIENT)
    public void onFogColors(FogColors event) {
        SceneEnhancer.getFogAdjuster().onFogColors(event);
		
	}
	
	@SubscribeEvent
	@OnlyIn(Dist.CLIENT)
	public void onFogRender(RenderFogEvent event) {
		SceneEnhancer.getFogAdjuster().onFogRender(event);
	}
	
	@SubscribeEvent
	@OnlyIn(Dist.CLIENT)
	public void onRenderTick(TickEvent.RenderTickEvent event) {
		SceneEnhancer.renderTick(event);
	}

	@SubscribeEvent
	public void onEntityLivingUpdate(LivingEvent.LivingUpdateEvent event) {
		// TODO: fix wind movement
		if (true) return;

		Entity ent = event.getEntity();
		if (!ent.world.isRemote || (ent instanceof PlayerEntity && ((PlayerEntity) ent).isUser())) {
			WindManager windMan = ServerTickHandler.getWeatherManagerFor(ent.world.getDimensionKey()).wind;
			windMan.applyWindForceNew(ent, 1F / 20F, 0.5F);
		}
	}
}
