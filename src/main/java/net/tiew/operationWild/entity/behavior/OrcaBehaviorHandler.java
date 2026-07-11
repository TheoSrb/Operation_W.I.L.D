package net.tiew.operationWild.entity.behavior;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.tiew.operationWild.entity.animals.aquatic.OrcaEntity;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Server-side coordinator for the wild Orca pack-hunting mechanic.
 *
 * This is a passive companion class to {@link OrcaEntity} (same pattern as
 * {@code CrocodileBehaviorHandler} / {@code KodiakBehaviorHandler}). It centralises
 * the heavy pack logic so that {@code OWOrcaPackHuntGoal} stays thin.
 *
 * <h2>Single source of truth</h2>
 * Only the <b>leader</b> (the orca whose {@code PACK_ROLE == 0}) coordinates. Its handler
 * owns the ordered member list, the shared target, and the {@code currentAttackerIndex}.
 * Every member reads "whose turn it is" from the leader, so only one orca ever charges at a
 * time. Goals tick sequentially on the single server thread, so no synchronisation primitives
 * are needed. The attack order itself is mirrored on each orca through the synced
 * {@code PACK_ROLE} accessor (project convention), while the member list / target / leader
 * reference are transient server-only state (a pack is ephemeral combat state, never saved).
 */
public class OrcaBehaviorHandler {

    // ── Pack tuning constants ────────────────────────────────────────────────

    /** Radius (blocks) within which a leader recruits packmates and looks for prey. */
    public static final double PACK_DETECTION_RADIUS = 16.0;
    /** Maximum pack size: 1 leader + 2 followers. */
    public static final int    MAX_PACK_SIZE         = 3;
    /** Distance (blocks) a member keeps from the target while waiting / after a charge. */
    public static final double SAFE_DISTANCE         = 8.0;
    /** A follower straying further than this from the target is dropped from the pack. */
    public static final double LEAVE_RANGE           = 24.0;
    /** The leader disbands once it is dragged further than this from the target. */
    public static final double DISBAND_RANGE         = 32.0;
    /** Ticks of delay before the next attacker engages, giving the cycle a readable beat. */
    public static final int    INTER_ATTACK_STAGGER  = 15;

    public static final int PACK_ROLE_NONE   = -1;
    public static final int PACK_ROLE_LEADER = 0;

    private final OrcaEntity orca;

    // Leader-only state (meaningful only while this.orca has role 0).
    private final List<OrcaEntity> packMembers = new ArrayList<>();
    private LivingEntity packTarget;
    private int currentAttackerIndex = 0;
    private int staggerTicks = 0;

    public OrcaBehaviorHandler(OrcaEntity orca) {
        this.orca = orca;
    }

    // ── Formation ────────────────────────────────────────────────────────────

    /**
     * Tries to build a pack around this orca (as leader) for the given target.
     * Recruits nearby wild, adult, pack-less orcas up to {@link #MAX_PACK_SIZE}.
     *
     * @return {@code true} if a real pack (>= 2 orcas) was formed; {@code false} when no
     *         recruitable orca is nearby, in which case the caller falls back to solo combat.
     */
    public boolean tryFormPack(LivingEntity target) {
        if (orca.isTame() || target == null || !target.isAlive()) return false;

        List<OrcaEntity> recruits = orca.level().getEntitiesOfClass(
                OrcaEntity.class,
                orca.getBoundingBox().inflate(PACK_DETECTION_RADIUS),
                OrcaBehaviorHandler::isRecruitable
        );
        recruits.remove(orca);
        if (recruits.isEmpty()) return false;

        packMembers.clear();
        packMembers.add(orca);
        orca.setPackRole(PACK_ROLE_LEADER);
        orca.setPackLeader(orca);

        for (OrcaEntity recruit : recruits) {
            if (packMembers.size() >= MAX_PACK_SIZE) break;
            packMembers.add(recruit);
            recruit.setPackRole(packMembers.size() - 1);
            recruit.setPackLeader(orca);
        }

        if (packMembers.size() < 2) { // no real pack, undo
            disband();
            return false;
        }

        this.packTarget = target;
        this.currentAttackerIndex = 0;
        this.staggerTicks = 0;
        return true;
    }

