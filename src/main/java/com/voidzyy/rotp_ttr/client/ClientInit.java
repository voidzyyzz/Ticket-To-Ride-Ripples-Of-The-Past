package com.voidzyy.rotp_ttr.client;

import com.github.standobyte.jojo.client.particle.custom.CustomParticlesHelper;
import com.github.standobyte.jojo.client.resources.CustomResources;
import com.voidzyy.rotp_ttr.AddonMain;
import com.voidzyy.rotp_ttr.client.Particle.*;
import com.voidzyy.rotp_ttr.client.render.DeadphotomemtoRenderer;
import com.voidzyy.rotp_ttr.client.render.TtrStandRenderer;
import com.voidzyy.rotp_ttr.init.InitEntities;
import com.voidzyy.rotp_ttr.init.InitParticle;
import com.voidzyy.rotp_ttr.init.InitStands;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ParticleFactoryRegisterEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@EventBusSubscriber(modid = AddonMain.MOD_ID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientInit {

    @SubscribeEvent
    public static void onFMLClientSetup(FMLClientSetupEvent event) {
        RenderingRegistry.registerEntityRenderingHandler(
                InitStands.TICKET_TO_RIDE_STAND.getEntityType(), TtrStandRenderer::new);
        RenderingRegistry.registerEntityRenderingHandler(
                InitEntities.DPM.get(), DeadphotomemtoRenderer::new);
    }

    @SubscribeEvent
    public static void onParticleFactoryRegistration(ParticleFactoryRegisterEvent event) {
        Minecraft mc = Minecraft.getInstance();

        // 正确的粒子工厂注册方式
        mc.particleEngine.register(InitParticle.LIGHTWALL.get(),//来源于LL的wide光粒子
                sprite -> new LightWallParticle.Factory(sprite, 0.3F));
        mc.particleEngine.register(InitParticle.GOLDLIGHT.get(),
                sprite -> new GoldLightParticle.Factory(sprite, 1F));
        mc.particleEngine.register(InitParticle.LIGHTWALLP.get(),
                sprite -> new GoldenLightPillarParticle.Factory(sprite, 1F));

        mc.particleEngine.register(InitParticle.LIGHTWALLS.get(),
                sprite -> new ShortGoldenLightPillarParticle.Factory(sprite, 1F));

        mc.particleEngine.register(InitParticle.LIGHTBALL.get(),
                sprite -> new LightBallParticle.Factory(sprite, 1F));

        CustomParticlesHelper.saveSprites(mc);
        CustomResources.initCustomResourceManagers(mc);
    }
}
/*import net.minecraft.client.Minecraft;
import net.minecraft.item.IItemPropertyGetter;
import net.minecraft.item.ItemModelsProperties;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ParticleFactoryRegisterEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@EventBusSubscriber(modid = AddonMain.MOD_ID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientInit {

    private static final IItemPropertyGetter STAND_ITEM_INVISIBLE = (itemStack, clientWorld, livingEntity) -> {
        return !ClientUtil.canSeeStands() ? 1 : 0;
    };

    @SubscribeEvent
    public static void onFMLClientSetup(FMLClientSetupEvent event) {
        Minecraft mc = event.getMinecraftSupplier().get();;

    //    RenderingRegistry.registerEntityRenderingHandler(AddonStands.ITEM_STAND.getEntityType(), ItemStandRenderer::new);

        /*Delete all the event.enqueueWork stuff if the Stand it's visible to no-stand Users like Cream Starter*/
/*        event.enqueueWork(() -> {
            ItemModelsProperties.register(InitItems.TEARS.get(),
                    new ResourceLocation(AddonMain.MOD_ID, "stand_invisible"),
                    STAND_ITEM_INVISIBLE);
        });

    }


    @SubscribeEvent
    public static void onMcConstructor(ParticleFactoryRegisterEvent event){
        Minecraft mc = Minecraft.getInstance();


    }

}*/
