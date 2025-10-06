package com.voidzyy.rotp_ttr.action;

import com.github.standobyte.jojo.action.ActionTarget;
import com.github.standobyte.jojo.action.stand.StandEntityAction;
import com.github.standobyte.jojo.entity.stand.StandEntity;
import com.github.standobyte.jojo.entity.stand.StandEntityTask;
import com.github.standobyte.jojo.power.impl.stand.IStandPower;
import com.voidzyy.rotp_ttr.items.TearsBlade;
import net.minecraft.block.BlockState;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.passive.horse.HorseEntity;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;

import java.util.Random;

public class MobCrash extends StandEntityAction {

    public MobCrash(Builder builder) {
        super(builder);
    }

    @Override
    public void standPerform(World world, StandEntity standEntity, IStandPower userPower, StandEntityTask task) {
        if (world.isClientSide()) return;

        LivingEntity user = userPower.getUser();
        ActionTarget target = task.getTarget();

        // 获取最近的带有TEARS标签的实体作为目标
        LivingEntity targetEntity = findNearestMarkedEntity(world, user);
        if (targetEntity == null) return;

        // 在目标实体附近生成带有不幸效果的马
        spawnUnluckyHorse(world, targetEntity);
    }

    /**
     * 查找最近的带有TEARS标签的实体
     */
    private LivingEntity findNearestMarkedEntity(World world, LivingEntity user) {
        LivingEntity nearest = null;
        double closestDistance = Double.MAX_VALUE;

        for (LivingEntity entity : world.getEntitiesOfClass(LivingEntity.class,
                user.getBoundingBox().inflate(100.0))) {
            if (entity != user && TearsBlade.isEntityMarked(entity)) {
                double distance = entity.distanceToSqr(user);
                if (distance < closestDistance) {
                    closestDistance = distance;
                    nearest = entity;
                }
            }
        }

        return nearest;
    }

    /**
     * 在目标实体附近生成带有不幸效果的马
     */
    private void spawnUnluckyHorse(World world, LivingEntity target) {
        // 尝试最多10次寻找合适位置
        for (int i = 0; i < 10; i++) {
            // 在目标周围15格随机位置生成
            Vector3d spawnPos = findValidSpawnPosition(world, target);
            if (spawnPos == null) continue;

            HorseEntity horse = EntityType.HORSE.create(world);
            if (horse == null) return;

            // 设置马的位置和属性
            horse.moveTo(spawnPos.x(), spawnPos.y(), spawnPos.z(),
                    world.random.nextFloat() * 360.0F, 0.0F);
            horse.setTamed(false); // 不可骑乘
            horse.setAge(0); // 成年马

            // 给马添加不幸效果（持续时间45秒）
            horse.addEffect(new EffectInstance(Effects.UNLUCK, 45 * 20, 0, true, true));

            // 设置马的一些属性使其更具威胁性
            horse.setHealth(horse.getMaxHealth()); // 满血
            horse.setAggressive(true); // 具有攻击性

            // 添加到世界
            world.addFreshEntity(horse);
            return; // 成功生成后退出
        }
    }

    /**
     * 寻找有效的生成位置
     */
    private Vector3d findValidSpawnPosition(World world, LivingEntity target) {
        Random random = world.random;

        // 在15格半径的球体内随机位置
        for (int i = 0; i < 10; i++) {
            // 随机角度和距离
            double distance = 5 + random.nextDouble() * 10; // 5-15格距离
            double yaw = random.nextDouble() * Math.PI * 2; // 0-360度
            double pitch = random.nextDouble() * Math.PI - Math.PI / 2; // -90到90度

            // 计算位置
            double x = target.getX() + distance * Math.cos(yaw) * Math.cos(pitch);
            double z = target.getZ() + distance * Math.sin(yaw) * Math.cos(pitch);
            double y = target.getY() + distance * Math.sin(pitch);

            // 检查位置是否有效
            BlockPos pos = new BlockPos(x, y, z);
            if (isValidSpawnPosition(world, pos)) {
                return new Vector3d(x, y, z);
            }
        }

        // 如果找不到合适位置，返回目标前方5格位置
        Vector3d look = target.getLookAngle();
        return target.position().add(look.scale(5)).add(0, 0.5, 0);
    }

    /**
     * 检查生成位置是否有效
     */
    private boolean isValidSpawnPosition(World world, BlockPos pos) {
        // 检查位置是否在边界内
        if (!world.isInWorldBounds(pos)) {
            return false;
        }

        // 检查位置是否在水或熔岩中
        BlockState blockState = world.getBlockState(pos);
        if (blockState.getMaterial().isLiquid()) {
            return false;
        }

        // 检查位置是否有碰撞体积
        if (!blockState.getCollisionShape(world, pos).isEmpty()) {
            return false;
        }

        // 检查位置上方是否有碰撞体积
        if (!world.getBlockState(pos.above()).getCollisionShape(world, pos.above()).isEmpty()) {
            return false;
        }

        // 检查位置是否在固体方块上
        return !world.getBlockState(pos.below()).getCollisionShape(world, pos.below()).isEmpty();
    }

    @Override
    protected boolean standKeepsTarget(ActionTarget target) {
        // 只有当目标是实体且带有TEARS标签时才保持目标
        return target.getType() == ActionTarget.TargetType.ENTITY &&
                target.getEntity() instanceof LivingEntity &&
                TearsBlade.isEntityMarked((LivingEntity) target.getEntity());
    }
}