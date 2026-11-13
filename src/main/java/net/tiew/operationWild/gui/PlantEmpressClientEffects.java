package net.tiew.operationWild.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RenderGuiLayerEvent;
import net.neoforged.neoforge.client.event.SelectMusicEvent;
import net.neoforged.neoforge.client.event.ViewportEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;
import net.tiew.operationWild.ClientConfig;
import net.tiew.operationWild.OperationWild;
import net.tiew.operationWild.entity.bosses.PlantEmpressEntity;
import net.tiew.operationWild.entity.bosses.plantempress.EmpressTimings;
import net.tiew.operationWild.sound.OWSounds;

import java.util.List;

@EventBusSubscriber(modid = OperationWild.MOD_ID, bus = EventBusSubscriber.Bus.GAME, value = Dist.CLIENT)
public final class PlantEmpressClientEffects {

    private static final ResourceLocation VIGNETTE = ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "textures/gui/hud/mount_alert_vignette.png");
    private static final double SEARCH_RADIUS = 96.0;
    private static final double MUSIC_RADIUS = 80.0;

    private static PlantEmpressEntity active;
    private static BossMusic music;
    private static int musicBoss = -1;
    private static int defeatedBoss = -1;
    private static int musicStartTick;
    private static int clientTicks;
    private static int suppressUntil = -1;

    private PlantEmpressClientEffects() {
    }

    private static final class BossMusic extends AbstractTickableSoundInstance {
        private float target = 1f;
        private float current = 0.02f;
        private final float fadeIn;

        BossMusic(SoundEvent sound, SoundSource source, float fadeIn) {
            super(sound, source, SoundInstance.createUnseededRandom());
            this.looping = true;
            this.delay = 0;
            this.relative = true;
            this.attenuation = Attenuation.NONE;
            this.fadeIn = fadeIn;
            this.volume = current;
        }

        void fadeOut() {
            this.target = 0f;
        }

        boolean fading() {
            return this.target <= 0f;
        }

        @Override
        public void tick() {
            float step = target > current ? fadeIn : 0.025f;
            current = Mth.approach(current, target, step);
            this.volume = Math.max(0.001f, current);
            if (target <= 0f && current <= 0.002f) this.stop();
        }
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft minecraft = Minecraft.getInstance();
        Player player = minecraft.player;
        clientTicks++;
        if (player == null || minecraft.level == null) {
            active = null;
            stopMusic();
            return;
        }
        List<PlantEmpressEntity> bosses = minecraft.level.getEntitiesOfClass(PlantEmpressEntity.class,
                player.getBoundingBox().inflate(SEARCH_RADIUS), boss -> !boss.isRemoved());
        PlantEmpressEntity nearest = null;
        double best = Double.MAX_VALUE;
        for (PlantEmpressEntity boss : bosses) {
            double d = boss.distanceToSqr(player);
            if (d < best) {
                best = d;
                nearest = boss;
            }
        }
        active = nearest;

        boolean wantMusic = nearest != null && Math.sqrt(best) < MUSIC_RADIUS && nearest.getPhase() != PlantEmpressEntity.PHASE_DORMANT
                && nearest.getPhase() != PlantEmpressEntity.PHASE_DYING && !nearest.isDeadOrDying();
        boolean bossNearby = nearest != null && Math.sqrt(best) < MUSIC_RADIUS && nearest.getPhase() != PlantEmpressEntity.PHASE_DORMANT;
        if (bossNearby) suppressUntil = Math.max(suppressUntil, clientTicks + 40);
        if (clientTicks < suppressUntil) minecraft.getMusicManager().stopPlaying();
        if (wantMusic) {
            boolean lost = music != null && !music.fading() && clientTicks - musicStartTick > 60
                    && !minecraft.getSoundManager().isActive(music);
            if (music != null && musicBoss != nearest.getId() && !music.fading()) music.fadeOut();
            if (music == null || music.fading() || music.isStopped() || lost) {
                if (music != null && !music.isStopped()) minecraft.getSoundManager().stop(music);
                boolean fromSpawn = nearest.getPhase() == PlantEmpressEntity.PHASE_SPAWN;
                minecraft.getMusicManager().stopPlaying();
                music = new BossMusic(OWSounds.PLANT_EMPRESS_THEME.get(), musicSource(minecraft), fromSpawn ? 0.05f : 0.02f);
                musicBoss = nearest.getId();
                musicStartTick = clientTicks;
                minecraft.getSoundManager().play(music);
            }
        } else if (music != null && !music.fading()) {
            music.fadeOut();
        }

        if (nearest != null && (nearest.getPhase() == PlantEmpressEntity.PHASE_DYING || nearest.isDeadOrDying())
                && defeatedBoss != nearest.getId() && Math.sqrt(best) < MUSIC_RADIUS) {
            defeatedBoss = nearest.getId();
            suppressUntil = clientTicks + 600;
            minecraft.getSoundManager().play(new SimpleSoundInstance(OWSounds.PLANT_EMPRESS_DEFEATED_THEME.get().getLocation(),
                    musicSource(minecraft), 1f, 1f, SoundInstance.createUnseededRandom(), false, 0, SoundInstance.Attenuation.NONE,
                    0, 0, 0, true));
        }
    }

    private static SoundSource musicSource(Minecraft minecraft) {
        return minecraft.options.getSoundSourceVolume(SoundSource.MUSIC) > 0.05f ? SoundSource.MUSIC : SoundSource.RECORDS;
    }

    private static void stopMusic() {
        if (music != null) {
            Minecraft.getInstance().getSoundManager().stop(music);
            music = null;
        }
    }

    @SubscribeEvent(priority = net.neoforged.bus.api.EventPriority.HIGHEST)
    public static void onSelectMusic(SelectMusicEvent event) {
        if (clientTicks < suppressUntil || (music != null && !music.isStopped())) {
            event.overrideMusic(null);
        }
    }


    private static float attenuation(PlantEmpressEntity boss) {
        Player player = Minecraft.getInstance().player;
        if (player == null) return 0f;
        double d = player.distanceTo(boss);
        return (float) Mth.clamp(1.0 - (d - 12.0) / 50.0, 0.12, 1.0);
    }

    private static float spawnTime(PlantEmpressEntity boss, float partialTick) {
        if (boss.getPhase() != PlantEmpressEntity.PHASE_SPAWN) return -1f;
        return boss.level().getGameTime() + partialTick - boss.getPhaseStart();
    }

    private static float shakeAmount(PlantEmpressEntity boss, float partialTick) {
        float amount = 0f;
        float t = spawnTime(boss, partialTick);
        if (t >= 0f) {
            if (t < 130f) amount += 0.25f + 2.4f * (t / 130f) * (t / 130f);
            else if (t < EmpressTimings.SPAWN_BURST) amount += 0.15f;
        }
        if (boss.getPhase() == PlantEmpressEntity.PHASE_DESPERATION) amount += 1.1f;
        for (float[] shake : boss.clientShakes) {
            float age = (boss.tickCount + partialTick - shake[0]) / shake[2];
            if (age < 0f || age > 1f) continue;
            float fade = 1f - age;
            amount += shake[1] * fade * fade;
        }
        return amount * attenuation(boss);
    }

    @SubscribeEvent
    public static void onCamera(ViewportEvent.ComputeCameraAngles event) {
        PlantEmpressEntity boss = active;
        if (boss == null) return;
        float amount = shakeAmount(boss, (float) event.getPartialTick()) * ClientConfig.CAMERA_SHAKE_INTENSITY.get().floatValue();
        if (amount <= 0.01f) return;
        double t = (boss.tickCount + event.getPartialTick()) * 50.0;
        event.setYaw(event.getYaw() + amount * 0.6f * (float) (Math.sin(t / 37.0) + Math.sin(t / 13.0) * 0.4));
        event.setPitch(event.getPitch() + amount * 0.5f * (float) (Math.cos(t / 31.0) + Math.sin(t / 11.0) * 0.35));
        event.setRoll(event.getRoll() + amount * 0.45f * (float) Math.sin(t / 23.0));
    }

    @SubscribeEvent
    public static void onFog(ViewportEvent.RenderFog event) {
        PlantEmpressEntity boss = active;
        if (boss == null) return;
        float partial = (float) event.getPartialTick();
        float density = fogDensity(boss, partial);
        float haze = arenaHaze(boss, partial);
        if (density <= 0.01f && haze <= 0.01f) return;
        float far = event.getFarPlaneDistance();
        if (haze > 0.01f) far = Mth.lerp(haze, far, boss.isRaging() ? 52f : 70f);
        if (density > 0.01f) far = Mth.lerp(density, far, 26f);
        event.setFarPlaneDistance(Math.min(event.getFarPlaneDistance(), far));
        event.setNearPlaneDistance(Math.min(event.getNearPlaneDistance(), far * 0.12f));
        event.setCanceled(true);
    }

    @SubscribeEvent
    public static void onFogColor(ViewportEvent.ComputeFogColor event) {
        PlantEmpressEntity boss = active;
        if (boss == null) return;
        float partial = (float) event.getPartialTick();
        float haze = arenaHaze(boss, partial) * 0.8f;
        if (haze > 0.01f) {
            float rage = boss.isRaging() ? 0.25f : 0f;
            event.setRed(Mth.lerp(haze, event.getRed(), 0.30f + rage * 0.3f));
            event.setGreen(Mth.lerp(haze, event.getGreen(), 0.56f - rage * 0.12f));
            event.setBlue(Mth.lerp(haze, event.getBlue(), 0.20f));
        }
        float density = fogDensity(boss, partial) * 0.85f;
        if (density <= 0.01f) return;
        event.setRed(Mth.lerp(density, event.getRed(), 0.10f));
        event.setGreen(Mth.lerp(density, event.getGreen(), 0.18f));
        event.setBlue(Mth.lerp(density, event.getBlue(), 0.07f));
    }

    private static float arenaHaze(PlantEmpressEntity boss, float partialTick) {
        int phase = boss.getPhase();
        boolean fighting = phase == PlantEmpressEntity.PHASE_FIGHT || phase == PlantEmpressEntity.PHASE_TRANSITION
                || phase == PlantEmpressEntity.PHASE_DESPERATION || phase == PlantEmpressEntity.PHASE_BURROW
                || phase == PlantEmpressEntity.PHASE_AWAKEN;
        float ramp = 0f;
        if (fighting) {
            ramp = 1f;
        } else if (phase == PlantEmpressEntity.PHASE_SPAWN) {
            float t = boss.level().getGameTime() + partialTick - boss.getPhaseStart();
            ramp = Mth.clamp((t - EmpressTimings.SPAWN_BURST) / 80f, 0f, 1f);
        } else if (phase == PlantEmpressEntity.PHASE_DYING) {
            float t = boss.level().getGameTime() + partialTick - boss.getPhaseStart();
            ramp = Mth.clamp(1f - t / 200f, 0f, 1f);
        }
        return ramp * attenuation(boss);
    }

    private static float fogDensity(PlantEmpressEntity boss, float partialTick) {

        float a = attenuation(boss);
        float t = spawnTime(boss, partialTick);
        if (t >= 0f) {
            if (t < EmpressTimings.SPAWN_BURST) return Mth.clamp(t / 110f, 0f, 1f) * a;
            return Mth.clamp(1f - (t - EmpressTimings.SPAWN_BURST) / 90f, 0f, 1f) * a;
        }
        if (boss.getPhase() == PlantEmpressEntity.PHASE_DESPERATION) {
            float d = boss.level().getGameTime() + partialTick - boss.getPhaseStart();
            return Mth.clamp(Math.min(d / 20f, (EmpressTimings.DESPERATION_TICKS - d) / 20f), 0f, 0.7f) * a;
        }
        return 0f;
    }

    @SubscribeEvent
    public static void onGui(RenderGuiLayerEvent.Post event) {
        if (!event.getName().equals(VanillaGuiLayers.CAMERA_OVERLAYS)) return;
        PlantEmpressEntity boss = active;
        if (boss == null || Minecraft.getInstance().options.hideGui) return;
        float partialTick = event.getPartialTick().getGameTimeDeltaPartialTick(true);
        float a = attenuation(boss);
        GuiGraphics g = event.getGuiGraphics();
        float t = spawnTime(boss, partialTick);
        if (t >= 0f) {
            float green = t < EmpressTimings.SPAWN_BURST ? Mth.clamp(t / 120f, 0f, 1f) * 0.75f
                    : Mth.clamp(1f - (t - EmpressTimings.SPAWN_BURST) / 140f, 0f, 1f) * 0.75f;
            float beat = 0f;
            if (t > 55 && t < 140) beat = 0.2f * Math.max(0f, Mth.sin(t * (0.35f + t / 400f)));
            vignette(g, 0x1E3A10, (green + beat) * a, 1.15f);
        }
        for (float[] shake : boss.clientShakes) {
            if (shake.length < 4) continue;
            float age = (boss.tickCount + partialTick - shake[0]) / shake[2];
            if (age < 0f || age > 1f) continue;
            vignette(g, 0x9A1A0C, (1f - age) * 0.55f * a, 1.1f);
        }
        if (boss.getPhase() == PlantEmpressEntity.PHASE_DESPERATION) {
            float pulse = 0.3f + 0.15f * Mth.sin((boss.tickCount + partialTick) * 0.3f);
            vignette(g, 0x7FA21E, pulse * a, 1.2f);
        } else if (boss.isRaging() && boss.getPhase() == PlantEmpressEntity.PHASE_FIGHT) {
            float pulse = 0.12f + 0.06f * Mth.sin((boss.tickCount + partialTick) * 0.12f);
            vignette(g, 0x6E140A, pulse * a, 1.35f);
        }
    }

    private static void vignette(GuiGraphics g, int rgb, float alpha, float spread) {
        if (alpha <= 0.003f) return;
        int screenWidth = g.guiWidth();
        int screenHeight = g.guiHeight();
        int width = Math.round(screenWidth * spread);
        int height = Math.round(screenHeight * spread);
        int x = (screenWidth - width) / 2;
        int y = (screenHeight - height) / 2;
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        g.setColor(((rgb >> 16) & 0xFF) / 255f, ((rgb >> 8) & 0xFF) / 255f, (rgb & 0xFF) / 255f, Mth.clamp(alpha, 0f, 1f));
        g.blit(VIGNETTE, x, y, width, height, 0f, 0f, 256, 256, 256, 256);
        g.setColor(1f, 1f, 1f, 1f);
        RenderSystem.disableBlend();
    }
}
