package net.tiew.operationWild.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.tiew.operationWild.OperationWild;
import net.tiew.operationWild.entity.bosses.PlantEmpressEntity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class PlantEmpressBossBar {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "textures/overlay/boss_bars/plant_empress_boss_bar.png");
    private static final double RENDER_DISTANCE = 100.0;
    private static final float SIDE_MAX = 75.0f;
    private static final float CENTER_MAX = 150.0f;
    private static final int FRAME_WIDTH = 254;
    private static final int BAR_SPACING = 60;
    private static final int TOP = 4;
    private static final int VISIBLE = 34;
    private static final int OVERLAP = 2;
    private static final int FILL_Y = 23;
    private static final int FLASH_TICKS = 6;
    private static final int LAG_HOLD_TICKS = 10;
    private static final float LAG_SPEED = 0.02f;
    private static final int FADE_TICKS = 20;
    private static final int TEXT_COLOR = 0xD6E8A0;
    private static final int TEXT_RAGE_COLOR = 0xFFAA8C;

    private static final int CENTER = 0;
    private static final int LEFT = 1;
    private static final int RIGHT = 2;

    private static final Map<Integer, Track> TRACKS = new HashMap<>();
    private static final RandomSource RANDOM = RandomSource.create();

    private static final class Leaf {
        float x, y, vx, vy, spin;
        int age;
        final int life;
        final int u;

        Leaf(float x, float y, int u) {
            this.x = x;
            this.y = y;
            this.u = u;
            this.vx = (RANDOM.nextFloat() - 0.5f) * 1.6f;
            this.vy = -0.6f - RANDOM.nextFloat() * 0.8f;
            this.spin = RANDOM.nextFloat() * Mth.TWO_PI;
            this.life = 26 + RANDOM.nextInt(14);
        }
    }

    private static final class Track {
        final float[] lag = new float[3];
        final float[] last = new float[3];
        final int[] hold = new int[3];
        final int[] flash = new int[3];
        final List<Leaf> leaves = new ArrayList<>();
        int lastTick = Integer.MIN_VALUE;
        int deathTicks;
        boolean initialized;
    }

    public static void render(GuiGraphics guiGraphics, int screenWidth, int screenHeight, float partialTick) {
        Minecraft minecraft = Minecraft.getInstance();
        Player player = minecraft.player;
        if (player == null || player.level() == null) return;

        List<PlantEmpressEntity> bosses = player.level().getEntitiesOfClass(
                PlantEmpressEntity.class,
                player.getBoundingBox().inflate(RENDER_DISTANCE),
                boss -> !boss.isRemoved() && player.distanceTo(boss) <= RENDER_DISTANCE
        );

        Set<Integer> seen = new HashSet<>();
        float scale = Math.min(1.0f, (screenWidth - 16) / (float) FRAME_WIDTH);

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        for (int i = 0; i < bosses.size(); i++) {
            PlantEmpressEntity boss = bosses.get(i);
            seen.add(boss.getId());
            Track track = TRACKS.computeIfAbsent(boss.getId(), id -> new Track());
            update(track, boss);

            guiGraphics.pose().pushPose();
            guiGraphics.pose().translate(screenWidth / 2.0f, TOP + i * BAR_SPACING * scale, 0);
            guiGraphics.pose().scale(scale, scale, 1.0f);
            renderBar(guiGraphics, minecraft.font, boss, track, -FRAME_WIDTH / 2, 0, partialTick);
            guiGraphics.pose().popPose();
        }
        guiGraphics.setColor(1.0f, 1.0f, 1.0f, 1.0f);
        RenderSystem.disableBlend();

        TRACKS.keySet().removeIf(id -> !seen.contains(id));
    }

    private static float[] lives(PlantEmpressEntity boss) {
        return new float[]{
                Mth.clamp(boss.getHead1Life() / CENTER_MAX, 0.0f, 1.0f),
                Mth.clamp(boss.getHead2Life() / SIDE_MAX, 0.0f, 1.0f),
                Mth.clamp(boss.getHead3Life() / SIDE_MAX, 0.0f, 1.0f)
        };
    }

    private static void update(Track track, PlantEmpressEntity boss) {
        float[] now = lives(boss);
        if (!track.initialized) {
            System.arraycopy(now, 0, track.lag, 0, 3);
            System.arraycopy(now, 0, track.last, 0, 3);
            track.initialized = true;
            track.lastTick = boss.tickCount;
            return;
        }
        if (track.lastTick == boss.tickCount) return;
        track.lastTick = boss.tickCount;

        for (int k = 0; k < 3; k++) {
            if (now[k] < track.last[k]) {
                track.flash[k] = FLASH_TICKS;
                track.hold[k] = LAG_HOLD_TICKS;
                if (now[k] <= 0 && k != CENTER) spawnLeaves(track, k);
            }
            if (track.flash[k] > 0) track.flash[k]--;
            if (now[k] >= track.lag[k]) {
                track.lag[k] = now[k];
            } else if (track.hold[k] > 0) {
                track.hold[k]--;
            } else {
                track.lag[k] = Math.max(now[k], track.lag[k] - LAG_SPEED);
            }
            track.last[k] = now[k];
        }

        if (boss.isDeadOrDying()) track.deathTicks++;

        Iterator<Leaf> it = track.leaves.iterator();
        while (it.hasNext()) {
            Leaf leaf = it.next();
            leaf.age++;
            leaf.vy += 0.12f;
            leaf.vx *= 0.96f;
            leaf.x += leaf.vx + Mth.sin(leaf.spin + leaf.age * 0.35f) * 0.35f;
            leaf.y += Math.min(leaf.vy, 1.2f);
            if (leaf.age >= leaf.life) it.remove();
        }
    }

    private static void spawnLeaves(Track track, int head) {
        float cx = head == LEFT ? 60 : 194;
        for (int n = 0; n < 9; n++) {
            int u = n % 3 == 0 ? 216 : (n % 3 == 1 ? 222 : 228);
            track.leaves.add(new Leaf(cx - 3 + (RANDOM.nextFloat() - 0.5f) * 14, 25 + (RANDOM.nextFloat() - 0.5f) * 10, u));
        }
    }

    private static void blit(GuiGraphics g, int x, int y, int u, int v, int w, int h) {
        if (w > 0 && h > 0) g.blit(TEXTURE, x, y, u, v, w, h, 256, 256);
    }

    private static int fillWidth(float frac) {
        if (frac <= 0) return 0;
        return Math.min(VISIBLE + 2 * OVERLAP, OVERLAP + Math.max(1, Math.round(VISIBLE * frac)));
    }

    private static void chamber(GuiGraphics g, int x0, int x1, int y, int fillV, float frac, float lag, boolean anchorRight, int scroll, boolean withered, int edgeU) {
        if (withered) {
            blit(g, x0, y, 160, 230, x1 - x0 + 1, 9);
            return;
        }
        int lw = fillWidth(lag);
        int fw = fillWidth(frac);
        if (lw > fw) blit(g, anchorRight ? x1 - lw + 1 : x0, y, 160, 221, lw, 9);
        if (fw <= 0) return;
        int u = anchorRight ? (16 - scroll) % 16 : scroll;
        blit(g, anchorRight ? x1 - fw + 1 : x0, y, 160 + u, fillV, fw, 9);
        if (edgeU >= 0 && fw < VISIBLE + OVERLAP) {
            blit(g, anchorRight ? x1 - fw + 1 : x0 + fw - 2, y, edgeU, 214, 2, 9);
        }
    }

    private static int shake(Track track, int head) {
        return track.flash[head] > FLASH_TICKS - 3 ? (track.flash[head] % 2 == 0 ? 1 : -1) : 0;
    }

    private static void renderBar(GuiGraphics g, Font font, PlantEmpressEntity boss, Track track, int x, int y, float partialTick) {
        float[] life = lives(boss);
        boolean dying = boss.isDeadOrDying();
        boolean leftDead = life[LEFT] <= 0;
        boolean rightDead = life[RIGHT] <= 0;
        boolean sealed = !(leftDead && rightDead);
        boolean centerDead = life[CENTER] <= 0 || dying;
        boolean rage = !sealed && !centerDead;

        float alpha = dying ? Mth.clamp((PlantEmpressEntity.FINAL_DEATH_TICKS - track.deathTicks - partialTick) / FADE_TICKS, 0.0f, 1.0f) : 1.0f;
        if (alpha <= 0) return;
        g.setColor(1.0f, 1.0f, 1.0f, alpha);

        float time = boss.tickCount + partialTick;
        int scroll = ((int) (time / 2)) % 16;

        blit(g, x, y, 0, 50, 254, 50);

        int fy = y + FILL_Y;
        chamber(g, x + 10, x + 47, fy, 194, life[LEFT], track.lag[LEFT], true, scroll, leftDead, 216);
        chamber(g, x + 206, x + 243, fy, 194, life[RIGHT], track.lag[RIGHT], false, scroll, rightDead, 216);
        int centerV = sealed ? 212 : 203;
        int centerEdge = sealed ? -1 : 218;
        chamber(g, x + 72, x + 109, fy, centerV, life[CENTER], track.lag[CENTER], true, scroll, centerDead, centerEdge);
        chamber(g, x + 144, x + 181, fy, centerV, life[CENTER], track.lag[CENTER], false, scroll, centerDead, centerEdge);

        int ls = shake(track, LEFT);
        int rs = shake(track, RIGHT);
        int cs = shake(track, CENTER);
        blit(g, x + 50 + ls, y + 18, leftDead ? 20 : 0, 174, 20, 20);
        blit(g, x + 184 + rs, y + 18, rightDead ? 60 : 40, 174, 20, 20);
        int centerU = centerDead ? 60 : (sealed ? 0 : 30);
        blit(g, x + 112 + cs, y + 7, centerU, 144, 30, 30);

        if (track.flash[LEFT] > 0 && !leftDead) flash(g, x + 50 + ls, y + 18, 80, 174, 20, track.flash[LEFT], alpha);
        if (track.flash[RIGHT] > 0 && !rightDead) flash(g, x + 184 + rs, y + 18, 100, 174, 20, track.flash[RIGHT], alpha);
        if (track.flash[CENTER] > 0 && !centerDead) flash(g, x + 112 + cs, y + 7, sealed ? 90 : 120, 144, 30, track.flash[CENTER], alpha);

        blit(g, x, y, 0, 0, 254, 50);

        if (rage) {
            float pulse = 0.7f + 0.3f * Mth.sin(time * 0.25f);
            g.setColor(1.0f, 1.0f, 1.0f, pulse * alpha);
            blit(g, x + 99, y - 6, 200, 100, 56, 56);
            g.setColor(1.0f, 1.0f, 1.0f, alpha);
        }

        blit(g, x + 43 + ls, y + 11, leftDead ? 166 : 132, 100, 34, 34);
        blit(g, x + 177 + rs, y + 11, rightDead ? 166 : 132, 100, 34, 34);
        blit(g, x + 105 + cs, y, centerDead ? 88 : (rage ? 44 : 0), 100, 44, 44);

        if (sealed && !centerDead) {
            blit(g, x + 107, y + 2, leftDead ? 80 : 0, 194, 40, 40);
            blit(g, x + 107, y + 2, rightDead ? 120 : 40, 194, 40, 40);
        }

        for (Leaf leaf : track.leaves) {
            float a = alpha * Mth.clamp((leaf.life - leaf.age) / 8.0f, 0.0f, 1.0f);
            g.setColor(1.0f, 1.0f, 1.0f, a);
            blit(g, x + Math.round(leaf.x), y + Math.round(leaf.y), leaf.u, 208, 6, 6);
        }
        g.setColor(1.0f, 1.0f, 1.0f, alpha);

        Component name = Component.translatable("entity.ow.plant_empress");
        int textWidth = font.width(name);
        int midWidth = textWidth + 10;
        int bannerX = x + FRAME_WIDTH / 2 - (midWidth + 24) / 2;
        int bannerY = y + 42;
        blit(g, bannerX, bannerY, 216, 194, 12, 14);
        int cursor = bannerX + 12;
        int remain = midWidth;
        while (remain > 0) {
            int w = Math.min(16, remain);
            blit(g, cursor, bannerY, 228, 194, w, 14);
            cursor += w;
            remain -= w;
        }
        blit(g, cursor, bannerY, 244, 194, 12, 14);
        if (alpha > 0.1f) {
            int color = (rage ? TEXT_RAGE_COLOR : TEXT_COLOR) | ((int) (alpha * 255) << 24);
            g.drawString(font, name, bannerX + 12 + 5, bannerY + 3, color, true);
        }
    }

    private static void flash(GuiGraphics g, int x, int y, int u, int v, int size, int ticks, float alpha) {
        g.setColor(1.0f, 1.0f, 1.0f, alpha * ticks / (float) FLASH_TICKS);
        blit(g, x, y, u, v, size, size);
        g.setColor(1.0f, 1.0f, 1.0f, alpha);
    }
}
