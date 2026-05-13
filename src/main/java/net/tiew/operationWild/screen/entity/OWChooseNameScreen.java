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
import net.tiew.operationWild.screen.entity.OWOptionsScreen;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;

import static net.tiew.operationWild.gui.OWEntityHud.getEntityIconData;

public class OWChooseNameScreen extends Screen {

    public static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "textures/gui/choose_name.png");

    private static final int MAX_NAME_LENGTH = 15;
    private boolean nameSubmitted = false;

    protected final OWEntity entity;
    private float xMouse;
    private float yMouse;
    protected int imageWidth = 140;
    protected int imageHeight = 140;

    private EditBox nameInput;
    private String enteredName = "";
    private Button sendButton;

    private String errorMessage = null;
    private long errorDisplayTime = 0;
    private static final int ERROR_DURATION_MS = 5000;

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
        int buttonWidth = 60;
        int buttonHeight = 20;

        this.nameInput = new EditBox(this.font, (this.width - 80) / 2, (this.height - 20) / 2 + 25, 80, 20, Component.literal("Name"));
        this.nameInput.setMaxLength(MAX_NAME_LENGTH);

        sendButton = createButton("Appliquer", 0xFFFFFF,
                (this.imageWidth / 2) - (buttonWidth / 2),
                this.imageHeight - (buttonHeight / 2) - 20,
                buttonWidth, buttonHeight, this::onSendClicked);

        this.addRenderableWidget(this.nameInput);
        this.addRenderableWidget(sendButton);
    }

    private void onSendClicked() {
        String name = this.enteredName == null ? "" : this.enteredName.trim();

        if (name.isEmpty()) {
            showError("Veuillez renseigner un nom.");
            return;
        }

        if (name.length() > MAX_NAME_LENGTH) {
            showError("Le nom ne peut pas dépasser " + MAX_NAME_LENGTH + " caractères.");
            return;
        }

        nameSubmitted = true;
        OWNetworkHandler.sendToServer(new OWNameEntityPacket(entity.getId(), name));
        this.onClose();
    }

    private void applyDefaultName() {
        if (entity == null) return;
        if (nameSubmitted) return;
        String current = entity.getNickname();
        if (current == null || current.isEmpty()) {
            OWNetworkHandler.sendToServer(new OWNameEntityPacket(entity.getId(), "Sans nom"));
        }
    }

    private void showError(String message) {
        this.errorMessage = message;
        this.errorDisplayTime = System.currentTimeMillis();
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == 256) { // Échap
            applyDefaultName();
            this.onClose();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
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
        int currentY = j + 10;

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
            int scaledX = (int) ((i + (this.imageWidth / 2f) - (iconData.width * scale / 2f)) / scale);
            int scaledY = (int) ((j + 40) / scale);
            graphics.blit(OWEntityHud.HUD, scaledX, scaledY, iconData.textureX, iconData.textureY, iconData.width, iconData.height);
            graphics.pose().popPose();
        }

        super.render(graphics, mouseX, mouseY, partialTick);

        int currentLength = this.enteredName == null ? 0 : this.enteredName.length();
        String counter = currentLength + " / " + MAX_NAME_LENGTH;
        int counterColor = currentLength >= MAX_NAME_LENGTH ? 0xFF5555 : 0xAAAAAA;
        int counterWidth = this.font.width(counter);
        graphics.drawString(this.font, counter,
                i + (this.imageWidth / 2) - (counterWidth / 2),
                (this.height - 20) / 2 + 15,
                counterColor);

        if (errorMessage != null) {
            long elapsed = System.currentTimeMillis() - errorDisplayTime;
            if (elapsed < ERROR_DURATION_MS) {
                int alpha = 255;
                if (elapsed > ERROR_DURATION_MS - 1000) {
                    alpha = (int) (255 * (1.0 - (elapsed - (ERROR_DURATION_MS - 1000)) / 1000.0));
                }
                int color = (alpha << 24) | 0xFF0000;
                int textWidth = this.font.width(errorMessage);
                graphics.drawString(this.font, errorMessage,
                        i + (this.imageWidth / 2) - (textWidth / 2),
                        j + this.imageHeight + 15, color);
            } else {
                errorMessage = null;
            }
        }

        this.enteredName = this.nameInput.getValue();
    }

    @Override
    public void renderBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {}

    @Override
    public void onClose() {
        super.onClose();
        applyDefaultName();
    }

    public Button createButton(String textOnButton, int color, int positionX, int positionY, int width, int height, Runnable onClick) {
        int i = (this.width - this.imageWidth) / 2;
        int j = (this.height - this.imageHeight) / 2;
        return Button.builder(
                Component.literal(textOnButton).setStyle(Style.EMPTY.withColor(TextColor.fromRgb(color))),
                button -> onClick.run()
        ).bounds(i + positionX, j + positionY, width, height).build();
    }
}