package net.tiew.operationWild.event;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingShieldBlockEvent;
import net.tiew.operationWild.OperationWild;
import net.tiew.operationWild.entity.OWEntity;
import net.tiew.operationWild.entity.OWSaddleShield;

@EventBusSubscriber(modid = OperationWild.MOD_ID, bus = EventBusSubscriber.Bus.GAME)
public class OWSaddleShieldEvents {

    @SubscribeEvent
    public static void onShieldBlock(LivingShieldBlockEvent event) {
        if (event.getBlocked()) return;

        LivingEntity victim = event.getEntity();
        if (victim.level().isClientSide()) return;

        OWEntity mount = victim instanceof OWEntity self ? self
                : victim.getVehicle() instanceof OWEntity vehicle ? vehicle : null;
        if (mount == null) return;

        DamageSource source = event.getDamageSource();
        Vec3 impact = OWSaddleShield.intercept(mount, victim, source);
        if (impact == null || victim.getRandom().nextFloat() >= OWSaddleShield.BLOCK_CHANCE) return;

        event.setBlocked(true);
        event.setShieldDamage(0f);

        Player hearsVanillaParry = victim instanceof Player player ? player : null;
        victim.level().playSound(hearsVanillaParry, impact.x, impact.y, impact.z, SoundEvents.SHIELD_BLOCK,
                victim.getSoundSource(), 1.0f, 0.8f + victim.getRandom().nextFloat() * 0.4f);

        if (!source.is(DamageTypeTags.IS_PROJECTILE)
                && source.getDirectEntity() instanceof LivingEntity attacker
                && attacker.canDisableShield()) {
            mount.disableSaddleShield(OWSaddleShield.AXE_DISABLE_TICKS);
            if (!(victim instanceof Player)) {
                victim.level().playSound(null, impact.x, impact.y, impact.z, SoundEvents.SHIELD_BREAK,
                        victim.getSoundSource(), 0.8f, 0.8f + victim.getRandom().nextFloat() * 0.4f);
            }
        }
    }
}
