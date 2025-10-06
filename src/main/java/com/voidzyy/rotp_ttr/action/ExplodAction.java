package com.voidzyy.rotp_ttr.action;

import com.github.standobyte.jojo.action.ActionConditionResult;
import com.github.standobyte.jojo.action.stand.StandEntityAction;
import com.github.standobyte.jojo.entity.stand.StandEntity;
import com.github.standobyte.jojo.entity.stand.StandEntityTask;
import com.github.standobyte.jojo.power.impl.stand.IStandPower;
import com.voidzyy.rotp_ttr.init.InitSounds;
import com.voidzyy.rotp_ttr.items.TearsBlade;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.item.TNTEntity;
import net.minecraft.entity.monster.CreeperEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.Explosion;
import net.minecraft.world.World;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Random;

public class ExplodAction extends StandEntityAction {

    public ExplodAction(StandEntityAction.Builder builder) {
        super(builder);
    }

    @Override
    public void standPerform(World world, StandEntity standEntity, IStandPower userPower, StandEntityTask task) {
        if (!world.isClientSide() && world.getServer() != null) {
            LivingEntity user = userPower.getUser();

            // 1. 获取玩家视角正前方45度范围内的目标
            List<LivingEntity> targetsInView = getTargetsInViewCone(user, 45);
            Optional<LivingEntity> targetOpt = targetsInView.stream()
                    .filter(TearsBlade::isEntityMarked) // 修改为标签检测
                    .findFirst();

            if (targetOpt.isPresent()) {
                // 2. 找到目标，生成爆炸
                LivingEntity target = targetOpt.get();
                Vector3d pos = target.position();
                standEntity.playSound(InitSounds.STAND_UNSUMMON_SOUND.get(), 1F, 1);

                // 随机选择爆炸类型
                Random random = new Random();
                switch (random.nextInt(3)) {
                    case 0: // TNT爆炸
                        spawnTNTExplosion(world, pos);
                        break;
                    case 1: // 直接爆炸
                        world.explode(standEntity, pos.x, pos.y, pos.z, 4.0f, false, Explosion.Mode.BREAK);
                        break;
                    case 2: // 苦力怕爆炸
                        spawnCreeperExplosion(world, pos);
                        break;
                }
            } else {
            }
        }
    }


    // 生成TNT爆炸（保持不变）
    private void spawnTNTExplosion(World world, Vector3d pos) {
        TNTEntity tnt = new TNTEntity(world, pos.x, pos.y, pos.z, null);
        world.addFreshEntity(tnt);
        tnt.setFuse(10);
        Objects.requireNonNull(world.getServer()).execute(() -> {
            new Explosion(world, tnt, null, null, pos.x, pos.y, pos.z, 0.0F, false, Explosion.Mode.NONE).explode();
        });
    }

    // 生成苦力怕爆炸（保持不变）
    private void spawnCreeperExplosion(World world, Vector3d pos) {
        CreeperEntity creeper = new CreeperEntity(EntityType.CREEPER, world);
        creeper.setPos(pos.x, pos.y, pos.z);
        world.addFreshEntity(creeper);
        creeper.ignite();
    }

    // 获取视角锥形范围内的实体（保持不变）
    private List<LivingEntity> getTargetsInViewCone(LivingEntity user, float angleDegrees) {
        Vector3d lookVec = user.getLookAngle();
        Vector3d userPos = user.getEyePosition(1.0F);
        return user.level.getEntitiesOfClass(LivingEntity.class,
                new AxisAlignedBB(userPos, userPos).inflate(100),
                entity -> {
                    if (entity == user) return false;
                    Vector3d toTarget = entity.position().subtract(userPos).normalize();
                    double dot = lookVec.dot(toTarget);
                    double angleRad = Math.acos(dot);
                    return Math.toDegrees(angleRad) <= angleDegrees;
                });
    }

    @Override
    public boolean greenSelection(IStandPower power, ActionConditionResult conditionCheck) {
        return false;
    }
}