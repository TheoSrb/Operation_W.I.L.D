package net.tiew.operationWild.screen.tribe;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextColor;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.tiew.operationWild.client.OWClientChampions;
import net.tiew.operationWild.client.OWClientTribeData;
import net.tiew.operationWild.core.OWChampions;
import net.tiew.operationWild.entity.OWEntity;
import net.tiew.operationWild.gui.OWEntityHud;
import net.tiew.operationWild.networking.OWNetworkHandler;
import net.tiew.operationWild.networking.packets.to_server.SelectChampionPacket;
import net.tiew.operationWild.team.OWArenaFighter;
import net.tiew.operationWild.team.OWTeam;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Onglet <b>Champions</b> : le chef y désigne les cinq créatures qui représentent la tribu.
 *
 * <p>Elles seules arborent l'étendard dans le monde. L'écran reprend délibérément la disposition de
 * la sélection de combattants d'arène — colonne d'emplacements à gauche, liste défilante à droite —
 * pour que le geste soit le même à deux endroits qui posent la même question : « lesquelles ? ».</p>
 *
 * <p>Les membres autres que le chef voient l'écran en lecture seule : savoir qui porte les couleurs
 * de sa tribu regarde toute la tribu, mais le choix appartient au chef.</p>
 */
public class OWTribeChampionsScreen extends OWTribeScreen {

    private static final int CONTENT_Y = 20;
    private static final int SLOT = 20, SLOT_GAP = 1;
    private static final int ROW_H = 13;

    /** Aperçus 3D construits à la demande, réutilisés d'une image à l'autre. */
    private final Map<String, LivingEntity> previewCache = new HashMap<>();
    /** Progressions d'animation en cours, par identifiant d'élément. */
    private final Map<String, Float> anims = new HashMap<>();

    /** Gabarit de la vignette de tête au bout d'une ligne (largeur, hauteur). */
    private static final int HEAD_W = 18, HEAD_H = ROW_H - 2;

    /** Pictogrammes de genre, partagés avec l'étiquette flottante ({@code OWRendererUtils}). */
    private static final ResourceLocation MOB_ICONS = ResourceLocation.fromNamespaceAndPath(
            net.tiew.operationWild.OperationWild.MOD_ID, "textures/gui/mob_types.png");
    private static final int ICON = 12, GENDER_V_MALE = 48, GENDER_V_FEMALE = 36;
    /** Niveau au-delà duquel la valeur passe au doré, comme dans l'écran d'inventaire. */
    private static final int MAX_LEVEL = 50;

    /** Ligne survolée à cette image, ou {@code null} — alimente la fiche détaillée. */
    private OWArenaFighter hoveredRow;
    private int hoveredRowY;

    private int scroll = 0, scrollMax = 0, visibleRows = 0;
    private float scrollShown = 0f;
    private long lastFrameMs = System.currentTimeMillis();
    /** Horloge relative à l'ouverture : une valeur absolue en float perd toute précision. */
    private final long epochMs = System.currentTimeMillis();

    public OWTribeChampionsScreen() {
        super(Component.translatable("owteams.champions.title"));
    }

    private float clock() { return (System.currentTimeMillis() - epochMs) / 1000f; }

    /** Vrai si le joueur local peut modifier la liste. */
    private boolean canEdit() {
        OWTeam t = OWClientTribeData.get();
        Minecraft mc = Minecraft.getInstance();
        return t != null && mc.player != null && mc.player.getUUID().equals(t.getTeamOwnerUUID());
    }

