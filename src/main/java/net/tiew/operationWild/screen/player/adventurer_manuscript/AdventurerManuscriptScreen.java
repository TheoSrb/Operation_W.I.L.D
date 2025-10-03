package net.tiew.operationWild.screen.player.adventurer_manuscript;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.tiew.operationWild.OperationWild;
import net.tiew.operationWild.core.OWUtils;
import net.tiew.operationWild.entity.OWEntity;
import net.tiew.operationWild.entity.OWEntityRegistry;
import net.tiew.operationWild.entity.client.model.AdventurerManuscriptModel;
import net.tiew.operationWild.entity.misc.AdventurerManuscript;
import net.tiew.operationWild.event.ClientEvents;
import net.tiew.operationWild.screen.player.adventurer_manuscript.chapter.OWChapter;
import net.tiew.operationWild.screen.player.adventurer_manuscript.chapter.OWChapters;
import org.lwjgl.glfw.GLFW;

import java.text.Collator;
import java.util.*;

public class AdventurerManuscriptScreen extends Screen {

    public static String leftChapterPage = "";
    public static String LEFT_PAGE;
    public static String RIGHT_PAGE;
    public static String NEXT_LEFT_PAGE;
    public static String NEXT_RIGHT_PAGE;
    public static String PREVIOUS_LEFT_PAGE;
    public static String PREVIOUS_RIGHT_PAGE;

    public static final float BOOK_ROTATION = -20.0f;

    private float xMouse;
    private float yMouse;
    protected int imageWidth = 256;
    protected int imageHeight = 193;

    private int actualPage = 1;
    private int maxPage;
    private int previousPage = 1;

    private AdventurerManuscript cachedBookEntity;

    private long initTime = 0;
    private float totalElapsedTime = 0.0f;
    private float animationSpeed = 1.0f;

    private float oscillationTimer = 0;
    private float oscillationSpeed = 0.1f;
    private float oscillationAmplitude = 5;
    private float oscillationFrequency = 0.25f;

    private long pageCooldownStartTime = 0;
    private long pageCooldownDuration = 1400;

    private long fadeStartTime = 0;
    private boolean isFading = false;
    private String targetLeftPage = "";
    private String currentLeftPage = "";
    private long fadeDuration = 1;

    private EntityType<? extends OWEntity> lastEntityTypeFound = null;

    public static Map<EntityType<? extends OWEntity>, Integer> OW_ENTITIES = new LinkedHashMap<>();
    public static Map<EntityType<? extends OWEntity>, Integer> tempMap = new HashMap<>();

    public AdventurerManuscriptScreen() {
        super(Component.literal(""));
    }

