package net.tiew.operationWild.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.tiew.operationWild.OperationWild;
import net.tiew.operationWild.entity.OWEntity;
import net.tiew.operationWild.entity.misc.Submarine;

/**
 * Notification « quêtes du jour renouvelées » de l'entité chevauchée.
 *
 * <p>Nouveau style : une plaque biseautée (bords clair en haut/gauche, sombre en bas/droite) avec
 * un badge d'icône à gauche, entièrement teintée par la couleur de l'individu
 * ({@link OWEntity#getEntityColor()}). La couleur est éclaircie au besoin pour rester lisible sur
 * les espèces sombres. Halo/accent pulsants + léger flottement. Propre à chaque individu : visible
 * pour l'entité chevauchée tant que ses quêtes n'ont pas été consultées.</p>
 */
public class OWUtilsOverlay {

    private static final ResourceLocation ICON =
            ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "textures/gui/ow_inventory_gui.png");

    public static void render(GuiGraphics g, int screenWidth, int screenHeight) {
        Player rider = Minecraft.getInstance().player;
        if (rider == null) return;
        if (!(rider.getVehicle() instanceof OWEntity owEntity) || owEntity instanceof Submarine) return;
        if (!owEntity.questsAreUpdated()) return;

        Font font = Minecraft.getInstance().font;
        // Base de temps continue (temps réel) → animations fluides à la fréquence d'affichage,
        // et non plus quantifiées aux 20 ticks/s de l'entité.
        // IMPORTANT : rester en double. Un float ne peut pas représenter finement ~1,7e9
        // (millis/1000) → la valeur se fige d'une frame à l'autre et l'animation s'arrête.
        double time = System.currentTimeMillis() / 1000.0;
        float pulse = 0.5f + 0.5f * (float) Math.sin(time * 3.3);

        // Couleur signature de l'individu, éclaircie pour rester lisible (certaines espèces sont très sombres).
        int raw = owEntity.getEntityColor();
        int accent = readable(raw);
        int accentDark = mix(accent, 0x000000, 0.45f);
        int accentLight = mix(accent, 0xFFFFFF, 0.35f);

        String name = owEntity.getNickname();
        boolean hasName = name != null && !name.isEmpty();

        // Pas de couleur de style : la couleur (avec alpha) est fournie au drawString pour que le
        // texte respire aussi en opacité avec le reste de la plaque.
        Component title = (hasName
                ? Component.literal(name)
                : Component.translatable("tooltip.questsUpdated"))
                .setStyle(Style.EMPTY.withBold(true));
        Component subtitle = hasName
                ? Component.translatable("tooltip.questsUpdated")
                : null;

        int textW = Math.max(font.width(title), subtitle != null ? font.width(subtitle) : 0);

        int badge = 24;
        int padX = 7, padY = 6, gap = 9;
        int contentH = subtitle != null ? (font.lineHeight * 2 + 3) : font.lineHeight;
        int boxH = Math.max(badge, contentH) + padY * 2;
        int boxW = padX + badge + gap + textW + padX + 4;

        int boxX = (screenWidth - boxW) / 2;
        int boxY = 10;

        // Flottement vertical fluide : décalage sous-pixel appliqué par translate (pas de cast int
        // saccadé), piloté par le temps réel.
        float floatOffset = (float) (Math.sin(time * 1.7) * 1.6);
        g.pose().pushPose();
        g.pose().translate(0.0f, floatOffset, 0.0f);

        // Respiration idle : léger gonflement/dégonflement lent autour du centre de la plaque
        // (inspiration/expiration), déphasé du flottement pour un rendu organique.
        float breath = 1.0f + (float) Math.sin(time * 1.25) * 0.025f;
        float bcx = boxX + boxW / 2f;
        float bcy = boxY + boxH / 2f;
        g.pose().translate(bcx, bcy, 0.0f);
        g.pose().scale(breath, breath, 1.0f);
        g.pose().translate(-bcx, -bcy, 0.0f);

        // Pulsation d'opacité idle, EN PHASE avec la respiration : la plaque « respire » aussi en
        // intensité (nette : plus opaque à l'inspiration, franchement plus légère à l'expiration).
        // Plage 0.35 → 1.0 (fondu franc, textes inclus).
        float glow = 0.35f + 0.65f * (0.5f + 0.5f * (float) Math.sin(time * 1.25));

        // Halo pulsant (couleur de l'individu), modulé par l'opacité idle.
        for (int r = 3; r >= 1; r--) {
            int a = (int) ((14 + 22 * pulse) * r * glow);
            g.fill(boxX - r, boxY - r, boxX + boxW + r, boxY + boxH + r, argb(a, accent));
        }

        // Plaque : slate légèrement teinté par la couleur de l'individu (plus clair qu'un aplat noir),
        // avec un dégradé haut → bas pour la profondeur.
        int plate = mix(0x2C313B, accent, 0.12f);
        int plateTop = argb((int) (0xF2 * glow), mix(plate, 0xFFFFFF, 0.14f));
        int plateBottom = argb((int) (0xF2 * glow), mix(plate, 0x000000, 0.12f));
        g.fillGradient(boxX, boxY, boxX + boxW, boxY + boxH, plateTop, plateBottom);
        // Voile intérieur discret en haut pour un léger effet « vitre ».
        g.fill(boxX + 1, boxY + 1, boxX + boxW - 1, boxY + boxH / 2, argb((int) (18 * glow), 0xFFFFFF));

        // Bordure biseautée : clair en haut/gauche, sombre en bas/droite
        int aFull = (int) (255 * glow);
        g.fill(boxX, boxY, boxX + boxW, boxY + 1, argb(aFull, accentLight));                 // haut
        g.fill(boxX, boxY, boxX + 1, boxY + boxH, argb(aFull, accentLight));                 // gauche
        g.fill(boxX, boxY + boxH - 1, boxX + boxW, boxY + boxH, argb(aFull, accentDark));     // bas
        g.fill(boxX + boxW - 1, boxY, boxX + boxW, boxY + boxH, argb(aFull, accentDark));     // droite

        // Badge d'icône (carré teinté + biseau interne + icône)
        int badgeX = boxX + padX;
        int badgeY = boxY + (boxH - badge) / 2;
        g.fill(badgeX, badgeY, badgeX + badge, badgeY + badge, argb(aFull, accentDark));
        g.fill(badgeX, badgeY, badgeX + badge, badgeY + 1, argb(aFull, accentLight));
        g.fill(badgeX, badgeY, badgeX + 1, badgeY + badge, argb(aFull, accentLight));
        g.fill(badgeX + badge - 1, badgeY, badgeX + badge, badgeY + badge, argb((int) (180 * glow), 0x000000));
        g.fill(badgeX, badgeY + badge - 1, badgeX + badge, badgeY + badge, argb((int) (180 * glow), 0x000000));
        g.setColor(1f, 1f, 1f, glow);
        g.blit(ICON, badgeX + (badge - 16) / 2, badgeY + (badge - 16) / 2, 176, 48, 16, 16);
        g.setColor(1f, 1f, 1f, 1f);

        // Séparateur vertical fin après le badge
        int sepX = badgeX + badge + gap / 2;
        g.fill(sepX, boxY + 4, sepX + 1, boxY + boxH - 4, argb((int) (90 * glow), accent));

        // Textes — fondus avec l'opacité idle (alpha porté par la couleur passée au drawString).
        int textX = badgeX + badge + gap;
        if (subtitle != null) {
            int ty = boxY + padY;
            g.drawString(font, title, textX, ty, argb(aFull, accentLight), true);
            g.drawString(font, subtitle, textX, ty + font.lineHeight + 3, argb(aFull, 0xE2E8EE), false);
        } else {
            int ty = boxY + (boxH - font.lineHeight) / 2;
            g.drawString(font, title, textX, ty, argb(aFull, accentLight), true);
        }

        g.pose().popPose();
    }

    /** Éclaircit une couleur trop sombre pour qu'elle reste visible sur fond noir. */
    private static int readable(int rgb) {
        int r = (rgb >> 16) & 0xFF, g = (rgb >> 8) & 0xFF, b = rgb & 0xFF;
        double lum = (0.299 * r + 0.587 * g + 0.114 * b) / 255.0;
        if (lum >= 0.35) return rgb;
        // Plus la couleur est sombre, plus on l'éclaircit (garde la teinte).
        float amount = (float) ((0.35 - lum) / 0.35) * 0.7f;
        return mix(rgb, 0xFFFFFF, amount);
    }

    private static int mix(int a, int b, float t) {
        t = Math.max(0f, Math.min(1f, t));
        int ar = (a >> 16) & 0xFF, ag = (a >> 8) & 0xFF, ab = a & 0xFF;
        int br = (b >> 16) & 0xFF, bg = (b >> 8) & 0xFF, bb = b & 0xFF;
        int r = (int) (ar + (br - ar) * t);
        int gg = (int) (ag + (bg - ag) * t);
        int bl = (int) (ab + (bb - ab) * t);
        return (r << 16) | (gg << 8) | bl;
    }

    /** Compose une couleur ARGB à partir d'un alpha (0..255) et d'un RGB. */
    private static int argb(int alpha, int rgb) {
        return (Math.max(0, Math.min(255, alpha)) << 24) | (rgb & 0xFFFFFF);
    }
}