    /**
     * Approche progressive d'une valeur cible, indépendante du nombre d'images par seconde.
     * Une interpolation par image donnerait des vitesses différentes selon la machine.
     */
    private float animate(String key, float target, float speed) {
        float cur = anims.getOrDefault(key, target);
        long now = System.currentTimeMillis();
        float dt = Math.min(0.1f, (now - lastFrameMs) / 1000f);
        float next = cur + (target - cur) * Math.min(1f, dt * speed);
        anims.put(key, next);
        return next;
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partial) {
        drawPanel(g, mouseX, mouseY, partial);
        renderTribeTabs(g, mouseX, mouseY, Tab.CHAMPIONS);
        drawHeader(g, Component.translatable("owteams.champions.title"));
        g.fill(leftPos + 6, topPos + 18, leftPos + IMG_W - 6, topPos + 19, 0xFF9A9A9A);

        hoveredRow = null;
        List<OWArenaFighter> champions = OWClientChampions.championFighters();
        int slotsY = topPos + CONTENT_Y + 9;

        renderSlots(g, champions, slotsY, mouseX, mouseY);

        int listX = leftPos + 6 + SLOT + 4;
        int listW = (leftPos + IMG_W - 6 - SLOT - 4) - listX;
        int listH = OWChampions.MAX_CHAMPIONS * (SLOT + SLOT_GAP) - SLOT_GAP;
        renderCandidates(g, listX, slotsY, listW, listH, mouseX, mouseY);

        // Compteur calé en bas du panneau, et non sous la liste : la liste a une hauteur fixe qui
        // laisse un grand vide en dessous, où le compteur semblait flotter au milieu de rien.
        int infoY = topPos + IMG_H - 15;
        int filled = OWClientChampions.championUuids().size();
        drawSmall(g, Component.translatable("owteams.champions.count", filled, OWChampions.MAX_CHAMPIONS),
                leftPos + 7, infoY, filled == 0 ? 0x808080 : 0x3F7A52);
        if (!canEdit()) {
            drawSmall(g, Component.translatable("owteams.champions.hint_readonly"),
                    leftPos + 7, infoY - 9, 0x707070);
        }

        super.render(g, mouseX, mouseY, partial);
        // La fiche de survol passe en dernier : elle recouvre le panneau, y compris les widgets.
        renderHoverCard(g, mouseX, mouseY);
        lastFrameMs = System.currentTimeMillis();
    }

    /** Colonne de gauche : un emplacement par champion possible, vide ou occupé. */
    private void renderSlots(GuiGraphics g, List<OWArenaFighter> champions, int slotsY, int mouseX, int mouseY) {
        List<UUID> ids = OWClientChampions.championUuids();
        for (int i = 0; i < OWChampions.MAX_CHAMPIONS; i++) {
            int x = leftPos + 6, y = slotsY + i * (SLOT + SLOT_GAP);
            boolean occupied = i < champions.size();
            boolean hov = canEdit() && occupied && inBox(mouseX, mouseY, x, y, SLOT, SLOT);
            float hoverAmt = animate("slot" + i, hov ? 1f : 0f, 15f);

            g.fill(x, y, x + SLOT, y + SLOT, 0xCC0E0E10);
            if (occupied) {
                // Liseré doré : c'est la marque du porte-étendard, reprise du fil de la bannière.
                int glow = 0x80 + (int) (0x50 * hoverAmt);
                g.fill(x, y, x + SLOT, y + 1, (glow << 24) | 0xE9B115);
                g.fill(x, y + SLOT - 1, x + SLOT, y + SLOT, (glow << 24) | 0xE9B115);
            }
            if (hoverAmt > 0.01f) g.fill(x, y, x + SLOT, y + SLOT, ((int) (50 * hoverAmt) << 24) | 0xFFFFFF);

            if (!occupied) {
                g.drawCenteredString(this.font, "·", x + SLOT / 2, y + SLOT / 2 - 4, 0xFF555555);
                continue;
            }
            OWArenaFighter f = champions.get(i);
            if (f == null) {
                // Champion hors des chunks chargés : sa place est tenue, mais on ne peut pas le
                // dessiner. Un « ? » vaut mieux qu'un emplacement qui semblerait libre.
                g.drawCenteredString(this.font, "?", x + SLOT / 2, y + SLOT / 2 - 4, 0xFF777777);
                if (ids.size() > i) drawSlotIndex(g, i, x, y);
                continue;
            }
            drawPreview(g, f, x, y, SLOT, hov ? mouseX : Float.NaN, hov ? mouseY : i);
            drawSlotIndex(g, i, x, y);
        }
    }

