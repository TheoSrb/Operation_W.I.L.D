package net.tiew.operationWild.screen.entity;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.tiew.operationWild.OperationWild;
import net.tiew.operationWild.entity.OWEntity;
import net.tiew.operationWild.entity.quests.daily_quests.DailyQuest;
import net.tiew.operationWild.entity.quests.daily_quests.DailyQuestRegistry;
import net.tiew.operationWild.entity.quests.daily_quests.DailyQuestTier;
import net.tiew.operationWild.entity.quests.daily_quests.OWDailyQuests;
import net.tiew.operationWild.networking.OWNetworkHandler;
import net.tiew.operationWild.networking.packets.to_server.RerollDailyQuestPacket;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Écran des quêtes quotidiennes, calqué sur {@link OWOptionsScreen} : même texture / même taille,
 * en-tête, {@link OWScrollPanel} déroulant (scrollbar automatique si le contenu déborde) et barre
 * d'onglets commune. Chaque quête active est une carte : titre, barre de progression, « x / y »
 * (ou « Quête terminée » si verrouillée).
 */
@OnlyIn(Dist.CLIENT)
public class OWDailyQuestScreen extends Screen {

    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "textures/gui/ow_options_screen.png");

    static final int IMG_W = 176;
    static final int IMG_H = 166;
    private static final int HEADER_H = 24;
    private static final int FOOTER_H = 16;
    private static final int LIST_MX = 4;

    private final OWEntity entity;
    private OWScrollPanel scrollPanel;

    private final OWTabsRenderer tabsRenderer = new OWTabsRenderer();

    public OWDailyQuestScreen() {
        super(Component.literal("OWDailyQuestScreen"));
        this.entity = net.tiew.operationWild.entity.animals.terrestrial.RedPandaEntity.resolveControlledEntity(Minecraft.getInstance().player);
    }

    @Override
    protected void init() {
        super.init();
        int i = (this.width - IMG_W) / 2;
        int j = (this.height - IMG_H) / 2;

        int listX = i + LIST_MX;
        int listY = j + HEADER_H;
        int listW = IMG_W - LIST_MX * 2;
        int listH = IMG_H - HEADER_H - FOOTER_H;

        scrollPanel = new OWScrollPanel(listX, listY, listW, listH);
        // Clic reload : reroll immédiat (pas de confirmation). Le reroll du jour est consommé, donc on
        // masque aussitôt TOUS les boutons côté client (le serveur confirmera via la synchro).
        scrollPanel.onReloadRequest = slot -> {
            OWNetworkHandler.sendToServer(new RerollDailyQuestPacket(slot));
            if (entity != null) entity.dailyRerollAvailable = false;
        };
        if (entity != null) {
            for (int slot = 0; slot < 3; slot++) {
                scrollPanel.add(new QuestEntry(entity, slot));
            }
        }
        this.addWidget(scrollPanel);

        if (entity != null) {
            tabsRenderer.init(this.width, this.height, IMG_W, IMG_H, entity, this::addWidget);
            tabsRenderer.setActiveTab(OWTabsRenderer.Tab.DAILY_QUESTS);
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partial) {
        int i = (this.width - IMG_W) / 2;
        int j = (this.height - IMG_H) / 2;

        // Assombrit le monde derrière (même rendu que l'inventaire / la tribu).
        this.renderBackground(g, mouseX, mouseY, partial);
        super.render(g, mouseX, mouseY, partial);

        g.blit(TEXTURE, i, j, 0, 0, IMG_W, IMG_H);

        renderHeader(g, i, j);
        scrollPanel.render(g, mouseX, mouseY, partial);
        renderFooter(g, i, j);

        if (entity != null) {
            tabsRenderer.renderTabs(g, this.font, entity, i, j, mouseX, mouseY);
        }
    }

    private void renderHeader(GuiGraphics g, int i, int j) {
        Component title = Component.translatable("tooltip.questMenu");
        g.drawString(this.font, title, i + IMG_W / 2 - this.font.width(title) / 2, j + 9, 0xFFFFFF);
    }

    private void renderFooter(GuiGraphics g, int i, int j) {
        Component remaining = Component.literal(getTimeUntilNextDailyQuests())
                .setStyle(Style.EMPTY.withBold(true).withColor(TextColor.fromRgb(0xCAEEE6)));
        Component text = Component.translatable("tooltip.nextDailyQuests")
                .setStyle(Style.EMPTY.withColor(TextColor.fromRgb(0xFFFFFF)))
                .append(remaining);
        g.drawString(this.font, text, i + IMG_W / 2 - this.font.width(text) / 2, j + IMG_H - 12, 0xFFFFFF);
    }

    // ------------------------------------------------------------------
    // Carte d'une quête
    // ------------------------------------------------------------------

    private static final int PAD = 6;
    private static final int LINE_H = 9;
    private static final int BAR_H = 6;
    private static final int GAP = 4;
    private static final int REWARD_H = 11;
    private static final int RELOAD_SZ = 13;
    /** Espace réservé de chaque côté du titre (badge de tier à gauche, bouton reload à droite). */
    private static final int SIDE_INSET = 17;

    private static final ResourceLocation MOB_TYPES =
            ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "textures/gui/mob_types.png");

    /** Couleurs du dégradé de la barre de progression : 0 % (jaune orangé léger) → 100 % (vert). */
    private static final int FILL_LOW = 0xF3C24C;
    private static final int FILL_HIGH = 0x8BE45A;

    // Icônes de récompense (mêmes fichiers que la Piste), déclinées selon le montant.
    private static ResourceLocation misc(String path) {
        return ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, path);
    }
    private static final ResourceLocation COIN_1 = misc("textures/misc/coin.png");
    private static final ResourceLocation COIN_2 = misc("textures/misc/coin_2.png");
    private static final ResourceLocation COIN_3 = misc("textures/misc/coin_3.png");
    private static final ResourceLocation COIN_4 = misc("textures/misc/coin_4.png");
    private static final ResourceLocation EXP_1 = misc("textures/misc/exp.png");
    private static final ResourceLocation EXP_2 = misc("textures/misc/exp_2.png");
    private static final ResourceLocation EXP_3 = misc("textures/misc/exp_3.png");
    private static final ResourceLocation EXP_4 = misc("textures/misc/exp_4.png");

    /** Pièce dont l'aspect grossit avec le nombre de pièces. */
    private static ResourceLocation coinTextureFor(int coins) {
        if (coins >= 4) return COIN_4;
        if (coins == 3) return COIN_3;
        if (coins == 2) return COIN_2;
        return COIN_1;
    }
    /** Orbe d'XP dont l'aspect grossit avec le nombre d'orbes récompensés. */
    private static ResourceLocation expTextureFor(int orbs) {
        if (orbs >= 17) return EXP_4;
        if (orbs >= 13) return EXP_3;
        if (orbs >= 9) return EXP_2;
        return EXP_1;
    }

    /** Code de récompense pré-tiré de l'emplacement (0..2) : &gt;0 = orbes, &lt;0 = -pièces. */
    private static int rewardCodeOf(OWEntity entity, int slot) {
        if (entity == null) return 0;
        return switch (slot) {
            case 0 -> entity.questReward0;
            case 1 -> entity.questReward1;
            case 2 -> entity.questReward2;
            default -> 0;
        };
    }

    /** Couleur du tier : vert (facile) → orange (moyen) → rouge (difficile). */
    private static int tierColor(DailyQuestTier tier) {
        return switch (tier) {
            case I -> 0x5FB84A;
            case II -> 0xE8952E;
            case III -> 0xD1442E;
        };
    }

    private static String tierLabel(DailyQuestTier tier) {
        return switch (tier) {
            case I -> "I";
            case II -> "II";
            case III -> "III";
        };
    }

    /** Rang de tri d'affichage (plus grand = plus dur, donc plus haut) ; -1 si quête inconnue. */
    private static int tierRankOf(OWEntity entity, int slot) {
        DailyQuest q = activeQuestOf(entity, slot);
        return (q != null && q.getTier() != null) ? q.getTier().ordinal() : -1;
    }

    /** Petit badge carré coloré (fond assombri + biseau) avec le chiffre romain du tier. */
    private static void drawTierBadge(GuiGraphics g, net.minecraft.client.gui.Font font, int x, int y, DailyQuestTier tier) {
        int col = tierColor(tier);
        int bs = 12;
        g.fill(x, y, x + bs, y + bs, 0xFF000000 | lerpColor(col, 0x000000, 0.55f));
        g.fill(x, y, x + bs, y + 1, 0xFF000000 | lerpColor(col, 0xFFFFFF, 0.35f));
        g.fill(x, y, x + 1, y + bs, 0xFF000000 | lerpColor(col, 0xFFFFFF, 0.35f));
        g.fill(x + bs - 1, y, x + bs, y + bs, 0xFF000000 | lerpColor(col, 0x000000, 0.40f));
        g.fill(x, y + bs - 1, x + bs, y + bs, 0xFF000000 | lerpColor(col, 0x000000, 0.40f));

        String label = tierLabel(tier);
        int tx = x + bs / 2 - font.width(label) / 2;
        int ty = y + bs / 2 - 4;
        g.drawString(font, label, tx, ty, 0xFF000000 | lerpColor(col, 0xFFFFFF, 0.55f), true);
    }

    /** Interpolation linéaire entre deux couleurs RGB (t dans [0,1]). */
    private static int lerpColor(int a, int b, float t) {
        t = Math.max(0f, Math.min(1f, t));
        int ar = (a >> 16) & 0xFF, ag = (a >> 8) & 0xFF, ab = a & 0xFF;
        int br = (b >> 16) & 0xFF, bg = (b >> 8) & 0xFF, bb = b & 0xFF;
        int r = (int) (ar + (br - ar) * t);
        int gg = (int) (ag + (bg - ag) * t);
        int bl = (int) (ab + (bb - ab) * t);
        return (r << 16) | (gg << 8) | bl;
    }

    private static class QuestEntry {
        final OWEntity entity;
        final int slot;
        List<FormattedCharSequence> titleLines;
        int cachedH = -1;

        QuestEntry(OWEntity entity, int slot) {
            this.entity = entity;
            this.slot = slot;
        }

        void initHeight(Font font, int entryW) {
            // Recalculé à chaque frame : les quêtes de l'entité peuvent arriver/évoluer par synchro
            // après l'ouverture de l'écran, il ne faut donc pas figer le titre/la hauteur.
            DailyQuest quest = activeQuestOf(entity, slot);
            Component title = quest != null
                    ? Component.translatable(quest.getName())
                    : Component.translatable("tooltip.questMenu");
            // Largeur réduite : le titre reste centré mais ne passe plus sous le badge de tier
            // (gauche) ni sous le bouton reload (droite). La hauteur s'adapte au nb de lignes,
            // donc la carte grandit sans jamais empiéter sur la barre.
            titleLines = font.split(title, entryW - 2 * SIDE_INSET);
            cachedH = PAD + titleLines.size() * LINE_H + GAP + BAR_H + GAP + LINE_H + GAP + REWARD_H + PAD;
        }

        void render(GuiGraphics g, Font font, int x, int y, int w, boolean hovered, int displayIndex, int mx, int my) {
            DailyQuest quest = activeQuestOf(entity, slot);
            DailyQuestTier tier = (quest != null) ? quest.getTier() : null;

            // Fond de carte : une carte sur deux (par position d'affichage) légèrement plus sombre.
            boolean darker = (displayIndex % 2 == 0);
            g.fill(x, y, x + w, y + cachedH, darker ? 0x2C000000 : 0x0CFFFFFF);
            if (hovered) {
                g.fill(x, y, x + w, y + cachedH, 0x26FFFFFF);       // surbrillance au survol
                // Liseré gauche à la couleur du tier (vert/orange/rouge), vert par défaut si inconnu.
                int borderCol = (tier != null) ? (0xFF000000 | tierColor(tier)) : 0xFF8BE45A;
                g.fill(x, y, x + 2, y + cachedH, borderCol);
            }
            g.fill(x, y + cachedH - 1, x + w, y + cachedH, 0xFF1C2028); // séparateur bas

            // Badge de tier en haut à gauche : petit carré coloré avec « I » / « II » / « III ».
            if (tier != null) {
                drawTierBadge(g, font, x + 3, y + 3, tier);
            }

            // Titre (centré, éventuellement grisé si verrouillée)
            boolean locked = quest != null && isLocked(entity, quest.getId());
            int titleColor = quest == null ? 0x808080 : (locked ? 0xA0A0A0 : 0xFFFFFF);
            int ty = y + PAD;
            for (FormattedCharSequence line : titleLines) {
                g.drawString(font, line, x + w / 2 - font.width(line) / 2, ty, titleColor);
                ty += LINE_H;
            }

            int[] progress = getProgress(entity, quest);

            // Barre de progression
            int barX = x + PAD;
            int barW = w - 2 * PAD;
            int barY = ty + GAP;

            // Rail
            g.fill(barX, barY, barX + barW, barY + BAR_H, 0xFF20242C);
            g.fill(barX, barY, barX + barW, barY + 1, 0xFF14171D); // ombre interne du rail

            float ratio = locked ? 1f : (progress[1] > 0 ? (float) progress[0] / progress[1] : 0f);
            ratio = Math.max(0f, Math.min(1f, ratio));
            int filled = (int) (barW * ratio);

            // Remplissage : dégradé jaune-orangé (0 %) → vert (100 %) selon le pourcentage, avec une
            // ligne de lumière en haut et une ligne d'ombre en bas (colonne par colonne).
            for (int px = 0; px < filled; px++) {
                float t = barW > 1 ? (float) px / (barW - 1) : 1f;
                int c = lerpColor(FILL_LOW, FILL_HIGH, t);
                int light = lerpColor(c, 0xFFFFFF, 0.45f);
                int shadow = lerpColor(c, 0x000000, 0.40f);
                int cx = barX + px;
                g.fill(cx, barY, cx + 1, barY + BAR_H, 0xFF000000 | c);
                g.fill(cx, barY, cx + 1, barY + 1, 0xFF000000 | light);                     // lumière (haut)
                g.fill(cx, barY + BAR_H - 1, cx + 1, barY + BAR_H, 0xFF000000 | shadow);     // ombre (bas)
            }

            // Texte sous la barre : « x / y » ou « Quête terminée »
            int textY = barY + BAR_H + GAP;
            Component status;
            if (locked) {
                status = Component.translatable("tooltip.questFinished")
                        .setStyle(Style.EMPTY.withItalic(true).withColor(0x00FF00));
            } else {
                status = Component.literal(progress[0] + " / " + progress[1])
                        .setStyle(Style.EMPTY.withBold(true).withColor(TextColor.fromRgb(0xDDDDDD)));
            }
            g.drawString(font, status, x + w / 2 - font.width(status) / 2, textY, 0xFFFFFF);

            // Récompense pré-tirée : icône (exp ou pièces, variante selon le montant) + montant, centrée.
            int code = rewardCodeOf(entity, slot);
            if (code != 0) {
                boolean isCoin = code < 0;
                int amount = Math.abs(code);
                ResourceLocation icon = isCoin ? coinTextureFor(amount) : expTextureFor(amount);
                int iconSz = 11;
                Component amt = Component.literal("+" + amount)
                        .setStyle(Style.EMPTY.withBold(true)
                                .withColor(TextColor.fromRgb(isCoin ? 0xF3C83B : 0x8BE45A)));
                int totalW = iconSz + 3 + font.width(amt);
                int rx = x + w / 2 - totalW / 2;
                int ry = textY + LINE_H + GAP - 1;
                g.blit(icon, rx, ry, iconSz, iconSz, 0f, 0f, 16, 16, 16, 16);
                g.drawString(font, amt, rx + iconSz + 3, ry + 2, 0xFFFFFF, true);
            }

            // Bouton reload en haut à droite : présent seulement si le reroll du jour est disponible.
            if (quest != null && entity != null && entity.dailyRerollAvailable) {
                int rbx = x + w - 3 - RELOAD_SZ;
                int rby = y + 3;
                boolean rbHover = mx >= rbx && mx < rbx + RELOAD_SZ && my >= rby && my < rby + RELOAD_SZ;
                drawReloadButton(g, rbx, rby, rbHover);
            }
        }

        /** Vrai si le bouton reload (disponible) est cliqué, pour l'entité à cette position. */
        boolean reloadButtonHit(int x, int y, int w, double mx, double my) {
            if (entity == null || !entity.dailyRerollAvailable || activeQuestOf(entity, slot) == null) return false;
            int rbx = x + w - 3 - RELOAD_SZ;
            int rby = y + 3;
            return mx >= rbx && mx < rbx + RELOAD_SZ && my >= rby && my < rby + RELOAD_SZ;
        }
    }

    /** Bouton reload 15×15 : icône 13×13 de mob_types.png (0,130) centrée, avec surbrillance au survol. */
    private static void drawReloadButton(GuiGraphics g, int x, int y, boolean hover) {
        if (hover) g.fill(x, y, x + RELOAD_SZ, y + RELOAD_SZ, 0x22FFFFFF);
        g.blit(MOB_TYPES, x + (RELOAD_SZ - 13) / 2, y + (RELOAD_SZ - 13) / 2, 0, 130, 13, 13);
    }

    /** Quête active à l'emplacement {@code slot} (0..2) pour cette entité, ou {@code null}. */
    private static DailyQuest activeQuestOf(OWEntity entity, int slot) {
        if (entity == null) return null;
        int id = switch (slot) {
            case 0 -> entity.activeQuest0;
            case 1 -> entity.activeQuest1;
            case 2 -> entity.activeQuest2;
            default -> -1;
        };
        return DailyQuestRegistry.getById(id);
    }

    private static boolean isLocked(OWEntity entity, int questId) {
        if (entity == null) return false;
        switch (questId) {
            case 0: return entity.quest0isLocked;
            case 1: return entity.quest1isLocked;
            case 2: return entity.quest2isLocked;
            case 3: return entity.quest3isLocked;
            case 4: return entity.quest4isLocked;
            case 5: return entity.quest5isLocked;
            case 6: return entity.quest6isLocked;
            case 7: return entity.quest7isLocked;
            case 8: return entity.quest8isLocked;
            case 9: return entity.quest9isLocked;
            case 10: return entity.quest10isLocked;
            default: return false;
        }
    }

    private static int[] getProgress(OWEntity entity, DailyQuest quest) {
        if (quest == null || entity == null) return new int[]{0, 1};
        int max = Math.max(1, quest.getMaxValue());
        int current;
        switch (quest.getId()) {
            case 0: current = entity.quest0Progression; break;
            case 1: current = entity.quest1Progression; break;
            case 2: current = entity.quest2Progression; break;
            case 3: current = entity.quest3Progression; break;
            case 4: current = entity.quest4Progression; break;
            case 5: current = entity.quest5Progression; break;
            case 6: current = entity.quest6Progression; break;
            case 7: current = entity.quest7Progression; break;
            case 8: current = entity.quest8Progression; break;
            case 9: current = entity.quest9Progression; break;
            case 10: current = entity.quest10Progression; break;
            default: return new int[]{0, 1};
        }
        return new int[]{Math.min(current, max), max};
    }

    private static String getTimeUntilNextDailyQuests() {
        Calendar now = Calendar.getInstance();
        Calendar nextQuests = Calendar.getInstance();

        nextQuests.set(Calendar.HOUR_OF_DAY, OWDailyQuests.DAILY_QUEST_HOUR);
        nextQuests.set(Calendar.MINUTE, OWDailyQuests.DAILY_QUEST_MINUTES);
        nextQuests.set(Calendar.SECOND, OWDailyQuests.DAILY_QUEST_SECONDS);

        if (now.after(nextQuests)) {
            nextQuests.add(Calendar.DAY_OF_MONTH, 1);
        }

        long diffInMillis = nextQuests.getTimeInMillis() - now.getTimeInMillis();

        long hours = TimeUnit.MILLISECONDS.toHours(diffInMillis);
        diffInMillis -= TimeUnit.HOURS.toMillis(hours);

        long minutes = TimeUnit.MILLISECONDS.toMinutes(diffInMillis);
        diffInMillis -= TimeUnit.MINUTES.toMillis(minutes);

        long seconds = TimeUnit.MILLISECONDS.toSeconds(diffInMillis);

        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }

    // ------------------------------------------------------------------
    // Panneau déroulant (scrollbar automatique) — calqué sur OWOptionsScreen
    // ------------------------------------------------------------------

    private static class OWScrollPanel extends AbstractWidget {

        private final List<QuestEntry> entries = new ArrayList<>();
        private int scrollY = 0;
        private int totalH = 0;
        private boolean dirty = true;
        /** Appelé avec le slot quand un bouton reload est cliqué (ouvre la confirmation). */
        java.util.function.IntConsumer onReloadRequest;

        private static final int SCROLLBAR_W = 5;
        private static final int SCROLL_SPEED = 14;
        private static final int GAP_BETWEEN = 4;

        OWScrollPanel(int x, int y, int w, int h) {
            super(x, y, w, h, Component.empty());
        }

        void add(QuestEntry entry) {
            entries.add(entry);
            dirty = true;
        }

        private int entryW() {
            return this.width - SCROLLBAR_W - 3;
        }

        private void recompute() {
            // Recalcul à chaque frame (3 entrées, coût négligeable) pour refléter les quêtes
            // synchronisées depuis le serveur même si l'écran était déjà ouvert.
            Font font = Minecraft.getInstance().font;
            // Tri d'affichage : du plus dur (tier III) en haut au plus facile (tier I) en bas.
            entries.sort((a, b) -> Integer.compare(tierRankOf(b.entity, b.slot), tierRankOf(a.entity, a.slot)));
            totalH = 0;
            for (QuestEntry e : entries) {
                e.initHeight(font, entryW());
                totalH += e.cachedH + GAP_BETWEEN;
            }
        }

        private int maxScroll() {
            return Math.max(0, totalH - this.height);
        }

        @Override
        protected void renderWidget(GuiGraphics g, int mx, int my, float partial) {
            recompute();
            Font font = Minecraft.getInstance().font;
            int eW = entryW();
            boolean scroll = maxScroll() > 0;

            g.enableScissor(getX(), getY(),
                    getX() + (scroll ? eW + 2 : this.width), getY() + this.height);

            int y = getY() - scrollY;
            int idx = 0;
            for (QuestEntry entry : entries) {
                boolean hov = mx >= getX() && mx < getX() + eW && my >= y && my < y + entry.cachedH;
                entry.render(g, font, getX(), y, eW, hov, idx, mx, my);
                y += entry.cachedH + GAP_BETWEEN;
                idx++;
            }
            g.disableScissor();

            if (scroll) renderScrollbar(g);
        }

        private void renderScrollbar(GuiGraphics g) {
            int sx = getX() + this.width - SCROLLBAR_W;
            int sy = getY();
            int sh = this.height;
            int ms = maxScroll();

            g.fill(sx, sy, sx + SCROLLBAR_W, sy + sh, 0xFF111111);

            int thumbH = Math.max(16, sh * sh / Math.max(1, totalH));
            int thumbY = ms > 0 ? sy + (sh - thumbH) * scrollY / ms : sy;
            g.fill(sx + 1, thumbY, sx + SCROLLBAR_W - 1, thumbY + thumbH, 0xFF888888);
        }

        @Override
        public boolean mouseClicked(double mx, double my, int button) {
            if (button != 0 || !isOver(mx, my)) return false;
            int eW = entryW();
            int y = getY() - scrollY;
            for (QuestEntry entry : entries) {
                if (entry.reloadButtonHit(getX(), y, eW, mx, my)) {
                    if (onReloadRequest != null) onReloadRequest.accept(entry.slot);
                    return true;
                }
                y += entry.cachedH + GAP_BETWEEN;
            }
            return false;
        }

        @Override
        public boolean mouseScrolled(double mx, double my, double dx, double dy) {
            if (!isOver(mx, my)) return false;
            scrollY = clamp((int) (scrollY - dy * SCROLL_SPEED), 0, maxScroll());
            return true;
        }

        private boolean isOver(double mx, double my) {
            return mx >= getX() && mx < getX() + this.width && my >= getY() && my < getY() + this.height;
        }

        @Override
        public boolean isMouseOver(double mx, double my) {
            return isOver(mx, my);
        }

        @Override
        protected void updateWidgetNarration(NarrationElementOutput n) {
        }

        private static int clamp(int v, int min, int max) {
            return Math.max(min, Math.min(v, max));
        }
    }
}
