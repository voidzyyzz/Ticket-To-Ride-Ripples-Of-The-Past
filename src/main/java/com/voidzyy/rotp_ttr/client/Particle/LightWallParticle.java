package com.voidzyy.rotp_ttr.client.Particle;

import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.client.particle.*;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.particles.BasicParticleType;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nullable;
import java.util.Random;

@OnlyIn(Dist.CLIENT)
public class LightWallParticle extends SpriteTexturedParticle {
    private static final Random RANDOM = new Random();
    private final IAnimatedSprite sprites;
    private final float pillarHeight;
    private final float pillarWidth;
    private final double relativeX;
    private final double relativeZ;
    private final double baseYOffset;
    private float downwardSpeed;
    private float currentDownwardOffset;
    private int targetPlayerId;

    protected LightWallParticle(ClientWorld world, double x, double y, double z,
                                double xSpeed, double ySpeed, double zSpeed,
                                IAnimatedSprite sprites, float customWidth) {
        super(world, x, y, z, 0.0D, 0.0D, 0.0D);
        this.sprites = sprites;
        this.lifetime = 30;
        this.gravity = 0.0F;
        this.hasPhysics = false; // 1.16.5使用hasPhysics替代canCollide
        this.pillarHeight = 8.0F;
        this.pillarWidth = customWidth;
        this.downwardSpeed = (float)(0.05 + RANDOM.nextDouble() * 0.2);
        this.currentDownwardOffset = 0.0F;

        // 设置颜色
        this.rCol = 1.0F;
        this.gCol = 1.0F;
        this.bCol = 1.0F;
        this.alpha = 0.8F;

        // 玩家追踪逻辑
        PlayerEntity player = findNearestPlayer(world, x, y, z);
        if (player != null) {
            this.targetPlayerId = player.getId(); // 1.16.5使用getId()
            this.relativeX = x - player.getX();   // 1.16.5使用getX()
            this.relativeZ = z - player.getZ();   // 1.16.5使用getZ()
            this.baseYOffset = y - player.getY();  // 1.16.5使用getY()
        } else {
            this.targetPlayerId = -1;
            this.relativeX = 0.0D;
            this.relativeZ = 0.0D;
            this.baseYOffset = 0.0D;
        }

        this.setSpriteFromAge(sprites); // 1.16.5使用setSpriteFromAge
    }

    @Override
    public void tick() {
        this.xo = this.x; // 1.16.5字段名
        this.yo = this.y;
        this.zo = this.z;

        if (this.age++ >= this.lifetime) {
            this.remove(); // 1.16.5使用remove()
            return;
        }

        this.currentDownwardOffset += this.downwardSpeed;

        // 玩家追踪更新
        if (this.targetPlayerId != -1) {
            PlayerEntity targetPlayer = null;
            for (Entity entity : this.level.entitiesForRendering()) { // 1.16.5使用entitiesForRendering()
                if (entity instanceof PlayerEntity && entity.getId() == this.targetPlayerId) {
                    targetPlayer = (PlayerEntity) entity;
                    break;
                }
            }

            if (targetPlayer != null) {
                this.x = targetPlayer.getX() + this.relativeX;
                this.z = targetPlayer.getZ() + this.relativeZ;
                this.y = targetPlayer.getY() + this.baseYOffset - this.currentDownwardOffset;
            }
        }

        // 渐隐效果
        if ((float)this.age > (float)this.lifetime * 0.8F) {
            float fadeProgress = ((float)this.age - (float)this.lifetime * 0.8F) / ((float)this.lifetime * 0.2F);
            this.alpha = 0.8F * (1.0F - fadeProgress);
        }

        this.setSpriteFromAge(this.sprites);
    }

    @Override
    public void render(IVertexBuilder buffer, ActiveRenderInfo renderInfo, float partialTicks) {
        Vector3d cameraPos = renderInfo.getPosition(); // 1.16.5使用getPosition()
        float x = (float)(MathHelper.lerp(partialTicks, this.xo, this.x) - cameraPos.x());
        float y = (float)(MathHelper.lerp(partialTicks, this.yo, this.y) - cameraPos.y());
        float z = (float)(MathHelper.lerp(partialTicks, this.zo, this.z) - cameraPos.z());

        // 构建面向摄像机的四边形
        Vector3d particlePos = new Vector3d(x, y, z);
        Vector3d lookDirection = particlePos.normalize();
        Vector3d up = new Vector3d(0.0D, 1.0D, 0.0D);
        Vector3d right = lookDirection.cross(up).normalize(); // 1.16.5使用cross()
        Vector3d actualUp = right.cross(lookDirection).normalize();

        Vector3d rightOffset = right.scale(this.pillarWidth);
        Vector3d upOffset = actualUp.scale(this.pillarHeight);

        Vector3d bottomLeft = particlePos.subtract(rightOffset);
        Vector3d bottomRight = particlePos.add(rightOffset);
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

        // 1.16.5渲染方法
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
        return 15728880;
    }

    @Override
    public IParticleRenderType getRenderType() {
        return IParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }

    private PlayerEntity findNearestPlayer(ClientWorld world, double x, double y, double z) {
        PlayerEntity nearestPlayer = null;
        double nearestDistance = Double.MAX_VALUE;

        for (Entity entity : world.entitiesForRendering()) {
            if (entity instanceof PlayerEntity) {
                PlayerEntity player = (PlayerEntity) entity;
                double distance = player.distanceToSqr(x, y, z);
                if (distance < nearestDistance && distance < 25.0D) {
                    nearestDistance = distance;
                    nearestPlayer = player;
                }
            }
        }

        return nearestPlayer;
    }

    public static class Factory implements IParticleFactory<BasicParticleType> {
        private final IAnimatedSprite sprites;
        private final float defaultWidth;

        public Factory(IAnimatedSprite sprites, float defaultWidth) {
            this.sprites = sprites;
            this.defaultWidth = defaultWidth;
        }

        @Nullable
        @Override
        public Particle createParticle(BasicParticleType type, ClientWorld world,
                                       double x, double y, double z,
                                       double xSpeed, double ySpeed, double zSpeed) {
            float customWidth = (float)(xSpeed != 0.0D ? xSpeed : this.defaultWidth);
            return new LightWallParticle(world, x, y, z, xSpeed, ySpeed, zSpeed, sprites, customWidth);
        }
    }
}