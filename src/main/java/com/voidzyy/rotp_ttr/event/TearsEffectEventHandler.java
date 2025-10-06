package com.voidzyy.rotp_ttr.event;

import com.github.standobyte.jojo.init.ModStatusEffects;
import com.voidzyy.rotp_ttr.items.TearsBlade;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.DoorBlock;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.item.TNTEntity;
import net.minecraft.entity.passive.BeeEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.DamageSource;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.living.LivingFallEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Comparator;
import java.util.List;
import java.util.Random;

@Mod.EventBusSubscriber(modid = "rotp_ttr")
public class TearsEffectEventHandler {
    private static final Random random = new Random();
    private static final double DISASTER_CHANCE = 0.15; // 基础灾厄触发概率

    // ========== 原有效果 ==========

    // 1. 摔伤骨折
    @SubscribeEvent
    public static void onLivingFall(LivingFallEvent event) {
        LivingEntity entity = event.getEntityLiving();
        if (!TearsBlade.isEntityMarked(entity)) return;

        float fallDistance = event.getDistance();
        float baseChance = 0.3F;
        float heightFactor = Math.min(fallDistance / 4.0F, 2.0F);
        float finalChance = baseChance * heightFactor;

        if (fallDistance >= 3.0F && random.nextFloat() < finalChance) {
            entity.addEffect(new EffectInstance(
                    Effects.MOVEMENT_SLOWDOWN,
                    (int)(200 * heightFactor), 2, false, false, false
            ));
            entity.addEffect(new EffectInstance(
                    ModStatusEffects.MISSHAPEN_LEGS.get(),
                    (int)(300 * heightFactor), 0, false, false, false
            ));

            float damage = 5.0F + (fallDistance - 3.0F) * 0.5F;
            entity.hurt(DamageSource.FALL, damage);

            if (!entity.level.isClientSide()) {
                entity.playSound(SoundEvents.SKELETON_HURT, 1.0F, 0.8F + random.nextFloat() * 0.4F);
            }
        }
    }

    // 2. 雨天滑倒/雪天冻结
    @SubscribeEvent
    public static void onLivingUpdate(LivingEvent.LivingUpdateEvent event) {
        LivingEntity entity = event.getEntityLiving();
        World world = entity.level;

        if (TearsBlade.isEntityMarked(entity) && entity.isOnGround()) {
            // 雨天滑倒
            if (world.isRaining() && random.nextDouble() < DISASTER_CHANCE * 0.7) {
                double speed = entity.getDeltaMovement().length();
                if (speed > 0.15) {
                    entity.addEffect(new EffectInstance(
                            Effects.MOVEMENT_SLOWDOWN, 300, 2, false, true, true
                    ));
                    entity.addEffect(new EffectInstance(
                            ModStatusEffects.MISSHAPEN_LEGS.get(), 400, 0, false, true, true
                    ));
                    entity.hurt(DamageSource.FALL, 8.0F);
                }
            }

            // 雪天冻结
            if (isInSnowBiome(entity) && random.nextDouble() < DISASTER_CHANCE * 0.5) {
                entity.addEffect(new EffectInstance(
                        Effects.MOVEMENT_SLOWDOWN, 100, 0, false, true, true
                ));
                entity.addEffect(new EffectInstance(
                        ModStatusEffects.FREEZE.get(), 200, 2, false, true, true
                ));
            }
        }
    }

    // 3. 怪物仇恨（不限于玩家）
    @SubscribeEvent
    public static void onMobUpdate(LivingEvent.LivingUpdateEvent event) {
        if (event.getEntityLiving() instanceof MobEntity) {
            MobEntity mob = (MobEntity) event.getEntityLiving();
            List<LivingEntity> targets = mob.level.getEntitiesOfClass(
                    LivingEntity.class,
                    mob.getBoundingBox().inflate(20.0),
                    entity -> TearsBlade.isEntityMarked(entity) && entity != mob
            );

            if (!targets.isEmpty()) {
                LivingEntity target = targets.stream()
                        .min(Comparator.comparingDouble(mob::distanceToSqr))
                        .orElse(null);
                if (target != null) {
                    mob.setTarget(target);
                }
            }
        }
    }

    // 4. 雨天虚弱
    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        PlayerEntity player = event.player;
        if (TearsBlade.isEntityMarked(player) &&
                player.level.isRaining() &&
                player.tickCount % 100 == 0) {
            player.addEffect(new EffectInstance(
                    Effects.WEAKNESS, 120, 0, false, true, true
            ));
        }
    }

    // ========== 新增灾厄效果 ==========

    // 5. 门夹伤
    @SubscribeEvent
    public static void onDoorInteract(PlayerInteractEvent.RightClickBlock event) {
        BlockState state = event.getWorld().getBlockState(event.getPos());
        if (state.getBlock() instanceof DoorBlock && TearsBlade.isEntityMarked(event.getPlayer())) {
            if (random.nextDouble() < DISASTER_CHANCE) {
                event.getPlayer().addEffect(new EffectInstance(
                        ModStatusEffects.BLEEDING.get(), 200, 0, false, true, true
                ));
                event.getPlayer().playSound(SoundEvents.IRON_TRAPDOOR_CLOSE, 1.0F, 0.8F + random.nextFloat() * 0.4F);
            }
        }
    }

    // 6. 战斗反伤
    @SubscribeEvent
    public static void onLivingAttack(LivingAttackEvent event) {
        if (event.getSource().getEntity() instanceof LivingEntity) {
            LivingEntity attacker = (LivingEntity) event.getSource().getEntity();
            if (TearsBlade.isEntityMarked(attacker) && random.nextDouble() < DISASTER_CHANCE) {
                attacker.hurt(DamageSource.GENERIC, event.getAmount() * 0.5F);
                attacker.playSound(SoundEvents.PLAYER_HURT, 1.0F, 0.8F + random.nextFloat() * 0.4F);
            }
        }
    }
