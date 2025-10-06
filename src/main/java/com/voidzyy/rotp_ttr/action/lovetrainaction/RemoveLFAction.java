package com.voidzyy.rotp_ttr.action.lovetrainaction;

import com.github.standobyte.jojo.action.ActionTarget;
import com.github.standobyte.jojo.action.stand.StandAction;
import com.github.standobyte.jojo.power.impl.stand.IStandPower;
import com.voidzyy.rotp_ttr.init.InitStatusEffect;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.potion.EffectInstance;
import net.minecraft.world.World;

public class RemoveLFAction extends StandAction {
    public RemoveLFAction(StandAction.Builder builder){super(builder);}

    public TargetRequirement getTargetRequirement() {
        return TargetRequirement.ENTITY;
    }

    @Override
    protected void perform(World world, LivingEntity user, IStandPower power, ActionTarget target) {
        Entity entity = target.getEntity();
        if (entity instanceof LivingEntity){
            LivingEntity livingEntity = (LivingEntity) entity;
            livingEntity.removeEffect(new EffectInstance(InitStatusEffect.LF.get(), 1200, 0, false, false, true).getEffect());
        }
    }
}