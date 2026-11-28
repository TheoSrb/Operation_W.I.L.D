package net.tiew.operationWild.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.network.protocol.game.ClientboundBlockEventPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.tiew.operationWild.entity.platform.OWPlatformSpace;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import javax.annotation.Nullable;

@Mixin(ServerLevel.class)
public abstract class ServerLevelPlatformMixin {

    @Inject(method = "addFreshEntity", at = @At("HEAD"), cancellable = true)
    private void ow$spawnFromPlatform(Entity entity, CallbackInfoReturnable<Boolean> cir) {
        if (!OWPlatformSpace.isVoidY(entity.getY())) return;
        ServerLevel level = (ServerLevel) (Object) this;
        Vec3 world = OWPlatformSpace.toWorld(level, entity.getX(), entity.getY(), entity.getZ());
        if (world == null) {
            cir.setReturnValue(false);
            return;
        }
        entity.setPos(world);
    }

    @Inject(method = "playSeededSound(Lnet/minecraft/world/entity/player/Player;DDDLnet/minecraft/core/Holder;Lnet/minecraft/sounds/SoundSource;FFJ)V",
            at = @At("HEAD"), cancellable = true)
    private void ow$soundFromPlatform(@Nullable Player player, double x, double y, double z, Holder<SoundEvent> sound,
                                      SoundSource category, float volume, float pitch, long seed, CallbackInfo ci) {
        if (!OWPlatformSpace.isVoidY(y)) return;
        ci.cancel();
        ServerLevel level = (ServerLevel) (Object) this;
        Vec3 world = OWPlatformSpace.toWorld(level, x, y, z);
        if (world != null) level.playSeededSound(null, world.x, world.y, world.z, sound, category, volume, pitch, seed);
    }

    @Inject(method = "levelEvent", at = @At("HEAD"), cancellable = true)
    private void ow$levelEventFromPlatform(@Nullable Player player, int type, BlockPos pos, int data, CallbackInfo ci) {
        if (!OWPlatformSpace.isVoidY(pos.getY())) return;
        ci.cancel();
        ServerLevel level = (ServerLevel) (Object) this;
        Vec3 world = OWPlatformSpace.toWorld(level, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);
        if (world != null) level.levelEvent(type == 2001 ? player : null, type, BlockPos.containing(world), data);
    }

    @Inject(method = "blockEvent", at = @At("HEAD"), cancellable = true)
    private void ow$blockEventOnPlatform(BlockPos pos, Block block, int eventId, int eventParam, CallbackInfo ci) {
        if (!OWPlatformSpace.isVoidY(pos.getY())) return;
        ci.cancel();
        ServerLevel level = (ServerLevel) (Object) this;
        BlockState state = level.getBlockState(pos);
        if (!state.is(block) || !state.triggerEvent(level, pos, eventId, eventParam)) return;
        Vec3 world = OWPlatformSpace.toWorld(level, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);
        if (world == null) return;
        level.getServer().getPlayerList().broadcast(null, world.x, world.y, world.z, 64.0, level.dimension(),
                new ClientboundBlockEventPacket(pos, block, eventId, eventParam));
    }

    @Inject(method = "sendBlockUpdated", at = @At("HEAD"), cancellable = true)
    private void ow$blockUpdatedOnPlatform(BlockPos pos, BlockState oldState, BlockState newState, int flags, CallbackInfo ci) {
        if (OWPlatformSpace.isVoidY(pos.getY())) ci.cancel();
    }
}
