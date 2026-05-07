package net.tiew.operationWild.entity.misc;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.tiew.operationWild.event.ClientEvents;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.tiew.operationWild.entity.OWEntityUtils;
import org.jetbrains.annotations.Nullable;
import net.tiew.operationWild.entity.variants.SeaBugVariant;
import net.tiew.operationWild.item.OWItems;

import java.util.List;

public class SeaBugEntity extends Submarine implements OWEntityUtils {

    public static final int MAX_DEPTH = 50;
    public static final int MAX_BATTERY = 2;

    private static final EntityDataAccessor<Integer> DATA_INITIAL_VARIANT = SynchedEntityData.defineId(SeaBugEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> IS_ALIMENTED_BY_BATTERY_1 = SynchedEntityData.defineId(SeaBugEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> IS_ALIMENTED_BY_BATTERY_2 = SynchedEntityData.defineId(SeaBugEntity.class, EntityDataSerializers.BOOLEAN);

    private static final EntityDataAccessor<Boolean> IS_CHEST_AMELIORATED = SynchedEntityData.defineId(SeaBugEntity.class, EntityDataSerializers.BOOLEAN);

    private static final EntityDataAccessor<Float> PITCH = SynchedEntityData.defineId(SeaBugEntity.class, EntityDataSerializers.FLOAT);


    public SeaBugVariant getVariant() {
        return SeaBugVariant.byId(this.getTypeVariant() & 255);
    }

    public void setVariant(SeaBugVariant variant) {
        this.entityData.set(VARIANT, variant.getId() & 255);
    }

    public SeaBugVariant getInitialVariant() {
        return SeaBugVariant.byId(this.entityData.get(DATA_INITIAL_VARIANT));
    }

    public void setInitialVariant(SeaBugVariant variant) {
        this.entityData.set(DATA_INITIAL_VARIANT, variant.getId());
    }

    public SeaBugEntity(EntityType<? extends TamableAnimal> entityType, Level level, float scale, int maxSleepBar, int sleepBarDownSpeed) {
        super(entityType, level, scale, maxSleepBar, sleepBarDownSpeed);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Animal.createLivingAttributes().add(Attributes.MAX_HEALTH, 100.0D).add(Attributes.MOVEMENT_SPEED, 0.2D).add(Attributes.FOLLOW_RANGE, 10.0D).add(Attributes.ATTACK_DAMAGE, 0.0D).add(Attributes.KNOCKBACK_RESISTANCE, 0.5D);
    }

    // Entity's AI
    protected void registerGoals() {
    }

    @Override
    protected void playStepSound(BlockPos p_20135_, BlockState p_20136_) {

    }

    @Override
    public void die(DamageSource damageSource) {
        super.die(damageSource);

        if (isAlimentedByBattery1()) {
            ItemStack battery1 = getItemInSlot(0);
            if (!battery1.isEmpty()) {
                this.spawnAtLocation(battery1);
            }
        }
        if (isAlimentedByBattery2()) {
            ItemStack battery2 = getItemInSlot(1);
            if (!battery2.isEmpty()) {
                this.spawnAtLocation(battery2);
            }
        }
    }

    public void setAlimentedByBattery1(boolean isAlimentedByBattery1) {
        this.entityData.set(IS_ALIMENTED_BY_BATTERY_1, isAlimentedByBattery1);
    }

    public boolean isAlimentedByBattery1() {
        return this.entityData.get(IS_ALIMENTED_BY_BATTERY_1);
    }

    public void setAlimentedByBattery2(boolean isAlimentedByBattery2) {
        this.entityData.set(IS_ALIMENTED_BY_BATTERY_2, isAlimentedByBattery2);
    }

    public boolean isAlimentedByBattery2() {
        return this.entityData.get(IS_ALIMENTED_BY_BATTERY_2);
    }

    public void setChestAmelioration(boolean isChestAmeliorated) {
        this.entityData.set(IS_CHEST_AMELIORATED, isChestAmeliorated);
    }

    public boolean isChestAmeliorated() {
        return this.entityData.get(IS_CHEST_AMELIORATED);
    }

    public void setLastPlayerPitch(float getLastPlayerPitch) {
        this.entityData.set(PITCH, getLastPlayerPitch);
    }

    public float getLastPlayerPitch() {
        return this.entityData.get(PITCH);
    }

    @Override
    public void setTarget(@Nullable LivingEntity target) {
        super.setTarget(target);
    }

    public void tick() {
        super.tick();

        Player rider = (Player) this.getControllingPassenger();
        int depth = (int) (this.level().getSeaLevel() - this.getY());

        if (this.level().isClientSide()) setupAnimationState();

        showSubmarine(15);

        if (depth >= MAX_DEPTH) {
            if (rider != null) {
                if (!rider.isCreative()) {
                    applyWaterPressureDamage(depth, rider);
                }
            } else applyWaterPressureDamage(depth, null);

            if (this.level().isClientSide()) {
                ClientEvents.blinkSubmarineShader = rider == null || !rider.isCreative();
            }
        } else {
            damageTimer = 0.0f;
            firstTimeToDeep = true;
            if (this.level().isClientSide()) {
                ClientEvents.blinkSubmarineShader = false;
            }
        }

        if (!this.level().isClientSide()) {
            ItemStack itemInSlot0 = getItemInSlot(0);
            ItemStack itemInSlot1 = getItemInSlot(1);

            if (!itemInSlot0.isEmpty()) {
                Item item = itemInSlot0.getItem();
                if (item == OWItems.BATTERY.get()) {
                    this.setAlimentedByBattery1(true);
                } else this.setAlimentedByBattery1(false);
            } else this.setAlimentedByBattery1(false);

            if (!itemInSlot1.isEmpty()) {
                Item item = itemInSlot1.getItem();
                if (item == OWItems.BATTERY.get()) {
                    this.setAlimentedByBattery2(true);
                } else this.setAlimentedByBattery2(false);
            } else this.setAlimentedByBattery2(false);
        }


        if (!this.level().isClientSide()) {
            ItemStack stack1 = getItemInSlot(0);
            ItemStack stack2 = getItemInSlot(1);

            int maxDurability1 = stack1.getMaxDamage();
            int currentDurability1 = stack1.getDamageValue();
            int durabilityRemaining1 = maxDurability1 - currentDurability1;

            int maxDurability2 = stack2.getMaxDamage();
            int currentDurability2 = stack2.getDamageValue();
            int durabilityRemaining2 = maxDurability2 - currentDurability2;

            setEnergy(Math.max(0, (durabilityRemaining1 / MAX_BATTERY) + (durabilityRemaining2 / MAX_BATTERY)));
            setOff(getEnergy() == 0);
        }

        int interval = isLightOn() ? 600 : 800;

        if (tickCount % interval == 0) {
            boolean consumedPower = false;

            if (isAlimentedByBattery1()) {
                ItemStack itemInSlot0 = getItemInSlot(0);
                if (!itemInSlot0.isEmpty() && itemInSlot0.getDamageValue() < itemInSlot0.getMaxDamage() && (itemInSlot0.getMaxDamage() - itemInSlot0.getDamageValue()) > 0) {
                    itemInSlot0.setDamageValue(itemInSlot0.getDamageValue() + 1);
                    consumedPower = true;
                }
            }

            if (!consumedPower && isAlimentedByBattery2()) {
                ItemStack itemInSlot1 = getItemInSlot(1);
                if (!itemInSlot1.isEmpty() && itemInSlot1.getDamageValue() < itemInSlot1.getMaxDamage() && (itemInSlot1.getMaxDamage() - itemInSlot1.getDamageValue()) > 0) {
                    itemInSlot1.setDamageValue(itemInSlot1.getDamageValue() + 1);
                }
            }
        }
    }

    public ItemStack getItemInSlot(int slot) {
        if (this.itemStackHandlerSeaBug != null && slot < this.itemStackHandlerSeaBug.getSlots()) {
            return this.itemStackHandlerSeaBug.getStackInSlot(slot);
        }
        return ItemStack.EMPTY;
    }

    @Override
    public boolean doHurtTarget(Entity entity) {
        return super.doHurtTarget(entity);
    }

    @Override
    protected void positionRider(Entity entity, MoveFunction function) {
        super.positionRider(entity, function);
        function.accept(entity, entity.getX(), entity.getY() - 1.4f, entity.getZ());
    }

    @Override
    public boolean killedEntity(ServerLevel serverLevel, LivingEntity entity) {
        return super.killedEntity(serverLevel, entity);
    }

    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor levelAccessor, DifficultyInstance difficultyInstance, MobSpawnType mobSpawnType, @Nullable SpawnGroupData spawnGroupData) {
        this.setVariant(SeaBugVariant.DEFAULT);
        this.setInitialVariant(this.getVariant());
        this.setSaddle(true);
        return super.finalizeSpawn(levelAccessor, difficultyInstance, mobSpawnType, spawnGroupData);
    }

    @Override
    protected float getRotationLag() {
        return 0.1f;
    }

    @Override
    protected double getSubmarineBaseSpeed() {
        return 0.5;
    }

    @Override
    protected float getSideSpeedMultiplier() {
        return 0.75f;
    }

    @Override
    protected float getBackwardSpeedMultiplier() {
        return 0.75f;
    }

    @Override
    protected double getUpSpeed() {
        return 0.4;
    }

    @Override
    protected float getAccelerationMultiplier() {
        return 1.5f;
    }

    /**
     * Distance horizontale de chaque phare par rapport au centre de l'écran.
     * Les deux spots sont placés à (0.5 - valeur) et (0.5 + valeur).
     * - Plus élevé → phares plus écartés (ex. 0.20 = très espacés)
     * - Plus faible → phares rapprochés du centre (ex. 0.05 = quasi fusionnés)
     * - Plage recommandée : 0.05 – 0.30
     */
    @Override
    public float getShaderLightSeparation() {
        return 0.12f;
    }

    /**
     * Position verticale des deux phares sur l'écran.
     * 0.0 = haut de l'écran, 1.0 = bas de l'écran, 0.5 = centre exact.
     * - Plus élevé → phares plus bas sur l'écran
     * - Plus faible → phares plus hauts sur l'écran
     * - Plage recommandée : 0.40 – 0.65
     */
    @Override
    public float getShaderLightY() {
        return 0.510f;
    }

    /**
     * Rayon du spot gaussien — contrôle la taille physique de chaque cercle lumineux.
     * La formule est exp(-distance² × valeur), donc plus la valeur est BASSE, plus le spot est GRAND.
     * - Plus élevé → spot petit et concentré (ex. 30.0 = petit point net)
     * - Plus faible → spot large et diffus (ex. 8.0 = grand halo)
     * - Plage recommandée : 8.0 – 30.0
     */
    @Override
    public float getShaderSpotRadius() {
        return 15.0f;
    }

    /**
     * Exposant de contraste appliqué après la somme des deux spots.
     * Contrôle la dureté du bord entre la zone éclairée et la zone sombre.
     * - Plus élevé → bord très net, zone sombre tranchée (ex. 6.0 = quasi découpe)
     * - Plus faible → transition douce et progressive (ex. 1.5 = dégradé large)
     * - Plage recommandée : 1.5 – 7.0
     */
    @Override
    public float getShaderContrastPow() {
        return 3.8f;
    }

    /**
     * Intensité de la composante additive : lumière plate ajoutée sur les blocs éclairés,
     * indépendamment de leur couleur de base. Produit un halo visible même sur les blocs noirs.
     * - Plus élevé → halo plus lumineux et coloré dans le faisceau (ex. 1.0 = très intense)
     * - Plus faible → halo discret, la couleur du bloc domine (ex. 0.1 = quasi invisible)
     * - Plage recommandée : 0.1 – 1.0
     */
    @Override
    public float getShaderAdditiveStrength() {
        return 0.35f;
    }

    /**
     * Facteur multiplicatif appliqué à la couleur originale des blocs dans le faisceau.
     * Amplifie la couleur naturelle du bloc, créant un contraste fort avec les zones non éclairées.
     * - Plus élevé → blocs éclairés très brillants, fort contraste (ex. 4.0 = surexposé)
     * - Plus faible → effet subtil, blocs à peine plus lumineux (ex. 0.5 = léger)
     * - 1.0 = aucun effet multiplicatif
     * - Plage recommandée : 0.5 – 4.0
     */
    @Override
    public float getShaderMultiplicativeStrength() {
        return 2.0f;
    }

    /**
     * Facteur d'assombrissement des zones hors faisceau, pour augmenter le contraste
     * entre les blocs éclairés et le reste de la scène sans toucher à la luminosité du faisceau.
     * - Plus élevé → zones sombres très assombries, contraste maximal (ex. 0.9 = quasi noir)
     * - Plus faible → peu d'effet, scène peu modifiée (ex. 0.1 = léger voile)
     * - 0.0 = aucun assombrissement (comportement neutre)
     * - Plage recommandée : 0.0 – 0.8
     */
    @Override
    public float getShaderShadowFactor() {
        return 0.50f;
    }

    /**
     * Couleur RGB du faisceau lumineux, exprimée en code hexadécimal.
     * Utilisez la méthode {@link #hexToRGB(String)} avec un code couleur HTML (ex. {@code "#C7EBFF"}).
     * - Bleu froid → {@code "#C7EBFF"} (couleur actuelle du SeaBug)
     * - Blanc neutre → {@code "#FFFFFF"}
     * - Jaune chaud → {@code "#FFE87C"}
     * - Vert bioluminescent → {@code "#7DFFC0"}
     */
    @Override
    public float[] getShaderLightColor() {
        return hexToRGB("#7effd0");
    }

    /**
     * Détermine si le sous-marin utilise un seul faisceau centré ou deux faisceaux écartés.
     * En mode faisceau unique, les deux spots fusionnent automatiquement au centre de l'écran
     * (position X = 0.5), indépendamment de la valeur de {@link #getShaderLightSeparation()}.
     * - true  → un seul phare central (ex. lampe frontale, projecteur unique)
     * - false → deux phares écartés symétriquement (comportement par défaut du SeaBug)
     */
    @Override
    public boolean isSingleBeam() {
        return false;
    }

    /**
     * Couleur de remplissage du losange waypoint du SeaBug.
     * - Plus élevé (canal rouge/vert) → losange plus chaud visuellement
     * - Plus faible → bleu froid, cohérent avec l'univers marin du SeaBug
     * - Valeur actuelle : {@code 0x1A8FFF} (bleu cobalt)
     */
    @Override
    public int getWaypointFillColor() { return 0x521aff; }

    /**
     * Couleur de la bordure du losange waypoint du SeaBug.
     * - Plus saturé → contour plus net et brillant (ex. {@code 0x00FFFF} = cyan)
     * - Plus désaturé → fondu dans l'interface (valeur actuelle : bleu très clair)
     * - Valeur actuelle : {@code 0xAADDFF}
     */
    @Override
    public int getWaypointBorderColor() { return 0xb3aaff; }

    /**
     * Couleur du texte de distance et du nom affiché sous/au-dessus du losange.
     * - Blanc → {@code 0xFFFFFF} : bonne lisibilité sur fond sombre (valeur actuelle)
     * - Assorti à la bordure ({@code 0xAADDFF}) → plus cohérent mais moins lisible
     */
    @Override
    public int getWaypointTextColor() { return 0xFFFFFF; }

    /**
     * Demi-taille de base du losange SeaBug en pixels GUI.
     * - Plus élevé → icône plus grande (ex. 10 = très visible)
     * - Plus faible → plus discret (ex. 5 = minimaliste)
     * - Valeur actuelle : {@code 7}
     */
    @Override
    public int getWaypointIconSize() { return 5; }

    /**
     * Distance maximale d'affichage du waypoint SeaBug, en blocs.
     * - Plus élevé → visible très loin (ex. 1000)
     * - Plus faible → waypoint limité à une zone proche (ex. 100)
     * - Valeur actuelle : {@code 500}
     */
    @Override
    public int getWaypointMaxDistance() { return 500; }

    /**
     * Distance minimale d'activation du waypoint SeaBug, en blocs.
     * En dessous de ce seuil le losange disparaît progressivement (joueur à bord ou tout proche).
     * - Plus élevé → se masque plus tôt à l'approche (ex. 20)
     * - Plus faible → reste visible quasiment au contact (ex. 2)
     * - Valeur actuelle : {@code 7.0}
     */
    @Override
    public float getWaypointMinDistance() { return 7.0f; }

    /**
     * Nom affiché sur le waypoint HUD du SeaBug.
     * - Valeur actuelle : {@code "Sea Bug"}
     */
    @Override
    public String getWaypointName() { return "SeaBug"; }

    /**
     * Opacité minimale du waypoint SeaBug quand le joueur ne regarde pas dessus.
     * - {@code 0.0} → invisible, {@code 1.0} → toujours plein
     * - Valeur actuelle : {@code 0.16}
     */
    @Override
    public float getWaypointMinOpacity() { return 0.25f; }

    /**
     * Facteur d'échelle de la police pour le texte de distance du SeaBug (ex. {@code "80 m"}).
     * - Plus élevé → texte plus grand, plus lisible de loin
     * - Valeur actuelle : {@code 1.0} (taille native)
     */
    @Override
    public float getWaypointDistanceFontScale() { return 0.75f; }

    private void setupAnimationState() {
    }

    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_INITIAL_VARIANT, -1);
        builder.define(IS_ALIMENTED_BY_BATTERY_1, false);
        builder.define(IS_ALIMENTED_BY_BATTERY_2, false);

