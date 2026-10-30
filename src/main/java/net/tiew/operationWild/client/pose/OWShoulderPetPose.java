package net.tiew.operationWild.client.pose;

import net.minecraft.client.model.PlayerModel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.tiew.operationWild.entity.animals.terrestrial.RedPandaEntity;

import static net.tiew.operationWild.client.pose.OWPoseMath.add;
import static net.tiew.operationWild.client.pose.OWPoseMath.blend;

@OnlyIn(Dist.CLIENT)
public final class OWShoulderPetPose extends OWPlayerPose<OWShoulderPetPose.PetData> {

    public static final OWShoulderPetPose INSTANCE = new OWShoulderPetPose();

    private static final int CYCLE_TICKS = 300;
    private static final int PET_TICKS = 56;

    private OWShoulderPetPose() {}

    static final class PetData {
        final float seed;
        final int offset;

        PetData(float seed) {
            this.seed = seed;
            this.offset = (int) (seed / Mth.TWO_PI * CYCLE_TICKS);
        }
    }

    @Override
    protected PetData createData(Player player) {
        return new PetData(OWPoseMath.seed(player));
    }

    @Override
    protected boolean tick(Player player, PetData data, OWPlayerPoseState state) {
        if (player.isPassenger() || player.swinging || player.isUsingItem()) return false;
        if ((player.tickCount + data.offset) % CYCLE_TICKS >= PET_TICKS) return false;
        for (Entity passenger : player.getPassengers()) {
            if (passenger instanceof RedPandaEntity panda && panda.isOnShoulder()) return true;
        }
        return false;
    }

    @Override
    protected float blendIn() {
        return 0.12f;
    }

    @Override
    protected float blendOut() {
        return 0.1f;
    }

    @Override
    protected void pose(PlayerModel<?> model, Player player, PetData data, float weight, float partialTick) {
        float t = player.tickCount + partialTick;
        float scratch = t * 1.6f + data.seed;
        add(model.head, weight, 0.1f, 0.55f, -0.22f);
        blend(model.leftArm, weight, -1.78f + Mth.sin(scratch) * 0.05f, 1.33f + Mth.sin(scratch + 1f) * 0.06f, Mth.sin(scratch) * 0.05f);
    }
}
