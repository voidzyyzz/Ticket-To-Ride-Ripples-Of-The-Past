package com.voidzyy.rotp_ttr.action.lovetrainaction;

import com.github.standobyte.jojo.action.ActionConditionResult;
import com.github.standobyte.jojo.action.ActionTarget;
import com.github.standobyte.jojo.action.stand.StandEntityAction;
import com.github.standobyte.jojo.entity.stand.StandEntity;
import com.github.standobyte.jojo.entity.stand.StandEntityTask;
import com.github.standobyte.jojo.power.impl.stand.IStandPower;
import com.voidzyy.rotp_ttr.init.InitParticle;
import com.voidzyy.rotp_ttr.init.InitStatusEffect;
import net.minecraft.entity.LivingEntity;
import net.minecraft.particles.BasicParticleType;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;

public class DispelLoveTrainAction extends StandEntityAction {

    public DispelLoveTrainAction(Builder builder) {
        super(builder);
    }

    @Override
    protected ActionConditionResult checkStandConditions(StandEntity stand, IStandPower power, ActionTarget target) {
        LivingEntity user = power.getUser();

        // 检查自己是否有LOVE_TRAIN效果
        if (!user.hasEffect(InitStatusEffect.LOVE_TRAIN.get())) {
            return ActionConditionResult.createNegative(new TranslationTextComponent("message.rotp_ttr.no_love_train_to_dispel"));
        }

        return ActionConditionResult.POSITIVE;
    }

    @Override
    public void standPerform(World world, StandEntity standEntity, IStandPower userPower, StandEntityTask task) {
        if (world.isClientSide()) return;

        LivingEntity user = userPower.getUser();

        // 移除LOVE_TRAIN效果
        user.removeEffect(InitStatusEffect.LOVE_TRAIN.get());

        // 播放解除音效和粒子效果
        playDispelEffects(world, user);
    }

    private void playDispelEffects(World world, LivingEntity user) {
        // 播放解除音效
        world.playSound(null, user.getX(), user.getY(), user.getZ(),
                SoundEvents.BEACON_DEACTIVATE, SoundCategory.PLAYERS,
                1.0F, 0.8F + world.random.nextFloat() * 0.4F);

        // 服务器端生成粒子效果
        if (world instanceof ServerWorld) {
            ServerWorld serverWorld = (ServerWorld) world;

            // 获取实际的粒子类型对象
            BasicParticleType particleType = InitParticle.GOLDLIGHT.get();

            // 解除粒子效果
            serverWorld.sendParticles(particleType,
                    user.getX(), user.getY() + 1.0, user.getZ(),
                    50, 0.5, 0.5, 0.5, 0.2);
        }
    }
}