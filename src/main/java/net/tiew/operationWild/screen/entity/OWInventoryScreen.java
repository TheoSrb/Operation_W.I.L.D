package net.tiew.operationWild.screen.entity;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.EffectRenderingInventoryScreen;
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
import net.neoforged.neoforge.client.extensions.common.IClientMobEffectExtensions;
import net.tiew.operationWild.OperationWild;
import net.tiew.operationWild.entity.OWEntity;
import net.tiew.operationWild.networking.OWNetworkHandler;
import net.tiew.operationWild.networking.packets.to_server.LevelUpOWInventoryPacket;
import net.tiew.operationWild.networking.packets.to_server.OpenDailyQuestScreen;
import net.tiew.operationWild.screen.entity.skins.*;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@OnlyIn(Dist.CLIENT)
public class OWInventoryScreen extends EffectRenderingInventoryScreen<OWInventoryMenu> {
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
    private Button skinButton;
    private Button dailyQuestButton;
    private Button stopHealthButton;
    private Button stopDamageButton;
    private Button stopSpeedButton;
    private int entityScale = 17;

    public OWInventoryScreen(OWInventoryMenu menu, Inventory inventory, Component component) {
        super(menu, inventory, component);
        if (Minecraft.getInstance().player.getRootVehicle() instanceof OWEntity entity) this.entity = entity;
        else this.entity = null;
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

    private void chooseSkinsScreenForEntity(OWEntity entity) {
        switch (entity.getClass().getSimpleName()) {
            case "TigerEntity" -> Minecraft.getInstance().setScreen(new TigerSkinsScreen());
            case "BoaEntity" -> Minecraft.getInstance().setScreen(new BoaSkinsScreen());
            case "PeacockEntity" -> Minecraft.getInstance().setScreen(new PeacockSkinsScreen());
            case "ElephantEntity" -> Minecraft.getInstance().setScreen(new ElephantSkinsScreen());
            case "KodiakEntity" -> Minecraft.getInstance().setScreen(new KodiakSkinsScreen());
            default -> Minecraft.getInstance().player.sendSystemMessage(Component.translatable("tooltip.noSkins").withStyle(Style.EMPTY).withColor(0xFF0000));
        }
    }

    private void dailyQuestsButtonIsClicked() {
        entity.getControllingPassenger().sendSystemMessage(Component.literal("ERROR"));
        /*
        Minecraft.getInstance().setScreen(new OWDailyQuestScreen());
        OWNetworkHandler.sendToServer(new OpenDailyQuestScreen());
        */
    }

    @Override
    protected void init() {
        super.init();
        upgradeHealthButton = createButton("+", 0xb8e45a,  175, -21, 10, 10, () -> OWNetworkHandler.sendToServer(new LevelUpOWInventoryPacket("MaxHealth")));
        upgradeDamageButton = createButton("+", 0xb8e45a, 175, -39, 10, 10, () -> OWNetworkHandler.sendToServer(new LevelUpOWInventoryPacket("AttackDamage")));
        upgradeSpeedButton = createButton("+", 0xb8e45a, 175, -57, 10, 10, () -> OWNetworkHandler.sendToServer(new LevelUpOWInventoryPacket("MovementSpeed")));

        skinButton = createButton("", 0xFFFFFF, 176, -146, 20, 20, () -> chooseSkinsScreenForEntity(entity));
        dailyQuestButton = createButton("", 0xFFFFFF, 176, -126, 20, 20, this::dailyQuestsButtonIsClicked);

        stopHealthButton = createButton("X", 0x9c0d0d,  175, -21, 10, 10, () -> {});
        stopHealthButton.setTooltip(Tooltip.create(Component.translatable("tooltip.stop_health")
                .setStyle(Style.EMPTY.withColor(TextColor.fromRgb(0x9c0d0d)))));

        stopDamageButton = createButton("X", 0x9c0d0d, 175, -39, 10, 10, () -> {});
        stopDamageButton.setTooltip(Tooltip.create(Component.translatable("tooltip.stop_damage")
                .setStyle(Style.EMPTY.withColor(TextColor.fromRgb(0x9c0d0d)))));

        stopSpeedButton = createButton("X", 0x9c0d0d, 175, -57, 10, 10, () -> {});
        stopSpeedButton.setTooltip(Tooltip.create(Component.translatable("tooltip.stop_speed")
                .setStyle(Style.EMPTY.withColor(TextColor.fromRgb(0x9c0d0d)))));

        this.addRenderableWidget(upgradeHealthButton);
        this.addRenderableWidget(upgradeDamageButton);
        this.addRenderableWidget(upgradeSpeedButton);

        this.addRenderableWidget(skinButton);
        this.addRenderableWidget(dailyQuestButton);

        this.addRenderableWidget(stopHealthButton);
        this.addRenderableWidget(stopDamageButton);
        this.addRenderableWidget(stopSpeedButton);

        switch (this.entity.getClass().getSimpleName()) {
            case "TigerEntity" -> entityScale = 22;
            case "PeacockEntity" -> entityScale = 35;
            case "ElephantEntity" -> entityScale = 12;
        }
    }

    protected void renderBg(GuiGraphics guiGraphics, float p_282998_, int p_282929_, int p_283133_) {
        int i = (this.width - this.imageWidth) / 2;
        int j = (this.height - this.imageHeight) / 2;
        int j2 = entity.isTank() ? 0 : entity.isAssassin() ? 12 : entity.isMarauder() ? 24 : 0;
        int titleLength = (int) font.width(this.title);
        int genderImagePosition = entity.isFemale() ? 36 : entity.isMale() ? 48 : 0;
        int mobTypePlacementX = this.entity.getLevel() >= 50 && this.entity.getLevelPoints() <= 0 ? this.entity.getPrestigeLevel() >= 100 ? i + 116 : this.entity.getPrestigeLevel() >= 10 ? i + 123 : this.entity.getPrestigeLevel() >= 0 ? i + 129 : 0 : i + 138;

        guiGraphics.blit(OW_INVENTORY_LOCATION, i, j, 0, 0, 176,166);
        guiGraphics.blit(MISC_LOCATION, mobTypePlacementX, j + 4, 0, j2, 12,12);
        guiGraphics.blit(MISC_LOCATION, i + titleLength + 10, j + 4, 0, genderImagePosition, 12,12);

        if (!entity.getItemFood().isEmpty()) {
            guiGraphics.blit(OW_INVENTORY_LOCATION, i + 5, j + 35, 0, 224, 16,16);
        }

        float xpPercentage = entity.getXp() / (float)entity.getXpStage();
        float xpPrestigePercentage = entity.getXp() / (float)entity.getPrestigeXpStage();
        int xpBarWidth = this.entity.getLevel() < 50 ? (int)(71 * xpPercentage) : (int)(71 * xpPrestigePercentage);

        if (entity.getXp() > 0 && xpBarWidth < 1) {
            xpBarWidth = 1;
        }

        if (xpBarWidth > 0) {
            if (this.entity.getLevel() >= 50) guiGraphics.blit(OW_INVENTORY_LOCATION, i + 89, j + 75, 0, 189, xpBarWidth, 5);
            else guiGraphics.blit(OW_INVENTORY_LOCATION, i + 89, j + 75, 0, 184, xpBarWidth, 5);
        }

        guiGraphics.blit(OW_INVENTORY_LOCATION, i + 6, j + 18, 240, entitySaddleCoords(), 16,16);

        InventoryScreen.renderEntityInInventoryFollowsMouse(guiGraphics, i + 26, j + 18, i + 78, j + 70, entityScale, 0.25F, this.xMouse, this.yMouse, this.entity);

        renderTexts(guiGraphics, i, j);
    }

    private int entitySaddleCoords() {
        switch (entity.getClass().getSimpleName()) {
            case "TigerEntity": return 240;
            case "BoaEntity": return 224;
            case "PeacockEntity": return 208;
            case "TigerSharkEntity": return 192;
            case "ElephantEntity": return 176;
            default: return 0;
        }
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float v) {
        this.renderBackground(graphics, mouseX, mouseY, v);
        this.xMouse = (float) mouseX;
        this.yMouse = (float) mouseY;
        super.render(graphics, mouseX, mouseY, v);
        this.renderEffects(graphics, mouseX, mouseY);
        this.renderTooltip(graphics, mouseX, mouseY);

        float upgradeHealthLimit = entity.isTank() ? 2.5f : entity.isAssassin() ? 1.75f : entity.isMarauder() ? 1.5f : 1.0f;
        float upgradeDamageLimit = entity.isTank() ? 1.5f : entity.isAssassin() ? 1.85f : entity.isMarauder() ? 1.7f : 1.0f;
        float upgradeSpeedLimit = entity.isTank() ? 1.1f : entity.isAssassin() ? 1.2f : entity.isMarauder() ? 1.35f : 1.0f;
        int color = (entity.tickCount / 7) % 2 == 0 ? 0xb8e45a : 0x8b8b8b;

        boolean hasLevelPoints = entity.getLevelPoints() > 0;
        upgradeHealthButton.visible = hasLevelPoints && this.entity.getMaxHealth() < (this.entity.getBaseHealth() * upgradeHealthLimit);
        upgradeDamageButton.visible = hasLevelPoints && this.entity.getDamageToClient() < (this.entity.getBaseDamage() * upgradeDamageLimit);
        upgradeSpeedButton.visible = hasLevelPoints && this.entity.getSpeed() < (this.entity.getBaseSpeed() * upgradeSpeedLimit);

        upgradeHealthButton.setMessage(Component.literal("+").withStyle(style -> style.withColor(color)));
        upgradeDamageButton.setMessage(Component.literal("+").withStyle(style -> style.withColor(color)));
        upgradeSpeedButton.setMessage(Component.literal("+").withStyle(style -> style.withColor(color)));

        stopHealthButton.visible = hasLevelPoints && !upgradeHealthButton.visible;
        stopDamageButton.visible = hasLevelPoints && !upgradeDamageButton.visible;
        stopSpeedButton.visible = hasLevelPoints && !upgradeSpeedButton.visible;

        int i = (this.width - this.imageWidth) / 2;
        int j = (this.height - this.imageHeight) / 2;
        int titleLength = this.title.getString().length() * 6;
        int iconY = j + 4;
        int mobTypePlacementX = this.entity.getLevel() >= 50 && this.entity.getLevelPoints() <= 0 ? this.entity.getPrestigeLevel() >= 100 ? i + 116 : this.entity.getPrestigeLevel() >= 10 ? i + 123 : this.entity.getPrestigeLevel() >= 0 ? i + 129 : 0 : i + 138;
        String tooltipKey = entity.isTank() ? "tooltip.mobTypesTank" : entity.isAssassin() ? "tooltip.mobTypesAssassin" : entity.isMarauder() ? "tooltip.mobTypesMarauder" : "";
        Component tooltipXpValue = Component.literal(String.valueOf(Math.round(entity.getXp() * 100) / 100.0 + " / ")).withStyle(Style.EMPTY.withColor(TextColor.fromRgb(0xFFFFFF)).withItalic(true).withBold(true));
        Component tooltipXpMaxValue = Component.literal(String.valueOf(this.entity.getLevel() < 50 ? (float) Math.round(entity.getXpStage() * 100) / 100.0 : (float) Math.round(entity.getPrestigeXpStage() * 100) / 100.0)).withStyle(Style.EMPTY.withColor(TextColor.fromRgb(0xFFFFFF)).withItalic(true).withBold(true));
        Component tooltipXp = Component.translatable("tooltip.xp").withStyle(Style.EMPTY.withColor(TextColor.fromRgb(this.entity.getLevel() < 50 ? 0xb8e45a : 0x7fe1ff)).withItalic(true).withBold(true));

        Component xpText = tooltipXpValue.copy().append(tooltipXpMaxValue.copy()).append(tooltipXp.copy());
        Component xpPrestigeText = tooltipXpValue.copy().append(tooltipXpMaxValue.copy()).append(tooltipXp.copy());

        Component genderText = Component.translatable(entity.isFemale() ? "tooltip.genderFemale" : entity.isMale() ? "tooltip.genderMale" : "").withStyle(Style.EMPTY).withColor(TextColor.fromRgb(entity.isFemale() ? 0xcb3eb3 : entity.isMale() ? 0x4647ce : 0x000000).getValue());
        Component cosmeticsText = Component.translatable("tooltip.cosmetics").withStyle(Style.EMPTY.withColor(TextColor.fromRgb(0xFFFFFF)).withItalic(true).withBold(true));
        Component dailyQuestsText = Component.translatable("tooltip.dailyQuests").withStyle(Style.EMPTY.withColor(TextColor.fromRgb(0xFFFFFF)).withItalic(true).withBold(true));

        graphics.blit(OW_INVENTORY_LOCATION, i + 178, j + 148, 0, 206, 16,16);
        graphics.blit(OW_INVENTORY_LOCATION, i + 178, j + 128, 16, 206, 16,16);

        if (mouseX >= mobTypePlacementX && mouseX <= mobTypePlacementX + 12 && mouseY >= iconY && mouseY <= iconY + 12) {
            graphics.renderComponentTooltip(this.font, List.of(Component.translatable(tooltipKey)), mouseX, mouseY);
        }
        if (mouseX >= i + 89 && mouseX <= i + 89 + 71 && mouseY >= j + 75 && mouseY <= j + 75 + 5) {
            graphics.renderComponentTooltip(this.font, List.of(this.entity.getLevel() >= 50 ? xpPrestigeText : xpText), mouseX, mouseY);
        }
        if (mouseX >= i + titleLength + 7 && mouseX <= i + titleLength + 7 + 12 && mouseY >= j + 4 && mouseY <= j + 4 + 12) {
            graphics.renderComponentTooltip(this.font, List.of(genderText), mouseX, mouseY);
        }
        if (mouseX >= i + 178 && mouseX <= i + 178 + 16 && mouseY >= j + 148 && mouseY <= j + 148 + 16) {
            graphics.renderComponentTooltip(this.font, List.of(cosmeticsText), mouseX, mouseY);
        }
        if (mouseX >= i + 178 && mouseX <= i + 178 + 16 && mouseY >= j + 128 && mouseY <= j + 128 + 16) {
            graphics.renderComponentTooltip(this.font, List.of(dailyQuestsText), mouseX, mouseY);
        }

        this.renderTooltip(graphics, mouseX, mouseY);


    }

    private void renderTexts(GuiGraphics graphics, int offsetX, int offsetY) {
        int centerX = offsetX + (this.imageWidth / 2);
        int centerY = offsetY + (this.imageHeight / 2);

        double speedBlocksPerSecond = 0;

        switch(entity.getClass().getSimpleName()) {
            //                                           blocks/s  speed
            case "TigerEntity" -> speedBlocksPerSecond = (7.84 / 0.181763) * entity.getSpeed();
            case "BoaEntity" -> speedBlocksPerSecond = (4.383 / 0.16604447) * entity.getSpeed();
            case "PeacockEntity" -> speedBlocksPerSecond = (5.833 / 0.19152214) * entity.getSpeed();
        }

        String levelPoints = String.valueOf(entity.getLevelPoints());
        String entityHealth = String.valueOf(Math.round(entity.getHealth() * 2) / 2.0 + " / " + Math.round(entity.getMaxHealth() * 2) / 2.0);
        String entityDamage = String.valueOf(Math.round(entity.getDamageToClient() * 10) / 10.0);
        String entityBaseSpeed = String.valueOf(Math.round(speedBlocksPerSecond * 100) / 100.0);
        Component entitySpeed = Component.literal(entityBaseSpeed)
                .append(Component.translatable("tooltip.entitySpeed"));

        Component levelText = Component.translatable("tooltip.level");
        Component levelValue = Component.literal(String.valueOf(entity.getLevel())).withStyle(Style.EMPTY.withColor(TextColor.fromRgb(entity.getLevel() >= 50 ? 0xdd9847 : 0xb8e45a)).withBold(true));
        Component fullLevelText = Component.empty().append(levelText).append(" ").append(levelValue);

        if (this.entity.getLevel() < 50 || this.entity.getLevelPoints() > 0) graphics.drawString(this.font, levelPoints, this.entity.getLevelPoints() > 9 ? centerX + 67 : centerX + 70, centerY - 76, this.entity.getLevelPoints() > 0 ? 0xb8e45a : 0x8b8b8b);
        else {
            graphics.blit(MISC_LOCATION, centerX + 68, centerY - 77, 0, 143, 10, 10);
            graphics.drawString(this.font, String.valueOf(this.entity.getPrestigeLevel()), this.entity.getPrestigeLevel() >= 100 ? centerX + 44 : this.entity.getPrestigeLevel() >= 10 ? centerX + 51 : this.entity.getPrestigeLevel() >= 0 ? centerX + 56 : 0, centerY - 76, 0xc8f6ff);
        }

        graphics.drawString(this.font, entityHealth, centerX + 12, centerY - 60, 0x8b8b8b);
        graphics.drawString(this.font, entityDamage, centerX + 12, centerY - 42, 0x8b8b8b);
        graphics.drawString(this.font, entitySpeed, centerX + 12, centerY - 25, 0x8b8b8b);

        graphics.drawString(this.font, fullLevelText, centerX - (this.font.width(fullLevelText) / 2), centerY + 100, 0xFFFFFF);
    }

    private void renderEffects(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        int i = this.leftPos + this.imageWidth + 2;
        int j = this.width - i;
        Collection<MobEffectInstance> collection = entity.getActiveEffects();
        if (!collection.isEmpty() && j >= 32) {
            boolean flag = j >= 120;
            ScreenEvent.RenderInventoryMobEffects event = ClientHooks.onScreenPotionSize(this, j, !flag, i);
            if (event.isCanceled()) {
                return;
            }

            flag = !event.isCompact();
            i = event.getHorizontalOffset();
            int k = 33;
            if (collection.size() > 5) {
                k = 132 / (collection.size() - 1);
            }

            Iterable<MobEffectInstance> iterable = (Iterable)collection.stream().filter(ClientHooks::shouldRenderEffect).sorted().collect(Collectors.toList());
            this.renderBackgrounds(guiGraphics, i, k, iterable, flag);
            this.renderIcons(guiGraphics, i, k, iterable, flag);
            if (flag) {
                this.renderLabels(guiGraphics, i, k, iterable);
            } else if (mouseX >= i && mouseX <= i + 33) {
                int l = this.topPos;
                MobEffectInstance mobeffectinstance = null;

                for(MobEffectInstance mobeffectinstance1 : iterable) {
                    if (mouseY >= l && mouseY <= l + k) {
                        mobeffectinstance = mobeffectinstance1;
                    }

                    l += k;
                }

                if (mobeffectinstance != null) {
                    List<Component> list = List.of(this.getEffectName(mobeffectinstance), MobEffectUtil.formatDuration(mobeffectinstance, 1.0F, this.minecraft.level.tickRateManager().tickrate()));
                    list = ClientHooks.getEffectTooltip(this, mobeffectinstance, list);
                    guiGraphics.renderTooltip(this.font, list, Optional.empty(), mouseX, mouseY);
                }
            }
        }

    }

    private void renderBackgrounds(GuiGraphics guiGraphics, int renderX, int yOffset, Iterable<MobEffectInstance> effects, boolean isSmall) {
        int i = this.topPos;

        for(MobEffectInstance mobeffectinstance : effects) {
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

        for(MobEffectInstance mobeffectinstance : effects) {
            IClientMobEffectExtensions renderer = IClientMobEffectExtensions.of(mobeffectinstance);
            if (renderer.renderInventoryIcon(mobeffectinstance, this, guiGraphics, renderX + (isSmall ? 6 : 7), i, 0)) {
                i += yOffset;
            } else {
                Holder<MobEffect> holder = mobeffectinstance.getEffect();
                TextureAtlasSprite textureatlassprite = mobeffecttexturemanager.get(holder);
                guiGraphics.blit(renderX + (isSmall ? 6 : 7), i + 7, 0, 18, 18, textureatlassprite);
                i += yOffset;
            }
        }

    }

    private void renderLabels(GuiGraphics guiGraphics, int renderX, int yOffset, Iterable<MobEffectInstance> effects) {
        int i = this.topPos;

        for(MobEffectInstance mobeffectinstance : effects) {
            IClientMobEffectExtensions renderer = IClientMobEffectExtensions.of(mobeffectinstance);
            if (renderer.renderInventoryText(mobeffectinstance, this, guiGraphics, renderX, i, 0)) {
                i += yOffset;
            } else {
                Component component = this.getEffectName(mobeffectinstance);
                guiGraphics.drawString(this.font, component, renderX + 10 + 18, i + 6, 16777215);
                Component component1 = MobEffectUtil.formatDuration(mobeffectinstance, 1.0F, this.minecraft.level.tickRateManager().tickrate());
                guiGraphics.drawString(this.font, component1, renderX + 10 + 18, i + 6 + 10, 8355711);
                i += yOffset;
            }
        }

    }

    private Component getEffectName(MobEffectInstance effect) {
        MutableComponent mutablecomponent = ((MobEffect)effect.getEffect().value()).getDisplayName().copy();
        if (effect.getAmplifier() >= 1 && effect.getAmplifier() <= 9) {
            MutableComponent var10000 = mutablecomponent.append(CommonComponents.SPACE);
            int var10001 = effect.getAmplifier();
            var10000.append(Component.translatable("enchantment.level." + (var10001 + 1)));
        }

        return mutablecomponent;
    }
}