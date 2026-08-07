package net.tiew.operationWild.networking.packets.to_server;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.tiew.operationWild.OperationWild;
import net.tiew.operationWild.entity.OWEntity;
import net.tiew.operationWild.entity.animals.aquatic.CrocodileEntity;
import net.tiew.operationWild.entity.attacks.OWAttack;
import net.tiew.operationWild.entity.attacks.OWChargedAttack;

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

            if (packet.action() == ACTION_TRIGGER_DEATH_ROLL) {
                // Le délai est vérifié côté serveur : le client en applique un de son côté, mais
                // lui seul ne protège de rien face à un envoi répété.
                if (entity instanceof CrocodileEntity croc) croc.startDeathRoll();
                return;
            }

            switch (packet.action()) {
                case ACTION_EXECUTE -> {
                    // Boa — toggle Crochets Venimeux : autorisé à tout moment (même pendant un combo)
                    if (packet.attackId() == 9) {
                        if (entity instanceof net.tiew.operationWild.entity.animals.terrestrial.BoaEntity boa)
                            boa.toggleVenomFangs();
                        return;
                    }
                    if (entity.isCombo()) return;
                    switch (packet.attackId()) {
                        case 2 -> {
                            if (entity instanceof net.tiew.operationWild.entity.animals.terrestrial.TigerEntity tiger)
                                tiger.activateShadowStrike();
                        }
                        case 4 -> {
                            if (entity instanceof CrocodileEntity croc)
                                croc.activatePrimalDive();
                        }
                        case 6 -> {
                            if (entity instanceof net.tiew.operationWild.entity.animals.terrestrial.KodiakEntity kodiak)
                                kodiak.activateUltimateNap();
                        }
                        case 8 -> {
                            if (entity instanceof net.tiew.operationWild.entity.animals.aquatic.OrcaEntity orca)
                                orca.activateBigMouth();
                        }
                        case 10 -> {
                            if (entity instanceof net.tiew.operationWild.entity.animals.terrestrial.BoaEntity boa)
                                boa.activateConstrictUltimate();
                        }
                        case 12 -> {
                            if (entity instanceof net.tiew.operationWild.entity.animals.terrestrial.KangarooEntity kangaroo)
                                kangaroo.activateTelluricStomp();
                        }
                        case 14 -> {
                            if (entity instanceof net.tiew.operationWild.entity.animals.terrestrial.ElephantEntity elephant)
                                elephant.activateEarthquake();
                        }
                    }
                }

                case ACTION_EXECUTE_WITH_TARGET -> {
                    switch (packet.attackId()) {
                        case 4 -> {
                            if (entity instanceof CrocodileEntity croc) {
                                int targetId = Float.floatToRawIntBits(packet.value());
                                croc.executePrimalDive(targetId);
                            }
                        }
                        case 10 -> {
                            if (entity instanceof net.tiew.operationWild.entity.animals.terrestrial.BoaEntity boa) {
                                int targetId = Float.floatToRawIntBits(packet.value());
                                boa.executeConstrictUltimate(targetId);
                            }
                        }
                    }
                }

                case ACTION_CHARGE_START -> {
                    // Kangourou — Tornade de Poings : démarrage du maintien (autorisé hors combo).
                    if (packet.attackId() == 11) {
                        if (!entity.isCombo()
                                && entity instanceof net.tiew.operationWild.entity.animals.terrestrial.KangarooEntity kangaroo)
                            kangaroo.startWhirlwind();
                        return;
                    }
                    if (entity.isCombo()) return;
                    entity.isChargingAttack = true;
                    switch (packet.attackId()) {
                        case 1 -> {
                            if (entity instanceof net.tiew.operationWild.entity.animals.terrestrial.TigerEntity tiger)
                                tiger.startJumpCharge();
                        }
                        case 5 -> {
                            if (entity instanceof net.tiew.operationWild.entity.animals.terrestrial.KodiakEntity kodiak)
                                kodiak.startPawSlamCharge();
                        }
                        case 3 -> {
                            if (entity instanceof net.tiew.operationWild.entity.animals.aquatic.CrocodileEntity croc) {
                                croc.startMouthSlamCharge();
                            }
                        }
                    }
                }

                case ACTION_CHARGE_CANCEL -> {
                    // Kangourou — Tornade de Poings : relâchement du maintien.
                    if (packet.attackId() == 11) {
                        if (entity instanceof net.tiew.operationWild.entity.animals.terrestrial.KangarooEntity kangaroo)
                            kangaroo.stopWhirlwind();
                        return;
                    }
                    entity.isChargingAttack = false;
                    switch (packet.attackId()) {
                        case 1 -> {
                            if (entity instanceof net.tiew.operationWild.entity.animals.terrestrial.TigerEntity tiger)
                                tiger.cancelJumpCharge();
                        }
                        case 5 -> {
                            if (entity instanceof net.tiew.operationWild.entity.animals.terrestrial.KodiakEntity kodiak)
                                kodiak.cancelPawSlamCharge();
                        }
                        case 3 -> {
                            if (entity instanceof net.tiew.operationWild.entity.animals.aquatic.CrocodileEntity croc) {
                                croc.cancelMouthSlamCharge();
                            }
                        }
                        case 13 -> {
                            if (entity instanceof net.tiew.operationWild.entity.animals.terrestrial.ElephantEntity elephant)
                                elephant.cancelShoulderBash();
                        }
                    }
                }

                case ACTION_CHARGE_RELEASE -> {
                    entity.isChargingAttack = false;
                    switch (packet.attackId()) {
                        case 1 -> {
                            if (entity instanceof net.tiew.operationWild.entity.animals.terrestrial.TigerEntity tiger)
                                tiger.performJumpAttack(packet.value());
                        }
                        case 5 -> {
                            if (entity instanceof net.tiew.operationWild.entity.animals.terrestrial.KodiakEntity kodiak)
                                kodiak.performPawSlam(packet.value());
                        }
                        case 3 -> {
                            if (entity instanceof net.tiew.operationWild.entity.animals.aquatic.CrocodileEntity croc) {
                                croc.performMouthSlam(packet.value());
                                croc.setPlayerMouthCharging(false);
                            }
                        }
                        case 7 -> {
                            if (entity instanceof net.tiew.operationWild.entity.animals.aquatic.OrcaEntity orca)
                                orca.performOrcaDash();
                        }
                        case 13 -> {
                            if (entity instanceof net.tiew.operationWild.entity.animals.terrestrial.ElephantEntity elephant)
                                elephant.performShoulderBash();
                        }
                    }
                }
            }
        });
    }
}
