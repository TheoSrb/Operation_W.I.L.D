package net.tiew.operationWild.client.platform;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.tiew.operationWild.entity.platform.OWPlatformSpace;
import net.minecraft.client.particle.TerrainParticle;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.tiew.operationWild.OperationWild;
import net.tiew.operationWild.entity.platform.OWPlatform;
import net.tiew.operationWild.entity.platform.OWPlatformRegistry;
import net.tiew.operationWild.entity.platform.OWPlatformTransform;
import net.tiew.operationWild.networking.packets.to_server.PlatformActionPacket;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.WeakHashMap;
import org.joml.Matrix4f;
import org.joml.Vector3f;

@EventBusSubscriber(modid = OperationWild.MOD_ID, bus = EventBusSubscriber.Bus.GAME, value = Dist.CLIENT)
public final class OWPlatformClient {

    private static final int BREAK_COOLDOWN_TICKS = 5;
    private static final boolean RIDER_CAN_BREAK = false;

    private record Target(OWPlatform platform, BlockPos cell, Direction face, Vec3 gridHit, Vec3 worldHit) {}

    private record VisualPose(Matrix4f pose, Matrix4f inverse, Vec3 camera, long time) {}

    private interface GridFrame {
        Vec3 toGrid(Vec3 world);

        Vec3 toWorld(Vec3 grid);
    }

    private static final long VISUAL_POSE_MAX_AGE_MS = 250L;
    private static final Map<OWPlatform, VisualPose> VISUAL_POSES = new WeakHashMap<>();

    @Nullable
    private static Target target;
    @Nullable
    private static OWPlatform breakingPlatform;
    @Nullable
    private static BlockPos breakingCell;
    private static float breakProgress;
    private static int breakTicks;
    private static int breakCooldown;

    private OWPlatformClient() {
    }

    public static void recordVisualPose(OWPlatform platform, Matrix4f pose) {
        Vec3 camera = Minecraft.getInstance().gameRenderer.getMainCamera().getPosition();
        Matrix4f copy = new Matrix4f(pose);
        VISUAL_POSES.put(platform, new VisualPose(copy, new Matrix4f(copy).invert(), camera, Util.getMillis()));
    }

    @Nullable
    private static VisualPose freshVisualPose(OWPlatform platform) {
        VisualPose visual = VISUAL_POSES.get(platform);
        if (visual == null || Util.getMillis() - visual.time() > VISUAL_POSE_MAX_AGE_MS) return null;
        return visual;
    }

    private static GridFrame frame(OWPlatform platform, float partialTick) {
        VisualPose visual = freshVisualPose(platform);
        if (visual != null) {
            return new GridFrame() {
                @Override
                public Vec3 toGrid(Vec3 world) {
                    Vector3f local = visual.inverse().transformPosition(
                            (float) (world.x - visual.camera().x), (float) (world.y - visual.camera().y), (float) (world.z - visual.camera().z), new Vector3f());
                    return new Vec3(local.x, local.y, local.z);
                }

                @Override
                public Vec3 toWorld(Vec3 grid) {
                    Vector3f relative = visual.pose().transformPosition((float) grid.x, (float) grid.y, (float) grid.z, new Vector3f());
                    return visual.camera().add(relative.x, relative.y, relative.z);
                }
            };
        }
        OWPlatformTransform transform = platform.interpolated(partialTick);
        return new GridFrame() {
            @Override
            public Vec3 toGrid(Vec3 world) {
                return transform.toGrid(world);
            }

            @Override
            public Vec3 toWorld(Vec3 grid) {
                return transform.toWorld(grid);
            }
        };
    }

    public static boolean hasTarget() {
        return target != null;
    }

    private static boolean ridesCarrier(Player player, OWPlatform platform) {
        return player.getRootVehicle() == platform.carrier().getRootVehicle() && player.isPassenger();
    }

    public static boolean capturesUse() {
        Target hit = target;
        Player player = Minecraft.getInstance().player;
        if (hit == null || player == null) return false;
        return holdsBlock(player) || !hit.platform().get(hit.cell()).isAir();
    }