        builder.define(IS_CHEST_AMELIORATED, false);

        builder.define(PITCH, 0.0f);
    }

    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("getInitialVariant", this.getInitialVariant().getId());
        tag.putInt("Variant", this.getTypeVariant());
        tag.putBoolean("isAlimentedByBattery1", this.isAlimentedByBattery1());
        tag.putBoolean("isAlimentedByBattery2", this.isAlimentedByBattery2());

        tag.putBoolean("isChestAmeliorated", this.isChestAmeliorated());

        tag.putFloat("getLastPlayerPitch", this.getLastPlayerPitch());
    }

    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.entityData.set(DATA_INITIAL_VARIANT, tag.getInt("getInitialVariant"));
        this.entityData.set(VARIANT, tag.getInt("Variant"));
        this.entityData.set(IS_ALIMENTED_BY_BATTERY_1, tag.getBoolean("isAlimentedByBattery1"));
        this.entityData.set(IS_ALIMENTED_BY_BATTERY_2, tag.getBoolean("isAlimentedByBattery2"));

        this.entityData.set(IS_CHEST_AMELIORATED, tag.getBoolean("isChestAmeliorated"));

        this.entityData.set(PITCH, tag.getFloat("getLastPlayerPitch"));
    }

    @Override
    public int getEntityColor() {
        return 14212580;
    }

    @Override
    public float getEntityScale() {
        return 0;
    }
}

