package net.tiew.operationWild.core;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.*;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.network.protocol.game.ClientboundSetSubtitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.core.Holder;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.DefaultAttributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.tiew.operationWild.entity.OWEntity;
import org.joml.Math;
import org.joml.Vector3f;

public class OWUtils {

    /**
     * Planifie une action sur le thread <b>principal</b> du serveur après {@code delayTicks} ticks (20 ticks = 1 s).
     * À utiliser systématiquement à la place de {@code new Timer()}/{@code TimerTask} : accéder au monde ou aux
     * entités depuis un thread annexe provoque des {@code ConcurrentModificationException}, de la corruption d'état
     * et des crashs (surtout en multijoueur). Sans effet côté client ou si le serveur est indisponible.
     */
    public static void scheduleServer(Level level, int delayTicks, Runnable action) {
        if (level == null || level.isClientSide()) return;
        net.minecraft.server.MinecraftServer server = level.getServer();
        if (server == null) return;
        if (delayTicks <= 0) {
            server.execute(action);
        } else {
            server.tell(new net.minecraft.server.TickTask(server.getTickCount() + delayTicks, action));
        }
    }

    public static boolean RANDOM(int i) { return OWEntity.RANDOM.nextInt(i) == 0;}

    public static double generateRandomInterval(double min, double max) { return (min + (Math.random() * (max - min)));}

    public static double determinateMinAndMax(int average, double percent) {
        int min = (int) (average * (1 - (percent / 100)));
        int max = (int) (average * (1 + percent / 100));
        return OWUtils.generateRandomInterval(min, max);
    }

    public static float distanceRest(LivingEntity livingEntity, BlockPos target) {
        float f = (float) (livingEntity.getX() - target.getX());
        float f1 = (float) (livingEntity.getY() - target.getY());
        float f2 = (float) (livingEntity.getZ() - target.getZ());
        return Mth.sqrt(f * f + f1 * f1 + f2 * f2);
    }

    public static void showMode(ServerPlayer player, boolean isAggressive) {
        Component label = Component.translatable("tooltip.mode").withStyle(style -> style.withColor(TextColor.fromRgb(0xFFFFFF)));

        Component translatedMode = Component.translatable(isAggressive ? "tooltip.modeAggressive" : "tooltip.modePassive")
                .withStyle(style -> style.withColor(TextColor.fromRgb(isAggressive ? 0xFF5555 : 0x55FF55)).withBold(true));

        player.displayClientMessage(label.copy().append(translatedMode), true);
    }

    /**
     * La statistique dépasse-t-elle la moyenne de son espèce ? {@code null} si l'espèce ne déclare
     * pas l'attribut, auquel cas il n'y a rien à comparer.
     *
     * <p>La référence est la valeur déclarée dans {@code createAttributes()}, autour de laquelle
     * {@code setRandomAttributes} tire chaque individu à l'apparition.</p>
     */
    public static Boolean aboveSpeciesAverage(LivingEntity entity, Holder<Attribute> attribute, double actual) {
        AttributeSupplier supplier = DefaultAttributes.getSupplier(
                (EntityType<? extends LivingEntity>) entity.getType());
        if (!supplier.hasAttribute(attribute)) return null;

        double average = supplier.getBaseValue(attribute);
        return average <= 0.0 ? null : actual >= average;
    }

    /**
     * Puissance de Soin comparée à la référence de l'espèce.
     *
     * <p>Elle ne vit pas dans un attribut : {@link #aboveSpeciesAverage} ne sait donc pas la lire,
     * et la fiche restait sans flèche là où la vie et la vitesse en avaient une. La référence est
     * la valeur pivot du tirage, celle autour de laquelle naissent les individus.</p>
     */
    public static Boolean aboveHealPowerAverage(net.tiew.operationWild.entity.animals.terrestrial.RedPandaEntity redPanda) {
        return redPanda.getHealPower()
                >= net.tiew.operationWild.entity.animals.terrestrial.RedPandaEntity.HEAL_POWER_BASE;
    }

    /**
     * Énergie vitale comparée à celle d'un animal de la même espèce <b>au même niveau</b> : cette
     * statistique n'a aucun tirage aléatoire à l'apparition, seul le bonus acquis la différencie.
     */
    public static Boolean aboveEnergyAverage(OWEntity entity) {
        double average = entity.getMaxVitalEnergy()
                * (1f + OWEntity.VITAL_ENERGY_LEVEL_GAIN * (java.lang.Math.min(entity.getLevel(), 50) / 50f));
        return average <= 0.0 ? null : entity.getVitalEnergyCapacity() >= average;
    }

