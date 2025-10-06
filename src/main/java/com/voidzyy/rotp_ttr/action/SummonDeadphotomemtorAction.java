package com.voidzyy.rotp_ttr.action;

import com.github.standobyte.jojo.action.stand.StandEntityAction;
import com.github.standobyte.jojo.entity.stand.StandEntity;
import com.github.standobyte.jojo.entity.stand.StandEntityTask;
import com.github.standobyte.jojo.power.impl.stand.IStandPower;
import com.voidzyy.rotp_ttr.entity.Deadphotomemtor;
import com.voidzyy.rotp_ttr.init.InitEntities;
import com.voidzyy.rotp_ttr.items.TearsBlade;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.world.World;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class SummonDeadphotomemtorAction extends StandEntityAction {

    public SummonDeadphotomemtorAction(Builder builder) {
        super(builder);
    }

    @Override
    public void standPerform(World world, StandEntity standEntity, IStandPower userPower, StandEntityTask task) {
        if (!world.isClientSide()) {
            // 获取周围有TEARS标记的实体，并按血量降序排序
            List<LivingEntity> markedEntities = world.getEntitiesOfClass(
                            LivingEntity.class,
                            new AxisAlignedBB(standEntity.blockPosition()).inflate(128.0D),
                            entity -> TearsBlade.isEntityMarked(entity) && entity != standEntity.getUser()
                    ).stream()
                    .sorted(Comparator.comparingDouble(LivingEntity::getHealth).reversed())
                    .collect(Collectors.toList());

            // 最多生成1个Deadphotomemtor
            int maxSummons = 1;
            for (int i = 0; i < Math.min(markedEntities.size(), maxSummons); i++) {
                LivingEntity target = markedEntities.get(i);
                summonDeadphotomemtorAboveTarget(world, target, standEntity.getUser());
            }
        }
    }

    private void summonDeadphotomemtorAboveTarget(World world, LivingEntity target, LivingEntity summoner) {
        // 设置生成位置（目标上方15格，X和Z轴有偏移）
        double spawnX = target.getX() - 15;
        double spawnY = target.getY() + 21.0D;
        double spawnZ = target.getZ() + 15.10;

        // 创建Deadphotomemtor实体
        Deadphotomemtor deadphotomemtor = new Deadphotomemtor(InitEntities.DPM.get(), world);
        deadphotomemtor.setPos(spawnX, spawnY, spawnZ);

        // 添加到世界
        world.addFreshEntity(deadphotomemtor);
    }
}