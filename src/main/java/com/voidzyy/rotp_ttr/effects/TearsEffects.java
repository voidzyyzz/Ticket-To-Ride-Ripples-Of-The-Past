package com.voidzyy.rotp_ttr.effects;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.potion.Effect;
import net.minecraft.potion.EffectType;
import net.minecraft.potion.Effects;

public class TearsEffects extends Effect {

    public TearsEffects(EffectType type, int color) {
        super(type, color);
    }
    public boolean isApplicable(LivingEntity entity) {
        // 强制对所有实体（包括凋零）生效
        return true;
    }
    @Override
    public void applyEffectTick(LivingEntity entity, int amplifier) {
        if (entity.level.isClientSide()) return; // 仅在服务端执行

        // 1. 阻止玩家回血
        if (entity instanceof PlayerEntity) {
            handlePlayerHealth((PlayerEntity) entity);
        }
        // 2. 对凋零的特殊处理（如果需要）
        else if (entity.getType() == EntityType.WITHER) {
            handleWither((LivingEntity) entity);
        }
    }

    // 处理玩家血量
    private void handlePlayerHealth(PlayerEntity player) {
        // 记录当前血量（用于检测回血）
        float currentHealth = player.getHealth();

        // 取消生命恢复效果
        if (player.hasEffect(Effects.REGENERATION)) {
            player.removeEffect(Effects.REGENERATION);
        }

        // 如果血量异常增加（如被其他模组治疗），强制重置
        if (player.getHealth() > currentHealth) {
            player.setHealth(currentHealth);
        }

    }

    // 处理凋零（如果需要）
    private void handleWither(LivingEntity wither) {
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return true;
    }

}