package com.voidzyy.rotp_ttr.init;

import com.voidzyy.rotp_ttr.AddonMain;
import com.voidzyy.rotp_ttr.entity.Deadphotomemtor;
import net.minecraft.entity.EntityClassification;
import net.minecraft.entity.EntityType;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class InitEntities {
    public static final DeferredRegister<EntityType<?>> ENTITIES =
            DeferredRegister.create(ForgeRegistries.ENTITIES, AddonMain.MOD_ID);

    public static final RegistryObject<EntityType<Deadphotomemtor>> DPM =ENTITIES.register("dpm",
            () -> EntityType.Builder.<Deadphotomemtor>of(Deadphotomemtor::new, EntityClassification.MISC)
                    .sized(1F, 1F)
                    .setUpdateInterval(2)
                    .build(AddonMain.MOD_ID + ":dpm"));


}