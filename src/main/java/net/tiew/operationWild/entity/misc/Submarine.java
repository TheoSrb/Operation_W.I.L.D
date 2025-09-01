package net.tiew.operationWild.entity.misc;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.fluids.FluidType;
import net.tiew.operationWild.entity.animals.aquatic.JellyfishEntity;
import org.jetbrains.annotations.Nullable;
import net.tiew.operationWild.entity.OWEntity;
import net.tiew.operationWild.event.ClientEvents;
import net.tiew.operationWild.sound.OWSounds;
import net.tiew.operationWild.core.OWDamageSources;
import net.tiew.operationWild.core.OWUtils;

import java.util.List;
import java.util.UUID;

public class Submarine extends OWEntity {
    public static final int MAX_DEPTH = 0;
    public int soundTimer = 0;
    public boolean isPlayingMoveSound = false;
    public float damageTimer = 0;
    public boolean stopLights = false;
    public boolean firstTimeToDeep = true;
    public boolean canDesactivateLight = false;

    public float accelerationLevel = 0.0f;
    public float rightAccelerationLevel = 0.0f;
    public float leftAccelerationLevel = 0.0f;
    public float upAccelerationLevel = 0.0f;
    public float backwardAccelerationLevel = 0.0f;

    private static final EntityDataAccessor<Boolean> IS_LIGHT_ON = SynchedEntityData.defineId(Submarine.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> IS_OFF = SynchedEntityData.defineId(Submarine.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Float> ENERGY = SynchedEntityData.defineId(Submarine.class, EntityDataSerializers.FLOAT);


    protected Submarine(EntityType<? extends TamableAnimal> entityType, Level level, float scale, int maxSleepBar, int sleepBarDownSpeed) {
        super(entityType, level, scale, maxSleepBar, sleepBarDownSpeed);
    }

    public void checkPlayer02(double recuperationSpeed) {
        LivingEntity rider = this.getControllingPassenger();
        if (rider != null) {
            if (rider.isUnderWater()) {
                int currentAir = rider.getAirSupply();
                int maxAir = rider.getMaxAirSupply();

                if (currentAir < maxAir) {
                    rider.setAirSupply((int) Math.min(currentAir + recuperationSpeed, maxAir));
                }
            }
        }
    }

    public void applyWaterPressureDamage(int depth, Player player) {
        int waterPressure = (int) ClientEvents.getWaterPressure(depth);
        float damageInterval = (Math.max((-1.25f * waterPressure + 65) / 30.0f, 0.1f));
        float normalizedPressure = waterPressure / 4.0f;
        float intensity = 0.05f * (float) Math.pow(normalizedPressure, 2f);

        ClientEvents.shakeCamera(intensity, player);

        if (this.level().isClientSide) {
            return;
        }

        damageTimer += 0.05f;

        if (damageTimer >= damageInterval) {
            this.invulnerableTime = 0;

            DamageSource waterPressureDamage = OWDamageSources.createWaterPressureDamage((ServerLevel) this.level());
            this.hurt(waterPressureDamage, 4);

            this.invulnerableTime = 0;
            damageTimer = 0.0f;
        }

        if (this.tickCount % 100 == 0 || firstTimeToDeep) {
            Component message = Component.translatable("tooHighPressure")
                    .setStyle(Style.EMPTY
                            .withColor(ChatFormatting.YELLOW));
            Minecraft.getInstance().gui.setOverlayMessage(message, true);
            firstTimeToDeep = false;
        }
    }

    public void setOff(boolean isOff) { this.entityData.set(IS_OFF, isOff);}
    public boolean isOff() { return this.entityData.get(IS_OFF);}

    public void spawnBubbleParticles() {
        ParticleOptions particleoptions = ParticleTypes.SPLASH;

        double backwardOffset = -0.5;

        for (int i = 0; i < 7; ++i) {
            double d0 = this.random.nextGaussian() * 0.1D;
            double d1 = this.random.nextGaussian() * 0.1D;
            double d2 = this.random.nextGaussian() * 0.1D;

            double particleX = this.getRandomX(0.4D) + this.getLookAngle().x * backwardOffset;
            double particleY = this.getRandomY() - 1.0D;
            double particleZ = this.getRandomZ(0.4D) + this.getLookAngle().z * backwardOffset;

            this.level().addParticle(particleoptions, particleX, particleY, particleZ, d0, d1, d2);
            this.level().addParticle(particleoptions, particleX, particleY, particleZ, d0, d1, d2);
            this.level().addParticle(particleoptions, particleX, particleY, particleZ, d0, d1, d2);
        }
    }

    @Override
    public Vec3 getDismountLocationForPassenger(LivingEntity living) {
        if (this.level().isClientSide) {
            Minecraft.getInstance().getSoundManager().stop(OWSounds.SUBMARINE_MOVE_LOOP.get().getLocation(), null);
        }
        this.isPlayingMoveSound = false;
        this.soundTimer = 0;
        return super.getDismountLocationForPassenger(living);
    }

    @Override
    public boolean hurt(DamageSource damageSource, float v) {
        float pitch = (float) OWUtils.generateRandomInterval(0.9, 1.1);
        this.playSound(SoundEvents.GLASS_BREAK, 1.0f, pitch);
        return super.hurt(damageSource, v);
    }

    @Override
    public boolean canDrownInFluidType(FluidType type) {
        return false;
    }

    @Override
    public boolean isPushedByFluid(FluidType type) {
        return false;
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        this.setSaddle(true);
        player.startRiding(this);
        return super.mobInteract(player, hand);
    }

    public void showSubmarine(int distance) {
        UUID ownerUUID = this.getOwnerUUID();
        if (ownerUUID == null) return;

        List<Player> players = level().getEntitiesOfClass(Player.class, getBoundingBox().inflate(distance));
        boolean ownerFound = false;

        for (Player player : players) {
            if (player.getUUID().equals(ownerUUID)) {
                ownerFound = true;
                break;
            }
        }

        if (!ownerFound) {
            if (level().isClientSide) {
                Player clientPlayer = Minecraft.getInstance().player;
                if (clientPlayer != null && clientPlayer.getUUID().equals(ownerUUID)) {
                    this.addEffect(new MobEffectInstance(MobEffects.GLOWING, 10, 0, false, false, false));
                }
            }
            this.setSitting(true);
        } else {
            if (level().isClientSide) {
                this.removeEffect(MobEffects.GLOWING);
            }
            this.setSitting(false);
        }
    }

    public void setLightOn(boolean isLightOn) {
        this.entityData.set(IS_LIGHT_ON, isLightOn);
        float pitch = (float) (OWUtils.generateRandomInterval(0.9, 1.1));
        this.playSound(OWSounds.SUBMARINE_SWITCH_LIGHT.get(), 1.0f, pitch);
    }
    public boolean isLightOn() { return this.entityData.get(IS_LIGHT_ON);}

    @Override
    public void onInsideBubbleColumn(boolean b) {
        if (!isVehicle()) {
            super.onAboveBubbleCol(b);
        }
        this.resetFallDistance();
    }

    @Override
    public void onAboveBubbleCol(boolean b) {
        if (!isVehicle()) {
            super.onAboveBubbleCol(b);
        }
    }

    public void tickRidden(Player player, Vec3 vec3) {
        super.tickRidden(player, vec3);
        Vec2 vec2 = this.getRiddenRotation(player);
        this.setXRot(vec2.x * 2);
        this.setYRot(vec2.y);
        this.yRotO = this.yBodyRot = this.yHeadRot = this.getYRot();
        player.resetFallDistance();
    }

    @Override
    public boolean addEffect(MobEffectInstance mobEffectInstance, @Nullable Entity entity) {
        if (mobEffectInstance.getEffect() == MobEffects.GLOWING) {
            return super.addEffect(mobEffectInstance, entity);
        }

        return false;
    }

    public void setEnergy(float getEnergy) {this.entityData.set(ENERGY, getEnergy);}
    public float getEnergy() { return this.entityData.get(ENERGY);}

    @Override
    public void die(DamageSource damageSource) {
        super.die(damageSource);
        if (this.level().isClientSide) {
            Minecraft.getInstance().getSoundManager().stop(OWSounds.SUBMARINE_MOVE_LOOP.get().getLocation(), null);
        }
        this.isPlayingMoveSound = false;
        this.soundTimer = 0;
    }

    @Override
    public void tick() {
        super.tick();

        List<JellyfishEntity> jellyfishEntities = this.level().getEntitiesOfClass(JellyfishEntity.class, getBoundingBox().inflate(1));

        if (jellyfishEntities.isEmpty()) {
            setOff(getEnergy() <= 0);
        }



        if (isOff()) {
            if (!canDesactivateLight) {
                setLightOn(false);
                canDesactivateLight = true;
            }
        } else canDesactivateLight = false;

        this.setSaddle(!isOff());

        setNoGravity(isInWater());

        if (!isOff()) checkPlayer02(5);

        if (this.level().getRawBrightness(this.blockPosition(), 0) > 3) {
            if (!stopLights) {
                this.setLightOn(false);
                stopLights = true;
            }
        } else stopLights = false;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(IS_LIGHT_ON, false);
        builder.define(ENERGY, 0.0f);
        builder.define(IS_OFF, false);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putBoolean("isLightOn", this.isLightOn());
        tag.putBoolean("isOff", this.isOff());
        tag.putFloat("getEnergy", this.getEnergy());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.entityData.set(IS_LIGHT_ON, tag.getBoolean("isLightOn"));
        this.entityData.set(IS_OFF, tag.getBoolean("isOff"));
        this.entityData.set(ENERGY, tag.getFloat("getEnergy"));
    }
}
