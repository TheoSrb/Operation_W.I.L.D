package net.tiew.operationWild.entity.clan;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.tiew.operationWild.entity.animals.terrestrial.LionEntity;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class LionWildMind {

    private static final int AGGRESSION_REFRESH_TICKS = 20;

    private final LionEntity lion;

    private float aggression = 1f;

    @Nullable
    private LivingEntity warnTarget;
    private int warnTicks;

    @Nullable
    private UUID intruder;
    private long intruderUntil;

    @Nullable
    private LivingEntity fleeFrom;
    private int fleeTicks;

    private int chorusReplyIn = -1;

    public boolean perchedWatch;
    public boolean eating;

    public LionWildMind(LionEntity lion) {
        this.lion = lion;
    }

    public void tick(ServerLevel level) {
        if ((lion.tickCount + lion.getId()) % AGGRESSION_REFRESH_TICKS == 0) {
            aggression = LionPride.aggression(lion, lion.wildClan(), level.getGameTime());
        }
        if (warnTicks > 0 && --warnTicks == 0) warnTarget = null;
        if (fleeTicks > 0 && --fleeTicks == 0) fleeFrom = null;
        if (chorusReplyIn > 0 && --chorusReplyIn == 0) {
            chorusReplyIn = -1;
            lion.chorusReply(level);
        }
        if (intruder != null && level.getGameTime() >= intruderUntil) intruder = null;
        if (lion.isSitting() && !perchedWatch) lion.setSitting(false);
    }

    public float aggression() {
        return aggression;
    }

    public void warn(LivingEntity target, int ticks) {
        warnTarget = target;
        warnTicks = Math.max(warnTicks, ticks);
    }

    @Nullable
    public LivingEntity warnTarget() {
        if (warnTarget != null && (!warnTarget.isAlive() || warnTicks <= 0)) warnTarget = null;
        return warnTarget;
    }

    public void designateIntruder(Player player, long until) {
        intruder = player.getUUID();
        intruderUntil = until;
    }

    @Nullable
    public Player intruder() {
        if (intruder == null) return null;
        Player player = lion.level().getPlayerByUUID(intruder);
        return player != null && player.isAlive() ? player : null;
    }

    public void forgetIntruder() {
        intruder = null;
    }

    public void flee(LivingEntity threat, int ticks) {
        fleeFrom = threat;
        fleeTicks = ticks;
    }

    @Nullable
    public LivingEntity fleeThreat() {
        return fleeTicks > 0 ? fleeFrom : null;
    }

    public boolean isConceding() {
        return fleeTicks > 0 && fleeFrom != null;
    }

    public void scheduleChorusReply(int delay) {
        chorusReplyIn = Math.max(1, delay);
    }
}
