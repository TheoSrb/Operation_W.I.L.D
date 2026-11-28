package net.tiew.operationWild.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.tiew.operationWild.entity.platform.OWPlatformSpace;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import net.minecraft.world.phys.AABB;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Level.class)
public abstract class LevelPlatformScopeMixin {

    @ModifyReturnValue(method = "getBlockState", at = @At("RETURN"))
    private BlockState ow$platformState(BlockState original, @Local(argsOnly = true) BlockPos pos) {
        return OWPlatformSpace.isVoidY(pos.getY()) ? OWPlatformSpace.read((Level) (Object) this, pos, original) : original;
    }

    @ModifyReturnValue(method = "getFluidState", at = @At("RETURN"))
    private FluidState ow$platformFluid(FluidState original, @Local(argsOnly = true) BlockPos pos) {
        return OWPlatformSpace.isVoidY(pos.getY()) ? OWPlatformSpace.read((Level) (Object) this, pos, original.createLegacyBlock()).getFluidState() : original;
    }

    @ModifyReturnValue(method = "getBlockEntity", at = @At("RETURN"))
    private BlockEntity ow$platformBlockEntity(BlockEntity original, @Local(argsOnly = true) BlockPos pos) {
        return OWPlatformSpace.isVoidY(pos.getY()) ? OWPlatformSpace.blockEntity((Level) (Object) this, pos, original) : original;
    }

    @ModifyVariable(method = "getEntities(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/phys/AABB;Ljava/util/function/Predicate;)Ljava/util/List;",
            at = @At("HEAD"), argsOnly = true)
    private AABB ow$platformEntitySearch(AABB box) {
        return OWPlatformSpace.remapBox((Level) (Object) this, box);
    }

    @ModifyVariable(method = "getEntities(Lnet/minecraft/world/level/entity/EntityTypeTest;Lnet/minecraft/world/phys/AABB;Ljava/util/function/Predicate;Ljava/util/List;I)V",
            at = @At("HEAD"), argsOnly = true)
    private AABB ow$platformTypedEntitySearch(AABB box) {
        return OWPlatformSpace.remapBox((Level) (Object) this, box);
    }

    @Inject(method = "setBlock(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;II)Z",
            at = @At("HEAD"), cancellable = true)
    private void ow$platformWrite(BlockPos pos, BlockState state, int flags, int recursionLeft, CallbackInfoReturnable<Boolean> cir) {
        if (OWPlatformSpace.isVoidY(pos.getY()) && OWPlatformSpace.write((Level) (Object) this, pos, state)) {
            cir.setReturnValue(true);
        }
    }
}
