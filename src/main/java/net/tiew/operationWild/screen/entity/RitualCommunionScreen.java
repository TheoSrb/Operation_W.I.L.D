package net.tiew.operationWild.screen.entity;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.tiew.operationWild.component.SoulData;
import net.tiew.operationWild.entity.OWEntity;
import net.tiew.operationWild.entity.client.render.OWEntityRenderer;
import net.tiew.operationWild.item.OWItems;
import net.tiew.operationWild.networking.OWNetworkHandler;
import net.tiew.operationWild.networking.packets.to_server.StartRitualPacket;

/**
 * Écran de Communion : la "fiche" du compagnon mort + lancement du Rituel de Communion.
 * Affiche un aperçu 3D, les caractéristiques restaurables, le coût (XP + amulette) et les
 * conditions. Le bouton n'est actif que si les conditions client sont remplies ; le serveur
 * re-valide à la réception du {@link StartRitualPacket}.
 */
public class RitualCommunionScreen extends Screen {

    private static final int PANEL_W = 256;
    private static final int PANEL_H = 188;
    private static final int ACCENT = 0x86DBFF;

    private final SoulData data;
    private OWEntity preview;
    private float ticks = 0f;

    public RitualCommunionScreen(SoulData data) {
        super(Component.translatable("screen.ow.ritual_communion"));
        this.data = data;
    }

    @Override
    protected void init() {
        // Construit l'aperçu (entité-modèle temporaire jamais ajoutée au monde).
        if (this.minecraft != null && this.minecraft.level != null && data.entityType() != null) {
            EntityType<?> type = BuiltInRegistries.ENTITY_TYPE.get(data.entityType());
            if (type != null && type.create(this.minecraft.level) instanceof OWEntity ow) {
                try {
                    ow.setVariant(ow, data.variant());
                    if (data.skinIndex() != 0) ow.changeSkinSilent(data.skinIndex());
                } catch (Exception ignored) {
                    // Restauration cosmétique non critique : on n'empêche jamais l'ouverture de l'écran.
                }
                ow.setScale(data.scale());
                this.preview = ow;
            }
        }

        int left = (this.width - PANEL_W) / 2;
        int top = (this.height - PANEL_H) / 2;

        boolean canStart = canStart();
        Button start = Button.builder(
                        Component.translatable("screen.ow.ritual_start"),
                        b -> {
                            OWNetworkHandler.sendToServer(new StartRitualPacket());
                            this.onClose();
                        })
                .bounds(left + PANEL_W / 2 - 70, top + PANEL_H - 26, 140, 20)
                .build();
        start.active = canStart;
        this.addRenderableWidget(start);

        this.addRenderableWidget(Button.builder(Component.translatable("gui.cancel"), b -> this.onClose())
                .bounds(left + PANEL_W - 54, top + 6, 48, 16).build());
    }

    /** Couleur par espèce (même charte que l'item Âme). */
    private int speciesColor() {
        String path = data.entityType() == null ? "" : data.entityType().getPath();
        return switch (path) {
            case "tiger" -> 0xC47037;
            case "boa" -> 0x838549;
            case "peacock" -> 0x464BC1;
            case "kodiak" -> 0x7D4A05;
            case "crocodile" -> 0x5E7242;
            case "orca" -> 0x3A4A5A;
            default -> ACCENT;
        };
    }

    private boolean canStart() {
        Player player = this.minecraft != null ? this.minecraft.player : null;
        if (player == null) return false;
        if (!data.ownerUuid().equals(player.getUUID())) return false;
        ItemStack chest = player.getItemBySlot(EquipmentSlot.CHEST);
        if (chest.isEmpty() || !chest.is(OWItems.RESURRECTION_AMULET.get())) return false;
        return player.experienceLevel >= data.xpLevelCost();
    }

