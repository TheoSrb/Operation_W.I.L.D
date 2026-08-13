package net.tiew.operationWild.event;

import net.minecraft.client.Minecraft;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public final class OWFeedingEffects {

    private static final int CHEW_PERIOD = 7;
    private static final float CHEW_DIP = 0.055f;
    private static final float CHEW_LEAN_DEGREES = 4.5f;
    private static final int FADE_TICKS = 6;

    private static final Map<Integer, Meal> MEALS = new HashMap<>();

    private OWFeedingEffects() {
    }

    private static final class Meal {
        int ticksLeft;
        final int total;
        final ItemStack food;

        Meal(int ticks, ItemStack food) {
            this.ticksLeft = ticks;
            this.total = Math.max(1, ticks);
            this.food = food;
        }
    }

    public static void begin(LivingEntity guest, int ticks, ItemStack food) {
        if (ticks <= 0 || food.isEmpty()) return;

        Meal current = MEALS.get(guest.getId());
        if (current != null && current.ticksLeft >= ticks) return;

        MEALS.put(guest.getId(), new Meal(ticks, food));
    }

    public static void clear() {
        MEALS.clear();
    }

    public static boolean isFeeding(LivingEntity guest) {
        return MEALS.containsKey(guest.getId());
    }

    /**
     * Amplitude du geste, de zero a un.
     *
     * <p>Elle s'eteint sur les dernieres images plutot que de s'arreter net : sans ce fondu, la bete
     * se redressait d'un bloc au dernier tick du repas.</p>
     */
    public static float intensity(LivingEntity guest, float partialTick) {
        Meal meal = MEALS.get(guest.getId());
        if (meal == null) return 0f;

        float remaining = meal.ticksLeft - partialTick;
        float rampIn = Mth.clamp((meal.total - remaining) / FADE_TICKS, 0f, 1f);
        float rampOut = Mth.clamp(remaining / FADE_TICKS, 0f, 1f);
        return Math.min(rampIn, rampOut);
    }

    public static float chewDip(LivingEntity guest, float ageInTicks, float partialTick) {
        float amount = intensity(guest, partialTick);
        if (amount <= 0f) return 0f;

        float wave = (Mth.cos(ageInTicks * (Mth.TWO_PI / CHEW_PERIOD)) + 1f) * 0.5f;
        return CHEW_DIP * amount * wave;
    }

    public static float chewLean(LivingEntity guest, float partialTick) {
        return CHEW_LEAN_DEGREES * intensity(guest, partialTick);
    }

    public static void tick(Minecraft minecraft) {
        if (MEALS.isEmpty()) return;

        Level level = minecraft.level;
        if (level == null) {
            MEALS.clear();
            return;
        }

        Iterator<Map.Entry<Integer, Meal>> iterator = MEALS.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<Integer, Meal> entry = iterator.next();
            Meal meal = entry.getValue();

            if (--meal.ticksLeft <= 0 || !(level.getEntity(entry.getKey()) instanceof LivingEntity guest)
                    || !guest.isAlive()) {
                iterator.remove();
                continue;
            }

            spawnCrumbs(level, guest, meal.food);

            if (meal.ticksLeft % CHEW_PERIOD == 0) {
                level.playLocalSound(guest.getX(), guest.getY(), guest.getZ(),
                        SoundEvents.GENERIC_EAT, SoundSource.NEUTRAL,
                        0.55f, 0.9f + level.random.nextFloat() * 0.3f, false);
            }
        }
    }

    /**
     * Miettes sous le museau, calquees sur {@code LivingEntity.spawnItemParticles}.
     *
     * <p>Le point d'emission est construit dans le repere de la bete puis tourne par son lacet et son
     * tangage : c'est ce qui le colle a la bouche quel que soit le gabarit, du poussin au pachyderme,
     * sans avoir a connaitre son squelette.</p>
     */
    private static void spawnCrumbs(Level level, LivingEntity guest, ItemStack food) {
        ItemParticleOption option = new ItemParticleOption(ParticleTypes.ITEM, food);

        for (int i = 0; i < 2; i++) {
            Vec3 velocity = new Vec3((level.random.nextDouble() - 0.5) * 0.1, 0.06, 0.0)
                    .xRot(-guest.getXRot() * Mth.DEG_TO_RAD)
                    .yRot(-guest.getYRot() * Mth.DEG_TO_RAD);

            Vec3 at = new Vec3((level.random.nextDouble() - 0.5) * 0.3,
                            -level.random.nextDouble() * 0.4 - 0.15,
                            guest.getBbWidth() * 0.5 + 0.1)
                    .xRot(-guest.getXRot() * Mth.DEG_TO_RAD)
                    .yRot(-guest.getYRot() * Mth.DEG_TO_RAD)
                    .add(guest.getX(), guest.getEyeY(), guest.getZ());

            level.addParticle(option, at.x, at.y, at.z, velocity.x, velocity.y + 0.05, velocity.z);
        }
    }
}
