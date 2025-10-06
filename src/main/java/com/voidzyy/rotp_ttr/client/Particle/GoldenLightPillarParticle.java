package com.voidzyy.rotp_ttr.client;

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
public class GoldenLightPillarParticle extends SpriteTexturedParticle {
    private static final Random RANDOM = new Random();
    private final IAnimatedSprite sprites;
    private final float pillarHeight;
    private final float pillarWidth;
    private final double relativeX;
    private final double relativeZ;
    private final double baseYOffset;
    private float upwardSpeed;
    private float currentUpwardOffset;
    private int targetPlayerId;

    protected GoldenLightPillarParticle(ClientWorld world, double x, double y, double z,
                                        double xSpeed, double ySpeed, double zSpeed,
                                        IAnimatedSprite sprites) {
        super(world, x, y, z, 0.0D, 0.0D, 0.0D);
        this.sprites = sprites;
        this.lifetime = 30;
        this.gravity = 0.0F;
        this.hasPhysics = false;
        this.pillarHeight = 8.0F;
        this.pillarWidth = 0.02F;
        this.upwardSpeed = (float)(0.05 + RANDOM.nextDouble() * 0.2);
        this.currentUpwardOffset = 0.0F;

        // 1.16.5 API修正
        PlayerEntity nearestPlayer = findNearestPlayer(world, x, y, z);
        if (nearestPlayer != null) {
            this.targetPlayerId = nearestPlayer.getId(); // getEntityId() -> getId()
            this.relativeX = x - nearestPlayer.getX();   // getPosX() -> getX()
            this.relativeZ = z - nearestPlayer.getZ();   // getPosZ() -> getZ()
            this.baseYOffset = y - nearestPlayer.getY(); // getPosY() -> getY()
        } else {
            this.targetPlayerId = -1;
            this.relativeX = 0.0D;
            this.relativeZ = 0.0D;
            this.baseYOffset = 0.0D;
        }

        // 1.16.5颜色设置
        this.rCol = 1.0F;
        this.gCol = 1.0F;
        this.bCol = 1.0F;
        this.alpha = 0.8F;

        this.setSpriteFromAge(sprites); // selectSpriteWithAge() -> setSpriteFromAge()
    }

    @Override
    public void tick() {
        // 1.16.5字段名修正
        this.xo = this.x; // prevPosX -> xo
        this.yo = this.y; // prevPosY -> yo
        this.zo = this.z; // prevPosZ -> zo

        if (this.age++ >= this.lifetime) {
            this.remove(); // setExpired() -> remove()
            return;
        }

        this.currentUpwardOffset += this.upwardSpeed;

        // 玩家追踪更新 - 1.16.5 API
        if (this.targetPlayerId != -1) {
            Entity targetEntity = this.level.getEntity(this.targetPlayerId); // getEntityByID() -> getEntity()
            if (targetEntity instanceof PlayerEntity) {
                PlayerEntity targetPlayer = (PlayerEntity) targetEntity;
                this.x = targetPlayer.getX() + this.relativeX; // posX -> x
                this.z = targetPlayer.getZ() + this.relativeZ; // posZ -> z
                this.y = targetPlayer.getY() + this.baseYOffset + this.currentUpwardOffset; // posY -> y
            } else {
                this.y += this.upwardSpeed;
            }
        } else {
            this.y += this.upwardSpeed;
        }

        // 渐隐效果 - 1.16.5 API
        float ageRatio = (float)this.age / (float)this.lifetime;
        if (ageRatio < 0.1F) {
            this.alpha = ageRatio / 0.1F * 0.9F; // setAlphaF() -> 直接赋值alpha
        } else if (ageRatio > 0.6F) {
            float fadeOutRatio = (ageRatio - 0.6F) / 0.4F;
            float smoothFade = 1.0F - fadeOutRatio * fadeOutRatio;
            this.alpha = smoothFade * 0.9F;
        } else {
            this.alpha = 0.9F;
        }

        this.setSpriteFromAge(this.sprites);
    }

    @Override
    public void render(IVertexBuilder buffer, ActiveRenderInfo renderInfo, float partialTicks) {
        // 1.16.5 API修正
        Vector3d cameraPos = renderInfo.getPosition(); // getProjectedView() -> getPosition()
        float x = (float)(MathHelper.lerp(partialTicks, this.xo, this.x) - cameraPos.x());
        float y = (float)(MathHelper.lerp(partialTicks, this.yo, this.y) - cameraPos.y());
        float z = (float)(MathHelper.lerp(partialTicks, this.zo, this.z) - cameraPos.z());

        Vector3d particlePos = new Vector3d(x, y, z);
        Vector3d lookDirection = particlePos.normalize();
        Vector3d up = new Vector3d(0.0D, 1.0D, 0.0D);
        Vector3d right = lookDirection.cross(up).normalize(); // crossProduct() -> cross()
        Vector3d actualUp = right.cross(lookDirection).normalize();

        // 1.16.5纹理坐标方法
        float u0 = this.getU0(); // getMinU() -> getU0()
        float u1 = this.getU1(); // getMaxU() -> getU1()
        float v0 = this.getV0(); // getMinV() -> getV0()
        float v1 = this.getV1(); // getMaxV() -> getV1()

        Vector3f[] vertices = new Vector3f[4];
        Vector3d rightOffset = right.scale(this.pillarWidth);
        Vector3d upOffset = actualUp.scale(this.pillarHeight);

        Vector3d bottomLeft = particlePos.subtract(rightOffset);
        Vector3d bottomRight = particlePos.add(rightOffset);
        Vector3d topLeft = bottomLeft.add(upOffset);
        Vector3d topRight = bottomRight.add(upOffset);

        vertices[0] = new Vector3f((float)bottomLeft.x, (float)bottomLeft.y, (float)bottomLeft.z);
        vertices[1] = new Vector3f((float)bottomRight.x, (float)bottomRight.y, (float)bottomRight.z);
        vertices[2] = new Vector3f((float)topRight.x, (float)topRight.y, (float)topRight.z);
        vertices[3] = new Vector3f((float)topLeft.x, (float)topLeft.y, (float)topLeft.z);

        int brightness = this.getLightColor(partialTicks);

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

        // 1.16.5实体获取方法
        for (Entity entity : world.entitiesForRendering()) { // getAllEntities() -> entitiesForRendering()
            if (entity instanceof PlayerEntity) {
                PlayerEntity player = (PlayerEntity) entity;
                double distance = player.distanceToSqr(x, y, z); // getDistanceSq() -> distanceToSqr()
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

        public Factory(IAnimatedSprite sprites) {
            this.sprites = sprites;
        }

        @Nullable
        @Override
        public Particle createParticle(BasicParticleType type, ClientWorld world,
                                       double x, double y, double z,
                                       double xSpeed, double ySpeed, double zSpeed) {
            return new GoldenLightPillarParticle(world, x, y, z, xSpeed, ySpeed, zSpeed, sprites);
        }
    }
}