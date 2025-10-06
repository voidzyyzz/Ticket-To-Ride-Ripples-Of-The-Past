package com.voidzyy.rotp_ttr.entity;

import com.voidzyy.rotp_ttr.init.InitSounds;
import net.minecraft.entity.AgeableEntity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ai.attributes.AttributeModifierMap;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.monster.MonsterEntity;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.DamageSource;
import net.minecraft.util.SoundEvent;
import net.minecraft.world.Explosion;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Mod.EventBusSubscriber(modid = "rotp_ttr")
public class Deadphotomemtor extends TameableEntity {

    private static final DataParameter<Integer> DATA_HITS = EntityDataManager.defineId(Deadphotomemtor.class, DataSerializers.INT);

    public Deadphotomemtor(EntityType<? extends TameableEntity> type, World world) {
        super(type, world);
        this.noCulling = true; // 防止渲染裁剪
    }

    @Nullable
    @Override
    public AgeableEntity getBreedOffspring(@NotNull ServerWorld world, @NotNull AgeableEntity mate) {
        return null;
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_HITS, 0);
    }

    @Override
    public void tick() {
        super.tick();

        // 禁止闪红
        this.hurtTime = 0;
        this.hurtDuration = 0;

        if (!this.level.isClientSide && this.isAlive()) {
            // 检查碰撞
            if (this.onGround || this.horizontalCollision || this.verticalCollision) {
                this.explodeD();
            }

            // 服务器端生成火焰拖尾
            spawnFireTail();
        } else if (this.level.isClientSide) {
            // 客户端生成火焰拖尾粒子效果
            spawnClientFireParticles();
        }
    }

    private void spawnFireTail() {
        if (this.level.isClientSide) return;

        ServerWorld serverWorld = (ServerWorld) this.level;
        double baseX = this.getX();
        double baseY = this.getY() + this.getBbHeight() * 0.5D;
        double baseZ = this.getZ();

        // 2×2 网格火焰拖尾
        for (int dx = 0; dx < 2; dx++) {
            for (int dz = 0; dz < 2; dz++) {
                double px = baseX + (dx * 0.5D) - 0.25D;
                double pz = baseZ + (dz * 0.5D) - 0.25D;

                // 生成火焰粒子
                serverWorld.sendParticles(
                        ParticleTypes.FLAME,
                        px, baseY, pz,
                        2,               // count
                        0.1, 0.1, 0.1,   // delta
                        0.05             // speed
                );

                // 添加烟雾粒子增强效果
                serverWorld.sendParticles(
                        ParticleTypes.SMOKE,
                        px, baseY, pz,
                        1,               // count
                        0.05, 0.05, 0.05, // delta
                        0.02             // speed
                );
            }
        }
    }

    private void spawnClientFireParticles() {
        // 客户端粒子效果
        if (this.random.nextFloat() < 0.3F) {
            for (int i = 0; i < 2; i++) {
                double ox = (this.random.nextDouble() - 0.5) * 0.5;
                double oy = this.random.nextDouble() * this.getBbHeight();
                double oz = (this.random.nextDouble() - 0.5) * 0.5;

                this.level.addParticle(ParticleTypes.FLAME,
                        this.getX() + ox, this.getY() + oy, this.getZ() + oz,
                        0, 0, 0);
            }
        }
    }

    private void explodeD() {
        if (!this.level.isClientSide && this.isAlive()) {
            Explosion.Mode mode = net.minecraftforge.event.ForgeEventFactory.getMobGriefingEvent(this.level, this) ?
                    Explosion.Mode.DESTROY : Explosion.Mode.NONE;

            int explosionRadius = 6;
            this.level.explode(this, this.getX(), this.getY(), this.getZ(), (float) explosionRadius, mode);

            ServerWorld serverWorld = (ServerWorld) this.level;
            serverWorld.sendParticles(ParticleTypes.FLAME, this.getX(), this.getY(), this.getZ(), 200, 3.0, 3.0, 3.0, 0.1);
            serverWorld.sendParticles(ParticleTypes.SMOKE, this.getX(), this.getY(), this.getZ(), 100, 3.0, 3.0, 3.0, 0.1);
            serverWorld.sendParticles(ParticleTypes.LAVA, this.getX(), this.getY(), this.getZ(), 50, 2.0, 2.0, 2.0, 0.05);

            this.remove();
        }
    }

    public static AttributeModifierMap.MutableAttribute createAttributes() {
        return MonsterEntity.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 999.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.25D)
                .add(Attributes.ATTACK_DAMAGE, 5.0D)
                .add(Attributes.FOLLOW_RANGE, 32.0D);
    }

    // 最重要的伤害免疫方法
    @Override
    public boolean hurt(DamageSource source, float amount) {
        // 完全免疫所有伤害
        return false;
    }

    @Override
    public boolean isInvulnerableTo(DamageSource source) {
        // 对所有伤害源无敌
        return true;
    }

    @Override
    public boolean isInvulnerable() {
        // 完全无敌
        return true;
    }

    @Override
    public boolean isNoGravity() {
        return false;
    }

    public int getHits() {
        return this.entityData.get(DATA_HITS);
    }

    @Override
    public void addAdditionalSaveData(@NotNull CompoundNBT nbt) {
        super.addAdditionalSaveData(nbt);
        nbt.putInt("hits", getHits());
    }

    @Override
    public void readAdditionalSaveData(@NotNull CompoundNBT nbt) {
        super.readAdditionalSaveData(nbt);
        if (nbt.contains("hits")) {
            this.entityData.set(DATA_HITS, nbt.getInt("hits"));
        }
    }

    @Nullable
    @Override
    protected SoundEvent getHurtSound(@NotNull DamageSource source) {
        return InitSounds.STAND_SUMMON_SOUND.get();
    }

    @SubscribeEvent
    public static void onAttack(LivingAttackEvent event) {
        if (event.getEntity() instanceof Deadphotomemtor) {
            event.setCanceled(true);   // 拒绝一切伤害
        }
    }

    @Override
    public boolean isFood(@NotNull ItemStack stack) {
        return false;
    }
}