package com.voidzyy.rotp_ttr.event;

import com.github.standobyte.jojo.entity.stand.StandEntity;
import com.github.standobyte.jojo.init.ModParticles;
import com.voidzyy.rotp_ttr.init.InitParticle;
import com.voidzyy.rotp_ttr.init.InitStatusEffect;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.potion.Effect;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.EntityRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.ProjectileImpactEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.living.LivingKnockBackEvent;
import net.minecraftforge.event.world.ExplosionEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Mod.EventBusSubscriber(modid = "rotp_ttr")
public class LoveTrainEventHandler {
    // ========== 核心参数 ==========
    private static final Random random = new Random();
    private static final Map<LivingEntity, LoveTrainData> ACTIVE_EFFECTS = new ConcurrentHashMap<>();
    private static final double BASE_RADIUS = 0.3;
    private static final double MAX_RADIUS = 27.0;
    private static final double GROWTH_RATE = 0.02;//待会改
    private static final int BASE_COLUMNS = 5;
    private static final int MAX_COLUMNS = 400;
    private static final int WORLD_TOP = 246;
    private static final int WORLD_BOTTOM = 0;
    private static final double MIN_PILLAR_DISTANCE = 0.6;

    // ========== 防御系统参数 ==========
    private static final float DAMAGE_IMMUNITY_CHANCE = 1.0f;
    private static final double LF_PROTECTION_RADIUS_MULTIPLIER = 1.2;
    private static final float LF_REFLECT_PERCENTAGE = 0.9f;
    private static final float RANDOM_REFLECT_CHANCE = 0.75f;
    private static final float RANDOM_REFLECT_MULTIPLIER_MIN = 1.5f;
    private static final float RANDOM_REFLECT_MULTIPLIER_MAX = 3.0f;

    // ========== 摩西开海参数 ==========
    private static final int PARTING_SEA_WIDTH = 5;
    private static final double PARTING_SEA_HEIGHT_OFFSET = 1.5;
    private static final int WATER_CLEAR_INTERVAL = 5;

    // ========== 投射物清除参数 ==========
    private static final double PROJECTILE_CLEAR_RANGE = 15;

    // ========== 排斥系统参数 ==========
    private static final double BASE_REPEL_RADIUS = 3.0;  // 基础排斥半径
    private static final double REPEL_RADIUS_MULTIPLIER = 1.2; // 排斥半径相对于光壁半径的倍数
    // 排斥力度
    private static final double PUSH_DISTANCE_MULTIPLIER = 1.0;
    private static final double VELOCITY_MULTIPLIER = 0.5;
    // ========== 内圈光带系统参数 ==========
    private static final int INNER_RING_COLUMNS = 7;
    private static final double INNER_RING_RADIUS_RATIO = 1.3;
    private static final double INNER_RING_HEIGHT_OFFSET = 1.0;
    private static final double INNER_RING_VERTICAL_SPACING = 3;
    private static final float INNER_RING_PARTICLE_CHANCE = 0.7f;
    private static final float INNER_RING_ENERGY_BALL_CHANCE = 0.01f;

    // ========== 装饰墙参数 ==========
    private static final int DECORATIVE_WALL_COUNT = 6;
    private static final double WALL_RADIUS_RATIO = 0.6;
    private static final double WALL_HEIGHT_OFFSET = 0.0;
    private static final double WALL_VERTICAL_SPACING = 3;
    private static final float WALL_PARTICLE_CHANCE = 0.4f;

    // ========== 粒子系统参数 ==========
    private static final double VERTICAL_SPACING = 2;
    private static final float PARTICLE_LIFETIME = 0.0f;
    private static final int PARTICLE_SPAWN_INTERVAL = 2;
    private static final int PARTICLE_BATCH_SIZE = 1;

    // 光墙参数
    private static final int LIGHTWALL_COLUMNS = 2;
    private static final double LIGHTWALL_RADIUS_RATIO = 0.85;
    private static final double LIGHTWALL_HEIGHT_OFFSET = 0.5;
    private static final double LIGHTWALL_VERTICAL_SPACING = 3.2;
    private static final double LIGHTWALL_DRIFT = 0.15;
    private static final float LIGHTWALL_CHANCE = 0.05f;

