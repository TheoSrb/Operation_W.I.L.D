package net.tiew.operationWild.networking.packets.to_server;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.tiew.operationWild.OperationWild;
import net.tiew.operationWild.entity.OWEntity;
import net.tiew.operationWild.entity.quests.daily_quests.OWDailyQuests;

import java.util.UUID;

public record OpenDailyQuestScreen() implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<OpenDailyQuestScreen> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "open_daily_quest"));

    public static final StreamCodec<FriendlyByteBuf, OpenDailyQuestScreen> STREAM_CODEC =
            StreamCodec.unit(new OpenDailyQuestScreen());

    @Override
    public CustomPacketPayload.Type<OpenDailyQuestScreen> type() {
        return TYPE;
    }

    /**
     * Le joueur a ouvert l'onglet des quêtes : la journée est lue, et elle l'est pour <b>toute son
     * écurie</b>.
     *
     * <p>La pastille annonce qu'il y a du nouveau à lire, pas que telle bête a changé de quêtes :
     * la reposer devant chaque compagnon obligeait à ouvrir le même onglet autant de fois qu'on a de
     * créatures pour la même nouvelle. On note donc la consultation <b>sur le joueur</b> — c'est
     * elle qui fait autorité, y compris pour les créatures actuellement déchargées, qui la
     * consulteront en tirant leurs quêtes (cf. {@code OWEntity#ownerHasSeenQuestPeriod}) — puis on
     * éteint les pastilles déjà allumées sur celles qui sont en jeu.</p>
     */
    public static void handle(OpenDailyQuestScreen packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player)) return;
            MinecraftServer server = player.getServer();
            if (server == null) return;

            OWDailyQuests.markCurrentPeriodSeen(player);

            UUID ownerId = player.getUUID();
            for (ServerLevel level : server.getAllLevels()) {
                for (Entity e : level.getAllEntities()) {
                    if (e instanceof OWEntity owE && owE.questsAreUpdated()
                            && ownerId.equals(owE.getOwnerUUID())) {
                        owE.setUpdatingQuests(false);
                    }
                }
            }
        });
    }
}

