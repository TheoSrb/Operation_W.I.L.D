package net.tiew.operationWild.screen.entity.skins;

import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.tiew.operationWild.screen.entity.OWSkinsInterface;

@OnlyIn(Dist.CLIENT)
public class KodiakSkinsScreen extends OWSkinsInterface {
    private Button skinButton1;
    private Button skinButton7;

    private int numberOfSkins = 2;

    public KodiakSkinsScreen() {
        super();
    }

    @Override
    protected void initEntityScale() {
        if (this.entity != null && "KodiakEntity".equals(this.entity.getClass().getSimpleName())) {
            entityScale = (int) (20 * 2.5f);
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
        RARE_SKIN.clear();
        COMMON_SKIN.clear();

        skinButton1 = createSkinButton(Component.translatable("tooltip.kodiakSkin1"), 1, LEGENDARY_SKIN);
        skinButton7 = createSkinButton(Component.translatable("tooltip.kodiakSkin7"), 7, COMMON_SKIN);

        updateButtonColors();
        addButtonsToList();
    }

    @Override
    protected int getSkinIndexForButton(Button button) {
        if (button == skinButton1) return 1;
        if (button == skinButton7) return 7;
        return -1;
    }

    @Override
    protected void addTooltipsToButtons() {
        if (isLocked(1)) skinButton1.setTooltip(Tooltip.create(Component.translatable("tooltip.kodiakSkin1Indication")));
    }

    @Override
    protected void updateLockStates() {
        if (this.entity != null) {
            setLockState(1, entity.getLevel() < 50);
            setLockState(7, false);
        }
    }
}