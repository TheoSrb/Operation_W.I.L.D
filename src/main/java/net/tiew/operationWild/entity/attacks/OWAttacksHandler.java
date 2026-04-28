package net.tiew.operationWild.entity.attacks;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.settings.KeyConflictContext;
import net.tiew.operationWild.entity.OWEntity;
import net.tiew.operationWild.entity.animals.aquatic.CrocodileEntity;
import net.tiew.operationWild.entity.animals.terrestrial.KodiakEntity;
import net.tiew.operationWild.entity.animals.terrestrial.TigerEntity;
import net.tiew.operationWild.networking.packets.to_server.OWAttackPacket;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Central attack registry.
 *
 * To add a new attack:
 *   1. Declare a KeyMapping constant (or reuse an existing one).
 *   2. Create an OWAttack or OWChargedAttack inside the animal's inner class.
 *   3. Call register(YourEntity.class, YOUR_ATTACK) inside registerAll().
 *
 * No new packet class is needed — OWAttackPacket handles everything.
 */
public class OWAttacksHandler {

    public static final String OW_CATEGORY = "key.categories.operationwild";

    // ── Shared key bindings ────────────────────────────────────────────────────
    public static final KeyMapping OW_ATTACK_0 = new KeyMapping(
            "key.ow.attack_0",
            KeyConflictContext.IN_GAME,
            InputConstants.Type.MOUSE,
            GLFW.GLFW_MOUSE_BUTTON_RIGHT,
            OW_CATEGORY
    );

    public static final KeyMapping OW_ATTACK_1 = new KeyMapping(
            "key.ow.attack_1",
            KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_X,
            OW_CATEGORY
    );

    // ── Internal registries ───────────────────────────────────────────────────
    private static final Map<Class<? extends OWEntity>, List<OWAttack>>  ENTITY_ATTACKS    = new HashMap<>();
    private static final Map<Class<? extends OWEntity>, OWPassive>       ENTITY_PASSIVES   = new HashMap<>();
    // Rangée dans la texture ow_tamed_attacksl.png — chaque entité occupe 2 rangées (normale + grisée)
    // Rangée 0 → Y=0 (normal) / Y=20 (grisé) ; rangée 1 → Y=40 / Y=60 ; etc.
    private static final Map<Class<? extends OWEntity>, Integer>        ENTITY_TEXTURE_ROWS      = new HashMap<>();
    // timeMax de l'attaque combo (1er param de createCombo dans l'entité) — utilisé pour la carte 0
    private static final Map<Class<? extends OWEntity>, Integer>        ENTITY_COMBO_MAX_TIMERS  = new HashMap<>();

    // ── Registration ─────────────────────────────────────────────────────────

    /** Enregistre une attaque pour une entité. Aussi enregistrée dans OWAttackPacket. */
    public static void register(Class<? extends OWEntity> entityClass, OWAttack attack) {
        ENTITY_ATTACKS.computeIfAbsent(entityClass, k -> new ArrayList<>()).add(attack);
        OWAttackPacket.register(attack);
    }

    /**
     * Définit la rangée de texture pour une entité dans ow_tamed_attacksl.png.
     * Tigre = 0 → Y=0 (normal) / Y=20 (grisé)
     * Entité suivante = 1 → Y=40 / Y=60, etc.
     */
    public static void registerEntityRow(Class<? extends OWEntity> entityClass, int row) {
        ENTITY_TEXTURE_ROWS.put(entityClass, row);
    }

    /**
     * Définit le timeMax du combo pour la carte 0.
     * Correspond au 1er paramètre passé à createCombo() dans l'entité.
     */
    public static void registerComboMaxTimer(Class<? extends OWEntity> entityClass, int timeMax) {
        ENTITY_COMBO_MAX_TIMERS.put(entityClass, timeMax);
    }

    /** Enregistre le passif d'une entité (appelé côté client uniquement). */
    public static void registerPassive(Class<? extends OWEntity> entityClass, OWPassive passive) {
        ENTITY_PASSIVES.put(entityClass, passive);
    }

