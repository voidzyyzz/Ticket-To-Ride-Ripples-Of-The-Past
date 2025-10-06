package com.voidzyy.rotp_ttr.entity;

import com.voidzyy.rotp_ttr.client.render.Deadphotomemtor;
import com.voidzyy.rotp_ttr.client.render.DeadphotomemtorModel;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.util.ResourceLocation;

public class DeadphotomemtoRenderer extends MobRenderer<Deadphotomemtor, DeadphotomemtorModel<Deadphotomemtor>> {



    protected static final ResourceLocation TEXTURE = new ResourceLocation("rotp_ttr", "textures/entity/deadphotom.png");

    public DeadphotomemtoRenderer(EntityRendererManager entityRendererManager) {
        //super(renderManager, new DeadphotomemtorModel<>(), 0.5F);
        super(entityRendererManager,new DeadphotomemtorModel<>(),0.7F);
    }

    @Override
    public ResourceLocation getTextureLocation(Deadphotomemtor entity) {
       return TEXTURE;

    }
}