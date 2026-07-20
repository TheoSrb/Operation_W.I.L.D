package net.tiew.operationWild.event;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.LevelAccessor;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.level.ExplosionEvent;
import net.tiew.operationWild.OperationWild;
import net.tiew.operationWild.worldgen.dimension.OWDimensions;

/**
 * Décor de l'arène en lecture seule.
 *
 * <p>L'aire de combat est bâtie une fois pour toutes et resservie à chaque duel : elle ne doit donc
 * ni se faire creuser, ni se faire murer, ni sauter. Casse, pose, explosions et propagation du feu
 * y sont refusées.</p>
 *
 * <p><b>Échappatoire volontaire</b> : un joueur en mode <i>créatif</i> conserve tous ses droits.
 * C'est ce qui permet de construire et de retoucher l'arène — le basculement de mode de jeu sert de
 * geste explicite, plutôt qu'un droit implicite lié au rang.</p>
 */
@EventBusSubscriber(modid = OperationWild.MOD_ID, bus = EventBusSubscriber.Bus.GAME)
public class OWArenaProtection {

    /** Vrai si le niveau est la dimension d'arène. */
    private static boolean inArena(LevelAccessor level) {
        return level instanceof net.minecraft.world.level.Level lvl
                && lvl.dimension().equals(OWDimensions.ARENA);
    }

    /** Un bâtisseur en créatif passe outre ; tous les autres non. */
    private static boolean mayEdit(Player player) {
        return player != null && player.isCreative();
    }

    private static void refuse(Player player) {
        if (player instanceof ServerPlayer sp) {
            sp.sendSystemMessage(Component.translatable("owteams.arena.protected")
                    .setStyle(Style.EMPTY.withColor(0xE8956A)));
        }
    }

    @SubscribeEvent
    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        if (!inArena(event.getLevel()) || mayEdit(event.getPlayer())) return;
        event.setCanceled(true);
        refuse(event.getPlayer());
    }

    @SubscribeEvent
    public static void onBlockPlace(BlockEvent.EntityPlaceEvent event) {
        if (!inArena(event.getLevel())) return;
        // Entité non-joueur qui pose un bloc (endermite, etc.) : refus sec, sans message.
        if (event.getEntity() instanceof Player player) {
            if (mayEdit(player)) return;
            event.setCanceled(true);
            refuse(player);
            return;
        }
        event.setCanceled(true);
    }

    /** Blocs multiples (lit, porte, fleur haute) : même règle. */
    @SubscribeEvent
    public static void onMultiPlace(BlockEvent.EntityMultiPlaceEvent event) {
        if (!inArena(event.getLevel())) return;
        if (event.getEntity() instanceof Player player && mayEdit(player)) return;
        event.setCanceled(true);
    }

    /** Une explosion ne doit rien emporter du décor ; son souffle sur les entités reste intact. */
    @SubscribeEvent
    public static void onExplosionDetonate(ExplosionEvent.Detonate event) {
        if (!inArena(event.getLevel())) return;
        event.getAffectedBlocks().clear();
    }

    /** Coupe la propagation du feu : une arène en bois ne doit pas partir en fumée d'un duel à l'autre. */
    @SubscribeEvent
    public static void onFireSpread(BlockEvent.NeighborNotifyEvent event) {
        if (!inArena(event.getLevel())) return;
        if (event.getState().is(net.minecraft.world.level.block.Blocks.FIRE)) event.setCanceled(true);
    }
}
