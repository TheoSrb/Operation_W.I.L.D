package net.tiew.operationWild.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.util.Mth;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.tiew.operationWild.entity.animals.terrestrial.GorillaEntity;
import net.tiew.operationWild.entity.attacks.OWAttackLogic;
import net.tiew.operationWild.entity.attacks.OWAttacksConstants;
import net.tiew.operationWild.entity.attacks.OWAttacksHandler;

@OnlyIn(Dist.CLIENT)
public final class OWGorillaAimOverlay {

    private OWGorillaAimOverlay() {}

    private static final int ARM_LENGTH = 5;
    private static final int ARM_THICKNESS = 1;
    private static final int RADIUS_LOOSE = 20;
    private static final int RADIUS_TIGHT = 7;

    private static final int COLOR_IDLE = 0xB0D8D8D8;
    private static final int COLOR_READY = 0xE0FFC864;
    private static final int COLOR_SHADOW = 0x70000000;

    private static GorillaEntity aimingGorilla() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) return null;
        if (!(mc.player.getRootVehicle() instanceof GorillaEntity gorilla)) return null;
        if (gorilla.getPassengers().indexOf(mc.player) != 0) return null;
        if (!OWAttackLogic.isCharging) return null;
        if (OWAttackLogic.getCurrentAttackId() != OWAttacksHandler.ROCK_THROW_ID) return null;
        return gorilla;
    }

    private static float chargeFactor() {
        long elapsed = System.currentTimeMillis() - OWAttackLogic.chargeStartMs;
        long min = OWAttacksConstants.Gorilla.ROCK_THROW_CHARGE_MIN_MS;
        long max = OWAttacksConstants.Gorilla.ROCK_THROW_CHARGE_MAX_MS;
        return (float) Mth.clamp((double) (elapsed - min) / (max - min), 0.0, 1.0);
    }

    public static void render(GuiGraphics graphics, int screenWidth, int screenHeight) {
        if (aimingGorilla() == null) return;

        float charge = chargeFactor();
        int cx = screenWidth / 2;
        int cy = screenHeight / 2;
        int radius = Math.round(Mth.lerp(charge, RADIUS_LOOSE, RADIUS_TIGHT));
        int color = charge >= 1.0f ? COLOR_READY : COLOR_IDLE;

        drawArm(graphics, cx, cy - radius - ARM_LENGTH, ARM_THICKNESS, ARM_LENGTH, color);
        drawArm(graphics, cx, cy + radius, ARM_THICKNESS, ARM_LENGTH, color);
        drawArm(graphics, cx - radius - ARM_LENGTH, cy, ARM_LENGTH, ARM_THICKNESS, color);
        drawArm(graphics, cx + radius, cy, ARM_LENGTH, ARM_THICKNESS, color);

        graphics.fill(cx - 1, cy - 1, cx + 1, cy + 1, color);
    }

    private static void drawArm(GuiGraphics graphics, int x, int y, int width, int height, int color) {
        graphics.fill(x - 1, y - 1, x + width + 1, y + height + 1, COLOR_SHADOW);
        graphics.fill(x, y, x + width, y + height, color);
    }

}
