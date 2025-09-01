package net.tiew.operationWild.entity.misc;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import net.tiew.operationWild.entity.OWEntityRegistry;
import net.tiew.operationWild.entity.bosses.PlantEmpressEntity;
import net.tiew.operationWild.core.OWUtils;

import java.util.*;

public class SlingshotProjectile extends AbstractArrow {
    private static final Map<BlockPos, Integer> blockDamageMap = new HashMap<>();
    private static final Timer decayTimer = new Timer(true);
    public int tranquilizerEffectiveness = 35;

    private static final Set<SlingshotProjectile> projectilesToRemove = Collections.synchronizedSet(new HashSet<>());

    static {
        decayTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if (blockDamageMap.isEmpty()) return;
                Set<BlockPos> toRemove = new HashSet<>();

                for (Map.Entry<BlockPos, Integer> entry : blockDamageMap.entrySet()) {
                    BlockPos pos = entry.getKey();
                    int damage = entry.getValue() - 1;

                    if (damage < 0) {
                        toRemove.add(pos);
                    } else {
                        blockDamageMap.put(pos, damage);
                        Level level = getCurrentLevel();
                        if (level != null) {
                            level.destroyBlockProgress(0, pos, damage);
                        }
                    }
                }

                for (BlockPos pos : toRemove) {
                    blockDamageMap.remove(pos);
                    Level level = getCurrentLevel();
                    if (level != null) {
                        level.destroyBlockProgress(0, pos, -1);
                    }
                }
            }
        }, 10000, 10000);
    }

    private static Level currentLevel = null;

    private static Level getCurrentLevel() {
        return currentLevel;
    }

    public SlingshotProjectile(EntityType<? extends AbstractArrow> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
        currentLevel = pLevel;
    }

    public SlingshotProjectile(LivingEntity shooter, Level level) {
        super(OWEntityRegistry.SLINGSHOT_PROJECTILE.get(), shooter, level, new ItemStack(Items.COBBLESTONE), null);
        currentLevel = level;
    }

    @Override
    protected ItemStack getDefaultPickupItem() {
        return new ItemStack(Items.COBBLESTONE);
    }

    @Override
    public void tick() {
        super.tick();

        if (!this.level().isClientSide() && projectilesToRemove.contains(this)) {
            projectilesToRemove.remove(this);
            this.spawnAtLocation(Items.COBBLESTONE);
            this.discard();
        }

        if (this.despawnCounter > 0) {
            this.despawnCounter--;
            if (this.despawnCounter == 0) {
                if (!this.level().isClientSide()) {
                    boolean shouldDrop = true;
                    dispawn(shouldDrop, this);
                }
            }
        }

        if (!this.level().isClientSide() && projectilesToRemove.contains(this)) {
            projectilesToRemove.remove(this);
            this.spawnAtLocation(Items.COBBLESTONE);
            this.discard();
        }
    }

    public static void dispawn(boolean dropProjectile, SlingshotProjectile projectile) {
        if (dropProjectile) {
            projectilesToRemove.add(projectile);
        } else {
            discardEntity(projectile);
        }
    }

    public static void discardEntity(SlingshotProjectile slingshotProjectile) {
        if (!slingshotProjectile.level().isClientSide()) {
            slingshotProjectile.discard();
        }
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        Entity entity = result.getEntity();
        if (entity == null) return;
        Entity $$3 = this.getOwner();
        DamageSource $$4 = this.damageSources().trident(this, (Entity) ($$3 == null ? this : $$3));

        OWUtils.spawnBlockParticles(this, Blocks.COBBLESTONE, 0, 0, 0, 20, 0.5);
        float pitch = (float) OWUtils.generateRandomInterval(0.85f, 1.15f);
        this.playSound(SoundEvents.STONE_BREAK, 1.0f, pitch);
        entity.hurt($$4, 2);

        Vec3 direction = entity.position().subtract(this.position()).normalize();
        if (!(entity instanceof PlantEmpressEntity)) {
            entity.push(direction.x * 0.35, 0.2, direction.z * 0.35);
        }

        this.setDespawnCounter(5);
    }

    @Override
    protected void onHitBlock(BlockHitResult result) {
        BlockPos blockPos = result.getBlockPos();
        BlockState blockState = this.level().getBlockState(blockPos);

        float hardness = blockState.getBlock().defaultDestroyTime();
        if (hardness > 3.5 || hardness < 0) {
            this.setDespawnCounter(5);
            return;
        }

        OWUtils.spawnBlockParticles(this, Blocks.COBBLESTONE, 0, 0, 0, 20, 0.5);
        float pitch = (float) OWUtils.generateRandomInterval(0.85f, 1.15f);
        this.playSound(SoundEvents.STONE_BREAK, 1.0f, pitch);

        int currentDamage = blockDamageMap.getOrDefault(blockPos, -1);
        int newDamage = Math.min(currentDamage + 1, 9);
        blockDamageMap.put(blockPos, newDamage);

        this.level().destroyBlockProgress(this.getId(), blockPos, newDamage);

        if (newDamage >= 9 && !this.level().isClientSide()) {
            this.level().destroyBlock(blockPos, true);
            blockDamageMap.remove(blockPos);
            this.level().destroyBlockProgress(this.getId(), blockPos, -1);
        }

        this.setDespawnCounter(5);
    }

    private int despawnCounter = -1;

    private void setDespawnCounter(int ticks) {
        this.despawnCounter = ticks;
    }

}