    /** Rang du champion dans l'ordre de nomination, discret dans le coin de l'emplacement. */
    private void drawSlotIndex(GuiGraphics g, int index, int x, int y) {
        String n = String.valueOf(index + 1);
        float s = 0.65f;
        g.pose().pushPose();
        g.pose().translate(x + SLOT - 2 - this.font.width(n) * s, y + SLOT - 7, 0);
        g.pose().scale(s, s, 1f);
        g.drawString(this.font, n, 0, 0, 0xFFE9B115, true);
        g.pose().popPose();
    }

    /** Liste défilante des créatures de la tribu. */
    private void renderCandidates(GuiGraphics g, int x, int y, int w, int h, int mouseX, int mouseY) {
        List<OWArenaFighter> candidates = OWClientChampions.candidates();
        g.fill(x, y, x + w, y + h, 0xCC0E0E10);

        if (candidates.isEmpty()) {
            drawSmallCentered(g, Component.translatable("owteams.champions.no_candidate"),
                    x + w / 2, y + h / 2 - 4, 0x777777);
            visibleRows = 0;
            return;
        }

        visibleRows = h / ROW_H;
        boolean hasScroll = candidates.size() > visibleRows;
        int contentW = hasScroll ? w - 6 : w;
        scrollMax = Math.max(0, candidates.size() - visibleRows);
        scroll = Math.min(scroll, scrollMax);
        scrollShown = animate("scroll", scroll, 17f);

        int first = Math.max(0, (int) Math.floor(scrollShown) - 1);
        int last = Math.min(candidates.size(), (int) Math.ceil(scrollShown) + visibleRows + 1);

        g.enableScissor(x, y, x + contentW, y + h);
        for (int i = first; i < last; i++) {
            OWArenaFighter f = candidates.get(i);
            int ry = Math.round(y + (i - scrollShown) * ROW_H);
            boolean chosen = OWClientChampions.isChampion(f.entityUuid());
            boolean full = !chosen && OWClientChampions.championUuids().size() >= OWChampions.MAX_CHAMPIONS;
            boolean hov = inBox(mouseX, mouseY, x, ry, contentW, ROW_H)
                    && mouseY >= y && mouseY < y + h;

            float hoverAmt = animate("cand" + i, hov && !full && canEdit() ? 1f : 0f, 15f);
            if (chosen) g.fill(x, ry, x + contentW, ry + ROW_H - 1, 0x33E9B115);
            else if ((i & 1) == 0) g.fill(x, ry, x + contentW, ry + ROW_H - 1, 0x14FFFFFF);
            if (hoverAmt > 0.01f) g.fill(x, ry, x + contentW, ry + ROW_H - 1,
                    ((int) (55 * hoverAmt) << 24) | 0xFFFFFF);

            // Pastille dorée pour un champion, grise sinon ; elle s'étire au survol.
            int chipW = 3 + Math.round(hoverAmt * 2f);
            g.fill(x + 2, ry + 3, x + 2 + chipW, ry + ROW_H - 4,
                    0xFF000000 | (chosen ? 0xE9B115 : (full ? 0x3A3A3A : 0x6A6A6A)));

            int nameCol = full ? 0x666666 : (chosen ? 0xF2DFA0 : 0xE8E8E8);
            int nameX = x + 8 + Math.round(hoverAmt * 2f);
            g.drawString(this.font, trimTo(f.name(), contentW - 14 - HEAD_W),
                    nameX, ry + 3, nameCol, false);
            // Portrait à droite : on reconnaît une créature à sa tête bien plus vite qu'à son nom.
            drawHead(g, f, x + contentW - HEAD_W - 2, ry + (ROW_H - HEAD_H) / 2);

            if (hov) { hoveredRow = f; hoveredRowY = ry; }
        }
        g.disableScissor();

        if (hasScroll) {
            drawScrollbar(g, x + w - 5, y, h, scroll, scrollMax, visibleRows * ROW_H,
                    candidates.size() * ROW_H);
        }
    }