    // 装饰环参数
    private static final int DECORATIVE_RING_COUNT = 4;
    private static final double DECORATIVE_RING_RADIUS_RATIO = 1.5;
    private static final double DECORATIVE_RING_HEIGHT_OFFSET = 1.2;
    private static final double DECORATIVE_RING_VERTICAL_SPACING = 3.2;
    private static final float DECORATIVE_PARTICLE_CHANCE = 0.05f;
    private static final float GOLD_BALL_CHANCE = 0.03f;

    // 能量场参数
    private static final int ENERGY_BALL_COUNT = 1;
    private static final double ENERGY_BALL_HEIGHT = 2.5;
    private static final float ENERGY_BALL_CHANCE = 0.01f;

    // ========== 事件处理器 ==========
    @SubscribeEvent
    public static void onWorldTick(TickEvent.WorldTickEvent event) {
        if (event.phase != TickEvent.Phase.END || event.world.isClientSide()) {
            return;
        }

        Iterator<Map.Entry<LivingEntity, LoveTrainData>> iterator = ACTIVE_EFFECTS.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<LivingEntity, LoveTrainData> entry = iterator.next();
            LivingEntity entity = entry.getKey();

            if (!entity.isAlive() || !entity.hasEffect(InitStatusEffect.LOVE_TRAIN.get())) {
                iterator.remove();
                continue;
            }

            LoveTrainData data = entry.getValue();
            data.update();

            if (event.world.getGameTime() % WATER_CLEAR_INTERVAL == 0) {
                clearPartingSea((ServerWorld) event.world, entity, data.getCurrentRadius());
            }

            if (event.world.getGameTime() % PARTICLE_SPAWN_INTERVAL == 0) {
                spawnFullParticleSystem((ServerWorld) event.world, entity, data);
            }

            repelEntities(entity, event.world, data);
        }
    }

    // ========== 摩西开海系统 ==========
    private static void clearPartingSea(ServerWorld world, LivingEntity entity, double currentRadius) {
        Vector3d lookVec = entity.getLookAngle();
        Vector3d center = entity.position().add(0, PARTING_SEA_HEIGHT_OFFSET, 0);
        Vector3d rightVec = new Vector3d(-lookVec.z, 0, lookVec.x).normalize();


        for (double distance = -currentRadius; distance <= currentRadius; distance += 1.0) {
            Vector3d linePos = center.add(lookVec.scale(distance));

            for (double widthOffset = -PARTING_SEA_WIDTH / 2.0; widthOffset <= PARTING_SEA_WIDTH / 2.0; widthOffset += 1.0) {
                Vector3d clearPos = linePos.add(rightVec.scale(widthOffset));

                for (int y = WORLD_BOTTOM; y <= WORLD_TOP; y++) {
                    BlockPos pos = new BlockPos(clearPos.x, y, clearPos.z);
                    clearLiquidAtPos(world, pos);
                }
            }
        }
    }

    private static void clearLiquidAtPos(World world, BlockPos pos) {
        BlockState state = world.getBlockState(pos);
        if (state.getMaterial() == Material.WATER || state.getMaterial() == Material.LAVA) {
            world.setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());
            if (world instanceof ServerWorld) {
                ((ServerWorld) world).getBlockTicks().scheduleTick(pos, state.getBlock(), 100);
            }
        }
    }

    // ========== 内圈光带系统 ==========
    private static void spawnInnerLightRing(ServerWorld world, Vector3d center, double radius) {
        double innerRadius = radius * INNER_RING_RADIUS_RATIO;

        // 计算垂直范围（实体上下10格）
        double minY = center.y - 1;
        double maxY = center.y +1;

        for (int i = 0; i < INNER_RING_COLUMNS; i++) {
            double angle = 2 * Math.PI * i / INNER_RING_COLUMNS;
            double x = center.x + Math.cos(angle) * innerRadius;
            double z = center.z + Math.sin(angle) * innerRadius;

            // 修改为在实体上下10格范围内生成粒子
            for (double y = minY; y <= maxY; y += INNER_RING_VERTICAL_SPACING) {
                world.sendParticles(InitParticle.LIGHTWALLP.get(),
                        x, y + INNER_RING_HEIGHT_OFFSET, z,
                        1, 0, 0, 0, PARTICLE_LIFETIME);
            }

            if (random.nextFloat() < INNER_RING_PARTICLE_CHANCE) {
                world.sendParticles(InitParticle.GOLDLIGHT.get(),
                        x, center.y + INNER_RING_HEIGHT_OFFSET, z,
                        1, 0, 0.05, 0, PARTICLE_LIFETIME);
            }

            if (random.nextFloat() < INNER_RING_ENERGY_BALL_CHANCE) {
                world.sendParticles(InitParticle.LIGHTBALL.get(),
                        x, center.y + INNER_RING_HEIGHT_OFFSET + 0.5, z,
                        1, 0, 0.1, 0, PARTICLE_LIFETIME);
            }
        }
    }

    // ========== 完整粒子系统 ==========
    private static void spawnFullParticleSystem(ServerWorld world, LivingEntity entity, LoveTrainData data) {
        Vector3d center = entity.position();
        double radius = data.getCurrentRadius();
        int columns = data.getCurrentColumns();

        // 计算垂直范围（实体上下10格）
        double minY = center.y - 1;
        double maxY = center.y +1;

        for (int i = 0; i < columns; i += 2) {
            double angle = 2 * Math.PI * i / columns;
            double x = center.x + Math.cos(angle) * radius;
            double z = center.z + Math.sin(angle) * radius;

            // 修改为在实体上下10格范围内生成粒子
            for (double y = minY; y <= maxY; y += VERTICAL_SPACING) {
                world.sendParticles(InitParticle.LIGHTWALLP.get(),
                        x, y, z,
                        PARTICLE_BATCH_SIZE, 0, 0, 0, PARTICLE_LIFETIME);
            }
        }

        spawnDecorativeWalls(world, entity, radius);

        if (world.getGameTime() % 3 == 0) {
            spawnInnerLightRing(world, center, radius);
        }

        if (random.nextFloat() < LIGHTWALL_CHANCE) {
            spawnLightWalls(world, center, radius);
        }

        spawnDecorativeOuterRing(world, center, radius);

        if (world.getGameTime() % 40 == 0) {
            spawnEnergyField(world, center, radius);
        }
    }

    // ========== 装饰墙系统 ==========
    private static void spawnDecorativeWalls(ServerWorld world, LivingEntity entity, double radius) {
        Vector3d center = entity.position();
        double wallRadius = radius * WALL_RADIUS_RATIO;

        // 计算垂直范围（实体上下10格）
        double minY = center.y - 1;
        double maxY = center.y +1;

        for (int i = 0; i < DECORATIVE_WALL_COUNT; i++) {
            double angle = 2 * Math.PI * i / DECORATIVE_WALL_COUNT;
            double x = center.x + Math.cos(angle) * wallRadius;
            double z = center.z + Math.sin(angle) * wallRadius;

            // 修改为在实体上下10格范围内生成粒子
            for (double y = minY; y <= maxY; y += WALL_VERTICAL_SPACING) {
                if (random.nextFloat() < WALL_PARTICLE_CHANCE) {
                    world.sendParticles(InitParticle.LIGHTWALLP.get(),
                            x, y + WALL_HEIGHT_OFFSET, z,
                            PARTICLE_BATCH_SIZE, 0, 0, 0, PARTICLE_LIFETIME);
                }
            }
        }
    }

    // ========== 光墙系统 ==========
    private static void spawnLightWalls(ServerWorld world, Vector3d center, double radius) {
        // 计算垂直范围（实体上下10格）
        double minY = center.y - 1;
        double maxY = center.y +1;

        for (int i = 0; i < LIGHTWALL_COLUMNS; i++) {
            double angle = 2 * Math.PI * i / LIGHTWALL_COLUMNS;
            double x = center.x + Math.cos(angle) * radius * LIGHTWALL_RADIUS_RATIO;
            double z = center.z + Math.sin(angle) * radius * LIGHTWALL_RADIUS_RATIO;

            // 修改为在实体上下10格范围内生成粒子
            for (double y = minY; y <= maxY; y += LIGHTWALL_VERTICAL_SPACING) {
                world.sendParticles(InitParticle.LIGHTWALLS.get(),
                        x + (random.nextDouble() - 0.5) * LIGHTWALL_DRIFT,
                        y + LIGHTWALL_HEIGHT_OFFSET,
                        z + (random.nextDouble() - 0.5) * LIGHTWALL_DRIFT,
                        3, 0.1, 0.1, 0.1, PARTICLE_LIFETIME);
            }
        }
    }

    // ========== 装饰外环系统 ==========
    private static void spawnDecorativeOuterRing(ServerWorld world, Vector3d center, double radius) {
        double outerRadius = radius * DECORATIVE_RING_RADIUS_RATIO;

        // 计算垂直范围（实体上下10格）
        double minY = center.y - 1;
        double maxY = center.y +1;

        for (int i = 0; i < DECORATIVE_RING_COUNT; i++) {
            double angle = 2 * Math.PI * i / DECORATIVE_RING_COUNT;
            double randomOffset = 0.2 + random.nextDouble() * 0.3;
            double x = center.x + Math.cos(angle) * (outerRadius + randomOffset);
            double z = center.z + Math.sin(angle) * (outerRadius + randomOffset);

            // 修改为在实体上下10格范围内生成粒子
            for (double y = minY; y <= maxY; y += DECORATIVE_RING_VERTICAL_SPACING) {
                if (random.nextFloat() < DECORATIVE_PARTICLE_CHANCE) {
                    world.sendParticles(InitParticle.GOLDLIGHT.get(),
                            x + (random.nextDouble() - 0.5) * 0.5,
                            y + DECORATIVE_RING_HEIGHT_OFFSET,
                            z + (random.nextDouble() - 0.5) * 0.5,
                            2, 0.1, 0.1, 0.1, PARTICLE_LIFETIME);
                }
            }

            if (random.nextFloat() < GOLD_BALL_CHANCE) {
                world.sendParticles(InitParticle.LIGHTBALL.get(),
                        x,
                        center.y + DECORATIVE_RING_HEIGHT_OFFSET + (random.nextDouble() * 3 - 1.5),
                        z,
                        2, 0.15, 0.15, 0.15, PARTICLE_LIFETIME);
            }
        }
    }

    private static void spawnEnergyField(ServerWorld world, Vector3d center, double radius) {
        if (random.nextFloat() < ENERGY_BALL_CHANCE) {
            for (int i = 0; i < ENERGY_BALL_COUNT; i++) {
                double offsetX = (random.nextDouble() - 0.5) * radius * 0.6;
                double offsetZ = (random.nextDouble() - 0.5) * radius * 0.6;
                world.sendParticles(InitParticle.LIGHTBALL.get(),
                        center.x + offsetX,
                        center.y + ENERGY_BALL_HEIGHT,
                        center.z + offsetZ,
                        1, 0, 0.1, 0, PARTICLE_LIFETIME);
            }
        }
    }



    private static void spawnReflectParticles(ServerWorld world, Vector3d source, Vector3d target) {
        Vector3d direction = target.subtract(source).normalize();
        double distance = source.distanceTo(target);
        int particleCount = Math.min(8, (int) (distance / 2));

        for (int i = 0; i < particleCount; i++) {
            double progress = i / (double) particleCount;
            Vector3d pos = source.add(direction.scale(progress * distance));

            world.sendParticles(InitParticle.LIGHTBALL.get(),
                    pos.x, pos.y + 0.5, pos.z,
                    1, 0, 0.05, 0, PARTICLE_LIFETIME);
        }
    }

    private static void repelEntities(LivingEntity source, World world, LoveTrainData data) {
        // 动态计算排斥半径
        double currentRepelRadius = Math.min(
                BASE_REPEL_RADIUS + (data.getCurrentRadius() * REPEL_RADIUS_MULTIPLIER),
                MAX_RADIUS * 1.5
        );

        // 获取检测区域
        AxisAlignedBB area = source.getBoundingBox().inflate(currentRepelRadius);

        // 先过滤掉绝对不处理的实体类型
        List<Entity> entities = world.getEntitiesOfClass(Entity.class, area, e ->
                e != source &&  // 不排斥自己
                        !(e instanceof StandEntity) &&  // 不排斥替身
                        !(e instanceof ItemEntity) &&  // 不排斥掉落物
                        !(e instanceof ProjectileEntity) &&  // 不排斥投射物
                        !(e instanceof net.minecraft.entity.item.TNTEntity)  // 不排斥TNT
        );

        for (Entity entity : entities) {
            // 只处理生物实体
            if (entity instanceof LivingEntity) {
                LivingEntity livingEntity = (LivingEntity) entity;

                // 额外检查生物是否受保护效果
                if (livingEntity.hasEffect(InitStatusEffect.LOVE_TRAIN.get()) ||
                        livingEntity.hasEffect(InitStatusEffect.LF.get())) {
                    continue;  // 跳过受保护的生物
                }

                // 计算排斥方向
                double dx = livingEntity.getX() - source.getX();
                double dz = livingEntity.getZ() - source.getZ();
                double distance = Math.sqrt(dx * dx + dz * dz);

                if (distance < currentRepelRadius && distance > 0.0) {
                    dx /= distance;
                    dz /= distance;
                    double pushDistance = (currentRepelRadius + 0.1 - distance) * PUSH_DISTANCE_MULTIPLIER;

                    livingEntity.setPos(
                            livingEntity.getX() + dx * pushDistance,
                            livingEntity.getY(),
                            livingEntity.getZ() + dz * pushDistance
                    );

                    livingEntity.setDeltaMovement(
                            dx * VELOCITY_MULTIPLIER,
                            livingEntity.getDeltaMovement().y,
                            dz * VELOCITY_MULTIPLIER
                    );
                    livingEntity.hurtMarked = true;
                }
            }
        }
    }

    // ========== 攻击检测与反伤系统 ==========
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onLivingAttack(LivingAttackEvent event) {
        LivingEntity target = event.getEntityLiving();
        DamageSource source = event.getSource();
        Entity attacker = source.getEntity();

        // 检查是否是有效攻击目标
        if (!(target.hasEffect(InitStatusEffect.LOVE_TRAIN.get()) ||
                target.hasEffect(InitStatusEffect.LF.get()))) {
            return;
        }

        // 检查是否在保护圈内（LF需要额外检查）
        boolean isProtected = target.hasEffect(InitStatusEffect.LOVE_TRAIN.get()) ||
                (target.hasEffect(InitStatusEffect.LF.get()) &&
                        ACTIVE_EFFECTS.keySet().stream()
                                .anyMatch(e -> e.distanceToSqr(target) < Math.pow(getCurrentRadius(e) * LF_PROTECTION_RADIUS_MULTIPLIER, 2)));

        if (isProtected) {
            event.setCanceled(true);

            // 只有实际攻击者触发反伤
            if (attacker != null && !target.level.isClientSide()) {
                performRandomCounterAttack(target.level, target, attacker);
            }
        }
    }

    private static void performRandomCounterAttack(World world, LivingEntity protectedEntity, Entity attacker) {
        // 获取周围生物（排除自己、攻击者、其他受保护实体）
        AxisAlignedBB area = protectedEntity.getBoundingBox().inflate(20.0);
        List<LivingEntity> potentialTargets = world.getEntitiesOfClass(LivingEntity.class, area,
                e -> e.isAlive() &&
                        e != protectedEntity &&
                    //  e != attacker &&
                        !e.hasEffect(InitStatusEffect.LOVE_TRAIN.get()) &&
                        !e.hasEffect(InitStatusEffect.LF.get()));

        if (!potentialTargets.isEmpty()) {
            LivingEntity randomTarget = potentialTargets.get(world.random.nextInt(potentialTargets.size()));

            // 计算反伤伤害（基础值+随机倍率）
            float baseDamage = protectedEntity.getMaxHealth() * 0.15f;
            float randomMultiplier = 0.8f + world.random.nextFloat() * 1.2f;
            float totalDamage = baseDamage * randomMultiplier;

            // 应用伤害
            if (randomTarget.hurt(LoveTrainDamageSource.INSTANCE, totalDamage)) {
                // 伤害成功应用时显示效果
                if (world instanceof ServerWorld) {
                    // 粒子效果从攻击者指向被反伤目标
                    Vector3d attackerPos = attacker.position();
                    Vector3d targetPos = randomTarget.position();
                    spawnReflectParticle((ServerWorld) world, attackerPos, targetPos);

                    // 给被反伤玩家发送消息
                    if (randomTarget instanceof PlayerEntity) {
                        ((PlayerEntity)randomTarget).displayClientMessage(
                                new TranslationTextComponent("message.rotp_ttr.love_train_reflect",
                                        String.format("%.1f", totalDamage)),
                                true);
                    }
                }
            }
        }
    }

    // ========== 反伤粒子效果生成 ==========
    private static void spawnReflectParticle(ServerWorld world, Vector3d source, Vector3d target) {
        Vector3d direction = target.subtract(source).normalize();
        double distance = source.distanceTo(target);
        int particles = Math.min(15, (int)(distance * 2));

        for (int i = 0; i < particles; i++) {
            float progress = i / (float)particles;
            Vector3d pos = source.add(direction.scale(progress * distance));

            world.sendParticles(ModParticles.HAMON_SPARK.get(),
                    pos.x, pos.y + 0.5, pos.z,
                    1, 0, 0.05, 0, 0.15f);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onExplosion(ExplosionEvent.Detonate event) {
        event.getAffectedEntities().removeIf(e -> {
            if (e instanceof LivingEntity) {
                LivingEntity living = (LivingEntity) e;

                // LOVE_TRAIN实体直接免疫爆炸
                if (living.hasEffect(InitStatusEffect.LOVE_TRAIN.get())) {
                    return true;
                }

                // LF实体需要在扩张圈内才免疫爆炸
                if (living.hasEffect(InitStatusEffect.LF.get())) {
                    return ACTIVE_EFFECTS.keySet().stream()
                            .anyMatch(lt -> lt.distanceToSqr(living) < Math.pow(getCurrentRadius(lt) * LF_PROTECTION_RADIUS_MULTIPLIER, 2));
                }
            }
            return false;
        });
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onKnockback(LivingKnockBackEvent event) {
        LivingEntity entity = event.getEntityLiving();

        // LOVE_TRAIN实体直接抵抗击退
        if (entity.hasEffect(InitStatusEffect.LOVE_TRAIN.get())) {
            event.setCanceled(true);
            return;
        }

        // LF实体需要在扩张圈内才抵抗击退
        if (entity.hasEffect(InitStatusEffect.LF.get())) {
            boolean inRange = ACTIVE_EFFECTS.keySet().stream()
                    .anyMatch(e -> e.distanceToSqr(entity) < Math.pow(getCurrentRadius(e) * LF_PROTECTION_RADIUS_MULTIPLIER, 2));

            if (inRange) {
                event.setCanceled(true);
            }
        }
    }

    // ========== 反伤系统 ==========
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onLivingHurt(LivingHurtEvent event) {
        LivingEntity entity = event.getEntityLiving();
        World world = entity.level;

        // LOVE_TRAIN实体直接反伤
        if (entity.hasEffect(InitStatusEffect.LOVE_TRAIN.get())) {
            if (random.nextFloat() <= DAMAGE_IMMUNITY_CHANCE) {
                event.setCanceled(true);

                float baseDamage = entity.getMaxHealth() * 0.75f;
                float randomMultiplier = RANDOM_REFLECT_MULTIPLIER_MIN +
                        random.nextFloat() * (RANDOM_REFLECT_MULTIPLIER_MAX - RANDOM_REFLECT_MULTIPLIER_MIN);
                float totalDamage = baseDamage + (event.getAmount() * randomMultiplier);

                if (!world.isClientSide()) {
                    List<LivingEntity> targets = findValidTargets(world, entity.position(), 1);

                    if (!targets.isEmpty()) {
                        LivingEntity target = targets.get(0);
                        target.hurt(DamageSource.MAGIC, totalDamage);

                        if (world instanceof ServerWorld) {
                            spawnReflectParticles((ServerWorld) world, entity.position(), target.position());
                        }

                        // 如果目标是玩家，发送反伤消息
                        if (target instanceof PlayerEntity) {
                            ((PlayerEntity) target).displayClientMessage(
                                    new TranslationTextComponent("message.rotp_ttr.love_train_reflect",
                                            String.format("%.1f", totalDamage)),
                                    true);
                        }
                    }

                    if (random.nextFloat() < RANDOM_REFLECT_CHANCE) {
                        performCounterAttack(world, entity, event.getAmount());
                        reflectToRandomTarget(world, entity, event.getAmount());
                    }
                }
            }
        }
        // LF实体在扩张圈内反伤
        else if (entity.hasEffect(InitStatusEffect.LF.get())) {
            boolean inRange = ACTIVE_EFFECTS.keySet().stream()
                    .anyMatch(e -> e.distanceToSqr(entity) < Math.pow(getCurrentRadius(e) * LF_PROTECTION_RADIUS_MULTIPLIER, 2));

            if (inRange) {
                event.setCanceled(true);

                if (!world.isClientSide()) {
                    List<LivingEntity> targets = findValidTargets(world, entity.position(), 1);

                    if (!targets.isEmpty()) {
                        LivingEntity target = targets.get(0);
                        float reflectDamage = target.getMaxHealth() * LF_REFLECT_PERCENTAGE;
                        target.hurt(DamageSource.MAGIC, reflectDamage);

                        if (world instanceof ServerWorld) {
                            spawnReflectParticles((ServerWorld) world, entity.position(), target.position());
                        }

                        // 如果目标是玩家，发送反伤消息
                        if (target instanceof PlayerEntity) {
                            ((PlayerEntity) target).displayClientMessage(
                                    new TranslationTextComponent("message.rotp_ttr.lf_reflect",
                                            String.format("%.1f", reflectDamage)),
                                    true);
                        }
                    }

                    if (random.nextFloat() < RANDOM_REFLECT_CHANCE) {
                        performCounterAttack(world, entity, event.getAmount());
                        reflectToRandomTarget(world, entity, event.getAmount());
                    }
                }
            }
        }
    }

    private static List<LivingEntity> findValidTargets(World world, Vector3d pos, int maxCount) {
        return world.getEntitiesOfClass(LivingEntity.class,
                        new AxisAlignedBB(
                                pos.x - 30, pos.y - 10, pos.z - 30,
                                pos.x + 30, pos.y + 10, pos.z + 30),
                        e -> e.isAlive() &&
                                !e.hasEffect(InitStatusEffect.LOVE_TRAIN.get()) &&
                                !e.hasEffect(InitStatusEffect.LF.get()) &&
                                e.distanceToSqr(pos.x, pos.y, pos.z) > 1.0)
                .stream()
                .sorted(Comparator.comparingDouble(e -> e.distanceToSqr(pos.x, pos.y, pos.z)))
                .limit(maxCount)
                .collect(Collectors.toList());
    }

    private static void reflectToRandomTarget(World world, LivingEntity source, float originalDamage) {
        List<LivingEntity> potentialTargets = world.getEntitiesOfClass(LivingEntity.class,
                new AxisAlignedBB(
                        source.getX() - PROJECTILE_CLEAR_RANGE,
                        source.getY() - PROJECTILE_CLEAR_RANGE / 2,
                        source.getZ() - PROJECTILE_CLEAR_RANGE,
                        source.getX() + PROJECTILE_CLEAR_RANGE,
                        source.getY() + PROJECTILE_CLEAR_RANGE / 2,
                        source.getZ() + PROJECTILE_CLEAR_RANGE
                ),
                e -> e.isAlive() &&
                        !e.hasEffect(InitStatusEffect.LOVE_TRAIN.get()) &&
                        !e.hasEffect(InitStatusEffect.LF.get()) &&
                        e != source
        );

        if (!potentialTargets.isEmpty()) {
            LivingEntity target = potentialTargets.get(random.nextInt(potentialTargets.size()));
            float reflectDamage = originalDamage * (RANDOM_REFLECT_MULTIPLIER_MIN +
                    random.nextFloat() * (RANDOM_REFLECT_MULTIPLIER_MAX - RANDOM_REFLECT_MULTIPLIER_MIN));

            target.hurt(DamageSource.MAGIC, reflectDamage);

            if (world instanceof ServerWorld) {
                spawnReflectParticles((ServerWorld) world, source.position(), target.position());
            }

            if (target instanceof PlayerEntity) {
                ((PlayerEntity) target).displayClientMessage(
                        new TranslationTextComponent("message.rotp_ttr.love_train_reflect",
                                source.getName().getString(),
                                String.format("%.1f", reflectDamage)),
                        true);
            }
        }
    }
    private static void performCounterAttack(World world, LivingEntity protectedEntity, float amount) {
        AxisAlignedBB area = protectedEntity.getBoundingBox().inflate(20.0);
        List<LivingEntity> nearbyEntities = world.getEntitiesOfClass(LivingEntity.class, area);
        List<LivingEntity> validTargets = new ArrayList<>();

        for (LivingEntity entity : nearbyEntities) {
            if (entity != protectedEntity && !entity.hasEffect(InitStatusEffect.LOVE_TRAIN.get()) && !entity.hasEffect(InitStatusEffect.LF.get())) {
                validTargets.add(entity);
            }
        }

        if (!validTargets.isEmpty()) {
            LivingEntity target = validTargets.get(random.nextInt(validTargets.size()));
            float damage = target.getMaxHealth() * 0.2F;
            target.hurt(DamageSource.MAGIC, damage);

            // 粒子效果
            if (world instanceof ServerWorld) {
                spawnReflectParticles((ServerWorld) world, protectedEntity.position(), target.position());
            }
        }
    }

    // ========== 数据结构 ==========
    private static class LoveTrainData {
        private final long startTime = System.currentTimeMillis();
        private double currentRadius = BASE_RADIUS;
        private int currentColumns = BASE_COLUMNS;

        public void update() {
            long elapsedSeconds = (System.currentTimeMillis() - startTime) / 1000;
            currentRadius = Math.min(BASE_RADIUS + elapsedSeconds * GROWTH_RATE, MAX_RADIUS);

            double circumference = 2 * Math.PI * currentRadius;
            double currentDistance = circumference / currentColumns;

            if (currentDistance > MIN_PILLAR_DISTANCE) {
                currentColumns = (int) (circumference / MIN_PILLAR_DISTANCE);
                currentColumns = Math.min(currentColumns, MAX_COLUMNS);
            }
        }

        public double getCurrentRadius() {
            return currentRadius;
        }

        public int getCurrentColumns() {
            return currentColumns;
        }
    }

    // ========== 辅助方法 ==========
    private static boolean hasEffect(Entity entity, Effect effect) {
        return entity instanceof LivingEntity && ((LivingEntity) entity).hasEffect(effect);
    }

    private static double getCurrentRadius(LivingEntity entity) {
        LoveTrainData data = ACTIVE_EFFECTS.get(entity);
        return data != null ? data.getCurrentRadius() : BASE_RADIUS;
    }

    @SubscribeEvent
    public static void onEffectUpdate(LivingEvent.LivingUpdateEvent event) {
        LivingEntity entity = event.getEntityLiving();
        if (entity.hasEffect(InitStatusEffect.LOVE_TRAIN.get())) {
            ACTIVE_EFFECTS.putIfAbsent(entity, new LoveTrainData());
        } else {
            ACTIVE_EFFECTS.remove(entity);
        }
    }

    @SubscribeEvent
    public static void onProjectileImpact(ProjectileImpactEvent event) {
        if (event.getRayTraceResult().getType() == RayTraceResult.Type.ENTITY) {
            Entity target = ((EntityRayTraceResult) event.getRayTraceResult()).getEntity();
            if (target instanceof LivingEntity && ACTIVE_EFFECTS.containsKey(target)) {
                event.setCanceled(true);
                event.getEntity().remove();
            }
        }
    }
    // ========== 自定义伤害源 ==========
    public static class LoveTrainDamageSource extends DamageSource {
        public static final LoveTrainDamageSource INSTANCE = new LoveTrainDamageSource();

        private LoveTrainDamageSource() {
            super("love_train_reflect");
            this.bypassArmor();
            this.bypassMagic();
            this.setMagic();
        }

        @Override
        public ITextComponent getLocalizedDeathMessage(LivingEntity entity) {
            return new TranslationTextComponent("death.attack.love_train_reflect", entity.getDisplayName());
        }
    }

}
