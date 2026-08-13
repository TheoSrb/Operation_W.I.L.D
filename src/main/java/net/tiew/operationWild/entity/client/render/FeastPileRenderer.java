package net.tiew.operationWild.entity.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.tiew.operationWild.entity.animals.terrestrial.RedPandaEntity;
import net.tiew.operationWild.entity.attacks.OWAttacksConstants;
import net.tiew.operationWild.entity.misc.FeastPileEntity;

import java.util.HashMap;
import java.util.Map;

/**
 * Le festin, etale sur toute sa nappe.
 *
 * <p>Un tas unique au pied du panda ne disait rien de la portee et ne ressemblait a rien. La
 * nourriture couvre desormais tout le rayon, avec un creux au centre pour laisser la place aux
 * convives, et chaque petit tas est <b>descendu sur le sol qu'il surplombe</b> : le festin epouse le
 * terrain au lieu de flotter en nappe plate au-dessus d'un talus.</p>
 *
 * <p>Les sondages de sol sont faits une fois puis gardes : l'entite ne bouge plus, ses emplacements
 * non plus. Les refaire a chaque image aurait coute vingt-deux lancers de rayon par festin et par
 * image, pour un resultat rigoureusement identique.</p>
 */
public class FeastPileRenderer extends EntityRenderer<FeastPileEntity> {

    private static final float ITEM_SCALE = 0.7f;
    private static final int GROUPS_PER_SPOT = 3;
    private static final double ARRIVAL_ORIGIN_HEIGHT = 1.35;
    private static final double ARRIVAL_ARC = 0.55;
    private static final float ARRIVAL_START_SCALE = 0.12f;

    private static final Map<Integer, Layout> LAYOUTS = new HashMap<>();

    private record Spot(double x, double y, double z, float spin, float tilt, int snack, float ratio) {
    }

    /**
     * Le releve garde son origine.
     *
     * <p>Le jeu recycle les identifiants d'entite : sans cette verification, un festin dresse la ou
     * un autre venait de finir aurait herite de ses hauteurs de sol et flotte au-dessus du terrain.</p>
     */
    private record Layout(BlockPos origin, Spot[] spots) {
    }

    private final ItemRenderer itemRenderer;

