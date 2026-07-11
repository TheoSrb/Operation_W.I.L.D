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
                           o-B-N--Z
                           | | |
                        B  | | H
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
                .xp('A', 63, 3, 18)
                .xp('B', 51, 2, 12)
                .xp('C', 63, 3, 18)
                .xp('D', 63, 3, 18)
                .xp('E', 57, 2, 15)
                .xp('F', 63, 3, 18)
                .xp('G', 51, 2, 12)
                .xp('H', 63, 3, 18)
                .coins('I', 4, 2, 9)
                .coins('K', 6, 2, 18)
                .coins('N', 5, 2, 15)
                .coins('O', 4, 1, 6)
                .coins('P', 10, 1, 0)
                .step('Q', 3, 14)
                .step('R', 3, 14)
                .step('T', 2, 12)
                .step('V', 3, 14)
                .fork('F')
                .fork('H')
                .fork('O')
                .xp('Z', 200, 3, 25).requireAll('Z')
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
                .xp('B', 63, 3, 39)
                .xp('C', 33, 1, 5)
                .xp('D', 63, 3, 18)
                .xp('E', 51, 2, 22)
                .step('F', 1, 10)
                .xp('G', 89, 3, 23)
                .xp('H', 57, 2, 25)
                .coins('I', 6, 2, 8)
                .coins('J', 5, 2, 23)
                .coins('K', 6, 2, 37)
                .coins('L', 5, 2, 12)
                .xp('M', 33, 2, 30)
                .coins('N', 6, 2, 15)
                .coins('O', 3, 1, 0)
                .step('P', 6, 16)
                .attack('X', 0, 1, 12, 40)
                .coins('Y', 5, 5, 50)
                .step('Q', 3, 28)
                .step('R', 2, 8)
                .step('T', 3, 32)
                .step('W', 4, 42)
                .step('U', 2, 6)
                .fork('H')
                .fork('O')
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
                .xp('A', 63, 3, 18)
                .xp('C', 63, 3, 18)
                .xp('E', 57, 2, 15)
                .xp('F', 63, 3, 18)
                .xp('G', 51, 2, 12)
                .coins('I', 5, 2, 15)
                .coins('J', 4, 1, 6)
                .coins('K', 6, 2, 18)
                .coins('M', 5, 2, 12)
                .coins('N', 4, 2, 9)
                .coins('O', 5, 2, 15)
                .coins('P', 3, 1, 0)
                .step('Q', 2, 10)
                .step('R', 3, 14)
                .step('T', 2, 8)
                .step('U', 2, 12)
                .step('V', 2, 12)
                .step('W', 1, 4)
                .fork('J')
                .fork('P')
                .xp('Z', 200, 3, 25).requireAll('Z')
                .build();
    }

    private static OWPisteGraph buildBoa() {
        return OWPisteMaze.create(U)
                .map("""
                    G
                    |
                  o-o-o-o-o
                  |       |
                D-o       o
                  |
                  o-o-o-H
                    |
                    C
                    |
                  o-o-o-o-o
                  |       |
                N-o o-o-o o
                  | |   | |
                P-o o S-o o-L
                  | |     |
                  o o-o-o-o
                  |   |   |
                  Z   B   A
                    """)
                .xp('A', 75, 3, 24)
                .xp('B', 63, 3, 18)
                .xp('C', 111, 5, 42)
                .coins('L', 7, 3, 27)
                .coins('N', 11, 4, 48)
                .coins('P', 11, 4, 20)
                .xp('Z', 200, 3, 25).requireAll('Z')
                .xp('G', 90, 5, 45)
                .coins('D', 10, 4, 30)
                .coins('H', 9, 4, 33)
                .build();
    }
}
