package com.voidzyy.rotp_ttr.init;

import com.github.standobyte.jojo.action.Action;
import com.github.standobyte.jojo.action.stand.CrazyDiamondRepairItem;
import com.github.standobyte.jojo.action.stand.StandAction;
import com.github.standobyte.jojo.action.stand.StandEntityAction;
import com.github.standobyte.jojo.action.stand.StandEntityBlock;
import com.github.standobyte.jojo.entity.stand.StandEntityType;
import com.github.standobyte.jojo.init.ModSounds;
import com.github.standobyte.jojo.init.power.stand.EntityStandRegistryObject;
import com.github.standobyte.jojo.init.power.stand.ModStandsInit;
import com.github.standobyte.jojo.power.impl.stand.StandInstance.StandPart;
import com.github.standobyte.jojo.power.impl.stand.stats.StandStats;
import com.github.standobyte.jojo.power.impl.stand.type.EntityStandType;
import com.github.standobyte.jojo.power.impl.stand.type.StandType;
import com.voidzyy.rotp_ttr.AddonMain;
import com.voidzyy.rotp_ttr.action.*;
import com.voidzyy.rotp_ttr.action.lovetrainaction.*;
import com.voidzyy.rotp_ttr.entity.TicketToRideStandEntity;
import net.minecraft.client.audio.ISound;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;

public class InitStands {
    @SuppressWarnings("unchecked")
    public static final DeferredRegister<Action<?>> ACTIONS = DeferredRegister.create(
            (Class<Action<?>>) ((Class<?>) Action.class), AddonMain.MOD_ID);
    @SuppressWarnings("unchecked")
    public static final DeferredRegister<StandType<?>> STANDS = DeferredRegister.create(
            (Class<StandType<?>>) ((Class<?>) StandType.class), AddonMain.MOD_ID);
 // ======================================== Stand ========================================
    
    
    // Create all the abilities here...
    public static final RegistryObject<CrazyDiamondRepairItem> REPAIR = ACTIONS.register("repair",
            () -> new CrazyDiamondRepairItem(new StandEntityAction.Builder().holdType().staminaCostTick(0.2F)
                    .resolveLevelToUnlock(0).isTrained()
                    .standOffsetFromUser(0.667, 0.2, 0).standPose(CrazyDiamondRepairItem.ITEM_FIX_POSE)
                    .standSound(StandEntityAction.Phase.PERFORM, ModSounds.CRAZY_DIAMOND_FIX_STARTED)
                    .standAutoSummonMode(StandEntityAction.AutoSummonMode.OFF_ARM)
                    .partsRequired(StandPart.ARMS)));


       public static final RegistryObject<StandEntityAction> BOOM = ACTIONS.register("explod",
            () -> new ExplodAction(new StandEntityAction.Builder()
                    .holdToFire(20, true)
                    .standRecoveryTicks(20)
                    .standSound(InitSounds.STAND_PUNCH_LIGHT)
                    .staminaCost(75)
                    .partsRequired(StandPart.ARMS)));

    public static final RegistryObject<StandEntityAction> ARCHER = ACTIONS.register("emiya",
            () -> new TraceOnAction(new StandEntityAction.Builder()
                    .holdToFire(200, true)
                    .standRecoveryTicks(20)
                    .staminaCost(750)
                    .partsRequired(StandPart.ARMS)));

    public static final RegistryObject<StandEntityAction> GIVE_TEARS = ACTIONS.register("givetears",
            () -> new GiveTears(new StandEntityAction.Builder()
                    .holdToFire(20, true)
                    .standRecoveryTicks(20)
                    .staminaCost(75)
                    .partsRequired(StandPart.ARMS)));

    public static final RegistryObject<StandEntityAction> CRASH = ACTIONS.register("crash",
            () -> new MobCrash(new StandEntityAction.Builder()
                    .holdToFire(20, false)
                    .standRecoveryTicks(20)
                    .standSound(InitSounds.STAND_PUNCH_LIGHT)
                    .staminaCost(150)
                    .partsRequired(StandPart.MAIN_BODY)));

