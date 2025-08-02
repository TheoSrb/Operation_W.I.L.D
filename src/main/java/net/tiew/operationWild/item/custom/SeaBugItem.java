package net.tiew.operationWild.item.custom;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.tiew.operationWild.OperationWild;
import net.tiew.operationWild.entity.OWEntityRegistry;
import net.tiew.operationWild.entity.custom.vehicle.SeaBugEntity;

public class SeaBugItem extends Item {

    public SeaBugItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack itemStack = player.getItemInHand(hand);

        if (player != null) {
            BlockHitResult hitResult = getPlayerPOVHitResult(level, player, ClipContext.Fluid.SOURCE_ONLY);

            if (hitResult.getType() == HitResult.Type.BLOCK) {
                BlockPos pos = hitResult.getBlockPos();
                BlockState blockState = level.getBlockState(pos);

                if (blockState.getFluidState().is(Fluids.WATER) || blockState.is(Blocks.WATER)) {
                    if (!level.isClientSide) {
                        SeaBugEntity seaBug = OWEntityRegistry.SEABUG.get().create(level);
                        if (seaBug != null) {
                            Vec3 lookDirection = player.getLookAngle();
                            double x = player.getX() + lookDirection.x * 3.0;
                            double y = player.getY() + player.getEyeHeight();
                            double z = player.getZ() + lookDirection.z * 3.0;

                            seaBug.setPos(x, y, z);
                            level.addFreshEntity(seaBug);
                            if (!player.isCreative()) itemStack.shrink(1);
                            seaBug.setOwnerUUID(player.getUUID());

                            if (player instanceof ServerPlayer serverPlayer) {
                                serverPlayer.getServer().getCommands().performPrefixedCommand(serverPlayer.getServer().createCommandSourceStack().withSuppressedOutput(), "advancement grant " + serverPlayer.getGameProfile().getName() + " only " + OperationWild.MOD_ID + ":captain");
                            }

                            return InteractionResultHolder.success(itemStack);
                        }
                    }
                } else {
                    player.displayClientMessage(Component.translatable("seabug.needWater").setStyle(Style.EMPTY.withBold(true).withColor(0xFF0000)), true);
                }
            }
        }

        return InteractionResultHolder.pass(itemStack);
    }
}
