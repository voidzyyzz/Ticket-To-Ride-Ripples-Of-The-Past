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
import net.minecraft.potion.EffectInstance;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.vector.Vector3d;
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
        if (user.hasEffect(InitStatusEffect.LOVE_TRAIN.get())) {
            return ActionConditionResult.createNegative(new TranslationTextComponent("message.rotp_ttr.already_has_love_train"));
        }
        return ActionConditionResult.POSITIVE;
    }

    @Override
    public void onHoldTickClientEffect(LivingEntity user, IStandPower power, int ticksHeld, boolean reqFulfilled, boolean reqStateChanged) {
        if (reqFulfilled) {
            // 从底部生成螺旋上升的GoldLight粒子
            if (ticksHeld % 3 == 0) {
                double progress = ticksHeld / 40.0;
                double radius = 0.8 + progress * 0.5;
                double startY = user.getY(); // 从使用者脚部开始
                double endY = user.getY() + user.getBbHeight() + 1.0; // 到碰撞箱顶部+1

                for (int i = 0; i < 4; i++) {
                    double angle = Math.toRadians(ticksHeld * 5 + i * 90);
                    // 修正：使用新的Vector3d构造函数而不是withY方法
                    Vector3d startPos = new Vector3d(
                            user.getX() + Math.cos(angle) * radius,
                            startY,
                            user.getZ() + Math.sin(angle) * radius
                    );

                    // 计算粒子运动方向
                    double verticalSpeed = 0.05 * (1 + progress * 0.5);

                    user.level.addParticle(InitParticle.GOLDLIGHT.get(),
                            startPos.x, startPos.y, startPos.z,
                            0, verticalSpeed, 0);

                }
            }

            // 随机逃逸粒子（保持不变）
            if (ticksHeld % 5 == 0) {
                Vector3d center = user.position().add(0, 1, 0);
                for (int i = 0; i < 3; i++) {
                    double angle = Math.toRadians(user.level.random.nextFloat() * 360);
                    double radius = user.level.random.nextDouble() * 1.5;

                    Vector3d pos = center.add(
                            Math.cos(angle) * radius,
                            user.level.random.nextDouble() * 2,
                            Math.sin(angle) * radius
                    );

                    // 50%概率生成GoldLight或LightBall
                    if (user.level.random.nextBoolean()) {
                        user.level.addParticle(InitParticle.GOLDLIGHT.get(),
                                pos.x, pos.y, pos.z,
                                Math.cos(angle) * 0.1,
                                0.05,
                                Math.sin(angle) * 0.1);
                    } else {
                        user.level.addParticle(InitParticle.LIGHTWALLS.get(),
                                pos.x, pos.y, pos.z,
                                Math.cos(angle) * 0.15,
                                0.1,
                                Math.sin(angle) * 0.15);
                    }
                }
            }

        }
    }

    @Override
    public void standPerform(World world, StandEntity standEntity, IStandPower userPower, StandEntityTask task) {
        if (world.isClientSide()) return;

        LivingEntity user = userPower.getUser();
        user.addEffect(new EffectInstance(
                InitStatusEffect.LOVE_TRAIN.get(),
                10 * 60 * 20,
                0,
                false,
                false,
                true
        ));

        world.playSound(null, user.getX(), user.getY(), user.getZ(),
                SoundEvents.BEACON_ACTIVATE, SoundCategory.PLAYERS,
                1.0F, 0.9F + world.random.nextFloat() * 0.2F);

        if (world instanceof ServerWorld) {
            spawnFullActivationEffect((ServerWorld) world, user.position());
        }
    }

    private void spawnFullActivationEffect(ServerWorld world, Vector3d center) {
        // 1. 中心光壁柱子展开
        spawnExpandingLightPillars(world, center);

        // 2. 底部扩散的LightBall
        spawnGroundSpread(world, center);

        // 3. 外层LightWall边界
        spawnOuterLightWall(world, center);
    }

    private void spawnExpandingLightPillars(ServerWorld world, Vector3d center) {
        // 主光柱(8个方向)
        for (int i = 0; i < 8; i++) {
            double angle = Math.toRadians(i * 45);
            double radius = 1.0;
            double x = center.x + Math.cos(angle) * radius;
            double z = center.z + Math.sin(angle) * radius;

            // 从地面到3格高的光柱
            for (double y = 0; y <= 3; y += 0.2) {
                world.sendParticles(InitParticle.LIGHTWALLP.get(),
                        x, center.y + y, z,
                        1, 0, 0.05, 0, 0.3f);
            }

            // 随机添加短光柱(LightWallS)
            if (world.random.nextFloat() < 0.3) {
                world.sendParticles(InitParticle.LIGHTWALLS.get(),
                        x + (world.random.nextDouble() - 0.5) * 0.3,
                        center.y + 1.5,
                        z + (world.random.nextDouble() - 0.5) * 0.3,
                        1, 0, 0.03, 0, 0.25f);
            }
        }
    }

    private void spawnGroundSpread(ServerWorld world, Vector3d center) {
        // 地面扩散的LightBall(2层)
        for (int layer = 0; layer < 2; layer++) {
            double radius = 1.0 + layer * 2.0;
            int particles = 30 + layer * 20;

            for (int i = 0; i < particles; i++) {
                double angle = Math.toRadians(world.random.nextDouble() * 360);
                double distance = world.random.nextDouble() * radius;

                world.sendParticles(InitParticle.LIGHTWALLP.get(),
                        center.x + Math.cos(angle) * distance,
                        center.y + 0.1,
                        center.z + Math.sin(angle) * distance,
                        1,
                        Math.cos(angle) * 0.05,
                        0.02,
                        Math.sin(angle) * 0.05,
                        0.4f);
            }
        }
    }

    private void spawnOuterLightWall(ServerWorld world, Vector3d center) {
        // 外层LightWall(12个方向)
        for (int i = 0; i < 12; i++) {
            double angle = Math.toRadians(i * 30);
            double radius = 4.0;

            // 从地面到2格高的光墙
            for (double y = 0; y <= 2; y += 0.3) {
                world.sendParticles(InitParticle.LIGHTWALLP.get(),
                        center.x + Math.cos(angle) * radius,
                        center.y + y,
                        center.z + Math.sin(angle) * radius,
                        2, 0, 0.1, 0, 0.35f);
            }

        }
    }

    @Override
    protected boolean standKeepsTarget(ActionTarget target) {
        return false;
    }
}