package net.tiew.operationWild.quests;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.tiew.operationWild.OperationWild;
import net.tiew.operationWild.core.OWDatasSave;
import net.tiew.operationWild.core.OWServerData;
import net.tiew.operationWild.entity.animals.terrestrial.TigerEntity;

@EventBusSubscriber(modid = OperationWild.MOD_ID, bus = EventBusSubscriber.Bus.GAME)
public class CosmeticsQuestEvents {

    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) {
        if (event.getEntity().level().isClientSide) return;

        LivingEntity victim = event.getEntity();

        if (event.getSource().getEntity() instanceof TigerEntity tiger && tiger.isTame() && !(victim instanceof Player)) {
            OWServerData.tigerKillCounts
                    .computeIfAbsent(tiger.getUUID(), k -> new java.util.concurrent.atomic.AtomicInteger(0))
                    .incrementAndGet();
        }
    }
}
