package net.tiew.operationWild.entity.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.tiew.operationWild.client.OWRiderSmoothing;
import net.tiew.operationWild.entity.OWEntity;
import net.tiew.operationWild.entity.animals.terrestrial.RedPandaEntity;
import net.tiew.operationWild.event.ClientEvents;
import org.joml.Quaternionf;
import net.tiew.operationWild.entity.client.layer.RedPandaLayer;
import net.tiew.operationWild.entity.client.layer.skins.RedPandaSkinRenderLayer;
import net.tiew.operationWild.entity.client.model.RedPandaModel;
import net.tiew.operationWild.entity.client.skin.RedPandaSkin;
import net.tiew.operationWild.entity.client.skin.SkinRegistry;

import java.util.HashMap;
import java.util.Map;

public class RedPandaRenderer extends OWEntityRenderer<RedPandaEntity, RedPandaModel<RedPandaEntity>> {

    private final EntityRendererProvider.Context context;
    private final Map<ModelLayerLocation, RedPandaModel<RedPandaEntity>> modelCache = new HashMap<>();

    private static final float FIRST_PERSON_SCALE = 0.41f;
    /** Avancé dans le champ, devant la ligne d'épaule. Négatif le renverrait derrière le regard. */
    private static final double FIRST_PERSON_FORWARD_PUSH = 0.06;
    /** Rapproché de l'axe du regard : à pleine largeur d'épaule il sortait par le bord de l'écran. */
    private static final double FIRST_PERSON_SIDE_PULL = 0.14;
    /** Remonté au niveau de l'œil : rapetissée, la bête retombait sous le champ de vision. */
    private static final double FIRST_PERSON_LIFT = 0.30;

    public RedPandaRenderer(EntityRendererProvider.Context context) {
        super(context, new RedPandaModel<>(context.bakeLayer(RedPandaModel.LAYER_LOCATION)), 0.3f);
        this.context = context;
        this.addLayer(new RedPandaLayer(this));
        this.addLayer(new RedPandaSkinRenderLayer(this, context));
    }

    @Override
    public ResourceLocation getTextureLocation(RedPandaEntity redPanda) {
        RedPandaSkin skin = SkinRegistry.RedPandaSkins.get(redPanda.getVariant());
        if (skin.getMode() == RedPandaSkin.Mode.OVERLAY) {
            return SkinRegistry.RedPandaSkins.get(redPanda.getInitialVariant()).getTexture();
        }
        return skin.getTexture();
    }