    @Override
    public void renderBackground(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        // Pas de flou (le renderBackground vanilla applique l'effet de flou du menu en 1.21) :
        // on assombrit simplement la scène d'un voile net.
        g.fill(0, 0, this.width, this.height, 0xB0050608);
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(g, mouseX, mouseY, partialTick);
        ticks += partialTick;

        int left = (this.width - PANEL_W) / 2;
        int top = (this.height - PANEL_H) / 2;

        // Panneau.
        g.fill(left, top, left + PANEL_W, top + PANEL_H, 0xE0101418);
        g.renderOutline(left, top, PANEL_W, PANEL_H, 0xFF2A3A44);
        g.drawCenteredString(this.font, this.title, this.width / 2, top + 8, ACCENT);

        // Aperçu à gauche (cadre + clipping pour que le modèle ne déborde jamais sur les textes).
        int bx1 = left + 14, by1 = top + 26, bx2 = left + 110, by2 = top + 108;
        if (this.preview != null) {
            g.fill(bx1, by1, bx2, by2, 0x66000000);
            g.renderOutline(bx1, by1, bx2 - bx1, by2 - by1, 0xFF2A3A44);
            boolean prev = OWEntityRenderer.SUPPRESS_INFO_IN_GUI;
            OWEntityRenderer.SUPPRESS_INFO_IN_GUI = true;
            // Le boa a sa queue faite d'entités séparées (absentes du GUI) : on demande au modèle
            // d'afficher le corps entier, comme dans OWInventoryScreen / l'écran des cosmétiques.
            boolean isBoa = "BoaEntity".equals(this.preview.getClass().getSimpleName());
            int scale = Mth.clamp((int) (30 / Math.max(0.7f, data.scale())), 10, 38);
            if (isBoa) scale = Math.round(scale * 0.85f);
            int up = isBoa ? 12 : 0; // remonte un peu pour que le long corps tienne dans le cadre
            net.tiew.operationWild.entity.client.model.BoaModel.RENDER_FULL_BODY = true;
            net.tiew.operationWild.entity.client.model.CrocodileModel.RENDER_FULL_BODY = true;
            g.enableScissor(bx1, by1, bx2, by2);
            try {
                // L'entité suit la souris (comme dans l'inventaire).
                InventoryScreen.renderEntityInInventoryFollowsMouse(
                        g, bx1, by1 - up, bx2, by2 - up, scale, 0.06f, mouseX, mouseY, this.preview);
            } finally {
                g.disableScissor();
                net.tiew.operationWild.entity.client.model.BoaModel.RENDER_FULL_BODY = false;
                net.tiew.operationWild.entity.client.model.CrocodileModel.RENDER_FULL_BODY = false;
                OWEntityRenderer.SUPPRESS_INFO_IN_GUI = prev;
            }
        }

        // Carte d'identité à droite.
        int tx = left + 122;
        int ty = top + 28;
        int line = 13;
        Component species = data.entityType() != null
                ? Component.translatable("entity.ow." + data.entityType().getPath()) : Component.literal("?");
        int speciesCol = speciesColor();
        g.drawString(this.font, species.copy().withStyle(s -> s.withBold(true).withColor(speciesCol)), tx, ty, speciesCol, true);
        int yy = ty + line;
        if (!data.nickname().isEmpty()) {
            g.drawString(this.font, Component.literal("\"" + data.nickname() + "\"").withStyle(s -> s.withItalic(true)), tx, yy, 0xC8C8C8, true);
            yy += line;
        }
        g.drawString(this.font, Component.translatable("screen.ow.ritual_level", data.level()), tx, yy, 0xFFFFFF, true);
        g.drawString(this.font, Component.translatable(data.male() ? "tooltip.genderMale" : "tooltip.genderFemale"), tx, yy + line, 0xFFFFFF, true);
        g.drawString(this.font, Component.translatable("screen.ow.ritual_hp", Math.round(data.maxHealth() * 2) / 2.0), tx, yy + line * 2, 0xFF7A7A, true);
        g.drawString(this.font, Component.translatable("screen.ow.ritual_dmg", Math.round(data.damage() * 10) / 10.0), tx, yy + line * 3, 0xFFC36B, true);
        g.drawString(this.font, Component.translatable("screen.ow.ritual_owner", data.ownerName()), tx, yy + line * 4, 0xB0B0B0, true);

        // Coût + vagues (sous l'aperçu, sans chevauchement).
        int cy = top + 116;
        g.drawString(this.font, Component.translatable("screen.ow.ritual_cost_xp", data.xpLevelCost()), left + 14, cy, 0x9BE36A, true);
        g.drawString(this.font, Component.translatable("screen.ow.ritual_cost_amulet"), left + 14, cy + 11, 0xC0C0C0, true);
        g.drawString(this.font, Component.translatable("screen.ow.ritual_waves", data.waveCount()), left + 14, cy + 22, 0xFF8888, true);

        if (!canStart()) {
            g.drawCenteredString(this.font, Component.translatable("screen.ow.ritual_conditions"),
                    this.width / 2, top + PANEL_H - 44, 0xD94747);
        }

        super.render(g, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
