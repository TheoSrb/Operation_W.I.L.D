package net.tiew.operationWild.screen.tribe;

import com.mojang.blaze3d.systems.RenderSystem;
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
import net.tiew.operationWild.gui.OWCinematicState;
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
    /**
     * Dimensions natives des sprites de coffre dans {@code ow_teams_sprites.png}.
     *
     * <p>Les trois gabarits (petit / moyen / grand) partagent la <b>même toile</b> : la différence de
     * taille est dessinée dans l'image, pas appliquée à l'exécution. Aucun facteur d'échelle n'est
     * donc utilisé — ce qui préserve la netteté du pixel-art, une réduction non entière la détruisant.</p>
     */
    private static final int CHEST_W = 37, CHEST_H = 29;

    // Durées de l'animation (ms).
    private static final long ANTICIPATION_MS = 700;   // le coffre s'installe, le monde s'efface
    private static final long SHAKE_MS = 2000;         // tremblement qui s'emballe, lumière qui fuit
    private static final long CHARGE_MS = 550;         // silence : tout se fige avant de céder
    private static final long BURST_MS = 720;          // rupture : le couvercle doit avoir le temps de partir
    private static final long REVEAL_STEP_MS = 170;    // cadence de révélation des lots
    private static final long COINS_DELAY_MS = 520;    // silence avant d'annoncer les Pièces Sauvages
    private static final long FLIGHT_MS = 280;         // trajet d'un lot, du coffre à sa case
    private static final long DISMISS_DELAY_MS = 720;  // avant « Cliquez pour continuer »
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
    /**
     * Le joueur a cliqué pour abréger l'animation.
     *
     * <p>Mémorisé plutôt qu'appliqué sur-le-champ : tant que le serveur n'a pas renvoyé le butin,
     * il n'y a rien à révéler. Le saut s'exécute donc dès son arrivée.</p>
     */
    private boolean skipRequested = false;
    /** Nombre de lots dont l'atterrissage a déjà été sonorisé (évite de rejouer à chaque frame). */
    private int landedSounds = 0;
    /** Zone cliquable du coffre, recalculée à chaque frame (le gabarit dépend du palier). */
    private int chestX, chestY, chestW = CHEST_W, chestH = CHEST_H;
    /** Zone du coffre de tribu, affiché à côté du précédent quand il y en a un à réclamer. */
    private int tribeChestX, tribeChestY;
    private boolean tribeChestShown = false;

    // ── Animations ───────────────────────────────────────────────────────────────
    /** Valeurs animées par nom : chaque valeur glisse doucement vers sa cible. */
    private final Map<String, Float> animated = new HashMap<>();
    private long lastFrameMs = System.currentTimeMillis();
    /**
     * Origine des temps de cet écran. Toutes les animations cycliques s'y rapportent.
     *
     * <p>Indispensable : {@code System.currentTimeMillis() / 1000f} vaut ~1,7 milliard, magnitude à
     * laquelle deux {@code float} consécutifs sont distants de <b>128 secondes</b>. Un temps absolu
     * stocké en {@code float} est donc gelé pendant des minutes entières, et tout ce qui en dépend
     * reste parfaitement immobile. En repartant de zéro à l'ouverture, la magnitude reste petite et
     * la précision totale.</p>
     */
    private final long screenEpochMs = System.currentTimeMillis();
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
    /**
     * Position de défilement telle qu'elle a été <b>dessinée</b> à la dernière frame.
     *
     * <p>Le défilement étant animé, la valeur logique et la valeur affichée diffèrent pendant la
     * transition. Les zones de clic se calent sur celle-ci : on clique toujours sur la ligne qu'on
     * voit, jamais sur celle où elle sera dans 150 ms.</p>
     */
    private float pickerScrollShown = 0f, candidateScrollShown = 0f;
    /** Éléments survolés cette frame (tooltips rendus en fin de frame). */
    private OWArenaFighter hoverFighter = null, hoverCandidate = null;
    /** Tribu survolée dans le sélecteur d'adversaire (tooltip de décision en fin de frame). */
    private OWClientTribeList.Entry hoverTribe = null;
    /** Créatures locales jetables servant d'aperçu 3D, indexées « type:skin ». */
    private final Map<String, LivingEntity> previewCache = new HashMap<>();
    /** Tribu en attente de confirmation de défi ({@code 0} = aucune). */
    private int pendingChallengeId = 0;
    private String pendingChallengeName = "";
    /**
     * Terrain choisi pour le défi en cours de rédaction. La terre ferme par défaut : c'est le duel
     * que toutes les créatures savent tenir, donc celui qui ne piège pas un chef distrait.
     */
    private OWArena.Terrain pendingTerrain = OWArena.Terrain.TERRESTRIAL;

    private Button chalAcceptBtn, chalDeclineBtn, cancelArenaBtn, readyBtn, challengeYesBtn;
    private Button terrainLandBtn, terrainWaterBtn;

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
        // L'onglet est déjà masqué sans badge, mais l'écran peut rester ouvert si la tribu vient
        // d'en perdre le bénéfice (transfert, refonte de la réputation) : on renvoie au tableau
        // de bord plutôt que d'afficher une arène à laquelle la tribu n'a plus droit.
        OWTeam here = OWClientTribeData.get();
        if (here == null || !OWArena.arenaUnlocked(here.getReputation())) {
            Minecraft.getInstance().setScreen(new OWTribeDashboardScreen());
            return;
        }
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

        // Le défi n'est plus envoyé d'ici : le terrain arrêté, on passe au choix du décor, qui se
        // charge de l'expédier. Un seul bouton, donc — c'est l'écran suivant qui porte l'abandon,
        // et Échap referme la boîte.
        challengeYesBtn = addRenderableWidget(Button.builder(
                        Component.translatable("owteams.arena.selection.start"), b -> {
                            if (pendingChallengeId == 0) return;
                            playUi(SoundEvents.UI_BUTTON_CLICK.value(), 1.1f);
                            Minecraft.getInstance().setScreen(new OWArenaVenueSelectScreen(
                                    pendingChallengeId, pendingChallengeName, pendingTerrain));
                        })
                .bounds(0, 0, 100, 16).build());

        // Choix du terrain, dans la boîte de défi : c'est le dernier moment où il se décide, et il
        // conditionne toute la composition qui suivra.
        terrainLandBtn = addRenderableWidget(Button.builder(
                        Component.translatable(OWArena.Terrain.TERRESTRIAL.translationKey()), b -> {
                            pendingTerrain = OWArena.Terrain.TERRESTRIAL;
                            playUi(SoundEvents.UI_BUTTON_CLICK.value(), 1.0f);
                        })
                .bounds(0, 0, 74, 16).build());
        terrainWaterBtn = addRenderableWidget(Button.builder(
                        Component.translatable(OWArena.Terrain.AQUATIC.translationKey()), b -> {
                            pendingTerrain = OWArena.Terrain.AQUATIC;
                            playUi(SoundEvents.UI_BUTTON_CLICK.value(), 1.15f);
                        })
                .bounds(0, 0, 74, 16).build());
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
        // Fermeture en plein milieu de l'animation : l'état ne doit pas rester coincé à « vrai »,
        // sinon le tchat resterait masqué indéfiniment.
        OWCinematicState.setChestOpening(false);
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
            if (button == 0) {
                boolean allShown = opening == Opening.REVEAL
                        && System.currentTimeMillis() - openingStart >= fullRevealMs();
                // Premier clic : on abrège l'animation. Second : on ferme.
                if (allShown) closeOpening();
                else if (reward != null) skipToReveal();
                else skipRequested = true;   // butin pas encore reçu : saut différé
            }
            return true;
        }
        // La confirmation de défi capte tout sauf ses propres boutons.
        if (pendingChallengeId != 0) return super.mouseClicked(mx, my, button);

        if (button == 0 && tribeTabClicked(mx, my, Tab.ARENA)) return true;

        if (button == 0 && accepted() && view == View.COMBAT && isChief()
                && combatClicked(mx, my)) return true;

        if (button == 0 && accepted()) {
            // Bouton de bascule Récompenses / Combat.
            int sx = switchX(), sy = switchY();
            if (switchButtonVisible() && mx >= sx && mx < sx + SWITCH_W && my >= sy && my < sy + SWITCH_H) {
                view = view == View.REWARDS ? View.COMBAT : View.REWARDS;
                restartContentFade();
                playTabSwitch();
                return true;
            }
            // Coffres cliquables (uniquement si le joueur en a un en attente).
            if (view == View.REWARDS && pendingChests() > 0
                    && mx >= chestX && mx < chestX + chestW && my >= chestY && my < chestY + chestH) {
                beginOpening(false);
                return true;
            }
            if (view == View.REWARDS && tribeChestShown
                    && mx >= tribeChestX && mx < tribeChestX + CHEST_W
                    && my >= tribeChestY && my < tribeChestY + CHEST_H) {
                beginOpening(true);
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
            int listY = y1 + 16, rowH = 20;
            int rows = (y2 - listY - 2) / rowH;
            boolean hasScroll = targets.size() > rows;
            int listX = x1 + 2, listW = x2 - x1 - 4 - (hasScroll ? 6 : 0);
            int firstRow = Math.max(0, (int) Math.floor(pickerScrollShown));
            int lastRow = Math.min(targets.size(), (int) Math.ceil(pickerScrollShown) + rows + 1);
            for (int i = firstRow; i < lastRow; i++) {
                int y = Math.round(listY + (i - pickerScrollShown) * rowH);
                if (my < listY || my >= listY + rows * rowH) break;
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
            int firstRow = Math.max(0, (int) Math.floor(candidateScrollShown));
            int lastRow = Math.min(candidates.size(), (int) Math.ceil(candidateScrollShown) + candidateRows + 1);
            for (int i = firstRow; i < lastRow; i++) {
                int ry = Math.round(slotsY + (i - candidateScrollShown) * rowH);
                if (my < slotsY || my >= slotsY + candidateRows * rowH) break;
                if (mx < listX || mx >= listX + contentW || my < ry || my >= ry + rowH) continue;
                OWArenaFighter f = candidates.get(i);
                boolean selected = OWClientArenaState.isSelected(f.entityUuid());
                // Créature hors de son élément : refus sec, quel que soit l'archétype.
                if (!OWClientArenaState.fitsTerrain(f)) {
                    playUi(SoundEvents.NOTE_BLOCK_BASS.value(), 0.5f);
                    return true;
                }
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

    /**
     * Le bouton de bascule s'efface dès que la vue Combat pose ses propres boutons en bas d'écran.
     *
     * <p>Il occupe le coin bas-droit, exactement là où tombent « Refuser », « Annuler » et
     * « Commencer » : il les chevauchait. Ces phases appellent une réponse — il n'y a rien à
     * consulter ailleurs tant qu'elle n'est pas donnée. Aucun risque d'impasse : la vue repart
     * toujours sur les récompenses à la réouverture de l'écran.</p>
     */
    private boolean switchButtonVisible() {
        if (view != View.COMBAT) return true;
        return switch (OWClientArenaState.phase()) {
            case CHALLENGE_SENT, CHALLENGE_RECEIVED, SELECTION -> false;
            case IDLE, FIGHTING, ENDED -> true;
        };
    }

    private int pendingChests() {
        OWTeam t = OWClientTribeData.get();
        UUID me = selfUuid();
        return t != null && me != null ? t.pendingArenaChests(me) : 0;
    }

    /**
     * Coffres de tribu en attente. Ils exigent un badge minimal : sous ce rang, ils restent
     * comptabilisés côté serveur mais ne sont ni affichés ni réclamables.
     */
    private int pendingTribeChests() {
        OWTeam t = OWClientTribeData.get();
        UUID me = selfUuid();
        if (t == null || me == null) return 0;
        if (!OWArena.tribeChestUnlocked(t.getReputation())) return 0;
        return t.pendingTribeChests(me);
    }

    // ── Animation d'ouverture ────────────────────────────────────────────────────
    private void beginOpening(boolean tribeChest) {
        opening = Opening.ANTICIPATION;
        skipRequested = false;
        landedSounds = 0;
        OWCinematicState.setChestOpening(true);
        openingStart = System.currentTimeMillis();
        reward = null;
        shakeTicks = 0;
        OWClientArenaReward.clear();
        OWNetworkHandler.sendToServer(new ClaimArenaChestPacket(tribeChest));
        playUi(SoundEvents.CHEST_OPEN, 0.7f);
    }

    private void closeOpening() {
        opening = Opening.NONE;
        skipRequested = false;
        OWCinematicState.setChestOpening(false);
        reward = null;
        burstParticles.clear();
    }

    /** Fanfare de trouvaille rare : trois notes montantes doublées d'un éclat cristallin. */
    private void playRareChime() {
        playUi(SoundEvents.AMETHYST_BLOCK_CHIME, 1.4f);
        playUi(SoundEvents.PLAYER_LEVELUP, 1.6f);
        playUi(SoundEvents.BEACON_POWER_SELECT, 1.8f);
    }

    /** Instant, depuis le début de la révélation, où absolument tout est affiché. */
    private long fullRevealMs() {
        int n = reward != null ? reward.items().size() : 0;
        return Math.max(0, n - 1) * REVEAL_STEP_MS + FLIGHT_MS + COINS_DELAY_MS + DISMISS_DELAY_MS;
    }

    /** Saute directement à la fin : tous les lots posés, les pièces annoncées, l'invite affichée. */
    private void skipToReveal() {
        opening = Opening.REVEAL;
        // On recule l'origine du chronomètre : la séquence se retrouve « déjà jouée », sans
        // avoir à dupliquer un état de fin quelque part.
        openingStart = System.currentTimeMillis() - fullRevealMs();
        skipRequested = false;
        if (reward != null) spawnBurst(reward.chest().accent());
        // Tout est révélé d'un coup : on neutralise les sons d'atterrissage individuels et on
        // résume par un seul son, plus intense s'il y avait une trouvaille rare.
        boolean anyRare = false;
        if (reward != null) {
            landedSounds = reward.items().size();
            for (int i = 0; i < reward.items().size(); i++) anyRare |= reward.isRare(i);
        }
        if (anyRare) playRareChime();
        else playUi(SoundEvents.PLAYER_LEVELUP, 1.1f);
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
        // Saut demandé : on attend d'avoir le butin, puis on va droit à la fin.
        if (skipRequested && reward != null) { skipToReveal(); return; }

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

        if (switchButtonVisible()) renderSwitchButton(g, mouseX, mouseY);
        super.render(g, mouseX, mouseY, partial);
        if (!confirming) renderTribeTabs(g, mouseX, mouseY, Tab.ARENA);

        if (confirming) { renderChallengeConfirm(g, mouseX, mouseY, partial); return; }

        // Tooltips en dernier, pour qu'ils passent au-dessus des widgets.
        if (hoverTribe != null) { renderTribeTooltip(g, hoverTribe, mouseX, mouseY); return; }

        OWArenaFighter tip = hoverFighter != null ? hoverFighter : hoverCandidate;
        if (tip != null) { renderFighterTooltip(g, tip, mouseX, mouseY); return; }

        int sx = switchX(), sy = switchY();
        if (switchButtonVisible() && mouseX >= sx && mouseX < sx + SWITCH_W
                && mouseY >= sy && mouseY < sy + SWITCH_H) {
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

        // Le défilement glisse vers sa cible plutôt que de sauter de 9 px par cran de molette.
        float scrollAnim = animate("introScroll", introScroll, 18f);

        g.enableScissor(x1 + 1, y1 + 1, x2 - 1, y2 - 1);
        int y = Math.round(y1 + 3 - scrollAnim);
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

        drawScrollFade(g, x1 + 1, y1 + 1, x2 - 1, y2 - 1, scrollAnim, maxScroll);
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
        int tribePending = pendingTribeChests();
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

        // Le coffre de badge occupe seul le haut : le coffre de tribu a sa propre section, tout en
        // bas, sous la barre de progression.
        //
        // Le dégagement sous le titre tient compte de l'ANIMATION, pas seulement du coffre : la pile
        // monte de 8 px et le soubresaut de 4 de plus. Sans cette marge, un coffre empilé venait
        // recouvrir le libellé du palier à chaque secousse.
        int bandTop = topPos + CONTENT_Y + 22;
        chestX = cx - CHEST_W / 2;
        chestY = bandTop;
        chestW = CHEST_W; chestH = CHEST_H;

        boolean hovered = pending > 0
                && mouseX >= chestX && mouseX < chestX + CHEST_W
                && mouseY >= chestY && mouseY < chestY + CHEST_H;

        float t0 = clock();
        drawIdleChest(g, cx, bandTop, chest, pending, hovered, t0, 0f);

        // Invitation à cliquer (clignotante), ou rappel qu'il faut encore progresser.
        int hintY = bandTop + CHEST_H + 4;
        if (pending > 0) {
            int a = (int) (170 + 85 * (0.5 + 0.5 * Math.sin(t0 * 4.0)));
            g.drawCenteredString(this.font, Component.translatable("owteams.arena.chest_ready"),
                    cx, hintY, (a << 24) | 0xFFD257);
        } else {
            g.drawCenteredString(this.font, Component.translatable("owteams.arena.chest_locked"),
                    cx, hintY, 0x7A7A7A);
        }

        // Barre de progression vers le prochain coffre (remplissage lissé).
        int barX = cx - BAR_W / 2, barY = hintY + 14;
        drawProgressBar(g, barX, barY, animate("prestige", OWArena.barProgress(prestige)), chest.accent());

        // Ratio écrit DANS la barre plutôt qu'en dessous : douze pixels de gagnés, dont la section
        // du coffre de tribu a besoin pour tenir au-dessus du bouton de bascule.
        String ratio = OWArena.prestigeInTier(prestige) + " / " + OWArena.PRESTIGE_PER_CHEST;
        g.drawString(this.font, ratio, cx - this.font.width(ratio) / 2, barY + 1, 0xFFFFFFFF, true);

        renderTribeChestSection(g, t, barY + BAR_H + 10, mouseX, mouseY, t0, tribePending);
    }

    /**
     * Bandeau du coffre de tribu, en pied d'onglet : le coffre à gauche, son avancement à droite.
     *
     * <p>Toujours affiché — même verrouillé — pour que la récompense soit connue avant d'être
     * atteignable. Trois états : rang insuffisant, progression en cours, coffre à réclamer.</p>
     */
    private void renderTribeChestSection(GuiGraphics g, OWTeam t, int top,
                                         int mouseX, int mouseY, float t0, int tribePending) {
        boolean unlocked = OWArena.tribeChestUnlocked(t.getReputation());
        boolean ready = tribePending > 0;

        // Filet de séparation : la section n'appartient pas à la progression du dessus.
        g.fill(leftPos + 14, top - 5, leftPos + IMG_W - 14, top - 4, 0x33000000);

        int chestLeft = leftPos + 10;
        tribeChestX = chestLeft;
        tribeChestY = top;
        tribeChestShown = ready;

        boolean hovered = ready
                && mouseX >= tribeChestX && mouseX < tribeChestX + CHEST_W
                && mouseY >= tribeChestY && mouseY < tribeChestY + CHEST_H;

        if (ready) {
            // Déphasé : les deux coffres ne doivent pas s'agiter à l'unisson, ce qui trahirait une
            // animation commune plutôt que deux objets distincts.
            drawIdleChest(g, chestLeft + CHEST_W / 2, top, OWArena.Chest.TRIBE,
                    tribePending, hovered, t0, 1.7f);
        } else {
            // Verrouillé : coffre éteint, sans halo ni étincelles.
            drawChest(g, chestLeft, top, OWArena.Chest.TRIBE, false, false, 0f, 1f);
        }

        int textX = chestLeft + CHEST_W + 8;
        int textW = leftPos + IMG_W - 10 - textX;
        int accent = OWArena.Chest.TRIBE.accent();

        // Les teintes du coffre servent sur fond sombre (cinématiques) ; sur le panneau clair, il
        // faut les assombrir, sinon l'ivoire disparaît purement et simplement.
        int nameCol = ready ? onPanel(accent) : 0x8A8A84;
        Component name = Component.translatable("owteams.arena.chest.tribe");
        g.drawString(this.font, name.copy().withStyle(Style.EMPTY.withBold(true)
                        .withColor(TextColor.fromRgb(nameCol))),
                textX, top + 1, nameCol, false);

        if (!unlocked) {
            // Rang insuffisant : on nomme la condition plutôt que d'afficher une barre trompeuse.
            drawSmall(g, Component.translatable("owteams.arena.tribe_chest.locked",
                            Component.translatable(OWArena.TRIBE_CHEST_MIN_BADGE.translationKey())),
                    textX, top + 13, 0x8A5A4A);
            return;
        }

        if (ready) {
            // Coffre dû : la barre d'avancement n'apprendrait plus rien, on la remplace par
            // l'invite. La section garde ainsi la hauteur du coffre, quel que soit son état.
            int a = (int) (170 + 85 * (0.5 + 0.5 * Math.sin(t0 * 4.0)));
            drawSmall(g, Component.translatable("owteams.arena.chest_ready"),
                    textX, top + 14, (a << 24) | 0xA8791A);
            return;
        }

        // Avancement vers le prochain coffre de tribu : coffres de badge ouverts, modulo dix.
        int opened = tribeProgress();
        int goal = OWArena.CHESTS_PER_TRIBE_CHEST;
        drawSmall(g, Component.translatable("owteams.arena.tribe_chest.progress", opened, goal),
                textX, top + 13, 0x82807A);

        int miniY = top + 23, miniH = 5;
        g.fill(textX - 1, miniY - 1, textX + textW + 1, miniY + miniH + 1, 0xFF000000);
        g.fill(textX, miniY, textX + textW, miniY + miniH, 0xFF16161A);
        int fill = Math.round(textW * animate("tribeBar", opened / (float) goal, 8f));
        if (fill > 0) {
            g.fill(textX, miniY, textX + fill, miniY + miniH, 0xFF000000 | accent);
            g.fill(textX, miniY, textX + fill, miniY + 2, 0x44FFFFFF);
        }
    }

    /**
     * Assombrit une teinte jusqu'à ce qu'elle se détache du panneau, en préservant sa couleur.
     *
     * <p>Le pendant de {@code readable} des cinématiques, qui éclaircit pour un fond noir : ici le
     * fond est le panneau gris clair. L'accent du coffre de tribu, un ivoire de luminance 0,82,
     * y était tout bonnement invisible — d'autant que le petit texte est tracé <b>sans ombre
     * portée</b>, contrairement aux titres centrés.</p>
     */
    private static int onPanel(int rgb) {
        int r = (rgb >> 16) & 0xFF, g = (rgb >> 8) & 0xFF, b = rgb & 0xFF;
        float luma = (0.299f * r + 0.587f * g + 0.114f * b) / 255f;
        final float ceiling = 0.42f;
        if (luma <= ceiling) return rgb & 0xFFFFFF;
        float k = ceiling / Math.max(0.04f, luma);
        return (Math.round(r * k) << 16) | (Math.round(g * k) << 8) | Math.round(b * k);
    }

    /** Coffres de badge ouverts depuis le dernier coffre de tribu (0 → 9). */
    private int tribeProgress() {
        OWTeam t = OWClientTribeData.get();
        UUID me = selfUuid();
        if (t == null || me == null) return 0;
        return t.getArenaChestsClaimed(me) % OWArena.CHESTS_PER_TRIBE_CHEST;
    }

    /**
     * Étincelles gravitant autour d'un coffre en attente. Déterministes (fonction du temps seul),
     * donc sans état à conserver ni allocation par frame.
     */
    private void drawIdleSparkles(GuiGraphics g, int cx, int cy, int accent, float t) {
        final int count = 7;
        int base = accent & 0xFFFFFF;
        for (int i = 0; i < count; i++) {
            float speed = 0.55f + (i % 3) * 0.22f;
            float angle = t * speed + i * (float) (Math.PI * 2 / count);
            float radiusX = CHEST_W * (0.58f + 0.10f * ((i * 5 % 7) / 7f));
            float radiusY = CHEST_H * (0.62f + 0.12f * ((i * 3 % 5) / 5f));
            float x = cx + (float) Math.cos(angle) * radiusX;
            float y = cy + (float) Math.sin(angle * 1.3f + i) * radiusY;

            float twinkle = 0.5f + 0.5f * (float) Math.sin(t * 3.4f + i * 2.1f);
            int a = (int) (200 * twinkle);
            if (a <= 8) continue;

            // Taille par mise à l'échelle continue et position en sous-pixel : basculer d'un carré
            // de 1 px à 2 px se voyait comme un clignotement, et arrondir la position faisait
            // sauter l'étincelle au lieu de la faire glisser.
            float size = 0.7f + 1.5f * twinkle;
            g.pose().pushPose();
            g.pose().translate(x, y, 0);
            g.pose().scale(size, size, 1f);
            g.fill(-1, -1, 1, 1, (a << 24) | base);
            g.fill(0, -1, 1, 0, (Math.min(255, a + 60) << 24) | 0xFFFFFF);
            g.pose().popPose();
        }
    }

    /** Petit texte aligné à gauche (échelle 0.75). */
    /**
     * Écrit un petit texte calé à droite entre {@code left} et {@code right}, rétréci s'il ne tient
     * pas. Le plancher à 55 % garde le texte lisible ; en deçà, c'est la mise en page qu'il faut
     * revoir plutôt que de continuer à réduire.
     */
    private void drawSmallRightAligned(GuiGraphics g, Component text, int left, int right, int y, int color) {
        int raw = this.font.width(text);
        int avail = Math.max(1, right - left);
        float sc = Math.min(0.75f, avail / (float) raw);
        if (sc < 0.55f) sc = 0.55f;
        g.pose().pushPose();
        g.pose().translate(right - raw * sc, y, 0);
        g.pose().scale(sc, sc, 1f);
        g.drawString(this.font, text, 0, 0, color, false);
        g.pose().popPose();
    }

    private void drawSmall(GuiGraphics g, Component text, int x, int y, int color) {
        final float sc = 0.75f;
        g.pose().pushPose();
        g.pose().translate(x, y, 0);
        g.pose().scale(sc, sc, 1f);
        g.drawString(this.font, text, 0, 0, color, false);
        g.pose().popPose();
    }

    /**
     * Dessine un coffre en attente d'ouverture, avec toute sa vie : halo, étincelles, flottement,
     * soubresaut périodique, pile et pastille de comptage.
     *
     * <p>{@code phaseOffset} décale l'horloge de ce coffre : deux coffres affichés en même temps ne
     * doivent pas s'agiter à l'unisson, ce qui trahirait une animation commune.</p>
     */
    private void drawIdleChest(GuiGraphics g, int centerX, int top, OWArena.Chest chest,
                               int pending, boolean hovered, float clock, float phaseOffset) {
        int x = centerX - CHEST_W / 2;
        float t0 = clock + phaseOffset;

        // Décalages gardés en FLOTTANT et appliqués par la pile de matrices : arrondir au pixel
        // entier faisait avancer le coffre par sauts de 4 px à l'écran (échelle d'interface).
        float bob = 0f, jolt = 0f, tilt = 0f;
        if (pending > 0) {
            bob = (float) Math.sin(t0 * 2.6) * 1.9f;

            // Soubresaut périodique, enveloppe en cloche : l'agitation naît et s'éteint en douceur.
            final float RATTLE_PERIOD = 3.6f, RATTLE_LEN = 0.75f;
            float phase = t0 % RATTLE_PERIOD;
            if (phase < RATTLE_LEN) {
                float u = phase / RATTLE_LEN;
                float envelope = (float) Math.sin(u * Math.PI);
                envelope *= envelope;
                jolt = (float) Math.sin(u * Math.PI * 6.0) * 2.6f * envelope;
                tilt = (float) Math.sin(u * Math.PI * 6.0) * 3.4f * envelope;
                bob -= 2.2f * envelope;
            }

            // Le halo respire par son INTENSITÉ : l'alpha a 256 niveaux, le rayon des pixels entiers.
            float pulse = 0.5f + 0.5f * (float) Math.sin(t0 * 2.0);
            int radius = Math.round(CHEST_W * 0.64f * (hovered ? 1.16f : 1f));
            OWCinematicFx.drawGlow(g, centerX, top + CHEST_H / 2, radius,
                    chest.accent(), (hovered ? 1.5f : 1.0f) * (0.7f + 0.45f * pulse));

            drawIdleSparkles(g, centerX, top + CHEST_H / 2, chest.accent(), t0);
        }

        float hoverScale = animate("hover" + chest.material().name(), hovered ? 1.10f : 1f, 14f);
        float pivotX = centerX, pivotY = top + CHEST_H / 2f;

        g.pose().pushPose();
        g.pose().translate(pivotX + jolt, pivotY + bob, 0);
        g.pose().scale(hoverScale, hoverScale, 1f);
        if (tilt != 0f) g.pose().mulPose(com.mojang.math.Axis.ZP.rotationDegrees(tilt));
        g.pose().translate(-pivotX, -pivotY, 0);

        // Pile : les coffres en attente s'empilent, le plus profond étant le plus sombre.
        int depth = Math.min(pending, 4);
        for (int i = depth - 1; i >= 1; i--) {
            float off = i * 2.6f;
            drawChest(g, Math.round(x + off * 0.55f), Math.round(top - off),
                    chest, true, false, 0f, 1f - i * 0.19f);
        }
        drawChest(g, x, top, chest, pending > 0, hovered, 0f, 1f);

        if (pending > 1) {
            String n = "×" + pending;
            int bw = this.font.width(n) + 6;
            int bx = x + CHEST_W + 1, by = top - 3;
            g.fill(bx - bw / 2 - 1, by - 1, bx + bw / 2 + 1, by + 12, 0xFF000000);
            g.fill(bx - bw / 2, by, bx + bw / 2, by + 11, 0xFF1E1E22);
            g.fill(bx - bw / 2, by, bx + bw / 2, by + 1, 0xFF000000 | chest.accent());
            g.drawString(this.font, n, bx - this.font.width(n) / 2, by + 2,
                    0xFF000000 | chest.accent(), false);
        }
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

        int listY = f[1] + 16, rowH = 20;
        int rows = (f[3] - listY - 2) / rowH;
        boolean hasScroll = targets.size() > rows;
        int listX = f[0] + 2, listW = f[2] - f[0] - 4 - (hasScroll ? 6 : 0);
        combatScrollMax = Math.max(0, targets.size() - rows);
        combatScroll = Math.min(combatScroll, combatScrollMax);

        // Défilement continu : la liste glisse vers sa nouvelle position au lieu de sauter d'une
        // ligne entière. On déborde d'une rangée de chaque côté pour qu'aucun trou n'apparaisse
        // pendant la transition.
        float scrollAnim = animate("pickerScroll", combatScroll, 17f);
        pickerScrollShown = scrollAnim;
        int listBottom = listY + rows * rowH;
        int first = Math.max(0, (int) Math.floor(scrollAnim) - 1);
        int last = Math.min(targets.size(), (int) Math.ceil(scrollAnim) + rows + 1);

        g.enableScissor(listX, listY, listX + listW, listBottom);
        for (int i = first; i < last; i++) {
            OWClientTribeList.Entry e = targets.get(i);
            int y = Math.round(listY + (i - scrollAnim) * rowH);
            boolean hov = mouseY >= y && mouseY < y + rowH
                    && mouseX >= listX && mouseX < listX + listW
                    && mouseY >= listY && mouseY < listBottom;

            if ((i & 1) == 0) g.fill(listX, y, listX + listW, y + rowH - 1, 0x18FFFFFF);
            float hoverAmt = animate("tribe" + i, hov ? 1f : 0f, 14f);
            if (hoverAmt > 0.01f) {
                g.fill(listX, y, listX + listW, y + rowH - 1, ((int) (60 * hoverAmt) << 24) | 0xFFFFFF);
                g.fill(listX, y, listX + 1, y + rowH - 1, ((int) (255 * hoverAmt) << 24) | 0xE8956A);
            }
            if (hov) hoverTribe = e;

            // Bannière miniature : on reconnaît une tribu à ses couleurs bien avant de lire son nom.
            float bs = (rowH - 5) / (float) OWBannerRenderer.H;
            float bw = OWBannerRenderer.W * bs;
            g.pose().pushPose();
            g.pose().translate(listX + 3 + hoverAmt * 1.5f, y + 2, 0);
            g.pose().scale(bs, bs, 1f);
            OWBannerRenderer.render(g, 0, 0, e.bannerShape(),
                    e.primaryColor(), e.secondaryColor(), e.tertiaryColor(), e.useTertiary(),
                    e.pattern(), e.paintPixels());
            g.pose().popPose();
            RenderSystem.setShaderColor(1f, 1f, 1f, 1f);

            int textX = listX + 6 + Math.round(bw) + Math.round(hoverAmt * 2f);
            String rep = OWTribeReputationScreen.formatNumber(e.reputation());
            int repW = this.font.width(rep);
            g.drawString(this.font, trimTo(e.name(), listX + listW - textX - repW - 8),
                    textX, y + 3, 0xE8E8E8, false);
            drawSmall(g, Component.translatable("owteams.reputation.score", rep),
                    textX, y + 13, hov ? 0xC8C8C8 : 0x8A8A8A);
        }
        g.disableScissor();

        drawScrollFade(g, listX, listY, listX + listW, listBottom, scrollAnim, combatScrollMax);
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
        // Tronqué à la largeur du cadre : un nom de tribu long sortait du panneau des deux côtés.
        g.drawCenteredString(this.font,
                trimTo(OWClientArenaState.get().outgoingTargetName(), f[2] - f[0] - 12),
                cx, cy - 2, 0xE8E8E8);
        OWArena.Terrain terrain = OWClientArenaState.terrain();
        drawSmallCentered(g, Component.translatable("owteams.arena.terrain.imposed",
                        Component.translatable(terrain.translationKey())),
                cx, cy + 10, terrainColor(terrain));
        // Trois points animés, pour signaler que l'attente est vivante.
        int dots = (int) ((System.currentTimeMillis() / 400) % 4);
        g.drawCenteredString(this.font, ".".repeat(dots), cx, cy + 24, 0x9A9A9A);
    }

    private void renderIncomingChallenge(GuiGraphics g) {
        int[] f = combatFrame(g);
        var st = OWClientArenaState.get();
        int cx = leftPos + IMG_W / 2, cy = f[1] + 12;

        g.drawCenteredString(this.font, Component.translatable("owteams.arena.challenge.incoming")
                .withStyle(Style.EMPTY.withBold(true).withColor(TextColor.fromRgb(0xE8956A))), cx, cy, 0xE8956A);
        g.drawCenteredString(this.font, trimTo(st.incomingChallengerName(), f[2] - f[0] - 12),
                cx, cy + 14, 0xFFFFFF);

        // Comparatif de réputation + gain potentiel : de quoi décider en connaissance de cause.
        drawSmallCentered(g, Component.translatable("owteams.arena.challenge.their_rep",
                OWTribeReputationScreen.formatNumber(st.opponentReputation())), cx, cy + 30, 0xBBBBBB);
        drawSmallCentered(g, Component.translatable("owteams.arena.challenge.your_rep",
                OWTribeReputationScreen.formatNumber(st.myReputation())), cx, cy + 40, 0xBBBBBB);
        // Les deux gains sont calculables d'avance : on les annonce tous les deux, pour qu'un chef
        // accepte ou décline en connaissance de cause.
        int gain = OWArena.reputationGain(st.myReputation(), st.opponentReputation());
        g.drawCenteredString(this.font, Component.translatable("owteams.arena.challenge.potential_gain",
                        OWTribeReputationScreen.formatNumber(gain))
                .withStyle(Style.EMPTY.withColor(TextColor.fromRgb(0x7ddd73))), cx, cy + 52, 0x7ddd73);
        drawSmallCentered(g, Component.translatable("owteams.arena.challenge.potential_prestige",
                        OWArena.prestigeGain(st.myReputation(), st.opponentReputation())),
                cx, cy + 64, 0x9AC88A);

        // Terrain imposé par le défiant : le défié doit le connaître avant d'accepter, c'est lui qui
        // décide des bêtes qu'il pourra aligner.
        OWArena.Terrain terrain = OWClientArenaState.terrain();
        drawSmallCentered(g, Component.translatable("owteams.arena.terrain.imposed",
                        Component.translatable(terrain.translationKey())),
                cx, cy + 78, terrainColor(terrain));

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

        // En-têtes de colonnes, calés sur les BORDS du panneau et non centrés sur leur colonne :
        // celle de droite est à 16 px du bord, un nom de tribu un peu long débordait donc de
        // l'écran. Chacun est en outre tronqué à la moitié de la largeur disponible, pour que les
        // deux ne puissent jamais se rejoindre au milieu.
        final int headerMax = (IMG_W - 20) / 2;
        drawSmall(g, Component.translatable("owteams.arena.selection.you"), leftPos + 8, y0, 0x7FD8A8);
        String oppName = trimTo(st.opponentName(), headerMax);
        drawSmall(g, Component.literal(oppName),
                leftPos + IMG_W - 8 - Math.round(this.font.width(oppName) * 0.75f), y0, 0xE8956A);
        // Rappel permanent du terrain : c'est lui qui explique les lignes grisées de la liste.
        OWArena.Terrain terrain = OWClientArenaState.terrain();
        drawSmallCentered(g, Component.translatable(terrain.translationKey()),
                leftPos + IMG_W / 2, y0, terrainColor(terrain));

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
        // Les deux libellés partagent une même ligne : l'état de l'adversaire ne prend que la place
        // que le compteur d'équipe lui laisse, et rétrécit plutôt que de lui passer dessus. Sans
        // cela, une traduction longue déborde du panneau (constaté en français en multijoueur).
        int rowRight = leftPos + IMG_W - 7;
        int rowLeft = leftPos + 7 + (int) Math.ceil(this.font.width(team) * 0.75f) + 6;
        drawSmallRightAligned(g, oppStatus.copy().withStyle(
                Style.EMPTY.withColor(TextColor.fromRgb(oppColor))), rowLeft, rowRight, infoY, oppColor);
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

        float scrollAnim = animate("candidateScroll", combatScroll, 17f);
        candidateScrollShown = scrollAnim;
        int first = Math.max(0, (int) Math.floor(scrollAnim) - 1);
        int last = Math.min(candidates.size(), (int) Math.ceil(scrollAnim) + rows + 1);

        g.enableScissor(x, y, x + contentW, y + h);
        for (int i = first; i < last; i++) {
            OWArenaFighter f = candidates.get(i);
            int ry = Math.round(y + (i - scrollAnim) * rowH);
            boolean selected = OWClientArenaState.isSelected(f.entityUuid());
            // Le terrain prime : une créature étrangère à l'élément du duel reste visible mais hors
            // d'atteinte — la masquer laisserait un chef chercher en vain son orque dans la liste.
            boolean unfit = !OWClientArenaState.fitsTerrain(f);
            boolean blocked = unfit
                    || (!selected && OWClientArenaState.archetypeTaken(f.archetypeOrdinal(), f.entityUuid()));
            boolean hov = mouseX >= x && mouseX < x + contentW
                    && mouseY >= ry && mouseY < ry + rowH
                    && mouseY >= y && mouseY < y + h;

            float hoverAmt = animate("cand" + i, hov && !blocked ? 1f : 0f, 15f);
            if (selected) g.fill(x, ry, x + contentW, ry + rowH - 1, 0x334C9A5A);
            else if ((i & 1) == 0) g.fill(x, ry, x + contentW, ry + rowH - 1, 0x14FFFFFF);
            if (hoverAmt > 0.01f) g.fill(x, ry, x + contentW, ry + rowH - 1,
                    ((int) (55 * hoverAmt) << 24) | 0xFFFFFF);

            // Pastille aux couleurs de l'espèce : elle s'étire au survol, ce qui désigne la ligne
            // visée sans dépendre d'un simple éclaircissement du fond. L'archétype, lui, se lit sur
            // les emplacements de gauche, où sa contrainte d'unicité a un sens.
            int chip = entityColorOf(f);
            int chipW = 3 + Math.round(hoverAmt * 2f);
            g.fill(x + 2, ry + 3, x + 2 + chipW, ry + rowH - 4, 0xFF000000 | (blocked ? dim(chip) : chip));

            int nameCol = blocked ? 0x666666 : (selected ? 0xB8F0C8 : 0xE8E8E8);
            String lvl = "L" + f.level();
            int nameX = x + 8 + Math.round(hoverAmt * 2f);
            g.drawString(this.font, trimTo(f.name(), contentW - 14 - this.font.width(lvl)),
                    nameX, ry + 3, nameCol, false);
            g.drawString(this.font, lvl, x + contentW - this.font.width(lvl) - 3, ry + 3,
                    blocked ? 0x555555 : 0x9A9A9A, false);

            // Une croix discrète dit pourquoi la ligne est refusée : l'archétype est déjà pris.
            if (blocked && hov) {
                g.drawString(this.font, "✕", x + contentW - this.font.width(lvl) - 13, ry + 3, 0xC05555, false);
            }
            if (hov) hoverCandidate = f;
        }
        g.disableScissor();
        drawScrollFade(g, x, y, x + contentW, y + h, scrollAnim, combatScrollMax);
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
            float phase = clock() * 0.8f + my * 1.3f;
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

    /**
     * Couleur propre à l'espèce, celle dont {@code OWRendererUtils} teinte déjà son étiquette dans
     * le monde. Lue sur l'aperçu déjà construit — aucun champ à faire voyager sur le réseau.
     */
    private int entityColorOf(OWArenaFighter f) {
        return previewFor(f) instanceof OWEntity owE ? owE.getEntityColor() : 0xAAAAAA;
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
                                 int alive, int total, int color, String key) {
        g.drawString(this.font, label, x, y, 0xE8E8E8, false);
        String count = alive + " / " + total;
        g.drawString(this.font, count, x + w - this.font.width(count), y, 0xBBBBBB, false);

        int by = y + 11, bh = 6;
        g.fill(x - 1, by - 1, x + w + 1, by + bh + 1, 0xFF000000);
        g.fill(x, by, x + w, by + bh, 0xFF16161A);

        // La jauge glisse vers sa nouvelle valeur : une chute instantanée passerait inaperçue,
        // alors qu'un combattant qui tombe est l'information la plus importante de l'écran.
        float target = total <= 0 ? 0f : alive / (float) total;
        float shown = animate(key, target, 6f);
        int fill = Math.round(w * shown);
        if (fill > 0) {
            g.fill(x, by, x + fill, by + bh, 0xFF000000 | color);
            g.fill(x, by, x + fill, by + 2, 0x33FFFFFF);
        }
        // Sillage clair sur la portion qui vient d'être perdue.
        int targetFill = Math.round(w * target);
        if (fill > targetFill + 1) {
            g.fill(x + targetFill, by, x + fill, by + bh, 0x77FFFFFF);
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
            drawSmallCentered(g, Component.translatable("owteams.arena.result.versus",
                            trimTo(st.opponentName(), 110)), cx, cy + 6, 0x9A9A9A);
        }
        int prestige = OWArena.prestigeGain(st.myReputation(), st.opponentReputation());
        if (r == OWArena.Result.WIN) {
            int gain = OWArena.reputationGain(st.myReputation(), st.opponentReputation());
            g.drawCenteredString(this.font, Component.translatable("owteams.arena.result.gains",
                            OWTribeReputationScreen.formatNumber(gain), prestige)
                    .withStyle(Style.EMPTY.withColor(TextColor.fromRgb(0x7ddd73))), cx, cy + 18, 0x7ddd73);
        } else if (r == OWArena.Result.LOSS) {
            // Le vaincu touche une part de ce qu'aurait valu SA victoire : le calcul est donc mené
            // depuis son propre point de vue, celui déjà porté par l'état d'arène.
            g.drawCenteredString(this.font, Component.translatable("owteams.arena.result.consolation",
                            OWArena.prestigeConsolation(prestige))
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
        terrainLandBtn.visible = confirming;
        terrainWaterBtn.visible = confirming;
    }

    /**
     * Échap referme la boîte de défi au lieu de quitter l'écran : c'est le seul moyen d'en sortir
     * depuis qu'elle ne porte plus qu'un bouton.
     */
    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (pendingChallengeId != 0 && keyCode == org.lwjgl.glfw.GLFW.GLFW_KEY_ESCAPE) {
            pendingChallengeId = 0;
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    /**
     * Boîte de confirmation avant d'envoyer un défi (action irréversible côté adversaire), où se
     * choisit aussi le <b>terrain</b> du duel.
     *
     * <p>Le terrain se décide ici et nulle part ailleurs : une fois le défi parti, il est annoncé à
     * l'adversaire et verrouille les deux compositions. Le bouton retenu s'affiche en clair, l'autre
     * en gris — le chef voit d'un coup d'œil ce qu'il s'apprête à imposer.</p>
     */
    private void renderChallengeConfirm(GuiGraphics g, int mouseX, int mouseY, float partial) {
        g.fill(0, 0, this.width, this.height, 0x99000000);
        Component title = Component.translatable("owteams.arena.challenge.confirm_title");
        Component body = Component.translatable("owteams.arena.challenge.confirm_body", pendingChallengeName);
        int ow = Math.min(this.width - 20, Math.max(190, Math.max(this.font.width(title), this.font.width(body)) + 24));
        int oh = 96, cx = this.width / 2, ox = cx - ow / 2, oy = this.height / 2 - oh / 2;
        OWTribeDashboardScreen.drawConfirmBox(g, ox, oy, ow, oh, title, body, 0xE8956A);

        drawSmallCentered(g, Component.translatable("owteams.arena.terrain.choose"), cx, oy + 40, 0x9A9A9A);
        styleTerrainButton(terrainLandBtn, OWArena.Terrain.TERRESTRIAL);
        styleTerrainButton(terrainWaterBtn, OWArena.Terrain.AQUATIC);
        terrainLandBtn.setPosition(cx - 78, oy + 50);
        terrainWaterBtn.setPosition(cx + 4, oy + 50);
        terrainLandBtn.render(g, mouseX, mouseY, partial);
        terrainWaterBtn.render(g, mouseX, mouseY, partial);

        challengeYesBtn.setPosition(cx - challengeYesBtn.getWidth() / 2, oy + oh - 20);
        challengeYesBtn.render(g, mouseX, mouseY, partial);
    }

    /** Marque le bouton du terrain retenu : couleur pleine pour le choix actif, gris pour l'autre. */
    private void styleTerrainButton(Button button, OWArena.Terrain terrain) {
        boolean active = pendingTerrain == terrain;
        int color = active ? terrainColor(terrain) : 0x777777;
        button.setMessage(Component.translatable(terrain.translationKey())
                .withStyle(Style.EMPTY.withBold(active).withColor(TextColor.fromRgb(color))));
    }

    /** Teinte propre au terrain : ocre pour la terre, azur pour l'eau. */
    private static int terrainColor(OWArena.Terrain terrain) {
        return terrain == OWArena.Terrain.AQUATIC ? 0x5AB4E0 : 0xC9A15A;
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
        // Dire pourquoi la ligne est grisée vaut mieux que de laisser deviner.
        if (!OWClientArenaState.fitsTerrain(f)) {
            tip.add(Component.translatable(OWClientArenaState.terrain().unfitKey(), f.name())
                    .withStyle(Style.EMPTY.withColor(TextColor.fromRgb(0xC05555))).getVisualOrderText());
        }
        g.renderTooltip(this.font, tip, mouseX, mouseY);
    }

    /**
     * Estompe le haut et le bas d'une zone défilante, proportionnellement à ce qui dépasse.
     *
     * <p>Une liste tronquée net ne dit pas qu'elle continue. Ce dégradé le signale en permanence,
     * et disparaît de lui-même quand on atteint une extrémité.</p>
     */
    private void drawScrollFade(GuiGraphics g, int x1, int y1, int x2, int y2,
                                float scroll, float maxScroll) {
        final int depth = 10;
        float top = Math.min(1f, scroll / 12f);
        float bottom = Math.min(1f, (maxScroll - scroll) / 12f);
        for (int i = 0; i < depth; i++) {
            float k = 1f - i / (float) depth;
            int aTop = (int) (150 * k * top), aBot = (int) (150 * k * bottom);
            if (aTop > 0) g.fill(x1, y1 + i, x2, y1 + i + 1, aTop << 24);
            if (aBot > 0) g.fill(x1, y2 - i - 1, x2, y2 - i, aBot << 24);
        }
    }

    /** Balayage lumineux périodique sur une barre de progression remplie. */
    private void drawBarShimmer(GuiGraphics g, int x, int y, int w, int h, int fillW, float t) {
        if (fillW <= 2) return;
        float cycle = (t * 0.42f) % 1f;
        int sweep = Math.round(cycle * (fillW + 24)) - 12;
        for (int i = -6; i <= 6; i++) {
            int sx = x + sweep + i;
            if (sx < x || sx >= x + fillW) continue;
            int a = (int) (95 * (1f - Math.abs(i) / 6f));
            if (a <= 0) continue;
            g.fill(sx, y, sx + 1, y + h, (a << 24) | 0xFFFFFF);
        }
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

    /** Secondes écoulées depuis l'ouverture de l'écran, en pleine précision (cf. {@link #screenEpochMs}). */
    private float clock() {
        return (System.currentTimeMillis() - screenEpochMs) / 1000f;
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
    /**
     * Bouton de bascule Récompenses ⇄ Combat : un bouton vanilla ordinaire, portant l'icône de la
     * vue <b>en cours</b>.
     */
    private void renderSwitchButton(GuiGraphics g, int mouseX, int mouseY) {
        int x = switchX(), y = switchY();
        boolean hov = mouseX >= x && mouseX < x + SWITCH_W && mouseY >= y && mouseY < y + SWITCH_H;

        g.blitSprite(net.minecraft.resources.ResourceLocation.withDefaultNamespace(
                        hov ? "widget/button_highlighted" : "widget/button"),
                x, y, SWITCH_W, SWITCH_H);

        int v = view == View.REWARDS ? 72 : 88;
        g.blit(OW_SPRITES, x + (SWITCH_W - 16) / 2, y + (SWITCH_H - 16) / 2, 0, v, 16, 16);
    }

    // ── Coffre dessiné à la main ─────────────────────────────────────────────────
    /**
     * Coffre en pixel-art procédural (l'image définitive n'existe pas encore) : couvercle, corps,
     * ferrures et serrure teintés par la couleur du palier. {@code lift} soulève le couvercle
     * (0 = fermé, 1 = grand ouvert) pendant l'animation d'ouverture.
     */
    /**
     * Coordonnée {@code v} du sprite d'une matière dans {@code ow_teams_sprites.png}, ou {@code -1}
     * si son visuel n'a pas encore été dessiné (on retombe alors sur le tracé procédural).
     */
    /**
     * Emplacement des sprites d'une matière dans {@code ow_teams_sprites.png}.
     *
     * <p>{@code u0} est la colonne du plus petit gabarit ; les trois se suivent de {@link #CHEST_W}
     * en {@code u}. {@code seam} est la ligne de séparation couvercle / caisse, comptée depuis le
     * haut du sprite — elle <b>diffère d'une matière à l'autre</b> selon le dessin, d'où son
     * stockage ici plutôt qu'en constante partagée.</p>
     */
    private record ChestSprite(int u0, int v, int seam) {}

    /**
     * Emplacement du sprite d'un coffre. Les coffres de badge se déclinent en trois gabarits alignés
     * horizontalement depuis {@code u0} ; le coffre de tribu est unique et occupe une seule case.
     *
     * <p>Le {@code switch} est exhaustif sur les matières : en ajouter une sans lui donner de
     * coordonnées ne compilera pas — un repli silencieux serait plus difficile à repérer.</p>
     */
    private static ChestSprite chestSprite(OWArena.Chest chest) {
        int step = chest.size().ordinal() * CHEST_W;
        return switch (chest.material()) {
            case RUBY  -> new ChestSprite(0 + step, 198, 212 - 198);
            case GOLD  -> new ChestSprite(0 + step, 227, 238 - 227);
            case JADE  -> new ChestSprite(111 + step, 227, 245 - 227);
            case TRIBE -> new ChestSprite(111, 198, 207 - 198);   // gabarit unique
        };
    }

    /** Ligne de séparation couvercle / caisse du coffre. */
    private static int chestSeam(OWArena.Chest chest) {
        return chestSprite(chest).seam();
    }

    /**
     * Dessine un coffre. Utilise le sprite de sa matière s'il existe, sinon le tracé procédural
     * (Jade et Rubis, en attendant leurs visuels).
     *
     * <p>{@code lift} soulève le couvercle : le sprite est alors blitté en <b>deux morceaux</b>
     * découpés sur la ligne de jointure, le haut s'écartant du bas. C'est ce qui rend l'ouverture
     * crédible sans seconde image.</p>
     */
    private void drawChest(GuiGraphics g, int x, int y, OWArena.Chest chest,
                           boolean active, boolean hovered, float lift) {
        drawChest(g, x, y, chest, active, hovered, lift, 1f);
    }

    /** {@code tint} < 1 assombrit le coffre : sert aux exemplaires du fond dans une pile. */
    private void drawChest(GuiGraphics g, int x, int y, OWArena.Chest chest,
                           boolean active, boolean hovered, float lift, float tint) {
        ChestSprite sprite = chestSprite(chest);
        int u = sprite.u0(), v = sprite.v(), seam = sprite.seam();

        // Coffre indisponible : assombri, pour qu'on voie qu'il n'y a rien à réclamer.
        if (!active) RenderSystem.setShaderColor(0.42f * tint, 0.42f * tint, 0.46f * tint, 1f);
        else if (hovered) RenderSystem.setShaderColor(1.18f * tint, 1.18f * tint, 1.18f * tint, 1f);
        else if (tint != 1f) RenderSystem.setShaderColor(tint, tint, tint, 1f);

        if (lift <= 0.001f) {
            g.blit(OW_SPRITES, x, y, u, v, CHEST_W, CHEST_H, 256, 256);
        } else {
            // Bas du coffre, immobile — la caisse ne bouge pas, seul le couvercle part.
            g.blit(OW_SPRITES, x, y + seam, u, v + seam,
                    CHEST_W, CHEST_H - seam, 256, 256);

            // Bouche du coffre : la lumière est dense sur la ligne de jointure et s'évanouit en
            // montant. Un aplat à bord franc se lisait comme une barre posée là ; un dégradé se
            // raccorde naturellement à la colonne de lumière qui monte au-dessus.
            float mouth = Math.min(1f, lift * 2.2f);
            int light = chestLightColor(chest);
            // ATTENTION au sens : dans une interface, y croît vers le BAS. Un décalage négatif
            // monte donc à l'écran. La lumière est dense sur la jointure — sa source — et
            // s'évanouit en s'élevant ; elle ne déborde pas sur la façade du coffre en dessous.
            final int fadeRows = 8;
            for (int i = -fadeRows; i <= 1; i++) {
                float k = i >= 0 ? 1f : 1f + i / (float) fadeRows;
                int a = (int) (235 * mouth * k * k);
                if (a <= 0) continue;
                // Le halo se resserre à mesure qu'il s'élève, comme la base d'un faisceau.
                int inset = 3 + Math.max(0, -i) / 2;
                g.fill(x + inset, y + seam + i, x + CHEST_W - inset, y + seam + i + 1,
                        (a << 24) | light);
            }

            // Couvercle propulsé : il monte en décélérant, bascule, et sort du cadre.
            float e = 1f - (1f - lift) * (1f - lift);
            int rise = Math.round(e * CHEST_H * 2.6f);
            g.pose().pushPose();
            g.pose().translate(x + CHEST_W / 2f, y + seam - rise, 0);
            g.pose().mulPose(com.mojang.math.Axis.ZP.rotationDegrees(-38f * e));
            g.pose().translate(-CHEST_W / 2f, -seam, 0);
            g.blit(OW_SPRITES, 0, 0, u, v, CHEST_W, seam, 256, 256);
            g.pose().popPose();
        }
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);

        if (hovered && active && lift == 0f) {
            g.fill(x - 1, y - 1, x + CHEST_W + 1, y, 0x55FFFFFF);
            g.fill(x - 1, y + CHEST_H, x + CHEST_W + 1, y + CHEST_H + 1, 0x55FFFFFF);
            g.fill(x - 1, y, x, y + CHEST_H, 0x55FFFFFF);
            g.fill(x + CHEST_W, y, x + CHEST_W + 1, y + CHEST_H, 0x55FFFFFF);
        }
    }

    /** Barre de progression vers le prochain coffre (remplissage teinté + curseur). */
    private void drawProgressBar(GuiGraphics g, int x, int y, float progress, int accent) {
        g.fill(x - 1, y - 1, x + BAR_W + 1, y + BAR_H + 1, 0xFF000000);
        g.fill(x, y, x + BAR_W, y + BAR_H, 0xFF16161A);
        int fillW = Math.max(0, Math.min(BAR_W, Math.round(BAR_W * progress)));
        if (fillW > 0) {
            g.fill(x, y, x + fillW, y + BAR_H, 0xFF000000 | (accent & 0xFFFFFF));
            g.fill(x, y, x + fillW, y + 2, 0x44FFFFFF);                      // reflet
            drawBarShimmer(g, x, y, BAR_W, BAR_H, fillW, clock());
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
                // Voile blanc qui envahit l'écran juste avant la rupture. Il monte jusqu'à saturation
                // pour se raccorder sans couture à l'éclair de la phase suivante.
                g.fill(0, 0, this.width, this.height, ((int) (255 * p * p) << 24) | 0xFFFFFF);
            }
            case BURST -> {
                float p = Math.min(1f, elapsed / (float) BURST_MS);
                float since = elapsed / 1000f;

                OWCinematicFx.drawRays(g, cx, cy, burstRays, since, 1f, accent, 0.8f);
                OWCinematicFx.drawShockwave(g, cx, cy, since, 1f);
                drawChestGlow(g, cx, cy, accent, 2.0f * (1f - p), 1f);
                // Faisceau posé AVANT le coffre : il jaillit de derrière la caisse, pas devant.
                drawLightColumn(g, cx, chestMouthY(cy), accent, 0.4f + 1.5f * p, chestSpanX());
                drawBigChest(g, cx, cy, accent, p, 1f);
                drawBurstParticles(g, cx, cy - 10, since, accent);

                // Éclair posé EN DERNIER, et non en fond : il doit recouvrir la scène au moment
                // précis où le couvercle se détache. C'est ce voile qui masque la bascule — sans
                // lui on voit le couvercle « sauter » d'un état à l'autre, ce qui fait brouillon.
                float flash = 1f - Math.min(1f, elapsed / 260f);
                int a = (int) (255 * flash * flash);
                if (a > 0) g.fill(0, 0, this.width, this.height, (a << 24) | 0xFFFFFF);
            }
            case REVEAL -> {
                float since = (BURST_MS + elapsed) / 1000f;
                // Plus il y a de rangées, plus la composition est haute : on la remonte d'autant
                // pour qu'elle reste centrée et que la ligne « Cliquez pour continuer » ne sorte
                // jamais de l'écran, même en interface très agrandie.
                int lift = reward != null
                        ? (rewardGrid(reward.items().size()).rows() - 1) * 15 : 0;
                // Borné pour que le sommet du coffre reste visible : mieux vaut une composition
                // légèrement basse qu'un coffre à moitié sorti par le haut.
                int chestHalf = (CHEST_H * bigChestScale()) / 2;
                lift = Math.min(lift, Math.max(0, cy - 46 - chestHalf - 4));
                int chestCy = cy - 46 - lift;
                OWCinematicFx.drawRays(g, cx, chestCy, burstRays, since, 1f, accent, 0.8f);
                drawChestGlow(g, cx, chestCy, accent, 0.5f, 1f);
                // Le faisceau persiste et respire : le coffre reste « vivant » pendant la remise.
                float breath = 0.55f + 0.18f * (float) Math.sin(System.currentTimeMillis() / 260.0);
                drawLightColumn(g, cx, chestMouthY(chestCy), accent, breath, chestSpanX());
                drawBigChest(g, cx, chestCy, accent, 1f, 1f);
                drawBurstParticles(g, cx, chestCy - 10, since, accent);
                renderRewardCards(g, cx, cy - 4 - lift, elapsed, accent);
            }
            default -> {}
        }
    }

    /**
     * Couleur de la lumière que dégage un coffre : teintes choisies à la main, matière par matière.
     *
     * <p>Elles étaient auparavant dérivées de l'accent par éclaircissement automatique ; la table
     * explicite l'emporte, une lumière se règle à l'œil et non par formule. Le {@code switch} est
     * exhaustif : une matière ajoutée sans sa teinte ne compilera pas.</p>
     */
    private static int chestLightColor(OWArena.Chest chest) {
        return switch (chest.material()) {
            case RUBY -> 0xFFB78C;
            case JADE -> 0xC0D988;
            case GOLD, TRIBE -> 0xFFFFFF;
        };
    }

    /** Coffre courant de l'animation, ou un repli si le butin n'est pas encore arrivé. */
    private OWArena.Chest currentChest() {
        return reward != null ? reward.chest() : OWArena.Chest.GOLD_NORMAL;
    }

    /**
     * Facteur d'agrandissement du coffre en gros plan. <b>Toujours entier</b>, sous peine de rendre
     * le pixel-art flou : on descend à ×2 sur les interfaces trop courtes (petite résolution ou
     * échelle d'interface élevée) plutôt que d'accepter une valeur intermédiaire.
     */
    private int bigChestScale() { return this.height < 220 ? 2 : 3; }

    /**
     * Largeur du faisceau en gros plan. Volontairement plus étroite que la caisse : la lumière sort
     * de la bouche du coffre, pas de ses flancs.
     */
    private int chestSpanX() { return Math.round(CHEST_W * bigChestScale() * 0.58f); }

    /** Ordonnée de la bouche du coffre en gros plan, centré sur {@code cy}. */
    private int chestMouthY(int cy) {
        int scale = bigChestScale();
        int seam = chestSeam(reward != null ? reward.chest() : OWArena.Chest.GOLD_NORMAL);
        return cy - (CHEST_H * scale) / 2 + seam * scale;
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
        // Au-delà de la pleine intensité, la teinte se décolore vers le blanc : la lumière « sature ».
        int color = intensity > 1f ? blend(base, 0xFFFFFF, Math.min(1f, intensity - 1f)) : base;
        int radius = Math.round(66 * (0.55f + 0.75f * Math.min(2.2f, intensity)) * pulse);
        OWCinematicFx.drawGlow(g, cx, cy, radius, color, Math.min(2.2f, intensity));
    }

    /**
     * Colonne de lumière jaillissant de la bouche du coffre. Dessinée en bandes de plus en plus
     * étroites et transparentes vers le haut : c'est ce qui donne un faisceau, là où un rectangle
     * plein ne donnait qu'une dalle opaque.
     */
    private void drawLightColumn(GuiGraphics g, int cx, int mouthY, int accent,
                                 float intensity, int width) {
        if (intensity <= 0.01f) return;
        // Beaucoup de tranches fines plutôt que quelques bandes épaisses : avec 16 segments de
        // 14 px, l'écart d'opacité entre deux voisines se voyait comme des marches d'escalier.
        final int segments = 56;
        final float segH = 4.5f;
        int light = chestLightColor(currentChest());
        int base = accent & 0xFFFFFF;

        for (int i = 0; i < segments; i++) {
            float t = i / (float) segments;
            // Effilement en racine : large à la base, resserré en montant, sans cassure.
            float taper = (1f - t) * (1f - t) * 0.75f + 0.12f;
            int halfW = Math.max(1, Math.round(width * 0.5f * taper * Math.min(1.3f, intensity)));
            int a = (int) (105 * intensity * (1f - t) * (1f - t));
            if (a <= 0) continue;
            // Le cœur du faisceau vire au blanc, ses bords gardent la teinte de la matière.
            int color = blend(base, light, Math.min(1f, 0.35f + (1f - t)));
            int y0 = Math.round(mouthY - segH * (i + 1));
            int y1 = Math.round(mouthY - segH * i);
            g.fill(cx - halfW, y0, cx + halfW, y1, (a << 24) | color);
        }
    }

    /** Fissures de lumière s'échappant de la jointure du couvercle, de plus en plus larges. */
    private void drawSeamCracks(GuiGraphics g, int cx, int cy, float p, long elapsed) {
        if (p <= 0.05f) return;
        float scale = bigChestScale();
        int halfW = (int) (CHEST_W * scale / 2f);
        // Calée sur la vraie ligne de jointure du sprite, pas sur une proportion approchée.
        int seam = chestSeam(reward != null ? reward.chest() : OWArena.Chest.GOLD_NORMAL);
        int seamY = cy - (int) (CHEST_H * scale / 2f) + (int) (seam * scale);

        // Scintillement : la lumière n'est jamais tout à fait stable.
        float flicker = 0.75f + 0.25f * (float) Math.sin(elapsed / 45.0);
        int thickness = Math.max(1, (int) (p * 6f * flicker));
        int alpha = (int) (255 * Math.min(1f, p * 1.3f) * flicker);
        g.fill(cx - halfW, seamY - thickness / 2, cx + halfW, seamY - thickness / 2 + thickness,
                (alpha << 24) | chestLightColor(currentChest()));

        // Rais verticaux qui percent par la fente, de plus en plus longs.
        int beams = 7;
        for (int i = 0; i < beams; i++) {
            int bx = cx - halfW + (int) ((i + 0.5f) * (halfW * 2f / beams));
            int len = (int) (p * p * (26 + (i * 13 % 22)) * flicker);
            if (len <= 0) continue;
            int a = (int) (150 * p * flicker);
            g.fill(bx - 1, seamY - len, bx + 1, seamY, (a << 24) | chestLightColor(currentChest()));
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
    /**
     * Gros plan du coffre pendant l'ouverture. Le facteur de base est un <b>entier</b> (×3) : toute
     * autre valeur rendrait le pixel-art flou. {@code extraScale} ne s'en écarte que le temps d'une
     * respiration ou d'un gonflement, jamais à l'arrêt.
     */
    private void drawBigChest(GuiGraphics g, int cx, int cy, int accent, float lift, float extraScale) {
        final float scale = bigChestScale() * extraScale;
        OWArena.Chest chest = reward != null ? reward.chest() : OWArena.Chest.GOLD_NORMAL;
        g.pose().pushPose();
        g.pose().translate(cx, cy, 0);
        g.pose().scale(scale, scale, 1f);
        drawChest(g, -CHEST_W / 2, -CHEST_H / 2, chest, true, false, lift);
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

        int n = items.size();
        RewardGrid grid = rewardGrid(n);
        int card = grid.card(), gap = grid.gap(), perRow = grid.perRow(), rows = grid.rows();
        int cardY = cy + 14;

        // Point de départ des lots : la bouche du coffre, juste au-dessus.
        int sourceY = cy - 34;

        int revealed = (int) Math.min(n, elapsed / REVEAL_STEP_MS);
        for (int i = 0; i < revealed; i++) {
            int row = i / perRow, col = i % perRow;
            // Chaque rangée est centrée sur elle-même : la dernière, souvent incomplète, ne part
            // pas en biais sur la gauche.
            int inRow = Math.min(perRow, n - row * perRow);
            int rowW = inRow * card + Math.max(0, inRow - 1) * gap;
            int x = cx - rowW / 2 + col * (card + gap);
            int rowY = cardY + row * (card + gap);
            long age = elapsed - i * REVEAL_STEP_MS;

            // Chaque lot s'échappe du coffre puis rejoint sa case : un arc, pas une apparition.
            float flight = easeOut(Math.min(1f, age / (float) FLIGHT_MS));
            int drawX = Math.round(cx + (x - cx) * flight);
            int drawY = Math.round(sourceY + (rowY - sourceY) * flight);
            // Léger rebond en fin de course, pour que l'arrivée claque.
            float pop = age < FLIGHT_MS ? 0.45f + 0.55f * flight
                    : (age < FLIGHT_MS + 130
                        ? 1f + 0.22f * (float) Math.sin((age - FLIGHT_MS) / 130f * Math.PI) : 1f);

            // Une trouvaille remarquable porte la couleur du coffre en version claire, sur les
            // quatre bords plutôt qu'un simple liseré : elle doit sauter aux yeux dans la grille.
            boolean rare = reward.isRare(i);
            int edge = rare ? chestLightColor(reward.chest()) : (accent & 0xFFFFFF);

            if (rare) {
                // Halo derrière la carte, pulsé, pour la détacher de ses voisines.
                float pulse = 0.55f + 0.45f * (float) Math.sin(clock() * 3.6f + i);
                OWCinematicFx.drawGlow(g, drawX + card / 2, drawY + card / 2,
                        Math.round(card * 0.95f), edge, 0.85f + 0.5f * pulse);
            }

            g.pose().pushPose();
            g.pose().translate(drawX + card / 2f, drawY + card / 2f, 0);
            g.pose().scale(pop, pop, 1f);
            g.pose().translate(-card / 2f, -card / 2f, 0);
            g.fill(0, 0, card, card, rare ? 0xF01C1C22 : 0xF0141418);
            if (rare) {
                g.fill(0, 0, card, 1, 0xFF000000 | edge);
                g.fill(0, card - 1, card, card, 0xFF000000 | edge);
                g.fill(0, 0, 1, card, 0xFF000000 | edge);
                g.fill(card - 1, 0, card, card, 0xFF000000 | edge);
            } else {
                g.fill(0, 0, card, 1, 0xFF000000 | edge);
                g.fill(0, card - 1, card, card, 0x33FFFFFF);
            }
            g.pose().popPose();

            // Son au moment précis où le lot se pose, une seule fois par lot.
            if (age >= FLIGHT_MS && i >= landedSounds) {
                landedSounds = i + 1;
                if (rare) playRareChime();
                else playUi(SoundEvents.NOTE_BLOCK_HAT.value(), 1.5f);
            }

            // Traînée lumineuse derrière le lot encore en vol.
            if (flight < 1f) {
                int a = (int) (150 * (1f - flight));
                g.fill(drawX + card / 2 - 2, drawY + card / 2, drawX + card / 2 + 2,
                        sourceY + card / 2, (a << 24) | edge);
            }

            // L'item et sa quantité sont rendus hors du pop : renderItem gère sa propre pile de matrices.
            ItemStack stack = items.get(i);
            g.renderItem(stack, drawX + (card - 16) / 2, drawY + (card - 16) / 2);
            g.renderItemDecorations(this.font, stack, drawX + (card - 16) / 2, drawY + (card - 16) / 2);
        }

        // Pièces Sauvages : annoncées APRÈS les objets, avec un temps mort volontaire. Les révéler
        // dans la foulée noyait le montant au milieu des lots qui volaient encore.
        long coinsAt = (n - 1) * REVEAL_STEP_MS + FLIGHT_MS + COINS_DELAY_MS;
        if (revealed >= n && elapsed >= coinsAt) {
            int coinsY = cardY + rows * (card + gap) + 6;
            long coinsAge = elapsed - coinsAt;
            float pop = coinsAge < 200 ? 0.6f + 0.4f * easeOut(coinsAge / 200f)
                    : (coinsAge < 340 ? 1f + 0.14f * (float) Math.sin((coinsAge - 200) / 140f * Math.PI) : 1f);

            String label = "+" + reward.coins();
            int labelW = this.font.width(label);
            final int icon = 16, gapIcon = 3;
            int totalW = labelW + gapIcon + icon;

            g.pose().pushPose();
            g.pose().translate(cx, coinsY + 6, 0);
            g.pose().scale(pop, pop, 1f);
            g.drawString(this.font, label, -totalW / 2, -4, 0xFFFFD257, true);
            g.blit(coinTexture(reward.coins()), -totalW / 2 + labelW + gapIcon, -icon / 2,
                    0, 0, icon, icon, icon, icon);
            g.pose().popPose();

            if (elapsed > coinsAt + 700) {
                g.drawCenteredString(this.font, Component.translatable("owteams.arena.reward.dismiss"),
                        cx, coinsY + 20, 0x8A8A8A);
            }
        }
    }

    /** Disposition des lots : taille de case, nombre par rangée et nombre de rangées. */
    private record RewardGrid(int card, int gap, int perRow, int rows) {
        /** Hauteur totale occupée par la grille. */
        int height() { return rows * (card + gap); }
    }

    /**
     * Répartit {@code n} lots en une grille bornée en largeur.
     *
     * <p>Un grand coffre de rubis peut lâcher une vingtaine d'objets : sur une seule ligne ils
     * débordaient de l'écran des deux côtés. On étale donc sur plusieurs rangées, et on ne rétrécit
     * les cases que si trois rangées n'y suffisent toujours pas.</p>
     */
    private RewardGrid rewardGrid(int n) {
        final int gap = 4;
        // La grille doit tenir en largeur ET en hauteur : sur une interface très courte
        // (petite résolution à forte échelle), trois rangées de grandes cases plus le coffre
        // et les deux lignes de texte ne rentrent tout simplement pas.
        boolean cramped = this.height < 200;
        int maxRows = cramped ? 2 : 3;
        int card = cramped ? 20 : 26;
        int maxRowW = Math.min(this.width - 40, 360);

        int perRow = Math.max(1, maxRowW / (card + gap));
        if (n > perRow * maxRows) {
            // Trop de lots pour la grille : on resserre les cases plutôt que d'ajouter une rangée.
            perRow = (n + maxRows - 1) / maxRows;
            card = Math.max(12, maxRowW / perRow - gap);
        }
        return new RewardGrid(card, gap, perRow, Math.max(1, (n + perRow - 1) / perRow));
    }

    /**
     * Icône de bourse correspondant au montant : plus la somme est grosse, plus le tas est fourni.
     * Les seuils suivent les fourchettes des coffres, du plus petit doré au plus grand rubis.
     */
    private static ResourceLocation coinTexture(int coins) {
        // Paliers fixés à la main : 0-6, 7-16, 17-32, 33 et plus. Ils sont volontairement
        // indépendants des montants de l'énumération Chest — si ceux-ci sont rééquilibrés,
        // c'est ici qu'il faut repasser.
        String name = coins >= 33 ? "coin_4"
                : coins >= 17 ? "coin_3"
                : coins >= 7 ? "coin_2"
                : "coin";
        return ResourceLocation.fromNamespaceAndPath(
                net.tiew.operationWild.OperationWild.MOD_ID, "textures/misc/" + name + ".png");
    }
}
