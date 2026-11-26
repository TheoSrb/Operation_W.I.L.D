package net.tiew.operationWild.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.tiew.operationWild.entity.OWSemiFlyingEntity;

@OnlyIn(Dist.CLIENT)
public class OWFlightWindSound extends AbstractTickableSoundInstance {

    private static final float MAX_VOLUME = 0.75f;
    private static final float START_SPEED = 0.35f;

    private final OWSemiFlyingEntity flyer;

    public OWFlightWindSound(OWSemiFlyingEntity flyer) {
        super(SoundEvents.ELYTRA_FLYING, SoundSource.NEUTRAL, SoundInstance.createUnseededRandom());
        this.flyer = flyer;
        this.looping = true;
        this.delay = 0;
        this.volume = 0.0f;
        this.relative = true;
    }

    @Override
    public void tick() {
        var player = Minecraft.getInstance().player;
        if (this.flyer.isRemoved() || player == null || player.getVehicle() != this.flyer || !this.flyer.isAirborneFlight()) {
            this.stop();
            return;
        }
        float speed = (float) this.flyer.getDeltaMovement().length();
        float wanted = Mth.clamp((speed - START_SPEED) / 1.0f, 0f, 1f) * MAX_VOLUME;
        this.volume += (wanted - this.volume) * 0.1f;
        this.pitch = 0.7f + Mth.clamp(speed, 0f, 1.6f) * 0.35f;
    }
}
