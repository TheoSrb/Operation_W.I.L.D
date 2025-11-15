package net.tiew.operationWild.event;

import net.minecraft.world.entity.SpawnPlacementTypes;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.level.levelgen.Heightmap;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.event.entity.RegisterSpawnPlacementsEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.tiew.operationWild.OperationWild;
import net.tiew.operationWild.entity.OWEntityRegistry;
import net.tiew.operationWild.entity.animals.aquatic.*;
import net.tiew.operationWild.entity.animals.terrestrial.*;
import net.tiew.operationWild.entity.bosses.PlantEmpressEntity;
import net.tiew.operationWild.entity.misc.*;
import net.tiew.operationWild.networking.OWNetworkHandler;

@EventBusSubscriber(modid = OperationWild.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
public class ModEventBusEvents {

    @SubscribeEvent
    public static void onRegisterPayloadHandlers(RegisterPayloadHandlersEvent event) {
        OWNetworkHandler.register(event);
    }

    @SubscribeEvent
    public static void registerAttributes(EntityAttributeCreationEvent event) {
        event.put(OWEntityRegistry.TIGER.get(), TigerEntity.createAttributes().build());
        event.put(OWEntityRegistry.MANDRILL.get(), MandrillEntity.createAttributes().build());
        event.put(OWEntityRegistry.CROCODILE.get(), CrocodileEntity.createAttributes().build());
        event.put(OWEntityRegistry.LION.get(), LionEntity.createAttributes().build());
        event.put(OWEntityRegistry.ELEPHANT.get(), ElephantEntity.createAttributes().build());
        event.put(OWEntityRegistry.WALRUS.get(), WalrusEntity.createAttributes().build());
        event.put(OWEntityRegistry.MANTA.get(), MantaEntity.createAttributes().build());
        event.put(OWEntityRegistry.JELLYFISH.get(), JellyfishEntity.createAttributes().build());
        event.put(OWEntityRegistry.CHAMELEON.get(), ChameleonEntity.createAttributes().build());
        event.put(OWEntityRegistry.RED_PANDA.get(), RedPandaEntity.createAttributes().build());
        event.put(OWEntityRegistry.KODIAK.get(), KodiakEntity.createAttributes().build());
        event.put(OWEntityRegistry.HYENA.get(), HyenaEntity.createAttributes().build());
        event.put(OWEntityRegistry.SEABUG.get(), SeaBugEntity.createAttributes().build());
        event.put(OWEntityRegistry.PLANT_EMPRESS.get(), PlantEmpressEntity.createAttributes().build());
        event.put(OWEntityRegistry.BOA.get(), BoaEntity.createAttributes().build());
        event.put(OWEntityRegistry.PEACOCK.get(), PeacockEntity.createAttributes().build());
        event.put(OWEntityRegistry.TIGER_SHARK.get(), TigerSharkEntity.createAttributes().build());

        event.put(OWEntityRegistry.SEABUG_SHARD_0.get(), SeaBugShard0Entity.createAttributes().build());
        event.put(OWEntityRegistry.SEABUG_SHARD_1.get(), SeaBugShard1Entity.createAttributes().build());
        event.put(OWEntityRegistry.SEABUG_SHARD_2.get(), SeaBugShard2Entity.createAttributes().build());

        event.put(OWEntityRegistry.ADVENTURER_MANUSCRIPT.get(), AdventurerManuscript.createAttributes().build());
    }

    @SubscribeEvent
    public static void registerSpawnPlacements(RegisterSpawnPlacementsEvent event) {
        event.register(OWEntityRegistry.TIGER.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                Animal::checkAnimalSpawnRules, RegisterSpawnPlacementsEvent.Operation.AND);
        event.register(OWEntityRegistry.BOA.get(), SpawnPlacementTypes.NO_RESTRICTIONS, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                Animal::checkAnimalSpawnRules, RegisterSpawnPlacementsEvent.Operation.AND);
        event.register(OWEntityRegistry.PEACOCK.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                Animal::checkAnimalSpawnRules, RegisterSpawnPlacementsEvent.Operation.AND);
        event.register(OWEntityRegistry.JELLYFISH.get(), SpawnPlacementTypes.IN_WATER, Heightmap.Types.OCEAN_FLOOR,
                JellyfishEntity::checkSurfaceWaterAnimalSpawnRules, RegisterSpawnPlacementsEvent.Operation.REPLACE);
        event.register(OWEntityRegistry.KODIAK.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                Animal::checkAnimalSpawnRules, RegisterSpawnPlacementsEvent.Operation.AND);
        event.register(OWEntityRegistry.CROCODILE.get(), SpawnPlacementTypes.NO_RESTRICTIONS, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                CrocodileEntity::checkCrocodileSpawnRules, RegisterSpawnPlacementsEvent.Operation.AND);
    }
}