// 在TearsEffectEventHandler类中添加以下内容

    // 岩浆桶泄漏灾厄
    @SubscribeEvent
    public static void onLavaBucketUse(PlayerInteractEvent.RightClickItem event) {
        if (event.getItemStack().getItem() == Items.LAVA_BUCKET &&
                TearsBlade.isEntityMarked(event.getPlayer())) {

            if (random.nextDouble() < 0.10) { // 10%概率泄漏
                PlayerEntity player = event.getPlayer();
                World world = player.level;

                // 在玩家位置生成泄漏的岩浆
                world.setBlockAndUpdate(player.blockPosition(), Blocks.LAVA.defaultBlockState());

                // 对玩家造成伤害
                player.hurt(DamageSource.LAVA, 2.0F);

                // 播放音效
                player.playSound(SoundEvents.LAVA_POP, 1.0F, 1.0F);

                // 消耗岩浆桶
                event.getItemStack().shrink(1);
                player.addItem(new ItemStack(Items.BUCKET));

                // 取消原事件
                event.setCanceled(true);
            }
        }
    }

    // TNT自动引燃灾厄
    @SubscribeEvent
    public static void onTNTApproach(LivingEvent.LivingUpdateEvent event) {
        if (TearsBlade.isEntityMarked(event.getEntityLiving())) {
            LivingEntity entity = event.getEntityLiving();
            World world = entity.level;

            // 检查周围3格内的TNT
            BlockPos.betweenClosedStream(entity.blockPosition().offset(-3, -3, -3),
                            entity.blockPosition().offset(3, 3, 3))
                    .filter(pos -> world.getBlockState(pos).getBlock() == Blocks.TNT)
                    .forEach(pos -> {
                        // 引燃TNT
                        world.setBlock(pos, Blocks.AIR.defaultBlockState(), 11);
                        world.addFreshEntity(new TNTEntity(world,
                                pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                                entity instanceof LivingEntity ? (LivingEntity)entity : null));

                        // 播放音效
                        world.playSound(null, pos, SoundEvents.TNT_PRIMED, SoundCategory.BLOCKS, 1.0F, 1.0F);
                    });
        }
    }

    // 8. 海草缠绕（水中移动时）
    @SubscribeEvent
    public static void onLivingTick(LivingEvent.LivingUpdateEvent event) {
        LivingEntity entity = event.getEntityLiving();
        if (TearsBlade.isEntityMarked(entity) && entity.isInWater() && random.nextDouble() < DISASTER_CHANCE * 0.5) {
            entity.addEffect(new EffectInstance(
                    Effects.MOVEMENT_SLOWDOWN, 100, 2, false, true, true
            ));
            entity.addEffect(new EffectInstance(
                    ModStatusEffects.SPIRIT_VISION.get(), 200, 0, false, true, true
            ));
        }
    }

    // 9. 蜜蜂攻击（靠近蜂巢时）[修改版]
    @SubscribeEvent
    public static void onEntityTick(LivingEvent.LivingUpdateEvent event) {
        LivingEntity entity = event.getEntityLiving();
        if (TearsBlade.isEntityMarked(entity)) {
            World world = entity.level;
            BlockPos.betweenClosedStream(entity.getBoundingBox().inflate(5.0))
                    .filter(pos -> world.getBlockState(pos).is(BlockTags.BEEHIVES))
                    .findFirst()
                    .ifPresent(pos -> {
                        if (random.nextDouble() < DISASTER_CHANCE * 0.3) {
                            // 先破坏蜂巢再生成蜜蜂
                            world.destroyBlock(pos, false); // false表示不掉落物品
                            spawnAngryBees(world, pos, entity);

                            // 播放破坏音效
                            world.playSound(null, pos,
                                    world.getBlockState(pos).getSoundType().getBreakSound(),
                                    SoundCategory.BLOCKS,
                                    1.0F,
                                    1.0F);
                        }
                    });
        }
    }

    private static void spawnAngryBees(World world, BlockPos hivePos, LivingEntity target) {
        for (int i = 0; i < 3 + random.nextInt(3); i++) {
            BeeEntity bee = EntityType.BEE.create(world);
            if (bee != null) {
                bee.setPos(hivePos.getX() + 0.5, hivePos.getY() + 0.5, hivePos.getZ() + 0.5);
                bee.setTarget(target);
                bee.setRemainingPersistentAngerTime(400);
                bee.setAge(-1); // 小蜜蜂
                bee.setHealth(10.0F); // 设置蜜蜂血量
                world.addFreshEntity(bee);
            }
        }
    }

    // ========== 辅助方法 ==========
    private static boolean isInSnowBiome(LivingEntity entity) {
        Biome biome = entity.level.getBiome(entity.blockPosition());
        return biome.getPrecipitation() == Biome.RainType.SNOW &&
                biome.getTemperature(entity.blockPosition()) < 0.15F;
    }
}