package net.tiew.operationWild.entity.piste;

import net.tiew.operationWild.entity.OWEntity;

import java.util.HashMap;
import java.util.Map;

public final class OWPisteGraphs {

    private OWPisteGraphs() {}

    private static final Map<String, OWPisteGraph> BY_SPECIES = new HashMap<>();

    static {
        BY_SPECIES.put("KodiakEntity", buildKodiak());
        BY_SPECIES.put("TigerEntity", buildTiger());
        BY_SPECIES.put("CrocodileEntity", buildCrocodile());
        BY_SPECIES.put("BoaEntity", buildBoa());
        BY_SPECIES.put("RedPandaEntity", buildRedPanda());
    }

    public static OWPisteGraph forEntity(OWEntity entity) {
        if (entity == null) return null;
        return BY_SPECIES.get(entity.getClass().getSimpleName());
    }

    public static OWPisteGraph forSpecies(String simpleClassName) {
        return BY_SPECIES.get(simpleClassName);
    }

    private static final int U = 32;

    private static OWPisteGraph buildKodiak() {
        return OWPisteMaze.create(U)
                .map("""
                         B
                         |
                         |
                         | C
                       F-I |
                         | |
                         | |
                       o-Y-W--Z
                       | | |
                    M  | | H
                    |  R |      S
                I---o-G--I      |
                      |  |   P--o
                      |  |   |
                    o-H-oO---o
                   L-F-K | o-T--Q
                     |   |   |
                     o---o---R-O-D
                     |   |     |
                     |   N     |
                     |   |     |
                 o---E-A |     B
                     |   G---H
                     |
                     |
                     V
                """)
                .coins('O', 2, 5, 0)
                .fork('O')
                .xp('C', 25, 5, 4)
                .step('P', 5, 6)
                .coins('I', 3, 5, 8)
                .step('S', 10, 10)
                .coins('L', 4, 10, 13)
                .coins('N', 4, 10, 15)
                .step('R', 15, 17)
                .xp('E', 45, 10, 19)
                .xp('F', 55, 10, 21)
                .fork('F')
                .xp('A', 60, 10, 24)
                .coins('K', 5, 10, 26)
                .xp('G', 70, 10, 28)
                .xp('H', 80, 15, 30)
                .fork('H')
                .step('T', 20, 33)
                .coins('M', 12, 10, 34)
                .step('Q', 20, 36)
                .coins('D', 6, 15, 39)
                .attack('B', 0, 1, 75, 35)
                .xp('Y', 65, 10, 42)
                .coins('W', 6, 15, 48)
                .step('V', 25, 46)
                .xp('Z', 250, 15, 50).requireAll('Z')
                .build();
    }

    private static OWPisteGraph buildTiger() {
        return OWPisteMaze.create(U)
                .map("""
                           o
                      G    |
                      |    |
                      J--o |
                 S---o|o-L-o
                F--o ||  | |
                   | |E--R P
                   I-O|  |
                     ||  |
                     |H-R|
                   T C---U
                   | o--K|
                   | |   | E-o---D
                   | |   |   |
                   o-H---N---o-H-B
                   | |       | |
                   Q |       | |
                  W--o-I     | M---X---Y---Z
                     |     W-o
                     |
                     E
                """)
                .coins('O', 3, 5, 0)
                .fork('O')
                .xp('C', 33, 5, 5)
                .step('U', 10, 6)
                .coins('I', 6, 10, 8)
                .step('R', 10, 8)
                .step('F', 5, 10)
                .coins('L', 5, 10, 12)
                .coins('N', 6, 10, 15)
                .step('P', 30, 16)
                .xp('D', 63, 15, 18)
                .xp('E', 51, 10, 22)
                .xp('G', 89, 15, 23)
                .coins('J', 5, 10, 23)
                .xp('H', 57, 10, 25)
                .fork('H')
                .step('Q', 15, 28)
                .xp('M', 33, 10, 30)
                .step('T', 15, 32)
                .coins('K', 6, 10, 37)
                .xp('B', 63, 15, 39)
                .attack('X', 0, 1, 60, 40)
                .step('W', 20, 42)
                .coins('Y', 5, 25, 50)
                .xp('Z', 200, 15, 25).requireAll('Z')
                .build();
    }

