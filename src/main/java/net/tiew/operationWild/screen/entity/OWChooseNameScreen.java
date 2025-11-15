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
import net.tiew.operationWild.OperationWild;
import net.tiew.operationWild.entity.OWEntity;
import net.tiew.operationWild.screen.entity.skins.*;

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

        this.nameInput = new EditBox(this.font, (this.width - 80) / 2, (this.height - 20) / 2, 80, 20, Component.literal("Name"));

        sendButton = createButton("Valid", 0x00FF00, (this.width - 80) / 2 - 50, (this.height - 20) / 2 + 50, 20, 20, this::sendButton);
        closeButton = createButton("Close", 0xFF0000, (this.width - 80) / 2 + 50, (this.height - 20) / 2 + 50, 20, 20, this::onClose);

        this.addRenderableWidget(this.nameInput);
        this.addRenderableWidget(sendButton);
        this.addRenderableWidget(closeButton);
    }

    private void sendButton() {
        entity.setNickname(this.enteredName);
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