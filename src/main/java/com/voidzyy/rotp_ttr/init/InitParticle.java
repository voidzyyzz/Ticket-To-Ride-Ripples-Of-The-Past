package com.voidzyy.rotp_ttr.init;

import com.voidzyy.rotp_ttr.AddonMain;
import net.minecraft.particles.BasicParticleType;
import net.minecraft.particles.ParticleType;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class InitParticle {
    public static final DeferredRegister<ParticleType<?>> PARTICLES = DeferredRegister.create(ForgeRegistries.PARTICLE_TYPES, AddonMain.MOD_ID);

    public static final RegistryObject<BasicParticleType> LIGHTWALL = PARTICLES.register("love_train_light_wall", () -> new BasicParticleType(false));

    public static final RegistryObject<BasicParticleType> GOLDLIGHT = PARTICLES.register("love_train_light", () -> new BasicParticleType(false));

    public static final RegistryObject<BasicParticleType> LIGHTWALLP = PARTICLES.register("love_train_light_pillar", () -> new BasicParticleType(false));

    public static final RegistryObject<BasicParticleType> LIGHTWALLS = PARTICLES.register("love_train_light_short", () -> new BasicParticleType(false));
    public static final RegistryObject<BasicParticleType> LIGHTBALL = PARTICLES.register("love_train_light_ball", () -> new BasicParticleType(false));

}
