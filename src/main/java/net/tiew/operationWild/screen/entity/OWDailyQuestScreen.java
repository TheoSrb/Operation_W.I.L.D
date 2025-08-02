package net.tiew.operationWild.screen.entity;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.tiew.operationWild.OperationWild;
import net.tiew.operationWild.entity.OWEntity;
import net.tiew.operationWild.entity.quests.daily_quests.DailyQuest;
import net.tiew.operationWild.entity.quests.daily_quests.DailyQuestsDate;
import net.tiew.operationWild.entity.quests.daily_quests.OWDailyQuests;
import net.tiew.operationWild.networking.OWNetworkHandler;
import net.tiew.operationWild.networking.packets.to_server.OpenOWInventoryPacket;

import java.util.Calendar;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class OWDailyQuestScreen extends Screen {
    private static final ResourceLocation OW_DAILY_QUESTS_INTERFACE_LOCATION = ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "textures/gui/ow_daily_quests_interface_gui.png");
    private static final ResourceLocation OW_DAILY_QUESTS_LOCKED_INTERFACE_LOCATION = ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "textures/gui/ow_daily_quests_locked_interface_gui.png");
    private static final ResourceLocation ICONS_LOCATION = ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "textures/gui/mob_types.png");
    protected final OWEntity entity;
    private float xMouse;
    private float yMouse;
    protected int imageWidth = 256;
    protected int imageHeight = 224;
    protected Button backButton;

    public OWDailyQuestScreen() {
        super(Component.literal("OWDailyQuestScreen"));
        if (Minecraft.getInstance().player.getRootVehicle() instanceof OWEntity entity) this.entity = entity;
        else this.entity = null;
    }

    public Button createButton(String textOnButton, int color, int positionX, int positionY, int width, int height, Runnable onClick) {
        int i = (this.width - this.imageWidth) / 2;
        int j = (this.height - this.imageHeight) / 2;

        return Button.builder(Component.literal(textOnButton)
                                .setStyle(Style.EMPTY.withColor(TextColor.fromRgb(color))),
                        button -> onClick.run())
                .bounds(i + positionX, j + positionY, width, height)
                .build();
    }

    @Override
    protected void init() {
        super.init();
        int i = (this.width - this.imageWidth) / 2;
        int j = (this.height - this.imageHeight) / 2;
        backButton = createButton("", 0xFFFFFF, -15, 0, 16, 16, () -> OWNetworkHandler.sendToServer(new OpenOWInventoryPacket()));
        this.addRenderableWidget(backButton);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        int i = (this.width - this.imageWidth) / 2;
        int j = (this.height - this.imageHeight) / 2;
        this.xMouse = mouseX;
        this.yMouse = mouseY;

        super.render(graphics, mouseX, mouseY, partialTick);

        graphics.blit(OW_DAILY_QUESTS_INTERFACE_LOCATION, i, j, 0, 0, this.imageWidth, this.imageHeight);

        backButton.visible = true;
        backButton.render(graphics, mouseX, mouseY, partialTick);

        graphics.blit(ICONS_LOCATION, i - 14, j + 1, 0, 130, 13, 13);

        drawQuestProgress(graphics, i, j);

        renderTexts(graphics, i, j);
    }

    private void drawQuestProgress(GuiGraphics graphics, int i, int j) {
        int[][] positionsQuest = {{84, 87}, {19, 172}, {149, 172}};

        if (isQuestLocked(DailyQuestsDate.savedQuests[0].getId())) {
            graphics.blit(OW_DAILY_QUESTS_LOCKED_INTERFACE_LOCATION, i + 77, j + 34, 0, 0, 102, 65);
        }
        if (isQuestLocked(DailyQuestsDate.savedQuests[1].getId())) {
            graphics.blit(OW_DAILY_QUESTS_LOCKED_INTERFACE_LOCATION, i + 12, j + 119, 0, 0, 102, 65);
        }
        if (isQuestLocked(DailyQuestsDate.savedQuests[2].getId())) {
            graphics.blit(OW_DAILY_QUESTS_LOCKED_INTERFACE_LOCATION, i + 142, j + 119, 0, 0, 102, 65);
        }

        for(int q = 0; q < 3; q++) {
            int[] progress = getQuestProgress(DailyQuestsDate.savedQuests[q]);
            graphics.blit(OW_DAILY_QUESTS_INTERFACE_LOCATION, i + positionsQuest[q][0], j + positionsQuest[q][1], 0, 251, (int)(88 * ((float)progress[0] / progress[1])), 5);
        }
    }

    private boolean isQuestLocked(int questId) {
        if (entity == null) return false;

        switch (questId) {
            case 0: return entity.quest0isLocked;
            case 1: return entity.quest1isLocked;
            case 2: return entity.quest2isLocked;
            case 3: return entity.quest3isLocked;
            case 4: return entity.quest4isLocked;
            case 5: return entity.quest5isLocked;
            case 6: return entity.quest6isLocked;
            case 7: return entity.quest7isLocked;
            case 8: return entity.quest8isLocked;
            case 9: return entity.quest9isLocked;
            case 10: return entity.quest10isLocked;
            default: return false;
        }
    }

    private void renderTexts(GuiGraphics graphics, int offsetX, int offsetY) {
        int i = (this.width - this.imageWidth) / 2;
        int j = (this.height - this.imageHeight) / 2;
        int centerX = offsetX + (this.imageWidth / 2);
        int centerY = offsetY + (this.imageHeight / 2);

        Component questMenu = Component.translatable("tooltip.questMenu")
                .setStyle(Style.EMPTY.withBold(true).withUnderlined(true));

        Component quest0 = Component.translatable(DailyQuestsDate.savedQuests[0] != null ? DailyQuestsDate.savedQuests[0].getName() : "NULL");
        Component quest1 = Component.translatable(DailyQuestsDate.savedQuests[1] != null ? DailyQuestsDate.savedQuests[1].getName() : "NULL");
        Component quest2 = Component.translatable(DailyQuestsDate.savedQuests[2] != null ? DailyQuestsDate.savedQuests[2].getName() : "NULL");

        Component timeRemaining = Component.literal(getTimeUntilNextDailyQuests())
                .setStyle(Style.EMPTY.withBold(true).withColor(TextColor.fromRgb(0xCAEEE6)));
        Component remainingTimeText = Component.translatable("tooltip.nextDailyQuests")
                .setStyle(Style.EMPTY.withColor(TextColor.fromRgb(0xFFFFFF)))
                .append(timeRemaining);

        graphics.drawCenteredString(this.font, questMenu, centerX, centerY - 102, 0xFFFFFF);

        graphics.drawCenteredString(this.font, remainingTimeText, centerX, centerY + 90, 0xFFFFFF);

        renderQuestText(graphics, quest0, centerX, centerY - 60, DailyQuestsDate.savedQuests[0] != null ? 0xFFFFFF : 0xFF0000, 85, DailyQuestsDate.savedQuests[0] != null ? DailyQuestsDate.savedQuests[0].getId() : 0);
        renderQuestText(graphics, quest1, centerX - 65, centerY + 25, DailyQuestsDate.savedQuests[1] != null ? 0xFFFFFF : 0xFF0000, 85, DailyQuestsDate.savedQuests[1] != null ? DailyQuestsDate.savedQuests[1].getId() : 0);
        renderQuestText(graphics, quest2, centerX + 65, centerY + 25, DailyQuestsDate.savedQuests[2] != null ? 0xFFFFFF : 0xFF0000, 85, DailyQuestsDate.savedQuests[2] != null ? DailyQuestsDate.savedQuests[2].getId() : 0);


        Component quest0ProgressText = Component.literal(getQuestProgress(DailyQuestsDate.savedQuests[0])[0] + " / " + getQuestProgress(DailyQuestsDate.savedQuests[0])[1])
                .setStyle(Style.EMPTY.withBold(true));
        Component quest1ProgressText = Component.literal(getQuestProgress(DailyQuestsDate.savedQuests[1])[0] + " / " + getQuestProgress(DailyQuestsDate.savedQuests[1])[1])
                .setStyle(Style.EMPTY.withBold(true));
        Component quest2ProgressText = Component.literal(getQuestProgress(DailyQuestsDate.savedQuests[2])[0] + " / " + getQuestProgress(DailyQuestsDate.savedQuests[2])[1])
                .setStyle(Style.EMPTY.withBold(true));

        Component questFinished = Component.translatable("tooltip.questFinished").setStyle(Style.EMPTY.withItalic(true).withColor(0x00FF00));

        if (!isQuestLocked(DailyQuestsDate.savedQuests[0].getId())) graphics.drawCenteredString(this.font, quest0ProgressText, centerX, centerY - 40, 0xFFFFFF);
        else graphics.drawCenteredString(this.font, questFinished, centerX, centerY - 40, 0xFFFFFF);

        if (!isQuestLocked(DailyQuestsDate.savedQuests[1].getId())) graphics.drawCenteredString(this.font, quest1ProgressText, centerX - 65, centerY + 45, 0xFFFFFF);
        else graphics.drawCenteredString(this.font, questFinished, centerX - 65, centerY + 45, 0xFFFFFF);

        if (!isQuestLocked(DailyQuestsDate.savedQuests[2].getId())) graphics.drawCenteredString(this.font, quest2ProgressText, centerX + 65, centerY + 45, 0xFFFFFF);
        else graphics.drawCenteredString(this.font, questFinished, centerX + 65, centerY + 45, 0xFFFFFF);

    }

    private int[] getQuestProgress(DailyQuest quest) {
        switch (quest.getId()) {
            case 0: return new int[]{entity.quest0Progression, 300};
            case 1: return new int[]{entity.quest1Progression, 200};
            case 2: return new int[]{entity.quest2Progression, 100};
            case 3: return new int[]{entity.quest3Progression, 2000};
            case 4: return new int[]{entity.quest4Progression, 1};
            case 5: return new int[]{entity.quest5Progression, 1};
            case 6: return new int[]{entity.quest6Progression, 25};
            case 7: return new int[]{entity.quest7Progression, 1};
            case 8: return new int[]{entity.quest8Progression, 1};
            case 9: return new int[]{entity.quest9Progression, 50};
            case 10: return new int[]{entity.quest10Progression, 8};
            default:
                return new int[]{0,0};
        }
    }

    private void renderQuestText(GuiGraphics graphics, Component text, int centerX, int centerY, int color, int maxWidth, int questId) {
        List<FormattedCharSequence> lines = this.font.split(text, maxWidth);

        String plainText = text.getString();
        int textLength = plainText.length();

        float scale = Math.max(0.5f, 1.0f - Math.max(0, textLength - 20) * 0.01f);

        int lineHeight = this.font.lineHeight;
        int totalHeight = lines.size() * lineHeight;
        int startY = centerY - (totalHeight / 2);

        if (isQuestLocked(questId)) {
            color = (color & 0x00FFFFFF) | (0x80 << 24);
        }

        graphics.pose().pushPose();
        graphics.pose().scale(scale, scale, 1.0f);

        for (int i = 0; i < lines.size(); i++) {
            int scaledCenterX = (int)(centerX * (1 / scale));
            int scaledY = (int)((startY + i * lineHeight) * (1 / scale));
            graphics.drawCenteredString(this.font, lines.get(i), scaledCenterX, scaledY, color);
        }

        graphics.pose().popPose();
    }

    private static String getTimeUntilNextDailyQuests() {
        Calendar now = Calendar.getInstance();
        Calendar nextQuests = Calendar.getInstance();

        nextQuests.set(Calendar.HOUR_OF_DAY, OWDailyQuests.DAILY_QUEST_HOUR);
        nextQuests.set(Calendar.MINUTE, OWDailyQuests.DAILY_QUEST_MINUTES);
        nextQuests.set(Calendar.SECOND, OWDailyQuests.DAILY_QUEST_SECONDS);

        if (now.after(nextQuests)) {
            nextQuests.add(Calendar.DAY_OF_MONTH, 1);
        }

        long diffInMillis = nextQuests.getTimeInMillis() - now.getTimeInMillis();

        long hours = TimeUnit.MILLISECONDS.toHours(diffInMillis);
        diffInMillis -= TimeUnit.HOURS.toMillis(hours);

        long minutes = TimeUnit.MILLISECONDS.toMinutes(diffInMillis);
        diffInMillis -= TimeUnit.MINUTES.toMillis(minutes);

        long seconds = TimeUnit.MILLISECONDS.toSeconds(diffInMillis);

        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }

}