package net.tiew.operationWild.screen.player.adventurer_manuscript.chapter;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.tiew.operationWild.OperationWild;
import net.tiew.operationWild.entity.OWEntity;

public class OWChapters extends OWChapter {

    public static final int maxLength = 120;

    public static void render(int x, int y, float scale, float alpha, EntityType<? extends OWEntity> entity) {
        Component title = Component.translatable("chapter.entity.ow." + entity.toString().split("entity.ow.")[1]);
        String titleStr = title.getString();

        assert Minecraft.getInstance().level != null;
        OWEntity entity2 = entity.create(Minecraft.getInstance().level);

        if (entity2 != null) {
            String tamingExperience = String.valueOf(entity2.getTamingExperience());
            ResourceLocation entityTexture = ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "textures/gui/mob_page/misc/ow_entity_journal_interface_taming_experience_gui.png");

            drawCombinedTextAndImageOnLeftPage(
                    titleStr, x + 28 - (Minecraft.getInstance().font.width(titleStr) / 2), y - 1, scale * 2, alpha, maxLength, 0xc8be95,
                    titleStr, x + 27 - (Minecraft.getInstance().font.width(titleStr) / 2), y - 2, scale * 2, alpha, maxLength, 0x887c57,
                    tamingExperience, x + 60, y + 20, scale, alpha, maxLength, 0x887c57,
                    entityTexture, x + 42, y + 15, 1.35f, alpha, 110, 12, 11, 11
            );
        }
    }


    public static class TigerChapter extends OWChapter {
        public static void render(int x, int y, float scale, float alpha, int page) {
            Component text_1 = Component.translatable("tiger.page1");
            Component text_2 = Component.translatable("tiger.page2");
            Component text_3 = Component.translatable("tiger.page3");
            Component text_4 = Component.translatable("tiger.page4");

            switch (page) {
                case 1:
                    drawTextOnRightPage(text_1, x, y, scale, alpha, maxLength, 0x887c57);
                    break;
                case 2:
                    drawTextOnLeftPage(text_2, x, y, scale, alpha, maxLength, 0x887c57);
                    drawTextOnRightPage(text_3, x, y, scale, alpha, maxLength, 0x887c57);
                    break;
                case 3:
                    drawTextOnLeftPage(text_4, x, y, scale, alpha, maxLength, 0x887c57);
                    break;
            }
        }
    }
}