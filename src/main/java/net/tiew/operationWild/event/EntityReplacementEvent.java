package net.tiew.operationWild.event;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.level.levelgen.Heightmap;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.tiew.operationWild.OperationWild;
import net.tiew.operationWild.entity.OWEntityRegistry;

@EventBusSubscriber(modid = OperationWild.MOD_ID, bus = EventBusSubscriber.Bus.GAME)
public class EntityReplacementEvent {

    @SubscribeEvent
    public static void onEntityJoinWorld(EntityJoinLevelEvent event) {
        if (event.getLevel().isClientSide()) return;

        ServerLevel level = (ServerLevel) event.getLevel();
        int seaLevel = level.getSeaLevel();

        replaceEntity(event, OWEntityRegistry.JELLYFISH.get(), 50, seaLevel - level.random.nextInt(10) - 1, true);
    }

    private static void replaceEntity(EntityJoinLevelEvent event, EntityType<?> entityType, int maxHeight, int wantedHeight, boolean isWaterEntity) {
        if (event.getEntity().getType() == entityType && !event.getLevel().isClientSide()) {
            ServerLevel level = (ServerLevel) event.getLevel();
            Entity entity = event.getEntity();

            if (entity instanceof Mob mob && mob.getSpawnType() != MobSpawnType.NATURAL) {
                return;
            }

            if (entity.getY() >= maxHeight) {
                int newY = wantedHeight;

                BlockPos newPos = new BlockPos((int)entity.getX(), newY, (int)entity.getZ());

                if (isWaterEntity) {
                    if (level.getFluidState(newPos).is(FluidTags.WATER) && level.getFluidState(newPos.above()).is(FluidTags.WATER)) {
                        entity.setPos(entity.getX(), newY + 0.5, entity.getZ());
                    } else {
                        for (int attempt = 0; attempt < 10; attempt++) {
                            int tryY = newY;
                            BlockPos tryPos = new BlockPos((int) entity.getX(), tryY, (int) entity.getZ());

                            if (level.getFluidState(tryPos).is(FluidTags.WATER) && level.getFluidState(tryPos.above()).is(FluidTags.WATER)) {
                                entity.setPos(entity.getX(), tryY + 0.5, entity.getZ());
                                break;
                            }
                        }
                    }
                } else {
                    if (level.getBlockState(newPos).isSolid() && !level.getBlockState(newPos.above()).isSolid() && !level.getFluidState(newPos.above()).is(FluidTags.WATER)) {
                        entity.setPos(entity.getX(), newY + 1.0, entity.getZ());
                    } else {
                        for (int attempt = 0; attempt < 10; attempt++) {
                            int tryY = level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, new BlockPos((int)entity.getX(), 0, (int)entity.getZ())).getY();
                            BlockPos tryPos = new BlockPos((int) entity.getX(), tryY, (int) entity.getZ());
                            BlockPos groundPos = tryPos.below();

                            if (level.getBlockState(groundPos).isSolid() && !level.getBlockState(tryPos).isSolid() && !level.getBlockState(tryPos.above()).isSolid() && !level.getFluidState(tryPos).is(FluidTags.WATER)) {
                                entity.setPos(entity.getX(), tryY + 0.5, entity.getZ());
                                break;
                            }
                        }
                    }
                }
            }
        }
    }

}