    public static final RegistryObject<StandEntityAction> MOBRANDOM = ACTIONS.register("randommob",
            () -> new SummonTaggedMobsAction(new StandEntityAction.Builder()
                    .holdToFire(20, false)
                    .standRecoveryTicks(20)
                    .standSound(ModSounds.AJA_STONE_BEAM)
                    .shiftVariationOf(InitStands.CRASH)
                    .staminaCost(75)
                    .partsRequired(StandPart.MAIN_BODY)));

    public static final RegistryObject<StandEntityAction> SDPM = ACTIONS.register("meteor",
            () -> new SummonDeadphotomemtorAction(new StandEntityAction.Builder()
                    .holdToFire(20, false)
                    .standRecoveryTicks(20)
                    .staminaCost(7500)
                    .partsRequired(StandPart.MAIN_BODY)));



    public static final RegistryObject<StandEntityAction> SINGLELIGHTING = ACTIONS.register("lighting_single",
            () -> new SingleLightingHit(new StandEntityAction.Builder()
                    .cooldown(120)
                    .staminaCostTick(75F)
                    .holdToFire(12, true)
                    .resolveLevelToUnlock(3)));

    public static final RegistryObject<StandEntityAction> LIGHTING = ACTIONS.register("lighting",
            () -> new lightinghit(new StandEntityAction.Builder()
                    .cooldown(1200)
                    .staminaCostTick(75F)
                    .shiftVariationOf(SINGLELIGHTING)
                    .holdToFire(30, true)
                    .resolveLevelToUnlock(3)));

    public static final RegistryObject<StandEntityAction> ITEM_FILL = ACTIONS.register("item_fill",
            ()->new RefillAmmoAction(new StandEntityAction.Builder().holdType()
                    .holdToFire(50, false)));

    public static final RegistryObject<StandEntityAction> LOVETRAIN = ACTIONS.register("lovetrain",
            ()->new LoveTrainAction(new StandEntityAction.Builder().holdType()
                    .holdToFire(50, false)));

    public static final RegistryObject<StandAction> GIVE = ACTIONS.register("sharelovetrain",
            () -> new GiveLFAction(new StandAction.Builder()
                    .staminaCost(400)));

    public static final RegistryObject<StandAction> REMOVE = ACTIONS.register("remove",
            () -> new RemoveLFAction(new StandAction.Builder()
                    .staminaCost(400)
                    .shiftVariationOf(InitStands.GIVE)));


    public static final RegistryObject<StandEntityAction> DLOVETRAIN = ACTIONS.register("removel",
            ()->new DispelLoveTrainAction(new StandEntityAction.Builder().holdType().shiftVariationOf(LOVETRAIN)));





    // ...then create the Stand type instance. Moves, stats, entity sizes, and a few other things are determined here.
    public static final EntityStandRegistryObject<EntityStandType<StandStats>, StandEntityType<TicketToRideStandEntity>> TICKET_TO_RIDE_STAND =
            new EntityStandRegistryObject<>("ticket_to_ride",
                    STANDS, 
                    () -> new EntityStandType.Builder<StandStats>()
                    .color(0xBEC8D6)
                    .storyPartName(ModStandsInit.PART_7_NAME)
                    .leftClickHotbar(
                            SINGLELIGHTING.get(),
                            BOOM.get(),
                            SDPM.get())
                    .rightClickHotbar(
                            ITEM_FILL.get(),
                            GIVE_TEARS.get(),
                            CRASH.get(),
                            ARCHER.get(),
                            LOVETRAIN.get(),
                            GIVE.get()
                            )
                    .defaultStats(StandStats.class, new StandStats.Builder()
                            .tier(2)
                            .power(1)
                            .speed(1)
                            .range(0, 0)
                            .durability(10)
                            .precision(1)
                            .build())
                    .addSummonShout(InitSounds.EXAMPLE_STAND_SUMMON_VOICELINE)
                    .addOst(InitSounds.STAND_OST)
                    .build(),
                    
                    InitEntities.ENTITIES,
                    () -> new StandEntityType<TicketToRideStandEntity>(TicketToRideStandEntity::new, 0.0F, 0.0F)
                    .summonSound(InitSounds.STAND_SUMMON_SOUND)
                    .unsummonSound(InitSounds.STAND_UNSUMMON_SOUND))
            .withDefaultStandAttributes();
    

    
    // ======================================== ??? ========================================
    
    
    
}
