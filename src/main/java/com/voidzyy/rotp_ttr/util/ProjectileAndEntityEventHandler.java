package com.voidzyy.rotp_ttr.util;

import com.github.standobyte.jojo.init.ModStatusEffects;
import com.voidzyy.rotp_ttr.entity.Deadphotomemtor;
import com.voidzyy.rotp_ttr.items.TearsBlade;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.potion.EffectInstance;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.EntityRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.Explosion;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.event.entity.ProjectileImpactEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Mod.EventBusSubscriber(modid = "rotp_ttr")
public class ProjectileAndEntityEventHandler {

    // 拦截弹射物的逻辑（保持不变）
    @SubscribeEvent
    public static void onProjectileImpact(ProjectileImpactEvent event) {
        if (event.getRayTraceResult().getType() == RayTraceResult.Type.ENTITY) {
            Entity entityHit = ((EntityRayTraceResult) event.getRayTraceResult()).getEntity();
            if (shouldInterceptProjectile(entityHit)) {
                event.getEntity().remove();
                event.setCanceled(true);
            }
        }
    }

    private static boolean shouldInterceptProjectile(Entity entityHit) {
        return false;
    }

    // Deadphotomemtor行为控制（增强粒子效果）
    @SubscribeEvent
    public static void onLivingUpdate(LivingEvent.LivingUpdateEvent event) {
        LivingEntity entity = event.getEntityLiving();

        if (entity instanceof Deadphotomemtor) {
            Deadphotomemtor deadphotomemtor = (Deadphotomemtor) entity;

            // 1. 生成3x3火焰和烟雾粒子
            spawn3x3FireParticles(deadphotomemtor);

            // 2. 原有逻辑：寻找目标并移动
            findAndMoveToTarget(deadphotomemtor);

            // 3. 原有逻辑：检查是否爆炸
            if (shouldExplode(deadphotomemtor)) {
                explodeWithShockwave(deadphotomemtor);
            }
        }
    }

    // 生成3x3火焰粒子阵列
    private static void spawn3x3FireParticles(Deadphotomemtor entity) {
        if (entity.level.isClientSide()) {
            Vector3d backPos = getBackPosition(entity);

            // 3x3火焰粒子阵列
            for (int x = -1; x <= 1; x++) {
                for (int z = -1; z <= 1; z++) {
                    double offsetX = x * 0.3;
                    double offsetZ = z * 0.3;

                    // 火焰粒子
                    entity.level.addParticle(ParticleTypes.FLAME,
                            backPos.x + offsetX, backPos.y, backPos.z + offsetZ,
                            (entity.getRandom().nextDouble() - 0.5) * 0.1,
                            3.1,
                            (entity.getRandom().nextDouble() - 0.5) * 0.1);

                    // 烟雾粒子
                    if (entity.tickCount % 3 == 0) {
                        entity.level.addParticle(ParticleTypes.SMOKE,
                                backPos.x + offsetX, backPos.y + 0.2, backPos.z + offsetZ,
                                0, 0.05, 0);
                    }
                }
            }

            // 额外的大火焰粒子在中心
            entity.level.addParticle(ParticleTypes.FLAME,
                    backPos.x, backPos.y + 3.3, backPos.z,
                    (entity.getRandom().nextDouble() - 0.5) * 0.2,
                    0.2,
                    (entity.getRandom().nextDouble() - 0.5) * 0.2);
        }
    }

    // 计算实体背部位置（用于火焰粒子）
    private static Vector3d getBackPosition(Deadphotomemtor entity) {
        double yawRad = Math.toRadians(entity.yRot + 180); // 背部方向
        double pitchRad = Math.toRadians(entity.xRot);
        double distance = -0.9; // 粒子距离实体背部的距离

        return new Vector3d(
                entity.getX() + Math.sin(yawRad) * distance * Math.cos(pitchRad),
                entity.getY() + Math.sin(pitchRad) * distance,
                entity.getZ() + Math.cos(yawRad) * distance * Math.cos(pitchRad)
        );
    }

    // 爆炸时生成冲击波粒子
    private static void spawnShockwaveParticles(Deadphotomemtor entity) {
        if (!entity.level.isClientSide()) {
            ServerWorld serverWorld = (ServerWorld) entity.level;
            Vector3d center = entity.position();

            // 冲击波粒子效果
            for (int i = 0; i < 360; i += 10) {
                double angle = Math.toRadians(i);
                double radius = 6.0;

                double x = center.x + Math.cos(angle) * radius;
                double z = center.z + Math.sin(angle) * radius;

                // 冲击波环状粒子
                serverWorld.sendParticles(ParticleTypes.FLAME,
                        x, center.y + 1.0, z,
                        1, 0, 0.1, 0, 0.05);

                // 爆炸核心粒子
                serverWorld.sendParticles(ParticleTypes.EXPLOSION,
                        center.x, center.y + 1.0, center.z,
                        5, 1.0, 1.0, 1.0, 0.1);

                // 烟雾冲击波
                serverWorld.sendParticles(ParticleTypes.CAMPFIRE_COSY_SMOKE,
                        x, center.y + 1.5, z,
                        5, 0, 0.6, 0, 0.02);
            }

            // 垂直冲击波
            for (int y = 0; y <= 10; y++) {
                double height = center.y + (y * 0.5);
                serverWorld.sendParticles(ParticleTypes.FLAME,
                        center.x, height, center.z,
                        6, 2.0, 0, 1.0, 0.1);
            }
        }
    }

