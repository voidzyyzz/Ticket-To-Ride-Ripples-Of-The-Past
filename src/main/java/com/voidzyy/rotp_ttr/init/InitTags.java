package com.voidzyy.rotp_ttr.init;

import net.minecraft.entity.EntityType;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.tags.ITag;
import net.minecraft.util.ResourceLocation;

public class InitTags {
    // 定义可以被TEARS影响的实体标签
    public static final ITag.INamedTag<EntityType<?>> TEARS_AFFECTABLE =
            EntityTypeTags.createOptional(new ResourceLocation("rotp_ttr", "tears_affectable"));

    // 如果需要物品标签，可以这样定义：
    // public static final ITag.INamedTag<Item> TEARS_WEAPONS =
    //     ItemTags.createOptional(new ResourceLocation("rotp_ttr", "tears_weapons"));
}