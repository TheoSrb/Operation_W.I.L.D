package net.tiew.operationWild.screen.player;

import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
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
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.EntityType;
import net.tiew.operationWild.OperationWild;
import net.tiew.operationWild.entity.OWEntity;
import net.tiew.operationWild.entity.OWEntityRegistry;
import net.tiew.operationWild.entity.client.model.AdventurerManuscriptModel;
import net.tiew.operationWild.entity.misc.AdventurerManuscript;
import org.lwjgl.glfw.GLFW;

import java.text.Collator;
import java.util.*;

public class AdventurerManuscriptScreen extends Screen {

    public static String LEFT_PAGE = "";
    public static String RIGHT_PAGE = "";

    public static final float BOOK_ROTATION = -20.0f;

    private float xMouse;
    private float yMouse;
    protected int imageWidth = 256;
    protected int imageHeight = 193;

    private int actualPage = 1;
    private int maxPage = 100;
    private int previousPage = 1;

    private AdventurerManuscript cachedBookEntity;
    private int guiOpenTime = 0;

    private float animationTime = 0.0f;
    private static final float ANIMATION_SPEED = 1.0f;

    private int pageCooldown = 0;
    private int pageCooldownMax = 40;

    private int fadeTimer = 0;
    private boolean isFading = false;
    private String targetLeftPage = "";
    private String currentLeftPage = "";
    private static final int FADE_DURATION = 20;

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

    public HashMap<EntityType<? extends OWEntity>, Integer> ENTITIES = new HashMap<>();

    public AdventurerManuscriptScreen() {
        super(Component.literal(""));
    }

    private void assignEntityToHisPage() {
        for (int i = 0; i < owEntities.size(); i++) {
            ENTITIES.put(owEntities.get(i), i + 1);
        }
    }

