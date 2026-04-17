package net.tiew.operationWild.quests;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.toasts.Toast;
import net.minecraft.client.gui.components.toasts.ToastComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class QuestCompletionToast implements Toast {

    private static final long DISPLAY_MS = 5000L;
    private static final int W = 180;
    private static final int H = 36;

    private static final int COLOR_GOLD   = 0xFFfdd85f;
    private static final int COLOR_WHITE  = 0xFFFFFFFF;
    private static final int COLOR_GRAY   = 0xFF888888;
    private static final int COLOR_BG     = 0xF0151820;
    private static final int COLOR_BORDER = 0xFF565656;

    private final Component title;
    private final Component subtitle = Component.translatable("quest.completed")
            .setStyle(Style.EMPTY.withColor(COLOR_GOLD).withBold(true));

    public QuestCompletionToast(CosmeticsQuest quest) {
        this.title = Component.translatable(quest.getTitleKey())
                .setStyle(Style.EMPTY.withColor(COLOR_WHITE));
    }

    @Override
    public int width()  { return W; }

    @Override
    public int height() { return H; }

    @Override
    public Visibility render(GuiGraphics g, ToastComponent toastComponent, long timeSinceLastVisible) {
        Font font = Minecraft.getInstance().font;

        // Fond
        g.fill(0, 0, W, H, COLOR_BG);

        // Bordure gauche dorée
        g.fill(0, 0, 3, H, COLOR_GOLD);

        // Bordures haut/bas
        g.fill(0, 0,     W, 1,     COLOR_BORDER);
        g.fill(0, H - 1, W, H,     COLOR_BORDER);
        g.fill(W - 1, 0, W, H,     COLOR_BORDER);

        // Icône (étoile de nether = icône "succès" universel)
        g.renderFakeItem(new ItemStack(Items.NETHER_STAR), 6, (H - 16) / 2);

        // Ligne décorative verticale après l'icône
        g.fill(26, 6, 27, H - 6, 0xFF2A2A2A);

        // Textes
        g.drawString(font, subtitle, 32, 8,  COLOR_GOLD,  false);
        g.drawString(font, title,    32, 20, COLOR_WHITE, false);

        return timeSinceLastVisible < DISPLAY_MS ? Visibility.SHOW : Visibility.HIDE;
    }
}
