package net.tiew.operationWild.screen.entity.submarine;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.tiew.operationWild.OperationWild;
import net.tiew.operationWild.entity.custom.vehicle.SeaBugEntity;
import net.tiew.operationWild.item.OWItems;

@OnlyIn(Dist.CLIENT)
public class SeaBugInventoryScreen extends AbstractContainerScreen<SeaBugInventoryMenu> {
    private static final ResourceLocation SEABUG_INVENTORY_LOCATION = ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "textures/gui/seabug_inventory_gui.png");
    private final SeaBugEntity seaBug;
    private float xMouse;
    private float yMouse;

    public SeaBugInventoryScreen(SeaBugInventoryMenu menu, Inventory inventory, Component component) {
        super(menu, inventory, component);
        if (Minecraft.getInstance().player.getRootVehicle() instanceof SeaBugEntity entity) this.seaBug = entity;
        else this.seaBug = null;
    }

    @Override
    protected void init() {
        super.init();
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
    }

    protected void renderBg(GuiGraphics guiGraphics, float p_282998_, int p_282929_, int p_283133_) {
        int i = (this.width - this.imageWidth) / 2;
        int j = (this.height - this.imageHeight) / 2;
        ItemStack itemInSlot0 = this.menu.getSlot(0).getItem();
        ItemStack itemInSlot1 = this.menu.getSlot(1).getItem();
        ItemStack itemInSlot2 = this.menu.getSlot(2).getItem();


        guiGraphics.blit(SEABUG_INVENTORY_LOCATION, i, j, 0, 0, 176,166);


        if (!itemInSlot0.isEmpty() && itemInSlot0.getItem() == OWItems.BATTERY.get()) {
            int maxDurability = itemInSlot0.getMaxDamage();
            int currentDurability = itemInSlot0.getDamageValue();
            int durabilityRemaining = maxDurability - currentDurability;

            int barHeight = (int) (33 * (durabilityRemaining / (float) maxDurability));
            int yOffset = 33 - barHeight;

            guiGraphics.blit(SEABUG_INVENTORY_LOCATION, i + 8, j + 12 + yOffset, 42, 166 + yOffset, 16, barHeight);

            guiGraphics.blit(SEABUG_INVENTORY_LOCATION, i + 25, j + 67, 0, 174, 38, 8);
        }

        if (!itemInSlot1.isEmpty() && itemInSlot1.getItem() == OWItems.BATTERY.get()) {
            int maxDurability = itemInSlot1.getMaxDamage();
            int currentDurability = itemInSlot1.getDamageValue();
            int durabilityRemaining = maxDurability - currentDurability;

            int barHeight = (int) (33 * (durabilityRemaining / (float) maxDurability));
            int yOffset = 33 - barHeight;

            guiGraphics.blit(SEABUG_INVENTORY_LOCATION, i + 152, j + 12 + yOffset, 42, 166 + yOffset, 16, barHeight);

            guiGraphics.blit(SEABUG_INVENTORY_LOCATION, i + 113, j + 67, 0, 166, 38, 8);
        }

        renderTexts(guiGraphics, i, j);
    }

    public void render(GuiGraphics graphics, int mouseX, int mouseY, float v) {
        this.renderBackground(graphics, mouseX, mouseY, v);
        this.xMouse = (float) mouseX;
        this.yMouse = (float) mouseY;
        super.render(graphics, mouseX, mouseY, v);
        this.renderTooltip(graphics, mouseX, mouseY);
    }

    private void renderTexts(GuiGraphics graphics, int offsetX, int offsetY) {
        int centerX = offsetX + (this.imageWidth / 2);
        int centerY = offsetY + (this.imageHeight / 2);
    }
}