package net.tiew.operationWild.entity.config;
import net.minecraft.world.item.Item;

public interface IOWRideable {
    float vehicleRunSpeedMultiplier();
    float vehicleWalkSpeedMultiplier();
    float vehicleComboSpeedMultiplier();
    float vehicleWaterSpeedDivider();
    boolean canIncreasesSpeedDuringSprint();

    /**
     * Vitesse à laquelle l'élan se construit au sprint, pour les espèces qui déclarent
     * {@link #canIncreasesSpeedDuringSprint()}.
     *
     * <p>La jauge d'accélération monte d'un point par tick, soit cinq secondes pour atteindre son
     * plafond. Ce facteur la ralentit ou l'accélère : {@code 0.5} demande dix secondes de course
     * avant la pleine allure, ce qui convient à une bête lourde qui doit s'arracher avant de lancer
     * ses quatre tonnes.</p>
     *
     * <p>Facultatif : sans redéfinition, l'élan se construit à la cadence d'origine.</p>
     */
    default float sprintAccelerationMultiplier() {
        return 1.0f;
    }

    boolean isChangeSpeedDuringCombo();
    Item acceptSaddle();
    float getMaxVitalEnergy();
    float getVitalEnergyRecuperation();
}
