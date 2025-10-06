package com.voidzyy.rotp_ttr.items;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.UseAction;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

public class TearsBlade extends Item {
    public static final String TAG_TEARS_MARKED = "TearsMarked";
    public static final String TAG_TEARS_EXPIRE = "TearsExpire";
    public static final String TAG_AMMO = "Ammo";
    public static final int MAX_AMMO = 30;

    public TearsBlade(Properties properties) {
        super(properties);
    }

    // ========== 弹药系统（保持不变） ==========
    public static int getAmmo(ItemStack stack) {
        return stack.getOrCreateTag().getInt(TAG_AMMO);
    }

    public static void setAmmo(ItemStack stack, int ammo) {
        stack.getOrCreateTag().putInt(TAG_AMMO, Math.min(ammo, MAX_AMMO));
    }

    public static void refillAmmo(ItemStack stack) {
        setAmmo(stack, 10);
    }

    public static boolean consumeAmmo(ItemStack stack) {
        int ammo = getAmmo(stack);
        if (ammo > 0) {
            setAmmo(stack, ammo - 1);
            return true;
        }
        return false;
    }

    public static boolean canConsumeAmmo(ItemStack stack) {
        return getAmmo(stack) > 0;
    }

    @Override
    public void onCraftedBy(@NotNull ItemStack stack, @NotNull World world, @NotNull PlayerEntity player) {
        super.onCraftedBy(stack, world, player);
        if (!stack.hasTag()) {
            refillAmmo(stack);
        }
    }

    // ========== 修复的关键部分：NBT访问方法 ==========
    public static boolean isEntityMarked(LivingEntity entity) {
        if (entity == null) return false;

        // 修复：使用getPersistentData()替代getTag()
        CompoundNBT persistentData = entity.getPersistentData();
        if (!persistentData.contains(TAG_TEARS_MARKED)) {
            return false;
        }

        // 检查是否过期
        long expireTime = persistentData.getLong(TAG_TEARS_EXPIRE);
        if (entity.level.getGameTime() > expireTime) {
            persistentData.remove(TAG_TEARS_MARKED);
            persistentData.remove(TAG_TEARS_EXPIRE);
            return false;
        }

        return persistentData.getBoolean(TAG_TEARS_MARKED);
    }

    public static void markEntity(LivingEntity entity, int durationTicks) {
        if (entity == null) return;

        // 修复：使用getPersistentData()替代getOrCreateTag()
        CompoundNBT persistentData = entity.getPersistentData();
        persistentData.putBoolean(TAG_TEARS_MARKED, true);
        persistentData.putLong(TAG_TEARS_EXPIRE,
                entity.level.getGameTime() + durationTicks);
    }

    public static void unmarkEntity(LivingEntity entity) {
        if (entity == null) return;

        CompoundNBT persistentData = entity.getPersistentData();
        persistentData.remove(TAG_TEARS_MARKED);
        persistentData.remove(TAG_TEARS_EXPIRE);
    }

    // ========== 其他方法保持不变 ==========
    @Override
    public boolean hurtEnemy(@NotNull ItemStack stack, @NotNull LivingEntity target, @NotNull LivingEntity attacker) {
        if (attacker instanceof PlayerEntity) {
            if (!canConsumeAmmo(stack)) {
                return super.hurtEnemy(stack, target, attacker);
            }

            if (consumeAmmo(stack)) {
                markEntity(target, 6000); // 5分钟
                return super.hurtEnemy(stack, target, attacker);
            }
        }
        return false;
    }

    @Override
    public @NotNull ActionResult<ItemStack> use(@NotNull World world, PlayerEntity player, @NotNull Hand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!canConsumeAmmo(stack)) {
            return ActionResult.fail(stack);
        }
        player.startUsingItem(hand);
        return ActionResult.consume(stack);
    }

    @Override
    public void releaseUsing(@NotNull ItemStack stack, @NotNull World world, @NotNull LivingEntity entity, int remainingTicks) {
        if (entity instanceof PlayerEntity) {
            ((PlayerEntity) entity).getCooldowns().addCooldown(this, 0);
        }
    }

    @Override
    public @NotNull UseAction getUseAnimation(@NotNull ItemStack stack) {
        return canConsumeAmmo(stack) ? UseAction.BLOCK : UseAction.NONE;
    }

    @Override
    public int getUseDuration(@NotNull ItemStack stack) {
        return 72000;
    }

    @Override
    public boolean showDurabilityBar(ItemStack stack) {
        return getAmmo(stack) < MAX_AMMO;
    }

    @Override
    public double getDurabilityForDisplay(ItemStack stack) {
        return 1.0 - ((double) getAmmo(stack) / (double) MAX_AMMO);
    }
}