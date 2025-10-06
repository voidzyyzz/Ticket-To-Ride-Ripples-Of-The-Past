package com.voidzyy.rotp_ttr.action;

import com.github.standobyte.jojo.action.ActionTarget;
import com.github.standobyte.jojo.action.stand.StandEntityAction;
import com.github.standobyte.jojo.entity.stand.StandEntity;
import com.github.standobyte.jojo.entity.stand.StandEntityTask;
import com.github.standobyte.jojo.power.impl.stand.IStandPower;
import com.voidzyy.rotp_ttr.init.InitSounds;
import com.voidzyy.rotp_ttr.items.TearsBlade;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.LightningBoltEntity;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;

import java.util.Random;

public class lightinghit extends StandEntityAction {

    public lightinghit(StandEntityAction.Builder builder) {
        super(builder);
    }

    @Override
    public void standPerform(World world, StandEntity standEntity, IStandPower userPower, StandEntityTask task) {
        if (!world.isClientSide()) {
            LivingEntity user = userPower.getUser();

            // 对世界中所有带有TEARS标签的实体降下雷电
            world.getEntitiesOfClass(LivingEntity.class, user.getBoundingBox().inflate(100.0))
                    .stream()
                    .filter(entity -> TearsBlade.isEntityMarked(entity) && entity != user)
                    .forEach(entity -> strikeEntityWithLightning(world, entity));
        }
    }

    // 对单个实体降下雷电
    private void strikeEntityWithLightning(World world, LivingEntity target) {
        Vector3d targetPos = target.position();
        Random random = world.random;

        // 计算闪电数量和基础伤害
        int lightningCount = calculateLightningCount(world);
        float baseDamage = calculateBaseDamage(world);

        // 生成多道闪电打击目标
        for (int i = 0; i < lightningCount; i++) {
            LightningBoltEntity bolt = EntityType.LIGHTNING_BOLT.create(world);

            // 随机偏移位置
            double offsetX = (random.nextDouble() - 0.5) * 2;
            double offsetZ = (random.nextDouble() - 0.5) * 2;
            bolt.moveTo(targetPos.x + offsetX, targetPos.y, targetPos.z + offsetZ);

            // 设置伤害
            bolt.setDamage(baseDamage);
            world.addFreshEntity(bolt);
        }

        // 播放音效
        world.playSound(null, targetPos.x, targetPos.y, targetPos.z,
                InitSounds.STAND_SUMMON_SOUND.get(),
                SoundCategory.HOSTILE, 1.0F, 0.8F + random.nextFloat() * 0.4F);
    }

    // 计算闪电数量（基于天气）
    private int calculateLightningCount(World world) {
        if (world.isThundering()) return 3;
        if (world.isRaining()) return 2;
        return 1;
    }

    // 计算基础伤害（基于天气）
    private float calculateBaseDamage(World world) {
        if (world.isThundering()) return 10.0F;
        if (world.isRaining()) return 8.0F;
        return 6.0F;
    }

    @Override
    public void onHoldTickClientEffect(LivingEntity user, IStandPower power, int ticksHeld, boolean reqFulfilled, boolean reqStateChanged) {
        if (reqFulfilled && ticksHeld % 10 == 0) {
            // 显示所有有TEARS标签的实体位置
            user.level.getEntitiesOfClass(LivingEntity.class, user.getBoundingBox().inflate(100.0))
                    .stream()
                    .filter(entity -> TearsBlade.isEntityMarked(entity) && entity != user)
                    .forEach(entity -> {
                        user.level.addParticle(ParticleTypes.SOUL_FIRE_FLAME,
                                entity.getX(),
                                entity.getY() + entity.getBbHeight(),
                                entity.getZ(),
                                0, 0.1, 0);
                    });
        }
    }

    @Override
    protected boolean standKeepsTarget(ActionTarget target) {
        return false;
    }

}