    /**
     * Vitesse affichée sur la fiche, en blocs par seconde.
     *
     * <p>Lue sur l'ATTRIBUT et non sur {@code getSpeed()}, qui ne rend que la vitesse du tick en
     * cours : une bête assise, endormie ou portée sur l'épaule y vaut zéro, et sa fiche annonçait
     * « 0,0 bloc/s » comme si elle était infirme. L'attribut, lui, est bien la statistique de
     * l'animal — celle-là même que plafonnent les boutons d'amélioration.</p>
     */
    public static float getSpeedBlocksPerSecond(OWEntity entity) {
        float base = (float) entity.getAttributeBaseValue(Attributes.MOVEMENT_SPEED);
        if (base <= 0f) base = entity.getBaseSpeed();
        if (base <= 0f) base = (float) entity.getAttributeValue(Attributes.MOVEMENT_SPEED);

        // Une espèce qui ne se monte pas n'a pas de multiplicateur de course : il vaut zéro, et
        // toute la formule avec lui. On affiche alors sa vitesse de marche telle quelle — c'est bien
        // à celle-là qu'elle se déplace, portée sur une épaule ou non.
        if (entity.vehicleRunSpeedMultiplier() <= 0f) return base * 20f;

        if (entity.canIncreasesSpeedDuringSprint()) {
            return (((base * 20) / 3) * ((entity.vehicleRunSpeedMultiplier() * (0.5f + ((float) (Math.min(100, 50 / 100))) / 2) * 1.75f)));
        }
        return (base * 20) * (entity.vehicleRunSpeedMultiplier() / 1.75f);
    }

    public static void showMessage(ServerPlayer player, String messageText, TextColor color, boolean isBold) {
        Component message = Component.translatable(messageText).setStyle(Style.EMPTY.withColor(color).withBold(isBold));
        player.displayClientMessage(message, true);
    }

    public static void showMessage(ServerPlayer player, Component component, int colorHex, boolean isBold) {
        Component message = component.copy().setStyle(Style.EMPTY.withColor(TextColor.fromRgb(colorHex)).withBold(isBold));
        player.displayClientMessage(message, true);
    }

    public static int generateExponentialExp(double min, double multiplicator) {
        return (int) (min * (1 + java.lang.Math.log(1 + multiplicator) / (3 + Math.sqrt(min))));
    }

    /**
     * XP nécessaire pour passer du niveau {@code level} au niveau {@code level + 1} (niveaux 1 → 50).
     * <p>
     * Courbe « équilibrée, progressive » : les premiers niveaux montent vite, puis l'exigence
     * augmente de façon quadratique douce. Cette valeur sert directement de seuil ({@code XpStage})
     * que l'entité remplit en absorbant des boules d'expérience, comme un joueur.
     * </p>
     * Total cumulé pour atteindre le niveau 50 : ≈ 7788 points d'XP.
     */
    public static int xpToNextLevel(int level) {
        if (level < 1) level = 1;
        return 6 + 2 * level + (level * level) / 8;
    }

    public static void showTitle(ServerPlayer player, String messageText, TextColor color, String messageText2, TextColor color2) {
        Component tooltip = Component.translatable(messageText).setStyle(Style.EMPTY.withColor(color).withBold(true));
        Component subtitle = Component.literal(messageText2).setStyle(Style.EMPTY.withColor(color2));
        player.connection.send(new ClientboundSetTitleTextPacket(tooltip));
        player.connection.send(new ClientboundSetSubtitleTextPacket(subtitle));
    }

    public static void spawnBlurrParticle(Level world, Entity entity, float red, float green, float blue) {
        double startX = entity.getX() + entity.getLookAngle().x * 2.25;
        double startY = entity.getY() + entity.getEyeHeight() + entity.getLookAngle().y * 1.5;
        double startZ = entity.getZ() + entity.getLookAngle().z * 2.25;

        double directionX = entity.getLookAngle().x;
        double directionY = entity.getLookAngle().y;
        double directionZ = entity.getLookAngle().z;

        double speed = 0.3;
        int count = 2;

        for (int i = 0; i < count; i++) {
            double spreadFactor = 0.4;

            world.addParticle(
                    new DustParticleOptions(new Vector3f(red, green, blue), 1.0F),
                    startX, startY, startZ,
                    directionX * speed * (0.8 + Math.random() * 0.4) + (Math.random() - 0.5) * spreadFactor,
                    directionY * speed * (0.8 + Math.random() * 0.4) + (Math.random() - 0.5) * spreadFactor,
                    directionZ * speed * (0.8 + Math.random() * 0.4) + (Math.random() - 0.5) * spreadFactor
            );
        }
    }

