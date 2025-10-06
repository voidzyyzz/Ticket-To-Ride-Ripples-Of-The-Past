package com.voidzyy.rotp_ttr.action;

import com.github.standobyte.jojo.action.ActionTarget;
import com.github.standobyte.jojo.action.stand.StandEntityAction;
import com.github.standobyte.jojo.entity.stand.StandEntity;
import com.github.standobyte.jojo.entity.stand.StandEntityTask;
import com.github.standobyte.jojo.power.impl.stand.IStandPower;
import com.github.standobyte.jojo.util.mod.JojoModUtil;
import com.voidzyy.rotp_ttr.init.InitParticle;
import com.voidzyy.rotp_ttr.items.TearsBlade;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.LightningBoltEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class SingleLightingHit extends StandEntityAction {

    public SingleLightingHit(StandEntityAction.Builder builder) {
        super(builder);
    }

    @Override
    public void onHoldTickClientEffect(LivingEntity user, IStandPower power, int ticksHeld, boolean reqFulfilled, boolean reqStateChanged) {
        if (reqFulfilled) {
            StandEntity standEntity = (StandEntity) power.getStandManifestation();
            RayTraceResult result = JojoModUtil.rayTrace(user, 100, entity -> entity != standEntity);
            Vector3d pos = result.getLocation();

            // 修复：使用get()获取实际的粒子类型
            user.level.addParticle(InitParticle.GOLDLIGHT.get(),
                    pos.x, pos.y(), pos.z(),
                    0, 0, 0);
        }
    }

    @Override
    public void standPerform(World world, StandEntity standEntity, IStandPower userPower, StandEntityTask task) {
        if (!world.isClientSide()) {
            LivingEntity user = userPower.getUser();

            // 获取玩家视角正前方45度锥形范围内的所有实体
            List<LivingEntity> targetsInView = getTargetsInViewCone(user, 100, 45);

            // 筛选有TEARS标签的实体
            List<LivingEntity> validTargets = targetsInView.stream()
                    .filter(TearsBlade::isEntityMarked)
                    .collect(Collectors.toList());

            if (!validTargets.isEmpty()) {
                // 选择最近的或最强的目标（这里选择最近的目标）
                Optional<LivingEntity> targetOpt = validTargets.stream()
                        .min(Comparator.comparingDouble(e -> e.distanceToSqr(user)));

                if (targetOpt.isPresent()) {
                    LivingEntity target = targetOpt.get();
                    Vector3d pos = target.position();

                    int lightningCount = calculateLightningCount(world);
                    float baseDamage = calculateBaseDamage(world);

                    for (int i = 0; i < lightningCount; i++) {
                        LightningBoltEntity bolt = EntityType.LIGHTNING_BOLT.create(world);
                        bolt.moveTo(pos.x + (world.random.nextDouble() - 0.5) * 2,
                                pos.y,
                                pos.z + (world.random.nextDouble() - 0.5) * 2);

                        bolt.setDamage(baseDamage);
                        world.addFreshEntity(bolt);
                    }
                }
            }
        }
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

    // 获取视角锥形范围内的实体
    private List<LivingEntity> getTargetsInViewCone(LivingEntity user, double range, float angleDegrees) {
        Vector3d lookVec = user.getLookAngle();
        Vector3d userPos = user.getEyePosition(1.0F);

        return user.level.getEntitiesOfClass(LivingEntity.class,
                new AxisAlignedBB(userPos, userPos).inflate(range),
                entity -> {
                    if (entity == user) return false;

                    Vector3d toTarget = entity.position().subtract(userPos).normalize();
                    double dot = lookVec.dot(toTarget);
                    double angleRad = Math.acos(dot);
                    double angleDeg = Math.toDegrees(angleRad);

                    return angleDeg <= angleDegrees;
                });
    }

    @Override
    public void holdTick(World world, LivingEntity user, IStandPower power, int ticksHeld, ActionTarget target, boolean requirementsFulfilled) {
        if (!world.isClientSide() && requirementsFulfilled) {
            // 蓄力时的效果可以添加服务端逻辑
            if (ticksHeld % 10 == 0) {
                world.getEntitiesOfClass(LivingEntity.class,
                                new AxisAlignedBB(user.blockPosition()).inflate(5),
                                e -> e != user && TearsBlade.isEntityMarked(e))
                        .forEach(e -> e.knockback(0.2F, user.getX() - e.getX(), user.getZ() - e.getZ()));
            }
        }
    }

    @Override
    protected boolean standKeepsTarget(ActionTarget target) {
        return false;
    }
}