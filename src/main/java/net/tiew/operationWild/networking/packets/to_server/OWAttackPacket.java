package net.tiew.operationWild.networking.packets.to_server;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.tiew.operationWild.OperationWild;
import net.tiew.operationWild.entity.OWEntity;
import net.tiew.operationWild.entity.animals.aquatic.CrocodileEntity;
import net.tiew.operationWild.entity.attacks.OWAttack;
import net.tiew.operationWild.entity.attacks.OWAttackIds;
import net.tiew.operationWild.entity.attacks.OWChargedAttack;
import net.tiew.operationWild.networking.packets.to_client.OWAttackRejectedPacket;

import java.util.HashMap;
import java.util.Map;

/**
 * Universal attack packet — one packet covers all attack types.
 *
 * Actions:
 *   ACTION_EXECUTE        (0) — trigger an instant attack
 *   ACTION_CHARGE_START   (1) — player pressed the key, start charging
 *   ACTION_CHARGE_CANCEL  (2) — released too early, cancel
 *   ACTION_CHARGE_RELEASE (3) — released after min charge; value = chargeFactor [0..1]
 */
public record OWAttackPacket(int attackId, byte action, float value) implements CustomPacketPayload {

    public static final byte ACTION_EXECUTE              = 0;
    public static final byte ACTION_CHARGE_START         = 1;
    public static final byte ACTION_CHARGE_CANCEL        = 2;
    public static final byte ACTION_CHARGE_RELEASE       = 3;
    public static final byte ACTION_EXECUTE_WITH_TARGET  = 4;
    public static final byte ACTION_TRIGGER_DEATH_ROLL   = 5;

