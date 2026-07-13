package net.tiew.operationWild.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.toasts.Toast;
import net.minecraft.client.gui.components.toasts.ToastComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.tiew.operationWild.OperationWild;

/**
 * Toast « façon succès » (en haut à droite) pour les quêtes quotidiennes : progression (barre qui se
 * remplit + valeur qui grimpe sur 2 s) ou complétion. Le glissement d'entrée/sortie est géré par le
 * {@link ToastComponent} de Minecraft.
 */
@OnlyIn(Dist.CLIENT)
public class OWDailyQuestToast implements Toast {

    private static final ResourceLocation ICON =
            ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "textures/gui/ow_inventory_gui.png");

    private static final long DISPLAY_MS = 4200L;
    private static final long COUNT_MS = 2000L;   // durée de la montée, indépendante du delta
    private static final int W = 182;
    private static final int H = 38;

    private static final int RAIL = 0xFF232A33;
    // Dégradé de la barre : 0 % (jaune orangé) → 100 % (vert), comme l'écran des quêtes.
    private static final int FILL_LOW = 0xF3C24C;
    private static final int FILL_HIGH = 0x8BE45A;
    // Fond bleu ardoise fixe (opacité réduite) + couleurs du biseau interne.
    private static final int BG_RGB = 0x333B47;
    private static final int BG = 0xC0000000 | BG_RGB;

    private final Component title;
    private final int accent;        // couleur du texte d'état (« +delta » / « Terminée »)
    private final int borderColor;   // liseré gauche = couleur du tier
    private final Object token;      // dédoublonnage : même quête = même toast à la fois

    private final boolean isProgress;
    private final int delta, fromProg, toProg, max;
    private final Component completionText;

    private OWDailyQuestToast(Component title, int accent, int tierColor, Object token, boolean isProgress,
                             int delta, int fromProg, int toProg, int max, Component completionText) {
        this.title = title;
        this.accent = accent;
        this.borderColor = 0xFF000000 | (tierColor & 0xFFFFFF);
        this.token = token;
        this.isProgress = isProgress;
        this.delta = delta;
        this.fromProg = fromProg;
        this.toProg = toProg;
        this.max = max;
        this.completionText = completionText;
    }

    /** Toast de progression : barre animée de {@code prog-delta} vers {@code prog}. */
    public static OWDailyQuestToast progress(Component questName, int delta, int prog, int max, int tierColor, int questId) {
        return new OWDailyQuestToast(questName, 0xFF8BE45A, tierColor, Integer.valueOf(questId), true, delta,
                Math.max(0, prog - delta), prog, Math.max(1, max), null);
    }

    /** Toast de complétion : « nom » + « Quête Terminée ». */
    public static OWDailyQuestToast completed(Component questName, int tierColor) {
        return new OWDailyQuestToast(questName, 0xFFFDD85F, tierColor, null, false, 0, 0, 0, 0,
                Component.translatable("tooltip.questFinished"));
    }

    @Override
    public Object getToken() {
        return token != null ? token : Toast.NO_TOKEN;
    }

    private static int mix(int a, int b, float t) {
        int ar = (a >> 16) & 0xFF, ag = (a >> 8) & 0xFF, ab = a & 0xFF;
        int br = (b >> 16) & 0xFF, bg = (b >> 8) & 0xFF, bb = b & 0xFF;
        int r = (int) (ar + (br - ar) * t);
        int gg = (int) (ag + (bg - ag) * t);
        int bl = (int) (ab + (bb - ab) * t);
        return (r << 16) | (gg << 8) | bl;
    }

    @Override
    public int width() { return W; }

    @Override
    public int height() { return H; }

    @Override
    public Visibility render(GuiGraphics g, ToastComponent toastComponent, long timeSinceLastVisible) {
        Font font = Minecraft.getInstance().font;

        g.fill(0, 0, W, H, BG);
        // Biseau interne : haut/gauche plus clair (lumière), bas/droite plus sombre (ombre).
        int hl = 0xFF000000 | mix(BG_RGB, 0xFFFFFF, 0.38f);
        int sh = 0xFF000000 | mix(BG_RGB, 0x000000, 0.50f);
        g.fill(0, 0, W, 1, hl);
        g.fill(0, 0, 1, H, hl);
        g.fill(0, H - 1, W, H, sh);
        g.fill(W - 1, 0, W, H, sh);
        g.fill(0, 0, 3, H, borderColor);   // liseré d'accent (tier) par-dessus la lumière gauche

        g.blit(ICON, 7, (H - 16) / 2, 176, 48, 16, 16);
        g.fill(27, 5, 28, H - 5, 0xFF4A525C);

        // Titre tronqué avec « … ».
        int maxTitleW = W - 40;
        Component shownTitle = title;
        if (font.width(title) > maxTitleW) {
            String cut = font.plainSubstrByWidth(title.getString(), maxTitleW - font.width("…"));
            shownTitle = Component.literal(cut + "…");
        }
        g.drawString(font, shownTitle, 33, 6, 0xFFFFFFFF, false);

        if (!isProgress) {
            g.drawString(font, completionText, 33, 20, accent, false);
            return timeSinceLastVisible < DISPLAY_MS ? Visibility.SHOW : Visibility.HIDE;
        }

        // Montée animée (easeOutCubic) sur COUNT_MS, quel que soit le delta.
        float t = Mth.clamp(timeSinceLastVisible / (float) COUNT_MS, 0f, 1f);
        float eased = 1f - (float) Math.pow(1f - t, 3);
        // Ratio calculé DIRECTEMENT en flottant (remplissage au pixel, lisse) — pas via l'entier affiché.
        float fromRatio = fromProg / (float) max, toRatio = toProg / (float) max;
        float ratio = Mth.clamp(fromRatio + (toRatio - fromRatio) * eased, 0f, 1f);
        int curVal = Math.round(fromProg + (toProg - fromProg) * eased);

        // Ligne : « +delta » à gauche, valeur qui grimpe à droite.
        g.drawString(font, Component.literal("+" + delta), 33, 18, accent, false);
        String cur = curVal + " / " + max;
        g.drawString(font, cur, W - 8 - font.width(cur), 18, 0xFFDDE4EA, false);

        // Barre de progression animée.
        int barX = 33, barY = H - 8, barW = W - barX - 8, barH = 4;
        g.fill(barX, barY, barX + barW, barY + barH, RAIL);
        g.fill(barX, barY, barX + barW, barY + 1, 0xFF191E25);        // ombre interne
        int filled = (int) (barW * ratio);
        // Remplissage en dégradé orange→vert (couleur selon la position), avec lumière (haut) + ombre (bas).
        for (int px = 0; px < filled; px++) {
            float tt = barW > 1 ? (float) px / (barW - 1) : 1f;
            int c = mix(FILL_LOW, FILL_HIGH, tt);
            int light = mix(c, 0xFFFFFF, 0.45f);
            int shadow = mix(c, 0x000000, 0.40f);
            int cxp = barX + px;
            g.fill(cxp, barY, cxp + 1, barY + barH, 0xFF000000 | c);
            g.fill(cxp, barY, cxp + 1, barY + 1, 0xFF000000 | light);
            g.fill(cxp, barY + barH - 1, cxp + 1, barY + barH, 0xFF000000 | shadow);
        }

        return timeSinceLastVisible < DISPLAY_MS ? Visibility.SHOW : Visibility.HIDE;
    }
}