    // ── Interaction ──────────────────────────────────────────────────────────────
    @Override
    public boolean mouseClicked(double mx, double my, int button) {
        if (tribeTabClicked(mx, my, Tab.CHAMPIONS)) return true;
        if (button != 0 || !canEdit()) return super.mouseClicked(mx, my, button);

        int slotsY = topPos + CONTENT_Y + 9;
        // Clic sur un emplacement occupé : le champion est révoqué.
        List<UUID> ids = OWClientChampions.championUuids();
        for (int i = 0; i < Math.min(ids.size(), OWChampions.MAX_CHAMPIONS); i++) {
            int x = leftPos + 6, y = slotsY + i * (SLOT + SLOT_GAP);
            if (inBox((int) mx, (int) my, x, y, SLOT, SLOT)) {
                OWNetworkHandler.sendToServer(new SelectChampionPacket(ids.get(i), false));
                playTabSwitch();
                return true;
            }
        }

        // Clic dans la liste : nomination (ou révocation si déjà champion).
        int listX = leftPos + 6 + SLOT + 4;
        int listW = (leftPos + IMG_W - 6 - SLOT - 4) - listX;
        int listH = OWChampions.MAX_CHAMPIONS * (SLOT + SLOT_GAP) - SLOT_GAP;
        List<OWArenaFighter> candidates = OWClientChampions.candidates();
        if (inBox((int) mx, (int) my, listX, slotsY, listW, listH) && !candidates.isEmpty()) {
            int index = (int) Math.floor((my - slotsY) / ROW_H + scrollShown);
            if (index >= 0 && index < candidates.size()) {
                OWArenaFighter f = candidates.get(index);
                boolean chosen = OWClientChampions.isChampion(f.entityUuid());
                if (chosen || ids.size() < OWChampions.MAX_CHAMPIONS) {
                    OWNetworkHandler.sendToServer(new SelectChampionPacket(f.entityUuid(), !chosen));
                    playTabSwitch();
                }
                return true;
            }
        }
        return super.mouseClicked(mx, my, button);
    }

    @Override
    public boolean mouseScrolled(double mx, double my, double dx, double dy) {
        if (scrollMax > 0) {
            scroll = Math.max(0, Math.min(scrollMax, scroll - (int) Math.signum(dy)));
            return true;
        }
        return super.mouseScrolled(mx, my, dx, dy);
    }

    // ── Dessin ───────────────────────────────────────────────────────────────────
    private static boolean inBox(int mx, int my, int x, int y, int w, int h) {
        return mx >= x && mx < x + w && my >= y && my < y + h;
    }

    private void drawSmall(GuiGraphics g, Component text, int x, int y, int color) {
        g.pose().pushPose();
        g.pose().translate(x, y, 0);
        g.pose().scale(0.75f, 0.75f, 1f);
        g.drawString(this.font, text, 0, 0, color, false);
        g.pose().popPose();
    }

    private void drawSmallCentered(GuiGraphics g, Component text, int cx, int y, int color) {
        float w = this.font.width(text) * 0.75f;
        drawSmall(g, text, Math.round(cx - w / 2f), y, color);
    }

    private String trimTo(String s, int maxW) {
        if (s == null) return "";
        if (this.font.width(s) <= maxW) return s;
        String out = s;
        while (out.length() > 1 && this.font.width(out + "…") > maxW) {
            out = out.substring(0, out.length() - 1);
        }
        return out.stripTrailing() + "…";
    }

