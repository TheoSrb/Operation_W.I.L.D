package net.tiew.operationWild.entity.resurrection;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.tiew.operationWild.component.OWDataComponentTypes;
import net.tiew.operationWild.component.SoulData;
import net.tiew.operationWild.entity.OWEntity;
import net.tiew.operationWild.entity.OWEntityRegistry;
import net.tiew.operationWild.entity.misc.SoulGhostEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * État d'un Rituel de Communion en cours : défense par vagues d'un fantôme spectral
 * jusqu'à sa rematérialisation complète. Piloté tick par tick par le
 * {@link ResurrectionRitualManager}.
 */
public class ResurrectionRitual {

    private enum Phase { INTRO, SPAWN, FIGHT, RESPITE, CLIMAX, END, DONE, FAILED }

    // ===================== RÉGLAGES AJUSTABLES =====================
    /** Durée de l'intro (ticks) avant la 1ère vague — calée sur la musique d'intro (~13 s). */
    public static int INTRO_TICKS = 260;
    /** Durée de l'outro (ticks) après le succès — calée sur la musique de fin (~24 s). */
    public static int END_TICKS = 480;
    /** Stabilité maximale de l'âme (= PV du fantôme). */
    public static float MAX_STABILITY = 100f;
    /** Durée du répit entre deux vagues (ticks). */
    public static int RESPITE_TICKS = 100;
    /** Stabilité regagnée par tick pendant le répit. */
    public static float STABILITY_RESPITE_HEAL = 0.5f;
    /** Distance max joueur↔fantôme avant interruption du rituel. */
    public static double MAX_PLAYER_DISTANCE = 72.0;
    /** Vitesse de lissage de la jauge de matérialisation. */
    public static float MAT_LERP = 0.015f;

    /** Distance minimale de spawn des monstres autour du fantôme. */
    public static double MIN_SPAWN_DISTANCE = 10.0;
    /** Distance maximale de spawn des monstres autour du fantôme. */
    public static double MAX_SPAWN_DISTANCE = 22.0;
    /** Nombre de monstres apparaissant par salve (spawn échelonné, pas tous d'un coup). */
    public static int SPAWN_BATCH = 2;
    /** Intervalle (ticks) entre deux salves d'apparition. */
    public static int SPAWN_INTERVAL_TICKS = 25;
    /** Intervalle (ticks) entre deux "respirations" qui font briller les monstres (repérage). */
    public static int GLOW_INTERVAL_TICKS = 300;
    /** Durée (ticks) du halo Glowing à chaque respiration. */
    public static int GLOW_DURATION_TICKS = 60;
    /** Proportion de monstres qui chassent activement le JOUEUR (le reste vise l'âme). 0 = aucun, 1 = tous. */
    public static float PLAYER_HUNTER_RATIO = 0.3f;
    // ==============================================================

    private final ServerLevel level;
    private final UUID playerUuid;
    private final SoulData soulData;
    private final Vec3 anchor;
    private final int totalWaves;
    private final int companionTier;

    private SoulGhostEntity ghost;
    private Phase phase = Phase.INTRO;
    private int currentWave = 0;
    private int respiteTimer = 0;
    private int introTimer = INTRO_TICKS;
    private int endTimer = END_TICKS;
    private float materialization = 0f;

    private final List<UUID> waveMobs = new ArrayList<>();
    private final List<EntityType<?>> pendingSpawns = new ArrayList<>();
    private int spawnCooldown = 0;

    public ResurrectionRitual(ServerLevel level, ServerPlayer player, SoulData soulData, Vec3 anchor) {
        this.level = level;
        this.playerUuid = player.getUUID();
        this.soulData = soulData;
        this.anchor = anchor;
        this.totalWaves = soulData.waveCount();
        this.companionTier = ResurrectionWaveRoster.powerTier(soulData.entityType());
    }

    public UUID getPlayerUuid() { return playerUuid; }
    public boolean isFinished() { return phase == Phase.DONE || phase == Phase.FAILED; }

    /** Annule le rituel sans message (ex : déconnexion du lanceur) : dissipe le fantôme et les monstres. */
    public void abort() { fail(null); }

    /** Vrai tant que le rituel "protège" sa zone (combat en cours) : restrictions de spawn/build actives.
     *  Dès le succès (outro END) ou la fin, plus aucune restriction. */
    public boolean isProtecting() {
        return phase != Phase.END && phase != Phase.DONE && phase != Phase.FAILED;
    }

