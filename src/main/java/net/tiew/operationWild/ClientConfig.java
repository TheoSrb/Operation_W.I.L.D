package net.tiew.operationWild;

import net.neoforged.neoforge.common.ModConfigSpec;

public class ClientConfig {

    public static final ModConfigSpec SPEC;
    public static final ModConfigSpec.DoubleValue CAMERA_SHAKE_INTENSITY;
    public static final ModConfigSpec.BooleanValue LEVEL_UP_ANIMATION;
    public static final ModConfigSpec.BooleanValue TUTORIAL_HINTS;
    public static final ModConfigSpec.BooleanValue MOUNT_ALERTS;
    public static final ModConfigSpec.BooleanValue WIND_RUSH;
    public static final ModConfigSpec.BooleanValue FEAR_EFFECTS;

    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();

        builder.comment("Paramètres visuels");
        CAMERA_SHAKE_INTENSITY = builder
                .comment("Intensité des shakes de caméra (0.0 = désactivé, 1.0 = normal, 2.0 = maximum)")
                .translation("config.ow.camera_shake_intensity")
                .defineInRange("cameraShakeIntensity", 1.0, 0.0, 2.0);
        LEVEL_UP_ANIMATION = builder
                .comment("Animation à l'écran quand la monture passe un niveau")
                .translation("config.ow.level_up_animation")
                .define("levelUpAnimation", true);
        TUTORIAL_HINTS = builder
                .comment("Indications de didacticiel à l'écran")
                .translation("config.ow.tutorial_hints")
                .define("tutorialHints", true);
        MOUNT_ALERTS = builder
                .comment("Vignette d'alerte quand la monture est blessée ou épuisée")
                .translation("config.ow.mount_alerts")
                .define("mountAlerts", true);
        WIND_RUSH = builder
                .comment("Effet de vent (traînées, champ de vision) aux grandes vitesses à dos de monture")
                .translation("config.ow.wind_rush")
                .define("windRush", true);
        FEAR_EFFECTS = builder
                .comment("Effets d'écran de la peur primitive de la monture")
                .translation("config.ow.fear_effects")
                .define("fearEffects", true);

        SPEC = builder.build();
    }

    public static boolean get(ModConfigSpec.BooleanValue value) {
        return SPEC.isLoaded() ? value.get() : value.getDefault();
    }
}
