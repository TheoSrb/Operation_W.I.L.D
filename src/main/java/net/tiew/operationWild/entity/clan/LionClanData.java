package net.tiew.operationWild.entity.clan;

import net.minecraft.core.BlockPos;
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
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
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

    public static final int MAX_SATIETY = 72000;
    public static final int MAX_COALITION_MALES = 3;
    private static final float START_SATIETY_MIN = 0.45f;
    private static final float START_SATIETY_SPREAD = 0.4f;

    public static final class Clan {
        public final UUID id;
        public final int color;
        public final boolean tamed;
        public UUID male;
        public final Set<UUID> coMales = new LinkedHashSet<>();
        public final Set<UUID> lionesses = new LinkedHashSet<>();
        @Nullable
        public BlockPos home;
        public int satiety = MAX_SATIETY / 2;
        public final LionPrideState state = new LionPrideState();

        private Clan(UUID id, int color, boolean tamed, UUID male) {
            this.id = id;
            this.color = color;
            this.tamed = tamed;
            this.male = male;
        }

        public int size() {
            return lionesses.size() + maleCount();
        }

        public int maleCount() {
            return 1 + coMales.size();
        }

        public boolean isMale(UUID member) {
            return male.equals(member) || coMales.contains(member);
        }

        public boolean isNomad() {
            return !tamed && lionesses.isEmpty();
        }

        public boolean isCoalitionFull() {
            return maleCount() >= MAX_COALITION_MALES;
        }

        public List<UUID> males() {
            List<UUID> males = new ArrayList<>(maleCount());
            males.add(male);
            males.addAll(coMales);
            return males;
        }

        public float satietyRatio() {
            return Mth.clamp(satiety / (float) MAX_SATIETY, 0f, 1f);
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

    public Collection<Clan> clans() {
        return Collections.unmodifiableCollection(clans.values());
    }

    public Clan found(UUID male, boolean tamed, RandomSource random) {
        leave(male);
        Clan clan = new Clan(UUID.randomUUID(), pickDistinctColor(random), tamed, male);
        clan.satiety = (int) (MAX_SATIETY * (START_SATIETY_MIN + random.nextFloat() * START_SATIETY_SPREAD));
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

    public boolean joinCoalition(UUID clanId, UUID male) {
        Clan clan = clans.get(clanId);
        if (clan == null || clan.tamed || clan.isCoalitionFull() || clan.isMale(male)) return false;
        leave(male);
        clan.coMales.add(male);
        memberToClan.put(male, clanId);
        setDirty();
        return true;
    }

    public void leave(UUID member) {
        UUID clanId = memberToClan.remove(member);
        if (clanId == null) return;
        Clan clan = clans.get(clanId);
        if (clan == null) return;
        if (member.equals(clan.male)) {
            Iterator<UUID> heirs = clan.coMales.iterator();
            if (!heirs.hasNext()) {
                dissolve(clanId);
                return;
            }
            clan.male = heirs.next();
            heirs.remove();
            setDirty();
            return;
        }
        clan.coMales.remove(member);
        clan.lionesses.remove(member);
        setDirty();
    }

    public void relocate(Clan clan, @Nullable BlockPos home) {
        clan.home = home == null ? null : home.immutable();
        setDirty();
    }

    public void feed(Clan clan, int amount) {
        clan.satiety = Mth.clamp(clan.satiety + amount, 0, MAX_SATIETY);
        setDirty();
    }

    public List<UUID> dissolve(UUID clanId) {
        Clan clan = clans.remove(clanId);
        if (clan == null) return List.of();
        List<UUID> freed = new ArrayList<>(clan.lionesses);
        for (UUID lioness : clan.lionesses) memberToClan.remove(lioness);
        for (UUID coMale : clan.coMales) memberToClan.remove(coMale);
        memberToClan.remove(clan.male);
        setDirty();
        return freed;
    }

    public int absorb(UUID victorClanId, UUID defeatedClanId) {
        Clan victor = clans.get(victorClanId);
        Clan defeated = clans.get(defeatedClanId);
        if (victor == null || defeated == null || victor == defeated) return 0;

        BlockPos inherited = defeated.home;
        List<UUID> freed = dissolve(defeatedClanId);
        if (victor.home == null && inherited != null) victor.home = inherited;
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
            ListTag allies = entry.getList("coMales", Tag.TAG_COMPOUND);
            for (int j = 0; j < allies.size(); j++) {
                CompoundTag ally = allies.getCompound(j);
                if (ally.hasUUID("uuid")) clan.coMales.add(ally.getUUID("uuid"));
            }
            if (entry.contains("home")) clan.home = BlockPos.of(entry.getLong("home"));
            clan.satiety = entry.contains("satiety") ? Mth.clamp(entry.getInt("satiety"), 0, MAX_SATIETY) : MAX_SATIETY / 2;
            data.clans.put(clan.id, clan);
            data.memberToClan.put(clan.male, clan.id);
            for (UUID coMale : clan.coMales) data.memberToClan.put(coMale, clan.id);
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
            ListTag allies = new ListTag();
            for (UUID coMale : clan.coMales) {
                CompoundTag ally = new CompoundTag();
                ally.putUUID("uuid", coMale);
                allies.add(ally);
            }
            entry.put("coMales", allies);
            if (clan.home != null) entry.putLong("home", clan.home.asLong());
            entry.putInt("satiety", clan.satiety);
            list.add(entry);
        }
        tag.put("clans", list);
        return tag;
    }
}
