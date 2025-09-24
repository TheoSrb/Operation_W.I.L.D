package net.tiew.operationWild.screen;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.tiew.operationWild.OperationWild;
import net.tiew.operationWild.screen.entity.OWSkinsInterface;

import java.util.Collections;
import java.util.List;

public class OWScreenUtils {
    private static final ResourceLocation ICONS_LOCATION = ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "textures/gui/mob_types.png");

    @OnlyIn(Dist.CLIENT)
    public static class ButtonListWidget extends ContainerObjectSelectionList<ButtonListWidget.ButtonEntry> {
        private final OWSkinsInterface parentScreen;

        public ButtonListWidget(Minecraft minecraft, int width, int height, int y0, int y1, OWSkinsInterface parentScreen) {
            super(minecraft, width, height, y0, y1);
            this.parentScreen = parentScreen;
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
            return this.width + 61;
        }

        public static class ButtonEntry extends Entry<ButtonEntry> {
            private final Button button;
            private final int skinIndex;
            private final OWSkinsInterface parentScreen;

            public ButtonEntry(Button button, int skinIndex, OWSkinsInterface parentScreen) {
                this.button = button;
                this.skinIndex = skinIndex;
                this.parentScreen = parentScreen;
            }

            @Override
            public void render(GuiGraphics guiGraphics, int index, int top, int left, int width, int height, int mouseX, int mouseY, boolean hovered, float partialTick) {
                this.button.setX(left - 4);
                this.button.setY(top);

                Component originalText = this.button.getMessage();

                if (parentScreen.isLocked(skinIndex)) {
                    this.button.setMessage(Component.empty());
                }

                this.button.render(guiGraphics, mouseX, mouseY, partialTick);
                this.button.setMessage(originalText);

                if (parentScreen.isLocked(skinIndex)) {
                    int buttonWidth = (int)(parentScreen.imageWidth * 0.8);
                    int buttonHeight = 20;
                    int imageScale = 14;

                    int iconX = left - 4 + (buttonWidth / 2) - (imageScale / 2);
                    int iconY = top + (buttonHeight / 2) - (imageScale / 2);

                    int lockedRarityPositionY = parentScreen.LEGENDARY_SKIN.contains(button) ? 74 :
                            parentScreen.EPIC_SKIN.contains(button) ? 88 :
                                parentScreen.HALLOWEEN_SKIN.contains(button) ? 88 :
                                    parentScreen.RARE_SKIN.contains(button) ? 102 :
                                            parentScreen.COMMON_SKIN.contains(button) ? 116 : 0;

                    guiGraphics.blit(ICONS_LOCATION, iconX, iconY, 0, lockedRarityPositionY, imageScale, imageScale);

                    if (parentScreen.getSkinPrice(skinIndex) > 0) {
                        guiGraphics.blit(ICONS_LOCATION, iconX + 17, iconY + 2, 0, 143, 10, 10);
                        String priceText = String.valueOf(parentScreen.getSkinPrice(skinIndex));
                        int priceX = iconX + 30;
                        int priceY = iconY + 4;

                        guiGraphics.drawString(Minecraft.getInstance().font, priceText, priceX, priceY, 0xc8f6ff, false);
                    }
                }
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
