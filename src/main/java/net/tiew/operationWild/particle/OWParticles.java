package net.tiew.operationWild.particle;

import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.tiew.operationWild.OperationWild;

import java.util.function.Supplier;

public class OWParticles {
    public static final DeferredRegister<ParticleType<?>> PARTICLE_TYPES = DeferredRegister.create(BuiltInRegistries.PARTICLE_TYPE, OperationWild.MOD_ID);
    public static void register(IEventBus bus) { PARTICLE_TYPES.register(bus);}

    public static final Supplier<SimpleParticleType> ELECTRIC_PARTICLES = PARTICLE_TYPES.register("electric_particles", () -> new SimpleParticleType(true));
    public static final Supplier<SimpleParticleType> NAP_PARTICLES = PARTICLE_TYPES.register("nap_particles", () -> new SimpleParticleType(true));
}
