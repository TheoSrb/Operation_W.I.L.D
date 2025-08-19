package net.tiew.operationWild.screen.entity;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.tiew.operationWild.entity.OWEntity;
import net.tiew.operationWild.screen.entity.skins.BoaSkinsScreen;
import net.tiew.operationWild.screen.entity.skins.ElephantSkinsScreen;
import net.tiew.operationWild.screen.entity.skins.TigerSkinsScreen;

public class OWSkinsConfirmationScreen extends Screen {
    protected final OWEntity entity;
    private final int skinIndex;
    private final int skinPrice;
    private float xMouse;
    private float yMouse;
    protected int imageWidth = 200;
    protected int imageHeight = 100;
    private Button yesButton;
    private Button noButton;

    public OWSkinsConfirmationScreen(int skinIndex, int skinPrice) {
        super(Component.literal("OWSkinsConfirmationScreen"));
        if (Minecraft.getInstance().player.getRootVehicle() instanceof OWEntity entity) this.entity = entity;
        else this.entity = null;
        this.skinIndex = skinIndex;
        this.skinPrice = skinPrice;
    }

    public Button createButton(String textOnButton, int color, int positionX, int positionY, int width, int height, Runnable onClick) {
        int i = (this.width - this.imageWidth) / 2;
        int j = (this.height - this.imageHeight) / 2;

        return Button.builder(Component.literal(textOnButton)
                                .setStyle(Style.EMPTY.withColor(TextColor.fromRgb(color))),
                        button -> onClick.run())
                .bounds(i + positionX, j + positionY, width, height)
                .build();
    }

    @Override
    protected void init() {
        super.init();
        int i = (this.width - this.imageWidth) / 2;
        int j = (this.height - this.imageHeight) / 2;

        yesButton = Button.builder(Component.translatable("tooltip.yesButton")
                                .setStyle(Style.EMPTY.withColor(TextColor.fromRgb(0x7ddd73)).withBold(true)),
                        button -> {
                            OWSkinsInterface.buySkin(skinIndex, skinPrice);
                            onClose();
                        })
                .bounds(i + 30, j + 70, 60, 20)
                .build();
        this.addRenderableWidget(yesButton);

        noButton = Button.builder(Component.translatable("tooltip.noButton")
                                .setStyle(Style.EMPTY.withColor(TextColor.fromRgb(0x9a1111)).withBold(true)),
                        button -> onClose())
                .bounds(i + 110, j + 70, 60, 20)
                .build();
        this.addRenderableWidget(noButton);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        int i = (this.width - this.imageWidth) / 2;
        int j = (this.height - this.imageHeight) / 2;
        this.xMouse = mouseX;
        this.yMouse = mouseY;

        super.render(graphics, mouseX, mouseY, partialTick);
        renderTexts(graphics, i, j);
    }

    private void renderTexts(GuiGraphics graphics, int offsetX, int offsetY) {
        int centerX = offsetX + (this.imageWidth / 2);

        Component confirmation = Component.translatable("tooltip.confirmationBuyingSkin");
        Component confirmation2 = Component.translatable("tooltip.confirmationBuyingSkin2", this.skinPrice);

        graphics.drawCenteredString(this.font, confirmation, centerX, offsetY + 20, 0xFFFFFF);
        graphics.drawCenteredString(this.font, confirmation2, centerX, offsetY + 40, 0xFF0000);
    }

    @Override
    public void onClose() {
        switch(entity.getClass().getSimpleName()) {
            case "TigerEntity" -> Minecraft.getInstance().setScreen(new TigerSkinsScreen());
            case "BoaEntity" -> Minecraft.getInstance().setScreen(new BoaSkinsScreen());
            case "ElephantEntity" -> Minecraft.getInstance().setScreen(new ElephantSkinsScreen());
        }
    }
}