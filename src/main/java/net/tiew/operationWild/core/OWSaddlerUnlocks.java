package net.tiew.operationWild.core;

import net.minecraft.world.entity.player.Player;

import java.util.List;

public final class OWSaddlerUnlocks {

    public static final String KEY = "ow_saddler_unlocks";

    private OWSaddlerUnlocks() {}

    public static int getMask(Player player) {
        return player.getPersistentData().getInt(KEY);
    }

    public static void setMask(Player player, int mask) {
        player.getPersistentData().putInt(KEY, mask);
    }

    public static boolean isUnlocked(int mask, int index) {
        return index >= 0 && (mask & (1 << index)) != 0;
    }

    public static int rebuild(Player player) {
        int mask = 0;
        double experience = OWTamingXp.getTamingXp(player);

        List<OWSaddleRecipe> recipes = OWSaddleRecipes.all();
        for (int i = 0; i < recipes.size(); i++) {
            if (experience >= recipes.get(i).tamingThreshold()) mask |= (1 << i);
        }

        setMask(player, mask);
        return mask;
    }

    public static int refresh(Player player) {
        int previous = getMask(player);
        int mask = previous;
        double experience = OWTamingXp.getTamingXp(player);

        List<OWSaddleRecipe> recipes = OWSaddleRecipes.all();
        for (int i = 0; i < recipes.size(); i++) {
            if (experience >= recipes.get(i).tamingThreshold()) mask |= (1 << i);
        }

        if (mask != previous) setMask(player, mask);
        return mask;
    }

    public static void copy(Player from, Player to) {
        setMask(to, getMask(from));
    }
}
