package com.voidzyy.rotp_ttr.util;

import com.github.standobyte.jojo.entity.stand.StandEntity;
import com.github.standobyte.jojo.power.impl.stand.IStandPower;
import com.github.standobyte.jojo.power.impl.stand.type.StandType;
import com.voidzyy.rotp_ttr.AddonMain;
import com.voidzyy.rotp_ttr.init.InitItems;
import com.voidzyy.rotp_ttr.init.InitStands;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.EntityPredicates;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayList;


@Mod.EventBusSubscriber(modid = AddonMain.MOD_ID)
public class GameplayHandler {


    //This code is when only the user is able to have the Stand Item
    @SubscribeEvent(priority = EventPriority.LOW)
    public static void onEntityJoinWorld(EntityJoinWorldEvent event){
        if(!event.getEntity().level.isClientSide){
            Entity entity = event.getEntity();
            if(entity instanceof ItemEntity){
                ItemEntity entItem = (ItemEntity) entity;
                if(entItem.getItem().getItem() == InitItems.TEARS.get()){
                    entity.remove();
                }
            }
        }
    }




    @SubscribeEvent(priority = EventPriority.LOW)
    public static void onPlayerTick(TickEvent.PlayerTickEvent event){
        PlayerEntity player = event.player;
        if(!player.level.isClientSide()){
            IStandPower.getStandPowerOptional(player).ifPresent(standPower -> {
                StandType<?> itemStandType = InitStands.TICKET_TO_RIDE_STAND.getStandType();

                //This code is when only the user is able to have the Stand Item
                /*
                if(standPower.getType() != itemStandType){
                    deleteItemStandItemSingle(player);
                }else if (standPower.getStandManifestation() instanceof StandEntity){
                    if(player.getItemInHand(Hand.MAIN_HAND).getItem() != InitItems.STAND_ITEM.get() && player.getItemInHand(Hand.OFF_HAND).getItem() != InitItems.TEARS.get()){
                        ItemStack hand = player.getItemInHand(Hand.MAIN_HAND);
                        if(!hand.isEmpty()){
                            ItemEntity ent = new ItemEntity(player.level,player.getX(),player.getY(),player.getZ(),hand);
                            player.level.addFreshEntity(ent);
                        }
                        ItemStack itemStack = new ItemStack(InitItems.STAND_ITEM.get(),1);
                        CompoundNBT nbt =itemStack.getOrCreateTag();
                        nbt.putInt("mode",0);
                        player.setItemInHand(Hand.MAIN_HAND,itemStack);
                        player.setItemSlot(EquipmentSlotType.HEAD,itemStack);
                        oneItemStandinInvetory(player);
                    }
                    deleteDuplicatedItemSingle(player);
                }else {
                    deleteItemStandItemSingle(player);
                }
                 */


                if(standPower.getType() == itemStandType){
                    if(standPower.getStandManifestation() instanceof StandEntity) {
                        //This checks if any player has the item stand or if it's in the world as an Item Entity
                        if(!playerHasItem(player) && !checkIfItemEntityIsTheStandObject(player)){
                            ItemStack itemStack = new ItemStack(InitItems.TEARS.get(),1);
                            /*NBTags to regulate item functions, this is from Cream Starter*/
                            CompoundNBT nbt = new CompoundNBT();
                            itemStack.setTag(nbt);
                            nbt.putString("owner",player.getName().getString());
                            nbt.putString("mode","attack");


                            player.addItem(itemStack);

                        }else {
                            if(playerHasItem(player)){
                                deleteItemStandEntityDropped(player);
                            }
                            deleteDuplicatedItem(player);
                        }


                    }else {
                        deleteItemStandItem(player);
                        deleteItemStandEntityDropped(player);
                    }
                }else{
                    deleteItemStandItem(player);
                    deleteItemStandEntityDropped(player);
                }
            });
        }
    }




    private static void deleteItemStandItem(PlayerEntity players){
        if(players instanceof ServerPlayerEntity){
            ServerPlayerEntity servPlater =  (ServerPlayerEntity) players;
            servPlater.getLevel().players().forEach(player -> {
                for (int i=0; i<player.inventory.getContainerSize();++i){
                    if(itemStandOwner(players,player.inventory.getItem(i))){
                        player.inventory.getItem(i).shrink(1);
                    }
                }
            });
        }else {
            for (int i=0; i<players.inventory.getContainerSize();++i){
                if(itemStandOwner(players,players.inventory.getItem(i))){
                    players.inventory.getItem(i).shrink(1);
                }
            }
        }
    }

    //This is a variation of the code above, but this is used when only the Stand User can have the Stand Item
    private static void deleteItemStandItemSingle(PlayerEntity player){
        for (int i = 0; i < player.inventory.getContainerSize(); ++i) {
            ItemStack inventoryStack = player.inventory.getItem(i);
            if (inventoryStack.getItem() == InitItems.TEARS.get()) {
                inventoryStack.shrink(inventoryStack.getCount());
            }
        }
    }

