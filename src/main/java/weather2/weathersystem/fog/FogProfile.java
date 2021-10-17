package weather2.weathersystem.fog;

import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.util.math.vector.Vector3f;

public class FogProfile {

    private Vector3f rgb = new Vector3f(1, 1, 1);
    private float fogStart = 0;
    private float fogEnd = 20;
    private float fogStartSky = 20;
    private float fogEndSky = 20;
    private GlStateManager.FogMode fogMode = GlStateManager.FogMode.LINEAR;
    private float fadeToRate = 0.1F;

    //likely only used when we fade back to vanilla fog, otherwise fadeToRate for the new profile will take control
    private float fadeFromRate = 0.1F;

    public FogProfile(Vector3f rgb, float fogStart, float fogEnd, GlStateManager.FogMode fogMode) {
        this.rgb = rgb;
        this.fogStart = fogStart;
        this.fogStartSky = fogStart;
        this.fogEnd = fogEnd;
        this.fogEndSky = fogEnd;
        this.fogMode = fogMode;
    }

    public Vector3f getRgb() {
        return rgb;
    }

    public void setRgb(Vector3f rgb) {
        this.rgb = rgb;
    }

    public float getFogStart() {
        return fogStart;
    }

    public void setFogStart(float fogStart) {
        this.fogStart = fogStart;
    }

    public float getFogEnd() {
        return fogEnd;
    }

    public void setFogEnd(float fogEnd) {
        this.fogEnd = fogEnd;
    }

    public GlStateManager.FogMode getFogMode() {
        return fogMode;
    }

    public void setFogMode(GlStateManager.FogMode fogMode) {
        this.fogMode = fogMode;
    }

    public float getFogStartSky() {
        return fogStartSky;
    }

    public void setFogStartSky(float fogStartSky) {
        this.fogStartSky = fogStartSky;
    }

    public float getFogEndSky() {
        return fogEndSky;
    }

    public void setFogEndSky(float fogEndSky) {
        this.fogEndSky = fogEndSky;
    }
}
