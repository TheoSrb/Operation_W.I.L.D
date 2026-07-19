package net.tiew.operationWild.event;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.world.phys.AABB;
import net.neoforged.api.distmarker.OnlyIn;
import net.tiew.operationWild.ClientConfig;
import net.tiew.operationWild.entity.animals.aquatic.OrcaEntity;
import net.tiew.operationWild.entity.attacks.OWAttacksConstants;
import net.tiew.operationWild.entity.IOWWaypointEntity;
import net.tiew.operationWild.entity.OWEntity;
import net.tiew.operationWild.entity.client.MousePositionSaver;
import net.tiew.operationWild.entity.misc.SeaBugEntity;
import net.tiew.operationWild.entity.misc.Submarine;
import net.tiew.operationWild.entity.variants.CrocodileVariant;
import net.tiew.operationWild.entity.variants.KodiakVariant;
import net.tiew.operationWild.entity.variants.KangarooVariant;
import net.tiew.operationWild.entity.variants.BoaVariant;
import org.joml.Matrix4f;
import org.joml.Vector4f;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.ContainerScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;

import net.minecraft.client.Camera;
import net.minecraft.client.renderer.PostChain;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.tiew.operationWild.core.OWDatasSave;
import net.tiew.operationWild.quests.CosmeticsQuestsRegistry;
import net.tiew.operationWild.entity.animals.aquatic.CrocodileEntity;
import net.tiew.operationWild.entity.animals.terrestrial.*;
import net.tiew.operationWild.networking.ClientKillData;
import net.tiew.operationWild.screen.player.adventurer_manuscript.AdventurerManuscriptScreen;
import net.tiew.operationWild.entity.variants.TigerVariant;
import net.tiew.operationWild.particle.OWParticles;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.lwjgl.glfw.GLFW;
import net.tiew.operationWild.OperationWild;
import net.tiew.operationWild.effect.OWEffects;
import net.tiew.operationWild.entity.OWEntity;
import net.tiew.operationWild.gui.*;
import net.tiew.operationWild.item.OWItems;
import net.tiew.operationWild.item.custom.MayaBlowpipeItem;
import net.tiew.operationWild.entity.attacks.OWAttackLogic;
import net.tiew.operationWild.entity.attacks.OWAttacksHandler;
import net.tiew.operationWild.entity.attacks.OWPassive;
import net.tiew.operationWild.networking.OWNetworkHandler;
import net.tiew.operationWild.networking.packets.to_server.*;
import net.tiew.operationWild.core.OWDamageSources;
import net.tiew.operationWild.core.OWKeysBinding;

import java.io.*;
import java.util.*;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.client.event.*;
import net.neoforged.neoforge.event.entity.player.AttackEntityEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;

@EventBusSubscriber(modid = OperationWild.MOD_ID, bus = EventBusSubscriber.Bus.GAME, value = Dist.CLIENT)
public class ClientEvents {

    public static boolean isNotifiedOWBook = false;
    private static float damageTimer = 0.0f;

    private static int questUpdateTick = 0;
    private static boolean pendingWarning = false;
    private static int warningTick = 0;