    /**
     * Vignette de tête d'une créature, au bout de sa ligne.
     *
     * <p>Image plate tirée de la planche du HUD, et non un rendu 3D : le rendu d'entité écrit dans
     * le tampon de profondeur et passe par-dessus tout ce qui est dessiné après lui — les têtes
     * traversaient la fiche de survol. Une vignette se dessine dans l'ordre, et se reconnaît mieux
     * à cette taille qu'un modèle réduit à douze pixels.</p>
     */
    private void drawHead(GuiGraphics g, OWArenaFighter f, int x, int y) {
        if (!(previewFor(f) instanceof OWEntity owE)) return;
        OWEntityHud.EntityIconData icon = OWEntityHud.getEntityIconData(owE);
        if (icon == null) return; // espèce sans vignette : la ligne reste simplement sans image
        // Les vignettes vont de 13×14 (crocodile) à 17×23 (kangourou) : sans réduction, les plus
        // hautes débordent sur les lignes voisines. On les ramène au gabarit en conservant leurs
        // proportions, et on cale le résultat à droite, centré en hauteur.
        float sc = Math.min(1f, Math.min(HEAD_W / (float) icon.width, HEAD_H / (float) icon.height));
        float dw = icon.width * sc, dh = icon.height * sc;
        g.pose().pushPose();
        g.pose().translate(x + HEAD_W - dw, y + (HEAD_H - dh) / 2f, 0f);
        g.pose().scale(sc, sc, 1f);
        g.blit(OWEntityHud.HUD, 0, 0, icon.textureX, icon.textureY, icon.width, icon.height);
        g.pose().popPose();
    }

    /**
     * Fiche détaillée de la créature survolée : portrait 3D et les mêmes renseignements que
     * l'étiquette flottant au-dessus d'elle dans le monde.
     *
     * <p>Dessinée en dernier et calée sur le curseur, à la façon d'une infobulle : elle ne prend
     * aucune place dans la mise en page, qui est déjà à l'étroit dans un panneau de 176 px.</p>
     */
    private void renderHoverCard(GuiGraphics g, int mouseX, int mouseY) {
        OWArenaFighter f = hoveredRow;
        if (f == null) return;

        int accent = entityColorOf(f);

        // Espèce et archétype se distinguent par leur couleur seule ; le gras est réservé aux
        // valeurs chiffrées, sans quoi la moitié de la fiche serait mise en avant et plus rien
        // ne ressortirait.
        Component species = speciesOf(f).copy().withStyle(st -> st.withColor(TextColor.fromRgb(accent)));
        Component archetype = Component.translatable(f.archetypeKey())
                .withStyle(archetypeFormat(f.archetypeOrdinal()));
        // Doré au niveau maximum, vert en deçà : le code couleur de l'écran d'inventaire.
        Component level = Component.translatable("tooltip.lvl",
                Component.literal(String.valueOf(f.level())).withStyle(st -> st
                        .withColor(TextColor.fromRgb(f.level() >= MAX_LEVEL ? 0xDD9847 : 0xB8E45A))
                        .withBold(true)));
        Component owner = f.ownerName().isEmpty() ? null
                : Component.translatable("tooltip.owner",
                        Component.literal(f.ownerName()).withStyle(st -> st
                                .withColor(TextColor.fromRgb(0xE8E8E8)).withBold(true)));

        final int pad = 5, portrait = 42, gap = 5, lineH = 10;
        int textW = Math.max(this.font.width(f.name()) + 2 + ICON, this.font.width(species));
        textW = Math.max(textW, this.font.width(level));
        textW = Math.max(textW, ICON + 2 + this.font.width(archetype));
        if (owner != null) textW = Math.max(textW, this.font.width(owner));

        int lineCount = owner != null ? 4 : 3;
        int cardW = pad + portrait + gap + textW + pad;
        int cardH = pad + Math.max(portrait, 12 + lineCount * lineH) + pad;

        // Maintenue dans l'écran : près d'un bord, la fiche bascule de l'autre côté du curseur.
        int cx = mouseX + 10, cy = mouseY - cardH / 2;
        if (cx + cardW > this.width - 2) cx = mouseX - 10 - cardW;
        cy = Math.max(2, Math.min(this.height - cardH - 2, cy));

        // Élevée en profondeur, à la manière des infobulles vanilla : les aperçus 3D de la colonne
        // de gauche écrivent dans le tampon de profondeur, et un remplissage au ras de l'écran
        // serait masqué par eux là où la fiche les recouvre.
        g.pose().pushPose();
        g.pose().translate(0f, 0f, 400f);

        // Bordures aux couleurs de l'espèce : la fiche prend la teinte de la bête qu'elle décrit,
        // comme le fait déjà son étiquette dans le monde.
        g.fill(cx, cy, cx + cardW, cy + cardH, 0xF00E0E12);
        g.fill(cx, cy, cx + cardW, cy + 1, 0xFF000000 | accent);
        g.fill(cx, cy + cardH - 1, cx + cardW, cy + cardH, 0x60000000 | accent);

        int px = cx + pad, py = cy + pad;
        g.fill(px, py, px + portrait, py + portrait, 0xFF15151A);
        drawPreview(g, f, px, py, portrait, Float.NaN, 0f);

        int tx = px + portrait + gap, ty = cy + pad + 1;
        Component name = Component.literal(f.name()).withStyle(st -> st
                .withColor(TextColor.fromRgb(0xF2DFA0)).withBold(true));
        g.drawString(this.font, name, tx, ty, 0xFFFFFFFF, false);
        // Pictogramme de genre accolé au nom, comme sur l'étiquette qui flotte au-dessus de la bête.
        g.blit(MOB_ICONS, tx + this.font.width(name) + 2, ty - 2, 0,
                f.male() ? GENDER_V_MALE : GENDER_V_FEMALE, ICON, ICON);
        ty += 12;

        g.drawString(this.font, species, tx, ty, 0xFFFFFFFF, false);
        ty += lineH;
        g.drawString(this.font, level, tx, ty, 0xFFB8B8B8, false);
        ty += lineH;

        // Archétype : son symbole, celui de l'écran d'inventaire de la créature, posé après le nom
        // comme le pictogramme de genre suit le nom de la bête.
        g.drawString(this.font, archetype, tx, ty, 0xFFFFFFFF, false);
        g.blit(MOB_ICONS, tx + this.font.width(archetype) + 2, ty - 2,
                archetypeIconU(f.archetypeOrdinal()), archetypeIconV(f.archetypeOrdinal()), ICON, ICON);
        ty += lineH;

        if (owner != null) g.drawString(this.font, owner, tx, ty, 0xFFB8B8B8, false);
        g.pose().popPose();
    }

