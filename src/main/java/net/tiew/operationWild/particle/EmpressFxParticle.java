package net.tiew.operationWild.particle;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public class EmpressFxParticle extends Particle {

    private static final float ATLAS = 256f;
    private static final int FULL_BRIGHT = 0xF000F0;

    private final TextureAtlasSprite sprite;
    private final int kind;
    private final float size;
    private final float yaw;
    private final float tintR;
    private final float tintG;
    private final float tintB;
    private final float heightOffset;
    private final int followId;
    private final int lockAge;

    protected EmpressFxParticle(ClientLevel level, double x, double y, double z, EmpressFxOptions options, SpriteSet sprites) {
        super(level, x, y, z);
        this.sprite = sprites.get(this.random);
        this.kind = options.kind();
        this.size = options.size();
        this.yaw = options.yaw();
        this.lifetime = options.life();
        this.followId = options.follow();
        this.lockAge = options.lockAge();
        this.tintR = ((options.color() >> 16) & 255) / 255f;
        this.tintG = ((options.color() >> 8) & 255) / 255f;
        this.tintB = (options.color() & 255) / 255f;
        this.heightOffset = 0.03f + this.random.nextFloat() * 0.025f;
        this.hasPhysics = false;
        this.gravity = 0f;
        this.xd = 0;
        this.yd = 0;
        this.zd = 0;
    }

    @Override
    public void tick() {
        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;
        if (this.age++ >= this.lifetime) {
            this.remove();
            return;
        }
        if (this.followId >= 0 && (this.lockAge == 0 || this.age < this.lockAge)) {
            Entity followed = this.level.getEntity(this.followId);
            if (followed != null) this.setPos(followed.getX(), followed.onGround() ? followed.getY() : this.y, followed.getZ());
        }
    }

    private static float fade(float life, float in, float out) {
        float a = in > 0 ? Math.min(1f, life / in) : 1f;
        float b = out > 0 ? Math.min(1f, (1f - life) / out) : 1f;
        return Mth.clamp(Math.min(a, b), 0f, 1f);
    }

    @Override
    public void render(VertexConsumer buffer, Camera camera, float partialTicks) {
        Vec3 cam = camera.getPosition();
        float cx = (float) (Mth.lerp(partialTicks, this.xo, this.x) - cam.x);
        float cy = (float) (Mth.lerp(partialTicks, this.yo, this.y) - cam.y) + this.heightOffset;
        float cz = (float) (Mth.lerp(partialTicks, this.zo, this.z) - cam.z);
        float age = this.age + partialTicks;
        float life = Mth.clamp(age / this.lifetime, 0f, 1f);
        float rot = this.yaw * Mth.DEG_TO_RAD;

        if (this.kind == EmpressFx.WAVE) {
            float radius = this.size * (0.07f + 0.93f * life);
            float a = (1f - life) * 0.95f;
            flat(buffer, EmpressFx.RING, cx, cy, cz, radius, 0f, this.tintR, this.tintG, this.tintB, a);
            flat(buffer, EmpressFx.FILL, cx, cy - 0.002f, cz, radius * 0.95f, 0f, this.tintR, this.tintG, this.tintB, a * 0.25f);
            return;
        }

        if (!EmpressFx.isDecal(this.kind)) {
            float a = fade(life, 0.15f, 0.2f) * (0.8f + 0.2f * Mth.sin(age * 0.5f));
            float pulse = 1f + 0.05f * Mth.sin(age * 0.6f);
            flat(buffer, this.kind, cx, cy, cz, this.size * pulse, rot, 1f, 1f, 1f, a);
            return;
        }

        float appear = Math.min(1f, age / 4f);
        float end = life > 0.88f ? (life - 0.88f) / 0.12f : 0f;
        float pulse = 0.5f + 0.5f * Mth.sin(age * (0.35f + 0.6f * life));
        float spin = rot;
        if (this.kind == EmpressFx.SNARE) spin += age * 0.04f;
        if (this.kind == EmpressFx.BLOOM) spin += age * 0.02f;
        float radius = this.size * (0.85f + 0.15f * appear) * (1f + 0.06f * end);
        flat(buffer, EmpressFx.RING, cx, cy + 0.001f, cz, radius * 1.04f, 0f, this.tintR, this.tintG, this.tintB, appear * (0.55f + 0.45f * pulse));
        flat(buffer, EmpressFx.FILL, cx, cy, cz, Math.max(0.05f, radius * life), 0f, this.tintR, this.tintG, this.tintB, appear * (0.35f + 0.4f * life));
        flat(buffer, this.kind, cx, cy + 0.002f, cz, radius, spin, 1f, 1f, 1f, appear * (0.7f + 0.3f * Math.max(pulse * life, end)));
        if (end > 0f) flat(buffer, EmpressFx.FILL, cx, cy + 0.003f, cz, radius, 0f, 1f, 1f, 1f, end * 0.55f);
    }

    private void flat(VertexConsumer buffer, int cell, float cx, float cy, float cz, float half, float rot,
                      float r, float g, float b, float a) {
        if (a <= 0.004f) return;
        float u0 = u(cell, 0f), u1 = u(cell, 1f), v0 = v(cell, 0f), v1 = v(cell, 1f);
        float cos = Mth.cos(rot) * half, sin = Mth.sin(rot) * half;
        float ax = -cos + sin, az = -sin - cos;
        float bx = cos + sin, bz = sin - cos;
        float x0 = cx + ax, z0 = cz + az;
        float x1 = cx + bx, z1 = cz + bz;
        float x2 = cx - ax, z2 = cz - az;
        float x3 = cx - bx, z3 = cz - bz;
        vertex(buffer, x0, cy, z0, u0, v0, r, g, b, a);
        vertex(buffer, x1, cy, z1, u1, v0, r, g, b, a);
        vertex(buffer, x2, cy, z2, u1, v1, r, g, b, a);
        vertex(buffer, x3, cy, z3, u0, v1, r, g, b, a);
        vertex(buffer, x3, cy, z3, u0, v1, r, g, b, a);
        vertex(buffer, x2, cy, z2, u1, v1, r, g, b, a);
        vertex(buffer, x1, cy, z1, u1, v0, r, g, b, a);
        vertex(buffer, x0, cy, z0, u0, v0, r, g, b, a);
    }

    private static void vertex(VertexConsumer buffer, float x, float y, float z, float u, float v, float r, float g, float b, float a) {
        buffer.addVertex(x, y, z).setUv(u, v).setColor(r, g, b, a).setLight(FULL_BRIGHT);
    }

    private static int[] rect(int index) {
        if (index < 8) return new int[]{(index % 4) * 64, (index / 4) * 64, 64};
        int i = index - 8;
        return new int[]{(i % 8) * 32, 128 + (i / 8) * 32, 32};
    }

    private float u(int index, float f) {
        int[] r = rect(index);
        return this.sprite.getU((r[0] + 0.5f + f * (r[2] - 1f)) / ATLAS);
    }

    private float v(int index, float f) {
        int[] r = rect(index);
        return this.sprite.getV((r[1] + 0.5f + f * (r[2] - 1f)) / ATLAS);
    }

    @Override
    public AABB getRenderBoundingBox(float partialTicks) {
        float s = this.size * 1.1f;
        return new AABB(this.x - s, this.y - 0.5, this.z - s, this.x + s, this.y + 0.5, this.z + s);
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }

    public static class Provider implements ParticleProvider<EmpressFxOptions> {

        private final SpriteSet sprites;

        public Provider(SpriteSet sprites) {
            this.sprites = sprites;
        }

        @Override
        public @Nullable Particle createParticle(EmpressFxOptions options, ClientLevel level, double x, double y, double z,
                                                 double xd, double yd, double zd) {
            return new EmpressFxParticle(level, x, y, z, options, this.sprites);
        }
    }
}
