package net.tiew.operationWild.entity.config;

public class OWEntityConfig {
    public enum Archetypes {
        TANK(1.2f, 1.0f, 0.8f, 1.3f),
        ASSASSIN(0.8f, 1.3f, 1.0f, 0.9f),
        MARAUDER(0.9f, 1.1f, 1.25f, 1.0f),
        HEALER(1.2f, 0.7f, 1.0f, 1.1f),
        BERSERKER(1.2f, 1.1f, 1.0f, 0.8f),
        SCOUT(1.0f, 0.8f, 1.4f, 1.4f),
        NORMAL(1.0f, 1.0f, 1.0f, 1.0f);

        private final float healthMultiplier;
        private final float damageMultiplier;
        private final float speedMultiplier;
        private final float energyMultiplier;

        Archetypes(float healthMultiplier, float damageMultiplier, float speedMultiplier, float energyMultiplier) {
            this.healthMultiplier = healthMultiplier;
            this.damageMultiplier = damageMultiplier;
            this.speedMultiplier = speedMultiplier;
            this.energyMultiplier = energyMultiplier;
        }

        public float getHealthMultiplier() {
            return healthMultiplier;
        }

        public float getDamageMultiplier() {
            return damageMultiplier;
        }

        public float getSpeedMultiplier() {
            return speedMultiplier;
        }

        public float getEnergyMultiplier() {
            return energyMultiplier;
        }
    }

    public enum Diet {
        VEGETARIAN(0.75f),
        CARNIVOROUS(1.0f),
        OMNIVOROUS(0.825f);

        private final float saturationMultiplier;

        Diet(float saturationMultiplier) {
            this.saturationMultiplier = saturationMultiplier;
        }

        public float getSaturationMultiplier() {
            return saturationMultiplier;
        }
    }

    public enum Temperament {
        PASSIVE,
        NEUTRAL,
        AGGRESSIVE
    }
}
