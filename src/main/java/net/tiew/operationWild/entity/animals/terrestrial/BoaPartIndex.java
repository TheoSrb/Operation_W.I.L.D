package net.tiew.operationWild.entity.animals.terrestrial;

/**
 * Copie adaptee d'AnacondaPartIndex (Alex's Mobs).
 *
 * Alex n'a que 4 types (HEAD/NECK/BODY/TAIL) reutilises sur ses segments. Toi tu
 * as 7 modeles distincts (body_0..body_6), donc on a 1 type par segment + HEAD.
 * Chaque type porte son backOffset (demi-longueur de couplage), exactement comme
 * Alex : c'est ce qui espace les maillons le long de la chaine.
 *
 * backOffset d'Alex : HEAD=0F, NECK=0.5F, BODY=0.5F, TAIL=0.4F. On reprend les
 * memes valeurs (0.5 pour le corps, 0.4 pour le tout dernier segment).
 */
public enum BoaPartIndex {
    HEAD(0.0F),
    SEG0(0.5F),
    SEG1(0.5F),
    SEG2(0.5F),
    SEG3(0.5F),
    SEG4(0.5F),
    SEG5(0.5F),
    SEG6(0.4F);

    private final float backOffset;

    BoaPartIndex(float backOffset) {
        this.backOffset = backOffset;
    }

    public float getBackOffset() {
        return this.backOffset;
    }

    public static BoaPartIndex fromOrdinal(int ordinal) {
        BoaPartIndex[] values = values();
        if (ordinal < 0 || ordinal >= values.length) return HEAD;
        return values[ordinal];
    }

    /**
     * Type de segment a l'index donne. Alex fait sizeAt(1 + i) pour mapper.
     * Ici : segment i (0..6) -> SEG{i}. L'index 1+i d'Alex correspond a notre i+1
     * dans values() (HEAD=0, SEG0=1, ...). On garde la meme convention d'appel.
     */
    public static BoaPartIndex sizeAt(int oneBasedIndex) {
        // oneBasedIndex va de 1 (premier segment) a 7 (dernier).
        return fromOrdinal(oneBasedIndex);
    }
}