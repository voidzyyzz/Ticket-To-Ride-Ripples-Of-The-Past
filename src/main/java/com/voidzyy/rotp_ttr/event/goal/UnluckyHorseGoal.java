package com.voidzyy.rotp_ttr.entity.goal;

import com.voidzyy.rotp_ttr.init.InitStatusEffect;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.passive.horse.HorseEntity;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.potion.Effects;
import net.minecraft.util.DamageSource;
import net.minecraft.util.SoundEvents;
import net.minecraft.world.World;

import java.util.EnumSet;

/**
 * 不幸马专用 AI
 * 持有 Unluck 时自动追踪带 TEARS 的生物，靠近后假爆炸并自杀
 */
public class UnluckyHorseGoal extends Goal {

    private final HorseEntity horse;
    private LivingEntity target;
    private boolean exploded = false; // 仅触发一次

    public UnluckyHorseGoal(HorseEntity horse) {
        this.horse = horse;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    /* 是否有 Unluck */
    private boolean hasUnluck() {
        return horse.hasEffect(Effects.UNLUCK);
    }

    /* 是否有 TEARS */
    private boolean hasTears(LivingEntity e) {
        return e.hasEffect(InitStatusEffect.TEARS_EFFECT.get());
    }

    /* 寻找最近带 TEARS 的生物（32 格）*/
    @Override
    public boolean canUse() {
        if (!hasUnluck()) return false;
        World world = horse.level;
        target = world.getNearestEntity(LivingEntity.class,
                e -> e != horse && e.isAlive() && hasTears(e),
                horse, horse.getX(), horse.getY(), horse.getZ(),
                horse.getBoundingBox().inflate(32));
        return target != null;
    }

    /* 每 tick 更新 */
    @Override
    public void tick() {
        if (target == null || !target.isAlive() || !hasUnluck()) {
            if (!hasUnluck()) suicide();     // Buff 消失立刻死
            return;
        }

        double dist = horse.distanceTo(target);
        horse.getLookControl().setLookAt(target, 30F, 30F);
        horse.getNavigation().moveTo(target, 1.2D); // 2 倍速

        if (dist <= 1.5D && !exploded) {
            exploded = true;
            fakeExplode();
            damageTarget();
            suicide();
        }
    }

    /* 假爆炸：仅粒子+音效 */
    private void fakeExplode() {
        World world = horse.level;
        if (world.isClientSide) return;
        for (int i = 0; i < 30; i++)
            world.addParticle(ParticleTypes.POOF,
                    horse.getX() + (world.random.nextDouble() - 0.5) * 2,
                    horse.getY() + world.random.nextDouble() * 2,
                    horse.getZ() + (world.random.nextDouble() - 0.5) * 2,
                    (world.random.nextDouble() - 0.5) * 0.2,
                    world.random.nextDouble() * 0.2,
                    (world.random.nextDouble() - 0.5) * 0.2);
        horse.playSound(SoundEvents.GENERIC_EXPLODE, 1.0F, 1.0F);
    }

    /* 30% 最大生命 + 12 伤害 */
    private void damageTarget() {
        if (!target.isAlive()) return;
        float dmg = (float) (target.getMaxHealth() * 0.3F + 12F);
        target.hurt(DamageSource.MAGIC, dmg);
    }

    /* 高空瞬移 + 立即死亡 */
    private void suicide() {
        horse.teleportTo(horse.getX(), horse.getY() + 100, horse.getZ());
        horse.hurt(DamageSource.OUT_OF_WORLD, Float.MAX_VALUE);
    }
}