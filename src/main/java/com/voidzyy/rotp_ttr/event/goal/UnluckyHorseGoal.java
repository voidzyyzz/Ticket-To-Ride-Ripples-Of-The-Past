package com.voidzyy.rotp_ttr.event.goal;

import com.voidzyy.rotp_ttr.items.TearsBlade;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.passive.horse.HorseEntity;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.potion.Effects;
import net.minecraft.util.DamageSource;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;

import java.util.Comparator;
import java.util.EnumSet;
import java.util.Objects;

public class UnluckyHorseGoal extends Goal {

    private final HorseEntity horse;
    private LivingEntity target;
    private int cooldown = 0; // 撞击冷却时间
    private int unluckTickCounter = 0; // 不幸效果持续时间计数器

    public UnluckyHorseGoal(HorseEntity horse) {
        this.horse = horse;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    /* 是否有 Unluck */
    private boolean hasUnluck() {
        return horse.hasEffect(Effects.UNLUCK);
    }

    /* 获取 Unluck 剩余时间（ticks） */
    private int getUnluckDuration() {
        if (horse.hasEffect(Effects.UNLUCK)) {
            return Objects.requireNonNull(horse.getEffect(Effects.UNLUCK)).getDuration();
        }
        return 0;
    }

    @Override
    public boolean canUse() {
        if (!hasUnluck()) return false;

        // 检查不幸效果是否只剩1秒（20 ticks）
        if (getUnluckDuration() <= 20) {
            return true; // 允许执行以触发消失逻辑
        }

        World world = horse.level;

        target = world.getEntitiesOfClass(LivingEntity.class,
                        horse.getBoundingBox().inflate(64))
                .stream()
                .filter(e -> e != horse && e.isAlive() && TearsBlade.isEntityMarked(e))
                .min(Comparator.comparingDouble(e -> e.distanceToSqr(horse)))
                .orElse(null);

        return target != null;
    }

    @Override
    public void tick() {
        // 检查不幸效果是否只剩1秒（20 ticks）
        if (hasUnluck() && getUnluckDuration() <= 20) {
            unluckTickCounter++;

            // 在最后5 ticks生成消失粒子效果
            if (unluckTickCounter >= 15 && horse.level instanceof ServerWorld) {
                ServerWorld serverWorld = (ServerWorld) horse.level;
                serverWorld.sendParticles(ParticleTypes.POOF,
                        horse.getX(), horse.getY() + horse.getBbHeight() / 2.0, horse.getZ(),
                        10, 0.5, 0.5, 0.5, 0.1);
            }

            // 当不幸效果只剩1秒时移除马
            if (getUnluckDuration() <= 1) {
                horse.remove();
                return;
            }
        }

        if (target == null || !target.isAlive() || !hasUnluck()) {
            return;
        }

        // 减少冷却时间
        if (cooldown > 0) {
            cooldown--;
            return;
        }

        double dist = horse.distanceTo(target);
        horse.getLookControl().setLookAt(target, 30F, 30F);
        horse.getNavigation().moveTo(target, 2.4D); // 2 倍速

        if (dist <= 1.7D) {
            // 立即生成爆炸粒子特效
            spawnExplosionParticles();
            // 播放爆炸音效
            playExplosionSound();
            float damageDealt = damageTarget(); // 获取造成的伤害值
            knockbackTarget(); // 击飞目标
            applyReflectiveDamage(damageDealt); // 应用反射伤害

            // 设置冷却时间（10 ticks = 0.5秒）
            cooldown = 2;
        }
    }

    /* 生成爆炸粒子特效 */
    private void spawnExplosionParticles() {
        if (!(horse.level instanceof ServerWorld)) return;

        ServerWorld serverWorld = (ServerWorld) horse.level;
        // 在目标位置生成爆炸粒子
        serverWorld.sendParticles(ParticleTypes.EXPLOSION,
                target.getX(), target.getY() + target.getBbHeight() / 2.0, target.getZ(),
                20, 0.5, 0.5, 0.5, 0.5);
    }

    /* 播放爆炸音效 */
    private void playExplosionSound() {
        if (!horse.level.isClientSide) {
            horse.level.playSound(null, horse.getX(), horse.getY(), horse.getZ(),
                    SoundEvents.GENERIC_EXPLODE, horse.getSoundSource(), 1.0F, 1.0F);
        }
    }

    /* 30% 最大生命 + 6 伤害，返回造成的伤害值 */
    private float damageTarget() {
        if (!target.isAlive()) return 0;
        float dmg = target.getMaxHealth() * 0.3F + 6F;
        target.hurt(DamageSource.MAGIC, dmg);
        return dmg;
    }

    /* 击飞目标 */
    private void knockbackTarget() {
        if (!target.isAlive()) return;

        // 计算击飞方向（从马指向目标的方向）
        Vector3d knockbackDirection = new Vector3d(
                target.getX() - horse.getX(),
                0.5, // 向上的击飞分量
                target.getZ() - horse.getZ()
        ).normalize();

        // 应用击飞力度
        double knockbackStrength = 2.5; // 击飞强度
        target.setDeltaMovement(
                knockbackDirection.x * knockbackStrength,
                knockbackDirection.y * knockbackStrength,
                knockbackDirection.z * knockbackStrength
        );

        // 确保速度更新
        target.hurtMarked = true;

        // 播放击飞音效
        if (!horse.level.isClientSide) {
            horse.level.playSound(null, target.getX(), target.getY(), target.getZ(),
                    SoundEvents.PLAYER_ATTACK_KNOCKBACK, target.getSoundSource(), 1.0F, 1.0F);
        }
    }

    /* 应用反射伤害：受到对方所受伤害60%的反射伤害 */
    private void applyReflectiveDamage(float damageDealt) {
        if (!horse.isAlive()) return;

        // 计算反射伤害（对方所受伤害的60%）
        float reflectiveDamage = damageDealt * 0.6F + 5;

        // 对马造成反射伤害
        horse.hurt(DamageSource.MAGIC, reflectiveDamage);

        // 播放马受伤音效
        if (!horse.level.isClientSide) {
            horse.level.playSound(null, horse.getX(), horse.getY(), horse.getZ(),
                    SoundEvents.HORSE_HURT, horse.getSoundSource(), 1.0F, 1.0F);
        }
    }

    /* 开始执行目标时重置冷却和计数器 */
    @Override
    public void start() {
        super.start();
        cooldown = 0; // 重置冷却时间
        unluckTickCounter = 0; // 重置不幸效果计数器
    }

    /* 停止执行目标时重置冷却和计数器 */
    @Override
    public void stop() {
        super.stop();
        cooldown = 0; // 重置冷却时间
        unluckTickCounter = 0; // 重置不幸效果计数器
        target = null; // 清除目标引用
    }
}