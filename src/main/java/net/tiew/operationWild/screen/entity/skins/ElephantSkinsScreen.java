package net.tiew.operationWild.screen.entity.skins;

import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.tiew.operationWild.entity.OWEntity;
import net.tiew.operationWild.screen.entity.OWSkinsInterface;

@OnlyIn(Dist.CLIENT)
public class ElephantSkinsScreen extends OWSkinsInterface {
    private Button skinButton1;
    private Button skinButton2;
    private Button skinButton3;
    private Button skinButton7;

    private int numberOfSkins = 4;

    public ElephantSkinsScreen() {
        super();
    }

    @Override
    protected void prepareGhostEntity(OWEntity ghost) {
        ghost.setSaddle(false);
    }

    @Override
    protected SkinInfo getSkinInfo(int skinIndex) {
        return switch (skinIndex) {
            case 1 -> SkinInfo.level("tooltip.elephantSkin1", "tooltip.elephantSkin1.desc", 50);
            case 2 -> SkinInfo.free("tooltip.elephantSkin2", "tooltip.elephantSkin2.desc");
            case 3 -> SkinInfo.free("tooltip.elephantSkin3", "tooltip.elephantSkin3.desc");
            case 7 -> SkinInfo.free("tooltip.elephantSkin7", "tooltip.elephantSkin7.desc");
            default -> null;
        };
    }

    /**
     * L'aperçu est plus petit que celui des autres espèces : à l'échelle 2,5 du kodiak, une bête de
     * 3,2 blocs de haut déborde du cadre.
     */
    @Override
    protected void initEntityScale() {
        if (this.entity != null && "ElephantEntity".equals(this.entity.getClass().getSimpleName())) {
            entityScale = (int) (20 * 1.2f);
        }
    }

    @Override
    protected void initLockedSkins() {
        for (int i = 1; i <= numberOfSkins; i++) {
            lockedSkins.put(i, false);
        }
    }

    @Override
    protected void initSkinPrices() {
    }

    @Override
    protected void createAndAddButtons() {
        LEGENDARY_SKIN.clear();
        EPIC_SKIN.clear();
        HALLOWEEN_SKIN.clear();
        RARE_SKIN.clear();
        COMMON_SKIN.clear();

        skinButton1 = createSkinButton(Component.translatable("tooltip.elephantSkin1"), 1, LEGENDARY_SKIN);
        skinButton2 = createSkinButton(Component.translatable("tooltip.elephantSkin2"), 2, HALLOWEEN_SKIN);
        skinButton3 = createSkinButton(Component.translatable("tooltip.elephantSkin3"), 3, HALLOWEEN_SKIN);
        skinButton7 = createSkinButton(Component.translatable("tooltip.elephantSkin7"), 7, COMMON_SKIN);

        updateButtonColors();
        addButtonsToList();
    }

    @Override
    protected int getSkinIndexForButton(Button button) {
        if (button == skinButton1) return 1;
        if (button == skinButton2) return 2;
        if (button == skinButton3) return 3;
        if (button == skinButton7) return 7;
        return -1;
    }

    @Override
    protected void addTooltipsToButtons() {
        if (isLocked(1)) skinButton1.setTooltip(Tooltip.create(Component.translatable("tooltip.elephantSkin1Indication")));
    }

    @Override
    protected void updateLockStates() {
        if (this.entity != null) {
            setLockState(1, entity.getLevel() < 50);
            setLockState(2, false);
            setLockState(3, false);
            setLockState(7, false);
        }
    }
}
