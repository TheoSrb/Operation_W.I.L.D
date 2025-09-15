package net.tiew.operationWild;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Style;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.block.FlowerPotBlock;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;
import net.neoforged.neoforge.common.util.TriState;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.player.ItemEntityPickupEvent;
import net.tiew.operationWild.block.OWBlocks;
import net.tiew.operationWild.component.OWDataComponentTypes;
import net.tiew.operationWild.effect.OWEffects;
import net.tiew.operationWild.enchantment.OWEnchantments;
import net.tiew.operationWild.enchantment.OWEnchantmentsEffects;
import net.tiew.operationWild.entity.OWEntity;
import net.tiew.operationWild.entity.OWEntityRegistry;
import net.tiew.operationWild.entity.client.render.*;
import net.tiew.operationWild.entity.client.render.misc.*;
import net.tiew.operationWild.entity.misc.SeaBugEntity;
import net.tiew.operationWild.event.ColorEvents;
import net.tiew.operationWild.gui.HeartRenderHandler;
import net.tiew.operationWild.item.OWCreativeTabs;
import net.tiew.operationWild.item.OWItems;
import net.tiew.operationWild.particle.ElectricParticles;
import net.tiew.operationWild.particle.NapParticles;
import net.tiew.operationWild.particle.OWParticles;
import net.tiew.operationWild.screen.OWMenuRegister;
import net.tiew.operationWild.screen.blocks.SaddlerScreen;
import net.tiew.operationWild.screen.entity.OWInventoryScreen;
import net.tiew.operationWild.screen.entity.submarine.SeaBugInventoryScreen;
import net.tiew.operationWild.screen.player.adventurer_manuscript.AdventurerManuscriptScreen;
import net.tiew.operationWild.sound.OWSounds;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.entity.living.LivingChangeTargetEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.tiew.operationWild.worldgen.tree.foliage.OWFoliagePlacerType;
import net.tiew.operationWild.worldgen.tree.trunk.OWTrunkPlacerTypes;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

@Mod(OperationWild.MOD_ID)
public class OperationWild {

    public static final String MOD_ID = "ow";