    @SubscribeEvent
    public static void onDebate(InputEvent.MouseButton.Pre event) {
        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;

        if (player != null && event.getButton() == 1 && event.getAction() == 1) {
            CrocodileEntity grabbingCroc = player.level()
                    .getEntitiesOfClass(CrocodileEntity.class, player.getBoundingBox().inflate(5.0))
                    .stream()
                    .filter(c -> c.isGrabbing() && c.getGrabbedTarget() == player)
                    .findFirst().orElse(null);

            if (grabbingCroc != null) {
                if (RightClickAlertOverlay.clickAnimationTimer <= 0) {
                    if (grabbingCroc.getGrabTimeout() <= 0) {
                        OWNetworkHandler.sendToServer(new StopGrabPacket());
                    } else {
                        OWNetworkHandler.sendToServer(new OWEntityGrabManagerPacket(true));
                        RightClickAlertOverlay.hasClicked = true;
                        RightClickAlertOverlay.clickAnimationTimer = 3;
                    }
                }
            } else if (player.getVehicle() instanceof TigerEntity tiger && tiger.isGrabbing() && tiger.getGrabbedTarget() == player) {
                if (RightClickAlertOverlay.clickAnimationTimer <= 0) {
                    if (tiger.getGrabTimeout() <= 0) {
                        OWNetworkHandler.sendToServer(new StopGrabPacket());
                    } else {
                        OWNetworkHandler.sendToServer(new OWEntityGrabManagerPacket(true));
                        RightClickAlertOverlay.hasClicked = true;
                        RightClickAlertOverlay.clickAnimationTimer = 3;
                    }
                }
            } else {
                BoaEntity grabbingBoa = player.level()
                        .getEntitiesOfClass(BoaEntity.class, player.getBoundingBox().inflate(5.0))
                        .stream()
                        .filter(b -> b.isGrabbing() && b.getGrabbedTarget() == player)
                        .findFirst().orElse(null);

                if (grabbingBoa != null && RightClickAlertOverlay.clickAnimationTimer <= 0) {
                    if (grabbingBoa.getGrabTimeout() <= 0) {
                        OWNetworkHandler.sendToServer(new StopGrabPacket());
                    } else {
                        OWNetworkHandler.sendToServer(new OWEntityGrabManagerPacket(true));
                        RightClickAlertOverlay.hasClicked = true;
                        RightClickAlertOverlay.clickAnimationTimer = 3;
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public static void onAttack(AttackEntityEvent event) {
        Player player = event.getEntity();

        if (player.getMainHandItem().is(OWItems.REPTILIAN_DAGGER.get())) {
            if (player.getOffhandItem().is(OWItems.REPTILIAN_DAGGER.get())) {
                player.swing(InteractionHand.OFF_HAND, true);
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerInput(MovementInputUpdateEvent event) {
        Entity entity = event.getEntity();

        if (entity != null && entity instanceof Player player) {
            if (player.getVehicle() instanceof CrocodileEntity crocodile && crocodile.getGrabbedTarget() == player ||
                    player.getVehicle() instanceof TigerEntity tiger && tiger.getGrabbedTarget() == player ||
                    player.getVehicle() instanceof BoaEntity boa && boa.getGrabbedTarget() == player) {
                event.getInput().shiftKeyDown = false;
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        Player player = event.getEntity();

        if (player.isPassenger()) {
            player.stopRiding();
        }
    }

    @SubscribeEvent
    public static void onPlayerLogout(ClientPlayerNetworkEvent.LoggingOut event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null) saveWaypointStates(mc.player);
        computedClusters.clear();
        clusterPopSmoothed.clear();
        waypointStates.clear();
        computedWaypoints.clear();
        currentEntityIds.clear();
        lastWaypointLevel = new java.lang.ref.WeakReference<>(null);
        cachedWorldName = null;
        cachedWaypointKey = null;
        pendingWarning = false;
        warningTick = 0;
        currentVenomBlur = null;
        maxEffectDuration = 0;
        setBlurPercentage(0);
    }

    @SubscribeEvent
    public static void onMouseClick(InputEvent.MouseButton.Pre event) {
        Minecraft minecraft = Minecraft.getInstance();
        Player player = minecraft.player;
        boolean leftButtonIsPressed = event.getButton() == 0 && event.getAction() == 1;
        boolean rightButtonIsPressed = event.getButton() == 1 && event.getAction() == 1;

        if (minecraft.screen != null &&
                (minecraft.screen instanceof InventoryScreen ||
                        minecraft.screen instanceof ContainerScreen)) {
            return;
        }

        if (player != null) {
            Entity ridingEntity = player.getRootVehicle();
            if (ridingEntity instanceof Submarine) {
                if (rightButtonIsPressed && canUseRightClick(minecraft)) {
                    OWNetworkHandler.sendToServer(new ClientPressedRightClick());
                }
            } else if (ridingEntity instanceof OWEntity entity && entity.isAlive() && entity.isSaddled()) {
                if (entity instanceof CrocodileEntity crocodile && crocodile.crocodileBehaviorHandler.isReadyForTaming() && !crocodile.isTame()) {
                    if (leftButtonIsPressed && !OWAttackLogic.isCharging) {
                        boolean isScreenOpen = minecraft.screen != null;
                        OWNetworkHandler.sendToServer(new ClientPressedLeftClick(isScreenOpen));
                    } else if (rightButtonIsPressed && canUseRightClick(minecraft)) {
                        OWNetworkHandler.sendToServer(new ClientPressedRightClick());
                    }
                } else if (entity.isTame()) {
                    if (leftButtonIsPressed && !OWAttackLogic.isCharging) {
                        boolean isScreenOpen = minecraft.screen != null;
                        OWNetworkHandler.sendToServer(new ClientPressedLeftClick(isScreenOpen));
                    } else if (rightButtonIsPressed && canUseRightClick(minecraft)) {
                        OWNetworkHandler.sendToServer(new ClientPressedRightClick());
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public static void onKeyInput(InputEvent.Key event) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player != null && minecraft.player.getRootVehicle() instanceof OWEntity owEntity) {

            // Inventaire bloqué tant qu'un didacticiel (vie / énergie / niveau / attaques) est affiché.
            if (OWKeysBinding.PET_INVENTORY.isDown() && event.getAction() == GLFW.GLFW_PRESS
                    && (owEntity.isTame() || owEntity instanceof Submarine)
                    && !OWIndicationOverlay.isActive()) {
                OWNetworkHandler.sendToServer(new OpenOWInventoryPacket());
            }
        }
        if (minecraft.player != null && OWKeysBinding.OW_ENTITY_JOURNAL.isDown()) {
            minecraft.setScreen(new AdventurerManuscriptScreen());
            isNotifiedOWBook = false;
            minecraft.player.playSound(SoundEvents.BOOK_PAGE_TURN);
        }
        // Menu de tribu (touche T) : demande les données au serveur puis ouvre l'écran.
        if (minecraft.player != null && OWKeysBinding.OW_TRIBE_MENU.isDown()
                && event.getAction() == GLFW.GLFW_PRESS
                && minecraft.screen == null
                && !OWIndicationOverlay.isActive()) {
            OWNetworkHandler.sendToServer(new net.tiew.operationWild.networking.packets.to_server.OpenTribeMenuPacket());
            minecraft.setScreen(new net.tiew.operationWild.screen.tribe.OWTribeMenuScreen());
        }
    }

    @SubscribeEvent
    public static void onMovementInput(MovementInputUpdateEvent event) {
        Player player = event.getEntity();
        if (player.getMainHandItem().is(OWItems.SEABUG.get()) || player.getOffhandItem().is(OWItems.SEABUG.get())) {
            if (!player.isCreative()) {
                event.getInput().jumping = false;
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerJoinWorld(ClientPlayerNetworkEvent.LoggingIn event) {
        currentVenomBlur = null;
        maxEffectDuration = 0;
        setBlurPercentage(0);

        // Les quêtes sont propres à chaque individu et gérées côté serveur : chaque OWEntity tire et
        // remet à zéro ses quêtes sur son tick, puis synchronise son cavalier. Plus de reset client ici.
        String worldName = getWorldName(event.getPlayer());
        ClientKillData.createEmptyFile(worldName);
        loadWaypointStates(event.getPlayer());
    }

    private static String cachedWorldName = null;

    public static String getWorldName(Player player) {
        if (cachedWorldName != null) return cachedWorldName;

        Minecraft mc = Minecraft.getInstance();

        if (mc.hasSingleplayerServer() && mc.getSingleplayerServer() != null) {
            try {
                java.nio.file.Path root = mc.getSingleplayerServer().getWorldPath(LevelResource.ROOT);
                cachedWorldName = root.toAbsolutePath().normalize().getFileName().toString();
            } catch (Exception ignored) {
                cachedWorldName = mc.getSingleplayerServer().getWorldData().getLevelName();
            }
            return cachedWorldName;
        }

        if (mc.getCurrentServer() != null) {
            cachedWorldName = "multiplayer_" + mc.getCurrentServer().ip
                    .replace(":", "_").replace(".", "_");
            return cachedWorldName;
        }

        return "unknown_world";
    }


    @SubscribeEvent
    public static void onRenderHand(RenderHandEvent event) {
        ItemStack stack = event.getItemStack();

        if (stack.getItem() instanceof MayaBlowpipeItem) {
            LocalPlayer player = Minecraft.getInstance().player;

            if (player != null && player.isUsingItem() && player.getUseItem() == stack) {
                float chargeProgress = MayaBlowpipeItem.getChargeProgress(stack, player);
                applyBlowpipeRotation(event.getPoseStack(), chargeProgress, event.getHand());
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerLoggedIn(ClientPlayerNetworkEvent.LoggingIn event) {
        OWDatasSave.loadFromFile();
        if (!OWDatasSave.hasSeenDevWarning()) {
            pendingWarning = true;
            warningTick = 10;
        }
    }

    private static void applyBlowpipeRotation(PoseStack poseStack, float chargeProgress, InteractionHand hand) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) return;

        double yTranslate = 1.0D - (chargeProgress * 0.25D);
        double zTranslate = 0.9D - (chargeProgress * 0.125D);

        if (hand == InteractionHand.OFF_HAND) {
            poseStack.mulPose(Axis.YP.rotationDegrees(-110));
            poseStack.mulPose(Axis.ZP.rotationDegrees(90));
            poseStack.translate(0.35D, yTranslate, zTranslate);
        } else {
            poseStack.mulPose(Axis.YP.rotationDegrees(110));
            poseStack.mulPose(Axis.ZP.rotationDegrees(-90));
            poseStack.translate(-0.35D, yTranslate, zTranslate);
        }
    }

    @SubscribeEvent
    public static void onPlayerAttackEntity(AttackEntityEvent event) {

        Player player = event.getEntity();
        if (player.getVehicle() instanceof OWEntity) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onEntityInteract(PlayerInteractEvent.EntityInteract event) {
        Player player = event.getEntity();
        if (player.getVehicle() instanceof OWEntity) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onMovementInputUpdate(MovementInputUpdateEvent event) {
        Player player = Minecraft.getInstance().player;
        if (player != null && !player.isCreative()) {
            boolean holdingSeaBug = player.getMainHandItem().is(OWItems.SEABUG.get()) || player.getOffhandItem().is(OWItems.SEABUG.get());
            if (holdingSeaBug) {
                event.getInput().shiftKeyDown = false;
            }
        }
    }

    private static boolean canUseRightClick(Minecraft minecraft) {
        return minecraft.player != null && minecraft.player.getMainHandItem().getUseAnimation() == UseAnim.NONE && !(minecraft.player.getMainHandItem().getItem() instanceof MayaBlowpipeItem);
    }

    @SubscribeEvent
    public static void onHypnosisCameraPull(ViewportEvent.ComputeCameraAngles event) {
        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        if (player == null || mc.level == null) return;
        if (player.isCreative() || player.isSpectator()) return;

        net.tiew.operationWild.entity.animals.terrestrial.BoaEntity boa = player.level()
                .getEntitiesOfClass(net.tiew.operationWild.entity.animals.terrestrial.BoaEntity.class,
                        player.getBoundingBox().inflate(12.0))
                .stream()
                .filter(b -> b.isHypnotizing() && b.getHypnosisTargetId() == player.getId())
                .findFirst().orElse(null);
        if (boa == null) {
            wasHypnotized = false;
            return;
        }

        if (!wasHypnotized) {
            hypnosisOverlayStart = System.currentTimeMillis();
            wasHypnotized = true;
        }

        Vec3 eyes = boa.getEyePosition();
        double dx = eyes.x - player.getX();
        double dz = eyes.z - player.getZ();
        double dy = eyes.y - player.getEyeY();
        double horiz = Math.sqrt(dx * dx + dz * dz);
        float wantedYaw = (float) (Mth.atan2(dz, dx) * Mth.RAD_TO_DEG) - 90f;
        float wantedPitch = (float) (-(Mth.atan2(dy, horiz) * Mth.RAD_TO_DEG));

        float dt = (float) mc.getTimer().getRealtimeDeltaTicks();
        float pull = Mth.clamp(dt * HYPNOSIS_PULL_SPEED, 0f, 1f);

        float newYaw = approachAngle(player.getYRot(), wantedYaw, pull);
        float newPitch = Mth.clamp(approachAngle(player.getXRot(), wantedPitch, pull), -90f, 90f);
        player.setYRot(newYaw);
        player.yRotO = newYaw;
        player.setXRot(newPitch);
        player.xRotO = newPitch;
        player.setYHeadRot(newYaw);
        player.yHeadRot = newYaw;
        player.yHeadRotO = newYaw;
        player.yBodyRot = newYaw;
        player.yBodyRotO = newYaw;
        event.setYaw(newYaw);
        event.setPitch(newPitch);
    }

    private static final float HYPNOSIS_PULL_SPEED = 0.25f;

    private static float approachAngle(float current, float wanted, float fraction) {
        return current + Mth.wrapDegrees(wanted - current) * fraction;
    }

    private static boolean wasHypnotized = false;
    private static long hypnosisOverlayStart = -1L;

    private static final ResourceLocation HYP_OVERLAY_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "textures/gui/hypnosis_overlay.png");
    private static final int HYP_OVERLAY_FRAME_W = 64;
    private static final int HYP_OVERLAY_FRAME_H = 64;
    private static final int HYP_OVERLAY_FRAMES = 1;
    private static final long HYP_OVERLAY_DURATION_MS = 1500;
    private static final long HYP_OVERLAY_FRAME_MS = 60;
    private static final float HYP_OVERLAY_SCALE = 2.3f;

    @SubscribeEvent
    public static void onRenderLevelUpOverlay(RenderGuiEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;
        OWLevelUpOverlay.render(event.getGuiGraphics(),
                event.getGuiGraphics().guiWidth(),
                event.getGuiGraphics().guiHeight());
        OWCoinGainOverlay.render(event.getGuiGraphics(),
                event.getGuiGraphics().guiWidth(),
                event.getGuiGraphics().guiHeight());
        // Duel d'arène : rendu en dernier pour passer par-dessus tout le HUD.
        net.tiew.operationWild.gui.OWArenaClashOverlay.render(event.getGuiGraphics(),
                event.getGuiGraphics().guiWidth(),
                event.getGuiGraphics().guiHeight());
        net.tiew.operationWild.gui.OWArenaVictoryOverlay.render(event.getGuiGraphics(),
                event.getGuiGraphics().guiWidth(),
                event.getGuiGraphics().guiHeight());
        net.tiew.operationWild.gui.OWXpGainOverlay.render(event.getGuiGraphics(),
                event.getGuiGraphics().guiWidth(),
                event.getGuiGraphics().guiHeight());
        net.tiew.operationWild.gui.OWTamingXpGainOverlay.render(event.getGuiGraphics(),
                event.getGuiGraphics().guiWidth(),
                event.getGuiGraphics().guiHeight());
        // Quand on chevauche un pet, l'indication est rendue par onRenderStage (après le HUD, donc au-dessus).
        // Ici, on ne la rend que hors monture (ex : tip de niveau reçu sans être monté).
        boolean mountedOnPet = mc.player.getVehicle() instanceof OWEntity ow && !(ow instanceof Submarine);
        if (!mountedOnPet) {
            OWIndicationOverlay.render(event.getGuiGraphics(),
                    event.getGuiGraphics().guiWidth(),
                    event.getGuiGraphics().guiHeight());
        }
    }

    /** Empêche l'ouverture de l'inventaire du joueur tant qu'un didacticiel est affiché. */
    @SubscribeEvent
    public static void onScreenOpening(ScreenEvent.Opening event) {
        if (OWIndicationOverlay.isActive() && event.getNewScreen() instanceof InventoryScreen) {
            event.setCanceled(true);
        }
    }

    /** Rendu des indications par-dessus l'écran d'inventaire de l'entité (les hooks HUD ne tournent pas écran ouvert). */
    @SubscribeEvent
    public static void onScreenRenderPost(ScreenEvent.Render.Post event) {
        if (event.getScreen() instanceof net.tiew.operationWild.screen.entity.OWInventoryScreen) {
            OWIndicationOverlay.render(event.getGuiGraphics(),
                    event.getGuiGraphics().guiWidth(),
                    event.getGuiGraphics().guiHeight());
        }

        // Animations d'obtention de récompense (XP / pièces / exp. d'apprivoisement) rendues PAR-DESSUS
        // les écrans (ex : franchissement d'un palier de la Piste Sauvage écran ouvert). Le HUD ne
        // s'affiche pas quand un écran est ouvert, d'où ce rendu dédié à z élevé.
        GuiGraphics g = event.getGuiGraphics();
        int w = g.guiWidth(), h = g.guiHeight();
        g.pose().pushPose();
        g.pose().translate(0, 0, 400);
        OWCoinGainOverlay.render(g, w, h);
        net.tiew.operationWild.gui.OWXpGainOverlay.render(g, w, h);
        net.tiew.operationWild.gui.OWTamingXpGainOverlay.render(g, w, h);
        g.pose().popPose();
    }

    @SubscribeEvent
    public static void onRenderHypnosisOverlay(RenderGuiEvent.Post event) {
        if (hypnosisOverlayStart < 0) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.options.hideGui) return;

        long el = System.currentTimeMillis() - hypnosisOverlayStart;
        if (el > HYP_OVERLAY_DURATION_MS) {
            hypnosisOverlayStart = -1L;
            return;
        }

        GuiGraphics g = event.getGuiGraphics();
        int sw = g.guiWidth(), sh = g.guiHeight();
        float t = el / (float) HYP_OVERLAY_DURATION_MS;

        float alpha = Mth.clamp(1f - t, 0f, 1f);
        float pop = 1f - (float) Math.pow(1f - Math.min(t / 0.25f, 1f), 3);
        float scale = HYP_OVERLAY_SCALE * (0.7f + 0.3f * pop);
        float yDrift = -t * sh * 0.06f;

        int frame = (int) (el / HYP_OVERLAY_FRAME_MS) % HYP_OVERLAY_FRAMES;

        com.mojang.blaze3d.systems.RenderSystem.enableBlend();
        g.setColor(1f, 1f, 1f, alpha);
        g.pose().pushPose();
        g.pose().translate(sw / 2f, sh / 2f + yDrift, 0);
        g.pose().scale(scale, scale, 1f);
        g.pose().translate(-HYP_OVERLAY_FRAME_W / 2f, -HYP_OVERLAY_FRAME_H / 2f, 0);
        g.blit(HYP_OVERLAY_TEXTURE, 0, 0, 0f, (float) frame * HYP_OVERLAY_FRAME_H,
                HYP_OVERLAY_FRAME_W, HYP_OVERLAY_FRAME_H,
                HYP_OVERLAY_FRAME_W, HYP_OVERLAY_FRAME_H * HYP_OVERLAY_FRAMES);
        g.pose().popPose();
        g.setColor(1f, 1f, 1f, 1f);
        com.mojang.blaze3d.systems.RenderSystem.disableBlend();
    }

    private static net.tiew.operationWild.entity.animals.terrestrial.BoaEntity localHypnotizingBoa() {
        Minecraft mc = Minecraft.getInstance();
        Player p = mc.player;
        if (p == null || mc.level == null || p.isCreative() || p.isSpectator()) return null;
        return p.level().getEntitiesOfClass(
                        net.tiew.operationWild.entity.animals.terrestrial.BoaEntity.class,
                        p.getBoundingBox().inflate(12.0))
                .stream()
                .filter(b -> b.isHypnotizing() && b.getHypnosisTargetId() == p.getId())
                .findFirst().orElse(null);
    }

    private static float hypnoScreenAlpha = 0f;
    private static final float HYPNO_VEIL_MIN_ALPHA = 0.10f;
    private static final float HYPNO_VEIL_MAX_ALPHA = 0.25f;

    @SubscribeEvent
    public static void onRenderHypnosisScreen(RenderGuiEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.options.hideGui) return;

        boolean active = localHypnotizingBoa() != null;
        float dt = (float) mc.getTimer().getRealtimeDeltaTicks();
        float fade = Mth.clamp(dt * 0.18f, 0f, 1f);
        hypnoScreenAlpha += ((active ? 1f : 0f) - hypnoScreenAlpha) * fade;
        if (hypnoScreenAlpha < 0.01f) {
            if (!active) hypnoScreenAlpha = 0f;
            return;
        }

        GuiGraphics g = event.getGuiGraphics();
        int sw = g.guiWidth(), sh = g.guiHeight();
        float time = (System.currentTimeMillis() % 1_000_000L) / 1000f;
        float mid = (HYPNO_VEIL_MIN_ALPHA + HYPNO_VEIL_MAX_ALPHA) * 0.5f;
        float amp = (HYPNO_VEIL_MAX_ALPHA - HYPNO_VEIL_MIN_ALPHA) * 0.5f;
        float osc = mid + amp * (float) Math.sin(time * 1.3);
        int aInt = (int) (hypnoScreenAlpha * osc * 255f);
        if (aInt <= 0) return;

        float cx = sw / 2f + (float) Math.sin(time * 2.1) * sw * 0.025f;
        float cy = sh / 2f + (float) Math.cos(time * 1.7) * sh * 0.025f;
        int rings = 24;
        int cycle = (int) (time * 6);

        com.mojang.blaze3d.systems.RenderSystem.enableBlend();
        for (int i = 1; i <= rings; i++) {
            float fo = i / (float) rings;
            float fi = (i - 1) / (float) rings;
            boolean yellow = ((i + cycle) & 1) == 0;
            int col = (aInt << 24) | (yellow ? 0xFFE000 : 0xE000E0);

            int ox0 = (int) (cx - cx * fo), oy0 = (int) (cy - cy * fo);
            int ox1 = (int) (cx + (sw - cx) * fo), oy1 = (int) (cy + (sh - cy) * fo);
            int ix0 = (int) (cx - cx * fi), iy0 = (int) (cy - cy * fi);
            int ix1 = (int) (cx + (sw - cx) * fi), iy1 = (int) (cy + (sh - cy) * fi);

            g.fill(ox0, oy0, ox1, iy0, col);
            g.fill(ox0, iy1, ox1, oy1, col);
            g.fill(ox0, iy0, ix0, iy1, col);
            g.fill(ix1, iy0, ox1, iy1, col);
        }
        com.mojang.blaze3d.systems.RenderSystem.disableBlend();
    }

    @SubscribeEvent
    public static void onClientTick(PlayerTickEvent.Post event) {
        Minecraft minecraft = Minecraft.getInstance();

        if (pendingWarning && event.getEntity() == minecraft.player) {
            if (warningTick > 0) {
                warningTick--;
            } else if (minecraft.screen == null) {
                pendingWarning = false;
                minecraft.setScreen(new net.tiew.operationWild.screen.OWDevWarningScreen());
            }
        }

        if (minecraft.level != null && minecraft.level.isClientSide()) {
            if (minecraft.player != null && minecraft.player.getVehicle() instanceof OWEntity owEntity) {
                // Conducteur = passager principal (index 0), comme la permission d'attaque côté serveur.
                boolean isDriver = !owEntity.getPassengers().isEmpty()
                        && owEntity.getPassengers().get(0) == minecraft.player;

                if (isDriver) {
                    boolean isSprintKeyDown = minecraft.options.keySprint.isDown();
                    OWNetworkHandler.sendToServer(new OWRunningPacket(isSprintKeyDown));

                    // Le didacticiel des attaques ne concerne QUE le conducteur : un passager annexe
                    // ne peut pas attaquer, donc pas de « appuyez sur X pour afficher les attaques ».
                    if (owEntity.getId() != lastTutorialVehicleId) {
                        lastTutorialVehicleId = owEntity.getId();
                        tryShowMountTutorials(owEntity);
                    }
                }
            } else if (minecraft.player == null || !(minecraft.player.getVehicle() instanceof OWEntity)) {
                lastTutorialVehicleId = -1;
            }
        }
    }

    private static int lastTutorialVehicleId = -1;

    /**
     * Didacticiels au montage d'un animal APPRIVOISÉ (uniquement package entity.animals.* : exclut
     * véhicules misc et boss).
     * <ul>
     *   <li>Première entité jamais chevauchée (une fois, tous mondes confondus) : séquence
     *       Vie → Énergie → Attaques/touche → Cartes d'attaque.</li>
     *   <li>Le message « touche d'attaque » (8 s) est le seul qui se répète à chaque nouvelle espèce.</li>
     * </ul>
     */
    private static void tryShowMountTutorials(OWEntity entity) {
        if (!entity.isTame()) return;
        if (!entity.getClass().getPackageName().startsWith("net.tiew.operationWild.entity.animals")) return;

        String speciesId = net.minecraft.core.registries.BuiltInRegistries.ENTITY_TYPE
                .getKey(entity.getType()).toString();

        boolean firstEntity = !net.tiew.operationWild.core.OWTutorialData.hasSeenMountTutorial();
        boolean newSpecies = !net.tiew.operationWild.core.OWTutorialData.hasSeenAttacksTutorial(speciesId);
        if (!firstEntity && !newSpecies) return;

        if (firstEntity) {
            OWIndicationOverlay.enqueue(Component.translatable("indication.ow.tuto_health"), 200, OWIndicationOverlay.Anchor.HEALTH);
            OWIndicationOverlay.enqueue(Component.translatable("indication.ow.tuto_energy"), 200, OWIndicationOverlay.Anchor.ENERGY);
            // Message d'attaque : au centre en bas, et ne disparaît qu'après maintien de la touche 1 s.
            OWIndicationOverlay.enqueue(buildAttacksIndication(entity), 200, OWIndicationOverlay.Anchor.BOTTOM, true);
            OWIndicationOverlay.enqueue(Component.translatable("indication.ow.tuto_attack_cards"), 200, OWIndicationOverlay.Anchor.ATTACKS);
            net.tiew.operationWild.core.OWTutorialData.markMountTutorialSeen();
            net.tiew.operationWild.core.OWTutorialData.markAttacksTutorialSeen(speciesId);
        } else {
            // Nouvelle espèce : même comportement que le tuto de base (maintien de la touche 1 s pour valider).
            OWIndicationOverlay.enqueue(buildAttacksIndication(entity), 200, OWIndicationOverlay.Anchor.BOTTOM, true);
            net.tiew.operationWild.core.OWTutorialData.markAttacksTutorialSeen(speciesId);
        }
    }

    /** Construit l'indication « afficher les attaques » : nom d'espèce coloré+gras et touche en gras. */
    private static Component buildAttacksIndication(OWEntity entity) {
        Component species = entity.getType().getDescription().copy()
                .withStyle(Style.EMPTY.withColor(net.minecraft.network.chat.TextColor.fromRgb(entity.getEntityColor())).withBold(true));
        Component key = OWKeysBinding.OW_ATTACKS_INFO.getTranslatedKeyMessage().copy()
                .withStyle(Style.EMPTY.withBold(true));
        return Component.translatable("indication.ow.show_attacks", species, key);
    }

    private static void spawnGoldTrailParticles(Player player, LivingEntity entity, float yRot) {
        if (entity.getDeltaMovement().horizontalDistanceSqr() > 4e-4) {
            var rand = entity.getRandom();
            float yaw = (float) Math.toRadians(yRot);
            for (int p = 0; p < 18; p++) {
                double bx = entity.getX() - Math.sin(yaw) * (0.3 + rand.nextDouble() * 0.8) + (rand.nextDouble() - 0.5) * 0.8;
                double by = entity.getY() + rand.nextDouble() * 1.0;
                double bz = entity.getZ() + Math.cos(yaw) * (0.3 + rand.nextDouble() * 0.8) + (rand.nextDouble() - 0.5) * 0.8;
                player.level().addParticle(
                        OWParticles.GOLD_TRAIL_PARTICLE.get(),
                        bx, by, bz,
                        (rand.nextDouble() - 0.5) * 0.05, 0.02 + rand.nextDouble() * 0.04, (rand.nextDouble() - 0.5) * 0.05);
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();

        if (player.level().isClientSide() && ++questUpdateTick >= 20) {
            questUpdateTick = 0;
            player.level().getEntitiesOfClass(TigerEntity.class,
                    player.getBoundingBox().inflate(64),
                    e -> e.isTame() && e.isOwnedBy(player)
            ).forEach(tiger -> CosmeticsQuestsRegistry.getAllQuests().forEach(q -> q.update(tiger.getUUID())));
        }

        if (player.level().isClientSide()) {
            player.level().getEntitiesOfClass(TigerEntity.class,
                    player.getBoundingBox().inflate(32),
                    e -> e.isTame() && e.getVariant() == TigerVariant.Cosmetics.GOLD.variant && !e.isDeadOrDying()
            ).forEach(entity -> spawnGoldTrailParticles(player, entity, entity.getYRot()));

            player.level().getEntitiesOfClass(KodiakEntity.class,
                    player.getBoundingBox().inflate(32),
                    e -> e.isTame() && e.getVariant() == KodiakVariant.Cosmetics.GOLD.variant && !e.isDeadOrDying()
            ).forEach(entity -> spawnGoldTrailParticles(player, entity, entity.getYRot()));

            player.level().getEntitiesOfClass(CrocodileEntity.class,
                    player.getBoundingBox().inflate(32),
                    e -> e.isTame() && e.getVariant() == CrocodileVariant.Cosmetics.GOLD.variant && !e.isDeadOrDying()
            ).forEach(entity -> spawnGoldTrailParticles(player, entity, entity.getYRot()));

            player.level().getEntitiesOfClass(KangarooEntity.class,
                    player.getBoundingBox().inflate(32),
                    e -> e.isTame() && e.getVariant() == KangarooVariant.Cosmetics.GOLD.variant && !e.isDeadOrDying()
            ).forEach(entity -> spawnGoldTrailParticles(player, entity, entity.getYRot()));

            player.level().getEntitiesOfClass(BoaEntity.class,
                    player.getBoundingBox().inflate(32),
                    e -> e.isTame() && e.getVariant() == BoaVariant.Cosmetics.GOLD.variant && !e.isDeadOrDying()
            ).forEach(entity -> spawnGoldTrailParticles(player, entity, entity.getYRot()));
        }

        int waterDepth = player.isInWater() ? (int) (player.level().getSeaLevel() - player.getY()) : -1;
        float waterPressure = getWaterPressure(waterDepth);

        if (!player.level().isClientSide()) {
            if (waterPressure >= 4 && !player.isCreative() && !isInSubmarine(player)) {
                float damageInterval = Math.max((-1.25f * waterPressure + 65) / 30.0f, 0.1f);

                damageTimer += 0.05f;

                if (damageTimer >= damageInterval) {
                    player.invulnerableTime = 0;

                    DamageSource waterPressureDamage = OWDamageSources.createWaterPressureDamage((ServerLevel) player.level());
                    player.hurt(waterPressureDamage, 1);

                    player.invulnerableTime = 0;
                    damageTimer = 0.0f;
                }

                if (player.tickCount % 100 == 0) {
                    Component message = Component.translatable("tooHighPressure")
                            .setStyle(Style.EMPTY
                                    .withColor(ChatFormatting.YELLOW));
                    Minecraft.getInstance().gui.setOverlayMessage(message, true);
                }

                player.addEffect(new MobEffectInstance(OWEffects.WATER_PRESSURE_EFFECT.getDelegate(), 100, 0, false, true));

            } else {
                damageTimer = 0.0f;
                player.removeEffect(OWEffects.WATER_PRESSURE_EFFECT.getDelegate());
            }
        }

        if (waterPressure >= 4 && !player.isCreative() && player.isAlive() && !isInSubmarine(player)) {
            float normalizedPressure = waterPressure / 4.0f;
            float intensity = 0.05f * (float) Math.pow(normalizedPressure, 2f);
            shakeCamera(intensity, player);
        }

        if (player.getMainHandItem().is(OWItems.SEABUG.get()) || player.getOffhandItem().is(OWItems.SEABUG.get())) {
            if (!player.isCreative()) {
                player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 5, 1, false, false, false));
            }
        }

        if (player.hasEffect(OWEffects.BLEEDING_EFFECT.getDelegate()) && !player.isCreative()) {
            player.setSprinting(false);
        }

        if (player.getVehicle() instanceof OWEntity owEntity && owEntity.isTame() && owEntity.isAlive() && owEntity.getLevelPoints() > 0) {
            List<Entity> passengers = owEntity.getPassengers();
            boolean isDriver = !passengers.isEmpty() && passengers.get(0) == player;
            boolean isOwner = player.getUUID().equals(owEntity.getOwnerUUID());

            if (isDriver && isOwner && player == Minecraft.getInstance().player) {
                Component message = Component.translatable("tooltip.level_points", owEntity.getLevelPoints())
                        .withStyle(Style.EMPTY.withColor(0xb8e45a).withBold(true));

                Minecraft.getInstance().gui.setOverlayMessage(message, true);
            }
        }

    }

    private static void updateWaterMovement(OWEntity entity, Player player, int speed, double VERTICAL_DRAG) {
        final float WATER_RESISTANCE = 0.85f;
        final double HORIZONTAL_DRAG = 0.95;
        final double GRAVITY_IN_WATER = 0;
        final double BUOYANCY_FACTOR = 0.015;
        final double MAX_SPEED_IN_WATER = 0.4;
        final double VERTICAL_SPEED_DAMPENING = 0.7;

        float playerYaw = player.getYRot();
        float playerPitch = player.getXRot();
        float baseSpeed = entity.getSpeed() * speed;
        float adjustedSpeed = baseSpeed * WATER_RESISTANCE;

        float pitchRadians = playerPitch * ((float) Math.PI / 180F);
        float yawRadians = -playerYaw * ((float) Math.PI / 180F);

        double horizontalDistance = Math.cos(pitchRadians);
        Vec3 direction = new Vec3(horizontalDistance * Math.sin(yawRadians), -Math.sin(pitchRadians) * VERTICAL_SPEED_DAMPENING, horizontalDistance * Math.cos(yawRadians));

        if (direction.y > -0.3) direction = direction.add(0, -GRAVITY_IN_WATER, 0);
        else if (direction.y < -0.7) direction = new Vec3(direction.x, Math.max(direction.y, -0.7), direction.z);

        direction = direction.add(0, BUOYANCY_FACTOR, 0);

        Vec3 currentMotion = entity.getDeltaMovement();
        Vec3 movementVector = direction.normalize().scale(adjustedSpeed / 100);

        Vec3 newMotion = new Vec3(currentMotion.x * HORIZONTAL_DRAG + movementVector.x, currentMotion.y * VERTICAL_DRAG + movementVector.y, currentMotion.z * HORIZONTAL_DRAG + movementVector.z);

        if (newMotion.length() > MAX_SPEED_IN_WATER) newMotion = newMotion.normalize().scale(MAX_SPEED_IN_WATER);

        if (newMotion.y < -0.1) newMotion = new Vec3(newMotion.x, Math.max(newMotion.y, -0.1), newMotion.z);

        entity.setDeltaMovement(newMotion);
    }

    @SubscribeEvent
    public static void onRenderStage(RenderGuiLayerEvent.Post event) {
        Minecraft minecraft = Minecraft.getInstance();
        Player player = minecraft.player;
        Entity targetedEntity = player.getVehicle();
        Entity vehicle = player.getVehicle();
        boolean screenOpen = minecraft.screen != null;
        boolean hasVenom = player.hasEffect(OWEffects.VENOM_EFFECT.getDelegate()) || (vehicle != null && vehicle instanceof LivingEntity livingEntity && livingEntity.hasEffect(OWEffects.VENOM_EFFECT.getDelegate()));
        boolean canShowAttacksInformation = OWAttacksInformation.shouldRender();
        boolean questsAreUpdated = targetedEntity instanceof OWEntity owEntity && owEntity.questsAreUpdated();
        boolean renderSeabug = targetedEntity instanceof SeaBugEntity;
        boolean renderKodiak = targetedEntity instanceof KodiakEntity;
        boolean renderCrocodile = targetedEntity instanceof CrocodileEntity;
        boolean renderFoodOverlay = targetedEntity instanceof OWEntity ow && !ow.getItemFood().isEmpty();

        boolean isGrabByCrocodile = player.level().getEntitiesOfClass(CrocodileEntity.class, player.getBoundingBox().inflate(5.0)).stream().anyMatch(
                crocodile -> crocodile.isGrabbing() && crocodile.getGrabbedTarget() == player
        );

        boolean isGrabByTiger = player.level().getEntitiesOfClass(TigerEntity.class, player.getBoundingBox().inflate(5.0)).stream().anyMatch(
                tiger -> tiger.isGrabbing()
                        && tiger.getGrabbedTargetId() == player.getId()
                        && !tiger.isTame()
                        && tiger.getControllingPassenger() == null
        );

        boolean isGrabByBoa = player.level().getEntitiesOfClass(BoaEntity.class, player.getBoundingBox().inflate(5.0)).stream().anyMatch(
                boa -> boa.isGrabbing() && boa.getGrabbedTargetId() == player.getId()
        );

        boolean isGrabBySomething = isGrabByCrocodile || isGrabByTiger || isGrabByBoa;

        if (player != null) {
            PlantEmpressBossBar.render(event.getGuiGraphics(),
                    event.getGuiGraphics().guiWidth(),
                    event.getGuiGraphics().guiHeight());

            if (isGrabBySomething) {
                RightClickAlertOverlay.render(event.getGuiGraphics(), event.getGuiGraphics().guiWidth(), event.getGuiGraphics().guiHeight());
            }

            if (player.getVehicle() instanceof OWEntity && !(player.getVehicle() instanceof Submarine)) {
                if (player.getVehicle() instanceof TigerEntity tiger && tiger.getGrabbedTarget() == player) return;
                if (player.getVehicle() instanceof CrocodileEntity crocodile && crocodile.getGrabbedTarget() == player)
                    return;
                if (player.getVehicle() instanceof BoaEntity boa && boa.getGrabbedTarget() == player) return;
                OWEntityHud.render(event.getGuiGraphics(), event.getGuiGraphics().guiWidth(), event.getGuiGraphics().guiHeight());
                OWAttacksOverlay.render(event.getGuiGraphics(), event.getGuiGraphics().guiWidth(), event.getGuiGraphics().guiHeight());
                // Rendu de l'indication ICI, juste après le HUD de l'entité, pour passer au-dessus de
                // la barre de vie / énergie / cartes (même handler → ordre de dessin garanti).
                OWIndicationOverlay.render(event.getGuiGraphics(), event.getGuiGraphics().guiWidth(), event.getGuiGraphics().guiHeight());
            }

            if (isNotifiedOWBook) {
                OWBookNotifyOverlay.render(event.getGuiGraphics(),
                        event.getGuiGraphics().guiWidth(),
                        event.getGuiGraphics().guiHeight());
            }

            if (renderSeabug) {
                SubmarineOverlay.render(event.getGuiGraphics(),
                        event.getGuiGraphics().guiWidth(),
                        event.getGuiGraphics().guiHeight());
            }

            if (canShowAttacksInformation && !(vehicle instanceof Submarine)) {
                OWAttacksInformation.render(event.getGuiGraphics(),
                        event.getGuiGraphics().guiWidth(),
                        event.getGuiGraphics().guiHeight());
            }


            if (questsAreUpdated) {
                OWUtilsOverlay.render(event.getGuiGraphics(),
                        event.getGuiGraphics().guiWidth(),
                        event.getGuiGraphics().guiHeight());
            }

            if (renderFoodOverlay) {
                OWEntityFoodOverlay.render(event.getGuiGraphics(),
                        event.getGuiGraphics().guiWidth(),
                        event.getGuiGraphics().guiHeight());
            }
        }
    }

    public static double getBlurPercentage() {
        return blurPercentage * 100;
    }

    public static void setBlurPercentage(double blurPercentage) {
        ClientEvents.blurPercentage = blurPercentage;
    }

    public static double blurPercentage = 0.0;

    private static int maxEffectDuration = 0;

    private static ResourceLocation currentVenomBlur = null;

    private static void applyMinecraftBlurShader(Player player) {
        if (player != null && player.hasEffect(OWEffects.VENOM_EFFECT.getDelegate())) {
            int duration = player.getEffect(OWEffects.VENOM_EFFECT.getDelegate()).getDuration();
            if (maxEffectDuration < duration) maxEffectDuration = duration;
            // 1) % ÉCOULÉ de l'effet = (durée max - durée actuelle) / durée max.
            //    Au début (durée pleine) → 0 % → blur faible ; en fin → ~100 % → blur fort.
            //    Ex. 12 % écoulé → fraction 0.12 → 12 % (via getBlurPercentage).
            double fraction = maxEffectDuration > 0
                    ? (double) (maxEffectDuration - duration) / maxEffectDuration : 0.0;
            setBlurPercentage(fraction);
        }

        ResourceLocation shader = pickBlurShader(getBlurPercentage());
        if (shader != null) {
            // On re-charge si le shader voulu a changé, MAIS aussi si l'effet actif du
            // GameRenderer n'est plus le nôtre (ex. il a été vidé par un changement de
            // perspective F5). Sans ce 2e cas, le cache currentVenomBlur empêchait toute
            // ré-application → plus aucun blur en vue tierce.
            PostChain active = Minecraft.getInstance().gameRenderer.currentEffect();
            boolean activeIsOurs = active != null && active.getName().equals(shader.toString());
            if (!shader.equals(currentVenomBlur) || !activeIsOurs) {
                Minecraft.getInstance().gameRenderer.loadEffect(shader);
                currentVenomBlur = shader;
            }
        }
    }

    // 2) Choix du shader selon le % : ≤10 % → blur1, ≤20 % → blur2, … ≤100 % → blur10.
    //    Soit n = arrondi supérieur de (% / 10), borné à [1, 10].
    private static ResourceLocation pickBlurShader(double bp) {
        if (bp <= 0) return null;
        int n = (int) Math.ceil(bp / 10.0);
        if (n < 1) n = 1;
        if (n > 10) n = 10;
        return ResourceLocation.parse("ow:shaders/blur_shader/blur" + n + ".json");
    }

    private static void removeMinecraftBlurShader() {
        Minecraft mc = Minecraft.getInstance();
        mc.gameRenderer.shutdownEffect();
        currentVenomBlur = null;
        maxEffectDuration = 0;
        setBlurPercentage(0);
    }

    public static boolean blinkSubmarineShader = false;
    private static int blinkTimer = 0;
    private static boolean blinkShaderOn = false;

    public static void tickBlinkShader(ResourceLocation shader) {
        if (blinkTimer <= 0) {
            blinkShaderOn = !blinkShaderOn;
            blinkTimer = 4 + (int) (Math.random() * 17);
            if (blinkShaderOn) {
                Minecraft.getInstance().gameRenderer.loadEffect(shader);
            } else {
                Minecraft.getInstance().gameRenderer.shutdownEffect();
            }
        }
        blinkTimer--;
    }

    public static void stopBlinkShader() {
        blinkTimer = 0;
        blinkShaderOn = false;
        Minecraft.getInstance().gameRenderer.shutdownEffect();
    }

    public static float getWaterPressure(int waterDepth) {
        return (1 + 0.1f * waterDepth + 0.005f * waterDepth * waterDepth + Math.max(0, waterDepth - 70) * 0.75f) / 2.125f;
    }

    public static void shakeCamera(float frequency, Player player) {
        if (player != null) {
            double pitchOffset = (Math.random() - 0.5) * frequency;
            double yawOffset = (Math.random() - 0.5) * frequency;

            player.setYRot((float) (player.getYRot() + yawOffset));
            player.setXRot((float) (player.getXRot() + pitchOffset));
        }
    }

    @SubscribeEvent
    public static void renderBorders(RenderGuiEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();

        if (mc.player != null && mc.player.getVehicle() instanceof OWEntity vehicle && !(vehicle instanceof Submarine)) {
            boolean isGrabbedByCrocodile = vehicle instanceof CrocodileEntity crocodile && crocodile.getGrabbedTarget() == mc.player;
            boolean isCrocodileReadyForTaming = vehicle instanceof CrocodileEntity croc
                    && croc.crocodileBehaviorHandler.isReadyForTaming() && !croc.isTame();

            if (vehicle.getOwner() == mc.player || isCrocodileReadyForTaming) {
                boolean isLowHealth = ((float) (vehicle.getHealth() / vehicle.getMaxHealth())) <= 0.25f;
                boolean showVitalEnergyLack = vehicle.canShowVitalEnergyLack;

                if (isLowHealth) {
                    float opacityPercent = ((float) (-2.8 * (float) (vehicle.getHealth() / vehicle.getMaxHealth())) + 1) * 1.5f;
                    renderBorder(event.getGuiGraphics(), mc, 0xbc0c0c, opacityPercent, 1.0f);
                }

                if (showVitalEnergyLack) {
                    renderBorder(event.getGuiGraphics(), mc, 0x6442ac, 1.0f, 1.0f);
                }
            }
        }
    }

    private static void renderBorder(GuiGraphics graphics, Minecraft mc, int baseColor, float opacityPercent, float sizeMultiplier) {
        if (mc.screen != null || mc.options.hideGui) {
            return;
        }

        int screenWidth = mc.getWindow().getGuiScaledWidth();
        int screenHeight = mc.getWindow().getGuiScaledHeight();
        float centerX = screenWidth / 2.0f;
        float centerY = screenHeight / 2.0f;
        int steps = 40;

        for (int step = 0; step < steps; step++) {
            float progress = (float) step / steps;
            float time = (System.currentTimeMillis() % 2000) / 2000.0f;
            float oscillation = 0.4f + 0.1f * (float) Math.sin(time * 2 * Math.PI);
            float size = progress * Math.min(screenWidth, screenHeight) * oscillation * ((1 + opacityPercent) / 2) * sizeMultiplier;
            float alpha = ((1.0f - progress) * 0.075f) * opacityPercent;

            int alphaInt = (int) (alpha * 255);
            int color = (alphaInt << 24) | baseColor;

            int topHeight = (int) (size * (1.0f - Math.abs(centerY - size) / centerY));
            if (topHeight > 0) {
                graphics.fill(0, 0, screenWidth, Math.min(topHeight, screenHeight), color);
            }

            int bottomStart = (int) (screenHeight - size * (1.0f - Math.abs(centerY - (screenHeight - size)) / centerY));
            if (bottomStart < screenHeight) {
                graphics.fill(0, Math.max(bottomStart, 0), screenWidth, screenHeight, color);
            }

            int leftWidth = (int) (size * (1.0f - Math.abs(centerX - size) / centerX));
            if (leftWidth > 0) {
                graphics.fill(0, 0, Math.min(leftWidth, screenWidth), screenHeight, color);
            }

            int rightStart = (int) (screenWidth - size * (1.0f - Math.abs(centerX - (screenWidth - size)) / centerX));
            if (rightStart < screenWidth) {
                graphics.fill(Math.max(rightStart, 0), 0, screenWidth, screenHeight, color);
            }
        }
    }

    @SubscribeEvent
    public static void onScreenOpen(ScreenEvent.Init.Post event) {
        if (MousePositionSaver.shouldRestore) {
            long window = Minecraft.getInstance().getWindow().getWindow();
            GLFW.glfwSetCursorPos(window, MousePositionSaver.savedX, MousePositionSaver.savedY);
            MousePositionSaver.clear();
        }
    }

    public static boolean isInSubmarine(LivingEntity entity) {
        return entity.getRootVehicle() instanceof Submarine;
    }

    private static float savedXRot = 0f;
    private static float savedXRotO = 0f;

    private record SavedRots(float xRot, float xRotO, float yBody, float yBodyO, float yHead, float yHeadO) {
    }

    private static final Map<UUID, SavedRots> SAVED_PLAYER_ROTS = new HashMap<>();

    @SubscribeEvent
    public static void onPlayerRenderPre(RenderPlayerEvent.Pre event) {
        Player player = event.getEntity();

        if (player == null || !(player.getVehicle() instanceof OWEntity owVehicle)) {
            shadowStrikeHiddenRiders.remove(player == null ? null : player.getId());
            return;
        }

        SAVED_PLAYER_ROTS.put(player.getUUID(), new SavedRots(
                player.getXRot(), player.xRotO,
                player.yBodyRot, player.yBodyRotO,
                player.yHeadRot, player.yHeadRotO
        ));

        float vehicleYaw = owVehicle.getYRot();
        float vehicleYawO = owVehicle.yRotO;

        boolean isKodiakPassenger = owVehicle instanceof KodiakEntity k
                && k.getPassengers().indexOf(player) != 0;

        boolean isOrcaPassenger = owVehicle instanceof OrcaEntity o
                && o.getPassengers().indexOf(player) != 0;

        if (isKodiakPassenger) {
            player.yBodyRot = ((KodiakEntity) owVehicle).yBodyRot;
            player.yBodyRotO = ((KodiakEntity) owVehicle).yBodyRot;
        } else if (isOrcaPassenger) {
            player.yBodyRot = ((OrcaEntity) owVehicle).yBodyRot;
            player.yBodyRotO = ((OrcaEntity) owVehicle).yBodyRot;
        } else {
            player.yBodyRot = vehicleYaw;
            player.yBodyRotO = vehicleYawO;
            player.setYHeadRot(vehicleYaw);
            player.yHeadRotO = vehicleYawO;
        }

        if (player.getRootVehicle() instanceof TigerEntity tiger && tiger.isShadowStrikeActive()) {
            event.setCanceled(true);
            shadowStrikeHiddenRiders.add(player.getId());
            return;
        }
        shadowStrikeHiddenRiders.remove(player.getId());

        if (owVehicle instanceof CrocodileEntity croc && croc.isInWater()) {
            player.setXRot(0f);
            player.xRotO = 0f;
        }

        PoseStack poseStack = event.getPoseStack();
        Vec3 pivotPoint = new Vec3(0, 0, 0);

        if (owVehicle instanceof SeaBugEntity seaBug) {
            poseStack.pushPose();
            pivotPoint = new Vec3(0, 1.3, 0);

            poseStack.mulPose(Axis.YP.rotationDegrees(-player.getYRot()));
            Quaternionf rotation = Axis.XP.rotationDegrees(seaBug.getLastPlayerPitch());
            poseStack.rotateAround(rotation, (float) pivotPoint.x, (float) pivotPoint.y, (float) pivotPoint.z);
            poseStack.mulPose(Axis.YP.rotationDegrees(player.getYRot()));

        } else if (owVehicle instanceof CrocodileEntity crocodile) {
            poseStack.pushPose();

            if (player == crocodile.getGrabbedTarget()) {
                Vec3 look = crocodile.getLookAngle();
                Quaternionf rotationZ = Axis.ZP.rotationDegrees(-crocodile.getBodyZRot());
                Quaternionf rotationX = Axis.XP.rotationDegrees(-crocodile.getBodyXRot());
                Quaternionf rotationY = Axis.YP.rotationDegrees(-crocodile.getBodyYRot());

                poseStack.rotateAround(rotationZ, (float) pivotPoint.x, (float) pivotPoint.y, (float) pivotPoint.z);
                poseStack.rotateAround(rotationX, (float) pivotPoint.x, (float) pivotPoint.y, (float) pivotPoint.z);
                poseStack.rotateAround(rotationY, (float) ((float) pivotPoint.x - (look.x * 0.75f)), (float) pivotPoint.y, (float) ((float) pivotPoint.z - (look.z * 0.75f)));
            } else {
                poseStack.mulPose(Axis.YP.rotationDegrees(-crocodile.yBodyRot));
                Quaternionf rotationZ = Axis.ZP.rotationDegrees(-crocodile.getBodyZRot());
                Quaternionf rotationX = Axis.XP.rotationDegrees(-crocodile.getBodyXRot() + crocodile.getRiderControlPitch());

                poseStack.rotateAround(rotationZ, (float) pivotPoint.x, (float) pivotPoint.y, (float) pivotPoint.z);
                poseStack.rotateAround(rotationX, (float) pivotPoint.x, (float) pivotPoint.y, (float) pivotPoint.z);
                poseStack.mulPose(Axis.YP.rotationDegrees(crocodile.yBodyRot));
            }
        } else if (owVehicle instanceof net.tiew.operationWild.entity.animals.terrestrial.BoaEntity boaPose
                && boaPose.getGrabbedTargetId() != player.getId()
                && boaPose.getFirstTailPart() != null) {
            poseStack.pushPose();
            float pt = event.getPartialTick();
            Entity seg = boaPose.getFirstTailPart();
            float segYaw = Mth.rotLerp(pt, seg.yRotO, seg.getYRot());
            float bodyYaw = Mth.rotLerp(pt, player.yBodyRotO, player.yBodyRot);
            poseStack.mulPose(Axis.YP.rotationDegrees(Mth.wrapDegrees(bodyYaw - segYaw) * 0.5f));

            // Sous l'eau, la queue prend du tangage (xRot des segments via la vague de nage).
            // On incline aussi verticalement le rider pour qu'il suive le bone du siege (body_1),
            // dans le repere du yaw du corps (meme convention que crocodile/orca/kodiak).
            Entity seatSeg = boaPose.getSecondTailPart();
            if (boaPose.isInWater() && seatSeg != null) {
                // Intensite divisee par 2 pour un tangage plus discret du rider sous l'eau.
                float segPitch = Mth.rotLerp(pt, seatSeg.xRotO, seatSeg.getXRot()) * 0.5f;
                poseStack.mulPose(Axis.YP.rotationDegrees(-bodyYaw));
                poseStack.mulPose(Axis.XP.rotationDegrees(-segPitch));
                poseStack.mulPose(Axis.YP.rotationDegrees(bodyYaw));
            }
        } else if (owVehicle instanceof KodiakEntity kodiak) {
            poseStack.pushPose();

            boolean isPassenger = kodiak.getPassengers().indexOf(player) != 0;

            float pivotYaw = kodiak.yBodyRot;

            poseStack.mulPose(Axis.YP.rotationDegrees(-pivotYaw));

            Quaternionf rotationZ = isPassenger
                    ? Axis.ZP.rotationDegrees(-kodiak.getBodyZRot_passenger())
                    : Axis.ZP.rotationDegrees(-kodiak.getBodyZRot());

            Quaternionf rotationX = isPassenger
                    ? Axis.XP.rotationDegrees(-kodiak.getBodyXRot_passenger())
                    : Axis.XP.rotationDegrees(-kodiak.getBodyXRot());

            poseStack.rotateAround(rotationZ, (float) pivotPoint.x, (float) pivotPoint.y, (float) pivotPoint.z);
            poseStack.rotateAround(rotationX, (float) pivotPoint.x, (float) pivotPoint.y, (float) pivotPoint.z);

            poseStack.mulPose(Axis.YP.rotationDegrees(pivotYaw));
        } else if (owVehicle instanceof OrcaEntity orca) {
            poseStack.pushPose();

            boolean isPassenger = orca.getPassengers().indexOf(player) != 0;
            float pivotYaw = orca.yBodyRot;

            poseStack.mulPose(Axis.YP.rotationDegrees(-pivotYaw));

            Quaternionf rotationZ = isPassenger
                    ? Axis.ZP.rotationDegrees(-orca.getBodyZRot_passenger())
                    : Axis.ZP.rotationDegrees(-orca.getBodyZRot());

            Quaternionf rotationX = isPassenger
                    ? Axis.XP.rotationDegrees(-orca.getBodyXRot_passenger())
                    : Axis.XP.rotationDegrees(-orca.getBodyXRot());

            poseStack.rotateAround(rotationZ, (float) pivotPoint.x, (float) pivotPoint.y, (float) pivotPoint.z);
            poseStack.rotateAround(rotationX, (float) pivotPoint.x, (float) pivotPoint.y, (float) pivotPoint.z);

            poseStack.mulPose(Axis.YP.rotationDegrees(pivotYaw));
        } else {
            poseStack.pushPose();

            poseStack.mulPose(Axis.YP.rotationDegrees(-player.getYRot()));

            Quaternionf rotationZ = Axis.ZP.rotationDegrees(-owVehicle.getBodyZRot());
            Quaternionf rotationX = Axis.XP.rotationDegrees(-owVehicle.getBodyXRot());

            poseStack.rotateAround(rotationZ, (float) pivotPoint.x, (float) pivotPoint.y, (float) pivotPoint.z);
            poseStack.rotateAround(rotationX, (float) pivotPoint.x, (float) pivotPoint.y, (float) pivotPoint.z);

            poseStack.mulPose(Axis.YP.rotationDegrees(player.getYRot()));
        }
    }

    @SubscribeEvent
    public static void onPlayerRenderPost(RenderPlayerEvent.Post event) {
        Player player = event.getEntity();

        if (shadowStrikeHiddenRiders.contains(player.getId())) return;

        if (player == null || !(player.getVehicle() instanceof OWEntity)) {
            return;
        }

        PoseStack poseStack = event.getPoseStack();
        poseStack.popPose();

        SavedRots rots = SAVED_PLAYER_ROTS.remove(player.getUUID());
        if (rots != null) {
            player.setXRot(rots.xRot());
            player.xRotO = rots.xRotO();

            player.yBodyRot = rots.yBody();
            player.yBodyRotO = rots.yBodyO();

            player.setYHeadRot(rots.yHead());
            player.yHeadRotO = rots.yHeadO();
        }
    }

    @SubscribeEvent
    public static void onCameraSetup(ViewportEvent.ComputeCameraAngles event) {
        Entity cameraEntity = event.getCamera().getEntity();
        if (cameraEntity != null) {
            Entity rootVehicle = cameraEntity.getRootVehicle();

            double intensity = ClientConfig.CAMERA_SHAKE_INTENSITY.get();

            if (rootVehicle instanceof KodiakEntity kodiak) {
                event.setRoll((float) (event.getRoll() + (kodiak.bodyZRotCamera / (kodiak.isRunning() ? 3 : 2)) * intensity));
                event.setPitch((float) (event.getPitch() + (kodiak.bodyXRotCamera / (kodiak.isRunning() ? 3 : 2)) * intensity));
            } else if (rootVehicle instanceof TigerEntity tiger) {
                event.setRoll((float) (event.getRoll() + (tiger.getBodyZRot() / 6) * intensity));
                event.setPitch((float) (event.getPitch() + (tiger.getBodyXRot() / 6) * intensity));
            } else if (rootVehicle instanceof CrocodileEntity crocodile) {
                if (crocodile.isDeathRolling()) return;
                event.setRoll((float) (event.getRoll() + (crocodile.getBodyZRot() / 4) * intensity));
                event.setPitch((float) (event.getPitch() + (crocodile.getBodyXRot() / 4) * intensity));
            } else if (rootVehicle instanceof OrcaEntity orca) {
                event.setRoll((float) (event.getRoll() + (orca.getBodyZRot() / 6) * intensity));
                event.setPitch((float) (event.getPitch() + (orca.getBodyXRot() / 6) * intensity));
            } else if (rootVehicle instanceof KangarooEntity kangaroo) {
                event.setRoll((float) (event.getRoll() + (kangaroo.getBodyZRot() / 6) * intensity));
                event.setPitch((float) (event.getPitch() + (kangaroo.getBodyXRot() / 6) * intensity));
            }
        }
    }

    private static final Set<Integer> shadowStrikeHiddenRiders = new HashSet<>();

    private static boolean hasProcessedThisFrame = false;
    private static int shaderLoadCooldown = 0;
    private static final Matrix4f cachedProj = new Matrix4f();
    private static final Map<UUID, WaypointState> waypointStates = new LinkedHashMap<>();
    private static final Map<UUID, Integer> currentEntityIds = new HashMap<>();

    /**
     * Dernier {@link ClientLevel} pour lequel les waypoints ont été synchronisés.
     * <p>
     * Sert à détecter au rendu un changement de monde/dimension (le ClientLevel change
     * d'instance) et à resynchroniser la persistance sur le BON monde. Corrige le bug où les
     * waypoints d'un monde A apparaissaient dans un monde B : la clé de monde pouvait être
     * résolue trop tôt (au {@code LoggingIn}, avant que le serveur intégré du nouveau monde ne
     * soit prêt), faisant charger le fichier du mauvais monde. Au rendu, le serveur du monde
     * courant est garanti prêt, donc la clé se résout correctement.
     */
    private static java.lang.ref.WeakReference<ClientLevel> lastWaypointLevel =
            new java.lang.ref.WeakReference<>(null);

    private static class WaypointState {
        UUID ownerUUID;
        Vec3 lastPos = null;
        String name = "";
        int fillColor = 0x1A8FFF, borderColor = 0xAADDFF, textColor = 0xFFFFFF;
        int entityColor = 0xFFFFFF;
        int iconSize = 7, maxDist = 2000;
        float minDist = 3.0f, minOpacity = 0.25f, fontScale = 1.0f;
        float visibility = 0f;
        float smoothedPop = 0f;
        boolean hasBeenSeen = false;
        boolean isEnabled = true;
    }

    private record ComputedWaypoint(
            float screenX, float screenY, int dist, float popFactor, float visibility,
            String name, int fillColor, int borderColor, int textColor,
            int iconSize, float minOpacity, float fontScale) {
    }

    private record ComputedCluster(
            float screenX, float screenY, int dist, float popFactor, float visibility,
            List<Integer> fillColors,
            String entityTypeName,
            int entityTypeColor,
            int borderColor, int textColor,
            int iconSize, float minOpacity, float fontScale,
            int totalCount) {
    }

    private static final Map<String, Float> clusterPopSmoothed = new HashMap<>();
    private static final Map<String, ComputedCluster> computedClusters = new LinkedHashMap<>();

    private static final Map<UUID, ComputedWaypoint> computedWaypoints = new LinkedHashMap<>();

    public static boolean isWaypointEnabled(UUID uuid) {
        WaypointState state = waypointStates.get(uuid);
        return state == null || state.isEnabled;
    }

    public static void toggleWaypoint(UUID uuid) {
        waypointStates.computeIfAbsent(uuid, k -> new WaypointState()).isEnabled ^= true;
    }

    private static void computeWaypointScreenPositions(RenderLevelStageEvent event) {
        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        computedWaypoints.clear();
        if (player == null || mc.level == null) return;

        // Changement de monde/dimension détecté au rendu : on resynchronise la persistance des
        // waypoints sur le monde courant (voir lastWaypointLevel). Fait ici car, contrairement au
        // LoggingIn, le serveur intégré du bon monde est garanti prêt à cet instant.
        if (lastWaypointLevel.get() != mc.level) {
            handleWaypointWorldChange(player, mc.level);
        }

        if (mc.options.hideGui) return;

        float pt = event.getPartialTick().getGameTimeDeltaPartialTick(true);

        currentEntityIds.clear();
        for (OWEntity candidate : mc.level.getEntitiesOfClass(
                OWEntity.class, player.getBoundingBox().inflate(2048))) {

            if (!(candidate instanceof IOWWaypointEntity w)) continue;
            if (candidate instanceof net.tiew.operationWild.entity.misc.Submarine) continue;
            if (!player.getUUID().equals(candidate.getOwnerUUID())) continue;

            UUID uuid = candidate.getUUID();

            boolean isCurrentMount = candidate == player.getVehicle();

            if (!candidate.isAlive()) {
                waypointStates.remove(uuid);
                continue;
            }

            currentEntityIds.put(uuid, candidate.getId());
            WaypointState state = waypointStates.computeIfAbsent(uuid, k -> new WaypointState());

            state.lastPos = new Vec3(
                    Mth.lerp(pt, candidate.xOld, candidate.getX()),
                    Mth.lerp(pt, candidate.yOld, candidate.getY()) + candidate.getBbHeight() * 0.5,
                    Mth.lerp(pt, candidate.zOld, candidate.getZ()));
            state.ownerUUID = player.getUUID();
            state.hasBeenSeen = true;
            state.name = candidate.getCustomName() != null
                    ? candidate.getCustomName().getString() : w.getWaypointName();
            state.fillColor = w.getWaypointFillColor();
            state.entityColor = candidate.getEntityColor();
            state.borderColor = w.getWaypointBorderColor();
            state.textColor = w.getWaypointTextColor();
            state.iconSize = w.getWaypointIconSize();
            state.maxDist = w.getWaypointMaxDistance();
            state.minDist = w.getWaypointMinDistance();
            state.minOpacity = w.getWaypointMinOpacity();
            state.fontScale = w.getWaypointDistanceFontScale();

            if (isCurrentMount) continue;
        }

        if (waypointStates.isEmpty()) return;

        int sw = mc.getWindow().getGuiScaledWidth();
        int sh = mc.getWindow().getGuiScaledHeight();
        int margin = 6;

        Camera cam = event.getCamera();
        Matrix4f projView = new Matrix4f(cachedProj)
                .rotate(new Quaternionf(cam.rotation()).conjugate());

        Map<UUID, Float> focusPerUUID = new LinkedHashMap<>();
        for (Map.Entry<UUID, WaypointState> entry : waypointStates.entrySet()) {
            WaypointState state = entry.getValue();
            if (!state.hasBeenSeen || state.lastPos == null) continue;
            if (!state.isEnabled) continue;
            if (!player.getUUID().equals(state.ownerUUID)) continue;

            Vec3 toTarget = state.lastPos.subtract(cam.getPosition());
            Vector4f clipPos = new Vector4f(
                    (float) toTarget.x, (float) toTarget.y, (float) toTarget.z, 1.0f);
            projView.transform(clipPos);
            if (Math.abs(clipPos.w) < 0.001f) {
                focusPerUUID.put(entry.getKey(), 0f);
                continue;
            }

            boolean isBehind = clipPos.w <= 0f;
            float ndcX = clipPos.x / clipPos.w;
            float ndcY = clipPos.y / clipPos.w;
            float angularDist = isBehind ? 2.0f : (float) Math.sqrt(ndcX * ndcX + ndcY * ndcY);
            float focus = Mth.clamp(1.0f - angularDist / 0.1f, 0.0f, 1.0f);
            focusPerUUID.put(entry.getKey(), focus);
        }

        UUID bestUUID = null;
        float bestFocus = 0f;
        for (Map.Entry<UUID, Float> e : focusPerUUID.entrySet()) {
            if (e.getValue() > bestFocus) {
                bestFocus = e.getValue();
                bestUUID = e.getKey();
            }
        }

        for (Map.Entry<UUID, WaypointState> entry : waypointStates.entrySet()) {
            WaypointState state = entry.getValue();
            if (!state.hasBeenSeen || state.lastPos == null) continue;
            if (!player.getUUID().equals(state.ownerUUID)) continue;
            if (!state.isEnabled) continue;

            Integer entityId = currentEntityIds.get(entry.getKey());
            Entity rawEntity = entityId != null ? mc.level.getEntity(entityId) : null;
            double dist = rawEntity != null
                    ? player.distanceTo(rawEntity)
                    : player.position().distanceTo(state.lastPos);

            float targetVisibility;
            float fadeZone = 1.5f;
            if (dist <= state.minDist || dist > state.maxDist) {
                targetVisibility = 0.0f;
            } else if (dist < state.minDist + fadeZone) {
                targetVisibility = (float) (dist - state.minDist) / fadeZone;
            } else {
                targetVisibility = 1.0f;
            }
            state.visibility = Mth.lerp(0.08f, state.visibility, targetVisibility);
            if (state.visibility < 0.01f) continue;

            boolean isChosen = entry.getKey().equals(bestUUID) && bestFocus > 0.3f;
            float targetPop = isChosen ? 1.0f : 0.0f;
            float lerpSpeed = state.smoothedPop < targetPop ? 0.11f : 0.07f;
            state.smoothedPop = Mth.lerp(lerpSpeed, state.smoothedPop, targetPop);

            Vec3 toTarget = state.lastPos.subtract(cam.getPosition());
            Vector4f clipPos = new Vector4f(
                    (float) toTarget.x, (float) toTarget.y, (float) toTarget.z, 1.0f);
            projView.transform(clipPos);
            if (Math.abs(clipPos.w) < 0.001f) continue;

            boolean isBehind = clipPos.w <= 0f;
            float ndcX = clipPos.x / clipPos.w;
            float ndcY = clipPos.y / clipPos.w;

            float screenX = (ndcX + 1f) * 0.5f * sw;
            float screenY = (1f - ndcY) * 0.5f * sh;
            float cx = sw * 0.5f, cy = sh * 0.5f;
            float dx = screenX - cx, dy = screenY - cy;

            boolean onScreen = !isBehind
                    && screenX > margin && screenX < sw - margin
                    && screenY > margin && screenY < sh - margin;
            if (!onScreen) {
                float scaleX = Math.abs(dx) > 0.001f ? (cx - margin) / Math.abs(dx) : Float.MAX_VALUE;
                float scaleY = Math.abs(dy) > 0.001f ? (cy - margin) / Math.abs(dy) : Float.MAX_VALUE;
                float scale = Math.min(scaleX, scaleY);
                screenX = cx + dx * scale;
                screenY = cy + dy * scale;
            }

            computedWaypoints.put(entry.getKey(), new ComputedWaypoint(
                    screenX, screenY, (int) dist, state.smoothedPop, state.visibility,
                    state.name, state.fillColor, state.borderColor, state.textColor,
                    state.iconSize, state.minOpacity, state.fontScale));
        }

        computedClusters.clear();
        final double CLUSTER_WORLD_RADIUS = 12.0;
        final int MIN_CLUSTER_SIZE = 3;

        List<UUID> allOwned = new ArrayList<>();
        for (Map.Entry<UUID, WaypointState> e : waypointStates.entrySet()) {
            WaypointState s = e.getValue();
            if (s.hasBeenSeen && s.lastPos != null && s.isEnabled
                    && player.getUUID().equals(s.ownerUUID))
                allOwned.add(e.getKey());
        }

        Set<UUID> alreadyClustered = new HashSet<>();
        List<List<UUID>> groups = new ArrayList<>();
        List<String> groupKeys = new ArrayList<>();

        for (int i = 0; i < allOwned.size(); i++) {
            UUID seedUUID = allOwned.get(i);
            if (alreadyClustered.contains(seedUUID)) continue;

            WaypointState seedState = waypointStates.get(seedUUID);
            if (player.position().distanceTo(seedState.lastPos) <= 75) continue;

            List<UUID> group = new ArrayList<>();
            group.add(seedUUID);

            for (int j = i + 1; j < allOwned.size(); j++) {
                UUID candUUID = allOwned.get(j);
                if (alreadyClustered.contains(candUUID)) continue;
                WaypointState candState = waypointStates.get(candUUID);
                if (player.position().distanceTo(candState.lastPos) <= 75) continue;
                boolean closeToGroup = false;
                for (UUID memberUUID : group) {
                    if (waypointStates.get(memberUUID).lastPos.distanceTo(candState.lastPos) <= CLUSTER_WORLD_RADIUS) {
                        closeToGroup = true;
                        break;
                    }
                }
                if (closeToGroup) group.add(candUUID);
            }

            if (group.size() < MIN_CLUSTER_SIZE) continue;

            alreadyClustered.addAll(group);
            String key = group.stream().map(UUID::toString).sorted()
                    .collect(java.util.stream.Collectors.joining(","));
            groups.add(group);
            groupKeys.add(key);
        }

        String bestClusterKey = null;
        float bestClusterFocus = 0f;

        for (int gi = 0; gi < groups.size(); gi++) {
            Vec3 avgPos = Vec3.ZERO;
            for (UUID u : groups.get(gi))
                avgPos = avgPos.add(waypointStates.get(u).lastPos);
            avgPos = avgPos.scale(1.0 / groups.get(gi).size());

            Vec3 toTarget = avgPos.subtract(cam.getPosition());
            Vector4f clip = new Vector4f((float) toTarget.x, (float) toTarget.y, (float) toTarget.z, 1f);
            projView.transform(clip);
            if (Math.abs(clip.w) < 0.001f) continue;

            boolean isBehind = clip.w <= 0f;
            float ndcX = clip.x / clip.w, ndcY = clip.y / clip.w;
            float angDist = isBehind ? 2f : (float) Math.sqrt(ndcX * ndcX + ndcY * ndcY);
            float focus = Mth.clamp(1f - angDist / 0.1f, 0f, 1f);
            if (focus > bestClusterFocus) {
                bestClusterFocus = focus;
                bestClusterKey = groupKeys.get(gi);
            }
        }

        Set<String> activeClusterKeys = new HashSet<>();
        Set<UUID> confirmedClusteredUUIDs = new HashSet<>();

        for (int gi = 0; gi < groups.size(); gi++) {
            List<UUID> group = groups.get(gi);
            String key = groupKeys.get(gi);
            activeClusterKeys.add(key);

            WaypointState seed = waypointStates.get(group.get(0));

            Vec3 avgPos = Vec3.ZERO;
            for (UUID u : group) avgPos = avgPos.add(waypointStates.get(u).lastPos);
            avgPos = avgPos.scale(1.0 / group.size());
            double avgDistD = player.position().distanceTo(avgPos);

            float fadeZone = 10f;
            float targetVis;
            if (avgDistD <= seed.minDist) targetVis = 0f;
            else if (avgDistD < seed.minDist + fadeZone) targetVis = (float) (avgDistD - seed.minDist) / fadeZone;
            else targetVis = 1f;

            String visKey = "v_" + key;
            float curVis = clusterPopSmoothed.getOrDefault(visKey, 0f);
            curVis = Mth.lerp(0.08f, curVis, targetVis);
            clusterPopSmoothed.put(visKey, curVis);
            if (curVis >= 0.01f) confirmedClusteredUUIDs.addAll(group);
            if (curVis < 0.01f) continue;

            boolean isChosen = key.equals(bestClusterKey) && bestClusterFocus > 0.3f;
            float curPop = clusterPopSmoothed.getOrDefault(key, 0f);
            curPop = Mth.lerp(curPop < (isChosen ? 1f : 0f) ? 0.11f : 0.07f, curPop, isChosen ? 1f : 0f);
            clusterPopSmoothed.put(key, curPop);

            Vec3 toTarget = avgPos.subtract(cam.getPosition());
            Vector4f clip = new Vector4f((float) toTarget.x, (float) toTarget.y, (float) toTarget.z, 1f);
            projView.transform(clip);
            if (Math.abs(clip.w) < 0.001f) continue;

            boolean isBehind = clip.w <= 0f;
            float ndcX = clip.x / clip.w, ndcY = clip.y / clip.w;
            float scrX = (ndcX + 1f) * 0.5f * sw;
            float scrY = (1f - ndcY) * 0.5f * sh;
            float cx = sw * 0.5f, cy = sh * 0.5f;
            float dx = scrX - cx, dy = scrY - cy;

            if (isBehind || scrX <= margin || scrX >= sw - margin || scrY <= margin || scrY >= sh - margin) {
                float sc = Math.min(
                        Math.abs(dx) > 0.001f ? (cx - margin) / Math.abs(dx) : Float.MAX_VALUE,
                        Math.abs(dy) > 0.001f ? (cy - margin) / Math.abs(dy) : Float.MAX_VALUE);
                scrX = cx + dx * sc;
                scrY = cy + dy * sc;
            }

            List<Integer> colors = new ArrayList<>();
            Map<String, long[]> nameData = new LinkedHashMap<>();
            for (UUID u : group) {
                WaypointState st = waypointStates.get(u);
                if (colors.size() < 5) colors.add(st.fillColor);
                if (!st.name.isEmpty()) {
                    nameData.computeIfAbsent(st.name, k2 -> new long[]{0, st.entityColor})[0]++;
                }
            }

            String mostCommonName = "";
            int mostCommonColor = 0xFFFFFF;
            long bestCount = 0;
            for (Map.Entry<String, long[]> e : nameData.entrySet()) {
                if (e.getValue()[0] > bestCount) {
                    bestCount = e.getValue()[0];
                    mostCommonName = e.getKey();
                    mostCommonColor = (int) e.getValue()[1];
                }
            }

            computedClusters.put(key, new ComputedCluster(
                    scrX, scrY, (int) avgDistD, curPop, curVis,
                    colors, mostCommonName, mostCommonColor,
                    seed.borderColor, seed.textColor,
                    seed.iconSize, seed.minOpacity, seed.fontScale,
                    group.size()));
        }

        for (UUID u : confirmedClusteredUUIDs) computedWaypoints.remove(u);

        clusterPopSmoothed.keySet().removeIf(k -> {
            String base = k.startsWith("v_") ? k.substring(2) : k;
            return !activeClusterKeys.contains(base);
        });
    }

    /**
     * Clé STRICTEMENT unique du monde courant, ou {@code null} si elle ne peut pas être résolue
     * de façon fiable.
     * <p>
     * Bug corrigé : auparavant on retombait sur le nom d'affichage du monde (non unique → deux
     * mondes au même nom partageaient le même fichier) ou sur un bucket partagé {@code "unknown_world"}.
     * Résultat : des waypoints d'un monde apparaissaient dans un autre. On préfère désormais ne RIEN
     * charger/sauvegarder plutôt que d'utiliser un fichier partagé.
     * </p>
     * <ul>
     *   <li>Solo : nom du <b>dossier</b> de sauvegarde (garanti unique par Minecraft).</li>
     *   <li>Multi : IP du serveur.</li>
     * </ul>
     */
    private static String cachedWaypointKey = null;

    private static String resolveUniqueWorldKey() {
        if (cachedWaypointKey != null) return cachedWaypointKey;
        Minecraft mc = Minecraft.getInstance();

        if (mc.hasSingleplayerServer() && mc.getSingleplayerServer() != null) {
            try {
                java.nio.file.Path root = mc.getSingleplayerServer().getWorldPath(LevelResource.ROOT);
                cachedWaypointKey = "sp_" + root.toAbsolutePath().normalize().getFileName().toString();
                return cachedWaypointKey;
            } catch (Exception ignored) {
                return null; // monde non résolu de façon fiable → pas de fichier partagé
            }
        }

        if (mc.getCurrentServer() != null) {
            cachedWaypointKey = "mp_" + mc.getCurrentServer().ip.replace(":", "_").replace(".", "_");
            return cachedWaypointKey;
        }

        return null;
    }

    private static File getWaypointFile(Player player) {
        String key = resolveUniqueWorldKey();
        if (key == null) return null;
        return new File("config/ow_waypoints_" + key.replace(":", "_") + ".dat");
    }

    /**
     * Resynchronise la persistance des waypoints quand le {@link ClientLevel} courant change
     * (changement de monde ou de dimension).
     * <p>
     * Sauvegarde l'état courant dans le fichier du monde qu'on quitte (clé encore en cache),
     * invalide les clés de monde en cache, vide la mémoire, puis recharge depuis le fichier du
     * monde courant — dont la clé est désormais résolue correctement (serveur intégré prêt).
     */
    private static void handleWaypointWorldChange(Player player, ClientLevel newLevel) {
        saveWaypointStates(player);
        cachedWaypointKey = null;
        cachedWorldName = null;
        waypointStates.clear();
        computedWaypoints.clear();
        computedClusters.clear();
        clusterPopSmoothed.clear();
        currentEntityIds.clear();
        loadWaypointStates(player);
        lastWaypointLevel = new java.lang.ref.WeakReference<>(newLevel);
    }

    public static void saveAndClearWaypoints(Player player) {
        saveWaypointStates(player);
        waypointStates.clear();
        computedWaypoints.clear();
        computedClusters.clear();
        clusterPopSmoothed.clear();
        cachedWorldName = null;
        cachedWaypointKey = null;
    }

    private static void saveWaypointStates(Player player) {
        File file = getWaypointFile(player);
        if (file == null) return; // monde non résolu : on ne sauvegarde pas dans un fichier partagé
        try (DataOutputStream out = new DataOutputStream(new FileOutputStream(file))) {
            List<Map.Entry<UUID, WaypointState>> toSave = waypointStates.entrySet().stream()
                    .filter(e -> e.getValue().hasBeenSeen && e.getValue().lastPos != null)
                    .toList();
            out.writeInt(toSave.size());
            for (Map.Entry<UUID, WaypointState> entry : toSave) {
                WaypointState s = entry.getValue();
                out.writeUTF(entry.getKey().toString());
                out.writeUTF(s.ownerUUID != null ? s.ownerUUID.toString() : "");
                out.writeDouble(s.lastPos.x);
                out.writeDouble(s.lastPos.y);
                out.writeDouble(s.lastPos.z);
                out.writeUTF(s.name);
                out.writeInt(s.fillColor);
                out.writeInt(s.borderColor);
                out.writeInt(s.textColor);
                out.writeInt(s.iconSize);
                out.writeInt(s.maxDist);
                out.writeFloat(s.minDist);
                out.writeFloat(s.minOpacity);
                out.writeFloat(s.fontScale);
                out.writeBoolean(s.isEnabled);
                out.writeInt(s.entityColor);
            }
        } catch (Exception e) {
        }
    }

    private static void loadWaypointStates(Player player) {
        waypointStates.clear();
        File file = getWaypointFile(player);
        if (file == null || !file.exists()) return;
        try (DataInputStream in = new DataInputStream(new FileInputStream(file))) {
            int count = in.readInt();
            for (int i = 0; i < count; i++) {
                UUID uuid = UUID.fromString(in.readUTF());
                String ownerStr = in.readUTF();
                double x = in.readDouble(), y = in.readDouble(), z = in.readDouble();
                String name = in.readUTF();

                WaypointState s = new WaypointState();
                s.ownerUUID = ownerStr.isEmpty() ? null : UUID.fromString(ownerStr);
                s.lastPos = new Vec3(x, y, z);
                s.name = name;
                s.hasBeenSeen = true;
                s.fillColor = in.readInt();
                s.borderColor = in.readInt();
                s.textColor = in.readInt();
                s.iconSize = in.readInt();
                s.maxDist = in.readInt();
                s.minDist = in.readFloat();
                s.minOpacity = in.readFloat();
                s.fontScale = in.readFloat();
                s.isEnabled = in.readBoolean();
                s.entityColor = in.readInt();
                waypointStates.put(uuid, s);
            }
        } catch (Exception e) {
            waypointStates.clear();
        }
    }

    @SubscribeEvent
    public static void onRenderLevelStage(RenderLevelStageEvent event) {
        if (event.getStage() == RenderLevelStageEvent.Stage.AFTER_SKY) {
            hasProcessedThisFrame = false;
            cachedProj.set(RenderSystem.getProjectionMatrix());
        }

        if (event.getStage() == RenderLevelStageEvent.Stage.AFTER_PARTICLES) {
            renderPassiveEsp(event);
            renderThermalHearts(event);
            computeWaypointScreenPositions(event);
        }

        if (event.getStage() == RenderLevelStageEvent.Stage.AFTER_LEVEL) {
            Minecraft minecraft = Minecraft.getInstance();
            Player player = minecraft.player;

            if (player != null) {
                Entity vehicle = player.getVehicle();
                boolean hasVenom = player.hasEffect(OWEffects.VENOM_EFFECT.getDelegate())
                        || (vehicle instanceof LivingEntity le && le.hasEffect(OWEffects.VENOM_EFFECT.getDelegate()));
                boolean shouldApplyBlur = hasVenom && minecraft.screen == null;

                PostChain currentEffect = minecraft.gameRenderer.currentEffect();
                boolean isSubmarineEffect = currentEffect != null
                        && currentEffect.getName().equals("ow:shaders/post/submarine_light.json");

                if (!isSubmarineEffect) {
                    if (shouldApplyBlur) {
                        applyMinecraftBlurShader(player);
                    } else {
                        if (blurPercentage > 0 || currentVenomBlur != null) removeMinecraftBlurShader();
                    }
                }

                boolean isOurEffect = currentEffect != null
                        && currentEffect.getName().equals("ow:shaders/post/submarine_light.json");

                boolean shouldActivate = shouldActivateSubmarineEffect(player)
                        && (!blinkSubmarineShader || blinkShaderOn);

                if (minecraft.screen == null && shouldActivate) {
                    if (currentEffect == null && shaderLoadCooldown <= 0) {
                        try {
                            minecraft.gameRenderer.loadEffect(ResourceLocation.parse("ow:shaders/post/submarine_light.json"));
                            if (minecraft.gameRenderer.currentEffect() == null) {
                                shaderLoadCooldown = 12000;
                            }
                        } catch (Exception e) {
                            shaderLoadCooldown = 12000;
                        }
                    } else if (isOurEffect && !hasProcessedThisFrame) {
                        if (vehicle instanceof Submarine submarine) {
                            pushSubmarineShaderUniforms(currentEffect, submarine);
                        }
                        currentEffect.process(minecraft.getTimer().getGameTimeDeltaPartialTick(true));
                        hasProcessedThisFrame = true;
                    }
                } else if (isOurEffect) {
                    minecraft.gameRenderer.shutdownEffect();
                }
            }
        }
    }

    @SubscribeEvent
    public static void onRenderWaypoint(RenderGuiEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.options.hideGui || (computedWaypoints.isEmpty() && computedClusters.isEmpty()))
            return;

        for (ComputedWaypoint wp : computedWaypoints.values()) {
            drawWaypoint(event.getGuiGraphics(), mc,
                    wp.screenX(), wp.screenY(), wp.dist(),
                    wp.popFactor(), wp.visibility(), wp.name(),
                    wp.fillColor(), wp.borderColor(), wp.textColor(),
                    wp.iconSize(), wp.minOpacity(), wp.fontScale());
        }

        for (ComputedCluster cl : computedClusters.values()) {
            drawClusteredWaypoint(event.getGuiGraphics(), mc, cl);
        }
    }

    @OnlyIn(Dist.CLIENT)
    private static void drawClusteredWaypoint(GuiGraphics gui, Minecraft mc, ComputedCluster cl) {
        float vis = cl.visibility();
        float pop = cl.popFactor();

        float baseOpacity = Math.min(cl.minOpacity() + 0.10f, 0.65f);
        int fillAlpha = (int) (Mth.lerp(pop, baseOpacity * 0xCC, 0xCC) * vis);
        int borderAlpha = (int) (Mth.lerp(pop, baseOpacity * 0xFF, 0xFF) * vis);
        int distAlpha = (int) (Mth.lerp(pop, baseOpacity * 0xBB, 0xDD) * vis);

        float nameFade = Mth.clamp((pop - 0.6f) / 0.4f, 0f, 1f);
        nameFade = nameFade * nameFade * (3f - 2f * nameFade);
        int nameAlpha = (int) (nameFade * 0xFE * vis);

        float totalScale = Mth.lerp(pop, 0.55f, 0.85f) * vis;
        int iconHalf = cl.iconSize() + 2;
        int border = (borderAlpha << 24) | cl.borderColor();

        List<Integer> colors = cl.fillColors();
        int n = colors.size();

        PoseStack pose = gui.pose();

        pose.pushPose();
        pose.translate(cl.screenX(), cl.screenY(), 300);
        pose.scale(totalScale, totalScale, 1f);
        pose.mulPose(Axis.ZP.rotationDegrees(45));

        int totalH = iconHalf * 2;
        for (int i = 0; i < n; i++) {
            int y1 = -iconHalf + (i * totalH / n);
            int y2 = -iconHalf + ((i + 1) * totalH / n);
            gui.fill(-iconHalf, y1, iconHalf, y2, (fillAlpha << 24) | colors.get(i));
        }
        int sepAlpha = Math.min(borderAlpha, 0x55);
        for (int i = 1; i < n; i++) {
            int sepY = -iconHalf + (i * totalH / n);
            gui.fill(-iconHalf, sepY, iconHalf, sepY + 1, (sepAlpha << 24) | 0xFFFFFF);
        }
        gui.fill(-iconHalf, -iconHalf, iconHalf, -iconHalf + 1, border);
        gui.fill(-iconHalf, iconHalf - 1, iconHalf, iconHalf, border);
        gui.fill(-iconHalf, -iconHalf, -iconHalf + 1, iconHalf, border);
        gui.fill(iconHalf - 1, -iconHalf, iconHalf, iconHalf, border);
        pose.popPose();

        float scaledHalf = iconHalf * totalScale;
        float effectiveFontSc = cl.fontScale() * (1.0f + 0.4f * pop);

        if (nameAlpha > 3 && !cl.entityTypeName().isEmpty()) {
            String prefix = Component.translatable("owwild.waypoint.group_of").getString() + " ";
            String typeName = cl.entityTypeName();

            int prefixW = mc.font.width(prefix);
            int nameW = mc.font.width(typeName) + 1;
            int totalW = prefixW + nameW;
            int startX = -totalW / 2;

            float nameY = cl.screenY() - scaledHalf - mc.font.lineHeight * effectiveFontSc - 2f;
            if (nameY < 2f) nameY = cl.screenY() + scaledHalf + mc.font.lineHeight * effectiveFontSc + 4f;

            pose.pushPose();
            pose.translate(cl.screenX(), nameY, 0);
            pose.scale(effectiveFontSc, effectiveFontSc, 1f);

            int prefixColor = (nameAlpha << 24) | (cl.textColor() & 0xFFFFFF);
            int entityColor = (nameAlpha << 24) | (cl.entityTypeColor() & 0xFFFFFF);

            gui.drawString(mc.font, prefix, startX, 0, prefixColor, true);
            gui.drawString(mc.font, typeName, startX + prefixW, 0, entityColor, true);
            gui.drawString(mc.font, typeName, startX + prefixW + 1, 0, entityColor, false);
            pose.popPose();
        }

        float baseTextY = cl.screenY() + scaledHalf + 3f;

        if (nameAlpha > 3) {
            String countLabel = "\u00d7" + cl.totalCount();
            pose.pushPose();
            pose.translate(cl.screenX(), baseTextY, 0);
            pose.scale(effectiveFontSc * 1.1f, effectiveFontSc * 1.1f, 1f);
            gui.drawString(mc.font, countLabel,
                    -mc.font.width(countLabel) / 2, 0,
                    (nameAlpha << 24) | cl.textColor(), true);
            pose.popPose();
            baseTextY += mc.font.lineHeight * effectiveFontSc * 1.1f + 1f;
        }

        if (distAlpha > 3) {
            String distLabel = cl.dist() >= 1000
                    ? String.format("%.1f km", cl.dist() / 1000f)
                    : cl.dist() + " m";
            pose.pushPose();
            pose.translate(cl.screenX(), baseTextY, 0);
            pose.scale(effectiveFontSc, effectiveFontSc, 1f);
            gui.drawString(mc.font, distLabel,
                    -mc.font.width(distLabel) / 2, 0,
                    (distAlpha << 24) | cl.textColor(), true);
            pose.popPose();
        }
    }

    @OnlyIn(Dist.CLIENT)
    private static void drawWaypoint(GuiGraphics gui, Minecraft mc, float x, float y, int distance,
                                     float popFactor, float visibility, String name,
                                     int fillColor, int borderColor, int textColor, int iconSize,
                                     float minOpacity, float fontScale) {
        float totalScale = Mth.lerp(popFactor, 0.55f, 1.35f) * visibility;
        int iconHalf = iconSize;

        float baseOpacity = Math.min(minOpacity + 0.10f, 0.65f);
        int fillAlpha = (int) (Mth.lerp(popFactor, baseOpacity * 0xCC, 0xCC) * visibility);
        int borderAlpha = (int) (Mth.lerp(popFactor, baseOpacity * 0xFF, 0xFF) * visibility);
        int distAlpha = (int) (Mth.lerp(popFactor, baseOpacity * 0xBB, 0xDD) * visibility);
        float nameFade = Mth.clamp((popFactor - 0.6f) / 0.4f, 0.0f, 1.0f);
        nameFade = nameFade * nameFade * (3f - 2f * nameFade);
        int nameAlpha = (int) (nameFade * 0xFE * visibility);

        int fill = (fillAlpha << 24) | fillColor;
        int border = (borderAlpha << 24) | borderColor;

        PoseStack pose = gui.pose();

        pose.pushPose();
        pose.translate(x, y, 300);
        pose.scale(totalScale, totalScale, 1f);
        pose.mulPose(Axis.ZP.rotationDegrees(45));
        gui.fill(-iconHalf, -iconHalf, iconHalf, iconHalf, fill);
        gui.fill(-iconHalf, -iconHalf, iconHalf, -iconHalf + 1, border);
        gui.fill(-iconHalf, iconHalf - 1, iconHalf, iconHalf, border);
        gui.fill(-iconHalf, -iconHalf, -iconHalf + 1, iconHalf, border);
        gui.fill(iconHalf - 1, -iconHalf, iconHalf, iconHalf, border);
        pose.popPose();

        float scaledHalf = iconHalf * totalScale;

        float effectiveFontScale = fontScale * (1.0f + 0.4f * popFactor);
        String distLabel = distance >= 1000
                ? String.format("%.1f km", distance / 1000f)
                : distance + " m";
        float distTextY = y + scaledHalf + 3f;
        pose.pushPose();
        pose.translate(x, distTextY, 0);
        pose.scale(effectiveFontScale, effectiveFontScale, 1f);
        gui.drawString(mc.font, distLabel, -mc.font.width(distLabel) / 2, 0, (distAlpha << 24) | textColor, true);
        pose.popPose();

        if (distAlpha > 3) {
            pose.pushPose();
            pose.translate(x, distTextY, 0);
            pose.scale(effectiveFontScale, effectiveFontScale, 1f);
            gui.drawString(mc.font, distLabel, -mc.font.width(distLabel) / 2, 0, (distAlpha << 24) | textColor, true);
            pose.popPose();
        }

        if (nameAlpha > 3 && !name.isEmpty()) {
            float nameY = y - scaledHalf - mc.font.lineHeight - 2f;
            if (nameY < 2f) nameY = distTextY + mc.font.lineHeight * fontScale + 1f;
            pose.pushPose();
            pose.translate(x, nameY, 0);
            gui.drawString(mc.font, name, -mc.font.width(name) / 2, 0, (nameAlpha << 24) | textColor, true);
            pose.popPose();
        }
    }

    @OnlyIn(Dist.CLIENT)
    private static void pushSubmarineShaderUniforms(PostChain chain, Submarine submarine) {
        try {
            Field passesField = chain.getClass().getDeclaredField("passes");
            passesField.setAccessible(true);
            List<?> passes = (List<?>) passesField.get(chain);
            float[] color = submarine.getShaderLightColor();
            for (Object pass : passes) {
                Object effect = findShaderEffectInPass(pass);
                if (effect == null) continue;
                applyUniform1f(effect, "LightSeparation", submarine.getShaderLightSeparation());
                applyUniform1f(effect, "LightY", submarine.getShaderLightY());
                applyUniform1f(effect, "SpotRadius", submarine.getShaderSpotRadius());
                applyUniform1f(effect, "ContrastPow", submarine.getShaderContrastPow());
                applyUniform1f(effect, "AdditiveStrength", submarine.getShaderAdditiveStrength());
                applyUniform1f(effect, "MultiplicativeStrength", submarine.getShaderMultiplicativeStrength());
                applyUniform1f(effect, "ShadowFactor", submarine.getShaderShadowFactor());
                applyUniform3f(effect, "LightColor", color[0], color[1], color[2]);
                applyUniform1f(effect, "SingleBeam", submarine.isSingleBeam() ? 1.0f : 0.0f);
            }
        } catch (Exception ignored) {
        }
    }

    private static Object findShaderEffectInPass(Object pass) {
        for (Field f : pass.getClass().getDeclaredFields()) {
            try {
                f.setAccessible(true);
                Object val = f.get(pass);
                if (val != null) {
                    val.getClass().getMethod("safeGetUniform", String.class);
                    return val;
                }
            } catch (Exception ignored) {
            }
        }
        return null;
    }

    private static void applyUniform1f(Object effect, String name, float value) {
        try {
            Method get = effect.getClass().getMethod("safeGetUniform", String.class);
            Object u = get.invoke(effect, name);
            if (u != null) u.getClass().getMethod("set", float.class).invoke(u, value);
        } catch (Exception ignored) {
        }
    }

    private static void applyUniform3f(Object effect, String name, float r, float g, float b) {
        try {
            Method get = effect.getClass().getMethod("safeGetUniform", String.class);
            Object u = get.invoke(effect, name);
            if (u != null) u.getClass().getMethod("set", float.class, float.class, float.class).invoke(u, r, g, b);
        } catch (Exception ignored) {
        }
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Pre event) {
        hasProcessedThisFrame = false;
        if (shaderLoadCooldown > 0) shaderLoadCooldown--;

        if (blinkSubmarineShader) {
            if (blinkTimer <= 0) {
                blinkShaderOn = !blinkShaderOn;
                blinkTimer = 4 + (int) (Math.random() * 17);
            }
            blinkTimer--;
        } else {
            blinkTimer = 0;
            blinkShaderOn = false;
        }
        OWAttacksInformation.tick();

        if (OWAttackLogic.isCrocTargeting) {
            Minecraft mcT = Minecraft.getInstance();
            if (mcT.player == null || mcT.level == null
                    || !(mcT.player.getRootVehicle() instanceof net.tiew.operationWild.entity.animals.aquatic.CrocodileEntity crocT)
                    || System.currentTimeMillis() - OWAttackLogic.crocTargetingStartMs
                    >= OWAttacksConstants.Crocodile.PRIMAL_DIVE_TARGETING_MS) {
                OWAttackLogic.cancelCrocTargeting(OWAttacksHandler.PRIMAL_DIVE_ID);
            } else {
                updateCrocTargeting(crocT, mcT.player, mcT.level);
            }
        }

        if (OWAttackLogic.isBoaTargeting) {
            Minecraft mcB = Minecraft.getInstance();
            if (mcB.player == null || mcB.level == null
                    || !(mcB.player.getRootVehicle() instanceof net.tiew.operationWild.entity.animals.terrestrial.BoaEntity boaT)
                    || System.currentTimeMillis() - OWAttackLogic.boaTargetingStartMs
                    >= OWAttacksConstants.Boa.CONSTRICT_ULT_TARGETING_MS) {
                OWAttackLogic.cancelBoaTargeting(OWAttacksHandler.CONSTRICT_ULTIMATE_ID);
            } else {
                updateBoaTargeting(boaT, mcB.player, mcB.level);
            }
        }
    }

    private static void updateBoaTargeting(
            net.tiew.operationWild.entity.animals.terrestrial.BoaEntity boa,
            Player player, net.minecraft.client.multiplayer.ClientLevel level) {

        Vec3 boaPos = boa.position();
        Vec3 lookVec = player.getLookAngle();
        double r = OWAttacksConstants.Boa.CONSTRICT_ULT_RANGE;

        AABB box = boa.getBoundingBox().inflate(r);
        LivingEntity best = null;
        double bestDot = Double.NEGATIVE_INFINITY;

        for (LivingEntity candidate : level.getEntitiesOfClass(LivingEntity.class, box,
                e -> e != player && boa.canConstrict(e) && boa.distanceToSqr(e) <= r * r)) {
            Vec3 dir = candidate.getBoundingBox().getCenter().subtract(boaPos).normalize();
            double dot = lookVec.dot(dir);
            if (dot > bestDot) {
                bestDot = dot;
                best = candidate;
            }
        }

        if (best != null && bestDot > 0.3) {
            OWAttackLogic.boaTargetEntityId = best.getId();
        }

        if (OWAttackLogic.boaTargetEntityId != -1) {
            Entity cur = level.getEntity(OWAttackLogic.boaTargetEntityId);
            if (!(cur instanceof LivingEntity le) || !boa.canConstrict(le)
                    || boa.distanceToSqr(le) > r * r) {
                OWAttackLogic.boaTargetEntityId = -1;
            }
        }
    }

    private static void updateCrocTargeting(
            net.tiew.operationWild.entity.animals.aquatic.CrocodileEntity croc,
            Player player, net.minecraft.client.multiplayer.ClientLevel level) {

        Vec3 crocPos = croc.position();
        Vec3 lookVec = player.getLookAngle();
        double radius = 10.0;

        AABB box = new AABB(crocPos.x - radius, crocPos.y - radius, crocPos.z - radius,
                crocPos.x + radius, crocPos.y + radius, crocPos.z + radius);

        LivingEntity best = null;
        double bestDot = Double.NEGATIVE_INFINITY;

        for (LivingEntity candidate : level.getEntitiesOfClass(LivingEntity.class, box, e -> {
            if (e == croc || e == player) return false;
            if (!e.isAlive() || !e.isInWater()) return false;
            if (croc.isAlliedTo(e)) return false;
            if (e instanceof net.tiew.operationWild.entity.animals.aquatic.CrocodileEntity) return false;
            if (e instanceof net.tiew.operationWild.entity.OWEntity ow && ow.getTheoreticalScale() >= 10) return false;
            return true;
        })) {
            Vec3 dir = candidate.getBoundingBox().getCenter().subtract(crocPos).normalize();
            double dot = lookVec.dot(dir);
            if (dot > bestDot) {
                bestDot = dot;
                best = candidate;
            }
        }

        if (best != null && bestDot > 0.3) {
            OWAttackLogic.crocTargetEntityId = best.getId();
        }

        if (OWAttackLogic.crocTargetEntityId != -1) {
            Entity cur = level.getEntity(OWAttackLogic.crocTargetEntityId);
            if (!(cur instanceof LivingEntity le) || !le.isAlive() || !le.isInWater())
                OWAttackLogic.crocTargetEntityId = -1;
        }
    }

    private static boolean shouldActivateSubmarineEffect(Player player) {
        return Minecraft.getInstance().options.getCameraType().isFirstPerson() && player.getVehicle() instanceof Submarine submarine && submarine.isLightOn() && submarine.isInWater() && player.isInWater() && !submarine.isOff();
    }

    private static void renderPassiveEsp(RenderLevelStageEvent event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) return;
        if (!(mc.player.getRootVehicle() instanceof OWEntity owEntity)) return;

        OWPassive passive = OWAttacksHandler.getPassive(owEntity.getClass());
        if (passive == null) return;

        java.util.Set<Integer> ids = passive.getHighlightEntityIds(owEntity, mc.level);
        if (ids.isEmpty()) return;

        org.joml.Quaternionf camRot = event.getCamera().rotation();
        org.joml.Vector3f jRight = new org.joml.Vector3f(1f, 0f, 0f);
        org.joml.Vector3f jUp = new org.joml.Vector3f(0f, 1f, 0f);
        camRot.transform(jRight);
        camRot.transform(jUp);

        Vec3 cam = event.getCamera().getPosition();
        PoseStack pose = event.getPoseStack();
        pose.pushPose();
        pose.translate(-cam.x, -cam.y, -cam.z);
        Matrix4f matrix = pose.last().pose();

        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        RenderSystem.disableDepthTest();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        Tesselator tes = Tesselator.getInstance();
        BufferBuilder buf = tes.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);

        boolean anyVertex = false;
        for (int id : ids) {
            if (!(mc.level.getEntity(id) instanceof LivingEntity le) || !le.isAlive()) continue;
            Vec3 center = le.getBoundingBox().getCenter();
            if (owEntity instanceof CrocodileEntity
                    || owEntity instanceof net.tiew.operationWild.entity.animals.terrestrial.BoaEntity) {
                addEspGreenDot(buf, matrix, center, jRight, jUp);
            } else {
                addEspGlowGradient(buf, matrix, center, jRight, jUp);
            }
            anyVertex = true;
        }

        if (anyVertex) {
            MeshData mesh = buf.build();
            if (mesh != null) BufferUploader.drawWithShader(mesh);
        }

        RenderSystem.disableBlend();
        RenderSystem.enableDepthTest();
        pose.popPose();
    }

    private static final int[][] HEART_MASK = {
            {0, 1, 1, 0, 1, 1, 0},
            {1, 1, 1, 1, 1, 1, 1},
            {1, 1, 1, 1, 1, 1, 1},
            {0, 1, 1, 1, 1, 1, 0},
            {0, 0, 1, 1, 1, 0, 0},
            {0, 0, 0, 1, 0, 0, 0},
    };

    private static final java.util.Map<Integer, Long> heartHitTimes = new java.util.HashMap<>();
    private static final float HEART_HIT_DURATION_MS = 350f;

    public static void triggerHeartHit(int entityId) {
        heartHitTimes.put(entityId, System.currentTimeMillis());
    }

    private static void renderThermalHearts(RenderLevelStageEvent event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) return;
        if (OWAttackLogic.isBoaTargeting) return;
        if (!(mc.player.getRootVehicle() instanceof net.tiew.operationWild.entity.animals.terrestrial.BoaEntity boa))
            return;
        if (boa.isGrabbing() && boa.getGrabbedTargetId() == mc.player.getId()) return;

        double range = OWAttacksConstants.Boa.THERMAL_RANGE;
        AABB box = boa.getBoundingBox().inflate(range);
        List<LivingEntity> targets = mc.level.getEntitiesOfClass(LivingEntity.class, box, boa::isThermalHeartTarget);
        if (targets.isEmpty()) return;

        org.joml.Quaternionf camRot = event.getCamera().rotation();
        Vector3f right = new Vector3f(1f, 0f, 0f);
        Vector3f up = new Vector3f(0f, 1f, 0f);
        camRot.transform(right);
        camRot.transform(up);

        Vec3 cam = event.getCamera().getPosition();
        PoseStack pose = event.getPoseStack();
        pose.pushPose();
        pose.translate(-cam.x, -cam.y, -cam.z);
        Matrix4f matrix = pose.last().pose();

        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        RenderSystem.disableDepthTest();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        Tesselator tes = Tesselator.getInstance();
        BufferBuilder buf = tes.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        boolean any = false;
        for (LivingEntity le : targets) {
            if (!le.isAlive()) continue;
            Vec3 c = net.tiew.operationWild.entity.animals.terrestrial.BoaEntity.thermalHeartCenter(le);
            net.minecraft.world.phys.BlockHitResult clip = mc.level.clip(new net.minecraft.world.level.ClipContext(
                    cam, c,
                    net.minecraft.world.level.ClipContext.Block.COLLIDER,
                    net.minecraft.world.level.ClipContext.Fluid.NONE, mc.player));
            if (clip.getType() != net.minecraft.world.phys.HitResult.Type.MISS) continue;

            float scale = 1f, gray = 0f;
            Long hit = heartHitTimes.get(le.getId());
            if (hit != null) {
                long el = System.currentTimeMillis() - hit;
                if (el >= 0 && el <= HEART_HIT_DURATION_MS) {
                    float pulse = (float) Math.sin((el / HEART_HIT_DURATION_MS) * Math.PI);
                    scale = 1f + 0.6f * pulse;
                    gray = pulse;
                } else if (el > HEART_HIT_DURATION_MS) {
                    heartHitTimes.remove(le.getId());
                }
            }

            float sizeScale = net.tiew.operationWild.entity.animals.terrestrial.BoaEntity.thermalSizeScale(le);
            addHeart(buf, matrix, c, right, up, scale * sizeScale, gray);
            any = true;
        }
        if (any) {
            MeshData mesh = buf.build();
            if (mesh != null) BufferUploader.drawWithShader(mesh);
        }

        RenderSystem.disableBlend();
        RenderSystem.enableDepthTest();
        pose.popPose();
    }

    private static int heartMaskAt(int r, int c) {
        if (r < 0 || r >= HEART_MASK.length || c < 0 || c >= HEART_MASK[0].length) return 0;
        return HEART_MASK[r][c];
    }

    private static void addHeart(BufferBuilder buf, Matrix4f m, Vec3 center, Vector3f right, Vector3f up,
                                 float scale, float gray) {
        final int rows = HEART_MASK.length, cols = HEART_MASK[0].length;
        final float p = 0.039f * scale;
        final float half = p * 0.5f;
        final float heartR = 0.85f + (0.5f - 0.85f) * gray;
        final float heartG = 0.05f + (0.5f - 0.05f) * gray;
        final float heartB = 0.05f + (0.5f - 0.05f) * gray;
        for (int r = -1; r <= rows; r++) {
            for (int col = -1; col <= cols; col++) {
                boolean lit = heartMaskAt(r, col) == 1;
                boolean outline = false;
                if (!lit) {
                    outline = heartMaskAt(r - 1, col) == 1 || heartMaskAt(r + 1, col) == 1
                            || heartMaskAt(r, col - 1) == 1 || heartMaskAt(r, col + 1) == 1;
                }
                if (!lit && !outline) continue;

                float rr, gg, bb;
                if (lit) {
                    rr = heartR;
                    gg = heartG;
                    bb = heartB;
                } else {
                    rr = 0.0f;
                    gg = 0.0f;
                    bb = 0.0f;
                }

                float xi = (col - (cols - 1) / 2f) * p;
                float yi = ((rows - 1) / 2f - r) * p;
                float cx = (float) center.x + right.x * xi + up.x * yi;
                float cy = (float) center.y + right.y * xi + up.y * yi;
                float cz = (float) center.z + right.z * xi + up.z * yi;
                float rx = right.x * half, ry = right.y * half, rz = right.z * half;
                float ux = up.x * half, uy = up.y * half, uz = up.z * half;
                buf.addVertex(m, cx - rx - ux, cy - ry - uy, cz - rz - uz).setColor(rr, gg, bb, 1f);
                buf.addVertex(m, cx + rx - ux, cy + ry - uy, cz + rz - uz).setColor(rr, gg, bb, 1f);
                buf.addVertex(m, cx + rx + ux, cy + ry + uy, cz + rz + uz).setColor(rr, gg, bb, 1f);
                buf.addVertex(m, cx - rx + ux, cy - ry + uy, cz - rz + uz).setColor(rr, gg, bb, 1f);
            }
        }
    }

    private static void addEspGlowGradient(BufferBuilder buf, Matrix4f matrix,
                                           Vec3 center, Vector3f right, Vector3f up) {
        float[][] layers = {
                {0.30f, 1.0f, 0.0f, 0.0f, 0.20f},
                {0.20f, 1.0f, 0.35f, 0.0f, 0.25f},
                {0.12f, 1.0f, 0.75f, 0.0f, 0.30f},
                {0.06f, 1.0f, 1.0f, 0.0f, 0.40f},
        };

        for (float[] l : layers) {
            float s = l[0], r = l[1], g = l[2], b = l[3], a = l[4];
            addQuad(buf, matrix, center, right, up, s, r, g, b, a);
        }
    }

    private static void addEspGreenDot(BufferBuilder buf, Matrix4f matrix,
                                       Vec3 center, Vector3f right, Vector3f up) {
        addQuad(buf, matrix, center, right, up, 0.12f, 0.0f, 1.0f, 0.2f, 0.70f);
    }

    private static void addQuad(BufferBuilder buf, Matrix4f matrix,
                                Vec3 center, Vector3f right, Vector3f up,
                                float size, float r, float g, float b, float a) {
        float cx = (float) center.x;
        float cy = (float) center.y;
        float cz = (float) center.z;

        float[][] corners = {
                {cx + (-right.x - up.x) * size, cy + (-right.y - up.y) * size, cz + (-right.z - up.z) * size},
                {cx + (right.x - up.x) * size, cy + (right.y - up.y) * size, cz + (right.z - up.z) * size},
                {cx + (right.x + up.x) * size, cy + (right.y + up.y) * size, cz + (right.z + up.z) * size},
                {cx + (-right.x + up.x) * size, cy + (-right.y + up.y) * size, cz + (-right.z + up.z) * size},
        };

        for (float[] c : corners) {
            buf.addVertex(matrix, c[0], c[1], c[2]).setColor(r, g, b, a);
        }
    }

    private static void addEspGlow(BufferBuilder buf, Matrix4f matrix, Vec3 center,
                                   org.joml.Vector3f right, org.joml.Vector3f up, int rgb) {
        float br = ((rgb >> 16) & 0xFF) / 255f;
        float bg = ((rgb >> 8) & 0xFF) / 255f;
        float bb = (rgb & 0xFF) / 255f;
        long t = System.currentTimeMillis();
        float pulse = 0.70f + 0.30f * (float) Math.abs(Math.sin(t * Math.PI / 700.0));

        float[][] layers = {
                {0.44f, 0.10f * pulse},
                {0.30f, 0.20f * pulse},
                {0.19f, 0.36f * pulse},
                {0.11f, 0.58f * pulse},
                {0.06f, 0.82f * pulse},
        };
        float[] intensities = {0.45f, 0.80f, 1.00f, 1.00f, 1.00f};
        for (int i = 0; i < layers.length; i++) {
            float sz = layers[i][0], a = layers[i][1], f = intensities[i];
            espBillboard(buf, matrix, center, right, up, sz, br * f, bg * f, bb * f, a);
        }
    }

    private static void espBillboard(BufferBuilder buf, Matrix4f m, Vec3 center,
                                     org.joml.Vector3f right, org.joml.Vector3f up,
                                     float size, float r, float g, float b, float a) {
        float cx = (float) center.x, cy = (float) center.y, cz = (float) center.z;
        float rx = right.x * size, ry = right.y * size, rz = right.z * size;
        float ux = up.x * size, uy = up.y * size, uz = up.z * size;
        buf.addVertex(m, cx - rx - ux, cy - ry - uy, cz - rz - uz).setColor(r, g, b, a);
        buf.addVertex(m, cx + rx - ux, cy + ry - uy, cz + rz - uz).setColor(r, g, b, a);
        buf.addVertex(m, cx + rx + ux, cy + ry + uy, cz + rz + uz).setColor(r, g, b, a);
        buf.addVertex(m, cx - rx + ux, cy - ry + uy, cz - rz + uz).setColor(r, g, b, a);
    }
}