    /** Vrai si le point (x,y,z) est à portée de l'âme (distance 3D). Sert au blocage des spawns naturels. */
    public boolean isNear(double x, double y, double z, double radius) {
        double dx = x - anchor.x, dy = y - anchor.y, dz = z - anchor.z;
        return dx * dx + dy * dy + dz * dz <= radius * radius;
    }

    /** Fait apparaître le fantôme spectral au début du rituel. */
    public void spawnGhost() {
        SoulGhostEntity g = OWEntityRegistry.SOUL_GHOST.get().create(level);
        if (g == null) return;
        g.moveTo(anchor.x, anchor.y, anchor.z, level.random.nextFloat() * 360f, 0f);
        g.setGhostType(soulData.entityType());
        g.setGhostVariant(soulData.variant());
        g.setGhostSkin(soulData.skinIndex());
        g.setGhostScale(soulData.scale());
        if (!soulData.ownerUuid().equals(SoulData.NO_UUID)) g.setSoulOwner(soulData.ownerUuid());
        g.setMaterialization(0f);
        g.getAttribute(Attributes.MAX_HEALTH).setBaseValue(MAX_STABILITY);
        g.setHealth(MAX_STABILITY);
        g.lockPosition(anchor.x, anchor.y, anchor.z); // âme totalement immobile
        level.addFreshEntity(g);
        this.ghost = g;
        announce(Component.translatable("ritual.ow.started", soulData.nickname().isEmpty()
                ? Component.translatable(typeTranslationKey()) : Component.literal(soulData.nickname()))
                .setStyle(Style.EMPTY.withColor(0x86DBFF)));
    }

    /** Avance le rituel d'un tick (côté serveur uniquement). */
    public void tick() {
        if (isFinished()) return;

        ServerPlayer player = (ServerPlayer) level.getServer().getPlayerList().getPlayer(playerUuid);

        // Outro (après le succès) : on laisse juste filer la musique de fin, plus aucune contrainte.
        if (phase == Phase.END) {
            if (--endTimer <= 0) phase = Phase.DONE;
            return;
        }

        // Interruptions : joueur déconnecté, mort, ou trop loin → échec (âme conservée).
        if (player == null || !player.isAlive() || player.distanceToSqr(anchor) > MAX_PLAYER_DISTANCE * MAX_PLAYER_DISTANCE) {
            fail(player);
            return;
        }
        if (ghost == null || !ghost.isAlive() || ghost.getHealth() <= 0f) {
            fail(player);
            return;
        }

        ghost.pingFromManager();

        // Lissage de la jauge de matérialisation vers la cible (vagues nettoyées / total).
        float target = (phase == Phase.CLIMAX) ? 1f : (float) currentWave / totalWaves;
        materialization = Mth.lerp(MAT_LERP, materialization, target);
        ghost.setMaterialization(materialization);

        switch (phase) {
            case INTRO -> {
                if (--introTimer <= 0) phase = Phase.SPAWN;
            }
            case SPAWN -> {
                beginWave();
                phase = Phase.FIGHT;
            }
            case FIGHT -> {
                // Apparition échelonnée des monstres de la vague.
                if (!pendingSpawns.isEmpty()) {
                    if (spawnCooldown <= 0) {
                        spawnBatch();
                        spawnCooldown = SPAWN_INTERVAL_TICKS;
                    } else {
                        spawnCooldown--;
                    }
                }
                pruneAndRetarget(player);
                if (pendingSpawns.isEmpty() && waveMobs.isEmpty()) {
                    currentWave++;
                    if (currentWave >= totalWaves) {
                        phase = Phase.CLIMAX;
                    } else {
                        respiteTimer = RESPITE_TICKS;
                        phase = Phase.RESPITE;
                        spawnAmbientParticles(8);
                    }
                }
            }
            case RESPITE -> {
                ghost.setHealth(Math.min(ghost.getMaxHealth(), ghost.getHealth() + STABILITY_RESPITE_HEAL));
                if (--respiteTimer <= 0) phase = Phase.SPAWN;
            }
            case CLIMAX -> {
                if (materialization >= 0.985f) {
                    resurrect(player);
                    endTimer = END_TICKS;
                    phase = Phase.END; // laisse jouer la musique d'outro avant de clôturer
                }
            }
            default -> {}
        }

        if (level.getGameTime() % 8 == 0) spawnAmbientParticles(3);

        // "Respiration" de repérage : toutes les ~15 s, les monstres de vague brillent ~3 s.
        if (!waveMobs.isEmpty() && level.getGameTime() % GLOW_INTERVAL_TICKS == 0) {
            applyGlowPulse();
        }
    }

