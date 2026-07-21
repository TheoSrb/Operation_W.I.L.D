package net.tiew.operationWild.entity.client.render.misc;

import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormatElement;
import net.minecraft.util.Mth;
import net.tiew.operationWild.team.OWTeamBannerShape;
import org.joml.Matrix4f;

/**
 * Consommateur de sommets qui transforme la bannière <b>plate</b> émise par
 * {@link OWRendererUtils#renderTeamBanner} en une <b>toile ondulante</b>.
 *
 * <p>Deux traitements sont enchaînés sur chaque quad reçu :</p>
 * <ol>
 *   <li><b>Tesselation adaptative</b> : un quad est découpé en une grille de sous-quads
 *       (interpolation bilinéaire de la position, de la couleur et des UV). Sans cela, un
 *       aplat de 55×93 ne compte que 4 sommets et ne peut pas se courber. Le pas de découpe
 *       est fonction de la taille du quad : les motifs déjà fins (peinture libre, 1 px par
 *       quad) ne sont pas subdivisés inutilement.</li>
 *   <li><b>Déformation</b> : chaque sommet est déplacé hors du plan par une somme de trois
 *       sinusoïdes de fréquences non harmoniques (l'onde ne se répète donc jamais à
 *       l'identique), d'amplitude nulle sur la hampe et croissant en {@code u²} vers
 *       l'extrémité libre. S'y ajoutent un raccourcissement (le tissu n'est pas élastique :
 *       plus il ondule, plus son extrémité se rapproche de la hampe) et un affaissement de la
 *       pointe d'autant plus marqué que l'entité est immobile.</li>
 * </ol>
 *
 * <p><b>Contrat d'appel</b> : ce wrapper suppose la séquence d'émission de
 * {@code OWRendererUtils} — {@code addVertex(Matrix4f, x, y, 0)} puis {@code setColor},
 * {@code setUv}, {@code setOverlay}, {@code setUv2} et enfin {@code setNormal}, quatre sommets
 * par quad. C'est {@code setNormal} qui clôt un sommet, et le 4ᵉ sommet déclenche l'émission.
 * Le repère attendu est celui de la bannière : X ∈ [0, 55] (largeur), Y ∈ [0, 93] (hauteur, la
 * hampe étant en Y = 93), Z = 0 (plan de la toile).</p>
 *
 * <p>Purement client et déterministe : l'onde ne dépend que du temps d'animation de l'entité et
 * de son identifiant. Rien n'est synchronisé, chaque client anime le drapeau localement — le
 * rendu est donc identique en solo et en multijoueur.</p>
 */
public final class OWWavingBannerConsumer implements VertexConsumer {

    /** Découpe maximale d'un quad, par axe (garde-fou sur les très grands aplats). */
    private static final int MAX_STEPS = 20;

    private final VertexConsumer parent;

    // Paramètres d'onde, en unités bannière (55 × 93).
    private final float amplitude;
    private final float phase;
    private final float frequency;
    private final float step;

    // Ligne moyenne de la toile : décalages cumulés aux nœuds de la chaîne articulée, du côté
    // hampe (indice 0, toujours nul) vers l'extrémité libre. Voir OWTribeFlagLayer.FlagState.
    private final float[] offX, offY, offZ;
    private final float segmentLength;

    // Tampon des 4 sommets du quad en cours.
    private final float[] vx = new float[4], vy = new float[4];
    private final int[] cr = new int[4], cg = new int[4], cb = new int[4], ca = new int[4];
    private final float[] tu = new float[4], tv = new float[4];
    private int u1u, u1v, u2u, u2v;
    private float nx, ny, nz;
    private Matrix4f mat = new Matrix4f();
    private int count;

