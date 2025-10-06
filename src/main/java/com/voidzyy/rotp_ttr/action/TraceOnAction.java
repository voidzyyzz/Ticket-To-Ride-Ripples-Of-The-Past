package com.voidzyy.rotp_ttr.action;

import com.github.standobyte.jojo.action.ActionConditionResult;
import com.github.standobyte.jojo.action.ActionTarget;
import com.github.standobyte.jojo.action.stand.StandEntityAction;
import com.github.standobyte.jojo.entity.stand.StandEntity;
import com.github.standobyte.jojo.entity.stand.StandEntityTask;
import com.github.standobyte.jojo.power.impl.stand.IStandPower;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Hand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.HashMap;
import java.util.Map;

public class TraceOnAction extends StandEntityAction {
    public static final String TAG_TRACE_COUNT = "TraceCount";
    public static final String TAG_ORIGINAL_ENCHANTS = "OriginalEnchants";
    public static final int MAX_USES = 3;

    public static final Map<Enchantment, Integer> FIXED_POOL = new HashMap<>();
    static {

        FIXED_POOL.put(Enchantments.ALL_DAMAGE_PROTECTION, 5);
        FIXED_POOL.put(Enchantments.FIRE_PROTECTION, 5);
        FIXED_POOL.put(Enchantments.FALL_PROTECTION, 5);
        FIXED_POOL.put(Enchantments.BLAST_PROTECTION, 5);
        FIXED_POOL.put(Enchantments.PROJECTILE_PROTECTION, 5);
        FIXED_POOL.put(Enchantments.RESPIRATION, 5);
        FIXED_POOL.put(Enchantments.AQUA_AFFINITY, 5);
        FIXED_POOL.put(Enchantments.THORNS, 5);
        FIXED_POOL.put(Enchantments.DEPTH_STRIDER, 5);
        FIXED_POOL.put(Enchantments.FROST_WALKER, 5);
        FIXED_POOL.put(Enchantments.SOUL_SPEED, 5);
        FIXED_POOL.put(Enchantments.SHARPNESS, 5);
        FIXED_POOL.put(Enchantments.SMITE, 5);
        FIXED_POOL.put(Enchantments.BANE_OF_ARTHROPODS, 5);
        FIXED_POOL.put(Enchantments.KNOCKBACK, 2);
        FIXED_POOL.put(Enchantments.FIRE_ASPECT, 5);
        FIXED_POOL.put(Enchantments.MOB_LOOTING, 5);
        FIXED_POOL.put(Enchantments.SWEEPING_EDGE, 5);
        FIXED_POOL.put(Enchantments.BLOCK_EFFICIENCY, 9);
        FIXED_POOL.put(Enchantments.UNBREAKING, 3);
        FIXED_POOL.put(Enchantments.BLOCK_FORTUNE, 5);
        FIXED_POOL.put(Enchantments.POWER_ARROWS, 5);
        FIXED_POOL.put(Enchantments.PUNCH_ARROWS, 5);
        FIXED_POOL.put(Enchantments.FLAMING_ARROWS, 5);
        FIXED_POOL.put(Enchantments.INFINITY_ARROWS, 5);
        FIXED_POOL.put(Enchantments.FISHING_LUCK, 5);
        FIXED_POOL.put(Enchantments.FISHING_SPEED, 5);
        FIXED_POOL.put(Enchantments.LOYALTY, 3);
        FIXED_POOL.put(Enchantments.IMPALING, 3);
        FIXED_POOL.put(Enchantments.CHANNELING, 1);
        FIXED_POOL.put(Enchantments.MULTISHOT, 1);
        FIXED_POOL.put(Enchantments.QUICK_CHARGE, 10);
        FIXED_POOL.put(Enchantments.PIERCING, 5);
        FIXED_POOL.put(Enchantments.MENDING, 1);
        FIXED_POOL.put(Enchantments.VANISHING_CURSE, 1);
}

    public TraceOnAction(Builder builder) {
        super(builder);
    }

