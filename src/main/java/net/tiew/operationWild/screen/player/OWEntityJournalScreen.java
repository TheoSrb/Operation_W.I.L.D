package net.tiew.operationWild.screen.player;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Squid;
import net.minecraft.world.entity.player.Player;
import net.tiew.operationWild.entity.OWEntityRegistry;
import org.lwjgl.glfw.GLFW;
import net.tiew.operationWild.OperationWild;
import net.tiew.operationWild.entity.OWEntity;
import net.tiew.operationWild.entity.custom.living.BoaEntity;
import net.tiew.operationWild.entity.custom.living.PeacockEntity;
import net.tiew.operationWild.entity.custom.living.TigerEntity;
import net.tiew.operationWild.entity.custom.living.TigerSharkEntity;
import net.tiew.operationWild.event.ClientEvents;

public class OWEntityJournalScreen extends Screen {
    private static final ResourceLocation OW_ENTITY_JOURNAL_INTERFACE_LOCATION = ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "textures/gui/mob_page/misc/ow_entity_journal_interface_gui.png");
    private static final ResourceLocation OW_ENTITY_JOURNAL_INTERFACE_TORN_OUT_LOCATION = ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "textures/gui/mob_page/misc/ow_entity_journal_interface_torn_out_gui.png");
    private static final ResourceLocation OW_ENTITY_JOURNAL_BUTTON_LOCATION = ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "textures/gui/mob_page/misc/ow_entity_journal_button.png");
    private static final ResourceLocation OW_ENTITY_JOURNAL_TAMING_EXPERIENCE_LOCATION = ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "textures/gui/mob_page/misc/ow_entity_journal_interface_taming_experience_gui.png");
    private static final ResourceLocation OW_ENTITY_JOURNAL_TAMING_EXPERIENCE_FULL_LOCATION = ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "textures/gui/mob_page/misc/ow_entity_journal_interface_taming_experience_full_gui.png");
    private static final ResourceLocation ARROW_FORWARD = ResourceLocation.fromNamespaceAndPath("minecraft", "widget/page_forward");
    private static final ResourceLocation ARROW_BACKWARD = ResourceLocation.fromNamespaceAndPath("minecraft", "widget/page_backward");
    private static final ResourceLocation ARROW_FORWARD_HIGHLIGHTED = ResourceLocation.fromNamespaceAndPath("minecraft", "widget/page_forward_highlighted");
    private static final ResourceLocation ARROW_BACKWARD_HIGHLIGHTED = ResourceLocation.fromNamespaceAndPath("minecraft", "widget/page_backward_highlighted");

    private static final ResourceLocation BOA_INTERFACE_LOCATION = ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "textures/gui/mob_page/description/boa_page.png");
    private static final ResourceLocation BOA_TAMING_INTERFACE_LOCATION = ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "textures/gui/mob_page/taming/boa_page_taming.png");
    private static final ResourceLocation PEACOCK_INTERFACE_LOCATION = ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "textures/gui/mob_page/description/peacock_page.png");
    private static final ResourceLocation PEACOCK_TAMING_INTERFACE_LOCATION = ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "textures/gui/mob_page/taming/peacock_page_taming.png");
    private static final ResourceLocation TIGER_INTERFACE_LOCATION = ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "textures/gui/mob_page/description/tiger_page.png");
    private static final ResourceLocation TIGER_TAMING_INTERFACE_LOCATION = ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "textures/gui/mob_page/taming/tiger_page_taming.png");
    private static final ResourceLocation TIGER_SHARK_INTERFACE_LOCATION = ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "textures/gui/mob_page/description/tiger_shark_page.png");

    private float xMouse;
    private float yMouse;
    protected int imageWidth = 256;
    protected int imageHeight = 193;

    private int actualPage = 1;
    private int maxPage = 9;

    private int[] descriptionPages = {2, 5};
    private int[] tamingPages = {6, 8};
    private int miscPage = 9;

    private final Player player;
    public static String newEntityDiscovered = "";
    public static boolean canNotifyNewTamingPage = false;
    public static int currentNotificationThreshold = -1;

    public static final int[] THRESHOLDS = {25, 80, 165, 115};
    public static int lastReachedThreshold = -1;

    private static OWEntity owEntity;

    public OWEntityJournalScreen() {
        super(Component.literal("Entity Journal"));
        this.minecraft = Minecraft.getInstance();

        this.player = minecraft.player;
    }

    private ResourceLocation getMobDescriptionTexture(String mobName) {
        return ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "textures/gui/mob_page/description/" + mobName + "_page.png");
    }

    @Override
    protected void init() {
        super.init();
        int i = (this.width - this.imageWidth) / 2;
        int j = (this.height - this.imageHeight) / 2;
        this.minecraft = Minecraft.getInstance();

        if (ClientEvents.isNotifiedOWBook) {

        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private void nextPage() {
        if (actualPage < maxPage) {
            actualPage++;
            this.minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.BOOK_PAGE_TURN, 1.0F));
            this.minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
        }
    }

    private void precedentPage() {
        if (actualPage > 1) {
            actualPage--;
            this.minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.BOOK_PAGE_TURN, 1.0F));
            this.minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
        }
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_RIGHT) {
            nextPage();
            return true;
        }
        if (keyCode == GLFW.GLFW_KEY_LEFT) {
            precedentPage();
            return true;
        }

        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        int i = (this.width - this.imageWidth) / 2;
        int j = (this.height - this.imageHeight) / 2;

        if (isMouseInEntityArea(mouseX, mouseY, i + 220, j + 160, 23, 13)) {
            nextPage();
            return true;
        }
        if (isMouseInEntityArea(mouseX, mouseY, i + 13, j + 160, 23, 13)) {
            precedentPage();
            return true;
        }


        if (isMouseInEntityArea(mouseX, mouseY, i + 25, j - 5, 38, 8)) {
            if (actualPage == 1) return false;
            this.minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.BOOK_PAGE_TURN, 1.0F));
            this.minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
            actualPage = 1;
            return true;
        }
        if (isMouseInEntityArea(mouseX, mouseY, i + 72, j - 5, 38, 8)) {
            if (actualPage >= descriptionPages[0] && actualPage <= descriptionPages[1]) return false;
            this.minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.BOOK_PAGE_TURN, 1.0F));
            this.minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
            actualPage = 2;
            return true;
        }
        if (isMouseInEntityArea(mouseX, mouseY, i + 143, j - 5, 38, 8)) {
            if (actualPage >= tamingPages[0] && actualPage <= tamingPages[1]) return false;
            this.minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.BOOK_PAGE_TURN, 1.0F));
            this.minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
            actualPage = 6;
            return true;
        }
        if (isMouseInEntityArea(mouseX, mouseY, i + 193, j - 5, 38, 8)) {
            if (actualPage == miscPage) return false;
            this.minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.BOOK_PAGE_TURN, 1.0F));
            this.minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
            actualPage = miscPage;
            return true;
        }





        if (isMouseInEntityArea(mouseX, mouseY, i + 254, j + 20, 38, 13)) {
            if (actualPage == 2 || actualPage == 6 || actualPage == miscPage) return false;
            this.minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.BOOK_PAGE_TURN, 1.0F));
            this.minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
            actualPage = (actualPage >= 2 && actualPage <= 5) ? 2 : 6;
            return true;
        }
        if (isMouseInEntityArea(mouseX, mouseY, i + 254, j + 37, 38, 13)) {
            if (actualPage == 3 || actualPage == 7 || actualPage == miscPage) return false;
            this.minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.BOOK_PAGE_TURN, 1.0F));
            this.minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
            actualPage = (actualPage >= 2 && actualPage <= 5) ? 3 : 7;;
            return true;
        }
        if (isMouseInEntityArea(mouseX, mouseY, i + 254, j + 54, 38, 13)) {
            if (actualPage == 4 || actualPage == 8 || actualPage == miscPage) return false;
            this.minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.BOOK_PAGE_TURN, 1.0F));
            this.minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
            actualPage = (actualPage >= 2 && actualPage <= 5) ? 4 : 8;;
            return true;
        }
        if (isMouseInEntityArea(mouseX, mouseY, i + 254, j + 71, 38, 13)) {
            if (actualPage == 5 || actualPage == 8 || actualPage == miscPage) return false;
            this.minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.BOOK_PAGE_TURN, 1.0F));
            this.minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
            actualPage = (actualPage >= 2 && actualPage <= 5) ? 5 : 9;;
            return true;
        }


        return super.mouseClicked(mouseX, mouseY, button);
    }

    private boolean isMouseInEntityArea(double mouseX, double mouseY, int x, int y, int width, int height) {
        return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
    }

    private void createCategory(GuiGraphics graphics, int i, int j, int textX, int textY, String textComponent, boolean colorHighlighted, float scale, String entityType, boolean isTaming) {
        Component text = Component.translatable(textComponent).setStyle(Style.EMPTY.withBold(true));
        int color;

        if (isTaming) {
            if (isMouseInEntityArea(xMouse, yMouse, i, j, 38, 8) || colorHighlighted) {
                graphics.blit(OW_ENTITY_JOURNAL_BUTTON_LOCATION, i, j, entityType != null ? ClientEvents.tamingExperience >= getMaxTamingExp(entityType) ? 0 : 38 : 0, entityType != null ? ClientEvents.tamingExperience >= getMaxTamingExp(entityType) ? 13 : 0 : 13, 38, 13);
                color = 0xf1dba6;
            } else {
                graphics.blit(OW_ENTITY_JOURNAL_BUTTON_LOCATION, i, j, entityType != null ? ClientEvents.tamingExperience >= getMaxTamingExp(entityType) ? 0 : 38 : 0, 0, 38, 13);
                color = 0x564c39;
            }
        } else {
            if (isMouseInEntityArea(xMouse, yMouse, i, j, 38, 8) || colorHighlighted) {
                graphics.blit(OW_ENTITY_JOURNAL_BUTTON_LOCATION, i, j, entityType != null ? ClientEvents.hasPlayerKilledOWEntity(player, entityType) ? 0 : 38 : 0, entityType != null ? ClientEvents.hasPlayerKilledOWEntity(player, entityType) ? 13 : 0 : 13, 38, 13);
                color = 0xf1dba6;
            } else {
                graphics.blit(OW_ENTITY_JOURNAL_BUTTON_LOCATION, i, j, entityType != null ? ClientEvents.hasPlayerKilledOWEntity(player, entityType) ? 0 : 38 : 0, 0, 38, 13);
                color = 0x564c39;
            }
        }

        if (colorHighlighted) color = 0xf1dba6;

        graphics.pose().pushPose();
        graphics.pose().scale(scale, scale, 1.0f);

        if (textComponent.equals("?")) {
            textX = 17;
        }

        int scaledX = (int) ((i + textX - ((float) this.font.width(text) / 2)) / scale);
        int scaledY = (int) ((j + textY) / scale);

        graphics.drawString(this.font, text, scaledX, scaledY, color, false);
        graphics.pose().popPose();
    }

    private int adaptSpace(String entityType) {
        switch (entityType) {
            case "boa": return 0;
            case "peacock": return 1;
            case "tiger": return 2;
            case "tiger_shark": return 3;
        }
        return 0;
    }

    public int getTamingPageForAnimal(String animal) {
        switch (animal) {
            case "tiger":
                return 7;
            case "boa":
                return 5;
            case "peacock":
                return 6;
            case "tiger_shark":
                return 8;
            default:
                return -1;
        }
    }

    public int getDescriptionPageForAnimal(String animal) {
        switch (animal) {
            case "tiger":
                return 4;
            case "boa":
                return 2;
            case "peacock":
                return 3;
            case "tiger_shark":
                return 5;
            default:
                return -1;
        }
    }

    private double getMaxTamingExp(String entityType) {
        switch (entityType) {
            case "boa": return BoaEntity.TAMING_EXPERIENCE;
            case "peacock": return PeacockEntity.TAMING_EXPERIENCE;
            case "tiger": return TigerEntity.TAMING_EXPERIENCE;
            case "tiger_shark": return TigerSharkEntity.TAMING_EXPERIENCE;
            default: return -1;
        }
    }

    public static EntityType<? extends OWEntity> getEntityTypeFromPage(int page) {
        switch (page) {
            case 0: return null;
            case 1: return null;
            case 2: case 6: return OWEntityRegistry.BOA.get();
            case 3: case 7: return OWEntityRegistry.PEACOCK.get();
            case 4: case 8: return OWEntityRegistry.TIGER.get();
            case 5: return OWEntityRegistry.TIGER_SHARK.get();
            default: return null;
        }
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        int i = (this.width - this.imageWidth) / 2;
        int j = (this.height - this.imageHeight) / 2;
        this.xMouse = mouseX;
        this.yMouse = mouseY;

        super.render(graphics, mouseX, mouseY, partialTick);

        EntityType<? extends OWEntity> entityType = getEntityTypeFromPage(actualPage);
        if (entityType != null) {
            owEntity = entityType.create(this.minecraft.level);
        }

        if (isMouseInEntityArea(mouseX, mouseY, i + 219, j + 160, 23, 13)) {
            graphics.blitSprite(ARROW_FORWARD_HIGHLIGHTED, i + 219, j + 160, 23, 13);
        } else graphics.blitSprite(ARROW_FORWARD, i + 219, j + 160, 23, 13);

        if (isMouseInEntityArea(mouseX, mouseY, i + 13, j + 160, 23, 13)) {
            graphics.blitSprite(ARROW_BACKWARD_HIGHLIGHTED, i + 13, j + 160, 23, 13);
        } else graphics.blitSprite(ARROW_BACKWARD, i + 13, j + 160, 23, 13);

        createCategory(graphics, i + 25, j - 5, 25, 4, "tooltip.menuBook", actualPage == 1, 0.6f, null, false);
        createCategory(graphics, i + 72, j - 5, 27, 4, "tooltip.entity", actualPage >= descriptionPages[0] && actualPage <= descriptionPages[1], 0.6f, null, false);
        createCategory(graphics, i + 143, j - 5, 31, 4, "tooltip.taming", actualPage >= tamingPages[0] && actualPage <= tamingPages[1], 0.6f, null, false);
        createCategory(graphics, i + 193, j - 5, 27, 4, "tooltip.other", actualPage >= miscPage, 0.6f, null, false);

        if (actualPage >= descriptionPages[0] && actualPage <= descriptionPages[1]) {
            createCategory(graphics, i + 254, j + 20, 15, 5, ClientEvents.hasPlayerKilledOWEntity(player, "boa") ? "entity.ow.boa" : "?", actualPage == 2, 0.6f, "boa", false);
            createCategory(graphics, i + 254, j + 37, 18, 5, ClientEvents.hasPlayerKilledOWEntity(player, "peacock") ? "entity.ow.peacock" : "?", actualPage == 3, 0.6f, "peacock", false);
            createCategory(graphics, i + 254, j + 54, 19, 4, ClientEvents.hasPlayerKilledOWEntity(player, "tiger") ? "entity.ow.tiger" : "?", actualPage == 4, 0.6f, "tiger", false);
            createCategory(graphics, i + 254, j + 71, 40, 5, ClientEvents.hasPlayerKilledOWEntity(player, "tiger_shark") ? "entity.ow.tiger_shark" : "?", actualPage == 5, 0.435f, "tiger_shark", false);
        }

        if (actualPage >= tamingPages[0] && actualPage <= tamingPages[1]) {
            createCategory(graphics, i + 254, j + 20, 15, 5, ClientEvents.tamingExperience >= BoaEntity.TAMING_EXPERIENCE ? "entity.ow.boa" : "?", actualPage == 5, 0.6f, "boa", true);
            createCategory(graphics, i + 254, j + 37, 18, 5, ClientEvents.tamingExperience >= PeacockEntity.TAMING_EXPERIENCE ? "entity.ow.peacock" : "?", actualPage == 6, 0.6f, "peacock", true);
            createCategory(graphics, i + 254, j + 54, 19, 4, ClientEvents.tamingExperience >= TigerEntity.TAMING_EXPERIENCE ? "entity.ow.tiger" : "?", actualPage == 7, 0.6f, "tiger", true);
            createCategory(graphics, i + 254, j + 71, 19, 4, ClientEvents.tamingExperience >= TigerSharkEntity.TAMING_EXPERIENCE ? "entity.ow.tiger_shark" : "?", actualPage == 8, 0.435f, "tiger_shark", true);
        }

        if (newEntityDiscovered != null && !newEntityDiscovered.isEmpty()) {
            if (actualPage >= descriptionPages[0] && actualPage <= descriptionPages[1]) {
                graphics.blit(OW_ENTITY_JOURNAL_BUTTON_LOCATION, i + 294, j + 21 + (17 * adaptSpace(newEntityDiscovered)), 0, 26, 3, 11);
            } else graphics.blit(OW_ENTITY_JOURNAL_BUTTON_LOCATION, i + 105, j - 10, 0, 26, 3, 11);

            if (actualPage == adaptSpace(newEntityDiscovered) + 2) {
                newEntityDiscovered = null;
            }
        }

        if (canNotifyNewTamingPage) {
            if (actualPage >= tamingPages[0] && actualPage <= tamingPages[1]) {

                if (currentNotificationThreshold == TigerEntity.TAMING_EXPERIENCE) {
                    graphics.blit(OW_ENTITY_JOURNAL_BUTTON_LOCATION, i + 285, j + 21 + (17 * adaptSpace("tiger")), 0, 26, 3, 11);
                    if (actualPage == getTamingPageForAnimal("tiger")) {
                        OWEntity.clearNotification();
                    }
                }
                if (currentNotificationThreshold == BoaEntity.TAMING_EXPERIENCE) {
                    graphics.blit(OW_ENTITY_JOURNAL_BUTTON_LOCATION, i + 285, j + 21 + (17 * adaptSpace("boa")), 0, 26, 3, 11);
                    if (actualPage == getTamingPageForAnimal("boa")) {
                        OWEntity.clearNotification();
                    }
                }
                if (currentNotificationThreshold == PeacockEntity.TAMING_EXPERIENCE) {
                    graphics.blit(OW_ENTITY_JOURNAL_BUTTON_LOCATION, i + 285, j + 21 + (17 * adaptSpace("peacock")), 0, 26, 3, 11);
                    if (actualPage == getTamingPageForAnimal("peacock")) {
                        OWEntity.clearNotification();
                    }
                }
                if (currentNotificationThreshold == TigerSharkEntity.TAMING_EXPERIENCE) {
                    graphics.blit(OW_ENTITY_JOURNAL_BUTTON_LOCATION, i + 285, j + 21 + (17 * adaptSpace("tiger_shark")), 0, 26, 3, 11);
                    if (actualPage == getTamingPageForAnimal("tiger_shark")) {
                        OWEntity.clearNotification();
                    }
                }

            } else graphics.blit(OW_ENTITY_JOURNAL_BUTTON_LOCATION, i + 176, j - 10, 0, 26, 3, 11);
        }



        graphics.blit(OW_ENTITY_JOURNAL_INTERFACE_LOCATION, i, j, 0, 0, this.imageWidth, this.imageHeight);

        graphics.drawString(this.font, String.valueOf(actualPage + " / " + maxPage), i + 113, j - 7, 0xb69d77);


        showPage(graphics, i, j);
        renderTexts(graphics, i, j);

        if (actualPage == miscPage) {
            int fillHeight = (int) (141 * (ClientEvents.tamingExperience / 4000.0));
            int startY = j + (141 - fillHeight);

            graphics.blit(OW_ENTITY_JOURNAL_TAMING_EXPERIENCE_FULL_LOCATION, i + 35, startY + 23, 0, 141 - fillHeight, 15, fillHeight);

            Component text = Component.literal(String.valueOf(ClientEvents.tamingExperience)).setStyle(Style.EMPTY.withBold(true));
            float scale = 0.65f;
            int textWidth = (int) (this.font.width(text) * scale);
            int baseX = i + 92;

            int adjustedX = (baseX - textWidth) + 15;

            graphics.pose().pushPose();
            graphics.pose().scale(scale, scale, 1.0f);
            graphics.drawString(this.font, text, (int) (adjustedX / scale), (int) ((j + 16) / scale), 0xb69d77, false);

            graphics.drawString(this.font, Component.literal("300 -").withStyle(Style.EMPTY.withBold(true)), (int) (i + 117 / scale), (int) ((j + 158) / scale), 0xb69d77, false);
            graphics.drawString(this.font, Component.literal("1500 -").withStyle(Style.EMPTY.withBold(true)), (int) (i + 112 / scale), (int) ((j + 115) / scale), 0xb69d77, false);
            graphics.drawString(this.font, Component.literal("2700 -").withStyle(Style.EMPTY.withBold(true)), (int) (i + 112 / scale), (int) ((j + 73) / scale), 0xb69d77, false);
            graphics.drawString(this.font, Component.literal("4000 -").withStyle(Style.EMPTY.withBold(true)), (int) (i + 112 / scale), (int) ((j + 30) / scale), 0xb69d77, false);

            graphics.pose().popPose();
        }
    }

    private void showPage(GuiGraphics graphics, int i, int j) {
        if (player == null || owEntity == null) return;

        ResourceLocation entityId = BuiltInRegistries.ENTITY_TYPE.getKey(owEntity.getType());
        String entityName = entityId.getPath();

        if (ClientEvents.hasPlayerKilledOWEntity(player, entityName)) {
            graphics.blit(getMobDescriptionTexture(entityName), i, j, 0, 0, this.imageWidth, this.imageHeight);


            Component title = Component.translatable("entity.ow." + entityName).setStyle(Style.EMPTY.withBold(true));
            Component tamingExperience = Component.literal(String.valueOf(getMaxTamingExp(entityName))).setStyle(Style.EMPTY.withBold(true));

            int centerX = i + (this.imageWidth / 2);
            int centerY = j + (this.imageHeight / 2);
            float scale = 0.5f;
            int textWidth = (int) (this.font.width(tamingExperience) * scale);
            int baseX = i + 51;
            int adjustedX = (baseX - textWidth) + 15;


            graphics.drawString(this.font, title, (centerX - 62) - (this.font.width(title) / 2), centerY - 80, 0xb69d77, false);

            graphics.pose().pushPose();
            graphics.pose().scale(scale, scale, 1.0f);
            graphics.drawString(this.font, tamingExperience, (int) (adjustedX / scale), (int) ((j + 30) / scale), 0xb69d77, false);
            graphics.pose().popPose();

            showText(graphics, i, j);
        }
        else {
            graphics.blit(OW_ENTITY_JOURNAL_INTERFACE_TORN_OUT_LOCATION, i, j, 0, 0, this.imageWidth, this.imageHeight);
            Component text = Component.translatable("tooltip.noDiscovered");
            graphics.drawString(this.font, text, i + 128 - (this.font.width(text) / 2), j + 70, 0xb69d77, false);
        }
    }

    private void showText(GuiGraphics graphics, int i, int j) {
        if (owEntity instanceof BoaEntity) {
            createParagraph(graphics, i, j, "boa.page1", 55, -5, 115);
        }
    }

    private void createParagraph(GuiGraphics graphics, int offsetX, int offsetY, String textTranslation, int xOffset, int yOffset, int maxWidth) {
        int centerX = offsetX + (this.imageWidth / 2);
        int centerY = offsetY + (this.imageHeight / 2);

        Component text1 = Component.translatable(textTranslation).setStyle(Style.EMPTY);

        graphics.pose().pushPose();
        graphics.pose().scale(0.6f, 0.6f, 1.0f);

        int lineHeight = 10;
        int currentX = centerX - 225;
        int currentY = centerY - 1;

        currentY = renderText(graphics, text1.getString(), centerX, currentX - xOffset, currentY + yOffset, maxWidth, lineHeight, 0x745f41);
        currentY += lineHeight * 2;

        graphics.pose().popPose();
    }

    private int renderText(GuiGraphics graphics, String text, int centerX, int xOffset, int startY, int maxWidth, int lineHeight, int color) {
        String[] words = text.split(" ");
        StringBuilder currentLine = new StringBuilder();
        int currentY = startY;
        int currentX = centerX - xOffset;
        for (String word : words) {
            String testLine = currentLine.isEmpty() ? word : currentLine + " " + word;
            if (this.font.width(testLine) > maxWidth) {
                if (!currentLine.isEmpty()) {
                    currentX = renderLineWithFormatting(graphics, currentLine.toString(), centerX - xOffset, currentY, color);
                    currentY += lineHeight;
                    currentLine = new StringBuilder(word);
                } else {
                    currentX = renderLineWithFormatting(graphics, word, centerX - xOffset, currentY, color);
                    currentY += lineHeight;
                }
            } else {
                currentLine = new StringBuilder(testLine);
            }
        }
        if (!currentLine.isEmpty()) {
            currentX = renderLineWithFormatting(graphics, currentLine.toString(), centerX - xOffset, currentY, color);
            currentY += lineHeight;
        }
        return currentY;
    }

    private int renderLineWithFormatting(GuiGraphics graphics, String line, int x, int y, int color) {
        String[] words = line.split(" ");
        int currentX = x;
        for (int i = 0; i < words.length; i++) {
            String word = words[i];
            graphics.drawString(this.font, word, currentX, y, color, false);
            currentX += this.font.width(word);

            if (i < words.length - 1) {
                graphics.drawString(this.font, " ", currentX, y, color, false);
                currentX += this.font.width(" ");
            }
        }
        return currentX;
    }

    private void renderTexts(GuiGraphics graphics, int offsetX, int offsetY) {
        int i = (this.width - this.imageWidth) / 2;
        int j = (this.height - this.imageHeight) / 2;
        int centerX = offsetX + (this.imageWidth / 2);
        int centerY = offsetY + (this.imageHeight / 2);

        if (actualPage == 1) {
            Component title = Component.literal("Operation").setStyle(Style.EMPTY.withBold(true));
            Component text1 = Component.literal("W.I.L.D").setStyle(Style.EMPTY.withBold(true));

            Component text2 = Component.translatable("tooltip.notebook").setStyle(Style.EMPTY);

            Component text3 = Component.translatable("tooltip.notebookDescribe").setStyle(Style.EMPTY);

            float scale = 1.4f;
            float scale2 = 0.8f;

            graphics.pose().pushPose();
            graphics.pose().scale(scale, scale, 1.0f);

            graphics.drawString(this.font, title, (int) (((centerX - 70) - ((float) this.font.width(title) / 2)) / scale), (int) ((centerY - 30) / scale), 0xb69d77, false);
            graphics.drawString(this.font, text1, (int) (((centerX - 65) - ((float) this.font.width(text1) / 2)) / scale), (int) ((centerY - 12) / scale), 0xb69d77, false);

            graphics.pose().popPose();

            graphics.pose().pushPose();
            graphics.pose().scale(scale2, scale2, 1.0f);

            int lineHeight = 10;
            int currentY = centerY - 1;

            graphics.drawString(this.font, text2, (int) (((centerX - 46) - ((float) this.font.width(text2) / 2)) / scale2), (int) ((centerY + 10) / scale2), 0xb69d77, false);
            currentY = renderText(graphics, text3.getString(), centerX, -75, currentY - 50, 115, lineHeight, 0xb69d77);


            graphics.pose().popPose();
        }
        /*else if (actualPage == 2 && ClientEvents.hasPlayerKilledOWEntity(player, "boa")) {

        } else if (actualPage == 3 && ClientEvents.hasPlayerKilledOWEntity(player, "peacock")) {
            Component text1 = Component.translatable("peacock.page1").setStyle(Style.EMPTY);
            Component text2 = Component.translatable("peacock.page2").setStyle(Style.EMPTY);
            Component text3 = Component.translatable("peacock.page3").setStyle(Style.EMPTY);
            Component text4 = Component.translatable("peacock.page4").setStyle(Style.EMPTY);

            graphics.pose().pushPose();
            graphics.pose().scale(0.6f, 0.6f, 1.0f);

            int maxLengthByLine = 175;
            int lineHeight = 10;
            int currentY = centerY - 1;

            String[] boldWords = {"Peacock", "Paon"};

            currentY = renderText(graphics, text1.getString(), centerX, 22, currentY, maxLengthByLine, lineHeight, 0x745f41, boldWords);
            currentY += lineHeight * 2;

            currentY = renderText(graphics, text2.getString(), centerX, 20, currentY + (isEnglish ? 10 : 0), maxLengthByLine, lineHeight, 0x745f41, boldWords);
            currentY += lineHeight * 2;

            currentY = renderText(graphics, text3.getString(), centerX, -180, currentY - (isEnglish ? 78 : 103), maxLengthByLine, lineHeight, 0x745f41, boldWords);
            currentY += lineHeight * 2;

            currentY = renderText(graphics, text4.getString(), centerX,  -180, currentY, maxLengthByLine, lineHeight, 0x745f41, boldWords);

            graphics.pose().popPose();
        } else if (actualPage == 4 && ClientEvents.hasPlayerKilledOWEntity(player, "tiger")) {
            Component text1 = Component.translatable("tiger.page1").setStyle(Style.EMPTY);
            Component text2 = Component.translatable("tiger.page2").setStyle(Style.EMPTY);
            Component text3 = Component.translatable("tiger.page3").setStyle(Style.EMPTY);
            Component text4 = Component.translatable("tiger.page4").setStyle(Style.EMPTY);

            graphics.pose().pushPose();
            graphics.pose().scale(0.6f, 0.6f, 1.0f);

            int maxLengthByLine = 160;
            int lineHeight = 10;
            int currentY = centerY - 1;

            String[] boldWords = {"Tiger", "Tigre"};

            currentY = renderText(graphics, text1.getString(), centerX, 22, currentY - 7, maxLengthByLine, lineHeight, 0x745f41, boldWords);
            currentY += lineHeight * 2;

            currentY = renderText(graphics, text2.getString(), centerX, 22, currentY, maxLengthByLine, lineHeight, 0x745f41, boldWords);
            currentY += lineHeight * 2;

            currentY = renderText(graphics, text3.getString(), centerX, -180, currentY - (isEnglish ? 40 : 50), maxLengthByLine, lineHeight, 0x745f41, boldWords);
            currentY += lineHeight * 2;

            currentY = renderText(graphics, text4.getString(), centerX,  -180, currentY - 10, maxLengthByLine, lineHeight, 0x745f41, boldWords);

            graphics.pose().popPose();
        } else if (actualPage == 5 && ClientEvents.hasPlayerKilledOWEntity(player, "tiger_shark")) {
            Component text1 = Component.translatable("tiger_shark.page1").setStyle(Style.EMPTY);
            Component text2 = Component.translatable("tiger_shark.page2").setStyle(Style.EMPTY);
            Component text3 = Component.translatable("tiger_shark.page3").setStyle(Style.EMPTY);
            Component text4 = Component.translatable("tiger_shark.page4").setStyle(Style.EMPTY);
            Component text5 = Component.translatable("tiger_shark.page5").setStyle(Style.EMPTY);

            graphics.pose().pushPose();
            graphics.pose().scale(0.6f, 0.6f, 1.0f);

            int maxLengthByLine = 160;
            int lineHeight = 10;
            int currentY = centerY - 1;

            String[] boldWords = {"Requin", "Tigre", "Requins", "Tigres", "Stations", "Immergées.", "Saumon"};

            currentY = renderText(graphics, text1.getString(), centerX, 22, currentY - 5, maxLengthByLine - 10, lineHeight, 0x745f41, boldWords);
            currentY += lineHeight * 2;

            currentY = renderText(graphics, text2.getString(), centerX, -50, currentY - 5 , maxLengthByLine - 60, lineHeight, 0x745f41, boldWords);
            currentY += lineHeight * 2;

            currentY = renderText(graphics, text3.getString(), centerX, -180, currentY - 140, maxLengthByLine, lineHeight, 0x745f41, boldWords);
            currentY += lineHeight * 2;

            currentY = renderText(graphics, text4.getString(), centerX,  -180, currentY - 10, maxLengthByLine, lineHeight, 0x745f41, boldWords);
            currentY += lineHeight * 2;

            currentY = renderText(graphics, text5.getString(), centerX,  -180, currentY - 10, maxLengthByLine, lineHeight, 0x745f41, boldWords);

            graphics.pose().popPose();
        }*/ else if (actualPage == 6 && ClientEvents.tamingExperience >= BoaEntity.TAMING_EXPERIENCE) {
            Component title = Component.translatable("tooltip.tamingOf").setStyle(Style.EMPTY.withBold(true)).append(" ")
                    .append(Component.translatable("owEntity.ow.boa").setStyle(Style.EMPTY.withBold(true)));

            Component text1 = Component.translatable("boa.taming.page1").setStyle(Style.EMPTY);
            Component text2 = Component.translatable("boa.taming.page2").setStyle(Style.EMPTY);
            Component text3 = Component.translatable("boa.taming.page3").setStyle(Style.EMPTY);
            Component text4 = Component.translatable("boa.taming.page4").setStyle(Style.EMPTY);

            float scale = 0.8f;

            graphics.pose().pushPose();
            graphics.pose().scale(scale, scale, 1.0f);

            graphics.drawString(this.font, title, (int) ((centerX - 62) / scale - (float) this.font.width(title) / 2), (int) ((centerY - 77) / scale), 0xb69d77, false);

            graphics.pose().popPose();

            graphics.pose().pushPose();
            graphics.pose().scale(0.6f, 0.6f, 1.0f);

            int maxLengthByLine = 175;
            int lineHeight = 10;
            int currentY = centerY - 1;

            currentY = renderText(graphics, text1.getString(), centerX, 15, currentY - 5, 150, lineHeight, 0x745f41);
            currentY += lineHeight * 2;

            currentY = renderText(graphics, text2.getString(), centerX, -180, currentY - 125, maxLengthByLine - 20, lineHeight, 0x745f41);
            currentY += lineHeight * 2;

            currentY = renderText(graphics, text3.getString(), centerX, -180, currentY, maxLengthByLine - 75, lineHeight, 0x745f41);
            currentY += lineHeight * 2;

            currentY = renderText(graphics, text4.getString(), centerX,  -180, currentY + 35, maxLengthByLine, lineHeight, 0x745f41);

            graphics.pose().popPose();

        } else if (actualPage == 7 && ClientEvents.tamingExperience >= PeacockEntity.TAMING_EXPERIENCE) {
            Component text1 = Component.translatable("peacock.taming.page1").setStyle(Style.EMPTY);
            Component text2 = Component.translatable("peacock.taming.page2").setStyle(Style.EMPTY);
            Component text3 = Component.translatable("peacock.taming.page3").setStyle(Style.EMPTY);
            Component text4 = Component.translatable("peacock.taming.page4").setStyle(Style.EMPTY);
            Component text5 = Component.translatable("peacock.taming.page5").setStyle(Style.EMPTY);

            float scale = 0.8f;

            graphics.pose().pushPose();
            graphics.pose().scale(scale, scale, 1.0f);

            graphics.drawString(this.font, title, (int) ((centerX - 62) / scale - (float) this.font.width(title) / 2), (int) ((centerY - 77) / scale), 0xb69d77, false);

            graphics.pose().popPose();

            graphics.pose().pushPose();
            graphics.pose().scale(0.6f, 0.6f, 1.0f);

            int maxLengthByLine = 175;
            int lineHeight = 10;
            int currentY = centerY - 1;


            currentY = renderText(graphics, text1.getString(), centerX, 15, currentY - 5, 80, lineHeight, 0x745f41);
            currentY += lineHeight * 2;

            currentY = renderText(graphics, text2.getString(), centerX, 15, currentY + 15, 90, lineHeight, 0x745f41);
            currentY += lineHeight * 2;

            currentY = renderText(graphics, text3.getString(), centerX, -180, currentY - 255, 150, lineHeight, 0x745f41);
            currentY += lineHeight * 2;

            currentY = renderText(graphics, text4.getString(), centerX,  -180, currentY - 7, maxLengthByLine, lineHeight, 0x745f41);
            currentY += lineHeight * 2;

            currentY = renderText(graphics, text5.getString(), centerX,  -180, currentY + 87, maxLengthByLine, lineHeight, 0x745f41);

            graphics.pose().popPose();

        } else if (actualPage == 8 && ClientEvents.tamingExperience >= TigerEntity.TAMING_EXPERIENCE) {
            Component title = Component.translatable("tooltip.tamingOf").setStyle(Style.EMPTY.withBold(true)).append(" ")
                    .append(Component.translatable("owEntity.ow.tiger").setStyle(Style.EMPTY.withBold(true)));

            Component text1 = Component.translatable("tiger.taming.page1").setStyle(Style.EMPTY);
            Component text2 = Component.translatable("tiger.taming.page2").setStyle(Style.EMPTY);
            Component text3 = Component.translatable("tiger.taming.page3").setStyle(Style.EMPTY);
            Component text4 = Component.translatable("tiger.taming.page4").setStyle(Style.EMPTY);

            float scale = 0.8f;

            graphics.pose().pushPose();
            graphics.pose().scale(scale, scale, 1.0f);

            graphics.drawString(this.font, title, (int) ((centerX - 62) / scale - (float) this.font.width(title) / 2), (int) ((centerY - 77) / scale), 0xb69d77, false);

            graphics.pose().popPose();

            graphics.pose().pushPose();
            graphics.pose().scale(0.6f, 0.6f, 1.0f);

            int maxLengthByLine = 175;
            int lineHeight = 10;
            int currentY = centerY - 1;


            currentY = renderText(graphics, text1.getString(), centerX, 15, currentY - 5, maxLengthByLine, lineHeight, 0x745f41);
            currentY += lineHeight * 2;

            currentY = renderText(graphics, text2.getString(), centerX, 15, currentY - 5, maxLengthByLine - 20, lineHeight, 0x745f41);
            currentY += lineHeight * 2;

            currentY = renderText(graphics, text3.getString(), centerX, -180, currentY - 90, maxLengthByLine - 20, lineHeight, 0x745f41);
            currentY += lineHeight * 2;

            currentY = renderText(graphics, text4.getString(), centerX,  -180, currentY + 35, maxLengthByLine, lineHeight, 0x745f41);

            graphics.pose().popPose();

        } else if (actualPage == miscPage) {
            Component text = Component.translatable("tooltip.tamingExperienceDescribe").setStyle(Style.EMPTY);
            int lineHeight = 10;
            int currentY = centerY - 11;
            graphics.pose().pushPose();
            graphics.pose().scale(0.8f, 0.8f, 1.0f);
            currentY = renderText(graphics, text.getString(), centerX, -75, currentY - 50, 115, lineHeight, 0xb69d77);
            graphics.pose().popPose();
        }
    }
}