    public static boolean capturesAttack() {
        Target hit = target;
        Player player = Minecraft.getInstance().player;
        if (hit == null || player == null) return false;
        return RIDER_CAN_BREAK || !ridesCarrier(player, hit.platform());
    }

    private static boolean holdsBlock(Player player) {
        return player.getMainHandItem().getItem() instanceof BlockItem || player.getOffhandItem().getItem() instanceof BlockItem;
    }

    public static void afterPick(float partialTick) {
        target = null;
        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        if (player == null || mc.level == null || player.isSpectator()) return;
        if (OWPlatformRegistry.isEmpty(mc.level)) return;

        Vec3 eye = player.getEyePosition(partialTick);
        Vec3 end = eye.add(player.getViewVector(partialTick).scale(player.blockInteractionRange()));
        AABB area = new AABB(eye, end).inflate(1.0);

        double best = Double.MAX_VALUE;
        Target found = null;
        for (OWPlatform platform : OWPlatformRegistry.near(mc.level, area)) {
            if (!platform.isCollidable()) continue;
            GridFrame frame = frame(platform, partialTick);
            BlockHitResult hit = platform.raycastGrid(frame.toGrid(eye), frame.toGrid(end));
            if (hit == null) continue;
            Vec3 world = frame.toWorld(hit.getLocation());
            double distance = world.distanceToSqr(eye);
            if (distance < best) {
                best = distance;
                found = new Target(platform, hit.getBlockPos(), hit.getDirection(), hit.getLocation(), world);
            }
        }
        if (found == null) return;

        HitResult vanilla = mc.hitResult;
        double vanillaDistance = vanilla == null || vanilla.getType() == HitResult.Type.MISS
                ? Double.MAX_VALUE : vanilla.getLocation().distanceToSqr(eye);
        boolean vanillaIsCarrier = vanilla instanceof EntityHitResult entityHit && entityHit.getEntity() == found.platform().carrier();
        boolean preferPlatform = vanillaIsCarrier && (holdsBlock(player) || found.platform().contains(found.cell()));
        if (best >= vanillaDistance && !preferPlatform) return;

        target = found;
        mc.hitResult = BlockHitResult.miss(found.worldHit(), found.face(), BlockPos.containing(found.worldHit()));
        mc.crosshairPickEntity = null;
    }

    @Nullable
    public static BlockPos crackCell(OWPlatform platform) {
        return platform == breakingPlatform ? breakingCell : null;
    }

    public static int crackStage(OWPlatform platform) {
        if (platform != breakingPlatform || breakingCell == null) return -1;
        return Math.min(9, Math.max(0, (int) (breakProgress * 10f)));
    }

    private static void send(Target hit, int action, boolean offHand) {
        PacketDistributor.sendToServer(new PlatformActionPacket(hit.platform().carrier().getId(), action,
                hit.cell().getX(), hit.cell().getY(), hit.cell().getZ(), hit.face().get3DDataValue(),
                (float) hit.gridHit().x, (float) hit.gridHit().y, (float) hit.gridHit().z, offHand));
    }

    @SubscribeEvent
    public static void onInteraction(InputEvent.InteractionKeyMappingTriggered event) {
        Target hit = target;
        if (hit == null) return;
        Player player = Minecraft.getInstance().player;
        if (player == null) return;

        if (event.isAttack()) {
            if (!capturesAttack()) return;
            event.setCanceled(true);
            event.setSwingHand(true);
            if (breakingPlatform == null && breakCooldown <= 0 && hit.platform().contains(hit.cell())) startBreaking(hit, player);
            return;
        }
        if (event.isPickBlock()) {
            event.setCanceled(true);
            event.setSwingHand(false);
            return;
        }
        if (!event.isUseItem()) return;

        boolean mainBlock = player.getMainHandItem().getItem() instanceof BlockItem;
        boolean offBlock = !mainBlock && player.getOffhandItem().getItem() instanceof BlockItem;
        boolean onBlock = !hit.platform().get(hit.cell()).isAir();
        if (!mainBlock && !offBlock && !onBlock) return;

        event.setCanceled(true);
        event.setSwingHand(false);
        if (event.getHand() != InteractionHand.MAIN_HAND) return;
        send(hit, mainBlock || offBlock ? PlatformActionPacket.PLACE : PlatformActionPacket.USE, offBlock);
    }

