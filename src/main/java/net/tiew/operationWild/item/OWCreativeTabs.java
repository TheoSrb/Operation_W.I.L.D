package net.tiew.operationWild.item;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.tiew.operationWild.OperationWild;
import net.tiew.operationWild.block.OWBlocks;

public class OWCreativeTabs {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, OperationWild.MOD_ID);

    public static void register(IEventBus eventBus) {
        CREATIVE_MODE_TABS.register(eventBus);
    }

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> OPERATION_WILD_TAB = CREATIVE_MODE_TABS.register("operation_wild_tab",
            () -> CreativeModeTab.builder().icon(() -> new ItemStack(OWItems.PLANT_FIBER.get())).title(Component.translatable("creativetab.ow.operation_wild"))
                    .displayItems(((itemDisplayParameters, output) -> {

                        output.accept(OWItems.BOA_SPAWN_EGG.get());
                        output.accept(OWItems.CHAMELEON_SPAWN_EGG.get());
                        output.accept(OWItems.ELEPHANT_SPAWN_EGG.get());
                        output.accept(OWItems.HYENA_SPAWN_EGG.get());
                        output.accept(OWItems.JELLYFISH_SPAWN_EGG.get());
                        output.accept(OWItems.KODIAK_SPAWN_EGG.get());
                        output.accept(OWItems.MANDRILL_SPAWN_EGG.get());
                        output.accept(OWItems.MANTA_SPAWN_EGG.get());
                        output.accept(OWItems.PEACOCK_SPAWN_EGG.get());
                        output.accept(OWItems.RED_PANDA_SPAWN_EGG.get());
                        output.accept(OWItems.TIGER_SHARK_SPAWN_EGG.get());
                        output.accept(OWItems.TIGER_SPAWN_EGG.get());
                        output.accept(OWItems.WALRUS_SPAWN_EGG.get());
                        output.accept(OWItems.PLANT_FIBER.get());
                        output.accept(OWItems.LAVENDER_BOUQUET.get());
                        output.accept(OWItems.JADE.get());
                        output.accept(OWBlocks.JADE_ORE.get());
                        output.accept(OWBlocks.DEEPSLATE_JADE_ORE.get());
                        output.accept(OWItems.JADE_SWORD.get());
                        output.accept(OWItems.JADE_SHOVEL.get());
                        output.accept(OWItems.JADE_PICKAXE.get());
                        output.accept(OWItems.JADE_AXE.get());
                        output.accept(OWItems.JADE_HOE.get());
                        output.accept(OWItems.JADE.get());
                        output.accept(OWItems.JADE_HELMET.get());
                        output.accept(OWItems.JADE_CHESTPLATE.get());
                        output.accept(OWItems.JADE_LEGGINGS.get());
                        output.accept(OWItems.JADE_BOOTS.get());
                        output.accept(OWItems.RUBY.get());
                        output.accept(OWBlocks.RUBY_ORE.get());
                        output.accept(OWBlocks.DEEPSLATE_RUBY_ORE.get());
                        output.accept(OWItems.RUBY_SWORD.get());
                        output.accept(OWItems.RUBY_SHOVEL.get());
                        output.accept(OWItems.RUBY_PICKAXE.get());
                        output.accept(OWItems.RUBY_AXE.get());
                        output.accept(OWItems.RUBY_HOE.get());
                        output.accept(OWItems.PLATINUM_INGOT.get());
                        output.accept(OWItems.PLATINUM_PLATED_RUBY_SWORD.get());
                        output.accept(OWItems.MAYA_BLOWPIPE.get());
                        output.accept(OWItems.PRIMITIVE_SICKLE.get());
                        output.accept(OWItems.PRIMITIVE_SLINGSHOT.get());
                        output.accept(OWItems.PRIMITIVE_SPEAR.get());
                        output.accept(OWItems.CAMOUFLAGE_HELMET.get());
                        output.accept(OWItems.CAMOUFLAGE_CHESTPLATE.get());
                        output.accept(OWItems.CAMOUFLAGE_LEGGINGS.get());
                        output.accept(OWItems.CAMOUFLAGE_BOOTS.get());
                        output.accept(OWItems.BOA_SADDLE.get());
                        output.accept(OWItems.ELEPHANT_SADDLE.get());
                        output.accept(OWItems.PEACOCK_SADDLE.get());
                        output.accept(OWItems.TIGER_SADDLE.get());
                        output.accept(OWItems.TIGER_SHARK_SADDLE.get());
                        output.accept(OWItems.RESURRECTION_AMULET.get());
                        output.accept(OWItems.TRANQUILIZER_ARROW.get());
                        output.accept(OWItems.WOODEN_STINGER.get());
                        output.accept(OWItems.TRANQUILIZER_WOODEN_STINGER.get());
                        output.accept(OWItems.VENOMOUS_ARROW.get());
                        output.accept(OWItems.TIGER_FUR.get());
                        output.accept(OWItems.BOA_TONG.get());
                        output.accept(OWItems.PREDATOR_TOOTH.get());
                        output.accept(OWItems.VENOMOUS_GLANDS.get());
                        output.accept(OWItems.VENOMOUS_TOOTH.get());
                        output.accept(OWItems.PEACOCK_FEATHER.get());
                        output.accept(OWItems.SHARK_FIN.get());
                        output.accept(OWItems.STINGING_FILAMENT.get());
                        output.accept(OWItems.BIOLUMINESCENT_JELLY.get());
                        output.accept(OWItems.ANIMAL_SOUL.get());
                        output.accept(OWItems.RAW_BOA.get());
                        output.accept(OWItems.COOKED_BOA.get());
                        output.accept(OWItems.RAW_KODIAK.get());
                        output.accept(OWItems.COOKED_KODIAK.get());
                        output.accept(OWItems.RAW_PEACOCK.get());
                        output.accept(OWItems.COOKED_PEACOCK.get());
                        output.accept(OWItems.RAW_TIGER.get());
                        output.accept(OWItems.COOKED_TIGER.get());
                        output.accept(OWItems.SAVAGE_BERRIES.get());
                        output.accept(OWItems.BATTERY.get());
                        output.accept(OWItems.SEABUG.get());
                        output.accept(OWItems.SEABUG_COCKPIT.get());
                        output.accept(OWItems.SEABUG_HULL.get());
                        output.accept(OWItems.SEABUG_PORTHOLE.get());
                        output.accept(OWBlocks.SADDLER.get());
                        output.accept(OWBlocks.SCARIFIED_OAK_LOG.get());
                        output.accept(OWBlocks.SCARIFIED_DARK_OAK_LOG.get());
                        output.accept(OWBlocks.SCARIFIED_SPRUCE_LOG.get());
                        output.accept(OWBlocks.SCARIFIED_MANGROVE_LOG.get());
                        output.accept(OWBlocks.SCARIFIED_CHERRY_LOG.get());
                        output.accept(OWBlocks.SCARIFIED_BIRCH_LOG.get());
                        output.accept(OWBlocks.SCARIFIED_ACACIA_LOG.get());
                        output.accept(OWBlocks.SCARIFIED_JUNGLE_LOG.get());
                        output.accept(OWBlocks.REDWOOD_LOG.get());
                        output.accept(OWBlocks.REDWOOD_PLANKS.get());
                        output.accept(OWBlocks.REDWOOD_LEAVES.get());
                        output.accept(OWBlocks.REDWOOD_SAPLING.get());
                        output.accept(OWBlocks.LAVENDER.get());
                        output.accept(OWBlocks.CAMELLIA.get());
                        output.accept(OWBlocks.PEACOCK_EGG.get());
                        output.accept(OWItems.PLANT_EMPRESS_MUSIC_DISC.get());

                    })).build());
}