    public static void spawnParticles(Entity entity, SimpleParticleType particleTypes, double spawnX, double spawnY, double spawnZ, int amount, double dispersionMultiplier) {
        ParticleOptions particleoptions = particleTypes;
        for (int i = 0; i < amount; ++i) {
            double offsetX = OWEntity.RANDOM.nextDouble() * (0.5D * dispersionMultiplier) - (0.25D * dispersionMultiplier);
            double offsetY = OWEntity.RANDOM.nextDouble() * (0.5D * dispersionMultiplier) - (0.25D * dispersionMultiplier);
            double offsetZ = OWEntity.RANDOM.nextDouble() * (0.5D * dispersionMultiplier) - (0.25D * dispersionMultiplier);

            double speedX = OWEntity.RANDOM.nextGaussian() * 0.02D;
            double speedY = OWEntity.RANDOM.nextGaussian() * 0.02D;
            double speedZ = OWEntity.RANDOM.nextGaussian() * 0.02D;

            double posX = entity.getRandomX(spawnX) + offsetX;
            double posY = entity.getRandomY() + spawnY + offsetY;
            double posZ = entity.getRandomZ(spawnZ) + offsetZ;

            entity.level().addParticle(particleoptions, posX, posY, posZ, speedX, speedY, speedZ);
        }
    }

    public static void spawnServerParticles(Entity entity, SimpleParticleType particleTypes, double spawnX, double spawnY, double spawnZ, int amount, double dispersionMultiplier) {
        if (!(entity.level() instanceof ServerLevel serverLevel)) return;

        for (int i = 0; i < amount; ++i) {
            double offsetX = OWEntity.RANDOM.nextDouble() * (0.5D * dispersionMultiplier) - (0.25D * dispersionMultiplier);
            double offsetY = OWEntity.RANDOM.nextDouble() * (0.5D * dispersionMultiplier) - (0.25D * dispersionMultiplier);
            double offsetZ = OWEntity.RANDOM.nextDouble() * (0.5D * dispersionMultiplier) - (0.25D * dispersionMultiplier);

            double speedX = OWEntity.RANDOM.nextGaussian() * 0.02D;
            double speedY = OWEntity.RANDOM.nextGaussian() * 0.02D;
            double speedZ = OWEntity.RANDOM.nextGaussian() * 0.02D;

            double posX = entity.getRandomX(spawnX) + offsetX;
            double posY = entity.getRandomY() + spawnY + offsetY;
            double posZ = entity.getRandomZ(spawnZ) + offsetZ;

            serverLevel.sendParticles(particleTypes, posX, posY, posZ, 1, speedX, speedY, speedZ, 0.0);
        }
    }

    public static void spawnBlockParticles(Entity entity, Block block, double spawnX, double spawnY, double spawnZ, int amount, double dispersionMultiplier) {
        BlockParticleOption particleOption = new BlockParticleOption(ParticleTypes.BLOCK, block.defaultBlockState());

        for (int i = 0; i < amount; ++i) {
            double offsetX = OWEntity.RANDOM.nextDouble() * (0.5D * dispersionMultiplier) - (0.25D * dispersionMultiplier);
            double offsetY = OWEntity.RANDOM.nextDouble() * (0.5D * dispersionMultiplier) - (0.25D * dispersionMultiplier);
            double offsetZ = OWEntity.RANDOM.nextDouble() * (0.5D * dispersionMultiplier) - (0.25D * dispersionMultiplier);

            double speedX = OWEntity.RANDOM.nextGaussian() * 0.02D;
            double speedY = OWEntity.RANDOM.nextGaussian() * 0.02D;
            double speedZ = OWEntity.RANDOM.nextGaussian() * 0.02D;

            double posX = entity.getRandomX(spawnX) + offsetX;
            double posY = entity.getRandomY() + spawnY + offsetY;
            double posZ = entity.getRandomZ(spawnZ) + offsetZ;

            entity.level().addParticle(particleOption, posX, posY, posZ, speedX, speedY, speedZ);
        }
    }

