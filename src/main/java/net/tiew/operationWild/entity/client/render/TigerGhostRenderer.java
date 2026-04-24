package net.tiew.operationWild.entity.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.tiew.operationWild.OperationWild;
import net.tiew.operationWild.entity.OWEntityRegistry;
import net.tiew.operationWild.entity.animals.terrestrial.TigerEntity;
import net.tiew.operationWild.entity.attacks.OWAttackLogic;

@EventBusSubscriber(modid = OperationWild.MOD_ID, bus = EventBusSubscriber.Bus.GAME, value = Dist.CLIENT)
public class TigerGhostRenderer {

    // Fixed opacity of the ghost tiger during the entire charge
    private static final float GHOST_ALPHA = 0.45f;
    // World-space distance the ghost travels at full charge (3s), matches max leap range
    private static final float GHOST_MAX_DISTANCE = 22f;

    private static TigerEntity ghostEntity = null;

    @SubscribeEvent
    public static void onRenderLevel(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_ENTITIES) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) return;

        // Auto-clear if the player is no longer riding a tiger
        if (!(mc.player.getRootVehicle() instanceof TigerEntity tiger)) {
            ghostEntity = null;
            // Only reset isCharging if a tiger ghost attack was active — do NOT touch it for other entities (croc, etc.)
            if (OWAttackLogic.isShowGhost()) {
                OWAttackLogic.isCharging = false;
            }
            return;
        }

        if (!OWAttackLogic.isCharging || !OWAttackLogic.isShowGhost()) {
            ghostEntity = null;
            return;
        }

        // Progress [0..1] over the full charge window of the active attack
        long elapsed = System.currentTimeMillis() - OWAttackLogic.chargeStartMs;
        float progress = (float) Math.min((double) elapsed / OWAttackLogic.getCurrentMaxChargeMs(), 1.0);

        // Le fantôme se déplace dans la direction actuelle du regard du tigre (dynamique)
        Vec3 tigerPos = tiger.position();
        Vec3 lookDir = tiger.getLookAngle();
        Vec3 horizontal = new Vec3(lookDir.x, 0, lookDir.z);
        if (horizontal.lengthSqr() > 1.0E-7) horizontal = horizontal.normalize();
        Vec3 ghostPos = tigerPos.add(horizontal.scale(progress * GHOST_MAX_DISTANCE));

        // Lazily create the ghost entity (never added to the level)
        if (ghostEntity == null) {
            ghostEntity = OWEntityRegistry.TIGER.get().create(mc.level);
            if (ghostEntity == null) return;
            // Démarre l'animation PREPARING_LEAP une seule fois à la création
            ghostEntity.isPreparing = true;
            ghostEntity.preparingLeapAnimationState.start(tiger.tickCount);
        }

        // Mirror the real tiger's visual state.
        // Les champs *O (valeurs du tick précédent) doivent être identiques aux valeurs courantes
        // pour éviter que l'interpolation de rotation fasse tourner le fantôme à l'apparition.
        ghostEntity.setVariant(tiger.getVariant());
        ghostEntity.setInitialVariant(tiger.getInitialVariant());
        ghostEntity.setPos(ghostPos.x, ghostPos.y, ghostPos.z);
        ghostEntity.isPreparing = true;
        // Orientation calquée sur le tigre réel (suivi dynamique)
        float yRot = tiger.getYRot();
        ghostEntity.setYRot(yRot);
        ghostEntity.yRotO = yRot;
        ghostEntity.yBodyRot = tiger.yBodyRot;
        ghostEntity.yBodyRotO = tiger.yBodyRot;
        ghostEntity.yHeadRot = tiger.yHeadRot;
        ghostEntity.yHeadRotO = tiger.yHeadRot;

        Vec3 camPos = event.getCamera().getPosition();
        PoseStack poseStack = event.getPoseStack();
        MultiBufferSource.BufferSource bufferSource = mc.renderBuffers().bufferSource();

        TigerRenderer.ghostAlpha = GHOST_ALPHA;
        try {
            mc.getEntityRenderDispatcher().render(
                    ghostEntity,
                    ghostPos.x - camPos.x,
                    ghostPos.y - camPos.y,
                    ghostPos.z - camPos.z,
                    ghostEntity.getYRot(),
                    event.getPartialTick().getGameTimeDeltaPartialTick(true), // DeltaTracker → float
                    poseStack,
                    bufferSource,
                    LightTexture.FULL_BRIGHT
            );
        } finally {
            TigerRenderer.ghostAlpha = 0f;
        }
    }
}
