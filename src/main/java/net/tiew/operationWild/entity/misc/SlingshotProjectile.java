package net.tiew.operationWild.entity.misc;

import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
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
    // Progression de casse des blocs, indexée par GlobalPos (dimension + position) pour éviter les
    // collisions entre dimensions. Le décai périodique est piloté par le tick SERVEUR (voir decayTick),
    // et non plus par un Timer sur un thread annexe (accéder au monde hors thread principal = crashs).
    private static final Map<GlobalPos, Integer> blockDamageMap = new HashMap<>();
    public int tranquilizerEffectiveness = 35;

    private static final Set<SlingshotProjectile> projectilesToRemove = Collections.synchronizedSet(new HashSet<>());

    /** Id de « casseur » déterministe par position (même valeur au marquage et au décai), négatif pour ne
     *  jamais recouvrir l'overlay de casse d'un joueur (ids d'entités positifs). */
    private static int blockBreakerId(BlockPos pos) {
        return Integer.MIN_VALUE + Math.floorMod(pos.hashCode(), Integer.MAX_VALUE);
    }

    /**
     * Décrémente la progression de casse de tous les blocs suivis, <b>sur le thread principal</b>.
     * Appelé périodiquement (toutes les ~10 s) depuis {@link net.tiew.operationWild.event.ServerEvents}.
     */
    public static void decayTick(net.minecraft.server.MinecraftServer server) {
        if (blockDamageMap.isEmpty()) return;
        Iterator<Map.Entry<GlobalPos, Integer>> it = blockDamageMap.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<GlobalPos, Integer> entry = it.next();
            GlobalPos gp = entry.getKey();
            net.minecraft.server.level.ServerLevel level = server.getLevel(gp.dimension());
            int damage = entry.getValue() - 1;
            if (damage < 0) {
                it.remove();
                if (level != null) level.destroyBlockProgress(blockBreakerId(gp.pos()), gp.pos(), -1);
            } else {
                entry.setValue(damage);
                if (level != null) level.destroyBlockProgress(blockBreakerId(gp.pos()), gp.pos(), damage);
            }
        }
    }

    public SlingshotProjectile(EntityType<? extends AbstractArrow> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }

    public SlingshotProjectile(LivingEntity shooter, Level level) {
        super(OWEntityRegistry.SLINGSHOT_PROJECTILE.get(), shooter, level, new ItemStack(Items.COBBLESTONE), null);
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

        GlobalPos key = GlobalPos.of(this.level().dimension(), blockPos);
        int breaker = blockBreakerId(blockPos);
        int currentDamage = blockDamageMap.getOrDefault(key, -1);
        int newDamage = Math.min(currentDamage + 1, 9);
        blockDamageMap.put(key, newDamage);

        this.level().destroyBlockProgress(breaker, blockPos, newDamage);

        if (newDamage >= 9 && !this.level().isClientSide()) {
            this.level().destroyBlock(blockPos, true);
            blockDamageMap.remove(key);
            this.level().destroyBlockProgress(breaker, blockPos, -1);
        }

        this.setDespawnCounter(5);
    }

    private int despawnCounter = -1;

    private void setDespawnCounter(int ticks) {
        this.despawnCounter = ticks;
    }

}