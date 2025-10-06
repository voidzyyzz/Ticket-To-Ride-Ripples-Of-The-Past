package com.voidzyy.rotp_ttr.action;

import com.github.standobyte.jojo.action.ActionConditionResult;
import com.github.standobyte.jojo.action.ActionTarget;
import com.github.standobyte.jojo.action.stand.StandEntityAction;
import com.github.standobyte.jojo.entity.stand.StandEntity;
import com.github.standobyte.jojo.entity.stand.StandEntityTask;
import com.github.standobyte.jojo.power.impl.stand.IStandPower;
import com.voidzyy.rotp_ttr.init.InitStatusEffect;
import net.minecraft.entity.LivingEntity;
import net.minecraft.potion.EffectInstance;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.particles.ParticleTypes;

public class GiveLFAction extends StandEntityAction {

    public GiveLFAction(Builder builder) {
        super(builder);
    }

    @Override
    protected ActionConditionResult checkStandConditions(StandEntity stand, IStandPower power, ActionTarget target) {
        // 使用正确的方法检查目标是否为实体
        if (target.getType() != ActionTarget.TargetType.ENTITY || !(target.getEntity() instanceof LivingEntity)) {
            return ActionConditionResult.createNegative(new TranslationTextComponent("message.rotp_ttr.no_target"));
        }

        LivingEntity targetEntity = (LivingEntity) target.getEntity();

        if (targetEntity.hasEffect(InitStatusEffect.LF.get())) {
            return ActionConditionResult.createNegative(new TranslationTextComponent("message.rotp_ttr.target_has_lf"));
        }

        return ActionConditionResult.POSITIVE;
    }

    @Override
    public void standPerform(World world, StandEntity standEntity, IStandPower userPower, StandEntityTask task) {
        if (world.isClientSide()) return;

        ActionTarget target = task.getTarget();
        if (target.getType() != ActionTarget.TargetType.ENTITY || !(target.getEntity() instanceof LivingEntity)) {
            return;
        }

        LivingEntity targetEntity = (LivingEntity) target.getEntity();
        LivingEntity user = userPower.getUser();

        // 根据resolve等级计算持续时间
        int duration = calculateDuration(userPower);

        targetEntity.addEffect(new EffectInstance(
                InitStatusEffect.LF.get(),
                duration, 0, false, true, true
        ));

        // 音效和粒子效果
        playEffects(world, targetEntity);
    }

    private int calculateDuration(IStandPower power) {
        int baseDuration = 20 * 20; // 20秒基础
        return power.getResolveLevel() >= 5 ? baseDuration + (10 * 20) : baseDuration;
    }

    private void playEffects(World world, LivingEntity target) {
        world.playSound(null, target.getX(), target.getY(), target.getZ(),
                SoundEvents.EVOKER_CAST_SPELL, SoundCategory.PLAYERS,
                1.0F, 1.2F + world.random.nextFloat() * 0.2F);

        if (world instanceof ServerWorld) {
            ServerWorld serverWorld = (ServerWorld) world;
            serverWorld.sendParticles(ParticleTypes.WITCH,
                    target.getX(), target.getY() + 1.0, target.getZ(),
                    30, 0.5, 0.5, 0.5, 0.1);

            serverWorld.sendParticles(ParticleTypes.ENCHANT,
                    target.getX(), target.getY() + 1.5, target.getZ(),
                    50, 0.7, 0.7, 0.7, 0.2);
        }
    }
}