    @Override
    public void render(RedPandaEntity redPanda, float entityYaw, float partialTicks,
                       PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {

        RedPandaSkin skin = SkinRegistry.RedPandaSkins.get(redPanda.getVariant());
        this.model = skin.getMode() == RedPandaSkin.Mode.REPLACEMENT
                ? skin.getModelLayer().map(this::getOrBakeModel).orElse(getOrBakeModel(RedPandaModel.LAYER_LOCATION))
                : getOrBakeModel(RedPandaModel.LAYER_LOCATION);

        Player carrier = redPanda.getCarrier();
        if (carrier == null) {
            super.render(redPanda, entityYaw, partialTicks, poseStack, bufferSource, packedLight);
            return;
        }

        OWEntity mount = carrier.getVehicle() instanceof OWEntity ridden ? ridden : null;
        boolean tucked = isTuckedForFirstPerson(carrier);

        // Ordre imposé : tout ce qui translate se fait pendant que le repère est encore aligné sur
        // le monde, et l'orientation ne vient qu'en dernier. Tourner d'abord obligerait à exprimer
        // chaque décalage suivant dans un repère pivoté.
        poseStack.pushPose();
        if (tucked) cancelViewBob(redPanda, carrier, partialTicks, poseStack);

        float anchorYaw = shoulderAnchorYaw(carrier, partialTicks, tucked);
        Vec3 offset = applyShoulderAnchor(redPanda, carrier, partialTicks, poseStack, anchorYaw);

        if (mount != null) applyCarrierRidingTransform(carrier, mount, partialTicks, poseStack, tucked, offset);
        if (tucked) applyFirstPersonTuck(partialTicks, poseStack, anchorYaw);
        applyShoulderFacing(redPanda, partialTicks, poseStack, anchorYaw);

        super.render(redPanda, entityYaw, partialTicks, poseStack, bufferSource, packedLight);
        poseStack.popPose();
    }

    /**
     * Vrai uniquement pour l'œil de celui qui porte la bête, et seulement en vue subjective.
     *
     * <p>La caméra est alors logée dans la tête du porteur, à quelques centimètres d'un animal assis
     * sur son épaule : à taille réelle il barre le quart de l'écran. Le rétrécissement est donc
     * purement local — les autres joueurs, et le porteur lui-même dès qu'il recule la caméra, voient
     * un panda roux de taille normale.</p>
     */
    private boolean isTuckedForFirstPerson(Player carrier) {
        if (carrier == null) return false;
        Minecraft minecraft = Minecraft.getInstance();
        return carrier == minecraft.player && minecraft.options.getCameraType().isFirstPerson();
    }

    /**
     * Soustrait la bête au balancement de vue.
     *
     * <p>Minecraft fait tanguer la <b>totalité</b> du monde à chaque pas — une translation et deux
     * rotations posées sur la matrice de vue avant l'orientation de la caméra. C'est imperceptible
     * sur un paysage à cinquante blocs et énorme sur un animal assis à cinquante centimètres de
     * l'œil : d'où une bête qui semblait s'agiter alors qu'elle ne bougeait pas d'un pouce.</p>
     *
     * <p>La chaîne de vue vaut {@code Bob · Rotation · Translation(-caméra)}. Pour en retirer le
     * seul balancement sur cet objet, on ramène le repère sur la caméra, on y annule le tangage dans
     * l'ordre inverse de sa pose, puis on revient. Le reste du monde continue de tanguer : seul le
     * passager est tenu immobile, comme l'est déjà l'objet en main.</p>
     */
    private void cancelViewBob(RedPandaEntity redPanda, Player carrier, float partialTicks, PoseStack poseStack) {
        Minecraft minecraft = Minecraft.getInstance();
        if (!minecraft.options.bobView().get()) return;

        float walked = carrier.walkDist - carrier.walkDistO;
        float phase = -(carrier.walkDist + walked * partialTicks);
        float amount = Mth.lerp(partialTicks, carrier.oBob, carrier.bob);
        if (amount == 0f) return;

        float bobX = Mth.sin(phase * Mth.PI) * amount * 0.5f;
        float bobY = -Math.abs(Mth.cos(phase * Mth.PI) * amount);
        float rollDeg = Mth.sin(phase * Mth.PI) * amount * 3.0f;
        float pitchDeg = Math.abs(Mth.cos(phase * Mth.PI - 0.2f) * amount) * 5.0f;

        net.minecraft.client.Camera camera = minecraft.gameRenderer.getMainCamera();
        Quaternionf view = com.mojang.math.Axis.XP.rotationDegrees(camera.getXRot());
        view.mul(com.mojang.math.Axis.YP.rotationDegrees(camera.getYRot() + 180.0f));
        Quaternionf viewInverse = new Quaternionf(view).conjugate();

        Vec3 cam = camera.getPosition();
        double dx = Mth.lerp(partialTicks, redPanda.xo, redPanda.getX()) - cam.x;
        double dy = Mth.lerp(partialTicks, redPanda.yo, redPanda.getY()) - cam.y;
        double dz = Mth.lerp(partialTicks, redPanda.zo, redPanda.getZ()) - cam.z;

        poseStack.translate(-dx, -dy, -dz);
        poseStack.mulPose(viewInverse);
        poseStack.mulPose(com.mojang.math.Axis.XP.rotationDegrees(-pitchDeg));
        poseStack.mulPose(com.mojang.math.Axis.ZP.rotationDegrees(-rollDeg));
        poseStack.translate(-bobX, -bobY, 0.0);
        poseStack.mulPose(view);
        poseStack.translate(dx, dy, dz);
    }

    /**
     * Lacet auquel la place d'épaule est accrochée.
     *
     * <p>En vue subjective, celui de la CAMÉRA : elle suit la souris à chaque image, là où les
     * lacets de corps et de tête sont figés au tick. Ailleurs, le lacet de corps interpolé — celui
     * avec lequel le modèle du porteur est lui-même dessiné, donc le seul qui colle à son épaule
     * visible.</p>
     */
    private float shoulderAnchorYaw(Player carrier, float partialTicks, boolean firstPerson) {
        return firstPerson
                ? carrier.getViewYRot(partialTicks)
                : Mth.rotLerp(partialTicks, carrier.yBodyRotO, carrier.yBodyRot);
    }

    /**
     * Recalcule la place d'épaule à l'image plutôt que de se fier à celle posée au tick.
     *
     * <p>C'était la cause du flottement en vue extérieure. La position est fixée une fois par tick,
     * puis le moteur interpole EN LIGNE DROITE entre deux emplacements — or l'épaule, elle, décrit
     * un ARC quand le porteur pivote. La corde coupant à l'intérieur de l'arc, la bête se
     * rapprochait du visage au milieu de chaque virage pour y revenir ensuite, d'autant plus
     * nettement que la souris tournait vite.</p>
     *
     * <p>Renvoie l'écart réellement appliqué : la transformation de monte en a besoin pour pivoter
     * autour de l'origine du porteur, et le recalculer de son côté rouvrirait l'écart qu'on vient
     * de fermer.</p>
     */
    private Vec3 applyShoulderAnchor(RedPandaEntity redPanda, Player carrier, float partialTicks,
                                     PoseStack poseStack, float anchorYaw) {
        float crouch = Mth.lerp(partialTicks, redPanda.crouchAmountO, redPanda.crouchAmount);
        Vec3 offset = RedPandaEntity.shoulderOffset(anchorYaw, redPanda.getScale(), crouch);

        poseStack.translate(
                Mth.lerp(partialTicks, carrier.xo, carrier.getX()) + offset.x - Mth.lerp(partialTicks, redPanda.xo, redPanda.getX()),
                Mth.lerp(partialTicks, carrier.yo, carrier.getY()) + offset.y - Mth.lerp(partialTicks, redPanda.yo, redPanda.getY()),
                Mth.lerp(partialTicks, carrier.zo, carrier.getZ()) + offset.z - Mth.lerp(partialTicks, redPanda.zo, redPanda.getZ()));

        return offset;
    }

    /**
     * Oriente la bête sur le même lacet que sa place d'épaule, en tout dernier.
     *
     * <p>Le modèle est dessiné avec le lacet de corps du panda, recopié du porteur au tick : il
     * accusait donc le même retard que la position, et la bête paraissait mal tournée pendant les
     * virages. La correction est posée après toutes les translations — l'appliquer plus tôt
     * obligerait à exprimer chaque décalage suivant dans un repère pivoté.</p>
     */
    private void applyShoulderFacing(RedPandaEntity redPanda, float partialTicks, PoseStack poseStack, float anchorYaw) {
        float modelYaw = Mth.rotLerp(partialTicks, redPanda.yBodyRotO, redPanda.yBodyRot);
        poseStack.mulPose(com.mojang.math.Axis.YP.rotationDegrees(-Mth.wrapDegrees(anchorYaw - modelYaw)));

        // Bascule avec le dos qui se penche. Posée APRÈS le lacet, donc dans le repère déjà tourné :
        // la bête pique vers l'avant du porteur, quelle que soit son orientation. Sans elle, une
        // bête bien placée mais restée droite paraissait décollée de l'épaule inclinée.
        float crouch = Mth.lerp(partialTicks, redPanda.crouchAmountO, redPanda.crouchAmount);
        if (crouch > 0.01f) {
            poseStack.mulPose(com.mojang.math.Axis.XP.rotationDegrees(
                    RedPandaEntity.CROUCH_PITCH_DEGREES * crouch));
        }
    }

    /** Rapetissement et mise en retrait propres à la vue subjective. */
    private void applyFirstPersonTuck(float partialTicks, PoseStack poseStack, float anchorYaw) {
        float yawRad = anchorYaw * Mth.DEG_TO_RAD;

        // Décalages pris sur le repère du lacet d'ancrage : ils doivent tourner avec l'épaule,
        // sinon la bête glisserait dans le champ dès que le regard pivote.
        double inX = Mth.cos(yawRad) * FIRST_PERSON_SIDE_PULL;
        double inZ = Mth.sin(yawRad) * FIRST_PERSON_SIDE_PULL;
        double forwardX = -Mth.sin(yawRad) * FIRST_PERSON_FORWARD_PUSH;
        double forwardZ = Mth.cos(yawRad) * FIRST_PERSON_FORWARD_PUSH;

        poseStack.translate(inX + forwardX, FIRST_PERSON_LIFT, inZ + forwardZ);
        poseStack.scale(FIRST_PERSON_SCALE, FIRST_PERSON_SCALE, FIRST_PERSON_SCALE);
    }

    /**
     * Fait subir au panda roux exactement ce que subit son porteur en selle.
     *
     * <p>Le cavalier n'est pas rendu à sa position brute : le mod le recale sur l'os de selle animé
     * ({@code OWRiderSmoothing}) puis le fait basculer avec la bête ({@code riderBodyRotation}). Le
     * panda, lui, est une entité indépendante dessinée à sa propre place : sans ce rattrapage il
     * restait droit et décalé pendant que son porteur se couchait dans les virages.</p>
     *
     * <p>Les deux transformations sont <b>lues</b> aux mêmes sources que le cavalier, jamais
     * recalculées : elles sont mises en cache par image, donc les deux modèles ne peuvent pas
     * diverger. La rotation s'applique autour de l'origine du porteur, d'où l'aller-retour par
     * l'écart {@code d} — une rotation prise autour du panda lui-même le ferait pivoter sur place au
     * lieu de le faire tourner avec l'épaule.</p>
     *
     * <p>Reste hors de portée : le scarabée des mers et la victime tenue en gueule, dont le cavalier
     * pivote autour d'un point qui lui est propre. {@code riderBodyRotation} y renvoie {@code null},
     * et le panda garde alors son assiette.</p>
     *
     * <p><b>En vue subjective, la référence n'est plus le modèle mais la CAMÉRA</b>, et les deux ne
     * bougent pas ensemble : {@code CameraMixin} n'ajoute l'arc d'inclinaison à la vue que pour les
     * espèces qui déclarent {@code riderCameraFollowsBodyTilt()}, et jamais pendant une figure —
     * suivre un tonneau complet ferait décrire à l'œil un cercle d'un mètre et demi. Faire basculer
     * le panda quand la vue, elle, reste posée, l'aurait fait sortir du champ par le côté. Le
     * recalage de selle, lui, s'applique dans les deux cas : la caméra le reçoit toujours.</p>
     */
    private void applyCarrierRidingTransform(Player carrier, OWEntity mount, float partialTicks,
                                             PoseStack poseStack, boolean firstPerson, Vec3 offset) {
        double dx = offset.x, dy = offset.y, dz = offset.z;

        Vec3 seatFix = OWRiderSmoothing.seatCorrection(carrier, mount, partialTicks);

        // Appelée quoi qu'il arrive, même sans emploi : c'est cet appel qui arrête l'orientation du
        // cavalier pour l'image entière, contrat que le mod tient déjà depuis la caméra.
        Quaternionf bodyTilt = ClientEvents.riderBodyRotation(carrier, mount, partialTicks);
        boolean cameraFollowsTilt = mount.riderCameraFollowsBodyTilt() && !mount.isRollingFigure();
        Quaternionf rotation = (!firstPerson || cameraFollowsTilt) ? bodyTilt : null;

        poseStack.translate(-dx, -dy, -dz);
        if (seatFix != null) poseStack.translate(seatFix.x, seatFix.y, seatFix.z);
        if (rotation != null) poseStack.mulPose(rotation);
        poseStack.translate(dx, dy, dz);
    }

    private RedPandaModel<RedPandaEntity> getOrBakeModel(ModelLayerLocation layer) {
        return modelCache.computeIfAbsent(layer, l -> new RedPandaModel<>(context.bakeLayer(l)));
    }

    @Override
    public double distanceToShowRealInfos() {
        return 3;
    }

    @Override
    public double infosUpOffset() {
        return 0;
    }
}