    /**
     * A recruitable orca is wild, adult, alive, currently pack-less, and free to fight.
     * Because goals tick sequentially, the first leader-candidate of the tick stamps the
     * others as followers here, so a second leader never forms for the same orcas.
     */
    private static boolean isRecruitable(OrcaEntity o) {
        return o.isAlive()
                && !o.isTame()
                && !o.isBaby()
                && o.getPackRole() == PACK_ROLE_NONE
                && o.isInWater()
                && !o.isVehicle()
                && !o.isSitting()
                && !o.isNapping();
    }

    // ── Leader tick (coordination) ───────────────────────────────────────────

    /**
     * Run once per tick by the leader's goal. Prunes lost members, validates the target,
     * and decays the inter-attack stagger. Dissolves the pack when it is no longer viable.
     */
    public void tickLeader() {
        if (orca.getPackRole() != PACK_ROLE_LEADER) return;

        if (staggerTicks > 0) staggerTicks--;

        pruneMembers();

        if (packTarget == null || !packTarget.isAlive()
                || orca.distanceTo(packTarget) > DISBAND_RANGE
                || packMembers.size() < 2) {
            disband();
            return;
        }

        if (currentAttackerIndex >= packMembers.size()) {
            currentAttackerIndex = 0;
        }
    }

    /**
     * Removes dead / tamed / detached / strayed followers and re-compacts the roles so the
     * remaining members keep a contiguous 0..n-1 ordering. The leader always stays at index 0.
     */
    private void pruneMembers() {
        boolean changed = false;
        Iterator<OrcaEntity> it = packMembers.iterator();
        while (it.hasNext()) {
            OrcaEntity member = it.next();
            if (member == orca) continue; // never drop the leader here

            boolean lost = member == null
                    || !member.isAlive()
                    || member.isTame()
                    || member.getPackLeader() != orca
                    || (packTarget != null && member.distanceTo(packTarget) > LEAVE_RANGE);

            if (lost) {
                if (member != null && member.getPackLeader() == orca) {
                    member.setPackRole(PACK_ROLE_NONE);
                    member.setPackLeader(null);
                }
                it.remove();
                changed = true;
            }
        }
        if (changed) reindexRoles();
    }

    private void reindexRoles() {
        for (int i = 0; i < packMembers.size(); i++) {
            packMembers.get(i).setPackRole(i);
        }
        if (currentAttackerIndex >= packMembers.size()) {
            currentAttackerIndex = 0;
        }
    }

    /** Fully dissolves the pack, releasing every member back to solo behaviour. */
    public void disband() {
        for (OrcaEntity member : packMembers) {
            if (member != null && member.getPackLeader() == orca) {
                member.setPackRole(PACK_ROLE_NONE);
                member.setPackLeader(null);
                member.forceSetTarget(null);
            }
        }
        packMembers.clear();
        packTarget = null;
        currentAttackerIndex = 0;
        staggerTicks = 0;
    }

    // ── Rotation ─────────────────────────────────────────────────────────────

    /** The member currently authorised to charge, or {@code null} if the pack is empty. */
    public OrcaEntity getCurrentAttacker() {
        if (packMembers.isEmpty()) return null;
        if (currentAttackerIndex < 0 || currentAttackerIndex >= packMembers.size()) return null;
        return packMembers.get(currentAttackerIndex);
    }

    /** Hands the turn to the next member and arms the inter-attack stagger. */
    public void advanceRotation() {
        if (packMembers.isEmpty()) return;
        currentAttackerIndex = (currentAttackerIndex + 1) % packMembers.size();
        staggerTicks = INTER_ATTACK_STAGGER;
    }

    // ── Accessors ────────────────────────────────────────────────────────────

    public LivingEntity getPackTarget() { return packTarget; }

    public int getStaggerTicks() { return staggerTicks; }

    public boolean isPackActive() {
        return orca.getPackRole() == PACK_ROLE_LEADER
                && packMembers.size() >= 2
                && packTarget != null
                && packTarget.isAlive();
    }

    // ── Target selection ─────────────────────────────────────────────────────

    /**
     * A valid pack-hunt target: a non-allied player, animal or monster that is in the water
     * (open-water hunting; coastal prey is handled by {@code OWOrcaBeachingGoal}). Other orcas
     * are never valid prey.
     */
    public static boolean isHuntableTarget(LivingEntity e, OrcaEntity orca) {
        if (e == null || !e.isAlive() || e == orca) return false;
        if (e instanceof OrcaEntity) return false;
        if (!e.isInWater()) return false;
        if (orca.isAlliedTo(e)) return false;
        if (e instanceof Player player) return !player.isSpectator() && !player.isCreative();
        return e instanceof Animal || e instanceof Monster;
    }
}
