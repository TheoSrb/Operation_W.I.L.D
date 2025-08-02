package net.tiew.operationWild.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.client.event.RenderGuiLayerEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.tiew.operationWild.OperationWild;
import net.tiew.operationWild.effect.OWEffects;

import java.util.Random;

public class HeartRenderHandler {

    private int lastHealth;
    private int displayHealth;
    private long lastHealthTime;
    private long healthBlinkTime;
    private int tickCount;
    private Random random = new Random();

    public static void register() {
        NeoForge.EVENT_BUS.register(new HeartRenderHandler());
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public void onRenderGuiLayer(RenderGuiLayerEvent.Post event) {
        if (!event.getName().equals(VanillaGuiLayers.PLAYER_HEALTH)) {
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();
        Player player = minecraft.player;

        if (player == null || player.isCreative()) return;

        this.tickCount = player.tickCount;

        if (player.hasEffect(OWEffects.VENOM_EFFECT.getDelegate())) {
            renderHearts(event.getGuiGraphics(), player, minecraft.getWindow().getGuiScaledWidth(), minecraft.getWindow().getGuiScaledHeight());
        } else if (player.hasEffect(OWEffects.WATER_PRESSURE_EFFECT.getDelegate())) {
            renderHearts(event.getGuiGraphics(), player, minecraft.getWindow().getGuiScaledWidth(), minecraft.getWindow().getGuiScaledHeight());
        }
    }

    private void renderHearts(GuiGraphics guiGraphics, Player player, int screenWidth, int screenHeight) {
        Minecraft minecraft = Minecraft.getInstance();

        int left = screenWidth / 2 - 91;
        int top = screenHeight - 39;

        boolean hardcore = player.level().getLevelData().isHardcore();
        int health = Mth.ceil(player.getHealth());
        int maxHealth = Mth.ceil(player.getMaxHealth());
        int absorption = Mth.ceil(player.getAbsorptionAmount());

        boolean blinking = this.healthBlinkTime > (long) this.tickCount && (this.healthBlinkTime - (long) this.tickCount) / 3L % 2L == 1L;

        long currentTime = Util.getMillis();
        if (health < this.lastHealth && player.invulnerableTime > 0) {
            this.lastHealthTime = currentTime;
            this.healthBlinkTime = (long) (this.tickCount + 20);
        } else if (health > this.lastHealth && player.invulnerableTime > 0) {
            this.lastHealthTime = currentTime;
            this.healthBlinkTime = (long) (this.tickCount + 10);
        }

        if (currentTime - this.lastHealthTime > 1000L) {
            this.lastHealth = health;
            this.displayHealth = health;
            this.lastHealthTime = currentTime;
        }

        this.lastHealth = health;
        int displayHealth = this.displayHealth;

        float maxHealthFloat = Math.max((float) player.getAttributeValue(Attributes.MAX_HEALTH), (float) Math.max(displayHealth, health));
        int totalHearts = Mth.ceil((maxHealthFloat + (float) absorption) / 2.0F / 10.0F);
        int heartSpacing = Math.max(10 - (totalHearts - 2), 3);

        int regenHeart = -1;
        if (player.hasEffect(MobEffects.REGENERATION)) {
            regenHeart = this.tickCount % Mth.ceil(maxHealthFloat + 5.0F);
        }

        this.random.setSeed((long) (this.tickCount * 312871));

        int normalHearts = Mth.ceil((double) maxHealthFloat / 2.0);
        int absorptionHearts = Mth.ceil((double) absorption / 2.0);

        for (int i = normalHearts + absorptionHearts - 1; i >= 0; --i) {
            int row = i / 10;
            int col = i % 10;
            int heartX = left + col * 8;
            int heartY = top - row * heartSpacing;

            if (health + absorption <= 4) {
                heartY += this.random.nextInt(2);
            }

            if (i < normalHearts && i == regenHeart) {
                heartY -= 2;
            }

            this.renderHeart(guiGraphics, HeartType.CONTAINER, heartX, heartY, hardcore, blinking, false);

            int heartIndex = i * 2;
            boolean isAbsorption = i >= normalHearts;

            if (isAbsorption) {
                int absIndex = heartIndex - (normalHearts * 2);
                if (absIndex < absorption) {
                    boolean isHalf = absIndex + 1 == absorption;
                    //this.renderHeart(guiGraphics, HeartType.ABSORBING, heartX, heartY, hardcore, false, isHalf);
                }
            }

            if (blinking && heartIndex < displayHealth) {
                boolean isHalf = heartIndex + 1 == displayHealth;
                HeartType heartType = HeartType.forPlayer(player);
                this.renderHeart(guiGraphics, heartType, heartX, heartY, hardcore, true, isHalf);
            }

            if (heartIndex < health) {
                boolean isHalf = heartIndex + 1 == health;
                HeartType heartType = HeartType.forPlayer(player);
                this.renderHeart(guiGraphics, heartType, heartX, heartY, hardcore, false, isHalf);
            }
        }
    }

    private void renderHeart(GuiGraphics guiGraphics, HeartType heartType, int x, int y, boolean hardcore, boolean blinking, boolean half) {
        RenderSystem.enableBlend();
        ResourceLocation texture = heartType.getTexture(hardcore, half, blinking);
        guiGraphics.blit(texture, x, y, 0, 0, 9, 9, 9, 9);
        RenderSystem.disableBlend();
    }

    public enum HeartType {
        CONTAINER(ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "textures/gui/container.png"),
                ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "textures/gui/container_blinking.png"),
                ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "textures/gui/container.png"),
                ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "textures/gui/container_blinking.png")),
        VENOMOUS(ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "textures/gui/venomous_full.png"),
                ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "textures/gui/venomous_full_blinking.png"),
                ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "textures/gui/venomous_half.png"),
                ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "textures/gui/venomous_half_blinking.png")),
        WATER_PRESSURE(ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "textures/gui/water_pressure_full.png"),
                ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "textures/gui/water_pressure_full_blinking.png"),
                ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "textures/gui/water_pressure_half.png"),
                ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "textures/gui/water_pressure_half_blinking.png"));

        private final ResourceLocation texture;
        private final ResourceLocation blinkingTexture;
        private final ResourceLocation halfTexture;
        private final ResourceLocation halfBlinkingTexture;

        HeartType(ResourceLocation texture, ResourceLocation blinkingTexture, ResourceLocation halfTexture, ResourceLocation halfBlinkingTexture) {
            this.texture = texture;
            this.blinkingTexture = blinkingTexture;
            this.halfTexture = halfTexture;
            this.halfBlinkingTexture = halfBlinkingTexture;
        }

        public ResourceLocation getTexture(boolean hardcore, boolean half, boolean blinking) {
            if (half && blinking) {
                return this.halfBlinkingTexture;
            } else if (half) {
                return this.halfTexture;
            } else if (blinking) {
                return this.blinkingTexture;
            } else {
                return this.texture;
            }
        }

        public static HeartType forPlayer(Player player) {
            HeartType heartType = null;
            if (player.hasEffect(OWEffects.VENOM_EFFECT.getDelegate())) {
                heartType = VENOMOUS;
            } else if (player.hasEffect(OWEffects.WATER_PRESSURE_EFFECT.getDelegate())) {
                heartType = WATER_PRESSURE;
            }
            return heartType;
        }
    }
}