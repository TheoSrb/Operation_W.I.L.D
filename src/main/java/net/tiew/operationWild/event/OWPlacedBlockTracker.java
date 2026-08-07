package net.tiew.operationWild.event;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.tiew.operationWild.OperationWild;
import net.tiew.operationWild.core.OWPlacedBlocks;

/**
 * Tient à jour {@link OWPlacedBlocks} : qui pose quoi, et ce qui disparaît.
 *
 * <p>Seuls les blocs posés <b>par un joueur</b> sont enregistrés. Ceux d'un villageois, d'un
 * distributeur ou de la génération du monde n'appartiennent à personne et restent disloquables : le
 * registre sert à protéger un ouvrage, pas à geler le décor.</p>
 *
 * <p>La reprise efface l'entrée quel que soit le casseur. Sans cela un emplacement libéré garderait
 * son ancien propriétaire, et le bloc de terrain qui viendrait l'occuper plus tard hériterait d'une
 * protection que personne n'a demandée.</p>
 */
@EventBusSubscriber(modid = OperationWild.MOD_ID, bus = EventBusSubscriber.Bus.GAME)
public class OWPlacedBlockTracker {

    @SubscribeEvent
    public static void onBlockPlaced(BlockEvent.EntityPlaceEvent event) {
        if (!(event.getLevel() instanceof ServerLevel level)) return;
        if (!(event.getEntity() instanceof Player player)) return;

        OWPlacedBlocks.get(level).record(event.getPos(), player.getUUID());
    }

    @SubscribeEvent
    public static void onBlockBroken(BlockEvent.BreakEvent event) {
        if (!(event.getLevel() instanceof ServerLevel level)) return;

        OWPlacedBlocks.get(level).forget(event.getPos());
    }
}
