package com.voidzyy.rotp_ttr.action;

import com.github.standobyte.jojo.action.ActionConditionResult;
import com.github.standobyte.jojo.action.ActionTarget;
import com.github.standobyte.jojo.action.stand.StandEntityAction;
import com.github.standobyte.jojo.entity.stand.StandEntity;
import com.github.standobyte.jojo.entity.stand.StandEntityTask;
import com.github.standobyte.jojo.power.impl.stand.IStandPower;
import com.voidzyy.rotp_ttr.items.TearsBlade;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;

public class RefillAmmoAction extends StandEntityAction {

    public RefillAmmoAction(Builder builder) {
        super(builder);
    }

    @Override
    protected ActionConditionResult checkSpecificConditions(LivingEntity user, IStandPower power, ActionTarget target) {
        // 检查用户是否是玩家
        if (!(user instanceof PlayerEntity)) {
            return ActionConditionResult.NEGATIVE;
        }

        PlayerEntity player = (PlayerEntity) user;

        // 检查玩家是否持有TearsBlade
        boolean hasTearsBlade = false;
        for (Hand hand : Hand.values()) {
            ItemStack stack = player.getItemInHand(hand);
            if (stack.getItem() instanceof TearsBlade) {
                hasTearsBlade = true;
                break;
            }
        }

        if (!hasTearsBlade) {
            return conditionMessage("no_tears_blade");
        }

        // 检查弹药是否已经满
        boolean needsRefill = false;
        for (Hand hand : Hand.values()) {
            ItemStack stack = player.getItemInHand(hand);
            if (stack.getItem() instanceof TearsBlade) {
                int ammo = TearsBlade.getAmmo(stack);
                if (ammo < TearsBlade.MAX_AMMO) {
                    needsRefill = true;
                    break;
                }
            }
        }

        if (!needsRefill) {
            return conditionMessage("ammo_full");
        }

        return ActionConditionResult.POSITIVE;
    }

    @Override
    public void standPerform(World world, StandEntity standEntity, IStandPower userPower, StandEntityTask task) {
        if (world.isClientSide()) return;

        LivingEntity user = userPower.getUser();
        if (!(user instanceof PlayerEntity)) return;

        PlayerEntity player = (PlayerEntity) user;
        boolean refilled = false;

        // 尝试填充主手和副手的TearsBlade
        for (Hand hand : Hand.values()) {
            ItemStack stack = player.getItemInHand(hand);
            if (stack.getItem() instanceof TearsBlade) {
                int currentAmmo = TearsBlade.getAmmo(stack);
                if (currentAmmo < TearsBlade.MAX_AMMO) {
                    stack.getOrCreateTag().putInt("Ammo", TearsBlade.MAX_AMMO);
                    refilled = true;

                    // 发送消息给玩家
                    player.displayClientMessage(
                            new TranslationTextComponent("message.tears_blade.refilled",
                                    hand == Hand.MAIN_HAND ? "主手" : "副手"),
                            true
                    );
                }
            }
        }

        if (refilled) {
            // 播放填充音效
            world.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.RAID_HORN, player.getSoundSource(), 1.0F, 1.0F);

            // 播放粒子效果（可选）
            if (world instanceof net.minecraft.world.server.ServerWorld) {
                net.minecraft.world.server.ServerWorld serverWorld = (net.minecraft.world.server.ServerWorld) world;
                serverWorld.sendParticles(ParticleTypes.TOTEM_OF_UNDYING,
                        player.getX(), player.getY() + 1.0, player.getZ(),
                        10, 0.5, 0.5, 0.5, 0.1);
            }
        }
        // 5秒冷却（20 ticks = 1秒）
    }

}