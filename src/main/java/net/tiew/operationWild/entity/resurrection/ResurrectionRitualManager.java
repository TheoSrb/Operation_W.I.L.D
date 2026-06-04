package net.tiew.operationWild.entity.resurrection;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.FinalizeSpawnEvent;
import net.neoforged.neoforge.event.entity.living.LivingChangeTargetEvent;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;
import net.neoforged.neoforge.event.entity.living.LivingExperienceDropEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.tiew.operationWild.OperationWild;
import net.tiew.operationWild.component.SoulData;
import net.tiew.operationWild.entity.misc.SoulGhostEntity;
import net.tiew.operationWild.networking.OWNetworkHandler;
import net.tiew.operationWild.networking.packets.to_client.RitualSyncPacket;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

/**
 * Gestionnaire serveur des Rituels de Communion actifs. Ticke chaque rituel, synchronise le
 * HUD du propriétaire, et nettoie les rituels terminés.
 */
@EventBusSubscriber(modid = OperationWild.MOD_ID, bus = EventBusSubscriber.Bus.GAME)
public class ResurrectionRitualManager {

    private static final List<ResurrectionRitual> ACTIVE = new ArrayList<>();

    // ===================== RÉGLAGES AJUSTABLES =====================
    /** Rayon horizontal de la zone qui doit être dégagée autour du point d'ancrage. */
    public static int CLEAR_RADIUS = 3;
    /** Hauteur de la zone à dégager. */
    public static int CLEAR_HEIGHT = 3;
    /** Ratio maximal de blocs obstrués toléré dans la zone (au-delà → "pas assez d'espace"). */
    public static double MAX_BLOCKED_RATIO = 0.25;
    /** Distance devant le joueur où s'ancre le rituel. */
    public static double ANCHOR_FORWARD = 3.0;
    /** Rayon autour de l'âme dans lequel AUCUN spawn naturel (grottes/nuit) n'est autorisé. */
    public static double NO_NATURAL_SPAWN_RADIUS = 32.0;
    /** Rayon autour de l'âme dans lequel le joueur ne peut PAS poser de blocs (anti-mur). */
    public static double NO_BUILD_RADIUS = 25.0;
    // ==============================================================

    public static boolean hasActiveRitual(UUID playerUuid) {
        return ACTIVE.stream().anyMatch(r -> r.getPlayerUuid().equals(playerUuid));
    }

    /** Démarre un rituel devant le joueur. Renvoie false si impossible (rituel en cours, pas d'espace...). */
    public static boolean startRitual(ServerPlayer player, SoulData soulData) {
        if (hasActiveRitual(player.getUUID())) return false;
        if (!(player.level() instanceof ServerLevel level)) return false;

        Vec3 look = player.getLookAngle();
        Vec3 horizontal = new Vec3(look.x, 0, look.z);
        if (horizontal.lengthSqr() > 1.0E-6) horizontal = horizontal.normalize();
        Vec3 anchor = player.position().add(horizontal.scale(ANCHOR_FORWARD));
        anchor = snapToGround(level, anchor); // pose l'âme exactement sur le sol → frappable

        if (!hasEnoughSpace(level, BlockPos.containing(anchor))) {
            player.displayClientMessage(Component.translatable("ritual.ow.deny.no_space")
                    .setStyle(Style.EMPTY.withColor(0xD94747)), true);
            return false;
        }

        ResurrectionRitual ritual = new ResurrectionRitual(level, player, soulData, anchor);
        ritual.spawnGhost();
        ACTIVE.add(ritual);
        return true;
    }

    /** Abaisse le point d'ancrage jusqu'au sol solide le plus proche (cherche vers le bas). */
    private static Vec3 snapToGround(ServerLevel level, Vec3 anchor) {
        int x = (int) Math.floor(anchor.x);
        int z = (int) Math.floor(anchor.z);
        int startY = (int) Math.floor(anchor.y) + 2;
        for (int y = startY; y > startY - 12; y--) {
            BlockPos p = new BlockPos(x, y, z);
            if (!level.getBlockState(p).getCollisionShape(level, p).isEmpty()) {
                return new Vec3(anchor.x, y + 1, anchor.z); // sur le dessus du bloc solide
            }
        }
        return anchor;
    }

    /** Vérifie qu'une zone suffisamment dégagée existe autour du point d'ancrage (pas un trou 2x2). */
    private static boolean hasEnoughSpace(ServerLevel level, BlockPos center) {
        int total = 0;
        int blocked = 0;
        for (int dx = -CLEAR_RADIUS; dx <= CLEAR_RADIUS; dx++) {
            for (int dz = -CLEAR_RADIUS; dz <= CLEAR_RADIUS; dz++) {
                for (int dy = 0; dy < CLEAR_HEIGHT; dy++) {
                    BlockPos pos = center.offset(dx, dy, dz);
                    total++;
                    if (!level.getBlockState(pos).getCollisionShape(level, pos).isEmpty()) {
                        blocked++;
                    }
                }
            }
        }
        return total > 0 && ((double) blocked / total) <= MAX_BLOCKED_RATIO;
    }

    /**
     * Empêche les spawns NATURELS (grottes, obscurité, patrouilles...) d'apparaître près d'une âme
     * en cours de rituel. N'affecte pas nos vagues (type {@code EVENT}) ni les spawns volontaires.
     */
    @SubscribeEvent
    public static void onFinalizeSpawn(FinalizeSpawnEvent event) {
        if (ACTIVE.isEmpty()) return;
        MobSpawnType type = event.getSpawnType();
        if (type != MobSpawnType.NATURAL && type != MobSpawnType.CHUNK_GENERATION
                && type != MobSpawnType.PATROL && type != MobSpawnType.REINFORCEMENT
                && type != MobSpawnType.JOCKEY) {
            return;
        }
        double x = event.getX(), y = event.getY(), z = event.getZ();
        for (ResurrectionRitual ritual : ACTIVE) {
            if (ritual.isProtecting() && ritual.isNear(x, y, z, NO_NATURAL_SPAWN_RADIUS)) {
                event.setSpawnCancelled(true);
                return;
            }
        }
    }

