package net.tiew.operationWild.entity.taming;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.EventHooks;
import net.tiew.operationWild.core.OWTags;
import net.tiew.operationWild.core.OWUtils;
import net.tiew.operationWild.entity.behavior.KodiakBehaviorHandler;
import net.tiew.operationWild.entity.animals.terrestrial.KodiakEntity;

import java.util.List;

/**
 * This class primarily manages the taming process for the Kodiak.
 * It manages the taming method from start to finish.
 */

public class TamingKodiak {

    private KodiakEntity kodiak;
    private KodiakBehaviorHandler kodiakManagement;

    public TamingKodiak(KodiakEntity kodiak, KodiakBehaviorHandler kodiakManagement) {
        this.kodiak = kodiak;
        this.kodiakManagement = kodiakManagement;
    }

    public void tick() {
        handleTamingSystem();
    }

    private void handleTamingSystem() {
        List<ItemEntity> items = kodiak.level().getEntitiesOfClass(ItemEntity.class, kodiak.getBoundingBox().inflate(1));

        if (canBeTamable()) {
            for (ItemEntity item : items) {
                if (item.getItem().is(OWTags.Items.KODIAK_FOOD)) {
                    LivingEntity target = (LivingEntity) item.getOwner();
                    if (target != null) {
                        kodiak.setRolling(false);

                        if (!EventHooks.onAnimalTame(kodiak, (Player) target)) {
                            if (!kodiak.level().isClientSide() && kodiak.foodGiven >= (kodiak.foodWanted - 1)) {
                                kodiak.setTame(true, (Player) target);
                                kodiak.setDirty(false);

                                if (kodiak.getFoodPick() != null && !kodiak.getFoodPick().isEmpty()) {
                                    kodiakManagement.eatFoodInHisMouth(kodiak.getFoodPick());
                                }

                                kodiak.setSleeping(false);
                                kodiak.resetSleepBar();
                            }

                            if (target instanceof Player player) {
                                if (!player.isCreative()) {
                                    if (!kodiak.level().isClientSide()) {
                                        kodiak.setTarget(target);
                                    }
                                }
                            } else {
                                if (!kodiak.level().isClientSide()) {
                                    kodiak.setTarget(target);
                                }
                            }
                            kodiak.foodGiven++;
                            kodiak.playSound(SoundEvents.GENERIC_EAT);

                            ItemStack itemStack = item.getItem();
                            Vec3 lookDirection = kodiak.getLookAngle();
                            double spawnX = kodiak.getX() + lookDirection.x * 2.0;
                            double spawnY = kodiak.getY() + 0.8;
                            double spawnZ = kodiak.getZ() + lookDirection.z * 2.0;

                            OWUtils.spawnItemParticles(kodiak, itemStack, spawnX, spawnY, spawnZ);
                            OWUtils.spawnComposterParticlesAround(kodiak, ParticleTypes.COMPOSTER);

                            item.discard();
                            break;
                        }
                    }
                }
            }
        }
    }

    public boolean canBeTamable() {
        return kodiak.isDirty() && !kodiak.isNapping() && kodiak.isRolling();
    }
}