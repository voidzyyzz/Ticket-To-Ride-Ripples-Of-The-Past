package com.voidzyy.rotp_ttr.event;

import com.voidzyy.rotp_ttr.AddonMain;
import com.voidzyy.rotp_ttr.event.goal.UnluckyHorseGoal;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.passive.horse.HorseEntity;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = AddonMain.MOD_ID)
public class UnluckyHorseSpawnEvent {

    @SubscribeEvent
    public static void onEntityJoin(EntityJoinWorldEvent event) {
        if (event.getEntity().getType() == EntityType.HORSE && event.getEntity() instanceof HorseEntity) {
            HorseEntity horse = (HorseEntity) event.getEntity();
            horse.goalSelector.addGoal(2, new UnluckyHorseGoal(horse));
        }
    }
}