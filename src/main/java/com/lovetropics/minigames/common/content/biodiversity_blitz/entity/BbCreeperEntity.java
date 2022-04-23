package com.lovetropics.minigames.common.content.biodiversity_blitz.entity;

import com.lovetropics.minigames.common.content.biodiversity_blitz.entity.ai.BbGroundNavigator;
import com.lovetropics.minigames.common.content.biodiversity_blitz.entity.ai.BbMobBrain;
import com.lovetropics.minigames.common.content.biodiversity_blitz.entity.ai.KaboomCropGoal;
import com.lovetropics.minigames.common.content.biodiversity_blitz.explosion.PlantAffectingExplosion;
import com.lovetropics.minigames.common.content.biodiversity_blitz.plot.Plot;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.ai.goal.LookRandomlyGoal;
import net.minecraft.entity.ai.goal.SwimGoal;
import net.minecraft.entity.ai.goal.WaterAvoidingRandomWalkingGoal;
import net.minecraft.entity.monster.CreeperEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.play.server.SExplosionPacket;
import net.minecraft.pathfinding.PathNavigator;
import net.minecraft.pathfinding.PathNodeType;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.Explosion;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;

public class BbCreeperEntity extends CreeperEntity implements BbMobEntity {
    private final BbMobBrain mobBrain;
    private final Plot plot;
    
    public BbCreeperEntity(EntityType<? extends CreeperEntity> type, World world, Plot plot) {
        super(type, world);
        this.mobBrain = new BbMobBrain(plot.walls);
        this.plot = plot;

        // Ignore sweet berry bushes and water
        this.setPathfindingMalus(PathNodeType.DANGER_OTHER, 0.0F);
        this.setPathfindingMalus(PathNodeType.DAMAGE_OTHER, 0.0F);
        this.setPathfindingMalus(PathNodeType.WATER, -1.0F);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new SwimGoal(this));
        this.goalSelector.addGoal(2, new KaboomCropGoal(this));
        this.goalSelector.addGoal(5, new WaterAvoidingRandomWalkingGoal(this, 0.8D));
        this.goalSelector.addGoal(6, new LookRandomlyGoal(this));
    }

    @Override
    public void explodeCreeper() {
        if (!this.level.isClientSide) {
            double x = this.getX();
            double y = this.getY();
            double z = this.getZ();

            Explosion explosion = new PlantAffectingExplosion(this.level, null, null, null, x, y, z, 3.0f, false, Explosion.Mode.BREAK, e -> true, this.plot);
            explosion.explode();
            explosion.finalizeExplosion(false);

            for (ServerPlayerEntity player : ((ServerWorld) this.level).players()) {
                if (player.distanceToSqr(x, y, z) < 4096.0) {
                    player.connection.send(new SExplosionPacket(x, y, z, 3.0f, explosion.getToBlow(), explosion.getHitPlayers().get(player)));
                }
            }

            this.remove();
        }
    }

    @Override
    protected Vector3d maybeBackOffFromEdge(Vector3d offset, MoverType mover) {
        return mobBrain.getPlotWalls().collide(this.getBoundingBox(), offset);
    }

    @Override
    protected PathNavigator createNavigation(World world) {
        return new BbGroundNavigator(this);
    }

    @Override
    public BbMobBrain getMobBrain() {
        return this.mobBrain;
    }

    @Override
    public MobEntity asMob() {
        return this;
    }

    @Override
    public Plot getPlot() {
        return this.plot;
    }
}
