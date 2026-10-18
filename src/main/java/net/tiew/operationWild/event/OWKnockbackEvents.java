package net.tiew.operationWild.event;

import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingKnockBackEvent;
import net.tiew.operationWild.OperationWild;
import net.tiew.operationWild.core.OWUtils;
import net.tiew.operationWild.entity.OWEntity;

@EventBusSubscriber(modid = OperationWild.MOD_ID, bus = EventBusSubscriber.Bus.GAME)
public class OWKnockbackEvents {

    @SubscribeEvent
    public static void onKnockBack(LivingKnockBackEvent event) {
        LivingEntity target = event.getEntity();
        if (target.level().isClientSide()) return;
        if (!(target.getLastHurtByMob() instanceof OWEntity attacker)) return;
        if (target.getLastHurtByMobTimestamp() != target.tickCount) return;

        double resistance = Mth.clamp(target.getAttributeValue(Attributes.KNOCKBACK_RESISTANCE), 0.0, 1.0);
        if (resistance <= 0.0 || resistance >= 1.0) return;

        double boost = OWUtils.knockbackFactor(attacker, target) / (1.0 - resistance);
        event.setStrength((float) (event.getStrength() * boost));
    }
}