    /** Retourne le passif d'une entité, ou null si aucun. */
    public static OWPassive getPassive(Class<?> entityClass) {
        return ENTITY_PASSIVES.get(entityClass);
    }

    /**
     * À appeler une fois au démarrage (depuis OWNetworkHandler.register).
     * Ajouter ici un appel par nouvelle entité/attaque.
     */
    public static void registerAll() {
        // ── Tigre — rangée 0 (Y=0 / Y=20) ───────────────────────────────────────
        registerEntityRow(TigerEntity.class, 0);
        registerComboMaxTimer(TigerEntity.class, 12); // timeToHit(10) + 2
        register(TigerEntity.class, TigerAttacks.JUMP_ATTACK);
        register(TigerEntity.class, TigerAttacks.SHADOW_STRIKE);
        registerPassive(TigerEntity.class, TigerPassives.PREDATOR_SENSE);

        // ── Kodiak — rangée 1 (Y=40 / Y=60) ─────────────────────────────────────
        registerEntityRow(KodiakEntity.class, 1);
        registerComboMaxTimer(KodiakEntity.class, 14); // timeToHit(12) + 2
        register(KodiakEntity.class, KodiakAttacks.PAW_SLAM);

        // ── Crocodile — rangée 2 (Y=80 / Y=100) ──────────────────────────────────
        registerEntityRow(CrocodileEntity.class, 2);
        registerComboMaxTimer(CrocodileEntity.class, 17); // timeToHit(15) + 2
        register(CrocodileEntity.class, CrocodileAttacks.MOUTH_SLAM);
        register(CrocodileEntity.class, CrocodileAttacks.PRIMAL_DIVE);
        registerPassive(CrocodileEntity.class, CrocodilePassives.PRIMAL_DIVE_SENSE);
    }

    // ── Queries ───────────────────────────────────────────────────────────────

    /** Liste des attaques enregistrées pour une entité (null si aucune). */
    public static List<OWAttack> getAttacks(Class<?> entityClass) {
        return ENTITY_ATTACKS.get(entityClass);
    }

    /** Rangée de texture pour cette entité (0 par défaut). */
    public static int getEntityRow(Class<?> entityClass) {
        return ENTITY_TEXTURE_ROWS.getOrDefault(entityClass, 0);
    }

    /** timeMax du combo pour cette entité (10 par défaut si non déclaré). */
    public static int getComboMaxTimer(Class<?> entityClass) {
        return ENTITY_COMBO_MAX_TIMERS.getOrDefault(entityClass, 10);
    }

    /**
     * Trouve l'OWChargedAttack associée à une entité et un bouton souris.
     * Retourne null si aucune ne correspond.
     */
    public static OWChargedAttack findChargedAttack(Class<?> entityClass, int button) {
        List<OWAttack> attacks = ENTITY_ATTACKS.get(entityClass);
        if (attacks == null) return null;
        for (OWAttack attack : attacks) {
            if (attack instanceof OWChargedAttack charged
                    && charged.getKey().getKey().getValue() == button) {
                return charged;
            }
        }
        return null;
    }

    /**
     * Trouve une OWAttack instantanée (non chargée) associée à une entité et une touche clavier.
     * Retourne null si aucune ne correspond.
     */
    public static OWAttack findInstantAttack(Class<?> entityClass, int key) {
        List<OWAttack> attacks = ENTITY_ATTACKS.get(entityClass);
        if (attacks == null) return null;
        for (OWAttack attack : attacks) {
            if (!(attack instanceof OWChargedAttack)
                    && attack.getKey().getKey().getValue() == key) {
                return attack;
            }
        }
        return null;
    }

    // ── Tiger attacks ─────────────────────────────────────────────────────────
    public static class TigerAttacks {

        public static final int JUMP_ATTACK_COOLDOWN_TICKS = 400;
        public static final float JUMP_POWER = 1.75f;

