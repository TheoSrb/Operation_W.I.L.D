package net.tiew.operationWild.networking.packets.to_client;

// !! À enregistrer dans OWNetworkHandler (côté to_client) !!

import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.tiew.operationWild.OperationWild;
import net.tiew.operationWild.screen.entity.OWTeamsInterface;

public record OWEntityAlreadyInTeamPacket(String targetNickname, String currentTeamName)
        implements CustomPacketPayload {

    public static final Type<OWEntityAlreadyInTeamPacket> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "entity_already_in_team"));

    public static final StreamCodec<ByteBuf, OWEntityAlreadyInTeamPacket> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.STRING_UTF8, OWEntityAlreadyInTeamPacket::targetNickname,
                    ByteBufCodecs.STRING_UTF8, OWEntityAlreadyInTeamPacket::currentTeamName,
                    OWEntityAlreadyInTeamPacket::new);

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }

    public static void handle(OWEntityAlreadyInTeamPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            Screen current = Minecraft.getInstance().screen;
            if (current instanceof OWTeamsInterface teamsInterface) {
                teamsInterface.onEntityAlreadyInTeam(packet.targetNickname(), packet.currentTeamName());
            }
        });
    }
}