    /**
     * Redirige les changements de cible des monstres du rituel vers l'âme. Empêche les OWEntity
     * sauvages d'aggro le joueur "à vue" via leurs propres goals. La <b>riposte</b> reste possible :
     * si le joueur ciblé a récemment blessé le monstre, on laisse le monstre le viser.
     */
    @SubscribeEvent
    public static void onChangeTarget(LivingChangeTargetEvent event) {
        if (!(event.getEntity() instanceof Mob mob)) return;
        if (!mob.getPersistentData().hasUUID("OWRitualGhost")) return;
        if (!(mob.level() instanceof ServerLevel sl)) return;

        Entity ghostEntity = sl.getEntity(mob.getPersistentData().getUUID("OWRitualGhost"));
        if (!(ghostEntity instanceof SoulGhostEntity ghost) || !ghost.isAlive()) return;

        LivingEntity newTarget = event.getNewAboutToBeSetTarget();
        boolean hunter = mob.getPersistentData().getBoolean("OWHuntPlayer");

        if (hunter) {
            // Chasseur : il poursuit le joueur. S'il essaie de viser autre chose, on le renvoie
            // vers le joueur le plus proche (ou l'âme en dernier recours).
            if (newTarget instanceof Player) return;
            Player nearest = sl.getNearestPlayer(mob, 64.0);
            event.setNewAboutToBeSetTarget(nearest != null ? nearest : ghost);
            return;
        }

        // Viseur d'âme : reste sur l'âme...
        if (newTarget == ghost) return;
        // ...sauf riposte : le joueur visé vient de le blesser.
        if (newTarget instanceof Player && mob.getLastHurtByMob() == newTarget) return;
        event.setNewAboutToBeSetTarget(ghost);
    }

    /** Anti-exploit : interdit de murer l'âme avec des blocs pendant le rituel (la rendrait intouchable). */
    @SubscribeEvent
    public static void onBlockPlace(BlockEvent.EntityPlaceEvent event) {
        if (ACTIVE.isEmpty()) return;
        if (!(event.getEntity() instanceof Player)) return;
        BlockPos pos = event.getPos();
        for (ResurrectionRitual ritual : ACTIVE) {
            if (ritual.isProtecting() && ritual.isNear(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, NO_BUILD_RADIUS)) {
                event.setCanceled(true);
                if (event.getEntity() instanceof ServerPlayer sp) {
                    sp.displayClientMessage(Component.translatable("ritual.ow.deny.no_build")
                            .setStyle(Style.EMPTY.withColor(0xD94747)), true);
                }
                return;
            }
        }
    }

    /** Anti-exploit : interdit aussi de DÉTRUIRE des blocs autour de l'âme pendant le rituel. */
    @SubscribeEvent
    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        if (ACTIVE.isEmpty()) return;
        BlockPos pos = event.getPos();
        for (ResurrectionRitual ritual : ACTIVE) {
            if (ritual.isProtecting() && ritual.isNear(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, NO_BUILD_RADIUS)) {
                event.setCanceled(true);
                if (event.getPlayer() instanceof ServerPlayer sp) {
                    sp.displayClientMessage(Component.translatable("ritual.ow.deny.no_build")
                            .setStyle(Style.EMPTY.withColor(0xD94747)), true);
                }
                return;
            }
        }
    }

    /** Anti-farm : les monstres invoqués par le rituel ne lâchent aucun butin. */
    @SubscribeEvent
    public static void onLivingDrops(LivingDropsEvent event) {
        if (event.getEntity().getPersistentData().hasUUID("OWRitualGhost")) {
            event.setCanceled(true);
        }
    }

    /** Anti-farm : ni XP non plus. */
    @SubscribeEvent
    public static void onXpDrop(LivingExperienceDropEvent event) {
        if (event.getEntity().getPersistentData().hasUUID("OWRitualGhost")) {
            event.setCanceled(true);
        }
    }

    /** Si le joueur qui a lancé le rituel se déconnecte, le rituel est annulé immédiatement. */
    @SubscribeEvent
    public static void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        UUID id = event.getEntity().getUUID();
        Iterator<ResurrectionRitual> it = ACTIVE.iterator();
        while (it.hasNext()) {
            ResurrectionRitual ritual = it.next();
            if (ritual.getPlayerUuid().equals(id)) {
                ritual.abort();
                it.remove();
            }
        }
    }

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        if (ACTIVE.isEmpty()) return;

        Iterator<ResurrectionRitual> it = ACTIVE.iterator();
        while (it.hasNext()) {
            ResurrectionRitual ritual = it.next();
            ritual.tick();

            ServerPlayer player = event.getServer().getPlayerList().getPlayer(ritual.getPlayerUuid());
            if (ritual.isFinished()) {
                if (player != null) {
                    OWNetworkHandler.sendToClient(new RitualSyncPacket(false, 0f, 0f, 0, 0, 0), player);
                }
                it.remove();
            } else if (player != null) {
                OWNetworkHandler.sendToClient(new RitualSyncPacket(true,
                        ritual.getMaterialization(), ritual.getStability(),
                        ritual.getCurrentWave(), ritual.getTotalWaves(), ritual.getMusicPhase()), player);
            }
        }
    }
}
