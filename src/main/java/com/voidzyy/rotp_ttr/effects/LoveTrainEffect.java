package com.voidzyy.rotp_ttr.effects;

import net.minecraft.entity.LivingEntity;
import net.minecraft.potion.Effect;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.EffectType;

import java.util.ArrayList;
import java.util.List;

public class LoveTrainEffect extends Effect {
    public LoveTrainEffect() {
        super(EffectType.BENEFICIAL, 16766720);
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return true;
    }

    @Override
    public void applyEffectTick(LivingEntity livingEntity, int amplifier) {
        this.clearNegativeEffects(livingEntity);
    }

    private void clearNegativeEffects(LivingEntity livingEntity) {
        List<Effect> effectsToRemove = new ArrayList<>();

        for (EffectInstance effectInstance : livingEntity.getActiveEffects()) {
            Effect effect = effectInstance.getEffect();
            if (effect.getCategory() == EffectType.HARMFUL) {
                effectsToRemove.add(effect);
            }
        }

        for (Effect effect : effectsToRemove) {
            livingEntity.removeEffect(effect);
        }
    }

    }