    /** Applique le halo Glowing à tous les monstres de la vague en cours (repérage visuel). */
    private void applyGlowPulse() {
        for (UUID id : waveMobs) {
            if (level.getEntity(id) instanceof LivingEntity le) {
                le.addEffect(new MobEffectInstance(MobEffects.GLOWING, GLOW_DURATION_TICKS, 0, false, false));
            }
        }
    }

    /** Prépare la file d'apparition de la vague courante (sans tout faire spawn d'un coup). */
    private void beginWave() {
        pendingSpawns.clear();
        pendingSpawns.addAll(ResurrectionWaveRoster.rollWave(level.random, currentWave, totalWaves, companionTier));
        waveMobs.clear();
        spawnCooldown = 0;
        announce(Component.translatable("ritual.ow.wave", currentWave + 1, totalWaves)
                .setStyle(Style.EMPTY.withColor(0xFF6B6B)));
        level.playSound(null, anchor.x, anchor.y, anchor.z,
                SoundEvents.WARDEN_ROAR, SoundSource.HOSTILE, 0.7f, 1.2f);
    }

    /** Fait apparaître une salve de monstres de la file d'attente. */
    private void spawnBatch() {
        RandomSource rng = level.random;
        int n = Math.min(SPAWN_BATCH, pendingSpawns.size());
        for (int i = 0; i < n; i++) {
            EntityType<?> type = pendingSpawns.remove(0);
            Entity created = type.create(level);
            if (!(created instanceof Mob mob)) continue;
            // On ne fait apparaître le mob QUE si on trouve un emplacement où sa hitbox réelle
            // ne chevauche aucun bloc (sinon il resterait coincé, immobile et intouchable).
            if (!placeMobAtValidSpawn(mob, rng)) {
                continue; // pas d'emplacement libre cette fois : ce mob n'est pas invoqué
            }
            mob.finalizeSpawn(level, level.getCurrentDifficultyAt(mob.blockPosition()), MobSpawnType.EVENT, null);
            // Le rituel ne fait jamais apparaître de bébés : on force l'âge adulte après le finalizeSpawn
            // (qui peut, lui, décider d'un bébé — ex. zombie nourrisson).
            forceAdult(mob);
            level.addFreshEntity(mob);
            // Marque le monstre comme assaillant du rituel → ses changements de cible sont
            // redirigés (cf. ResurrectionRitualManager.onChangeTarget).
            mob.getPersistentData().putUUID("OWRitualGhost", ghost.getUUID());
            // Une partie des monstres chasse le joueur, le reste vise l'âme.
            mob.getPersistentData().putBoolean("OWHuntPlayer", rng.nextFloat() < PLAYER_HUNTER_RATIO);
            mob.setTarget(ghost);
            waveMobs.add(mob.getUUID());
        }
    }

    /**
     * Garantit qu'un mob invoqué par le rituel est un <b>adulte</b>, quelle que soit son espèce.
     * Couvre les animaux ({@link net.minecraft.world.entity.AgeableMob}) ainsi que les monstres à
     * variante bébé qui n'héritent pas d'AgeableMob (zombies et dérivés, piglins, hoglins).
     */
    private void forceAdult(Mob mob) {
        if (mob instanceof net.minecraft.world.entity.AgeableMob ageable) {
            ageable.setBaby(false);
        } else if (mob instanceof net.minecraft.world.entity.monster.Zombie zombie) {
            zombie.setBaby(false);
        } else if (mob instanceof net.minecraft.world.entity.monster.piglin.AbstractPiglin piglin) {
            piglin.setBaby(false);
        } else if (mob instanceof net.minecraft.world.entity.monster.hoglin.Hoglin hoglin) {
            hoglin.setBaby(false);
        }
    }

