package net.tiew.operationWild.entity.piste;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Un nœud (« palier ») de la Piste Sauvage.
 * <p>La topologie d'une Piste est identique pour tous les individus d'une même espèce
 * (voir {@link OWPisteGraphs}) ; seule la progression est propre à chaque individu
 * (stockée sur l'entité). Un nœud connaît ses enfants (arêtes orientées départ → arrivée),
 * son coût d'entrée en Empreintes, un éventuel palier de niveau requis, et sa récompense.</p>
 *
 * <p>Types de récompense v1 : {@link Type#START} (départ, aucune récompense),
 * {@link Type#XP} et {@link Type#COINS}. Les types Attaque / Passif / Cosmétique
 * viendront se brancher ici plus tard.</p>
 */
public class OWPisteNode {

    public enum Type {
        START,
        STEP,   // palier neutre : sert à faire cheminer/serpenter la piste, sans récompense
        XP,
        COINS,
        ATTACK  // palier d'attaque : choix définitif entre deux attaques
    }

    /** Une option d'un palier à choix (récompense possible parmi plusieurs). */
    public record Option(Type type, int amount) {}

    private final int id;
    private final int x;
    private final int y;
    private final Type type;
    private final int rewardAmount;
    private final int cost;
    private final int requiredLevel;
    /**
     * Si vrai, ce nœud est un carrefour de choix exclusif : dès qu'un de ses enfants est
     * débloqué, les autres enfants sont verrouillés <b>à vie</b> pour l'individu.
     */
    private final boolean exclusiveFork;
    /**
     * Si vrai, ce nœud est la récompense finale : il ne devient accessible que lorsque plus aucun
     * autre palier n'est explorable (tout le reste débloqué ; les branches rendues inatteignables
     * par un choix exclusif ne comptent pas). Voir {@link OWPisteRules#finalGateOpen}.
     */
    private final boolean requiresAll;

    private final List<Integer> children = new ArrayList<>();
    /** Options d'un palier à choix XP/Pièces (≥ 2 = ce nœud demande un choix définitif à l'unlock). */
    private final List<Option> options = new ArrayList<>();
    /** Ids d'attaques d'un palier ATTACK (≥ 2 = choix définitif entre deux attaques). */
    private final List<Integer> attackIds = new ArrayList<>();

    OWPisteNode(int id, int x, int y, Type type, int rewardAmount, int cost, int requiredLevel,
                boolean exclusiveFork, boolean requiresAll) {
        this.id = id;
        this.x = x;
        this.y = y;
        this.type = type;
        this.rewardAmount = rewardAmount;
        this.cost = cost;
        this.requiredLevel = requiredLevel;
        this.exclusiveFork = exclusiveFork;
        this.requiresAll = requiresAll;
    }

    void addChild(int childId) {
        if (!children.contains(childId)) children.add(childId);
    }

    void addOption(Type type, int amount) {
        options.add(new Option(type, amount));
    }

    public List<Option> getOptions() { return Collections.unmodifiableList(options); }

    /** Vrai si ce palier demande un choix définitif (au moins deux options). */
    public boolean hasChoice() { return options.size() >= 2; }

    void addAttack(int attackId) { attackIds.add(attackId); }

    public List<Integer> getAttackIds() { return Collections.unmodifiableList(attackIds); }

    /** Vrai si ce palier propose un choix définitif entre deux attaques. */
    public boolean isAttackChoice() { return type == Type.ATTACK && attackIds.size() >= 2; }

    public int getId() { return id; }

    public int getX() { return x; }

    public int getY() { return y; }

    public Type getType() { return type; }

    public int getRewardAmount() { return rewardAmount; }

    public int getCost() { return cost; }

    public int getRequiredLevel() { return requiredLevel; }

    public boolean isExclusiveFork() { return exclusiveFork; }

    public boolean isRequiresAll() { return requiresAll; }

    public List<Integer> getChildren() { return Collections.unmodifiableList(children); }
}
