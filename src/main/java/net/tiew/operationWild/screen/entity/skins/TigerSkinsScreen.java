package net.tiew.operationWild.screen.entity.skins;

import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.tiew.operationWild.entity.custom.living.TigerEntity;
import net.tiew.operationWild.screen.entity.OWSkinsInterface;

@OnlyIn(Dist.CLIENT)
public class TigerSkinsScreen extends OWSkinsInterface {
    private Button skinButton1;
    private Button skinButton2;
    private Button skinButton3;
    private Button skinButton4;
    private Button skinButton5;
    private Button skinButton6;
    private Button skinButton7;
    private Button skinButton8;

    private int numberOfSkins = 8;

    public TigerSkinsScreen() {
        super();
    }

    @Override
    protected void initEntityScale() {
        if (this.entity != null && "TigerEntity".equals(this.entity.getClass().getSimpleName())) {
            entityScale = (int) (22 * 2.5f);
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
        skinPrices.put(3, 300);
        skinPrices.put(5, 200);
        skinPrices.put(6, 500);
    }

    @Override
    protected void createAndAddButtons() {
        LEGENDARY_SKIN.clear();
        EPIC_SKIN.clear();
        RARE_SKIN.clear();
        COMMON_SKIN.clear();

        skinButton1 = createSkinButton(Component.translatable("tooltip.tigerSkin1"), 1, LEGENDARY_SKIN);
        skinButton2 = createSkinButton(Component.translatable("tooltip.tigerSkin2"), 2, RARE_SKIN);
        skinButton3 = createSkinButton(Component.translatable("tooltip.tigerSkin3"), 3, EPIC_SKIN);
        skinButton4 = createSkinButton(Component.translatable("tooltip.tigerSkin4"), 4, RARE_SKIN);
        skinButton5 = createSkinButton(Component.translatable("tooltip.tigerSkin5"), 5, RARE_SKIN);
        skinButton6 = createSkinButton(Component.translatable("tooltip.tigerSkin6"), 6, LEGENDARY_SKIN);
        skinButton7 = createSkinButton(Component.translatable("tooltip.tigerSkin7"), 7, COMMON_SKIN);
        skinButton8 = createSkinButton(Component.translatable("tooltip.tigerSkin8"), 8, EPIC_SKIN);

        updateButtonColors();
        addButtonsToList();
    }

    @Override
    protected int getSkinIndexForButton(Button button) {
        if (button == skinButton1) return 1;
        if (button == skinButton2) return 2;
        if (button == skinButton3) return 3;
        if (button == skinButton4) return 4;
        if (button == skinButton5) return 5;
        if (button == skinButton6) return 6;
        if (button == skinButton7) return 7;
        if (button == skinButton8) return 8;
        return -1;
    }

    @Override
    protected void addTooltipsToButtons() {
        if (isLocked(1)) skinButton1.setTooltip(Tooltip.create(Component.translatable("tooltip.tigerSkin1Indication")));
        if (isLocked(2)) skinButton2.setTooltip(Tooltip.create(Component.translatable("tooltip.tigerSkin2Indication")));
        if (isLocked(3)) skinButton3.setTooltip(Tooltip.create(Component.translatable("tooltip.tigerSkin3Indication", getSkinPrice(3))));
        if (isLocked(4)) skinButton4.setTooltip(Tooltip.create(Component.translatable("tooltip.tigerSkin4Indication")));
        if (isLocked(5)) skinButton5.setTooltip(Tooltip.create(Component.translatable("tooltip.tigerSkin5Indication", getSkinPrice(5))));
        if (isLocked(6)) skinButton6.setTooltip(Tooltip.create(Component.translatable("tooltip.tigerSkin6Indication", getSkinPrice(6))));
        if (isLocked(8)) skinButton7.setTooltip(Tooltip.create(Component.translatable("tooltip.tigerSkin8Indication")));
    }

    @Override
    protected void updateLockStates() {
        if (this.entity != null) {
            setLockState(1, this.entity.getLevel() < 50);
            setLockState(2, false);
            setLockState(3, entity instanceof TigerEntity tigerEntity && !tigerEntity.skinPizzaChefIsAlreadyBuying());
            setLockState(4, false);
            setLockState(5, entity instanceof TigerEntity tigerEntity && !tigerEntity.skinDetectiveIsAlreadyBuying());
            setLockState(6, entity instanceof TigerEntity tigerEntity && !tigerEntity.skinVirusIsAlreadyBuying());
            setLockState(7, false);
            setLockState(8, false);
        }
    }
}