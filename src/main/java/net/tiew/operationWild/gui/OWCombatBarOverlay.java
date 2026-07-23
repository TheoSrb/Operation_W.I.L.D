package net.tiew.operationWild.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.tiew.operationWild.entity.OWEntity;
import net.tiew.operationWild.entity.misc.Submarine;

/**
 * Jauge de sortie de combat de la monture : c'est la barre d'expérience vanilla elle-même, teintée
 * en orange, qui prend le relais tant que le décompte court.
 *
 * <p>Elle répond à une question précise : <i>quand ma bête pourra-t-elle enfin manger ?</i> Une
 * créature ne se ravitaille pas au milieu d'un échange de coups. Chaque coup porté ou encaissé
 * remplit la jauge et la maintient pleine cinq secondes, puis elle fond vers la gauche pendant cinq
 * secondes de plus. Tant qu'il en reste, la monture ignore la nourriture rangée dans son inventaire ;
 * une fois la jauge vide, elle y pioche une bouchée toutes les trois secondes.</p>
 *
 * <p>Reprendre le sprite vanilla plutôt que d'en dessiner un autre garde le HUD d'un seul tenant :
 * même bande, même relief, même position — seule la couleur change, et elle suffit à dire que ce
 * n'est plus de l'expérience qu'on lit. L'expérience du joueur n'a de toute façon aucun rôle une
 * fois en selle.</p>
 */
@OnlyIn(Dist.CLIENT)
public final class OWCombatBarOverlay {

    private OWCombatBarOverlay() {}

    private static final ResourceLocation BAR_BACKGROUND =
            ResourceLocation.withDefaultNamespace("hud/experience_bar_background");
    private static final ResourceLocation BAR_PROGRESS =
            ResourceLocation.withDefaultNamespace("hud/experience_bar_progress");

    /**
     * Facteurs de teinte du sprite de progression, qui culmine à (180, 245, 126). Le résultat vise
     * un orange ambré, ~(255, 150, 20) sur les pixels les plus clairs.
     *
     * <p>Le rouge dépasse 1 volontairement : la teinte est un produit, et sans le pousser au-delà on
     * ne pourrait qu'assombrir un vert — jamais le virer à l'orange. Le shader borne le résultat.</p>
     */
    private static final float TINT_R = 1.42f, TINT_G = 0.61f, TINT_B = 0.16f;

    /** La monture du joueur local, si elle relève de cette jauge. */
    private static OWEntity mount() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return null;
        if (!(mc.player.getVehicle() instanceof OWEntity owE) || owE instanceof Submarine) return null;
        return owE.isTame() ? owE : null;
    }

    /** Vrai si la jauge prend la place de la barre vanilla cette frame. */
    public static boolean isActive() {
        OWEntity mount = mount();
        return mount != null && mount.getFightCooldown() > 0;
    }

    public static void render(GuiGraphics g, int screenW, int screenH) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.options.hideGui) return;

        OWEntity mount = mount();
        if (mount == null || mount.getFightCooldown() <= 0) return;

        // Mêmes repères que Gui#renderExperienceBar : la jauge doit tomber au pixel près sur la barre
        // qu'elle remplace.
        int x = screenW / 2 - 91;
        int y = screenH - 32 + 3;
        int filled = (int) (Mth.clamp(mount.getFightCooldownFraction(), 0f, 1f) * 183.0F);

        g.blitSprite(BAR_BACKGROUND, x, y, 182, 5);
        if (filled > 0) {
            g.setColor(TINT_R, TINT_G, TINT_B, 1.0F);
            g.blitSprite(BAR_PROGRESS, 182, 5, 0, 0, x, y, filled, 5);
            g.setColor(1.0F, 1.0F, 1.0F, 1.0F);
        }
    }
}
