package net.tiew.operationWild.mixin;

import net.minecraft.core.Holder;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.biome.MultiNoiseBiomeSource;
import net.tiew.operationWild.worldgen.biome.OWRedwoodGroves;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import terrablender.worldgen.IExtendedParameterList;

@Mixin(value = MultiNoiseBiomeSource.class, priority = 900)
public abstract class MultiNoiseBiomeSourceMixin {

    @Unique
    private volatile OWRedwoodGroves ow$groves;
    @Unique
    private volatile boolean ow$grovesResolved;

    @Shadow
    public abstract Climate.ParameterList<Holder<Biome>> parameters();

    @Inject(method = "getNoiseBiome(IIILnet/minecraft/world/level/biome/Climate$Sampler;)Lnet/minecraft/core/Holder;",
            at = @At("HEAD"), cancellable = true)
    private void ow$redwoodGroves(int x, int y, int z, Climate.Sampler sampler, CallbackInfoReturnable<Holder<Biome>> cir) {
        OWRedwoodGroves groves = ow$groves();
        if (groves == null) return;
        Holder<Biome> raw = ow$raw(x, y, z, sampler);
        cir.setReturnValue(groves.apply(raw, x, z, sampler, (qx, qy, qz) -> ow$raw(qx, qy, qz, sampler)));
    }

    @Unique
    @SuppressWarnings("unchecked")
    private Holder<Biome> ow$raw(int x, int y, int z, Climate.Sampler sampler) {
        Climate.ParameterList<Holder<Biome>> parameters = this.parameters();
        Climate.TargetPoint target = sampler.sample(x, y, z);
        if (parameters instanceof IExtendedParameterList<?> extended && extended.isInitialized()) {
            return (Holder<Biome>) extended.findValuePositional(target, x, y, z);
        }
        return parameters.findValue(target);
    }

    @Unique
    private OWRedwoodGroves ow$groves() {
        if (ow$grovesResolved) return ow$groves;
        if (this.parameters() instanceof IExtendedParameterList<?> extended && !extended.isInitialized()) return null;
        ow$groves = OWRedwoodGroves.create(((BiomeSource) (Object) this).possibleBiomes());
        ow$grovesResolved = true;
        return ow$groves;
    }
}