    /**
     * @param parent    tampon de destination
     * @param amplitude débattement maximal de l'extrémité libre, en unités bannière
     * @param phase     phase temporelle de l'onde (croissante dans le temps)
     * @param step      pas de tesselation visé, en unités bannière
     * @param offX      décalage vertical cumulé de la ligne moyenne, par nœud (unités bannière)
     * @param offY      raccourcissement cumulé vers la hampe, par nœud
     * @param offZ      décalage latéral cumulé, par nœud
     */
    public OWWavingBannerConsumer(VertexConsumer parent, float amplitude, float phase, float frequency,
                                  float step, float[] offX, float[] offY, float[] offZ) {
        this.parent = parent;
        this.amplitude = amplitude;
        this.phase = phase;
        this.frequency = frequency;
        this.step = Math.max(step, 0.5f);
        this.offX = offX;
        this.offY = offY;
        this.offZ = offZ;
        this.segmentLength = OWTeamBannerShape.BASE_H / (float) (offX.length - 1);
    }

    // ── Déformation ──────────────────────────────────────────────────────────────

    /**
     * Déplace un point de la toile. {@code out} reçoit la position déformée (x, y, z).
     *
     * @param bx abscisse dans la bannière (0 → 55, soit de haut en bas une fois posée)
     * @param by ordonnée dans la bannière (0 = extrémité libre, 93 = hampe)
     */
    private void wave(float bx, float by, float[] out) {
        // u : 0 sur la hampe → 1 à l'extrémité libre. t : position transversale, centrée.
        float u = Mth.clamp(1f - by / OWTeamBannerShape.BASE_H, 0f, 1f);
        float t = bx / OWTeamBannerShape.BASE_W - 0.5f;

        // Le tissu est cloué sur la hampe : l'amplitude ne décolle qu'en s'en éloignant.
        float amp = amplitude * u * u;

        // Fréquences volontairement non multiples : la somme ne boucle pas, l'ondulation
        // ne « bat » jamais deux fois de la même façon.
        float w = Mth.sin(u * 7.1f * frequency - phase)
                + 0.42f * Mth.sin(u * 12.7f * frequency - phase * 1.63f + t * 2.4f)
                + 0.17f * Mth.sin(u * 21.3f * frequency - phase * 2.29f - t * 3.9f);
        w *= 0.62f; // renormalise la somme des trois harmoniques dans [-1, 1]

        float z = amp * w;

        // Le tissu est inextensible : ce qu'il gagne en ondulation, il le perd en portée.
        // (+by rapproche de la hampe, celle-ci étant en by = 93.)
        float pull = 1.15f * u * (amp * amp) / OWTeamBannerShape.BASE_H;

        // Ligne moyenne : virage et affaissement, lus le long de la chaîne articulée. L'ondulation
        // ci-dessus vient simplement s'y superposer, comme un pli sur un tissu déjà orienté.
        float d = u * OWTeamBannerShape.BASE_H / segmentLength;
        int i = Mth.clamp((int) d, 0, offX.length - 2);
        float f = Mth.clamp(d - i, 0f, 1f);

        // +bx descend vers le bas du monde (la bannière est posée sur le flanc), +by rapproche
        // de la hampe.
        out[0] = bx + Mth.lerp(f, offX[i], offX[i + 1]);
        out[1] = by + pull + Mth.lerp(f, offY[i], offY[i + 1]);
        out[2] = z + Mth.lerp(f, offZ[i], offZ[i + 1]);
    }

    // ── Tesselation ──────────────────────────────────────────────────────────────

    private final float[] pos = new float[3];

