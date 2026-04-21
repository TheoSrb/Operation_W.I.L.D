package net.tiew.operationWild.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.entity.player.Player;
import net.tiew.operationWild.OperationWild;
import net.tiew.operationWild.core.OWKeysBinding;
import net.tiew.operationWild.entity.OWEntity;
import net.tiew.operationWild.entity.animals.terrestrial.TigerEntity;
import net.tiew.operationWild.entity.attacks.OWAttacksHandler;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * Overlay d'information sur les attaques — affiché tant que la touche dédiée est maintenue.
 *
 * ─── Ajouter une entité ──────────────────────────────────────────────────────
 *   1. Créer un nouveau bloc dans la section "DÉFINITIONS" ci-dessous.
 *   2. PROFILES.put(MonEntity.class, new EntityProfile(...)).
 *   3. Pour les cartes, cardTexX = index × 20, cardTexY = entityRow × 40.
 *
 * ─── Formater du texte ───────────────────────────────────────────────────────
 *   e -> desc("ow.attacks.xxx.desc", val("3"), val(e.getDamage()))
 */
public class OWAttacksInformation {

    // ── Textures ──────────────────────────────────────────────────────────────
    private static final ResourceLocation BG    = ResourceLocation.fromNamespaceAndPath(
            OperationWild.MOD_ID, "textures/overlay/attacks_information.png");
    private static final ResourceLocation CARDS = ResourceLocation.fromNamespaceAndPath(
            OperationWild.MOD_ID, "textures/overlay/ow_tamed_attacks.png");

    private static final int CARD_SIZE = 20;
    private static final int TEX_SIZE  = 256;
    private static final int BG_W      = 256;
    private static final int BG_H      = 136;

    // ── Animation ouverte / fermée ────────────────────────────────────────────
    private static long    openStartMs  = -1L;
    private static long    closeStartMs = -1L;
    private static boolean wasKeyDown   = false;
    private static final long ANIM_MS   = 160L;

    /** Appelé chaque tick client (depuis ClientEvents). */
    public static void tick() {
        boolean keyDown = OWKeysBinding.OW_ATTACKS_INFO.isDown();
        long    now     = System.currentTimeMillis();

        if (keyDown && !wasKeyDown) {
            openStartMs  = now;
            closeStartMs = -1L;
        } else if (!keyDown && wasKeyDown) {
            closeStartMs = now;
            openStartMs  = -1L;
        }
        wasKeyDown = keyDown;
    }

    /** true pendant l'ouverture, la phase active et la fermeture animée. */
    public static boolean shouldRender() {
        long now = System.currentTimeMillis();
        return wasKeyDown || (closeStartMs >= 0 && now - closeStartMs < ANIM_MS);
    }

    private static float animScale() {
        long now = System.currentTimeMillis();
        if (openStartMs  >= 0) return easeOut(Math.min(1f, (float)(now - openStartMs)  / ANIM_MS));
        if (closeStartMs >= 0) return easeOut(Math.max(0f, 1f - (float)(now - closeStartMs) / ANIM_MS));
        return 1f;
    }

    private static float easeOut(float t) { return 1f - (1f - t) * (1f - t); }

    // =========================================================================
    //  MODÈLE DE DONNÉES
    // =========================================================================

    record AttackSlot(
            int cardTexX, int cardTexY,
            String keyLabel,
            Component title,
            Function<OWEntity, Component> desc
    ) {
        boolean hasCard() { return cardTexX >= 0; }
    }

    record EntityProfile(
            AttackSlot combo,
            AttackSlot charged,
            AttackSlot ultimate,
            AttackSlot passive
    ) {
        AttackSlot[] slots() { return new AttackSlot[]{ combo, charged, ultimate, passive }; }
    }

    private static final Map<Class<? extends OWEntity>, EntityProfile> PROFILES = new HashMap<>();

    // =========================================================================
    //  DÉFINITIONS DES ENTITÉS  ──  MODIFIER / AJOUTER ICI
    // =========================================================================

