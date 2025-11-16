package net.tiew.operationWild.screen.entity;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.tiew.operationWild.OperationWild;
import net.tiew.operationWild.entity.OWEntity;
import net.tiew.operationWild.gui.OWEntityHud;
import net.tiew.operationWild.networking.OWNetworkHandler;
import net.tiew.operationWild.networking.packets.to_server.OWNameEntityPacket;
import net.tiew.operationWild.screen.entity.skins.*;

import java.util.List;

import static net.tiew.operationWild.gui.OWEntityHud.getEntityIconData;

public class OWChooseNameScreen extends Screen {

    public static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "textures/gui/choose_name.png");

    protected final OWEntity entity;
    private float xMouse;
    private float yMouse;
    protected int imageWidth = 140;
    protected int imageHeight = 140;

    private EditBox nameInput;
    private String enteredName = "";

    private Button sendButton;
    private Button closeButton;

    public OWChooseNameScreen(int entityID) {
        super(Component.literal("OWChooseNameScreen"));
        if (Minecraft.getInstance().level != null && Minecraft.getInstance().level.getEntity(entityID) instanceof OWEntity owEntity) {
            this.entity = owEntity;
        } else {
            this.entity = null;
        }
    }

    @Override
    protected void init() {
        super.init();
        int buttonWidth = 20;
        int buttonHeight = 20;

        this.nameInput = new EditBox(this.font, (this.width - 80) / 2, (this.height - 20) / 2 + 25, 80, 20, Component.literal("Name"));
        this.nameInput.setMaxLength(15);

        sendButton = createButton("✔", 0x00FF00, (this.imageWidth / 2) - (buttonWidth / 2) - 35, this.imageHeight - (buttonHeight / 2) - 20, buttonWidth, buttonHeight, this::sendButton);
        closeButton = createButton("✘", 0xFF0000, (this.imageWidth / 2) - (buttonWidth / 2) + 35, this.imageHeight - (buttonHeight / 2) - 20, buttonWidth, buttonHeight, this::onClose);

        this.addRenderableWidget(this.nameInput);
        this.addRenderableWidget(sendButton);
        this.addRenderableWidget(closeButton);
    }

    private void sendButton() {
        OWNetworkHandler.sendToServer(new OWNameEntityPacket(entity.getId(), this.enteredName));
        this.onClose();
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics, mouseX, mouseY, partialTick);

        int i = (this.width - this.imageWidth) / 2;
        int j = (this.height - this.imageHeight) / 2;
        this.xMouse = mouseX;
        this.yMouse = mouseY;

        graphics.blit(TEXTURE, i, j, 0, 0, imageWidth, imageHeight);

        Component c1 = Component.translatable("ow.advancements." + entity.getTamingAdvancement().getPath() + ".description");

        int maxWidth = this.imageWidth - 10;
        int startY = j + 10;
        int currentY = startY;

        List<FormattedCharSequence> c1Lines = this.font.split(c1, maxWidth);
        for (FormattedCharSequence line : c1Lines) {
            int lineWidth = this.font.width(line);
            graphics.drawString(this.font, line, i + (this.imageWidth / 2) - (lineWidth / 2), currentY, 0xFFFFFF);
            currentY += 10;
        }

        OWEntityHud.EntityIconData iconData = getEntityIconData(entity);
        if (iconData != null) {
            float scale = 2f;
            graphics.pose().pushPose();
            graphics.pose().scale(scale, scale, scale);

            float scaledWidth = iconData.width * scale;
            float scaledHeight = iconData.height * scale;

            int scaledX = (int) ((i + (this.imageWidth / 2f) - (scaledWidth / 2f)) / scale);
            int scaledY = (int) ((j + 40) / scale);

            graphics.blit(OWEntityHud.HUD, scaledX, scaledY, iconData.textureX, iconData.textureY, iconData.width, iconData.height);
            graphics.pose().popPose();
        }

        super.render(graphics, mouseX, mouseY, partialTick);

        this.enteredName = this.nameInput.getValue();
    }

    @Override
    public void renderBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {

    }

    @Override
    public void onClose() {
        super.onClose();
        if (entity != null && entity.getNickname() == null) {
            entity.setNickname(String.valueOf(Component.translatable("entity.ow." + this.getClass().getSimpleName().toLowerCase().split("entity")[0])));
        }
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
}