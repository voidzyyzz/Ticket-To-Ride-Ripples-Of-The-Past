package com.voidzyy.rotp_ttr.init;

import com.github.standobyte.jojo.item.LadybugBroochItem;
import com.voidzyy.rotp_ttr.items.RedStoneSword;
import com.voidzyy.rotp_ttr.items.TearsBlade;
import net.minecraft.item.DyeColor;
import net.minecraft.item.Item;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Map;
import java.util.function.Supplier;

import static com.github.standobyte.jojo.init.ModItems.MAIN_TAB;
import static com.github.standobyte.jojo.init.ModItems.register16colorsItem;


public class InitItems {

    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, "rotp_ttr");
    private static <T extends Item> RegistryObject<T> register(final String name, final Supplier<T> item){
        return ITEMS.register(name, item);
    }
    public static final RegistryObject<Item> RED_STONE_SWORD =
            register("red_stone_sword", () -> new RedStoneSword(new Item.Properties().stacksTo(1).defaultDurability(100)));

    public static final RegistryObject<Item> TEARS =
            register("tearsblade", () -> new TearsBlade(new Item.Properties().stacksTo(1).defaultDurability(0)));

    //public static final RegistryObject<StoneMaskItem> AJA_STONE_MASK = ITEMS.register("aja_stone_mask",
    //        () -> new StoneMaskItem(ModArmorMaterials.STONE_MASK, EquipmentSlotType.HEAD, new Item.Properties().rarity(Rarity.RARE).tab(MAIN_TAB), ModBlocks.AJA_STONE_MASK.get()));

    //public static final RegistryObject<SpawnEggItem> ROCK_PAPER_SCISSORS_KID_SPAWN_EGG = ITEMS.register("rps_kid_spawn_egg",
    //        () -> new ForgeSpawnEggItem(ModEntityTypes.ROCK_PAPER_SCISSORS_KID, 0x563C33, 0xBD8B72, new Item.Properties().tab(MAIN_TAB)));


    public static final Map<DyeColor, RegistryObject<Item>> LADYBUG_BROOCH = register16colorsItem("ladybug_brooch", dye -> {
        Item.Properties builder = new Item.Properties();
        if (dye == DyeColor.LIGHT_BLUE) {
            builder.tab(MAIN_TAB);
        }
        return new LadybugBroochItem(builder, dye);
    });


}