    /**
     * Découpe le quad tamponné en grille et pousse chaque sous-quad déformé vers {@link #parent}.
     * Les sommets arrivent dans l'ordre bas-gauche, bas-droite, haut-droite, haut-gauche : le
     * paramètre {@code a} parcourt le bord bas (v0 → v1), {@code b} monte vers le bord haut.
     */
    private void emitQuad() {
        float wA = Math.abs(vx[1] - vx[0]) + Math.abs(vy[1] - vy[0]);
        float wB = Math.abs(vx[3] - vx[0]) + Math.abs(vy[3] - vy[0]);
        int n = Mth.clamp(Mth.ceil(wA / step), 1, MAX_STEPS);
        int m = Mth.clamp(Mth.ceil(wB / step), 1, MAX_STEPS);

        for (int i = 0; i < n; i++) {
            float a0 = (float) i / n, a1 = (float) (i + 1) / n;
            for (int j = 0; j < m; j++) {
                float b0 = (float) j / m, b1 = (float) (j + 1) / m;
                emitCorner(a0, b0);
                emitCorner(a1, b0);
                emitCorner(a1, b1);
                emitCorner(a0, b1);
            }
        }
    }

    /** Interpole bilinéairement le sommet (a, b) du quad tamponné, le déforme et l'émet. */
    private void emitCorner(float a, float b) {
        float x = bilinear(vx[0], vx[1], vx[2], vx[3], a, b);
        float y = bilinear(vy[0], vy[1], vy[2], vy[3], a, b);
        wave(x, y, pos);

        parent.addVertex(mat, pos[0], pos[1], pos[2])
                .setColor(
                        Math.round(bilinear(cr[0], cr[1], cr[2], cr[3], a, b)),
                        Math.round(bilinear(cg[0], cg[1], cg[2], cg[3], a, b)),
                        Math.round(bilinear(cb[0], cb[1], cb[2], cb[3], a, b)),
                        Math.round(bilinear(ca[0], ca[1], ca[2], ca[3], a, b)))
                .setUv(bilinear(tu[0], tu[1], tu[2], tu[3], a, b),
                       bilinear(tv[0], tv[1], tv[2], tv[3], a, b))
                .setUv1(u1u, u1v)
                .setUv2(u2u, u2v)
                .setNormal(nx, ny, nz);
    }

    /** Interpolation bilinéaire sur les 4 coins (ordre bas-gauche, bas-droite, haut-droite, haut-gauche). */
    private static float bilinear(float q0, float q1, float q2, float q3, float a, float b) {
        return Mth.lerp(b, Mth.lerp(a, q0, q1), Mth.lerp(a, q3, q2));
    }

    // ── VertexConsumer ───────────────────────────────────────────────────────────

    @Override
    public VertexConsumer addVertex(Matrix4f pose, float x, float y, float z) {
        // On intercepte la variante « matricée » pour conserver les coordonnées locales de la
        // bannière : c'est sur elles, et non sur le résultat transformé, que l'onde s'applique.
        this.mat = pose;
        if (count < 4) {
            vx[count] = x;
            vy[count] = y;
        }
        return this;
    }

    @Override
    public VertexConsumer addVertex(float x, float y, float z) {
        return addVertex(this.mat, x, y, z);
    }

    @Override
    public VertexConsumer setColor(int r, int g, int b, int a) {
        if (count < 4) {
            cr[count] = r; cg[count] = g; cb[count] = b; ca[count] = a;
        }
        return this;
    }

    @Override
    public VertexConsumer setUv(float u, float v) {
        if (count < 4) {
            tu[count] = u; tv[count] = v;
        }
        return this;
    }

    @Override
    public VertexConsumer setUv1(int u, int v) {
        this.u1u = u; this.u1v = v;
        return this;
    }

    @Override
    public VertexConsumer setUv2(int u, int v) {
        this.u2u = u; this.u2v = v;
        return this;
    }

    @Override
    public VertexConsumer setNormal(float x, float y, float z) {
        // Dernier attribut d'un sommet : il clôt celui-ci, et le 4ᵉ referme le quad.
        this.nx = x; this.ny = y; this.nz = z;
        if (++count == 4) {
            emitQuad();
            count = 0;
        }
        return this;
    }

    @Override
    public VertexConsumer misc(VertexFormatElement element, int... values) {
        parent.misc(element, values);
        return this;
    }
}
