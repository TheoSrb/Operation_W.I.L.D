package net.tiew.operationWild.screen.tribe;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.tiew.operationWild.client.OWClientArenaReward;
import net.tiew.operationWild.client.OWClientArenaState;
import net.tiew.operationWild.client.OWClientTribeData;
import net.tiew.operationWild.client.OWClientTribeList;
import net.tiew.operationWild.core.OWArena;
import net.tiew.operationWild.core.OWReputation;
import net.tiew.operationWild.entity.OWEntity;
import net.tiew.operationWild.gui.OWCinematicFx;
import net.tiew.operationWild.networking.OWNetworkHandler;
import net.tiew.operationWild.networking.packets.to_server.AcceptArenaPacket;
import net.tiew.operationWild.networking.packets.to_server.CancelArenaPacket;
import net.tiew.operationWild.networking.packets.to_server.ChallengeTribePacket;
import net.tiew.operationWild.networking.packets.to_server.ClaimArenaChestPacket;
import net.tiew.operationWild.networking.packets.to_server.ConfirmArenaFightersPacket;
import net.tiew.operationWild.networking.packets.to_server.RespondArenaChallengePacket;
import net.tiew.operationWild.networking.packets.to_server.SelectArenaFighterPacket;
import net.tiew.operationWild.team.OWArenaFighter;
import net.tiew.operationWild.team.OWTeam;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

/**
 * Onglet « Arène » de la tribu.
 *
 * <p>Trois états d'affichage :</p>
 * <ul>
 *   <li><b>Présentation</b> — tant que le chef n'a pas engagé la tribu : texte défilant sur fond
 *       assombri + bouton « J'accepte » hors zone de défilement. Seul le chef voit cet onglet à ce
 *       stade (cf. {@code OWTribeScreen#visibleTabs}).</li>
 *   <li><b>Récompenses</b> (défaut une fois accepté) — barre de progression vers le prochain coffre
 *       ({@link OWArena#PRESTIGE_PER_CHEST} points de prestige de tribu), coffre cliquable au palier
 *       du badge de réputation, et animation d'ouverture.</li>
 *   <li><b>Combat</b> — bascule via le bouton en bas à droite. Réservé à la phase suivante.</li>
 * </ul>
 */
public class OWTribeArenaScreen extends OWTribeScreen {

    /** Vue active dans l'onglet, basculée par le bouton en bas à droite. */
    private enum View { REWARDS, COMBAT }

    /** Étapes de l'animation d'ouverture de coffre (façon Clash Royale). */
    private enum Opening { NONE, ANTICIPATION, SHAKE, CHARGE, BURST, REVEAL }

    // ── Géométrie ────────────────────────────────────────────────────────────────
    private static final int CONTENT_Y = 22;
    private static final int SWITCH_W = 20, SWITCH_H = 18;   // même gabarit que les onglets
    private static final int BAR_W = 140, BAR_H = 9;
    private static final int CHEST_W = 52, CHEST_H = 40;

    // Durées de l'animation (ms).
    private static final long ANTICIPATION_MS = 700;   // le coffre s'installe, le monde s'efface
    private static final long SHAKE_MS = 2000;         // tremblement qui s'emballe, lumière qui fuit
    private static final long CHARGE_MS = 550;         // silence : tout se fige avant de céder
    private static final long BURST_MS = 550;          // rupture
    private static final long REVEAL_STEP_MS = 170;    // cadence de révélation des lots
    /** Au-delà, on renonce à attendre le butin : le serveur a refusé la demande (ou le paquet s'est perdu). */
    private static final long CLAIM_TIMEOUT_MS = 6000;

    private View view = View.REWARDS;

    // Présentation / défilement.
    private int introScroll = 0, introContentH = 0, introViewH = 0;
    private Button acceptBtn;

    // Ouverture de coffre.
    private Opening opening = Opening.NONE;
    private long openingStart = 0L;
    private OWClientArenaReward.Reward reward = null;
    private final List<float[]> burstParticles = new ArrayList<>(); // {angle, vitesse px/s, taille}
    private final List<float[]> burstRays = new ArrayList<>();      // {angle, longueur, épaisseur}
    /** Nombre de battements déjà joués pendant le tremblement (cadence croissante). */
    private int shakeTicks = 0;
    /** Zone cliquable du coffre, recalculée à chaque frame (le gabarit dépend du palier). */
    private int chestX, chestY, chestW = CHEST_W, chestH = CHEST_H;

    // ── Animations ───────────────────────────────────────────────────────────────
    /** Valeurs animées par nom : chaque valeur glisse doucement vers sa cible. */
    private final Map<String, Float> animated = new HashMap<>();
    private long lastFrameMs = System.currentTimeMillis();
    /** Delta de la frame courante, en secondes (borné pour survivre à un freeze). */
    private float frameDelta = 0f;
    /** Début de la transition d'apparition du contenu (changement de vue ou de phase). */
    private long contentFadeStart = System.currentTimeMillis();
    private OWArena.Phase lastPhase = null;

    // ── Vue combat ───────────────────────────────────────────────────────────────
    /**
     * Côté d'un emplacement de combattant, et écart vertical entre deux. Dimensionnés pour que les
     * cinq emplacements tiennent entre l'en-tête de colonne et la bande de boutons du bas
     * (5 × 21 = 105 px de {@code topPos+31} à {@code topPos+136}) — au-delà, la colonne passait
     * sous le bouton « Commencer ».
     */
    private static final int SLOT = 20, SLOT_GAP = 1;

    private int combatScroll = 0, combatScrollMax = 0, candidateRows = 0;
    /** Éléments survolés cette frame (tooltips rendus en fin de frame). */
    private OWArenaFighter hoverFighter = null, hoverCandidate = null;
    /** Tribu survolée dans le sélecteur d'adversaire (tooltip de décision en fin de frame). */
    private OWClientTribeList.Entry hoverTribe = null;
    /** Créatures locales jetables servant d'aperçu 3D, indexées « type:skin ». */
    private final Map<String, LivingEntity> previewCache = new HashMap<>();
    /** Tribu en attente de confirmation de défi ({@code 0} = aucune). */
    private int pendingChallengeId = 0;
    private String pendingChallengeName = "";

    private Button chalAcceptBtn, chalDeclineBtn, cancelArenaBtn, readyBtn, challengeYesBtn, challengeNoBtn;

    public OWTribeArenaScreen() {
        super(Component.translatable("owteams.arena.title"));
    }

    // ── Rôle & état ──────────────────────────────────────────────────────────────
    private boolean isChief() {
        OWTeam t = OWClientTribeData.get();
        Minecraft mc = Minecraft.getInstance();
        return t != null && mc.player != null && t.isChief(mc.player.getUUID());
    }

    private UUID selfUuid() {
        Minecraft mc = Minecraft.getInstance();
        return mc.player != null ? mc.player.getUUID() : null;
    }

    private boolean accepted() {
        OWTeam t = OWClientTribeData.get();
        return t != null && t.isArenaAccepted();
    }

    @Override
    protected void init() {
        super.init();
        acceptBtn = addRenderableWidget(Button.builder(
                        Component.translatable("owteams.arena.intro.accept"),
                        b -> OWNetworkHandler.sendToServer(new AcceptArenaPacket()))
                .bounds(leftPos + IMG_W / 2 - 50, topPos + IMG_H - 22, 100, 16).build());

        // ── Boutons de la vue combat (visibilité pilotée par la phase dans render) ──
        int by = topPos + IMG_H - 22;
        chalAcceptBtn = addRenderableWidget(Button.builder(Component.translatable("owteams.arena.challenge.accept"),
                        b -> OWNetworkHandler.sendToServer(new RespondArenaChallengePacket(true)))
                .bounds(leftPos + IMG_W / 2 - 74, by, 70, 16).build());
        chalDeclineBtn = addRenderableWidget(Button.builder(Component.translatable("owteams.arena.challenge.decline"),
                        b -> OWNetworkHandler.sendToServer(new RespondArenaChallengePacket(false)))
                .bounds(leftPos + IMG_W / 2 + 4, by, 70, 16).build());
        cancelArenaBtn = addRenderableWidget(Button.builder(Component.translatable("owteams.arena.cancel"),
                        b -> OWNetworkHandler.sendToServer(new CancelArenaPacket()))
                .bounds(leftPos + IMG_W / 2 - 45, by, 90, 16).build());
        readyBtn = addRenderableWidget(Button.builder(Component.translatable("owteams.arena.selection.start"),
                        b -> OWNetworkHandler.sendToServer(
                                new ConfirmArenaFightersPacket(!OWClientArenaState.get().myReady())))
                .bounds(leftPos + IMG_W / 2 - 50, by, 100, 16).build());

        challengeYesBtn = addRenderableWidget(Button.builder(Component.translatable("owteams.confirm.yes"), b -> {
            if (pendingChallengeId != 0) {
                OWNetworkHandler.sendToServer(new ChallengeTribePacket(pendingChallengeId));
                playUi(SoundEvents.UI_TOAST_OUT, 1.2f);
            }
            pendingChallengeId = 0;
        }).bounds(0, 0, 60, 16).build());
        challengeNoBtn = addRenderableWidget(Button.builder(Component.translatable("owteams.confirm.no"),
                b -> pendingChallengeId = 0).bounds(0, 0, 60, 16).build());
    }

    @Override
    public void tick() {
        super.tick();
        // Renvoie sur le tableau de bord si la tribu disparaît, ou si l'onglet cesse d'être accessible
        // (ex. un membre simple ouvre l'écran juste avant que le chef ne quitte l'arène).
        if (!OWClientTribeData.hasTribe()) { Minecraft.getInstance().setScreen(new OWTribeMenuScreen()); return; }
        if (!isChief() && !accepted()) Minecraft.getInstance().setScreen(new OWTribeDashboardScreen());
    }

    @Override
    public void onClose() {
        OWClientArenaReward.clear();
        super.onClose();
    }

    // ── Entrées ──────────────────────────────────────────────────────────────────
    @Override
    public boolean mouseScrolled(double mx, double my, double sx, double sy) {
        if (!accepted()) {
            int max = Math.max(0, introContentH - introViewH);
            introScroll = Math.max(0, Math.min(max, introScroll - (int) Math.signum(sy) * 9));
            return true;
        }
        if (view == View.COMBAT && opening == Opening.NONE && pendingChallengeId == 0) {
            combatScroll = Math.max(0, Math.min(combatScrollMax, combatScroll - (int) Math.signum(sy)));
            return true;
        }
        return super.mouseScrolled(mx, my, sx, sy);
    }