    /**
     * Coordonnées du symbole d'archétype dans {@code mob_types.png}, reprises de l'écran
     * d'inventaire de la créature — c'est là que le joueur les a apprises.
     */
    private static int archetypeIconU(int ordinal) {
        return switch (ordinal) {
            case 0, 1, 2 -> 0;   // TANK, ASSASSIN, MARAUDEUR
            default -> 12;       // SOIGNEUR, BERSERKER, ÉCLAIREUR, NORMAL
        };
    }

    private static int archetypeIconV(int ordinal) {
        return switch (ordinal) {
            case 0, 3 -> 0;      // TANK, SOIGNEUR
            case 1, 4 -> 12;     // ASSASSIN, BERSERKER
            case 2, 5 -> 24;     // MARAUDEUR, ÉCLAIREUR
            default -> 36;       // NORMAL
        };
    }

    /**
     * Couleur de chaque archétype, reprise telle quelle des libellés {@code tooltip.mobTypes*} de
     * l'écran d'inventaire de la créature — c'est là que le joueur apprend le code couleur, il ne
     * doit pas en découvrir un second ici.
     */
    private static ChatFormatting archetypeFormat(int ordinal) {
        return switch (ordinal) {
            case 0 -> ChatFormatting.AQUA;        // Tank
            case 1 -> ChatFormatting.DARK_RED;    // Assassin
            case 2 -> ChatFormatting.DARK_PURPLE; // Maraudeur
            case 3 -> ChatFormatting.GREEN;       // Soigneur
            case 4 -> ChatFormatting.GOLD;        // Berserker
            case 5 -> ChatFormatting.YELLOW;      // Éclaireur
            default -> ChatFormatting.WHITE;      // Normal
        };
    }

