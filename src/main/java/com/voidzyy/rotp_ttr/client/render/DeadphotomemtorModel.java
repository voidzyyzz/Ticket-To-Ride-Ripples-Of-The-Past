package com.voidzyy.rotp_ttr.client.render;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.client.renderer.entity.model.EntityModel;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.entity.Entity;

public class DeadphotomemtorModel<T extends Entity> extends EntityModel<T> {
    private final ModelRenderer bone;
    private final ModelRenderer cube_r1;

    public DeadphotomemtorModel() {
        texWidth = 64;
        texHeight = 64;

        bone = new ModelRenderer(this);
        bone.setPos(-11.0F, 22.0F, 2.0F);
        setRotationAngle(bone, 0.7418F, 0.0F, 0.0F);

        bone.texOffs(0, 0).addBox(18.3881F, -36.4963F, 14.9855F, 16.0F, 16.0F, 16.0F, 4.0F, false);
        bone.texOffs(0, 32).addBox(15.8418F, -40.6104F, -9.9758F, 16.0F, 16.0F, 16.0F, 5.0F, false);
        bone.texOffs(0, 32).addBox(39.8418F, -9.6104F, 11.0242F, 16.0F, 16.0F, 16.0F, 3.0F, false);
        bone.texOffs(0, 32).addBox(37.8418F, -40.6104F, 14.0242F, 16.0F, 16.0F, 16.0F, 3.0F, false);
        bone.texOffs(0, 32).addBox(26.8418F, -22.6104F, 0.0242F, 16.0F, 16.0F, 16.0F, 13.0F, false);
        bone.texOffs(0, 0).addBox(13.8418F, -11.6104F, -14.9758F, 16.0F, 16.0F, 16.0F, 5.0F, false);
        bone.texOffs(0, 32).addBox(13.8418F, 5.7793F, 17.0242F, 16.0F, 3.6104F, 16.0F, 4.0F, false);
        bone.texOffs(0, 0).addBox(13.8418F, -6.6104F, 17.0242F, 16.0F, 4.3896F, 16.0F, 4.0F, false);
        bone.texOffs(0, 0).addBox(35.8418F, -6.6104F, -14.9758F, 16.0F, 16.0F, 16.0F, 4.0F, false);
        bone.texOffs(0, 0).addBox(39.8418F, -36.6104F, -14.9758F, 16.0F, 16.0F, 16.0F, 4.0F, false);
        bone.texOffs(0, 0).addBox(11.8418F, -37.6104F, 11.0242F, 16.0F, 16.0F, 16.0F, 4.0F, false);

        cube_r1 = new ModelRenderer(this);
        cube_r1.setPos(21.8418F, -8.6104F, 0.0242F);
        bone.addChild(cube_r1);
        setRotationAngle(cube_r1, 0.2753F, -0.7926F, -0.3776F);
        cube_r1.texOffs(0, 0).addBox(-1.0F, -16.0F, -15.0F, 16.0F, 16.0F, 16.0F, 4.0F, false);
    }

    @Override
    public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        // 动画逻辑可以在这里添加
    }

    @Override
    public void renderToBuffer(MatrixStack matrixStack, IVertexBuilder buffer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
        bone.render(matrixStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
    }

    public void setRotationAngle(ModelRenderer modelRenderer, float x, float y, float z) {
        modelRenderer.xRot = x;
        modelRenderer.yRot = y;
        modelRenderer.zRot = z;
    }
}