    @Override
    public boolean mouseClicked(double mx, double my, int button) {
        // Pendant l'animation, tout clic sert uniquement à la faire avancer / la clore.
        if (opening != Opening.NONE) {
            if (button == 0 && opening == Opening.REVEAL) closeOpening();
            return true;
        }
        // La confirmation de défi capte tout sauf ses deux boutons.
        if (pendingChallengeId != 0) return super.mouseClicked(mx, my, button);

        if (button == 0 && tribeTabClicked(mx, my, Tab.ARENA)) return true;

        if (button == 0 && accepted() && view == View.COMBAT && isChief()
                && combatClicked(mx, my)) return true;

        if (button == 0 && accepted()) {
            // Bouton de bascule Récompenses / Combat.
            int sx = switchX(), sy = switchY();
            if (mx >= sx && mx < sx + SWITCH_W && my >= sy && my < sy + SWITCH_H) {
                view = view == View.REWARDS ? View.COMBAT : View.REWARDS;
                restartContentFade();
                playTabSwitch();
                return true;
            }
            // Coffre cliquable (uniquement si le joueur en a un en attente).
            if (view == View.REWARDS && pendingChests() > 0
                    && mx >= chestX && mx < chestX + chestW && my >= chestY && my < chestY + chestH) {
                beginOpening();
                return true;
            }
        }
        return super.mouseClicked(mx, my, button);
    }

    /**
     * Clics propres à la vue combat (chef uniquement). Les zones sont recalculées à l'identique du
     * rendu : le serveur revalide de toute façon chaque intention reçue.
     */
    private boolean combatClicked(double mx, double my) {
        OWArena.Phase phase = OWClientArenaState.phase();

        if (phase == OWArena.Phase.IDLE) {
            List<OWClientTribeList.Entry> targets = challengeableTribes();
            if (targets.isEmpty()) return false;
            int x1 = leftPos + 7, y1 = topPos + CONTENT_Y, x2 = leftPos + IMG_W - 7, y2 = topPos + IMG_H - 26;
            int listY = y1 + 16, rowH = 14;
            int rows = (y2 - listY - 2) / rowH;
            boolean hasScroll = targets.size() > rows;
            int listX = x1 + 2, listW = x2 - x1 - 4 - (hasScroll ? 6 : 0);
            for (int i = combatScroll; i < Math.min(combatScroll + rows, targets.size()); i++) {
                int y = listY + (i - combatScroll) * rowH;
                if (mx >= listX && mx < listX + listW && my >= y && my < y + rowH) {
                    OWClientTribeList.Entry e = targets.get(i);
                    pendingChallengeId = e.teamId();
                    pendingChallengeName = e.name();
                    playUi(SoundEvents.UI_BUTTON_CLICK.value(), 0.9f);
                    return true;
                }
            }
            return false;
        }

        if (phase == OWArena.Phase.SELECTION) {
            int slotsY = slotsTop();
            List<OWArenaFighter> mine = OWClientArenaState.myFighters();

            // Clic sur un de mes emplacements → retire le combattant.
            for (int i = 0; i < mine.size(); i++) {
                int x = leftPos + 6, y = slotsY + i * (SLOT + SLOT_GAP);
                if (mx >= x && mx < x + SLOT && my >= y && my < y + SLOT) {
                    OWNetworkHandler.sendToServer(
                            new SelectArenaFighterPacket(mine.get(i).entityUuid().toString(), false));
                    playUi(SoundEvents.ITEM_PICKUP, 0.7f);   // retrait : note descendante
                    return true;
                }
            }

            // Clic dans la liste centrale → engage (ou retire) la créature.
            List<OWArenaFighter> candidates = OWClientArenaState.candidates();
            if (candidates.isEmpty() || candidateRows <= 0) return false;
            int listX = leftPos + 6 + SLOT + 4;
            int listW = (leftPos + IMG_W - 6 - SLOT - 4) - listX;
            boolean hasScroll = candidates.size() > candidateRows;
            int contentW = hasScroll ? listW - 6 : listW;
            int rowH = 13;
            for (int i = combatScroll; i < Math.min(combatScroll + candidateRows, candidates.size()); i++) {
                int ry = slotsY + (i - combatScroll) * rowH;
                if (mx < listX || mx >= listX + contentW || my < ry || my >= ry + rowH) continue;
                OWArenaFighter f = candidates.get(i);
                boolean selected = OWClientArenaState.isSelected(f.entityUuid());
                // Un archétype déjà pris par un AUTRE combattant bloque l'ajout.
                if (!selected && OWClientArenaState.archetypeTaken(f.archetypeOrdinal(), f.entityUuid())) {
                    playUi(SoundEvents.NOTE_BLOCK_BASS.value(), 0.6f);  // refus : archétype déjà pris
                    return true;
                }
                OWNetworkHandler.sendToServer(
                        new SelectArenaFighterPacket(f.entityUuid().toString(), !selected));
                playUi(SoundEvents.ITEM_PICKUP, selected ? 0.7f : 1.4f);
                return true;
            }
        }
        return false;
    }

