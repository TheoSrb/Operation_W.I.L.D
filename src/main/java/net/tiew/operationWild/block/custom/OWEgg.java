package net.tiew.operationWild.block.custom;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.tiew.operationWild.entity.OWEntity;
import net.tiew.operationWild.entity.OWEntityRegistry;
import net.tiew.operationWild.entity.animals.terrestrial.PeacockEntity;
import net.tiew.operationWild.entity.variants.PeacockVariant;
import net.tiew.operationWild.core.OWUtils;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public  class OWEgg extends HorizontalDirectionalBlock {
    private int counter = 0;
    private final int MAX_TIME_TICKS = 100;
    public LivingEntity owner = null;
    public static final MapCodec<OWEgg> CODEC = simpleCodec(OWEgg::new);
    public static final VoxelShape SHAPE = Block.box(5.0, 0.0, 5.0, 11.0, 9.0, 11.0);

    private static final Map<BlockPos, Float> BABY_MAX_HEALTH = new HashMap<>();
    private static final Map<BlockPos, Float> BABY_MAX_DAMAGE = new HashMap<>();
    private static final Map<BlockPos, Float> BABY_MAX_SPEED = new HashMap<>();
    private static final Map<BlockPos, Integer> BABY_VARIANT = new HashMap<>();
    private static final Map<BlockPos, Float> BABY_SCALE = new HashMap<>();

    public void setMaxHealthForPosition(BlockPos pos, float maxHealth) { BABY_MAX_HEALTH.put(pos.immutable(), maxHealth);}
    public float getMaxHealthForPosition(BlockPos pos) { return BABY_MAX_HEALTH.getOrDefault(pos, 12.0f);}

    public void setMaxDamageForPosition(BlockPos pos, float maxDamage) { BABY_MAX_DAMAGE.put(pos.immutable(), maxDamage);}
    public float getMaxDamageForPosition(BlockPos pos) { return BABY_MAX_DAMAGE.getOrDefault(pos, 1.5f);}

    public void setMaxSpeedForPosition(BlockPos pos, float maxSpeed) { BABY_MAX_SPEED.put(pos.immutable(), maxSpeed);}
    public float getMaxSpeedForPosition(BlockPos pos) { return BABY_MAX_SPEED.getOrDefault(pos, 0.19f);}

    public void setVariantForPosition(BlockPos pos, int variant) { BABY_VARIANT.put(pos.immutable(), variant);}
    public int getVariantForPosition(BlockPos pos) { return BABY_VARIANT.getOrDefault(pos, 0);}

    public void setScaleForPosition(BlockPos pos, float scale) { BABY_SCALE.put(pos.immutable(), scale);}
    public float getScaleForPosition(BlockPos pos) { return BABY_SCALE.getOrDefault(pos, 1.1f);}

    public OWEgg(Properties pProperties) {
        super(pProperties);
    }


    @Override
    protected VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
        return SHAPE;
    }

    @Override
    protected MapCodec<? extends HorizontalDirectionalBlock> codec() {
        return CODEC;
    }

    @Override
    protected void tick(BlockState blockState, ServerLevel serverLevel, BlockPos blockPos, RandomSource randomSource) {
        super.tick(blockState, serverLevel, blockPos, randomSource);
        serverLevel.scheduleTick(blockPos, this, 1);
        if (!canStartCrack(serverLevel, blockPos)) return;
        counter++;

        if (counter >= MAX_TIME_TICKS) {
            counter = 0;
            serverLevel.playSound(null, blockPos, SoundEvents.SNIFFER_EGG_HATCH, SoundSource.BLOCKS, 0.7F, 0.9F + randomSource.nextFloat() * 0.2F);
            PeacockEntity entity = OWEntityRegistry.PEACOCK.get().create(serverLevel);
            if (entity != null) {
                spawnBaby(entity, serverLevel, blockPos, OWUtils.RANDOM(20));
            }
            serverLevel.destroyBlock(blockPos, false);
        } else if (counter % (MAX_TIME_TICKS / 4) == 0) {
            serverLevel.playSound(null, blockPos, SoundEvents.SNIFFER_EGG_CRACK, SoundSource.BLOCKS, 0.7F, 0.9F + randomSource.nextFloat() * 0.2F);
        }

    }

    @Override
    public void destroy(LevelAccessor levelAccessor, BlockPos blockPos, BlockState blockState) {
        super.destroy(levelAccessor, blockPos, blockState);
        int radius = 20;
        LivingEntity target = levelAccessor.getNearestPlayer(blockPos.getX(), blockPos.getY(), blockPos.getZ(), 10.0, false);
        List<LivingEntity> nearbyEntities = levelAccessor.getEntitiesOfClass(LivingEntity.class, new AABB(blockPos.getX() - radius, blockPos.getY() - radius, blockPos.getZ() - radius, blockPos.getX() + radius, blockPos.getY() + radius, blockPos.getZ() + radius), entity -> entity instanceof PeacockEntity && !((PeacockEntity) entity).isBaby());

        for (LivingEntity entity : nearbyEntities) {
            if (target instanceof Player player) {
                if (!player.isCreative()) {
                    ((PeacockEntity) entity).peacockIsAggressive = true;
                    ((PeacockEntity) entity).setTarget(player);
                }
            } else {
                ((PeacockEntity) entity).peacockIsAggressive = true;
                ((PeacockEntity) entity).setTarget(target);
            }
        }

        BABY_MAX_HEALTH.remove(blockPos);
        BABY_MAX_DAMAGE.remove(blockPos);
        BABY_MAX_SPEED.remove(blockPos);
        BABY_VARIANT.remove(blockPos);
        BABY_SCALE.remove(blockPos);
    }

    public void spawnBaby(OWEntity entity, ServerLevel serverLevel, BlockPos blockPos, boolean spawnTwins) {
        Vec3 vec3 = blockPos.getCenter();
        float pitch = (float) OWUtils.generateExponentialExp(0.9, 1.1);
        entity.setBaby(true);
        DifficultyInstance difficulty = serverLevel.getCurrentDifficultyAt(blockPos);
        entity.finalizeSpawn(serverLevel, difficulty, MobSpawnType.BREEDING, null);
        if (entity instanceof PeacockEntity peacock) {
            peacock.maxHealth = getMaxHealthForPosition(blockPos);
            peacock.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(getMaxDamageForPosition(blockPos));
            peacock.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(getMaxSpeedForPosition(blockPos));
            peacock.setBaseHealth(getMaxHealthForPosition(blockPos));
            peacock.setDamageToClient(getMaxDamageForPosition(blockPos));
            peacock.setBaseDamage(getMaxDamageForPosition(blockPos));
            peacock.setBaseSpeed(getMaxSpeedForPosition(blockPos));
            peacock.setVariant(PeacockVariant.byId(getVariantForPosition(blockPos)));
            peacock.setInitialVariant(PeacockVariant.byId(getVariantForPosition(blockPos)));
            peacock.setScale(getScaleForPosition(blockPos));

            if (OWUtils.RANDOM(50)) peacock.setVariant(PeacockVariant.ALBINO);
        }
        entity.moveTo(vec3.x(), vec3.y(), vec3.z(), Mth.wrapDegrees(serverLevel.random.nextFloat() * 360.0F), 0.0F);
        serverLevel.addFreshEntity(entity);

        if (spawnTwins) {
            OWEntity secondEntity = (OWEntity) entity.getType().create(serverLevel);
            if (secondEntity != null) {
                secondEntity.setBaby(true);
                secondEntity.finalizeSpawn(serverLevel, difficulty, MobSpawnType.BREEDING, null);
                if (secondEntity instanceof PeacockEntity peacock) {
                    peacock.maxHealth = getMaxHealthForPosition(blockPos);
                    peacock.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(getMaxDamageForPosition(blockPos));
                    peacock.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(getMaxSpeedForPosition(blockPos));
                    peacock.setBaseHealth(getMaxHealthForPosition(blockPos));
                    peacock.setDamageToClient(getMaxDamageForPosition(blockPos));
                    peacock.setBaseDamage(getMaxDamageForPosition(blockPos));
                    peacock.setBaseSpeed(getMaxSpeedForPosition(blockPos));
                    peacock.setVariant(PeacockVariant.byId(getVariantForPosition(blockPos)));
                    peacock.setInitialVariant(PeacockVariant.byId(getVariantForPosition(blockPos)));
                    peacock.setScale(getScaleForPosition(blockPos));

                    if (OWUtils.RANDOM(50)) peacock.setVariant(PeacockVariant.ALBINO);
                }
                double offsetX = (serverLevel.random.nextDouble() - 0.5) * 2.0;
                double offsetZ = (serverLevel.random.nextDouble() - 0.5) * 2.0;
                secondEntity.moveTo(vec3.x() + offsetX, vec3.y(), vec3.z() + offsetZ, Mth.wrapDegrees(serverLevel.random.nextFloat() * 360.0F), 0.0F);
                serverLevel.addFreshEntity(secondEntity);
            }
        }
    }

    private boolean canStartCrack(Level world, BlockPos pos) {
        return true;
    }

    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean isMoving) {
        super.onPlace(state, level, pos, oldState, isMoving);
        if (!level.isClientSide) {
            level.scheduleTick(pos, this, 1);
        }
    }

    @Override
    public void setPlacedBy(Level level, BlockPos blockPos, BlockState blockState, @org.jetbrains.annotations.Nullable LivingEntity living, ItemStack itemStack) {
        super.setPlacedBy(level, blockPos, blockState, living, itemStack);
        if (living != null) owner = living;
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext pContext) {
        return this.defaultBlockState().setValue(FACING, pContext.getHorizontalDirection().getOpposite());
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> pBuilder) {
        pBuilder.add(FACING);
    }
}
