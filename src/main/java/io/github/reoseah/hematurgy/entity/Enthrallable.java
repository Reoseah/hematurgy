package io.github.reoseah.hematurgy.entity;

import net.minecraft.block.BlockState;
import net.minecraft.block.LeavesBlock;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.TargetPredicate;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.ai.goal.TrackTargetGoal;
import net.minecraft.entity.ai.pathing.EntityNavigation;
import net.minecraft.entity.ai.pathing.LandPathNodeMaker;
import net.minecraft.entity.ai.pathing.PathNodeType;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;
import java.util.Optional;
import java.util.UUID;

public interface Enthrallable {
    TrackedData<Optional<UUID>> MASTER_UUID = DataTracker.registerData(MobEntity.class, TrackedDataHandlerRegistry.OPTIONAL_UUID);

    @Nullable
    UUID hematurgy$getMasterUuid();

    void hematurgy$setMasterUuid(@Nullable UUID uuid);

    @Nullable
    default LivingEntity getMaster(World world) {
        try {
            var uuid = this.hematurgy$getMasterUuid();
            return uuid != null ? world.getPlayerByUuid(uuid) : null;
        } catch (IllegalArgumentException exception) {
            return null;
        }
    }

    /**
     * Copy of {@link net.minecraft.entity.ai.goal.FollowOwnerGoal} for enthralled mobs.
     */
    class FollowMasterGoal extends Goal {
        private final MobEntity mob;
        private final EntityNavigation navigation;
        private final float maxDistance;
        private final float minDistance;
        private final World world;
        private final double speed;

        private LivingEntity master;
        private int updateCountdownTicks;
        private float oldWaterPathfindingPenalty;

        public FollowMasterGoal(MobEntity mob, float maxDistance, float minDistance, double speed) {
            this.mob = mob;
            this.maxDistance = maxDistance;
            this.minDistance = minDistance;
            this.speed = speed;
            this.navigation = mob.getNavigation();
            this.world = mob.getWorld();
            this.setControls(EnumSet.of(Goal.Control.MOVE, Goal.Control.LOOK));
        }

        @Override
        public boolean canStart() {
            LivingEntity master = ((Enthrallable) this.mob).getMaster(this.world);
            if (master == null) {
                return false;
            }
            if (master.isSpectator()) {
                return false;
            }
            if (this.mob.squaredDistanceTo(master) < this.minDistance * this.minDistance) {
                return false;
            }
            this.master = master;
            return true;
        }


        @Override
        public void start() {
            this.updateCountdownTicks = 0;
            this.oldWaterPathfindingPenalty = this.mob.getPathfindingPenalty(PathNodeType.WATER);
            this.mob.setPathfindingPenalty(PathNodeType.WATER, 0.0F);
        }

        @Override
        public void stop() {
            this.master = null;
            this.navigation.stop();
            this.mob.setPathfindingPenalty(PathNodeType.WATER, this.oldWaterPathfindingPenalty);
        }

        @Override
        public boolean shouldContinue() {
            if (this.navigation.isIdle()) {
                return false;
            }
            return this.mob.squaredDistanceTo(this.master) > this.maxDistance * this.maxDistance;
        }

        @Override
        public void tick() {
            this.mob.getLookControl().lookAt(this.master, 10.0f, this.mob.getMaxLookPitchChange());
            if (--this.updateCountdownTicks > 0) {
                return;
            }
            this.updateCountdownTicks = this.getTickCount(10);
            if (this.mob.isLeashed() || this.mob.hasVehicle()) {
                return;
            }
            if (this.mob.squaredDistanceTo(this.master) >= 256.0) {
                this.tryTeleport();
            } else {
                this.navigation.startMovingTo(this.master, this.speed);
            }
        }

        private void tryTeleport() {
            BlockPos pos = this.master.getBlockPos();
            for (int i = 0; i < 10; i++) {
                int dx = this.getRandomInt(-3, 3);
                int dy = this.getRandomInt(-1, 1);
                int dz = this.getRandomInt(-3, 3);
                if (this.tryTeleportTo(pos.getX() + dx, pos.getY() + dy, pos.getZ() + dz)) {
                    return;
                }
            }
        }

