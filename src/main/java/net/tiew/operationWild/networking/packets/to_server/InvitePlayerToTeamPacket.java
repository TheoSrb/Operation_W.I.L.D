package net.tiew.operationWild.networking.packets.to_server;

import io.netty.buffer.ByteBuf;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.tiew.operationWild.OperationWild;
import net.tiew.operationWild.entity.OWEntity;
import net.tiew.operationWild.team.OWTeam;
import net.tiew.operationWild.team.OWTeamInvites;

import java.util.UUID;

/**
 * Le chef d'une tribu invite un joueur (par UUID). Le serveur valide, enregistre l'invitation en
 * attente ({@link OWTeamInvites}) et envoie au joueur cible un message cliquable Accepter / Refuser
 * (commandes {@code /owteamaccept} et {@code /owteamdecline}).
 */
public record InvitePlayerToTeamPacket(int teamEntityId, UUID targetPlayerUUID)
        implements CustomPacketPayload {

    public static final Type<InvitePlayerToTeamPacket> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "invite_player_to_team"));

    public static final StreamCodec<ByteBuf, InvitePlayerToTeamPacket> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.INT, InvitePlayerToTeamPacket::teamEntityId,
                    UUIDUtil.STREAM_CODEC, InvitePlayerToTeamPacket::targetPlayerUUID,
                    InvitePlayerToTeamPacket::new);

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }

    public static void handle(InvitePlayerToTeamPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer sender)) return;
            if (!(sender.level() instanceof ServerLevel serverLevel)) return;

            Entity teamEntity = serverLevel.getEntity(packet.teamEntityId());
            if (!(teamEntity instanceof OWEntity owEntity) || owEntity.currentTeam == null) return;

            OWTeam team = owEntity.currentTeam;
            // Seul le chef peut inviter.
            if (!sender.getUUID().equals(team.getTeamOwnerUUID())) return;

            ServerPlayer target = serverLevel.getServer().getPlayerList().getPlayer(packet.targetPlayerUUID());
            if (target == null) {
                msg(sender, "owteams.invite.target_offline", 0xFF6666);
                return;
            }
            if (target.getUUID().equals(sender.getUUID())) return;
            if (team.isMember(target.getUUID())) {
                msg(sender, "owteams.invite.already_member", 0xFF9944, target.getName().getString());
                return;
            }
            if (team.getPlayerUUIDs().size() >= team.getMaxPlayers()) {
                msg(sender, "owteams.invite.full", 0xFF6666);
                return;
            }

            OWTeamInvites.put(target.getUUID(), team, owEntity.getId(),
                    sender.getUUID(), sender.getName().getString());

            // Confirmation à l'invitant.
            msg(sender, "owteams.invite.sent", 0x7ddd73, target.getName().getString());

            // Message cliquable au joueur cible.
            Component accept = Component.translatable("owteams.invite.accept")
                    .setStyle(Style.EMPTY
                            .withColor(TextColor.fromRgb(0x7ddd73)).withBold(true)
                            .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/owteamaccept"))
                            .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                                    Component.translatable("owteams.invite.accept.hover"))));
            Component decline = Component.translatable("owteams.invite.decline")
                    .setStyle(Style.EMPTY
                            .withColor(TextColor.fromRgb(0xdd4444)).withBold(true)
                            .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/owteamdecline"))
                            .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                                    Component.translatable("owteams.invite.decline.hover"))));

            target.sendSystemMessage(Component.translatable("owteams.invite.received",
                            Component.literal(sender.getName().getString())
                                    .setStyle(Style.EMPTY.withColor(TextColor.fromRgb(0xFFD700))),
                            Component.literal(team.getTeamName())
                                    .setStyle(Style.EMPTY.withColor(TextColor.fromRgb(0x7dbedd))))
                    .setStyle(Style.EMPTY.withColor(TextColor.fromRgb(0xFFFFFF))));
            target.sendSystemMessage(Component.literal("  ")
                    .append(accept)
                    .append(Component.literal("   "))
                    .append(decline));
        });
    }

    private static void msg(ServerPlayer p, String key, int color, Object... args) {
        p.sendSystemMessage(Component.translatable(key, args)
                .setStyle(Style.EMPTY.withColor(TextColor.fromRgb(color))));
    }
}
