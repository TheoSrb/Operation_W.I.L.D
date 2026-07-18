package net.tiew.operationWild.screen.tribe;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.FormattedCharSequence;
import net.tiew.operationWild.client.OWClientTribeData;
import net.tiew.operationWild.networking.OWNetworkHandler;
import net.tiew.operationWild.networking.packets.to_server.UpdateTribeSettingsPacket;
import net.tiew.operationWild.team.OWTeam;
import net.tiew.operationWild.team.OWTribeJoinCondition;
import net.tiew.operationWild.team.OWTribeJoinRequirement;

import java.util.ArrayList;
import java.util.List;

/**
 * Choix des <b>conditions d'entrée</b> d'une tribu publique — sous-écran de
 * {@link OWTribeSettingsScreen} (bouton « Personnaliser »), réservé au chef.
 *
 * <p>De 0 à {@link OWTeam#MAX_JOIN_REQUIREMENTS} conditions, <b>toutes requises</b> : la liste est
 * une série de cases à cocher, et le seuil d'une condition cochée se règle sur sa propre ligne.
 * Aucune condition cochée = tribu ouverte à tous. Aucun état local n'est conservé — chaque clic
 * envoie un {@link UpdateTribeSettingsPacket} et l'écran se redessine depuis
 * {@link OWClientTribeData}, réalimenté par le serveur.</p>
 */
public class OWTribeJoinConditionScreen extends OWTribeScreen {

    /** 3 lignes par condition : libellé, description, réglage du seuil. La description reste
     *  toujours visible — c'est elle qui dit ce que le chiffre du seuil compte (heures, créatures…). */
    private static final int LIST_X = 6, LIST_Y = 22, ROW_H = 30, VISIBLE_ROWS = 4;
    private static final int LIST_W = IMG_W - LIST_X * 2;
    private static final int SCROLLBAR_W = 5;
    private static final int FOOTER_Y = 146, FOOTER_H = 14, BACK_W = 60;
    private static final int STEP_BTN = 9, VALUE_W = 30; // boutons « - » / « + » et champ de valeur
    /** Durée d'affichage de l'avertissement « maximum atteint », en ticks. */
    private static final int HINT_TICKS = 60;

    /** Toutes les conditions réglables — {@code NONE} n'en est pas une : « aucune » = rien de coché. */
    private static final List<OWTribeJoinCondition> CONDITIONS = buildConditions();

    private int scroll = 0;
    private int hoveredRow = -1;
    private int hintTicks = 0;

    private static List<OWTribeJoinCondition> buildConditions() {
        List<OWTribeJoinCondition> out = new ArrayList<>();
        for (OWTribeJoinCondition c : OWTribeJoinCondition.values()) if (c.hasThreshold()) out.add(c);
        return List.copyOf(out);
    }

    public OWTribeJoinConditionScreen() {
        super(Component.translatable("owteams.cond.title"));
    }

    @Override
    protected void init() {
        super.init();
        // Retour à droite du pied de page ; la gauche accueille le compteur de conditions actives.
        this.addRenderableWidget(Button.builder(Component.translatable("owteams.cond.back"),
                        b -> Minecraft.getInstance().setScreen(new OWTribeSettingsScreen()))
                .bounds(leftPos + IMG_W - 6 - BACK_W, topPos + FOOTER_Y, BACK_W, FOOTER_H).build());
    }

    private boolean isChief() {
        OWTeam t = OWClientTribeData.get();
        Minecraft mc = Minecraft.getInstance();
        return t != null && mc.player != null && mc.player.getUUID().equals(t.getTeamOwnerUUID());
    }

    @Override
    public void tick() {
        super.tick();
        if (hintTicks > 0) hintTicks--;
        // La tribu a disparu, ou le joueur n'est plus chef / la tribu est repassée en privé :
        // cet écran n'a plus de raison d'être.
        OWTeam t = OWClientTribeData.get();
        if (t == null) { Minecraft.getInstance().setScreen(new OWTribeMenuScreen()); return; }
        if (!isChief() || !t.isPublic()) Minecraft.getInstance().setScreen(new OWTribeSettingsScreen());
    }

