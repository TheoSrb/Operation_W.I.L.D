package net.tiew.operationWild.screen.entity.piste;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.tiew.operationWild.OperationWild;
import net.tiew.operationWild.entity.OWEntity;
import net.tiew.operationWild.entity.piste.OWPisteAttacks;
import net.tiew.operationWild.entity.piste.OWPisteGraph;
import net.tiew.operationWild.entity.piste.OWPisteGraphs;
import net.tiew.operationWild.entity.piste.OWPisteNode;
import net.tiew.operationWild.entity.piste.OWPisteRules;
import net.tiew.operationWild.event.ClientEvents;
import net.tiew.operationWild.gui.OWEntityHud;
import net.tiew.operationWild.networking.OWNetworkHandler;
import net.tiew.operationWild.networking.packets.to_server.OWPisteAdvancePacket;
import net.tiew.operationWild.networking.packets.to_server.OpenOWInventoryPacket;
import net.tiew.operationWild.screen.entity.OWTabsRenderer;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * La Piste Sauvage : un labyrinthe de progression, propre à chaque individu, mais dont le tracé
 * est identique pour toute l'espèce (voir {@link OWPisteGraphs}). Le labyrinthe est affiché dans
 * le panneau d'interface de base (176x166), sur un fond de bloc tuilé (parallaxe, façon écran des
 * succès), rogné à sa zone intérieure. Il est parcourable au glisser et zoomable à la molette : le
 * labyrinthe entier est dessiné sous une <b>transformation unique</b> (translate → scale → pan),
 * ce qui rend le zoom continu (pas de tremblement d'arrondi) et fait échelonner tout le contenu,
 * « ? » compris. La tête de l'entité sert de pion sur le nœud courant.
 */
@OnlyIn(Dist.CLIENT)
public class OWPisteScreen extends Screen {

    private static final ResourceLocation BG_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "textures/gui/ow_options_screen.png");
    private static final ResourceLocation COIN_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "textures/misc/coin.png");
    private static final ResourceLocation COIN_TEXTURE_2 =
            ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "textures/misc/coin_2.png");
    private static final ResourceLocation COIN_TEXTURE_3 =
            ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "textures/misc/coin_3.png");
    private static final ResourceLocation ATTACKS_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "textures/misc/attacks.png");
    private static final ResourceLocation COIN_TEXTURE_4 =
            ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "textures/misc/coin_4.png");

    private static final ResourceLocation EXP_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "textures/misc/exp.png");
    private static final ResourceLocation EXP_TEXTURE_2 =
            ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "textures/misc/exp_2.png");
    private static final ResourceLocation EXP_TEXTURE_3 =
            ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "textures/misc/exp_3.png");
    private static final ResourceLocation EXP_TEXTURE_4 =
            ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "textures/misc/exp_4.png");

    /** Atlas 256×256 de l'Expérience d'Apprivoisement ; l'icône « patte » est en (110,12), 11×11. */
    private static final ResourceLocation TAMING_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID,
                    "textures/gui/mob_page/misc/ow_entity_journal_interface_taming_experience_gui.png");
    /** Couleur d'affichage du solde d'Expérience d'Apprivoisement (fauve/cuir). */
    private static final int TAMING_COLOR = 0xD9B45A;

    /** Pièce dont l'aspect grossit avec le montant : <5 classique, 5-9 moyenne, ≥10 grosse. */
    private static ResourceLocation coinTextureFor(int amount) {
        if (amount >= 10) return COIN_TEXTURE_4;
        if (amount >= 5) return COIN_TEXTURE_3;
        if (amount >= 3) return COIN_TEXTURE_2;
        return COIN_TEXTURE;
    }
    private static ResourceLocation expTextureFor(int amount) {
        if (amount >= 75) return EXP_TEXTURE_4;
        if (amount >= 50) return EXP_TEXTURE_3;
        if (amount >= 25) return EXP_TEXTURE_2;
        return EXP_TEXTURE;
    }
    private static final ResourceLocation TILE_TEXTURE =
            ResourceLocation.withDefaultNamespace("textures/block/moss_block.png");

    private static final int IMG_W = 176;
    private static final int IMG_H = 166;

    private static final int VP_LEFT = 7;
    private static final int VP_TOP = 18;
    private static final int VP_RIGHT = IMG_W - 7;   // 169
    private static final int VP_BOTTOM = IMG_H - 8;  // 158

    // Tailles exprimées en unités « canvas » (échelonnées par le zoom via la PoseStack).
    private static final int NODE_HALF = 9;
    private static final int STEP_HALF = 5;
    private static final int CORRIDOR = 2;

    private static final double DEFAULT_ZOOM = 0.85;
    private static final double MIN_ZOOM = 0.4;
    private static final double MAX_ZOOM = 1.7;

    private final OWEntity entity;
    private final OWPisteGraph graph;
    private final OWTabsRenderer tabsRenderer = new OWTabsRenderer();

    private int panelX;
    private int panelY;

    private double panX;
    private double panY;
    private double zoom = DEFAULT_ZOOM;
    private boolean dragged = false;
    private boolean returning = false;

    // Animation du pion : il suit le VRAI chemin (nœud par nœud, le long des couloirs).
    private int displayedNode = -1;
    private double tokenX, tokenY;
    private boolean tokenAnimating = false;
    private int animTargetNode = -1;
    private List<double[]> animPoints;   // polyligne à suivre (coordonnées canvas)
    private double[] animCum;            // longueurs cumulées à chaque point
    private double animTotal, animTravelled;
    private long lastMillis;

    // Mini-fenêtre de choix définitif (ouverte sur un palier à choix).
    private OWPisteNode pendingChoice = null;
    // Mini-fenêtre de confirmation (ouverte sur un palier de récompense simple).
    private OWPisteNode pendingConfirm = null;
    // Fenêtre de choix d'attaque + confirmation secondaire (index d'attaque en cours de validation, -1 = aucune).
    private OWPisteNode pendingAttack = null;
    private int pendingAttackConfirm = -1;
    // Un clic-labyrinthe a démarré sur le labyrinthe (anti clic-fantôme au relâchement quand une fenêtre se ferme).
    private boolean armedMazeClick = false;
    // Boutons vanilla des mini-fenêtres (style identique à l'achat de skin).
    private Button dlgYes, dlgNo, dlgOptA, dlgOptB, dlgChoisirA, dlgChoisirB, dlgAttackClose;
    private int boxX, boxY, boxW, boxH;   // dernière géométrie de la boîte (clic hors-boîte = annulation)
    private int hoveredNodeId = -1;       // palier sous la souris (effet de survol)

    public OWPisteScreen() {
        super(Component.translatable("piste.title"));
        Minecraft mc = Minecraft.getInstance();
        this.entity = (mc.player != null && mc.player.getRootVehicle() instanceof OWEntity e) ? e : null;
        this.graph = OWPisteGraphs.forEntity(entity);
    }

    @Override
    protected void init() {
        super.init();
        this.panelX = (this.width - IMG_W) / 2;
        this.panelY = (this.height - IMG_H) / 2;

        if (graph != null && entity != null) {
            OWPisteNode current = graph.get(entity.getPisteCurrentNode());
            if (current == null) current = graph.get(graph.getStartId());
            if (current != null) {
                panX = -current.getX();
                panY = -current.getY();
                displayedNode = current.getId();
                tokenX = current.getX();
                tokenY = current.getY();
            }
        }
        lastMillis = System.currentTimeMillis();

        if (entity != null) {
            tabsRenderer.init(this.width, this.height, IMG_W, IMG_H, entity, this::addRenderableWidget);
            tabsRenderer.setActiveTab(OWTabsRenderer.Tab.PISTE);
        }

        // Boutons vanilla des mini-fenêtres (rendus / cliqués manuellement, pas ajoutés à l'écran).
        dlgYes = Button.builder(Component.translatable("tooltip.yesButton")
                        .setStyle(Style.EMPTY.withColor(TextColor.fromRgb(0x7DDD73)).withBold(true)),
                b -> {
                    if (pendingAttack != null && pendingAttackConfirm >= 0) {
                        OWNetworkHandler.sendToServer(new OWPisteAdvancePacket(pendingAttack.getId(), pendingAttackConfirm));
                        pendingAttack = null; pendingAttackConfirm = -1;
                    } else if (pendingConfirm != null) {
                        OWNetworkHandler.sendToServer(new OWPisteAdvancePacket(pendingConfirm.getId(), -1));
                        pendingConfirm = null;
                    }
                }).bounds(0, 0, 60, 20).build();
        dlgNo = Button.builder(Component.translatable("tooltip.noButton")
                        .setStyle(Style.EMPTY.withColor(TextColor.fromRgb(0xDD4444)).withBold(true)),
                b -> {
                    if (pendingAttackConfirm >= 0) pendingAttackConfirm = -1;   // retour à la sélection d'attaque
                    else pendingConfirm = null;
                }).bounds(0, 0, 60, 20).build();
        dlgOptA = Button.builder(Component.empty(),
                b -> { if (pendingChoice != null) {
                    OWNetworkHandler.sendToServer(new OWPisteAdvancePacket(pendingChoice.getId(), 0));
                    pendingChoice = null;
                } }).bounds(0, 0, 150, 20).build();
        dlgOptB = Button.builder(Component.empty(),
                b -> { if (pendingChoice != null) {
                    OWNetworkHandler.sendToServer(new OWPisteAdvancePacket(pendingChoice.getId(), 1));
                    pendingChoice = null;
                } }).bounds(0, 0, 150, 20).build();
        dlgChoisirA = Button.builder(Component.translatable("piste.attack.button")
                        .setStyle(Style.EMPTY.withBold(true)),
                b -> pendingAttackConfirm = 0).bounds(0, 0, 90, 20).build();
        dlgChoisirB = Button.builder(Component.translatable("piste.attack.button")
                        .setStyle(Style.EMPTY.withBold(true)),
                b -> pendingAttackConfirm = 1).bounds(0, 0, 90, 20).build();
        dlgAttackClose = Button.builder(Component.literal("X")
                        .setStyle(Style.EMPTY.withColor(TextColor.fromRgb(0xDD4444)).withBold(true)),
                b -> pendingAttack = null).bounds(0, 0, 16, 16).build();
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void onClose() {
        returnToInventory();
    }

    private void returnToInventory() {
        if (returning) return;
        returning = true;
        OWNetworkHandler.sendToServer(new OpenOWInventoryPacket());
    }

    // --- Conversion canvas ↔ écran ---

    private double viewportCenterX() { return panelX + (VP_LEFT + VP_RIGHT) / 2.0; }

    private double viewportCenterY() { return panelY + (VP_TOP + VP_BOTTOM) / 2.0; }

    private double mouseToCanvasX(double mouseX) { return (mouseX - viewportCenterX()) / zoom - panX; }

    private double mouseToCanvasY(double mouseY) { return (mouseY - viewportCenterY()) / zoom - panY; }

    private static int half(OWPisteNode node) {
        return node.getType() == OWPisteNode.Type.STEP ? STEP_HALF : NODE_HALF;
    }

    private boolean inViewport(double mx, double my) {
        return mx >= panelX + VP_LEFT && mx <= panelX + VP_RIGHT
                && my >= panelY + VP_TOP && my <= panelY + VP_BOTTOM;
    }

    private OWPisteNode nodeAt(double mouseX, double mouseY) {
        double cx = mouseToCanvasX(mouseX), cy = mouseToCanvasY(mouseY);
        for (OWPisteNode node : graph.nodes()) {
            int h = half(node);
            if (Math.abs(cx - node.getX()) <= h && Math.abs(cy - node.getY()) <= h) return node;
        }
        return null;
    }

    // --- États d'un nœud (lus en direct sur l'entité synchronisée) ---

    private boolean isReachable(int nodeId) {
        OWPisteNode n = (graph == null) ? null : graph.get(nodeId);
        return n != null && OWPisteRules.reachable(entity, graph, n);
    }

    /** Récompense finale atteignable structurellement mais encore bloquée par la porte de complétion. */
    private boolean isGatedFinal(OWPisteNode node) {
        return OWPisteRules.isBlockedByGate(entity, graph, node);
    }

    private boolean isAffordable(OWPisteNode node) {
        return entity != null
                && entity.getLevel() >= node.getRequiredLevel()
                && ClientEvents.tamingExperience >= node.getCost();
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        // Assombrit le monde derrière (même rendu que l'inventaire / la tribu).
        this.renderBackground(graphics, mouseX, mouseY, partialTick);
        super.render(graphics, mouseX, mouseY, partialTick);

        graphics.blit(BG_TEXTURE, panelX, panelY, 0, 0, IMG_W, IMG_H);

        if (entity == null) return;

        if (graph == null) {
            renderHeader(graphics);
            graphics.drawCenteredString(this.font,
                    Component.translatable("piste.unavailable"),
                    panelX + IMG_W / 2, panelY + IMG_H / 2, 0xFFAAAAAA);
            tabsRenderer.renderTabs(graphics, this.font, entity, panelX, panelY, mouseX, mouseY);
            return;
        }

        // Palier survolé (pas pendant une mini-fenêtre).
        hoveredNodeId = -1;
        if (!anyModalOpen() && inViewport(mouseX, mouseY)) {
            OWPisteNode h = nodeAt(mouseX, mouseY);
            if (h != null) hoveredNodeId = h.getId();
        }

        graphics.enableScissor(panelX + VP_LEFT, panelY + VP_TOP, panelX + VP_RIGHT, panelY + VP_BOTTOM);
        renderTiledBackground(graphics);

        // Une seule transformation : tout le labyrinthe se dessine en coordonnées canvas.
        graphics.pose().pushPose();
        graphics.pose().translate(viewportCenterX(), viewportCenterY(), 0);
        graphics.pose().scale((float) zoom, (float) zoom, 1f);
        graphics.pose().translate(panX, panY, 0);

        renderCorridors(graphics);
        renderNodes(graphics);
        renderToken(graphics);

        graphics.pose().popPose();
        graphics.disableScissor();

        renderHeader(graphics);
        tabsRenderer.renderTabs(graphics, this.font, entity, panelX, panelY, mouseX, mouseY);

        // Tooltip et modales élevés sur l'axe Z (au-dessus du texte des nœuds, « ? » compris).
        graphics.pose().pushPose();
        graphics.pose().translate(0, 0, 400);
        if (!anyModalOpen() && inViewport(mouseX, mouseY)) {
            OWPisteNode hovered = nodeAt(mouseX, mouseY);
            if (hovered != null) renderRewardTooltip(graphics, hovered, mouseX, mouseY);
        }
        if (pendingChoice != null) renderChoiceModal(graphics, mouseX, mouseY);
        if (pendingConfirm != null) renderConfirmModal(graphics, mouseX, mouseY);
        if (pendingAttack != null) renderAttackModal(graphics, mouseX, mouseY);
        if (pendingAttackConfirm >= 0) {
            // Confirmation élevée encore au-dessus de la fenêtre de choix (texte des nœuds/colonnes inclus).
            graphics.pose().pushPose();
            graphics.pose().translate(0, 0, 150);
            renderAttackConfirm(graphics, mouseX, mouseY);
            graphics.pose().popPose();
        }
        graphics.pose().popPose();
    }

    /** Fond en texture de bloc tuilée, assombrie, décalée par le pan (parallaxe). */
    private void renderTiledBackground(GuiGraphics graphics) {
        int left = panelX + VP_LEFT, top = panelY + VP_TOP, right = panelX + VP_RIGHT, bottom = panelY + VP_BOTTOM;
        int tile = 16;
        int ox = Math.floorMod((int) Math.round(panX * zoom), tile);
        int oy = Math.floorMod((int) Math.round(panY * zoom), tile);

        RenderSystem.setShaderColor(0.34f, 0.38f, 0.30f, 1.0f);
        for (int y = top - oy - tile; y < bottom + tile; y += tile) {
            for (int x = left - ox - tile; x < right + tile; x += tile) {
                graphics.blit(TILE_TEXTURE, x, y, tile, tile, 0f, 0f, 16, 16, 16, 16);
            }
        }
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        graphics.fill(left, top, right, bottom, 0x55000000);
    }

    // Ces méthodes dessinent en coordonnées canvas (sous la PoseStack).

    private void renderCorridors(GuiGraphics graphics) {
        // Passe 1 : contours (évite les coutures entre segments en L).
        for (OWPisteNode node : graph.nodes()) {
            int x1 = node.getX(), y1 = node.getY();
            for (int childId : node.getChildren()) {
                OWPisteNode child = graph.get(childId);
                if (child == null) continue;
                int x2 = child.getX(), y2 = child.getY();
                int outline = entity.isPisteNodeLocked(childId) ? 0xFF1F0C0C : 0xFF15170D;
                int co = CORRIDOR + 1;
                fillRange(graphics, x1, x2, y1 - co, y1 + co, outline);                 // segment horizontal
                fillRange(graphics, x2 - co, x2 + co, y1, y2, outline);                 // segment vertical
                fillRange(graphics, x2 - co, x2 + co, y1 - co, y1 + co, outline);       // carré du coude
            }
        }
        // Passe 2 : cœurs colorés selon l'état.
        for (OWPisteNode node : graph.nodes()) {
            int x1 = node.getX(), y1 = node.getY();
            for (int childId : node.getChildren()) {
                OWPisteNode child = graph.get(childId);
                if (child == null) continue;
                int x2 = child.getX(), y2 = child.getY();
                boolean lit = entity.isPisteNodeUnlocked(node.getId()) && entity.isPisteNodeUnlocked(childId);
                boolean dead = entity.isPisteNodeLocked(childId);
                int core = dead ? 0xFF6A2A2A : lit ? 0xFFB6E45A : 0xFF57574B;
                fillRange(graphics, x1, x2, y1 - CORRIDOR, y1 + CORRIDOR, core);            // horizontal
                fillRange(graphics, x2 - CORRIDOR, x2 + CORRIDOR, y1, y2, core);            // vertical
                fillRange(graphics, x2 - CORRIDOR, x2 + CORRIDOR, y1 - CORRIDOR, y1 + CORRIDOR, core); // coude

                // Relief sur tous les tronçons : liseré clair (haut/gauche) + ombre foncée (bas/droite).
                // Vert clair/foncé pour les parcourus, gris clair/foncé pour les non parcourus.
                int hl, sh;
                if (dead) { hl = 0xFF8A3A3A; sh = 0xFF3A1414; }
                else if (lit) { hl = 0xFFE9F5A2; sh = 0xFF5F9A2A; }
                else { hl = 0xFF74746A; sh = 0xFF3A3A32; }
                fillRange(graphics, x1, x2, y1 - CORRIDOR, y1 - CORRIDOR + 1, hl);          // haut horizontal
                fillRange(graphics, x1, x2, y1 + CORRIDOR - 1, y1 + CORRIDOR, sh);          // bas horizontal
                fillRange(graphics, x2 - CORRIDOR, x2 - CORRIDOR + 1, y1, y2, hl);          // gauche vertical
                fillRange(graphics, x2 + CORRIDOR - 1, x2 + CORRIDOR, y1, y2, sh);          // droite vertical
                fillRange(graphics, x2 - CORRIDOR, x2 + CORRIDOR, y1 - CORRIDOR, y1 - CORRIDOR + 1, hl); // coude haut
                fillRange(graphics, x2 - CORRIDOR, x2 + CORRIDOR, y1 + CORRIDOR - 1, y1 + CORRIDOR, sh); // coude bas
            }
        }
    }

    private void renderNodes(GuiGraphics graphics) {
        boolean blink = (entity.tickCount / 8) % 2 == 0;

        for (OWPisteNode node : graph.nodes()) {
            int cx = node.getX(), cy = node.getY();
            int h = half(node);
            int left = cx - h, top = cy - h, right = cx + h, bottom = cy + h;

            boolean unlocked = entity.isPisteNodeUnlocked(node.getId());
            boolean lockedForever = entity.isPisteNodeLocked(node.getId());
            boolean reachable = isReachable(node.getId());
            boolean affordable = reachable && isAffordable(node);
            boolean gatedFinal = isGatedFinal(node);
            // La récompense finale montre toujours son butin (objectif visible), même verrouillée.
            boolean revealed = unlocked || reachable || lockedForever || node.isRequiresAll();

            int fill, border;
            if (node.getType() == OWPisteNode.Type.START || unlocked) {
                fill = 0xFF2F4A22; border = 0xFFB8E45A;
            } else if (lockedForever) {
                fill = 0xFF3A1414; border = 0xFF7A2626;
            } else if (gatedFinal) {
                // Finale en vue mais verrouillée : cadenas doré terne (pas de clignotement).
                fill = 0xFF3A3320; border = 0xFF9A8A46;
            } else if (affordable) {
                fill = 0xFF4A4220; border = blink ? 0xFFF3E27F : 0xFFB8A94A;
            } else if (reachable) {
                fill = 0xFF33291A; border = 0xFF8A7A52;
            } else {
                fill = 0xFF191914; border = 0xFF34342A;
            }

            graphics.fill(left + 1, top + 1, right + 1, bottom + 1, 0x66000000);
            graphics.fill(left, top, right, bottom, fill);
            drawBorder(graphics, left, top, right, bottom, border);
            graphics.fill(left + 1, top + 1, right - 1, top + 2, (border & 0x00FFFFFF) | 0x55FFFFFF);

            if (revealed) {
                renderNodeIcon(graphics, node, cx, cy, h, lockedForever);
            } else if (node.getType() != OWPisteNode.Type.STEP && node.getType() != OWPisteNode.Type.START) {
                graphics.drawCenteredString(this.font, "?", cx, cy - 4, 0xFF7A7A60);
            }

            if (lockedForever) {
                graphics.fill(left + 3, cy - 1, right - 3, cy + 1, 0xFFB84040);
            }

            // Effet de survol : liseré clair autour + léger éclaircissement.
            if (node.getId() == hoveredNodeId) {
                graphics.fill(left, top, right, bottom, 0x26FFFFFF);
                drawBorder(graphics, left - 1, top - 1, right + 1, bottom + 1, 0xC0FFFFFF);
            }

            // Petit cadenas sur la finale encore verrouillée par la porte de complétion.
            if (gatedFinal) {
                int ly = cy + 2;
                graphics.fill(cx - 3, ly, cx + 3, ly + 4, 0xFF241F14);      // corps
                graphics.fill(cx - 2, ly - 3, cx - 1, ly, 0xFFC9B45E);      // anse gauche
                graphics.fill(cx + 1, ly - 3, cx + 2, ly, 0xFFC9B45E);      // anse droite
                graphics.fill(cx - 2, ly - 3, cx + 2, ly - 2, 0xFFC9B45E);  // anse haut
            }
        }
    }

    private void renderNodeIcon(GuiGraphics graphics, OWPisteNode node, int cx, int cy, int h, boolean grey) {
        if (node.getType() == OWPisteNode.Type.ATTACK) {
            int r = Math.max(4, h - 2);
            ResourceLocation icon = ATTACKS_TEXTURE;
            int chosen = entity.getPisteChoice(node.getId());
            if (chosen >= 0 && chosen < node.getAttackIds().size()) {
                OWPisteAttacks.AttackDef d = OWPisteAttacks.get(node.getAttackIds().get(chosen));
                if (d != null) icon = d.icon();
            }
            if (grey) RenderSystem.setShaderColor(0.5f, 0.5f, 0.5f, 1.0f);
            graphics.blit(icon, cx - r, cy - r, r * 2, r * 2, 0f, 0f, 16, 16, 16, 16);
            if (grey) RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
            return;
        }
        if (node.hasChoice()) {
            int chosen = entity.getPisteChoice(node.getId());
            if (chosen >= 0 && chosen < node.getOptions().size()) {
                OWPisteNode.Option o = node.getOptions().get(chosen);
                drawTypeIcon(graphics, o.type(), o.amount(), cx, cy, h, grey);   // choix déjà fait
            } else {
                // Marqueur « choix » : deux demi-pastilles (XP vert / Pièces or).
                graphics.fill(cx - 4, cy - 4, cx, cy + 4, grey ? 0xFF4A5A3A : 0xFF7FE04A);
                graphics.fill(cx, cy - 4, cx + 4, cy + 4, grey ? 0xFF6A5A2A : 0xFFF3C83B);
                graphics.fill(cx - 1, cy - 4, cx + 1, cy + 4, 0xFF201810);
            }
            return;
        }
        drawTypeIcon(graphics, node.getType(), node.getRewardAmount(), cx, cy, h, grey);
    }

    private void drawTypeIcon(GuiGraphics graphics, OWPisteNode.Type type, int amount, int cx, int cy, int h, boolean grey) {
        switch (type) {
            case COINS -> {
                int r = Math.max(3, h - 3);
                if (grey) RenderSystem.setShaderColor(0.5f, 0.5f, 0.5f, 1.0f);
                graphics.blit(coinTextureFor(amount), cx - r, cy - r, r * 2, r * 2, 0f, 0f, 16, 16, 16, 16);
                if (grey) RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
            }
            case XP -> {
                int r = Math.max(3, h - 3);
                if (grey) RenderSystem.setShaderColor(0.5f, 0.5f, 0.5f, 1.0f);
                graphics.blit(expTextureFor(amount), cx - r, cy - r, r * 2, r * 2, 0f, 0f, 16, 16, 16, 16);
                if (grey) RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
            }
            case STEP -> graphics.fill(cx - 2, cy - 2, cx + 2, cy + 2, 0xFF8A8A72);
            default -> { /* START : le pion vient s'y poser */ }
        }
    }

    /** Le pion : la tête de l'entité, animée le long du couloir (dessiné sous la PoseStack). */
    private void renderToken(GuiGraphics graphics) {
        updateTokenAnimation();
        int cx = (int) Math.round(tokenX), cy = (int) Math.round(tokenY);

        graphics.fill(cx - 10, cy - 10, cx + 10, cy + 10, 0x33FFF0A0);

        OWEntityHud.EntityIconData icon = OWEntityHud.getEntityIconData(entity);
        if (icon != null) {
            graphics.blit(OWEntityHud.HUD, cx - icon.width / 2, cy - icon.height / 2,
                    icon.textureX, icon.textureY, icon.width, icon.height);
        } else {
            graphics.fill(cx - 4, cy - 4, cx + 4, cy + 4, 0xFFFFFFFF);
        }
    }

    /** Fait avancer le pion en douceur vers le nœud courant, en suivant le vrai chemin. */
    private void updateTokenAnimation() {
        long now = System.currentTimeMillis();
        double dt = Math.min(0.1, Math.max(0, (now - lastMillis) / 1000.0));
        lastMillis = now;

        int cur = entity.getPisteCurrentNode();
        if (!tokenAnimating && cur != displayedNode) beginTokenAnim(cur);

        if (tokenAnimating) {
            animTravelled += dt * 130.0; // vitesse (unités canvas / s)
            if (animPoints == null || animPoints.size() < 2 || animTravelled >= animTotal) {
                if (animPoints != null && !animPoints.isEmpty()) {
                    double[] last = animPoints.get(animPoints.size() - 1);
                    tokenX = last[0]; tokenY = last[1];
                }
                displayedNode = animTargetNode;
                tokenAnimating = false;
            } else {
                int seg = 0;
                while (seg < animCum.length - 2 && animTravelled > animCum[seg + 1]) seg++;
                double segLen = animCum[seg + 1] - animCum[seg];
                double t = segLen <= 0 ? 1 : (animTravelled - animCum[seg]) / segLen;
                double[] a = animPoints.get(seg), b = animPoints.get(seg + 1);
                tokenX = lerp(a[0], b[0], t);
                tokenY = lerp(a[1], b[1], t);
            }
        }
    }

    /** Construit la polyligne à suivre : le chemin réel (BFS dans le graphe) puis les couloirs en L. */
    private void beginTokenAnim(int cur) {
        OWPisteNode to = graph.get(cur);
        if (to == null) { displayedNode = cur; return; }

        animTargetNode = cur;
        animPoints = new ArrayList<>();
        animPoints.add(new double[]{tokenX, tokenY});                 // départ = position actuelle du pion

        List<Integer> path = computeNodePath(displayedNode, cur);
        if (path == null || path.size() < 2) {
            animPoints.add(new double[]{to.getX(), to.getY()});       // repli : trajet direct
        } else {
            for (int k = 0; k < path.size() - 1; k++) {
                OWPisteNode a = graph.get(path.get(k)), b = graph.get(path.get(k + 1));
                double cornerX, cornerY;
                if (graph.isAdjacent(a.getId(), b.getId())) {         // a parent : horizontal puis vertical
                    cornerX = b.getX(); cornerY = a.getY();
                } else {                                              // b parent : vertical puis horizontal
                    cornerX = a.getX(); cornerY = b.getY();
                }
                animPoints.add(new double[]{cornerX, cornerY});
                animPoints.add(new double[]{b.getX(), b.getY()});
            }
        }

        animCum = new double[animPoints.size()];
        animCum[0] = 0;
        for (int k = 1; k < animPoints.size(); k++) {
            double[] p0 = animPoints.get(k - 1), p1 = animPoints.get(k);
            animCum[k] = animCum[k - 1] + Math.abs(p1[0] - p0[0]) + Math.abs(p1[1] - p0[1]);
        }
        animTotal = animCum[animCum.length - 1];
        animTravelled = 0;
        tokenAnimating = true;
        if (animTotal <= 0.001) { tokenX = to.getX(); tokenY = to.getY(); displayedNode = cur; tokenAnimating = false; }
    }

    /** Plus court chemin de {@code from} à {@code to} sur les couloirs (adjacence non orientée). */
    private List<Integer> computeNodePath(int from, int to) {
        if (from == to || graph.get(from) == null) return null;
        Map<Integer, Integer> prev = new HashMap<>();
        ArrayDeque<Integer> q = new ArrayDeque<>();
        q.add(from); prev.put(from, -1);
        while (!q.isEmpty()) {
            int n = q.poll();
            if (n == to) break;
            for (int m : neighbors(n)) {
                if (!prev.containsKey(m)) { prev.put(m, n); q.add(m); }
            }
        }
        if (!prev.containsKey(to)) return null;
        LinkedList<Integer> path = new LinkedList<>();
        for (int at = to; at != -1; at = prev.get(at)) path.addFirst(at);
        return path;
    }

    private List<Integer> neighbors(int id) {
        List<Integer> res = new ArrayList<>();
        OWPisteNode n = graph.get(id);
        if (n != null) res.addAll(n.getChildren());
        for (OWPisteNode p : graph.parentsOf(id)) res.add(p.getId());
        return res;
    }

    private static double lerp(double a, double b, double t) { return a + (b - a) * t; }

    private void renderHeader(GuiGraphics graphics) {
        graphics.drawCenteredString(this.font, this.title, panelX + IMG_W / 2, panelY + 6, 0xFFFFFF);
        if (entity != null && graph != null) {
            // % de complétion, seul, en haut à droite du panneau (pas de fond).
            String pct = completionPercent() + "%";
            graphics.drawString(this.font, pct, panelX + IMG_W - 6 - this.font.width(pct), panelY + 6, 0xB8E45A, true);

            // Solde d'Expérience d'Apprivoisement (icône patte + nombre) en bas à droite du panneau.
            String taming = String.valueOf((int) ClientEvents.tamingExperience);
            int iconX = panelX + IMG_W - 6 - 10;
            int iconY = panelY + IMG_H - 13;
            graphics.blit(TAMING_TEXTURE, iconX, iconY, 10, 10, 110f, 12f, 11, 11, 256, 256);
            graphics.drawString(this.font, taming, iconX - 2 - this.font.width(taming),
                    panelY + IMG_H - 12, TAMING_COLOR, true);
        }
    }

    /** Pourcentage de nœuds débloqués (hors départ). */
    private int completionPercent() {
        int total = 0, done = 0;
        for (OWPisteNode node : graph.nodes()) {
            if (node.getType() == OWPisteNode.Type.START) continue;
            total++;
            if (entity.isPisteNodeUnlocked(node.getId())) done++;
        }
        return total == 0 ? 100 : Math.round(done * 100f / total);
    }

    // --- Tooltip de récompense, panneau custom stylé (dessiné en espace écran) ---

    private void renderRewardTooltip(GuiGraphics graphics, OWPisteNode node, int mouseX, int mouseY) {
        boolean unlocked = entity.isPisteNodeUnlocked(node.getId());
        boolean lockedForever = entity.isPisteNodeLocked(node.getId());
        boolean reachable = isReachable(node.getId());
        boolean gatedFinal = isGatedFinal(node);
        boolean revealed = unlocked || reachable || lockedForever || node.isRequiresAll();

        int accent = switch (node.getType()) {
            case XP -> 0x7FE04A;
            case COINS -> 0xF3C83B;
            case STEP -> 0x9AA0A6;
            case START -> 0xB8E45A;
            case ATTACK -> 0xC9A0E8;
        };

        // Ligne titre (récompense / nature du nœud).
        Component title;
        boolean drawCoinIcon = false, drawXpIcon = false;
        switch (node.getType()) {
            case START -> title = Component.translatable("piste.start");
            case STEP -> title = Component.translatable("piste.step");
            case ATTACK -> title = Component.translatable("piste.attack");
            case XP -> {
                title = revealed ? Component.translatable("piste.reward.xp", node.getRewardAmount())
                        : Component.translatable("piste.reward.hidden");
                drawXpIcon = revealed;
            }
            case COINS -> {
                title = revealed ? Component.translatable("piste.reward.coins", node.getRewardAmount())
                        : Component.translatable("piste.reward.hidden");
                drawCoinIcon = revealed;
            }
            default -> title = Component.empty();
        }

        // Palier à choix non débloqué : titre générique (les options sont montrées dans la mini-fenêtre).
        if (node.hasChoice() && !unlocked) {
            title = Component.translatable("piste.choice");
            drawCoinIcon = false;
            drawXpIcon = false;
        }

        // Ligne coût (si applicable).
        Component costLine = (node.getType() != OWPisteNode.Type.START && !unlocked)
                ? Component.translatable("piste.cost", node.getCost()) : null;

        // Ligne statut.
        Component status = null;
        int statusColor = 0xFFFFFF;
        if (lockedForever) {
            status = Component.translatable("piste.locked"); statusColor = 0xE06666;
        } else if (unlocked && node.getType() != OWPisteNode.Type.START) {
            status = Component.translatable("piste.owned"); statusColor = 0x9AD16B;
        } else if (gatedFinal) {
            status = Component.translatable("piste.explore_all"); statusColor = 0xE0B24A;
        } else if (reachable) {
            if (entity.getLevel() < node.getRequiredLevel()) {
                status = Component.translatable("piste.need_level", node.getRequiredLevel()); statusColor = 0xFF6B6B;
            } else if (ClientEvents.tamingExperience < node.getCost()) {
                status = Component.translatable("piste.not_enough"); statusColor = 0xFF6B6B;
            } else {
                status = Component.translatable("piste.reachable"); statusColor = 0x9AD16B;
            }
        }

        // Titre stylé (gras) : on mesure la version réellement affichée pour éviter tout débordement.
        Component boldTitle = title.copy().withStyle(Style.EMPTY.withColor(TextColor.fromRgb(accent)).withBold(true));

        // Mesures.
        int pad = 6;
        int iconW = (drawCoinIcon || drawXpIcon) ? 12 : 0;
        int titleW = iconW + (iconW > 0 ? 4 : 0) + this.font.width(boldTitle);
        int contentW = titleW;
        if (costLine != null) contentW = Math.max(contentW, this.font.width(costLine) + 8 + 12);
        if (status != null) contentW = Math.max(contentW, this.font.width(status) + 8);

        int rows = 1 + (costLine != null ? 1 : 0) + (status != null ? 1 : 0);
        int boxW = contentW + pad * 2;
        int boxH = pad * 2 + 12 /*titre*/ + (rows - 1) * 11 + (rows > 1 ? 3 : 0) /*séparateur*/;

        // Position près du curseur, bornée à l'écran.
        int bx = mouseX + 12;
        int by = mouseY - boxH - 6;
        if (bx + boxW > this.width) bx = mouseX - boxW - 12;
        if (bx < 2) bx = 2;
        if (by < 2) by = mouseY + 14;
        if (by + boxH > this.height) by = this.height - boxH - 2;

        // Fond : ombre, corps, liseré d'accent en haut/bas, bordure fine.
        graphics.fill(bx - 1, by - 1, bx + boxW + 1, by + boxH + 1, 0xF0060504);
        graphics.fill(bx, by, bx + boxW, by + boxH, 0xF2181410);
        graphics.fill(bx, by, bx + boxW, by + 2, 0xFF000000 | accent);
        graphics.fill(bx, by + boxH - 1, bx + boxW, by + boxH, (0x66 << 24) | (accent & 0x00FFFFFF));
        drawBorder(graphics, bx, by, bx + boxW, by + boxH, (0x88 << 24) | (accent & 0x00FFFFFF));

        int cx = bx + pad;
        int cy = by + pad + 1;

        // Titre + icône.
        if (drawCoinIcon) {
            graphics.blit(coinTextureFor(node.getRewardAmount()), cx, cy - 1, 11, 11, 0f, 0f, 16, 16, 16, 16);
        } else if (drawXpIcon) {
            int r = 5;
            int ix = cx + 5, iy = cy + 4;
            graphics.fill(ix - r, iy - r, ix + r, iy + r, 0xFF2E7D32);
            graphics.fill(ix - r + 1, iy - r + 1, ix + r - 1, iy + r - 1, 0xFF7FE04A);
            graphics.fill(ix - r + 1, iy - r + 1, ix - r + 2, iy - r + 2, 0xFFCFFFB0);
        }
        int titleX = cx + (iconW > 0 ? iconW + 4 : 0);
        graphics.drawString(this.font, boldTitle, titleX, cy, 0xFFFFFF, true);

        int rowY = cy + 12;
        if (rows > 1) {
            graphics.fill(bx + 4, rowY, bx + boxW - 4, rowY + 1, 0x40FFFFFF);
            rowY += 3;
        }
        if (costLine != null) {
            Component styledCost = costLine.copy().withStyle(Style.EMPTY.withColor(TextColor.fromRgb(0xF3E27F)));
            graphics.drawString(this.font, styledCost, cx, rowY, 0xFFFFFF, true);
            graphics.blit(TAMING_TEXTURE, cx + this.font.width(styledCost) + 2, rowY - 1,
                    10, 10, 110f, 12f, 11, 11, 256, 256);
            rowY += 11;
        }
        if (status != null) {
            graphics.drawString(this.font, status.copy().withStyle(
                    Style.EMPTY.withColor(TextColor.fromRgb(statusColor)).withItalic(true)), cx, rowY, 0xFFFFFF, true);
        }
    }

    // --- Interaction : panoramique + zoom + clic sur un nœud ---

    private boolean anyModalOpen() {
        return pendingChoice != null || pendingConfirm != null || pendingAttack != null || pendingAttackConfirm >= 0;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        armedMazeClick = false;
        // Confirmation secondaire d'attaque (par-dessus la fenêtre d'attaque).
        if (pendingAttackConfirm >= 0) {
            if (button == 0) {
                if (dlgYes.mouseClicked(mouseX, mouseY, button)) return true;
                if (dlgNo.mouseClicked(mouseX, mouseY, button)) return true;
            }
            return true;
        }
        if (pendingAttack != null) {
            if (button == 0) {
                if (dlgAttackClose.mouseClicked(mouseX, mouseY, button)) return true;   // croix de fermeture
                if (!entity.isPisteNodeUnlocked(pendingAttack.getId())) {   // sélection : boutons "Choisir"
                    if (dlgChoisirA.mouseClicked(mouseX, mouseY, button)) return true;
                    if (dlgChoisirB.mouseClicked(mouseX, mouseY, button)) return true;
                }
                if (outsideBox(mouseX, mouseY)) pendingAttack = null;
            }
            return true;
        }
        if (pendingConfirm != null) {                // la mini-fenêtre capte tous les clics
            if (button == 0) {
                if (dlgYes.mouseClicked(mouseX, mouseY, button)) return true;
                if (dlgNo.mouseClicked(mouseX, mouseY, button)) return true;
                if (outsideBox(mouseX, mouseY)) pendingConfirm = null;
            }
            return true;
        }
        if (pendingChoice != null) {
            if (button == 0) {
                if (dlgOptA.mouseClicked(mouseX, mouseY, button)) return true;
                if (dlgOptB.mouseClicked(mouseX, mouseY, button)) return true;
                if (outsideBox(mouseX, mouseY)) pendingChoice = null;
            }
            return true;
        }
        if (super.mouseClicked(mouseX, mouseY, button)) return true; // onglets
        if (button == 0 && inViewport(mouseX, mouseY)) {
            dragged = false;
            armedMazeClick = true;                    // le clic a bien démarré sur le labyrinthe
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (button == 0 && !anyModalOpen() && inViewport(mouseX, mouseY)) {
            if (Math.abs(dragX) + Math.abs(dragY) > 1.5) dragged = true;
            panX += dragX / zoom;
            panY += dragY / zoom;
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        // Verrous : pas de clic-nœud si le clic n'a pas démarré sur le labyrinthe (fenêtre fermée juste avant),
        // ni tant que le pion n'est pas arrivé (animation en cours), ni pendant une fenêtre.
        if (button == 0 && !dragged && armedMazeClick && !tokenAnimating && graph != null && entity != null
                && !anyModalOpen() && inViewport(mouseX, mouseY)) {
            OWPisteNode node = nodeAt(mouseX, mouseY);
            if (node != null) {
                boolean unlocked = entity.isPisteNodeUnlocked(node.getId());
                boolean reward = node.getType() == OWPisteNode.Type.XP || node.getType() == OWPisteNode.Type.COINS;
                if (node.isAttackChoice() && (unlocked || (isReachable(node.getId()) && isAffordable(node)))) {
                    pendingAttack = node;                              // fenêtre d'attaque (sélection ou revue)
                } else if (unlocked) {
                    OWNetworkHandler.sendToServer(new OWPisteAdvancePacket(node.getId(), -1)); // repositionnement
                } else if (isReachable(node.getId()) && isAffordable(node)) {
                    if (node.hasChoice()) {
                        pendingChoice = node;                          // choix définitif
                    } else if (reward && node.getCost() > 0) {
                        pendingConfirm = node;                         // confirmation « récupérer contre X »
                    } else {
                        OWNetworkHandler.sendToServer(new OWPisteAdvancePacket(node.getId(), -1)); // palier neutre
                    }
                }
            }
        }
        armedMazeClick = false;
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (graph == null || scrollY == 0 || anyModalOpen()) return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);

        double oldZoom = zoom;
        double newZoom = Math.max(MIN_ZOOM, Math.min(MAX_ZOOM, zoom * (scrollY > 0 ? 1.1 : 1 / 1.1)));
        if (newZoom == oldZoom) return true;

        double ax = inViewport(mouseX, mouseY) ? mouseX : viewportCenterX();
        double ay = inViewport(mouseX, mouseY) ? mouseY : viewportCenterY();
        double canvasX = (ax - viewportCenterX()) / oldZoom - panX;
        double canvasY = (ay - viewportCenterY()) / oldZoom - panY;
        zoom = newZoom;
        panX = (ax - viewportCenterX()) / zoom - canvasX;
        panY = (ay - viewportCenterY()) / zoom - canvasY;
        return true;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == 256 && anyModalOpen()) {           // Échap : ferme la fenêtre du dessus
            if (pendingAttackConfirm >= 0) pendingAttackConfirm = -1;   // retour à la sélection d'attaque
            else if (pendingAttack != null) pendingAttack = null;
            else { pendingChoice = null; pendingConfirm = null; }
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    // --- Mini-fenêtres : rendues DANS le panneau de la Piste, texte à la ligne, boutons vanilla ---
    // (même esprit que la confirmation d'achat de skin : sur le panneau, pas une boîte flottante).

    /** Assombrit la zone intérieure du panneau et mémorise ses bornes (clic hors panneau = annulation). */
    private void beginPanelModal(GuiGraphics g) {
        int bx = panelX + VP_LEFT, by = panelY + VP_TOP, bw = VP_RIGHT - VP_LEFT, bh = VP_BOTTOM - VP_TOP;
        g.fill(bx, by, bx + bw, by + bh, 0xF0130F13);
        boxX = panelX; boxY = panelY; boxW = IMG_W; boxH = IMG_H;
    }

    private void renderConfirmModal(GuiGraphics graphics, int mouseX, int mouseY) {
        beginPanelModal(graphics);
        int inW = VP_RIGHT - VP_LEFT;
        int cxp = panelX + IMG_W / 2;
        boolean xp = pendingConfirm.getType() == OWPisteNode.Type.XP;
        int accent = xp ? 0x7FE04A : 0xF3C83B;
        int amount = pendingConfirm.getRewardAmount();

        int y = panelY + VP_TOP + 8;
        for (net.minecraft.util.FormattedCharSequence line : this.font.split(
                Component.translatable("piste.confirm_title").withStyle(Style.EMPTY.withBold(true)), inW - 8)) {
            graphics.drawCenteredString(this.font, line, cxp, y, 0xFFFFFF);
            y += 11;
        }
        y += 8;

        // Récompense (icône selon le montant + montant).
        Component rew = Component.translatable(xp ? "piste.reward.xp" : "piste.reward.coins", amount)
                .withStyle(Style.EMPTY.withColor(TextColor.fromRgb(accent)).withBold(true));
        int tw = 16 + this.font.width(rew);
        int sx = cxp - tw / 2;
        if (xp) {
            int r = 5, ix = sx + 5, cyy = y + 4;
            graphics.fill(ix - r, cyy - r, ix + r, cyy + r, 0xFF2E7D32);
            graphics.fill(ix - r + 1, cyy - r + 1, ix + r - 1, cyy + r - 1, 0xFF7FE04A);
        } else {
            graphics.blit(coinTextureFor(amount), sx, y - 1, 11, 11, 0f, 0f, 16, 16, 16, 16);
        }
        graphics.drawString(this.font, rew, sx + 16, y, 0xFFFFFF, true);
        y += 15;

        Component costComp = Component.translatable("piste.cost", pendingConfirm.getCost())
                .withStyle(Style.EMPTY.withColor(TextColor.fromRgb(0xF3E27F)));
        int ctw = this.font.width(costComp);
        int csx = cxp - (ctw + 2 + 10) / 2;
        graphics.drawString(this.font, costComp, csx, y, 0xFFFFFF, true);
        graphics.blit(TAMING_TEXTURE, csx + ctw + 2, y - 1, 10, 10, 110f, 12f, 11, 11, 256, 256);

        int btnW = 62, gap = 8, tot = btnW * 2 + gap, btnY = panelY + VP_BOTTOM - 26;
        dlgYes.setWidth(btnW); dlgYes.setPosition(cxp - tot / 2, btnY);
        dlgNo.setWidth(btnW);  dlgNo.setPosition(cxp - tot / 2 + btnW + gap, btnY);
        dlgYes.render(graphics, mouseX, mouseY, 0f);
        dlgNo.render(graphics, mouseX, mouseY, 0f);
    }

    private void renderChoiceModal(GuiGraphics graphics, int mouseX, int mouseY) {
        beginPanelModal(graphics);
        int inW = VP_RIGHT - VP_LEFT;
        int cxp = panelX + IMG_W / 2;
        setOptionLabel(dlgOptA, pendingChoice.getOptions().get(0));
        if (pendingChoice.getOptions().size() > 1) setOptionLabel(dlgOptB, pendingChoice.getOptions().get(1));

        int y = panelY + VP_TOP + 8;
        for (net.minecraft.util.FormattedCharSequence line : this.font.split(
                Component.translatable("piste.choice_title").withStyle(Style.EMPTY.withBold(true)), inW - 8)) {
            graphics.drawCenteredString(this.font, line, cxp, y, 0xFFFFFF);
            y += 11;
        }
        y += 3;
        for (net.minecraft.util.FormattedCharSequence line :
                this.font.split(Component.translatable("piste.choice_warning"), inW - 8)) {
            graphics.drawString(this.font, line, cxp - this.font.width(line) / 2, y, 0xE0A85A, false);
            y += 9;
        }
        y += 8;

        int ow = inW - 8, ax = cxp - ow / 2;
        dlgOptA.setWidth(ow); dlgOptA.setPosition(ax, y);
        dlgOptB.setWidth(ow); dlgOptB.setPosition(ax, y + 22);
        dlgOptA.render(graphics, mouseX, mouseY, 0f);
        dlgOptB.render(graphics, mouseX, mouseY, 0f);
    }

    private void setOptionLabel(Button b, OWPisteNode.Option opt) {
        boolean xp = opt.type() == OWPisteNode.Type.XP;
        b.setMessage(Component.translatable(xp ? "piste.reward.xp" : "piste.reward.coins", opt.amount())
                .withStyle(Style.EMPTY.withColor(TextColor.fromRgb(xp ? 0x7FE04A : 0xF3C83B)).withBold(true)));
    }

    private boolean outsideBox(double mx, double my) {
        return mx < boxX || mx > boxX + boxW || my < boxY || my > boxY + boxH;
    }

    // --- Fenêtre de choix d'attaque (2 attaques côte à côte) + confirmation secondaire ---

    /** Boîte centrée neutre (voile + panneau biseauté), pour les fenêtres d'attaque. */
    private void drawCenteredBox(GuiGraphics g, int bx, int by, int bw, int bh) {
        g.fill(0, 0, this.width, this.height, 0xB0000000);
        g.fill(bx - 1, by - 1, bx + bw + 1, by + bh + 1, 0xFF000000);
        g.fill(bx, by, bx + bw, by + bh, 0xFF23232B);
        g.fill(bx, by, bx + bw, by + 1, 0xFF55555F);
        g.fill(bx, by, bx + 1, by + bh, 0xFF55555F);
        g.fill(bx, by + bh - 1, bx + bw, by + bh, 0xFF101014);
        g.fill(bx + bw - 1, by, bx + bw, by + bh, 0xFF101014);
        boxX = bx; boxY = by; boxW = bw; boxH = bh;
    }

    private void renderAttackModal(GuiGraphics graphics, int mouseX, int mouseY) {
        int W = 248, H = 156;
        int bx = (this.width - W) / 2, by = (this.height - H) / 2;
        drawCenteredBox(graphics, bx, by, W, H);

        graphics.drawCenteredString(this.font, Component.translatable("piste.attack.choose")
                .withStyle(Style.EMPTY.withBold(true)), bx + W / 2, by + 8, 0xFFFFFF);

        // Croix de fermeture en haut à droite.
        dlgAttackClose.setPosition(bx + W - 18, by + 4);
        dlgAttackClose.render(graphics, mouseX, mouseY, 0f);

        // Trait de séparation vertical au milieu.
        graphics.fill(bx + W / 2, by + 22, bx + W / 2 + 1, by + H - 8, 0xFF3A3A42);

        boolean review = entity.isPisteNodeUnlocked(pendingAttack.getId());
        int chosen = entity.getPisteChoice(pendingAttack.getId());
        drawAttackColumn(graphics, 0, bx + 4, by + 20, W / 2 - 6, H - 28, review, chosen, mouseX, mouseY);
        drawAttackColumn(graphics, 1, bx + W / 2 + 2, by + 20, W / 2 - 6, H - 28, review, chosen, mouseX, mouseY);
    }

    private void drawAttackColumn(GuiGraphics g, int i, int cx0, int cy0, int cw, int ch,
                                  boolean review, int chosen, int mouseX, int mouseY) {
        if (i >= pendingAttack.getAttackIds().size()) return;
        OWPisteAttacks.AttackDef def = OWPisteAttacks.get(pendingAttack.getAttackIds().get(i));
        if (def == null) return;
        int ccx = cx0 + cw / 2;
        boolean rejected = review && chosen >= 0 && i != chosen;

        int img = 32, ix = ccx - img / 2, iy = cy0 + 4;
        if (rejected) RenderSystem.setShaderColor(0.55f, 0.32f, 0.32f, 1f);
        g.blit(def.icon(), ix, iy, img, img, 0f, 0f, 16, 16, 16, 16);
        if (rejected) RenderSystem.setShaderColor(1f, 1f, 1f, 1f);

        int y = iy + img + 3;
        int nameColor = rejected ? 0xC06060 : (review && i == chosen ? 0x8FE070 : 0xFFFFFF);
        Component name = Component.translatable(def.nameKey()).withStyle(
                Style.EMPTY.withBold(true).withColor(TextColor.fromRgb(nameColor)));
        for (net.minecraft.util.FormattedCharSequence line : this.font.split(name, cw - 4)) {
            g.drawCenteredString(this.font, line, ccx, y, 0xFFFFFF);
            y += 10;
        }
        y += 2;
        for (net.minecraft.util.FormattedCharSequence line : this.font.split(Component.translatable(def.descKey()), cw - 4)) {
            g.drawString(this.font, line, ccx - this.font.width(line) / 2, y, rejected ? 0x9A7070 : 0xC8C8C8, false);
            y += 9;
        }

        if (rejected) {
            g.fill(cx0 + 4, iy + img / 2, cx0 + cw - 4, iy + img / 2 + 2, 0xD0D03030); // barré rouge
        }

        if (!review) {
            Button b = (i == 0) ? dlgChoisirA : dlgChoisirB;
            int bw = Math.min(cw - 8, 96);
            b.setWidth(bw);
            b.setPosition(ccx - bw / 2, cy0 + ch - 22);
            b.render(g, mouseX, mouseY, 0f);
        } else if (i == chosen) {
            g.drawCenteredString(this.font, Component.translatable("piste.attack.taken")
                    .withStyle(Style.EMPTY.withColor(TextColor.fromRgb(0x8FE070))), ccx, cy0 + ch - 14, 0xFFFFFF);
        }
    }

    private void renderAttackConfirm(GuiGraphics graphics, int mouseX, int mouseY) {
        int W = 232;
        OWPisteAttacks.AttackDef def = OWPisteAttacks.get(pendingAttack.getAttackIds().get(pendingAttackConfirm));

        // Nom du passif intégré au texte (en blanc gras), reste de la phrase en rouge rosé (warning).
        Component name = (def != null ? Component.translatable(def.nameKey()) : Component.empty())
                .withStyle(Style.EMPTY.withBold(true).withColor(TextColor.fromRgb(0xFFFFFF)));
        Component question = Component.translatable("piste.attack.confirm", name)
                .withStyle(Style.EMPTY.withColor(TextColor.fromRgb(0xF08A8A)));
        java.util.List<net.minecraft.util.FormattedCharSequence> lines = this.font.split(question, W - 20);

        int H = 12 + 22 + 4 + lines.size() * 10 + 10 + 22;
        int bx = (this.width - W) / 2, by = (this.height - H) / 2;
        int cxp = bx + W / 2;
        drawCenteredBox(graphics, bx, by, W, H);

        if (def != null) graphics.blit(def.icon(), cxp - 10, by + 8, 20, 20, 0f, 0f, 16, 16, 16, 16);

        int y = by + 34;
        for (net.minecraft.util.FormattedCharSequence line : lines) {
            graphics.drawCenteredString(this.font, line, cxp, y, 0xF08A8A);
            y += 10;
        }

        int btnW = 62, gap = 12, tot = btnW * 2 + gap, btnY = by + H - 26;
        dlgYes.setWidth(btnW); dlgYes.setPosition(cxp - tot / 2, btnY);
        dlgNo.setWidth(btnW);  dlgNo.setPosition(cxp - tot / 2 + btnW + gap, btnY);
        dlgYes.render(graphics, mouseX, mouseY, 0f);
        dlgNo.render(graphics, mouseX, mouseY, 0f);
    }

    // --- Utilitaires de dessin ---

    private static void fillRange(GuiGraphics g, int xa, int xb, int ya, int yb, int color) {
        g.fill(Math.min(xa, xb), Math.min(ya, yb), Math.max(xa, xb), Math.max(ya, yb), color);
    }

    private static void drawBorder(GuiGraphics g, int left, int top, int right, int bottom, int color) {
        g.fill(left, top, right, top + 1, color);
        g.fill(left, bottom - 1, right, bottom, color);
        g.fill(left, top, left + 1, bottom, color);
        g.fill(right - 1, top, right, bottom, color);
    }
}