    public FeastPileRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.itemRenderer = context.getItemRenderer();
    }

    @Override
    public void render(FeastPileEntity pile, float yaw, float partialTick, PoseStack pose,
                       MultiBufferSource buffers, int light) {
        Spot[] spots = layoutFor(pile);
        int total = spots.length;

        // La nourriture disparait au rythme ou elle est mangee : les emplacements s'eteignent du
        // centre vers le bord, comme une tablee qu'on desert.
        int shown = Mth.ceil(total * (pile.portions() / (float) pile.portionsMax()));
        if (shown <= 0) return;

        float age = pile.tickCount + partialTick;
        Vec3 origin = throwOrigin(pile, partialTick);

        for (int i = 0; i < Math.min(shown, total); i++) {
            Spot spot = spots[i];

            // Vol d'arrivee : la piece part du panda, grandit et se pose. Le retard croit avec la
            // distance au centre, ce qui deroule la nappe vers l'exterieur au lieu de la faire
            // apparaitre d'un bloc.
            float landing = Mth.clamp(
                    (age - spot.ratio() * OWAttacksConstants.RedPanda.FEAST_ARRIVAL_SPREAD)
                            / OWAttacksConstants.RedPanda.FEAST_ARRIVAL_TICKS, 0f, 1f);
            if (landing <= 0f) continue;

            float eased = 1f - (1f - landing) * (1f - landing) * (1f - landing);
            float grow = ARRIVAL_START_SCALE + (1f - ARRIVAL_START_SCALE) * eased;

            pose.pushPose();
            pose.translate(
                    Mth.lerp(eased, origin.x, spot.x()),
                    Mth.lerp(eased, origin.y, spot.y())
                            + ARRIVAL_ARC * Mth.sin(eased * Mth.PI) * (1f - eased),
                    Mth.lerp(eased, origin.z, spot.z()));

            // Culbute pendant le vol, nulle une fois posee : sans elle les pieces glissaient a plat.
            pose.mulPose(Axis.XP.rotationDegrees((1f - eased) * 260f * (1f + spot.ratio())));
            pose.scale(grow, grow, grow);

            for (int layer = 0; layer < GROUPS_PER_SPOT; layer++) {
                pose.pushPose();
                // Les pieces d'un meme emplacement s'ecartent surtout DE COTE : empilees a la
                // verticale, cinquante-cinq emplacements auraient donne autant de petites tours au
                // lieu d'une nourriture repandue.
                double swirl = layer * 1.9;
                pose.translate(Math.cos(swirl) * 0.22, layer * 0.028, Math.sin(swirl) * 0.22);
                pose.mulPose(Axis.YP.rotationDegrees(spot.spin() + layer * 63f));
                pose.mulPose(Axis.XP.rotationDegrees(90f + spot.tilt() + layer * 9f));
                pose.translate(0, 0, Mth.sin(age * 0.07f + i + layer) * 0.008);
                pose.scale(ITEM_SCALE, ITEM_SCALE, ITEM_SCALE);

                ItemStack snack = RedPandaEntity.snackForIndex(spot.snack() + layer);
                this.itemRenderer.renderStatic(snack, ItemDisplayContext.GROUND,
                        lightAt(pile.level(), pile, spot), OverlayTexture.NO_OVERLAY,
                        pose, buffers, pile.level(), pile.getId() + i * 7 + layer);
                pose.popPose();
            }

            pose.popPose();
        }

        super.render(pile, yaw, partialTick, pose, buffers, light);
    }

    /**
     * D'ou part la nourriture, exprime dans le repere du tas.
     *
     * <p>Des pattes du panda quand on le retrouve, du centre du tas a defaut — un festin dresse par
     * une bete deja disparue ou hors de portee de suivi ne doit pas voir sa nourriture jaillir du
     * neant a l'origine du monde.</p>
     */
    private static Vec3 throwOrigin(FeastPileEntity pile, float partialTick) {
        Entity owner = pile.level().getEntity(pile.ownerId());
        if (owner == null) return new Vec3(0, ARRIVAL_ORIGIN_HEIGHT, 0);

        Vec3 paws = owner.getPosition(partialTick).add(0, owner.getBbHeight() * 0.7, 0);
        return paws.subtract(pile.getPosition(partialTick));
    }

    private static int lightAt(Level level, FeastPileEntity pile, Spot spot) {
        BlockPos at = BlockPos.containing(pile.getX() + spot.x(), pile.getY() + spot.y() + 0.1,
                pile.getZ() + spot.z());
        return LightTexture.pack(level.getBrightness(LightLayer.BLOCK, at),
                level.getBrightness(LightLayer.SKY, at));
    }

    /**
     * Emplacements tires une fois pour toutes, en spirale d'or.
     *
     * <p>L'angle d'or repartit les points sans les aligner ni les agglutiner, ce qu'un tirage
     * purement aleatoire ne garantit pas : sur vingt-deux tas, il laisse toujours des trous beants a
     * cote de paquets serres. Le rayon suit une racine carree pour que la densite reste egale du
     * centre au bord, au lieu de s'entasser au milieu.</p>
     */
    private static Spot[] layoutFor(FeastPileEntity pile) {
        BlockPos origin = pile.blockPosition();
        Layout cached = LAYOUTS.get(pile.getId());
        if (cached != null && cached.origin().equals(origin)) return cached.spots();

        double reach = pile.radius();

        // Le nombre d'emplacements decoule de la SURFACE couverte, pas d'une constante : une nappe
        // elargie par la Grande Tablee se garnit d'autant.
        int count = Mth.clamp(
                (int) Math.round(OWAttacksConstants.RedPanda.FEAST_SPOT_DENSITY * Math.PI * reach * reach),
                OWAttacksConstants.RedPanda.FEAST_SPOTS_MIN,
                OWAttacksConstants.RedPanda.FEAST_SPOTS_MAX);

        double inner = OWAttacksConstants.RedPanda.FEAST_SPREAD_INNER;
        double outer = OWAttacksConstants.RedPanda.FEAST_SPREAD_OUTER;

        RandomSource jitter = RandomSource.create(pile.getId() * 8191L);
        Spot[] spots = new Spot[count];
        double golden = Math.PI * (3.0 - Math.sqrt(5.0));

        for (int i = 0; i < count; i++) {
            // La spirale d'or sert de trame, le desordre est ajoute par-dessus : elle garantit
            // qu'aucun trou beant ne subsiste, le bruit efface le motif qu'elle laisserait voir sur
            // cent dix points.
            double share = (i + 0.5) / count;
            double ratio = inner + (outer - inner) * Math.sqrt(share)
                    + (jitter.nextDouble() - 0.5) * 0.14;
            double angle = i * golden + (jitter.nextDouble() - 0.5) * 1.4;

            double clamped = Mth.clamp(ratio, inner * 0.5, outer);
            double x = Math.cos(angle) * clamped * reach;
            double z = Math.sin(angle) * clamped * reach;

            spots[i] = new Spot(x, groundOffset(pile, x, z), z,
                    jitter.nextFloat() * 360f, (jitter.nextFloat() - 0.5f) * 30f, i,
                    (float) (clamped / outer));
        }

        LAYOUTS.put(pile.getId(), new Layout(origin, spots));
        if (LAYOUTS.size() > 32) LAYOUTS.keySet().removeIf(id -> pile.level().getEntity(id) == null);
        return spots;
    }

    private static double groundOffset(FeastPileEntity pile, double x, double z) {
        Vec3 from = new Vec3(pile.getX() + x, pile.getY() + 3.0, pile.getZ() + z);
        Vec3 to = new Vec3(pile.getX() + x, pile.getY() - 5.0, pile.getZ() + z);

        BlockHitResult hit = pile.level().clip(new ClipContext(
                from, to, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, pile));

        if (hit.getType() == HitResult.Type.MISS) return 0.0;
        return hit.getLocation().y - pile.getY() + 0.02;
    }

    @Override
    public ResourceLocation getTextureLocation(FeastPileEntity pile) {
        return TextureAtlas.LOCATION_BLOCKS;
    }
}
