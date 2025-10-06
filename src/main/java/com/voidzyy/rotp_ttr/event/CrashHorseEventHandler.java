package com.voidzyy.rotp_ttr.util;

import com.voidzyy.rotp_ttr.init.InitStatusEffect;
import com.voidzyy.rotp_ttr.util.Tags;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.passive.horse.HorseEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.UUID;

@Mod.EventBusSubscriber
public class CrashHorseEventHandler {

    @SubscribeEvent
    public static void onWorldTick(TickEvent.WorldTickEvent event) {
        if (event.phase == TickEvent.Phase.END && !event.world.isClientSide()) {
            // 遍历所有实体，寻找带有CrashHorse标签的马
            for (Entity entity : event.world.entitiesForRendering()) {
                if (entity instanceof HorseEntity) {
                    HorseEntity horse = (HorseEntity) entity;
                    CompoundNBT tag = horse.getPersistentData();

                    if (tag.getBoolean(Tags.CRASH_HORSE)) {
                        handleCrashHorseMovement(horse, event.world);
                    }
                }
            }
        }
    }

    private static void handleCrashHorseMovement(HorseEntity horse, World world) {
        // 获取目标实体UUID
        CompoundNBT tag = horse.getPersistentData();
        UUID targetUUID = tag.getUUID(Tags.TARGET_ENTITY);

        Entity targetEntity = world.getEntity(targetUUID);

        if (targetEntity instanceof LivingEntity && targetEntity.isAlive()) {
            LivingEntity target = (LivingEntity) targetEntity;

            // 检查目标是否还有TEARS效果
            if (target.hasEffect(InitStatusEffect.TEARS_EFFECT.get())) {
                // 让马看向目标
                horse.lookAt(target, 30.0F, 30.0F);

                // 加速向目标移动
                Vector3d moveDirection = target.position().subtract(horse.position()).normalize();
                double speed = 2.5; // 加快速度
                horse.setDeltaMovement(moveDirection.x * speed, horse.getDeltaMovement().y, moveDirection.z * speed);

                // 检查碰撞
                checkCollision(horse, target);
            } else {
                // 目标失去TEARS效果，马匹死亡
                horse.hurt(DamageSource.OUT_OF_WORLD, Float.MAX_VALUE);
            }
        } else {
            // 目标不存在或死亡，马匹死亡
            horse.hurt(DamageSource.OUT_OF_WORLD, Float.MAX_VALUE);
        }
    }

    private static void checkCollision(HorseEntity horse, LivingEntity target) {
        double distance = horse.distanceToSqr(target);

        if (distance <= 2.25) { // 1.5格距离内发生碰撞
            performCrashDamage(horse, target);

            // 马匹死亡
            horse.hurt(DamageSource.OUT_OF_WORLD, Float.MAX_VALUE);

            // 播放死亡特效
            world.addParticle(net.minecraft.particles.ParticleTypes.POOF,
                    horse.getX(), horse.getY() + horse.getBbHeight() / 2, horse.getZ(),
                    0, 0.1, 0);
        }
    }

    private static void performCrashDamage(HorseEntity horse, LivingEntity target) {
        // 计算伤害：马总血量的40% + 7点固定伤害
        float damage = (horse.getMaxHealth() * 0.4f) + 7.0f;

        // 造成伤害
        target.hurt(DamageSource.mobAttack(horse), damage);

        // 击退效果
        Vector3d knockbackDir = target.position().subtract(horse.position()).normalize();
        target.setDeltaMovement(knockbackDir.x * 3.0, 0.7, knockbackDir.z * 3.0);

        // 播放碰撞特效
        world.addParticle(net.minecraft.particles.ParticleTypes.EXPLOSION,
                target.getX(), target.getY() + 1.0, target.getZ(),
                0, 0, 0);

        world.playSound(null, target.getX(), target.getY(), target.getZ(),
                net.minecraft.util.SoundEvents.GENERIC_EXPLODE,
                net.minecraft.util.SoundCategory.NEUTRAL, 1.0F, 1.0F);
    }
}