    /**
     * Positionne le mob à un endroit VALABLE : sol solide, et surtout la <b>hitbox réelle</b> du mob
     * ne chevauche aucun bloc ({@code noCollision}) — ce qui évite les mobs coincés/immobiles.
     * Renvoie false si aucun emplacement libre n'a été trouvé (le mob ne doit alors pas être invoqué).
     */
    private boolean placeMobAtValidSpawn(Mob mob, RandomSource rng) {
        for (int attempt = 0; attempt < 24; attempt++) {
            double angle = rng.nextDouble() * Math.PI * 2;
            double radius = MIN_SPAWN_DISTANCE + rng.nextDouble() * (MAX_SPAWN_DISTANCE - MIN_SPAWN_DISTANCE);
            // Les premiers essais loin ; si ça échoue souvent, on resserre progressivement vers l'ancrage.
            if (attempt >= 16) radius = 5.0 + rng.nextDouble() * 4.0;
            int ix = Mth.floor(anchor.x + Math.cos(angle) * radius);
            int iz = Mth.floor(anchor.z + Math.sin(angle) * radius);
            Integer top = findFooting(ix, iz);
            if (top == null) continue;
            mob.moveTo(ix + 0.5, top, iz + 0.5, (float) Math.toDegrees(angle), 0f);
            if (level.noCollision(mob)) {
                return true; // hitbox entièrement dégagée
            }
        }
        return false;
    }

    /**
     * Hauteur d'appui valable dans la colonne (ix,iz) : un bloc solide surmonté de 2 blocs libres
     * (sans fluide), au plus près du niveau de l'âme. {@code null} si rien de correct trouvé.
     */
    private Integer findFooting(int ix, int iz) {
        int startY = Mth.floor(anchor.y) + 8;
        for (int y = startY; y > startY - 24; y--) {
            BlockPos ground = new BlockPos(ix, y, iz);
            BlockPos feet = ground.above();
            BlockPos head = feet.above();
            boolean solidGround = !level.getBlockState(ground).getCollisionShape(level, ground).isEmpty();
            boolean feetFree = level.getBlockState(feet).getCollisionShape(level, feet).isEmpty()
                    && level.getBlockState(feet).getFluidState().isEmpty();
            boolean headFree = level.getBlockState(head).getCollisionShape(level, head).isEmpty()
                    && level.getBlockState(head).getFluidState().isEmpty();
            if (solidGround && feetFree && headFree) {
                return y + 1; // dessus du bloc solide
            }
        }
        return null;
    }

    /**
     * Nettoie les morts et FORCE le ciblage sur le fantôme — sauf si le monstre est en train
     * d'attaquer le défenseur (le joueur). Empêche les monstres de se battre entre eux.
     */
    private void pruneAndRetarget(ServerPlayer defender) {
        waveMobs.removeIf(uuid -> {
            Entity e = level.getEntity(uuid);
            return e == null || !e.isAlive();
        });
        for (UUID uuid : waveMobs) {
            if (level.getEntity(uuid) instanceof Mob mob) {
                boolean hunter = mob.getPersistentData().getBoolean("OWHuntPlayer");
                LivingEntity desired = (hunter && defender != null && defender.isAlive()) ? defender : ghost;
                LivingEntity tgt = mob.getTarget();
                // Riposte conservée pour les viseurs d'âme : s'ils ont été blessés par le joueur visé.
                boolean retaliating = !hunter && tgt instanceof net.minecraft.world.entity.player.Player
                        && mob.getLastHurtByMob() == tgt;
                if (tgt == null || (tgt != desired && !retaliating)) {
                    mob.setTarget(desired);
                }
            }
        }
    }

