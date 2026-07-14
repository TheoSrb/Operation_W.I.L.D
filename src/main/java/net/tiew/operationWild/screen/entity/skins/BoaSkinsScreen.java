package net.tiew.operationWild.screen.entity.skins;

import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.tiew.operationWild.entity.OWEntity;
import net.tiew.operationWild.screen.entity.OWSkinsInterface;
import net.tiew.operationWild.screen.entity.SkinRarity;

@OnlyIn(Dist.CLIENT)
public class BoaSkinsScreen extends OWSkinsInterface {

    private Button skinButton1;
    private Button skinButton4;
    private Button skinButton5;
    private Button skinButton7;

    private final int numberOfSkins = 7;

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
        skinPrices.put(4, SkinRarity.LEGENDARY.cost);
        skinPrices.put(5, SkinRarity.RARE.cost);
    }

    @Override
    protected SkinInfo getSkinInfo(int skinIndex) {
        return switch (skinIndex) {
            case 1 -> SkinInfo.level(    "tooltip.boaSkin1", "tooltip.boaSkin1.desc", 50);
            case 4 -> SkinInfo.rarity( "tooltip.boaSkin4", "tooltip.boaSkin4.desc", SkinRarity.LEGENDARY);
            case 5 -> SkinInfo.rarity( "tooltip.boaSkin5", "tooltip.boaSkin5.desc", SkinRarity.RARE);
            case 7 -> SkinInfo.free("tooltip.boaSkin7", "tooltip.boaSkin7.desc");
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

        skinButton1 = createSkinButton(Component.translatable("tooltip.boaSkin1"), 1, LEGENDARY_SKIN);
        skinButton4 = createSkinButton(Component.translatable("tooltip.boaSkin4"), 4, LEGENDARY_SKIN);
        skinButton5 = createSkinButton(Component.translatable("tooltip.boaSkin5"), 5, RARE_SKIN);
        skinButton7 = createSkinButton(Component.translatable("tooltip.boaSkin7"), 7, COMMON_SKIN);

        updateButtonColors();
        addButtonsToList();
    }

    @Override
    protected int getSkinIndexForButton(Button button) {
        if (button == skinButton1) return 1;
        if (button == skinButton4) return 4;
        if (button == skinButton5) return 5;
        if (button == skinButton7) return 7;
        return -1;
    }

    @Override
    protected void addTooltipsToButtons() {
        // No locked-skin tooltips for now
    }

    @Override
    protected void updateLockStates() {
        if (this.entity != null) {
            setLockState(1, entity.getLevel() < 50);
            setLockState(4, !entity.isSkinUnlocked(4));
            setLockState(5, !entity.isSkinUnlocked(5));
            setLockState(7, false);
        }
    }
}
