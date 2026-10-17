package net.tiew.operationWild.entity.taming;

public class OWSedationAnimState {
    public enum Phase { NONE, SLEEP, WAKE }

    private static final int LOADED_ASLEEP_TICKS = 5;

    private Phase phase = Phase.NONE;
    private int phaseStartTick;
    private boolean skipFall;
    private Phase previous = Phase.NONE;
    private int previousElapsedTicks;
    private boolean previousSkippedFall;

    public void update(boolean asleep, int tickCount) {
        if (asleep && this.phase != Phase.SLEEP) {
            this.enter(Phase.SLEEP, tickCount, tickCount <= LOADED_ASLEEP_TICKS);
        } else if (!asleep && this.phase == Phase.SLEEP) {
            this.enter(Phase.WAKE, tickCount, false);
        }
    }

    private void enter(Phase next, int tickCount, boolean skip) {
        this.previous = this.phase;
        this.previousElapsedTicks = tickCount - this.phaseStartTick;
        this.previousSkippedFall = this.skipFall;
        this.phase = next;
        this.phaseStartTick = tickCount;
        this.skipFall = skip;
    }

    public Phase phase() {
        return this.phase;
    }

    public Phase previous() {
        return this.previous;
    }

    public boolean skipsFall() {
        return this.skipFall;
    }

    public boolean previousSkippedFall() {
        return this.previousSkippedFall;
    }

    public long elapsedMs(float ageInTicks) {
        return (long) (Math.max(0f, ageInTicks - this.phaseStartTick) * 50f);
    }

    public long previousElapsedMs() {
        return this.previousElapsedTicks * 50L;
    }
}
