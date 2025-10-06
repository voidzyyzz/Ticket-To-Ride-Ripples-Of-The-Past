package com.voidzyy.rotp_ttr.util;

import com.github.standobyte.jojo.init.ModStatusEffects;
import com.github.standobyte.jojo.power.impl.stand.IStandPower;
import com.github.standobyte.jojo.power.impl.stand.stats.StandStats;
import com.github.standobyte.jojo.power.impl.stand.type.EntityStandType;
import com.voidzyy.rotp_ttr.init.InitStands;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.monster.ZombifiedPiglinEntity;
import net.minecraft.entity.monster.piglin.PiglinEntity;
import net.minecraft.entity.passive.BeeEntity;
import net.minecraft.entity.passive.WolfEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.potion.EffectInstance;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingSetAttackTargetEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.List;

@Mod.EventBusSubscriber(modid = "rotp_ttr")
public class StandAggroHandler {

    // 1. 持续给予 integrated_stand 效果
    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase == TickEvent.Phase.END && event.player != null) {
            PlayerEntity player = event.player;
            if (isStandUser(player, InitStands.TICKET_TO_RIDE_STAND.getStandType())) {
                // 给予持续效果，时间设为足够长(20秒)，并持续刷新
                player.addEffect(new EffectInstance(
                        ModStatusEffects.INTEGRATED_STAND.get(),
                        10,  // 20秒*20ticks
                        0,
                        false,
                        false,
                        true
                ));
            }
        }
    }

    // 1. 检测生物设置目标（Forge 提供的事件）
    @SubscribeEvent
    public static void onLivingSetAttackTarget(LivingSetAttackTargetEvent event) {
        if (!(event.getEntityLiving() instanceof MobEntity)) return;

        MobEntity mob = (MobEntity) event.getEntityLiving();
        LivingEntity target = event.getTarget();

        if (target instanceof PlayerEntity) {
            PlayerEntity player = (PlayerEntity) target;

            // 检查是否是替身玩家
            if (isStandUser(player, InitStands.TICKET_TO_RIDE_STAND.getStandType())) {
                // 检查玩家是否主动攻击
                if (!isPlayerAggressive(player, mob)) {
                    // 检查是否应该取消仇恨
                    if (shouldCancelAggro(mob, player)) {
                        mob.setTarget(null); // 直接清除目标
                        cancelGroupAggro(mob, player); // 处理集体仇恨
                    }
                }
            }
        }
    }

    // 2. 检测玩家攻击行为（确保玩家主动攻击时不会触发仇恨抑制）
    @SubscribeEvent
    public static void onLivingAttack(LivingAttackEvent event) {
        if (event.getSource().getEntity() instanceof PlayerEntity) {
            PlayerEntity player = (PlayerEntity) event.getSource().getEntity();
            LivingEntity target = event.getEntityLiving();

            // 如果玩家攻击生物，则允许该生物反击
            if (target instanceof MobEntity) {
                ((MobEntity) target).setTarget(player);
            }
        }
    }

    // ========== 核心判断方法 ==========
    private static boolean isStandUser(PlayerEntity player, EntityStandType<StandStats> standType) {
        return IStandPower.getStandPowerOptional(player)
                .map(standPower -> standPower.getType() == standType)
                .orElse(false);
    }

    private static boolean isPlayerAggressive(PlayerEntity player, MobEntity mob) {
        // 1.16.5 的语法：检查玩家是否攻击过该生物
        return mob.getLastHurtByMob() == player ||
                player.getLastHurtMob() == mob;
    }

    private static boolean shouldCancelAggro(MobEntity mob, PlayerEntity player) {
        // 普通怪物：总是抑制仇恨
        if (!(mob instanceof PiglinEntity ||
                mob instanceof ZombifiedPiglinEntity ||
                mob instanceof WolfEntity ||
                mob instanceof BeeEntity)) {
            return true;
        }

        // 特殊生物条件检查
        if (mob instanceof PiglinEntity) {
            return !((PiglinEntity) mob).isAggressive();
        }
        if (mob instanceof ZombifiedPiglinEntity) {
            return ((ZombifiedPiglinEntity) mob).getLastHurtByMob() != player;
        }
        if (mob instanceof WolfEntity) {
            return !((WolfEntity) mob).isAngry();
        }
        if (mob instanceof BeeEntity) {
            return ((BeeEntity) mob).getTarget() != player;
        }

        return false;
    }

    // ========== 集体仇恨处理 ==========
    private static void cancelGroupAggro(MobEntity sourceMob, PlayerEntity player) {
        Class<? extends MobEntity> mobClass = null;
        double searchRange = 16.0D;

        if (sourceMob instanceof PiglinEntity || sourceMob instanceof ZombifiedPiglinEntity) {
            mobClass = sourceMob instanceof PiglinEntity ? PiglinEntity.class : ZombifiedPiglinEntity.class;
            searchRange = 24.0D; // 猪灵系有更大的仇恨范围
        }
        else if (sourceMob instanceof WolfEntity) {
            mobClass = WolfEntity.class;
        }
        else if (sourceMob instanceof BeeEntity) {
            mobClass = BeeEntity.class;
            searchRange = 20.0D; // 蜜蜂有较远的巡逻范围
        }

        if (mobClass != null) {
            // 1.16.5 的语法获取附近同类生物
            List<? extends MobEntity> groupMobs = sourceMob.level.getEntitiesOfClass(
                    mobClass,
                    sourceMob.getBoundingBox().inflate(searchRange),
                    e -> e != sourceMob && e.getTarget() == player
            );

            // 清除整个群体的仇恨
            groupMobs.forEach(mob -> mob.setTarget(null));
        }
    }
}