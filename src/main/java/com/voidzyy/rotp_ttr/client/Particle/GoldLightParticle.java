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
public class GoldLightParticle extends SpriteTexturedParticle {
    private static final Random RANDOM = new Random();
    private final IAnimatedSprite sprites;
    private final double initialXSpeed;
    private final double initialYSpeed;
    private final double initialZSpeed;

    protected GoldLightParticle(ClientWorld world, double x, double y, double z,
                                double xSpeed, double ySpeed, double zSpeed,
                                IAnimatedSprite sprites) {
        super(world, x, y, z, xSpeed, ySpeed, zSpeed);
        this.sprites = sprites;
        this.initialXSpeed = xSpeed;
        this.initialYSpeed = ySpeed;
        this.initialZSpeed = zSpeed;
        this.lifetime = 20 + RANDOM.nextInt(40);
        this.gravity = 0.0F;
        this.hasPhysics = false;
        this.quadSize = 0.1F + RANDOM.nextFloat() * 0.1F;

        // 设置颜色
        float red = 0.9F + RANDOM.nextFloat() * 0.1F;
        float green = 0.8F + RANDOM.nextFloat() * 0.2F;
        float blue = 0.3F + RANDOM.nextFloat() * 0.4F;
        this.setColor(red, green, blue);
        this.alpha = 0.9F;

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

        this.xd = this.initialXSpeed;
        this.yd = this.initialYSpeed;
        this.zd = this.initialZSpeed;
        this.x += this.xd;
        this.y += this.yd;
        this.z += this.zd;

        float ageRatio = (float)this.age / (float)this.lifetime;
        if (ageRatio < 0.2F) {
            this.alpha = ageRatio / 0.2F * 0.9F;
        } else if (ageRatio > 0.7F) {
            float fadeOutRatio = (ageRatio - 0.7F) / 0.3F;
            this.alpha = (1.0F - fadeOutRatio) * 0.9F;
        } else {
            this.alpha = 0.9F;
        }

        if (RANDOM.nextFloat() < 0.1F) {
            this.alpha *= 0.7F;
        }

        this.setSpriteFromAge(this.sprites);
    }

    @Override
    public void render(IVertexBuilder buffer, ActiveRenderInfo renderInfo, float partialTicks) {
        Vector3d cameraPos = renderInfo.getPosition();
        float x = (float)(MathHelper.lerp(partialTicks, this.xo, this.x) - cameraPos.x());
        float y = (float)(MathHelper.lerp(partialTicks, this.yo, this.y) - cameraPos.y());
        float z = (float)(MathHelper.lerp(partialTicks, this.zo, this.z) - cameraPos.z());

        float rotation = ((float)this.age + partialTicks) * 0.1F;
        float u0 = this.getU0();
        float u1 = this.getU1();
        float v0 = this.getV0();
        float v1 = this.getV1();

        Vector3f[] vertices = new Vector3f[4];
        float size = this.getQuadSize(partialTicks);
        float cos = MathHelper.cos(rotation);
        float sin = MathHelper.sin(rotation);

        vertices[0] = new Vector3f(-size * cos - size * sin, -size * cos + size * sin, 0.0F);
        vertices[1] = new Vector3f(-size * cos + size * sin, -size * cos - size * sin, 0.0F);
        vertices[2] = new Vector3f(size * cos + size * sin, size * cos - size * sin, 0.0F);
        vertices[3] = new Vector3f(size * cos - size * sin, size * cos + size * sin, 0.0F);

        for (Vector3f vertex : vertices) {
            vertex.add(x, y, z);
        }

        int brightness = this.getLightColor(partialTicks);

        // 1.16.5渲染方法
        buffer.vertex(vertices[0].x(), vertices[0].y(), vertices[0].z()).uv(u1, v1)
                .color(rCol, gCol, bCol, alpha).uv2(brightness).endVertex();
        buffer.vertex(vertices[1].x(), vertices[1].y(), vertices[1].z()).uv(u1, v0)
                .color(rCol, gCol, bCol, alpha).uv2(brightness).endVertex();
        buffer.vertex(vertices[2].x(), vertices[2].y(), vertices[2].z()).uv(u0, v0)
                .color(rCol, gCol, bCol, alpha).uv2(brightness).endVertex();
        buffer.vertex(vertices[3].x(), vertices[3].y(), vertices[3].z()).uv(u0, v1)
                .color(rCol, gCol, bCol, alpha).uv2(brightness).endVertex();
    }

    @Override
    public int getLightColor(float partialTick) {
        return 15728880;
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
            return new GoldLightParticle(world, x, y, z, xSpeed, ySpeed, zSpeed, sprites);
        }
    }
}