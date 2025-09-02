package net.tiew.operationWild.screen.player;

import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.tiew.operationWild.entity.OWEntityRegistry;
import net.tiew.operationWild.entity.animals.aquatic.JellyfishEntity;
import net.tiew.operationWild.entity.animals.aquatic.MantaEntity;
import net.tiew.operationWild.entity.animals.aquatic.TigerSharkEntity;
import net.tiew.operationWild.entity.animals.terrestrial.*;
import net.tiew.operationWild.entity.client.model.AdventurerManuscriptModel;
import net.tiew.operationWild.entity.misc.AdventurerManuscript;
import org.lwjgl.glfw.GLFW;
import net.tiew.operationWild.OperationWild;
import net.tiew.operationWild.entity.OWEntity;
import net.tiew.operationWild.event.ClientEvents;

import java.text.Collator;
import java.util.*;

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

    public static final List<EntityType<? extends OWEntity>> owEntities = new ArrayList<>(Arrays.asList(
            OWEntityRegistry.BOA.get(),
            OWEntityRegistry.TIGER.get(),
            OWEntityRegistry.PEACOCK.get(),
            OWEntityRegistry.TIGER_SHARK.get(),
            OWEntityRegistry.HYENA.get(),
            OWEntityRegistry.KODIAK.get(),
            OWEntityRegistry.RED_PANDA.get(),
            OWEntityRegistry.CHAMELEON.get(),
            OWEntityRegistry.JELLYFISH.get(),
            OWEntityRegistry.MANTA.get(),
            OWEntityRegistry.WALRUS.get(),
            OWEntityRegistry.ELEPHANT.get(),
            OWEntityRegistry.MANDRILL.get()
    ));
    public static final double[] THRESHOLDS = {
            TigerEntity.TAMING_EXPERIENCE, TigerSharkEntity.TAMING_EXPERIENCE, BoaEntity.TAMING_EXPERIENCE, PeacockEntity.TAMING_EXPERIENCE,
            HyenaEntity.TAMING_EXPERIENCE, KodiakEntity.TAMING_EXPERIENCE, RedPandaEntity.TAMING_EXPERIENCE, ChameleonEntity.TAMING_EXPERIENCE,
            JellyfishEntity.TAMING_EXPERIENCE, MantaEntity.TAMING_EXPERIENCE, WalrusEntity.TAMING_EXPERIENCE, ElephantEntity.TAMING_EXPERIENCE,
            MandrillEntity.TAMING_EXPERIENCE
    };

    private float xMouse;
    private float yMouse;
    protected int imageWidth = 256;
    protected int imageHeight = 193;

    private int actualPage = 1;
    private int maxPage = (owEntities.size() * 2) + 2;

    private int[] descriptionPages = {2, owEntities.size() + 2 - 1};
    private int[] tamingPages = {owEntities.size() + 2, maxPage - 1};
    private int miscPage = maxPage;

    private int spacement = 16;

    private int sortCode = 0;
    private boolean descendingOrder = false;

    private final Player player;
    public static List<String> newEntitiesDiscovered = new ArrayList<>();
    public static List<String> newEntitiesTamed = new ArrayList<>();

    public static double lastReachedThreshold = -1;

    private static OWEntity owEntity;

    public OWEntityJournalScreen() {
        super(Component.literal("Entity Journal"));
        this.minecraft = Minecraft.getInstance();

        this.player = minecraft.player;
    }

    private ResourceLocation getMobDescriptionTexture(String mobName) {
        return ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "textures/gui/mob_page/description/" + mobName + "_page.png");
    }

    private ResourceLocation getMobTamingTexture(String mobName) {
        return ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "textures/gui/mob_page/taming/" + mobName + "_page_taming.png");
    }

    private int adaptSpace(String entityType) {
        for (int i = 0; i < owEntities.size(); i++) {
            ResourceLocation entityId = BuiltInRegistries.ENTITY_TYPE.getKey(owEntities.get(i));
            String entityName = entityId.getPath();
            if (entityName.equals(entityType)) {
                return i;
            }
        }
        return 0;
    }

    private String getEntityName(EntityType<?> entityType) {
        ResourceLocation entityKey = BuiltInRegistries.ENTITY_TYPE.getKey(entityType);
        if (entityKey != null && entityKey.getNamespace().equals("ow")) {
            return entityKey.getPath();
        }
        return "";
    }

    public int getDescriptionPageForAnimal(String animal) {
        for (int i = 0; i < owEntities.size(); i++) {
            ResourceLocation entityId = BuiltInRegistries.ENTITY_TYPE.getKey(owEntities.get(i));
            String entityName = entityId.getPath();
            if (entityName.equals(animal)) {
                return i + 2;
            }
        }
        return 0;
    }

    public int getTamingPageForAnimal(String animal) {
        for (int i = 0; i < owEntities.size(); i++) {
            ResourceLocation entityId = BuiltInRegistries.ENTITY_TYPE.getKey(owEntities.get(i));
            String entityName = entityId.getPath();
            if (entityName.equals(animal)) {
                return i + 2 + ((maxPage - 2) / 2);
            }
        }
        return 0;
    }

    public EntityType<? extends OWEntity> getEntityTypeFromPage(int page) {
        if (page <= 1) {
            return null;
        }

        int halfPages = (maxPage - 2) / 2;

        int entityIndex;
        if (page <= 1 + halfPages) {
            entityIndex = page - 2;
        } else if (page <= 1 + (2 * halfPages)) {
            entityIndex = page - 2 - halfPages;
        } else {
            return null;
        }

        if (entityIndex < 0 || entityIndex >= owEntities.size()) {
            return null;
        }

        return owEntities.get(entityIndex);
    }

    private double getMaxTamingExp(String entityType) {
        switch (entityType) {
            case "boa": return BoaEntity.TAMING_EXPERIENCE;
            case "peacock": return PeacockEntity.TAMING_EXPERIENCE;
            case "tiger": return TigerEntity.TAMING_EXPERIENCE;
            case "tiger_shark": return TigerSharkEntity.TAMING_EXPERIENCE;
            case "hyena": return HyenaEntity.TAMING_EXPERIENCE;
            case "kodiak": return KodiakEntity.TAMING_EXPERIENCE;
            case "red_panda": return RedPandaEntity.TAMING_EXPERIENCE;
            case "chameleon": return ChameleonEntity.TAMING_EXPERIENCE;
            case "jellyfish": return JellyfishEntity.TAMING_EXPERIENCE;
            case "manta": return MantaEntity.TAMING_EXPERIENCE;
            case "walrus": return WalrusEntity.TAMING_EXPERIENCE;
            case "elephant": return ElephantEntity.TAMING_EXPERIENCE;
            case "mandrill": return MandrillEntity.TAMING_EXPERIENCE;
            default: return -1;
        }
    }

    @Override
    protected void init() {
        super.init();
        int i = (this.width - this.imageWidth) / 2;
        int j = (this.height - this.imageHeight) / 2;
        this.minecraft = Minecraft.getInstance();

        Collator collator = Collator.getInstance();
        collator.setStrength(Collator.PRIMARY);

        if (sortCode == 0) {
            owEntities.sort((entity1, entity2) -> {
                String translationKey1 = entity1.getDescriptionId();
                String translationKey2 = entity2.getDescriptionId();

                String translatedName1 = Component.translatable(translationKey1).getString();
                String translatedName2 = Component.translatable(translationKey2).getString();

                return descendingOrder ? collator.compare(translatedName2, translatedName1) : collator.compare(translatedName1, translatedName2);
            });
        } else if (sortCode == 1) {
            owEntities.sort((entity1, entity2) -> {
                String entityName1 = getEntityName(entity1);
                String entityName2 = getEntityName(entity2);

                double tamingExp1 = getMaxTamingExp(entityName1);
                double tamingExp2 = getMaxTamingExp(entityName2);

                return descendingOrder ? Double.compare(tamingExp2, tamingExp1) : Double.compare(tamingExp1, tamingExp2);
            });
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
            actualPage = 2 + ((maxPage - 2) / 2);
            return true;
        }
        if (isMouseInEntityArea(mouseX, mouseY, i + 193, j - 5, 38, 8)) {
            if (actualPage == miscPage) return false;
            this.minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.BOOK_PAGE_TURN, 1.0F));
            this.minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
            actualPage = miscPage;
            return true;
        }

        int centerX = i + (this.imageWidth / 2);
        int centerY = j + (this.imageHeight / 2);

        if (isMouseInEntityArea(xMouse, yMouse, centerX - (100 / 2), centerY + 100, 100, 13)) {
            if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
                sortCode = sortCode == 0 ? 1 : 0;
                this.minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.BOOK_PAGE_TURN, 1.0F));
                this.minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
                init();
                return true;
            } else if (button == GLFW.GLFW_MOUSE_BUTTON_RIGHT) {
                descendingOrder = !descendingOrder;
                this.minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.BOOK_PAGE_TURN, 1.0F));
                this.minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
                init();
                return true;
            }
        }


        for (int $$0 = 0; $$0 < owEntities.size(); $$0++) {
            int xPlacement = $$0 < 11 ? i - 36 : i + 254;
            int yPlacement = j + (spacement * (($$0 % 11) + 1));
            if (isMouseInEntityArea(mouseX, mouseY, xPlacement, yPlacement, 38, 13)) {
                int descriptionPage = $$0 + 2;
                int tamingPage = $$0 + 2 + ((maxPage - 2) / 2);

                if (actualPage == descriptionPage || actualPage == tamingPage || actualPage == miscPage) return false;
                this.minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.BOOK_PAGE_TURN, 1.0F));
                this.minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
                actualPage = (actualPage >= descriptionPages[0] && actualPage <= descriptionPages[1]) ? descriptionPage : tamingPage;
                return true;
            }
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    private boolean isMouseInEntityArea(double mouseX, double mouseY, int x, int y, int width, int height) {
        return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
    }

    private void createCategory(boolean isMainCategory, GuiGraphics graphics, int i, int j, int textX, int textY, String textComponent, boolean colorHighlighted, float scale, String entityType, boolean isTaming, boolean isLeftCorner) {
        Component text = Component.translatable(textComponent).setStyle(Style.EMPTY.withBold(true));
        int color;

        if (isTaming) {
            if (isMouseInEntityArea(xMouse, yMouse, i, j, 38, 13) || colorHighlighted) {
                graphics.blit(OW_ENTITY_JOURNAL_BUTTON_LOCATION, i, j, entityType != null ? ClientEvents.tamingExperience >= getMaxTamingExp(entityType) ? 0 : 38 : 0, entityType != null ? ClientEvents.tamingExperience >= getMaxTamingExp(entityType) ? 13 : (isLeftCorner && ClientEvents.tamingExperience < getMaxTamingExp(entityType)) ? 13 : 0 : 13, 38, 13);
                color = 0xf1dba6;
            } else {
                graphics.blit(OW_ENTITY_JOURNAL_BUTTON_LOCATION, i, j, entityType != null ? ClientEvents.tamingExperience >= getMaxTamingExp(entityType) ? 0 : 38 : 0, (isLeftCorner && entityType != null && ClientEvents.tamingExperience < getMaxTamingExp(entityType)) ? 13 : 0, 38, 13);
                color = 0x564c39;
            }
        } else {
            if (isMouseInEntityArea(xMouse, yMouse, i, j, 38, 13) || colorHighlighted) {
                graphics.blit(OW_ENTITY_JOURNAL_BUTTON_LOCATION, i, j, entityType != null ? ClientEvents.hasPlayerKilledOWEntity(player, entityType) ? 0 : 38 : 0, entityType != null ? ClientEvents.hasPlayerKilledOWEntity(player, entityType) ? 13 : (isLeftCorner && !ClientEvents.hasPlayerKilledOWEntity(player, entityType)) ? 13 : 0 : 13, 38, 13);
                color = 0xf1dba6;
            } else {
                graphics.blit(OW_ENTITY_JOURNAL_BUTTON_LOCATION, i, j, entityType != null ? ClientEvents.hasPlayerKilledOWEntity(player, entityType) ? 0 : 38 : 0, (isLeftCorner && !ClientEvents.hasPlayerKilledOWEntity(player, entityType)) ? 13 : 0, 38, 13);
                color = 0x564c39;
            }
        }

        if (colorHighlighted) color = 0xf1dba6;

        graphics.pose().pushPose();
        graphics.pose().scale(scale, scale, 1.0f);

        if (textComponent.equals("?")) {
            textX = 13;
        }

        int scaledX = (int) ((i + textX - ((float) this.font.width(text) / 2)) / scale);
        int scaledY = (int) ((j + textY) / scale);

        if (isMainCategory) graphics.drawString(this.font, text, scaledX, scaledY, color, false);
        else graphics.drawString(this.font, text, (int) ((i + textX) / scale), (int) ((j + textY) / scale), color, false);

        graphics.pose().popPose();
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        int i = (this.width - this.imageWidth) / 2;
        int j = (this.height - this.imageHeight) / 2;
        this.xMouse = mouseX;
        this.yMouse = mouseY;

        super.render(graphics, mouseX, mouseY, partialTick);

        boolean isInDescriptionCategory = actualPage >= descriptionPages[0] && actualPage <= descriptionPages[1];
        boolean isInTamingCategory = actualPage >= tamingPages[0] && actualPage <= tamingPages[1];

        EntityType<? extends OWEntity> entityType = getEntityTypeFromPage(actualPage);
        if (entityType != null) {
            try {
                owEntity = entityType.create(this.minecraft.level);
            } catch (Exception e) {
                owEntity = OWEntityRegistry.TIGER.get().create(this.minecraft.level);
            }
        }

        createCategory(true, graphics, i + 25, j - 5, 25, 4, "tooltip.menuBook", actualPage == 1, 0.6f, null, false, false);
        createCategory(true, graphics, i + 72, j - 5, 27, 4, "tooltip.entity", actualPage >= descriptionPages[0] && actualPage <= descriptionPages[1], 0.6f, null, false, false);
        createCategory(true, graphics, i + 143, j - 5, 31, 4, "tooltip.taming", actualPage >= tamingPages[0] && actualPage <= tamingPages[1], 0.6f, null, false, false);
        createCategory(true, graphics, i + 193, j - 5, 27, 4, "tooltip.other", actualPage >= miscPage, 0.6f, null, false, false);

        if (isInDescriptionCategory) {
            for (int $$0 = 0; $$0 < owEntities.size(); $$0++) {
                ResourceLocation entityId = BuiltInRegistries.ENTITY_TYPE.getKey(owEntities.get($$0));
                String entityName = entityId.getPath();

                boolean isTigerShark = entityName.contains("tiger_shark");
                boolean isChameleon = entityName.contains("chameleon");
                boolean isRedPanda = entityName.contains("red_panda");
                boolean isManta = entityName.contains("manta");

                int xPlacement = $$0 < 11 ? i - 36 : i + 254;
                int yPlacement = j + (spacement * (($$0 % 11) + 1));
                boolean isLeftCorner = $$0 < 11;

                if (isTigerShark && ClientEvents.hasPlayerKilledOWEntity(player, "tiger_shark")) createCategory(false, graphics, xPlacement, yPlacement, 5, 5, ClientEvents.hasPlayerKilledOWEntity(player, entityName) ? "entity.ow." + entityName : "?", actualPage == getDescriptionPageForAnimal(entityName), 0.43f, entityName, false, isLeftCorner);
                else if (isChameleon && ClientEvents.hasPlayerKilledOWEntity(player, "chameleon")) createCategory(false, graphics, xPlacement, yPlacement, 5, 5, ClientEvents.hasPlayerKilledOWEntity(player, entityName) ? "entity.ow." + entityName : "?", actualPage == getDescriptionPageForAnimal(entityName), 0.535f, entityName, false, isLeftCorner);
                else if (isRedPanda && ClientEvents.hasPlayerKilledOWEntity(player, "red_panda")) createCategory(false, graphics, xPlacement, yPlacement, 5, 5, ClientEvents.hasPlayerKilledOWEntity(player, entityName) ? "entity.ow." + entityName : "?", actualPage == getDescriptionPageForAnimal(entityName), 0.43f, entityName, false, isLeftCorner);
                else if (isManta && ClientEvents.hasPlayerKilledOWEntity(player, "manta")) createCategory(false, graphics, xPlacement, yPlacement, 5, 5, ClientEvents.hasPlayerKilledOWEntity(player, entityName) ? "entity.ow." + entityName : "?", actualPage == getDescriptionPageForAnimal(entityName), 0.45f, entityName, false, isLeftCorner);
                else createCategory(false, graphics, xPlacement, yPlacement, 5, 5, ClientEvents.hasPlayerKilledOWEntity(player, entityName) ? "entity.ow." + entityName : "?", actualPage == getDescriptionPageForAnimal(entityName), 0.6f, entityName, false, isLeftCorner);
            }
        }
        else if (isInTamingCategory) {
            for (int $0 = 0; $0 < owEntities.size(); $0++) {
                ResourceLocation entityId = BuiltInRegistries.ENTITY_TYPE.getKey(owEntities.get($0));
                String entityName = entityId.getPath();

                boolean isTigerShark = entityName.contains("tiger_shark");
                boolean isChameleon = entityName.contains("chameleon");
                boolean isRedPanda = entityName.contains("red_panda");
                boolean isManta = entityName.contains("manta");

                int xPlacement = $0 < 11 ? i - 36 : i + 254;
                int yPlacement = j + (spacement * (($0 % 11) + 1));
                boolean isLeftCorner = $0 < 11;

                if (isTigerShark) createCategory(false, graphics, xPlacement, yPlacement, 5, 5, ClientEvents.tamingExperience >= getMaxTamingExp(entityName) ? "entity.ow." + entityName : "?", actualPage == getTamingPageForAnimal(entityName), ClientEvents.tamingExperience >= TigerSharkEntity.TAMING_EXPERIENCE ? 0.43f : 0.6f, entityName, true, isLeftCorner);
                else if (isChameleon) createCategory(false, graphics, xPlacement, yPlacement, 5, 5, ClientEvents.tamingExperience >= getMaxTamingExp(entityName) ? "entity.ow." + entityName : "?", actualPage == getTamingPageForAnimal(entityName), ClientEvents.tamingExperience >= ChameleonEntity.TAMING_EXPERIENCE ? 0.535f : 0.6f, entityName, true, isLeftCorner);
                else if (isRedPanda) createCategory(false, graphics, xPlacement, yPlacement, 5, 5, ClientEvents.tamingExperience >= getMaxTamingExp(entityName) ? "entity.ow." + entityName : "?", actualPage == getTamingPageForAnimal(entityName), ClientEvents.tamingExperience >= RedPandaEntity.TAMING_EXPERIENCE ? 0.43f : 0.6f, entityName, true, isLeftCorner);
                else if (isManta) createCategory(false, graphics, xPlacement, yPlacement, 5, 5, ClientEvents.tamingExperience >= getMaxTamingExp(entityName) ? "entity.ow." + entityName : "?", actualPage == getTamingPageForAnimal(entityName), ClientEvents.tamingExperience >= MantaEntity.TAMING_EXPERIENCE ? 0.45f : 0.6f, entityName, true, isLeftCorner);
                else createCategory(false, graphics, xPlacement, yPlacement, 5, 5, ClientEvents.tamingExperience >= getMaxTamingExp(entityName) ? "entity.ow." + entityName : "?", actualPage == getTamingPageForAnimal(entityName), 0.6f, entityName, true, isLeftCorner);

            }
        }

        if (!newEntitiesDiscovered.isEmpty()) {
            for (int idx = 0; idx < newEntitiesDiscovered.size(); idx++) {
                String entity = newEntitiesDiscovered.get(idx);
                int entityPosition = adaptSpace(entity);
                boolean isLeftCorner = entityPosition < 11;

                int relativePosition = isLeftCorner ? entityPosition : (entityPosition - 11);

                if (actualPage >= descriptionPages[0] && actualPage <= descriptionPages[1]) {
                    graphics.blit(OW_ENTITY_JOURNAL_BUTTON_LOCATION,
                            isLeftCorner ? i - 41 : i + 294,
                            j + 16 + (spacement * relativePosition),
                            0, 26, 3, 11);
                } else {
                    graphics.blit(OW_ENTITY_JOURNAL_BUTTON_LOCATION,
                            i + 105,
                            j - 10 + (idx * 15),
                            0, 26, 3, 11);
                }
            }
            newEntitiesDiscovered.removeIf(entity -> actualPage == adaptSpace(entity) + 2);
        }

        if (!newEntitiesTamed.isEmpty()) {
            for (int idx = 0; idx < newEntitiesTamed.size(); idx++) {
                String entity = newEntitiesTamed.get(idx);
                int entityPosition = adaptSpace(entity);
                boolean isLeftCorner = entityPosition < 11;

                int relativePosition = isLeftCorner ? entityPosition : (entityPosition - 11);

                if (actualPage >= tamingPages[0] && actualPage <= tamingPages[1]) {
                    graphics.blit(OW_ENTITY_JOURNAL_BUTTON_LOCATION,
                            isLeftCorner ? i - 41 : i + 294,
                            j + 16 + (spacement * relativePosition),
                            0, 26, 3, 11);
                } else {
                    graphics.blit(OW_ENTITY_JOURNAL_BUTTON_LOCATION,
                            i + 176,
                            j - 10,
                            0, 26, 3, 11);
                }
            }
            newEntitiesTamed.removeIf(entity -> actualPage == adaptSpace(entity) + 2);
        }

        if (newEntitiesTamed != null && !newEntitiesTamed.isEmpty()) {
            if (owEntity != null) {
                String entityId = BuiltInRegistries.ENTITY_TYPE.getKey(owEntity.getType()).getPath();
                newEntitiesTamed.remove(entityId);
            }
        }

        graphics.blit(OW_ENTITY_JOURNAL_INTERFACE_LOCATION, i, j, 0, 0, this.imageWidth, this.imageHeight);

        graphics.pose().pushPose();
        graphics.pose().scale(0.8f, 0.8f, 0.8f);
        graphics.drawString(this.font, String.valueOf(actualPage + " / " + maxPage), (int) ((i + (actualPage >= 10 ? 112 : 115)) / 0.8f), (int) ((j - 7) / 0.8f), 0xb69d77);
        graphics.pose().popPose();

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

        if (actualPage < miscPage) {
        if (isMouseInEntityArea(mouseX, mouseY, i + 219, j + 160, 23, 13)) {
            graphics.blitSprite(ARROW_FORWARD_HIGHLIGHTED, i + 219, j + 160, 23, 13);
        } else graphics.blitSprite(ARROW_FORWARD, i + 219, j + 160, 23, 13);
        }

        if (actualPage > 1) {
            if (isMouseInEntityArea(mouseX, mouseY, i + 13, j + 160, 23, 13)) {
                graphics.blitSprite(ARROW_BACKWARD_HIGHLIGHTED, i + 13, j + 160, 23, 13);
            } else graphics.blitSprite(ARROW_BACKWARD, i + 13, j + 160, 23, 13);
        }
    }

    private void showPage(GuiGraphics graphics, int i, int j) {
        if (player == null || owEntity == null || actualPage == 1) return;

        ResourceLocation entityId = BuiltInRegistries.ENTITY_TYPE.getKey(owEntity.getType());
        String entityName = entityId.getPath();

        boolean isInDescriptionCategory = actualPage >= descriptionPages[0] && actualPage <= descriptionPages[1];
        boolean isInTamingCategory = actualPage >= tamingPages[0] && actualPage <= tamingPages[1];

        if (actualPage != miscPage) {
            if (isInDescriptionCategory) {
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
                } else {
                    graphics.blit(OW_ENTITY_JOURNAL_INTERFACE_TORN_OUT_LOCATION, i, j, 0, 0, this.imageWidth, this.imageHeight);
                    Component text = Component.translatable("tooltip.noDiscovered");
                    graphics.drawString(this.font, text, i + 128 - (this.font.width(text) / 2), j + 70, 0xb69d77, false);
                }
            }

            if (isInTamingCategory) {
                if (ClientEvents.tamingExperience >= getMaxTamingExp(entityName)) {
                    ResourceLocation tamingTexture = getMobTamingTexture(entityName);
                    if (Minecraft.getInstance().getResourceManager().getResource(tamingTexture).isPresent()) {
                        graphics.blit(tamingTexture, i, j, 0, 0, this.imageWidth, this.imageHeight);
                    } else {
                        graphics.blit(getMobDescriptionTexture(entityName), i, j, 0, 0, this.imageWidth, this.imageHeight);
                    }

                    String tooltip = switch (entityName) {
                        case "boa" -> "tooltip.tamingOf";
                        case "tiger" -> "tooltip.tamingOf";
                        case "tiger_shark" -> "tooltip.tamingOf";
                        case "peacock" -> "tooltip.tamingOf";
                        case "hyena" -> "tooltip.tamingOfA";
                        case "kodiak" -> "tooltip.tamingOf";
                        case "red_panda" -> "tooltip.tamingOf";
                        case "chameleon" -> "tooltip.tamingOf";
                        case "jellyfish" -> "tooltip.tamingOfA";
                        case "manta" -> "tooltip.tamingOfA";
                        case "walrus" -> "tooltip.tamingOf";
                        case "elephant" -> "tooltip.taming2";
                        case "mandrill" -> "tooltip.tamingOf";
                        default -> "tooltip.tamingOf";
                    };

                    Component title = Component.translatable(tooltip).setStyle(Style.EMPTY.withBold(true))
                            .append(" ")
                            .append(Component.translatable("entity.ow." + entityName).setStyle(Style.EMPTY.withBold(true)));

                    boolean isTigerShark = entityName.equals("tiger_shark");
                    boolean isHyena = entityName.equals("hyena");
                    boolean isChameleon = entityName.equals("chameleon");
                    boolean isKodiak = entityName.equals("kodiak");
                    boolean isWalrus = entityName.equals("walrus");
                    boolean isElephant = entityName.equals("elephant");
                    boolean isManta = entityName.equals("manta");
                    boolean isJellyfish = entityName.equals("jellyfish");
                    boolean isRedPanda = entityName.equals("red_panda");
                    boolean isMandrill = entityName.equals("mandrill");

                    int centerX = i + (this.imageWidth / 2);
                    int centerY = j + (this.imageHeight / 2);
                    float scale = 1.0f;

                    if (isTigerShark || isManta || isRedPanda) scale = 0.7f;
                    if (isHyena || isChameleon || isJellyfish || isElephant || isMandrill) scale = 0.8f;
                    if (isKodiak || isWalrus) scale = 0.9f;


                    graphics.pose().pushPose();
                    graphics.pose().scale(scale, scale, scale);
                    graphics.drawString(this.font, title, (int) (((centerX - 62) / scale) - (this.font.width(title) / 2.0f)), (int) ((centerY - 80) / scale), 0xb69d77, false);                    graphics.pose().popPose();

                    showText(graphics, i, j);
                } else {
                    graphics.blit(OW_ENTITY_JOURNAL_INTERFACE_TORN_OUT_LOCATION, i, j, 0, 0, this.imageWidth, this.imageHeight);
                    Component text = Component.translatable("tooltip.tamingNoDiscovered");
                    graphics.drawString(this.font, text, i + 128 - (this.font.width(text) / 2), j + 70, 0xb69d77, false);
                }
            }
        } else graphics.blit(OW_ENTITY_JOURNAL_TAMING_EXPERIENCE_LOCATION, i, j, 0, 0, this.imageWidth, this.imageHeight);

        if (isInDescriptionCategory || isInTamingCategory) {
            int centerX = i + (this.imageWidth / 2);
            int centerY = j + (this.imageHeight / 2);
            Component title = Component.translatable("tooltip.orderBy").setStyle(Style.EMPTY.withBold(true))
                    .append(" ")
                    .append(Component.translatable(sortCode == 0 ? "tooltip.name" : "tooltip.tamingExperience")).setStyle(Style.EMPTY.withBold(true))
                    .append(Component.literal(descendingOrder ? " (-)" : " (+)")).setStyle(Style.EMPTY.withBold(true));

            float scale = 0.6f;

            graphics.blit(OW_ENTITY_JOURNAL_BUTTON_LOCATION, centerX - (100 / 2), centerY + 100, 0, isMouseInEntityArea(xMouse, yMouse, centerX - (100 / 2), centerY + 100, 100, 13) ? 50 : 37, 100, 13);

            graphics.pose().pushPose();
            graphics.pose().scale(scale, scale, scale);

            graphics.drawString(this.font, title, (int) ((centerX / scale) - ((float) this.font.width(title) / 2)), (int) ((centerY / scale) + 174), isMouseInEntityArea(xMouse, yMouse, centerX - (100 / 2), centerY + 100, 100, 13) ? 0xf1dba6 : 0x564c39, false);

            graphics.pose().popPose();
        }
    }

    private void showText(GuiGraphics graphics, int i, int j) {
        boolean isInTamingCategory = actualPage >= tamingPages[0] && actualPage <= tamingPages[1];

        boolean isEnglish = Minecraft.getInstance().getLanguageManager().getSelected().equals("en_us");
        boolean isFrench = Minecraft.getInstance().getLanguageManager().getSelected().equals("fr_fr");

        int firstPageX = -107;
        int secondPageX = 15;

        if (!isInTamingCategory) {
            if (owEntity instanceof BoaEntity) {
                createParagraph(graphics, i, j, "boa.page1", firstPageX + 34, -55, 115);
                createParagraph(graphics, i, j, "boa.page2", firstPageX, 45, 160);
                createParagraph(graphics, i, j, "boa.page3", secondPageX, -80, 160);
                createParagraph(graphics, i, j, "boa.page4", secondPageX, -35, 160);
            } else if (owEntity instanceof PeacockEntity) {
                createParagraph(graphics, i, j, "peacock.page1", firstPageX, -55, 160);
                createParagraph(graphics, i, j, "peacock.page2", firstPageX, -15, 160);
                createParagraph(graphics, i, j, "peacock.page3", secondPageX, 20, 160);
                createParagraph(graphics, i, j, "peacock.page4", secondPageX, 55, 160);
            } else if (owEntity instanceof TigerEntity) {
                createParagraph(graphics, i, j, "tiger.page1", firstPageX, -55, 160);
                createParagraph(graphics, i, j, "tiger.page2", firstPageX, -7, 160);
                createParagraph(graphics, i, j, "tiger.page3", secondPageX, !isFrench ? 0 : -5, 160);
                createParagraph(graphics, i, j, "tiger.page4", secondPageX, !isFrench ? 45 : 50, 160);
            } else if (owEntity instanceof TigerSharkEntity) {
                createParagraph(graphics, i, j, "tiger_shark.page1", firstPageX, -55, 160);
                createParagraph(graphics, i, j, "tiger_shark.page2", firstPageX + 40, 3, 100);
                createParagraph(graphics, i, j, "tiger_shark.page3", secondPageX,-18, 160);
                createParagraph(graphics, i, j, "tiger_shark.page4", secondPageX,!isFrench ? 29 : 32, 160);
                createParagraph(graphics, i, j, "tiger_shark.page5", secondPageX,65, 160);
            }
        } else {
            if (owEntity instanceof BoaEntity) {
                createParagraph(graphics, i, j, "boa.taming.page1", firstPageX, -55, 160);
                createParagraph(graphics, i, j, "boa.taming.page2", secondPageX, -80, 160);
                createParagraph(graphics, i, j, "boa.taming.page3", secondPageX, !isFrench ? 10 : 0, 160);
                createParagraph(graphics, i, j, "boa.taming.page4", secondPageX, 65, 160);
            } else if (owEntity instanceof PeacockEntity) {
                createParagraph(graphics, i, j, "peacock.taming.page1", firstPageX, -55, 80);
                createParagraph(graphics, i, j, "peacock.taming.page2", firstPageX, 30, 100);
                createParagraph(graphics, i, j, "peacock.taming.page3", secondPageX, -80, 160);
                createParagraph(graphics, i, j, "peacock.taming.page4", secondPageX, -45, 160);
                createParagraph(graphics, i, j, "peacock.taming.page5", secondPageX, 0, 160);
            } else if (owEntity instanceof TigerEntity) {
                createParagraph(graphics, i, j, "tiger.taming.page1", firstPageX, -55, 160);
                createParagraph(graphics, i, j, "tiger.taming.page2", firstPageX, -30, 160);
                createParagraph(graphics, i, j, "tiger.taming.page3", secondPageX, 0, 160);
                createParagraph(graphics, i, j, "tiger.taming.page4", secondPageX, 40, 160);
            }
        }
    }

    private void createParagraph(GuiGraphics graphics, int offsetX, int offsetY, String textTranslation, int xOffset, int yOffset, int maxWidth) {
        int centerX = offsetX + (this.imageWidth / 2);
        int centerY = offsetY + (this.imageHeight / 2);

        Component text1 = Component.translatable(textTranslation).setStyle(Style.EMPTY);

        graphics.pose().pushPose();

        int finalX = centerX + xOffset;
        int finalY = centerY + yOffset;

        graphics.pose().scale(0.6f, 0.6f, 1.0f);

        int scaledX = (int)(finalX / 0.6f);
        int scaledY = (int)(finalY / 0.6f);

        int lineHeight = 10;

        renderText(graphics, text1.getString(), scaledX, 0, scaledY, maxWidth, lineHeight, 0x745f41);

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
        } else if (actualPage == 6 && ClientEvents.tamingExperience >= BoaEntity.TAMING_EXPERIENCE) {
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

        }*/ else if (actualPage == miscPage) {
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