        // ── Shadow Strike (ultime) ────────────────────────────────────────────
        public static final int    SHADOW_STRIKE_KILLS_REQUIRED  = 5;
        public static final int    SHADOW_STRIKE_DURATION_TICKS  = 200;                          // 10 s de phase cachée
        public static final long   SHADOW_STRIKE_DURATION_MS     = SHADOW_STRIKE_DURATION_TICKS * 50L; // 10 000 ms
        public static final int    SHADOW_STRIKE_COOLDOWN_TICKS  = 1200;                         // 60 s de cooldown
        public static final double SHADOW_STRIKE_DAMAGE_BONUS    = 0.25;                         // +25 % dégâts
        public static final float  SHADOW_STRIKE_SPEED_FACTOR    = 1.15f;                        // +15 % vitesse véhicule

        public static final OWAttack SHADOW_STRIKE = new OWAttack(
                2,
                OW_ATTACK_1,
                100f,
                entity -> ((TigerEntity) entity).activateShadowStrike(),
                SHADOW_STRIKE_COOLDOWN_TICKS
        ).withUnlockCondition(
                entity -> entity instanceof TigerEntity tiger
                        && tiger.getShadowStrikeKillCount() >= SHADOW_STRIKE_KILLS_REQUIRED
        ).withUnlockProgress(
                entity -> entity instanceof TigerEntity tiger
                        ? (float) tiger.getShadowStrikeKillCount() / SHADOW_STRIKE_KILLS_REQUIRED
                        : 0f
        ).withUltimateDuration(SHADOW_STRIKE_DURATION_MS);

        public static final OWChargedAttack JUMP_ATTACK = new OWChargedAttack(
                1,
                OW_ATTACK_0,
                100,
                JUMP_ATTACK_COOLDOWN_TICKS,
                1000L,   // charge minimale : 1 seconde
                2000L,   // charge maximale : 3 secondes
                entity -> ((TigerEntity) entity).startJumpCharge(),
                entity -> ((TigerEntity) entity).cancelJumpCharge(),
                (entity, factor) -> ((TigerEntity) entity).performJumpAttack(factor),
                (entity, factor, dir) -> {
                    Vec3 horizontal = new Vec3(dir.x, 0, dir.z);
                    if (horizontal.lengthSqr() > 1.0E-7) horizontal = horizontal.normalize();
                    float speed = ((0.55f + 0.75f * factor) * JUMP_POWER) * 2;
                    float vert  = (float) (((0.30f + 0.12f * factor) * JUMP_POWER) / 1.25);
                    entity.setDeltaMovement(horizontal.x * speed, vert, horizontal.z * speed);
                    entity.hasImpulse = true;
                    // Flag leaping immediately on the rider's client so travelRidden
                    // doesn't overwrite the jump velocity before the server sync arrives.
                    if (entity instanceof TigerEntity tiger) tiger.isLeaping = true;
                },
                true, // afficher le fantôme pendant la charge
                true  // effet de caméra pendant la charge
        );
    }

    // ── Kodiak attacks ────────────────────────────────────────────────────────
    public static class KodiakAttacks {

        public static final int PAW_SLAM_ID             = 5;
        public static final int PAW_SLAM_COOLDOWN_TICKS = 800; // 40 secondes

        public static final OWChargedAttack PAW_SLAM = new OWChargedAttack(
                PAW_SLAM_ID,
                OW_ATTACK_0,
                100f,
                PAW_SLAM_COOLDOWN_TICKS,
                1000L,   // charge minimale : 1 seconde
                3000L,   // charge maximale : 3 secondes
                entity -> ((KodiakEntity) entity).startPawSlamCharge(),
                entity -> ((KodiakEntity) entity).cancelPawSlamCharge(),
                (entity, factor) -> ((KodiakEntity) entity).performPawSlam(factor),
                (entity, factor, dir) -> {},
                false,
                true
        );
    }

    // ── Crocodile attacks ─────────────────────────────────────────────────────
    public static class CrocodileAttacks {

        public static final int MOUTH_SLAM_ID             = 3;
        public static final int MOUTH_SLAM_COOLDOWN_TICKS = 600; // 30 secondes

