package net.tiew.operationWild.entity.attacks;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.settings.KeyConflictContext;
import net.tiew.operationWild.core.OWKeysBinding;
import net.tiew.operationWild.entity.OWEntity;
import net.tiew.operationWild.entity.animals.aquatic.CrocodileEntity;
import net.tiew.operationWild.entity.animals.aquatic.OrcaEntity;
import net.tiew.operationWild.entity.animals.terrestrial.KodiakEntity;
import net.tiew.operationWild.entity.animals.terrestrial.TigerEntity;
import net.tiew.operationWild.networking.packets.to_server.OWAttackPacket;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class OWAttacksHandler {

    public static final String OW_CATEGORY = "key.categories.operationwild";

    public static final KeyMapping OW_ATTACK_0 = OWKeysBinding.OW_ATTACK_0;
    public static final KeyMapping OW_ATTACK_1 = OWKeysBinding.OW_ATTACK_1;


    private static final Map<Class<? extends OWEntity>, List<OWAttack>> ENTITY_ATTACKS = new HashMap<>();
    private static final Map<Class<? extends OWEntity>, OWPassive> ENTITY_PASSIVES = new HashMap<>();
    private static final Map<Class<? extends OWEntity>, Integer> ENTITY_TEXTURE_ROWS = new HashMap<>();
    private static final Map<Class<? extends OWEntity>, Integer> ENTITY_COMBO_MAX_TIMERS = new HashMap<>();


    public static final int ATTACK_JUMP_ID = 1;
    public static final int SHADOW_STRIKE_ID = 2;
    public static final int MOUTH_SLAM_ID = 3;
    public static final int PRIMAL_DIVE_ID = 4;
    public static final int PAW_SLAM_ID = 5;
    public static final int NAP_ULTIMATE_ID = 6;
    public static final int TIDAL_RUSH_ID = 7;
    public static final int ORCA_CALL_ID = 8;


    public static void register(Class<? extends OWEntity> entityClass, OWAttack attack) {
        ENTITY_ATTACKS.computeIfAbsent(entityClass, k -> new ArrayList<>()).add(attack);
        OWAttackPacket.register(attack);
    }

    public static void registerEntityRow(Class<? extends OWEntity> entityClass, int row) {
        ENTITY_TEXTURE_ROWS.put(entityClass, row);
    }

    public static void registerComboMaxTimer(Class<? extends OWEntity> entityClass, int timeMax) {
        ENTITY_COMBO_MAX_TIMERS.put(entityClass, timeMax);
    }

    public static void registerPassive(Class<? extends OWEntity> entityClass, OWPassive passive) {
        ENTITY_PASSIVES.put(entityClass, passive);
    }

    public static OWPassive getPassive(Class<?> entityClass) {
        return ENTITY_PASSIVES.get(entityClass);
    }

    public static void registerAll() {
        registerEntityRow(TigerEntity.class, 0);
        registerComboMaxTimer(TigerEntity.class, 12);
        register(TigerEntity.class, TigerAttacks.JUMP_ATTACK);
        register(TigerEntity.class, TigerAttacks.SHADOW_STRIKE);
        registerPassive(TigerEntity.class, TigerPassives.PREDATOR_SENSE);

        registerEntityRow(KodiakEntity.class, 1);
        registerComboMaxTimer(KodiakEntity.class, 14);
        register(KodiakEntity.class, KodiakAttacks.PAW_SLAM);
        register(KodiakEntity.class, KodiakAttacks.NAP_ULTIMATE);
        registerPassive(KodiakEntity.class, KodiakPassives.BUTCHER_INSTINCT);

        registerEntityRow(CrocodileEntity.class, 2);
        registerComboMaxTimer(CrocodileEntity.class, 17);
        register(CrocodileEntity.class, CrocodileAttacks.MOUTH_SLAM);
        register(CrocodileEntity.class, CrocodileAttacks.PRIMAL_DIVE);
        registerPassive(CrocodileEntity.class, CrocodilePassives.PRIMAL_DIVE_SENSE);

        registerEntityRow(OrcaEntity.class, 4);
        registerComboMaxTimer(OrcaEntity.class, 24);
        register(OrcaEntity.class, OrcaAttacks.TIDAL_RUSH);
        register(OrcaEntity.class, OrcaAttacks.ORCA_CALL);
    }

    public static List<OWAttack> getAttacks(Class<?> entityClass) {
        return ENTITY_ATTACKS.get(entityClass);
    }

    public static int getEntityRow(Class<?> entityClass) {
        return ENTITY_TEXTURE_ROWS.getOrDefault(entityClass, 0);
    }

    public static int getComboMaxTimer(Class<?> entityClass) {
        return ENTITY_COMBO_MAX_TIMERS.getOrDefault(entityClass, 10);
    }

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

    public static class TigerAttacks {

        public static final OWAttack SHADOW_STRIKE = new OWAttack(
                SHADOW_STRIKE_ID,
                OW_ATTACK_1,
                100f,
                entity -> ((TigerEntity) entity).activateShadowStrike(),
                OWAttacksConstants.Tiger.SHADOW_STRIKE_COOLDOWN_TICKS
        ).withUnlockCondition(
                entity -> entity instanceof TigerEntity tiger
                        && tiger.getShadowStrikeKillCount() >= OWAttacksConstants.Tiger.SHADOW_STRIKE_KILLS_REQUIRED
        ).withUnlockProgress(
                entity -> entity instanceof TigerEntity tiger
                        ? (float) tiger.getShadowStrikeKillCount() / OWAttacksConstants.Tiger.SHADOW_STRIKE_KILLS_REQUIRED
                        : 0f
        ).withUltimateDuration(OWAttacksConstants.Tiger.SHADOW_STRIKE_DURATION_MS);

        public static final OWChargedAttack JUMP_ATTACK = new OWChargedAttack(
                ATTACK_JUMP_ID,
                OW_ATTACK_0,
                100,
                OWAttacksConstants.Tiger.JUMP_ATTACK_COOLDOWN_TICKS,
                1000L,
                2000L,
                entity -> ((TigerEntity) entity).startJumpCharge(),
                entity -> ((TigerEntity) entity).cancelJumpCharge(),
                (entity, factor) -> ((TigerEntity) entity).performJumpAttack(factor),
                (entity, factor, dir) -> {
                    Vec3 horizontal = new Vec3(dir.x, 0, dir.z);
                    if (horizontal.lengthSqr() > 1.0E-7) horizontal = horizontal.normalize();
                    float speed = ((0.55f + 0.75f * factor) * OWAttacksConstants.Tiger.JUMP_POWER) * 2;
                    float vert = (float) (((0.30f + 0.12f * factor) * OWAttacksConstants.Tiger.JUMP_POWER) / 1.25);
                    entity.setDeltaMovement(horizontal.x * speed, vert, horizontal.z * speed);
                    entity.hasImpulse = true;
                    if (entity instanceof TigerEntity tiger) {
                        tiger.isLeaping = true;
                        tiger.isPreparing = false;
                        tiger.setPlayerLeaping(true);
                        tiger.playJumpSound();
                        tiger.playJumpParticles();
                    }
                },
                true,
                true
        );
    }

    public static class KodiakAttacks {

        public static final OWChargedAttack PAW_SLAM = new OWChargedAttack(
                PAW_SLAM_ID,
                OW_ATTACK_0,
                100f,
                OWAttacksConstants.Kodiak.PAW_SLAM_COOLDOWN_TICKS,
                1000L,
                3000L,
                entity -> ((KodiakEntity) entity).startPawSlamCharge(),
                entity -> ((KodiakEntity) entity).cancelPawSlamCharge(),
                (entity, factor) -> ((KodiakEntity) entity).performPawSlam(factor),
                (entity, factor, dir) -> {
                },
                false,
                true
        );

        public static final OWAttack NAP_ULTIMATE = new OWAttack(
                NAP_ULTIMATE_ID,
                OW_ATTACK_1,
                100f,
                entity -> ((KodiakEntity) entity).activateUltimateNap(),
                OWAttacksConstants.Kodiak.NAP_COOLDOWN_TICKS
        ).withUnlockCondition(
                entity -> entity instanceof KodiakEntity kodiak
                        && kodiak.getNapKillCount() >= OWAttacksConstants.Kodiak.NAP_KILLS_REQUIRED
        ).withUnlockProgress(
                entity -> entity instanceof KodiakEntity kodiak
                        ? (float) kodiak.getNapKillCount() / OWAttacksConstants.Kodiak.NAP_KILLS_REQUIRED
                        : 0f
        ).withUltimateDuration(OWAttacksConstants.Kodiak.NAP_DURATION_MS);
    }

    public static class CrocodileAttacks {

        public static final OWChargedAttack MOUTH_SLAM = new OWChargedAttack(
                MOUTH_SLAM_ID,
                OW_ATTACK_0,
                100f,
                OWAttacksConstants.Crocodile.MOUTH_SLAM_COOLDOWN_TICKS,
                1000L,
                3000L,
                entity -> ((CrocodileEntity) entity).startMouthSlamCharge(),
                entity -> ((CrocodileEntity) entity).cancelMouthSlamCharge(),
                (entity, factor) -> ((CrocodileEntity) entity).performMouthSlam(factor),
                (entity, factor, dir) -> {
                },
                false,
                true
        ).withCanUsePredicate(entity -> !entity.isInWater());

        public static final OWAttack PRIMAL_DIVE = new OWAttack(
                PRIMAL_DIVE_ID,
                OW_ATTACK_1,
                100f,
                entity -> ((net.tiew.operationWild.entity.animals.aquatic.CrocodileEntity) entity).activatePrimalDive(),
                OWAttacksConstants.Crocodile.PRIMAL_DIVE_COOLDOWN_TICKS
        ).withUnlockCondition(
                entity -> entity instanceof net.tiew.operationWild.entity.animals.aquatic.CrocodileEntity croc
                        && croc.getUltimateKillCount() >= OWAttacksConstants.Crocodile.PRIMAL_DIVE_KILLS_REQUIRED
        ).withUnlockProgress(
                entity -> entity instanceof net.tiew.operationWild.entity.animals.aquatic.CrocodileEntity croc
                        ? (float) croc.getUltimateKillCount() / OWAttacksConstants.Crocodile.PRIMAL_DIVE_KILLS_REQUIRED
                        : 0f
        ).withUltimateDuration(OWAttacksConstants.Crocodile.PRIMAL_DIVE_TARGETING_MS);
    }

    public static class OrcaAttacks {

        public static final OWChargedAttack TIDAL_RUSH = new OWChargedAttack(
                TIDAL_RUSH_ID,
                OW_ATTACK_0,
                100f,
                OWAttacksConstants.Orca.TIDAL_RUSH_COOLDOWN_TICKS,
                0L,
                50L,
                entity -> {
                },
                entity -> {
                },
                (entity, factor) -> ((OrcaEntity) entity).performOrcaDash(),
                (entity, factor, dir) -> {
                    Vec3 forward = Vec3.directionFromRotation(0, entity.getYRot());
                    entity.setDeltaMovement(forward.x * OWAttacksConstants.Orca.TIDAL_RUSH_SPEED, 0, forward.z * OWAttacksConstants.Orca.TIDAL_RUSH_SPEED);
                    entity.hasImpulse = true;
                    if (entity instanceof OrcaEntity orca) orca.setDashing(true);
                },
                false,
                false
        );

        public static final OWAttack ORCA_CALL = new OWAttack(
                ORCA_CALL_ID,
                OW_ATTACK_1,
                100f,
                entity -> ((OrcaEntity) entity).activateOrcaCall(),
                OWAttacksConstants.Orca.ORCA_CALL_COOLDOWN_TICKS
        ).withUnlockCondition(
                entity -> entity instanceof OrcaEntity orca
                        && orca.getOrcaUltimateKillCount() >= OWAttacksConstants.Orca.ORCA_CALL_KILLS_REQUIRED
        ).withUnlockProgress(
                entity -> entity instanceof OrcaEntity orca
                        ? (float) orca.getOrcaUltimateKillCount() / OWAttacksConstants.Orca.ORCA_CALL_KILLS_REQUIRED
                        : 0f
        ).withUltimateDuration(OWAttacksConstants.Orca.ORCA_CALL_DURATION_MS);
    }

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
            public int highlightColor() {
                return 0x00FF55;
            }
        };
    }

    public static class TigerPassives {

        public static final OWPassive PREDATOR_SENSE = new OWPassive() {
            @Override
            public Set<Integer> getHighlightEntityIds(OWEntity entity, Level level) {
                Vec3 pos = entity.position();
                AABB range = new AABB(
                        pos.x - OWAttacksConstants.Tiger.PREDATOR_RADIUS, pos.y - OWAttacksConstants.Tiger.PREDATOR_RADIUS, pos.z - OWAttacksConstants.Tiger.PREDATOR_RADIUS,
                        pos.x + OWAttacksConstants.Tiger.PREDATOR_RADIUS, pos.y + OWAttacksConstants.Tiger.PREDATOR_RADIUS, pos.z + OWAttacksConstants.Tiger.PREDATOR_RADIUS);
                return level.getEntitiesOfClass(LivingEntity.class, range, e ->
                        e != entity
                                && !e.isDeadOrDying()
                                && e.getMaxHealth() > 0
                                && e.getHealth() / e.getMaxHealth() <= OWAttacksConstants.Tiger.PREDATOR_THRESHOLD
                ).stream().map(Entity::getId).collect(Collectors.toSet());
            }

            @Override
            public int highlightColor() {
                return 0xFF2020;
            }
        };
    }

    public static class KodiakPassives {

        public static final float BUTCHER_INSTINCT_MULTIPLIER = 1.5f;

        public static final OWPassive BUTCHER_INSTINCT = new OWPassive() {
            @Override
            public Set<Integer> getHighlightEntityIds(OWEntity entity, Level level) {
                return Set.of();
            }

            @Override
            public int highlightColor() {
                return 0xFF8C00;
            }
        };
    }
}
