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
import net.tiew.operationWild.entity.IOWWaypointEntity;
import net.tiew.operationWild.entity.OWEntity;
import net.tiew.operationWild.event.ClientEvents;
import net.tiew.operationWild.networking.OWNetworkHandler;
import net.tiew.operationWild.networking.packets.to_server.OWEntityTogglePacket;
import net.tiew.operationWild.networking.packets.to_server.OpenOWInventoryPacket;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

@OnlyIn(Dist.CLIENT)
public class OWOptionsScreen extends Screen {

    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "textures/gui/ow_options_screen.png");

    static final int IMG_W = 176;
    static final int IMG_H = 166;
    private static final int HEADER_H = 30;
    private static final int LIST_MX = 4;
    private static final int FOOTER_H = 4;

    private final OWEntity entity;
    private OWScrollPanel scrollPanel;

    private final OWTabsRenderer tabsRenderer = new OWTabsRenderer();

    public OWOptionsScreen() {
        super(Component.literal("OWOptionsScreen"));
        if (Minecraft.getInstance().player.getRootVehicle() instanceof OWEntity e)
            this.entity = e;
        else
            this.entity = null;
    }

    public static ToggleEntry toggle(Component label, Component desc,
                                     Supplier<Boolean> state, Runnable onToggle) {
        return new ToggleEntry(label, desc, state, onToggle, null, null);
    }

    public static ToggleEntry modeEntry(Component label, Component desc,
                                        Supplier<Boolean> state,
                                        Component trueLabel, Component falseLabel,
                                        Runnable onToggle) {
        return new ToggleEntry(label, desc, state, onToggle, trueLabel, falseLabel);
    }

    public static ActionEntry action(Component label, Component desc,
                                     Component btnLabel, int color, Runnable onClick) {
        return new ActionEntry(label, desc, btnLabel, color, onClick);
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

        if (entity != null) {
            scrollPanel.add(action(
                    Component.translatable("option.rename"),
                    Component.translatable("option.rename.desc"),
                    Component.translatable("option.rename.action"),
                    0x9fe8ff,
                    () -> Minecraft.getInstance().setScreen(new OWChooseNameScreen(entity.getId()))
            ));
            scrollPanel.add(modeEntry(
                    Component.translatable("option.passive"),
                    Component.translatable("option.passive.desc"),
                    entity::isPassive,
                    Component.translatable("option.passive.value.passive"),
                    Component.translatable("option.passive.value.aggressive"),
                    () -> OWNetworkHandler.sendToServer(new OWEntityTogglePacket("passive"))
            ));
            scrollPanel.add(toggle(
                    Component.translatable("option.autoPickup"),
                    Component.translatable("option.autoPickup.desc"),
                    entity::isAutoPickup,
                    () -> OWNetworkHandler.sendToServer(new OWEntityTogglePacket("autoPickup"))
            ));
        }

        if (entity instanceof IOWWaypointEntity) {
            scrollPanel.add(toggle(
                    Component.translatable("option.waypoint"),
                    Component.translatable("option.waypoint.desc"),
                    () -> ClientEvents.isWaypointEnabled(entity.getUUID()),
                    () -> ClientEvents.toggleWaypoint(entity.getUUID())
            ));
        }

        this.addWidget(scrollPanel);

        tabsRenderer.init(this.width, this.height, IMG_W, IMG_H, entity, this::addWidget);
        tabsRenderer.setActiveTab(OWTabsRenderer.Tab.OPTIONS);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partial) {
        int i = (this.width - IMG_W) / 2;
        int j = (this.height - IMG_H) / 2;

        super.render(g, mouseX, mouseY, partial);

        g.blit(TEXTURE, i, j, 0, 0, IMG_W, IMG_H);

        renderHeader(g, i, j);
        scrollPanel.render(g, mouseX, mouseY, partial);
        // Rendu des tabs après le scroll panel pour que les tooltips passent par-dessus
        tabsRenderer.renderTabs(g, this.font, entity, i, j, mouseX, mouseY);
    }

    private void renderHeader(GuiGraphics g, int i, int j) {
        Component title = Component.translatable("option.screenTitle")
                .setStyle(Style.EMPTY.withBold(true).withColor(TextColor.fromRgb(0x9fe8ff)));
        g.drawString(this.font, title,
                i + IMG_W / 2 - this.font.width(title) / 2, j + 7, 0x9fe8ff);

        if (entity != null) {
            String name = entity.getNickname();
            if (name != null && !name.isEmpty()) {
                Component nc = Component.literal(name)
                        .setStyle(Style.EMPTY.withColor(TextColor.fromRgb(0xAAAAAA)).withItalic(true));
                g.drawString(this.font, nc,
                        i + IMG_W / 2 - this.font.width(nc) / 2, j + 16, 0xAAAAAA);
            }
        }
    }

    static final int PAD = 6;
    static final int LABEL_H = 9;
    static final int LINE_H = 9;
    static final int LABEL_MARGIN = 3;
    static final int BTN_H = 14;

    public abstract static class BaseEntry {
        int cachedH = -1;
        List<FormattedCharSequence> descLines;

        final void initHeight(Font font, int entryW) {
            if (cachedH < 0) cachedH = Math.max(computeHeight(font, entryW), BTN_H + PAD * 2 + 2);
        }

        abstract int computeHeight(Font font, int entryW);

        abstract void render(GuiGraphics g, Font font, int x, int y, int w, int h,
                             int mx, int my, boolean hovered, float partial);

        void onMouseClicked(double mx, double my, int button, int x, int y, int w) {
        }

        void drawBg(GuiGraphics g, int x, int y, int w, int h, boolean hovered) {
            g.fill(x, y, x + w, y + h, hovered ? 0x22FFFFFF : 0x0FFFFFFF);
            g.fill(x, y + h - 1, x + w, y + h, 0xFF1C2028);
        }

        void drawLabel(GuiGraphics g, Font font, Component label, int x, int y) {
            g.drawString(font, label, x + PAD, y + PAD + (BTN_H - LABEL_H) / 2, 0xDDDDDD);
        }

        void drawDesc(GuiGraphics g, Font font, int x, int y) {
            int dy = y + PAD + BTN_H + LABEL_MARGIN;
            for (FormattedCharSequence line : descLines) {
                g.drawString(font, line, x + PAD, dy, 0x777777);
                dy += LINE_H;
            }
        }
    }

    public static class ToggleEntry extends BaseEntry {
        final Component label;
        final Component desc;
        final Supplier<Boolean> state;
        final Runnable onToggle;
        final Component trueLabel;
        final Component falseLabel;
        final boolean isModeEntry;
        int btnW;

        ToggleEntry(Component label, Component desc,
                    Supplier<Boolean> state, Runnable onToggle,
                    Component trueLabel, Component falseLabel) {
            this.label = label;
            this.desc = desc;
            this.state = state;
            this.onToggle = onToggle;
            this.trueLabel = trueLabel;
            this.falseLabel = falseLabel;
            this.isModeEntry = (trueLabel != null);
        }

        @Override
        int computeHeight(Font font, int entryW) {
            if (isModeEntry) {
                btnW = Math.max(
                        font.width(trueLabel),
                        font.width(falseLabel)
                ) + 20;
            } else {
                btnW = font.width("OFF") + 14;
            }
            descLines = font.split(desc, entryW - 2 * PAD);
            return PAD + BTN_H + LABEL_MARGIN + descLines.size() * LINE_H + PAD;
        }

        @Override
        void render(GuiGraphics g, Font font, int x, int y, int w, int h,
                    int mx, int my, boolean hovered, float partial) {
            drawBg(g, x, y, w, h, hovered);
            drawLabel(g, font, label, x, y);
            drawDesc(g, font, x, y);

            boolean on = state.get();
            int clr = on ? 0x44CC66 : isModeEntry ? 0xCC4444 : 0xCC4444;
            int btnX = x + w - btnW - PAD;
            int btnY = y + PAD;

            boolean btnHov = mx >= btnX && mx < btnX + btnW && my >= btnY && my < btnY + BTN_H;
            g.blitSprite(btnHov
                            ? ResourceLocation.withDefaultNamespace("widget/button_highlighted")
                            : ResourceLocation.withDefaultNamespace("widget/button"),
                    btnX, btnY, btnW, BTN_H);

            Component txt;
            if (isModeEntry) {
                txt = (on ? trueLabel : falseLabel).copy()
                        .setStyle(Style.EMPTY.withColor(TextColor.fromRgb(clr)).withBold(true));
            } else {
                txt = Component.literal(on ? "ON" : "OFF")
                        .setStyle(Style.EMPTY.withColor(TextColor.fromRgb(clr)).withBold(true));
            }
            g.drawString(font, txt, btnX + btnW / 2 - font.width(txt) / 2, btnY + 3, clr);
        }

        @Override
        void onMouseClicked(double mx, double my, int button, int x, int y, int w) {
            int btnX = x + w - btnW - PAD;
            int btnY = y + PAD;
            if (mx >= btnX && mx < btnX + btnW && my >= btnY && my < btnY + BTN_H)
                onToggle.run();
        }
    }

    public static class ActionEntry extends BaseEntry {
        final Component label;
        final Component desc;
        final Component btnLabel;
        final int color;
        final Runnable onClick;
        int btnW;

        ActionEntry(Component label, Component desc, Component btnLabel, int color, Runnable onClick) {
            this.label = label;
            this.desc = desc;
            this.btnLabel = btnLabel;
            this.color = color;
            this.onClick = onClick;
        }

        @Override
        int computeHeight(Font font, int entryW) {
            btnW = font.width(btnLabel) + 14;
            descLines = font.split(desc, entryW - 2 * PAD);
            return PAD + BTN_H + LABEL_MARGIN + descLines.size() * LINE_H + PAD;
        }

        @Override
        void render(GuiGraphics g, Font font, int x, int y, int w, int h,
                    int mx, int my, boolean hovered, float partial) {
            drawBg(g, x, y, w, h, hovered);
            drawLabel(g, font, label, x, y);
            drawDesc(g, font, x, y);

            int btnX = x + w - btnW - PAD;
            int btnY = y + PAD;

            boolean btnHov = mx >= btnX && mx < btnX + btnW && my >= btnY && my < btnY + BTN_H;
            g.blitSprite(btnHov
                            ? ResourceLocation.withDefaultNamespace("widget/button_highlighted")
                            : ResourceLocation.withDefaultNamespace("widget/button"),
                    btnX, btnY, btnW, BTN_H);

            Component display = btnLabel.copy()
                    .setStyle(Style.EMPTY.withColor(TextColor.fromRgb(color)));
            g.drawString(font, display,
                    btnX + btnW / 2 - font.width(display) / 2, btnY + 3, color);
        }

        @Override
        void onMouseClicked(double mx, double my, int button, int x, int y, int w) {
            int btnX = x + w - btnW - PAD;
            int btnY = y + PAD;
            if (mx >= btnX && mx < btnX + btnW && my >= btnY && my < btnY + BTN_H)
                onClick.run();
        }
    }

    private static class OWScrollPanel extends AbstractWidget {

        private final List<BaseEntry> entries = new ArrayList<>();
        private int scrollY = 0;
        private int totalH = 0;
        private boolean dirty = true;

        private static final int SCROLLBAR_W = 5;
        private static final int SCROLL_SPEED = 14;

        OWScrollPanel(int x, int y, int w, int h) {
            super(x, y, w, h, Component.empty());
        }

        void add(BaseEntry entry) {
            entries.add(entry);
            dirty = true;
        }

        private int entryW() {
            return this.width - SCROLLBAR_W - 3;
        }

        private void recompute() {
            if (!dirty) return;
            Font font = Minecraft.getInstance().font;
            totalH = 0;
            for (BaseEntry e : entries) {
                e.initHeight(font, entryW());
                totalH += e.cachedH;
            }
            dirty = false;
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
            for (BaseEntry entry : entries) {
                boolean hov = mx >= getX() && mx < getX() + eW
                        && my >= y && my < y + entry.cachedH;
                entry.render(g, font, getX(), y, eW, entry.cachedH, mx, my, hov, partial);
                y += entry.cachedH;
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
        public boolean mouseScrolled(double mx, double my, double dx, double dy) {
            if (!isOver(mx, my)) return false;
            scrollY = clamp((int) (scrollY - dy * SCROLL_SPEED), 0, maxScroll());
            return true;
        }

        @Override
        public boolean mouseClicked(double mx, double my, int button) {
            if (!isOver(mx, my)) return false;
            int y = getY() - scrollY;
            int eW = entryW();
            for (BaseEntry entry : entries) {
                if (my >= y && my < y + entry.cachedH) {
                    entry.onMouseClicked(mx, my, button, getX(), y, eW);
                    return true;
                }
                y += entry.cachedH;
            }
            return false;
        }

        private boolean isOver(double mx, double my) {
            return mx >= getX() && mx < getX() + this.width
                    && my >= getY() && my < getY() + this.height;
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