    /**
     * Couleur propre à l'espèce, celle dont {@code OWRendererUtils} teinte déjà son étiquette.
     * Repli sur l'or de la bannière si l'aperçu n'a pas pu être construit.
     */
    private int entityColorOf(OWArenaFighter f) {
        return previewFor(f) instanceof OWEntity owE ? owE.getEntityColor() : 0xE9B115;
    }

    /** Nom d'espèce, tiré du type d'entité — inutile de le faire voyager sur le réseau. */
    private Component speciesOf(OWArenaFighter f) {
        ResourceLocation rl = ResourceLocation.tryParse(f.entityTypeId());
        EntityType<?> type = rl != null ? BuiltInRegistries.ENTITY_TYPE.getOptional(rl).orElse(null) : null;
        return type != null ? type.getDescription() : Component.literal(f.entityTypeId());
    }

    /** Aperçu 3D d'une créature dans un cadre carré, mis à l'échelle pour ne rien laisser dépasser. */
    private void drawPreview(GuiGraphics g, OWArenaFighter f, int x, int y, int box, float mx, float my) {
        LivingEntity preview = previewFor(f);
        if (preview == null) {
            g.drawCenteredString(this.font, "?", x + box / 2, y + box / 2 - 4, 0x777777);
            return;
        }
        float span = Math.max(preview.getBbWidth(), preview.getBbHeight());
        if (span <= 0.01f) span = 1f;
        int scale = Math.max(3, (int) (box * 0.80f / span));

        float px, py;
        if (Float.isNaN(mx)) {
            // Oscillation lente, déphasée par emplacement : la colonne respire sans s'agiter.
            float phase = clock() * 0.8f + my * 1.3f;
            px = x + box / 2f + (float) Math.sin(phase) * 26f;
            py = y + box * 0.35f;
        } else {
            px = mx; py = my;
        }

        // Le rendu d'entité écrit dans le depth buffer : on l'isole pour ne pas percer le cadre.
        g.enableScissor(x + 1, y + 1, x + box - 1, y + box - 1);
        InventoryScreen.renderEntityInInventoryFollowsMouse(g,
                x + 1, y + 1, x + box - 1, y + box - 1,
                scale, entityYOffset(preview), px, py, preview);
        g.disableScissor();
    }

    /** Rattrape les silhouettes très éloignées de la taille d'un joueur, sans rien pousser hors cadre. */
    private static float entityYOffset(LivingEntity entity) {
        float h = entity.getBbHeight();
        if (h <= 0.01f) return 0.0625f;
        return Math.max(0f, Math.min(0.4f, 0.0625f + (h - 1.4f) * 0.06f));
    }

    private LivingEntity previewFor(OWArenaFighter f) {
        // La clé inclut la variante autant que le skin : deux créatures de même skin mais de
        // variantes différentes n'ont pas la même apparence.
        String key = f.entityTypeId() + ":" + f.typeVariant() + ":" + f.skinIndex();
        LivingEntity cached = previewCache.get(key);
        if (cached != null) return cached;

        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return null;
        ResourceLocation rl = ResourceLocation.tryParse(f.entityTypeId());
        if (rl == null) return null;
        EntityType<?> type = BuiltInRegistries.ENTITY_TYPE.getOptional(rl).orElse(null);
        if (type == null) return null;
        if (!(type.create(mc.level) instanceof LivingEntity living)) return null;
        if (living instanceof OWEntity owE) {
            // Variante PUIS skin : la variante porte le pelage naturel, le skin le cosmétique.
            owE.setVariant(owE, f.typeVariant());
            owE.changeSkinSilent(f.skinIndex());
            owE.setLevel(f.level());
        }
        living.yBodyRot = 0f;
        living.setYRot(0f);
        living.setXRot(0f);
        living.yHeadRot = 0f;

        previewCache.put(key, living);
        return living;
    }

    @Override
    public boolean isPauseScreen() { return false; }
}
