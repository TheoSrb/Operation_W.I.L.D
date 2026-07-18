package net.tiew.operationWild.screen.tribe;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.util.FormattedCharSequence;
import net.tiew.operationWild.client.OWClientTribeData;
import net.tiew.operationWild.client.OWClientTribeList;
import net.tiew.operationWild.networking.OWNetworkHandler;
import net.tiew.operationWild.networking.packets.to_server.JoinTribePacket;

import java.util.ArrayList;
import java.util.List;

/** Découverte des tribus : liste déroulante (publiques d'abord, privées grisées en fin) + création. */
public class OWTribeListScreen extends OWTribeScreen {

    private static final int LIST_MX = 6;
    private static final int LIST_TOP = 22;
    /** 3 lignes de texte par tribu : nom, méta (chef · membres), conditions d'entrée. */
    private static final int ROW_H = 30;
    private static final int FOOTER_H = 24;
    private static final int JOIN_W = 40, JOIN_H = 16;
    /** Condition remplie / non remplie — teintes sombres, lisibles sur le panneau gris clair. */
    private static final int COND_OK = 0x1E7A1E, COND_KO = 0xB02020;

    private int listX, listY, listW, listH, visibleRows;
    private int scroll = 0;
    /** Tribu survolée dont il faut détailler les conditions en tooltip, ou {@code null}. */
    private OWClientTribeList.Entry hoveredEntry = null;

    public OWTribeListScreen() {
        super(Component.translatable("owteams.list.title"));
    }

    @Override
    protected void init() {
        super.init();
        listX = leftPos + LIST_MX;
        listY = topPos + LIST_TOP;
        listW = IMG_W - LIST_MX * 2;
        listH = IMG_H - LIST_TOP - FOOTER_H;
        visibleRows = listH / ROW_H;

        int by = topPos + IMG_H - FOOTER_H + 4;
        // Bouton « Créer une tribu » centré (le bouton « Terminé » a été retiré : Échap ferme l'écran).
        int createW = 120;
        this.addRenderableWidget(Button.builder(
                        Component.translatable("owteams.list.create"),
                        b -> Minecraft.getInstance().setScreen(new OWBannerShapeSelectScreen()))
                .bounds(leftPos + (IMG_W - createW) / 2, by, createW, 16).build());
    }

    @Override
    public void tick() {
        super.tick();
        if (OWClientTribeData.hasTribe()) {
            Minecraft.getInstance().setScreen(new OWTribeDashboardScreen());
        }
    }

    private int maxScroll(int count) {
        return Math.max(0, count - visibleRows);
    }

    @Override
    public boolean mouseScrolled(double mx, double my, double sx, double sy) {
        int count = OWClientTribeList.get().size();
        scroll = Math.max(0, Math.min(maxScroll(count), scroll - (int) Math.signum(sy)));
        return true;
    }

    @Override
    public boolean mouseClicked(double mx, double my, int button) {
        if (button == 0) {
            List<OWClientTribeList.Entry> entries = OWClientTribeList.get();
            for (int i = scroll; i < Math.min(scroll + visibleRows, entries.size()); i++) {
                OWClientTribeList.Entry e = entries.get(i);
                if (!e.isPublic()) continue;
                int rowY = listY + (i - scroll) * ROW_H;
                int jx = listX + listW - JOIN_W - 8;
                int jy = rowY + (ROW_H - JOIN_H) / 2;
                if (mx >= jx && mx < jx + JOIN_W && my >= jy && my < jy + JOIN_H) {
                    OWNetworkHandler.sendToServer(new JoinTribePacket(e.teamId()));
                    return true;
                }
            }
        }
        return super.mouseClicked(mx, my, button);
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partial) {
        hoveredEntry = null;
        drawPanel(g, mouseX, mouseY, partial);
        drawHeader(g, Component.translatable("owteams.list.title"));

        List<OWClientTribeList.Entry> entries = OWClientTribeList.get();
        boolean hasScroll = entries.size() > visibleRows;
        int contentW = hasScroll ? listW - 6 : listW;

        if (entries.isEmpty()) {
            g.drawCenteredString(this.font, Component.translatable("owteams.list.empty"),
                    leftPos + IMG_W / 2, listY + listH / 2 - 4, 0x808080);
        }

        g.enableScissor(listX, listY, listX + contentW, listY + visibleRows * ROW_H);
        for (int i = scroll; i < Math.min(scroll + visibleRows, entries.size()); i++) {
            renderRow(g, entries.get(i), i, listY + (i - scroll) * ROW_H, contentW, mouseX, mouseY);
        }
        g.disableScissor();

        if (hasScroll) {
            drawScrollbar(g, listX + listW - 5, listY, visibleRows * ROW_H,
                    scroll, maxScroll(entries.size()), visibleRows, entries.size());
        }

        super.render(g, mouseX, mouseY, partial);

        // Détail des conditions de la tribu survolée : une ligne verte / rouge par condition.
        if (hoveredEntry != null) {
            List<FormattedCharSequence> tip = new ArrayList<>();
            tip.add(Component.translatable("owteams.list.conditions_title")
                    .copy().withStyle(Style.EMPTY.withBold(true).withColor(TextColor.fromRgb(0xFFE070)))
                    .getVisualOrderText());
            for (OWClientTribeList.Req r : hoveredEntry.joinRequirements()) {
                tip.add(r.requirement().describe().copy().withStyle(Style.EMPTY
                        .withColor(TextColor.fromRgb(r.met() ? 0x6FDD6F : 0xE86A6A))).getVisualOrderText());
            }
            g.renderTooltip(this.font, tip, mouseX, mouseY);
        }
    }

