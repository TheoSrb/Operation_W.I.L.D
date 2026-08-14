package net.tiew.operationWild.event;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.tiew.operationWild.OperationWild;
import net.tiew.operationWild.core.OWTags;
import net.tiew.operationWild.entity.animals.terrestrial.RedPandaEntity;

@EventBusSubscriber(modid = OperationWild.MOD_ID, bus = EventBusSubscriber.Bus.GAME)
public class OWSweetLureTracker {

    public static final double LURE_RANGE = 48.0;

    @SubscribeEvent
    public static void onSweetPlaced(BlockEvent.EntityPlaceEvent event) {
        if (!(event.getLevel() instanceof ServerLevel level)) return;
        if (!(event.getEntity() instanceof Player)) return;
        if (!event.getPlacedBlock().is(OWTags.Blocks.RED_PANDA_SWEETS)) return;

        BlockPos pos = event.getPos().immutable();
        AABB range = new AABB(pos).inflate(LURE_RANGE);

        for (RedPandaEntity panda : level.getEntitiesOfClass(RedPandaEntity.class, range)) {
            panda.noticeSweetLure(pos);
        }
    }
}
