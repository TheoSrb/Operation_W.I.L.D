package net.tiew.operationWild.quests;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.tiew.operationWild.OperationWild;
import net.tiew.operationWild.entity.animals.terrestrial.TigerEntity;

@EventBusSubscriber(modid = OperationWild.MOD_ID, bus = EventBusSubscriber.Bus.GAME)
public class CosmeticsQuestEvents {

    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) {
        if (event.getEntity().level().isClientSide) return;

        LivingEntity victim = event.getEntity();

        if (event.getSource().getEntity() instanceof TigerEntity tiger && tiger.isTame() && !(victim instanceof Player)) {
            // Le compte est tenu sur la créature elle-même : c'est ce qui le fait parvenir au client,
            // et donc à la barre d'avancement, en partie multijoueur comme en solo.
            tiger.addCosmeticQuestKill();
        }
    }
}
