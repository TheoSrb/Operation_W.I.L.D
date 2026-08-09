package net.tiew.operationWild.screen.blocks;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.tiew.operationWild.OperationWild;
import net.tiew.operationWild.core.OWSaddleCrafting;
import net.tiew.operationWild.core.OWSaddleRecipe;
import net.tiew.operationWild.core.OWSaddleRecipes;
import net.tiew.operationWild.core.OWWoolColors;
import net.tiew.operationWild.networking.ClientTamingData;
import net.tiew.operationWild.networking.OWNetworkHandler;
import net.tiew.operationWild.networking.packets.to_server.OWCraftSaddlePacket;

import java.util.ArrayList;
import java.util.List;

@OnlyIn(Dist.CLIENT)
public class SaddlerScreen extends AbstractContainerScreen<SaddlerMenu> {

    private static final ResourceLocation PANEL =
            ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "textures/gui/ow_options_screen.png");
    private static final ResourceLocation TAMING_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID,
                    "textures/gui/mob_page/misc/ow_entity_journal_interface_taming_experience_gui.png");

    private static final ResourceLocation LOCK_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "textures/gui/ow_teams_interface.png");

    private static final ResourceLocation BUTTON = ResourceLocation.withDefaultNamespace("widget/button");
    private static final ResourceLocation BUTTON_HOVER = ResourceLocation.withDefaultNamespace("widget/button_highlighted");
    private static final ResourceLocation BUTTON_OFF = ResourceLocation.withDefaultNamespace("widget/button_disabled");

    private static final int TEXT = 0x3F3F3F;
    private static final int TEXT_MUTED = 0x7C7C7C;
    private static final int TEXT_OK = 0x2E7D32;
    private static final int TEXT_PENDING = 0x707070;
    private static final int TAMING_COLOR = 0xC08A26;

    private static final int LIST_BG = 0xFF17171B;
    private static final int LIST_TEXT = 0xE6E6E6;
    private static final int LIST_LOCKED = 0xD07A68;

    private static final int CONTENT_Y = 20;
    private static final int CONTENT_H = 138;
    private static final int LIST_X = 5;
    private static final int LIST_W = 66;
    private static final int SCROLLBAR_W = 5;
    private static final int ROW_W = LIST_W - SCROLLBAR_W - 1;
    private static final int ROW_H = 23;
    private static final int VISIBLE_ROWS = 6;
    private static final int DETAIL_X = 73;
    private static final int DETAIL_W = 98;
    private static final int NEEDS_Y = 48;
    private static final int NEEDS_PITCH = 18;

    private int listX, listY;
    private int detailX, detailY;
    private int buttonX, buttonY, buttonW, buttonH;

    private int selected = -1;
    private int scroll = 0;
    private boolean draggingThumb = false;

    private static DyeColor primaryColor = DyeColor.WHITE;
    private static DyeColor secondaryColor = DyeColor.LIGHT_GRAY;
    private int pickerSlot = -1;

    private static final int SWATCH_W = 16, SWATCH_H = 11, SWATCH_GAP = 1, SWATCH_COLS = 4;
    private static final int PICKER_W = SWATCH_COLS * (SWATCH_W + SWATCH_GAP) - SWATCH_GAP + 6;
    private static final int PICKER_H = 4 * (SWATCH_H + SWATCH_GAP) - SWATCH_GAP + 6;

    public SaddlerScreen(SaddlerMenu menu, Inventory inventory, Component component) {
        super(menu, inventory, component);
        this.imageWidth = 176;
        this.imageHeight = 166;
    }

    @Override
    protected void init() {
        super.init();

        listX = leftPos + LIST_X;
        listY = topPos + CONTENT_Y;
        detailX = leftPos + DETAIL_X;
        detailY = topPos + CONTENT_Y;

        buttonX = detailX;
        buttonY = detailY + CONTENT_H - 16;
        buttonW = DETAIL_W;
        buttonH = 16;

        if (selected >= 0) ensureVisible(selected);
    }

    @Override
    public void renderBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        graphics.fill(0, 0, this.width, this.height, 0xA0000000);
        this.renderBg(graphics, partialTick, mouseX, mouseY);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        if (selected >= 0 && !this.menu.isUnlocked(selected)) selected = -1;

        graphics.blit(PANEL, leftPos, topPos, 0, 0, imageWidth, imageHeight);

        drawHeader(graphics);
        drawList(graphics, mouseX, mouseY);
        drawDetail(graphics, mouseX, mouseY);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        renderHoverTooltips(graphics, mouseX, mouseY);
    }

    // ── Bandeau ──────────────────────────────────────────────────────────────

    private void drawHeader(GuiGraphics graphics) {
        graphics.drawString(this.font, this.title, leftPos + 7, topPos + 6, TEXT, false);

        String balance = formatXp(ClientTamingData.tamingXp);
        int iconX = leftPos + imageWidth - 7 - 9;
        graphics.blit(TAMING_TEXTURE, iconX, topPos + 5, 9, 9, 110f, 12f, 11, 11, 256, 256);
        graphics.drawString(this.font, balance, iconX - 3 - this.font.width(balance), topPos + 6, TAMING_COLOR, false);

        graphics.fill(leftPos + 6, topPos + 17, leftPos + imageWidth - 6, topPos + 18, 0x33000000);
    }

    // ── Liste des selles ─────────────────────────────────────────────────────

    private void drawList(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.fill(listX - 1, listY - 1, listX + LIST_W + 1, listY + CONTENT_H + 1, 0xFF000000);
        graphics.fill(listX, listY, listX + LIST_W, listY + CONTENT_H, LIST_BG);

        List<OWSaddleRecipe> recipes = OWSaddleRecipes.all();
        int shown = Math.min(VISIBLE_ROWS, recipes.size());

        for (int i = 0; i < shown; i++) {
            int index = scroll + i;
            if (index >= recipes.size()) break;
            drawRow(graphics, recipes.get(index), index, listX, listY + i * ROW_H, ROW_W, mouseX, mouseY);
        }

        drawScrollbar(graphics, recipes.size());
    }

    private void drawScrollbar(GuiGraphics graphics, int total) {
        int trackX = listX + LIST_W - SCROLLBAR_W;
        graphics.fill(trackX, listY, trackX + SCROLLBAR_W, listY + CONTENT_H, 0xFF0A0A0C);

        if (total <= VISIBLE_ROWS) {
            graphics.fill(trackX + 1, listY + 1, trackX + SCROLLBAR_W - 1, listY + CONTENT_H - 1, 0xFF3C3C40);
            return;
        }

        int thumbH = Math.max(16, CONTENT_H * VISIBLE_ROWS / total);
        int thumbY = listY + (CONTENT_H - thumbH) * scroll / (total - VISIBLE_ROWS);

        graphics.fill(trackX + 1, thumbY, trackX + SCROLLBAR_W - 1, thumbY + thumbH, 0xFF8B8B8B);
        graphics.fill(trackX + 1, thumbY, trackX + SCROLLBAR_W - 1, thumbY + 1, 0xFFC8C8C8);
        graphics.fill(trackX + 1, thumbY + thumbH - 1, trackX + SCROLLBAR_W - 1, thumbY + thumbH, 0xFF4A4A4A);
    }

    private void scrollToPointer(double mouseY, int total) {
        int maxScroll = total - VISIBLE_ROWS;
        if (maxScroll <= 0) return;

        int thumbH = Math.max(16, CONTENT_H * VISIBLE_ROWS / total);
        int travel = CONTENT_H - thumbH;
        if (travel <= 0) return;

        double offset = mouseY - listY - thumbH / 2.0;
        scroll = Mth.clamp((int) Math.round(offset / travel * maxScroll), 0, maxScroll);
    }

    private void drawRow(GuiGraphics graphics, OWSaddleRecipe recipe, int index,
                         int x, int y, int w, int mouseX, int mouseY) {
        boolean unlocked = this.menu.isUnlocked(index);
        boolean active = unlocked && index == selected;
        boolean hovered = unlocked && isInside(mouseX, mouseY, x, y, w, ROW_H);
        int accent = unlocked ? 0xFF000000 | readableOnDark(recipe.accentColor()) : 0xFF4E4E54;

        if (active) graphics.fill(x, y, x + w, y + ROW_H, 0x38FFFFFF);
        else if (hovered) graphics.fill(x, y, x + w, y + ROW_H, 0x1CFFFFFF);
        graphics.fill(x, y + ROW_H - 1, x + w, y + ROW_H, 0x1AFFFFFF);

        graphics.fill(x, y, x + 3, y + ROW_H - 1, accent);
        if (active) {
            graphics.fill(x, y, x + w, y + 1, accent);
            graphics.fill(x, y + ROW_H - 1, x + w, y + ROW_H, accent);
            graphics.fill(x + w - 1, y, x + w, y + ROW_H, accent);
        }

        ItemStack icon = new ItemStack(recipe.result().get());
        graphics.renderItem(icon, x + 5, y + 3);

        Component label = unlocked
                ? Component.translatable(recipe.entityKey())
                : Component.translatable("saddler.locked");
        fitText(graphics, label, x + 23, y + 3, w - 25, unlocked ? LIST_TEXT : LIST_LOCKED);

        String threshold = formatXp(recipe.tamingThreshold());
        graphics.drawString(this.font, threshold, x + 23, y + 13, TAMING_COLOR, false);
        graphics.blit(TAMING_TEXTURE, x + 25 + this.font.width(threshold), y + 12,
                8, 8, 110f, 12f, 11, 11, 256, 256);

        if (!unlocked) {
            graphics.pose().pushPose();
            graphics.pose().translate(0, 0, 220);
            graphics.fill(x + 3, y, x + w, y + ROW_H - 1, 0x8C15151A);
            graphics.pose().popPose();
            drawLockBadge(graphics, x + 12, y + 8, 10, 12);
        }
    }

    // ── Fiche de la selle sélectionnée ───────────────────────────────────────

    private void drawDetail(GuiGraphics graphics, int mouseX, int mouseY) {
        OWSaddleRecipe recipe = OWSaddleRecipes.byIndex(selected);
        if (recipe == null) {
            List<FormattedCharSequence> hint =
                    this.font.split(Component.translatable("saddler.no_selection"), DETAIL_W - 6);
            int hintY = detailY + (CONTENT_H - hint.size() * 10) / 2;

            for (FormattedCharSequence line : hint) {
                graphics.drawString(this.font, line,
                        detailX + (DETAIL_W - this.font.width(line)) / 2, hintY, TEXT_MUTED, false);
                hintY += 10;
            }
            return;
        }

        int px = detailX, py = detailY;

        ItemStack icon = new ItemStack(recipe.result().get());
        graphics.renderItem(icon, px + 1, py + 3);

        fitText(graphics, icon.getHoverName(), px + 20, py + 4, DETAIL_W - 22, TEXT);
        fitText(graphics, Component.translatable("saddler.unlocked"),
                px + 20, py + 16, DETAIL_W - 22, TEXT_OK);

        int wellTop = py + 30;
        int wellBottom = buttonY - 3;
        graphics.fill(px, wellTop, px + DETAIL_W, wellBottom, 0x30000000);
        graphics.fill(px, wellTop, px + DETAIL_W, wellTop + 1, 0x22000000);
        graphics.fill(px, wellBottom - 1, px + DETAIL_W, wellBottom, 0x1AFFFFFF);

        graphics.drawString(this.font, Component.translatable("saddler.materials"), px + 3, py + 36, TEXT, false);

        drawNeeds(graphics, recipe, px, py + NEEDS_Y);
        drawCraftButton(graphics, recipe, mouseX, mouseY);

        if (pickerSlot != OWSaddleRecipe.NO_COLOR_SLOT) drawColorPicker(graphics, mouseX, mouseY);
    }

    private int pickerX() {
        return detailX + (DETAIL_W - PICKER_W) / 2;
    }

    private int pickerY() {
        return detailY + (CONTENT_H - PICKER_H) / 2;
    }

    private void drawColorPicker(GuiGraphics graphics, int mouseX, int mouseY) {
        int x = pickerX(), y = pickerY();

        graphics.pose().pushPose();
        graphics.pose().translate(0, 0, 260);

        graphics.fill(x - 1, y - 1, x + PICKER_W + 1, y + PICKER_H + 1, 0xFF16161A);
        graphics.fill(x, y, x + PICKER_W, y + PICKER_H, 0xFF2E2E34);

        DyeColor current = colorFor(pickerSlot);
        DyeColor other = colorFor(pickerSlot == 0 ? 1 : 0);

        for (DyeColor color : DyeColor.values()) {
            int i = color.getId();
            int sx = x + 3 + (i % SWATCH_COLS) * (SWATCH_W + SWATCH_GAP);
            int sy = y + 3 + (i / SWATCH_COLS) * (SWATCH_H + SWATCH_GAP);
            boolean hovered = isInside(mouseX, mouseY, sx, sy, SWATCH_W, SWATCH_H);

            graphics.fill(sx, sy, sx + SWATCH_W, sy + SWATCH_H, 0xFF000000 | OWWoolColors.rgb(color));
            if (color == other) graphics.fill(sx, sy, sx + SWATCH_W, sy + SWATCH_H, 0x99000000);

            if (color == current) {
                graphics.fill(sx - 1, sy - 1, sx + SWATCH_W + 1, sy, 0xFFFFFFFF);
                graphics.fill(sx - 1, sy + SWATCH_H, sx + SWATCH_W + 1, sy + SWATCH_H + 1, 0xFFFFFFFF);
                graphics.fill(sx - 1, sy, sx, sy + SWATCH_H, 0xFFFFFFFF);
                graphics.fill(sx + SWATCH_W, sy, sx + SWATCH_W + 1, sy + SWATCH_H, 0xFFFFFFFF);
            } else if (hovered) {
                graphics.fill(sx, sy, sx + SWATCH_W, sy + 1, 0x88FFFFFF);
                graphics.fill(sx, sy + SWATCH_H - 1, sx + SWATCH_W, sy + SWATCH_H, 0x88FFFFFF);
            }
        }

        graphics.pose().popPose();
    }

    private boolean colorPickerClicked(double mouseX, double mouseY) {
        int x = pickerX(), y = pickerY();

        for (DyeColor color : DyeColor.values()) {
            int i = color.getId();
            int sx = x + 3 + (i % SWATCH_COLS) * (SWATCH_W + SWATCH_GAP);
            int sy = y + 3 + (i / SWATCH_COLS) * (SWATCH_H + SWATCH_GAP);
            if (!isInside((int) mouseX, (int) mouseY, sx, sy, SWATCH_W, SWATCH_H)) continue;

            DyeColor other = colorFor(pickerSlot == 0 ? 1 : 0);
            if (color == other) {
                DyeColor previous = colorFor(pickerSlot);
                if (pickerSlot == 0) secondaryColor = previous;
                else primaryColor = previous;
            }
            if (pickerSlot == 0) primaryColor = color;
            else secondaryColor = color;

            pickerSlot = OWSaddleRecipe.NO_COLOR_SLOT;
            playClick();
            return true;
        }

        pickerSlot = OWSaddleRecipe.NO_COLOR_SLOT;
        return true;
    }

    private void drawNeeds(GuiGraphics graphics, OWSaddleRecipe recipe, int px, int y) {
        List<OWSaddleRecipe.Need> needs = recipe.needs();

        for (int i = 0; i < needs.size(); i++) {
            OWSaddleRecipe.Need need = needs.get(i);
            int rowY = y + i * NEEDS_PITCH;

            ItemStack display = displayStack(need);
            boolean colored = need.colorSlot() != OWSaddleRecipe.NO_COLOR_SLOT;

            if (colored) {
                graphics.fill(px + 2, rowY - 1, px + 20, rowY + 16, 0x33000000);
                graphics.fill(px + 2, rowY - 1, px + 4, rowY + 16,
                        0xFF000000 | OWWoolColors.rgb(colorFor(need.colorSlot())));
                renderItemScaled(graphics, display, px + 5, rowY + 1, 13);
            } else {
                graphics.renderItem(display, px + 3, rowY);
            }

            int owned = countOwned(need);
            boolean enough = owned >= need.count();

            String counter = owned + "/" + need.count();
            int counterX = px + DETAIL_W - 3 - this.font.width(counter);

            Component label = need.labelKey() != null
                    ? Component.translatable(need.labelKey())
                    : display.getHoverName();
            fitText(graphics, label, px + 22, rowY + 4, counterX - (px + 22) - 3,
                    enough ? TEXT : TEXT_MUTED);

            graphics.drawString(this.font, counter, counterX, rowY + 4, enough ? TEXT_OK : TEXT_PENDING, false);
        }
    }

    private void drawCraftButton(GuiGraphics graphics, OWSaddleRecipe recipe, int mouseX, int mouseY) {
        boolean ready = hasAllMaterials(recipe);
        boolean hovered = ready && isInside(mouseX, mouseY, buttonX, buttonY, buttonW, buttonH);

        graphics.blitSprite(ready ? (hovered ? BUTTON_HOVER : BUTTON) : BUTTON_OFF,
                buttonX, buttonY, buttonW, buttonH);

        Component label = ready
                ? Component.translatable("saddler.button.craft")
                : Component.translatable("saddler.button.missing");

        int labelW = this.font.width(label);
        int available = buttonW - 6;
        float scale = labelW > available ? Math.max(0.65f, available / (float) labelW) : 1f;

        graphics.pose().pushPose();
        graphics.pose().translate(buttonX + (buttonW - labelW * scale) / 2f,
                buttonY + (buttonH - 8 * scale) / 2f, 0);
        graphics.pose().scale(scale, scale, 1f);
        graphics.drawString(this.font, label, 0, 0, ready ? 0xFFFFFF : 0xA0A0A0, ready);
        graphics.pose().popPose();
    }

    // ── Survols ──────────────────────────────────────────────────────────────

    private void renderHoverTooltips(GuiGraphics graphics, int mouseX, int mouseY) {
        if (pickerSlot != OWSaddleRecipe.NO_COLOR_SLOT) return;

        OWSaddleRecipe recipe = OWSaddleRecipes.byIndex(selected);
        if (recipe != null) {
            List<OWSaddleRecipe.Need> needs = recipe.needs();
            for (int i = 0; i < needs.size(); i++) {
                int rowY = detailY + NEEDS_Y + i * NEEDS_PITCH;
                if (!isInside(mouseX, mouseY, detailX + 2, rowY - 1, 18, 17)) continue;
                graphics.renderTooltip(this.font, displayStack(needs.get(i)), mouseX, mouseY);
                return;
            }
        }

        List<OWSaddleRecipe> recipes = OWSaddleRecipes.all();
        int shown = Math.min(VISIBLE_ROWS, recipes.size());
        for (int i = 0; i < shown; i++) {
            int index = scroll + i;
            if (index >= recipes.size()) break;
            if (!isInside(mouseX, mouseY, listX, listY + i * ROW_H, ROW_W, ROW_H)) continue;

            OWSaddleRecipe hovered = recipes.get(index);
            List<Component> lines = new ArrayList<>();
            if (this.menu.isUnlocked(index)) {
                lines.add(new ItemStack(hovered.result().get()).getHoverName());
                lines.add(Component.translatable("saddler.unlocked").withStyle(ChatFormatting.GREEN));
            } else {
                lines.add(Component.translatable("saddler.locked").withStyle(ChatFormatting.RED));
                lines.add(Component.translatable("saddler.tooltip.requirement", formatXp(hovered.tamingThreshold()))
                        .withStyle(ChatFormatting.GOLD));
            }
            graphics.renderComponentTooltip(this.font, lines, mouseX, mouseY);
            return;
        }
    }

    // ── Interaction ──────────────────────────────────────────────────────────

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) {
            if (pickerSlot != OWSaddleRecipe.NO_COLOR_SLOT) return colorPickerClicked(mouseX, mouseY);

            List<OWSaddleRecipe> recipes = OWSaddleRecipes.all();

            if (isInside((int) mouseX, (int) mouseY,
                    listX + LIST_W - SCROLLBAR_W, listY, SCROLLBAR_W, CONTENT_H)) {
                draggingThumb = true;
                scrollToPointer(mouseY, recipes.size());
                return true;
            }

            int shown = Math.min(VISIBLE_ROWS, recipes.size());
            for (int i = 0; i < shown; i++) {
                int index = scroll + i;
                if (index >= recipes.size()) break;
                if (!isInside((int) mouseX, (int) mouseY, listX, listY + i * ROW_H, ROW_W, ROW_H)) continue;
                if (!this.menu.isUnlocked(index)) return true;

                if (selected != index) {
                    selected = index;
                    playClick();
                }
                return true;
            }

            OWSaddleRecipe recipe = OWSaddleRecipes.byIndex(selected);
            if (recipe == null || !this.menu.isUnlocked(selected)) {
                return super.mouseClicked(mouseX, mouseY, button);
            }

            List<OWSaddleRecipe.Need> needs = recipe.needs();
            for (int i = 0; i < needs.size(); i++) {
                OWSaddleRecipe.Need need = needs.get(i);
                if (need.colorSlot() == OWSaddleRecipe.NO_COLOR_SLOT) continue;

                int rowY = detailY + NEEDS_Y + i * NEEDS_PITCH;
                if (!isInside((int) mouseX, (int) mouseY, detailX + 2, rowY - 1, DETAIL_W - 4, 17)) continue;

                pickerSlot = need.colorSlot();
                playClick();
                return true;
            }

            if (isInside((int) mouseX, (int) mouseY, buttonX, buttonY, buttonW, buttonH)
                    && hasAllMaterials(recipe)) {
                OWNetworkHandler.sendToServer(new OWCraftSaddlePacket(selected,
                        primaryColor.getId(), secondaryColor.getId()));
                playClick();
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (draggingThumb && button == 0) {
            scrollToPointer(mouseY, OWSaddleRecipes.size());
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (button == 0) draggingThumb = false;
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        int maxScroll = OWSaddleRecipes.size() - VISIBLE_ROWS;
        if (maxScroll > 0 && isInside((int) mouseX, (int) mouseY, listX, listY, LIST_W, CONTENT_H)) {
            scroll = Mth.clamp(scroll - (int) Math.signum(scrollY), 0, maxScroll);
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    private void ensureVisible(int index) {
        int maxScroll = Math.max(0, OWSaddleRecipes.size() - VISIBLE_ROWS);
        if (index < scroll) scroll = index;
        else if (index >= scroll + VISIBLE_ROWS) scroll = index - VISIBLE_ROWS + 1;
        scroll = Mth.clamp(scroll, 0, maxScroll);
    }

    private void playClick() {
        if (this.minecraft != null && this.minecraft.getSoundManager() != null) {
            this.minecraft.getSoundManager().play(
                    net.minecraft.client.resources.sounds.SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK.value(), 1.0F));
        }
    }

    // ── Outils ───────────────────────────────────────────────────────────────

    private boolean hasAllMaterials(OWSaddleRecipe recipe) {
        for (OWSaddleRecipe.Need need : recipe.needs()) {
            if (countOwned(need) < need.count()) return false;
        }
        return true;
    }

    private int countOwned(OWSaddleRecipe.Need need) {
        return this.minecraft == null || this.minecraft.player == null
                ? 0 : OWSaddleCrafting.countIn(this.minecraft.player,
                OWSaddleCrafting.resolve(need, primaryColor, secondaryColor));
    }

    private DyeColor colorFor(int slot) {
        return slot == 0 ? primaryColor : secondaryColor;
    }

    private ItemStack displayStack(OWSaddleRecipe.Need need) {
        if (need.colorSlot() != OWSaddleRecipe.NO_COLOR_SLOT) {
            return new ItemStack(OWWoolColors.wool(colorFor(need.colorSlot())));
        }

        ItemStack[] options = need.ingredient().getItems();
        if (options.length == 0) return ItemStack.EMPTY;
        int index = options.length == 1 ? 0 : (int) ((System.currentTimeMillis() / 1000L) % options.length);
        return options[index];
    }

    private static String formatXp(double value) {
        return String.valueOf(Math.round(value));
    }

    private static boolean isInside(int mouseX, int mouseY, int x, int y, int w, int h) {
        return mouseX >= x && mouseX < x + w && mouseY >= y && mouseY < y + h;
    }

    private void fitText(GuiGraphics graphics, Component text, int x, int y, int maxWidth, int color) {
        int width = this.font.width(text);
        if (maxWidth <= 0) return;

        if (width <= maxWidth) {
            graphics.drawString(this.font, text, x, y, color, false);
            return;
        }

        float scale = maxWidth / (float) width;
        if (scale >= 0.75f) {
            graphics.pose().pushPose();
            graphics.pose().translate(x, y + (8f - 8f * scale) / 2f, 0);
            graphics.pose().scale(scale, scale, 1f);
            graphics.drawString(this.font, text, 0, 0, color, false);
            graphics.pose().popPose();
            return;
        }

        String plain = this.font.plainSubstrByWidth(text.getString(), Mth.floor(maxWidth / 0.75f) - 4);
        graphics.pose().pushPose();
        graphics.pose().translate(x, y + 1f, 0);
        graphics.pose().scale(0.75f, 0.75f, 1f);
        graphics.drawString(this.font, plain + "…", 0, 0, color, false);
        graphics.pose().popPose();
    }

    private void renderItemScaled(GuiGraphics graphics, ItemStack stack, int x, int y, int size) {
        float scale = size / 16f;
        graphics.pose().pushPose();
        graphics.pose().translate(x, y, 0);
        graphics.pose().scale(scale, scale, 1f);
        graphics.renderItem(stack, 0, 0);
        graphics.pose().popPose();
    }

    private void drawLockBadge(GuiGraphics graphics, int x, int y, int w, int h) {
        graphics.pose().pushPose();
        graphics.pose().translate(0, 0, 240);
        graphics.blit(LOCK_TEXTURE, x, y, w, h, 53f, 167f, 12, 14, 256, 256);
        graphics.pose().popPose();
    }

    private static int readableOnDark(int rgb) {
        int r = (rgb >> 16) & 0xFF, g = (rgb >> 8) & 0xFF, b = rgb & 0xFF;
        float luminance = 0.299f * r + 0.587f * g + 0.114f * b;
        if (luminance >= 110f) return rgb;

        float t = Mth.clamp((110f - luminance) / 160f, 0f, 0.6f);
        r = Math.round(r + (255 - r) * t);
        g = Math.round(g + (255 - g) * t);
        b = Math.round(b + (255 - b) * t);
        return (r << 16) | (g << 8) | b;
    }
}