    @Override
    protected ActionConditionResult checkSpecificConditions(LivingEntity user, IStandPower power, ActionTarget target) {
        if (!(user instanceof PlayerEntity)) {
            return ActionConditionResult.NEGATIVE;
        }

        PlayerEntity player = (PlayerEntity) user;
        for (Hand hand : Hand.values()) {
            ItemStack stack = player.getItemInHand(hand);
            if (isValidForTrace(stack)) {
                return ActionConditionResult.POSITIVE;
            }
        }
        return conditionMessage("no_enchantable_item");
    }

    @Override
    public void standPerform(World world, StandEntity standEntity, IStandPower userPower, StandEntityTask task) {
        if (world.isClientSide()) return;

        PlayerEntity player = (PlayerEntity) userPower.getUser();
        Hand targetHand = findTraceableHand(player);

        if (targetHand == null) {
            player.displayClientMessage(new TranslationTextComponent("message.trace_on.none"), true);
            return;
        }

        ItemStack stack = player.getItemInHand(targetHand);
        applyTraceEffect(player, stack, targetHand);
    }

    private Hand findTraceableHand(PlayerEntity player) {
        for (Hand hand : Hand.values()) {
            if (isValidForTrace(player.getItemInHand(hand))) {
                return hand;
            }
        }
        return null;
    }

    private void applyTraceEffect(PlayerEntity player, ItemStack stack, Hand hand) {
        // 1. 首次使用时保存原始附魔
        if (hasTraceTag(stack)) {
            saveOriginalEnchants(stack);
        }

        // 2. 应用新附魔
        applyEnchantments(stack);

        // 3. 更新使用计数
        updateTraceCount(stack);

        // 4. 播放效果
        playEffects(player, hand, stack);
    }

    private void saveOriginalEnchants(ItemStack stack) {
        CompoundNBT tag = stack.getOrCreateTag();
        CompoundNBT enchants = new CompoundNBT();

        EnchantmentHelper.getEnchantments(stack).forEach((enchant, level) -> {
            ResourceLocation id = ForgeRegistries.ENCHANTMENTS.getKey(enchant);
            if (id != null) {
                enchants.putInt(id.toString(), level);
            }
        });

        tag.put(TAG_ORIGINAL_ENCHANTS, enchants);
        tag.putInt(TAG_TRACE_COUNT, 0);
    }

    private void applyEnchantments(ItemStack stack) {
        FIXED_POOL.forEach((enchant, level) -> {
            if (enchant.canApplyAtEnchantingTable(stack)) {
                stack.enchant(enchant, level);
            }
        });
    }

    private void updateTraceCount(ItemStack stack) {
        CompoundNBT tag = stack.getOrCreateTag();
        int newCount = tag.getInt(TAG_TRACE_COUNT) + 1;
        tag.putInt(TAG_TRACE_COUNT, newCount);
    }

    private void playEffects(PlayerEntity player, Hand hand, ItemStack stack) {
        player.playSound(SoundEvents.ENCHANTMENT_TABLE_USE, 1.0F, 1.2F);

        int remaining = MAX_USES - getTraceCount(stack);
        String handName = hand == Hand.MAIN_HAND ? "主手" : "副手";

        player.displayClientMessage(
                new TranslationTextComponent("message.trace_on.success",
                        handName, remaining),
                true);
    }

    private boolean isValidForTrace(ItemStack stack) {
        return !stack.isEmpty() &&
                canApplyAtLeastOne(stack) &&
                hasTraceTag(stack);
    }

    private boolean hasTraceTag(ItemStack stack) {
        CompoundNBT tag = stack.getTag();
        return tag == null || !tag.contains(TAG_TRACE_COUNT);
    }

    private int getTraceCount(ItemStack stack) {
        CompoundNBT tag = stack.getTag();
        return tag != null ? tag.getInt(TAG_TRACE_COUNT) : 0;
    }

    private boolean canApplyAtLeastOne(ItemStack stack) {
        return FIXED_POOL.keySet().stream()
                .anyMatch(enchant -> enchant.canApplyAtEnchantingTable(stack));
    }
}