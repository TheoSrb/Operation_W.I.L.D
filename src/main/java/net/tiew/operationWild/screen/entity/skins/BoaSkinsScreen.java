package net.tiew.operationWild.screen.entity.skins;

import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.tiew.operationWild.entity.OWEntity;
import net.tiew.operationWild.core.OWDatasSave;
import net.tiew.operationWild.screen.entity.OWSkinsInterface;

@OnlyIn(Dist.CLIENT)
public class BoaSkinsScreen extends OWSkinsInterface {

    private Button skinButton1;

    private final int numberOfSkins = 1;

    public BoaSkinsScreen() { super(); }

    @Override
    protected void prepareGhostEntity(OWEntity ghost) {
        ghost.setSaddle(false);
    }

    @Override
    protected void initEntityScale() {
        if (this.entity != null && "BoaEntity".equals(this.entity.getClass().getSimpleName())) {
            entityScale = (int) (20 * 2.5f);
        }
    }

    @Override
    protected void initLockedSkins() {
        for (int i = 1; i <= numberOfSkins; i++) lockedSkins.put(i, false);
    }

    @Override
    protected void initSkinPrices() {
        skinPrices.put(1, 200);
    }

    @Override
    protected SkinInfo getSkinInfo(int skinIndex) {
        return switch (skinIndex) {
            case 1 -> SkinInfo.prestige("tooltip.boaSkin1", "tooltip.boaSkin1.desc", 200);
            default -> null;
        };
    }

    @Override
    protected void createAndAddButtons() {
        LEGENDARY_SKIN.clear();
        EPIC_SKIN.clear();
        HALLOWEEN_SKIN.clear();
        RARE_SKIN.clear();
        COMMON_SKIN.clear();

        skinButton1 = createSkinButton(Component.translatable("tooltip.boaSkin1"), 1, RARE_SKIN);

        updateButtonColors();
        addButtonsToList();
    }

    @Override
    protected int getSkinIndexForButton(Button button) {
        if (button == skinButton1) return 1;
        return -1;
    }

    @Override
    protected void addTooltipsToButtons() {
        // No locked-skin tooltips for now
    }

    @Override
    protected void updateLockStates() {
        if (this.entity != null) {
            setLockState(1, !OWDatasSave.hasPurchasedSkin(entity.getUUID(), 1));
        }
    }
}