    public static final CustomPacketPayload.Type<OWAttackPacket> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "ow_attack"));

    public static final StreamCodec<FriendlyByteBuf, OWAttackPacket> STREAM_CODEC = StreamCodec.of(
            (buf, p) -> { buf.writeInt(p.attackId()); buf.writeByte(p.action()); buf.writeFloat(p.value()); },
            buf -> new OWAttackPacket(buf.readInt(), buf.readByte(), buf.readFloat())
    );

    private static final Map<Integer, OWAttack> REGISTRY = new HashMap<>();

    public static void register(OWAttack attack) {
        REGISTRY.put(attack.getId(), attack);
    }

    public static OWAttack getAttack(int id) {
        return REGISTRY.get(id);
    }

    public static void registerId(int id) {
        REGISTRY.putIfAbsent(id, null);
    }

    @Override
    public CustomPacketPayload.Type<OWAttackPacket> type() {
        return TYPE;
    }

    public static void handle(OWAttackPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player)) return;
            if (!(player.getRootVehicle() instanceof OWEntity entity)) return;
            if (entity.getPassengers().indexOf(player) != 0) return;
            // Le propriétaire OU tout membre de sa tribu doté du Contrôle peut piloter les attaques
            // de l'entité (cf. OWEntity#canPilotAttacks, garde commune client/serveur).
            if (!entity.canPilotAttacks(player)) return;

            // Geste exclusif en cours — panique, rugissement… : la bête n'accepte plus rien. Le refus
            // est net pour les ultimes — le client a déjà armé sa prédiction (recharge, son, jauge
            // grisée), il faut la lui faire défaire, sans quoi la capacité reste consommée pour rien.
            if (entity.isAttackLocked()) {
                if (OWAttackIds.isUltimate(packet.attackId())) {
                    PacketDistributor.sendToPlayer(player,
                            new OWAttackRejectedPacket(entity.getId(), packet.attackId()));
                }
                return;
            }

            if (packet.action() == ACTION_TRIGGER_DEATH_ROLL) {
                // Le délai est vérifié côté serveur : le client en applique un de son côté, mais
                // lui seul ne protège de rien face à un envoi répété.
                if (entity instanceof CrocodileEntity croc) croc.startDeathRoll();
                return;
            }

            switch (packet.action()) {
                case ACTION_EXECUTE -> {
                    // Boa — toggle Crochets Venimeux : autorisé à tout moment (même pendant un combo)
                    if (packet.attackId() == OWAttackIds.VENOM_FANGS) {
                        if (entity instanceof net.tiew.operationWild.entity.animals.terrestrial.BoaEntity boa)
                            boa.toggleVenomFangs();
                        return;
                    }

                    boolean ultimate = OWAttackIds.isUltimate(packet.attackId());

                    // Refus net plutôt que silencieux : le client a déjà armé sa prédiction, il
                    // faut la lui faire défaire (cf. OWAttackRejectedPacket plus bas).
                    if (ultimate && !entity.canUseUltimate()) {
                        PacketDistributor.sendToPlayer(player,
                                new OWAttackRejectedPacket(entity.getId(), packet.attackId()));
                        return;
                    }

                    // Un ultime l'emporte sur l'enchaînement en cours au lieu d'être jeté en
                    // silence : le client, dont la copie de l'état de combo arrive avec un tick de
                    // retard, croyait l'avoir lancé et en jouait déjà le son.
                    if (entity.isCombo()) {
                        if (!ultimate) return;
                        entity.resetCombo(0);
                        entity.actualAttackNumber = 0;
                    }

                    boolean started = switch (packet.attackId()) {
                        case OWAttackIds.SHADOW_STRIKE ->
                                entity instanceof net.tiew.operationWild.entity.animals.terrestrial.TigerEntity tiger
                                        && tiger.activateShadowStrike();
                        case OWAttackIds.PRIMAL_DIVE ->
                                entity instanceof CrocodileEntity croc && croc.activatePrimalDive();
                        case OWAttackIds.NAP_ULTIMATE ->
                                entity instanceof net.tiew.operationWild.entity.animals.terrestrial.KodiakEntity kodiak
                                        && kodiak.activateUltimateNap();
                        case OWAttackIds.BIG_MOUTH ->
                                entity instanceof net.tiew.operationWild.entity.animals.aquatic.OrcaEntity orca
                                        && orca.activateBigMouth();
                        case OWAttackIds.CONSTRICT_ULTIMATE ->
                                entity instanceof net.tiew.operationWild.entity.animals.terrestrial.BoaEntity boa
                                        && boa.activateConstrictUltimate();
                        case OWAttackIds.TELLURIC_STOMP ->
                                entity instanceof net.tiew.operationWild.entity.animals.terrestrial.KangarooEntity kangaroo
                                        && kangaroo.activateTelluricStomp();
                        case OWAttackIds.EARTHQUAKE ->
                                entity instanceof net.tiew.operationWild.entity.animals.terrestrial.ElephantEntity elephant
                                        && elephant.activateEarthquake();
                        default -> true;
                    };

                    // Le serveur reste seul juge de l'énergie et du déverrouillage : quand il refuse,
                    // le client doit défaire sa prédiction (temps de recharge, jauge de drain, effet
                    // de déclenchement) au lieu de laisser croire au joueur que le coup est parti.
                    if (!started && ultimate) {
                        PacketDistributor.sendToPlayer(player,
                                new OWAttackRejectedPacket(entity.getId(), packet.attackId()));
                    }
                }

                case ACTION_EXECUTE_WITH_TARGET -> {
                    switch (packet.attackId()) {
                        case OWAttackIds.PRIMAL_DIVE -> {
                            if (entity instanceof CrocodileEntity croc) {
                                int targetId = Float.floatToRawIntBits(packet.value());
                                croc.executePrimalDive(targetId);
                            }
                        }
                        case OWAttackIds.CONSTRICT_ULTIMATE -> {
                            if (entity instanceof net.tiew.operationWild.entity.animals.terrestrial.BoaEntity boa) {
                                int targetId = Float.floatToRawIntBits(packet.value());
                                boa.executeConstrictUltimate(targetId);
                            }
                        }
                    }
                }

                case ACTION_CHARGE_START -> {
                    // Éléphant — Jet de Trompe : ouverture du robinet (autorisée hors combo).
                    if (packet.attackId() == OWAttackIds.WATER_SPRAY) {
                        if (!entity.isCombo()
                                && entity instanceof net.tiew.operationWild.entity.animals.terrestrial.ElephantEntity elephant)
                            elephant.startTrunkAction();
                        return;
                    }
                    // Kangourou — Tornade de Poings : démarrage du maintien (autorisé hors combo).
                    if (packet.attackId() == OWAttackIds.WHIRLWIND_FISTS) {
                        if (!entity.isCombo()
                                && entity instanceof net.tiew.operationWild.entity.animals.terrestrial.KangarooEntity kangaroo)
                            kangaroo.startWhirlwind();
                        return;
                    }
                    if (entity.isCombo()) return;
                    entity.isChargingAttack = true;
                    switch (packet.attackId()) {
                        case OWAttackIds.PRIMAL_ROAR -> {
                            if (entity instanceof net.tiew.operationWild.entity.animals.terrestrial.TigerEntity tiger)
                                tiger.startRoarCharge();
                        }
                        case OWAttackIds.ATTACK_JUMP -> {
                            if (entity instanceof net.tiew.operationWild.entity.animals.terrestrial.TigerEntity tiger)
                                tiger.startJumpCharge();
                        }
                        case OWAttackIds.PAW_SLAM -> {
                            if (entity instanceof net.tiew.operationWild.entity.animals.terrestrial.KodiakEntity kodiak)
                                kodiak.startPawSlamCharge();
                        }
                        case OWAttackIds.MOUTH_SLAM -> {
                            if (entity instanceof net.tiew.operationWild.entity.animals.aquatic.CrocodileEntity croc) {
                                croc.startMouthSlamCharge();
                            }
                        }
                    }
                }

                case ACTION_CHARGE_CANCEL -> {
                    // Éléphant — Jet de Trompe : fermeture du robinet.
                    if (packet.attackId() == OWAttackIds.WATER_SPRAY) {
                        if (entity instanceof net.tiew.operationWild.entity.animals.terrestrial.ElephantEntity elephant)
                            elephant.stopTrunkAction();
                        return;
                    }
                    // Kangourou — Tornade de Poings : relâchement du maintien.
                    if (packet.attackId() == OWAttackIds.WHIRLWIND_FISTS) {
                        if (entity instanceof net.tiew.operationWild.entity.animals.terrestrial.KangarooEntity kangaroo)
                            kangaroo.stopWhirlwind();
                        return;
                    }
                    entity.isChargingAttack = false;
                    switch (packet.attackId()) {
                        case OWAttackIds.PRIMAL_ROAR -> {
                            if (entity instanceof net.tiew.operationWild.entity.animals.terrestrial.TigerEntity tiger)
                                tiger.cancelRoarCharge();
                        }
                        case OWAttackIds.ATTACK_JUMP -> {
                            if (entity instanceof net.tiew.operationWild.entity.animals.terrestrial.TigerEntity tiger)
                                tiger.cancelJumpCharge();
                        }
                        case OWAttackIds.PAW_SLAM -> {
                            if (entity instanceof net.tiew.operationWild.entity.animals.terrestrial.KodiakEntity kodiak)
                                kodiak.cancelPawSlamCharge();
                        }
                        case OWAttackIds.MOUTH_SLAM -> {
                            if (entity instanceof net.tiew.operationWild.entity.animals.aquatic.CrocodileEntity croc) {
                                croc.cancelMouthSlamCharge();
                            }
                        }
                        case OWAttackIds.SHOULDER_BASH -> {
                            if (entity instanceof net.tiew.operationWild.entity.animals.terrestrial.ElephantEntity elephant)
                                elephant.cancelShoulderBash();
                        }
                    }
                }

                case ACTION_CHARGE_RELEASE -> {
                    entity.isChargingAttack = false;
                    switch (packet.attackId()) {
                        case OWAttackIds.PRIMAL_ROAR -> {
                            if (entity instanceof net.tiew.operationWild.entity.animals.terrestrial.TigerEntity tiger)
                                tiger.performPrimalRoar();
                        }
                        case OWAttackIds.ATTACK_JUMP -> {
                            if (entity instanceof net.tiew.operationWild.entity.animals.terrestrial.TigerEntity tiger)
                                tiger.performJumpAttack(packet.value());
                        }
                        case OWAttackIds.PAW_SLAM -> {
                            if (entity instanceof net.tiew.operationWild.entity.animals.terrestrial.KodiakEntity kodiak)
                                kodiak.performPawSlam(packet.value());
                        }
                        case OWAttackIds.MOUTH_SLAM -> {
                            if (entity instanceof net.tiew.operationWild.entity.animals.aquatic.CrocodileEntity croc) {
                                croc.performMouthSlam(packet.value());
                                croc.setPlayerMouthCharging(false);
                            }
                        }
                        case OWAttackIds.TIDAL_RUSH -> {
                            if (entity instanceof net.tiew.operationWild.entity.animals.aquatic.OrcaEntity orca)
                                orca.performOrcaDash();
                        }
                        case OWAttackIds.SHOULDER_BASH -> {
                            if (entity instanceof net.tiew.operationWild.entity.animals.terrestrial.ElephantEntity elephant)
                                elephant.performShoulderBash();
                        }
                    }
                }
            }
        });
    }
}