    @Override
    protected void init() {
        super.init();
        if (this.cachedBookEntity == null) {
            this.cachedBookEntity = new AdventurerManuscript(
                    OWEntityRegistry.ADVENTURER_MANUSCRIPT.get(),
                    Minecraft.getInstance().level
            );
            this.cachedBookEntity.tickCount = 1;
        }
        this.guiOpenTime = 0;
        this.animationTime = 0.0f;
        this.fadeTimer = 0;
        this.isFading = false;

        Collator collator = Collator.getInstance();
        collator.setStrength(Collator.PRIMARY);

        owEntities.sort((entity1, entity2) -> {
            String translationKey1 = entity1.getDescriptionId();
            String translationKey2 = entity2.getDescriptionId();

            String translatedName1 = Component.translatable(translationKey1).getString();
            String translatedName2 = Component.translatable(translationKey2).getString();

            return collator.compare(translatedName1, translatedName2);
        });

        assignEntityToHisPage();
        this.maxPage = owEntities.size();
        updatePageContent();
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private void updatePageContent() {
        String newLeftPage = "";

        EntityType<? extends OWEntity> entityType = getPageAnimal(actualPage);
        if (entityType != null) {
            if (entityType == OWEntityRegistry.KODIAK.get()) {
                newLeftPage = "textures/gui/adventurer_manuscript/kodiak_chapter.png";
            } else if (entityType == OWEntityRegistry.TIGER.get()) {
                newLeftPage = "textures/gui/adventurer_manuscript/tiger_chapter.png";
            } else if (entityType == OWEntityRegistry.JELLYFISH.get()) {
                newLeftPage = "textures/gui/adventurer_manuscript/jellyfish_chapter.png";
            } else if (entityType == OWEntityRegistry.ELEPHANT.get()) {
                newLeftPage = "textures/gui/adventurer_manuscript/elephant_chapter.png";
            }
        }

        if (!newLeftPage.equals(currentLeftPage)) {
            targetLeftPage = newLeftPage;
            isFading = true;
            fadeTimer = 0;
        }
    }

    public String getPageTexture(int actualPage) {
        EntityType<? extends OWEntity> entityType = getPageAnimal(actualPage);

        if (entityType != null) {
            String entityName = entityType.getDescriptionId().replace("entity.your_mod.", "");
            return "textures/gui/adventurer_manuscript/" + entityName + "_chapter.png";
        }

        return "textures/gui/adventurer_manuscript/default_chapter.png";
    }

    public EntityType<? extends OWEntity> getPageAnimal(int actualPage) {
        for (Map.Entry<EntityType<? extends OWEntity>, Integer> entry : ENTITIES.entrySet()) {
            if (entry.getValue().equals(actualPage)) {
                return entry.getKey();
            }
        }
        return null;
    }

    private void nextPage() {
        if (actualPage < maxPage && pageCooldown <= 0) {
            previousPage = actualPage;
            actualPage++;
            this.minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.BOOK_PAGE_TURN, 1.0F));
            this.minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));

            pageCooldown = pageCooldownMax;

            EntityModelSet modelSet = Minecraft.getInstance().getEntityModels();
            AdventurerManuscriptModel<AdventurerManuscript> bookModel = new AdventurerManuscriptModel<>(
                    modelSet.bakeLayer(AdventurerManuscriptModel.LAYER_LOCATION)
            );

            float ageInTicks = this.animationTime;
            if (this.cachedBookEntity != null) {
                this.cachedBookEntity.nextPageAnimationState.stop();
                this.cachedBookEntity.nextPageAnimationState.startIfStopped(this.cachedBookEntity.tickCount);
                bookModel.setupAnim(this.cachedBookEntity, 0.0F, 0.0F, ageInTicks, 0.0F, 0.0F);
            }

            updatePageContent();
        }
    }

    private void precedentPage() {
        if (actualPage > 1 && pageCooldown <= 0) {
            previousPage = actualPage;
            actualPage--;
            this.minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.BOOK_PAGE_TURN, 1.0F));
            this.minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));

            pageCooldown = pageCooldownMax;

            EntityModelSet modelSet = Minecraft.getInstance().getEntityModels();
            AdventurerManuscriptModel<AdventurerManuscript> bookModel = new AdventurerManuscriptModel<>(
                    modelSet.bakeLayer(AdventurerManuscriptModel.LAYER_LOCATION)
            );

            float ageInTicks = this.animationTime;
            if (this.cachedBookEntity != null) {
                this.cachedBookEntity.precedentPageAnimationState.stop();
                this.cachedBookEntity.precedentPageAnimationState.startIfStopped(this.cachedBookEntity.tickCount);
                bookModel.setupAnim(this.cachedBookEntity, 0.0F, 0.0F, ageInTicks, 0.0F, 0.0F);
            }

            updatePageContent();
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

        if (fadeTimer < FADE_DURATION) {
            return 1.0f - (fadeTimer / (float) FADE_DURATION);
        } else if (fadeTimer < FADE_DURATION * 2) {
            return (fadeTimer - FADE_DURATION) / (float) FADE_DURATION;
        }

        return 1.0f;
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        int i = (this.width - this.imageWidth) / 2;
        int j = (this.height - this.imageHeight) / 2;
        this.xMouse = mouseX;
        this.yMouse = mouseY;

        this.guiOpenTime++;

        this.animationTime += ANIMATION_SPEED;

        if (this.cachedBookEntity != null) {
            this.cachedBookEntity.tickCount = this.guiOpenTime;

            if (this.cachedBookEntity.level().isClientSide()) this.cachedBookEntity.setupAnimationState();
        }

        if (pageCooldown > 0) {
            pageCooldown--;
        }

        if (isFading) {
            fadeTimer++;
            if (fadeTimer == FADE_DURATION) {
                currentLeftPage = targetLeftPage;
                LEFT_PAGE = currentLeftPage;
            } else if (fadeTimer >= FADE_DURATION * 2) {
                isFading = false;
                fadeTimer = 0;
            }
        }

        super.render(guiGraphics, mouseX, mouseY, partialTick);

        int screenCenterX = this.width / 2;
        int screenCenterY = (this.height / 2) - 55;

        int modelRadius = 50;

        renderModel(guiGraphics, screenCenterX - modelRadius, screenCenterY - modelRadius, screenCenterX + modelRadius, screenCenterY + modelRadius, 130);

        String animal = LEFT_PAGE != null && !LEFT_PAGE.equals("") ?
                LEFT_PAGE.split("textures/gui/adventurer_manuscript/")[1].split("_chapter.png")[0]
                : "";

        if (LEFT_PAGE != null && !LEFT_PAGE.equals("")) {
            createAnimalChapterTitle(guiGraphics, ("entity.ow." + animal), 1.75f, getFadeAlpha());
        }
    }

    @Override
    public void removed() {
        super.removed();
        this.cachedBookEntity = null;
    }

    private void createAnimalChapterTitle(GuiGraphics graphics, String entityTranslation, float scale, float alpha) {
        int screenCenterX = this.width / 2;
        int screenCenterY = (this.height / 2) - 55;

        graphics.pose().pushPose();
        graphics.pose().mulPose(Axis.XP.rotationDegrees(BOOK_ROTATION));
        graphics.pose().translate(0, 0, 200.0F);
        graphics.pose().scale(scale, scale, scale);

        Component chapterTitle = Component.translatable(entityTranslation).setStyle(Style.EMPTY.withBold(true));

        int titleWidth = Minecraft.getInstance().font.width(chapterTitle);
        int centeredX = (int)((screenCenterX - (titleWidth * scale / 2) - 72) / scale);
        int centeredY = (int)((screenCenterY - 90) / scale);

        int alphaValue = (int)(alpha * 255);
        int textColor = (alphaValue << 24) | 0xffffe3;
        int outlineColor = (alphaValue << 24) | 0xa2956e;

        Minecraft.getInstance().font.drawInBatch8xOutline(
                chapterTitle.getVisualOrderText(),
                centeredX,
                centeredY,
                textColor,
                outlineColor,
                graphics.pose().last().pose(),
                graphics.bufferSource(),
                15728880
        );

        graphics.pose().popPose();
    }

    private void renderModel(GuiGraphics guiGraphics, int x1, int y1, int x2, int y2, float scale) {
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

        float ageInTicks = this.animationTime;
        if (this.cachedBookEntity != null) {
            this.cachedBookEntity.openAnimationState.startIfStopped(this.cachedBookEntity.tickCount);
            bookModel.setupAnim(this.cachedBookEntity, 0.0F, 0.0F, ageInTicks, 0.0F, 0.0F);
        }

        bookModel.renderToBuffer(guiGraphics.pose(), vertexConsumer, 15728880, OverlayTexture.NO_OVERLAY, 0xFFFFFF);

        if (LEFT_PAGE != null && !LEFT_PAGE.equals("")) {
            ResourceLocation leftOverlayTexture = ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, LEFT_PAGE);
            RenderSystem.setShaderTexture(0, leftOverlayTexture);
            VertexConsumer leftOverlayConsumer = guiGraphics.bufferSource().getBuffer(RenderType.entityTranslucent(leftOverlayTexture));

            float alpha = getFadeAlpha();
            int alphaValue = (int)(alpha * 187);
            int color = (alphaValue << 24) | 0xFFFFFF;

            bookModel.renderToBuffer(guiGraphics.pose(), leftOverlayConsumer, 15728880, OverlayTexture.NO_OVERLAY, color);
        }

        if (RIGHT_PAGE != null && !RIGHT_PAGE.equals("")) {
            ResourceLocation rightOverlayTexture = ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, RIGHT_PAGE);
            RenderSystem.setShaderTexture(0, rightOverlayTexture);
            VertexConsumer rightOverlayConsumer = guiGraphics.bufferSource().getBuffer(RenderType.entityTranslucent(rightOverlayTexture));

            float alpha = getFadeAlpha();
            int alphaValue = (int)(alpha * 187);
            int color = (alphaValue << 24) | 0xFFFFFF;

            bookModel.renderToBuffer(guiGraphics.pose(), rightOverlayConsumer, 15728880, OverlayTexture.NO_OVERLAY, color);
        }

        guiGraphics.flush();
        guiGraphics.pose().popPose();

        Lighting.setupFor3DItems();
    }

    public int getAnimalPage(EntityType<? extends OWEntity> entityType) {
        Integer page = ENTITIES.get(entityType);
        return page != null ? page : 1;
    }
}