package net.tiew.operationWild.screen.blocks;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Item;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.tiew.operationWild.OperationWild;
import net.tiew.operationWild.entity.custom.living.BoaEntity;
import net.tiew.operationWild.entity.custom.living.PeacockEntity;
import net.tiew.operationWild.entity.custom.living.TigerEntity;
import net.tiew.operationWild.entity.custom.living.TigerSharkEntity;
import net.tiew.operationWild.event.ClientEvents;

import java.util.Collections;
import java.util.List;

@OnlyIn(Dist.CLIENT)
public class SaddlerScreen extends AbstractContainerScreen<SaddlerMenu> {
    private static final ResourceLocation SADDLER_LOCATION = ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "textures/gui/saddler.png");
    private float xMouse;
    private float yMouse;
    private ButtonListWidget buttonList;

    public static boolean canShowBoaSaddle = false;
    public static boolean canShowTigerSaddle = false;
    public static boolean canShowPeacockSaddle = false;
    public static boolean canShowTigerSharkSaddle = false;


    public Button boaSaddleButton;
    public Button tigerSaddleButton;
    public Button peacockSaddleButton;
    public Button tigerSharkSaddleButton;


    public SaddlerScreen(SaddlerMenu menu, Inventory inventory, Component component) {
        super(menu, inventory, component);
    }

    @Override
    protected void init() {
        super.init();
        int i = (this.width - this.imageWidth) / 2;
        int j = (this.height - this.imageHeight) / 2;

        this.buttonList = new ButtonListWidget(this.minecraft, i + 59, 105, 57, j + 14, j + 160);


        boaSaddleButton = createTransparentButton("item.ow.boa_saddle", 0x8b8b8b, 0, 0, 105, 19, this::boaButtonClick);
        tigerSaddleButton = createTransparentButton("item.ow.tiger_saddle", 0x8b8b8b, 0, 0, 105, 19, this::tigerButtonClick);
        peacockSaddleButton = createTransparentButton("item.ow.peacock_saddle", 0x8b8b8b, 0, 0, 105, 19, this::peacockButtonClick);
        tigerSharkSaddleButton = createTransparentButton("item.ow.tiger_shark_saddle", 0x8b8b8b, 0, 0, 105, 19, this::tigerSharkButtonClick);


        if (ClientEvents.tamingExperience >= BoaEntity.TAMING_EXPERIENCE) this.buttonList.addButtonEntry(new ButtonListWidget.ButtonEntry(boaSaddleButton));
        if (ClientEvents.tamingExperience >= TigerEntity.TAMING_EXPERIENCE) this.buttonList.addButtonEntry(new ButtonListWidget.ButtonEntry(tigerSaddleButton));
        if (ClientEvents.tamingExperience >= PeacockEntity.TAMING_EXPERIENCE) this.buttonList.addButtonEntry(new ButtonListWidget.ButtonEntry(peacockSaddleButton));
        if (ClientEvents.tamingExperience >= TigerSharkEntity.TAMING_EXPERIENCE) this.buttonList.addButtonEntry(new ButtonListWidget.ButtonEntry(tigerSharkSaddleButton));


        this.addRenderableWidget(this.buttonList);
    }

    private void boaButtonClick() {
        canShowTigerSaddle = false;
        canShowTigerSharkSaddle= false;
        canShowPeacockSaddle = false;
        canShowBoaSaddle = !canShowBoaSaddle;
    }

    private void peacockButtonClick() {
        canShowTigerSaddle = false;
        canShowBoaSaddle = false;
        canShowTigerSharkSaddle= false;
        canShowPeacockSaddle = !canShowPeacockSaddle;
    }

    private void tigerButtonClick() {
        canShowBoaSaddle = false;
        canShowPeacockSaddle = false;
        canShowTigerSharkSaddle= false;
        canShowTigerSaddle = !canShowTigerSaddle;
    }

    private void tigerSharkButtonClick() {
        canShowBoaSaddle = false;
        canShowPeacockSaddle = false;
        canShowTigerSaddle= false;
        canShowTigerSharkSaddle = !canShowTigerSharkSaddle;
    }

    public Button createTransparentButton(String textOnButton, int color, int positionX, int positionY, int width, int height, Runnable onClick) {
        return new TransparentButton(
                positionX, positionY, width, height,
                Component.translatable(textOnButton).setStyle(Style.EMPTY.withColor(TextColor.fromRgb(color))),
                button -> onClick.run());
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        int i = (this.width - this.imageWidth) / 2;
        int j = (this.height - this.imageHeight) / 2;

        guiGraphics.blit(SADDLER_LOCATION, i, j, 0, 0, 176, 166);

        if (!menuWorking()) guiGraphics.blit(SADDLER_LOCATION, i + 14, j + 14, 192, 0, 36, 58);

        if (canShowBoaSaddle) guiGraphics.blit(SADDLER_LOCATION, i + 24, j + 53, 240, entitySaddleCoords(), 16, 16);
        if (canShowTigerSaddle) guiGraphics.blit(SADDLER_LOCATION, i + 24, j + 53, 240, entitySaddleCoords(), 16, 16);
        if (canShowPeacockSaddle) guiGraphics.blit(SADDLER_LOCATION, i + 24, j + 53, 240, entitySaddleCoords(), 16, 16);
        if (canShowTigerSharkSaddle) guiGraphics.blit(SADDLER_LOCATION, i + 24, j + 53, 240, entitySaddleCoords(), 16, 16);
    }

    public static boolean menuWorking() {
        return canShowBoaSaddle || canShowTigerSaddle || canShowPeacockSaddle || canShowTigerSharkSaddle;
    }

    private int entitySaddleCoords() {
        if (canShowBoaSaddle) return 224;
        if (canShowTigerSaddle) return 240;
        if (canShowPeacockSaddle) return 208;
        if (canShowTigerSharkSaddle) return 192;

        return 0;
    }

    private void renderTooltipBackground(GuiGraphics guiGraphics, int x, int y, int width, int height) {
        int reduceWidth = 55;
        int reduceHeight = 7;
        int padding = 3;
        int left = x - padding;
        int top = y - padding;

        int adjustedWidth = Math.max(0, width - reduceWidth);
        int adjustedHeight = Math.max(0, height - reduceHeight);

        int right = x + adjustedWidth + padding;
        int bottom = y + adjustedHeight + padding;

        guiGraphics.fillGradient(left, top, right, bottom, 0xF0100010, 0xF0100010);

        guiGraphics.fillGradient(left, top, right, top + 1, 0x505000FF, 0x5028007F);
        guiGraphics.fillGradient(left, bottom - 1, right, bottom, 0x5028007F, 0x505000FF);
        guiGraphics.fillGradient(left, top, left + 1, bottom, 0x505000FF, 0x5028007F);
        guiGraphics.fillGradient(right - 1, top, right, bottom, 0x5028007F, 0x505000FF);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.xMouse = mouseX;
        this.yMouse = mouseY;


        boaSaddleButton.setMessage(Component.translatable("item.ow.boa_saddle").setStyle(Style.EMPTY.withColor(TextColor.fromRgb(canShowBoaSaddle ? 0xFFFFFF : 0x8b8b8b))));
        tigerSaddleButton.setMessage(Component.translatable("item.ow.tiger_saddle").setStyle(Style.EMPTY.withColor(TextColor.fromRgb(canShowTigerSaddle ? 0xFFFFFF : 0x8b8b8b))));
        peacockSaddleButton.setMessage(Component.translatable("item.ow.peacock_saddle").setStyle(Style.EMPTY.withColor(TextColor.fromRgb(canShowPeacockSaddle ? 0xFFFFFF : 0x8b8b8b))));
        tigerSharkSaddleButton.setMessage(Component.translatable("item.ow.tiger_shark_saddle").setStyle(Style.EMPTY.withColor(TextColor.fromRgb(canShowTigerSharkSaddle ? 0xFFFFFF : 0x8b8b8b))));


        super.render(graphics, mouseX, mouseY, partialTick);

        PoseStack poseStack = graphics.pose();

        poseStack.pushPose();
        poseStack.translate(0, 0, 500);

        if (menuWorking()) {
            for (ButtonListWidget.ButtonEntry entry : this.buttonList.children()) {
                Button button = entry.button;
                if (button.isMouseOver(mouseX, mouseY)) {
                    String key = "";
                    if (button.getMessage().getContents() instanceof TranslatableContents contents) key = contents.getKey();

                    if (key.contains("boa")) createVisualCraft(mouseX, mouseY, graphics, SaddlerMenu.boaSaddleCraftItems, SaddlerMenu.boaSaddleCraftAmount);
                    else if (key.contains("tiger")) createVisualCraft(mouseX, mouseY, graphics, SaddlerMenu.tigerSaddleCraftItems, SaddlerMenu.tigerSaddleCraftAmount);
                    else if (key.contains("peacock")) createVisualCraft(mouseX, mouseY, graphics, SaddlerMenu.peacockSaddleCraftItems, SaddlerMenu.peacockSaddleCraftAmount);
                    else if (key.contains("tiger_shark")) createVisualCraft(mouseX, mouseY, graphics, SaddlerMenu.tigerSharkSaddleCraftItems, SaddlerMenu.tigerSharkSaddleCraftAmount);

                }
            }
        }

        poseStack.popPose();



        this.renderTooltip(graphics, mouseX, mouseY);
    }

    public void createVisualCraft(int mouseX, int mouseY, GuiGraphics graphics, Item[] items, int[] amounts) {
        int tooltipX = mouseX + 10;
        int tooltipY = mouseY + 10;
        int tooltipWidth = 100;
        int tooltipHeight = 20 * items.length;

        renderTooltipBackground(graphics, tooltipX, tooltipY, tooltipWidth, tooltipHeight);
        for (int i = 0; i < items.length; i++) {
            graphics.renderItem(items[i].getDefaultInstance(), tooltipX + 2, tooltipY + 2 + (i * 18));
            graphics.drawString(Minecraft.getInstance().font, "* " + amounts[i], tooltipX + 20, tooltipY + 4 + (i * 18), 0xFFFFFF, true);
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static class TransparentButton extends Button {
        private final ResourceLocation texture;

        public TransparentButton(int x, int y, int width, int height, Component message, OnPress onPress) {
            super(x, y, width, height, message, onPress, DEFAULT_NARRATION);

            String key = "";
            if (message.getContents() instanceof TranslatableContents contents) {
                key = contents.getKey();
            }

            if (key.contains("boa")) {
                this.texture = ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "textures/item/boa_saddle.png");
            } else if (key.contains("tiger")) {
                this.texture = ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "textures/item/tiger_saddle.png");
            } else if (key.contains("peacock")) {
                this.texture = ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "textures/item/peacock_saddle.png");
            } else if (key.contains("tiger_shark")) {
                this.texture = ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "textures/item/tiger_shark_saddle.png");
            } else {
                this.texture = ResourceLocation.fromNamespaceAndPath("minecraft", "textures/item/saddle.png");
            }
        }

        @Override
        public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
            RenderSystem.setShaderTexture(0, texture);
            guiGraphics.blit(texture, this.getX() + 2, this.getY() + (this.getHeight() - 16) / 2, 0, 0, 16, 16, 16, 16);

            if (this.getMessage() != null) {
                int textColor = this.isHovered ? 0xFFFF55 : 0xFFFFFF;
                int textX = this.getX() + 16 + 6;
                int textY = this.getY() + (this.getHeight() - 8) / 2;

                guiGraphics.drawString(Minecraft.getInstance().font, this.getMessage(), textX, textY, textColor);
            }
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static class ButtonListWidget extends ContainerObjectSelectionList<ButtonListWidget.ButtonEntry> {

        public ButtonListWidget(Minecraft minecraft, int x, int width, int height, int y0, int y1) {
            super(minecraft, width, height, y0, y1 - 185);
            this.setX(x);
        }

        @Override
        protected void renderListBackground(GuiGraphics p_333412_) {
        }

        public void addButtonEntry(ButtonEntry entry) {
            this.addEntry(entry);
        }

        public void clearEntries() {
            this.children().clear();
        }

        @Override
        public int getRowWidth() {
            return this.width - 12;
        }

        @Override
        protected int getScrollbarPosition() {
            return this.width + 212;
        }

        public static class ButtonEntry extends Entry<ButtonEntry> {
            private final Button button;

            public ButtonEntry(Button button) {
                this.button = button;
            }

            @Override
            public void render(GuiGraphics guiGraphics, int index, int top, int left, int width, int height, int mouseX, int mouseY, boolean hovered, float partialTick) {
                this.button.setX(left - 8);
                this.button.setY(top - 4);
                this.button.render(guiGraphics, mouseX, mouseY, partialTick);
            }

            @Override
            public boolean mouseClicked(double mouseX, double mouseY, int button) {
                return this.button.mouseClicked(mouseX, mouseY, button);
            }

            @Override
            public List<? extends GuiEventListener> children() {
                return Collections.singletonList(this.button);
            }

            @Override
            public List<? extends NarratableEntry> narratables() {
                return Collections.singletonList(this.button);
            }
        }
    }
}