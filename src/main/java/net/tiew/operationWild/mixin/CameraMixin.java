package net.tiew.operationWild.mixin;

import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.phys.Vec3;
import net.tiew.operationWild.client.OWRiderSmoothing;
import net.tiew.operationWild.event.ClientEvents;
import net.tiew.operationWild.entity.OWEntity;
import net.tiew.operationWild.entity.animals.aquatic.OrcaEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Camera.class)
public abstract class CameraMixin {

    @Shadow protected abstract void setPosition(Vec3 pos);

    @Shadow public abstract Vec3 getPosition();

    @Shadow private float eyeHeight;

    @Shadow private float eyeHeightOld;

    @ModifyConstant(method = "setup", constant = @Constant(floatValue = 4.0F))
    private float modifyCameraDistance(float original) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null && mc.player.getVehicle() instanceof OWEntity) {
            return chooseFOVAnimal(mc.player.getVehicle());
        }
        return original;
    }

    private float chooseFOVAnimal(Entity rider) {
        return switch (rider) {
            case OrcaEntity e -> 8.0F;
            default -> 4.0F;
        };
    }

    /**
     * Pose la vue là où le cavalier est réellement DESSINÉ, et non là où le moteur l'a placé.
     *
     * <p>Vanilla assied la caméra sur la position d'entité interpolée entre deux ticks. Or le modèle
     * du cavalier, lui, est décalé à chaque image par {@code OWRiderSmoothing} pour rattraper le
     * retard de l'os qui le porte. Tant que ce rattrapage valait quelques centimètres — le seul
     * balancement de nage — l'écart passait inaperçu. Le roulis de virage et le tangage de l'orque
     * l'ont porté à plusieurs décimètres, et à la troisième personne le pivot de la caméra n'était
     * plus sur le joueur : il dérivait autour de lui dès qu'on virait en piquant.
     *
     * <p>On applique donc à la vue exactement la même translation qu'au corps. Elle est calculée une
     * seule fois par image et mise en cache : les deux lisent la même valeur, donc ils ne peuvent
     * plus se désolidariser.
     */
    @Inject(method = "setup", at = @At("TAIL"))
    private void owFollowDrawnSeat(BlockGetter level, Entity entity, boolean detached,
                                   boolean thirdPersonReverse, float partialTick, CallbackInfo ci) {
        OWRiderSmoothing.beginFrame();

        if (!(entity.getVehicle() instanceof OWEntity mount)) return;

        // Le calcul rejoue positionRider, qui écrit dans le lacet du corps du cavalier. Ce n'était
        // sans conséquence que parce qu'il tournait APRÈS l'instantané pris par RenderPlayerEvent.Pre
        // pour restauration. Depuis la caméra, il tourne avant : on lui rend sa valeur.
        float savedBodyYaw = entity instanceof LivingEntity living ? living.yBodyRot : 0f;
        Vec3 seatFix = OWRiderSmoothing.seatCorrection(entity, mount, partialTick);
        if (entity instanceof LivingEntity living) living.yBodyRot = savedBodyYaw;

        if (seatFix != null) this.setPosition(this.getPosition().add(seatFix));

        // Le corps du cavalier pivote autour de ses PIEDS : son oeil décrit donc un arc que la vue,
        // posée droit au-dessus de la position d'entité, ignorait. On la porte sur l'oeil réel.
        //
        // Première personne seule : à la troisième, ce point est le centre d'orbite de la caméra, et
        // le faire voyager balancerait tout le monde autour du joueur pour corriger un décalage que
        // l'on ne voit pas — le modèle, lui, est déjà au bon endroit.
        // L'appel a lieu dans les DEUX modes de vue, même quand son résultat ne sert pas : c'est lui
        // qui arrête l'orientation du cavalier pour l'image entière. Le modèle la relira, à la
        // troisième personne comme à la première, au lieu de la relever lui-même à un instant qui
        // dépend de l'ordre de rendu.
        if (entity instanceof Player player) {
            float eye = Mth.lerp(partialTick, this.eyeHeightOld, this.eyeHeight);
            Vec3 eyeArc = ClientEvents.riderEyeOffset(player, mount, eye, partialTick);
            // Suspendu pendant une figure : sur un tonneau complet, suivre l'arc de l'œil ferait
            // décrire à la vue un cercle d'un mètre et demi, sans la rotation qui le rendrait lisible.
            // Le corps tourne, la vue reste posée. L'écart abandonné à cet instant vaut le seul arc de
            // virage, minime sur une ruée en ligne droite.
            // L'appel ci-dessus a lieu quoi qu'il arrive : c'est lui qui arrête l'orientation du
            // cavalier pour l'image entière, ce dont le rendu du modèle dépend. Seule son APPLICATION
            // à la vue est facultative, espèce par espèce.
            if (!detached && mount.riderCameraFollowsBodyTilt() && !mount.isRollingFigure()) {
                this.setPosition(this.getPosition().add(eyeArc));
            }
        }
    }
}
