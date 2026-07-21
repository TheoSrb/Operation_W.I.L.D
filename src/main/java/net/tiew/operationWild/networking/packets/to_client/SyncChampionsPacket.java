package net.tiew.operationWild.networking.packets.to_client;

// !! À enregistrer dans OWNetworkHandler (côté to_client) !!

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.tiew.operationWild.OperationWild;
import net.tiew.operationWild.client.OWClientChampions;
import net.tiew.operationWild.team.OWArenaFighter;
import net.tiew.operationWild.team.OWTeam;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Alimente l'écran des champions : les créatures éligibles de la tribu, et celles déjà nommées.
 *
 * <p>Les champions voyagent comme une liste d'UUID <b>ordonnée</b> et non comme un drapeau posé sur
 * chaque candidat : l'ordre de nomination est celui de l'affichage, et un champion resté dans un
 * chunk déchargé n'apparaît pas parmi les candidats mais doit malgré tout se voir dans ses
 * emplacements.</p>
 */
public record SyncChampionsPacket(
        List<OWArenaFighter> candidates,
        List<String> championUUIDs
) implements CustomPacketPayload {

    public static final Type<SyncChampionsPacket> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "sync_champions"));

    public static final StreamCodec<ByteBuf, SyncChampionsPacket> STREAM_CODEC = StreamCodec.of(
            (buf, p) -> {
                OWArenaFighter.LIST_STREAM_CODEC.encode(buf, p.candidates());
                ByteBufCodecs.INT.encode(buf, p.championUUIDs().size());
                for (String s : p.championUUIDs()) ByteBufCodecs.STRING_UTF8.encode(buf, s);
            },
            buf -> {
                List<OWArenaFighter> candidates = OWArenaFighter.LIST_STREAM_CODEC.decode(buf);
                int n = ByteBufCodecs.INT.decode(buf);
                List<String> champions = new ArrayList<>(n);
                for (int i = 0; i < n; i++) champions.add(ByteBufCodecs.STRING_UTF8.decode(buf));
                return new SyncChampionsPacket(candidates, champions);
            }
    );

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }

    public static SyncChampionsPacket empty() {
        return new SyncChampionsPacket(List.of(), List.of());
    }

    public static SyncChampionsPacket of(List<OWArenaFighter> candidates, OWTeam team) {
        List<String> champions = new ArrayList<>();
        for (UUID u : team.getChampionUUIDs()) champions.add(u.toString());
        return new SyncChampionsPacket(candidates, champions);
    }

    /** Les champions, dans l'ordre de nomination. */
    public List<UUID> champions() {
        List<UUID> out = new ArrayList<>(championUUIDs.size());
        for (String s : championUUIDs) {
            try { out.add(UUID.fromString(s)); } catch (IllegalArgumentException ignored) {}
        }
        return out;
    }

    public static void handle(SyncChampionsPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> OWClientChampions.set(packet));
    }
}
