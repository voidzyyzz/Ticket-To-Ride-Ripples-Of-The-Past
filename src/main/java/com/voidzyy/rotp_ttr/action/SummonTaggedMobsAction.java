package com.voidzyy.rotp_ttr.action;

import com.github.standobyte.jojo.action.ActionConditionResult;
import com.github.standobyte.jojo.action.stand.StandEntityAction;
import com.github.standobyte.jojo.entity.stand.StandEntity;
import com.github.standobyte.jojo.entity.stand.StandEntityTask;
import com.github.standobyte.jojo.power.impl.stand.IStandPower;
import com.voidzyy.rotp_ttr.init.InitStatusEffect;
import com.voidzyy.rotp_ttr.init.InitSounds;
import com.voidzyy.rotp_ttr.items.TearsBlade;
import net.minecraft.entity.*;
import net.minecraft.entity.monster.*;
import net.minecraft.entity.passive.BeeEntity;
import net.minecraft.potion.EffectInstance;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;

import java.util.*;
import java.util.function.Supplier;

public class SummonTaggedMobsAction extends StandEntityAction {

    // 可生成的生物类型列表
    private static final List<Supplier<EntityType<? extends MobEntity>>> MOB_TYPES = Arrays.asList(
            () -> EntityType.BEE,
            () -> EntityType.PHANTOM,
            () -> EntityType.CAVE_SPIDER,
            () -> EntityType.WOLF,
            () -> EntityType.SILVERFISH
    );

    public SummonTaggedMobsAction(StandEntityAction.Builder builder) {
        super(builder);
    }

    @Override
    public void standPerform(World world, StandEntity standEntity, IStandPower userPower, StandEntityTask task) {
        if (!world.isClientSide()) {
            LivingEntity user = userPower.getUser();

            // 在10格范围内寻找被标记的生物
            world.getEntitiesOfClass(LivingEntity.class,
                            user.getBoundingBox().inflate(10),
                            entity -> TearsBlade.isEntityMarked(entity))
                    .forEach(markedEntity -> {
                        // 为每个标记生物生成3个相同类型的生物
                        EntityType<? extends MobEntity> chosenType = chooseRandomMobType(world);
                        for (int i = 0; i < 3; i++) {
                            spawnTaggedMob((ServerWorld) world, markedEntity.position(), chosenType);
                        }
                        standEntity.playSound(InitSounds.STAND_UNSUMMON_SOUND.get(), 1F, 1);
                    });
        }
    }

    private EntityType<? extends MobEntity> chooseRandomMobType(World world) {
        return MOB_TYPES.get(world.getRandom().nextInt(MOB_TYPES.size())).get();
    }

    private void spawnTaggedMob(ServerWorld world, Vector3d centerPos, EntityType<? extends MobEntity> mobType) {
        // 生成位置计算
        Vector3d spawnPos = calculateSpawnPos(world, centerPos, mobType);

        // 创建实体
        MobEntity mob = mobType.create(world);
        if (mob == null) return;

        // 设置位置和属性
        mob.moveTo(spawnPos.x(), spawnPos.y(), spawnPos.z(),
                world.getRandom().nextFloat() * 360.0F, 0.0F);

        // 特殊处理幻翼（只在晚上生成）
        if (mob instanceof PhantomEntity && !world.isNight()) {
            return;
        }

        // 特殊处理蜜蜂（50%概率生成幼年蜜蜂）
        if (mob instanceof BeeEntity && world.getRandom().nextBoolean()) {
            ((BeeEntity)mob).setBaby(true);
        }

        // 添加永久Tear效果
        mob.addEffect(new EffectInstance(
                InitStatusEffect.TEARS_EFFECT.get(),
                Integer.MAX_VALUE, // 永久持续时间
                0,
                false,
                false,
                true));

        // 添加到世界
        world.addFreshEntity(mob);

        // 播放音效
        world.playSound(null, spawnPos.x(), spawnPos.y(), spawnPos.z(),
                SoundEvents.EVOKER_CAST_SPELL,
                mob.getSoundSource(),
                1.0F, 0.8F + world.getRandom().nextFloat() * 0.4F);
    }

    private Vector3d calculateSpawnPos(ServerWorld world, Vector3d centerPos, EntityType<?> mobType) {
        Random random = world.getRandom();

        // 随机水平偏移（10格范围内）
        double offsetX = (random.nextDouble() - 0.5) * 20;
        double offsetZ = (random.nextDouble() - 0.5) * 20;

        // 高度处理
        double baseY = centerPos.y();
        double offsetY;

        if (mobType == EntityType.PHANTOM) {
            // 幻翼在10格高度生成
            offsetY = 10.0;
        } else if (mobType == EntityType.SILVERFISH) {
            // 蠹虫贴地生成
            offsetY = 0.1; // 略高于地面
        } else {
            // 其他生物在5格高度生成
            offsetY = 5.0;
        }

        return new Vector3d(
                centerPos.x() + offsetX,
                baseY + offsetY,
                centerPos.z() + offsetZ);
    }

    @Override
    public boolean greenSelection(IStandPower power, ActionConditionResult conditionCheck) {
        return false;
    }
}