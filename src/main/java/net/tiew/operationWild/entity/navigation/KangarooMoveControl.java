package net.tiew.operationWild.entity.navigation;

import net.minecraft.util.Mth;
import net.tiew.operationWild.entity.OWEntity;
import net.tiew.operationWild.entity.animals.terrestrial.KangarooEntity;

public class KangarooMoveControl extends OWEntity.OWMoveControl {

    public static final float BACKSTEP_ANGLE = 130.0f;
    public static final float PIVOT_STEP_DEGREES = 13.0f;

    private final KangarooEntity kangaroo;

    public KangarooMoveControl(KangarooEntity kangaroo) {
        super(kangaroo);
        this.kangaroo = kangaroo;
    }

    @Override
    public void tick() {
        if (!shouldConstrain()) {
            kangaroo.setPivoting(false);
            super.tick();
            return;
        }

        double dx = this.wantedX - kangaroo.getX();
        double dz = this.wantedZ - kangaroo.getZ();

        if (dx * dx + dz * dz < 2.5E-7D) {
            kangaroo.setPivoting(false);
            super.tick();
            return;
        }

        float wantedYaw = (float) (Mth.atan2(dz, dx) * Mth.RAD_TO_DEG) - 90.0f;
        float delta = Mth.wrapDegrees(wantedYaw - kangaroo.getYRot());

        if (Math.abs(delta) <= BACKSTEP_ANGLE) {
            kangaroo.setPivoting(false);
            super.tick();
            return;
        }

        kangaroo.setPivoting(true);

        float step = Mth.clamp(delta, -PIVOT_STEP_DEGREES, PIVOT_STEP_DEGREES);
        kangaroo.setYRot(kangaroo.getYRot() + step);
        kangaroo.yBodyRot = kangaroo.getYRot();
        kangaroo.setYHeadRot(kangaroo.getYRot());

        kangaroo.setSpeed(0.0f);
        kangaroo.setZza(0.0f);
        kangaroo.setXxa(0.0f);
    }

    private boolean shouldConstrain() {
        return this.operation == Operation.MOVE_TO
                && kangaroo.getControllingPassenger() == null
                && !kangaroo.isSpinning()
                && !kangaroo.isTelluricStomping()
                && !kangaroo.isNapping()
                && !kangaroo.isSitting()
                && !kangaroo.isInWater()
                && kangaroo.onGround();
    }
}