    private static void startBreaking(Target hit, Player player) {
        send(hit, PlatformActionPacket.START_BREAK, false);
        if (player.getAbilities().instabuild) {
            spawnBreakParticles(hit.platform(), hit.cell(), hit.platform().get(hit.cell()));
            breakCooldown = BREAK_COOLDOWN_TICKS;
            return;
        }
        breakingPlatform = hit.platform();
        breakingCell = hit.cell();
        breakProgress = 0f;
        breakTicks = 0;
    }

    private static void resetBreaking() {
        breakingPlatform = null;
        breakingCell = null;
        breakProgress = 0f;
        breakTicks = 0;
    }

    private static void abortBreaking() {
        if (breakingPlatform != null && breakingCell != null) {
            PacketDistributor.sendToServer(new PlatformActionPacket(breakingPlatform.carrier().getId(), PlatformActionPacket.ABORT_BREAK,
                    breakingCell.getX(), breakingCell.getY(), breakingCell.getZ(), 0, 0f, 0f, 0f, false));
        }
        resetBreaking();
    }

    @SubscribeEvent
    public static void onClientTickPre(ClientTickEvent.Pre event) {
        Minecraft mc = Minecraft.getInstance();
        Target hit = target;
        if (hit == null || mc.player == null || mc.screen != null) return;
        boolean picked = false;
        while (mc.options.keyPickItem.consumeClick()) picked = true;
        if (picked) pickPlatformBlock(mc, mc.player, hit);
    }

    private static void pickPlatformBlock(Minecraft mc, Player player, Target hit) {
        OWPlatform platform = hit.platform();
        if (!platform.contains(hit.cell()) || mc.level == null || mc.gameMode == null) return;
        BlockState state = platform.get(hit.cell());
        if (state.isAir()) return;
        BlockPos voidPos = OWPlatformSpace.toVoid(platform, hit.cell());
        BlockHitResult voidHit = new BlockHitResult(OWPlatformSpace.toVoid(platform, hit.gridHit()), hit.face(), voidPos, false);
        ItemStack stack = state.getCloneItemStack(voidHit, mc.level, voidPos, player);
        if (stack.isEmpty()) return;
        boolean creative = player.getAbilities().instabuild;
        if (creative && Screen.hasControlDown() && state.hasBlockEntity()) {
            BlockEntity blockEntity = platform.blockEntity(hit.cell());
            if (blockEntity != null) {
                CompoundTag tag = blockEntity.saveCustomAndMetadata(mc.level.registryAccess());
                blockEntity.removeComponentsFromTag(tag);
                BlockItem.setBlockEntityData(stack, blockEntity.getType(), tag);
                stack.applyComponents(blockEntity.collectComponents());
            }
        }
        Inventory inventory = player.getInventory();
        int slot = inventory.findSlotMatchingItem(stack);
        if (creative) {
            inventory.setPickedItem(stack);
            mc.gameMode.handleCreativeModeItemAdd(player.getItemInHand(InteractionHand.MAIN_HAND), 36 + inventory.selected);
        } else if (slot != -1) {
            if (Inventory.isHotbarSlot(slot)) inventory.selected = slot;
            else mc.gameMode.handlePickItem(slot);
        }
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        if (breakCooldown > 0) breakCooldown--;
        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        if (player == null || mc.level == null) {
            target = null;
            resetBreaking();
            return;
        }
        Target hit = target;
        boolean attacking = mc.options.keyAttack.isDown() && mc.screen == null;

        if (breakingPlatform == null) {
            if (attacking && hit != null && capturesAttack() && breakCooldown <= 0 && !player.isUsingItem() && hit.platform().contains(hit.cell())) {
                startBreaking(hit, player);
            }
            return;
        }
        if (!attacking || hit == null || hit.platform() != breakingPlatform || !hit.cell().equals(breakingCell) || !breakingPlatform.isActive()) {
            abortBreaking();
            return;
        }
        BlockState state = breakingPlatform.get(breakingCell);
        if (state.isAir()) {
            resetBreaking();
            return;
        }
        breakProgress += state.getDestroyProgress(player, breakingPlatform.level(), breakingCell);
        if (breakTicks++ % 4 == 0) {
            SoundType sound = state.getSoundType();
            Vec3 at = hit.worldHit();
            mc.getSoundManager().play(new SimpleSoundInstance(sound.getHitSound(), SoundSource.BLOCKS,
                    (sound.getVolume() + 1.0F) / 8.0F, sound.getPitch() * 0.5F, SoundInstance.createUnseededRandom(), at.x, at.y, at.z));
        }
        player.swing(InteractionHand.MAIN_HAND);
        spawnHitParticle(breakingPlatform, breakingCell, hit.face(), state);
        if (breakProgress >= 1.0f) {
            spawnBreakParticles(breakingPlatform, breakingCell, state);
            PacketDistributor.sendToServer(new PlatformActionPacket(breakingPlatform.carrier().getId(), PlatformActionPacket.FINISH_BREAK,
                    breakingCell.getX(), breakingCell.getY(), breakingCell.getZ(), 0,
                    (float) hit.gridHit().x, (float) hit.gridHit().y, (float) hit.gridHit().z, false));
            breakCooldown = BREAK_COOLDOWN_TICKS;
            resetBreaking();
        }
    }