    private static OWPisteGraph buildCrocodile() {
        return OWPisteMaze.create(U)
                .map("""
                        A
                        |
                        |
                        E--W
                        |
                R---F---G---U--C
                    |O-T| E-J-o---I
                    ||  |   |     |
                    |o--o---J-W Q |
                    V   |   | | | R
                    K   | W | | K-P--N
                    |   | | T |   |  |
                R---o---M | | P---C  O
                       F--V-o |      |
                          | S-o      W
                          |
                          Z
                """)
                .coins('P', 3, 5, 0)
                .fork('P')
                .xp('C', 25, 5, 3)
                .step('W', 5, 5)
                .coins('J', 4, 5, 7)
                .fork('J')
                .coins('O', 4, 10, 9)
                .step('T', 10, 12)
                .step('Q', 15, 14)
                .coins('I', 5, 10, 16)
                .step('U', 15, 19)
                .coins('N', 5, 10, 21)
                .xp('E', 45, 10, 24)
                .step('V', 20, 27)
                .coins('M', 6, 10, 30)
                .xp('F', 55, 10, 33)
                .xp('G', 70, 10, 36)
                .coins('K', 6, 15, 39)
                .xp('A', 90, 15, 42)
                .attack('R', 0, 1, 75, 46)
                .xp('Z', 250, 15, 50).requireAll('Z')
                .build();
    }

    /**
     * Piste du panda roux, bâtie sur le patron du tigre : un tronc qui serpente, des embranchements
     * qui reviennent sur eux-mêmes, et les gros gains rejetés en bout de branche pour qu'on ait à
     * choisir par où passer plutôt qu'à tout ramasser au passage.
     *
     * <p>Le palier de choix est posé au niveau 34, à mi-chemin de la dernière remontée : assez tard
     * pour que le joueur sache déjà comment il soigne, assez tôt pour en profiter seize niveaux.</p>
     */
    private static OWPisteGraph buildRedPanda() {
        return OWPisteMaze.create(U)
                .map("""
                       o
                       |
                     A-o--C
                     |    |
                   D-o-E  |
                     |  | |
                     F  o-G---H
                     |  |     |
                   I-o--J     o-K
                     |  |     |
                     L  o-M-N-o
                     |      |
                     o--P   Q---R
                     |  |   |
                     S  o-T-o-U
                     |      |
                     V------W---X
                            |
                            Y
                            |
                            Z
                """)
                .coins('J', 3, 5, 0)
                .fork('J')
                .xp('E', 30, 5, 4)
                .step('M', 8, 6)
                .coins('G', 4, 10, 9)
                .step('N', 12, 11)
                .xp('C', 42, 10, 13)
                .coins('K', 5, 10, 16)
                .step('T', 15, 18)
                .xp('H', 55, 10, 20)
                .coins('P', 5, 10, 22)
                .step('Q', 15, 24)
                .xp('A', 68, 12, 26)
                .fork('A')
                .coins('U', 6, 12, 28)
                .step('F', 18, 30)
                .xp('D', 82, 12, 32)
                .attack('R', 2, 3, 70, 34)
                .coins('I', 6, 15, 36)
                .step('L', 20, 38)
                .xp('S', 105, 15, 40)
                .coins('W', 7, 15, 42)
                .step('V', 25, 44)
                .xp('X', 140, 15, 46)
                .coins('Y', 8, 20, 48)
                .xp('Z', 250, 15, 50).requireAll('Z')
                .build();
    }

    private static OWPisteGraph buildBoa() {
        return OWPisteMaze.create(U)
                .map("""
                G
                |
              E-o-o-o-F
              |       |
            D-o       o
              |
              M-o-o-H
                |
                C
                |
              J-o-K-o-o
              |       |
            N-o Q-o-o I
              | |   | |
            P-o T S-R o-L
              | |     |
              U o-X-o-O
              |   |   |
              Z   B   A
                """)
                .coins('S', 2, 5, 0)
                .fork('S')
                .step('R', 5, 3)
                .coins('Q', 3, 5, 5)
                .step('T', 10, 7)
                .xp('B', 30, 5, 9)
                .coins('F', 4, 10, 11)
                .attack('X', 0, 1, 75, 13)
                .step('A', 10, 15)
                .xp('G', 45, 10, 17)
                .coins('L', 4, 10, 19)
                .step('O', 15, 21)
                .xp('D', 55, 10, 23)
                .coins('E', 5, 10, 25)
                .fork('E')
                .step('I', 15, 27)
                .xp('H', 70, 10, 29)
                .coins('M', 5, 15, 31)
                .step('K', 20, 33)
                .xp('C', 85, 15, 35)
                .coins('N', 6, 15, 37)
                .step('J', 20, 39)
                .xp('P', 110, 15, 41)
                .xp('U', 140, 15, 44)
                .xp('Z', 250, 15, 50).requireAll('Z')
                .build();
    }
}
