package com.voidzyy.rotp_ttr.effects;

import net.minecraft.entity.LivingEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.EffectType;

import java.util.Random;

public class EffectErosion extends BaseEffect {
    public EffectErosion(EffectType type, int color, boolean isInstant) {
        super(type, color, isInstant);
    }

    @Override
    protected boolean canApplyEffect(int remainingTicks, int level) {
        return remainingTicks % 7 == 0;
    }

    //这个是buff在身上持续作用的效果函数
    @Override
    public void applyEffectTick(LivingEntity living, int amplified) {
        amplified ++;
        Random ran = new Random();
        int co = ran.nextInt(5);
        for (EquipmentSlotType slot: EquipmentSlotType.values()) {
            DamageItemInSlot(slot, living, co*amplified);
        }

    }

    //我们的效果是要让装备盔甲值快速减少，所以增加一个Damage函数
    public void DamageItemInSlot(EquipmentSlotType slot, LivingEntity livingBase, int amount)
    {
        ItemStack stack = livingBase.getItemBySlot(slot);
        stack.hurtAndBreak(1, livingBase, (p_220287_1_) -> {
            p_220287_1_.broadcastBreakEvent(slot);
        });
    }

    //声明buff是好buff还是debuff
    @Override
    public boolean isBeneficial() {
        return false;
    }

}
