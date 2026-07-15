package net.tiew.operationWild.screen.tribe;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.tiew.operationWild.client.OWClientTribeData;
import net.tiew.operationWild.client.OWClientTribeList;
import net.tiew.operationWild.networking.OWNetworkHandler;
import net.tiew.operationWild.networking.packets.to_server.JoinTribePacket;

import java.util.List;

/** Découverte des tribus : liste déroulante (publiques d'abord, privées grisées en fin) + création. */
public class OWTribeListScreen extends OWTribeScreen {

    private static final int LIST_MX = 6;
    private static final int LIST_TOP = 22;
    private static final int ROW_H = 27;
    private static final int FOOTER_H = 24;
    private static final int JOIN_W = 40, JOIN_H = 16;

    private int listX, listY, listW, listH, visibleRows;
    private int scroll = 0;

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
        this.addRenderableWidget(Button.builder(
                        Component.translatable("owteams.list.create"),
                        b -> Minecraft.getInstance().setScreen(new OWBannerShapeSelectScreen()))
                .bounds(leftPos + LIST_MX, by, 108, 16).build());
        this.addRenderableWidget(Button.builder(
                        Component.translatable("gui.done"), b -> this.onClose())
                .bounds(leftPos + IMG_W - LIST_MX - 50, by, 50, 16).build());
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
                e.primaryColor(), e.secondaryColor(), e.pattern(), e.paintPixels());
        g.pose().popPose();
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);

        int textX = listX + 3 + (int) (OWBannerRenderer.W * s) + 6;
        int nameColor = priv ? 0x808080 : 0x303030;
        int nameMaxW = rowW - (textX - listX) - JOIN_W - 14;
        g.drawString(this.font, trim(e.name(), nameMaxW), textX, rowY + 4, nameColor, false);

        // Ligne méta : chef · membres (· min pièces si public conditionné)
        String meta = Component.translatable("owteams.list.chief", e.chiefName()).getString()
                + "  " + Component.translatable("owteams.list.members", e.memberCount()).getString();
        if (!priv && e.minWildCoins() > 0) {
            meta += "  " + Component.translatable("owteams.list.min_coins", e.minWildCoins()).getString();
        }
        g.drawString(this.font, trim(meta, nameMaxW), textX, rowY + 15, priv ? 0x909090 : 0x606060, false);

        // Bouton rejoindre (public) ou tag « Privée »
        int jx = listX + rowW - JOIN_W - 8;
        int jy = rowY + (ROW_H - JOIN_H) / 2;
        if (priv) {
            Component tag = Component.translatable("owteams.list.private");
            g.drawString(this.font, tag, jx + JOIN_W - this.font.width(tag), rowY + (ROW_H - 8) / 2, 0x909090, false);
        } else {
            boolean jHov = mouseX >= jx && mouseX < jx + JOIN_W && mouseY >= jy && mouseY < jy + JOIN_H;
            drawSpriteButton(g, jx, jy, JOIN_W, JOIN_H, Component.translatable("owteams.list.join"), jHov, 0xFFFFFF);
        }
    }

    private String trim(String s, int maxW) {
        if (s == null) return "";
        String out = s;
        while (this.font.width(out) > maxW && out.length() > 1) out = out.substring(0, out.length() - 1);
        return out;
    }
}
