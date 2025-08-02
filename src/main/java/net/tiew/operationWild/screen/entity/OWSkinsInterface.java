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
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.tiew.operationWild.OperationWild;
import net.tiew.operationWild.entity.OWEntity;
import net.tiew.operationWild.networking.OWNetworkHandler;
import net.tiew.operationWild.networking.packets.to_server.OWVariantsSkinsPacket;
import net.tiew.operationWild.networking.packets.to_server.OpenOWInventoryPacket;
import net.tiew.operationWild.networking.packets.to_server.SkinBuyingPacket;
import net.tiew.operationWild.screen.OWScreenUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@OnlyIn(Dist.CLIENT)
public class OWSkinsInterface extends Screen {
    private static final ResourceLocation OW_SKINS_INTERFACE_LOCATION = ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "textures/gui/ow_skins_interface_gui.png");
    private static final ResourceLocation ICONS_LOCATION = ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "textures/gui/mob_types.png");
    protected final OWEntity entity;
    private float xMouse;
    private float yMouse;
    public int imageWidth = 140;
    protected int imageHeight = 218;
    protected int entityScale = 17;

    protected Button backButton;
    protected OWScreenUtils.ButtonListWidget buttonList;

    public final List<Button> LEGENDARY_SKIN = new ArrayList<>();
    public final List<Button> EPIC_SKIN = new ArrayList<>();
    public final List<Button> RARE_SKIN = new ArrayList<>();
    public final List<Button> COMMON_SKIN = new ArrayList<>();

    protected final Map<Integer, Boolean> lockedSkins = new HashMap<>();
    protected final Map<Integer, Integer> skinPrices = new HashMap<>();

    public OWSkinsInterface() {
        super(Component.literal("OWSkinsInterface"));
        if (Minecraft.getInstance().player.getRootVehicle() instanceof OWEntity entity) this.entity = entity;
        else this.entity = null;

        initLockedSkins();
        initSkinPrices();
    }

    protected void initLockedSkins() {
    }

    protected void initSkinPrices() {
    }

    public boolean isLocked(int skinIndex) {
        return lockedSkins.getOrDefault(skinIndex, false);
    }

    public int getSkinPrice(int skinIndex) {
        return skinPrices.getOrDefault(skinIndex, 0);
    }

    public Button createButton(String textOnButton, int color, int positionX, int positionY, int width, int height, Runnable onClick) {
        int i = (this.width - this.imageWidth) / 2;
        int j = (this.height - this.imageHeight) / 2;

        return Button.builder(Component.literal(textOnButton)
                                .setStyle(Style.EMPTY.withColor(TextColor.fromRgb(color))),
                        button -> onClick.run())
                .bounds(i + positionX, j - positionY, width, height)
                .build();
    }

    @Override
    protected void init() {
        super.init();
        int i = (this.width - this.imageWidth) / 2;
        int j = (this.height - this.imageHeight) / 2;

        int listX = i - 120 + 10;
        int listY = j + 50;
        int listWidth = this.imageWidth - 20;
        int listHeight = 150;

        backButton = createButton("", 0xFFFFFF, -135, 0, 16, 16, () -> OWNetworkHandler.sendToServer(new OpenOWInventoryPacket()));
        this.addRenderableWidget(backButton);

        this.buttonList = new OWScreenUtils.ButtonListWidget(this.minecraft, listWidth, listHeight, listY, 30, this);
        this.buttonList.setX(listX);

        createAndAddButtons();

        this.addWidget(this.buttonList);

        initEntityScale();
    }

    protected void initEntityScale() {
    }

    protected void createAndAddButtons() {
        LEGENDARY_SKIN.clear();
        EPIC_SKIN.clear();
        RARE_SKIN.clear();
        COMMON_SKIN.clear();

        updateButtonColors();
        addButtonsToList();
    }

    protected Button createSkinButton(Component text, int skinIndex, List<Button> list) {
        Component buttonText = text.copy().setStyle(Style.EMPTY.withColor(TextColor.fromRgb(0xFFFFFF)).withBold(true));

        Button button = Button.builder(buttonText,
                        btn -> onSkinButtonPressed(skinIndex))
                .width((int)(this.imageWidth * 0.8))
                .build();

        list.add(button);
        return button;
    }

    protected void addButtonsToList() {
        this.buttonList.clearEntries();

        List<Button> orderedButtons = new ArrayList<>();

        orderedButtons.addAll(COMMON_SKIN);
        orderedButtons.addAll(RARE_SKIN);
        orderedButtons.addAll(EPIC_SKIN);

        List<Button> legendaryButtons = new ArrayList<>(LEGENDARY_SKIN);

        Button firstSkinButton = null;
        for (Button button : legendaryButtons) {
            if (getSkinIndexForButton(button) == 1) {
                firstSkinButton = button;
                break;
            }
        }

        if (firstSkinButton != null) {
            legendaryButtons.remove(firstSkinButton);
        }

        orderedButtons.addAll(legendaryButtons);

        if (firstSkinButton != null) {
            orderedButtons.add(firstSkinButton);
        }

        for (Button button : orderedButtons) {
            int skinIndex = getSkinIndexForButton(button);
            if (skinIndex != -1) {
                this.buttonList.addButtonEntry(new OWScreenUtils.ButtonListWidget.ButtonEntry(button, skinIndex, this));
            }
        }
    }

    protected void updateButtonColors() {
        updateButtonsInList(LEGENDARY_SKIN, 0xfdd85f);
        updateButtonsInList(EPIC_SKIN, 0x682b90);
        updateButtonsInList(RARE_SKIN, 0xf19f60);
        updateButtonsInList(COMMON_SKIN, 0xc4def2);
    }

    private void updateButtonsInList(List<Button> buttonList, int colorCode) {
        for (Button button : buttonList) {
            int skinIndex = getSkinIndexForButton(button);
            button.setMessage(button.getMessage().copy().setStyle(
                    Style.EMPTY.withColor(TextColor.fromRgb(isLocked(skinIndex) ? 0xFFFFFF : colorCode)).withBold(true)
            ));
        }
    }

    protected int getSkinIndexForButton(Button button) {
        return -1;
    }

    protected void onSkinButtonPressed(int skinIndex) {
        if (isLocked(skinIndex)) {
            int price = getSkinPrice(skinIndex);
            if (price > 0 && entity.getPrestigeLevel() >= price) {
                Minecraft.getInstance().setScreen(new OWSkinsConfirmationScreen(skinIndex, price));
            } else {
                showLockedMessage(price);
            }
        } else {
            OWNetworkHandler.sendToServer(new OWVariantsSkinsPacket(skinIndex));
        }
    }

    public static void buySkin(int skinIndex, int price) {
        OWNetworkHandler.sendToServer(new SkinBuyingPacket(price, skinIndex));
        Minecraft.getInstance().player.displayClientMessage(
                Component.translatable("skin.unlocked.message").withStyle(ChatFormatting.GREEN),
                false
        );
    }

    private void showLockedMessage(int price) {
        Minecraft.getInstance().player.displayClientMessage(
                Component.translatable(price > 0
                                ? "skin.locked.message.prestige"
                                : "skin.locked.message")
                        .withStyle(ChatFormatting.RED),
                false
        );
    }

    public void setLockState(int skinIndex, boolean locked) {
        lockedSkins.put(skinIndex, locked);
        updateButtonColors();
        addButtonsToList();
    }

    @Override
    public boolean isPauseScreen() { return false; }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        this.xMouse = mouseX;
        this.yMouse = mouseY;
        int i = (this.width - this.imageWidth) / 2;
        int j = (this.height - this.imageHeight) / 2;

        boolean backButtonVisible = backButton.visible;
        backButton.visible = false;

        super.render(guiGraphics, mouseX, mouseY, partialTicks);

        if (this.entity != null) {
            InventoryScreen.renderEntityInInventoryFollowsMouse(guiGraphics, i - 104, j - 54, i + 400, j + 300, entityScale, 0.25F, this.xMouse, this.yMouse, this.entity);
        }

        guiGraphics.blit(OW_SKINS_INTERFACE_LOCATION, i - 120, j, 0, 0, this.imageWidth, this.imageHeight);

        backButton.visible = backButtonVisible;
        backButton.render(guiGraphics, mouseX, mouseY, partialTicks);

        this.buttonList.render(guiGraphics, mouseX, mouseY, partialTicks);

        guiGraphics.blit(ICONS_LOCATION, i - 134, j + 1, 0, 130, 13, 13);

        addTooltipsToButtons();
        renderTexts(guiGraphics, i, j);

        updateLockStates();
    }

    protected void addTooltipsToButtons() {
    }

    protected void updateLockStates() {
    }

    protected void renderTexts(GuiGraphics graphics, int offsetX, int offsetY) {
        int centerX = offsetX + (this.imageWidth / 2);
        int centerY = offsetY + (this.imageHeight / 2);
        Component titleText = Component.translatable("tooltip.skinsTitle").withStyle(Style.EMPTY.withBold(true).withUnderlined(true));

        graphics.blit(ICONS_LOCATION, centerX - 65, centerY - 104, 0, 143, 10, 10);

        int textPositionX = centerX - 65 - 3;

        if (this.entity.getPrestigeLevel() >= 100) {
            textPositionX -= this.font.width("100");
        } else if (this.entity.getPrestigeLevel() >= 10) {
            textPositionX -= this.font.width("10");
        } else {
            textPositionX -= this.font.width("0");
        }

        graphics.drawString(this.font, String.valueOf(this.entity.getPrestigeLevel()),
                textPositionX,
                centerY - 104 + 1,
                0xc8f6ff);

        graphics.drawString(this.font, titleText, centerX - (this.font.width(titleText) / 2) - 120, centerY - 90, 0x8b8b8b);
    }
}
