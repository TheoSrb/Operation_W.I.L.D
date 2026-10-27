package net.tiew.operationWild.event;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.CalculatePlayerTurnEvent;
import net.neoforged.neoforge.client.event.ComputeFovModifierEvent;
import net.neoforged.neoforge.client.event.RenderGuiLayerEvent;
import net.neoforged.neoforge.client.event.RenderHandEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;
import net.tiew.operationWild.OperationWild;
import net.tiew.operationWild.item.custom.SyringeRifleItem;

@EventBusSubscriber(modid = OperationWild.MOD_ID, bus = EventBusSubscriber.Bus.GAME, value = Dist.CLIENT)
public class SyringeRifleClientEvents {
    private static final ResourceLocation SPYGLASS_SCOPE = ResourceLocation.withDefaultNamespace("textures/misc/spyglass_scope.png");
    private static final float SCOPE_FOV_MODIFIER = 0.1F;
    private static final float AIM_FOV_ZOOM = 0.15F;

    private static float scopeScale = 0.5F;

    private static boolean isScopingInFirstPerson(Player player) {
        return Minecraft.getInstance().options.getCameraType().isFirstPerson() && SyringeRifleItem.isScoping(player);
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onComputeFov(ComputeFovModifierEvent event) {
        Player player = event.getPlayer();
        if (isScopingInFirstPerson(player)) {
            event.setNewFovModifier(SCOPE_FOV_MODIFIER);
        } else if (SyringeRifleItem.isAiming(player)) {
            float aim = SyringeRifleItem.getAimProgress(player, 0.0F);
            event.setNewFovModifier(event.getNewFovModifier() * (1.0F - aim * aim * AIM_FOV_ZOOM));
        }
    }

    @SubscribeEvent
    public static void onCalculatePlayerTurn(CalculatePlayerTurnEvent event) {
        Player player = Minecraft.getInstance().player;
        if (player == null || event.getCinematicCameraEnabled() || !isScopingInFirstPerson(player)) return;

        double scaled = event.getMouseSensitivity() * 0.6 + 0.2;
        event.setMouseSensitivity((scaled / 2.0 - 0.2) / 0.6);
    }

    @SubscribeEvent
    public static void onRenderHand(RenderHandEvent event) {
        Player player = Minecraft.getInstance().player;
        if (player != null && isScopingInFirstPerson(player)) event.setCanceled(true);
    }

    @SubscribeEvent
    public static void onRenderCameraOverlays(RenderGuiLayerEvent.Post event) {
        if (!event.getName().equals(VanillaGuiLayers.CAMERA_OVERLAYS)) return;

        Player player = Minecraft.getInstance().player;
        if (player == null || !isScopingInFirstPerson(player)) {
            scopeScale = 0.5F;
            return;
        }

        scopeScale = Mth.lerp(0.5F * event.getPartialTick().getGameTimeDeltaTicks(), scopeScale, 1.125F);
        renderScope(event.getGuiGraphics(), scopeScale);
    }

    private static void renderScope(GuiGraphics guiGraphics, float scale) {
        float shortest = (float) Math.min(guiGraphics.guiWidth(), guiGraphics.guiHeight());
        float ratio = Math.min((float) guiGraphics.guiWidth() / shortest, (float) guiGraphics.guiHeight() / shortest) * scale;
        int size = Mth.floor(shortest * ratio);
        int left = (guiGraphics.guiWidth() - size) / 2;
        int top = (guiGraphics.guiHeight() - size) / 2;
        int right = left + size;
        int bottom = top + size;

        RenderSystem.enableBlend();
        guiGraphics.blit(SPYGLASS_SCOPE, left, top, -90, 0.0F, 0.0F, size, size, size, size);
        RenderSystem.disableBlend();
        guiGraphics.fill(RenderType.guiOverlay(), 0, bottom, guiGraphics.guiWidth(), guiGraphics.guiHeight(), -90, 0xFF000000);
        guiGraphics.fill(RenderType.guiOverlay(), 0, 0, guiGraphics.guiWidth(), top, -90, 0xFF000000);
        guiGraphics.fill(RenderType.guiOverlay(), 0, top, left, bottom, -90, 0xFF000000);
        guiGraphics.fill(RenderType.guiOverlay(), right, top, guiGraphics.guiWidth(), bottom, -90, 0xFF000000);
    }
}