    private static void spawnHitParticle(OWPlatform platform, BlockPos cell, Direction face, BlockState state) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || !state.shouldSpawnTerrainParticles()) return;
        VoxelShape shape = state.getShape(platform.level(), cell);
        if (shape.isEmpty()) return;
        AABB bounds = shape.bounds();
        RandomSource random = mc.level.getRandom();
        double inset = 0.1;
        double x = cell.getX() + random.nextDouble() * (bounds.maxX - bounds.minX - inset * 2) + inset + bounds.minX;
        double y = cell.getY() + random.nextDouble() * (bounds.maxY - bounds.minY - inset * 2) + inset + bounds.minY;
        double z = cell.getZ() + random.nextDouble() * (bounds.maxZ - bounds.minZ - inset * 2) + inset + bounds.minZ;
        switch (face) {
            case DOWN -> y = cell.getY() + bounds.minY - inset;
            case UP -> y = cell.getY() + bounds.maxY + inset;
            case NORTH -> z = cell.getZ() + bounds.minZ - inset;
            case SOUTH -> z = cell.getZ() + bounds.maxZ + inset;
            case WEST -> x = cell.getX() + bounds.minX - inset;
            case EAST -> x = cell.getX() + bounds.maxX + inset;
        }
        Vec3 world = frame(platform, 1f).toWorld(new Vec3(x, y, z));
        BlockPos at = BlockPos.containing(world);
        mc.particleEngine.add(new TerrainParticle(mc.level, world.x, world.y, world.z, 0, 0, 0, state, at)
                .updateSprite(state, at).setPower(0.2F).scale(0.6F));
    }

    private static void spawnBreakParticles(OWPlatform platform, BlockPos cell, BlockState state) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || state.isAir()) return;
        GridFrame frame = frame(platform, 1f);
        Vec3 center = frame.toWorld(new Vec3(cell.getX() + 0.5, cell.getY() + 0.5, cell.getZ() + 0.5));
        SoundType sound = state.getSoundType();
        mc.level.playLocalSound(center.x, center.y, center.z, sound.getBreakSound(), SoundSource.BLOCKS,
                (sound.getVolume() + 1.0F) / 2.0F, sound.getPitch() * 0.8F, false);
        VoxelShape shape = state.getShape(platform.level(), cell);
        if (shape.isEmpty()) shape = Shapes.block();
        shape.forAllBoxes((x0, y0, z0, x1, y1, z1) -> {
            double dx = Math.min(1.0, x1 - x0);
            double dy = Math.min(1.0, y1 - y0);
            double dz = Math.min(1.0, z1 - z0);
            int nx = Math.max(2, Mth.ceil(dx / 0.25));
            int ny = Math.max(2, Mth.ceil(dy / 0.25));
            int nz = Math.max(2, Mth.ceil(dz / 0.25));
            for (int i = 0; i < nx; i++) {
                for (int j = 0; j < ny; j++) {
                    for (int k = 0; k < nz; k++) {
                        double fx = (i + 0.5) / nx;
                        double fy = (j + 0.5) / ny;
                        double fz = (k + 0.5) / nz;
                        Vec3 local = new Vec3(cell.getX() + x0 + fx * dx, cell.getY() + y0 + fy * dy, cell.getZ() + z0 + fz * dz);
                        Vec3 world = frame.toWorld(local);
                        Vec3 speed = frame.toWorld(local.add(fx - 0.5, fy - 0.5, fz - 0.5)).subtract(world);
                        BlockPos at = BlockPos.containing(world);
                        mc.particleEngine.add(new TerrainParticle(mc.level, world.x, world.y, world.z, speed.x, speed.y, speed.z, state, at)
                                .updateSprite(state, at));
                    }
                }
            }
        });
    }

    @SubscribeEvent
    public static void onRenderStage(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS) return;
        Target hit = target;
        Minecraft mc = Minecraft.getInstance();
        if (hit == null || mc.options.hideGui || mc.level == null) return;

        OWPlatform platform = hit.platform();
        VoxelShape shape;
        if (platform.contains(hit.cell())) {
            BlockState state = platform.get(hit.cell());
            if (state.isAir()) return;
            shape = state.getShape(platform.level(), hit.cell());
        } else {
            double thickness = platform.profile().deckThickness();
            shape = Shapes.box(0, 1 - thickness, 0, 1, 1, 1);
        }
        if (shape.isEmpty()) return;

        float partialTick = event.getPartialTick().getGameTimeDeltaPartialTick(false);
        Vec3 cam = event.getCamera().getPosition();
        PoseStack poseStack = event.getPoseStack();
        poseStack.pushPose();
        VisualPose visual = freshVisualPose(platform);
        if (visual != null) {
            poseStack.translate(visual.camera().x - cam.x, visual.camera().y - cam.y, visual.camera().z - cam.z);
            poseStack.mulPose(visual.pose());
        } else {
            OWPlatformTransform transform = platform.interpolated(partialTick);
            poseStack.translate(transform.x - cam.x, transform.y - cam.y, transform.z - cam.z);
            poseStack.mulPose(Axis.YP.rotationDegrees(-transform.yaw));
            poseStack.translate(-platform.profile().centerX(), 0, -platform.profile().centerZ());
        }
        poseStack.translate(hit.cell().getX(), hit.cell().getY(), hit.cell().getZ());

        MultiBufferSource.BufferSource buffers = mc.renderBuffers().bufferSource();
        VertexConsumer lines = buffers.getBuffer(RenderType.lines());
        PoseStack.Pose pose = poseStack.last();
        shape.forAllEdges((x1, y1, z1, x2, y2, z2) -> {
            float dx = (float) (x2 - x1);
            float dy = (float) (y2 - y1);
            float dz = (float) (z2 - z1);
            float length = (float) Math.sqrt(dx * dx + dy * dy + dz * dz);
            if (length < 1.0E-6f) return;
            dx /= length;
            dy /= length;
            dz /= length;
            lines.addVertex(pose, (float) x1, (float) y1, (float) z1).setColor(0f, 0f, 0f, 0.4f).setNormal(pose, dx, dy, dz);
            lines.addVertex(pose, (float) x2, (float) y2, (float) z2).setColor(0f, 0f, 0f, 0.4f).setNormal(pose, dx, dy, dz);
        });
        buffers.endBatch(RenderType.lines());
        poseStack.popPose();
    }
}
