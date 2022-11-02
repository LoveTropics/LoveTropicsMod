package com.lovetropics.minigames.common.content.biodiversity_blitz.entity;

import com.lovetropics.minigames.common.content.biodiversity_blitz.entity.ai.BbGroundNavigator;
import com.lovetropics.minigames.common.content.biodiversity_blitz.entity.ai.BbMobBrain;
import com.lovetropics.minigames.common.content.biodiversity_blitz.entity.ai.KaboomCropGoal;
import com.lovetropics.minigames.common.content.biodiversity_blitz.explosion.PlantAffectingExplosion;
import com.lovetropics.minigames.common.content.biodiversity_blitz.plot.Plot;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.protocol.game.ClientboundExplodePacket;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.level.pathfinder.BlockPathTypes;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.server.level.ServerLevel;

public class BbCreeperEntity extends Creeper implements BbMobEntity {
    private final BbMobBrain mobBrain;
    private final Plot plot;
    
    public BbCreeperEntity(EntityType<? extends Creeper> type, Level world, Plot plot) {
        super(type, world);
        this.mobBrain = new BbMobBrain(plot.walls);
        this.plot = plot;

        // Ignore sweet berry bushes and water
        this.setPathfindingMalus(BlockPathTypes.DANGER_OTHER, 0.0F);
        this.setPathfindingMalus(BlockPathTypes.DAMAGE_OTHER, 0.0F);
        this.setPathfindingMalus(BlockPathTypes.WATER, -1.0F);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new FloatGoal(this));
        this.goalSelector.addGoal(2, new KaboomCropGoal(this));
        this.goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 0.8D));
        this.goalSelector.addGoal(6, new RandomLookAroundGoal(this));
    }

    @Override
    public void explodeCreeper() {
        if (!this.level.isClientSide) {
            double x = this.getX();
            double y = this.getY();
            double z = this.getZ();

            Explosion explosion = new PlantAffectingExplosion(this.level, null, null, null, x, y, z, 2.5f, false, Explosion.BlockInteraction.BREAK, e -> true, this.plot);
            explosion.explode();
            explosion.finalizeExplosion(false);

            float factor = this.isPowered() ? 2.0F : 1.0F;
            for (ServerPlayer player : ((ServerLevel) this.level).players()) {
                if (player.distanceToSqr(x, y, z) < 4096.0) {
                    player.connection.send(new ClientboundExplodePacket(x, y, z, 2.5f * factor, explosion.getToBlow(), explosion.getHitPlayers().get(player)));
                }
            }

            this.remove(RemovalReason.KILLED);
        }
    }

    @Override
    protected Vec3 maybeBackOffFromEdge(Vec3 offset, MoverType mover) {
        return mobBrain.getPlotWalls().collide(this.getBoundingBox(), offset);
    }

    @Override
    protected PathNavigation createNavigation(Level world) {
        return new BbGroundNavigator(this);
    }

    @Override
    public BbMobBrain getMobBrain() {
        return this.mobBrain;
    }

    @Override
    public Mob asMob() {
        return this;
    }

    @Override
    public Plot getPlot() {
        return this.plot;
    }
}