    public OperationWild(IEventBus modEventBus, ModContainer modContainer) {

        modEventBus.addListener(this::commonSetup);

        NeoForge.EVENT_BUS.register(this);

        OWEntityRegistry.register(modEventBus);
        OWEnchantmentsEffects.register(modEventBus);
        OWCreativeTabs.register(modEventBus);
        OWItems.register(modEventBus);
        OWBlocks.register(modEventBus);
        OWSounds.register(modEventBus);
        OWMenuRegister.register(modEventBus);
        OWEffects.register(modEventBus);
        OWDataComponentTypes.register(modEventBus);
        OWParticles.register(modEventBus);
        OWTrunkPlacerTypes.register(modEventBus);
        OWFoliagePlacerType.register(modEventBus);
        ColorEvents.register(modEventBus);
        //OWDailyQuests.run();

        modEventBus.addListener(this::addCreative);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {((FlowerPotBlock) Blocks.FLOWER_POT).addPlant(OWBlocks.LAVENDER.getId(), OWBlocks.POTTED_LAVENDER);});
        event.enqueueWork(() -> {((FlowerPotBlock) Blocks.FLOWER_POT).addPlant(OWBlocks.CAMELLIA.getId(), OWBlocks.POTTED_CAMELLIA);});
    }

    private void addCreative(BuildCreativeModeTabContentsEvent event) {
    }

    private final Set<UUID> revealedTargets = new HashSet<>();
    private LivingEntity lastVisibleTarget = null;

    @SubscribeEvent
    public void onItemPickup(ItemEntityPickupEvent.Pre event) {
        ItemStack pickedUpStack = event.getItemEntity().getItem();
        Player player = event.getPlayer();

        if (pickedUpStack.getItem() == OWItems.ANIMAL_SOUL.get()) {
            boolean alreadyHasItem = player.getInventory().items.stream()
                    .anyMatch(stack -> stack.getItem() == OWItems.ANIMAL_SOUL.get());

            if (alreadyHasItem) {
                event.setCanPickup(TriState.FALSE);
            }
        }
    }

    @SubscribeEvent
    public void onLivingChangeTarget(LivingChangeTargetEvent event) {
        if (event.getEntity() instanceof Mob mob) {
            LivingEntity target = mob.getTarget();

            if (target != null) {
                if (target.getRootVehicle() instanceof OWEntity owVehicle && !target.equals(owVehicle)) {
                    event.setNewAboutToBeSetTarget(owVehicle);
                    return;
                }

                if (target.getRootVehicle() instanceof SeaBugEntity seaBug && !target.equals(seaBug)) {
                    event.setNewAboutToBeSetTarget(seaBug);
                    return;
                }

                boolean hasCamouflage = target.hasEffect(OWEffects.CAMOUFLAGE_EFFECT.getDelegate()) && target.isSteppingCarefully();
                boolean isRevealed = revealedTargets.contains(target.getUUID());
                boolean hasSight = mob.hasLineOfSight(target);

                if (!hasSight) {
                    if (lastVisibleTarget != null && lastVisibleTarget.equals(target)) {
                        lastVisibleTarget = null;
                    }
                    revealedTargets.remove(target.getUUID());
                    event.setCanceled(true);
                    return;
                }

                if (!hasCamouflage || isRevealed) {
                    lastVisibleTarget = target;
                    return;
                }

                event.setCanceled(true);
            }
        }
    }

    @SubscribeEvent
    public void onLivingHurt(LivingDamageEvent.Pre event) {
        if (event.getEntity() instanceof Mob mob) {
            Entity attacker = event.getSource().getEntity();
            if (attacker instanceof LivingEntity attackerLiving) {
                lastVisibleTarget = attackerLiving;

                boolean hasCamouflage = attackerLiving.hasEffect(OWEffects.CAMOUFLAGE_EFFECT.getDelegate()) && attackerLiving.isSteppingCarefully();
                if (hasCamouflage) {
                    revealedTargets.add(attackerLiving.getUUID());
                }
            }
        }
        if (event.getEntity().hasEffect(OWEffects.VENOM_EFFECT.getDelegate())) {
            int venomLevel = Objects.requireNonNull(event.getEntity().getEffect(OWEffects.VENOM_EFFECT.getDelegate())).getAmplifier() + 1;
            float venomDamage = 1.5F + ((float) venomLevel / 5);
            Holder<Enchantment> slidingHolder = event.getEntity().level().registryAccess()
                    .registryOrThrow(Registries.ENCHANTMENT)
                    .getHolderOrThrow(OWEnchantments.VENOM_PROTECTION);

            int venomProtectionLevel = EnchantmentHelper.getEnchantmentLevel(slidingHolder, event.getEntity());
            float modifier = venomDamage * (1 - ((float) venomProtectionLevel / 10));
            modifier = Math.max(modifier, 1.0F);
            event.setNewDamage(event.getNewDamage() * modifier);
        }
    }

    public static void addEntityToManuscript(EntityType<? extends OWEntity> entityType, int page, Player player) {
        AdventurerManuscriptScreen.addEntityToManuscript(entityType, page, player);
    }

    public static int getMaxPageForEntityInManuscript(EntityType<? extends OWEntity> entityType) {
        if (entityType == OWEntityRegistry.TIGER.get()) {
            return 4;
        } else if (entityType == OWEntityRegistry.BOA.get()) {
            return 4;
        } else if (entityType == OWEntityRegistry.PEACOCK.get()) {
            return 3;
        } else if (entityType == OWEntityRegistry.TIGER_SHARK.get()) {
            return 3;
        } else if (entityType == OWEntityRegistry.HYENA.get()) {
            return 2;
        } else if (entityType == OWEntityRegistry.KODIAK.get()) {
            return 3;
        } else if (entityType == OWEntityRegistry.RED_PANDA.get()) {
            return 2;
        } else if (entityType == OWEntityRegistry.CHAMELEON.get()) {
            return 2;
        } else if (entityType == OWEntityRegistry.JELLYFISH.get()) {
            return 2;
        } else if (entityType == OWEntityRegistry.MANTA.get()) {
            return 2;
        } else if (entityType == OWEntityRegistry.WALRUS.get()) {
            return 3;
        } else if (entityType == OWEntityRegistry.ELEPHANT.get()) {
            return 3;
        } else if (entityType == OWEntityRegistry.MANDRILL.get()) {
            return 2;
        }
        return -1;
    }

    @SubscribeEvent
    public void onRegisterCommands(RegisterCommandsEvent event) {
        AddExperienceCommand.register(event.getDispatcher());
        SetPrestigeCommand.register(event.getDispatcher());
    }

    @EventBusSubscriber(modid = MOD_ID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents {

        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {
            EntityRenderers.register(OWEntityRegistry.TIGER.get(), TigerRenderer::new);
            EntityRenderers.register(OWEntityRegistry.MANDRILL.get(), MandrillRenderer::new);
            EntityRenderers.register(OWEntityRegistry.ELEPHANT.get(), ElephantRenderer::new);
            EntityRenderers.register(OWEntityRegistry.WALRUS.get(), WalrusRenderer::new);
            EntityRenderers.register(OWEntityRegistry.MANTA.get(), MantaRenderer::new);
            EntityRenderers.register(OWEntityRegistry.JELLYFISH.get(), JellyfishRenderer::new);
            EntityRenderers.register(OWEntityRegistry.CHAMELEON.get(), ChameleonRenderer::new);
            EntityRenderers.register(OWEntityRegistry.RED_PANDA.get(), RedPandaRenderer::new);
            EntityRenderers.register(OWEntityRegistry.KODIAK.get(), KodiakRenderer::new);
            EntityRenderers.register(OWEntityRegistry.HYENA.get(), HyenaRenderer::new);
            EntityRenderers.register(OWEntityRegistry.SEABUG.get(), SeaBugRenderer::new);
            EntityRenderers.register(OWEntityRegistry.PLANT_EMPRESS.get(), PlantEmpressRenderer::new);
            EntityRenderers.register(OWEntityRegistry.BOA.get(), BoaRenderer::new);
            EntityRenderers.register(OWEntityRegistry.PEACOCK.get(), PeacockRenderer::new);
            EntityRenderers.register(OWEntityRegistry.TIGER_SHARK.get(), TigerSharkRenderer::new);
            EntityRenderers.register(OWEntityRegistry.PRIMITIVE_SPEAR_PROJECTILE.get(), PrimitiveSpearProjectileRenderer::new);
            EntityRenderers.register(OWEntityRegistry.TRANQUILIZER_ARROW.get(), TranquilizerArrowRenderer::new);
            EntityRenderers.register(OWEntityRegistry.WOODEN_STINGER.get(), WoodenStingerRenderer::new);
            EntityRenderers.register(OWEntityRegistry.TRANQUILIZER_WOODEN_STINGER.get(), TranquilizerWoodenStingerRenderer::new);
            EntityRenderers.register(OWEntityRegistry.VENOMOUS_ARROW.get(), VenomousArrowRenderer::new);
            EntityRenderers.register(OWEntityRegistry.SLINGSHOT_PROJECTILE.get(), SlingshotProjectileRenderer::new);

            EntityRenderers.register(OWEntityRegistry.SEABUG_SHARD_0.get(), SeaBugShard0Renderer::new);
            EntityRenderers.register(OWEntityRegistry.SEABUG_SHARD_1.get(), SeaBugShard1Renderer::new);
            EntityRenderers.register(OWEntityRegistry.SEABUG_SHARD_2.get(), SeaBugShard2Renderer::new);

            EntityRenderers.register(OWEntityRegistry.ADVENTURER_MANUSCRIPT.get(), AdventurerManuscriptRenderer::new);

            HeartRenderHandler.register();
        }

        @SubscribeEvent
        public static void onRegisterMenuScreens(RegisterMenuScreensEvent event) {
            event.register(OWMenuRegister.OW_INVENTORY_MENU.get(), OWInventoryScreen::new);
            event.register(OWMenuRegister.SEABUG_INVENTORY_MENU.get(), SeaBugInventoryScreen::new);
            event.register(OWMenuRegister.SADDLER_MENU.get(), SaddlerScreen::new);
        }

        @SubscribeEvent
        public static void registerParticleFactories(RegisterParticleProvidersEvent event) {
            event.registerSpriteSet(OWParticles.ELECTRIC_PARTICLES.get(), ElectricParticles.Provider::new);
            event.registerSpriteSet(OWParticles.NAP_PARTICLES.get(), NapParticles.Provider::new);
        }
    }

    public static class AddExperienceCommand {
        public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
            dispatcher.register(
                    Commands.literal("addexperience").then(Commands.argument("amount", IntegerArgumentType.integer(1)).executes(AddExperienceCommand::execute))
            );
        }

        private static int execute(CommandContext<CommandSourceStack> context) {
            CommandSourceStack source = context.getSource();
            int amount = IntegerArgumentType.getInteger(context, "amount");
            try {
                ServerPlayer player = source.getPlayerOrException();
                if (player.getRootVehicle() != null && player.getRootVehicle() != player) {
                    OWEntity.addExperienceCommand((OWEntity) player.getRootVehicle(), amount);
                    source.sendSuccess(() -> Component.translatable("addExperienceCommandWork", amount).setStyle(Style.EMPTY.withColor(0x00FF00)), false);
                } else source.sendSuccess(() -> Component.translatable("addExperienceCommandError").setStyle(Style.EMPTY.withColor(0xFF0000)), false);
            } catch (Exception ignored) {
            }
            return 1;
        }
    }

    public static class SetPrestigeCommand {
        public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
            dispatcher.register(
                    Commands.literal("setprestige").then(Commands.argument("amount", IntegerArgumentType.integer(0, 999)).executes(SetPrestigeCommand::execute))
            );
        }

        private static int execute(CommandContext<CommandSourceStack> context) {
            CommandSourceStack source = context.getSource();
            int amount = IntegerArgumentType.getInteger(context, "amount");
            try {
                ServerPlayer player = source.getPlayerOrException();
                if (player.getRootVehicle() != null && player.getRootVehicle() != player) {
                    if (player.getRootVehicle() instanceof OWEntity owEntity) owEntity.setPrestigeLevel(amount);
                    source.sendSuccess(() -> Component.translatable("addPrestigeCommandWork", amount).setStyle(Style.EMPTY.withColor(0x00FF00)), false);
                } else source.sendSuccess(() -> Component.translatable("addPrestigeCommandError").setStyle(Style.EMPTY.withColor(0xFF0000)), false);
            } catch (Exception ignored) {
            }
            return 1;
        }
    }
}
