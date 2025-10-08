package net.tiew.operationWild.event;

import net.minecraft.world.entity.SpawnPlacementTypes;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.level.levelgen.Heightmap;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.event.entity.RegisterSpawnPlacementsEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.tiew.operationWild.OperationWild;
import net.tiew.operationWild.entity.OWEntityRegistry;
import net.tiew.operationWild.entity.animals.aquatic.JellyfishEntity;
import net.tiew.operationWild.entity.animals.aquatic.MantaEntity;
import net.tiew.operationWild.entity.animals.aquatic.TigerSharkEntity;
import net.tiew.operationWild.entity.animals.aquatic.WalrusEntity;
import net.tiew.operationWild.entity.animals.terrestrial.*;
import net.tiew.operationWild.entity.client.model.*;
import net.tiew.operationWild.entity.client.model.misc.*;
import net.tiew.operationWild.entity.bosses.PlantEmpressEntity;
import net.tiew.operationWild.entity.misc.*;
import net.tiew.operationWild.networking.OWNetworkHandler;
import net.tiew.operationWild.core.OWKeysBinding;

@EventBusSubscriber(modid = OperationWild.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
public class ModEventBusEvents {

    @SubscribeEvent
    public static void onKeyRegister(RegisterKeyMappingsEvent event) {
        event.register(OWKeysBinding.OW_ULTIMATE);
        event.register(OWKeysBinding.OW_ATTACKS_INFO);
        event.register(OWKeysBinding.OW_ENTITY_JOURNAL);
    }

    @SubscribeEvent
    public static void onRegisterPayloadHandlers(RegisterPayloadHandlersEvent event) {
        OWNetworkHandler.register(event);
    }

    @SubscribeEvent
    public static void registerLayer(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(TigerModel.LAYER_LOCATION, TigerModel::createBodyLayer);
        event.registerLayerDefinition(MandrillModel.LAYER_LOCATION, MandrillModel::createBodyLayer);
        event.registerLayerDefinition(ElephantModel.LAYER_LOCATION, ElephantModel::createBodyLayer);
        event.registerLayerDefinition(WalrusModel.LAYER_LOCATION, WalrusModel::createBodyLayer);
        event.registerLayerDefinition(MantaModel.LAYER_LOCATION, MantaModel::createBodyLayer);
        event.registerLayerDefinition(JellyfishModel.LAYER_LOCATION, JellyfishModel::createBodyLayer);
        event.registerLayerDefinition(ChameleonModel.LAYER_LOCATION, ChameleonModel::createBodyLayer);
        event.registerLayerDefinition(RedPandaModel.LAYER_LOCATION, RedPandaModel::createBodyLayer);
        event.registerLayerDefinition(KodiakModel.LAYER_LOCATION, KodiakModel::createBodyLayer);
        event.registerLayerDefinition(HyenaModel.LAYER_LOCATION, HyenaModel::createBodyLayer);
        event.registerLayerDefinition(SeaBugModel.LAYER_LOCATION, SeaBugModel::createBodyLayer);
        event.registerLayerDefinition(PlantEmpressModel.LAYER_LOCATION, PlantEmpressModel::createBodyLayer);
        event.registerLayerDefinition(BoaModel.LAYER_LOCATION, BoaModel::createBodyLayer);
        event.registerLayerDefinition(PeacockModel.LAYER_LOCATION, PeacockModel::createBodyLayer);
        event.registerLayerDefinition(TigerSharkModel.LAYER_LOCATION, TigerSharkModel::createBodyLayer);

        event.registerLayerDefinition(SeaBugShard0Model.LAYER_LOCATION, SeaBugShard0Model::createBodyLayer);
        event.registerLayerDefinition(SeaBugShard1Model.LAYER_LOCATION, SeaBugShard1Model::createBodyLayer);
        event.registerLayerDefinition(SeaBugShard2Model.LAYER_LOCATION, SeaBugShard2Model::createBodyLayer);

        event.registerLayerDefinition(PrimitiveSpearProjectileModel.LAYER_LOCATION, PrimitiveSpearProjectileModel::createBodyLayer);
        event.registerLayerDefinition(TranquilizerArrowModel.LAYER_LOCATION, TranquilizerArrowModel::createBodyLayer);
        event.registerLayerDefinition(WoodenStingerModel.LAYER_LOCATION, WoodenStingerModel::createBodyLayer);
        event.registerLayerDefinition(TranquilizerWoodenStingerModel.LAYER_LOCATION, TranquilizerWoodenStingerModel::createBodyLayer);
        event.registerLayerDefinition(VenomousArrowModel.LAYER_LOCATION, VenomousArrowModel::createBodyLayer);
        event.registerLayerDefinition(SlingshotProjectileModel.LAYER_LOCATION, SlingshotProjectileModel::createBodyLayer);

        event.registerLayerDefinition(AdventurerManuscriptModel.LAYER_LOCATION, AdventurerManuscriptModel::createBodyLayer);
    }

    @SubscribeEvent
    public static void registerAttributes(EntityAttributeCreationEvent event) {
        event.put(OWEntityRegistry.TIGER.get(), TigerEntity.createAttributes().build());
        event.put(OWEntityRegistry.MANDRILL.get(), MandrillEntity.createAttributes().build());
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


    }
}
