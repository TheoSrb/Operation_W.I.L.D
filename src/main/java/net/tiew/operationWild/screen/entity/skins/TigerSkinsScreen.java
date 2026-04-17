package net.tiew.operationWild.screen.entity.skins;

import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.tiew.operationWild.entity.OWEntity;
import net.tiew.operationWild.core.OWDatasSave;
import net.tiew.operationWild.quests.CosmeticsQuestsRegistry;
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

    private final int numberOfSkins = 8;

    public TigerSkinsScreen() { super(); }

    @Override
    protected void prepareGhostEntity(OWEntity ghost) {
        ghost.setSaddle(false);
    }

    @Override
    protected void initEntityScale() {
        if (this.entity != null && "TigerEntity".equals(this.entity.getClass().getSimpleName())) {
            entityScale = (int)(20 * 2.5f);
        }
    }

    @Override
    protected void initLockedSkins() {
        for (int i = 1; i <= numberOfSkins; i++) lockedSkins.put(i, false);
    }

    @Override
    protected void initSkinPrices() {
        skinPrices.put(3, 50); // skin 3 coûte 50 prestige
    }

    // =========================================================================
    // Données du panel de détails
    // =========================================================================
    @Override
    protected SkinInfo getSkinInfo(int skinIndex) {
        return switch (skinIndex) {
            case 1 -> SkinInfo.level(    "tooltip.tigerSkin1", "tooltip.tigerSkin1.desc", 50);
            case 2 -> SkinInfo.free(     "tooltip.tigerSkin2", "tooltip.tigerSkin2.desc");
            case 3 -> SkinInfo.prestige( "tooltip.tigerSkin3", "tooltip.tigerSkin3.desc", 50);
            case 4 -> SkinInfo.free(     "tooltip.tigerSkin4", "tooltip.tigerSkin4.desc");
            case 5 -> SkinInfo.free(     "tooltip.tigerSkin5", "tooltip.tigerSkin5.desc");
            case 6 -> SkinInfo.quest("tooltip.tigerSkin6", "tooltip.tigerSkin6.desc", 0);
            case 7 -> SkinInfo.free(     "tooltip.tigerSkin7", "tooltip.tigerSkin7.desc");
            case 8 -> SkinInfo.free(     "tooltip.tigerSkin8", "tooltip.tigerSkin8.desc");
            default -> null;
        };
    }

    // =========================================================================
    // Boutons
    // =========================================================================
    @Override
    protected void createAndAddButtons() {
        LEGENDARY_SKIN.clear();
        EPIC_SKIN.clear();
        HALLOWEEN_SKIN.clear();
        RARE_SKIN.clear();
        COMMON_SKIN.clear();

        skinButton1 = createSkinButton(Component.translatable("tooltip.tigerSkin1"), 1, LEGENDARY_SKIN);
        skinButton2 = createSkinButton(Component.translatable("tooltip.tigerSkin2"), 2, RARE_SKIN);
        skinButton3 = createSkinButton(Component.translatable("tooltip.tigerSkin3"), 3, EPIC_SKIN);
        skinButton4 = createSkinButton(Component.translatable("tooltip.tigerSkin4"), 4, EPIC_SKIN);
        skinButton5 = createSkinButton(Component.translatable("tooltip.tigerSkin5"), 5, EPIC_SKIN);
        skinButton6 = createSkinButton(Component.translatable("tooltip.tigerSkin6"), 6, COMMON_SKIN);
        skinButton7 = createSkinButton(Component.translatable("tooltip.tigerSkin7"), 7, RARE_SKIN);
        skinButton8 = createSkinButton(Component.translatable("tooltip.tigerSkin8"), 8, COMMON_SKIN);

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
        if (isLocked(1))
            skinButton1.setTooltip(Tooltip.create(Component.translatable("tooltip.tigerSkin1Indication")));
    }

    @Override
    protected void updateLockStates() {
        if (this.entity != null) {
            CosmeticsQuestsRegistry.getById(0).update(entity.getUUID());

            setLockState(1, entity.getLevel() < 50);
            setLockState(2, false);
            setLockState(3, !OWDatasSave.hasPurchasedSkin(entity.getUUID(), 3));
            setLockState(4, false);
            setLockState(5, false);
            setLockState(6, !CosmeticsQuestsRegistry.isCompleted(0, entity.getUUID()));
            setLockState(7, false);
            setLockState(8, false);
        }
    }
}