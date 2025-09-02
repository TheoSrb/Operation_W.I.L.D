package net.tiew.operationWild.event;

import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Axis;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.ContainerScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.PostChain;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.tiew.operationWild.entity.animals.aquatic.TigerSharkEntity;
import net.tiew.operationWild.entity.animals.terrestrial.*;
import net.tiew.operationWild.networking.ClientKillData;
import net.tiew.operationWild.screen.player.AdventurerManuscriptScreen;
import org.joml.Quaternionf;
import org.lwjgl.glfw.GLFW;
import net.tiew.operationWild.OperationWild;
import net.tiew.operationWild.effect.OWEffects;
import net.tiew.operationWild.entity.OWEntity;
import net.tiew.operationWild.entity.misc.SeaBugEntity;
import net.tiew.operationWild.entity.misc.Submarine;
import net.tiew.operationWild.entity.quests.daily_quests.DailyQuestsDate;
import net.tiew.operationWild.gui.*;
import net.tiew.operationWild.item.OWItems;
import net.tiew.operationWild.item.custom.MayaBlowpipeItem;
import net.tiew.operationWild.networking.OWNetworkHandler;
import net.tiew.operationWild.networking.packets.to_server.*;
import net.tiew.operationWild.screen.player.OWEntityJournalScreen;
import net.tiew.operationWild.sound.OWSounds;
import net.tiew.operationWild.core.OWDamageSources;
import net.tiew.operationWild.core.OWKeysBinding;