    public static void spawnItemParticles(OWEntity owEntity, ItemStack itemStack, double x, double y, double z) {
        if (!owEntity.level().isClientSide()) {
            ServerLevel serverLevel = (ServerLevel) owEntity.level();
            for (int i = 0; i < 16; i++) {
                serverLevel.sendParticles(
                        new ItemParticleOption(ParticleTypes.ITEM, (itemStack != null && !itemStack.isEmpty()) ? itemStack : Items.APPLE.getDefaultInstance()),
                        x + (owEntity.getRandom().nextDouble() - 0.5) * 0.5,
                        y,
                        z + (owEntity.getRandom().nextDouble() - 0.5) * 0.5,
                        1,
                        (owEntity.getRandom().nextDouble() - 0.5) * 0.1,
                        owEntity.getRandom().nextDouble() * 0.1,
                        (owEntity.getRandom().nextDouble() - 0.5) * 0.1,
                        0.0
                );
            }
        } else {
            for (int i = 0; i < 16; i++) {
                owEntity.level().addParticle(
                        new ItemParticleOption(ParticleTypes.ITEM, (itemStack != null && !itemStack.isEmpty()) ? itemStack : Items.APPLE.getDefaultInstance()),
                        x + (owEntity.getRandom().nextDouble() - 0.5) * 0.5,
                        y,
                        z + (owEntity.getRandom().nextDouble() - 0.5) * 0.5,
                        (owEntity.getRandom().nextDouble() - 0.5) * 0.1,
                        owEntity.getRandom().nextDouble() * 0.1,
                        (owEntity.getRandom().nextDouble() - 0.5) * 0.1
                );
            }
        }
    }

    public static void spawnComposterParticlesAround(OWEntity owEntity, ParticleOptions particleOptions) {
        double centerX = owEntity.getX();
        double centerY = owEntity.getY();
        double centerZ = owEntity.getZ();

        RandomSource random = owEntity.getRandom();

        if (!owEntity.level().isClientSide()) {
            ServerLevel serverLevel = (ServerLevel) owEntity.level();

            for (int i = 0; i < 100; i++) {
                double angle = (2 * java.lang.Math.PI * i) / 100.0;
                double radius = 0.5 + random.nextDouble() * 1.5;

                double particleX = centerX + java.lang.Math.cos(angle) * radius;
                double particleZ = centerZ + java.lang.Math.sin(angle) * radius;
                double particleY = centerY + random.nextDouble() * 2.0;

                serverLevel.sendParticles(
                        particleOptions,
                        particleX,
                        particleY,
                        particleZ,
                        1,
                        (random.nextDouble() - 0.5) * 0.2,
                        random.nextDouble() * 0.2,
                        (random.nextDouble() - 0.5) * 0.2,
                        0.1
                );
            }

            for (int i = 0; i < 50; i++) {
                double randomX = centerX + (random.nextDouble() - 0.5) * 6.0;
                double randomY = centerY + random.nextDouble() * 3.0;
                double randomZ = centerZ + (random.nextDouble() - 0.5) * 6.0;

                serverLevel.sendParticles(
                        particleOptions,
                        randomX,
                        randomY,
                        randomZ,
                        1,
                        (random.nextDouble() - 0.5) * 0.3,
                        random.nextDouble() * 0.3,
                        (random.nextDouble() - 0.5) * 0.3,
                        0.15
                );
            }
        } else {
            for (int i = 0; i < 100; i++) {
                double angle = (2 * java.lang.Math.PI * i) / 100.0;
                double radius = 0.5 + random.nextDouble() * 1.5;

                double particleX = centerX + java.lang.Math.cos(angle) * radius;
                double particleZ = centerZ + java.lang.Math.sin(angle) * radius;
                double particleY = centerY + random.nextDouble() * 2.0;

                owEntity.level().addParticle(
                        particleOptions,
                        particleX,
                        particleY,
                        particleZ,
                        (random.nextDouble() - 0.5) * 0.2,
                        random.nextDouble() * 0.2,
                        (random.nextDouble() - 0.5) * 0.2
                );
            }

            for (int i = 0; i < 50; i++) {
                double randomX = centerX + (random.nextDouble() - 0.5) * 6.0;
                double randomY = centerY + random.nextDouble() * 3.0;
                double randomZ = centerZ + (random.nextDouble() - 0.5) * 6.0;

                owEntity.level().addParticle(
                        particleOptions,
                        randomX,
                        randomY,
                        randomZ,
                        (random.nextDouble() - 0.5) * 0.3,
                        random.nextDouble() * 0.3,
                        (random.nextDouble() - 0.5) * 0.3
                );
            }
        }
    }

}
