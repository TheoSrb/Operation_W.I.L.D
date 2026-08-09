package net.tiew.operationWild.screen.entity;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.MobEffectTextureManager;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffectUtil;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.client.ClientHooks;
import net.neoforged.neoforge.client.event.ScreenEvent;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.tiew.operationWild.OperationWild;
import net.tiew.operationWild.entity.OWEntity;
import net.tiew.operationWild.networking.OWNetworkHandler;
import net.tiew.operationWild.networking.packets.to_server.LevelUpOWInventoryPacket;
import net.tiew.operationWild.screen.entity.skins.*;
import net.tiew.operationWild.screen.entity.OWOptionsScreen;
import net.tiew.operationWild.core.OWUtils;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@OnlyIn(Dist.CLIENT)
public class OWInventoryScreen extends AbstractContainerScreen<OWInventoryMenu> {
    private static final ResourceLocation OW_INVENTORY_LOCATION = ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "textures/gui/ow_inventory_gui.png");
    private static final ResourceLocation MISC_LOCATION = ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "textures/gui/mob_types.png");
    private static final ResourceLocation EFFECT_BACKGROUND_LARGE_SPRITE = ResourceLocation.withDefaultNamespace("container/inventory/effect_background_large");
    private static final ResourceLocation EFFECT_BACKGROUND_SMALL_SPRITE = ResourceLocation.withDefaultNamespace("container/inventory/effect_background_small");

    private final OWEntity entity;
    private float xMouse;
    private float yMouse;
    private Button upgradeHealthButton;
    private Button upgradeDamageButton;
    private Button upgradeSpeedButton;
    private Button upgradeEnergyButton;

    private final OWTabsRenderer tabsRenderer = new OWTabsRenderer();

    private Button stopHealthButton;
    private Button stopDamageButton;
    private Button stopSpeedButton;
    private int entityScale = 17;

    public OWInventoryScreen(OWInventoryMenu menu, Inventory inventory, Component component) {
        super(menu, inventory, component);
        if (Minecraft.getInstance().player.getRootVehicle() instanceof OWEntity entity) this.entity = entity;
        else this.entity = null;
    }

    private static final int STAT_ROW_TOP = 18;
    private static final int STAT_ROW_STEP = 13;
    private static final int STAT_ICON_CELL = 10;
    private static final int STAT_ICON_U = 205;
    private static final int STAT_ICON_V = 0;
    private static final int STAT_ICON_X = 82;
    private static final int STAT_TEXT_X = 94;
    private static final float STAT_TEXT_SCALE = 0.75f;

    private static final int STAT_ARROW_X = 159;
    private static final int STAT_ARROW_V = 183;
    private static final int STAT_ARROW_UP_U = 123;
    private static final int STAT_ARROW_DOWN_U = 130;
    private static final int STAT_ARROW_W = 7;
    private static final int STAT_ARROW_H = 8;

    private static int statRowY(int row) {
        return STAT_ROW_TOP + row * STAT_ROW_STEP + 1;
    }

    private Boolean statAboveAverage(int row) {
        if (this.entity == null) return null;

        return switch (row) {
            case 0 -> OWUtils.aboveSpeciesAverage(this.entity, Attributes.MAX_HEALTH, this.entity.getMaxHealth());
            case 1 -> OWUtils.aboveSpeciesAverage(this.entity, Attributes.ATTACK_DAMAGE, this.entity.getDamageToClient());
            case 2 -> OWUtils.aboveSpeciesAverage(this.entity, Attributes.MOVEMENT_SPEED, this.entity.getSpeed());
            case 3 -> OWUtils.aboveEnergyAverage(this.entity);
            default -> null;
        };
    }

    public Button createButton(String textOnButton, int color, int positionX, int positionY, int width, int height, Runnable onClick) {
        int i = (this.width - this.imageWidth) / 2;
        int j = (this.height - this.imageHeight) / 2;

        return Button.builder(Component.literal(textOnButton)
                                .setStyle(Style.EMPTY.withColor(TextColor.fromRgb(color))),
                        button -> onClick.run())
                .bounds(i + positionX, j - positionY, width, height)
                .build();
    }

    @Override
    protected void init() {
        super.init();
        upgradeHealthButton = createButton("+", 0xb8e45a, 175, -statRowY(0), 10, 10, () -> OWNetworkHandler.sendToServer(new LevelUpOWInventoryPacket("MaxHealth")));
        upgradeDamageButton = createButton("+", 0xb8e45a, 175, -statRowY(1), 10, 10, () -> OWNetworkHandler.sendToServer(new LevelUpOWInventoryPacket("AttackDamage")));
        upgradeSpeedButton = createButton("+", 0xb8e45a, 175, -statRowY(2), 10, 10, () -> OWNetworkHandler.sendToServer(new LevelUpOWInventoryPacket("MovementSpeed")));
        upgradeEnergyButton = createButton("+", 0xb8e45a, 175, -statRowY(3), 10, 10, () -> OWNetworkHandler.sendToServer(new LevelUpOWInventoryPacket("VitalEnergy")));

        stopHealthButton = createButton("X", 0x9c0d0d, 175, -statRowY(0), 10, 10, () -> {
        });
        stopHealthButton.setTooltip(Tooltip.create(Component.translatable("tooltip.stop_health")
                .setStyle(Style.EMPTY.withColor(TextColor.fromRgb(0x9c0d0d)))));

        stopDamageButton = createButton("X", 0x9c0d0d, 175, -statRowY(1), 10, 10, () -> {
        });
        stopDamageButton.setTooltip(Tooltip.create(Component.translatable("tooltip.stop_damage")
                .setStyle(Style.EMPTY.withColor(TextColor.fromRgb(0x9c0d0d)))));

        stopSpeedButton = createButton("X", 0x9c0d0d, 175, -statRowY(2), 10, 10, () -> {
        });
        stopSpeedButton.setTooltip(Tooltip.create(Component.translatable("tooltip.stop_speed")
                .setStyle(Style.EMPTY.withColor(TextColor.fromRgb(0x9c0d0d)))));

        this.addRenderableWidget(upgradeHealthButton);
        this.addRenderableWidget(upgradeDamageButton);
        this.addRenderableWidget(upgradeSpeedButton);
        this.addRenderableWidget(upgradeEnergyButton);

        this.addRenderableWidget(stopHealthButton);
        this.addRenderableWidget(stopDamageButton);
        this.addRenderableWidget(stopSpeedButton);

        switch (this.entity.getClass().getSimpleName()) {
            case "TigerEntity" -> entityScale = 22;
            case "CrocodileEntity" -> entityScale = 18;
            case "PeacockEntity" -> entityScale = 35;
            case "ElephantEntity" -> entityScale = 12;
            case "HyenaEntity" -> entityScale = 30;
        }

        tabsRenderer.init(this.width, this.height, this.imageWidth, this.imageHeight, entity, this::addRenderableWidget);
        //tabsRenderer.setNotification(3, 3);

        tabsRenderer.setActiveTab(OWTabsRenderer.Tab.INVENTORY);

        maybeShowInventoryTutorials();
    }

    /**
     * Didacticiels liés à l'écran d'inventaire (une seule fois chacun, tous mondes confondus) :
     * <ul>
     *   <li>1re ouverture : onglets puis archétype.</li>
     *   <li>Après le tip de niveau, à la réouverture avec des points à dépenser : les statistiques et les boutons +.</li>
     * </ul>
     */
    private void maybeShowInventoryTutorials() {
        if (this.entity == null) return;
        int i = this.leftPos;
        int j = this.topPos;

        if (!net.tiew.operationWild.core.OWTutorialData.hasSeenInventoryTutorial()) {
            // Onglets : glow sur la rangée d'onglets (i+2 → i+102, j-18, hauteur 18).
            net.tiew.operationWild.gui.OWIndicationOverlay.enqueue(
                    Component.translatable("indication.ow.tuto_tabs"), 200, i + 40, j - 20, true,
                    i + 2, j - 18, 100, 18);
            // Archétype : glow sur le petit symbole en haut à droite (i+138, j+4, 12x12).
            net.tiew.operationWild.gui.OWIndicationOverlay.enqueue(
                    Component.translatable("indication.ow.tuto_archetype"), 200, i + 144, j + 4, true,
                    i + 138, j + 4, 12, 12);
            net.tiew.operationWild.core.OWTutorialData.markInventoryTutorialSeen();
        }

        if (net.tiew.operationWild.core.OWTutorialData.hasSeenLevelTutorial()
                && !net.tiew.operationWild.core.OWTutorialData.hasSeenStatsTutorial()
                && this.entity.getLevelPoints() > 0) {
            // Stats + boutons « + » : encart à droite, étroit (plus de retours à la ligne), glow sur la zone.
            net.tiew.operationWild.gui.OWIndicationOverlay.enqueue(
                    Component.translatable("indication.ow.tuto_stats"), 200, i + 258, j + 18, false,
                    i + 96, j + 19, 92, 48, 110);
            net.tiew.operationWild.core.OWTutorialData.markStatsTutorialSeen();
        }
    }

    @Override
    public boolean shouldCloseOnEsc() {
        // Pas de fermeture par Échap tant qu'un didacticiel d'inventaire est affiché.
        return !net.tiew.operationWild.gui.OWIndicationOverlay.isActive();
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        // Renoncement aux didacticiels : c'est ici, et pas dans les événements de touche, que la
        // touche doit être vue — ceux-ci ne se déclenchent plus dès qu'un écran est ouvert, et les
        // didacticiels d'onglets et d'archétype s'affichent précisément par-dessus celui-ci.
        if (net.tiew.operationWild.gui.OWIndicationOverlay.isSkipKey(keyCode, scanCode)
                && net.tiew.operationWild.gui.OWIndicationOverlay.skipAll()) {
            return true;
        }
        // La touche d'inventaire ferme aussi l'écran : on la bloque pendant le didacticiel.
        if (net.tiew.operationWild.gui.OWIndicationOverlay.isActive()
                && this.minecraft != null
                && this.minecraft.options.keyInventory.matches(keyCode, scanCode)) {
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    protected void renderBg(GuiGraphics guiGraphics, float p_282998_, int p_282929_, int p_283133_) {
        int i = (this.width - this.imageWidth) / 2;
        int j = (this.height - this.imageHeight) / 2;
        int j1 = (entity.isHealer() || entity.isBerserker() || entity.isScout() || entity.isNormal()) ? 12 : 0;
        int j2 = (entity.isTank() || entity.isHealer()) ? 0 : (entity.isAssassin() || entity.isBerserker()) ? 12 : (entity.isMarauder() || entity.isScout()) ? 24 : entity.isNormal() ? 36 : 0;
        int titleLength = (int) font.width(this.title);
        int genderImagePosition = entity.isFemale() ? 36 : entity.isMale() ? 48 : 0;
        int mobTypePlacementX = i + 138;

        guiGraphics.blit(OW_INVENTORY_LOCATION, i, j, 0, 0, 176, 166);
        guiGraphics.blit(MISC_LOCATION, mobTypePlacementX, j + 4, j1, j2, 12, 12);
        guiGraphics.blit(MISC_LOCATION, i + titleLength + 10, j + 4, 0, genderImagePosition, 12, 12);

        if (!entity.getItemFood().isEmpty()) {
            guiGraphics.blit(OW_INVENTORY_LOCATION, i + 5, j + 35, 0, 224, 16, 16);
        }

        // 3e slot (gants de boxe) du kangourou : cadre dessine manuellement (absent du fond).
        if ("KangarooEntity".equals(this.entity.getClass().getSimpleName())) {
            guiGraphics.blit(OW_INVENTORY_LOCATION, i + 5, j + 53, 0, 224, 18, 18);
            // Ombre de l'item (icone fantome) tant que le slot est vide.
            if (!this.menu.getSlot(2).hasItem()) {
                guiGraphics.blit(OW_INVENTORY_LOCATION, i + 6, j + 54, 57, 226, 16, 16);
            }
        }

        float xpPercentage = entity.getXpStage() > 0 ? entity.getXp() / (float) entity.getXpStage() : 0f;
        int xpBarWidth = this.entity.getLevel() >= 50 ? 71 : (int) (71 * xpPercentage);

        if (this.entity.getLevel() < 50 && entity.getXp() > 0 && xpBarWidth < 1) {
            xpBarWidth = 1;
        }

        if (xpBarWidth > 0) {
            guiGraphics.blit(OW_INVENTORY_LOCATION, i + 89, j + 75, 0, 184, xpBarWidth, 5);
        }

        guiGraphics.blit(OW_INVENTORY_LOCATION, i + 6, j + 18, 240, entitySaddleCoords(), 16, 16);

        for (int row = 0; row < 4; row++) {
            guiGraphics.blit(OW_INVENTORY_LOCATION, i + STAT_ICON_X, j + statRowY(row),
                    STAT_ICON_U, STAT_ICON_V + row * STAT_ICON_CELL, STAT_ICON_CELL, STAT_ICON_CELL);

            Boolean aboveAverage = statAboveAverage(row);
            if (aboveAverage == null) continue;

            guiGraphics.blit(OW_INVENTORY_LOCATION, i + STAT_ARROW_X, j + statRowY(row) + 1,
                    aboveAverage ? STAT_ARROW_UP_U : STAT_ARROW_DOWN_U, STAT_ARROW_V,
                    STAT_ARROW_W, STAT_ARROW_H);
        }

        // Affiche le boa ENTIER (tete + corps) : la queue est faite d'entites separees
        // qui n'existent pas dans le GUI, donc on demande au BoaModel d'afficher le corps.
        // Le boa entier est long : on le retrecit un peu et on le remonte un peu pour
        // qu'il tienne dans le cadre (valeurs a ajuster a l'oeil).
        boolean isBoa = "BoaEntity".equals(this.entity.getClass().getSimpleName());
        int previewScale = isBoa ? Math.round(entityScale * 0.85f) : entityScale;
        int boaUp = isBoa ? 7 : 0;
        net.tiew.operationWild.entity.client.model.BoaModel.RENDER_FULL_BODY = true;
        net.tiew.operationWild.entity.client.model.CrocodileModel.RENDER_FULL_BODY = true;
        try {
            InventoryScreen.renderEntityInInventoryFollowsMouse(guiGraphics, i + 26, j + 18 - boaUp, i + 78, j + 70 - boaUp, previewScale, 0.25F, this.xMouse, this.yMouse, this.entity);
        } finally {
            net.tiew.operationWild.entity.client.model.BoaModel.RENDER_FULL_BODY = false;
            net.tiew.operationWild.entity.client.model.CrocodileModel.RENDER_FULL_BODY = false;
        }

        renderTexts(guiGraphics, i, j);
    }

    private int entitySaddleCoords() {
        switch (entity.getClass().getSimpleName()) {
            case "TigerEntity":
                return 240;
            case "BoaEntity":
                return 224;
            case "PeacockEntity":
                return 208;
            case "TigerSharkEntity":
                return 192;
            case "ElephantEntity":
                return 176;
            case "KodiakEntity":
                return 160;
            case "CrocodileEntity":
                return 144;
            case "OrcaEntity":
                return 128;
            default:
                return 0;
        }
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float v) {
        this.xMouse = (float) mouseX;
        this.yMouse = (float) mouseY;
        // Onglets verrouillés tant qu'un didacticiel est affiché (pas de navigation entre onglets).
        tabsRenderer.setLocked(net.tiew.operationWild.gui.OWIndicationOverlay.isActive());
        super.render(graphics, mouseX, mouseY, v);
        this.renderEffects(graphics, mouseX, mouseY);
        this.renderTooltip(graphics, mouseX, mouseY);

        float upgradeHealthLimit = 0.75f + entity.getArchetype().getHealthMultiplier();
        float upgradeDamageLimit = 0.75f + entity.getArchetype().getDamageMultiplier();
        float upgradeSpeedLimit = 0.5f + entity.getArchetype().getSpeedMultiplier();
        int color = (entity.tickCount / 7) % 2 == 0 ? 0xb8e45a : 0x8b8b8b;

        boolean hasLevelPoints = entity.getLevelPoints() > 0;
        upgradeHealthButton.visible = hasLevelPoints && this.entity.getMaxHealth() < (this.entity.getBaseHealth() * upgradeHealthLimit);
        upgradeDamageButton.visible = hasLevelPoints && this.entity.getDamageToClient() < (this.entity.getBaseDamage() * upgradeDamageLimit);
        upgradeSpeedButton.visible = hasLevelPoints && this.entity.getSpeed() < (this.entity.getBaseSpeed() * upgradeSpeedLimit);
        upgradeEnergyButton.visible = hasLevelPoints;

        upgradeHealthButton.setMessage(Component.literal("+").withStyle(style -> style.withColor(color)));
        upgradeDamageButton.setMessage(Component.literal("+").withStyle(style -> style.withColor(color)));
        upgradeSpeedButton.setMessage(Component.literal("+").withStyle(style -> style.withColor(color)));
        upgradeEnergyButton.setMessage(Component.literal("+").withStyle(style -> style.withColor(color)));

        stopHealthButton.visible = hasLevelPoints && !upgradeHealthButton.visible;
        stopDamageButton.visible = hasLevelPoints && !upgradeDamageButton.visible;
        stopSpeedButton.visible = hasLevelPoints && !upgradeSpeedButton.visible;

        int i = (this.width - this.imageWidth) / 2;
        int j = (this.height - this.imageHeight) / 2;
        int titleLength = this.title.getString().length() * 6;
        int iconY = j + 4;
        int mobTypePlacementX = i + 138;

        tabsRenderer.renderTabs(graphics, this.font, entity, i, j, mouseX, mouseY);

        String tooltipKey = entity.isTank() ? "tooltip.mobTypesTank"
                : entity.isAssassin()  ? "tooltip.mobTypesAssassin"
                : entity.isMarauder()  ? "tooltip.mobTypesMarauder"
                : entity.isHealer()    ? "tooltip.mobTypesHealer"
                : entity.isBerserker() ? "tooltip.mobTypesBerserker"
                : entity.isScout()     ? "tooltip.mobTypesScout"
                : entity.isNormal()    ? "tooltip.mobTypesNormal" : "";

        Component tooltipXpValue    = Component.literal(String.valueOf(Math.round(entity.getXp() * 100) / 100.0 + " / "))
                .withStyle(Style.EMPTY.withColor(TextColor.fromRgb(0xFFFFFF)).withItalic(true).withBold(true));
        Component tooltipXpMaxValue = Component.literal(String.valueOf((float) Math.round(entity.getXpStage() * 100) / 100.0))
                .withStyle(Style.EMPTY.withColor(TextColor.fromRgb(0xFFFFFF)).withItalic(true).withBold(true));
        Component tooltipXp = Component.translatable("tooltip.xp")
                .withStyle(Style.EMPTY.withColor(TextColor.fromRgb(0xb8e45a)).withItalic(true).withBold(true));

        Component xpText        = tooltipXpValue.copy().append(tooltipXpMaxValue.copy()).append(tooltipXp.copy());
        Component genderText = Component.translatable(entity.isFemale() ? "tooltip.genderFemale" : entity.isMale() ? "tooltip.genderMale" : "")
                .withStyle(Style.EMPTY).withColor(TextColor.fromRgb(entity.isFemale() ? 0xcb3eb3 : entity.isMale() ? 0x4647ce : 0x000000).getValue());

        if (mouseX >= mobTypePlacementX && mouseX <= mobTypePlacementX + 12 && mouseY >= iconY && mouseY <= iconY + 12)
            graphics.renderComponentTooltip(this.font, List.of(Component.translatable(tooltipKey)), mouseX, mouseY);
        if (this.entity.getLevel() < 50 && mouseX >= i + 89 && mouseX <= i + 89 + 71 && mouseY >= j + 75 && mouseY <= j + 75 + 5)
            graphics.renderComponentTooltip(this.font, List.of(xpText), mouseX, mouseY);
        if (mouseX >= i + titleLength + 7 && mouseX <= i + titleLength + 7 + 12 && mouseY >= j + 4 && mouseY <= j + 4 + 12)
            graphics.renderComponentTooltip(this.font, List.of(genderText), mouseX, mouseY);

        this.renderTooltip(graphics, mouseX, mouseY);
    }

    private void renderEffects(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        int i = this.leftPos + this.imageWidth + 2 + (entity.getLevelPoints() > 0 ? 8 : 0);
        int j = this.width - i;
        Collection<MobEffectInstance> collection = entity.getActiveEffects();
        if (!collection.isEmpty() && j >= 32) {
            boolean flag = j >= 120;
            ScreenEvent.RenderInventoryMobEffects event = ClientHooks.onScreenPotionSize(this, j, !flag, i);
            if (event.isCanceled()) return;

            flag = !event.isCompact();
            i = event.getHorizontalOffset();
            int k = 33;
            if (collection.size() > 5) k = 132 / (collection.size() - 1);

            Iterable<MobEffectInstance> iterable = collection.stream()
                    .filter(MobEffectInstance::showIcon)
                    .filter(ClientHooks::shouldRenderEffect)
                    .sorted()
                    .collect(Collectors.toList());

            this.renderBackgrounds(guiGraphics, i, k, iterable, flag);
            this.renderIcons(guiGraphics, i, k, iterable, flag);
            if (flag) {
                this.renderLabels(guiGraphics, i, k, iterable);
            } else if (mouseX >= i && mouseX <= i + 33) {
                int l = this.topPos;
                MobEffectInstance mobeffectinstance = null;
                for (MobEffectInstance mobeffectinstance1 : iterable) {
                    if (mouseY >= l && mouseY <= l + k) mobeffectinstance = mobeffectinstance1;
                    l += k;
                }
                if (mobeffectinstance != null) {
                    List<Component> list = List.of(
                            this.getEffectName(mobeffectinstance),
                            MobEffectUtil.formatDuration(mobeffectinstance, 1.0F, this.minecraft.level.tickRateManager().tickrate()));
                    guiGraphics.renderTooltip(this.font, list, Optional.empty(), mouseX, mouseY);
                }
            }
        }
    }

    private void renderTexts(GuiGraphics graphics, int offsetX, int offsetY) {
        int centerX = offsetX + (this.imageWidth / 2);
        int centerY = offsetY + (this.imageHeight / 2);

        String levelPoints = String.valueOf(entity.getLevelPoints());
        String entityHealth = String.valueOf(Math.round(entity.getHealth() * 2) / 2.0 + " / " + Math.round(entity.getMaxHealth() * 2) / 2.0);
        String entityDamage = String.valueOf(Math.round(entity.getDamageToClient() * 10) / 10.0);
        String entityBaseSpeed = String.valueOf(Math.round(OWUtils.getSpeedBlocksPerSecond(entity) * 100) / 100.0);
        Component entitySpeed = Component.literal(entityBaseSpeed)
                .append(Component.translatable("tooltip.entitySpeed"));

        Component levelText = Component.translatable("tooltip.level");
        Component levelValue = Component.literal(String.valueOf(entity.getLevel())).withStyle(Style.EMPTY.withColor(TextColor.fromRgb(entity.getLevel() >= 50 ? 0xdd9847 : 0xb8e45a)).withBold(true));
        Component fullLevelText = Component.empty().append(levelText).append(" ").append(levelValue);

        graphics.drawString(this.font, levelPoints, this.entity.getLevelPoints() > 9 ? centerX + 67 : centerX + 70, centerY - 76, this.entity.getLevelPoints() > 0 ? 0xb8e45a : 0x8b8b8b);

        float energyLeft = Math.max(0f, entity.getVitalEnergyCapacity() - entity.getVitalEnergy());
        String entityEnergy = Math.round(energyLeft) + " / " + Math.round(entity.getVitalEnergyCapacity());

        graphics.pose().pushPose();
        graphics.pose().scale(STAT_TEXT_SCALE, STAT_TEXT_SCALE, 1f);
        int textX = Math.round((offsetX + STAT_TEXT_X) / STAT_TEXT_SCALE);
        drawStatLine(graphics, entityHealth, textX, offsetY, 0);
        drawStatLine(graphics, entityDamage, textX, offsetY, 1);
        drawStatLine(graphics, entitySpeed, textX, offsetY, 2);
        drawStatLine(graphics, entityEnergy, textX, offsetY, 3);
        graphics.pose().popPose();

        graphics.drawString(this.font, fullLevelText, centerX - (this.font.width(fullLevelText) / 2), centerY + 100, 0xFFFFFF);
    }

    private void drawStatLine(GuiGraphics graphics, Object text, int textX, int offsetY, int row) {
        int y = Math.round((offsetY + STAT_ROW_TOP + row * STAT_ROW_STEP + 3) / STAT_TEXT_SCALE);
        if (text instanceof Component component) graphics.drawString(this.font, component, textX, y, 0x8b8b8b);
        else graphics.drawString(this.font, String.valueOf(text), textX, y, 0x8b8b8b);
    }

    private void renderBackgrounds(GuiGraphics guiGraphics, int renderX, int yOffset, Iterable<MobEffectInstance> effects, boolean isSmall) {
        int i = this.topPos;

        for (MobEffectInstance mobeffectinstance : effects) {
            if (isSmall) {
                guiGraphics.blitSprite(EFFECT_BACKGROUND_LARGE_SPRITE, renderX, i, 120, 32);
            } else {
                guiGraphics.blitSprite(EFFECT_BACKGROUND_SMALL_SPRITE, renderX, i, 32, 32);
            }

            i += yOffset;
        }

    }

    private void renderIcons(GuiGraphics guiGraphics, int renderX, int yOffset, Iterable<MobEffectInstance> effects, boolean isSmall) {
        MobEffectTextureManager mobeffecttexturemanager = this.minecraft.getMobEffectTextures();
        int i = this.topPos;

        for (MobEffectInstance mobeffectinstance : effects) {
            Holder<MobEffect> holder = mobeffectinstance.getEffect();
            TextureAtlasSprite textureatlassprite = mobeffecttexturemanager.get(holder);
            guiGraphics.blit(renderX + (isSmall ? 6 : 7), i + 7, 0, 18, 18, textureatlassprite);
            i += yOffset;
        }

    }

    private void renderLabels(GuiGraphics guiGraphics, int renderX, int yOffset, Iterable<MobEffectInstance> effects) {
        int i = this.topPos;

        for (MobEffectInstance mobeffectinstance : effects) {
            Component component = this.getEffectName(mobeffectinstance);
            guiGraphics.drawString(this.font, component, renderX + 10 + 18, i + 6, 16777215);
            Component component1 = MobEffectUtil.formatDuration(mobeffectinstance, 1.0F, this.minecraft.level.tickRateManager().tickrate());
            guiGraphics.drawString(this.font, component1, renderX + 10 + 18, i + 6 + 10, 8355711);
            i += yOffset;
        }

    }

    private Component getEffectName(MobEffectInstance effect) {
        MutableComponent mutablecomponent = ((MobEffect) effect.getEffect().value()).getDisplayName().copy();
        if (effect.getAmplifier() >= 1 && effect.getAmplifier() <= 9) {
            MutableComponent var10000 = mutablecomponent.append(CommonComponents.SPACE);
            int var10001 = effect.getAmplifier();
            var10000.append(Component.translatable("enchantment.level." + (var10001 + 1)));
        }

        return mutablecomponent;
    }
}