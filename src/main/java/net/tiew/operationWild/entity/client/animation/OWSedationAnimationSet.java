package net.tiew.operationWild.entity.client.animation;

import net.minecraft.client.animation.AnimationDefinition;

public record OWSedationAnimationSet(AnimationDefinition fall, AnimationDefinition rest, AnimationDefinition wake) {

    public long fallMs() {
        return (long) (this.fall.lengthInSeconds() * 1000f);
    }

    public long wakeMs() {
        return (long) (this.wake.lengthInSeconds() * 1000f);
    }
}