    static {

        // ──────────────────────────────────────────────────────────────────────
        //  TIGRE  (entityRow = 0 dans ow_tamed_attacks.png)
        // ──────────────────────────────────────────────────────────────────────
        PROFILES.put(TigerEntity.class, new EntityProfile(

            new AttackSlot(0, 0, "LMB",
                title("ow.attacks.tiger.combo.title"),
                e -> desc("ow.attacks.tiger.combo.desc",
                    val("0.65"), val(e.getDamageToClient() / 3))
            ),

            new AttackSlot(20, 0, "RMB",
                title("ow.attacks.tiger.bond.title"),
                e -> desc("ow.attacks.tiger.bond.desc",
                    val(OWAttacksHandler.TigerAttacks.JUMP_ATTACK_COOLDOWN_TICKS / 20))
            ),

            new AttackSlot(40, 0, "X",
                title("ow.attacks.tiger.shadow_strike.title"),
                e -> desc("ow.attacks.tiger.shadow_strike.desc",
                    val(OWAttacksHandler.TigerAttacks.SHADOW_STRIKE_DURATION_TICKS / 20),
                    val("15"),
                    val("25"),
                    val(String.valueOf(OWAttacksHandler.TigerAttacks.SHADOW_STRIKE_KILLS_REQUIRED)),
                    val(String.valueOf(OWAttacksHandler.TigerAttacks.SHADOW_STRIKE_COOLDOWN_TICKS / 20)))
            ),

            new AttackSlot(-1, -1, "",
                title("ow.attacks.tiger.predator_sense.title"),
                e -> desc("ow.attacks.tiger.predator_sense.desc",
                        val("30"),
                        val("32"))
            )

        ));

        // PROFILES.put(KodiakEntity.class, new EntityProfile(
        //     new AttackSlot( 0, 40, "LMB", title("ow.attacks.kodiak.combo.title"),  e -> desc("ow.attacks.kodiak.combo.desc")),
        //     new AttackSlot(20, 40, "RMB", title("ow.attacks.kodiak.charge.title"), e -> desc("ow.attacks.kodiak.charge.desc")),
        //     new AttackSlot(40, 40, "X",   title("ow.attacks.kodiak.ult.title"),    e -> desc("ow.attacks.kodiak.ult.desc")),
        //     new AttackSlot(-1, -1, "",    title("ow.attacks.kodiak.passive.title"),e -> desc("ow.attacks.kodiak.passive.desc"))
        // ));

    }

    // =========================================================================
    //  HELPERS DE FORMATAGE
    // =========================================================================

    /** Valeur numérique ou mot-clé mis en avant : blanc gras. */
    public static Component val(String s) {
        return Component.literal(s).withStyle(Style.EMPTY.withBold(true).withColor(0xFFFFFF));
    }

    /** Valeur float (arrondie à 1 décimale) en blanc gras. */
    public static Component val(float value) {
        String s = (value == (int) value) ? String.valueOf((int) value)
                                          : String.format("%.1f", value);
        return val(s);
    }

    /** Description traduite avec valeurs dynamiques injectées dans les %s. Le texte statique est en gris. */
    public static Component desc(String key, Object... args) {
        return Component.translatable(key, args).withStyle(Style.EMPTY.withColor(0x999999));
    }

    /** Concatène plusieurs Components en un seul. */
    public static Component line(Component... parts) {
        MutableComponent r = Component.empty();
        for (Component c : parts) r = r.append(c);
        return r;
    }

    /** Titre de slot : blanc souligné, clé de traduction. */
    public static Component title(String key) {
        return Component.translatable(key).withStyle(Style.EMPTY.withUnderlined(true).withColor(0xFFFFFF));
    }

    // =========================================================================
    //  RENDU PRINCIPAL
    // =========================================================================