import java.io.*;
import java.util.Properties;
import java.util.Timer;
import java.util.TimerTask;

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

    public static int rightClickNips = 0;
    private static long lastRightClick = 0;
    private static final long CLICK_COOLDOWN = 50;


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
            if (ridingEntity instanceof OWEntity entity &&
                    entity.isAlive() && entity.isTame() && entity.isSaddled()) {
                if (leftButtonIsPressed) {
                    OWNetworkHandler.sendToServer(new ClientPressedLeftClick());
                } else if (rightButtonIsPressed && canUseRightClick(Minecraft.getInstance())) {
                    OWNetworkHandler.sendToServer(new ClientPressedRightClick());
                }
            } else if (ridingEntity instanceof Submarine) {
                if (rightButtonIsPressed && canUseRightClick(Minecraft.getInstance())) {
                    OWNetworkHandler.sendToServer(new ClientPressedRightClick());
                }
            }


            if (rightButtonIsPressed) {
                long currentTime = System.currentTimeMillis();
                if (currentTime - lastRightClick < CLICK_COOLDOWN) return;
                lastRightClick = currentTime;
                BoaEntity boaEntity = player.getPassengers().stream()
                        .filter(passenger -> passenger instanceof BoaEntity)
                        .map(passenger -> (BoaEntity) passenger)
                        .findFirst()
                        .orElse(null);

                if (boaEntity != null) {
                    rightClickNips++;
                    int $$0 = (int) (boaEntity.getMaxHealth() / 1.25f);

                    if (rightClickNips >= $$0) {
                        rightClickNips = 0;
                        OWNetworkHandler.sendToServer(new StopNipsBoaPacket());
                    }
                }

                if (isPlayerBeingShaken(player)) {
                    rightClickNips++;
                    int $$0 = 20;

                    if (rightClickNips >= $$0) {
                        rightClickNips = 0;
                        OWNetworkHandler.sendToServer(new StopShakingSharkPacket());
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public static void onKeyInput(InputEvent.Key event) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player != null && minecraft.player.getRootVehicle() instanceof OWEntity owEntity) {
            int inventoryKey = minecraft.options.keyInventory.getKey().getValue();
            int runKey = minecraft.options.keySprint.getKey().getValue();

            if (event.getKey() == inventoryKey && event.getAction() == GLFW.GLFW_PRESS) {
                OWNetworkHandler.sendToServer(new OpenOWInventoryPacket());
            }

            if (owEntity instanceof TigerEntity entity) {
                if (OWKeysBinding.OW_ULTIMATE.isDown() && entity.ultimateCooldown <= 0) {
                    OWNetworkHandler.sendToServer(new SendUltimateCapacityPacket());
                }
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
    public static void onMouseInput(InputEvent.MouseButton.Pre event) {
        Minecraft minecraft = Minecraft.getInstance();
        boolean canUse = minecraft.player != null && (minecraft.player.getMainHandItem().isEmpty() ||
                !(minecraft.player.getMainHandItem().is(ItemTags.MEAT) ||
                        minecraft.player.getMainHandItem().getItem() instanceof EggItem ||
                        minecraft.player.getMainHandItem().getItem() instanceof SpawnEggItem ||
                        minecraft.player.getMainHandItem().getItem() instanceof ProjectileWeaponItem ||
                        minecraft.player.getMainHandItem().getItem() instanceof TridentItem ||
                        minecraft.player.getMainHandItem().getUseAnimation() != UseAnim.NONE));
        if (minecraft.player != null && minecraft.player.getRootVehicle() instanceof OWEntity entity) {
            if (event.getButton() == 1 && event.getAction() == GLFW.GLFW_PRESS && canUse) {

                if (entity instanceof BoaEntity boa) {
                    if (!boa.canVenom && boa.venomCooldown == 801) OWNetworkHandler.sendToServer(new BoaVenomPacket());
                }
            }
        }
    }

    public static boolean hasPlayerKilledOWEntity(Player player, String entityNameId) {
        File file = new File("saves/" + getWorldName(player) + "/owDatas.properties");
        if (file.exists()) {
            try (BufferedReader buffer = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF-8"))) {
                String line = "";
                while (line != null) {
                    if (line.contains(entityNameId + "=")) return true;
                    line = buffer.readLine();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    public static boolean hasPlayerTamedOWEntity(Player player, String entityNameId) {
        File file = new File("saves/" + getWorldName(player) + "/owDatas.properties");
        if (file.exists()) {
            try (BufferedReader buffer = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF-8"))) {
                String line = "";
                while (line != null) {
                    if (line.contains(entityNameId + "_tamed" + "=")) return true;
                    line = buffer.readLine();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    @SubscribeEvent
    public static void onPlayerJoinWorld(ClientPlayerNetworkEvent.LoggingIn event) {
        loadTamingExperience(event.getPlayer());

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

    private static void loadTamingExperience(Player player) {
        try {
            String worldName = getWorldName(player);
            String filePath = "saves/" + worldName + "/owDatas.properties";

            File file = new File(filePath);
            if (file.exists()) {
                Properties props = new Properties();
                try (FileInputStream fis = new FileInputStream(filePath)) {
                    props.load(fis);
                    String value = props.getProperty("tamingExperience", "0.0");
                    tamingExperience = Float.parseFloat(value);
                }
            } else {
                tamingExperience = 0.0f;
            }
        } catch (IOException | NumberFormatException e) {
            tamingExperience = 0.0f;
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
            if (minecraft.player != null && minecraft.player.getVehicle() instanceof OWEntity) {
                OWNetworkHandler.sendToServer(new OWRunningPacket());
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();
        boolean useKeyIsPressed = Minecraft.getInstance().options.keyUse.isDown();
        boolean forwardKeyIsPressed = Minecraft.getInstance().options.keyUp.isDown();
        boolean rightKeyIsPressed = Minecraft.getInstance().options.keyRight.isDown();
        boolean leftKeyIsPressed = Minecraft.getInstance().options.keyLeft.isDown();
        boolean backKeyIsPressed = Minecraft.getInstance().options.keyDown.isDown();
        boolean upKeyIsPressed = Minecraft.getInstance().options.keyJump.isDown();

        boolean someKeyIsPressed = forwardKeyIsPressed || backKeyIsPressed || leftKeyIsPressed || rightKeyIsPressed || upKeyIsPressed;

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
            if (player == Minecraft.getInstance().player) {
                Component message = Component.translatable("tooltip.level_points", owEntity.getLevelPoints())
                        .withStyle(Style.EMPTY.withColor(0xb8e45a).withBold(true));

                Minecraft.getInstance().gui.setOverlayMessage(message, true);
            }
        }

        Entity ridingEntity = player.getRootVehicle();

        if (ridingEntity instanceof Submarine submarine && !submarine.isOff()) {
            double baseSpeed = 0.4;
            Vec3 newMovement = Vec3.ZERO;

            if (forwardKeyIsPressed) {
                submarine.backwardAccelerationLevel = 0;

                if (submarine.accelerationLevel < 100) {
                    submarine.accelerationLevel += 1.0f;
                }

                float normalizedAccel = submarine.accelerationLevel / 100.0f;
                float smoothAccel = 1.0f - (1.0f - normalizedAccel) * (1.0f - normalizedAccel);
                double currentSpeed = 0.1 + (smoothAccel * 0.9);

                Vec3 lookDirection = submarine.getViewVector(1.0f);
                newMovement = new Vec3(
                        lookDirection.x * (baseSpeed * currentSpeed),
                        lookDirection.y * (baseSpeed * currentSpeed),
                        lookDirection.z * (baseSpeed * currentSpeed)
                );
            }
            else if (backKeyIsPressed) {
                submarine.accelerationLevel = 0;

                if (submarine.backwardAccelerationLevel < 100) {
                    submarine.backwardAccelerationLevel += 1.0f;
                }

                float normalizedAccel = submarine.backwardAccelerationLevel / 100.0f;
                float smoothAccel = 1.0f - (1.0f - normalizedAccel) * (1.0f - normalizedAccel);
                double currentSpeed = 0.1 + (smoothAccel * 0.9);

                Vec3 lookDirection = submarine.getViewVector(1.0f);
                newMovement = new Vec3(
                        -lookDirection.x * (baseSpeed * currentSpeed * 0.5),
                        -lookDirection.y * (baseSpeed * currentSpeed * 0.5),
                        -lookDirection.z * (baseSpeed * currentSpeed * 0.5)
                );
            }
            else {
                if (submarine.accelerationLevel > 0) {
                    submarine.accelerationLevel -= 1.0f;
                    submarine.accelerationLevel = Math.max(0, submarine.accelerationLevel);

                    if (submarine.accelerationLevel > 0) {
                        float normalizedAccel = submarine.accelerationLevel / 100.0f;
                        float smoothDecel = normalizedAccel * normalizedAccel;
                        double currentSpeed = 0.1 + (smoothDecel * 0.9);

                        Vec3 lookDirection = submarine.getViewVector(1.0f);
                        newMovement = new Vec3(
                                lookDirection.x * (baseSpeed * currentSpeed * 0.7),
                                lookDirection.y * (baseSpeed * currentSpeed * 0.7),
                                lookDirection.z * (baseSpeed * currentSpeed * 0.7)
                        );
                    }
                }

                if (submarine.backwardAccelerationLevel > 0) {
                    submarine.backwardAccelerationLevel -= 1.0f;
                    submarine.backwardAccelerationLevel = Math.max(0, submarine.backwardAccelerationLevel);

                    if (submarine.backwardAccelerationLevel > 0) {
                        float normalizedAccel = submarine.backwardAccelerationLevel / 100.0f;
                        float smoothDecel = normalizedAccel * normalizedAccel;
                        double currentSpeed = 0.1 + (smoothDecel * 0.9);

                        Vec3 lookDirection = submarine.getViewVector(1.0f);
                        Vec3 backwardMovement = new Vec3(
                                -lookDirection.x * (baseSpeed * currentSpeed * 0.5 * 0.7),
                                -lookDirection.y * (baseSpeed * currentSpeed * 0.5 * 0.7),
                                -lookDirection.z * (baseSpeed * currentSpeed * 0.5 * 0.7)
                        );
                        newMovement = newMovement.add(backwardMovement);
                    }
                }
            }

            if (rightKeyIsPressed) {
                submarine.leftAccelerationLevel = 0;

                if (submarine.rightAccelerationLevel < 100) {
                    submarine.rightAccelerationLevel += 1.0f;
                }

                float normalizedAccel = submarine.rightAccelerationLevel / 100.0f;
                float smoothAccel = 1.0f - (1.0f - normalizedAccel) * (1.0f - normalizedAccel);
                double currentSpeed = 0.1 + (smoothAccel * 0.9);

                float yaw = submarine.getYRot();
                float angle = yaw + 90.0F;
                Vec3 rightDirection = new Vec3(-Mth.sin(angle * (float) Math.PI / 180.0F), 0, Mth.cos(angle * (float) Math.PI / 180.0F));

                Vec3 rightMovement = new Vec3(
                        rightDirection.x * (0.5 * baseSpeed * currentSpeed),
                        rightDirection.y * (0.5 * baseSpeed * currentSpeed),
                        rightDirection.z * (0.5 * baseSpeed * currentSpeed)
                );
                newMovement = newMovement.add(rightMovement);
            } else {
                if (submarine.rightAccelerationLevel > 0) {
                    submarine.rightAccelerationLevel -= 1.0f;
                    submarine.rightAccelerationLevel = Math.max(0, submarine.rightAccelerationLevel);

                    if (submarine.rightAccelerationLevel > 0) {
                        float normalizedAccel = submarine.rightAccelerationLevel / 100.0f;
                        float smoothDecel = normalizedAccel * normalizedAccel;
                        double currentSpeed = 0.1 + (smoothDecel * 0.9);

                        float yaw = submarine.getYRot();
                        float angle = yaw + 90.0F;
                        Vec3 rightDirection = new Vec3(-Mth.sin(angle * (float) Math.PI / 180.0F), 0, Mth.cos(angle * (float) Math.PI / 180.0F));

                        Vec3 rightMovement = new Vec3(
                                rightDirection.x * (0.5 * baseSpeed * currentSpeed * 0.7),
                                rightDirection.y * (0.5 * baseSpeed * currentSpeed * 0.7),
                                rightDirection.z * (0.5 * baseSpeed * currentSpeed * 0.7)
                        );
                        newMovement = newMovement.add(rightMovement);
                    }
                }
            }

            if (leftKeyIsPressed) {
                submarine.rightAccelerationLevel = 0;

                if (submarine.leftAccelerationLevel < 100) {
                    submarine.leftAccelerationLevel += 1.0f;
                }

                float normalizedAccel = submarine.leftAccelerationLevel / 100.0f;
                float smoothAccel = 1.0f - (1.0f - normalizedAccel) * (1.0f - normalizedAccel);
                double currentSpeed = 0.1 + (smoothAccel * 0.9);

                float yaw = submarine.getYRot();
                float angle = yaw - 90.0F;
                Vec3 leftDirection = new Vec3(-Mth.sin(angle * (float) Math.PI / 180.0F), 0, Mth.cos(angle * (float) Math.PI / 180.0F));

                Vec3 leftMovement = new Vec3(
                        leftDirection.x * (0.5 * baseSpeed * currentSpeed),
                        leftDirection.y * (0.5 * baseSpeed * currentSpeed),
                        leftDirection.z * (0.5 * baseSpeed * currentSpeed)
                );
                newMovement = newMovement.add(leftMovement);
            } else {
                if (submarine.leftAccelerationLevel > 0) {
                    submarine.leftAccelerationLevel -= 1.0f;
                    submarine.leftAccelerationLevel = Math.max(0, submarine.leftAccelerationLevel);

                    if (submarine.leftAccelerationLevel > 0) {
                        float normalizedAccel = submarine.leftAccelerationLevel / 100.0f;
                        float smoothDecel = normalizedAccel * normalizedAccel;
                        double currentSpeed = 0.1 + (smoothDecel * 0.9);

                        float yaw = submarine.getYRot();
                        float angle = yaw - 90.0F;
                        Vec3 leftDirection = new Vec3(-Mth.sin(angle * (float) Math.PI / 180.0F), 0, Mth.cos(angle * (float) Math.PI / 180.0F));

                        Vec3 leftMovement = new Vec3(
                                leftDirection.x * (0.5 * baseSpeed * currentSpeed * 0.7),
                                leftDirection.y * (0.5 * baseSpeed * currentSpeed * 0.7),
                                leftDirection.z * (0.5 * baseSpeed * currentSpeed * 0.7)
                        );
                        newMovement = newMovement.add(leftMovement);
                    }
                }
            }

            if (upKeyIsPressed) {
                if (submarine.upAccelerationLevel < 100) {
                    submarine.upAccelerationLevel += 1f;
                }

                float normalizedAccel = submarine.upAccelerationLevel / 150.0f;
                float smoothAccel = 1.0f - (1.0f - normalizedAccel) * (1.0f - normalizedAccel);
                double currentSpeed = 0.1 + (smoothAccel * 0.9);

                Vec3 upMovement = new Vec3(0, 0.2 * currentSpeed, 0);
                newMovement = newMovement.add(upMovement);
            } else {
                if (submarine.upAccelerationLevel > 0) {
                    submarine.upAccelerationLevel -= 1.0f;
                    submarine.upAccelerationLevel = Math.max(0, submarine.upAccelerationLevel);

                    if (submarine.upAccelerationLevel > 0) {
                        float normalizedAccel = submarine.upAccelerationLevel / 100.0f;
                        float smoothDecel = normalizedAccel * normalizedAccel;
                        double currentSpeed = 0.1 + (smoothDecel * 0.9);

                        Vec3 upMovement = new Vec3(0, 0.125 * currentSpeed * 0.7, 0);
                        newMovement = newMovement.add(upMovement);
                    }
                }
            }

            if (submarine.isAlive() && submarine.isInWater()) submarine.setDeltaMovement(newMovement);

            if (someKeyIsPressed && submarine.isInWater() && player == Minecraft.getInstance().player) {
                submarine.spawnBubbleParticles();

                if (submarine.level().isClientSide) {
                    float pitch = 1.25f;
                    int maxSoundTime = (int) (280 * pitch);
                    submarine.soundTimer++;

                    if (!submarine.isPlayingMoveSound || submarine.soundTimer >= maxSoundTime) {
                        SimpleSoundInstance soundInstance = new SimpleSoundInstance(
                                OWSounds.SUBMARINE_MOVE_LOOP.get().getLocation(),
                                SoundSource.BLOCKS,
                                1.0f,
                                pitch,
                                SoundInstance.createUnseededRandom(),
                                false,
                                0,
                                SoundInstance.Attenuation.NONE,
                                player.getX(),
                                player.getY(),
                                player.getZ(),
                                true
                        );

                        Minecraft.getInstance().getSoundManager().play(soundInstance);
                        submarine.isPlayingMoveSound = true;
                        submarine.soundTimer = 0;
                    }
                }
            } else {
                if (submarine.level().isClientSide) {
                    Minecraft.getInstance().getSoundManager().stop(OWSounds.SUBMARINE_MOVE_LOOP.get().getLocation(), null);
                }
                submarine.isPlayingMoveSound = false;
                submarine.soundTimer = 0;
            }
        }

        if (ridingEntity instanceof TigerSharkEntity tigerShark && tigerShark.isSaddled()) {
            double baseSpeed = 0.35 * (tigerShark.getSpeed() * 5.2);
            Vec3 newMovement = Vec3.ZERO;

            if (forwardKeyIsPressed) {
                tigerShark.backwardAccelerationLevel = 0;

                if (tigerShark.accelerationLevel < 100) {
                    tigerShark.accelerationLevel += 1.0f;
                }

                float normalizedAccel = tigerShark.accelerationLevel / 100.0f;
                float smoothAccel = 1.0f - (1.0f - normalizedAccel) * (1.0f - normalizedAccel);
                double currentSpeed = 0.1 + (smoothAccel * 0.9);

                Vec3 lookDirection = tigerShark.getViewVector(1.0f);
                newMovement = new Vec3(
                        lookDirection.x * (baseSpeed * currentSpeed),
                        lookDirection.y * (baseSpeed * currentSpeed),
                        lookDirection.z * (baseSpeed * currentSpeed)
                );
            }
            else if (backKeyIsPressed) {
                tigerShark.accelerationLevel = 0;

                if (tigerShark.backwardAccelerationLevel < 100) {
                    tigerShark.backwardAccelerationLevel += 1.0f;
                }

                float normalizedAccel = tigerShark.backwardAccelerationLevel / 100.0f;
                float smoothAccel = 1.0f - (1.0f - normalizedAccel) * (1.0f - normalizedAccel);
                double currentSpeed = 0.1 + (smoothAccel * 0.9);

                Vec3 lookDirection = tigerShark.getViewVector(1.0f);
                newMovement = new Vec3(
                        -lookDirection.x * (baseSpeed * currentSpeed * 0.5),
                        -lookDirection.y * (baseSpeed * currentSpeed * 0.5),
                        -lookDirection.z * (baseSpeed * currentSpeed * 0.5)
                );
            }
            else {
                if (tigerShark.accelerationLevel > 0) {
                    tigerShark.accelerationLevel -= 1.0f;
                    tigerShark.accelerationLevel = Math.max(0, tigerShark.accelerationLevel);

                    if (tigerShark.accelerationLevel > 0) {
                        float normalizedAccel = tigerShark.accelerationLevel / 100.0f;
                        float smoothDecel = normalizedAccel * normalizedAccel;
                        double currentSpeed = 0.1 + (smoothDecel * 0.9);

                        Vec3 lookDirection = tigerShark.getViewVector(1.0f);
                        newMovement = new Vec3(
                                lookDirection.x * (baseSpeed * currentSpeed * 0.7),
                                lookDirection.y * (baseSpeed * currentSpeed * 0.7),
                                lookDirection.z * (baseSpeed * currentSpeed * 0.7)
                        );
                    }
                }

                if (tigerShark.backwardAccelerationLevel > 0) {
                    tigerShark.backwardAccelerationLevel -= 1.0f;
                    tigerShark.backwardAccelerationLevel = Math.max(0, tigerShark.backwardAccelerationLevel);

                    if (tigerShark.backwardAccelerationLevel > 0) {
                        float normalizedAccel = tigerShark.backwardAccelerationLevel / 100.0f;
                        float smoothDecel = normalizedAccel * normalizedAccel;
                        double currentSpeed = 0.1 + (smoothDecel * 0.9);

                        Vec3 lookDirection = tigerShark.getViewVector(1.0f);
                        Vec3 backwardMovement = new Vec3(
                                -lookDirection.x * (baseSpeed * currentSpeed * 0.5 * 0.7),
                                -lookDirection.y * (baseSpeed * currentSpeed * 0.5 * 0.7),
                                -lookDirection.z * (baseSpeed * currentSpeed * 0.5 * 0.7)
                        );
                        newMovement = newMovement.add(backwardMovement);
                    }
                }
            }

            if (rightKeyIsPressed) {
                tigerShark.leftAccelerationLevel = 0;

                if (tigerShark.rightAccelerationLevel < 100) {
                    tigerShark.rightAccelerationLevel += 1.0f;
                }

                float normalizedAccel = tigerShark.rightAccelerationLevel / 100.0f;
                float smoothAccel = 1.0f - (1.0f - normalizedAccel) * (1.0f - normalizedAccel);
                double currentSpeed = 0.1 + (smoothAccel * 0.9);

                float yaw = tigerShark.getYRot();
                float angle = yaw + 90.0F;
                Vec3 rightDirection = new Vec3(-Mth.sin(angle * (float) Math.PI / 180.0F), 0, Mth.cos(angle * (float) Math.PI / 180.0F));

                Vec3 rightMovement = new Vec3(
                        rightDirection.x * (0.5 * baseSpeed * currentSpeed),
                        rightDirection.y * (0.5 * baseSpeed * currentSpeed),
                        rightDirection.z * (0.5 * baseSpeed * currentSpeed)
                );
                newMovement = newMovement.add(rightMovement);
            } else {
                if (tigerShark.rightAccelerationLevel > 0) {
                    tigerShark.rightAccelerationLevel -= 1.0f;
                    tigerShark.rightAccelerationLevel = Math.max(0, tigerShark.rightAccelerationLevel);

                    if (tigerShark.rightAccelerationLevel > 0) {
                        float normalizedAccel = tigerShark.rightAccelerationLevel / 100.0f;
                        float smoothDecel = normalizedAccel * normalizedAccel;
                        double currentSpeed = 0.1 + (smoothDecel * 0.9);

                        float yaw = tigerShark.getYRot();
                        float angle = yaw + 90.0F;
                        Vec3 rightDirection = new Vec3(-Mth.sin(angle * (float) Math.PI / 180.0F), 0, Mth.cos(angle * (float) Math.PI / 180.0F));

                        Vec3 rightMovement = new Vec3(
                                rightDirection.x * (0.5 * baseSpeed * currentSpeed * 0.7),
                                rightDirection.y * (0.5 * baseSpeed * currentSpeed * 0.7),
                                rightDirection.z * (0.5 * baseSpeed * currentSpeed * 0.7)
                        );
                        newMovement = newMovement.add(rightMovement);
                    }
                }
            }

            if (leftKeyIsPressed) {
                tigerShark.rightAccelerationLevel = 0;

                if (tigerShark.leftAccelerationLevel < 100) {
                    tigerShark.leftAccelerationLevel += 1.0f;
                }

                float normalizedAccel = tigerShark.leftAccelerationLevel / 100.0f;
                float smoothAccel = 1.0f - (1.0f - normalizedAccel) * (1.0f - normalizedAccel);
                double currentSpeed = 0.1 + (smoothAccel * 0.9);

                float yaw = tigerShark.getYRot();
                float angle = yaw - 90.0F;
                Vec3 leftDirection = new Vec3(-Mth.sin(angle * (float) Math.PI / 180.0F), 0, Mth.cos(angle * (float) Math.PI / 180.0F));

                Vec3 leftMovement = new Vec3(
                        leftDirection.x * (0.5 * baseSpeed * currentSpeed),
                        leftDirection.y * (0.5 * baseSpeed * currentSpeed),
                        leftDirection.z * (0.5 * baseSpeed * currentSpeed)
                );
                newMovement = newMovement.add(leftMovement);
            } else {
                if (tigerShark.leftAccelerationLevel > 0) {
                    tigerShark.leftAccelerationLevel -= 1.0f;
                    tigerShark.leftAccelerationLevel = Math.max(0, tigerShark.leftAccelerationLevel);

                    if (tigerShark.leftAccelerationLevel > 0) {
                        float normalizedAccel = tigerShark.leftAccelerationLevel / 100.0f;
                        float smoothDecel = normalizedAccel * normalizedAccel;
                        double currentSpeed = 0.1 + (smoothDecel * 0.9);

                        float yaw = tigerShark.getYRot();
                        float angle = yaw - 90.0F;
                        Vec3 leftDirection = new Vec3(-Mth.sin(angle * (float) Math.PI / 180.0F), 0, Mth.cos(angle * (float) Math.PI / 180.0F));

                        Vec3 leftMovement = new Vec3(
                                leftDirection.x * (0.5 * baseSpeed * currentSpeed * 0.7),
                                leftDirection.y * (0.5 * baseSpeed * currentSpeed * 0.7),
                                leftDirection.z * (0.5 * baseSpeed * currentSpeed * 0.7)
                        );
                        newMovement = newMovement.add(leftMovement);
                    }
                }
            }

            if (upKeyIsPressed) {
                if (tigerShark.upAccelerationLevel < 100) {
                    tigerShark.upAccelerationLevel += 1f;
                }

                float normalizedAccel = tigerShark.upAccelerationLevel / 150.0f;
                float smoothAccel = 1.0f - (1.0f - normalizedAccel) * (1.0f - normalizedAccel);
                double currentSpeed = 0.1 + (smoothAccel * 0.9);

                Vec3 upMovement = new Vec3(0, 0.2 * currentSpeed, 0);
                newMovement = newMovement.add(upMovement);
            } else {
                if (tigerShark.upAccelerationLevel > 0) {
                    tigerShark.upAccelerationLevel -= 1.0f;
                    tigerShark.upAccelerationLevel = Math.max(0, tigerShark.upAccelerationLevel);

                    if (tigerShark.upAccelerationLevel > 0) {
                        float normalizedAccel = tigerShark.upAccelerationLevel / 100.0f;
                        float smoothDecel = normalizedAccel * normalizedAccel;
                        double currentSpeed = 0.1 + (smoothDecel * 0.9);

                        Vec3 upMovement = new Vec3(0, 0.125 * currentSpeed * 0.7, 0);
                        newMovement = newMovement.add(upMovement);
                    }
                }
            }

            if (tigerShark.isAlive() && tigerShark.isInWater()) tigerShark.setDeltaMovement(newMovement);
        }

        if (ridingEntity instanceof TigerEntity tiger) {
            int maxTime = 200;
            if (forwardKeyIsPressed && tiger.isRunning() && !tiger.isInWater() && tiger.cooldownJump <= 0) {
                if (tiger.chargeTimer < maxTime) tiger.chargeTimer++;
            } else tiger.chargeTimer = 0;
            if (useKeyIsPressed && canUseRightClick(Minecraft.getInstance()) && tiger.chargeTimer >= maxTime && tiger.cooldownJump <= 0) {
                OWNetworkHandler.sendToServer(new TigerChargePacket());
                tiger.chargeTimer = 0;
            }
        }

        if (ridingEntity instanceof BoaEntity boa) {
            int soundTimeMax = 277;
            handleBoaSound(boa, forwardKeyIsPressed, player, soundTimeMax);

            if (forwardKeyIsPressed && boa.isInWater() && player == Minecraft.getInstance().player) {
                updateWaterMovement(boa, player, 15, 0.9);
            }
        } else if (ridingEntity instanceof TigerEntity tiger) {
            if (forwardKeyIsPressed && tiger.isInWater() && player == Minecraft.getInstance().player) {
                updateWaterMovement(tiger, player, 12, 0.9);
            }
        }
    }

    private static void handleBoaSound(BoaEntity boa, boolean isMoving, Player player, int soundTimeMax) {
        if (!isMoving) {
            stopBoaSound(boa);
            return;
        }

        if (boa.getSoundTime() < soundTimeMax) {
            boa.setSoundTime(boa.getSoundTime() + 1);
        }

        if (boa.getSoundTime() >= soundTimeMax && !boa.isInWater()) {
            boa.level().playSound(null, boa, OWSounds.BOA_SLITHER.get(), SoundSource.PLAYERS, 1.0F, 1.2f);
            boa.setSoundTime(0);
        }

        if (boa.isInWater()) {
            stopBoaSound(boa);
        }
    }

    private static void stopBoaSound(BoaEntity boa) {
        boa.setSoundTime(276);
        Minecraft.getInstance().getSoundManager().stop(OWSounds.BOA_SLITHER.get().getLocation(), SoundSource.PLAYERS);
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

    private static boolean isPlayerBeingShaken(Player player) {
        return player.level().getEntitiesOfClass(TigerSharkEntity.class,
                        player.getBoundingBox().inflate(5.0))
                .stream()
                .anyMatch(tigerShark -> tigerShark.isShakingPrey() && tigerShark.getTarget() == player);
    }

    @SubscribeEvent
    public static void onRenderStage(RenderGuiLayerEvent.Post event) {
        Minecraft minecraft = Minecraft.getInstance();
        Player player = minecraft.player;
        Entity targetedEntity = player.getVehicle();
        Entity vehicle = player.getVehicle();
        boolean screenOpen = minecraft.screen != null;
        boolean hasVenom = player.hasEffect(OWEffects.VENOM_EFFECT.getDelegate()) || (vehicle != null && vehicle instanceof LivingEntity livingEntity && livingEntity.hasEffect(OWEffects.VENOM_EFFECT.getDelegate()));
        boolean canShowAttacksInformation = OWKeysBinding.OW_ATTACKS_INFO.isDown();
        boolean questsAreUpdated = targetedEntity instanceof OWEntity owEntity && owEntity.questsAreUpdated();
        boolean renderTiger = targetedEntity instanceof TigerEntity;
        boolean renderSeabug = targetedEntity instanceof SeaBugEntity;
        boolean renderBoa = targetedEntity instanceof BoaEntity;
        boolean renderPeacock = targetedEntity instanceof PeacockEntity;
        boolean renderFoodOverlay = targetedEntity instanceof OWEntity ow && !ow.getItemFood().isEmpty();

        if (player != null) {
            PlantEmpressBossBar.render(event.getGuiGraphics(),
                    event.getGuiGraphics().guiWidth(),
                    event.getGuiGraphics().guiHeight());

            if (player.getPassengers().stream().anyMatch(passenger -> passenger instanceof BoaEntity) ||
                    player.level().getEntitiesOfClass(TigerSharkEntity.class, player.getBoundingBox().inflate(5.0))
                            .stream()
                            .anyMatch(tigerShark -> tigerShark.isShakingPrey() && tigerShark.getTarget() == player)) {
                RightClickAlertOverlay.render(event.getGuiGraphics(),
                        event.getGuiGraphics().guiWidth(), event.getGuiGraphics().guiHeight());
            }

            if (player.getVehicle() instanceof OWEntity && !(player.getVehicle() instanceof Submarine)) {
                OWEntityHud.render(event.getGuiGraphics(), event.getGuiGraphics().guiWidth(), event.getGuiGraphics().guiHeight());
                TigerOverlay.render(event.getGuiGraphics(), event.getGuiGraphics().guiWidth(), event.getGuiGraphics().guiHeight());
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

            if (renderBoa) {
                BoaOverlay.render(event.getGuiGraphics(),
                        event.getGuiGraphics().guiWidth(),
                        event.getGuiGraphics().guiHeight());
            }

            if (renderPeacock) {
                PeacockOverlay.render(event.getGuiGraphics(),
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
    public static void renderRedBorder(RenderGuiEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();

        if (mc.player != null) {
            OWEntity vehicle = (OWEntity) mc.player.getVehicle();

            if (vehicle != null) {
                if ((((float) (vehicle.getHealth() / vehicle.getMaxHealth())) <= 0.25f) || vehicle.canShowVitalEnergyLack) {
                    float opacityPercent = ((float) (vehicle.getHealth() / vehicle.getMaxHealth()) > 0.25 && vehicle.canShowVitalEnergyLack) ? (float) 1 : ((float) (-2.8 * (float) (vehicle.getHealth() / vehicle.getMaxHealth())) + 1) * 1.5f;

                    if (mc.screen == null && !mc.options.hideGui) {
                        GuiGraphics graphics = event.getGuiGraphics();
                        int screenWidth = mc.getWindow().getGuiScaledWidth();
                        int screenHeight = mc.getWindow().getGuiScaledHeight();

                        float centerX = screenWidth / 2.0f;
                        float centerY = screenHeight / 2.0f;
                        int steps = 40;

                        for (int step = 0; step < steps; step++) {
                            float progress = (float) step / steps;
                            float time = (System.currentTimeMillis() % 2000) / 2000.0f;
                            float oscillation = 0.4f + 0.1f * (float) Math.sin(time * 2 * Math.PI);
                            float size = progress * Math.min(screenWidth, screenHeight) * oscillation * ((1 + opacityPercent) / 2);
                            float alpha = ((1.0f - progress) * 0.075f) * opacityPercent;

                            int alphaInt = (int) (alpha * 255);
                            int color = (alphaInt << 24) | (((float) (vehicle.getHealth() / vehicle.getMaxHealth()) > 0.25 && vehicle.canShowVitalEnergyLack) ? 0x6442ac : 0xbc0c0c);

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
                }
            }
        }
    }

    public static boolean isInSubmarine(LivingEntity entity) {
        return entity.getRootVehicle() instanceof Submarine;
    }

    @SubscribeEvent
    public static void onPlayerRenderPre(RenderPlayerEvent.Pre event) {
        if (event.getEntity() == null || !(event.getEntity().getVehicle() instanceof OWEntity)) {
            return;
        }

        if (event.getEntity().getVehicle() instanceof SeaBugEntity seaBug) {

            PoseStack poseStack = event.getPoseStack();
            poseStack.pushPose();

            Vec3 pivotPoint = new Vec3(0, 1.3, 0);

            poseStack.mulPose(Axis.YP.rotationDegrees(-event.getEntity().getYRot()));

            Quaternionf rotation = Axis.XP.rotationDegrees(seaBug.getLastPlayerPitch());
            poseStack.rotateAround(rotation, (float) pivotPoint.x, (float) pivotPoint.y, (float) pivotPoint.z);

            poseStack.mulPose(Axis.YP.rotationDegrees(event.getEntity().getYRot()));
        } else if (event.getEntity().getVehicle() instanceof OWEntity owEntity) {

            PoseStack poseStack = event.getPoseStack();
            poseStack.pushPose();

            Vec3 pivotPoint = new Vec3(0, 0, 0);

            poseStack.mulPose(Axis.YP.rotationDegrees(-event.getEntity().getYRot()));

            Quaternionf rotationZ = Axis.ZP.rotationDegrees(-owEntity.getBodyZRot());
            Quaternionf rotationX = Axis.XP.rotationDegrees(-owEntity.getBodyXRot());
            poseStack.rotateAround(rotationZ, (float) pivotPoint.x, (float) pivotPoint.y, (float) pivotPoint.z);
            poseStack.rotateAround(rotationX, (float) pivotPoint.x, (float) pivotPoint.y, (float) pivotPoint.z);

            poseStack.mulPose(Axis.YP.rotationDegrees(event.getEntity().getYRot()));
        }
    }

    @SubscribeEvent
    public static void onPlayerRenderPost(RenderPlayerEvent.Post event) {
        if (event.getEntity() == null || !(event.getEntity().getVehicle() instanceof OWEntity)) {
            return;
        }

        PoseStack poseStack = event.getPoseStack();
        poseStack.popPose();
    }

    @SubscribeEvent
    public static void onCameraSetup(ViewportEvent.ComputeCameraAngles event) {
        Entity cameraEntity = event.getCamera().getEntity();
        if (cameraEntity != null) {
            Entity rootVehicle = cameraEntity.getRootVehicle();

            if (rootVehicle instanceof ElephantEntity elephant) {
                event.setRoll(event.getRoll() + (elephant.getBodyZRot() / 4));
                event.setPitch(event.getPitch() + (elephant.getBodyXRot() / 2));
            } else if (rootVehicle instanceof KodiakEntity kodiak) {
                event.setRoll(event.getRoll() + (kodiak.getBodyZRot() / (kodiak.isRunning() ? 1 : 2)));
                event.setPitch(event.getPitch() + (kodiak.getBodyXRot() / (kodiak.isRunning() ? 1 : 2)));
            } else if (rootVehicle instanceof TigerEntity tiger) {
                event.setRoll(event.getRoll() + (tiger.getBodyZRot()));
                event.setPitch(event.getPitch() + (tiger.getBodyXRot()));
            } else if (rootVehicle instanceof HyenaEntity hyena) {
                event.setRoll(event.getRoll() + (hyena.getBodyZRot() / 3));
                event.setPitch(event.getPitch() + (hyena.getBodyXRot() / 3));
            }
        }
    }

    @SubscribeEvent
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

                /*if (waterPressure >= 4 && !player.isCreative() && player.isAlive() && minecraft.screen == null && !minecraft.isPaused() && !isInSubmarine(player)) {
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
                        Minecraft.getInstance().gameRenderer.loadEffect(ResourceLocation.parse("ow:shaders/blur_shader/blur1.json"));*/


            }
        }
    }

    @SubscribeEvent
    public static void onRenderLevelStage(RenderLevelStageEvent event) {
        if (event.getStage() == RenderLevelStageEvent.Stage.AFTER_LEVEL && Minecraft.getInstance().screen == null
                && !Minecraft.getInstance().options.hideGui && !Minecraft.getInstance().getDebugOverlay().showDebugScreen()) {
            Minecraft minecraft = Minecraft.getInstance();
            Player player = minecraft.player;

            if (player != null) {
                boolean shouldHaveEffect = shouldActivateSubmarineEffect(player);
                PostChain currentEffect = minecraft.gameRenderer.currentEffect();

                boolean hasSubmarineShader = currentEffect != null && currentEffect.getName().equals("ow:shaders/post/submarine_light.json");

                if (shouldHaveEffect && !hasSubmarineShader) {
                    if (currentEffect != null) {
                        minecraft.gameRenderer.shutdownEffect();
                    }

                    try {
                        minecraft.gameRenderer.loadEffect(ResourceLocation.parse("ow:shaders/post/submarine_light.json"));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else if (!shouldHaveEffect && hasSubmarineShader) {
                    minecraft.gameRenderer.shutdownEffect();
                }

            }
        }
    }

    private static boolean shouldActivateSubmarineEffect(Player player) {
        return Minecraft.getInstance().options.getCameraType().isFirstPerson() && player.getVehicle() instanceof Submarine submarine && submarine.isLightOn() && submarine.isInWater() && player.isInWater() && !submarine.isOff();
    }
}