        private boolean tryTeleportTo(int x, int y, int z) {
            if (Math.abs(x - this.master.getX()) < 2.0 && Math.abs(z - this.master.getZ()) < 2.0) {
                return false;
            }
            if (!this.canTeleportTo(new BlockPos(x, y, z))) {
                return false;
            }
            this.mob.refreshPositionAndAngles(x + 0.5, y, z + 0.5, this.mob.getYaw(), this.mob.getPitch());
            this.navigation.stop();
            return true;
        }

        private boolean canTeleportTo(BlockPos pos) {
            PathNodeType type = LandPathNodeMaker.getLandNodeType(this.mob, pos);
            if (type != PathNodeType.WALKABLE) {
                return false;
            }
            BlockState state = this.world.getBlockState(pos.down());
            if (state.getBlock() instanceof LeavesBlock) {
                return false;
            }
            BlockPos target = pos.subtract(this.mob.getBlockPos());
            return this.world.isSpaceEmpty(this.mob, this.mob.getBoundingBox().offset(target));
        }

        private int getRandomInt(int min, int max) {
            return this.mob.getRandom().nextInt(max - min + 1) + min;
        }
    }

    /**
     * Copy of {@link net.minecraft.entity.ai.goal.TrackOwnerAttackerGoal} for enthralled mobs.
     */
    class TrackMasterAttackerGoal extends TrackTargetGoal {
        private final MobEntity mob;
        private LivingEntity attacker;
        private int lastAttackedTime;

        public TrackMasterAttackerGoal(MobEntity mob) {
            super(mob, false);
            this.mob = mob;
            this.setControls(EnumSet.of(Goal.Control.TARGET));
        }

        @Override
        public boolean canStart() {
            if (((Enthrallable) this.mob).hematurgy$getMasterUuid() == null) {
                return false;
            }
            LivingEntity master = ((Enthrallable) this.mob).getMaster(this.mob.getWorld());
            if (master == null) {
                return false;
            }
            this.attacker = master.getAttacker();
            int lastAttackedTime = master.getLastAttackedTime();
            return lastAttackedTime != this.lastAttackedTime && this.canTrack(this.attacker, TargetPredicate.DEFAULT);
        }

        @Override
        public void start() {
            this.mob.setTarget(this.attacker);
            LivingEntity master = ((Enthrallable) this.mob).getMaster(this.mob.getWorld());
            if (master != null) {
                this.lastAttackedTime = master.getLastAttackedTime();
            }
            super.start();
        }
    }

    /**
     * Copy of {@link net.minecraft.entity.ai.goal.AttackWithOwnerGoal} for enthralled mobs.
     */
    class AttackWithMasterGoal extends TrackTargetGoal {
        private final MobEntity mob;
        private LivingEntity attacking;
        private int lastAttackTime;

        public AttackWithMasterGoal(MobEntity mob) {
            super(mob, false);
            this.mob = mob;
            this.setControls(EnumSet.of(Goal.Control.TARGET));
        }

        @Override
        public boolean canStart() {
            if (((Enthrallable) this.mob).hematurgy$getMasterUuid() == null) {
                return false;
            }
            LivingEntity master = ((Enthrallable) this.mob).getMaster(this.mob.getWorld());
            if (master == null) {
                return false;
            }
            this.attacking = master.getAttacking();
            int lastAttackTime = master.getLastAttackTime();
            return lastAttackTime != this.lastAttackTime && this.canTrack(this.attacking, TargetPredicate.DEFAULT);
        }

        @Override
        public void start() {
            this.mob.setTarget(this.attacking);
            LivingEntity master = ((Enthrallable) this.mob).getMaster(this.mob.getWorld());
            if (master != null) {
                this.lastAttackTime = master.getLastAttackTime();
            }
            super.start();
        }
    }
}
