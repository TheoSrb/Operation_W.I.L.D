package net.tiew.operationWild.event;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.math.Axis;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.tiew.operationWild.OperationWild;
import net.tiew.operationWild.entity.animals.aquatic.ColossalSquidEntity;
import net.tiew.operationWild.entity.attacks.OWAttackIds;
import net.tiew.operationWild.entity.attacks.OWAttacksConstants;
import net.tiew.operationWild.networking.packets.to_server.OWAttackPacket;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector4f;

import java.util.ArrayList;
import java.util.List;

/**
 * Visée de la Saisie Abyssale : clic droit maintenu, un réticule de tentacules s'affiche à l'écran
 * sur chaque proie saisissable à portée et se referme sur celle du viseur ; au relâché, c'est elle
 * qui est harponnée.
 *
 * <p>Les positions sont projetées pendant le rendu du monde, puis dessinées dans le HUD : en 2D,
 * le réticule garde une taille lisible et passe toujours au-dessus des entités.
 */
public final class ColossalSquidGrabAim {

    private static final ResourceLocation OPEN = ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "textures/gui/colossal_squid/grab_open.png");
    private static final ResourceLocation LOCK = ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "textures/gui/colossal_squid/grab_lock.png");
    private static final int TEXTURE = 64;
    private static final long SERVER_CONFIRM_MS = 600L;
    private static final double MIN_AIM_RADIUS = 0.4;
    private static final float MIN_MARKER = 24f;
    private static final float MAX_MARKER = 64f;
    private static final float LOCK_SPEED = 7f;

    public static boolean aiming = false;
    public static int squidId = -1;
    public static int targetId = -1;
    private static long aimStartMs = 0L;

    private static final List<Marker> markers = new ArrayList<>();
    private static int lockedId = -1;
    private static float lockProgress = 0f;
    private static long lastHudMs = 0L;

    private record Marker(int id, float x, float y, float size, boolean locked, Component name) {
    }

    private ColossalSquidGrabAim() {
    }

    public static void start(ColossalSquidEntity squid) {
        aiming = true;
        squidId = squid.getId();
        targetId = -1;
        aimStartMs = System.currentTimeMillis();
        PacketDistributor.sendToServer(new OWAttackPacket(OWAttackIds.ABYSSAL_GRAB, OWAttackPacket.ACTION_CHARGE_START, 0f));
    }

    /** @return vrai si une proie était dans le viseur au relâché. */
    public static boolean release() {
        if (!aiming) return false;
        int target = targetId;
        reset();
        if (target != -1) {
            PacketDistributor.sendToServer(new OWAttackPacket(OWAttackIds.ABYSSAL_GRAB,
                    OWAttackPacket.ACTION_EXECUTE_WITH_TARGET, Float.intBitsToFloat(target)));
            return true;
        }
        PacketDistributor.sendToServer(new OWAttackPacket(OWAttackIds.ABYSSAL_GRAB, OWAttackPacket.ACTION_CHARGE_CANCEL, 0f));
        return false;
    }

    private static void reset() {
        aiming = false;
        squidId = -1;
        targetId = -1;
        markers.clear();
        lockedId = -1;
        lockProgress = 0f;
    }

    public static void tick() {
        if (!aiming) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null || !(mc.player.getRootVehicle() instanceof ColossalSquidEntity squid)
                || squid.getId() != squidId) {
            if (mc.player != null) PacketDistributor.sendToServer(
                    new OWAttackPacket(OWAttackIds.ABYSSAL_GRAB, OWAttackPacket.ACTION_CHARGE_CANCEL, 0f));
            reset();
            return;
        }
        boolean preparing = squid.getGrabPhase() == ColossalSquidEntity.GRAB_PREPARE;
        if (!preparing && System.currentTimeMillis() - aimStartMs > SERVER_CONFIRM_MS) {
            reset();
            return;
        }
        LivingEntity best = pickTarget(squid, mc.player);
        targetId = best == null ? -1 : best.getId();
    }

    private static double range(ColossalSquidEntity squid) {
        return OWAttacksConstants.ColossalSquid.GRAB_RANGE * squid.getScale();
    }

    private static List<LivingEntity> candidates(ColossalSquidEntity squid, Player player) {
        double r = range(squid);
        return squid.level().getEntitiesOfClass(LivingEntity.class, squid.getBoundingBox().inflate(r),
                e -> e != player && squid.canGrab(e) && squid.distanceTo(e) <= r);
    }

    private static LivingEntity pickTarget(ColossalSquidEntity squid, Player player) {
        Vec3 eye = player.getEyePosition();
        Vec3 look = player.getLookAngle();
        LivingEntity best = null;
        double bestScore = 1.0;
        for (LivingEntity candidate : candidates(squid, player)) {
            Vec3 to = candidate.getBoundingBox().getCenter().subtract(eye);
            double distance = to.length();
            if (distance < 1.0E-3) continue;
            double angle = Math.acos(Math.min(1.0, look.dot(to.scale(1.0 / distance))));
            double size = Math.max(candidate.getBbWidth(), candidate.getBbHeight()) * 0.6 + MIN_AIM_RADIUS;
            double score = angle / Math.atan2(size, distance);
            if (score < bestScore) {
                bestScore = score;
                best = candidate;
            }
        }
        return best;
    }

    /** Projette chaque proie à l'écran pendant le rendu du monde. */
    public static void project(RenderLevelStageEvent event) {
        markers.clear();
        if (!aiming) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null || !(mc.player.getRootVehicle() instanceof ColossalSquidEntity squid)) return;

        Camera cam = event.getCamera();
        Matrix4f projView = new Matrix4f(event.getProjectionMatrix()).rotate(new Quaternionf(cam.rotation()).conjugate());
        float partial = event.getPartialTick().getGameTimeDeltaPartialTick(true);
        int sw = mc.getWindow().getGuiScaledWidth();
        int sh = mc.getWindow().getGuiScaledHeight();
        Vec3 camPos = cam.getPosition();

        for (LivingEntity target : candidates(squid, mc.player)) {
            Vec3 feet = target.getPosition(partial);
            float[] center = toScreen(projView, feet.add(0, target.getBbHeight() * 0.5, 0).subtract(camPos), sw, sh);
            if (center == null) continue;
            float[] top = toScreen(projView, feet.add(0, target.getBbHeight(), 0).subtract(camPos), sw, sh);
            float[] bottom = toScreen(projView, feet.subtract(camPos), sw, sh);
            float projected = top != null && bottom != null ? Math.abs(bottom[1] - top[1]) * 1.25f : MIN_MARKER;
            float size = Mth.clamp(projected, MIN_MARKER, MAX_MARKER);
            markers.add(new Marker(target.getId(), center[0], center[1], size, target.getId() == targetId,
                    target.getDisplayName()));
        }
    }

    private static float[] toScreen(Matrix4f projView, Vec3 rel, int sw, int sh) {
        Vector4f clip = new Vector4f((float) rel.x, (float) rel.y, (float) rel.z, 1f);
        projView.transform(clip);
        if (clip.w <= 0.05f) return null;
        float ndcX = clip.x / clip.w;
        float ndcY = clip.y / clip.w;
        if (Math.abs(ndcX) > 1.2f || Math.abs(ndcY) > 1.2f) return null;
        return new float[]{(ndcX + 1f) * 0.5f * sw, (1f - ndcY) * 0.5f * sh};
    }

    /** Dessine le réticule dans le HUD. */
    public static void renderHud(GuiGraphics g) {
        if (!aiming) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.options.hideGui) return;

        long now = System.currentTimeMillis();
        float dt = lastHudMs == 0L ? 0f : Math.min((now - lastHudMs) / 1000f, 0.1f);
        lastHudMs = now;
        if (targetId != lockedId) {
            lockedId = targetId;
            lockProgress = 0f;
        }
        lockProgress = Math.min(1f, lockProgress + dt * LOCK_SPEED);
        float time = (now % 100000L) / 1000f;

        float cx = g.guiWidth() * 0.5f;
        float cy = g.guiHeight() * 0.5f;
        float hint = targetId == -1 ? 0.55f : 0.25f;
        drawIcon(g, OPEN, cx, cy, 22f, time * 25f, 0.62f, 0.92f, 1.0f, hint);

        Font font = mc.font;
        for (Marker m : markers) {
            if (!m.locked()) {
                float breath = 1f + 0.05f * Mth.sin(time * 3f + m.id());
                drawIcon(g, OPEN, m.x(), m.y(), m.size() * breath, time * 30f + m.id() * 40f, 0.55f, 0.9f, 1.0f, 0.8f);
                continue;
            }
            float p = smooth(lockProgress);
            float pulse = 1f + 0.07f * Mth.sin(time * 9f);
            float size = m.size() * Mth.lerp(p, 1.35f, 1.05f) * pulse;
            float spin = time * 70f + (1f - p) * 140f;
            float r = Mth.lerp(p, 0.55f, 1.0f);
            float gr = Mth.lerp(p, 0.9f, 0.32f);
            float b = Mth.lerp(p, 1.0f, 0.22f);
            drawIcon(g, OPEN, m.x(), m.y(), size, spin, r, gr, b, 1f - p);
            drawIcon(g, LOCK, m.x(), m.y(), size, spin, r, gr, b, p);

            String label = m.name().getString();
            int width = font.width(label);
            int ty = (int) (m.y() + size * 0.5f + 4);
            g.fill((int) m.x() - width / 2 - 3, ty - 2, (int) m.x() + width / 2 + 3, ty + 10, 0x9A0A0E14);
            g.drawString(font, label, (int) m.x() - width / 2, ty, 0xFFFF7A5C, true);
        }
    }

    private static float smooth(float t) {
        return t * t * (3f - 2f * t);
    }

    private static void drawIcon(GuiGraphics g, ResourceLocation texture, float x, float y, float size, float angle,
                                 float r, float gr, float b, float a) {
        if (a <= 0.01f) return;
        g.pose().pushPose();
        g.pose().translate(x, y, 0f);
        g.pose().mulPose(Axis.ZP.rotationDegrees(angle));
        float scale = size / TEXTURE;
        g.pose().scale(scale, scale, 1f);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShaderColor(r, gr, b, a);
        g.blit(texture, -TEXTURE / 2, -TEXTURE / 2, 0f, 0f, TEXTURE, TEXTURE, TEXTURE, TEXTURE);
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
        RenderSystem.disableBlend();
        g.pose().popPose();
    }
}
