package com.voidzyy.rotp_ttr.init;

import com.voidzyy.rotp_ttr.AddonMain;
import com.voidzyy.rotp_ttr.effects.LoveTrainEffect;
import com.voidzyy.rotp_ttr.effects.TearsEffects;
import net.minecraft.potion.Effect;
import net.minecraft.potion.EffectType;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;


@Mod.EventBusSubscriber(modid = AddonMain.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class InitStatusEffect {

    public static final DeferredRegister<Effect> EFFECTS = DeferredRegister.create(ForgeRegistries.POTIONS,"rotp_ttr");

    public static final RegistryObject<Effect> TEARS_EFFECT = EFFECTS.register("tears",
            ()-> new TearsEffects(EffectType.NEUTRAL,0x465D58));

    public static final RegistryObject<Effect> LOVE_TRAIN = EFFECTS.register("love_train",
            ()-> new LoveTrainEffect());

    public static RegistryObject<Effect> LF = EFFECTS.register("lovetrainss",()->
            new TearsEffects(EffectType.NEUTRAL,0x465D58));

    //public static void afterEffectsRegister() {
    //    ModStatusEffects.setEffectAsTracked(
    //            TEARS_EFFECT.get());
   // }

}