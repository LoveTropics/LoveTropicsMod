package com.lovetropics.minigames.common.content.paint_party.entity;

import com.lovetropics.minigames.common.content.paint_party.PaintParty;
import com.lovetropics.minigames.common.content.paint_party.PaintPartyEvents;
import com.lovetropics.minigames.common.core.game.IGameManager;
import com.lovetropics.minigames.common.core.game.IGamePhase;
import net.minecraft.core.BlockPos;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class PaintBallEntity extends ThrowableProjectile {
    public PaintBallEntity(EntityType<? extends ThrowableProjectile> entityType, Level level) {
        super(entityType, level);
    }

    public PaintBallEntity(Level level) {
        this(PaintParty.PAINTBALL.get(), level);
    }

    @Override
    protected void onHitBlock(BlockHitResult result) {
        super.onHitBlock(result);

        IGamePhase game = IGameManager.get().getGamePhaseAt(level(), result.getBlockPos());
        if (game != null) {
            game.invoker(PaintPartyEvents.PAINTBALL_HIT).onPaintBallHit(level(), this, result.getBlockPos());
        }

        discard();
    }

    @Override
    public void shootFromRotation(Entity shooter, float x, float y, float z, float velocity, float inaccuracy) {
        float f = -Mth.sin(y * (float) (Math.PI / 180.0)) * Mth.cos(x * (float) (Math.PI / 180.0));
        float f1 = -Mth.sin((x + z) * (float) (Math.PI / 180.0));
        float f2 = Mth.cos(y * (float) (Math.PI / 180.0)) * Mth.cos(x * (float) (Math.PI / 180.0));
        this.shoot((double)f, (double)f1, (double)f2, velocity, inaccuracy);
        Vec3 vec3 = new Vec3(0, 0, 0); // Don't add player movement
        this.setDeltaMovement(this.getDeltaMovement().add(vec3.x, shooter.onGround() ? 0.0 : vec3.y, vec3.z));
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {

    }
}
