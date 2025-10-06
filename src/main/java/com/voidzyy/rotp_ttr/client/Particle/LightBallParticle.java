package com.voidzyy.rotp_ttr.client.Particle;

import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.client.particle.*;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.particles.BasicParticleType;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nullable;
import java.util.Random;

@OnlyIn(Dist.CLIENT)
public class LightBallParticle extends SpriteTexturedParticle {
    private static final Random RANDOM = new Random();
    private final IAnimatedSprite sprites;
    private final float initialScale;
    private final Vector3d targetPosition;
    private final Vector3d startPosition;
    private float progress = 0.0F;

    protected LightBallParticle(ClientWorld world, double x, double y, double z,
                                double xSpeed, double ySpeed, double zSpeed,
                                IAnimatedSprite sprites) {
        super(world, x, y, z, 0.0D, 0.0D, 0.0D);
        this.sprites = sprites;
        this.lifetime = 45;
        this.gravity = 0.0F;
        this.hasPhysics = false;
        this.initialScale = 0.05F + RANDOM.nextFloat() * 0.025F;
        this.quadSize = this.initialScale;
        this.startPosition = new Vector3d(x, y, z);
        this.targetPosition = new Vector3d(xSpeed, ySpeed, zSpeed);

        // 1.16.5颜色设置
        float red = 0.95F + RANDOM.nextFloat() * 0.05F;
        float green = 0.85F + RANDOM.nextFloat() * 0.15F;
        float blue = 0.4F + RANDOM.nextFloat() * 0.5F;
        this.rCol = red;
        this.gCol = green;
        this.bCol = blue;
        this.alpha = 1.0F;

        this.setSpriteFromAge(sprites);
    }

    @Override
    public void tick() {
        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;

        if (this.age++ >= this.lifetime) {
            this.remove();
            return;
        }

        this.progress = (float)this.age / (float)this.lifetime;
        float easedProgress = easeInQuart(this.progress);

        // 1.16.5位置更新
        this.x = MathHelper.lerp(easedProgress, this.startPosition.x, this.targetPosition.x);
        this.y = MathHelper.lerp(easedProgress, this.startPosition.y, this.targetPosition.y);
        this.z = MathHelper.lerp(easedProgress, this.startPosition.z, this.targetPosition.z);

        this.quadSize = this.initialScale * (1.0F - this.progress * 0.3F);
        this.alpha = 1.0F * (1.0F - this.progress * 0.2F);

        this.setSpriteFromAge(this.sprites);
    }

    private float easeInQuart(float t) {
        return t * t * t * t;
    }

    @Override
    public void render(IVertexBuilder buffer, ActiveRenderInfo renderInfo, float partialTicks) {
        Vector3d cameraPos = renderInfo.getPosition();
        float x = (float)(MathHelper.lerp(partialTicks, this.xo, this.x) - cameraPos.x());
        float y = (float)(MathHelper.lerp(partialTicks, this.yo, this.y) - cameraPos.y());
        float z = (float)(MathHelper.lerp(partialTicks, this.zo, this.z) - cameraPos.z());

        // 构建面向摄像机的四边形
        Vector3d pos = new Vector3d(x, y, z);
        Vector3d look = pos.normalize();
        Vector3d up = new Vector3d(0.0D, 1.0D, 0.0D);
        Vector3d right = look.cross(up).normalize();
        Vector3d actualUp = right.cross(look).normalize();

        Vector3d rightOffset = right.scale(this.getQuadSize(partialTicks));
        Vector3d upOffset = actualUp.scale(this.getQuadSize(partialTicks));

        Vector3d bottomLeft = pos.subtract(rightOffset);
        Vector3d bottomRight = pos.add(rightOffset);
        Vector3d topRight = bottomRight.add(upOffset);
        Vector3d topLeft = bottomLeft.add(upOffset);

        Vector3f[] vertices = new Vector3f[4];
        vertices[0] = new Vector3f((float)bottomLeft.x, (float)bottomLeft.y, (float)bottomLeft.z);
        vertices[1] = new Vector3f((float)bottomRight.x, (float)bottomRight.y, (float)bottomRight.z);
        vertices[2] = new Vector3f((float)topRight.x, (float)topRight.y, (float)topRight.z);
        vertices[3] = new Vector3f((float)topLeft.x, (float)topLeft.y, (float)topLeft.z);

        int brightness = this.getLightColor(partialTicks);
        float u0 = this.getU0();
        float u1 = this.getU1();
        float v0 = this.getV0();
        float v1 = this.getV1();

        buffer.vertex(vertices[0].x(), vertices[0].y(), vertices[0].z()).uv(u1, v1)
                .color(this.rCol, this.gCol, this.bCol, this.alpha)
                .uv2(brightness).endVertex();
        buffer.vertex(vertices[1].x(), vertices[1].y(), vertices[1].z()).uv(u0, v1)
                .color(this.rCol, this.gCol, this.bCol, this.alpha)
                .uv2(brightness).endVertex();
        buffer.vertex(vertices[2].x(), vertices[2].y(), vertices[2].z()).uv(u0, v0)
                .color(this.rCol, this.gCol, this.bCol, this.alpha)
                .uv2(brightness).endVertex();
        buffer.vertex(vertices[3].x(), vertices[3].y(), vertices[3].z()).uv(u1, v0)
                .color(this.rCol, this.gCol, this.bCol, this.alpha)
                .uv2(brightness).endVertex();
    }

    @Override
    public int getLightColor(float partialTick) {
        int skyLight = 15;
        int blockLight = 15;
        return skyLight << 20 | blockLight << 4;
    }

    @Override
    public IParticleRenderType getRenderType() {
        return IParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }

    public static class Factory implements IParticleFactory<BasicParticleType> {
        private final IAnimatedSprite sprites;

        public Factory(IAnimatedSprite sprites, float v) {
            this.sprites = sprites;
        }

        @Nullable
        @Override
        public Particle createParticle(BasicParticleType type, ClientWorld world,
                                       double x, double y, double z,
                                       double xSpeed, double ySpeed, double zSpeed) {
            return new LightBallParticle(world, x, y, z, xSpeed, ySpeed, zSpeed, sprites);
        }
    }
}