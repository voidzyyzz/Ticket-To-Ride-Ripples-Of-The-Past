package com.voidzyy.rotp_ttr.items;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.world.World;

public class RedStoneSword extends Item {
    public RedStoneSword(Properties properties) {
        super(properties);
    }

    @Override
    public ActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
        ItemStack stack = player.getItemInHand(hand); // 获取玩家手中的物品
        player.addEffect(new EffectInstance(Effects.HEALTH_BOOST,400)); // 玩家获得一个最大生命值提升的buff,持续400tick,20tick=1秒
        stack.hurtAndBreak(1,player, (p_220040_1_) -> {
            p_220040_1_.broadcastBreakEvent(hand);
        }); // stack(玩家手中的物品)消耗1点耐久度
        return ActionResult.success(stack); // 执行的动作
    }
}

