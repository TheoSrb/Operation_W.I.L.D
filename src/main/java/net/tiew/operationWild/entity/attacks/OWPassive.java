package net.tiew.operationWild.entity.attacks;

import net.minecraft.world.level.Level;
import net.tiew.operationWild.entity.OWEntity;

import java.util.Set;

/**
 * Client-side passive ability for an OWEntity.
 * Returns entity IDs to highlight as ESP through walls.
 * Register via OWAttacksHandler.registerPassive().
 */
public interface OWPassive {
    Set<Integer> getHighlightEntityIds(OWEntity entity, Level level);

    default int highlightColor() { return 0xFF2020; }
}
