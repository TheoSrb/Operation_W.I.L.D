package net.tiew.operationWild.screen.player.adventurer_manuscript.chapter;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.tiew.operationWild.OperationWild;
import net.tiew.operationWild.entity.OWEntity;

public class OWChapters extends OWChapter {

    public static final int maxLength = 110;

    public static void render(int x, int y, float scale, float alpha, EntityType<? extends OWEntity> entity, EntityType<? extends OWEntity> entityNext, EntityType<? extends OWEntity> entityPrevious) {
        Component title = Component.translatable("chapter.entity.ow." + (entity != null ? entity.toString().split("entity.ow.")[1] : ""));
        Component title2 = Component.translatable("chapter.entity.ow." + (entityNext != null ? entityNext.toString().split("entity.ow.")[1] : ""));
        Component title3 = Component.translatable("chapter.entity.ow." + (entityPrevious != null ? entityPrevious.toString().split("entity.ow.")[1] : ""));
        String titleStr = title.getString();
        String titleStr2 = title2.getString();
        String titleStr3 = title3.getString();

        assert Minecraft.getInstance().level != null;
        OWEntity entity2 = entity != null ? entity.create(Minecraft.getInstance().level) : null;
        OWEntity entity3 = entityNext != null ? entityNext.create(Minecraft.getInstance().level) : null;
        OWEntity entity4 = entityPrevious != null ? entityPrevious.create(Minecraft.getInstance().level) : null;


        if (entity2 != null) {
            String tamingExperience = String.valueOf(entity2.getTamingExperience());
            ResourceLocation entityTexture = ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "textures/gui/mob_page/misc/ow_entity_journal_interface_taming_experience_gui.png");

            drawCombinedTextAndImageOnLeftPage(
                    titleStr, x + 25 - (Minecraft.getInstance().font.width(titleStr) / 2), y - 1, scale * 2, alpha, 150, 0xc8be95,
                    titleStr, x + 24 - (Minecraft.getInstance().font.width(titleStr) / 2), y - 2, scale * 2, alpha, 150, 0x887c57,
                    tamingExperience, x + 60, y + 20, scale, alpha, maxLength, 0x887c57,
                    entityTexture, x + 42, y + 15, 1.35f, alpha, 110, 12, 11, 11
            );
        } else {
            drawCombinedTextAndImageOnLeftPage(
                    "", x + 25 - (Minecraft.getInstance().font.width(titleStr) / 2), y - 1, scale * 2, alpha, 150, 0xc8be95,
                    "", x + 24 - (Minecraft.getInstance().font.width(titleStr) / 2), y - 2, scale * 2, alpha, 150, 0x887c57,
                    "", x + 60, y + 20, scale, alpha, maxLength, 0x887c57,
                    ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "textures/gui/adventurer_manuscript/empty.png"), x + 42, y + 15, 1.35f, alpha, 110, 12, 11, 11
            );
        }


        if (entity3 != null) {
            String tamingExperience = String.valueOf(entity3.getTamingExperience());
            ResourceLocation entityTexture = ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "textures/gui/mob_page/misc/ow_entity_journal_interface_taming_experience_gui.png");
            ResourceLocation entityTextureChapter = ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "textures/gui/adventurer_manuscript/" + entityNext.toString().split("entity.ow.")[1] + "_chapter.png");
            drawCombinedTextAndImageOnNextLeftPage(
                    titleStr2, x + 25 - (Minecraft.getInstance().font.width(titleStr2) / 2), y - 1, scale * 2, alpha, 150, 0xc8be95,
                    titleStr2, x + 24 - (Minecraft.getInstance().font.width(titleStr2) / 2), y - 2, scale * 2, alpha, 150, 0x887c57,
                    tamingExperience, x + 60, y + 20, scale, alpha, maxLength, 0x887c57,
                    entityTexture, x + 42, y + 15, 1.35f, alpha, 110, 12, 11, 11,
                    entityTextureChapter, x + 2, y + 30, 1.0f, alpha, 762, 885, 97, 114
            );
        } else {
            drawCombinedTextAndImageOnNextLeftPage(
                    "", x + 25 - (Minecraft.getInstance().font.width(titleStr) / 2), y - 1, scale * 2, alpha, 150, 0xc8be95,
                    "", x + 24 - (Minecraft.getInstance().font.width(titleStr) / 2), y - 2, scale * 2, alpha, 150, 0x887c57,
                    "", x + 60, y + 20, scale, alpha, maxLength, 0x887c57,
                    ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "textures/gui/adventurer_manuscript/empty.png"), x + 42, y + 15, 1.35f, alpha, 110, 12, 11, 11,
                    ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "textures/gui/adventurer_manuscript/empty.png"), x + 42, y + 15, 1.35f, alpha, 110, 12, 11, 11
            );
        }

        if (entity4 != null) {
            String tamingExperience = String.valueOf(entity4.getTamingExperience());
            ResourceLocation entityTexture = ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "textures/gui/mob_page/misc/ow_entity_journal_interface_taming_experience_gui.png");
            ResourceLocation entityTextureChapter = ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "textures/gui/adventurer_manuscript/" + entityPrevious.toString().split("entity.ow.")[1] + "_chapter.png");

            drawCombinedTextAndImageOnPreviousLeftPage(
                    titleStr3, x + 25 - (Minecraft.getInstance().font.width(titleStr3) / 2), y - 1, scale * 2, alpha, 150, 0xc8be95,
                    titleStr3, x + 24 - (Minecraft.getInstance().font.width(titleStr3) / 2), y - 2, scale * 2, alpha, 150, 0x887c57,
                    tamingExperience, x + 60, y + 20, scale, alpha, maxLength, 0x887c57,
                    entityTexture, x + 42, y + 15, 1.35f, alpha, 110, 12, 11, 11,
                    entityTextureChapter, x + 2, y + 30, 1.0f, alpha, 762, 885, 97, 114
            );
        } else {
            drawCombinedTextAndImageOnPreviousLeftPage(
                    "", x + 25 - (Minecraft.getInstance().font.width(titleStr) / 2), y - 1, scale * 2, alpha, 150, 0xc8be95,
                    "", x + 24 - (Minecraft.getInstance().font.width(titleStr) / 2), y - 2, scale * 2, alpha, 150, 0x887c57,
                    "", x + 60, y + 20, scale, alpha, maxLength, 0x887c57,
                    ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "textures/gui/adventurer_manuscript/empty.png"), x + 42, y + 15, 1.35f, alpha, 110, 12, 11, 11,
                    ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "textures/gui/adventurer_manuscript/empty.png"), x + 42, y + 15, 1.35f, alpha, 110, 12, 11, 11
            );
        }
    }


    public static class TigerChapter extends OWChapter {
        public static void render(int x, int y, float scale, float alpha, int page) {
            Component text_1 = Component.translatable("adventurer_manuscript.entity.tiger.page_1");
            Component text_2 = Component.translatable("adventurer_manuscript.entity.tiger.page_2");
            Component text_3 = Component.translatable("adventurer_manuscript.entity.tiger.page_3");
            Component text_4 = Component.translatable("adventurer_manuscript.entity.tiger.page_4");
            Component text_5 = Component.translatable("adventurer_manuscript.entity.tiger.page_5");
            Component text_6 = Component.translatable("adventurer_manuscript.entity.tiger.page_6");
            Component text_7 = Component.translatable("adventurer_manuscript.entity.tiger.page_7");




            switch (page) {
                case 1:
                    drawTextOnRightPage(text_1, x, y, scale, alpha, maxLength, 0x887c57);

                    drawTextOnNextLeftPage(text_2, x, y, scale, alpha, maxLength, 0x887c57);

                    drawTextAndImageOnNextRightPage(text_3, x, y, scale, alpha, maxLength, 0x887c57,
                            ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "textures/gui/adventurer_manuscript/tiger_widget_0.png"),
                            x + 2, y + 83, 0.85f, alpha, 416, 922, 126, 77);
                    break;
                case 2:
                    drawTextOnLeftPage(text_2, x, y, scale, alpha, maxLength, 0x887c57);

                    drawTextAndImageOnRightPage(text_3, x, y, scale, alpha, maxLength, 0x887c57,
                            ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "textures/gui/adventurer_manuscript/tiger_widget_0.png"),
                            x + 2, y + 83, 0.85f, alpha, 416, 922, 126, 77);

                    drawTextOnNextLeftPage(text_4, x, y, scale, alpha, maxLength, 0x887c57);
                    drawTextOnNextRightPage(text_5, x, y, scale, alpha, maxLength, 0x887c57);

                    drawTextOnPreviousRightPage(text_1, x, y, scale, alpha, maxLength, 0x887c57);
                    break;
                case 3:
                    drawTextOnLeftPage(text_4, x, y, scale, alpha, maxLength, 0x887c57);
                    drawTextOnRightPage(text_5, x, y, scale, alpha, maxLength, 0x887c57);

                    drawTextOnNextLeftPage(text_6, x, y, scale, alpha, maxLength, 0x887c57);
                    drawTextOnNextRightPage(text_7, x, y, scale, alpha, maxLength, 0x887c57);

                    drawTextOnPreviousLeftPage(text_2, x, y, scale, alpha, maxLength, 0x887c57);
                    drawTextOnPreviousRightPage(text_3, x, y, scale, alpha, maxLength, 0x887c57);

                    drawTextAndImageOnPreviousRightPage(text_3, x, y, scale, alpha, maxLength, 0x887c57,
                            ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "textures/gui/adventurer_manuscript/tiger_widget_0.png"),
                            x + 2, y + 83, 0.85f, alpha, 416, 922, 126, 77);
                    break;
                case 4:
                    drawTextOnLeftPage(text_6, x, y, scale, alpha, maxLength, 0x887c57);
                    drawTextOnRightPage(text_7, x, y, scale, alpha, maxLength, 0x887c57);

                    drawTextOnPreviousLeftPage(text_4, x, y, scale, alpha, maxLength, 0x887c57);
                    drawTextOnPreviousRightPage(text_5, x, y, scale, alpha, maxLength, 0x887c57);
                    break;
            }
        }
    }
}