    /** Petit retour sonore paramétré : chaque action a sa tonalité, plutôt qu'un clic uniforme. */
    private void playUi(net.minecraft.sounds.SoundEvent sound, float pitch) {
        Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(sound, pitch));
    }

    private int switchX() { return leftPos + IMG_W - SWITCH_W - 4; }
    private int switchY() { return topPos + IMG_H - SWITCH_H - 4; }

    private int pendingChests() {
        OWTeam t = OWClientTribeData.get();
        UUID me = selfUuid();
        return t != null && me != null ? t.pendingArenaChests(me) : 0;
    }

    // ── Animation d'ouverture ────────────────────────────────────────────────────
    private void beginOpening() {
        opening = Opening.ANTICIPATION;
        openingStart = System.currentTimeMillis();
        reward = null;
        shakeTicks = 0;
        OWClientArenaReward.clear();
        OWNetworkHandler.sendToServer(new ClaimArenaChestPacket());
        playUi(SoundEvents.CHEST_OPEN, 0.7f);
    }

    private void closeOpening() {
        opening = Opening.NONE;
        reward = null;
        burstParticles.clear();
    }

    /** Passe à l'étape suivante et repart de zéro sur le chronomètre. */
    private void toPhase(Opening next) {
        opening = next;
        openingStart = System.currentTimeMillis();
    }

    /** Fait avancer la machine à états de l'animation et récupère le butin dès qu'il arrive. */
    private void advanceOpening() {
        if (opening == Opening.NONE) return;
        if (reward == null) {
            OWClientArenaReward.Reward r = OWClientArenaReward.poll();
            if (r != null) reward = r;
        }
        long elapsed = System.currentTimeMillis() - openingStart;
        switch (opening) {
            case ANTICIPATION -> {
                if (elapsed >= ANTICIPATION_MS) { toPhase(Opening.SHAKE); playUi(SoundEvents.NOTE_BLOCK_BASS.value(), 0.5f); }
            }
            case SHAKE -> {
                // Le serveur n'a rien renvoyé : il a refusé la demande (coffre déjà pris ailleurs,
                // tribu quittée…). On sort proprement plutôt que de trembler indéfiniment.
                if (reward == null && elapsed >= CLAIM_TIMEOUT_MS) { closeOpening(); return; }

                // Battements de plus en plus rapprochés et de plus en plus aigus : c'est ce qui
                // fait monter la tension bien plus que le tremblement lui-même.
                float p = Math.min(1f, elapsed / (float) SHAKE_MS);
                int expected = (int) (p * p * 14);
                while (shakeTicks < expected) {
                    shakeTicks++;
                    playUi(SoundEvents.NOTE_BLOCK_BASS.value(), 0.55f + 1.35f * p);
                }

                // On n'ouvre qu'une fois le butin reçu : en multijoueur la latence peut dépasser
                // la durée du tremblement.
                if (elapsed >= SHAKE_MS && reward != null) {
                    toPhase(Opening.CHARGE);
                    playUi(SoundEvents.BEACON_ACTIVATE, 1.6f);
                }
            }
            case CHARGE -> {
                if (elapsed >= CHARGE_MS) {
                    toPhase(Opening.BURST);
                    spawnBurst(reward != null ? reward.chest().accent() : 0xE9B115);
                    playUi(SoundEvents.GENERIC_EXPLODE.value(), 1.2f);
                    playUi(SoundEvents.PLAYER_LEVELUP, 1.1f);
                }
            }
            case BURST -> {
                if (elapsed >= BURST_MS) toPhase(Opening.REVEAL);
            }
            default -> {}
        }
    }

    private void spawnBurst(int accent) {
        burstParticles.clear();
        Random r = new Random();
        for (int i = 0; i < 130; i++) {
            float angle = (float) (-Math.PI / 2 + (r.nextDouble() - 0.5) * Math.PI * 1.45);
            float speed = 70f + r.nextFloat() * 230f;
            float size = 1.4f + r.nextFloat() * 3.6f;
            burstParticles.add(new float[]{ angle, speed, size });
        }
        burstRays.clear();
        for (int i = 0; i < 20; i++) {
            burstRays.add(new float[]{
                    (float) (r.nextDouble() * Math.PI * 2),
                    150f + r.nextFloat() * 300f,
                    2f + r.nextFloat() * 6f });
        }
    }

    // ── Rendu ────────────────────────────────────────────────────────────────────
    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partial) {
        // Delta réel de la frame : toutes les animations s'y indexent plutôt que sur le nombre
        // d'images, et il est borné pour qu'un à-coup ne fasse pas sauter les transitions.
        long now = System.currentTimeMillis();
        frameDelta = Math.min(0.1f, (now - lastFrameMs) / 1000f);
        lastFrameMs = now;

        // Un changement de phase relance le fondu d'apparition du contenu.
        OWArena.Phase phase = OWClientArenaState.phase();
        if (phase != lastPhase) { lastPhase = phase; restartContentFade(); }

        advanceOpening();
        hoverFighter = null; hoverCandidate = null; hoverTribe = null;

        OWTeam t = OWClientTribeData.get();
        boolean acc = accepted();
        boolean overlay = opening != Opening.NONE;
        boolean confirming = pendingChallengeId != 0;
        acceptBtn.visible = !acc && isChief() && !overlay;
        updateCombatButtons(acc && view == View.COMBAT && !overlay && !confirming);

        // Pendant l'ouverture d'un coffre, l'overlay occupe tout l'écran : on ne dessine ni le
        // panneau, ni les widgets, ni les onglets derrière lui.
        //
        // Ce n'est pas seulement une économie : les textes et les aplats ne sont pas rendus dans le
        // même lot, si bien qu'un simple voile posé par-dessus laisse les textes du panneau
        // ressortir au premier plan. Ne rien dessiner derrière est le seul remède fiable.
        if (overlay) {
            renderOpeningOverlay(g);
            return;
        }

        drawPanel(g, mouseX, mouseY, partial);
        drawHeader(g, Component.translatable("owteams.arena.title"));
        g.fill(leftPos + 6, topPos + 18, leftPos + IMG_W - 6, topPos + 19, 0xFF9A9A9A); // bordure d'en-tête

        if (t == null) { super.render(g, mouseX, mouseY, partial); return; }

        if (!acc) {
            renderIntro(g);
            super.render(g, mouseX, mouseY, partial); // bouton « J'accepte »
            renderTribeTabs(g, mouseX, mouseY, Tab.ARENA);
            return;
        }

        if (view == View.REWARDS) renderRewards(g, t, mouseX, mouseY);
        else renderCombat(g, mouseX, mouseY);

        renderSwitchButton(g, mouseX, mouseY);
        super.render(g, mouseX, mouseY, partial);
        if (!confirming) renderTribeTabs(g, mouseX, mouseY, Tab.ARENA);

        if (confirming) { renderChallengeConfirm(g, mouseX, mouseY, partial); return; }

        // Tooltips en dernier, pour qu'ils passent au-dessus des widgets.
        if (hoverTribe != null) { renderTribeTooltip(g, hoverTribe, mouseX, mouseY); return; }

        OWArenaFighter tip = hoverFighter != null ? hoverFighter : hoverCandidate;
        if (tip != null) { renderFighterTooltip(g, tip, mouseX, mouseY); return; }

        int sx = switchX(), sy = switchY();
        if (mouseX >= sx && mouseX < sx + SWITCH_W && mouseY >= sy && mouseY < sy + SWITCH_H) {
            g.renderTooltip(this.font, Component.translatable(view == View.REWARDS
                    ? "owteams.arena.switch.to_combat" : "owteams.arena.switch.to_rewards"), mouseX, mouseY);
        }
    }

    // ── Vue « présentation » (avant acceptation) ─────────────────────────────────
    /**
     * Texte de présentation défilant, posé sur un renfoncement plus sombre que le panneau. Le bouton
     * « J'accepte » vit hors de cette zone : il reste visible quel que soit le défilement.
     */
    private void renderIntro(GuiGraphics g) {
        final float s = 0.85f;
        int x1 = leftPos + 7, y1 = topPos + CONTENT_Y;
        int x2 = leftPos + IMG_W - 7, y2 = topPos + IMG_H - 26;
        introViewH = y2 - y1;

        // Renfoncement sombre + biseau.
        g.fill(x1, y1, x2, y2, 0xCC0E0E10);
        g.fill(x1, y1, x2, y1 + 1, 0xFF000000);
        g.fill(x1, y1, x1 + 1, y2, 0xFF000000);
        g.fill(x1, y2 - 1, x2, y2, 0x33FFFFFF);
        g.fill(x2 - 1, y1, x2, y2, 0x33FFFFFF);

        int textX = x1 + 6;
        int wrapVisual = (x2 - x1) - 12 - 6;              // marge + gouttière de scrollbar
        int wrap = (int) (wrapVisual / s);
        int lineH = Math.round(this.font.lineHeight * s) + 1;

        // Corps du texte : titre d'accroche puis paragraphes, en une seule liste de lignes.
        List<Object[]> rendered = new ArrayList<>();     // {FormattedCharSequence | null (espace), couleur}
        addTitle(rendered, "owteams.arena.intro.headline", 0xFFD257);
        addParagraph(rendered, "owteams.arena.intro.p1", wrap, 0xC8C8C8);
        addTitle(rendered, "owteams.arena.intro.s1", 0x8FD0FF);
        addParagraph(rendered, "owteams.arena.intro.p2", wrap, 0xC8C8C8);
        addTitle(rendered, "owteams.arena.intro.s2", 0x8FD0FF);
        addParagraph(rendered, "owteams.arena.intro.p3", wrap, 0xC8C8C8);
        addTitle(rendered, "owteams.arena.intro.s3", 0x8FD0FF);
        addParagraph(rendered, "owteams.arena.intro.p4", wrap, 0xC8C8C8);
        addParagraph(rendered, "owteams.arena.intro.warning", wrap, 0xE8956A);

        introContentH = rendered.size() * lineH + 4;
        int maxScroll = Math.max(0, introContentH - introViewH);
        introScroll = Math.min(introScroll, maxScroll);

        g.enableScissor(x1 + 1, y1 + 1, x2 - 1, y2 - 1);
        int y = y1 + 3 - introScroll;
        for (Object[] row : rendered) {
            if (row[0] != null && y + lineH >= y1 && y <= y2) {
                g.pose().pushPose();
                g.pose().translate(textX, y, 0);
                g.pose().scale(s, s, 1f);
                g.drawString(this.font, (FormattedCharSequence) row[0], 0, 0, (Integer) row[1], false);
                g.pose().popPose();
            }
            y += lineH;
        }
        g.disableScissor();

        if (maxScroll > 0) {
            drawScrollbar(g, x2 - 6, y1 + 1, introViewH - 2, introScroll, maxScroll, introViewH, introContentH);
        }

        // Un membre ne devrait pas atteindre cet écran ; message de repli si cela arrive.
        if (!isChief()) {
            g.drawCenteredString(this.font, Component.translatable("owteams.arena.intro.wait"),
                    leftPos + IMG_W / 2, topPos + IMG_H - 18, 0x707070);
        }
    }

    private void addTitle(List<Object[]> out, String key, int color) {
        if (!out.isEmpty()) out.add(new Object[]{ null, 0 }); // ligne vide de respiration
        out.add(new Object[]{ Component.translatable(key)
                .withStyle(Style.EMPTY.withBold(true)).getVisualOrderText(), color });
    }

    private void addParagraph(List<Object[]> out, String key, int wrap, int color) {
        for (FormattedCharSequence line : this.font.split(Component.translatable(key), wrap)) {
            out.add(new Object[]{ line, color });
        }
    }

    // ── Vue « récompenses » ──────────────────────────────────────────────────────
    private void renderRewards(GuiGraphics g, OWTeam t, int mouseX, int mouseY) {
        int cx = leftPos + IMG_W / 2;
        int prestige = t.getArenaPrestige();
        int pending = pendingChests();
        OWReputation.Badge badge = OWReputation.badgeFor(t.getReputation());
        OWArena.Chest chest = OWArena.chestFor(badge);

        // Nom complet du coffre (« Grand coffre de jade ») précédé du badge dont il découle.
        Component chestLabel = Component.translatable(chest.translationKey());
        int labelW = this.font.width(chestLabel);
        if (badge.hasSprite()) {
            OWTribeReputationScreen.renderBadge(g, badge, cx - labelW / 2 - 15, topPos + CONTENT_Y - 2, 13);
        }
        g.drawString(this.font, chestLabel.copy().withStyle(Style.EMPTY.withBold(true)
                        .withColor(TextColor.fromRgb(chest.accent()))),
                cx - labelW / 2, topPos + CONTENT_Y, chest.accent(), false);

        // Le gabarit du coffre suit la division du badge : petit / normal / grand. On lui réserve
        // une bande de hauteur fixe, dimensionnée sur le PLUS GRAND coffre, et on l'y centre :
        // sinon un grand coffre débordait sur le libellé posé juste en dessous.
        int cw = Math.round(CHEST_W * chest.size().scale());
        int ch = Math.round(CHEST_H * chest.size().scale());
        int bandTop = topPos + CONTENT_Y + 14;
        int bandH = Math.round(CHEST_H * OWArena.Size.LARGE.scale());
        chestX = cx - cw / 2;
        chestY = bandTop + (bandH - ch) / 2;
        chestW = cw; chestH = ch;

        boolean hovered = pending > 0
                && mouseX >= chestX && mouseX < chestX + cw
                && mouseY >= chestY && mouseY < chestY + ch;

        // Flottement + halo pulsé tant qu'un coffre attend d'être ouvert.
        float t0 = System.currentTimeMillis() / 1000f;
        float bob = pending > 0 ? (float) Math.sin(t0 * 3.1) * 1.8f : 0f;
        if (pending > 0) {
            float pulse = 0.5f + 0.5f * (float) Math.sin(t0 * 2.4);
            int glowA = (int) (34 + 46 * pulse) + (hovered ? 40 : 0);
            int r = Math.round(cw * 0.62f);
            g.fill(cx - r, chestY + ch / 2 - r, cx + r, chestY + ch / 2 + r,
                    (glowA << 24) | (chest.accent() & 0xFFFFFF));
        }
        drawChest(g, chestX, chestY + Math.round(bob), cw, ch,
                chest.accent(), pending > 0, hovered, 0f);

        // Pastille du nombre de coffres en attente (façon badge de notification).
        if (pending > 1) {
            String n = "x" + pending;
            int bx = chestX + cw - 4, by = chestY - 2;
            int bw = this.font.width(n) + 5;
            g.fill(bx - bw / 2, by, bx + bw / 2, by + 11, 0xFF1E1E22);
            g.fill(bx - bw / 2, by, bx + bw / 2, by + 1, 0xFF000000 | chest.accent());
            g.drawString(this.font, n, bx - bw / 2 + 3, by + 2, 0xFFFFFF, false);
        }

        // Invitation à cliquer (clignotante), ou rappel qu'il faut encore progresser. Posé sous la
        // bande réservée au coffre, donc jamais recouvert quel que soit son gabarit.
        int hintY = bandTop + bandH + 4;
        if (pending > 0) {
            int a = (int) (170 + 85 * (0.5 + 0.5 * Math.sin(t0 * 4.0)));
            g.drawCenteredString(this.font, Component.translatable("owteams.arena.chest_ready"),
                    cx, hintY, (a << 24) | 0xFFD257);
        } else {
            g.drawCenteredString(this.font, Component.translatable("owteams.arena.chest_locked"),
                    cx, hintY, 0x7A7A7A);
        }

        // Barre de progression vers le prochain coffre (remplissage lissé).
        int barX = cx - BAR_W / 2, barY = hintY + 16;
        drawProgressBar(g, barX, barY, animate("prestige", OWArena.barProgress(prestige)), chest.accent());
        g.drawCenteredString(this.font,
                OWArena.prestigeInTier(prestige) + " / " + OWArena.PRESTIGE_PER_CHEST,
                cx, barY + BAR_H + 3, 0xBBBBBB);

        // Total de prestige de la tribu, en petit.
        drawSmallCentered(g, Component.translatable("owteams.arena.prestige_total",
                OWTribeReputationScreen.formatNumber(prestige)), cx, barY + BAR_H + 15, 0x4A4A4A);
    }

    /** Petit texte aligné à gauche (échelle 0.75). */
    private void drawSmall(GuiGraphics g, Component text, int x, int y, int color) {
        final float s = 0.75f;
        g.pose().pushPose();
        g.pose().translate(x, y, 0);
        g.pose().scale(s, s, 1f);
        g.drawString(this.font, text, 0, 0, color, false);
        g.pose().popPose();
    }

    /** Petit texte centré (échelle 0.75), pour les mentions discrètes sur fond gris clair. */
    private void drawSmallCentered(GuiGraphics g, Component text, int cx, int y, int color) {
        final float s = 0.75f;
        float w = this.font.width(text) * s;
        g.pose().pushPose();
        g.pose().translate(cx - w / 2f, y, 0);
        g.pose().scale(s, s, 1f);
        g.drawString(this.font, text, 0, 0, color, false);
        g.pose().popPose();
    }

    // ── Vue « combat » ───────────────────────────────────────────────────────────
    /** Aiguillage sur la phase courante ; tout l'état vient du serveur via {@link OWClientArenaState}. */
    private void renderCombat(GuiGraphics g, int mouseX, int mouseY) {
        switch (OWClientArenaState.phase()) {
            case IDLE -> renderTribePicker(g, mouseX, mouseY);
            case CHALLENGE_SENT -> renderWaiting(g);
            case CHALLENGE_RECEIVED -> renderIncomingChallenge(g);
            case SELECTION -> renderSelection(g, mouseX, mouseY);
            case FIGHTING -> renderFighting(g);
            case ENDED -> renderResult(g);
        }
    }

    /** Cadre sombre commun aux écrans de combat. */
    private int[] combatFrame(GuiGraphics g) {
        int x1 = leftPos + 7, y1 = topPos + CONTENT_Y;
        int x2 = leftPos + IMG_W - 7, y2 = topPos + IMG_H - 26;
        g.fill(x1, y1, x2, y2, 0xCC0E0E10);
        g.fill(x1, y1, x2, y1 + 1, 0xFF000000);
        g.fill(x1, y1, x1 + 1, y2, 0xFF000000);
        g.fill(x1, y2 - 1, x2, y2, 0x33FFFFFF);
        g.fill(x2 - 1, y1, x2, y2, 0x33FFFFFF);
        return new int[]{ x1, y1, x2, y2 };
    }

    // ── Phase IDLE : choisir un adversaire ───────────────────────────────────────
    private void renderTribePicker(GuiGraphics g, int mouseX, int mouseY) {
        int[] f = combatFrame(g);
        int cx = leftPos + IMG_W / 2;

        if (!isChief()) {
            g.drawCenteredString(this.font, Component.translatable("owteams.arena.combat.chief_only"),
                    cx, (f[1] + f[3]) / 2 - 4, 0x9A9A9A);
            return;
        }

        List<OWClientTribeList.Entry> targets = challengeableTribes();
        if (targets.isEmpty()) {
            g.drawCenteredString(this.font, Component.translatable("owteams.arena.combat.no_target"),
                    cx, (f[1] + f[3]) / 2 - 4, 0x9A9A9A);
            return;
        }

        g.drawCenteredString(this.font, Component.translatable("owteams.arena.combat.pick_target")
                .withStyle(Style.EMPTY.withColor(TextColor.fromRgb(0xFFD257))), cx, f[1] + 4, 0xFFD257);

        int listY = f[1] + 16, rowH = 14;
        int rows = (f[3] - listY - 2) / rowH;
        boolean hasScroll = targets.size() > rows;
        int listX = f[0] + 2, listW = f[2] - f[0] - 4 - (hasScroll ? 6 : 0);
        combatScrollMax = Math.max(0, targets.size() - rows);
        combatScroll = Math.min(combatScroll, combatScrollMax);

        g.enableScissor(listX, listY, listX + listW, listY + rows * rowH);
        for (int i = combatScroll; i < Math.min(combatScroll + rows, targets.size()); i++) {
            OWClientTribeList.Entry e = targets.get(i);
            int y = listY + (i - combatScroll) * rowH;
            boolean hov = mouseX >= listX && mouseX < listX + listW && mouseY >= y && mouseY < y + rowH;
            if ((i & 1) == 0) g.fill(listX, y, listX + listW, y + rowH - 1, 0x18FFFFFF);
            float hoverAmt = animate("tribe" + i, hov ? 1f : 0f, 14f);
            if (hoverAmt > 0.01f) {
                g.fill(listX, y, listX + listW, y + rowH - 1, ((int) (60 * hoverAmt) << 24) | 0xFFFFFF);
                g.fill(listX, y, listX + 1, y + rowH - 1, ((int) (255 * hoverAmt) << 24) | 0xE8956A);
            }
            if (hov) hoverTribe = e;
            // La réputation adverse est l'information qui décide : on la double du gain potentiel.
            String rep = OWTribeReputationScreen.formatNumber(e.reputation());
            g.drawString(this.font, trimTo(e.name(), listW - 16 - this.font.width(rep)),
                    listX + 4, y + 3, 0xE8E8E8, false);
            g.drawString(this.font, rep, listX + listW - this.font.width(rep) - 3, y + 3,
                    hov ? 0xD8D8D8 : 0x9A9A9A, false);
        }
        g.disableScissor();
        if (hasScroll) drawScrollbar(g, f[2] - 7, listY, rows * rowH, combatScroll, combatScrollMax, rows, targets.size());
    }

    /** Tribus défiables : toutes sauf la mienne (le serveur refusera celles non inscrites à l'arène). */
    private List<OWClientTribeList.Entry> challengeableTribes() {
        OWTeam mine = OWClientTribeData.get();
        int myId = mine != null ? mine.getTeamId() : 0;
        List<OWClientTribeList.Entry> out = new ArrayList<>();
        for (OWClientTribeList.Entry e : OWClientTribeList.get()) {
            if (e.teamId() != myId) out.add(e);
        }
        return out;
    }

    // ── Phase CHALLENGE_SENT / RECEIVED ──────────────────────────────────────────
    private void renderWaiting(GuiGraphics g) {
        int[] f = combatFrame(g);
        int cx = leftPos + IMG_W / 2, cy = (f[1] + f[3]) / 2;
        g.drawCenteredString(this.font, Component.translatable("owteams.arena.challenge.waiting")
                .withStyle(Style.EMPTY.withBold(true).withColor(TextColor.fromRgb(0xFFD257))), cx, cy - 16, 0xFFD257);
        g.drawCenteredString(this.font, OWClientArenaState.get().outgoingTargetName(), cx, cy - 2, 0xE8E8E8);
        // Trois points animés, pour signaler que l'attente est vivante.
        int dots = (int) ((System.currentTimeMillis() / 400) % 4);
        g.drawCenteredString(this.font, ".".repeat(dots), cx, cy + 12, 0x9A9A9A);
    }

    private void renderIncomingChallenge(GuiGraphics g) {
        int[] f = combatFrame(g);
        var st = OWClientArenaState.get();
        int cx = leftPos + IMG_W / 2, cy = f[1] + 12;

        g.drawCenteredString(this.font, Component.translatable("owteams.arena.challenge.incoming")
                .withStyle(Style.EMPTY.withBold(true).withColor(TextColor.fromRgb(0xE8956A))), cx, cy, 0xE8956A);
        g.drawCenteredString(this.font, st.incomingChallengerName(), cx, cy + 14, 0xFFFFFF);

        // Comparatif de réputation + gain potentiel : de quoi décider en connaissance de cause.
        drawSmallCentered(g, Component.translatable("owteams.arena.challenge.their_rep",
                OWTribeReputationScreen.formatNumber(st.opponentReputation())), cx, cy + 30, 0xBBBBBB);
        drawSmallCentered(g, Component.translatable("owteams.arena.challenge.your_rep",
                OWTribeReputationScreen.formatNumber(st.myReputation())), cx, cy + 40, 0xBBBBBB);
        int gain = OWArena.reputationGain(st.myReputation(), st.opponentReputation());
        g.drawCenteredString(this.font, Component.translatable("owteams.arena.challenge.potential_gain",
                        OWTribeReputationScreen.formatNumber(gain))
                .withStyle(Style.EMPTY.withColor(TextColor.fromRgb(0x7ddd73))), cx, cy + 54, 0x7ddd73);

        if (!isChief()) {
            g.drawCenteredString(this.font, Component.translatable("owteams.arena.combat.chief_only"),
                    cx, f[3] - 12, 0x9A9A9A);
        }
    }

    // ── Phase SELECTION ──────────────────────────────────────────────────────────
    /**
     * Composition des équipes : ma colonne à gauche (aperçus 3D en direct), celle de l'adversaire à
     * droite masquée par des « ? » — on voit sa <i>progression</i> sans voir ses choix — et la liste
     * des créatures recrutables au centre.
     */
    private void renderSelection(GuiGraphics g, int mouseX, int mouseY) {
        var st = OWClientArenaState.get();
        int y0 = topPos + CONTENT_Y;
        float fade = easeOut(contentFade());

        // En-têtes de colonnes.
        drawSmallCentered(g, Component.translatable("owteams.arena.selection.you"),
                leftPos + 6 + SLOT / 2, y0, 0x7FD8A8);
        drawSmallCentered(g, Component.literal(trimTo(st.opponentName(), 60)),
                leftPos + IMG_W - 6 - SLOT / 2, y0, 0xE8956A);

        int slotsY = slotsTop();
        List<OWArenaFighter> mine = st.myFighters();

        // Colonne gauche : mes combattants, aperçu 3D de la créature réelle.
        for (int i = 0; i < OWArena.MAX_FIGHTERS; i++) {
            int x = leftPos + 6, y = slotsY + i * (SLOT + SLOT_GAP);
            OWArenaFighter f = i < mine.size() ? mine.get(i) : null;
            boolean hov = f != null && mouseX >= x && mouseX < x + SLOT && mouseY >= y && mouseY < y + SLOT;

            // Apparition décalée slot par slot : la colonne se remplit en cascade.
            float appear = easeOut(Math.min(1f, Math.max(0f, fade * 5f - i * 0.6f)));
            if (appear <= 0.02f) continue;

            drawSlot(g, x, y, f != null ? archetypeColor(f.archetypeOrdinal()) : 0x3A3A3A, hov, appear);
            if (f != null) {
                // Le slot survolé suit la souris ; les autres oscillent doucement, chacun déphasé.
                renderFighterPreview(g, f, x, y, SLOT, hov ? mouseX : Float.NaN, hov ? mouseY : i);
                drawSlotBadge(g, f, x, y);
                if (hov) hoverFighter = f;
            }
        }

        // Colonne droite : l'adversaire, masqué — on voit sa progression, pas ses choix.
        for (int i = 0; i < OWArena.MAX_FIGHTERS; i++) {
            int x = leftPos + IMG_W - 6 - SLOT, y = slotsY + i * (SLOT + SLOT_GAP);
            boolean filled = i < st.opponentFighterCount();
            float appear = easeOut(Math.min(1f, Math.max(0f, fade * 5f - i * 0.6f)));
            if (appear <= 0.02f) continue;
            drawSlot(g, x, y, filled ? 0xE8956A : 0x3A3A3A, false, appear);
            if (filled) {
                // Le « ? » respire, pour signaler que le camp adverse est bien vivant.
                float pulse = 0.62f + 0.38f * (float) Math.sin(System.currentTimeMillis() / 420.0 + i);
                int a = (int) (255 * pulse);
                g.drawCenteredString(this.font, "?", x + SLOT / 2, y + SLOT / 2 - 4, (a << 24) | 0xFFD257);
            } else {
                g.drawCenteredString(this.font, "·", x + SLOT / 2, y + SLOT / 2 - 4, 0xFF555555);
            }
        }

        // Centre : créatures recrutables.
        int listX = leftPos + 6 + SLOT + 4;
        int listW = (leftPos + IMG_W - 6 - SLOT - 4) - listX;
        int listH = OWArena.MAX_FIGHTERS * (SLOT + SLOT_GAP) - SLOT_GAP;
        renderCandidateList(g, listX, slotsY, listW, listH, mouseX, mouseY);

        // Compteur d'équipe + archétypes encore libres, dans la bande sous les colonnes.
        int infoY = slotsY + listH + 2;
        Component team = Component.translatable("owteams.arena.selection.count",
                mine.size(), OWArena.MAX_FIGHTERS);
        drawSmall(g, team.copy().withStyle(Style.EMPTY.withColor(TextColor.fromRgb(
                mine.isEmpty() ? 0x9A9A9A : 0x7FD8A8))), leftPos + 7, infoY, 0x7FD8A8);

        Component oppStatus = Component.translatable(st.opponentReady()
                ? "owteams.arena.selection.opponent_ready" : "owteams.arena.selection.opponent_waiting");
        int oppColor = st.opponentReady() ? 0x7ddd73 : 0x9A9A9A;
        float w = this.font.width(oppStatus) * 0.75f;
        drawSmall(g, oppStatus.copy().withStyle(Style.EMPTY.withColor(TextColor.fromRgb(oppColor))),
                leftPos + IMG_W - 7 - (int) w, infoY, oppColor);
    }

    /** Ordonnée du premier emplacement de combattant. */
    private int slotsTop() { return topPos + CONTENT_Y + 9; }

    /** Niveau + pastille d'archétype dans le coin d'un emplacement rempli. */
    private void drawSlotBadge(GuiGraphics g, OWArenaFighter f, int x, int y) {
        int chip = archetypeColor(f.archetypeOrdinal());
        g.fill(x + 2, y + 2, x + 5, y + 6, 0xFF000000 | chip);
        String lvl = String.valueOf(f.level());
        float s = 0.65f;
        float lw = this.font.width(lvl) * s;
        g.pose().pushPose();
        g.pose().translate(x + SLOT - 2 - lw, y + SLOT - 7, 0);
        g.pose().scale(s, s, 1f);
        g.drawString(this.font, lvl, 0, 0, 0xFFE8E8E8, true);
        g.pose().popPose();
    }

    /** Liste défilante des créatures recrutables, avec grisage des archétypes déjà pris. */
    private void renderCandidateList(GuiGraphics g, int x, int y, int w, int h, int mouseX, int mouseY) {
        List<OWArenaFighter> candidates = OWClientArenaState.candidates();
        g.fill(x, y, x + w, y + h, 0xCC0E0E10);

        if (candidates.isEmpty()) {
            drawSmallCentered(g, Component.translatable("owteams.arena.selection.no_candidate"),
                    x + w / 2, y + h / 2 - 4, 0x777777);
            candidateRows = 0;
            return;
        }

        int rowH = 13;
        int rows = h / rowH;
        candidateRows = rows;
        boolean hasScroll = candidates.size() > rows;
        int contentW = hasScroll ? w - 6 : w;
        combatScrollMax = Math.max(0, candidates.size() - rows);
        combatScroll = Math.min(combatScroll, combatScrollMax);

        g.enableScissor(x, y, x + contentW, y + h);
        for (int i = combatScroll; i < Math.min(combatScroll + rows, candidates.size()); i++) {
            OWArenaFighter f = candidates.get(i);
            int ry = y + (i - combatScroll) * rowH;
            boolean selected = OWClientArenaState.isSelected(f.entityUuid());
            boolean blocked = !selected && OWClientArenaState.archetypeTaken(f.archetypeOrdinal(), f.entityUuid());
            boolean hov = mouseX >= x && mouseX < x + contentW && mouseY >= ry && mouseY < ry + rowH;

            if ((i & 1) == 0) g.fill(x, ry, x + contentW, ry + rowH - 1, 0x14FFFFFF);
            if (selected) {
                // Engagé : liseré vert plein largeur, sans ambiguïté avec un simple survol.
                g.fill(x, ry, x + contentW, ry + rowH - 1, 0x384C9A5A);
                g.fill(x, ry, x + 1, ry + rowH - 1, 0xFF7ddd73);
            }
            // Le survol glisse au lieu d'apparaître sec (une valeur animée par ligne).
            float hoverAmt = animate("cand" + i, hov && !blocked ? 1f : 0f, 14f);
            if (hoverAmt > 0.01f) {
                g.fill(x, ry, x + contentW, ry + rowH - 1, ((int) (56 * hoverAmt) << 24) | 0xFFFFFF);
            }

            // Pastille d'archétype à gauche : lecture immédiate de la contrainte d'unicité.
            int chip = archetypeColor(f.archetypeOrdinal());
            g.fill(x + 2, ry + 3, x + 5, ry + rowH - 4, 0xFF000000 | (blocked ? dim(chip) : chip));

            int nameCol = blocked ? 0x666666 : (selected ? 0xB8F0C8 : 0xE8E8E8);
            // Suffixe d'état : coche si engagé, cadenas si l'archétype est déjà pris.
            String mark = selected ? "✔ " : (blocked ? "✖ " : "");
            String lvl = "L" + f.level();
            int markW = this.font.width(mark);
            if (!mark.isEmpty()) {
                g.drawString(this.font, mark, x + 8, ry + 3, selected ? 0x7ddd73 : 0x8A5A5A, false);
            }
            g.drawString(this.font, trimTo(f.name(), contentW - 12 - markW - this.font.width(lvl)),
                    x + 8 + markW, ry + 3, nameCol, false);
            g.drawString(this.font, lvl, x + contentW - this.font.width(lvl) - 3, ry + 3,
                    blocked ? 0x555555 : 0x9A9A9A, false);

            if (hov) hoverCandidate = f;
        }
        g.disableScissor();
        if (hasScroll) drawScrollbar(g, x + w - 5, y, h, combatScroll, combatScrollMax, rows, candidates.size());
    }

    /** Emplacement de combattant : cadre biseauté teinté par l'archétype, avec apparition en fondu. */
    private void drawSlot(GuiGraphics g, int x, int y, int accent, boolean hovered, float appear) {
        int alpha = (int) (255 * Math.max(0f, Math.min(1f, appear)));
        if (alpha <= 2) return;
        int bgA = (int) ((hovered ? 0xF0 : 0xE0) * appear);
        g.fill(x, y, x + SLOT, y + SLOT, (bgA << 24) | (hovered ? 0x1E1E24 : 0x141418));
        int a = (alpha << 24) | (accent & 0xFFFFFF);
        g.fill(x, y, x + SLOT, y + 1, a);
        g.fill(x, y + SLOT - 1, x + SLOT, y + SLOT, a);
        g.fill(x, y, x + 1, y + SLOT, a);
        g.fill(x + SLOT - 1, y, x + SLOT, y + SLOT, a);
        // Liseré clair au survol, pour que la cible du clic soit sans ambiguïté.
        if (hovered) {
            g.fill(x + 1, y + 1, x + SLOT - 1, y + 2, 0x55FFFFFF);
            g.fill(x + 1, y + SLOT - 2, x + SLOT - 1, y + SLOT - 1, 0x33FFFFFF);
        }
    }

    /**
     * Aperçu 3D d'un combattant. On instancie une créature <b>locale et jetable</b> depuis son type
     * et son skin (même approche que l'analyseur d'ADN) : la vraie créature peut être à des milliers
     * de blocs et n'être chargée sur aucun client.
     *
     * <p>L'échelle est <b>déduite de la boîte englobante</b> de l'espèce et non fixée en dur : sans
     * cela un kodiak (1,9 bloc) débordait largement du cadre là où un boa (1 bloc) flottait perdu au
     * milieu. {@code mx} vaut {@code NaN} pour une oscillation automatique — {@code my} porte alors
     * l'indice du slot, qui déphase l'oscillation d'un emplacement à l'autre.</p>
     */
    private void renderFighterPreview(GuiGraphics g, OWArenaFighter f, int x, int y, int box,
                                      float mx, float my) {
        LivingEntity preview = previewFor(f);
        if (preview == null) {
            g.drawCenteredString(this.font, "?", x + box / 2, y + box / 2 - 4, 0x777777);
            return;
        }

        // Ajustement au cadre : on prend la plus grande dimension pour que rien ne dépasse.
        float span = Math.max(preview.getBbWidth(), preview.getBbHeight());
        if (span <= 0.01f) span = 1f;
        int scale = Math.max(3, (int) (box * 0.80f / span));

        float px, py;
        if (Float.isNaN(mx)) {
            // Oscillation lente, déphasée par slot : la colonne « respire » sans être agitée.
            float phase = (System.currentTimeMillis() / 1000f) * 0.8f + my * 1.3f;
            px = x + box / 2f + (float) Math.sin(phase) * 26f;
            py = y + box * 0.35f;
        } else {
            px = mx; py = my;
        }

        // Le rendu d'entité écrit dans le depth buffer : on l'isole pour ne pas percer le cadre.
        g.enableScissor(x + 1, y + 1, x + box - 1, y + box - 1);
        InventoryScreen.renderEntityInInventoryFollowsMouse(g,
                x + 1, y + 1, x + box - 1, y + box - 1,
                scale, entityYOffset(preview), px, py, preview);
        g.disableScissor();
    }

    /**
     * Décalage vertical de l'aperçu, en unités de {@code scale} (positif = vers le bas), autour de la
     * valeur que le vanilla utilise pour le joueur dans l'inventaire.
     *
     * <p>Correction volontairement <b>douce</b> : l'essentiel du cadrage est déjà assuré par la mise
     * à l'échelle automatique dans {@link #renderFighterPreview}. On se contente ici de rattraper les
     * silhouettes très éloignées de la taille d'un joueur — un boa rampant contre un kodiak dressé —
     * sans risquer de pousser quoi que ce soit hors du cadre.</p>
     */
    private static float entityYOffset(LivingEntity entity) {
        float h = entity.getBbHeight();
        if (h <= 0.01f) return 0.0625f;
        return Math.max(0f, Math.min(0.4f, 0.0625f + (h - 1.4f) * 0.06f));
    }

    private LivingEntity previewFor(OWArenaFighter f) {
        // La clé inclut la VARIANTE autant que le skin : deux tigres de même skin mais de variantes
        // différentes n'ont pas la même apparence, et les confondre affichait la mauvaise créature.
        String key = f.entityTypeId() + ":" + f.typeVariant() + ":" + f.skinIndex();
        LivingEntity cached = previewCache.get(key);
        if (cached != null) return cached;

        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return null;
        ResourceLocation rl = ResourceLocation.tryParse(f.entityTypeId());
        if (rl == null) return null;
        EntityType<?> type = BuiltInRegistries.ENTITY_TYPE.getOptional(rl).orElse(null);
        if (type == null) return null;
        if (!(type.create(mc.level) instanceof LivingEntity living)) return null;
        if (living instanceof OWEntity owE) {
            // Variante PUIS skin, dans cet ordre : la variante porte le pelage naturel, le skin le
            // cosmétique par-dessus. Les deux sont posés même à 0, l'instance neuve étant par défaut.
            owE.setVariant(owE, f.typeVariant());
            owE.changeSkinSilent(f.skinIndex());
            owE.setLevel(f.level());
        }
        // Adulte figé, sans animation de marche parasite dans un cadre de 20 px.
        living.yBodyRot = 0f;
        living.setYRot(0f);
        living.setXRot(0f);
        living.yHeadRot = 0f;

        previewCache.put(key, living);
        return living;
    }

    // ── Phases FIGHTING / ENDED ──────────────────────────────────────────────────
    private void renderFighting(GuiGraphics g) {
        int[] f = combatFrame(g);
        var st = OWClientArenaState.get();
        int cx = leftPos + IMG_W / 2, cy = f[1] + 10;

        // Le titre pulse doucement : l'écran reste vivant même quand rien ne bouge côté jauges.
        float beat = 0.72f + 0.28f * (float) Math.sin(System.currentTimeMillis() / 500.0);
        int titleA = (int) (255 * beat);
        g.drawCenteredString(this.font, Component.translatable("owteams.arena.fight.ongoing")
                .withStyle(Style.EMPTY.withBold(true)), cx, cy, (titleA << 24) | 0xE8956A);

        // Deux jauges de survivants qui se font face.
        int total = OWArena.MAX_FIGHTERS;
        drawSurvivorBar(g, f[0] + 6, cy + 20, f[2] - f[0] - 12,
                Component.translatable("owteams.arena.selection.you"), st.aliveMine(), total, 0x4C9A5A, "aliveMine");
        drawSurvivorBar(g, f[0] + 6, cy + 46, f[2] - f[0] - 12,
                Component.literal(trimTo(st.opponentName(), 80)), st.aliveOpponent(), total, 0xB04A3A, "aliveOpp");

        drawSmallCentered(g, Component.translatable("owteams.arena.fight.hint"), cx, f[3] - 14, 0x777777);
    }

    /** Jauge de survivants : le remplissage descend en glissant, pas d'un coup, à chaque perte. */
    private void drawSurvivorBar(GuiGraphics g, int x, int y, int w, Component label,
                                 int alive, int total, int color, String animKey) {
        g.drawString(this.font, label, x, y, 0xE8E8E8, false);
        String count = alive + " / " + total;
        g.drawString(this.font, count, x + w - this.font.width(count), y, 0xBBBBBB, false);

        int by = y + 11, bh = 6;
        g.fill(x - 1, by - 1, x + w + 1, by + bh + 1, 0xFF000000);
        g.fill(x, by, x + w, by + bh, 0xFF16161A);

        float target = total <= 0 ? 0f : alive / (float) total;
        float shown = animate(animKey, target, 4.5f);
        int fill = Math.round(w * shown);
        if (fill > 0) {
            g.fill(x, by, x + fill, by + bh, 0xFF000000 | color);
            g.fill(x, by, x + fill, by + 2, 0x40FFFFFF);
        }
        // Traîne claire là où la jauge est en train de redescendre : la perte est lisible.
        int targetFill = Math.round(w * target);
        if (fill > targetFill) g.fill(x + targetFill, by, x + fill, by + bh, 0x66FFFFFF);
        // Graduations : une par combattant.
        for (int i = 1; i < total; i++) {
            int gx = x + Math.round(w * (i / (float) total));
            g.fill(gx, by, gx + 1, by + bh, 0x55000000);
        }
    }

    private void renderResult(GuiGraphics g) {
        int[] f = combatFrame(g);
        var st = OWClientArenaState.get();
        int cx = leftPos + IMG_W / 2, cy = (f[1] + f[3]) / 2;
        OWArena.Result r = OWClientArenaState.result();
        int color = switch (r) { case WIN -> 0x7ddd73; case LOSS -> 0xE04444; default -> 0xBBBBBB; };
        float in = easeOut(contentFade());

        // Bandeau de verdict qui se déplie horizontalement, puis titre qui « pop ».
        int bandH = 26, bandW = (int) ((f[2] - f[0]) * in);
        g.fill(cx - bandW / 2, cy - 24, cx + bandW / 2, cy - 24 + bandH, 0x33000000 | (color & 0xFFFFFF));
        g.fill(cx - bandW / 2, cy - 24, cx + bandW / 2, cy - 23, 0xFF000000 | (color & 0xFFFFFF));
        g.fill(cx - bandW / 2, cy - 25 + bandH, cx + bandW / 2, cy - 24 + bandH, 0xFF000000 | (color & 0xFFFFFF));

        String key = switch (r) {
            case WIN -> "owteams.arena.result.win_title";
            case LOSS -> "owteams.arena.result.loss_title";
            default -> "owteams.arena.result.draw_title";
        };
        Component title = Component.translatable(key)
                .withStyle(Style.EMPTY.withBold(true).withColor(TextColor.fromRgb(color)));
        float pop = 0.6f + 0.4f * in + (r == OWArena.Result.WIN
                ? 0.06f * (float) Math.sin(System.currentTimeMillis() / 260.0) : 0f);
        g.pose().pushPose();
        g.pose().translate(cx, cy - 17, 0);
        g.pose().scale(pop, pop, 1f);
        g.drawCenteredString(this.font, title, 0, 0, color);
        g.pose().popPose();

        // Adversaire et gains concrets, plutôt qu'un simple « allez voir ailleurs ».
        if (!st.opponentName().isEmpty()) {
            drawSmallCentered(g, Component.translatable("owteams.arena.result.versus", st.opponentName()),
                    cx, cy + 6, 0x9A9A9A);
        }
        if (r == OWArena.Result.WIN) {
            int gain = OWArena.reputationGain(st.myReputation(), st.opponentReputation());
            g.drawCenteredString(this.font, Component.translatable("owteams.arena.result.gains",
                            OWTribeReputationScreen.formatNumber(gain), OWArena.PRESTIGE_WIN)
                    .withStyle(Style.EMPTY.withColor(TextColor.fromRgb(0x7ddd73))), cx, cy + 18, 0x7ddd73);
        } else if (r == OWArena.Result.LOSS) {
            g.drawCenteredString(this.font, Component.translatable("owteams.arena.result.consolation",
                            OWArena.PRESTIGE_LOSS)
                    .withStyle(Style.EMPTY.withColor(TextColor.fromRgb(0xBBBBBB))), cx, cy + 18, 0xBBBBBB);
        }

        drawSmallCentered(g, Component.translatable("owteams.arena.result.returning"), cx, f[3] - 12, 0x777777);
    }

    /** Visibilité des boutons de la vue combat selon la phase et le rôle. */
    private void updateCombatButtons(boolean inCombatView) {
        OWArena.Phase phase = OWClientArenaState.phase();
        boolean chief = isChief();
        chalAcceptBtn.visible = inCombatView && chief && phase == OWArena.Phase.CHALLENGE_RECEIVED;
        chalDeclineBtn.visible = chalAcceptBtn.visible;
        cancelArenaBtn.visible = inCombatView && chief
                && (phase == OWArena.Phase.CHALLENGE_SENT || phase == OWArena.Phase.SELECTION);
        readyBtn.visible = inCombatView && chief && phase == OWArena.Phase.SELECTION;
        if (readyBtn.visible) {
            boolean ready = OWClientArenaState.get().myReady();
            readyBtn.setMessage(Component.translatable(ready
                    ? "owteams.arena.selection.cancel_ready" : "owteams.arena.selection.start")
                    .withStyle(Style.EMPTY.withColor(TextColor.fromRgb(ready ? 0x7ddd73 : 0xFFFFFF))));
            readyBtn.active = !OWClientArenaState.myFighters().isEmpty();
            // Le bouton « Annuler » se décale à gauche pour cohabiter avec « Commencer ».
            cancelArenaBtn.setPosition(leftPos + 6, topPos + IMG_H - 22);
            cancelArenaBtn.setWidth(40);
            readyBtn.setPosition(leftPos + IMG_W / 2 - 30, topPos + IMG_H - 22);
            readyBtn.setWidth(88);
        } else {
            cancelArenaBtn.setPosition(leftPos + IMG_W / 2 - 45, topPos + IMG_H - 22);
            cancelArenaBtn.setWidth(90);
        }

        boolean confirming = pendingChallengeId != 0;
        challengeYesBtn.visible = confirming;
        challengeNoBtn.visible = confirming;
    }

    /** Boîte de confirmation avant d'envoyer un défi (action irréversible côté adversaire). */
    private void renderChallengeConfirm(GuiGraphics g, int mouseX, int mouseY, float partial) {
        g.fill(0, 0, this.width, this.height, 0x99000000);
        Component title = Component.translatable("owteams.arena.challenge.confirm_title");
        Component body = Component.translatable("owteams.arena.challenge.confirm_body", pendingChallengeName);
        int ow = Math.min(this.width - 20, Math.max(190, Math.max(this.font.width(title), this.font.width(body)) + 24));
        int oh = 58, cx = this.width / 2, ox = cx - ow / 2, oy = this.height / 2 - oh / 2;
        OWTribeDashboardScreen.drawConfirmBox(g, ox, oy, ow, oh, title, body, 0xE8956A);
        challengeYesBtn.setPosition(cx - 64, oy + oh - 20);
        challengeNoBtn.setPosition(cx + 4, oy + oh - 20);
        challengeYesBtn.render(g, mouseX, mouseY, partial);
        challengeNoBtn.render(g, mouseX, mouseY, partial);
    }

    /** Tooltip d'un adversaire potentiel : réputation, gain en cas de victoire, et rappel du risque. */
    private void renderTribeTooltip(GuiGraphics g, OWClientTribeList.Entry e, int mouseX, int mouseY) {
        var st = OWClientArenaState.get();
        int gain = OWArena.reputationGain(st.myReputation(), e.reputation());
        List<FormattedCharSequence> tip = new ArrayList<>();
        tip.add(Component.literal(e.name()).withStyle(Style.EMPTY.withBold(true)
                .withColor(TextColor.fromRgb(0xFFD257))).getVisualOrderText());
        tip.add(Component.translatable("owteams.arena.challenge.their_rep",
                OWTribeReputationScreen.formatNumber(e.reputation()))
                .withStyle(Style.EMPTY.withColor(TextColor.fromRgb(0xBBBBBB))).getVisualOrderText());
        tip.add(Component.translatable("owteams.arena.challenge.potential_gain",
                OWTribeReputationScreen.formatNumber(gain))
                .withStyle(Style.EMPTY.withColor(TextColor.fromRgb(0x7ddd73))).getVisualOrderText());
        tip.add(Component.translatable("owteams.arena.combat.click_to_challenge")
                .withStyle(Style.EMPTY.withColor(TextColor.fromRgb(0x777777))).getVisualOrderText());
        g.renderTooltip(this.font, tip, mouseX, mouseY);
    }

    /** Tooltip d'un combattant : nom, propriétaire, niveau et archétype. */
    private void renderFighterTooltip(GuiGraphics g, OWArenaFighter f, int mouseX, int mouseY) {
        List<FormattedCharSequence> tip = new ArrayList<>();
        tip.add(Component.literal(f.name()).withStyle(Style.EMPTY.withBold(true)
                .withColor(TextColor.fromRgb(archetypeColor(f.archetypeOrdinal())))).getVisualOrderText());
        tip.add(Component.translatable(f.archetypeKey())
                .withStyle(Style.EMPTY.withColor(TextColor.fromRgb(archetypeColor(f.archetypeOrdinal()))))
                .getVisualOrderText());
        tip.add(Component.translatable("owteams.arena.fighter.level", f.level())
                .withStyle(Style.EMPTY.withColor(TextColor.fromRgb(0xBBBBBB))).getVisualOrderText());
        tip.add(Component.translatable("owteams.arena.fighter.owner", f.ownerName())
                .withStyle(Style.EMPTY.withColor(TextColor.fromRgb(0x888888))).getVisualOrderText());
        g.renderTooltip(this.font, tip, mouseX, mouseY);
    }

    // ── Animation ────────────────────────────────────────────────────────────────
    /**
     * Fait glisser une valeur nommée vers {@code target} au lieu de la faire sauter. Le lissage est
     * exponentiel et indexé sur le temps réel, donc indépendant du nombre d'images par seconde.
     */
    private float animate(String key, float target) {
        return animate(key, target, 9f);
    }

    private float animate(String key, float target, float speed) {
        float current = animated.getOrDefault(key, target);
        float next = current + (target - current) * Math.min(1f, frameDelta * speed);
        if (Math.abs(target - next) < 0.0005f) next = target;
        animated.put(key, next);
        return next;
    }

    /** Progression [0..1] du fondu d'apparition du contenu courant. */
    private float contentFade() {
        return Math.min(1f, (System.currentTimeMillis() - contentFadeStart) / 220f);
    }

    /** Redémarre le fondu (bascule de vue, changement de phase). */
    private void restartContentFade() {
        contentFadeStart = System.currentTimeMillis();
    }

    /** Courbe d'accélération douce, pour que les apparitions ne soient pas linéaires. */
    private static float easeOut(float t) {
        float c = Math.max(0f, Math.min(1f, t));
        return 1f - (1f - c) * (1f - c);
    }

    // ── Utilitaires d'affichage ──────────────────────────────────────────────────
    private static int archetypeColor(int ordinal) {
        return switch (ordinal) {
            case 0 -> 0x7FA8D8; // TANK
            case 1 -> 0xD87F9E; // ASSASSIN
            case 2 -> 0xD8A87F; // MARAUDER
            case 3 -> 0x7FD89A; // HEALER
            case 4 -> 0xD87F7F; // BERSERKER
            case 5 -> 0xD8D07F; // SCOUT
            default -> 0xAAAAAA; // NORMAL
        };
    }

    /** Version assombrie d'une couleur, pour les entrées indisponibles. */
    private static int dim(int rgb) {
        return (((rgb >> 16) & 0xFF) / 3 << 16) | (((rgb >> 8) & 0xFF) / 3 << 8) | ((rgb & 0xFF) / 3);
    }

    private String trimTo(String s, int maxW) {
        if (s == null) return "";
        String out = s;
        while (this.font.width(out) > maxW && out.length() > 1) out = out.substring(0, out.length() - 1);
        return out;
    }

    /** Bouton de bascule Récompenses ⇄ Combat, au gabarit d'un onglet (image à venir : fond seul). */
    private void renderSwitchButton(GuiGraphics g, int mouseX, int mouseY) {
        int x = switchX(), y = switchY();
        boolean hov = mouseX >= x && mouseX < x + SWITCH_W && mouseY >= y && mouseY < y + SWITCH_H;
        g.blit(OW_INVENTORY, x, y, hov ? 20 : 0, 206, SWITCH_W, SWITCH_H);
        // L'icône reste vide tant que l'image dédiée n'existe pas ; le tooltip est rendu en fin de frame.
    }

    // ── Coffre dessiné à la main ─────────────────────────────────────────────────
    /**
     * Coffre en pixel-art procédural (l'image définitive n'existe pas encore) : couvercle, corps,
     * ferrures et serrure teintés par la couleur du palier. {@code lift} soulève le couvercle
     * (0 = fermé, 1 = grand ouvert) pendant l'animation d'ouverture.
     */
    private void drawChest(GuiGraphics g, int x, int y, int w, int h,
                           int accent, boolean active, boolean hovered, float lift) {
        int base = accent & 0xFFFFFF;
        float dim = active ? 1f : 0.32f;
        int body = shade(base, hovered && active ? 1.15f : 1.0f, dim);
        int bodyDark = shade(base, 0.55f, dim);
        int bodyLight = shade(base, 1.45f, dim);
        int iron = shade(0xC8C8D0, hovered && active ? 1.1f : 1.0f, dim);
        int ironDark = shade(0x50505A, 1.0f, dim);
        int outline = shade(0x1A1A1E, 1.0f, 1f);

        int lidH = Math.round(h * 0.42f);
        int lidY = y - Math.round(lift * (h * 0.55f));

        // Corps.
        int bodyY = y + lidH;
        g.fill(x, bodyY, x + w, y + h, outline);
        g.fill(x + 1, bodyY + 1, x + w - 1, y + h - 1, body);
        g.fill(x + 1, bodyY + 1, x + w - 1, bodyY + 3, bodyLight);          // arête supérieure
        g.fill(x + 1, y + h - 3, x + w - 1, y + h - 1, bodyDark);           // ombre basse
        // Ferrures verticales du corps.
        for (int fx : new int[]{ x + 6, x + w - 9 }) {
            g.fill(fx, bodyY + 1, fx + 3, y + h - 1, iron);
            g.fill(fx, bodyY + 1, fx + 1, y + h - 1, ironDark);
        }

        // Couvercle (bombé : deux paliers de largeur).
        g.fill(x, lidY + 2, x + w, lidY + lidH, outline);
        g.fill(x + 2, lidY, x + w - 2, lidY + 3, outline);
        g.fill(x + 1, lidY + 3, x + w - 1, lidY + lidH, body);
        g.fill(x + 3, lidY + 1, x + w - 3, lidY + 4, body);
        g.fill(x + 3, lidY + 1, x + w - 3, lidY + 2, bodyLight);           // reflet du dôme
        g.fill(x + 1, lidY + lidH - 2, x + w - 1, lidY + lidH, bodyDark);  // ombre sous le couvercle
        for (int fx : new int[]{ x + 6, x + w - 9 }) {
            g.fill(fx, lidY + 2, fx + 3, lidY + lidH, iron);
            g.fill(fx, lidY + 2, fx + 1, lidY + lidH, ironDark);
        }

        // Serrure, à cheval sur le couvercle fermé (disparaît quand il se soulève).
        if (lift < 0.15f) {
            int lx = x + w / 2 - 4, ly = y + lidH - 4;
            g.fill(lx, ly, lx + 8, ly + 9, ironDark);
            g.fill(lx + 1, ly + 1, lx + 7, ly + 8, iron);
            g.fill(lx + 3, ly + 3, lx + 5, ly + 6, 0xFF15151A);
        }

        // Lueur intérieure quand le couvercle est levé.
        if (lift > 0.05f) {
            int a = (int) (200 * Math.min(1f, lift * 1.6f));
            g.fill(x + 3, bodyY - 2, x + w - 3, bodyY + 4, (a << 24) | 0xFFF2C0);
        }

        // Halo de survol.
        if (hovered && active && lift == 0f) {
            g.fill(x - 1, y - 1, x + w + 1, y, 0x55FFFFFF);
            g.fill(x - 1, y + h, x + w + 1, y + h + 1, 0x55FFFFFF);
            g.fill(x - 1, y, x, y + h, 0x55FFFFFF);
            g.fill(x + w, y, x + w + 1, y + h, 0x55FFFFFF);
        }
    }

    /** Éclaircit/assombrit une couleur RGB puis applique un facteur de désaturation vers le gris. */
    private static int shade(int rgb, float factor, float vividness) {
        int r = Math.min(255, Math.round(((rgb >> 16) & 0xFF) * factor));
        int gg = Math.min(255, Math.round(((rgb >> 8) & 0xFF) * factor));
        int b = Math.min(255, Math.round((rgb & 0xFF) * factor));
        if (vividness < 1f) {
            int grey = Math.round((r + gg + b) / 3f * 0.75f);
            r = Math.round(grey + (r - grey) * vividness);
            gg = Math.round(grey + (gg - grey) * vividness);
            b = Math.round(grey + (b - grey) * vividness);
        }
        return 0xFF000000 | (r << 16) | (gg << 8) | b;
    }

    /** Barre de progression vers le prochain coffre (remplissage teinté + curseur). */
    private void drawProgressBar(GuiGraphics g, int x, int y, float progress, int accent) {
        g.fill(x - 1, y - 1, x + BAR_W + 1, y + BAR_H + 1, 0xFF000000);
        g.fill(x, y, x + BAR_W, y + BAR_H, 0xFF16161A);
        int fillW = Math.max(0, Math.min(BAR_W, Math.round(BAR_W * progress)));
        if (fillW > 0) {
            g.fill(x, y, x + fillW, y + BAR_H, 0xFF000000 | (accent & 0xFFFFFF));
            g.fill(x, y, x + fillW, y + 2, 0x44FFFFFF);                      // reflet
            if (fillW < BAR_W) g.fill(x + fillW - 1, y - 1, x + fillW + 1, y + BAR_H + 1, 0xFFFFFFFF);
        }
    }

    // ── Overlay d'ouverture de coffre ────────────────────────────────────────────
    private void renderOpeningOverlay(GuiGraphics g) {
        long elapsed = System.currentTimeMillis() - openingStart;
        int cx = this.width / 2, cy = this.height / 2;
        int accent = reward != null ? reward.chest().accent() : 0xE9B115;

        // Le monde s'efface de plus en plus à mesure que la tension monte.
        int veilAlpha = switch (opening) {
            case ANTICIPATION -> (int) (216 * Math.min(1f, elapsed / (float) ANTICIPATION_MS));
            case SHAKE -> 216 + (int) (24 * Math.min(1f, elapsed / (float) SHAKE_MS));
            case CHARGE -> 240 + (int) (15 * Math.min(1f, elapsed / (float) CHARGE_MS));
            default -> 236;
        };
        g.fill(0, 0, this.width, this.height, (Math.min(255, veilAlpha) << 24));
        OWCinematicFx.drawVignette(g, this.width, this.height, 1f);

        switch (opening) {
            case ANTICIPATION -> {
                // Le coffre arrive de loin et se pose : on prend le temps de le regarder.
                float p = OWCinematicFx.easeOut(Math.min(1f, elapsed / (float) ANTICIPATION_MS));
                drawChestGlow(g, cx, cy, accent, 0.25f * p, 0.9f);
                drawBigChest(g, cx, cy, accent, 0f, 0.55f + 0.45f * p);
                drawOpeningCaption(g, cx, cy, "owteams.arena.opening", 0x9A9A9A, p);
            }
            case SHAKE -> {
                float p = Math.min(1f, elapsed / (float) SHAKE_MS);
                // Tremblement qui s'emballe : amplitude et fréquence croissent ensemble.
                float freq = 26.0f - 16.0f * p;
                int amp = Math.round(1f + p * p * 9f);
                int dx = (int) (Math.sin(elapsed / freq) * amp);
                int dy = (int) (Math.cos(elapsed / (freq * 0.72)) * amp * 0.6);

                drawChestGlow(g, cx + dx, cy + dy, accent, 0.25f + 0.75f * p * p,
                        0.9f + 0.35f * (float) Math.sin(elapsed / 90.0) * p);
                drawBigChest(g, cx + dx, cy + dy, accent, 0f, 1f);
                drawSeamCracks(g, cx + dx, cy + dy, p, elapsed);
                drawOpeningCaption(g, cx, cy, "owteams.arena.opening", 0xCCCCCC, 1f);
            }
            case CHARGE -> {
                // Suspension : tout s'immobilise, la lumière devient aveuglante.
                float p = Math.min(1f, elapsed / (float) CHARGE_MS);
                drawChestGlow(g, cx, cy, accent, 1f + 2.4f * p, 1f);
                drawBigChest(g, cx, cy, accent, 0f, 1f + 0.08f * p);
                drawSeamCracks(g, cx, cy, 1f, elapsed);
                // Voile blanc qui envahit l'écran juste avant la rupture.
                g.fill(0, 0, this.width, this.height, ((int) (150 * p * p) << 24) | 0xFFFFFF);
            }
            case BURST -> {
                float p = Math.min(1f, elapsed / (float) BURST_MS);
                float since = elapsed / 1000f;
                // Éclair de rupture, puis les effets prennent le relais.
                g.fill(0, 0, this.width, this.height, ((int) (235 * (1f - p) * (1f - p)) << 24) | 0xFFFFFF);
                OWCinematicFx.drawRays(g, cx, cy, burstRays, since, 1f, accent, 0.8f);
                OWCinematicFx.drawShockwave(g, cx, cy, since, 1f);
                drawChestGlow(g, cx, cy, accent, 2.0f * (1f - p), 1f);
                drawBigChest(g, cx, cy, accent, p, 1f);
                drawBurstParticles(g, cx, cy - 10, since, accent);
            }
            case REVEAL -> {
                float since = (BURST_MS + elapsed) / 1000f;
                OWCinematicFx.drawRays(g, cx, cy - 46, burstRays, since, 1f, accent, 0.8f);
                drawChestGlow(g, cx, cy - 46, accent, 0.5f, 1f);
                drawBigChest(g, cx, cy - 46, accent, 1f, 1f);
                drawBurstParticles(g, cx, cy - 56, since, accent);
                renderRewardCards(g, cx, cy - 4, elapsed, accent);
            }
            default -> {}
        }
    }

    /** Légende sous le coffre pendant les phases d'attente. */
    private void drawOpeningCaption(GuiGraphics g, int cx, int cy, String key, int color, float alpha) {
        int a = (int) (255 * Math.max(0f, Math.min(1f, alpha)));
        if (a <= 4) return;
        g.drawCenteredString(this.font, Component.translatable(key), cx, cy + 66, (a << 24) | (color & 0xFFFFFF));
    }

    /**
     * Halo qui enfle autour du coffre. {@code intensity} dépasse 1 pendant la charge : le halo
     * devient alors plus large que l'écran et vire au blanc, ce qui « aveugle » avant la rupture.
     */
    private void drawChestGlow(GuiGraphics g, int cx, int cy, int accent, float intensity, float pulse) {
        if (intensity <= 0.01f) return;
        int base = accent & 0xFFFFFF;
        for (int k = 0; k < 5; k++) {
            float layer = 1f - k / 5f;
            int r = (int) ((34 + k * 26) * (0.6f + intensity) * pulse);
            int a = (int) (46 * layer * Math.min(2.2f, intensity));
            if (a <= 0) continue;
            // Au-delà de la pleine intensité, la teinte se décolore vers le blanc.
            int color = intensity > 1f ? blend(base, 0xFFFFFF, Math.min(1f, intensity - 1f)) : base;
            g.fill(cx - r, cy - r, cx + r, cy + r, (Math.min(255, a) << 24) | color);
        }
    }

    /** Fissures de lumière s'échappant de la jointure du couvercle, de plus en plus larges. */
    private void drawSeamCracks(GuiGraphics g, int cx, int cy, float p, long elapsed) {
        if (p <= 0.05f) return;
        float scale = 2.0f * (reward != null ? reward.chest().size().scale() : 1f);
        int halfW = (int) (CHEST_W * scale / 2f);
        int seamY = cy - (int) (CHEST_H * scale / 2f) + (int) (CHEST_H * 0.42f * scale);

        // Scintillement : la lumière n'est jamais tout à fait stable.
        float flicker = 0.75f + 0.25f * (float) Math.sin(elapsed / 45.0);
        int thickness = Math.max(1, (int) (p * 6f * flicker));
        int alpha = (int) (255 * Math.min(1f, p * 1.3f) * flicker);
        g.fill(cx - halfW, seamY - thickness / 2, cx + halfW, seamY - thickness / 2 + thickness,
                (alpha << 24) | 0xFFF6D0);

        // Rais verticaux qui percent par la fente, de plus en plus longs.
        int beams = 7;
        for (int i = 0; i < beams; i++) {
            int bx = cx - halfW + (int) ((i + 0.5f) * (halfW * 2f / beams));
            int len = (int) (p * p * (26 + (i * 13 % 22)) * flicker);
            if (len <= 0) continue;
            int a = (int) (150 * p * flicker);
            g.fill(bx - 1, seamY - len, bx + 1, seamY, (a << 24) | 0xFFF6D0);
        }
    }

    /** Mélange deux couleurs RGB. */
    private static int blend(int a, int b, float t) {
        int ar = (a >> 16) & 0xFF, ag = (a >> 8) & 0xFF, ab = a & 0xFF;
        int br = (b >> 16) & 0xFF, bg = (b >> 8) & 0xFF, bb = b & 0xFF;
        return (Math.round(ar + (br - ar) * t) << 16)
                | (Math.round(ag + (bg - ag) * t) << 8)
                | Math.round(ab + (bb - ab) * t);
    }

    /** Le coffre de l'overlay, à l'échelle ×2 et centré sur {@code (cx, cy)}. */
    private void drawBigChest(GuiGraphics g, int cx, int cy, int accent, float lift, float extraScale) {
        // Le gros plan reprend le gabarit du coffre gagné (petit / normal / grand).
        final float scale = 2.0f * (reward != null ? reward.chest().size().scale() : 1f) * extraScale;
        g.pose().pushPose();
        g.pose().translate(cx, cy, 0);
        g.pose().scale(scale, scale, 1f);
        drawChest(g, -CHEST_W / 2, -CHEST_H / 2, CHEST_W, CHEST_H, accent, true, false, lift);
        g.pose().popPose();
    }

    private void drawBurstParticles(GuiGraphics g, int cx, int cy, float t, int accent) {
        int base = accent & 0xFFFFFF;
        for (float[] pt : burstParticles) {
            float ang = pt[0], speed = pt[1], size = pt[2];
            float px = cx + (float) Math.cos(ang) * speed * t;
            float py = cy + (float) Math.sin(ang) * speed * t + 0.5f * 190f * t * t; // gravité
            float life = Math.max(0f, 1f - t / 1.6f);
            int a = (int) (255 * life);
            if (a <= 0) continue;
            int s = Math.max(1, (int) (size * (0.5f + 0.5f * life)));
            int ix = (int) px, iy = (int) py;
            g.fill(ix - s, iy - s, ix + s, iy + s, (a << 24) | base);
            g.fill(ix - 1, iy - 1, ix + 1, iy + 1, (Math.min(255, a + 60) << 24) | 0xFFFFFF);
        }
    }

    /** Cartes de butin révélées une par une, puis la ligne de Pièces Sauvages. */
    private void renderRewardCards(GuiGraphics g, int cx, int cy, long elapsed, int accent) {
        if (reward == null) return;
        List<ItemStack> items = reward.items();

        g.drawCenteredString(this.font, Component.translatable("owteams.arena.reward.title")
                .withStyle(Style.EMPTY.withBold(true).withColor(TextColor.fromRgb(accent))), cx, cy, accent);

        final int card = 26, gap = 4;
        int n = items.size();
        int rowW = n * card + Math.max(0, n - 1) * gap;
        int startX = cx - rowW / 2, cardY = cy + 14;

        int revealed = (int) Math.min(n, elapsed / REVEAL_STEP_MS);
        for (int i = 0; i < revealed; i++) {
            int x = startX + i * (card + gap);
            long age = elapsed - i * REVEAL_STEP_MS;
            float pop = age < 140 ? 1f + 0.30f * (float) Math.sin(age / 140f * Math.PI) : 1f;

            g.pose().pushPose();
            g.pose().translate(x + card / 2f, cardY + card / 2f, 0);
            g.pose().scale(pop, pop, 1f);
            g.pose().translate(-card / 2f, -card / 2f, 0);
            g.fill(0, 0, card, card, 0xF0141418);
            g.fill(0, 0, card, 1, 0xFF000000 | (accent & 0xFFFFFF));
            g.fill(0, card - 1, card, card, 0x33FFFFFF);
            g.pose().popPose();

            // L'item et sa quantité sont rendus hors du pop : renderItem gère sa propre pile de matrices.
            ItemStack stack = items.get(i);
            g.renderItem(stack, x + (card - 16) / 2, cardY + (card - 16) / 2);
            g.renderItemDecorations(this.font, stack, x + (card - 16) / 2, cardY + (card - 16) / 2);
        }

        // Pièces Sauvages une fois tous les items révélés.
        if (revealed >= n) {
            int coinsY = cardY + card + 8;
            g.drawCenteredString(this.font, Component.translatable("owteams.arena.reward.coins", reward.coins())
                    .withStyle(Style.EMPTY.withColor(TextColor.fromRgb(0xFFD257))), cx, coinsY, 0xFFD257);
            if (elapsed > (n + 1) * REVEAL_STEP_MS + 400) {
                g.drawCenteredString(this.font, Component.translatable("owteams.arena.reward.dismiss"),
                        cx, coinsY + 18, 0x8A8A8A);
            }
        }
    }
}
