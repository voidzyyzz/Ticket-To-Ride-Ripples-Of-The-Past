package com.voidzyy.rotp_ttr.event;

import com.voidzyy.rotp_ttr.action.TraceOnAction;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.HashMap;
import java.util.Map;

@Mod.EventBusSubscriber(modid = "rotp_ttr")
public class EnchantedItemMaxUse {

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        PlayerEntity player = event.player;
        World world = player.level;

        if (world.isClientSide()) return;

        // 检查玩家所有手持物品
        for (ItemStack stack : player.getHandSlots()) {
            checkAndRestoreTraceItem(stack);
        }
    }

    private static void checkAndRestoreTraceItem(ItemStack stack) {
        if (stack.isEmpty()) return;

        CompoundNBT tag = stack.getTag();
        if (tag == null || !tag.contains(TraceOnAction.TAG_TRACE_COUNT)) return;

        int count = tag.getInt(TraceOnAction.TAG_TRACE_COUNT);
        if (count >= TraceOnAction.MAX_USES) {
            restoreOriginalEnchants(stack);
        }
    }

    private static void restoreOriginalEnchants(ItemStack stack) {
        CompoundNBT tag = stack.getTag();
        if (tag == null || !tag.contains(TraceOnAction.TAG_ORIGINAL_ENCHANTS)) return;

        // 保存当前非追踪附魔
        Map<Enchantment, Integer> toKeep = new HashMap<>();
        EnchantmentHelper.getEnchantments(stack).forEach((enchant, level) -> {
            if (!TraceOnAction.FIXED_POOL.containsKey(enchant)) {
                toKeep.put(enchant, level);
            }
        });

        // 完全清除附魔
        stack.removeTagKey("Enchantments");
        stack.removeTagKey("StoredEnchantments");

        // 恢复原始附魔
        CompoundNBT original = tag.getCompound(TraceOnAction.TAG_ORIGINAL_ENCHANTS);
        original.getAllKeys().forEach(key -> {
            Enchantment enchant = ForgeRegistries.ENCHANTMENTS.getValue(new ResourceLocation(key));
            if (enchant != null && enchant.canApplyAtEnchantingTable(stack)) {
                stack.enchant(enchant, original.getInt(key));
            }
        });

        // 重新添加需要保留的附魔
        toKeep.forEach((enchant, level) -> {
            stack.enchant(enchant, level);
        });

        // 清除标记
        tag.remove(TraceOnAction.TAG_TRACE_COUNT);
        tag.remove(TraceOnAction.TAG_ORIGINAL_ENCHANTS);
    }
}