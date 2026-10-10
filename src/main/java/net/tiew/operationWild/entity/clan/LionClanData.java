package net.tiew.operationWild.entity.clan;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.saveddata.SavedData;
import net.tiew.operationWild.entity.attacks.OWAttacksConstants;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class LionClanData extends SavedData {

    public static final String DATA_NAME = "ow_lion_clans";

    private static final float COLOR_SATURATION = 0.78f;
    private static final float COLOR_BRIGHTNESS = 0.92f;
    private static final int HUE_CANDIDATES = 36;

    public static final class Clan {
        public final UUID id;
        public final int color;
        public final boolean tamed;
        public UUID male;
        public final Set<UUID> lionesses = new LinkedHashSet<>();

        private Clan(UUID id, int color, boolean tamed, UUID male) {
            this.id = id;
            this.color = color;
            this.tamed = tamed;
            this.male = male;
        }

        public int size() {
            return lionesses.size() + 1;
        }

        public boolean isFull() {
            return lionesses.size() >= OWAttacksConstants.Lion.CLAN_MAX_LIONESSES;
        }
    }

    private final Map<UUID, Clan> clans = new HashMap<>();
    private final Map<UUID, UUID> memberToClan = new HashMap<>();

    public LionClanData() {}

    public static LionClanData get(MinecraftServer server) {
        return server.overworld().getDataStorage().computeIfAbsent(
                new SavedData.Factory<>(LionClanData::new, LionClanData::load, null),
                DATA_NAME);
    }

    @Nullable
    public Clan getClan(@Nullable UUID clanId) {
        return clanId == null ? null : clans.get(clanId);
    }

    @Nullable
    public Clan clanOf(UUID member) {
        UUID clanId = memberToClan.get(member);
        return clanId == null ? null : clans.get(clanId);
    }

    public boolean isMember(UUID clanId, UUID member) {
        return clanId != null && clanId.equals(memberToClan.get(member));
    }

    public Clan found(UUID male, boolean tamed, RandomSource random) {
        leave(male);
        Clan clan = new Clan(UUID.randomUUID(), pickDistinctColor(random), tamed, male);
        clans.put(clan.id, clan);
        memberToClan.put(male, clan.id);
        setDirty();
        return clan;
    }

    public boolean join(UUID clanId, UUID lioness) {
        Clan clan = clans.get(clanId);
        if (clan == null || clan.isFull()) return false;
        leave(lioness);
        clan.lionesses.add(lioness);
        memberToClan.put(lioness, clanId);
        setDirty();
        return true;
    }

    public void leave(UUID member) {
        UUID clanId = memberToClan.remove(member);
        if (clanId == null) return;
        Clan clan = clans.get(clanId);
        if (clan == null) return;
        if (member.equals(clan.male)) {
            dissolve(clanId);
            return;
        }
        clan.lionesses.remove(member);
        setDirty();
    }

    public List<UUID> dissolve(UUID clanId) {
        Clan clan = clans.remove(clanId);
        if (clan == null) return List.of();
        List<UUID> freed = new ArrayList<>(clan.lionesses);
        for (UUID lioness : clan.lionesses) memberToClan.remove(lioness);
        memberToClan.remove(clan.male);
        setDirty();
        return freed;
    }

    public int absorb(UUID victorClanId, UUID defeatedClanId) {
        Clan victor = clans.get(victorClanId);
        Clan defeated = clans.get(defeatedClanId);
        if (victor == null || defeated == null || victor == defeated) return 0;

        List<UUID> freed = dissolve(defeatedClanId);
        int joined = 0;
        for (UUID lioness : freed) {
            if (victor.isFull()) break;
            victor.lionesses.add(lioness);
            memberToClan.put(lioness, victorClanId);
            joined++;
        }
        setDirty();
        return joined;
    }

    private int pickDistinctColor(RandomSource random) {
        List<Float> used = new ArrayList<>();
        for (Clan clan : clans.values()) used.add(hueOf(clan.color));

        float offset = random.nextFloat();
        float bestHue = offset;
        float bestGap = -1f;
        for (int i = 0; i < HUE_CANDIDATES; i++) {
            float hue = (offset + (float) i / HUE_CANDIDATES) % 1f;
            float gap = 1f;
            for (float other : used) {
                float distance = Math.abs(hue - other);
                gap = Math.min(gap, Math.min(distance, 1f - distance));
            }
            if (gap > bestGap) {
                bestGap = gap;
                bestHue = hue;
            }
        }
        return Mth.hsvToRgb(bestHue, COLOR_SATURATION, COLOR_BRIGHTNESS) & 0xFFFFFF;
    }

    private static float hueOf(int rgb) {
        float r = ((rgb >> 16) & 0xFF) / 255f;
        float g = ((rgb >> 8) & 0xFF) / 255f;
        float b = (rgb & 0xFF) / 255f;
        float max = Math.max(r, Math.max(g, b));
        float min = Math.min(r, Math.min(g, b));
        float delta = max - min;
        if (delta <= 1.0E-5f) return 0f;
        float hue;
        if (max == r) hue = ((g - b) / delta) % 6f;
        else if (max == g) hue = (b - r) / delta + 2f;
        else hue = (r - g) / delta + 4f;
        hue /= 6f;
        return hue < 0f ? hue + 1f : hue;
    }

    public static LionClanData load(CompoundTag tag, HolderLookup.Provider provider) {
        LionClanData data = new LionClanData();
        ListTag list = tag.getList("clans", Tag.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++) {
            CompoundTag entry = list.getCompound(i);
            if (!entry.hasUUID("id") || !entry.hasUUID("male")) continue;
            Clan clan = new Clan(entry.getUUID("id"), entry.getInt("color"), entry.getBoolean("tamed"), entry.getUUID("male"));
            ListTag members = entry.getList("lionesses", Tag.TAG_COMPOUND);
            for (int j = 0; j < members.size(); j++) {
                CompoundTag member = members.getCompound(j);
                if (member.hasUUID("uuid")) clan.lionesses.add(member.getUUID("uuid"));
            }
            data.clans.put(clan.id, clan);
            data.memberToClan.put(clan.male, clan.id);
            for (UUID lioness : clan.lionesses) data.memberToClan.put(lioness, clan.id);
        }
        return data;
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider provider) {
        ListTag list = new ListTag();
        for (Clan clan : clans.values()) {
            CompoundTag entry = new CompoundTag();
            entry.putUUID("id", clan.id);
            entry.putInt("color", clan.color);
            entry.putBoolean("tamed", clan.tamed);
            entry.putUUID("male", clan.male);
            ListTag members = new ListTag();
            for (UUID lioness : clan.lionesses) {
                CompoundTag member = new CompoundTag();
                member.putUUID("uuid", lioness);
                members.add(member);
            }
            entry.put("lionesses", members);
            list.add(entry);
        }
        tag.put("clans", list);
        return tag;
    }
}
