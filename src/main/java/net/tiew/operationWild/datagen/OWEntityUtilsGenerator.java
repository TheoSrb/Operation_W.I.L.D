package net.tiew.operationWild.datagen;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.data.event.GatherDataEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.tiew.operationWild.OperationWild;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@EventBusSubscriber(modid = OperationWild.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
public class OWEntityUtilsGenerator implements DataProvider {

    @SubscribeEvent
    public static void onGatherData(GatherDataEvent event) {
        event.getGenerator().addProvider(event.includeClient(), new OWEntityUtilsGenerator());
    }

    @Override
    public CompletableFuture<?> run(CachedOutput cachedOutput) {

        /*createEntity("Mandrill", MobCategory.CREATURE,
                20f, 0.18f, 35f, 4f, 0.3f,
                0x3b3734, 1.1f, 7000, 5, 2.5f, 3f,
                "Mandrill", false);*/


        return CompletableFuture.completedFuture(null);
    }

    public void createEntity(String newEntity, MobCategory mobCategory, float maxHealth, float maxSpeed, float followRange, float maxDamages, float knockbackResistance, int color, float scale, int maxSleepBar, int sleepBarDownSpeed, float width, float height, String frTranslation, boolean createSkins) {
        newEntity = newEntity.replace(" ", "_");
        generateEntityRegistry(newEntity, scale, maxSleepBar, sleepBarDownSpeed, mobCategory, width, height);
        generateEntityRenderer(newEntity);
        generateEntityVariants(newEntity);
        addEntityVariants(newEntity);
        generateEntityModel(newEntity);
        generateEntityAnimations(newEntity);
        generateEntityMainClass(newEntity, mobCategory, maxHealth, maxSpeed, followRange, maxDamages, knockbackResistance, color);
        generateEntityTranslations(newEntity, frTranslation);
        generateEntityEvents(newEntity);
        generateEntitySetup(newEntity);
        generateEntityLayer(newEntity);
        if (createSkins) {
            generateEntitySkins(newEntity);
            generateEntitySkinsLayer(newEntity);
        }
    }

    @Override
    public String getName() {
        return "OW Entity Utils Generator";
    }

    public void generateEntityMainClass(String entityType, MobCategory mobCategory, float maxHealth, float speed, float followRange, float attackDamages, float knockbackResistance, int color) {
        String entityName = entityType;

        String path = "C:\\Users\\Tiew_37\\IdeaProjects\\Operation_W.I.L.D\\src\\main\\java\\net\\tiew\\operationWild\\entity\\custom\\" + (mobCategory.equals(MobCategory.MISC) ? "misc" : "living" + "\\");
        String fileType = ".java";
        String fileName = entityName + "Entity" + fileType;
        File file = new File(path + fileName);

        if (!file.exists()) {
            List<String> lines = List.of(
                    "package org.operationWild.entity.custom.living;\n" +
                            "\n" +
                            "import net.minecraft.core.BlockPos;\n" +
                            "import net.minecraft.core.Direction;\n" +
                            "import net.minecraft.core.particles.ParticleTypes;\n" +
                            "import net.minecraft.nbt.CompoundTag;\n" +
                            "import net.minecraft.network.chat.Component;\n" +
                            "import net.minecraft.network.syncher.EntityDataAccessor;\n" +
                            "import net.minecraft.network.syncher.EntityDataSerializers;\n" +
                            "import net.minecraft.network.syncher.SynchedEntityData;\n" +
                            "import net.minecraft.server.level.ServerLevel;\n" +
                            "import net.minecraft.sounds.SoundEvent;\n" +
                            "import net.minecraft.sounds.SoundEvents;\n" +
                            "import net.minecraft.world.DifficultyInstance;\n" +
                            "import net.minecraft.world.InteractionHand;\n" +
                            "import net.minecraft.world.InteractionResult;\n" +
                            "import net.minecraft.world.damagesource.DamageSource;\n" +
                            "import net.minecraft.world.entity.*;\n" +
                            "import net.minecraft.world.entity.ai.attributes.AttributeSupplier;\n" +
                            "import net.minecraft.world.entity.ai.attributes.Attributes;\n" +
                            "import net.minecraft.world.entity.ai.goal.FloatGoal;\n" +
                            "import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;\n" +
                            "import net.minecraft.world.entity.animal.Animal;\n" +
                            "import net.minecraft.world.entity.player.Player;\n" +
                            "import net.minecraft.world.item.Item;\n" +
                            "import net.minecraft.world.item.ItemStack;\n" +
                            "import net.minecraft.world.item.context.UseOnContext;\n" +
                            "import net.minecraft.world.level.Level;\n" +
                            "import net.minecraft.world.level.ServerLevelAccessor;\n" +
                            "import net.minecraft.world.level.block.state.BlockState;\n" +
                            "import net.minecraft.world.phys.BlockHitResult;\n" +
                            "import net.minecraft.world.phys.Vec3;\n" +
                            "import net.minecraftforge.event.ForgeEventFactory;\n" +
                            "import org.jetbrains.annotations.Nullable;\n" +
                            "import org.operationWild.entity.AI.OWFollowOwnerGoal;\n" +
                            "import org.operationWild.entity.AI.OWPanicGoal;\n" +
                            "import org.operationWild.entity.AI.OWRandomLookAroundGoal;\n" +
                            "import org.operationWild.entity.OWEntity;\n" +
                            "import org.operationWild.entity.OWEntityUtils;\n" +
                            "import org.operationWild.entity.variants." + entityName + "Variant;\n" +
                            "import org.operationWild.item.OWItems;\n" +
                            "import org.operationWild.item.custom.AnimalSoulItem;\n" +
                            "import org.operationWild.utils.OWUtils;\n" +
                            "\n" +
                            "import static org.operationWild.utils.OWUtils.RANDOM;\n" +
                            "\n" +
                            "public class " + entityName + "Entity" + " extends OWEntity implements OWEntityUtils {\n" +
                            "\n" +
                            "    public String[] quests = {};\n" +
                            "    public int foodGiven = 0;\n" +
                            "    public int foodWanted;\n" +
                            "\n" +
                            "    private static final EntityDataAccessor<Integer> DATA_INITIAL_VARIANT = SynchedEntityData.defineId(" + entityName + "Entity" + ".class, EntityDataSerializers.INT);\n" +
                            "\n" +
                            "    public " + entityName + "Variant getVariant() { return " + entityName + "Variant.byId(this.getTypeVariant() & 255);}\n" +
                            "    public void setVariant(" + entityName + "Variant variant) { this.entityData.set(VARIANT, variant.getId() & 255);}\n" +
                            "    public " + entityName + "Variant getInitialVariant() { return " + entityName + "Variant.byId(this.entityData.get(DATA_INITIAL_VARIANT));}\n" +
                            "    public void setInitialVariant(" + entityName + "Variant variant) { this.entityData.set(DATA_INITIAL_VARIANT, variant.getId());}\n" +
                            "\n" +
                            "    public " + entityName + "Entity" + "(EntityType<? extends TamableAnimal> entityType, Level level, float scale, int maxSleepBar, int sleepBarDownSpeed) {\n" +
                            "        super(entityType, level, scale, maxSleepBar, sleepBarDownSpeed);\n" +
                            "    }\n" +
                            "\n" +
                            "\n" +
                            "    // Entity's AI\n" +
                            "    protected void registerGoals() {\n" +
                            "        this.goalSelector.addGoal(0, new FloatGoal(this));\n" +
                            "        this.goalSelector.addGoal(7, new OWRandomLookAroundGoal(this));\n" +
                            "        this.goalSelector.addGoal(2, new OWFollowOwnerGoal(this, this.getSpeed() * 20f, 15, 3));\n" +
                            "        this.goalSelector.addGoal(1, new OWPanicGoal(this, this.getSpeed() * 16f, 3));\n" +
                            "        this.goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 0.8));\n" +
                            "    }\n" +
                            "\n" +
                            "    public static AttributeSupplier.Builder createAttributes() {\n" +
                            "        return Animal.createLivingAttributes().add(Attributes.MAX_HEALTH, " + maxHealth + "D).add(Attributes.MOVEMENT_SPEED, " + speed + "D).add(Attributes.FOLLOW_RANGE, " + followRange + "D).add(Attributes.ATTACK_DAMAGE, " + attackDamages + "D).add(Attributes.KNOCKBACK_RESISTANCE, " + knockbackResistance + "D);\n" +
                            "    }\n" +
                            "\n" +
                            "    protected @Nullable SoundEvent getAmbientSound() {\n" +
                            "        return RANDOM(5) ? null : null;\n" +
                            "    }\n" +
                            "\n" +
                            "    protected float getSoundVolume() { return 1f;}\n" +
                            "\n" +
                            "    @Override\n" +
                            "    protected void playStepSound(BlockPos blockPos, BlockState blockState) {\n" +
                            "        super.playStepSound(blockPos, blockState);\n" +
                            "    }\n" +
                            "\n" +
                            "    public void setBuyingSkin(int skinIndex) {\n" +
                            "        switch (skinIndex) {\n" +
                            "            default -> throw new IllegalArgumentException(\"Invalid skin index: \" + skinIndex);\n" +
                            "        }\n" +
                            "    }\n" +
                            "    \n" +
                            "    @Override\n" +
                            "    public void travel(Vec3 vec3) {\n" +
                            "        super.travel(vec3);\n" +
                            "        //if (this.onGround() && this.horizontalCollision && !isSleeping() && !isNapping() && !this.isVehicle()) this.jumpFromGround();\n" +
                            "    }\n" +
                            "\n" +
                            "    @Override\n" +
                            "    public void die(DamageSource damageSource) {\n" +
                            "        super.die(damageSource);\n" +
                            "        ItemStack soulStack = new ItemStack(OWItems.ANIMAL_SOUL.get());\n" +
                            "\n" +
                            "        Item item = soulStack.getItem();\n" +
                            "        if (item instanceof AnimalSoulItem animalSoulItem) {\n" +
                            "            UseOnContext fakeContext = new UseOnContext(this.level(), null, InteractionHand.MAIN_HAND, soulStack, new BlockHitResult(this.position(), Direction.UP, this.blockPosition(), false));\n" +
                            "\n" +
                            "            animalSoulItem.saveEntityType(fakeContext, Component.nullToEmpty(this.getClass().getSimpleName()));\n" +
                            "            animalSoulItem.saveEntityOwner(fakeContext, Component.nullToEmpty(this.getOwner() != null ? this.getOwner().getName().getString() : \"\"));\n" +
                            "            animalSoulItem.saveEntityGender(fakeContext, this.isMale());\n" +
                            "            animalSoulItem.saveEntityMaxHealth(fakeContext, this.getMaxHealth());\n" +
                            "            animalSoulItem.saveEntityDamages(fakeContext, this.getDamage());\n" +
                            "            animalSoulItem.saveEntitySpeed(fakeContext, this.getSpeed());\n" +
                            "            animalSoulItem.saveEntityScale(fakeContext, this.getScale());\n" +
                            "            animalSoulItem.saveEntityLevel(fakeContext, this.getLevel());\n" +
                            "            animalSoulItem.saveEntityVariant(fakeContext, this.getVariant().getId());\n" +
                            "        }\n" +
                            "\n" +
                            "        if (canDropSoul() && this.isTame() && !this.isInResurrection() && !isBaby()) {\n" +
                            "            this.spawnAtLocation(soulStack);\n" +
                            "        }\n" +
                            "        /*if (this.isSaddled()) this.spawnAtLocation(OWItems." + entityName.toUpperCase() + "_SADDLE.get());*/\n" +
                            "    }\n" +
                            "\n" +
                            "    @Override\n" +
                            "    public void setTarget(@Nullable LivingEntity target) {\n" +
                            "        super.setTarget(target);\n" +
                            "    }\n" +
                            "\n" +
                            "\n" +
                            "    public void changeSkin(int skinIndex) {\n" +
                            "        this.setVariant(getInitialVariant());\n" +
                            "        \n" +
                            "        if (!this.level().isClientSide()) {\n" +
                            "            Level world = this.level();\n" +
                            "            if (world instanceof ServerLevel) {\n" +
                            "                ServerLevel serverWorld = (ServerLevel) world;\n" +
                            "                serverWorld.sendParticles(ParticleTypes.ITEM_SLIME,\n" +
                            "                        this.getX(), this.getY() + 1, this.getZ(),\n" +
                            "                        100,\n" +
                            "                        0.5, 0.5, 0.5,\n" +
                            "                        0.02\n" +
                            "                );\n" +
                            "            }\n" +
                            "        }\n" +
                            "    }\n" +
                            "\n" +
                            "    public void tick() {\n" +
                            "        super.tick();\n" +
                            "        setTamingPercentage(this.foodGiven, this.foodWanted);\n" +
                            "        if (this.level().isClientSide()) setupAnimationState();\n" +
                            "        if (this.isInResurrection()) this.setSleeping(true);\n" +
                            "        \n" +
                            "        \n" +
                            "        \n" +
                            "        \n" +
                            "        \n" +
                            "        \n" +
                            "\n" +
                            "        /*if (this.getVariant() == " + entityName + "Variant.SKIN_GOLD && this.tickCount % 150 == 0) {\n" +
                            "            OWUtils.spawnParticles(this, ParticleTypes.END_ROD, 0, 0, 0, 5, 2);\n" +
                            "        }*/\n" +
                            "    }\n" +
                            "\n" +
                            "    @Override\n" +
                            "    public boolean doHurtTarget(Entity entity) {\n" +
                            "        return super.doHurtTarget(entity);\n" +
                            "    }\n" +
                            "\n" +
                            "    @Override\n" +
                            "    public boolean hurt(DamageSource damageSource, float v) {\n" +
                            "        return super.hurt(damageSource, v);\n" +
                            "    }\n" +
                            "\n" +
                            "    @Override\n" +
                            "    protected void positionRider(Entity entity, MoveFunction function) {\n" +
                            "        super.positionRider(entity, function);\n" +
                            "        function.accept(entity, entity.getX(), entity.getY() - 1, entity.getZ());\n" +
                            "    }\n" +
                            "\n" +
                            "    @Override\n" +
                            "    public boolean killedEntity(ServerLevel serverLevel, LivingEntity entity) {\n" +
                            "        return super.killedEntity(serverLevel, entity);\n" +
                            "    }\n" +
                            "\n" +
                            "    @Override\n" +
                            "    public boolean isAlliedTo(Entity entity) {\n" +
                            "        if (entity instanceof " + entityName + "Entity" + " other" + entityName + ") {\n" +
                            "            if (this.isTame()) return other" + entityName + ".isTame() && this.getOwnerUUID() != null && this.getOwnerUUID().equals(other" + entityName + ".getOwnerUUID());\n" +
                            "            else return !other" + entityName + ".isTame();\n" +
                            "        }\n" +
                            "        return super.isAlliedTo(entity);\n" +
                            "    }\n" +
                            "\n" +
                            "    @Override\n" +
                            "    public InteractionResult mobInteract(Player player, InteractionHand hand) {\n" +
                            "        ItemStack itemStack = player.getItemInHand(hand);\n" +
                            "\n" +
                            "        if (/*itemStack.is(OWItems.SAVAGE_BERRIES.get()) &&*/ !this.isTame() && this.isBaby()) {\n" +
                            "            foodGiven++;\n" +
                            "            this.playSound(SoundEvents.CAMEL_EAT);\n" +
                            "            itemStack.shrink(1);\n" +
                            "\n" +
                            "            if (!ForgeEventFactory.onAnimalTame(this, player)) {\n" +
                            "                if (!this.level().isClientSide() && foodGiven >= foodWanted) {\n" +
                            "                    this.setTame(true, player);\n" +
                            "                    this.setSleeping(false);\n" +
                            "                    resetSleepBar();\n" +
                            "                }\n" +
                            "            }\n" +
                            "            return InteractionResult.SUCCESS;\n" +
                            "        }\n" +
                            "\n" +
                            "        if (itemStack.is(OWItems.SAVAGE_BERRIES.get())) return InteractionResult.PASS;\n" +
                            "        return super.mobInteract(player, hand);\n" +
                            "    }\n" +
                            "\n" +
                            "    @Override\n" +
                            "    public SpawnGroupData finalizeSpawn(ServerLevelAccessor levelAccessor, DifficultyInstance difficultyInstance, MobSpawnType mobSpawnType, @Nullable SpawnGroupData spawnGroupData) {\n" +
                            "        if (mobSpawnType != MobSpawnType.BREEDING) {\n" +
                            "            this.setRandomAttributes(this, this.getAttributeBaseValue(Attributes.MAX_HEALTH), this.getAttributeBaseValue(Attributes.ATTACK_DAMAGE), this.getAttributeBaseValue(Attributes.MOVEMENT_SPEED));\n" +
                            "            this.setBaseHealth((float) this.getAttributeBaseValue(Attributes.MAX_HEALTH) * 1.3f);\n" +
                            "            this.setBaseDamage((float) this.getAttributeBaseValue(Attributes.ATTACK_DAMAGE));\n" +
                            "            this.setBaseSpeed((float) this.getAttributeBaseValue(Attributes.MOVEMENT_SPEED));\n" +
                            "\n" +
                            "\n" +
                            "            this.setVariant(" + entityName + "Variant.DEFAULT);\n" +
                            "            this.setInitialVariant(this.getVariant());\n" +
                            "        }\n" +
                            "\n" +
                            "        return super.finalizeSpawn(levelAccessor, difficultyInstance, mobSpawnType, spawnGroupData);\n" +
                            "    }\n" +
                            "\n" +
                            "\n" +
                            "    private void setupAnimationState() {\n" +
                            "        createIdleAnimation(54, true);\n" +
                            "        createSitAnimation(80, true);\n" +
                            "    }\n" +
                            "\n" +
                            "    protected void defineSynchedData(SynchedEntityData.Builder builder) {\n" +
                            "        super.defineSynchedData(builder);\n" +
                            "        builder.define(DATA_INITIAL_VARIANT, -1);\n" +
                            "    }\n" +
                            "\n" +
                            "    public void addAdditionalSaveData(CompoundTag tag) {\n" +
                            "        super.addAdditionalSaveData(tag);\n" +
                            "        tag.putInt(\"getInitialVariant\", this.getInitialVariant().getId());\n" +
                            "        tag.putInt(\"Variant\", this.getTypeVariant());\n" +
                            "        tag.putInt(\"numberFeedsGiven\", this.numberFeedsGiven);\n" +
                            "        tag.putInt(\"numberFeedsGiven\", this.numberFeedsGiven);\n" +
                            "\n" +
                            "    }\n" +
                            "\n" +
                            "    public void readAdditionalSaveData(CompoundTag tag) {\n" +
                            "        super.readAdditionalSaveData(tag);\n" +
                            "        this.entityData.set(DATA_INITIAL_VARIANT, tag.getInt(\"getInitialVariant\"));\n" +
                            "        this.entityData.set(VARIANT, tag.getInt(\"Variant\"));\n" +
                            "        this.numberFeedsGiven = tag.getInt(\"numberFeedsGiven\");\n" +
                            "        this.numberFeedsGiven = tag.getInt(\"numberFeedsGiven\");\n" +
                            "    }\n" +
                            "\n" +
                            "    @Override\n" +
                            "    public int getEntityColor() {\n" +
                            "        return " + color + ";\n" +
                            "    }\n" +
                            "}\n"
            );

            if (!file.exists()) {
                try {
                    file.createNewFile();
                    FileWriter writer = new FileWriter(file);
                    BufferedWriter buffer = new BufferedWriter(writer);
                    for (String line : lines) {
                        buffer.write(line + "\n");
                    }
                    buffer.close();
                    writer.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } else throw new RuntimeException(entityType + "Main entity is already created.");
    }

    public void addEntityVariants(String entityType) {
        String entityName = entityType;
        File desktop = new File("C:\\Users\\Tiew_37\\Desktop");
        File[] files = desktop.listFiles();

        ArrayList<File> textures = new ArrayList<>();

        if (desktop.exists()) {
            for (File file : files) {
                if (file.getName().endsWith(".png") && file.getName().startsWith(entityName.toLowerCase())) {
                    textures.add(file);
                }
            }
        }

        File fileWrite = new File("C:\\Users\\Tiew_37\\IdeaProjects\\Operation_W.I.L.D\\src\\main\\java\\net\\tiew\\operationWild\\entity\\variants\\" + entityName + "Variant.java");

        try {
            List<String> lines = new ArrayList<>();
            BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(fileWrite), "UTF-8"));
            String line;
            while ((line = reader.readLine()) != null) {
                lines.add(line);
            }
            reader.close();

            BufferedWriter writer = new BufferedWriter(new FileWriter(fileWrite));
            int i = 0;
            for (String currentLine : lines) {
                writer.write(currentLine + '\n');

                if (currentLine.contains("public enum")) {
                    for (int j = 0; j < textures.size(); j++) {
                        File texture = textures.get(j);
                        String end = j == textures.size() - 1 ? ";" : ",";
                        String variant = texture.getName().replace(entityName.toLowerCase() + "_", "").replace(".png", "").toUpperCase() + "(" + i + ")" + end;
                        writer.write("    " + variant + '\n');
                        i++;
                    }
                }
            }
            writer.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void generateEntityVariants(String entityType) {
        String entityName = entityType;

        String path = "C:\\Users\\Tiew_37\\IdeaProjects\\Operation_W.I.L.D\\src\\main\\java\\net\\tiew\\operationWild\\entity\\variants\\";
        String fileType = ".java";
        String fileName = entityName + "Variant" + fileType;
        File file = new File(path + fileName);
        File folder = new File("C:\\Users\\Tiew_37\\IdeaProjects\\Operation_W.I.L.D\\src\\main\\resources\\assets\\ow\\textures\\entity\\" + entityName.toLowerCase());
        File folderSkins = new File("C:\\Users\\Tiew_37\\IdeaProjects\\Operation_W.I.L.D\\src\\main\\resources\\assets\\ow\\textures\\entity\\" + entityName.toLowerCase() + "\\" + "skins");

        if (!file.exists()) {
            List<String> lines = List.of(
                    "package org.operationWild.entity.variants;\n" +
                            "\n" +
                            "import java.util.Arrays;\n" +
                            "import java.util.Comparator;\n" +
                            "\n" +
                            "public enum " + entityName + "Variant {\n" +
                            "\n" +
                            "    public static final " + entityName + "Variant[] BY_ID = Arrays.stream(values()).sorted(Comparator.comparingInt(" + entityName + "Variant::getId)).toArray(" + entityName + "Variant[]::new);\n" +
                            "\n" +
                            "    private final int id;\n" +
                            "\n" +
                            "    " + entityName + "Variant(int id) {\n" +
                            "        this.id = id;\n" +
                            "    }\n" +
                            "\n" +
                            "    public int getId() {\n" +
                            "        return id;\n" +
                            "    }\n" +
                            "\n" +
                            "    public static " + entityName + "Variant byId(int id) {\n" +
                            "        return BY_ID[id % BY_ID.length];\n" +
                            "    }\n" +
                            "}"
            );

            if (!file.exists()) {
                try {
                    folder.mkdir();
                    folderSkins.mkdir();
                    file.createNewFile();
                    FileWriter writer = new FileWriter(file);
                    BufferedWriter buffer = new BufferedWriter(writer);
                    for (String line : lines) {
                        buffer.write(line + "\n");
                    }
                    buffer.close();
                    writer.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } else throw new RuntimeException(entityType + "Variants is already created.");
    }

    public void generateEntitySkins(String entityType) {
        String entityName = entityType;

        String path = "C:\\Users\\Tiew_37\\IdeaProjects\\Operation_W.I.L.D\\src\\main\\java\\net\\tiew\\operationWild\\screen\\entity\\skins\\";
        String fileType = ".java";
        String fileName = entityName + "SkinsScreen" + fileType;
        File file = new File(path + fileName);

        if (!file.exists()) {
            List<String> lines = List.of(
                    "package org.operationWild.screen.entity.skins;\n" +
                            "\n" +
                            "import net.minecraft.client.gui.components.Button;\n" +
                            "import net.minecraft.client.gui.components.Tooltip;\n" +
                            "import net.minecraft.network.chat.Component;\n" +
                            "import net.minecraftforge.api.distmarker.Dist;\n" +
                            "import net.minecraftforge.api.distmarker.OnlyIn;\n" +
                            "import org.operationWild.screen.entity.OWSkinsInterface;\n" +
                            "\n" +
                            "@OnlyIn(Dist.CLIENT)\n" +
                            "public class " + entityName + "SkinsScreen extends OWSkinsInterface {\n" +
                            "    private Button skinButton1;\n" +
                            "    private Button skinButton7;\n" +
                            "\n" +
                            "    private int numberOfSkins = 2;\n" +
                            "\n" +
                            "    public " + entityName + "SkinsScreen() {\n" +
                            "        super();\n" +
                            "    }\n" +
                            "\n" +
                            "    @Override\n" +
                            "    protected void initEntityScale() {\n" +
                            "        if (this.entity != null && \"" + entityName + "Entity\".equals(this.entity.getClass().getSimpleName())) {\n" +
                            "            entityScale = (int) (40 * 2.5f);\n" +
                            "        }\n" +
                            "    }\n" +
                            "\n" +
                            "    @Override\n" +
                            "    protected void initLockedSkins() {\n" +
                            "        for (int i = 1; i <= numberOfSkins; i++) {\n" +
                            "            lockedSkins.put(i, false);\n" +
                            "        }\n" +
                            "    }\n" +
                            "\n" +
                            "    @Override\n" +
                            "    protected void initSkinPrices() {\n" +
                            "    }\n" +
                            "\n" +
                            "    @Override\n" +
                            "    protected void createAndAddButtons() {\n" +
                            "        LEGENDARY_SKIN.clear();\n" +
                            "        EPIC_SKIN.clear();\n" +
                            "        RARE_SKIN.clear();\n" +
                            "        COMMON_SKIN.clear();\n" +
                            "\n" +
                            "        skinButton1 = createSkinButton(Component.translatable(\"tooltip." + entityName.toLowerCase() + "Skin1\"), 1, LEGENDARY_SKIN);\n" +
                            "        skinButton7 = createSkinButton(Component.translatable(\"tooltip." + entityName.toLowerCase() + "Skin7\"), 7, COMMON_SKIN);\n" +
                            "\n" +
                            "        updateButtonColors();\n" +
                            "        addButtonsToList();\n" +
                            "    }\n" +
                            "\n" +
                            "    @Override\n" +
                            "    protected int getSkinIndexForButton(Button button) {\n" +
                            "        if (button == skinButton1) return 1;\n" +
                            "        if (button == skinButton7) return 7;\n" +
                            "        return -1;\n" +
                            "    }\n" +
                            "\n" +
                            "    @Override\n" +
                            "    protected void addTooltipsToButtons() {\n" +
                            "        if (isLocked(1)) skinButton1.setTooltip(Tooltip.create(Component.translatable(\"tooltip." + entityName.toLowerCase() + "Skin1Indication\")));\n" +
                            "    }\n" +
                            "\n" +
                            "    @Override\n" +
                            "    protected void updateLockStates() {\n" +
                            "        if (this.entity != null) {\n" +
                            "            setLockState(1, entity.getLevel() < 50);\n" +
                            "            setLockState(7, false);\n" +
                            "        }\n" +
                            "    }\n" +
                            "}"
            );

            if (!file.exists()) {
                try {
                    file.createNewFile();
                    FileWriter writer = new FileWriter(file);
                    BufferedWriter buffer = new BufferedWriter(writer);
                    for (String line : lines) {
                        buffer.write(line + "\n");
                    }
                    buffer.close();
                    writer.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } else throw new RuntimeException(entityType + "Variants is already created.");
    }

    public void generateEntityEvents(String entityType) {
        String entityName = entityType;

        File frFile = new File("C:\\Users\\Tiew_37\\IdeaProjects\\Operation_W.I.L.D\\src\\main\\java\\net\\tiew\\operationWild\\event\\ModEventBusEvents.java");

        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(frFile), "UTF-8"));
            List<String> allLines = new ArrayList<>();
            String line = reader.readLine();
            int numberOfLayers = 0;
            int numberOfAttributes = 0;
            int numberOfImports = 0;

            while (line != null) {
                allLines.add(line);
                if (line.contains("import org.operationWild.entity") && numberOfImports <= 0) {
                    allLines.add("import org.operationWild.entity.custom.living." + entityName + "Entity;");
                    numberOfImports++;
                }
                if (line.contains("LAYER_LOCATION") && numberOfLayers <= 0) {
                    allLines.add("        event.registerLayerDefinition(" + entityName + "Model" + ".LAYER_LOCATION, " + entityName + "Model" + "::createBodyLayer);");
                    numberOfLayers++;
                }
                if (line.contains(".createAttributes()") && numberOfAttributes <= 0) {
                    allLines.add("        event.put(OWEntityRegistry." + entityName.toUpperCase() + ".get(), " + entityName + "Entity" + ".createAttributes().build());");
                    numberOfAttributes++;
                }
                line = reader.readLine();
            }
            reader.close();

            BufferedWriter bufferFr = new BufferedWriter(new FileWriter(frFile));
            for (String l : allLines) {
                bufferFr.write(l + "\n");
            }
            bufferFr.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void generateEntityRegistry(String entityType, float scale, int maxSleepBar, int sleepBarDownSpeed, MobCategory mobCategory, float width, float height) {
        String entityName = entityType;

        File frFile = new File("C:\\Users\\Tiew_37\\IdeaProjects\\Operation_W.I.L.D\\src\\main\\java\\net\\tiew\\operationWild\\entity\\OWEntityRegistry.java");

        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(frFile), "UTF-8"));
            List<String> allLines = new ArrayList<>();
            String line = reader.readLine();
            int numberOfRegistry = 0;
            int numberOfImports = 0;

            while (line != null) {
                allLines.add(line);
                if (line.contains("import org.operationWild.entity.custom") && numberOfImports <= 0) {
                    allLines.add("import org.operationWild.entity.custom.living." + entityName + "Entity;");
                    numberOfImports++;
                }
                if (line.contains("public static final RegistryObject<EntityType") && numberOfRegistry <= 0) {
                    allLines.add("    public static final RegistryObject<EntityType<" + entityName + "Entity>> " + entityName.toUpperCase() + " = ENTITY_TYPES.register(\"" + entityName.toLowerCase() + "\", () -> EntityType.Builder.<" + entityName + "Entity>of((type, world) -> new " + entityName + "Entity(type, world, " + scale + "f" + ", " + maxSleepBar + ", " + sleepBarDownSpeed + "), " + "MobCategory." + mobCategory + ").sized(" + width + "f" + ", " + height + "f" + ").build(\"" + entityName + "\"));");
                    numberOfRegistry++;
                }
                line = reader.readLine();
            }
            reader.close();

            BufferedWriter bufferFr = new BufferedWriter(new FileWriter(frFile));
            for (String l : allLines) {
                bufferFr.write(l + "\n");
            }
            bufferFr.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void generateEntityLayer(String entityType) {
        String entityName = entityType;

        String path = "C:\\Users\\Tiew_37\\IdeaProjects\\Operation_W.I.L.D\\src\\main\\java\\net\\tiew\\operationWild\\entity\\client\\layer\\";
        String fileType = ".java";
        String fileName = entityName + "Layer" + fileType;
        File file = new File(path + fileName);

        if (!file.exists()) {
            List<String> lines = List.of(
                    "package org.operationWild.entity.client.layer;\n",
                    "import com.mojang.blaze3d.vertex.PoseStack;",
                    "import com.mojang.blaze3d.vertex.VertexConsumer;",
                    "import net.minecraft.client.renderer.MultiBufferSource;",
                    "import net.minecraft.client.renderer.RenderType;",
                    "import net.minecraft.client.renderer.entity.layers.RenderLayer;",
                    "import net.minecraft.client.renderer.texture.OverlayTexture;",
                    "import net.minecraft.resources.ResourceLocation;",
                    "import org.operationWild.OperationWild;",
                    "import org.operationWild.entity.client.model." + entityName + "Model;",
                    "import org.operationWild.entity.client.render." + entityName + "Renderer;",
                    "import org.operationWild.entity.custom.living." + entityName + "Entity;\n",
                    "public class " + entityName + "Layer extends RenderLayer<" + entityName + "Entity, " + entityName + "Model<" + entityName + "Entity>> {",
                    "    private static final ResourceLocation RESURRECTION_TEXTURE = ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, \"textures/entity/" + entityName.toLowerCase() + "/in_resurrection.png\");",
                    "    private static final ResourceLocation RESURRECTION_GLOWING_TEXTURE = ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, \"textures/entity/" + entityName.toLowerCase() + "/skins/" + entityName.toLowerCase() + "_skin_gold_glowing.png\");",
                    "    private static final ResourceLocation BLOODY_STAGE_0_TEXTURE = ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, \"textures/entity/" + entityName.toLowerCase() + "/" + entityName.toLowerCase() + "_bloody_stage_0.png\");",
                    "    private static final ResourceLocation BLOODY_STAGE_1_TEXTURE = ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, \"textures/entity/" + entityName.toLowerCase() + "/" + entityName.toLowerCase() + "_bloody_stage_1.png\");",
                    "    private static final ResourceLocation BLOODY_STAGE_2_TEXTURE = ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, \"textures/entity/" + entityName.toLowerCase() + "/" + entityName.toLowerCase() + "_bloody_stage_2.png\");",
                    "    private static final ResourceLocation SADDLE_TEXTURE = ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, \"textures/entity/" + entityName.toLowerCase() + "/" + entityName.toLowerCase() + "_saddle.png\");\n",
                    "    public " + entityName + "Layer(" + entityName + "Renderer " + entityName.toLowerCase() + "Renderer) {",
                    "        super(" + entityName.toLowerCase() + "Renderer);",
                    "    }\n",
                    "    @Override",
                    "    public void render(PoseStack poseStack, MultiBufferSource multiBufferSource, int packedLight, " + entityName + "Entity " + entityName.toLowerCase() + ", float v, float v1, float v2, float v3, float v4, float v5) {",
                    "        double " + entityName.toLowerCase() + "HealthTier = " + entityName.toLowerCase() + ".getMaxHealth() / 4;",
                    "        if (" + entityName.toLowerCase() + ".isInResurrection()) {",
                    "            float opacity = (float) (0.75 * (1 - " + entityName.toLowerCase() + ".getResurrectionPercentage() / 100.0f));",
                    "            renderOverlayWithOpacity(poseStack, multiBufferSource, RESURRECTION_TEXTURE, false, packedLight, opacity);",
                    "            renderOverlay(poseStack, multiBufferSource, RESURRECTION_GLOWING_TEXTURE, true, packedLight);",
                    "        }",
                    "        if (" + entityName.toLowerCase() + ".isSaddled()) renderOverlay(poseStack, multiBufferSource, SADDLE_TEXTURE, false, packedLight);",
                    "        if (" + entityName.toLowerCase() + ".getHealth() < " + entityName.toLowerCase() + "HealthTier) renderOverlay(poseStack, multiBufferSource, BLOODY_STAGE_2_TEXTURE, false, packedLight);",
                    "        else if (" + entityName.toLowerCase() + ".getHealth() < (" + entityName.toLowerCase() + "HealthTier * 2)) renderOverlay(poseStack, multiBufferSource, BLOODY_STAGE_1_TEXTURE, false, packedLight);",
                    "        else if (" + entityName.toLowerCase() + ".getHealth() < (" + entityName.toLowerCase() + "HealthTier * 3)) renderOverlay(poseStack, multiBufferSource, BLOODY_STAGE_0_TEXTURE, false, packedLight);",
                    "    }\n",
                    "    private void renderOverlay(PoseStack poseStack, MultiBufferSource bufferSource, ResourceLocation texture, boolean glowLayer, int packedLight) {",
                    "        VertexConsumer vertexConsumer = bufferSource.getBuffer(glowLayer ? RenderType.eyes(texture) : RenderType.entityCutout(texture));",
                    "        this.getParentModel().renderToBuffer(poseStack, vertexConsumer, glowLayer ? 15728640 : packedLight, OverlayTexture.NO_OVERLAY);",
                    "    }\n",
                    "    private void renderOverlayWithColor(PoseStack poseStack, MultiBufferSource bufferSource, ResourceLocation texture, boolean glowLayer, int packedLight, int color) {",
                    "        RenderType renderType = glowLayer ? RenderType.eyes(texture) : RenderType.entityCutoutNoCull(texture);",
                    "        VertexConsumer vertexConsumer = bufferSource.getBuffer(renderType);",
                    "        this.getParentModel().renderToBuffer(poseStack, vertexConsumer, glowLayer ? 15728640 : packedLight, OverlayTexture.NO_OVERLAY, color);",
                    "    }\n",
                    "    private void renderOverlayWithOpacity(PoseStack poseStack, MultiBufferSource bufferSource, ResourceLocation texture, boolean glowLayer, int packedLight, float opacity) {",
                    "        opacity = Math.max(0.0f, Math.min(1.0f, opacity));",
                    "        int alpha = (int)(opacity * 255.0f);",
                    "        int color = 0xFFFFFF | (alpha << 24);",
                    "        RenderType renderType = glowLayer ? RenderType.eyes(texture) : RenderType.entityTranslucent(texture);",
                    "        VertexConsumer vertexConsumer = bufferSource.getBuffer(renderType);",
                    "        this.getParentModel().renderToBuffer(poseStack, vertexConsumer, glowLayer ? 15728640 : packedLight, OverlayTexture.NO_OVERLAY, color);",
                    "    }",
                    "}"
            );

            try {
                file.createNewFile();
                try (BufferedWriter buffer = new BufferedWriter(new FileWriter(file))) {
                    for (String line : lines) {
                        buffer.write(line + "\n");
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else throw new RuntimeException(entityType + "Layer is already created.");
    }


    public void generateEntitySkinsLayer(String entityType) {
        String entityName = entityType;

        String path = "C:\\Users\\Tiew_37\\IdeaProjects\\Operation_W.I.L.D\\src\\main\\java\\net\\tiew\\operationWild\\entity\\client\\layer\\skins\\";
        String fileType = ".java";
        String fileName = entityName + "Skins" + fileType;
        File file = new File(path + fileName);

        if (!file.exists()) {
            List<String> lines = List.of(
                    "package org.operationWild.entity.client.layer.skins;\n",
                    "import com.mojang.blaze3d.vertex.PoseStack;",
                    "import com.mojang.blaze3d.vertex.VertexConsumer;",
                    "import net.minecraft.client.renderer.MultiBufferSource;",
                    "import net.minecraft.client.renderer.RenderType;",
                    "import net.minecraft.client.renderer.entity.layers.RenderLayer;",
                    "import net.minecraft.client.renderer.texture.OverlayTexture;",
                    "import net.minecraft.resources.ResourceLocation;",
                    "import org.operationWild.OperationWild;",
                    "import org.operationWild.entity.client.model." + entityName + "Model;",
                    "import org.operationWild.entity.client.render." + entityName + "Renderer;",
                    "import org.operationWild.entity.custom.living." + entityName + "Entity;",
                    "import org.operationWild.entity.variants." + entityName + "Variant;\n",
                    "public class " + entityName + "Skins extends RenderLayer<" + entityName + "Entity, " + entityName + "Model<" + entityName + "Entity>> {",
                    "    private static final ResourceLocation SKIN_GOLD_GLOWING_TEXTURE = ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, \"textures/entity/" + entityName.toLowerCase() + "/skins/" + entityName.toLowerCase() + "_skin_gold_glowing.png\");\n",
                    "    public " + entityName + "Skins(" + entityName + "Renderer " + entityName.toLowerCase() + "Renderer) {",
                    "        super(" + entityName.toLowerCase() + "Renderer);",
                    "    }\n",
                    "    @Override",
                    "    public void render(PoseStack poseStack, MultiBufferSource multiBufferSource, int packedLight, " + entityName + "Entity " + entityName.toLowerCase() + ", float v, float v1, float v2, float v3, float v4, float v5) {",
                    "        if (" + entityName.toLowerCase() + ".getVariant() == " + entityName + "Variant.SKIN_GOLD) {",
                    "            renderOverlay(poseStack, multiBufferSource, SKIN_GOLD_GLOWING_TEXTURE, true, packedLight);",
                    "        }",
                    "    }\n",
                    "    private void renderOverlay(PoseStack poseStack, MultiBufferSource bufferSource, ResourceLocation texture, boolean glowLayer, int packedLight) {",
                    "        VertexConsumer vertexConsumer = bufferSource.getBuffer(glowLayer ? RenderType.eyes(texture) : RenderType.entityCutout(texture));",
                    "        this.getParentModel().renderToBuffer(poseStack, vertexConsumer, glowLayer ? 15728640 : packedLight, OverlayTexture.NO_OVERLAY);",
                    "    }\n",
                    "    private void renderOverlayWithColor(PoseStack poseStack, MultiBufferSource bufferSource, ResourceLocation texture, boolean glowLayer, int packedLight, int color) {",
                    "        RenderType renderType = glowLayer ? RenderType.eyes(texture) : RenderType.entityCutoutNoCull(texture);",
                    "        VertexConsumer vertexConsumer = bufferSource.getBuffer(renderType);",
                    "        this.getParentModel().renderToBuffer(poseStack, vertexConsumer, glowLayer ? 15728640 : packedLight, OverlayTexture.NO_OVERLAY, color);",
                    "    }\n",
                    "    private void renderOverlayWithOpacity(PoseStack poseStack, MultiBufferSource bufferSource, ResourceLocation texture, boolean glowLayer, int packedLight, float opacity) {",
                    "        opacity = Math.max(0.0f, Math.min(1.0f, opacity));",
                    "        int alpha = (int)(opacity * 255.0f);",
                    "        int color = 0xFFFFFF | (alpha << 24);",
                    "        RenderType renderType = glowLayer ? RenderType.eyes(texture) : RenderType.entityTranslucent(texture);",
                    "        VertexConsumer vertexConsumer = bufferSource.getBuffer(renderType);",
                    "        this.getParentModel().renderToBuffer(poseStack, vertexConsumer, glowLayer ? 15728640 : packedLight, OverlayTexture.NO_OVERLAY, color);",
                    "    }",
                    "}"
            );

            try {
                file.createNewFile();
                try (BufferedWriter buffer = new BufferedWriter(new FileWriter(file))) {
                    for (String line : lines) {
                        buffer.write(line + "\n");
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else throw new RuntimeException(entityType + "Skins is already created.");
    }

    public void generateEntitySetup(String entityType) {
        String entityName = entityType;

        File frFile = new File("C:\\Users\\Tiew_37\\IdeaProjects\\Operation_W.I.L.D\\src\\main\\java\\net\\tiew\\operationWild\\OperationWild.java");

        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(frFile), "UTF-8"));
            List<String> allLines = new ArrayList<>();
            String line = reader.readLine();
            int $$1 = 0;

            while (line != null) {
                allLines.add(line);
                if (line.contains("EntityRenderers.register") && $$1 <= 0) {
                    allLines.add("            EntityRenderers.register(OWEntityRegistry." + entityName.toUpperCase() + ".get(), " + entityName + "Renderer" + "::new);");
                    $$1++;
                }
                line = reader.readLine();
            }
            reader.close();

            BufferedWriter bufferFr = new BufferedWriter(new FileWriter(frFile));
            for (String l : allLines) {
                bufferFr.write(l + "\n");
            }
            bufferFr.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void generateEntityTranslations(String entityType, String entityNameFR) {
        String entityName = entityType;

        File frFile = new File("C:\\Users\\Tiew_37\\IdeaProjects\\Operation_W.I.L.D\\src\\main\\resources\\assets\\ow\\lang\\fr_fr.json");
        File enFile = new File("C:\\Users\\Tiew_37\\IdeaProjects\\Operation_W.I.L.D\\src\\main\\resources\\assets\\ow\\lang\\en_us.json");

        try {
            BufferedReader readerFr = new BufferedReader(new InputStreamReader(new FileInputStream(frFile), "UTF-8"));
            BufferedReader readerEn = new BufferedReader(new InputStreamReader(new FileInputStream(enFile), "UTF-8"));
            List<String> allLinesFr = new ArrayList<>();
            List<String> allLinesEn = new ArrayList<>();
            String lineFr = readerFr.readLine();
            String lineEn = readerEn.readLine();
            int numberOfTimeAddingFr = 0;
            int numberOfTimeAddingEn = 0;

            while (lineEn != null) {
                allLinesEn.add(lineEn);
                if (lineEn.contains("entity.ow") && numberOfTimeAddingEn <= 0) {
                    allLinesEn.add("  \"entity.ow." + entityName.toLowerCase() + "\": \"" + entityName + "\",");
                    numberOfTimeAddingEn++;
                }
                lineEn = readerEn.readLine();
            }
            readerEn.close();

            BufferedWriter bufferEn = new BufferedWriter(new FileWriter(enFile));
            for (String l : allLinesEn) {
                bufferEn.write(l + "\n");
            }
            bufferEn.close();

            while (lineFr != null) {
                allLinesFr.add(lineFr);
                if (lineFr.contains("entity.ow") && numberOfTimeAddingFr <= 0) {
                    allLinesFr.add("  \"entity.ow." + entityName.toLowerCase() + "\": \"" + entityNameFR + "\",");
                    numberOfTimeAddingFr++;
                }
                lineFr = readerFr.readLine();
            }
            readerFr.close();

            BufferedWriter bufferFr = new BufferedWriter(new FileWriter(frFile));
            for (String l : allLinesFr) {
                bufferFr.write(l + "\n");
            }
            bufferFr.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void generateEntityAnimations(String entityType) {
        String entityName = entityType;

        File file = new File("C:\\Users\\Tiew_37\\Desktop\\" + entityName.toLowerCase() + "_animations" + ".txt");
        File fileToCreate = new File("C:\\Users\\Tiew_37\\IdeaProjects\\Operation_W.I.L.D\\src\\main\\java\\net\\tiew\\operationWild\\entity\\client\\animation\\" + entityName + "Animations.java");

        if (!file.exists()) throw new RuntimeException(entityType + "Animations doesn't exist on Desktop.");

        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF-8"));
            String line = reader.readLine();

            ArrayList<String> animations = new ArrayList<>();

            while (line != null) {
                if (line.contains("public static final AnimationDefinition") || line.contains(".addAnimation") || line.contains("new AnimationChannel") || line.contains("new Keyframe") || line.contains("AnimationChannel")) animations.add(line);
                line = reader.readLine();
            }

            reader.close();

            List<String> lines1 = List.of(
                    "package org.operationWild.entity.client.animation;\n" +
                            "\n" +
                            "import net.minecraft.client.animation.AnimationChannel;\n" +
                            "import net.minecraft.client.animation.AnimationDefinition;\n" +
                            "import net.minecraft.client.animation.Keyframe;\n" +
                            "import net.minecraft.client.animation.KeyframeAnimations;\n" +
                            "\n" +
                            "public class " + entityName + "Animations {" +
                            "\n"
            );

            List<String> lines2 = List.of(
                    "\n    }"
            );

            fileToCreate.createNewFile();
            FileWriter writer = new FileWriter(fileToCreate);
            BufferedWriter buffer = new BufferedWriter(writer);

            for (String linee : lines1) {
                buffer.write(linee + "\n");
            }

            for (String modelPart : animations) {
                buffer.write(modelPart + "\n");
            }

            for (String linee : lines2) {
                buffer.write(linee + "\n");
            }


            buffer.close();
            writer.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void generateEntityRenderer(String entityType) {
        String entityName = entityType;

        String path = "C:\\Users\\Tiew_37\\IdeaProjects\\Operation_W.I.L.D\\src\\main\\java\\net\\tiew\\operationWild\\entity\\client\\render\\";
        String fileType = ".java";
        String fileName = entityName + "Renderer" + fileType;
        File file = new File(path + fileName);

        if (!file.exists()) {
            List<String> lines = List.of(
                    "package org.operationWild.entity.client.render;\n" +
                            "\n" +
                            "import com.google.common.collect.Maps;\n" +
                            "import com.mojang.blaze3d.vertex.PoseStack;\n" +
                            "import net.minecraft.Util;\n" +
                            "import net.minecraft.client.renderer.MultiBufferSource;\n" +
                            "import net.minecraft.client.renderer.entity.EntityRendererProvider;\n" +
                            "import net.minecraft.client.renderer.entity.MobRenderer;\n" +
                            "import net.minecraft.network.chat.Component;\n" +
                            "import net.minecraft.resources.ResourceLocation;\n" +
                            "import net.minecraft.world.entity.player.Player;\n" +
                            "import org.operationWild.OperationWild;\n" +
                            "import org.operationWild.entity.client.layer." + entityName + "Layer;\n" +
                            "import org.operationWild.entity.client.layer.skins." + entityName + "Skins;\n" +
                            "import org.operationWild.entity.client.model." + entityName + "Model;\n" +
                            "import org.operationWild.entity.custom.living." + entityName + "Entity;\n" +
                            "import org.operationWild.entity.variants." + entityName + "Variant;\n" +
                            "\n" +
                            "import java.util.Map;" +

                            "public class " + entityName + "Renderer extends MobRenderer<" + entityName + "Entity, " + entityName + "Model<" + entityName + "Entity>> {\n" +
                            "    private static final Map<" + entityName + "Variant, ResourceLocation> LOCATION_BY_VARIANT = Util.make(Maps.newEnumMap(" + entityName + "Variant.class), map -> {\n" +
                            "        map.put(" + entityName + "Variant.DEFAULT, ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, \"textures/entity/" + entityName.toLowerCase() + "/" + entityName.toLowerCase() + "_default.png\"));\n" +
                            "    });\n" +
                            "    private static final ResourceLocation ICONS = ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, \"textures/gui/mob_types.png\");\n" +
                            "\n" +
                            "    public " + entityName + "Renderer(EntityRendererProvider.Context context) {\n" +
                            "        super(context, new " + entityName + "Model<>(context.bakeLayer(" + entityName + "Model.LAYER_LOCATION)), 0.4f);\n" +
                            "        this.addLayer(new " + entityName + "Skins(this));\n" +
                            "        this.addLayer(new " + entityName + "Layer(this));\n" +
                            "    }\n" +
                            "\n" +
                            "    @Override\n" +
                            "    public ResourceLocation getTextureLocation(" + entityName + "Entity " + entityName.toLowerCase() + ") {\n" +
                            "        return LOCATION_BY_VARIANT.get(" + entityName.toLowerCase() + ".getVariant());\n" +
                            "    }\n" +
                            "\n" +
                            "    @Override\n" +
                            "    public void render(" + entityName + "Entity " + entityName.toLowerCase() + ", float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {\n" +
                            "        float scale = " + entityName.toLowerCase() + ".getScale() / 1.4f;\n" +
                            "        float babyScale = scale / 2.25f;\n" +
                            "        int genderPosition = " + entityName.toLowerCase() + ".isFemale() ? 36 : " + entityName.toLowerCase() + ".isMale() ? 48 : 0;\n" +
                            "        Player player = " + entityName.toLowerCase() + ".level().getNearestPlayer(" + entityName.toLowerCase() + ", 64.0D);\n" +
                            "\n" +
                            "        poseStack.pushPose();\n" +
                            "\n" +
                            "        if (" + entityName.toLowerCase() + ".isBaby()) {\n" +
                            "            float maturationPercent = (float) " + entityName.toLowerCase() + ".getMaturationPercentage() / 100f;\n" +
                            "            float currentScale = babyScale + (scale - babyScale) * maturationPercent;\n" +
                            "            poseStack.scale(currentScale, currentScale, currentScale);\n" +
                            "        } else {\n" +
                            "            poseStack.scale(scale, scale, scale);\n" +
                            "        }\n" +
                            "\n" +
                            "        super.render(" + entityName.toLowerCase() + ", entityYaw, partialTicks, poseStack, bufferSource, packedLight);\n" +
                            "        poseStack.popPose();\n" +
                            "\n" +
                            "        if (!" + entityName.toLowerCase() + ".isInResurrection()) {\n" +
                            "            if (" + entityName.toLowerCase() + ".isAlive() && !" + entityName.toLowerCase() + ".isVehicle()) {\n" +
                            "                if (" + entityName.toLowerCase() + ".isTame()) {\n" +
                            "                    if (player != null && " + entityName.toLowerCase() + ".distanceTo(player) > 4.0D) {\n" +
                            "                        OWRendererUtils.displayOwnerAboveEntity(" + entityName.toLowerCase() + ", poseStack, bufferSource, packedLight, this.entityRenderDispatcher);\n" +
                            "                        OWRendererUtils.displayLevelAboveEntity(" + entityName.toLowerCase() + ", poseStack, bufferSource, packedLight, this.entityRenderDispatcher);\n" +
                            "                        OWRendererUtils.displayImageAboveEntity(ICONS, 0, genderPosition, 12, 256, 0, 0, " + entityName.toLowerCase() + ", poseStack, bufferSource, packedLight);\n" +
                            "                        if (" + entityName.toLowerCase() + ".isPassive())\n" +
                            "                            OWRendererUtils.displayImageAboveEntity(ICONS, 0, 60, 14, 256, 1.5f, 0f, " + entityName.toLowerCase() + ", poseStack, bufferSource, packedLight);\n" +
                            "                        if (" + entityName.toLowerCase() + ".getLevel() >= 50) {\n" +
                            "                            OWRendererUtils.displayImageAboveEntity(ICONS, 0, 143, 10, 256, -1f, 1f, " + entityName.toLowerCase() + ", poseStack, bufferSource, packedLight);\n" +
                            "                            OWRendererUtils.displayPrestigeLevelAboveEntity(" + entityName.toLowerCase() + ", poseStack, bufferSource, packedLight, this.entityRenderDispatcher);\n" +
                            "                        }\n" +
                            "                    }\n" +
                            "                } else {\n" +
                            "                    if (" + entityName.toLowerCase() + ".isSleeping()) {\n" +
                            "                        OWRendererUtils.displayBonusPointAboveEntity(" + entityName.toLowerCase() + ", poseStack, bufferSource, packedLight, this.entityRenderDispatcher, 0);\n" +
                            "                    }\n" +
                            "                }\n" +
                            "            }\n" +
                            "        }\n" +
                            "        OWRendererUtils.createInformationImage(" + entityName.toLowerCase() + ", poseStack, bufferSource, packedLight, 0, 0, 0, 0, 2);\n" +
                            "    }\n" +
                            "\n" +
                            "    @Override\n" +
                            "    protected void renderNameTag(" + entityName + "Entity " + entityName.toLowerCase() + ", Component component, PoseStack poseStack, MultiBufferSource bufferSource, int i, float v) {\n" +
                            "        poseStack.pushPose();\n" +
                            "        poseStack.translate(0.0D, 0.65D, 0.0D);\n" +
                            "        super.renderNameTag(" + entityName.toLowerCase() + ", component, poseStack, bufferSource, i, v);\n" +
                            "        poseStack.popPose();\n" +
                            "    }\n" +
                            "}"
            );

            if (!file.exists()) {
                try {
                    file.createNewFile();
                    FileWriter writer = new FileWriter(file);
                    BufferedWriter buffer = new BufferedWriter(writer);
                    for (String line : lines) {
                        buffer.write(line + "\n");
                    }
                    buffer.close();
                    writer.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } else throw new RuntimeException(entityType + "Renderer is already created.");
    }

    public void generateEntityModel(String entityType) {
        String entityName = entityType;

        File file = new File("C:\\Users\\Tiew_37\\Desktop\\" + entityName.toLowerCase() + ".java");
        File fileToCreate = new File("C:\\Users\\Tiew_37\\IdeaProjects\\Operation_W.I.L.D\\src\\main\\java\\net\\tiew\\operationWild\\entity\\client\\model\\" + entityName + "Model.java");

        if (!file.exists()) throw new RuntimeException(entityType + "Model doesn't exist on Desktop.");

        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF-8"));
            String line = reader.readLine();

            ArrayList<String> modelParts = new ArrayList<>();
            ArrayList<String> modelChilds = new ArrayList<>();
            ArrayList<String> modelCubes = new ArrayList<>();

            while (line != null) {
                if (line.contains("final ModelPart")) modelParts.add(line);
                if (line.contains(".getChild")) modelChilds.add(line);
                if (line.contains("PartDefinition") || line.contains("MeshDefinition") || line.contains("return LayerDefinition.create") || line.contains("texOffs")) modelCubes.add(line);
                line = reader.readLine();
            }

            reader.close();

            List<String> lines1 = List.of(
                    "package org.operationWild.entity.client.model;",
                    "",
                    "import com.mojang.blaze3d.vertex.PoseStack;",
                    "import com.mojang.blaze3d.vertex.VertexConsumer;",
                    "import net.minecraft.client.model.HierarchicalModel;",
                    "import net.minecraft.client.model.geom.ModelLayerLocation;",
                    "import net.minecraft.client.model.geom.ModelPart;",
                    "import net.minecraft.client.model.geom.PartPose;",
                    "import net.minecraft.client.model.geom.builders.*;",
                    "import net.minecraft.resources.ResourceLocation;",
                    "import net.minecraft.util.Mth;",
                    "import org.operationWild.OperationWild;",
                    "import org.operationWild.entity.client.animation." + entityName + "Animations;",
                    "import org.operationWild.entity.custom.living." + entityName + "Entity;",
                    "",
                    "public class " + entityName + "Model<T extends " + entityName + "Entity> extends HierarchicalModel<T> {",
                    "    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, \"" + entityName.toLowerCase() + "_default\"), \"main\");",
                    ""
            );

            List<String> lines2 = List.of(
                    "",
                    "    public " + entityName + "Model(ModelPart root) {",
                    ""
            );

            List<String> lines3 = List.of(
                    "    }",
                    "",
                    "    public static LayerDefinition createBodyLayer() {",
                    ""
            );

            List<String> lines4 = List.of(
                    "    }",
                    "",
                    "    @Override",
                    "    public void setupAnim(" + entityName + "Entity " + entityName.toLowerCase() + ", float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {",
                    "        this.root().getAllParts().forEach(ModelPart::resetPose);",
                    "        if (" + entityName.toLowerCase() + ".isBaby()) {",
                    "            float maturationPercent = (float) " + entityName.toLowerCase() + ".getMaturationPercentage() / 100f;",
                    "            float headScale = 2f - (2f - 1.0f) * maturationPercent;",
                    "",
                    "            this.head.xScale *= headScale;",
                    "            this.head.yScale *= headScale;",
                    "            this.head.zScale *= headScale;",
                    "        }",
                    "        this.applyHeadRotation(netHeadYaw, headPitch);",
                    "",
                    "        if (" + entityName.toLowerCase() + ".isSitting()) {",
                    "            this.animate(" + entityName.toLowerCase() + ".sittingAnimationState, " + entityName + "Animations.SIT, ageInTicks, 1.0f);",
                    "            return;",
                    "        }",
                    "        this.animate(" + entityName.toLowerCase() + ".idleAnimationState, " + entityName + "Animations.MISC_IDLE, ageInTicks, 1.0f);",
                    "        this.animateWalk(" + entityName + "Animations.MOVE_WALK, limbSwing, limbSwingAmount, 4.25f, 3.5f);",
                    "        if (" + entityName.toLowerCase() + ".isRunning() || " + entityName.toLowerCase() + ".getState() == 2) this.animateWalk(" + entityName + "Animations.MOVE_RUN, limbSwing, limbSwingAmount, 0.01f, 1.0f);",
                    "    }",
                    "",
                    "    @Override",
                    "    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, int color) {",
                    "        this.ALL2.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);",
                    "    }",
                    "",
                    "    private void applyHeadRotation(float pNetHeadYaw, float pHeadPitch) {",
                    "        pNetHeadYaw = Mth.clamp(pNetHeadYaw, -30.0F, 30.0F);",
                    "        pHeadPitch = Mth.clamp(pHeadPitch, -30.0F, 30.0F);",
                    "",
                    "        this.head.yRot = pNetHeadYaw * ((float)Math.PI / 180F);",
                    "        this.head.xRot = pHeadPitch * ((float)Math.PI / 180F);",
                    "    }",
                    "",
                    "    @Override",
                    "    public ModelPart root() {",
                    "        return this.ALL2;",
                    "    }",
                    "}"
            );

            fileToCreate.createNewFile();
            FileWriter writer = new FileWriter(fileToCreate);
            BufferedWriter buffer = new BufferedWriter(writer);

            for (String linee : lines1) {
                buffer.write(linee + "\n");
            }

            for (String modelPart : modelParts) {
                buffer.write(modelPart + "\n");
            }

            for (String linee : lines2) {
                buffer.write(linee + "\n");
            }

            for (String modelChild : modelChilds) {
                buffer.write(modelChild + "\n");
            }

            for (String linee : lines3) {
                buffer.write(linee + "\n");
            }

            for (String modelCube : modelCubes) {
                buffer.write(modelCube + "\n");
            }

            for (String linee : lines4) {
                buffer.write(linee + "\n");
            }

            buffer.close();
            writer.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}