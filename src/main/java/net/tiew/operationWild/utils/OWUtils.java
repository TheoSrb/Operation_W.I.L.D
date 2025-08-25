package net.tiew.operationWild.utils;

import net.minecraft.core.particles.*;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.network.protocol.game.ClientboundSetSubtitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.tiew.operationWild.entity.OWEntity;
import org.joml.Math;
import org.joml.Vector3f;
import net.tiew.operationWild.entity.custom.living.BoaEntity;

public class OWUtils {

    public static boolean RANDOM(int i) { return OWEntity.RANDOM.nextInt(i) == 0;}

    public static double generateRandomInterval(double min, double max) { return (min + (Math.random() * (max - min)));}

    public static double determinateMinAndMax(int average, double percent) {
        int min = (int) (average * (1 - (percent / 100)));
        int max = (int) (average * (1 + percent / 100));
        return OWUtils.generateRandomInterval(min, max);
    }

    public static void showMode(ServerPlayer player, boolean isAggressive) {
        Component label = Component.translatable("tooltip.mode").withStyle(style -> style.withColor(TextColor.fromRgb(0xFFFFFF)));

        Component translatedMode = Component.translatable(isAggressive ? "tooltip.modeAggressive" : "tooltip.modePassive")
                .withStyle(style -> style.withColor(TextColor.fromRgb(isAggressive ? 0xFF5555 : 0x55FF55)).withBold(true));

        player.displayClientMessage(label.copy().append(translatedMode), true);
    }

    public static float getSpeedBlocksPerSecond(OWEntity entity) {
        if (entity.canIncreasesSpeedDuringSprint()) {
            return (((entity.getSpeed() * 20) / 3) * ((entity.vehicleRunSpeedMultiplier() * (0.5f + ((float) (Math.min(100, 50 / 100))) / 2) * 1.75f)));
        }
        return (entity.getSpeed() * 20) * (entity.vehicleRunSpeedMultiplier() / 1.75f);
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
                    startX, entity instanceof BoaEntity ? startY + 1 : startY, startZ,
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


}
