package com.voidzyy.rotp_ttr.client.render;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.client.renderer.entity.model.EntityModel;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.entity.Entity;

public class DeadphotomemtorModel<T extends Entity> extends EntityModel<T> {
    private final ModelRenderer bone;
    private final ModelRenderer body;
    private final ModelRenderer wing0;
    private final ModelRenderer wingtip0;
    private final ModelRenderer wing1;
    private final ModelRenderer wingtip1;
    private final ModelRenderer head;
    private final ModelRenderer tail;
    private final ModelRenderer tailtip;

    public DeadphotomemtorModel() {
        texWidth = 64;
        texHeight = 64;

        bone = new ModelRenderer(this);
        bone.setPos(-11.0F, 22.0F, 0.0F);

        ModelRenderer cube_r1 = new ModelRenderer(this);
        cube_r1.setPos(2.0F, -9.0F, 5.0F);
        bone.addChild(cube_r1);
        setRotationAngle(cube_r1, -0.4363F, 0.8727F, 1.5708F);
        cube_r1.texOffs(0, 32).addBox(-1.0F, -16.0F, -15.0F, 16.0F, 16.0F, 16.0F, -4.0F, false);

        ModelRenderer cube_r2 = new ModelRenderer(this);
        cube_r2.setPos(5.0F, 0.0F, 0.0F);
        bone.addChild(cube_r2);
        setRotationAngle(cube_r2, 0.9554F, -0.8261F, -1.1877F);
        cube_r2.texOffs(0, 0).addBox(-1.0F, -16.0F, -15.0F, 16.0F, 16.0F, 16.0F, -4.0F, false);

        ModelRenderer cube_r3 = new ModelRenderer(this);
        cube_r3.setPos(2.0F, -7.0F, 5.0F);
        bone.addChild(cube_r3);
        setRotationAngle(cube_r3, 0.0F, 0.8727F, 1.5708F);
        cube_r3.texOffs(0, 32).addBox(-1.0F, -16.0F, -15.0F, 16.0F, 16.0F, 16.0F, -4.0F, false);

        ModelRenderer cube_r4 = new ModelRenderer(this);
        cube_r4.setPos(0.0F, 0.0F, 0.0F);
        bone.addChild(cube_r4);
        setRotationAngle(cube_r4, 0.2753F, -0.7926F, -0.3776F);
        cube_r4.texOffs(0, 0).addBox(-1.0F, -16.0F, -15.0F, 16.0F, 16.0F, 16.0F, -5.0F, false);

        body = new ModelRenderer(this);
        body.setPos(0.0F, 0.0F, 0.0F);
        body.xRot = 0.829F;

        ModelRenderer body_r1 = new ModelRenderer(this);
        body_r1.setPos(0.0F, 24.0F, 0.0F);
        body.addChild(body_r1);
        setRotationAngle(body_r1, -0.1745F, 0.0F, 0.0F);
        body_r1.texOffs(15, 7).addBox(-3.0F, -19.5664F, -14.25F, 5.0F, 3.0F, 9.0F, 0.0F, false);

        wing0 = new ModelRenderer(this);
        wing0.setPos(2.0F, -2.0F, -8.0F);
        body.addChild(wing0);
        setRotationAngle(wing0, 0.0F, 0.0F, 0.0873F);

        ModelRenderer wing0_r1 = new ModelRenderer(this);
        wing0_r1.setPos(-2.0F, 26.0F, 8.0F);
        wing0.addChild(wing0_r1);
        setRotationAngle(wing0_r1, -0.1739F, 0.0151F, -0.0013F);
        wing0_r1.texOffs(34, 2).addBox(2.5607F, -19.5909F, -14.25F, 6.0F, 2.0F, 9.0F, 0.0F, false);

        wingtip0 = new ModelRenderer(this);
        wingtip0.setPos(6.0F, 0.0F, 0.0F);
        wing0.addChild(wingtip0);
        setRotationAngle(wingtip0, 0.0F, 0.0F, 0.1745F);

        ModelRenderer wingtip0_r1 = new ModelRenderer(this);
        wingtip0_r1.setPos(-8.0F, 26.0F, 8.0F);
        wingtip0.addChild(wingtip0_r1);
        setRotationAngle(wingtip0_r1, -0.1687F, 0.045F, -0.0038F);
        wingtip0_r1.texOffs(20, 7).addBox(9.6651F, -19.7856F, -14.25F, 13.0F, 1.0F, 9.0F, 0.0F, false);

        wing1 = new ModelRenderer(this);
        wing1.setPos(-3.0F, -2.0F, -8.0F);
        body.addChild(wing1);
        setRotationAngle(wing1, 0.0F, 0.0F, -0.0873F);

        ModelRenderer wing1_r1 = new ModelRenderer(this);
        wing1_r1.setPos(3.0F, 26.0F, 8.0F);
        wing1.addChild(wing1_r1);
        setRotationAngle(wing1_r1, -0.1739F, -0.0151F, 0.0013F);
        wing1_r1.texOffs(33, 11).addBox(-9.5607F, -19.5909F, -14.25F, 6.0F, 2.0F, 9.0F, 0.0F, true);

        wingtip1 = new ModelRenderer(this);
        wingtip1.setPos(-6.0F, 0.0F, 0.0F);
        wing1.addChild(wingtip1);
        setRotationAngle(wingtip1, 0.0F, 0.0F, -0.1745F);

        ModelRenderer wingtip1_r1 = new ModelRenderer(this);
        wingtip1_r1.setPos(9.0F, 26.0F, 8.0F);
        wingtip1.addChild(wingtip1_r1);
        setRotationAngle(wingtip1_r1, -0.1687F, -0.045F, 0.0038F);
        wingtip1_r1.texOffs(10, 15).addBox(-23.6651F, -19.7856F, -14.25F, 13.0F, 1.0F, 9.0F, 0.0F, true);

        head = new ModelRenderer(this);
        head.setPos(0.0F, 1.0F, -7.0F);
        body.addChild(head);

        ModelRenderer head_r1 = new ModelRenderer(this);
        head_r1.setPos(0.0F, 23.0F, 7.0F);
        head.addChild(head_r1);
        setRotationAngle(head_r1, -0.1745F, 0.0F, 0.0F);
        head_r1.texOffs(0, 0).addBox(-4.0F, -18.5664F, -18.2499F, 7.0F, 3.0F, 5.0F, 0.0F, false);

        tail = new ModelRenderer(this);
        tail.setPos(0.0F, -2.0F, 1.0F);
        body.addChild(tail);
        setRotationAngle(tail, -0.0873F, 0.0F, 0.0F);

        ModelRenderer tail_r1 = new ModelRenderer(this);
        tail_r1.setPos(0.0F, 26.0F, -1.0F);
        tail.addChild(tail_r1);
        setRotationAngle(tail_r1, -0.1745F, 0.0F, 0.0F);
        tail_r1.texOffs(3, 20).addBox(-2.0F, -19.0462F, -4.6654F, 3.0F, 2.0F, 6.0F, 0.0F, false);

        tailtip = new ModelRenderer(this);
        tailtip.setPos(0.0F, 0.5F, 6.0F);
        tail.addChild(tailtip);
        setRotationAngle(tailtip, -0.0873F, 0.0F, 0.0F);

        ModelRenderer tailtip_r1 = new ModelRenderer(this);
        tailtip_r1.setPos(0.0F, 25.5F, -7.0F);
        tailtip.addChild(tailtip_r1);
        setRotationAngle(tailtip_r1, -0.1745F, 0.0F, 0.0F);
        tailtip_r1.texOffs(28, 9).addBox(-1.0F, -18.0788F, 1.9622F, 1.0F, 1.0F, 6.0F, 0.0F, false);
    }

    @Override
    public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        // 动画逻辑可以在这里添加
    }

    @Override
    public void renderToBuffer(MatrixStack matrixStack, IVertexBuilder buffer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
        bone.render(matrixStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
        body.render(matrixStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
    }

    public void setRotationAngle(ModelRenderer modelRenderer, float x, float y, float z) {
        modelRenderer.xRot = x;
        modelRenderer.yRot = y;
        modelRenderer.zRot = z;
    }
}