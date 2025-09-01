package net.tiew.operationWild.screen.entity.skins;

import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.tiew.operationWild.entity.animals.terrestrial.BoaEntity;
import net.tiew.operationWild.screen.entity.OWSkinsInterface;

@OnlyIn(Dist.CLIENT)
public class BoaSkinsScreen extends OWSkinsInterface {
    private Button skinButton1;
    private Button skinButton2;
    private Button skinButton3;
    private Button skinButton4;
    private Button skinButton5;
    private Button skinButton6;
    private Button skinButton7;

    private int numberOfSkins = 7;

    public BoaSkinsScreen() {
        super();
    }

    @Override
    protected void initEntityScale() {
        if (this.entity != null && "BoaEntity".equals(this.entity.getClass().getSimpleName())) {
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
        skinPrices.put(2, 200);
    }

    @Override
    protected void createAndAddButtons() {
        LEGENDARY_SKIN.clear();
        EPIC_SKIN.clear();
        RARE_SKIN.clear();
        COMMON_SKIN.clear();

        skinButton1 = createSkinButton(Component.translatable("tooltip.boaSkin1"), 1, LEGENDARY_SKIN);
        skinButton2 = createSkinButton(Component.translatable("tooltip.boaSkin2"), 2, RARE_SKIN);
        skinButton3 = createSkinButton(Component.translatable("tooltip.boaSkin3"), 3, EPIC_SKIN);
        skinButton4 = createSkinButton(Component.translatable("tooltip.boaSkin4"), 4, LEGENDARY_SKIN);
        skinButton5 = createSkinButton(Component.translatable("tooltip.boaSkin5"), 5, EPIC_SKIN);
        skinButton6 = createSkinButton(Component.translatable("tooltip.boaSkin6"), 6, RARE_SKIN);
        skinButton7 = createSkinButton(Component.translatable("tooltip.boaSkin7"), 7, COMMON_SKIN);

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
        return -1;
    }

    @Override
    protected void addTooltipsToButtons() {
        if (isLocked(1)) skinButton1.setTooltip(Tooltip.create(Component.translatable("tooltip.boaSkin1Indication")));
        if (isLocked(2)) skinButton2.setTooltip(Tooltip.create(Component.translatable("tooltip.boaSkin2Indication",getSkinPrice(2))));
    }

    @Override
    protected void updateLockStates() {
        if (this.entity != null) {
            setLockState(1, entity.getLevel() < 50);
            setLockState(2, entity instanceof BoaEntity boaEntity && !boaEntity.skinVikingIsAlreadyBuying());
            setLockState(3, false);
            setLockState(4, false);
            setLockState(5, false);
            setLockState(6, false);
            setLockState(7, false);
        }
    }
}