package net.tiew.operationWild.screen.entity;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.tiew.operationWild.OperationWild;
import net.tiew.operationWild.entity.OWEntity;
import net.tiew.operationWild.entity.client.MousePositionSaver;
import net.tiew.operationWild.networking.OWNetworkHandler;
import net.tiew.operationWild.networking.packets.to_server.OpenOWInventoryPacket;
import net.tiew.operationWild.screen.entity.skins.*;

import java.util.List;
import java.util.function.Consumer;

@OnlyIn(Dist.CLIENT)
public class OWTabsRenderer {

    private static final ResourceLocation OW_INVENTORY_LOCATION =
            ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "textures/gui/ow_inventory_gui.png");

    public enum Tab {
        INVENTORY,
        COSMETICS,
        TEAM,
        DAILY_QUESTS,
        PISTE,
        OPTIONS
    }

    private final int[] notifications = new int[6];
    private Tab activeTab = Tab.INVENTORY;

    private Button inventoryButton;
    private Button skinButton;
    private Button teamButton;
    private Button dailyQuestButton;
    private Button pisteButton;
    private Button optionsButton;

    public void setActiveTab(Tab tab) {
        this.activeTab = tab;
        updateButtonStates();
    }

    private void updateButtonStates() {
        if (inventoryButton != null) inventoryButton.active = activeTab != Tab.INVENTORY;
        if (skinButton != null) skinButton.active = activeTab != Tab.COSMETICS;
        if (teamButton != null) teamButton.active = activeTab != Tab.TEAM;
        if (dailyQuestButton != null) dailyQuestButton.active = activeTab != Tab.DAILY_QUESTS;
        if (pisteButton != null) pisteButton.active = activeTab != Tab.PISTE;
        if (optionsButton != null) optionsButton.active = activeTab != Tab.OPTIONS;
    }

    public Tab getActiveTab() {
        return activeTab;
    }

    /** Verrouille (clics ignorés) ou déverrouille tous les onglets — utilisé pendant le didacticiel. */
    public void setLocked(boolean locked) {
        if (locked) {
            if (inventoryButton != null) inventoryButton.active = false;
            if (skinButton != null) skinButton.active = false;
            if (teamButton != null) teamButton.active = false;
            if (dailyQuestButton != null) dailyQuestButton.active = false;
            if (pisteButton != null) pisteButton.active = false;
            if (optionsButton != null) optionsButton.active = false;
        } else {
            updateButtonStates();
        }
    }

    public void init(int screenWidth, int screenHeight, int imageWidth, int imageHeight, OWEntity entity, Consumer<Button> addWidget) {
        int i = (screenWidth - imageWidth) / 2;
        int j = (screenHeight - imageHeight) / 2;

        inventoryButton = buildButton("", 0xFFFFFF, i + 2, j - 18, 20, 18,
                () -> onInventoryClicked(entity));
        inventoryButton.setAlpha(0f);

        skinButton = buildButton("", 0xFFFFFF, i + 22, j - 18, 20, 18,
                () -> chooseSkinsScreenForEntity(entity));
        skinButton.setAlpha(0f);

        teamButton = buildButton("", 0xFFFFFF, i + 42, j - 18, 20, 18,
                () -> onTeamClicked(entity));
        teamButton.setAlpha(0f);

        dailyQuestButton = buildButton("", 0xFFFFFF, i + 62, j - 18, 20, 18,
                () -> onDailyQuestsClicked(entity));
        dailyQuestButton.setAlpha(0f);

        pisteButton = buildButton("", 0xFFFFFF, i + 82, j - 18, 20, 18,
                () -> onPisteClicked(entity));
        pisteButton.setAlpha(0f);

        optionsButton = buildButton("", 0xFFFFFF, i + 102, j - 18, 20, 18,
                () -> Minecraft.getInstance().setScreen(new OWOptionsScreen()));
        optionsButton.setAlpha(0f);

        addWidget.accept(inventoryButton);
        addWidget.accept(skinButton);
        addWidget.accept(teamButton);
        addWidget.accept(dailyQuestButton);
        addWidget.accept(pisteButton);
        addWidget.accept(optionsButton);
    }

    private Button buildButton(String label, int color, int x, int y, int width, int height, Runnable onClick) {
        return Button.builder(
                        Component.literal(label).setStyle(Style.EMPTY.withColor(TextColor.fromRgb(color))),
                        btn -> onClick.run())
                .bounds(x, y, width, height)
                .build();
    }

    public void setNotification(int tabId, int count) {
        if (tabId >= 1 && tabId <= 6) {
            notifications[tabId - 1] = Math.max(0, count);
        }
    }

    public int getNotification(int tabId) {
        if (tabId >= 1 && tabId <= 6) return notifications[tabId - 1];
        return 0;
    }

    private void onInventoryClicked(OWEntity entity) {
        setNotification(1, 0);

        Minecraft mc = Minecraft.getInstance();

        double[] xPos = new double[1];
        double[] yPos = new double[1];
        org.lwjgl.glfw.GLFW.glfwGetCursorPos(mc.getWindow().getWindow(), xPos, yPos);
        MousePositionSaver.save(xPos[0], yPos[0]);

        mc.setScreen(null);
        OWNetworkHandler.sendToServer(new OpenOWInventoryPacket());
    }

    public void setElementColor(int hexColor) {
        float r = ((hexColor >> 16) & 0xFF) / 255.0f;
        float g = ((hexColor >> 8) & 0xFF) / 255.0f;
        float b = (hexColor & 0xFF) / 255.0f;
        RenderSystem.setShaderColor(r, g, b, 1.0f);
    }

    private void chooseSkinsScreenForEntity(OWEntity entity) {
        setNotification(2, 0);
        switch (entity.getClass().getSimpleName()) {
            case "KodiakEntity"    -> Minecraft.getInstance().setScreen(new KodiakSkinsScreen());
            case "TigerEntity"     -> Minecraft.getInstance().setScreen(new TigerSkinsScreen());
            case "CrocodileEntity" -> Minecraft.getInstance().setScreen(new CrocodileSkinsScreen());
            case "BoaEntity"       -> Minecraft.getInstance().setScreen(new BoaSkinsScreen());
            case "KangarooEntity"  -> Minecraft.getInstance().setScreen(new KangarooSkinsScreen());
            default -> Minecraft.getInstance().player.sendSystemMessage(
                    Component.translatable("tooltip.noSkins").withStyle(Style.EMPTY).withColor(0xFF0000));
        }
    }

    /*
        public void onTeamClicked(OWEntity entity) {
        setNotification(3, 0);
        Minecraft.getInstance().setScreen(new OWTeamsInterface(Component.empty()));
    }
     */

    public void onTeamClicked(OWEntity entity) {
        setNotification(3, 0);
        if (entity.currentTeam == null) {
            Minecraft.getInstance().setScreen(new OWTeamCreationScreen(entity));
        } else {
            Minecraft.getInstance().setScreen(new OWTeamsInterface(Component.empty()));
        }
    }

    public void onDailyQuestsClicked(OWEntity entity) {
        setNotification(4, 0);
        entity.getControllingPassenger().sendSystemMessage(Component.literal("ERROR"));
    }

    public void onPisteClicked(OWEntity entity) {
        setNotification(5, 0);
        Minecraft.getInstance().setScreen(new net.tiew.operationWild.screen.entity.piste.OWPisteScreen());
    }

    public void renderTabs(GuiGraphics graphics, Font font, OWEntity entity, int i, int j, int mouseX, int mouseY) {
        boolean hoverInventory = isHovering(mouseX, mouseY, i + 2, j - 18, 20, 18);
        boolean hoverSkin = isHovering(mouseX, mouseY, i + 22, j - 18, 20, 18);
        boolean hoverTeam = isHovering(mouseX, mouseY, i + 42, j - 18, 20, 18);
        boolean hoverQuests = isHovering(mouseX, mouseY, i + 62, j - 18, 20, 18);
        boolean hoverPiste = isHovering(mouseX, mouseY, i + 82, j - 18, 20, 18);
        boolean hoverOptions = isHovering(mouseX, mouseY, i + 102, j - 18, 20, 18);

        graphics.blit(OW_INVENTORY_LOCATION, i + 2, j - 18, (hoverInventory || activeTab == Tab.INVENTORY) ? 20 : 0, 206, 20, 18);
        graphics.blit(OW_INVENTORY_LOCATION, i + 22, j - 18, (hoverSkin || activeTab == Tab.COSMETICS) ? 20 : 0, 206, 20, 18);
        graphics.blit(OW_INVENTORY_LOCATION, i + 42, j - 18, (hoverTeam || activeTab == Tab.TEAM) ? 20 : 0, 206, 20, 18);
        graphics.blit(OW_INVENTORY_LOCATION, i + 62, j - 18, (hoverQuests || activeTab == Tab.DAILY_QUESTS) ? 20 : 0, 206, 20, 18);
        graphics.blit(OW_INVENTORY_LOCATION, i + 82, j - 18, (hoverPiste || activeTab == Tab.PISTE) ? 20 : 0, 206, 20, 18);
        graphics.blit(OW_INVENTORY_LOCATION, i + 102, j - 18, (hoverOptions || activeTab == Tab.OPTIONS) ? 20 : 0, 206, 20, 18);

        graphics.blit(OW_INVENTORY_LOCATION, i + 4, j - 16, 176, 0, 16, 16); // Inventaire
        graphics.blit(OW_INVENTORY_LOCATION, i + 24, j - 16, 176, 16, 16, 16); // Skin

        int teamColor = (entity != null && entity.currentTeam != null) ? entity.currentTeam.getTeamColor() : 0xFFFFFF;
        setElementColor(teamColor);
        graphics.blit(OW_INVENTORY_LOCATION, i + 44, j - 16, 176, 32, 16, 16);
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);


        graphics.blit(OW_INVENTORY_LOCATION, i + 64, j - 16, 176, 48, 16, 16); // Daily Quests
        graphics.blit(OW_INVENTORY_LOCATION, i + 84, j - 16, 176, 64, 16, 16); // Piste Sauvage
        graphics.blit(OW_INVENTORY_LOCATION, i + 104, j - 16, 176, 80, 16, 16); // Options

        renderNotificationBadges(graphics, font, entity, i, j);

        if (hoverInventory)
            graphics.renderComponentTooltip(font, List.of(tabTooltip("tooltip.inventory")), mouseX, mouseY);
        if (hoverSkin) graphics.renderComponentTooltip(font, List.of(tabTooltip("tooltip.cosmetics")), mouseX, mouseY);
        if (hoverTeam) graphics.renderComponentTooltip(font, List.of(tabTooltip("tooltip.team")), mouseX, mouseY);
        if (hoverQuests)
            graphics.renderComponentTooltip(font, List.of(tabTooltip("tooltip.dailyQuests")), mouseX, mouseY);
        if (hoverPiste) graphics.renderComponentTooltip(font, List.of(tabTooltip("tooltip.piste")), mouseX, mouseY);
        if (hoverOptions) graphics.renderComponentTooltip(font, List.of(tabTooltip("tooltip.options")), mouseX, mouseY);
    }

    private void renderNotificationBadges(GuiGraphics graphics, Font font, OWEntity entity, int i, int j) {
        int[] tabOffsets = {2, 22, 42, 62, 82, 102};
        int color = (entity.tickCount / 7) % 2 == 0 ? 0x92d124 : 0xF3F28F;

        for (int tab = 0; tab < 6; tab++) {
            int count = notifications[tab];
            if (count <= 0) continue;

            String label = count > 9 ? "9+" : String.valueOf(count);
            int exclamX = i + tabOffsets[tab] + 1;
            int exclamY = j - 18 - 3;
            graphics.blit(OW_INVENTORY_LOCATION, exclamX, exclamY, 0, 166, 3, 10);
            graphics.drawString(font, label, exclamX + 20 - font.width(label), exclamY + 2, color, true);
        }
    }

    private Component tabTooltip(String key) {
        return Component.translatable(key)
                .withStyle(Style.EMPTY.withColor(TextColor.fromRgb(0xFFFFFF)).withItalic(true).withBold(true));
    }

    private boolean isHovering(int mouseX, int mouseY, int x, int y, int w, int h) {
        return mouseX >= x && mouseX <= x + w && mouseY >= y && mouseY <= y + h;
    }
}