    public static void render(GuiGraphics g, int screenWidth, int screenHeight) {
        Player player = Minecraft.getInstance().player;
        if (player == null) return;
        if (!(player.getVehicle() instanceof OWEntity entity)) return;

        EntityProfile profile = PROFILES.get(entity.getClass());
        if (profile == null) return;

        float scale = animScale();
        if (scale <= 0f) return;

        int   bgX    = (screenWidth  - BG_W) / 2;
        int   bgY    = (screenHeight - BG_H) / 2;
        float pivotX = bgX + BG_W / 2f;
        float pivotY = bgY + BG_H / 2f;

        g.pose().pushPose();
        g.pose().translate(pivotX, pivotY, 0f);
        g.pose().scale(scale, scale, 1f);
        g.pose().translate(-pivotX, -pivotY, 0f);

        g.blit(BG, bgX, bgY, 0, 0, BG_W, BG_H);

        Font font      = Minecraft.getInstance().font;
        int  PASSIVE_H = 34;
        int  colW      = BG_W / 3;
        int  innerW    = colW - 14;
        int  entityRow = OWAttacksHandler.getEntityRow(entity.getClass());
        int  cardTexY  = entityRow * 40;
        int  maxDescY  = bgY + BG_H - PASSIVE_H - 8;

        AttackSlot[] slots = profile.slots();

        // ── 3 colonnes d'attaque ──────────────────────────────────────────────
        for (int i = 0; i < 3 && i < slots.length; i++) {
            AttackSlot slot  = slots[i];
            int colCX = bgX + i * colW + colW / 2;
            int curY  = bgY + 6;

            if (slot.hasCard()) {
                g.blit(CARDS,
                        colCX - CARD_SIZE / 2, curY,
                        slot.cardTexX(), cardTexY,
                        CARD_SIZE, CARD_SIZE,
                        TEX_SIZE, TEX_SIZE);
            }
            curY += CARD_SIZE + 2;

            if (!slot.keyLabel().isEmpty()) {
                drawText(g, font,
                        Component.literal("[" + slot.keyLabel() + "]")
                                .withStyle(Style.EMPTY.withColor(0x666666)),
                        colCX, curY, 0.6f);
            }
            curY += 7;

            drawText(g, font, slot.title(), colCX, curY, 0.7f);
            curY += (int)(font.lineHeight * 0.7f) + 3;

            final float DS = 0.52f;
            int wrapW = (int)(innerW / DS);
            List<FormattedCharSequence> lines = font.split(slot.desc().apply(entity), wrapW);
            int lineH = (int)(font.lineHeight * DS) + 1;
            for (FormattedCharSequence line : lines) {
                if (curY > maxDescY) break;
                drawLine(g, font, line, colCX, curY, DS);
                curY += lineH;
            }
        }

        // ── Séparateur ────────────────────────────────────────────────────────
        int divY = bgY + BG_H - PASSIVE_H - 4;
        g.fill(bgX + 6, divY, bgX + BG_W - 6, divY + 1, 0x33FFFFFF);

        // ── Bandeau passif ────────────────────────────────────────────────────
        if (slots.length >= 4) {
            AttackSlot passive = slots[3];
            int bannerCX = bgX + BG_W / 2;
            int bannerY  = divY + 5;

            Component header = Component.empty()
                    .append(Component.literal("✦ ").withStyle(Style.EMPTY.withColor(0xFFD080).withBold(true)))
                    .append(Component.translatable("ow.attacks.passive_label")
                            .withStyle(Style.EMPTY.withColor(0x555555)))
                    .append(passive.title());
            drawText(g, font, header, bannerCX, bannerY, 0.65f);

            int descY  = bannerY + (int)(font.lineHeight * 0.65f) + 3;
            final float DS2 = 0.50f;
            int wrapW2 = (int)((BG_W - 24) / DS2);
            List<FormattedCharSequence> descLines = font.split(passive.desc().apply(entity), wrapW2);
            int lineH2 = (int)(font.lineHeight * DS2) + 1;
            for (int li = 0; li < Math.min(2, descLines.size()); li++) {
                drawLine(g, font, descLines.get(li), bannerCX, descY + li * lineH2, DS2);
            }
        }

        g.pose().popPose();
    }

    // ── Utilitaires de rendu ──────────────────────────────────────────────────

    private static void drawText(GuiGraphics g, Font font, Component text, int cx, int y, float scale) {
        g.pose().pushPose();
        g.pose().translate(cx, y, 0f);
        g.pose().scale(scale, scale, 1f);
        g.drawCenteredString(font, text, 0, 0, 0xFFFFFF);
        g.pose().popPose();
    }

    private static void drawLine(GuiGraphics g, Font font, FormattedCharSequence seq, int cx, int y, float scale) {
        g.pose().pushPose();
        g.pose().translate(cx, y, 0f);
        g.pose().scale(scale, scale, 1f);
        int tw = font.width(seq);
        g.drawString(font, seq, -tw / 2, 0, 0xFFFFFF, false);
        g.pose().popPose();
    }
}
