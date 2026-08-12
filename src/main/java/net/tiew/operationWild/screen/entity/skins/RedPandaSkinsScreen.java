package net.tiew.operationWild.screen.entity.skins;

import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.tiew.operationWild.entity.OWEntity;
import net.tiew.operationWild.screen.entity.OWSkinsInterface;

@OnlyIn(Dist.CLIENT)
public class RedPandaSkinsScreen extends OWSkinsInterface {

    private Button skinButton1;
    private Button skinButton2;

    private final int numberOfSkins = 1;


    public RedPandaSkinsScreen() { super(); }

    @Override
    protected void prepareGhostEntity(OWEntity ghost) {
        ghost.setSaddle(false);
    }

    @Override
    protected SkinInfo getSkinInfo(int skinIndex) {
        return switch (skinIndex) {
            case 1 -> SkinInfo.level("tooltip.redPandaSkin1", "tooltip.redPandaSkin1.desc", 50);
            // Le pelage naturel est une entrée comme une autre, sans quoi on peut mettre un skin
            // cosmétique mais jamais le retirer. Même convention que le tigre, dont l'index 8 joue
            // ce rôle : l'index ne correspond à aucun cosmétique, donc changeSkin rend la variante
            // d'origine par son cas par défaut.
            case 2 -> SkinInfo.free("tooltip.redPandaSkin0", "tooltip.redPandaSkin0.desc");
            default -> null;
        };
    }

    @Override
    protected void initEntityScale() {
        if (this.entity != null && "RedPandaEntity".equals(this.entity.getClass().getSimpleName())) {
            entityScale = (int) (20 * 4.5f);
        }
    }

    @Override
    protected void initLockedSkins() {
        for (int i = 1; i <= numberOfSkins; i++) {
            lockedSkins.put(i, false);
        }
        lockedSkins.put(net.tiew.operationWild.entity.animals.terrestrial.RedPandaEntity.DEFAULT_SKIN_INDEX, false);
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

        skinButton1 = createSkinButton(Component.translatable("tooltip.redPandaSkin1"), 1, LEGENDARY_SKIN);
        skinButton2 = createSkinButton(Component.translatable("tooltip.redPandaSkin0"), net.tiew.operationWild.entity.animals.terrestrial.RedPandaEntity.DEFAULT_SKIN_INDEX, COMMON_SKIN);

        updateButtonColors();
        addButtonsToList();
    }

    @Override
    protected int getSkinIndexForButton(Button button) {
        if (button == skinButton1) return 1;
        if (button == skinButton2) return net.tiew.operationWild.entity.animals.terrestrial.RedPandaEntity.DEFAULT_SKIN_INDEX;
        return -1;
    }

    @Override
    protected void addTooltipsToButtons() {
        if (isLocked(1)) skinButton1.setTooltip(Tooltip.create(Component.translatable("tooltip.redPandaSkin1Indication")));
    }

    @Override
    protected void updateLockStates() {
        if (this.entity != null) {
            setLockState(1, entity.getLevel() < 50);
        }
    }
}
