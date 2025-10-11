package net.tiew.operationWild.event;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.tiew.operationWild.OperationWild;
import net.tiew.operationWild.core.OWKeysBinding;
import net.tiew.operationWild.entity.client.model.*;
import net.tiew.operationWild.entity.client.model.misc.*;

@OnlyIn(Dist.CLIENT)
@EventBusSubscriber(modid = OperationWild.MOD_ID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ModClientEventBusEvents {

    @SubscribeEvent
    public static void onKeyRegister(RegisterKeyMappingsEvent event) {
        event.register(OWKeysBinding.OW_ULTIMATE);
        event.register(OWKeysBinding.OW_ATTACKS_INFO);
        event.register(OWKeysBinding.OW_ENTITY_JOURNAL);
    }

    @SubscribeEvent
    public static void registerLayer(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(TigerModel.LAYER_LOCATION, TigerModel::createBodyLayer);
        event.registerLayerDefinition(MandrillModel.LAYER_LOCATION, MandrillModel::createBodyLayer);
        event.registerLayerDefinition(ElephantModel.LAYER_LOCATION, ElephantModel::createBodyLayer);
        event.registerLayerDefinition(CrocodileModel.LAYER_LOCATION, CrocodileModel::createBodyLayer);
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
}