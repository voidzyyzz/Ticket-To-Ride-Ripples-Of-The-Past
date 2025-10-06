package com.voidzyy.rotp_ttr.client.render;

import com.github.standobyte.jojo.client.render.entity.model.stand.StandEntityModel;
import com.github.standobyte.jojo.client.render.entity.renderer.stand.StandEntityRenderer;
import com.voidzyy.rotp_ttr.AddonMain;
import com.voidzyy.rotp_ttr.entity.TicketToRideStandEntity;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.util.ResourceLocation;

public class TtrStandRenderer extends StandEntityRenderer<TicketToRideStandEntity, StandEntityModel<TicketToRideStandEntity>> {

    public TtrStandRenderer(EntityRendererManager renderManager) {
        super(renderManager, new TtrStandModel(), new ResourceLocation(AddonMain.MOD_ID, "textures/entity/stand/item_stand.png"), 0);
    }

}