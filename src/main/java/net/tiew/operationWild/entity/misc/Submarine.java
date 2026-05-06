package net.tiew.operationWild.entity.misc;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
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
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
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
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.fluids.FluidType;
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

    // Axes signés : [-100, +100] pour longitudinal/latéral, [0, 100] pour vertical
    // positif longitudinal = avant, négatif = arrière
    // positif latéral = droite, négatif = gauche
    public float longitudinalAccel = 0.0f;
    public float lateralAccel = 0.0f;
    public float verticalAccel = 0.0f;

    private float laggedXRot = 0.0f;

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
            //Minecraft.getInstance().gui.setOverlayMessage(message, true); /!\ SERVER DON'T WORK
            firstTimeToDeep = false;
        }
    }

    public void setOff(boolean isOff) {
        this.entityData.set(IS_OFF, isOff);
    }

    public boolean isOff() {
        return this.entityData.get(IS_OFF);
    }

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
        /*if (this.level().isClientSide) {
            Minecraft.getInstance().getSoundManager().stop(OWSounds.SUBMARINE_MOVE_LOOP.get().getLocation(), null);
        }*/
        // /!\ SERVER DON'T WORK
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
            if (!level().isClientSide()) {
                Player owner = level().getPlayerByUUID(ownerUUID);
                if (owner != null && owner.distanceToSqr(this) < 64.0) { // Dans un rayon raisonnable
                    this.addEffect(new MobEffectInstance(MobEffects.GLOWING, 10, 0, false, false, false));
                }
            }
            this.setSitting(true);
        } else {
            if (!level().isClientSide()) {
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

    public boolean isLightOn() {
        return this.entityData.get(IS_LIGHT_ON);
    }

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

    protected double getSubmarineBaseSpeed() {
        return 0.4;
    }

    protected float getSideSpeedMultiplier() {
        return 0.5f;
    }

    protected float getBackwardSpeedMultiplier() {
        return 0.5f;
    }

    protected double getUpSpeed() {
        return 0.2;
    }

    protected float getRotationLag() {
        return 1.0f;
    }

    protected float getAccelerationMultiplier() {
        return 1.0f;
    }

    protected float getBrakingMultiplier() {
        return 3.0f;
    }

    // --- Paramètres du shader de lumière — à surcharger dans chaque sous-marin ---
    // écart de chaque phare par rapport au centre de l'écran [0.0–0.5]
    public float getShaderLightSeparation()        { return 0.15f; }
    // position verticale des phares [0.0–1.0], 0.5 = centre
    public float getShaderLightY()                 { return 0.500f; }
    // rayon gaussien du spot : plus bas = spot plus grand
    public float getShaderSpotRadius()             { return 20.0f; }
    // exposant de contraste : plus haut = bords plus nets, zone sombre plus dure
    public float getShaderContrastPow()            { return 2.0f; }
    // intensité de la lumière plate ajoutée sur les blocs éclairés
    public float getShaderAdditiveStrength()       { return 0.30f; }
    // facteur d'amplification de la couleur des blocs dans le faisceau
    public float getShaderMultiplicativeStrength() { return 1.0f; }
    // assombrissement des zones hors faisceau [0.0–1.0]
    public float getShaderShadowFactor()           { return 0.0f; }
    // couleur RGB du faisceau
    public float[] getShaderLightColor()           { return new float[]{1.0f, 1.0f, 1.0f}; }
    // true = un seul faisceau centré, false = deux faisceaux écartés
    public boolean isSingleBeam()                  { return false; }

    protected static float[] hexToRGB(String hex) {
        int color = Integer.parseUnsignedInt(hex.replace("#", ""), 16);
        return new float[]{
            ((color >> 16) & 0xFF) / 255.0f,
            ((color >> 8)  & 0xFF) / 255.0f,
            ( color        & 0xFF) / 255.0f
        };
    }

    public void tickRidden(Player player, Vec3 vec3) {
        super.tickRidden(player, vec3);
        Vec2 vec2 = this.getRiddenRotation(player);

        float lag = getRotationLag();

        float currentYRot = this.getYRot();
        float deltaYRot = Mth.wrapDegrees(vec2.y - currentYRot);
        float newYRot = currentYRot + deltaYRot * lag;

        float targetXRot = vec2.x * 2;
        laggedXRot += (targetXRot - laggedXRot) * lag;

        this.setXRot(laggedXRot);
        this.setYRot(newYRot);
        this.yRotO = this.yBodyRot = this.yHeadRot = newYRot;

        if (this.level().isClientSide()) {
            tickSubmarineMovement(player);
        }
        player.resetFallDistance();
    }

    @OnlyIn(Dist.CLIENT)
    private void tickSubmarineMovement(Player player) {
        boolean forwardKey = Minecraft.getInstance().options.keyUp.isDown();
        boolean backKey = Minecraft.getInstance().options.keyDown.isDown();
        boolean rightKey = Minecraft.getInstance().options.keyRight.isDown();
        boolean leftKey = Minecraft.getInstance().options.keyLeft.isDown();
        boolean upKey = Minecraft.getInstance().options.keyJump.isDown();
        boolean anyKey = forwardKey || backKey || rightKey || leftKey || upKey;

        float step = getAccelerationMultiplier();
        float brakeStep = step * getBrakingMultiplier();
        double base = getSubmarineBaseSpeed();
        Vec3 move = Vec3.ZERO;

        // Axe longitudinal : avant (+) / arrière (-)
        // Si la touche s'oppose à la direction actuelle → freinage fort (brakeStep)
        if (forwardKey && !backKey) {
            float s = (longitudinalAccel < 0) ? brakeStep : step;
            longitudinalAccel = Math.min(100.0f, longitudinalAccel + s);
        } else if (backKey && !forwardKey) {
            float s = (longitudinalAccel > 0) ? brakeStep : step;
            longitudinalAccel = Math.max(-100.0f, longitudinalAccel - s);
        } else if (longitudinalAccel > 0) {
            longitudinalAccel = Math.max(0.0f, longitudinalAccel - step);
        } else if (longitudinalAccel < 0) {
            longitudinalAccel = Math.min(0.0f, longitudinalAccel + step);
        }

        if (longitudinalAccel != 0) {
            float norm = Math.abs(longitudinalAccel) / 100.0f;
            float smooth = 1.0f - (1.0f - norm) * (1.0f - norm);
            float speed = (float) (base * (0.1 + smooth * 0.9));
            if (longitudinalAccel < 0) speed *= getBackwardSpeedMultiplier();
            move = move.add(this.getViewVector(1.0f).scale(Math.signum(longitudinalAccel) * speed));
        }

        // Axe latéral : droite (+) / gauche (-)
        if (rightKey && !leftKey) {
            float s = (lateralAccel < 0) ? brakeStep : step;
            lateralAccel = Math.min(100.0f, lateralAccel + s);
        } else if (leftKey && !rightKey) {
            float s = (lateralAccel > 0) ? brakeStep : step;
            lateralAccel = Math.max(-100.0f, lateralAccel - s);
        } else if (lateralAccel > 0) {
            lateralAccel = Math.max(0.0f, lateralAccel - step);
        } else if (lateralAccel < 0) {
            lateralAccel = Math.min(0.0f, lateralAccel + step);
        }

        if (lateralAccel != 0) {
            float norm = Math.abs(lateralAccel) / 100.0f;
            float smooth = 1.0f - (1.0f - norm) * (1.0f - norm);
            float angle = this.getYRot() + 90.0f; // vecteur pointant à droite
            Vec3 rightDir = new Vec3(-Mth.sin(angle * Mth.PI / 180.0f), 0, Mth.cos(angle * Mth.PI / 180.0f));
            move = move.add(rightDir.scale(Math.signum(lateralAccel) * getSideSpeedMultiplier() * base * (0.1 + smooth * 0.9)));
        }

        // Axe vertical : haut uniquement
        if (upKey) {
            verticalAccel = Math.min(100.0f, verticalAccel + step);
        } else if (verticalAccel > 0) {
            verticalAccel = Math.max(0.0f, verticalAccel - step);
        }

        if (verticalAccel > 0) {
            float norm = verticalAccel / 150.0f;
            float smooth = 1.0f - (1.0f - norm) * (1.0f - norm);
            move = move.add(new Vec3(0, getUpSpeed() * (0.1 + smooth * 0.9), 0));
        }

        if (this.isAlive() && this.isInWater()) {
            this.setDeltaMovement(move);
        }

        if (anyKey && this.isInWater() && player == Minecraft.getInstance().player) {
            this.spawnBubbleParticles();
            if (!this.isPlayingMoveSound) {
                SimpleSoundInstance sound = new SimpleSoundInstance(
                        OWSounds.SUBMARINE_MOVE_LOOP.get().getLocation(),
                        SoundSource.BLOCKS, 1.0f, 1.25f,
                        SoundInstance.createUnseededRandom(),
                        true, 0,
                        SoundInstance.Attenuation.NONE,
                        player.getX(), player.getY(), player.getZ(),
                        true
                );
                Minecraft.getInstance().getSoundManager().play(sound);
                this.isPlayingMoveSound = true;
            }
        } else {
            Minecraft.getInstance().getSoundManager().stop(OWSounds.SUBMARINE_MOVE_LOOP.get().getLocation(), null);
            this.isPlayingMoveSound = false;
            this.soundTimer = 0;
        }
    }

    @Override
    public boolean addEffect(MobEffectInstance mobEffectInstance, @Nullable Entity entity) {
        if (mobEffectInstance.getEffect() == MobEffects.GLOWING) {
            return super.addEffect(mobEffectInstance, entity);
        }

        return false;
    }

    public void setEnergy(float getEnergy) {
        this.entityData.set(ENERGY, getEnergy);
    }

    public float getEnergy() {
        return this.entityData.get(ENERGY);
    }

    @Override
    public void die(DamageSource damageSource) {
        super.die(damageSource);
        /*if (this.level().isClientSide) {
            Minecraft.getInstance().getSoundManager().stop(OWSounds.SUBMARINE_MOVE_LOOP.get().getLocation(), null);
        }*/
        // /!\ SERVER DON'T WORK
        this.isPlayingMoveSound = false;
        this.soundTimer = 0;
    }

    @Override
    public void tick() {
        super.tick();

        if (isOff()) {
            if (!canDesactivateLight) {
                setLightOn(false);
                canDesactivateLight = true;
            }
        } else canDesactivateLight = false;

        this.setSaddle(!isOff());

        setNoGravity(isInWater());

        if (!isOff()) checkPlayer02(5);

        if (this.level().getRawBrightness(this.blockPosition(), 0) > 7) {
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
