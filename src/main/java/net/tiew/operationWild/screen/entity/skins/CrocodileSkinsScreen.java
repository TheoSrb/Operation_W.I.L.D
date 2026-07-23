package net.tiew.operationWild.screen.entity.skins;

import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.tiew.operationWild.entity.OWEntity;
import net.tiew.operationWild.quests.CosmeticsQuestsRegistry;
import net.tiew.operationWild.screen.entity.OWSkinsInterface;
import net.tiew.operationWild.screen.entity.SkinRarity;
import org.checkerframework.checker.units.qual.C;

@OnlyIn(Dist.CLIENT)
public class CrocodileSkinsScreen extends OWSkinsInterface {

    private Button skinButton1;
    private Button skinButton2;
    private Button skinButton3;
    private Button skinButton7;

    private final int numberOfSkins = 4;

    public CrocodileSkinsScreen() { super(); }

    @Override
    protected void prepareGhostEntity(OWEntity ghost) {
        ghost.setSaddle(false);
    }

    @Override
    protected void initEntityScale() {
        if (this.entity != null && "CrocodileEntity".equals(this.entity.getClass().getSimpleName())) {
            entityScale = (int)(20 * 2.5f);
        }
    }

    @Override
    protected void initLockedSkins() {
        for (int i = 1; i <= numberOfSkins; i++) lockedSkins.put(i, false);
    }

    @Override
    protected void initSkinPrices() {
        skinPrices.put(2, SkinRarity.LEGENDARY.cost);
        skinPrices.put(3, SkinRarity.EPIC.cost);
    }

    // =========================================================================
    // Données du panel de détails
    // =========================================================================
    @Override
    protected SkinInfo getSkinInfo(int skinIndex) {
        return switch (skinIndex) {
            case 1 -> SkinInfo.level(    "tooltip.crocodileSkin1", "tooltip.crocodileSkin1.desc", 50);
            case 2 -> SkinInfo.rarity(     "tooltip.crocodileSkin2", "tooltip.crocodileSkin2.desc", SkinRarity.LEGENDARY);
            case 3 -> SkinInfo.rarity( "tooltip.crocodileSkin3", "tooltip.crocodileSkin3.desc", SkinRarity.EPIC);
            case 7 -> SkinInfo.free(     "tooltip.crocodileSkin7", "tooltip.crocodileSkin7.desc");
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

        skinButton1 = createSkinButton(Component.translatable("tooltip.crocodileSkin1"), 1, LEGENDARY_SKIN);
        skinButton2 = createSkinButton(Component.translatable("tooltip.crocodileSkin2"), 2, LEGENDARY_SKIN);
        skinButton3 = createSkinButton(Component.translatable("tooltip.crocodileSkin3"), 3, EPIC_SKIN);
        skinButton7 = createSkinButton(Component.translatable("tooltip.crocodileSkin7"), 7, COMMON_SKIN);

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
        if (isLocked(1))
            skinButton1.setTooltip(Tooltip.create(Component.translatable("tooltip.crocodileSkin1Indication")));
    }

    @Override
    protected void updateLockStates() {
        if (this.entity != null) {
            CosmeticsQuestsRegistry.getById(0).update(entity);
            CosmeticsQuestsRegistry.getById(1).update(entity);

            setLockState(1, entity.getLevel() < 50);
            setLockState(2, !entity.isSkinUnlocked(2));
            setLockState(3, !entity.isSkinUnlocked(3));
            setLockState(7, false);
        }
    }
}