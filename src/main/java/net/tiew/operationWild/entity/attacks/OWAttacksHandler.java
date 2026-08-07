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
import net.tiew.operationWild.entity.animals.terrestrial.BoaEntity;
import net.tiew.operationWild.entity.animals.terrestrial.ElephantEntity;
import net.tiew.operationWild.entity.animals.terrestrial.KangarooEntity;
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
    private static final Map<Class<? extends OWEntity>, Integer> ENTITY_TEXTURE_COLUMNS = new HashMap<>();
    private static final Map<Class<? extends OWEntity>, Integer> ENTITY_COMBO_MAX_TIMERS = new HashMap<>();


    public static final int ATTACK_JUMP_ID = 1;
    public static final int SHADOW_STRIKE_ID = 2;
    public static final int MOUTH_SLAM_ID = 3;
    public static final int PRIMAL_DIVE_ID = 4;
    public static final int PAW_SLAM_ID = 5;
    public static final int NAP_ULTIMATE_ID = 6;
    public static final int TIDAL_RUSH_ID = 7;
    public static final int BIG_MOUTH_ID = 8;
    public static final int VENOM_FANGS_ID = 9;
    public static final int CONSTRICT_ULTIMATE_ID = 10;
    public static final int WHIRLWIND_FISTS_ID = 11;
    public static final int TELLURIC_STOMP_ID = 12;
    public static final int SHOULDER_BASH_ID = 13;
    public static final int EARTHQUAKE_ID = 14;


    public static void register(Class<? extends OWEntity> entityClass, OWAttack attack) {
        ENTITY_ATTACKS.computeIfAbsent(entityClass, k -> new ArrayList<>()).add(attack);
        OWAttackPacket.register(attack);
    }

    public static void registerEntityRow(Class<? extends OWEntity> entityClass, int row) {
        ENTITY_TEXTURE_ROWS.put(entityClass, row);
    }

    /**
     * Décalage horizontal (en nombre de cartes) de la première carte de l'entité dans la texture.
     * 0 = combo en colonne 0 (cas par défaut). Pour le Boa : 3 → combo en colonne 60px.
     */
    public static void registerEntityColumn(Class<? extends OWEntity> entityClass, int column) {
        ENTITY_TEXTURE_COLUMNS.put(entityClass, column);
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

        // Kangaroo : combo + attaque secondaire « Tornade de Poings » (carte en ligne 5 → texY 200/220).
        registerEntityRow(KangarooEntity.class, 5);
        registerComboMaxTimer(KangarooEntity.class, 12);
        register(KangarooEntity.class, KangarooAttacks.WHIRLWIND_FISTS);
        register(KangarooEntity.class, KangarooAttacks.TELLURIC_STOMP);

        registerEntityRow(CrocodileEntity.class, 2);
        registerComboMaxTimer(CrocodileEntity.class, 17);
        register(CrocodileEntity.class, CrocodileAttacks.MOUTH_SLAM);
        register(CrocodileEntity.class, CrocodileAttacks.PRIMAL_DIVE);
        registerPassive(CrocodileEntity.class, CrocodilePassives.PRIMAL_DIVE_SENSE);

        registerEntityRow(OrcaEntity.class, 4);
        registerComboMaxTimer(OrcaEntity.class, 24);
        register(OrcaEntity.class, OrcaAttacks.TIDAL_RUSH);
        register(OrcaEntity.class, OrcaAttacks.BIG_MOUTH);

        // Boa : cartes dessinées en colonnes 60/80/100 de la ligne Y=120 → offset de 3 cartes.
        // Combo identique au Tigre (createCombo(16, 10, …)) → même comboMaxTimer d'affichage : 12.
        registerEntityRow(BoaEntity.class, 3);
        registerEntityColumn(BoaEntity.class, 3);
        registerComboMaxTimer(BoaEntity.class, 12);
        register(BoaEntity.class, BoaAttacks.VENOM_FANGS);
        register(BoaEntity.class, BoaAttacks.CONSTRICT_ULTIMATE);
        registerPassive(BoaEntity.class, BoaPassives.ULTIMATE_TARGET_SENSE);

        // Éléphant : cartes dessinées en colonnes 60/80/100 de la ligne Y=160 → même rangée que
        // l'orque, dont les trois cartes occupent les colonnes 0 à 2, avec un décalage de 3 cartes.
        registerEntityRow(ElephantEntity.class, 4);
        registerEntityColumn(ElephantEntity.class, 3);
        registerComboMaxTimer(ElephantEntity.class, 27);
        register(ElephantEntity.class, ElephantAttacks.SHOULDER_BASH);
        register(ElephantEntity.class, ElephantAttacks.EARTHQUAKE);
    }

    public static List<OWAttack> getAttacks(Class<?> entityClass) {
        return ENTITY_ATTACKS.get(entityClass);
    }

    public static int getEntityRow(Class<?> entityClass) {
        return ENTITY_TEXTURE_ROWS.getOrDefault(entityClass, 0);
    }

    public static int getEntityColumn(Class<?> entityClass) {
        return ENTITY_TEXTURE_COLUMNS.getOrDefault(entityClass, 0);
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

    public static class KangarooAttacks {

        /**
         * Tornade de Poings — attaque secondaire MAINTENUE (clic droit / OW_ATTACK_0).
         * Maintenir le clic droit fait tourner les poings du kangourou de plus en plus vite
         * (pic à 3 s) ; il ne peut plus bouger et inflige des dégâts de plus en plus rapides
         * aux entités à ≤ 2 blocs (jusqu'à 1 dmg/tick). 15 s max ; un cooldown de 35 s ne se
         * déclenche que si l'attaque a duré ≥ 3 s consécutives.
         *
         * <p>Comme le toggle du Boa, toute la machine d'état (rotation / cooldown) vit sur l'entité
         * via des données synchronisées. L'input maintenu (press/release) est géré spécifiquement
         * dans {@link OWAttackLogic#onMouseInput} ; on n'utilise donc PAS le cooldownTicks de l'OWAttack.
         */
        public static final OWAttack WHIRLWIND_FISTS = new OWAttack(
                WHIRLWIND_FISTS_ID,
                OW_ATTACK_0,
                OWAttacksConstants.Kangaroo.WHIRLWIND_ENERGY,
                entity -> { },
                0
        );

        /**
         * Pilon Tellurique — ultime (touche X). Se charge avec 5 kills et exige que le kangourou
         * soit à au moins 5 blocs du sol (sinon carte grisée). Une fois déclenché : suspension en
         * l'air (~10 ticks) puis plongeon vertical fulgurant ; à l'impact, onde de choc dans un rayon
         * de 5 blocs (dégâts dégressifs du centre vers le bord) + Lenteur I (10 s) + léger envol.
         */
        public static final OWAttack TELLURIC_STOMP = new OWAttack(
                TELLURIC_STOMP_ID,
                OW_ATTACK_1,
                OWAttacksConstants.Kangaroo.TELLURIC_STOMP_ENERGY,
                entity -> ((KangarooEntity) entity).activateTelluricStomp(),
                OWAttacksConstants.Kangaroo.TELLURIC_STOMP_COOLDOWN_TICKS
        ).withUnlockCondition(
                entity -> entity instanceof KangarooEntity kangaroo
                        && kangaroo.getUltimateKillCount() >= OWAttacksConstants.Kangaroo.TELLURIC_STOMP_KILLS_REQUIRED
        ).withUnlockProgress(
                entity -> entity instanceof KangarooEntity kangaroo
                        ? (float) kangaroo.getUltimateKillCount() / OWAttacksConstants.Kangaroo.TELLURIC_STOMP_KILLS_REQUIRED
                        : 0f
        );
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
                entity -> { },
                entity -> { },
                (entity, factor) -> ((OrcaEntity) entity).performOrcaDash(),
                (entity, factor, dir) -> {
                    // Élan en trois dimensions, et non plus aplati sur l'horizontale. C'est la
                    // poussée que ressent réellement le joueur : sur une monture, c'est son client
                    // qui fait autorité sur la position, donc une impulsion à plat ici annulait le
                    // relief calculé côté serveur. La visée est lue via getDashAimDirection(), le
                    // tangage propre de l'orque étant remis à zéro à chaque tick sous l'eau.
                    if (entity instanceof OrcaEntity orca) {
                        entity.setDeltaMovement(orca.getDashAimDirection()
                                .scale(OWAttacksConstants.Orca.TIDAL_RUSH_SPEED));
                        entity.hasImpulse = true;
                        orca.setDashing(true);
                    }
                },
                false,
                false
        ).withCanUsePredicate(entity -> entity.isInWater());

        /**
         * Grande Gueule — ultime (touche X). Cinq victimes pour l'armer. L'orque happe
         * l'entite qui se presente devant son museau et l'emporte dans sa gueule : une proie y est
         * broyee et privee d'air pendant la descente, un allie y voyage sans une egratignure ni le
         * moindre souci de respiration. Un second appui la recrache avant terme.
         */
        public static final OWAttack BIG_MOUTH = new OWAttack(
                BIG_MOUTH_ID,
                OW_ATTACK_1,
                OWAttacksConstants.Orca.BIG_MOUTH_ENERGY,
                entity -> ((OrcaEntity) entity).activateBigMouth(),
                OWAttacksConstants.Orca.BIG_MOUTH_COOLDOWN_TICKS
        ).withUnlockCondition(
                entity -> entity instanceof OrcaEntity orca
                        && orca.getOrcaUltimateKillCount() >= OWAttacksConstants.Orca.BIG_MOUTH_KILLS_REQUIRED
        ).withUnlockProgress(
                entity -> entity instanceof OrcaEntity orca
                        ? (float) orca.getOrcaUltimateKillCount() / OWAttacksConstants.Orca.BIG_MOUTH_KILLS_REQUIRED
                        : 0f
        ).withUltimateDuration(OWAttacksConstants.Orca.BIG_MOUTH_DURATION_MS);
    }

    public static class BoaAttacks {

        /**
         * Crochets Venimeux — attaque secondaire en toggle (clic droit / OW_ATTACK_0).
         * Un clic l'arme (carte qui respire), un autre la désarme. Le prochain coup de combo
         * qui touche injecte VENOM I (30–60 s) puis déclenche un cooldown de 1 min 30.
         * Toute la machine d'état (armé / cooldown) vit sur l'entité via des données synchronisées,
         * donc le cooldownTicks de l'OWAttack reste à 0 (pas géré par la map client d'OWAttackLogic).
         */
        public static final OWAttack VENOM_FANGS = new OWAttack(
                VENOM_FANGS_ID,
                OW_ATTACK_0,
                OWAttacksConstants.Boa.VENOM_FANGS_ENERGY,
                entity -> ((BoaEntity) entity).toggleVenomFangs(),
                0
        );

        /**
         * Étreinte Fatale — ultime (touche X). 5 kills pour charger. 1er X : ciblage (point vert sur
         * les entités grabables à ≤3 blocs) ; 2e X : la cible visée est enroulée et le rider descend.
         */
        public static final OWAttack CONSTRICT_ULTIMATE = new OWAttack(
                CONSTRICT_ULTIMATE_ID,
                OW_ATTACK_1,
                OWAttacksConstants.Boa.CONSTRICT_ULT_ENERGY,
                entity -> ((BoaEntity) entity).activateConstrictUltimate(),
                OWAttacksConstants.Boa.CONSTRICT_ULT_COOLDOWN_TICKS
        ).withUnlockCondition(
                entity -> entity instanceof BoaEntity boa
                        && boa.getUltimateKillCount() >= OWAttacksConstants.Boa.CONSTRICT_ULT_KILLS_REQUIRED
        ).withUnlockProgress(
                entity -> entity instanceof BoaEntity boa
                        ? (float) boa.getUltimateKillCount() / OWAttacksConstants.Boa.CONSTRICT_ULT_KILLS_REQUIRED
                        : 0f
        ).withUltimateDuration(OWAttacksConstants.Boa.CONSTRICT_ULT_TARGETING_MS);
    }

    public static class ElephantAttacks {

        /**
         * Coup d'Épaule — attaque secondaire instantanée (clic droit / OW_ATTACK_0).
         *
         * <p>Déclarée en {@link OWChargedAttack} avec une fenêtre de charge de 0 à 50 ms, comme la
         * Ruée Tidale de l'orque : un clic suffit et l'attaque part aussitôt, mais on récupère le
         * {@code localEffect}, seul endroit d'où l'on ait le droit de pousser une monture pilotée.
         * Un {@link OWAttack} simple n'a pas ce point d'entrée, et la poussée serait alors appliquée
         * deux fois.</p>
         *
         * <p>Le côté du déport se recalcule des deux côtés du réseau au lieu d'être synchronisé :
         * client et serveur lisent les mêmes angles au même instant, là où une donnée synchronisée
         * arriverait un tick trop tard et enverrait l'éléphant du mauvais côté.</p>
         */
        public static final OWChargedAttack SHOULDER_BASH = new OWChargedAttack(
                SHOULDER_BASH_ID,
                OW_ATTACK_0,
                OWAttacksConstants.Elephant.SHOULDER_BASH_ENERGY,
                OWAttacksConstants.Elephant.SHOULDER_BASH_COOLDOWN_TICKS,
                0L,
                50L,
                entity -> { },
                entity -> ((ElephantEntity) entity).cancelShoulderBash(),
                (entity, factor) -> ((ElephantEntity) entity).performShoulderBash(),
                (entity, factor, dir) -> {
                    if (!(entity instanceof ElephantEntity elephant)) return;

                    LivingEntity rider = elephant.getControllingPassenger();
                    int side = ElephantEntity.computeBashSide(
                            rider != null ? rider.getYRot() : elephant.getYRot(), elephant.yBodyRot);

                    Vec3 push = elephant.getBashDirection(side)
                            .scale(OWAttacksConstants.Elephant.SHOULDER_BASH_SIDE_POWER);

                    elephant.setDeltaMovement(push.x, OWAttacksConstants.Elephant.SHOULDER_BASH_LIFT, push.z);
                    elephant.hasImpulse = true;
                },
                false,
                false
        );

        /**
         * Tremblement de Terre — ultime (touche X). Cinq victimes pour l'armer. L'éléphant se cabre
         * pendant trois secondes, retombe de tout son poids, et le sol se disloque : dégâts
         * dégressifs sur douze blocs, envol, Lenteur, cratère et caméras qui tremblent une seconde
         * de plus. Le tick d'impact est calé sur l'image de contact de l'animation, pas sur sa fin.
         */
        public static final OWAttack EARTHQUAKE = new OWAttack(
                EARTHQUAKE_ID,
                OW_ATTACK_1,
                OWAttacksConstants.Elephant.EARTHQUAKE_ENERGY,
                entity -> ((ElephantEntity) entity).activateEarthquake(),
                OWAttacksConstants.Elephant.EARTHQUAKE_COOLDOWN_TICKS
        ).withUnlockCondition(
                entity -> entity instanceof ElephantEntity elephant
                        && elephant.getUltimateKillCount() >= OWAttacksConstants.Elephant.EARTHQUAKE_KILLS_REQUIRED
        ).withUnlockProgress(
                entity -> entity instanceof ElephantEntity elephant
                        ? (float) elephant.getUltimateKillCount() / OWAttacksConstants.Elephant.EARTHQUAKE_KILLS_REQUIRED
                        : 0f
        ).withUltimateDuration(OWAttacksConstants.Elephant.EARTHQUAKE_DURATION_MS);
    }

    public static class BoaPassives {
        /** Point vert sur les entités grabables à ≤3 blocs, uniquement pendant le ciblage de l'ultime. */
        public static final OWPassive ULTIMATE_TARGET_SENSE = new OWPassive() {
            @Override
            public Set<Integer> getHighlightEntityIds(OWEntity entity, Level level) {
                if (!OWAttackLogic.isBoaTargeting) return Set.of();
                if (!(entity instanceof BoaEntity boa)) return Set.of();
                double r = OWAttacksConstants.Boa.CONSTRICT_ULT_RANGE;
                AABB box = boa.getBoundingBox().inflate(r);
                return level.getEntitiesOfClass(LivingEntity.class, box,
                                e -> boa.canConstrict(e) && boa.distanceToSqr(e) <= r * r)
                        .stream().map(Entity::getId).collect(Collectors.toSet());
            }

            @Override
            public int highlightColor() {
                return 0x00FF55;
            }
        };
    }

    public static class CrocodilePassives {
        /**
         * Marque TOUTES les proies recevables pendant la désignation, et non la seule cible déjà
         * verrouillée. Sans elles, l'écran restait vide tant qu'aucune n'était acquise : le joueur
         * n'avait aucun moyen de savoir vers quoi tourner le regard. Le réticule distingue ensuite
         * les candidates (marques d'angle ternes) de la sélection (cadre complet et chevron).
         */
        public static final OWPassive PRIMAL_DIVE_SENSE = new OWPassive() {
            @Override
            public Set<Integer> getHighlightEntityIds(OWEntity entity, Level level) {
                if (!OWAttackLogic.isCrocTargeting) return Set.of();
                if (!(entity instanceof CrocodileEntity croc)) return Set.of();
                double r = OWAttacksConstants.Crocodile.PRIMAL_DIVE_TARGET_RANGE;
                AABB box = croc.getBoundingBox().inflate(r);
                return level.getEntitiesOfClass(LivingEntity.class, box,
                                e -> croc.canPrimalDiveTarget(e) && croc.distanceToSqr(e) <= r * r)
                        .stream().map(Entity::getId).collect(Collectors.toSet());
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
