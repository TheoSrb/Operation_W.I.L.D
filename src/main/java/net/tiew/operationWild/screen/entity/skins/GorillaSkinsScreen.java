package net.tiew.operationWild.screen.entity.skins;

import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.tiew.operationWild.entity.OWEntity;
import net.tiew.operationWild.entity.animals.terrestrial.GorillaEntity;
import net.tiew.operationWild.screen.entity.OWSkinsInterface;

@OnlyIn(Dist.CLIENT)
public class GorillaSkinsScreen extends OWSkinsInterface {

    private Button skinButton0;

    public GorillaSkinsScreen() { super(); }

    @Override
    protected void prepareGhostEntity(OWEntity ghost) {
        ghost.setSaddle(false);
    }

    @Override
    protected SkinInfo getSkinInfo(int skinIndex) {
        return skinIndex == GorillaEntity.DEFAULT_SKIN_INDEX
                ? SkinInfo.free("tooltip.gorillaSkin0", "tooltip.gorillaSkin0.desc")
                : null;
    }

    @Override
    protected void initEntityScale() {
        if (this.entity != null && "GorillaEntity".equals(this.entity.getClass().getSimpleName())) {
            entityScale = (int) (20 * 2.2f);
        }
    }

    @Override
    protected void initLockedSkins() {
        lockedSkins.put(GorillaEntity.DEFAULT_SKIN_INDEX, false);
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

        skinButton0 = createSkinButton(Component.translatable("tooltip.gorillaSkin0"), GorillaEntity.DEFAULT_SKIN_INDEX, COMMON_SKIN);

        updateButtonColors();
        addButtonsToList();
    }

    @Override
    protected int getSkinIndexForButton(Button button) {
        return button == skinButton0 ? GorillaEntity.DEFAULT_SKIN_INDEX : -1;
    }

    @Override
    protected void addTooltipsToButtons() {
    }

    @Override
    protected void updateLockStates() {
    }
}
