package com.voidzyy.rotp_ttr;

import com.voidzyy.rotp_ttr.client.render.DeadphotomemtoRenderer;
import com.voidzyy.rotp_ttr.entity.Deadphotomemtor;
import com.voidzyy.rotp_ttr.init.*;
import net.minecraft.entity.ai.attributes.GlobalEntityTypeAttributes;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DeferredWorkQueue;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

// Your addon's main file

@Mod(AddonMain.MOD_ID)
public class AddonMain {
    // The mod's id. Used quite often, mostly when creating ResourceLocation (objects).
    // Its value should match the "modid" entry in the META-INF/mods.toml file
    public static final String MOD_ID = "rotp_ttr";
    public static final Logger LOGGER = LogManager.getLogger();

    public AddonMain() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        // All DeferredRegister objects are registered here.
        // A DeferredRegister needs to be created for each type of objects that need to be registered in the game
        // (see ForgeRegistries or JojoCustomRegistries)
        InitEntities.ENTITIES.register(modEventBus);
        InitSounds.SOUNDS.register(modEventBus);
        InitStands.ACTIONS.register(modEventBus);
        InitStands.STANDS.register(modEventBus);
        InitStatusEffect.EFFECTS.register(modEventBus);
        InitItems.ITEMS.register(modEventBus);
        InitParticle.PARTICLES.register(modEventBus);
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
        bus.register(this.getClass());
        bus.addListener(this::CommonSetup);
        bus.addListener(this::ClientSetup);

        modEventBus.addListener(this::preInit);
    }
    private void CommonSetup(final FMLCommonSetupEvent event) {
    }
    private void ClientSetup(final FMLClientSetupEvent event) {
    }
    private void Setup(final FMLClientSetupEvent event) {
        DeferredWorkQueue.runLater(() -> {
            GlobalEntityTypeAttributes.put(InitEntities.DPM.get(), Deadphotomemtor.createAttributes().build());
        });
    }
    @Mod.EventBusSubscriber(modid = "rotp_ttr", bus = Mod.EventBusSubscriber.Bus.MOD)
    public static class RegistryEvents {

        @SubscribeEvent
        public static void onEntityAttributeCreation(EntityAttributeCreationEvent event) {
            // 确保这里正确注册了实体属性
            event.put(InitEntities.DPM.get(), Deadphotomemtor.createAttributes().build());
            }


    }


    private void preInit(FMLCommonSetupEvent event){
        //AddonPackets.init();
        //CapabilityHandler.commonSetupRegister();
        //InitTags.iniTags();
        //InitStatusEffect.afterEffectsRegister();
    }
    @Mod.EventBusSubscriber(modid = "rotp_ttr", bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientSetup {

        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {
            RenderingRegistry.registerEntityRenderingHandler(
                    InitEntities.DPM.get(),
                    DeadphotomemtoRenderer::new
            );
        }
    }

    public static Logger getLogger() {
        return LOGGER;
    }
}
