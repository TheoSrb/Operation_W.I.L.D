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
import net.tiew.operationWild.networking.packets.to_server.OpenOWInventoryPacket;
import net.tiew.operationWild.networking.packets.to_server.OWVariantsSkinsPacket;
import net.tiew.operationWild.networking.packets.to_server.SkinBuyingPacket;
import net.tiew.operationWild.screen.OWScreenUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@OnlyIn(Dist.CLIENT)
public class OWSkinsInterface extends Screen {

    private static final ResourceLocation OW_SKINS_INTERFACE_LOCATION =
            ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "textures/gui/ow_skins_interface_gui.png");
    private static final ResourceLocation ICONS_LOCATION =
            ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "textures/gui/mob_types.png");

    protected final OWEntity entity;
    private float xMouse;
    private float yMouse;

    public int imageWidth = 140;
    protected int imageHeight = 218;
    protected int entityScale = 17;

    /** Skin sélectionné dans le panel de détails. -1 = panel fermé. */
    public int selectedSkinIndex = -1;

    /**
     * Entité fantôme utilisée exclusivement pour la preview 3D.
     * Créée une seule fois à chaque changement de selectedSkinIndex.
     * La vraie entité n'est jamais modifiée.
     */
    private OWEntity previewEntity = null;
    private int previewEntitySkinIndex = -1; // skin actuellement appliqué sur previewEntity

    protected static final int DETAIL_PANEL_OFFSET_X = 25;
    protected static final int DETAIL_PANEL_WIDTH    = 170;

    private static final int FOOTER_HEIGHT = 48;

    protected Button backButton;
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

    // =========================================================================
    // SkinInfo
    // =========================================================================
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

        public static SkinInfo free(String n, String d)              { return new SkinInfo(n, d, UnlockType.FREE, 0, -1); }
        public static SkinInfo level(String n, String d, int lvl)    { return new SkinInfo(n, d, UnlockType.LEVEL, lvl, -1); }
        public static SkinInfo prestige(String n, String d, int pts) { return new SkinInfo(n, d, UnlockType.PRESTIGE, pts, -1); }
        public static SkinInfo quest(String n, String d, int id)     { return new SkinInfo(n, d, UnlockType.QUEST, 0, id); }
    }

    // =========================================================================
    // Constructeur
    // =========================================================================
    public OWSkinsInterface() {
        super(Component.literal("OWSkinsInterface"));
        if (Minecraft.getInstance().player.getRootVehicle() instanceof OWEntity e)
            this.entity = e;
        else
            this.entity = null;
        initLockedSkins();
        initSkinPrices();
    }

    protected void initLockedSkins() {}
    protected void initSkinPrices()  {}
    protected void initEntityScale() {}
    protected SkinInfo getSkinInfo(int skinIndex) { return null; }

    // =========================================================================
    // Entité fantôme — créée une seule fois par skin sélectionné
    // =========================================================================

    /**
     * Retourne l'entité fantôme avec le bon skin.
     * La recrée uniquement si le skin cible a changé depuis la dernière fois.
     */
    private OWEntity getOrCreatePreviewEntity(int targetSkinIndex) {
        if (previewEntity != null && previewEntitySkinIndex == targetSkinIndex) {
            return previewEntity;
        }

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

        // Vide les slots d'équipement standards
        for (net.minecraft.world.entity.EquipmentSlot slot :
                net.minecraft.world.entity.EquipmentSlot.values()) {
            try { ghost.setItemSlot(slot, net.minecraft.world.item.ItemStack.EMPTY); }
            catch (Exception ignored) {}
        }

        // Hook pour la suppression custom de la selle — override dans TigerSkinsScreen, etc.
        prepareGhostEntity(ghost);

        // Applique le skin sans sons ni particules
        ghost.changeSkinSilent(targetSkinIndex);

        previewEntity = ghost;
        previewEntitySkinIndex = targetSkinIndex;
        return previewEntity;
    }

    /**
     * Hook appelé après la création du fantôme, avant le changeSkin.
     * Override dans les sous-classes pour retirer la selle ou tout autre équipement.
     *
     * Exemple dans TigerSkinsScreen :
     *   @Override
     *   protected void prepareGhostEntity(OWEntity ghost) {
     *       ((TigerEntity) ghost).setSaddled(false); // remplace par ta méthode réelle
     *   }
     */
    protected void prepareGhostEntity(OWEntity ghost) {}

    /** Appelé quand le panel se ferme pour libérer la référence. */
    private void discardPreviewEntity() {
        previewEntity = null;
        previewEntitySkinIndex = -1;
    }

    // =========================================================================
    // Rareté
    // =========================================================================
    public int getRarityColor(int skinIndex) {
        if (LEGENDARY_SKIN.stream().anyMatch(b -> getSkinIndexForButton(b) == skinIndex)) return 0xfdd85f;
        if (EPIC_SKIN.stream()     .anyMatch(b -> getSkinIndexForButton(b) == skinIndex)) return 0x9b59b6;
        if (HALLOWEEN_SKIN.stream().anyMatch(b -> getSkinIndexForButton(b) == skinIndex)) return 0xd86a2e;
        if (RARE_SKIN.stream()     .anyMatch(b -> getSkinIndexForButton(b) == skinIndex)) return 0xf19f60;
        return 0xc4def2;
    }

    private int getRarityUV(int skinIndex) {
        if (LEGENDARY_SKIN.stream().anyMatch(b -> getSkinIndexForButton(b) == skinIndex)) return 74;
        if (EPIC_SKIN.stream()     .anyMatch(b -> getSkinIndexForButton(b) == skinIndex)) return 88;
        if (HALLOWEEN_SKIN.stream().anyMatch(b -> getSkinIndexForButton(b) == skinIndex)) return 88;
        if (RARE_SKIN.stream()     .anyMatch(b -> getSkinIndexForButton(b) == skinIndex)) return 102;
        return 116;
    }

    private Component getUnlockDescription(SkinInfo info) {
        return switch (info.unlockType) {
            case FREE     -> Component.translatable("tooltip.skinUnlockFree");
            case LEVEL    -> Component.translatable("tooltip.skinUnlockLevel",    info.unlockValue);
            case PRESTIGE -> Component.translatable("tooltip.skinUnlockPrestige", info.unlockValue);
            case QUEST    -> Component.translatable("tooltip.skinUnlockQuest",
                    info.questId != -1
                            ? Component.translatable("skin.unlock.quest." + info.questId)
                            : Component.empty());
        };
    }

    // =========================================================================
    // Accesseurs
    // =========================================================================
    public boolean isLocked(int skinIndex)  { return lockedSkins.getOrDefault(skinIndex, false); }
    public int getSkinPrice(int skinIndex)   { return skinPrices.getOrDefault(skinIndex, 0); }

    public void setLockState(int skinIndex, boolean locked) {
        if (lockedSkins.getOrDefault(skinIndex, false) == locked) return;
        lockedSkins.put(skinIndex, locked);
        updateButtonColors();
        addButtonsToList();
    }

    // =========================================================================
    // Init
    // =========================================================================
    @Override
    protected void init() {
        super.init();
        int i = (this.width  - this.imageWidth)  / 2;
        int j = (this.height - this.imageHeight) / 2;

        backButton = createButton("", 0xFFFFFF, -135, 0, 16, 16,
                () -> OWNetworkHandler.sendToServer(new OpenOWInventoryPacket()));
        this.addRenderableWidget(backButton);

        int listHeight = imageHeight - 50 - FOOTER_HEIGHT - 8;
        this.buttonList = new OWScreenUtils.ButtonListWidget(
                this.minecraft,
                this.imageWidth - 20, listHeight,
                j + 50, 28, this);
        this.buttonList.setX(i - 120 + 10);
        createAndAddButtons();
        this.addWidget(this.buttonList);

        int btnX = (i + DETAIL_PANEL_OFFSET_X) + DETAIL_PANEL_WIDTH / 2 - 50;
        int buyBtnX = (i + DETAIL_PANEL_OFFSET_X) + DETAIL_PANEL_WIDTH / 2 - 35;
        int btnY = j + imageHeight - 28;

        equipButton = Button.builder(
                        Component.translatable("tooltip.equipSkin")
                                .setStyle(Style.EMPTY.withColor(TextColor.fromRgb(0x7ddd73)).withBold(true)),
                        btn -> doEquip())
                .bounds(btnX, btnY, 100, 20).build();
        equipButton.visible = false;
        this.addRenderableWidget(equipButton);

        buyButton = Button.builder(Component.empty(), btn -> doBuy())
                .bounds(buyBtnX, btnY, 70, 20).build();
        buyButton.visible = false;
        this.addRenderableWidget(buyButton);

        int confirmCenterX = (i + DETAIL_PANEL_OFFSET_X) + DETAIL_PANEL_WIDTH / 2;
        confirmYesButton = Button.builder(
                        Component.translatable("tooltip.yesButton")
                                .setStyle(Style.EMPTY.withColor(TextColor.fromRgb(0x7ddd73))),
                        btn -> doConfirmBuy())
                .bounds(confirmCenterX - 68, btnY, 58, 20).build();
        confirmYesButton.visible = false;
        this.addRenderableWidget(confirmYesButton);

        confirmNoButton = Button.builder(
                        Component.translatable("tooltip.noButton")
                                .setStyle(Style.EMPTY.withColor(TextColor.fromRgb(0xdd4444))),
                        btn -> { confirmingPurchase = false; })
                .bounds(confirmCenterX + 10, btnY, 58, 20).build();
        confirmNoButton.visible = false;
        this.addRenderableWidget(confirmNoButton);

        initEntityScale();
    }

    private void doEquip() {
        if (selectedSkinIndex == -1 || isLocked(selectedSkinIndex) || entity == null) return;
        entity.changeSkin(selectedSkinIndex, true);
        OWNetworkHandler.sendToServer(new OWVariantsSkinsPacket(selectedSkinIndex));
    }

    private void doBuy() {
        if (selectedSkinIndex == -1 || !isLocked(selectedSkinIndex) || entity == null) return;
        int price = getSkinPrice(selectedSkinIndex);
        if (price > 0 && entity.getPrestigeLevel() >= price)
            confirmingPurchase = true;
        else
            showLockedMessage(price);
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

    // =========================================================================
    // Boutons
    // =========================================================================
    public Button createButton(String text, int color,
                               int positionX, int positionY,
                               int width, int height, Runnable onClick) {
        int i = (this.width  - this.imageWidth)  / 2;
        int j = (this.height - this.imageHeight) / 2;
        return Button.builder(
                        Component.literal(text).setStyle(Style.EMPTY.withColor(TextColor.fromRgb(color))),
                        btn -> onClick.run())
                .bounds(i + positionX, j - positionY, width, height).build();
    }

    protected Button createSkinButton(Component text, int skinIndex, List<Button> list) {
        Button button = Button.builder(
                        text.copy().setStyle(Style.EMPTY.withColor(TextColor.fromRgb(0xFFFFFF)).withBold(true)),
                        btn -> onSkinButtonPressed(skinIndex))
                .width((int)(this.imageWidth * 0.8)).build();
        list.add(button);
        return button;
    }

    protected void createAndAddButtons() {
        LEGENDARY_SKIN.clear();
        EPIC_SKIN.clear();
        HALLOWEEN_SKIN.clear();
        RARE_SKIN.clear();
        COMMON_SKIN.clear();
        updateButtonColors();
        addButtonsToList();
    }

    protected void addButtonsToList() {
        this.buttonList.clearEntries();
        addCategoryGroup("LEGENDAIRE", 0xfdd85f, buildLegendaryOrdered());
        addCategoryGroup("HALLOWEEN",  0xd86a2e, HALLOWEEN_SKIN);
        addCategoryGroup("EPIQUE",     0x9b59b6, EPIC_SKIN);
        addCategoryGroup("RARE",       0xf19f60, RARE_SKIN);
        addCategoryGroup("COMMUN",     0xc4def2, COMMON_SKIN);
    }

    private void addCategoryGroup(String label, int color, List<Button> buttons) {
        if (buttons.isEmpty()) return;
        // Pas de header de catégorie
        for (Button btn : buttons) {
            int idx = getSkinIndexForButton(btn);
            if (idx != -1)
                this.buttonList.addButtonEntry(
                        new OWScreenUtils.ButtonListWidget.ButtonEntry(btn, idx, this));
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
                    Style.EMPTY.withColor(TextColor.fromRgb(isLocked(idx) ? 0x777777 : color))
                            .withBold(true)));
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

    @Override
    public void onClose() {
        discardPreviewEntity();
        super.onClose();
    }

    @Override public boolean isPauseScreen() { return false; }

    // =========================================================================
    // Render
    // =========================================================================
    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partial) {
        this.xMouse = mouseX;
        this.yMouse = mouseY;

        int i = (this.width  - this.imageWidth)  / 2;
        int j = (this.height - this.imageHeight) / 2;

        boolean showEquip = selectedSkinIndex != -1
                && !isLocked(selectedSkinIndex)
                && entity != null && selectedSkinIndex != entity.getSkinIndex(); // déjà équipé = pas de bouton
        boolean showBuy   = selectedSkinIndex != -1
                &&  isLocked(selectedSkinIndex)
                && getSkinPrice(selectedSkinIndex) > 0;

        backButton.visible       = false;
        equipButton.visible      = false;
        buyButton.visible        = false;
        confirmYesButton.visible = false;
        confirmNoButton.visible  = false;

        super.render(g, mouseX, mouseY, partial);

        // Entité principale supprimée (c'était celle à droite qui suivait la souris)

        // 2. Fond du panel principal
        g.blit(OW_SKINS_INTERFACE_LOCATION, i - 120, j, 0, 0, imageWidth, imageHeight);

        // 3. Footer "Skin actuel"
        renderCurrentSkinFooter(g, i, j);

        // 4. Bouton retour
        backButton.visible = true;
        backButton.render(g, mouseX, mouseY, partial);

        // 5. Liste scrollable
        this.buttonList.render(g, mouseX, mouseY, partial);

        // 6. Icône type
        g.blit(ICONS_LOCATION, i - 134, j + 1, 0, 130, 13, 13);

        // 7. Panel de détails (rendu avec l'entité fantôme)
        renderDetailPanel(g, mouseX, mouseY);

        // 8. Boutons Équiper / Acheter / Confirmation
        if (showEquip) { equipButton.visible = true; equipButton.render(g, mouseX, mouseY, partial); }
        if (showBuy && !confirmingPurchase) {
            buyButton.visible = true;
            buyButton.render(g, mouseX, mouseY, partial);
            renderBuyButtonContent(g, getSkinPrice(selectedSkinIndex));
        }
        if (showBuy && confirmingPurchase) {
            renderConfirmationOverlay(g, mouseX, mouseY, partial);
        }

        addTooltipsToButtons();
        renderTexts(g, i, j);
        updateLockStates();
    }

    // =========================================================================
    // Footer "Skin actuel" (intégré dans le panel principal, en bas)
    // =========================================================================
    protected void renderCurrentSkinFooter(GuiGraphics g, int i, int j) {
        if (entity == null) return;

        int fx = i - 120 + 4;
        int fy = j + imageHeight - FOOTER_HEIGHT - 3;
        int fw = imageWidth - 8;
        int fh = FOOTER_HEIGHT;

        int currentSkin = entity.getSkinIndex();
        int rc = getRarityColor(currentSkin);

        // Fond + barre rareté gauche + bordure haute
        g.fill(fx,      fy, fx + fw, fy + fh, 0xCC08080F);
        g.fill(fx,      fy, fx + 3,  fy + fh, rc | 0xFF000000);
        g.fill(fx,      fy, fx + fw, fy + 1,  0xFF2A2A2A);

        // Petite preview 3D du skin actuel (à gauche dans le footer)
        int previewRight = fx + 58;
        InventoryScreen.renderEntityInInventoryFollowsMouse(
                g,
                fx + 3, fy + 2,
                previewRight, fy + fh - 2,
                Math.min(entityScale / 3 + 4, 18), 0.04F,
                (float) xMouse, (float) yMouse, entity);

        // Séparateur vertical
        g.fill(previewRight + 3, fy + 6, previewRight + 4, fy + fh - 6, 0xFF252525);

        // Label + nom du skin actuel
        int textX = previewRight + 8;
        g.drawString(this.font,
                Component.translatable("tooltip.currentSkin")
                        .setStyle(Style.EMPTY.withColor(TextColor.fromRgb(0x555555)).withItalic(true)),
                textX, fy + 10, 0x555555);

        SkinInfo info = getSkinInfo(currentSkin);
        Component nameComp = (info != null)
                ? Component.translatable(info.nameKey)
                .setStyle(Style.EMPTY.withColor(TextColor.fromRgb(rc)).withBold(true))
                : Component.literal("").setStyle(Style.EMPTY.withColor(TextColor.fromRgb(rc)));

        int nameTextY = fy + 23;
        int maxNameW  = (fx + fw - 4) - textX;  // largeur disponible jusqu'au bord droit du footer
        int nameW     = this.font.width(nameComp);
        if (nameW > maxNameW) {
            float scale = (float) maxNameW / nameW;
            g.pose().pushPose();
            g.pose().translate(textX, nameTextY, 0);
            g.pose().scale(scale, scale, 1.0f);
            g.drawString(this.font, nameComp, 0, 0, rc);
            g.pose().popPose();
        } else {
            g.drawString(this.font, nameComp, textX, nameTextY, rc);
        }
    }

    // =========================================================================
    // Panel de détails — preview via entité fantôme
    // =========================================================================
    protected void renderDetailPanel(GuiGraphics g, int mouseX, int mouseY) {
        if (selectedSkinIndex == -1 || entity == null) return;

        int i  = (this.width  - this.imageWidth)  / 2;
        int j  = (this.height - this.imageHeight) / 2;
        int px = i + DETAIL_PANEL_OFFSET_X;
        int py = j;
        int pw = DETAIL_PANEL_WIDTH;
        int ph = imageHeight;
        int rc = getRarityColor(selectedSkinIndex);

        // Fond + barre de rareté + bordures
        g.fill(px, py, px + pw, py + ph, 0xF0151820);
        g.fill(px, py,     px + pw, py + 4, rc | 0xFF000000);
        g.fill(px, py + 4, px + pw, py + 5, (0x66 << 24) | (rc & 0x00FFFFFF));
        g.fill(px,          py,          px + pw,     py + 1,  0xFF565656);
        g.fill(px,          py + ph - 1, px + pw,     py + ph, 0xFF565656);
        g.fill(px,          py,          px + 1,      py + ph, 0xFF565656);
        g.fill(px + pw - 1, py,          px + pw,     py + ph, 0xFF565656);

        SkinInfo info = getSkinInfo(selectedSkinIndex);
        if (info == null) return;

        // ── Preview 3D en PREMIER — le texte sera dessiné par-dessus ─────────
        int previewTop    = py + ph - 112;
        int previewBottom = py + ph - 30;

        g.fill(px + 4, previewTop - 2,    px + pw - 4, previewBottom + 2, 0x22FFFFFF);
        g.fill(px + 4, previewTop - 2,    px + pw - 4, previewTop - 1,    0xFF4A4A4A);
        g.fill(px + 4, previewBottom + 1, px + pw - 4, previewBottom + 2, 0xFF4A4A4A);

        OWEntity ghost = getOrCreatePreviewEntity(selectedSkinIndex);
        if (ghost != null) {
            int previewScale = Math.min(Math.max((int)(entityScale * 0.8f), 22), 52);
            g.enableScissor(px + 4, previewTop - 2, px + pw - 4, previewBottom + 2);
            InventoryScreen.renderEntityInInventoryFollowsMouse(
                    g,
                    px + 4,      previewTop + 12,
                    px + pw - 4, previewBottom,
                    previewScale, 0.08F,
                    mouseX, mouseY, ghost);
            g.disableScissor();
        }

        // ── Tout le texte APRÈS l'entité — couvre tout débordement ───────────
        int textX = px + 9;
        int curY  = py + 10;

        // Nom
        Component nameComp = Component.translatable(info.nameKey)
                .setStyle(Style.EMPTY.withColor(TextColor.fromRgb(rc)).withBold(true));
        g.drawString(this.font, nameComp, px + pw / 2 - this.font.width(nameComp) / 2, curY, rc);
        curY += 14;

        g.fill(px + 6, curY, px + pw - 6, curY + 1, (0x88 << 24) | (rc & 0x00FFFFFF));
        curY += 7;

        // Description
        for (FormattedCharSequence line :
                this.font.split(Component.translatable(info.descriptionKey), pw - 18)) {
            g.drawString(this.font, line, textX, curY, 0xCCCCCC);
            curY += 9;
        }
        curY += 6;

        g.fill(px + 6, curY, px + pw - 6, curY + 1, 0xFF404040);
        curY += 8;

        // Condition d'obtention
        g.drawString(this.font,
                Component.translatable("tooltip.skinObtention")
                        .setStyle(Style.EMPTY.withColor(TextColor.fromRgb(0x888888)).withItalic(true)),
                textX, curY, 0x888888);
        curY += 11;

        for (FormattedCharSequence line :
                this.font.split(getUnlockDescription(info), pw - 18)) {
            g.drawString(this.font, line, textX, curY, 0xDDDDDD);
            curY += 9;
        }

        // Label preview (au-dessus de la case, dessiné en dernier)
        Component previewLabel = Component.translatable("tooltip.skinPreview")
                .setStyle(Style.EMPTY.withColor(TextColor.fromRgb(0x666666)).withItalic(true));
        g.drawString(this.font, previewLabel,
                px + pw / 2 - this.font.width(previewLabel) / 2, previewTop + 2, 0x666666);
    }

    private void renderConfirmationOverlay(GuiGraphics g, int mouseX, int mouseY, float partial) {
        int i  = (this.width  - this.imageWidth)  / 2;
        int j  = (this.height - this.imageHeight) / 2;
        int px = i + DETAIL_PANEL_OFFSET_X;
        int pw = DETAIL_PANEL_WIDTH;
        int btnY       = j + imageHeight - 28;
        int overlayTop = btnY - 28;
        int overlayBot = j + imageHeight;

        // Fond de la zone de confirmation
        g.fill(px + 1, overlayTop, px + pw - 1, overlayBot, 0xE0101318);
        g.fill(px + 1, overlayTop, px + pw - 1, overlayTop + 1, 0xFF404040);

        // Texte de la question — centré dans la zone au-dessus des boutons
        Component question = Component.translatable("tooltip.confirmationBuyingSkin")
                .setStyle(Style.EMPTY.withColor(TextColor.fromRgb(0xCCCCCC)));
        List<FormattedCharSequence> lines = this.font.split(question, pw - 20);
        int totalTextH = lines.size() * (this.font.lineHeight + 1);
        int textY  = overlayTop + (btnY - overlayTop - totalTextH) / 2;
        int centerX = px + pw / 2;
        for (FormattedCharSequence line : lines) {
            g.drawString(this.font, line, centerX - this.font.width(line) / 2, textY, 0xCCCCCC, false);
            textY += this.font.lineHeight + 1;
        }

        // Boutons Oui / Non
        confirmYesButton.visible = true;
        confirmNoButton.visible  = true;
        confirmYesButton.render(g, mouseX, mouseY, partial);
        confirmNoButton.render(g, mouseX, mouseY, partial);
    }

    private void renderBuyButtonContent(GuiGraphics g, int price) {
        String priceStr = String.valueOf(price);
        int textW   = this.font.width(priceStr);
        int totalW  = 10 + 3 + textW;
        int iconX   = buyButton.getX() + (buyButton.getWidth() - totalW) / 2;
        int iconY   = buyButton.getY() + 5;
        g.blit(ICONS_LOCATION, iconX, iconY, 0, 143, 10, 10);
        g.drawString(this.font, priceStr, iconX + 13, iconY, 0xc8f6ff, false);
    }

    // =========================================================================
    // Hooks sous-classes
    // =========================================================================
    protected void addTooltipsToButtons() {}
    protected void updateLockStates()     {}

    protected void renderTexts(GuiGraphics graphics, int offsetX, int offsetY) {
        int centerX = offsetX + (this.imageWidth / 2);
        int centerY = offsetY + (this.imageHeight / 2);

        Component titleText = Component.translatable("tooltip.skinsTitle")
                .withStyle(Style.EMPTY.withBold(true).withUnderlined(true));

        graphics.blit(ICONS_LOCATION, centerX - 65, centerY - 104, 0, 143, 10, 10);

        int textPositionX = centerX - 65 - 3;
        if      (this.entity.getPrestigeLevel() >= 100) textPositionX -= this.font.width("100");
        else if (this.entity.getPrestigeLevel() >= 10)  textPositionX -= this.font.width("10");
        else                                            textPositionX -= this.font.width("0");

        graphics.drawString(this.font,
                String.valueOf(this.entity.getPrestigeLevel()),
                textPositionX, centerY - 104 + 1, 0xc8f6ff);

        graphics.drawString(this.font, titleText,
                centerX - (this.font.width(titleText) / 2) - 120, centerY - 90, 0x8b8b8b);
    }
}