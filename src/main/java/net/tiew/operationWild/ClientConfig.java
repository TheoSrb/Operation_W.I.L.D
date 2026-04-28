package net.tiew.operationWild;

import net.neoforged.neoforge.common.ModConfigSpec;

public class ClientConfig {

    public static final ModConfigSpec SPEC;
    public static final ModConfigSpec.DoubleValue CAMERA_SHAKE_INTENSITY;

    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();

        builder.comment("Paramètres visuels");
        CAMERA_SHAKE_INTENSITY = builder
                .comment("Intensité des shakes de caméra (0.0 = désactivé, 1.0 = normal, 2.0 = maximum)")
                .translation("config.ow.camera_shake_intensity")
                .defineInRange("cameraShakeIntensity", 1.0, 0.0, 2.0);

        SPEC = builder.build();
    }
}