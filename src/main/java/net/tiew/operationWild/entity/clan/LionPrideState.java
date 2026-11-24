package net.tiew.operationWild.entity.clan;

import net.minecraft.core.BlockPos;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class LionPrideState {

    public long lastBrainTick = -1_000_000L;
    public final Map<UUID, Float> intrusion = new HashMap<>();
    public long disturbedUntil;

    public long lastChorusSlot = -1L;
    public long chorusDueTick = -1L;
    public int lastVoices;

    @Nullable
    public UUID rival;
    public long rivalUntil;

    @Nullable
    public LionHunt hunt;
    public long nextHuntTick;

    @Nullable
    public BlockPos feastPos;
    public long feastUntil;
    public long feastStartTick;
    public int maleShare;

    @Nullable
    public BlockPos perch;
    public long perchExpiry;
    public long perchSearchedAt = -1_000_000L;

    @Nullable
    public BlockPos waypoint;
    public long waypointUntil;
    public boolean waypointIsChallenge;

    public boolean hasRival(long gameTime) {
        return rival != null && gameTime < rivalUntil;
    }

    public boolean isFeasting(long gameTime) {
        return feastPos != null && gameTime < feastUntil;
    }

    public void declareRival(UUID clanId, long until) {
        rival = clanId;
        rivalUntil = until;
    }

    public void clearRival() {
        rival = null;
        rivalUntil = 0L;
    }
}