        // Charge RMB : 1 s minimum → 3 s maximum.
        // 5 dégâts à factor=0, 15 dégâts à factor=1 + gros recul + saignement 10 s au max.
        public static final OWChargedAttack MOUTH_SLAM = new OWChargedAttack(
                MOUTH_SLAM_ID,
                OW_ATTACK_0,
                100f,
                MOUTH_SLAM_COOLDOWN_TICKS,
                1000L,
                3000L,
                entity -> ((CrocodileEntity) entity).startMouthSlamCharge(),
                entity -> ((CrocodileEntity) entity).cancelMouthSlamCharge(),
                (entity, factor) -> ((CrocodileEntity) entity).performMouthSlam(factor),
                (entity, factor, dir) -> {},
                false,
                true
        ).withCanUsePredicate(entity -> !entity.isInWater());

        // Primal Dive — ultime
        public static final int PRIMAL_DIVE_ID             = 4;
        public static final int PRIMAL_DIVE_KILLS_REQUIRED = 5;
        public static final int PRIMAL_DIVE_COOLDOWN_TICKS = 1200; // 60 s
        public static final long PRIMAL_DIVE_TARGETING_MS  = 10_000L; // 10 s

        public static final OWAttack PRIMAL_DIVE = new OWAttack(
                PRIMAL_DIVE_ID,
                OW_ATTACK_1,
                100f,
                entity -> ((net.tiew.operationWild.entity.animals.aquatic.CrocodileEntity) entity).activatePrimalDive(),
                PRIMAL_DIVE_COOLDOWN_TICKS
        ).withUnlockCondition(
                entity -> entity instanceof net.tiew.operationWild.entity.animals.aquatic.CrocodileEntity croc
                        && croc.getUltimateKillCount() >= PRIMAL_DIVE_KILLS_REQUIRED
        ).withUnlockProgress(
                entity -> entity instanceof net.tiew.operationWild.entity.animals.aquatic.CrocodileEntity croc
                        ? (float) croc.getUltimateKillCount() / PRIMAL_DIVE_KILLS_REQUIRED
                        : 0f
        ).withUltimateDuration(PRIMAL_DIVE_TARGETING_MS);
    }

    // ── Crocodile passive ─────────────────────────────────────────────────────
    public static class CrocodilePassives {
        public static final OWPassive PRIMAL_DIVE_SENSE = new OWPassive() {
            @Override
            public Set<Integer> getHighlightEntityIds(OWEntity entity, Level level) {
                if (!OWAttackLogic.isCrocTargeting) return Set.of();
                int id = OWAttackLogic.crocTargetEntityId;
                if (id == -1) return Set.of();
                Entity e = level.getEntity(id);
                if (!(e instanceof LivingEntity le) || !le.isAlive()) return Set.of();
                return Set.of(id);
            }
            @Override
            public int highlightColor() { return 0x00FF55; }
        };
    }

    // ── Tiger passive ─────────────────────────────────────────────────────────
    public static class TigerPassives {
        private static final int   RADIUS           = 32;
        public  static final float PREDATOR_THRESHOLD = 0.30f;

        /** Révèle en ESP (rouge, à travers les murs) toute entité blessée (≤ 30 % HP) dans 32 blocs. */
        public static final OWPassive PREDATOR_SENSE = new OWPassive() {
            @Override
            public Set<Integer> getHighlightEntityIds(OWEntity entity, Level level) {
                Vec3 pos = entity.position();
                AABB range = new AABB(
                        pos.x - RADIUS, pos.y - RADIUS, pos.z - RADIUS,
                        pos.x + RADIUS, pos.y + RADIUS, pos.z + RADIUS);
                return level.getEntitiesOfClass(LivingEntity.class, range, e ->
                        e != entity
                        && !e.isDeadOrDying()
                        && e.getMaxHealth() > 0
                        && e.getHealth() / e.getMaxHealth() <= PREDATOR_THRESHOLD
                ).stream().map(Entity::getId).collect(Collectors.toSet());
            }

            @Override
            public int highlightColor() { return 0xFF2020; }
        };
    }
}
