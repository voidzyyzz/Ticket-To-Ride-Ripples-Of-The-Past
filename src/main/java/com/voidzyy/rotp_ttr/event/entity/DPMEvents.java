package com.voidzyy.rotp_ttr.event;


import com.voidzyy.rotp_ttr.AddonMain;
import com.voidzyy.rotp_ttr.client.render.DeadphotomemtoRenderer;
import com.voidzyy.rotp_ttr.init.InitEntities;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@Mod.EventBusSubscriber(modid = AddonMain.MOD_ID,bus = Mod.EventBusSubscriber.Bus.MOD)
public class DPMEvents {
    @SubscribeEvent
    public static void DPMEvents(FMLClientSetupEvent event){
        RenderingRegistry.registerEntityRenderingHandler(InitEntities.DPM.get(), DeadphotomemtoRenderer::new);
    }
}
