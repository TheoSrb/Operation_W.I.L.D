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
                .coins('O', 2, 1, 0)
                .fork('O')
                .xp('C', 25, 1, 4)
                .step('P', 1, 6)
                .coins('I', 3, 1, 8)
                .step('S', 2, 10)
                .coins('L', 4, 2, 13)
                .coins('N', 4, 2, 15)
                .step('R', 3, 17)
                .xp('E', 45, 2, 19)
                .xp('F', 55, 2, 21)
                .fork('F')
                .xp('A', 60, 2, 24)
                .coins('K', 5, 2, 26)
                .xp('G', 70, 2, 28)
                .xp('H', 80, 3, 30)
                .fork('H')
                .step('T', 4, 33)
                .coins('M', 12, 2, 34)
                .step('Q', 4, 36)
                .coins('D', 6, 3, 39)
                .attack('B', 0, 1, 15, 35)
                .xp('Y', 65, 2, 42)
                .coins('W', 6, 3, 48)
                .step('V', 5, 46)
                .xp('Z', 250, 3, 50).requireAll('Z')
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
                .coins('O', 3, 1, 0)
                .fork('O')
                .xp('C', 33, 1, 5)
                .step('U', 2, 6)
                .coins('I', 6, 2, 8)
                .step('R', 2, 8)
                .step('F', 1, 10)
                .coins('L', 5, 2, 12)
                .coins('N', 6, 2, 15)
                .step('P', 6, 16)
                .xp('D', 63, 3, 18)
                .xp('E', 51, 2, 22)
                .xp('G', 89, 3, 23)
                .coins('J', 5, 2, 23)
                .xp('H', 57, 2, 25)
                .fork('H')
                .step('Q', 3, 28)
                .xp('M', 33, 2, 30)
                .step('T', 3, 32)
                .coins('K', 6, 2, 37)
                .xp('B', 63, 3, 39)
                .attack('X', 0, 1, 12, 40)
                .step('W', 4, 42)
                .coins('Y', 5, 5, 50)
                .xp('Z', 200, 3, 25).requireAll('Z')
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
                .coins('P', 3, 1, 0)
                .fork('P')
                .xp('C', 25, 1, 3)
                .step('W', 1, 5)
                .coins('J', 4, 1, 7)
                .fork('J')
                .coins('O', 4, 2, 9)
                .step('T', 2, 12)
                .step('Q', 3, 14)
                .coins('I', 5, 2, 16)
                .step('U', 3, 19)
                .coins('N', 5, 2, 21)
                .xp('E', 45, 2, 24)
                .step('V', 4, 27)
                .coins('M', 6, 2, 30)
                .xp('F', 55, 2, 33)
                .xp('G', 70, 2, 36)
                .coins('K', 6, 3, 39)
                .xp('A', 90, 3, 42)
                .attack('R', 0, 1, 15, 46)
                .xp('Z', 250, 3, 50).requireAll('Z')
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
                .coins('S', 2, 1, 0)
                .fork('S')
                .step('R', 1, 3)
                .coins('Q', 3, 1, 5)
                .step('T', 2, 7)
                .xp('B', 30, 1, 9)
                .coins('F', 4, 2, 11)
                .attack('X', 0, 1, 15, 13)
                .step('A', 2, 15)
                .xp('G', 45, 2, 17)
                .coins('L', 4, 2, 19)
                .step('O', 3, 21)
                .xp('D', 55, 2, 23)
                .coins('E', 5, 2, 25)
                .fork('E')
                .step('I', 3, 27)
                .xp('H', 70, 2, 29)
                .coins('M', 5, 3, 31)
                .step('K', 4, 33)
                .xp('C', 85, 3, 35)
                .coins('N', 6, 3, 37)
                .step('J', 4, 39)
                .xp('P', 110, 3, 41)
                .xp('U', 140, 3, 44)
                .xp('Z', 250, 3, 50).requireAll('Z')
                .build();
    }
}
