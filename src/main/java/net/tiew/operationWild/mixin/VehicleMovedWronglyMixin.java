package net.tiew.operationWild.mixin;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.tiew.operationWild.entity.OWEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

/**
 * Corrige le rubber-band des montures OWEntity ("(vehicle of X) moved wrongly!").
 *
 * Dans {@code handleMoveVehicle}, le serveur rejoue le deplacement reclame par le client via
 * {@code entity.move(MoverType.PLAYER, ...)} puis compare la position HORIZONTALE obtenue a
 * celle reclamee : si l'ecart au carre depasse 0.0625 (~0.25 bloc), il considere que le
 * vehicule "moved wrongly", REVERT a la position d'avant et renvoie un paquet de resync. Lors
 * d'un step-up brutal (descendre dans un trou d'1 bloc puis remonter aussitot sur un bloc plus
 * haut en face), le client grimpe alors que le replay serveur reste court -> ecart > seuil ->
 * revert -> et comme le joueur pousse encore, ca se rejoue chaque tick : rubber-band en boucle.
 *
 * Nos montures sont pilotees par le client (isControlledByLocalInstance cote client), donc on
 * fait du client l'autorite sur leur position : on neutralise UNIQUEMENT ce test de divergence
 * (on rend son seuil enorme) pour les OWEntity. La detection de collision reelle (les deux
 * autres 0.0625, qui sont des {@code deflate()} de boite) n'est PAS touchee (ordinal cible = 1).
 */
@Mixin(ServerGamePacketListenerImpl.class)
public abstract class VehicleMovedWronglyMixin {

    @Shadow
    public ServerPlayer player;

    @ModifyConstant(
            method = "handleMoveVehicle",
            constant = @Constant(doubleValue = 0.0625, ordinal = 1)
    )
    private double ow$relaxVehicleMovedWrongly(double original) {
        if (this.player != null && this.player.getRootVehicle() instanceof OWEntity) {
            // Seuil enorme => "d10 > seuil" toujours faux => pas de "moved wrongly"/revert.
            return Double.MAX_VALUE;
        }
        return original;
    }
}
