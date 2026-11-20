package net.tiew.operationWild.particle;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.Mth;

public record EmpressFxOptions(int kind, int color, float size, int life, float yaw, int follow) implements ParticleOptions {

    public static final MapCodec<EmpressFxOptions> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.INT.fieldOf("kind").forGetter(EmpressFxOptions::kind),
            Codec.INT.fieldOf("color").forGetter(EmpressFxOptions::color),
            Codec.FLOAT.fieldOf("size").forGetter(EmpressFxOptions::size),
            Codec.INT.fieldOf("life").forGetter(EmpressFxOptions::life),
            Codec.FLOAT.fieldOf("yaw").forGetter(EmpressFxOptions::yaw),
            Codec.INT.optionalFieldOf("follow", -1).forGetter(EmpressFxOptions::follow)
    ).apply(instance, EmpressFxOptions::new));

    public static final StreamCodec<ByteBuf, EmpressFxOptions> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, EmpressFxOptions::kind,
            ByteBufCodecs.INT, EmpressFxOptions::color,
            ByteBufCodecs.FLOAT, EmpressFxOptions::size,
            ByteBufCodecs.VAR_INT, EmpressFxOptions::life,
            ByteBufCodecs.FLOAT, EmpressFxOptions::yaw,
            ByteBufCodecs.VAR_INT, EmpressFxOptions::follow,
            EmpressFxOptions::new);

    public EmpressFxOptions {
        size = Mth.clamp(size, 0.01f, 64f);
        life = Mth.clamp(life, 1, 1200);
    }

    public EmpressFxOptions(int kind, int color, float size, int life, float yaw) {
        this(kind, color, size, life, yaw, -1);
    }

    public int rgb() {
        return this.color & 0xFFFFFF;
    }

    public int lockAge() {
        return (this.color >>> 24) & 255;
    }

    @Override
    public ParticleType<?> getType() {
        return OWParticles.PLANT_EMPRESS_FX.get();
    }
}
