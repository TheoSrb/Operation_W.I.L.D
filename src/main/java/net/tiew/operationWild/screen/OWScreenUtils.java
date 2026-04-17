package net.tiew.operationWild.screen;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.tiew.operationWild.OperationWild;
import net.tiew.operationWild.screen.entity.OWSkinsInterface;

import java.util.Collections;
import java.util.List;

public class OWScreenUtils {

    private static final ResourceLocation ICONS_LOCATION =
            ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "textures/gui/mob_types.png");

    @OnlyIn(Dist.CLIENT)
    public static class ButtonListWidget
            extends ContainerObjectSelectionList<ButtonListWidget.BaseEntry> {

        private final OWSkinsInterface parentScreen;

        public ButtonListWidget(Minecraft minecraft, int width, int height,
                                int y0, int itemHeight, OWSkinsInterface parentScreen) {
            super(minecraft, width, height, y0, itemHeight);
            this.parentScreen = parentScreen;
        }

        public void addButtonEntry(ButtonEntry entry)     { this.addEntry(entry); }
        public void addCategoryEntry(CategoryEntry entry) { this.addEntry(entry); }
        public void clearEntries()                        { this.children().clear(); }

        @Override public int getRowWidth()               { return this.width - 6; }
        @Override protected int getScrollbarPosition()   { return this.getX() + this.width + 1; }

        // ─────────────────────────────────────────────────────────────────
        // BaseEntry — classe parente commune à ButtonEntry et CategoryEntry
        // ─────────────────────────────────────────────────────────────────
        public abstract static class BaseEntry
                extends ContainerObjectSelectionList.Entry<BaseEntry> {}

        // ─────────────────────────────────────────────────────────────────
        // CategoryEntry — header de groupe de rareté (flat sombre)
        // ─────────────────────────────────────────────────────────────────
        public static class CategoryEntry extends BaseEntry {

            private final String label;
            private final int color;

            public CategoryEntry(String label, int color) {
                this.label = label;
                this.color = color;
            }

            @Override
            public void render(GuiGraphics g, int index, int top, int left,
                               int width, int height,
                               int mouseX, int mouseY, boolean hovered, float partial) {
                int bx = left - 4;
                int by = top;
                int bw = width + 4;

                // Fond sombre
                g.fill(bx, by, bx + bw, by + height, 0xFF060608);

                // Ligne de couleur en bas (2px)
                g.fill(bx, by + height - 2, bx + bw, by + height,
                        (0xBB << 24) | (color & 0x00FFFFFF));

                // Label centré
                int textW = Minecraft.getInstance().font.width(label);
                g.drawString(Minecraft.getInstance().font,
                        label,
                        bx + bw / 2 - textW / 2,
                        by + (height - 8) / 2 - 1,
                        color, false);
            }

            @Override public List<? extends GuiEventListener> children()   { return Collections.emptyList(); }
            @Override public List<? extends NarratableEntry> narratables()  { return Collections.emptyList(); }
            @Override public boolean mouseClicked(double mx, double my, int btn) { return false; }
        }

        // ─────────────────────────────────────────────────────────────────
        // ButtonEntry — aspect vanilla + barre rareté gauche + highlight
        // ─────────────────────────────────────────────────────────────────
        public static class ButtonEntry extends BaseEntry {

            private final Button button;
            private final int skinIndex;
            private final OWSkinsInterface parentScreen;

            public ButtonEntry(Button button, int skinIndex, OWSkinsInterface parentScreen) {
                this.button       = button;
                this.skinIndex    = skinIndex;
                this.parentScreen = parentScreen;
            }

            @Override
            public void render(GuiGraphics g, int index, int top, int left,
                               int width, int height,
                               int mouseX, int mouseY, boolean hovered, float partial) {

                // Mise à jour bounds pour la détection de clics (invisible mais cliquable)
                this.button.setX(left - 4);
                this.button.setY(top + 2);

                int bx = left - 4;
                int by = top + 2;
                int bw = (int)(parentScreen.imageWidth * 0.8);
                int bh = 20;

                boolean locked   = parentScreen.isLocked(skinIndex);
                boolean selected = parentScreen.selectedSkinIndex == skinIndex;
                boolean isHov    = mouseX >= bx && mouseX <= bx + bw
                        && mouseY >= by && mouseY <= by + bh;
                int rc = parentScreen.getRarityColor(skinIndex);

                // ── Fond plat custom (sans la bordure noire vanilla) ────────
                // Déverrouillé = clair, verrouillé = sombre
                int bgColor = locked   ? (isHov ? 0xFF606060 : 0xFF585858)
                        : selected ? 0xFF969696
                        : isHov    ? 0xFFAAAAAA
                        : 0xFF979797;
                g.fill(bx, by, bx + bw, by + bh, bgColor);

                // ── Léger reflet en haut (1px) ───────────────────────────
                g.fill(bx, by, bx + bw, by + 1, 0x44FFFFFF);

                // ── Barre de rareté gauche (3px) ──────────────────────────
                g.fill(bx, by, bx + 3, by + bh, locked ? 0xFF555555 : (rc | 0xFF000000));

                // ── Contenu (texte ou icône verrou) ────────────────────────
                if (locked) {
                    renderLockedOverlay(g, bx, by, bh);
                } else {
                    renderSkinName(g, bx, by, bw, bh, rc);
                }

                // ── Highlight de sélection (bordure couleur rareté) ────────
                if (selected) {
                    g.fill(bx,          by,          bx + bw, by + 1,      (0xEE << 24) | (rc & 0x00FFFFFF));
                    g.fill(bx,          by + bh - 1, bx + bw, by + bh,     (0xEE << 24) | (rc & 0x00FFFFFF));
                    g.fill(bx,          by,          bx + 1,  by + bh,     (0xEE << 24) | (rc & 0x00FFFFFF));
                    g.fill(bx + bw - 1, by,          bx + bw, by + bh,     (0xEE << 24) | (rc & 0x00FFFFFF));
                }
            }

            /** Affiche le nom du skin avec réduction de taille si ça déborde. */
            private void renderSkinName(GuiGraphics g, int bx, int by, int bw, int bh, int rc) {
                var font  = Minecraft.getInstance().font;
                Component text   = button.getMessage();
                int textW        = font.width(text);
                int maxW         = bw - 14;
                int textX        = bx + 8;
                int textY        = by + (bh - 8) / 2;

                if (textW > maxW) {
                    float scale = (float) maxW / textW;
                    g.pose().pushPose();
                    g.pose().translate(textX, textY, 0);
                    g.pose().scale(scale, scale, 1.0f);
                    g.drawString(font, text, 0, 0, rc, false);
                    g.pose().popPose();
                } else {
                    g.drawString(font, text, textX, textY, rc, false);
                }
            }

            private void renderLockedOverlay(GuiGraphics g, int bx, int by, int bh) {
                int iconSize = 12;
                int iconX    = bx + 8;
                int iconY    = by + (bh - iconSize) / 2;

                int uvY = parentScreen.LEGENDARY_SKIN.contains(button) ? 74  :
                        parentScreen.EPIC_SKIN.contains(button)      ? 88  :
                                parentScreen.HALLOWEEN_SKIN.contains(button) ? 88  :
                                        parentScreen.RARE_SKIN.contains(button)      ? 102 : 116;

                g.blit(ICONS_LOCATION, iconX, iconY, 0, uvY, iconSize, iconSize);

                int price = parentScreen.getSkinPrice(skinIndex);
                if (price > 0) {
                    g.blit(ICONS_LOCATION, iconX + 16, iconY, 0, 143, 10, 10);
                    g.drawString(Minecraft.getInstance().font,
                            String.valueOf(price), iconX + 28, iconY + 1, 0xc8f6ff, false);
                } else {
                    // Texte du nom avec scaling si débordement
                    var font   = Minecraft.getInstance().font;
                    Component msg  = button.getMessage();
                    int textStartX = iconX + 17;
                    int maxW       = bx + (int)(parentScreen.imageWidth * 0.8) - textStartX - 4;
                    int textW      = font.width(msg);
                    int textY      = by + (bh - 8) / 2;

                    if (textW > maxW) {
                        float scale = (float) maxW / textW;
                        g.pose().pushPose();
                        g.pose().translate(textStartX, textY, 0);
                        g.pose().scale(scale, scale, 1.0f);
                        g.drawString(font, msg, 0, 0, 0x555555, false);
                        g.pose().popPose();
                    } else {
                        g.drawString(font, msg, textStartX, textY, 0x555555, false);
                    }
                }
            }

            @Override
            public boolean mouseClicked(double mouseX, double mouseY, int btn) {
                return this.button.mouseClicked(mouseX, mouseY, btn);
            }

            @Override
            public List<? extends GuiEventListener> children() {
                return Collections.singletonList(this.button);
            }

            @Override
            public List<? extends NarratableEntry> narratables() {
                return Collections.singletonList(this.button);
            }
        }
    }
}