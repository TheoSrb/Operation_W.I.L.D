package net.tiew.operationWild.screen;

import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.minecraft.core.registries.BuiltInRegistries;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.tiew.operationWild.OperationWild;
import net.tiew.operationWild.screen.blocks.SaddlerMenu;
import net.tiew.operationWild.screen.entity.OWInventoryMenu;
import net.tiew.operationWild.screen.entity.submarine.SeaBugInventoryMenu;

public class OWMenuRegister {

    public static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(BuiltInRegistries.MENU, OperationWild.MOD_ID);

    public static final DeferredHolder<MenuType<?>, MenuType<OWInventoryMenu>> OW_INVENTORY_MENU =
            MENUS.register("ow_inventory_menu", () -> new MenuType<>(
                    (containerId, playerInventory) -> new OWInventoryMenu(containerId, playerInventory, new ItemStackHandler(2)),
                    FeatureFlags.VANILLA_SET
            ));

    public static final DeferredHolder<MenuType<?>, MenuType<SeaBugInventoryMenu>> SEABUG_INVENTORY_MENU =
            MENUS.register("seabug_inventory_menu", () -> new MenuType<>(
                    (containerId, playerInventory) -> new SeaBugInventoryMenu(containerId, playerInventory, new ItemStackHandler(15)),
                    FeatureFlags.VANILLA_SET
            ));

    public static final DeferredHolder<MenuType<?>, MenuType<SaddlerMenu>> SADDLER_MENU =
            MENUS.register("saddler_menu", () -> new MenuType<>(
                    (containerId, playerInventory) -> new SaddlerMenu(containerId, playerInventory, new ItemStackHandler(5)),
                    FeatureFlags.VANILLA_SET
            ));

    public static void register(IEventBus eventBus) {
        MENUS.register(eventBus);
    }
}