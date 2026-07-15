package net.tiew.operationWild.networking.packets.to_server;

// !! À enregistrer dans OWNetworkHandler (côté to_server) !!

import io.netty.buffer.ByteBuf;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.tiew.operationWild.OperationWild;
import net.tiew.operationWild.team.OWTeam;
import net.tiew.operationWild.team.OWTribeInvites;
import net.tiew.operationWild.team.OWTribesSavedData;

import java.util.UUID;

/** Le chef invite un joueur (par UUID) à rejoindre sa tribu : message cliquable + store transitoire. */
public record InvitePlayerToTribePacket(UUID targetPlayerUUID) implements CustomPacketPayload {

    public static final Type<InvitePlayerToTribePacket> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "invite_player_to_tribe"));

    public static final StreamCodec<ByteBuf, InvitePlayerToTribePacket> STREAM_CODEC =
            StreamCodec.composite(UUIDUtil.STREAM_CODEC, InvitePlayerToTribePacket::targetPlayerUUID,
                    InvitePlayerToTribePacket::new);

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }

    public static void handle(InvitePlayerToTribePacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer sp)) return;
            MinecraftServer server = sp.getServer();
            if (server == null) return;
            OWTribesSavedData data = OWTribesSavedData.get(server);

            OWTeam team = data.findTeamByMember(sp.getUUID());
            if (team == null || !sp.getUUID().equals(team.getTeamOwnerUUID())) return; // chef seul invite
            if (team.getPlayerUUIDs().size() >= team.getMaxPlayers()) {
                sp.sendSystemMessage(Component.translatable("owteams.invite.full")
                        .setStyle(Style.EMPTY.withColor(0xFF6666)));
                return;
            }

            ServerPlayer target = server.getPlayerList().getPlayer(packet.targetPlayerUUID());
            if (target == null) return;
            if (team.isMember(target.getUUID())) return;
            if (data.findTeamByMember(target.getUUID()) != null) {
                sp.sendSystemMessage(Component.translatable("owteams.invite.target_in_tribe",
                        target.getName().getString()).setStyle(Style.EMPTY.withColor(0xFF9944)));
                return;
            }

            OWTribeInvites.put(target.getUUID(), team.getTeamId(), sp.getUUID(),
                    sp.getName().getString(), team.getTeamName());

            target.sendSystemMessage(Component.translatable("owteams.invite.received",
                    sp.getName().getString(), team.getTeamName())
                    .setStyle(Style.EMPTY.withColor(TextColor.fromRgb(0xFFD700))));
            Component accept = Component.translatable("owteams.invite.accept_button")
                    .setStyle(Style.EMPTY.withColor(TextColor.fromRgb(0x7DDD73)).withBold(true)
                            .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/owtribeaccept")));
            Component decline = Component.translatable("owteams.invite.decline_button")
                    .setStyle(Style.EMPTY.withColor(TextColor.fromRgb(0xDD4444)).withBold(true)
                            .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/owtribedecline")));
            target.sendSystemMessage(Component.literal("  ").append(accept).append(Component.literal("   ")).append(decline));

            sp.sendSystemMessage(Component.translatable("owteams.invite.sent",
                    target.getName().getString()).setStyle(Style.EMPTY.withColor(0x7DDD73)));
        });
    }
}