    private static void deleteItemStandEntityDropped(PlayerEntity player){
        if(player instanceof ServerPlayerEntity){
            ServerPlayerEntity serverPlayer = (ServerPlayerEntity) player;
            serverPlayer.getLevel().getEntities().filter(entity -> entity instanceof ItemEntity && ((ItemEntity)entity).getItem().getItem() == InitItems.TEARS.get())
                    .forEach(entity -> {
                        ItemStack itemStack = ((ItemEntity) entity).getItem();
                        if(itemStandOwner(player,itemStack)){
                            entity.remove();
                        }
                    });
        }
    }


    private static void deleteDuplicatedItem(PlayerEntity players){
        ServerPlayerEntity serverPlayer = (ServerPlayerEntity) players;
        int count = 0;
        for (ServerPlayerEntity player : serverPlayer.getLevel().players()) {
            for (int i=0; i<player.inventory.getContainerSize();++i){
                ItemStack itemStack = player.inventory.getItem(i);
                if(itemStandOwner(players,itemStack)){
                    count++;
                    if(count>1){
                        itemStack.shrink(1);
                    }
                }
            }
        }
    }


    private static void deleteDuplicatedItemSingle(PlayerEntity player){
        int count = 0;
        ArrayList<Integer> places = new ArrayList<>();

        for (int i = 0; i < player.inventory.getContainerSize(); ++i) {
            ItemStack inventoryStack = player.inventory.getItem(i);
            if (inventoryStack.getItem() == InitItems.TEARS.get()) {
                ++count;
                places.add((Integer) i);
            }
            if(count>1){
                for (Integer in:places) {
                    int pl = (int) in;
                    ItemStack dupliEmp = player.inventory.getItem(pl);
                    dupliEmp.shrink(1);
                }
            }
        }

    }


    //
    private static void oneItemStandinInvetory(PlayerEntity player) {

        int selected = player.inventory.selected;

        for (int i = 9; i < player.inventory.getContainerSize(); ++i){
            ItemStack inventoryStack = player.inventory.getItem(i);
            if (inventoryStack.getItem() == InitItems.TEARS.get()) {
                inventoryStack.shrink(inventoryStack.getCount());
            }
        }
        for (int i = 0; i < player.inventory.getContainerSize(); ++i) {
            ItemStack inventoryStack = player.inventory.getItem(i);
            if (inventoryStack.getItem() == InitItems.TEARS.get()) {

                if (i!=selected){
                    inventoryStack.shrink(inventoryStack.getCount());
                }
            }
        }
    }

    private static boolean playerHasItem(PlayerEntity players){
        if(players instanceof ServerPlayerEntity){
            boolean turn = false;
            ServerPlayerEntity serverPlayer = (ServerPlayerEntity) players;
            for (ServerPlayerEntity player : serverPlayer.getLevel().players()) {
                for(int i=0;i<player.inventory.getContainerSize();++i){
                    if(itemStandOwner(players, player.inventory.getItem(i))){
                        turn = true;
                        i= player.inventory.getContainerSize();
                    }
                }
            }
            return turn;
        }else {
            boolean turn = false;
            for(int i=0;i<players.inventory.getContainerSize();++i){
                turn = itemStandOwner(players, players.inventory.getItem(i));
                i= players.inventory.getContainerSize();
            }
            return turn;
        }
    }

    private static boolean checkIfItemEntityIsTheStandObject(PlayerEntity players){
        if(players instanceof ServerPlayerEntity){
            ServerPlayerEntity player = (ServerPlayerEntity) players;
            boolean turn = player.getLevel().getEntities().filter(entity -> entity instanceof ItemEntity && ((ItemEntity)entity).getItem().getItem() == InitItems.TEARS.get())
                    .anyMatch(entity -> {
                        ItemStack itemStack = ((ItemEntity) entity).getItem();
                        return itemStandOwner(player, itemStack);
                    });

            return turn;
        }else {
            return players.level.getEntitiesOfClass(ItemEntity.class,players.getBoundingBox().inflate(1000), EntityPredicates.ENTITY_STILL_ALIVE).stream()
                    .anyMatch(itemEntity -> itemStandOwner(players,itemEntity.getItem()));
        }
    }

    private static boolean itemStandOwner(PlayerEntity player, ItemStack itemStack){
        if(itemStack.getItem() == InitItems.TEARS.get()){
            CompoundNBT nbt = itemStack.getTag();
            if(nbt != null && nbt.contains("owner")){
                return nbt.getString("owner").equals(player.getName().getString());
            }
            return false;
        }
        return false;
    }
}