    // ---------- 修改后的逻辑（使用标签检测） ----------
    private static void findAndMoveToTarget(Deadphotomemtor entity) {
        if (!entity.level.isClientSide()) {
            List<LivingEntity> targets = entity.level.getEntitiesOfClass(
                    LivingEntity.class,
                    new AxisAlignedBB(entity.blockPosition()).inflate(50.0D),
                    target -> TearsBlade.isEntityMarked(target) && target != entity && target.isAlive()
            );

            if (!targets.isEmpty()) {
                Optional<LivingEntity> nearestTarget = targets.stream()
                        .min(Comparator.comparingDouble(target -> target.distanceToSqr(entity)));

                if (nearestTarget.isPresent()) {
                    LivingEntity target = nearestTarget.get();
                    Vector3d direction = new Vector3d(
                            target.getX() - entity.getX(),
                            (target.getY() + target.getEyeHeight()) - entity.getY(),
                            target.getZ() - entity.getZ()
                    ).normalize();

                    double speed = 1.2;
                    entity.setDeltaMovement(
                            direction.x * speed,
                            -0.8,
                            direction.z * speed
                    );

                    float yaw = (float) Math.toDegrees(Math.atan2(direction.x, direction.z));
                    float pitch = (float) Math.toDegrees(Math.atan2(direction.y, Math.sqrt(direction.x * direction.x + direction.z * direction.z)));
                    entity.yRot = yaw + 180;
                    entity.xRot = -pitch;

                    // 接近目标时生成追踪粒子
                    if (entity.distanceToSqr(target) < 25.0) {
                        spawnTrackingParticles(entity, target);
                    }
                }
            } else {
                entity.setDeltaMovement(
                        (entity.getRandom().nextDouble() - 0.5) * 0.1,
                        -0.5,
                        (entity.getRandom().nextDouble() - 0.5) * 0.1
                );
            }
        }
    }

    // 生成追踪粒子（接近目标时）
    private static void spawnTrackingParticles(Deadphotomemtor entity, LivingEntity target) {
        if (entity.level.isClientSide()) {
            Vector3d entityPos = entity.position();
            Vector3d targetPos = target.position();
            Vector3d direction = targetPos.subtract(entityPos).normalize();

            // 在两者之间生成连线粒子
            for (int i = 0; i < 5; i++) {
                double progress = i / 5.0;
                Vector3d particlePos = entityPos.add(
                        direction.x * progress * entity.distanceTo(target),
                        direction.y * progress * entity.distanceTo(target),
                        direction.z * progress * entity.distanceTo(target)
                );

                entity.level.addParticle(ParticleTypes.SOUL_FIRE_FLAME,
                        particlePos.x, particlePos.y, particlePos.z,
                        0, 4.6, 0);
            }
        }
    }

    private static boolean shouldExplode(Deadphotomemtor entity) {
        if (entity.level.isClientSide()) return false;
        if (isOnGround(entity)) return true;

        List<LivingEntity> nearbyEntities = entity.level.getEntitiesOfClass(
                LivingEntity.class,
                entity.getBoundingBox().inflate(1.1D),
                target -> TearsBlade.isEntityMarked(target) && target != entity && target.isAlive()
        );
        return !nearbyEntities.isEmpty();
    }

    private static boolean isOnGround(Deadphotomemtor entity) {
        BlockPos blockPosBelow = new BlockPos(entity.getX(), entity.getY() - 1, entity.getZ());
        return !entity.level.isEmptyBlock(blockPosBelow);
    }

    private static void explodeWithShockwave(Deadphotomemtor entity) {
        if (!entity.isAlive()) return;

        // 生成冲击波粒子
        spawnShockwaveParticles(entity);

        // 执行爆炸
        entity.level.explode(entity, entity.getX(), entity.getY(), entity.getZ(), 9.0f, false, Explosion.Mode.BREAK);

        // 应用流血效果
        List<Entity> nearbyEntities = entity.level.getEntities(entity, entity.getBoundingBox().inflate(6.0D));
        for (Entity nearbyEntity : nearbyEntities) {
            if (nearbyEntity instanceof LivingEntity) {
                LivingEntity livingEntity = (LivingEntity) nearbyEntity;
                if (TearsBlade.isEntityMarked(livingEntity)) {
                    livingEntity.addEffect(new EffectInstance(ModStatusEffects.BLEEDING.get(), 200, 0));
                }
            }
        }
        entity.remove();
    }
}