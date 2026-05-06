package net.tiew.operationWild.event;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.world.phys.AABB;
import net.tiew.operationWild.ClientConfig;
import net.tiew.operationWild.entity.animals.aquatic.OrcaEntity;
import net.tiew.operationWild.entity.attacks.OWAttacksConstants;
import net.tiew.operationWild.entity.misc.SeaBugEntity;
import net.tiew.operationWild.entity.misc.Submarine;
import net.tiew.operationWild.entity.variants.CrocodileVariant;
import net.tiew.operationWild.entity.variants.KodiakVariant;
import org.joml.Matrix4f;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.ContainerScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
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
import net.tiew.operationWild.entity.quests.daily_quests.DailyQuestsDate;
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
    public static double tamingExperience = 0;
    private static float damageTimer = 0.0f;

    private static int questUpdateTick = 0;

    @SubscribeEvent
    public static void onDebate(InputEvent.MouseButton.Pre event) {
        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;

        if (player != null) {
            if (event.getButton() == 1 && event.getAction() == 1) {
                if (player.getVehicle() instanceof CrocodileEntity crocodile && crocodile.getOwner() != player && crocodile.isGrabbing() && crocodile.getGrabbedTarget() == player) {
                    if (RightClickAlertOverlay.clickAnimationTimer <= 0) {
                        if (crocodile.getGrabTimeout() <= 0) {
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
                    player.getVehicle() instanceof TigerEntity tiger && tiger.getGrabbedTarget() == player) {
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
                // Submarine checked first — it extends OWEntity but uses its own right-click logic
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

            if (OWKeysBinding.PET_INVENTORY.isDown() && event.getAction() == GLFW.GLFW_PRESS && (owEntity.isTame() || owEntity instanceof Submarine)) {
                OWNetworkHandler.sendToServer(new OpenOWInventoryPacket());
            }
        }
        if (minecraft.player != null && OWKeysBinding.OW_ENTITY_JOURNAL.isDown()) {
            //minecraft.setScreen(new OWEntityJournalScreen());
            minecraft.setScreen(new AdventurerManuscriptScreen());
            isNotifiedOWBook = false;
            minecraft.player.playSound(SoundEvents.BOOK_PAGE_TURN);
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
        if (DailyQuestsDate.isAlreadyChanged) {
            new Timer().schedule(new TimerTask() {
                @Override
                public void run() {
                    ClientLevel clientLevel = Minecraft.getInstance().level;
                    if (clientLevel != null) {
                        int count = 0;
                        for (Entity entity : clientLevel.entitiesForRendering()) {
                            if (entity instanceof OWEntity owEntity) {
                                owEntity.setUpdatingQuests(true);
                                owEntity.quest0Progression = 0;
                                owEntity.quest1Progression = 0;
                                owEntity.quest2Progression = 0;
                                owEntity.quest3Progression = 0;
                                owEntity.quest4Progression = 0;
                                owEntity.quest5Progression = 0;
                                owEntity.quest6Progression = 0;
                                owEntity.quest7Progression = 0;
                                owEntity.quest8Progression = 0;
                                owEntity.quest9Progression = 0;
                                owEntity.quest10Progression = 0;

                                owEntity.quest0isLocked = false;
                                owEntity.quest1isLocked = false;
                                owEntity.quest2isLocked = false;
                                owEntity.quest3isLocked = false;
                                owEntity.quest4isLocked = false;
                                owEntity.quest5isLocked = false;
                                owEntity.quest6isLocked = false;
                                owEntity.quest7isLocked = false;
                                owEntity.quest8isLocked = false;
                                owEntity.quest9isLocked = false;
                                owEntity.quest10isLocked = false;
                                count++;
                            }
                        }
                    }
                }
            }, 2000);
        }
        String worldName = getWorldName(event.getPlayer());
        ClientKillData.createEmptyFile(worldName);
    }

    public static String getWorldName(Player player) {
        if (player != null && player.level() != null) {
            Minecraft minecraft = Minecraft.getInstance();

            if (minecraft.hasSingleplayerServer()) {
                MinecraftServer server = minecraft.getSingleplayerServer();

                if (server != null) {
                    File worldDir = server.getWorldPath(LevelResource.ROOT).toAbsolutePath().toFile();
                    String pathStr = worldDir.getAbsolutePath();
                    String[] parts = pathStr.split("\\\\");
                    return parts[parts.length - 2];
                }
            }

            if (minecraft.getCurrentServer() != null) {
                return "multiplayer:" + minecraft.getCurrentServer().name;
            }
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
    public static void onClientTick(PlayerTickEvent.Post event) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level != null && minecraft.level.isClientSide()) {
            if (minecraft.player != null && minecraft.player.getVehicle() instanceof OWEntity owEntity) {
                boolean isSprintKeyDown = minecraft.options.keySprint.isDown();
                OWNetworkHandler.sendToServer(new OWRunningPacket(isSprintKeyDown));

            }
        }
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

        // Trainée dorée — SKIN_GOLD en mouvement
        if (player.level().isClientSide()) {
            // Tiger SKIN_GOLD
            player.level().getEntitiesOfClass(TigerEntity.class,
                    player.getBoundingBox().inflate(32),
                    e -> e.isTame() && e.getVariant() == TigerVariant.Cosmetics.GOLD.variant && !e.isDeadOrDying()
            ).forEach(entity -> spawnGoldTrailParticles(player, entity, entity.getYRot()));

            // Kodiak SKIN_GOLD
            player.level().getEntitiesOfClass(KodiakEntity.class,
                    player.getBoundingBox().inflate(32),
                    e -> e.isTame() && e.getVariant() == KodiakVariant.Cosmetics.GOLD.variant && !e.isDeadOrDying()
            ).forEach(entity -> spawnGoldTrailParticles(player, entity, entity.getYRot()));

            // Crocodile SKIN_GOLD
            player.level().getEntitiesOfClass(CrocodileEntity.class,
                    player.getBoundingBox().inflate(32),
                    e -> e.isTame() && e.getVariant() == CrocodileVariant.Cosmetics.GOLD.variant && !e.isDeadOrDying()
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

        boolean isGrabBySomething = isGrabByCrocodile || isGrabByTiger;

        if (player != null) {
            PlantEmpressBossBar.render(event.getGuiGraphics(),
                    event.getGuiGraphics().guiWidth(),
                    event.getGuiGraphics().guiHeight());

            if (isGrabBySomething) {
                RightClickAlertOverlay.render(event.getGuiGraphics(), event.getGuiGraphics().guiWidth(), event.getGuiGraphics().guiHeight());
            }

            if (player.getVehicle() instanceof OWEntity && !(player.getVehicle() instanceof Submarine)) {
                if (player.getVehicle() instanceof TigerEntity tiger && tiger.getGrabbedTarget() == player) return;
                if (player.getVehicle() instanceof CrocodileEntity crocodile && crocodile.getGrabbedTarget() == player) return;
                OWEntityHud.render(event.getGuiGraphics(), event.getGuiGraphics().guiWidth(), event.getGuiGraphics().guiHeight());
                OWAttacksOverlay.render(event.getGuiGraphics(), event.getGuiGraphics().guiWidth(), event.getGuiGraphics().guiHeight());
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

    private static void applyMinecraftBlurShader(Player player) {
        if (player != null && player.hasEffect(OWEffects.VENOM_EFFECT.getDelegate())) {
            int duration = player.getEffect(OWEffects.VENOM_EFFECT.getDelegate()).getDuration();
            if (maxEffectDuration < duration) maxEffectDuration = duration;
            double blurPercentage = 1 - ((double) duration / maxEffectDuration);
            setBlurPercentage(blurPercentage <= 0.9999 ? blurPercentage : 0.0);
        }

        Minecraft mc = Minecraft.getInstance();

        if (getBlurPercentage() >= 90) mc.gameRenderer.loadEffect(ResourceLocation.parse("ow:shaders/blur_shader/blur10.json"));
        else if (getBlurPercentage() >= 80) mc.gameRenderer.loadEffect(ResourceLocation.parse("ow:shaders/blur_shader/blur9.json"));
        else if (getBlurPercentage() >= 70) mc.gameRenderer.loadEffect(ResourceLocation.parse("ow:shaders/blur_shader/blur8.json"));
        else if (getBlurPercentage() >= 60) mc.gameRenderer.loadEffect(ResourceLocation.parse("ow:shaders/blur_shader/blur7.json"));
        else if (getBlurPercentage() >= 50) mc.gameRenderer.loadEffect(ResourceLocation.parse("ow:shaders/blur_shader/blur6.json"));
        else if (getBlurPercentage() >= 40) mc.gameRenderer.loadEffect(ResourceLocation.parse("ow:shaders/blur_shader/blur5.json"));
        else if (getBlurPercentage() >= 30) mc.gameRenderer.loadEffect(ResourceLocation.parse("ow:shaders/blur_shader/blur4.json"));
        else if (getBlurPercentage() >= 20) mc.gameRenderer.loadEffect(ResourceLocation.parse("ow:shaders/blur_shader/blur3.json"));
        else if (getBlurPercentage() >= 10) mc.gameRenderer.loadEffect(ResourceLocation.parse("ow:shaders/blur_shader/blur2.json"));
        else if (getBlurPercentage() >= 0) mc.gameRenderer.loadEffect(ResourceLocation.parse("ow:shaders/blur_shader/blur1.json"));

    }

    private static void removeMinecraftBlurShader() {
        Minecraft mc = Minecraft.getInstance();
        mc.gameRenderer.shutdownEffect();
        setBlurPercentage(0);
    }

    // --- Blink shader ---
    public static boolean blinkSubmarineShader = false;  // set par SeaBugEntity
    private static int blinkTimer = 0;
    private static boolean blinkShaderOn = false;

    /**
     * À appeler chaque tick client. Fait clignoter le shader donné à des intervalles
     * aléatoires entre 0.2 s (4 ticks) et 1 s (20 ticks).
     * Arrêter avec {@link #stopBlinkShader()}.
     *
     * Exemple : ClientEvents.tickBlinkShader(ResourceLocation.parse("ow:shaders/my_shader.json"));
     */
    public static void tickBlinkShader(ResourceLocation shader) {
        if (blinkTimer <= 0) {
            blinkShaderOn = !blinkShaderOn;
            // Intervalle aléatoire : 4 à 20 ticks (0.2 s – 1 s)
            blinkTimer = 4 + (int) (Math.random() * 17);
            if (blinkShaderOn) {
                Minecraft.getInstance().gameRenderer.loadEffect(shader);
            } else {
                Minecraft.getInstance().gameRenderer.shutdownEffect();
            }
        }
        blinkTimer--;
    }

    /** Arrête le clignotement et coupe le shader. */
    public static void stopBlinkShader() {
        blinkTimer = 0;
        blinkShaderOn = false;
        Minecraft.getInstance().gameRenderer.shutdownEffect();
    }
    // --------------------

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

    public static boolean isInSubmarine(LivingEntity entity) {
        return entity.getRootVehicle() instanceof Submarine;
    }

    private static float savedXRot = 0f;
    private static float savedXRotO = 0f;

    private record SavedRots(float xRot, float xRotO, float yBody, float yBodyO, float yHead, float yHeadO) {}
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
            player.yBodyRot  = ((KodiakEntity) owVehicle).yBodyRot;
            player.yBodyRotO = ((KodiakEntity) owVehicle).yBodyRot;
        } else if (isOrcaPassenger) {
            player.yBodyRot  = ((OrcaEntity) owVehicle).yBodyRot;
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
                event.setRoll((float) (event.getRoll() + (tiger.getBodyZRot() / 4) * intensity));
                event.setPitch((float) (event.getPitch() + (tiger.getBodyXRot() / 4) * intensity));
            } else if (rootVehicle instanceof CrocodileEntity crocodile) {
                if (crocodile.isDeathRolling()) return;
                event.setRoll((float) (event.getRoll() + (crocodile.getBodyZRot() / 4) * intensity));
                event.setPitch((float) (event.getPitch() + (crocodile.getBodyXRot() / 4) * intensity));
            } else if (rootVehicle instanceof OrcaEntity orca) {
                event.setRoll((float) (event.getRoll() + (orca.getBodyZRot() / 6) * intensity));
                event.setPitch((float) (event.getPitch() + (orca.getBodyXRot() / 6) * intensity));
            }
        }
    }

    /*@SubscribeEvent
    public static void renderCustomHearts(RenderLevelStageEvent event) {
        if (event.getStage() == RenderLevelStageEvent.Stage.AFTER_LEVEL && Minecraft.getInstance().screen == null
                && !Minecraft.getInstance().options.hideGui && !Minecraft.getInstance().getDebugOverlay().showDebugScreen()) {
            Minecraft minecraft = Minecraft.getInstance();
            Player player = minecraft.player;

            if (player != null) {
                Entity vehicle = player.getVehicle();
                boolean screenOpen = minecraft.screen != null;
                boolean hasVenom = player.hasEffect(OWEffects.VENOM_EFFECT.getDelegate()) || (vehicle != null && vehicle instanceof LivingEntity livingEntity && livingEntity.hasEffect(OWEffects.VENOM_EFFECT.getDelegate()));
                int waterDepth = player.isInWater() ? (int) (player.level().getSeaLevel() - player.getY()) : -1;
                float waterPressure = getWaterPressure(waterDepth);
                boolean shouldApplyBlur = hasVenom && !screenOpen;

                if (shouldApplyBlur) {
                    applyMinecraftBlurShader(player);
                } else {
                    removeMinecraftBlurShader();
                }

                if (waterPressure >= 4 && !player.isCreative() && player.isAlive() && minecraft.screen == null && !minecraft.isPaused() && !isInSubmarine(player)) {
                    if (waterPressure >= 60)
                        Minecraft.getInstance().gameRenderer.loadEffect(ResourceLocation.parse("ow:shaders/blur_shader/blur10.json"));
                    else if (waterPressure >= 54)
                        Minecraft.getInstance().gameRenderer.loadEffect(ResourceLocation.parse("ow:shaders/blur_shader/blur9.json"));
                    else if (waterPressure >= 48)
                        Minecraft.getInstance().gameRenderer.loadEffect(ResourceLocation.parse("ow:shaders/blur_shader/blur8.json"));
                    else if (waterPressure >= 42)
                        Minecraft.getInstance().gameRenderer.loadEffect(ResourceLocation.parse("ow:shaders/blur_shader/blur7.json"));
                    else if (waterPressure >= 36)
                        Minecraft.getInstance().gameRenderer.loadEffect(ResourceLocation.parse("ow:shaders/blur_shader/blur6.json"));
                    else if (waterPressure >= 30)
                        Minecraft.getInstance().gameRenderer.loadEffect(ResourceLocation.parse("ow:shaders/blur_shader/blur5.json"));
                    else if (waterPressure >= 24)
                        Minecraft.getInstance().gameRenderer.loadEffect(ResourceLocation.parse("ow:shaders/blur_shader/blur4.json"));
                    else if (waterPressure >= 18)
                        Minecraft.getInstance().gameRenderer.loadEffect(ResourceLocation.parse("ow:shaders/blur_shader/blur3.json"));
                    else if (waterPressure >= 12)
                        Minecraft.getInstance().gameRenderer.loadEffect(ResourceLocation.parse("ow:shaders/blur_shader/blur2.json"));
                    else if (waterPressure >= 6)
                        Minecraft.getInstance().gameRenderer.loadEffect(ResourceLocation.parse("ow:shaders/blur_shader/blur1.json"));


            }
        }
    }*/

    // IDs des joueurs dont le render a été annulé par Shadow Strike (pour éviter un popPose orphelin)
    private static final Set<Integer> shadowStrikeHiddenRiders = new HashSet<>();

    private static boolean hasProcessedThisFrame = false;
    private static int shaderLoadCooldown = 0;

    @SubscribeEvent
    public static void onRenderLevelStage(RenderLevelStageEvent event) {
        if (event.getStage() == RenderLevelStageEvent.Stage.AFTER_SKY) {
            hasProcessedThisFrame = false;
        }

        if (event.getStage() == RenderLevelStageEvent.Stage.AFTER_PARTICLES) {
            renderPassiveEsp(event);
        }

        if (event.getStage() == RenderLevelStageEvent.Stage.AFTER_LEVEL) {
            Minecraft minecraft = Minecraft.getInstance();
            Player player = minecraft.player;

            if (player != null) {
                PostChain currentEffect = minecraft.gameRenderer.currentEffect();
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
    }

    private static void updateCrocTargeting(
            net.tiew.operationWild.entity.animals.aquatic.CrocodileEntity croc,
            Player player, net.minecraft.client.multiplayer.ClientLevel level) {

        Vec3 crocPos  = croc.position();
        Vec3 lookVec  = player.getLookAngle();
        double radius = 10.0;

        AABB box = new AABB(crocPos.x - radius, crocPos.y - radius, crocPos.z - radius,
                            crocPos.x + radius, crocPos.y + radius, crocPos.z + radius);

        LivingEntity best  = null;
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
            if (dot > bestDot) { bestDot = dot; best = candidate; }
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

    // ── Passive ESP rendering ─────────────────────────────────────────────────

    private static void renderPassiveEsp(RenderLevelStageEvent event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) return;
        if (!(mc.player.getRootVehicle() instanceof OWEntity owEntity)) return;

        OWPassive passive = OWAttacksHandler.getPassive(owEntity.getClass());
        if (passive == null) return;

        java.util.Set<Integer> ids = passive.getHighlightEntityIds(owEntity, mc.level);
        if (ids.isEmpty()) return;

        // Vecteurs caméra (right / up) pour les billboards toujours face au joueur
        org.joml.Quaternionf camRot = event.getCamera().rotation();
        org.joml.Vector3f jRight = new org.joml.Vector3f(1f, 0f, 0f);
        org.joml.Vector3f jUp    = new org.joml.Vector3f(0f, 1f, 0f);
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
            if (owEntity instanceof CrocodileEntity) {
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

    private static void addEspGlowGradient(BufferBuilder buf, Matrix4f matrix,
                                           Vec3 center, Vector3f right, Vector3f up) {
        // Couches : de l'extérieur (rouge) vers l'intérieur (jaune)
        float[][] layers = {
                // { size,  R,    G,    B,    A   }
                { 0.30f, 1.0f, 0.0f, 0.0f, 0.20f }, // Rouge     — couche externe
                { 0.20f, 1.0f, 0.35f,0.0f, 0.25f }, // Orange
                { 0.12f, 1.0f, 0.75f,0.0f, 0.30f }, // Jaune-orangé
                { 0.06f, 1.0f, 1.0f, 0.0f, 0.40f }, // Jaune pur — cœur
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

        // 4 coins du quad billboard (toujours face caméra)
        float[][] corners = {
                { cx + (-right.x - up.x) * size, cy + (-right.y - up.y) * size, cz + (-right.z - up.z) * size },
                { cx + ( right.x - up.x) * size, cy + ( right.y - up.y) * size, cz + ( right.z - up.z) * size },
                { cx + ( right.x + up.x) * size, cy + ( right.y + up.y) * size, cz + ( right.z + up.z) * size },
                { cx + (-right.x + up.x) * size, cy + (-right.y + up.y) * size, cz + (-right.z + up.z) * size },
        };

        for (float[] c : corners) {
            buf.addVertex(matrix, c[0], c[1], c[2]).setColor(r, g, b, a);
        }
    }

    private static void addEspGlow(BufferBuilder buf, Matrix4f matrix, Vec3 center,
                                    org.joml.Vector3f right, org.joml.Vector3f up, int rgb) {
        float br = ((rgb >> 16) & 0xFF) / 255f;
        float bg = ((rgb >> 8)  & 0xFF) / 255f;
        float bb = (rgb         & 0xFF) / 255f;
        long t = System.currentTimeMillis();
        float pulse = 0.70f + 0.30f * (float) Math.abs(Math.sin(t * Math.PI / 700.0));

        float[][] layers = {
            { 0.44f, 0.10f * pulse },
            { 0.30f, 0.20f * pulse },
            { 0.19f, 0.36f * pulse },
            { 0.11f, 0.58f * pulse },
            { 0.06f, 0.82f * pulse },
        };
        float[] intensities = { 0.45f, 0.80f, 1.00f, 1.00f, 1.00f };
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
        float ux = up.x * size,    uy = up.y * size,    uz = up.z * size;
        buf.addVertex(m, cx - rx - ux, cy - ry - uy, cz - rz - uz).setColor(r, g, b, a);
        buf.addVertex(m, cx + rx - ux, cy + ry - uy, cz + rz - uz).setColor(r, g, b, a);
        buf.addVertex(m, cx + rx + ux, cy + ry + uy, cz + rz + uz).setColor(r, g, b, a);
        buf.addVertex(m, cx - rx + ux, cy - ry + uy, cz - rz + uz).setColor(r, g, b, a);
    }
}
