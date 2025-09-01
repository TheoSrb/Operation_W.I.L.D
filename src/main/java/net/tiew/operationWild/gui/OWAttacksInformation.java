package net.tiew.operationWild.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.tiew.operationWild.OperationWild;
import net.tiew.operationWild.entity.OWEntity;
import net.tiew.operationWild.entity.animals.terrestrial.TigerEntity;
import net.tiew.operationWild.core.OWKeysBinding;

import java.util.List;

public class OWAttacksInformation {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "textures/overlay/attacks_information.png");
    private static final ResourceLocation CARDS = ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "textures/overlay/boa_cards.png");

    public static void render(GuiGraphics guiGraphics, int screenWidth, int screenHeight) {
        Player rider = Minecraft.getInstance().player;
        if (rider != null) {
            LivingEntity entity = (LivingEntity) rider.getVehicle();
            if (entity != null) {
                if (entity instanceof OWEntity owEntity) {
                    createOverlayCharge(guiGraphics, screenWidth, screenHeight, 256, owEntity);
                }
            }
        }
    }

    public static float getEntityAttackSpeed(OWEntity owEntity) {
        switch(owEntity.getClass().getSimpleName()) {
            case "TigerEntity": return 0.65f;
            case "BoaEntity": return 0.55f;
            case "PeacockEntity": return 0.50f;
            default: return 0f;
        }
    }

    private static String chooseAnimalType(OWEntity entity) {
        switch (entity.getClass().getSimpleName()) {
            case "TigerEntity": return "entity.ow.tiger";
            case "BoaEntity": return "entity.ow.boa";
            case "PeacockEntity": return "entity.ow.peacock";
            default: return "entity.ow.tiger";
        }
    }

    private static float getSecondAttackCooldown(OWEntity entity) {
        switch (entity.getClass().getSimpleName()) {
            case "TigerEntity": return 30;
            case "BoaEntity": return 40;
            case "PeacockEntity": return 0;
            default: return 0;
        }
    }

    public static void createOverlayCharge(GuiGraphics guiGraphics, int screenWidth, int screenHeight, int maxScale, OWEntity owEntity) {
        int imageWidth = 256;
        int imageHeight = 136;

        int cardsWidth = 20;
        int cardsHeight = 20;
        int spacing = 78;

        int x = (screenWidth - imageWidth) / 2;
        int y = (screenHeight - imageHeight) / 2;

        int firstCardX = x + (cardsWidth * 2);
        int secondCardX = x + (cardsWidth * 2) + spacing;
        int thirdCardX = x + (cardsWidth * 2) + (spacing * 2);

        int cardsY = y + (cardsHeight / 2);
        float policyScaleTITLE = 0.7f;
        float policyScaleDESCRIPTION = 0.5f;

        int entityCardPosition = 0;

        switch (owEntity.getClass().getSimpleName()) {
            case "TigerEntity" -> entityCardPosition = 40;
            case "PeacockEntity" -> entityCardPosition = 80;
            case "BoaEntity" -> entityCardPosition = 0;
            default -> entityCardPosition = 0;
        }

        guiGraphics.blit(TEXTURE, x, y, 0, 0, imageWidth, imageHeight);

        guiGraphics.blit(CARDS, firstCardX, cardsY, 0, entityCardPosition, cardsWidth, cardsHeight);
        guiGraphics.blit(CARDS, secondCardX, cardsY, 20, entityCardPosition, cardsWidth, cardsHeight);
        guiGraphics.blit(CARDS, thirdCardX, cardsY, 40, entityCardPosition, cardsWidth, cardsHeight);

        guiGraphics.drawCenteredString(Minecraft.getInstance().font, Component.literal(Minecraft.getInstance().options.keyAttack.getTranslatedKeyMessage().getString()).setStyle(Style.EMPTY), firstCardX + (cardsWidth / 2), cardsY + 100, 0xFFFFFF);
        guiGraphics.drawCenteredString(Minecraft.getInstance().font, Component.literal(Minecraft.getInstance().options.keyUse.getTranslatedKeyMessage().getString()).setStyle(Style.EMPTY), secondCardX + (cardsWidth / 2), cardsY + 100, 0xFFFFFF);
        guiGraphics.drawCenteredString(Minecraft.getInstance().font, Component.literal(OWKeysBinding.OW_ULTIMATE.getTranslatedKeyMessage().getString()).setStyle(Style.EMPTY), thirdCardX + (cardsWidth / 2), cardsY + 100, 0xFFFFFF);

        Component firstTitle = Component.translatable("attacks.title1." + owEntity.getClass().getSimpleName()).setStyle(Style.EMPTY.withUnderlined(true));
        Component secondTitle = Component.translatable("attacks.title2." + owEntity.getClass().getSimpleName()).setStyle(Style.EMPTY.withUnderlined(true));
        Component thirdTitle = Component.translatable("attacks.title3." + owEntity.getClass().getSimpleName()).setStyle(Style.EMPTY.withUnderlined(true));

        Component attackDescription1 = Component.translatable("attacks.description1." + owEntity.getClass().getSimpleName(),
                Component.translatable(chooseAnimalType(owEntity)).setStyle(Style.EMPTY.withBold(true).withColor(owEntity.getEntityColor())),
                Component.literal(String.valueOf(getEntityAttackSpeed(owEntity))).setStyle(Style.EMPTY.withBold(true).withColor(0xFFFFFF)),
                Component.literal(String.valueOf(Math.round(owEntity.getDamageToClient() * 10.0) / 10.0)).setStyle(Style.EMPTY.withBold(true)).setStyle(Style.EMPTY.withBold(true).withColor(0xFFFFFF)),
                Component.literal("33").setStyle(Style.EMPTY.withBold(true).withColor(0xFFFFFF)),
                Component.literal("8.8").setStyle(Style.EMPTY.withBold(true).withColor(0xFFFFFF)),
                Component.literal("17.6").setStyle(Style.EMPTY.withBold(true).withColor(0xFFFFFF)));

        Component attackDescription2 = Component.translatable("attacks.description2." + owEntity.getClass().getSimpleName(),
                Component.translatable(chooseAnimalType(owEntity)).setStyle(Style.EMPTY.withBold(true).withColor(owEntity.getEntityColor())),
                Component.literal(owEntity instanceof TigerEntity ? String.valueOf(getSecondAttackCooldown(owEntity)) : "3").setStyle(Style.EMPTY.withBold(true).withColor(0xFFFFFF)),
                Component.literal("5").setStyle(Style.EMPTY.withBold(true).withColor(0xFFFFFF)),
                Component.literal("40").setStyle(Style.EMPTY.withBold(true).withColor(0xFFFFFF)));

        Component attackDescription3 = Component.translatable("attacks.description3." + owEntity.getClass().getSimpleName(),
                Component.translatable(chooseAnimalType(owEntity))
                        .setStyle(Style.EMPTY.withBold(true).withColor(owEntity.getEntityColor())),
                Component.literal("15")
                        .setStyle(Style.EMPTY.withBold(true).withColor(0xFFFFFF)),
                Component.literal("200")
                        .setStyle(Style.EMPTY.withBold(true).withColor(0xFFFFFF)),
                Component.literal("150")
                        .setStyle(Style.EMPTY.withBold(true).withColor(0xFFFFFF)),
                Component.literal("125")
                        .setStyle(Style.EMPTY.withBold(true).withColor(0xFFFFFF)),
                Component.literal("135")
                        .setStyle(Style.EMPTY.withBold(true).withColor(0xFFFFFF)),
                Component.literal("5")
                        .setStyle(Style.EMPTY.withBold(true).withColor(0xFFFFFF)));

        guiGraphics.pose().pushPose();
        guiGraphics.pose().scale(policyScaleTITLE, policyScaleTITLE, 1.0f);

        guiGraphics.drawCenteredString(Minecraft.getInstance().font, firstTitle, (int) ((firstCardX + (float) cardsWidth / 2) * (1 / policyScaleTITLE)), (int) ((cardsY + 20 + 5) * (1 / policyScaleTITLE)), 0xFFFFFF);
        guiGraphics.drawCenteredString(Minecraft.getInstance().font, secondTitle, (int) ((secondCardX + (float) cardsWidth / 2) * (1 / policyScaleTITLE)), (int) ((cardsY + 20 + 5) * (1 / policyScaleTITLE)), 0xFFFFFF);
        guiGraphics.drawCenteredString(Minecraft.getInstance().font, thirdTitle, (int) ((thirdCardX + (float) cardsWidth / 2) * (1 / policyScaleTITLE)), (int) ((cardsY + 20 + 5) * (1 / policyScaleTITLE)), 0xFFFFFF);

        guiGraphics.pose().popPose();

        guiGraphics.pose().pushPose();
        guiGraphics.pose().scale(policyScaleDESCRIPTION, policyScaleDESCRIPTION, 1.0f);

        List<FormattedCharSequence> linesDescription1 = Minecraft.getInstance().font.split(attackDescription1, 140);
        int startY = (int) ((cardsY + 20 + 5 + 15) * (1 / policyScaleDESCRIPTION));
        for (int i = 0; i < linesDescription1.size(); i++) {
            guiGraphics.drawCenteredString(Minecraft.getInstance().font, linesDescription1.get(i), (int) ((firstCardX + (float) cardsWidth / 2) * (1 / policyScaleDESCRIPTION)), startY + i * 10, 0xcdcdcd);
        }

        List<FormattedCharSequence> linesDescription2 = Minecraft.getInstance().font.split(attackDescription2, 140);
        for (int i = 0; i < linesDescription2.size(); i++) {
            guiGraphics.drawCenteredString(Minecraft.getInstance().font, linesDescription2.get(i), (int) ((secondCardX + (float) cardsWidth / 2) * (1 / policyScaleDESCRIPTION)), startY + i * 10, 0xcdcdcd);
        }

        List<FormattedCharSequence> linesDescription3 = Minecraft.getInstance().font.split(attackDescription3, 140);
        for (int i = 0; i < linesDescription3.size(); i++) {
            guiGraphics.drawCenteredString(Minecraft.getInstance().font, linesDescription3.get(i), (int) ((thirdCardX + (float) cardsWidth / 2) * (1 / policyScaleDESCRIPTION)), startY + i * 10, 0xcdcdcd);
        }

        guiGraphics.pose().popPose();
    }
}
