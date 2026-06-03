package net.tiew.operationWild.core;

import net.minecraft.world.level.GameRules;

public class OWGameRules {

    public static final GameRules.Key<GameRules.BooleanValue> ANIMALS_NO_EFFORT =
            GameRules.register(
                    "OWAnimalsNoEffort",
                    GameRules.Category.MOBS,
                    GameRules.BooleanValue.create(false)
            );

    public static void init() {
    }
}