    private int maxScroll() { return Math.max(0, CONDITIONS.size() - VISIBLE_ROWS); }

    @Override
    public boolean mouseScrolled(double mx, double my, double sx, double sy) {
        scroll = Math.max(0, Math.min(maxScroll(), scroll - (int) Math.signum(sy)));
        return true;
    }

    /** Pas de réglage du seuil : ×10 avec Maj enfoncée, pour traverser vite les grandes échelles. */
    private int step(OWTribeJoinCondition c) {
        return hasShiftDown() ? c.getStep() * 10 : c.getStep();
    }

    private void send(List<OWTribeJoinRequirement> requirements) {
        OWTeam t = OWClientTribeData.get();
        if (t == null || !isChief()) return;
        OWNetworkHandler.sendToServer(new UpdateTribeSettingsPacket(t.isPublic(), requirements, t.isDirectJoin()));
    }

    private void playClick(float pitch) {
        Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, pitch));
    }

    /** Coche / décoche une condition. Refuse silencieusement (avec avertissement) au-delà du maximum. */
    private void toggle(OWTeam t, OWTribeJoinCondition condition) {
        List<OWTribeJoinRequirement> reqs = new ArrayList<>(t.getJoinRequirements());
        OWTribeJoinRequirement existing = t.requirementFor(condition);
        if (existing != null) {
            reqs.remove(existing);
            playClick(0.8f);
        } else {
            if (reqs.size() >= OWTeam.MAX_JOIN_REQUIREMENTS) {
                hintTicks = HINT_TICKS;
                playClick(0.5f);
                return;
            }
            reqs.add(OWTribeJoinRequirement.of(condition));
            playClick(1.1f);
        }
        send(reqs);
    }

    /** Décale le seuil d'une condition déjà cochée. */
    private void shiftThreshold(OWTeam t, OWTribeJoinCondition condition, int delta) {
        List<OWTribeJoinRequirement> reqs = new ArrayList<>(t.getJoinRequirements());
        for (int i = 0; i < reqs.size(); i++) {
            if (reqs.get(i).condition() == condition) {
                reqs.set(i, reqs.get(i).withThresholdShift(delta));
                send(reqs);
                return;
            }
        }
    }

    private int plusX(int x, int contentW) { return x + contentW - 4 - STEP_BTN; }
    private int minusX(int x, int contentW) { return plusX(x, contentW) - VALUE_W - STEP_BTN; }

    @Override
    public boolean mouseClicked(double mx, double my, int button) {
        OWTeam t = OWClientTribeData.get();
        if (t == null || button != 0 || !isChief()) return super.mouseClicked(mx, my, button);

        int contentW = CONDITIONS.size() > VISIBLE_ROWS ? LIST_W - SCROLLBAR_W : LIST_W;
        int x = leftPos + LIST_X;
        for (int i = scroll; i < Math.min(scroll + VISIBLE_ROWS, CONDITIONS.size()); i++) {
            int rowY = topPos + LIST_Y + (i - scroll) * ROW_H;
            if (mx < x || mx >= x + contentW || my < rowY || my >= rowY + ROW_H) continue;
            OWTribeJoinCondition c = CONDITIONS.get(i);

            // Sur une ligne cochée, les boutons de seuil priment sur la bascule de la case.
            if (t.requirementFor(c) != null) {
                int by = rowY + 20;
                if (my >= by && my < by + STEP_BTN) {
                    if (mx >= minusX(x, contentW) && mx < minusX(x, contentW) + STEP_BTN) {
                        playClick(0.9f);
                        shiftThreshold(t, c, -step(c));
                        return true;
                    }
                    if (mx >= plusX(x, contentW) && mx < plusX(x, contentW) + STEP_BTN) {
                        playClick(1.2f);
                        shiftThreshold(t, c, step(c));
                        return true;
                    }
                }
            }
            toggle(t, c);
            return true;
        }
        return super.mouseClicked(mx, my, button);
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partial) {
        hoveredRow = -1;
        drawPanel(g, mouseX, mouseY, partial);
        drawHeader(g, Component.translatable("owteams.cond.title"));
        g.fill(leftPos + 6, topPos + 18, leftPos + IMG_W - 6, topPos + 19, 0xFF9A9A9A); // séparateur d'en-tête

        OWTeam t = OWClientTribeData.get();
        if (t == null) { super.render(g, mouseX, mouseY, partial); return; }

        boolean hasScroll = CONDITIONS.size() > VISIBLE_ROWS;
        int contentW = hasScroll ? LIST_W - SCROLLBAR_W : LIST_W;
        int lx = leftPos + LIST_X, ly = topPos + LIST_Y, boxH = VISIBLE_ROWS * ROW_H;

        g.enableScissor(lx, ly, lx + contentW, ly + boxH);
        for (int i = scroll; i < Math.min(scroll + VISIBLE_ROWS, CONDITIONS.size()); i++) {
            renderRow(g, t, CONDITIONS.get(i), i, ly + (i - scroll) * ROW_H, lx, contentW, mouseX, mouseY);
        }
        g.disableScissor();

        if (hasScroll) {
            drawScrollbar(g, lx + LIST_W - SCROLLBAR_W, ly, boxH, scroll, maxScroll(), VISIBLE_ROWS, CONDITIONS.size());
        }

        renderFooter(g, t);
        super.render(g, mouseX, mouseY, partial);

        // Tooltip : la description complète de la ligne survolée (la ligne la tronque).
        if (hoveredRow >= 0) {
            OWTribeJoinCondition c = CONDITIONS.get(hoveredRow);
            List<FormattedCharSequence> tip = new ArrayList<>();
            tip.add(c.getLabel().copy().withStyle(Style.EMPTY.withBold(true)
                    .withColor(TextColor.fromRgb(0xFFE070))).getVisualOrderText());
            for (FormattedCharSequence line : this.font.split(
                    c.getDescription().copy().withStyle(Style.EMPTY.withColor(TextColor.fromRgb(0xBBBBBB))), 160)) {
                tip.add(line);
            }
            g.renderTooltip(this.font, tip, mouseX, mouseY);
        }
    }

    private void renderRow(GuiGraphics g, OWTeam t, OWTribeJoinCondition c, int index, int rowY,
                           int x, int w, int mouseX, int mouseY) {
        OWTribeJoinRequirement req = t.requirementFor(c);
        boolean checked = req != null;
        boolean hov = mouseX >= x && mouseX < x + w && mouseY >= rowY && mouseY < rowY + ROW_H;
        if (hov) hoveredRow = index;

        g.fill(x, rowY, x + w, rowY + ROW_H, (index & 1) == 0 ? 0xAA111111 : 0xCC111111);
        if (checked) {
            g.fill(x, rowY, x + w, rowY + ROW_H, 0x553CA0FF);
            g.fill(x, rowY, x + 2, rowY + ROW_H, 0xFF5AB0FF); // accent gauche
        } else if (hov) {
            g.fill(x, rowY, x + w, rowY + ROW_H, 0x18FFFFFF);
        }
        g.fill(x, rowY + ROW_H - 1, x + w, rowY + ROW_H, 0x40000000);

        drawCheckbox(g, x + 5, rowY + (ROW_H - 7) / 2, checked);

        int textX = x + 16, textMaxW = w - (textX - x) - 4;
        g.drawString(this.font, trim(c.getLabel().getString(), textMaxW), textX, rowY + 2,
                checked ? 0xFFFFFF : 0xD8D8D8, false);
        // La description reste affichée même cochée : c'est elle qui dit ce que le seuil compte.
        g.drawString(this.font, trim(c.getDescription().getString(), textMaxW), textX, rowY + 11, 0x9A9A9A, false);

        if (!checked) return;

        // 3ᵉ ligne : réglage du seuil.
        int minusX = minusX(x, w), plusX = plusX(x, w), by = rowY + 20;
        g.drawString(this.font, trim(Component.translatable("owteams.cond.threshold").getString(), minusX - textX - 3),
                textX, rowY + 21, 0xD8D8D8, false);
        drawStepButton(g, minusX, by, "-", req.threshold() > c.getMinThreshold(), mouseX, mouseY);
        drawStepButton(g, plusX, by, "+", req.threshold() < c.getMaxThreshold(), mouseX, mouseY);
        String value = String.valueOf(req.threshold());
        g.drawString(this.font, value, minusX + STEP_BTN + (VALUE_W - this.font.width(value)) / 2,
                rowY + 21, 0xFFFFFF, false);
    }

    /** Case à cocher 7×7 : coche bleue quand la condition est retenue. */
    private void drawCheckbox(GuiGraphics g, int x, int y, boolean checked) {
        int border = checked ? 0xFF5AB0FF : 0xFF6A6A6A;
        g.fill(x, y, x + 7, y + 1, border);
        g.fill(x, y + 6, x + 7, y + 7, border);
        g.fill(x, y, x + 1, y + 7, border);
        g.fill(x + 6, y, x + 7, y + 7, border);
        if (checked) g.fill(x + 2, y + 2, x + 5, y + 5, 0xFF5AB0FF);
    }

    private void drawStepButton(GuiGraphics g, int x, int y, String label, boolean enabled, int mouseX, int mouseY) {
        boolean hov = enabled && mouseX >= x && mouseX < x + STEP_BTN && mouseY >= y && mouseY < y + STEP_BTN;
        g.fill(x, y, x + STEP_BTN, y + STEP_BTN, enabled ? (hov ? 0xFF5A616B : 0xFF3C424B) : 0xFF2A2E33);
        g.drawString(this.font, label, x + (STEP_BTN - this.font.width(label)) / 2, y + (STEP_BTN - 8) / 2,
                enabled ? 0xFFFFFF : 0xFF6A6A6A, false);
    }

    /** Pied de page (à gauche du bouton Retour) : conditions actives, ou avertissement de maximum. */
    private void renderFooter(GuiGraphics g, OWTeam t) {
        int active = t.getJoinRequirements().size();
        Component text;
        int color;
        if (hintTicks > 0) {
            text = Component.translatable("owteams.cond.max_reached", OWTeam.MAX_JOIN_REQUIREMENTS);
            color = 0xE86A6A;
        } else if (active == 0) {
            text = Component.translatable("owteams.cond.none.band");
            color = 0x6FBF6F;
        } else {
            text = Component.translatable("owteams.cond.active_count", active, OWTeam.MAX_JOIN_REQUIREMENTS);
            color = 0x505050;
        }
        int maxW = IMG_W - 12 - BACK_W - 6;
        g.drawString(this.font, trim(text.getString(), maxW), leftPos + 6,
                topPos + FOOTER_Y + (FOOTER_H - 8) / 2, color, false);
    }

    @Override
    public boolean keyPressed(int key, int scancode, int modifiers) {
        // Échap revient aux paramètres plutôt que de fermer toute l'interface de tribu.
        if (key == org.lwjgl.glfw.GLFW.GLFW_KEY_ESCAPE) {
            Minecraft.getInstance().setScreen(new OWTribeSettingsScreen());
            return true;
        }
        return super.keyPressed(key, scancode, modifiers);
    }

    private String trim(String s, int maxW) {
        if (s == null) return "";
        String out = s;
        while (this.font.width(out) > maxW && out.length() > 1) out = out.substring(0, out.length() - 1);
        return out;
    }
}
