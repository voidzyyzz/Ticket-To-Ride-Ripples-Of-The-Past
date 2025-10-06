package com.voidzyy.rotp_ttr.client.render;

import com.github.standobyte.jojo.client.render.entity.model.stand.StandEntityModel;
import com.github.standobyte.jojo.client.render.entity.renderer.stand.StandEntityRenderer;
import com.voidzyy.rotp_ttr.AddonMain;
import com.voidzyy.rotp_ttr.entity.TicketToRideStandEntity;

import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.util.ResourceLocation;

public class ExampleStandRenderer extends StandEntityRenderer<TicketToRideStandEntity, StandEntityModel<TicketToRideStandEntity>> {

    public ExampleStandRenderer(EntityRendererManager renderManager) {
        super(renderManager, new ExampleStandModel(), new ResourceLocation(AddonMain.MOD_ID, "textures/entity/stand/item_stand.png"), 0);
    }

}