    private void renderRow(GuiGraphics g, OWClientTribeList.Entry e, int index, int rowY, int rowW, int mouseX, int mouseY) {
        boolean priv = !e.isPublic();
        boolean hovered = mouseX >= listX && mouseX < listX + rowW && mouseY >= rowY && mouseY < rowY + ROW_H;
        // Zébrage + survol (overlays sombres sur panneau gris)
        g.fill(listX, rowY, listX + rowW, rowY + ROW_H, (index & 1) == 0 ? 0x18000000 : 0x28000000);
        if (hovered) g.fill(listX, rowY, listX + rowW, rowY + ROW_H, 0x22FFFFFF);
        g.fill(listX, rowY + ROW_H - 1, listX + rowW, rowY + ROW_H, 0x30000000);

        // Mini bannière
        float s = 24f / OWBannerRenderer.H;
        g.pose().pushPose();
        g.pose().translate(listX + 3, rowY + 2, 0);
        g.pose().scale(s, s, 1f);
        OWBannerRenderer.render(g, 0, 0, e.bannerShape(),
                e.primaryColor(), e.secondaryColor(), e.tertiaryColor(), e.useTertiary(),
                e.pattern(), e.paintPixels());
        g.pose().popPose();
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);

        // Badge de réputation (petit) à gauche du bouton rejoindre / tag privé.
        net.tiew.operationWild.core.OWReputation.Badge badge =
                net.tiew.operationWild.core.OWReputation.badgeFor(e.reputation());
        int badgeSize = 16;
        int badgeSpace = badge.hasSprite() ? badgeSize + 3 : 0;

        int textX = listX + 3 + (int) (OWBannerRenderer.W * s) + 6;
        int nameColor = priv ? 0x808080 : 0x303030;
        int nameMaxW = rowW - (textX - listX) - JOIN_W - 14 - badgeSpace;
        g.drawString(this.font, trim(e.name(), nameMaxW), textX, rowY + 3, nameColor, false);

        // Ligne méta : chef · membres
        String meta = Component.translatable("owteams.list.chief", e.chiefName()).getString()
                + "  " + Component.translatable("owteams.list.members", e.memberCount()).getString();
        g.drawString(this.font, trim(meta, nameMaxW), textX, rowY + 12, priv ? 0x909090 : 0x606060, false);

        // Ligne conditions d'entrée : verte si le joueur les remplit, rouge sinon (verdict du serveur).
        // Une tribu privée ne se rejoint que sur invitation : ses conditions n'ont rien à dire ici.
        if (!priv) {
            List<OWClientTribeList.Req> reqs = e.joinRequirements();
            Component cond;
            int color;
            if (reqs.isEmpty()) {
                cond = Component.translatable("owteams.list.no_condition");
                color = 0x707070;
            } else {
                // Une seule condition tient en toutes lettres ; au-delà, on résume et le survol détaille.
                cond = reqs.size() == 1
                        ? reqs.get(0).requirement().describe()
                        : Component.translatable("owteams.list.conditions_met", e.conditionsMetCount(), reqs.size());
                color = e.allConditionsMet() ? COND_OK : COND_KO;
                if (hovered) hoveredEntry = e;
            }
            g.drawString(this.font, trim(cond.getString(), nameMaxW), textX, rowY + 21, color, false);
        }

        // Bouton rejoindre (public) ou tag « Privée »
        int jx = listX + rowW - JOIN_W - 8;
        int jy = rowY + (ROW_H - JOIN_H) / 2;

        // Badge de réputation, juste à gauche du bouton / tag.
        if (badge.hasSprite()) {
            OWTribeReputationScreen.renderBadge(g, badge, jx - badgeSize - 3, rowY + (ROW_H - badgeSize) / 2, badgeSize);
        }
        if (priv) {
            Component tag = Component.translatable("owteams.list.private");
            g.drawString(this.font, tag, jx + JOIN_W - this.font.width(tag), rowY + (ROW_H - 8) / 2, 0x909090, false);
        } else {
            boolean jHov = mouseX >= jx && mouseX < jx + JOIN_W && mouseY >= jy && mouseY < jy + JOIN_H;
            // Sans entrée directe, le clic n'inscrit pas : il envoie une demande au chef et aux adjoints.
            Component label = Component.translatable(e.directJoin() ? "owteams.list.join" : "owteams.list.request");
            // Le bouton reste cliquable même si la condition n'est pas remplie : le serveur répond
            // alors par le détail de ce qui manque, plus parlant qu'un bouton mort.
            drawSpriteButton(g, jx, jy, JOIN_W, JOIN_H, label, jHov,
                    e.allConditionsMet() ? 0xFFFFFF : 0xA0A0A0);
        }
    }

    private String trim(String s, int maxW) {
        if (s == null) return "";
        String out = s;
        while (this.font.width(out) > maxW && out.length() > 1) out = out.substring(0, out.length() - 1);
        return out;
    }
}
