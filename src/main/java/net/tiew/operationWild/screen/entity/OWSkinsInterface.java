package net.tiew.operationWild.screen.entity;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.FormattedCharSequence;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.tiew.operationWild.OperationWild;
import net.tiew.operationWild.entity.OWEntity;
import net.tiew.operationWild.networking.OWNetworkHandler;
import net.tiew.operationWild.networking.packets.to_server.OWVariantsSkinsPacket;
import net.tiew.operationWild.networking.packets.to_server.SkinBuyingPacket;
import net.tiew.operationWild.quests.CosmeticsQuest;
import net.tiew.operationWild.quests.CosmeticsQuestsRegistry;
import net.tiew.operationWild.screen.OWScreenUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@OnlyIn(Dist.CLIENT)
public class OWSkinsInterface extends Screen {

    private static final ResourceLocation BG_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "textures/gui/ow_options_screen.png");
    private static final ResourceLocation BG_2_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "textures/gui/ow_skins_interface.png");
    private static final ResourceLocation ICONS_LOCATION =
            ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "textures/gui/mob_types.png");

    protected final OWEntity entity;
    private float xMouse;
    private float yMouse;

    public int imageWidth  = 176;
    public int imageHeight = 166;
    protected int entityScale = 30;

    public int selectedSkinIndex = -1;

    private final OWTabsRenderer tabsRenderer = new OWTabsRenderer();

    private OWEntity previewEntity          = null;
    private int      previewEntitySkinIndex = -1;

    private static final int LST_X  = 5;
    private static final int LST_Y  = 18;
    private static final int LST_W  = 163;
    private static final int LST_H  = 112;
    private static final int STRIP_H = 28;
    private static final int SEP_X   = 87;

    private static final int DET_X  = 185;
    private static final int DET_Y  = -15;
    private static final int DET_W  = 100;
    private static final int PREV_H = 80;

    private static final int OBTAIN_X_PAD  = 4;
    private static final int OBTAIN_Y_GAP  = 4;
    private static final int OBTAIN_LBL_H  = 10;
    private static final int OBTAIN_LINE_H = 8;
    private static final int OBTAIN_W      = DET_W - 8;

    protected Button equipButton;
    protected Button buyButton;
    private   Button confirmYesButton;
    private   Button confirmNoButton;
    private   boolean confirmingPurchase = false;
    protected OWScreenUtils.ButtonListWidget buttonList;

    public final List<Button> LEGENDARY_SKIN = new ArrayList<>();
    public final List<Button> EPIC_SKIN      = new ArrayList<>();
    public final List<Button> RARE_SKIN      = new ArrayList<>();
    public final List<Button> COMMON_SKIN    = new ArrayList<>();
    public final List<Button> HALLOWEEN_SKIN = new ArrayList<>();

    protected final Map<Integer, Boolean> lockedSkins = new HashMap<>();
    protected final Map<Integer, Integer> skinPrices  = new HashMap<>();

    public static class SkinInfo {
        public enum UnlockType { FREE, LEVEL, PRESTIGE, QUEST }

        public final String nameKey;
        public final String descriptionKey;
        public final UnlockType unlockType;
        public final int unlockValue;
        public final int questId;

        public SkinInfo(String nameKey, String descriptionKey,
                        UnlockType unlockType, int unlockValue, int questId) {
            this.nameKey        = nameKey;
            this.descriptionKey = descriptionKey;
            this.unlockType     = unlockType;
            this.unlockValue    = unlockValue;
            this.questId        = questId;
        }

        public static SkinInfo free(String n, String d)              { return new SkinInfo(n, d, UnlockType.FREE,     0,   -1); }
        public static SkinInfo level(String n, String d, int lvl)    { return new SkinInfo(n, d, UnlockType.LEVEL,    lvl, -1); }
        public static SkinInfo prestige(String n, String d, int pts) { return new SkinInfo(n, d, UnlockType.PRESTIGE, pts, -1); }
        public static SkinInfo quest(String n, String d, int id)     { return new SkinInfo(n, d, UnlockType.QUEST,    0,   id); }
    }

    public OWSkinsInterface() {
        super(Component.literal("OWSkinsInterface"));
        if (Minecraft.getInstance().player.getRootVehicle() instanceof OWEntity e)
            this.entity = e;
        else
            this.entity = null;
        initLockedSkins();
        initSkinPrices();
    }

    protected void initLockedSkins()                  {}
    protected void initSkinPrices()                   {}
    protected void initEntityScale()                  {}
    protected SkinInfo getSkinInfo(int skinIndex)     { return null; }
    protected void prepareGhostEntity(OWEntity ghost) {}

    private OWEntity getOrCreatePreviewEntity(int targetSkinIndex) {
        if (previewEntity != null && previewEntitySkinIndex == targetSkinIndex) return previewEntity;

        @SuppressWarnings("unchecked")
        OWEntity ghost = (OWEntity) entity.getType().create(entity.level());
        if (ghost == null) return null;

        try {
            net.minecraft.nbt.CompoundTag nbt = new net.minecraft.nbt.CompoundTag();
            entity.saveWithoutId(nbt);
            nbt.putBoolean("Saddle", false);
            nbt.remove("SaddleItem");
            ghost.load(nbt);
        } catch (Exception ignored) {}

        for (net.minecraft.world.entity.EquipmentSlot slot : net.minecraft.world.entity.EquipmentSlot.values()) {
            try { ghost.setItemSlot(slot, net.minecraft.world.item.ItemStack.EMPTY); }
            catch (Exception ignored) {}
        }

        prepareGhostEntity(ghost);
        ghost.changeSkinSilent(targetSkinIndex);
        previewEntity          = ghost;
        previewEntitySkinIndex = targetSkinIndex;
        return previewEntity;
    }

    private void discardPreviewEntity() {
        previewEntity          = null;
        previewEntitySkinIndex = -1;
    }

    public int getRarityColor(int skinIndex) {
        if (LEGENDARY_SKIN.stream().anyMatch(b -> getSkinIndexForButton(b) == skinIndex)) return 0xfdd85f;
        if (EPIC_SKIN.stream()     .anyMatch(b -> getSkinIndexForButton(b) == skinIndex)) return 0x9b59b6;
        if (HALLOWEEN_SKIN.stream().anyMatch(b -> getSkinIndexForButton(b) == skinIndex)) return 0xd86a2e;
        if (RARE_SKIN.stream()     .anyMatch(b -> getSkinIndexForButton(b) == skinIndex)) return 0xf19f60;
        return 0xc4def2;
    }

    private Component getUnlockDescription(SkinInfo info) {
        return switch (info.unlockType) {
            case FREE     -> Component.translatable("tooltip.skinUnlockFree");
            case LEVEL    -> Component.translatable("tooltip.skinUnlockLevel",    info.unlockValue);
            case PRESTIGE -> Component.translatable("tooltip.skinUnlockPrestige", info.unlockValue);
            case QUEST    -> Component.translatable("tooltip.skinUnlockQuest",
                    info.questId != -1 ? Component.translatable("skin.unlock.quest." + info.questId) : Component.empty());
        };
    }

    public boolean isLocked(int skinIndex) { return lockedSkins.getOrDefault(skinIndex, false); }
    public int getSkinPrice(int skinIndex)  { return skinPrices.getOrDefault(skinIndex, 0); }

    public void setLockState(int skinIndex, boolean locked) {
        if (lockedSkins.getOrDefault(skinIndex, false) == locked) return;
        lockedSkins.put(skinIndex, locked);
        updateButtonColors();
        addButtonsToList();
    }

    @Override
    protected void init() {
        super.init();
        int i = (this.width  - imageWidth)  / 2;
        int j = (this.height - imageHeight) / 2;

        this.buttonList = new OWScreenUtils.ButtonListWidget(
                this.minecraft, LST_W - 4, LST_H,
                j + LST_Y, 20, this);
        this.buttonList.setX(i + LST_X + 2);
        createAndAddButtons();
        this.addWidget(this.buttonList);

        equipButton = Button.builder(
                        Component.translatable("tooltip.equipSkin")
                                .setStyle(Style.EMPTY.withColor(TextColor.fromRgb(0x7ddd73)).withBold(true)),
                        btn -> doEquip())
                .bounds(0, 0, 72, 20).build();
        equipButton.visible = false;
        this.addRenderableWidget(equipButton);

        buyButton = Button.builder(Component.empty(), btn -> doBuy())
                .bounds(0, 0, 60, 20).build();
        buyButton.visible = false;
        this.addRenderableWidget(buyButton);

        confirmYesButton = Button.builder(
                        Component.translatable("tooltip.yesButton")
                                .setStyle(Style.EMPTY.withColor(TextColor.fromRgb(0x7ddd73))),
                        btn -> doConfirmBuy())
                .bounds(0, 0, 52, 20).build();
        confirmYesButton.visible = false;
        this.addRenderableWidget(confirmYesButton);

        confirmNoButton = Button.builder(
                        Component.translatable("tooltip.noButton")
                                .setStyle(Style.EMPTY.withColor(TextColor.fromRgb(0xdd4444))),
                        btn -> { confirmingPurchase = false; })
                .bounds(0, 0, 52, 20).build();
        confirmNoButton.visible = false;
        this.addRenderableWidget(confirmNoButton);

        initEntityScale();

        tabsRenderer.init(this.width, this.height, imageWidth, imageHeight, entity, this::addWidget);
        tabsRenderer.setActiveTab(OWTabsRenderer.Tab.COSMETICS);
    }

    private void doEquip() {
        if (selectedSkinIndex == -1 || isLocked(selectedSkinIndex) || entity == null) return;
        entity.changeSkin(selectedSkinIndex, true);
        OWNetworkHandler.sendToServer(new OWVariantsSkinsPacket(selectedSkinIndex));
    }

    private void doBuy() {
        if (selectedSkinIndex == -1 || !isLocked(selectedSkinIndex) || entity == null) return;
        int price = getSkinPrice(selectedSkinIndex);
        if (price > 0 && entity.getPrestigeLevel() >= price) confirmingPurchase = true;
        else showLockedMessage(price);
    }

    private void doConfirmBuy() {
        confirmingPurchase = false;
        if (entity == null || selectedSkinIndex == -1) return;
        entity.level().playSound(
                Minecraft.getInstance().player,
                Minecraft.getInstance().player.blockPosition(),
                SoundEvents.UI_TOAST_CHALLENGE_COMPLETE,
                SoundSource.MASTER, 0.5f, 1.2f);
        buySkin(selectedSkinIndex, getSkinPrice(selectedSkinIndex));
    }

    protected Button createSkinButton(Component text, int skinIndex, List<Button> list) {
        Button button = Button.builder(
                        text.copy().setStyle(Style.EMPTY.withColor(TextColor.fromRgb(0xFFFFFF)).withBold(true)),
                        btn -> onSkinButtonPressed(skinIndex))
                .width(LST_W - 8).build();
        list.add(button);
        return button;
    }

    protected void createAndAddButtons() {
        LEGENDARY_SKIN.clear(); EPIC_SKIN.clear(); HALLOWEEN_SKIN.clear();
        RARE_SKIN.clear(); COMMON_SKIN.clear();
        updateButtonColors();
        addButtonsToList();
    }

    protected void addButtonsToList() {
        this.buttonList.clearEntries();
        addCategoryGroup(buildLegendaryOrdered());
        addCategoryGroup(HALLOWEEN_SKIN);
        addCategoryGroup(EPIC_SKIN);
        addCategoryGroup(RARE_SKIN);
        addCategoryGroup(COMMON_SKIN);
    }

    private void addCategoryGroup(List<Button> buttons) {
        if (buttons.isEmpty()) return;
        for (Button btn : buttons) {
            int idx = getSkinIndexForButton(btn);
            if (idx != -1)
                this.buttonList.addButtonEntry(new OWScreenUtils.ButtonListWidget.ButtonEntry(btn, idx, this));
        }
    }

    private List<Button> buildLegendaryOrdered() {
        List<Button> ordered = new ArrayList<>(LEGENDARY_SKIN);
        Button skin1 = ordered.stream().filter(b -> getSkinIndexForButton(b) == 1).findFirst().orElse(null);
        if (skin1 != null) { ordered.remove(skin1); ordered.add(skin1); }
        return ordered;
    }

    protected void updateButtonColors() {
        tintButtons(LEGENDARY_SKIN, 0xfdd85f);
        tintButtons(HALLOWEEN_SKIN, 0xd86a2e);
        tintButtons(EPIC_SKIN,      0x9b59b6);
        tintButtons(RARE_SKIN,      0xf19f60);
        tintButtons(COMMON_SKIN,    0xc4def2);
    }

    private void tintButtons(List<Button> list, int color) {
        for (Button btn : list) {
            int idx = getSkinIndexForButton(btn);
            btn.setMessage(btn.getMessage().copy().setStyle(
                    Style.EMPTY.withColor(TextColor.fromRgb(isLocked(idx) ? 0x777777 : color)).withBold(true)));
        }
    }

    protected int getSkinIndexForButton(Button button) { return -1; }

    protected void onSkinButtonPressed(int skinIndex) {
        confirmingPurchase = false;
        if (this.selectedSkinIndex == skinIndex) {
            this.selectedSkinIndex = -1;
            discardPreviewEntity();
        } else {
            this.selectedSkinIndex = skinIndex;
        }
    }

    public static void buySkin(int skinIndex, int price) {
        OWNetworkHandler.sendToServer(new SkinBuyingPacket(price, skinIndex));
        Minecraft.getInstance().player.displayClientMessage(
                Component.translatable("skin.unlocked.message").withStyle(ChatFormatting.GREEN), false);
    }

    private void showLockedMessage(int price) {
        Minecraft.getInstance().player.displayClientMessage(
                Component.translatable(price > 0 ? "skin.locked.message.prestige" : "skin.locked.message")
                        .withStyle(ChatFormatting.RED), false);
    }

    @Override public void onClose()          { discardPreviewEntity(); super.onClose(); }
    @Override public boolean isPauseScreen() { return false; }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partial) {
        this.xMouse = mouseX;
        this.yMouse = mouseY;

        int i = (this.width  - imageWidth)  / 2;
        int j = (this.height - imageHeight) / 2;

        boolean showEquip = selectedSkinIndex != -1 && !isLocked(selectedSkinIndex)
                && entity != null && selectedSkinIndex != entity.getSkinIndex();
        boolean showBuy   = selectedSkinIndex != -1 && isLocked(selectedSkinIndex)
                && getSkinPrice(selectedSkinIndex) > 0;

        equipButton.visible = false; buyButton.visible = false;
        confirmYesButton.visible = false; confirmNoButton.visible = false;

        super.render(g, mouseX, mouseY, partial);

        g.blit(BG_TEXTURE,   i, j, 0, 0, imageWidth, imageHeight);
        if (selectedSkinIndex != -1)
            g.blit(BG_2_TEXTURE, i + imageHeight + 9, j - 25, 0, 0, 121, 233);

        tabsRenderer.renderTabs(g, this.font, entity, i, j, mouseX, mouseY);

        renderLeftPanel(g, i, j);
        renderRightPanel(g, i, j, mouseX, mouseY);

        this.buttonList.render(g, mouseX, mouseY, partial);

        int btnCX     = i + DET_X + DET_W / 2;
        int btnBottom = j - 25 + 233 - 20 - 5;

        equipButton.setX(btnCX - 36);      equipButton.setY(btnBottom);
        buyButton.setX(btnCX - 30);        buyButton.setY(btnBottom);
        confirmYesButton.setX(btnCX - 55); confirmYesButton.setY(btnBottom);
        confirmNoButton.setX(btnCX + 3);  confirmNoButton.setY(btnBottom);

        if (showEquip) {
            equipButton.visible = true;
            equipButton.render(g, mouseX, mouseY, partial);
        }
        if (showBuy && !confirmingPurchase) {
            buyButton.visible = true;
            buyButton.render(g, mouseX, mouseY, partial);
            renderBuyButtonContent(g, getSkinPrice(selectedSkinIndex));
        }
        if (showBuy && confirmingPurchase)
            renderConfirmationOverlay(g, i, j, mouseX, mouseY, partial);

        addTooltipsToButtons();
        updateLockStates();
    }

    private void renderLeftPanel(GuiGraphics g, int i, int j) {
        if (entity == null) return;

        g.blit(ICONS_LOCATION, i + LST_X, j + 5, 0, 143, 10, 10);
        g.drawString(this.font, String.valueOf(entity.getPrestigeLevel()), i + LST_X + 12, (int) (j + 4.5f), 0xc8f6ff);

        int fx = i + LST_X;
        int fy = j + LST_Y + LST_H + 3;
        int fw = LST_W;
        int currentSkin = entity.getSkinIndex();
        int rc          = getRarityColor(currentSkin);

        g.fill(fx, fy, fx + fw, fy + STRIP_H, 0xBB08080F);
        g.fill(fx, fy, fx + 2,  fy + STRIP_H, rc | 0xFF000000);
        g.fill(fx, fy, fx + fw, fy + 1,       0xFF2A2A2A);

        InventoryScreen.renderEntityInInventoryFollowsMouse(
                g, fx + 2, fy + 1, fx + 22, fy + STRIP_H - 1,
                Math.min(entityScale / 4 + 3, 13), 0.04f,
                xMouse, yMouse, entity);

        g.fill(fx + 24, fy + 4, fx + 25, fy + STRIP_H - 4, 0xFF252525);

        SkinInfo info = getSkinInfo(currentSkin);
        Component nameComp = info != null
                ? Component.translatable(info.nameKey).setStyle(Style.EMPTY.withColor(TextColor.fromRgb(rc)).withBold(true))
                : Component.empty();

        g.drawString(this.font,
                Component.translatable("tooltip.currentSkin")
                        .setStyle(Style.EMPTY.withColor(TextColor.fromRgb(0x555555)).withItalic(true)),
                fx + 28, fy + 4, 0x555555);

        int nameW = this.font.width(nameComp);
        int maxW  = fw - 30;
        if (nameW > maxW) {
            float s = (float) maxW / nameW;
            g.pose().pushPose();
            g.pose().translate(fx + 28, fy + 14, 0);
            g.pose().scale(s, s, 1f);
            g.drawString(this.font, nameComp, 0, 0, rc);
            g.pose().popPose();
        } else {
            g.drawString(this.font, nameComp, fx + 28, fy + 14, rc);
        }
    }

    private void renderRightPanel(GuiGraphics g, int i, int j, int mouseX, int mouseY) {
        int px = i + DET_X;
        int py = j + DET_Y;
        int pw = DET_W;

        if (selectedSkinIndex == -1) return;

        SkinInfo info = getSkinInfo(selectedSkinIndex);
        if (info == null) return;
        int rc = getRarityColor(selectedSkinIndex);

        g.fill(px,     py,     px + pw, py + 3,  rc | 0xFF000000);
        g.fill(px,     py + 3, px + pw, py + 4, (0x55 << 24) | (rc & 0x00FFFFFF));

        int prevTop    = py + 5;
        int prevBottom = py + PREV_H;
        g.fill(px + 2, prevTop,         px + pw - 2, prevBottom,     0x22FFFFFF);
        g.fill(px + 2, prevTop,         px + pw - 2, prevTop + 1,    0xFF3A3A3A);
        g.fill(px + 2, prevBottom - 1,  px + pw - 2, prevBottom,     0xFF3A3A3A);

        OWEntity ghost = getOrCreatePreviewEntity(selectedSkinIndex);
        if (ghost != null) {
            int ps = Math.min(Math.max((int)(entityScale * 0.75f), 18), 44);

            g.enableScissor(px + 2, prevTop, px + pw - 2, prevBottom);
            InventoryScreen.renderEntityInInventoryFollowsMouse(
                    g, px + 2, prevTop + 6, px + pw - 2, prevBottom - 2,
                    ps, 0.08f, mouseX, mouseY, ghost);
            g.disableScissor();
        }

        Component previewLabel = Component.translatable("tooltip.skinPreview")
                .setStyle(Style.EMPTY.withColor(TextColor.fromRgb(0x555555)).withItalic(true));
        g.drawString(this.font, previewLabel,
                px + pw / 2 - this.font.width(previewLabel) / 2, prevTop + 2, 0x555555);

        int curY = prevBottom + 5;

        Component nameComp = Component.translatable(info.nameKey)
                .setStyle(Style.EMPTY.withColor(TextColor.fromRgb(rc)).withBold(true));
        g.drawString(this.font, nameComp, px + pw / 2 - this.font.width(nameComp) / 2, curY, rc);
        curY += 12;

        g.fill(px + 4, curY, px + pw - 4, curY + 1, (0x77 << 24) | (rc & 0x00FFFFFF));
        curY += 5;

        for (FormattedCharSequence line : this.font.split(Component.translatable(info.descriptionKey), pw - 8)) {
            g.drawString(this.font, line, px + pw / 2 - this.font.width(line) / 2, curY, 0xBBBBBB);
            curY += 8;
        }

        curY += OBTAIN_Y_GAP;
        g.fill(px + OBTAIN_X_PAD, curY, px + DET_W - OBTAIN_X_PAD, curY + 1, 0xFF333333);
        curY += 5;

        if (confirmingPurchase) {
            Component question = Component.translatable("tooltip.confirmationBuyingSkin")
                    .setStyle(Style.EMPTY.withColor(TextColor.fromRgb(0xCCCCCC)));
            for (FormattedCharSequence line : this.font.split(question, OBTAIN_W)) {
                g.drawString(this.font, line, px + pw / 2 - this.font.width(line) / 2, curY, 0xCCCCCC);
                curY += OBTAIN_LINE_H;
            }
        } else {
            Component obtLabel = Component.translatable("tooltip.skinObtention")
                    .setStyle(Style.EMPTY.withColor(TextColor.fromRgb(0x777777)).withItalic(true));
            g.drawString(this.font, obtLabel, px + pw / 2 - this.font.width(obtLabel) / 2, curY, 0x777777);
            curY += OBTAIN_LBL_H;

            for (FormattedCharSequence line : this.font.split(getUnlockDescription(info), OBTAIN_W)) {
                g.drawString(this.font, line, px + pw / 2 - this.font.width(line) / 2, curY, 0xDDDDDD);
                curY += OBTAIN_LINE_H;
            }
        }

        curY += 4;
        g.fill(px + OBTAIN_X_PAD, curY, px + DET_W - OBTAIN_X_PAD, curY + 1, 0xFF333333);

        if (info.unlockType == SkinInfo.UnlockType.QUEST && info.questId != -1 && entity != null) {
            CosmeticsQuest quest = CosmeticsQuestsRegistry.getById(info.questId);
            if (quest != null) {
                curY += 8;
                boolean completed = quest.isCompleted(entity.getUUID());
                float   frac      = quest.getProgressFraction(entity.getUUID());
                int barX = px + 6; int barW = pw - 12; int barH = 4;
                g.fill(barX - 1, curY - 1, barX + barW + 1, curY + barH + 1, 0xFF2A2A2A);
                g.fill(barX, curY, barX + barW, curY + barH, 0xFF111111);
                int fillW = (int)(barW * Math.min(frac, 1f));
                if (fillW > 0)
                    g.fill(barX, curY, barX + fillW, curY + barH, completed ? 0xFF44CC66 : 0xFFE8901A);
                Component pctText = Component.literal(Math.round(frac * 100f) + "%")
                        .setStyle(Style.EMPTY.withColor(TextColor.fromRgb(0xDDDDDD)));
                g.drawString(this.font, pctText,
                        px + pw / 2 - this.font.width(pctText) / 2, curY - 1, 0xFFFFFF, false);
            }
        }
    }

    private void renderConfirmationOverlay(GuiGraphics g, int i, int j, int mouseX, int mouseY, float partial) {
        confirmYesButton.visible = true;
        confirmNoButton.visible  = true;
        confirmYesButton.render(g, mouseX, mouseY, partial);
        confirmNoButton.render(g, mouseX, mouseY, partial);
    }

    private void renderBuyButtonContent(GuiGraphics g, int price) {
        String priceStr = String.valueOf(price);
        int totalW = 10 + 3 + this.font.width(priceStr);
        int iconX  = buyButton.getX() + (buyButton.getWidth() - totalW) / 2;
        int iconY  = buyButton.getY() + 5;
        g.blit(ICONS_LOCATION, iconX, iconY, 0, 143, 10, 10);
        g.drawString(this.font, priceStr, iconX + 13, iconY, 0xc8f6ff, false);
    }

    protected void addTooltipsToButtons() {}
    protected void updateLockStates()     {}
}