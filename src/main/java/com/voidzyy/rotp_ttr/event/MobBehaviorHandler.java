package com.voidzyy.rotp_ttr.event;

import com.voidzyy.rotp_ttr.init.InitStatusEffect;
import com.voidzyy.rotp_ttr.init.InitStands;
import com.voidzyy.rotp_ttr.items.TearsBlade;
import net.minecraft.entity.*;
import net.minecraft.entity.ai.goal.*;
import net.minecraft.entity.monster.MonsterEntity;
import net.minecraft.entity.passive.BatEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.*;

@Mod.EventBusSubscriber
public class MobBehaviorHandler {
    private static final Map<MobEntity, Integer> MOB_TIMERS = new WeakHashMap<>();
    private static final int MAX_LIFETIME = 200; // 10秒 (20 ticks/second)

    // 被动生物列表
    private static final Set<EntityType<?>> PASSIVE_MOBS = new HashSet<>(Arrays.asList(
            EntityType.BAT,
            EntityType.SQUID,
            EntityType.DOLPHIN
    ));

    // 负面效果池
    private static final List<EffectInstance> NEGATIVE_EFFECTS = Arrays.asList(
            new EffectInstance(Effects.MOVEMENT_SLOWDOWN, 200, 1), // 减速10秒
            new EffectInstance(Effects.WEAKNESS, 200, 0),         // 虚弱10秒
            new EffectInstance(Effects.POISON, 100, 0),           // 中毒5秒
            new EffectInstance(Effects.CONFUSION, 300, 0),          // 反胃15秒
            new EffectInstance(Effects.BLINDNESS, 200, 0),          // 失明10秒
            new EffectInstance(Effects.WITHER, 200, 0)
    );

    @SubscribeEvent
    public static void onEntitySpawn(EntityJoinWorldEvent event) {
        if (event.getWorld().isClientSide()) return;

        if (event.getEntity() instanceof MobEntity) {
            MobEntity mob = (MobEntity) event.getEntity();

            // 只有带有TEARS_EFFECT的生物才进行处理
            if (!mob.hasEffect(InitStatusEffect.TEARS_EFFECT.get())) {
                return;
            }

            // 初始化计时器
            MOB_TIMERS.put(mob, MAX_LIFETIME);

            // 修改AI行为
            modifyMobAI(mob);
        }
    }

    @SubscribeEvent
    public static void onLivingUpdate(LivingEvent.LivingUpdateEvent event) {
        if (event.getEntity().level.isClientSide()) return;

        if (event.getEntityLiving() instanceof MobEntity) {
            MobEntity mob = (MobEntity) event.getEntityLiving();

            // 只处理有TEARS_EFFECT和计时器的生物
            if (!mob.hasEffect(InitStatusEffect.TEARS_EFFECT.get()) || !MOB_TIMERS.containsKey(mob)) {
                return;
            }

            // 计时器递减
            int remainingTime = MOB_TIMERS.get(mob) - 1;
            MOB_TIMERS.put(mob, remainingTime);

            // 检查是否超时
            if (remainingTime <= 0) {
                mob.remove();
                MOB_TIMERS.remove(mob);
                return;
            }

            // 每2秒检查一次标记生物
            if (mob.tickCount % 40 == 0) {
                LivingEntity markedTarget = findMarkedTarget(mob);

                if (markedTarget != null) {
                    // 找到目标则重置计时器
                    MOB_TIMERS.put(mob, MAX_LIFETIME);

                    // 特殊处理被动生物
                    if (PASSIVE_MOBS.contains(mob.getType())) {
                        applyPassiveEffects(mob, markedTarget);
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public static void onLivingAttack(LivingAttackEvent event) {
        if (event.getSource().getEntity() instanceof MobEntity) {
            MobEntity attacker = (MobEntity) event.getSource().getEntity();
            LivingEntity target = event.getEntityLiving();

            // 只有带有TEARS_EFFECT的生物才施加负面效果
            if (attacker.hasEffect(InitStatusEffect.TEARS_EFFECT.get()) && TearsBlade.isEntityMarked(target)) {
                // 随机应用3种负面效果
                Collections.shuffle(NEGATIVE_EFFECTS);
                for (int i = 0; i < 3 && i < NEGATIVE_EFFECTS.size(); i++) {
                    target.addEffect(new EffectInstance(NEGATIVE_EFFECTS.get(i)));
                }
            }
        }
    }

    // 修改生物AI行为（仅对有TEARS_EFFECT的生物）
    private static void modifyMobAI(MobEntity mob) {
        // 清除所有原版目标
        clearAllGoals(mob.targetSelector);

        // 为怪物添加对标记玩家的仇恨
        if (mob instanceof MonsterEntity) {
            mob.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(
                    mob,
                    LivingEntity.class,
                    10,
                    true,
                    false,
                    TearsBlade::isEntityMarked
            ));
        }

        // 确保不会攻击替身使者
        mob.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(
                mob,
                LivingEntity.class,
                10,
                false,
                false,
                e -> e.getType() != InitStands.TICKET_TO_RIDE_STAND.getEntityType()
        ));
    }

    // 安全清除所有目标
    private static void clearAllGoals(GoalSelector selector) {
        Iterator<PrioritizedGoal> iterator = selector.getRunningGoals().iterator();
        while (iterator.hasNext()) {
            PrioritizedGoal goal = iterator.next();
            selector.removeGoal(goal.getGoal());
        }
    }

    // 查找标记目标
    private static LivingEntity findMarkedTarget(MobEntity mob) {
        return mob.level.getEntitiesOfClass(
                LivingEntity.class,
                mob.getBoundingBox().inflate(20),
                entity -> TearsBlade.isEntityMarked(entity) && mob.canSee(entity)
        ).stream().findFirst().orElse(null);
    }

    // 被动生物效果施加
    private static void applyPassiveEffects(MobEntity mob, LivingEntity target) {
        if (mob instanceof BatEntity) {
            // 蝙蝠特殊效果：附加失明和缓慢
            target.addEffect(new EffectInstance(Effects.BLINDNESS, 200, 0));
            target.addEffect(new EffectInstance(Effects.MOVEMENT_SLOWDOWN, 200, 1));
        } else {
            // 其他被动生物默认效果
            Collections.shuffle(NEGATIVE_EFFECTS);
            target.addEffect(new EffectInstance(NEGATIVE_EFFECTS.get(0)));
        }
    }
}