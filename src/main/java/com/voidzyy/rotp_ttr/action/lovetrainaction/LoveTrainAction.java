package com.voidzyy.rotp_ttr.action;

import com.github.standobyte.jojo.action.ActionConditionResult;
import com.github.standobyte.jojo.action.ActionTarget;
import com.github.standobyte.jojo.action.stand.StandEntityAction;
import com.github.standobyte.jojo.entity.stand.StandEntity;
import com.github.standobyte.jojo.entity.stand.StandEntityTask;
import com.github.standobyte.jojo.power.impl.stand.IStandPower;
import com.voidzyy.rotp_ttr.init.InitStatusEffect;
import net.minecraft.entity.LivingEntity;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.potion.EffectInstance;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;

public class LoveTrainAction extends StandEntityAction {

    public LoveTrainAction(Builder builder) {
        super(builder);
    }

    @Override
    protected ActionConditionResult checkStandConditions(StandEntity stand, IStandPower power, ActionTarget target) {
        LivingEntity user = power.getUser();

        // 检查是否已经有LOVE_TRAIN效果
        if (user.hasEffect(InitStatusEffect.LOVE_TRAIN.get())) {
            return ActionConditionResult.createNegative(new TranslationTextComponent("message.rotp_ttr.already_has_love_train"));
        }

        return ActionConditionResult.POSITIVE;
    }

    @Override
    public void standPerform(World world, StandEntity standEntity, IStandPower userPower, StandEntityTask task) {
        if (world.isClientSide()) return;

        LivingEntity user = userPower.getUser();

        // 添加LOVE_TRAIN效果（30秒）
        user.addEffect(new EffectInstance(
                InitStatusEffect.LOVE_TRAIN.get(),
                999 * 20,  // 30秒持续时间
                0,        // 放大等级
                false,    // 环境效果
                false,     // 显示粒子
                true      // 显示图标
        ));

        // 播放激活音效
        world.playSound(null, user.getX(), user.getY(), user.getZ(),
                SoundEvents.BEACON_ACTIVATE, SoundCategory.PLAYERS,
                1.0F, 0.8F + world.random.nextFloat() * 0.4F);

        }
    }