    private void resurrect(ServerPlayer player) {
        EntityType<?> type = soulData.entityType() == null ? null
                : net.minecraft.core.registries.BuiltInRegistries.ENTITY_TYPE.get(soulData.entityType());
        if (type == null) { fail(player); return; }
        Entity created = type.create(level);
        if (!(created instanceof OWEntity owEntity)) { fail(player); return; }

        // Ré-ancre avec le MÊME uuid → skins débloqués & progression de quêtes cosmétiques (indexés
        // par uuid d'entité) restaurés automatiquement.
        if (!soulData.originalUuid().equals(SoulData.NO_UUID)) {
            owEntity.setUUID(soulData.originalUuid());
        }
        owEntity.moveTo(ghost.getX(), ghost.getY(), ghost.getZ(), ghost.getYRot(), 0f);
        if (!soulData.ownerUuid().equals(SoulData.NO_UUID)) {
            owEntity.setOwnerUUID(soulData.ownerUuid());
        }
        owEntity.skipNameSelection = true; // nom déjà restauré → pas d'écran de nommage
        owEntity.setTame(true, player);
        owEntity.restoreFromSoul(soulData);

        // Pas de phase de fragilité : le compagnon revient immédiatement pleinement vivant.
        owEntity.setResurrection(false);
        owEntity.setHealth(owEntity.getMaxHealth());

        level.addFreshEntity(owEntity);

        player.giveExperienceLevels(-soulData.xpLevelCost());
        ItemStack amulet = player.getItemBySlot(net.minecraft.world.entity.EquipmentSlot.CHEST);
        if (!amulet.isEmpty() && amulet.isDamageableItem()) {
            amulet.hurtAndBreak(8, player, net.minecraft.world.entity.EquipmentSlot.CHEST);
        }
        consumeSoulStack(player);

        ghost.setMaterialization(1f);
        spawnAmbientParticles(60);
        level.sendParticles(ParticleTypes.FLASH, ghost.getX(), ghost.getY() + 0.5, ghost.getZ(), 2, 0, 0, 0, 0);
        level.playSound(null, ghost.getX(), ghost.getY(), ghost.getZ(),
                SoundEvents.CONDUIT_DEACTIVATE, SoundSource.PLAYERS, 1.4f, 1.0f);
        level.playSound(null, ghost.getX(), ghost.getY(), ghost.getZ(),
                SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 1.0f, 1.2f);
        announce(Component.translatable("ritual.ow.success", owEntity.getName().getString())
                .setStyle(Style.EMPTY.withBold(true).withColor(0x86DBFF)));

        ghost.discard();
    }

    private void fail(ServerPlayer player) {
        phase = Phase.FAILED;
        if (ghost != null && ghost.isAlive()) {
            level.playSound(null, ghost.getX(), ghost.getY(), ghost.getZ(),
                    SoundEvents.SOUL_ESCAPE, SoundSource.PLAYERS, 1.0f, 0.6f);
            level.sendParticles(ParticleTypes.SMOKE, ghost.getX(), ghost.getY() + 0.5, ghost.getZ(),
                    30, 0.4, 0.6, 0.4, 0.02);
            ghost.discard();
        }
        for (UUID uuid : waveMobs) {
            if (level.getEntity(uuid) instanceof Mob mob) mob.discard();
        }
        if (player != null) {
            player.sendSystemMessage(Component.translatable("ritual.ow.failed")
                    .setStyle(Style.EMPTY.withBold(true).withColor(0xD94747)));
        }
    }

    /** Consomme un exemplaire de l'Âme correspondant à ce compagnon dans l'inventaire du joueur. */
    private void consumeSoulStack(ServerPlayer player) {
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            SoulData data = stack.get(OWDataComponentTypes.SOUL_DATA.get());
            if (data != null && data.originalUuid().equals(soulData.originalUuid())) {
                stack.shrink(1);
                return;
            }
        }
    }

    private void spawnAmbientParticles(int count) {
        if (ghost == null) return;
        level.sendParticles(ParticleTypes.SOUL, ghost.getX(), ghost.getY() + ghost.getBbHeight() * 0.5,
                ghost.getZ(), count, ghost.getBbWidth(), ghost.getBbHeight() * 0.6, ghost.getBbWidth(), 0.01);
    }

    private void announce(Component message) {
        ServerPlayer player = (ServerPlayer) level.getServer().getPlayerList().getPlayer(playerUuid);
        if (player != null) player.displayClientMessage(message, true);
    }

    private String typeTranslationKey() {
        return "entity.ow." + soulData.entityType().getPath();
    }

    // ── Accès lecture pour le HUD ────────────────────────────────────────────
    public float getMaterialization() { return materialization; }
    public float getStability() { return ghost == null ? 0f : ghost.getStabilityFraction(); }
    public int getCurrentWave() { return Math.min(currentWave + 1, totalWaves); }
    public int getTotalWaves() { return totalWaves; }

    /** Phase musicale pour le client : 0 = intro, 1 = combat, 2 = outro. */
    public int getMusicPhase() {
        return switch (phase) {
            case INTRO -> 0;
            case END -> 2;
            default -> 1;
        };
    }

    /** Vrai pendant la phase d'intro (avant la 1ère vague) — sert au HUD. */
    public boolean isIntro() { return phase == Phase.INTRO; }
}
