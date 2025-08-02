package net.tiew.operationWild.enchantment.custom;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.enchantment.EnchantedItemInUse;
import net.minecraft.world.item.enchantment.effects.EnchantmentEntityEffect;
import net.minecraft.world.phys.Vec3;

import java.util.Random;

public record VampirismEnchantment() implements EnchantmentEntityEffect {
    public static final MapCodec<VampirismEnchantment> CODEC = MapCodec.unit(VampirismEnchantment::new);


    @Override
    public void apply(ServerLevel serverLevel, int enchantmentLevel, EnchantedItemInUse enchantedItemInUse, Entity entity, Vec3 vec3) {
        Random random = new Random();
        int chance = random.nextInt(100);
        LivingEntity livingEntity = enchantedItemInUse.owner();
        LivingEntity target = entity instanceof LivingEntity ? (LivingEntity) entity : null;

        if (livingEntity != null && target != null && livingEntity.getHealth() < livingEntity.getMaxHealth()) {
            if (enchantmentLevel == 1) {
                if (chance <= 15) {
                    livingEntity.heal(target.getMaxHealth() * 0.03f);
                    serverLevel.sendParticles(
                            ParticleTypes.HEART,
                            livingEntity.getX(),
                            livingEntity.getY() + 1.0D,
                            livingEntity.getZ(),
                            5,
                            0.5D, 0.5D, 0.5D,
                            0.1D
                    );
                }
            }
            else if (enchantmentLevel == 2) {
                if (chance <= 11) {
                    livingEntity.heal(target.getMaxHealth() * 0.06f);
                    serverLevel.sendParticles(
                            ParticleTypes.HEART,
                            livingEntity.getX(),
                            livingEntity.getY() + 1.0D,
                            livingEntity.getZ(),
                            5,
                            0.5D, 0.5D, 0.5D,
                            0.1D
                    );
                }
            }
            else if (enchantmentLevel == 3) {
                if (chance <= 8) {
                    livingEntity.heal(target.getMaxHealth() * 0.09f);
                    serverLevel.sendParticles(
                            ParticleTypes.HEART,
                            livingEntity.getX(),
                            livingEntity.getY() + 1.0D,
                            livingEntity.getZ(),
                            5,
                            0.5D, 0.5D, 0.5D,
                            0.1D
                    );
                }
            }
        }
    }

    @Override
    public MapCodec<? extends EnchantmentEntityEffect> codec() {
        return CODEC;
    }
}
