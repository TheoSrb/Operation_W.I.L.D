package net.tiew.operationWild.quests;

import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.tiew.operationWild.entity.OWEntity;

import java.util.*;

/**
 * Base abstraite d'une quête cosmétique trackée par entité.
 * Chaque UUID d'entité a sa propre progression indépendante.
 */
@OnlyIn(Dist.CLIENT)
public abstract class CosmeticsQuest {

    private final int    id;
    private final String titleKey;
    private final String descriptionKey;
    private final int    maxProgress;

    private final Map<UUID, Integer> progressByEntity  = new HashMap<>();
    private final Set<UUID>          completedEntities = new HashSet<>();

    protected CosmeticsQuest(int id, String titleKey, String descriptionKey, int maxProgress) {
        this.id             = id;
        this.titleKey       = titleKey;
        this.descriptionKey = descriptionKey;
        this.maxProgress    = maxProgress;
    }

    // =========================================================================
    // Méthodes abstraites
    // =========================================================================

    /**
     * Vérifie et met à jour la progression d'une créature.
     *
     * <p>Prend la créature et non son seul identifiant : l'avancement se lit désormais sur elle,
     * où il est répliqué jusqu'au client, et non plus dans une table que seul le serveur remplit.</p>
     */
    public abstract void update(OWEntity entity);

    // =========================================================================
    // Logique de progression par entité
    // =========================================================================

    protected void setProgress(UUID entityId, int value) {
        int clamped = Math.min(Math.max(value, 0), maxProgress);
        if (clamped == getProgress(entityId)) return;
        progressByEntity.put(entityId, clamped);
        if (clamped >= maxProgress) complete(entityId);
        else CosmeticsQuestsRegistry.saveAll();
    }

    protected void complete(UUID entityId) {
        completedEntities.add(entityId);
        progressByEntity.put(entityId, maxProgress);
        CosmeticsQuestsRegistry.saveAll();
        Minecraft.getInstance().getToasts().addToast(new QuestCompletionToast(this));
    }

    /**
     * Charge l'état depuis la sauvegarde pour une entité donnée.
     * Sans effet de bord (pas de save, pas de toast).
     */
    /** Remet à zéro la progression d'une entité et sauvegarde. */
    public void reset(UUID entityId) {
        completedEntities.remove(entityId);
        progressByEntity.put(entityId, 0);
        CosmeticsQuestsRegistry.saveAll();
    }

    public void loadState(UUID entityId, boolean completed, int progress) {
        if (completed) {
            completedEntities.add(entityId);
            progressByEntity.put(entityId, maxProgress);
        } else {
            progressByEntity.put(entityId, Math.min(Math.max(progress, 0), maxProgress));
        }
    }

    // =========================================================================
    // Accesseurs
    // =========================================================================

    public boolean isCompleted(UUID entityId)      { return completedEntities.contains(entityId); }
    public int     getProgress(UUID entityId)       { return progressByEntity.getOrDefault(entityId, 0); }
    public float   getProgressFraction(UUID entityId) {
        return maxProgress > 0 ? (float) getProgress(entityId) / maxProgress : 0f;
    }

    /** Pour la sauvegarde — toutes les progressions connues. */
    public Map<UUID, Integer> getAllProgress()      { return Collections.unmodifiableMap(progressByEntity); }
    public Set<UUID>          getCompletedEntities(){ return Collections.unmodifiableSet(completedEntities); }

    public int    getId()             { return id; }
    public String getTitleKey()       { return titleKey; }
    public String getDescriptionKey() { return descriptionKey; }
    public int    getMaxProgress()    { return maxProgress; }
}