    public static InteractionResult addEntityToManuscript(EntityType<? extends OWEntity> entityType, int page, Player player) {
        if (tempMap.containsKey(entityType) || OW_ENTITIES.containsKey(entityType)) return InteractionResult.PASS;

        tempMap.put(entityType, page);

        Collator collator = Collator.getInstance();
        collator.setStrength(Collator.PRIMARY);

        tempMap.entrySet().stream()
                .sorted((entry1, entry2) -> {
                    String translationKey1 = entry1.getKey().getDescriptionId();
                    String translationKey2 = entry2.getKey().getDescriptionId();

                    String translatedName1 = Component.translatable(translationKey1).getString();
                    String translatedName2 = Component.translatable(translationKey2).getString();

                    return collator.compare(translatedName1, translatedName2);
                });

        ClientEvents.isNotifiedOWBook = true;
        player.playSound(SoundEvents.EXPERIENCE_ORB_PICKUP);


        if (player.level().isClientSide()) {
            OWEntity entity = entityType.create(Minecraft.getInstance().level);

            player.displayClientMessage(Component.translatable("tooltip.newEntity",
                    Component.translatable(String.valueOf(entityType)).setStyle(Style.EMPTY.withBold(true).withColor(entity != null ? entity.getEntityColor() : 0xFFFFFF))), true);

            ManuscriptPersistence.saveClient();
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public void tick() {
        super.tick();
        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;

        if (player != null) {
            long window = mc.getWindow().getWindow();
            if (InputConstants.isKeyDown(window, GLFW.GLFW_KEY_RIGHT) || InputConstants.isKeyDown(window, GLFW.GLFW_KEY_LEFT)) {
                pageCooldownDuration /= 1.04f;
            } else pageCooldownDuration = 1400;
        }

        pageCooldownDuration = Math.max(pageCooldownDuration, 200);
        fadeDuration = 1;

        oscillationTimer += oscillationSpeed;


        if (actualPage > maxPage) {
            actualPage = maxPage;
        }

        if (actualPage < 1) {
            actualPage = 1;
        }
    }

    private float getOscillationValue() {
        return (float) (oscillationAmplitude * Math.sin(oscillationFrequency * oscillationTimer));
    }

    @Override
    public void onClose() {
        ManuscriptPersistence.saveClient();
        super.onClose();
        OW_ENTITIES.clear();
        LEFT_PAGE = null;
        RIGHT_PAGE = null;
        NEXT_LEFT_PAGE = null;
        NEXT_RIGHT_PAGE = null;
        PREVIOUS_LEFT_PAGE = null;
        PREVIOUS_RIGHT_PAGE = null;
        leftChapterPage = null;
    }

    @Override
    protected void init() {
        super.init();
        ManuscriptPersistence.loadClient();
        Collator collator = Collator.getInstance();
        collator.setStrength(Collator.PRIMARY);

        tempMap.entrySet().stream()
                .sorted((entry1, entry2) -> {
                    String translationKey1 = entry1.getKey().getDescriptionId();
                    String translationKey2 = entry2.getKey().getDescriptionId();

                    String translatedName1 = Component.translatable(translationKey1).getString();
                    String translatedName2 = Component.translatable(translationKey2).getString();

                    return collator.compare(translatedName1, translatedName2);
                })
                .forEach(entry -> OW_ENTITIES.put(entry.getKey(), entry.getValue()));

        if (this.cachedBookEntity == null) {
            this.cachedBookEntity = new AdventurerManuscript(
                    OWEntityRegistry.ADVENTURER_MANUSCRIPT.get(),
                    Minecraft.getInstance().level
            );
            this.cachedBookEntity.tickCount = 1;
        }

        this.initTime = System.currentTimeMillis();
        this.totalElapsedTime = 0.0f;
        this.fadeStartTime = 0;
        this.isFading = false;
        this.pageCooldownStartTime = System.currentTimeMillis();

        this.maxPage = getAllAnimalPages();
        updatePageContent();
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private void updateTime(float partialTick) {
        long currentTime = System.currentTimeMillis();
        float elapsedSeconds = (currentTime - this.initTime) / 1000.0f;
        this.totalElapsedTime = elapsedSeconds * animationSpeed;
    }

    private boolean isPageCooldownActive() {
        if (this.pageCooldownStartTime == 0) return false;
        return (System.currentTimeMillis() - this.pageCooldownStartTime) < pageCooldownDuration;
    }

    private void updatePageContent() {
        String newLeftPage = "";

        EntityType<? extends OWEntity> entityType = getChapterPageAnimal(actualPage);

        if (entityType != null) {
            String baseTexturePath = "textures/gui/adventurer_manuscript/" + entityType.toString().split("entity.ow.")[1] + "_chapter.png";
            ResourceLocation baseTexture = ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, baseTexturePath);
            newLeftPage = baseTexture.toString();
        }

        if (!newLeftPage.equals(currentLeftPage)) {
            targetLeftPage = newLeftPage;
            isFading = true;
            fadeStartTime = System.currentTimeMillis();
        }

    }

    private int getChapterPage(EntityType<? extends OWEntity> entityType) {
        int currentPage = 1;

        for (Map.Entry<EntityType<? extends OWEntity>, Integer> entry : OW_ENTITIES.entrySet()) {
            if (entry.getKey().equals(entityType)) {
                return currentPage;
            }
            currentPage += entry.getValue();
        }

        return -1;
    }

    private int getAllAnimalPages() {
        int pages = 0;
        for (Integer $$0 : OW_ENTITIES.values()) {
            pages += $$0;
        }
        return pages;
    }

    public EntityType<? extends OWEntity> getChapterPageAnimal(int chapterStartPage) {
        int currentPage = 1;

        for (Map.Entry<EntityType<? extends OWEntity>, Integer> entry : OW_ENTITIES.entrySet()) {
            if (currentPage == chapterStartPage) {
                return entry.getKey();
            }
            currentPage += entry.getValue();
        }

        return null;
    }

    private void nextPage() {
        if (actualPage < maxPage && !isPageCooldownActive()) {

            previousPage = actualPage;

            Timer timer = new Timer();

            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    actualPage++;
                    OWChapter.resetPageTexts();
                    updatePageContent();
                }
            }, pageCooldownDuration);

            this.minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.BOOK_PAGE_TURN, 1.0F));

            this.pageCooldownStartTime = System.currentTimeMillis();

            if (this.cachedBookEntity != null) {
                this.cachedBookEntity.nextPageAnimationState.stop();
                this.cachedBookEntity.nextPageAnimationState.startIfStopped(this.cachedBookEntity.tickCount);
            }
        }
    }

    private void precedentPage() {
        if (actualPage > 1 && !isPageCooldownActive()) {
            OWChapter.resetPageTexts();

            previousPage = actualPage;

            Timer timer = new Timer();

            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    actualPage--;
                    OWChapter.resetPageTexts();
                    updatePageContent();
                }
            }, pageCooldownDuration);

            this.minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.BOOK_PAGE_TURN, 1.0F));

            this.pageCooldownStartTime = System.currentTimeMillis();

            if (this.cachedBookEntity != null) {
                this.cachedBookEntity.precedentPageAnimationState.stop();
                this.cachedBookEntity.precedentPageAnimationState.startIfStopped(this.cachedBookEntity.tickCount);
            }
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

    private float getFadeAlpha() {
        if (!isFading) return 1.0f;

        long currentTime = System.currentTimeMillis();
        long elapsedTime = currentTime - fadeStartTime;

        if (elapsedTime < fadeDuration) {
            return 1.0f - (elapsedTime / (float) fadeDuration);
        } else if (elapsedTime < fadeDuration * 2) {
            return (elapsedTime - fadeDuration) / (float) fadeDuration;
        }

        return 1.0f;
    }


    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        int i = (this.width - this.imageWidth) / 2;
        int j = (this.height - this.imageHeight) / 2;
        this.xMouse = mouseX;
        this.yMouse = mouseY;


        EntityType<? extends OWEntity> entityType = getChapterPageAnimal(actualPage);
        EntityType<? extends OWEntity> entityTypeNext = getChapterPageAnimal(actualPage + 1);
        EntityType<? extends OWEntity> entityTypePrevious = getChapterPageAnimal(actualPage - 1);
        OWEntity entity = null;
        if (entityType != null) {
            entity = entityType.create(Minecraft.getInstance().level != null ? Minecraft.getInstance().level : null);
            lastEntityTypeFound = entityType;
        }

        updateTime(partialTick);

        if (this.cachedBookEntity != null) {
            this.cachedBookEntity.tickCount = (int)(this.totalElapsedTime * 20);

            if (this.cachedBookEntity.level().isClientSide()) this.cachedBookEntity.setupAnimationState();
        }

        if (isFading) {
            long currentTime = System.currentTimeMillis();
            long elapsedTime = currentTime - fadeStartTime;

            if (elapsedTime == fadeDuration || (elapsedTime > fadeDuration && currentLeftPage != targetLeftPage)) {
                currentLeftPage = targetLeftPage;
                leftChapterPage = currentLeftPage;
            } else if (elapsedTime >= fadeDuration * 2) {
                isFading = false;
            }
        }

        super.render(guiGraphics, mouseX, mouseY, partialTick);

        int screenCenterX = this.width / 2;
        int screenCenterY = (this.height / 2) - 55;

        int modelRadius = 50;

        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(0, (getOscillationValue() / 1.5f) - 5, 0);
        renderModel(guiGraphics, screenCenterX - modelRadius, (screenCenterY - modelRadius), screenCenterX + modelRadius, (int)(screenCenterY + modelRadius), 130, partialTick);

        if (entityType != null || entityTypeNext != null || entityTypePrevious != null) {
            OWChapters.render(0, 0, 1.0f, getFadeAlpha(), entityType, entityTypeNext, entityTypePrevious);
        }

        guiGraphics.pose().popPose();

        Component page = Component.literal(String.valueOf(Math.max(Math.min(maxPage, actualPage), 1) + " / " + maxPage));
        guiGraphics.drawString(this.font, page, screenCenterX - (this.font.width(page) / 2), screenCenterY + 165, 0xa2956e);
    }

    @Override
    public void removed() {
        ManuscriptPersistence.saveClient();
        super.removed();
        this.cachedBookEntity = null;
        this.totalElapsedTime = 0;
        this.initTime = 0;
    }

    private void handleChapterText(EntityType<? extends OWEntity> entityType, int x, int y, float alpha) {
        if (entityType == OWEntityRegistry.TIGER.get()) {
            int page = getChapterPageOf(OWEntityRegistry.TIGER.get(), actualPage);
            OWChapters.TigerChapter.render(0, 0, 1f, alpha, page);
        }
        if (entityType == OWEntityRegistry.BOA.get()) {
            int page = getChapterPageOf(OWEntityRegistry.BOA.get(), actualPage);
            OWChapters.BoaChapter.render(0, 0, 1f, alpha, page);
        }
        if (entityType == OWEntityRegistry.KODIAK.get()) {
            int page = getChapterPageOf(OWEntityRegistry.KODIAK.get(), actualPage);
            OWChapters.KodiakChapter.render(0, 0, 1f, alpha, page);
        }
    }

    private int getChapterPageOf(EntityType<? extends OWEntity> entityType, int actualPage) {
        for (Map.Entry<EntityType<? extends OWEntity>, Integer> entry : OW_ENTITIES.entrySet()) {
            if (entry.getKey() == entityType) {
                if (actualPage < getChapterPage(entry.getKey()) || actualPage > (getChapterPage(entry.getKey()) + entry.getValue())) return -1;
                return (actualPage - getChapterPage(entry.getKey())) + 1;
            }
        }
        return -1;
    }

    private void renderModel(GuiGraphics guiGraphics, int x1, int y1, int x2, int y2, float scale, float partialTick) {
        float centerX = (float) (x1 + x2) / 2.0F;
        float centerY = (float) (y1 + y2) / 2.0F;

        EntityModelSet modelSet = Minecraft.getInstance().getEntityModels();

        AdventurerManuscriptModel<AdventurerManuscript> bookModel = new AdventurerManuscriptModel<>(
                modelSet.bakeLayer(AdventurerManuscriptModel.LAYER_LOCATION)
        );

        ResourceLocation bookTexture = ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "textures/entity/adventurer_manuscript.png");

        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(centerX, centerY, 100.0F);
        guiGraphics.pose().scale(scale, scale, -scale);
        guiGraphics.pose().mulPose(Axis.XP.rotationDegrees(BOOK_ROTATION));

        Lighting.setupForEntityInInventory();
        RenderSystem.setShaderTexture(0, bookTexture);
        VertexConsumer vertexConsumer = guiGraphics.bufferSource().getBuffer(RenderType.entityCutoutNoCull(bookTexture));

        float ageInTicks = (this.totalElapsedTime * 20);

        if (this.cachedBookEntity != null) {
            this.cachedBookEntity.openAnimationState.startIfStopped(this.cachedBookEntity.tickCount);
            bookModel.setupAnim(this.cachedBookEntity, 0.0F, 0.0F, ageInTicks, 0.0F, 0.0F);
        }

        bookModel.renderToBuffer(guiGraphics.pose(), vertexConsumer, 15728880, OverlayTexture.NO_OVERLAY, 0xFFFFFF);
        int screenCenterX = this.width / 2;
        int screenCenterY = (this.height / 2) - 55;
        handleChapterText(lastEntityTypeFound, (int) (screenCenterX / 0.8f), (int) (screenCenterY / 0.8f), getFadeAlpha());

        float alpha = getFadeAlpha();
        int alphaValue = (int)(alpha * 187);
        int color = (alphaValue << 24) | 0xFFFFFF;


        if (leftChapterPage != null && !leftChapterPage.equals("")) {
            ResourceLocation leftOverlayTexture = ResourceLocation.parse(leftChapterPage);
            RenderSystem.setShaderTexture(0, leftOverlayTexture);
            VertexConsumer leftOverlayConsumer = guiGraphics.bufferSource().getBuffer(RenderType.entityTranslucent(leftOverlayTexture));

            bookModel.renderToBuffer(guiGraphics.pose(), leftOverlayConsumer, 15728880, OverlayTexture.NO_OVERLAY, color);
        }

        if (RIGHT_PAGE != null && !RIGHT_PAGE.equals("")) {
            ResourceLocation rightOverlayTexture = ResourceLocation.parse(RIGHT_PAGE);
            RenderSystem.setShaderTexture(0, rightOverlayTexture);
            VertexConsumer rightOverlayConsumer = guiGraphics.bufferSource().getBuffer(RenderType.entityTranslucent(rightOverlayTexture));

            bookModel.renderToBuffer(guiGraphics.pose(), rightOverlayConsumer, 15728880, OverlayTexture.NO_OVERLAY, color);
        }

        if (LEFT_PAGE != null && !LEFT_PAGE.equals("")) {
            ResourceLocation rightOverlayTexture = ResourceLocation.parse(LEFT_PAGE);
            RenderSystem.setShaderTexture(0, rightOverlayTexture);
            VertexConsumer rightOverlayConsumer = guiGraphics.bufferSource().getBuffer(RenderType.entityTranslucent(rightOverlayTexture));

            bookModel.renderToBuffer(guiGraphics.pose(), rightOverlayConsumer, 15728880, OverlayTexture.NO_OVERLAY, color);
        }

        if (NEXT_LEFT_PAGE != null && !NEXT_LEFT_PAGE.equals("")) {
            ResourceLocation rightOverlayTexture = ResourceLocation.parse(NEXT_LEFT_PAGE);
            RenderSystem.setShaderTexture(0, rightOverlayTexture);
            VertexConsumer rightOverlayConsumer = guiGraphics.bufferSource().getBuffer(RenderType.entityTranslucent(rightOverlayTexture));

            bookModel.renderToBuffer(guiGraphics.pose(), rightOverlayConsumer, 15728880, OverlayTexture.NO_OVERLAY, color);
        }

        if (NEXT_RIGHT_PAGE != null && !NEXT_RIGHT_PAGE.equals("")) {
            ResourceLocation rightOverlayTexture = ResourceLocation.parse(NEXT_RIGHT_PAGE);
            RenderSystem.setShaderTexture(0, rightOverlayTexture);
            VertexConsumer rightOverlayConsumer = guiGraphics.bufferSource().getBuffer(RenderType.entityTranslucent(rightOverlayTexture));

            bookModel.renderToBuffer(guiGraphics.pose(), rightOverlayConsumer, 15728880, OverlayTexture.NO_OVERLAY, color);
        }

        if (PREVIOUS_LEFT_PAGE != null && !PREVIOUS_LEFT_PAGE.equals("")) {
            ResourceLocation rightOverlayTexture = ResourceLocation.parse(PREVIOUS_LEFT_PAGE);
            RenderSystem.setShaderTexture(0, rightOverlayTexture);
            VertexConsumer rightOverlayConsumer = guiGraphics.bufferSource().getBuffer(RenderType.entityTranslucent(rightOverlayTexture));

            bookModel.renderToBuffer(guiGraphics.pose(), rightOverlayConsumer, 15728880, OverlayTexture.NO_OVERLAY, color);
        }

        if (PREVIOUS_RIGHT_PAGE != null && !PREVIOUS_RIGHT_PAGE.equals("")) {
            ResourceLocation rightOverlayTexture = ResourceLocation.parse(PREVIOUS_RIGHT_PAGE);
            RenderSystem.setShaderTexture(0, rightOverlayTexture);
            VertexConsumer rightOverlayConsumer = guiGraphics.bufferSource().getBuffer(RenderType.entityTranslucent(rightOverlayTexture));

            bookModel.renderToBuffer(guiGraphics.pose(), rightOverlayConsumer, 15728880, OverlayTexture.NO_OVERLAY, color);
        }

        guiGraphics.flush();
        guiGraphics.pose().popPose();

        Lighting.setupFor3DItems();
    }
}