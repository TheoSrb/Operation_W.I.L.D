package net.tiew.operationWild.datagen.wiki;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.tiew.operationWild.core.OWArena;
import net.tiew.operationWild.core.OWArenaVenue;
import net.tiew.operationWild.core.OWArenaVenueUnlocks;
import net.tiew.operationWild.core.OWBannerUnlocks;
import net.tiew.operationWild.core.OWChampions;
import net.tiew.operationWild.core.OWCurrency;
import net.tiew.operationWild.core.OWKeysBinding;
import net.tiew.operationWild.core.OWReputation;
import net.tiew.operationWild.datagen.OWArenaChestLootProvider;
import net.tiew.operationWild.entity.OWEntity;
import net.tiew.operationWild.entity.config.OWEntityConfig;
import net.tiew.operationWild.team.OWArenaChallenges;
import net.tiew.operationWild.team.OWArenaMatch;
import net.tiew.operationWild.team.OWPendingSouls;
import net.tiew.operationWild.team.OWReputationData;
import net.tiew.operationWild.team.OWTeam;
import net.tiew.operationWild.team.OWTeamBannerShape;
import net.tiew.operationWild.team.OWTeamMosaicPattern;
import net.tiew.operationWild.team.OWTribeInvites;
import net.tiew.operationWild.team.OWTribeJoinCheck;
import net.tiew.operationWild.team.OWTribeJoinCondition;
import net.tiew.operationWild.team.OWTribeJoinRequests;
import net.tiew.operationWild.team.OWTribePermission;
import net.tiew.operationWild.team.OWTribesSavedData;
import net.tiew.operationWild.worldgen.biome.OWBiomes;
import net.tiew.operationWild.worldgen.dimension.OWDimensions;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

public final class OWWikiTribesSection {

    private OWWikiTribesSection() {}

    private static final String LANG_PREFIX = "owteams.";
    private static final String MODEL_PACKAGE = "net.tiew.operationWild.entity.client.model.";
    private static final String TRIBE_SCREEN_PACKAGE = "net/tiew/operationWild/screen/tribe";
    private static final String TEAM_PACKAGE = "net/tiew/operationWild/team";
    private static final String PACKET_TO_CLIENT_PACKAGE = "net/tiew/operationWild/networking/packets/to_client";
    private static final String PACKET_TO_SERVER_PACKAGE = "net/tiew/operationWild/networking/packets/to_server";

    private static final String[] PACKET_KEYWORDS = {"Tribe", "Team", "Arena", "Champion", "Banner", "Reputation"};

    private static final int NAME_MAX_LENGTH = 15;
    private static final int COLOR_FIELD_MAX_LENGTH = 7;
    private static final int PAINT_WIDTH = 55;
    private static final int PAINT_HEIGHT = 93;
    private static final int TICKS_PER_HOUR = 20 * 60 * 60;

    private static final int[] LEVEL_SAMPLES = {1, 10, 25, 40, 49, 50};
    private static final int[] REPUTATION_SAMPLES =
            {0, 500, 750, 2000, 4500, 9000, 16000, 28000, 45000, 70000, 110000, 200000};
    private static final int[] PRESTIGE_DELTA_SAMPLES =
            {-20000, -10000, -5000, -1000, 0, 1000, 2500, 5000, 10000, 20000};

    public static JsonObject build(HolderLookup.Provider registries, OWWikiLang lang, OWWikiSources sources) {
        JsonObject tribes = new JsonObject();
        tribes.add("overview", overview(lang));
        tribes.add("defaults", defaults());
        tribes.add("roles", roles(lang));
        tribes.add("permissions", permissions(lang));
        tribes.add("membership", membership(lang));
        tribes.add("join_conditions", joinConditions(lang));
        tribes.add("banner", banner(lang));
        tribes.add("reputation", reputation(lang));
        tribes.add("champions", champions(lang));
        tribes.add("arena", arena(registries, lang));
        tribes.add("economy", economy());
        tribes.add("commands", commands());
        tribes.add("persistence", persistence());
        tribes.add("networking", networking(sources));
        tribes.add("constants", constants());
        tribes.add("lang", langDump(lang));

        JsonArray screens = sources.classNames(TRIBE_SCREEN_PACKAGE);
        if (screens != null) tribes.add("screens", screens);
        JsonArray classes = sources.classNames(TEAM_PACKAGE);
        if (classes != null) tribes.add("classes", classes);
        return tribes;
    }

    private static JsonObject overview(OWWikiLang lang) {
        OWTeam blank = blankTeam();
        JsonObject overview = new JsonObject();
        overview.addProperty("model", "player_centric");
        overview.addProperty("membership_source_of_truth", "player_uuids");
        overview.addProperty("entities_follow_owner", true);
        overview.addProperty("tribes_per_player", 1);
        overview.addProperty("max_players", blank.getMaxPlayers());
        overview.addProperty("max_entities", blank.getMaxEntities());
        overview.addProperty("max_join_requirements", OWTeam.MAX_JOIN_REQUIREMENTS);
        overview.addProperty("max_champions", OWChampions.MAX_CHAMPIONS);
        overview.addProperty("max_arena_fighters", OWArena.MAX_FIGHTERS);
        overview.addProperty("name_max_length", NAME_MAX_LENGTH);
        overview.addProperty("name_unique", true);
        overview.addProperty("color_field_max_length", COLOR_FIELD_MAX_LENGTH);
        overview.addProperty("registry", OWTribesSavedData.DATA_NAME);

        JsonObject keyBinding = new JsonObject();
        keyBinding.addProperty("default_key", "T");
        text(keyBinding, "name", lang, OWKeysBinding.OW_TRIBE_MENU_KEY);
        overview.add("key_binding", keyBinding);

        JsonArray tabs = new JsonArray();
        tabs.add(tab("dashboard", "OWTribeDashboardScreen", lang, LANG_PREFIX + "dashboard.title"));
        tabs.add(tab("members", "OWTribePermissionsScreen", lang, LANG_PREFIX + "permissions.title"));
        tabs.add(tab("reputation", "OWTribeReputationScreen", lang, LANG_PREFIX + "reputation.title"));
        tabs.add(tab("arena", "OWTribeArenaScreen", lang, LANG_PREFIX + "arena.title"));
        tabs.add(tab("champions", "OWTribeChampionsScreen", lang, LANG_PREFIX + "champions.title"));
        overview.add("tabs", tabs);
        return overview;
    }

    private static JsonObject tab(String id, String screen, OWWikiLang lang, String titleKey) {
        JsonObject tab = new JsonObject();
        tab.addProperty("id", id);
        tab.addProperty("screen", screen);
        text(tab, "title", lang, titleKey);
        return tab;
    }

    private static JsonObject defaults() {
        OWTeam blank = blankTeam();
        JsonObject defaults = new JsonObject();
        defaults.addProperty("public", blank.isPublic());
        defaults.addProperty("direct_join", blank.isDirectJoin());
        defaults.addProperty("banner_shape", blank.getBannerShape().name());
        defaults.addProperty("mosaic_pattern", blank.getTeamMosaicPattern().name());
        defaults.addProperty("tertiary_color", hex(blank.getTertiaryColor()));
        defaults.addProperty("use_tertiary", blank.isUseTertiary());
        defaults.addProperty("join_requirements", blank.getJoinRequirements().size());
        defaults.addProperty("reputation_enabled", blank.isReputationEnabled());
        defaults.addProperty("reputation_override", blank.getReputationOverride());
        defaults.addProperty("arena_accepted", blank.isArenaAccepted());
        defaults.addProperty("arena_prestige", blank.getArenaPrestige());
        defaults.addProperty("arena_reputation_bonus", blank.getArenaReputationBonus());
        defaults.addProperty("champions", blank.getChampionUUIDs().size());
        return defaults;
    }

    private static JsonArray roles(OWWikiLang lang) {
        JsonArray roles = new JsonArray();
        roles.add(role("chief", LANG_PREFIX + "chief_role", lang, OWTribePermission.ALL, true, true, true, true));
        roles.add(role("deputy", LANG_PREFIX + "deputy_role", lang, OWTribePermission.DEPUTY_DEFAULT,
                false, true, true, false));
        roles.add(role("member", null, lang, OWTribePermission.MEMBER_DEFAULT,
                false, false, false, false));
        return roles;
    }

    private static JsonObject role(String id, String labelKey, OWWikiLang lang, int defaultMask,
                                   boolean owner, boolean invites, boolean kicksMembers, boolean disbands) {
        JsonObject role = new JsonObject();
        role.addProperty("id", id);
        if (labelKey != null) text(role, "label", lang, labelKey);
        role.addProperty("default_permission_mask", defaultMask);
        role.add("default_permissions", permissionNames(defaultMask));
        role.addProperty("owns_tribe", owner);
        role.addProperty("can_invite", invites);
        role.addProperty("can_kick_members", kicksMembers);
        role.addProperty("can_disband", disbands);
        role.addProperty("can_approve_requests", invites);
        role.addProperty("full_access_on_own_entities", true);
        return role;
    }

    private static JsonArray permissionNames(int mask) {
        JsonArray names = new JsonArray();
        for (OWTribePermission permission : OWTribePermission.values()) {
            if ((mask & permission.bit()) != 0) names.add(permission.name());
        }
        return names;
    }

    private static JsonArray permissions(OWWikiLang lang) {
        JsonArray permissions = new JsonArray();
        for (OWTribePermission permission : OWTribePermission.values()) {
            JsonObject entry = new JsonObject();
            entry.addProperty("name", permission.name());
            entry.addProperty("ordinal", permission.ordinal());
            entry.addProperty("bit", permission.bit());
            text(entry, "label", lang, permission.getKey());
            text(entry, "description", lang, permission.getKey() + ".desc");
            entry.addProperty("member_default", (OWTribePermission.MEMBER_DEFAULT & permission.bit()) != 0);
            entry.addProperty("deputy_default", (OWTribePermission.DEPUTY_DEFAULT & permission.bit()) != 0);
            permissions.add(entry);
        }
        return permissions;
    }

    private static JsonObject membership(OWWikiLang lang) {
        JsonObject membership = new JsonObject();
        membership.addProperty("invite_ttl_ms", OWTribeInvites.TTL_MS);
        membership.addProperty("join_request_ttl_ms", OWTribeJoinRequests.TTL_MS);
        membership.addProperty("one_pending_invite_per_player", true);
        membership.addProperty("one_pending_request_per_player", true);
        membership.addProperty("requests_require_online_approver", true);
        membership.addProperty("approvers", "chief_and_deputies");
        membership.addProperty("invites_persisted", false);
        membership.addProperty("requests_persisted", false);
        membership.addProperty("checker_class", OWTribeJoinCheck.class.getName());

        JsonArray flows = new JsonArray();
        flows.add(flow("invite", "private_or_public", "owtribeaccept", "owtribedecline",
                lang, LANG_PREFIX + "invite.received"));
        flows.add(flow("direct_join", "public_direct", null, null,
                lang, LANG_PREFIX + "list.join"));
        flows.add(flow("request", "public_moderated", "owtribeapprove", "owtribereject",
                lang, LANG_PREFIX + "request.received"));
        membership.add("flows", flows);
        return membership;
    }

    private static JsonObject flow(String id, String applies, String acceptCommand, String rejectCommand,
                                   OWWikiLang lang, String messageKey) {
        JsonObject flow = new JsonObject();
        flow.addProperty("id", id);
        flow.addProperty("applies_to", applies);
        if (acceptCommand != null) flow.addProperty("accept_command", acceptCommand);
        if (rejectCommand != null) flow.addProperty("reject_command", rejectCommand);
        text(flow, "message", lang, messageKey);
        return flow;
    }

    private static JsonObject joinConditions(OWWikiLang lang) {
        JsonObject conditions = new JsonObject();
        conditions.addProperty("max_cumulated", OWTeam.MAX_JOIN_REQUIREMENTS);
        conditions.addProperty("combination", "all_required");
        conditions.addProperty("server_authoritative", true);
        conditions.addProperty("ticks_per_hour", TICKS_PER_HOUR);

        JsonArray retired = new JsonArray();
        retired.add(7);
        retired.add(12);
        conditions.add("retired_ids", retired);

        JsonArray values = new JsonArray();
        for (OWTribeJoinCondition condition : OWTribeJoinCondition.values()) {
            JsonObject entry = new JsonObject();
            entry.addProperty("name", condition.name());
            entry.addProperty("id", condition.getId());
            entry.addProperty("ordinal", condition.ordinal());
            entry.addProperty("has_threshold", condition.hasThreshold());
            entry.addProperty("default_threshold", condition.getDefaultThreshold());
            entry.addProperty("step", condition.getStep());
            entry.addProperty("min_threshold", condition.getMinThreshold());
            entry.addProperty("max_threshold", condition.getMaxThreshold());
            String key = LANG_PREFIX + "cond." + condition.name().toLowerCase(Locale.ROOT);
            text(entry, "label", lang, key);
            text(entry, "description", lang, key + ".desc");
            text(entry, "requirement", lang, key + ".req");
            values.add(entry);
        }
        conditions.add("values", values);
        return conditions;
    }

    private static JsonObject banner(OWWikiLang lang) {
        JsonObject banner = new JsonObject();
        banner.addProperty("shapes_texture", OWTeamBannerShape.TEXTURE.toString());
        banner.addProperty("texture_size", OWTeamBannerShape.TEXTURE_SIZE);
        banner.addProperty("base_width", OWTeamBannerShape.BASE_W);
        banner.addProperty("base_height", OWTeamBannerShape.BASE_H);
        banner.addProperty("unlock_storage_key", OWBannerUnlocks.KEY);
        banner.addProperty("unlock_storage", "player_persistent_bitmask");
        banner.addProperty("look_record", "OWBannerLook");

        JsonArray shapes = new JsonArray();
        for (OWTeamBannerShape shape : OWTeamBannerShape.values()) {
            JsonObject entry = new JsonObject();
            entry.addProperty("name", shape.name());
            entry.addProperty("id", shape.getId());
            entry.addProperty("price", shape.getPrice());
            entry.addProperty("purchasable", shape.isPurchasable());
            entry.addProperty("unlock_bit", 1 << shape.getId());
            text(entry, "label", lang, shape.getDisplayName());

            JsonObject atlas = new JsonObject();
            atlas.addProperty("base_u", shape.getBaseU());
            atlas.addProperty("base_v", shape.getBaseV());
            atlas.addProperty("highlight_u", shape.getHighlightU());
            atlas.addProperty("highlight_v", shape.getHighlightV());
            atlas.addProperty("highlight_width", shape.getHighlightW());
            atlas.addProperty("highlight_height", shape.getHighlightH());
            entry.add("atlas", atlas);
            shapes.add(entry);
        }
        banner.add("shapes", shapes);

        JsonArray patterns = new JsonArray();
        for (OWTeamMosaicPattern pattern : OWTeamMosaicPattern.values()) {
            JsonObject entry = new JsonObject();
            entry.addProperty("name", pattern.name());
            entry.addProperty("id", pattern.getId());
            entry.addProperty("unlockable", pattern.isUnlockable());
            entry.addProperty("custom_paint", pattern == OWTeamMosaicPattern.CUSTOM_PAINT);
            text(entry, "label", lang, pattern.getDisplayName());
            patterns.add(entry);
        }
        banner.add("patterns", patterns);

        JsonObject paint = new JsonObject();
        paint.addProperty("pixel_count", OWTeamMosaicPattern.CUSTOM_PAINT_PIXEL_COUNT);
        paint.addProperty("width", PAINT_WIDTH);
        paint.addProperty("height", PAINT_HEIGHT);
        paint.addProperty("color_slots", 3);
        paint.addProperty("bits_per_pixel", 2);
        paint.addProperty("pixels_per_byte", 4);
        paint.addProperty("packed_bytes", (OWTeamMosaicPattern.CUSTOM_PAINT_PIXEL_COUNT + 3) / 4);
        banner.add("paint", paint);

        JsonArray colors = new JsonArray();
        colors.add("primary");
        colors.add("secondary");
        colors.add("tertiary");
        banner.add("color_slots", colors);
        return banner;
    }

    private static JsonObject reputation(OWWikiLang lang) {
        JsonObject reputation = new JsonObject();
        reputation.addProperty("opt_in", true);
        reputation.addProperty("server_authoritative", true);
        reputation.addProperty("registry", OWReputationData.DATA_NAME);
        reputation.addProperty("override_disabled_value", -1);
        reputation.addProperty("formula",
                "round((somme(valeur_creatures(membre) + taming_xp(membre) * TAMING_WEIGHT)"
                        + " * (1 + SYNERGY_STEP * (membres_actifs - 1)) + bonus_arene) * multiplicateur_solo)");
        reputation.addProperty("entity_value_formula",
                "danger * (BASE_PER_ENTITY + niveau * LEVEL_WEIGHT)"
                        + " + (niveau >= 50 ? danger * MAX_LEVEL_BONUS : 0)");
        reputation.add("constants", OWWikiReflect.constants(OWReputation.class, true));

        JsonArray species = new JsonArray();
        for (OWWikiSpecies entry : OWWikiSpecies.all()) {
            JsonObject line = new JsonObject();
            line.addProperty("id", entry.key().toString());
            line.addProperty("path", entry.id());
            line.addProperty("danger", OWReputation.dangerOf(entry.id()));
            JsonObject byLevel = new JsonObject();
            for (int level : LEVEL_SAMPLES) {
                byLevel.addProperty(String.valueOf(level), OWReputation.entityValue(entry.id(), level));
            }
            line.add("value_by_level", byLevel);
            species.add(line);
        }
        reputation.add("species_values", species);

        JsonArray badges = new JsonArray();
        for (OWReputation.Badge badge : OWReputation.Badge.values()) {
            JsonObject entry = new JsonObject();
            entry.addProperty("name", badge.name());
            entry.addProperty("ordinal", badge.ordinal());
            entry.addProperty("threshold", badge.threshold());
            entry.addProperty("accent", hex(badge.accent()));
            entry.addProperty("has_sprite", badge.hasSprite());
            entry.addProperty("sprite_u", badge.u());
            entry.addProperty("sprite_v", badge.v());
            entry.addProperty("sprite_size", OWReputation.Badge.SPRITE_SIZE);
            text(entry, "label", lang, badge.translationKey());
            OWReputation.Badge next = OWReputation.nextBadge(badge);
            if (next != null) {
                entry.addProperty("next", next.name());
                entry.addProperty("next_threshold", next.threshold());
                entry.addProperty("span", next.threshold() - badge.threshold());
            }
            entry.addProperty("unlocks_arena", badge.ordinal() >= OWArena.ARENA_MIN_BADGE.ordinal());
            entry.addProperty("unlocks_tribe_chest", badge.ordinal() >= OWArena.TRIBE_CHEST_MIN_BADGE.ordinal());
            entry.addProperty("chest", OWArena.chestFor(badge).name());
            badges.add(entry);
        }
        reputation.add("badges", badges);

        JsonArray samples = new JsonArray();
        for (int value : REPUTATION_SAMPLES) {
            OWReputation.Badge badge = OWReputation.badgeFor(value);
            JsonObject entry = new JsonObject();
            entry.addProperty("reputation", value);
            entry.addProperty("badge", badge.name());
            entry.addProperty("progress", OWReputation.progress(value, badge));
            entry.addProperty("arena_unlocked", OWArena.arenaUnlocked(value));
            entry.addProperty("tribe_chest_unlocked", OWArena.tribeChestUnlocked(value));
            entry.addProperty("chest", OWArena.chestForReputation(value).name());
            samples.add(entry);
        }
        reputation.add("samples", samples);
        return reputation;
    }

    private static JsonObject champions(OWWikiLang lang) {
        JsonObject champions = new JsonObject();
        champions.addProperty("max", OWChampions.MAX_CHAMPIONS);
        champions.addProperty("named_by", "chief");
        champions.addProperty("carries_banner", true);
        champions.addProperty("independent_from_arena_roster", true);
        champions.addProperty("survives_unloaded_chunks", true);
        champions.addProperty("manager_class", "OWChampionManager");
        text(champions, "full_message", lang, LANG_PREFIX + "champions.full");

        JsonObject flag = new JsonObject();
        flag.addProperty("layer", "OWTribeFlagLayer");
        flag.addProperty("renderer", "OWTribeFlagRenderer");
        flag.addProperty("model_interface", "OWFlagModel");
        JsonArray bones = new JsonArray();
        bones.add("mainFlag");
        bones.add("flag");
        flag.add("bones", bones);
        champions.add("flag", flag);

        JsonArray carriers = new JsonArray();
        for (OWWikiSpecies species : OWWikiSpecies.all()) {
            Boolean carries = carriesFlag(species.stem());
            if (carries == null) continue;
            JsonObject entry = new JsonObject();
            entry.addProperty("id", species.key().toString());
            entry.addProperty("model", species.stem() + "Model");
            entry.addProperty("carries_flag", carries);
            carriers.add(entry);
        }
        if (!carriers.isEmpty()) champions.add("species", carriers);
        return champions;
    }

    private static Boolean carriesFlag(String stem) {
        Class<?> model = OWWikiReflect.classOrNull(MODEL_PACKAGE + stem + "Model");
        if (model == null) return null;
        try {
            for (Class<?> implemented : model.getInterfaces()) {
                if (implemented.getSimpleName().equals("OWFlagModel")) return Boolean.TRUE;
            }
            return Boolean.FALSE;
        } catch (Throwable ignored) {
            return null;
        }
    }

    private static JsonObject arena(HolderLookup.Provider registries, OWWikiLang lang) {
        JsonObject arena = new JsonObject();
        arena.add("access", arenaAccess(lang));
        arena.add("terrains", terrains(lang));
        arena.add("venues", venues(lang));
        arena.add("match", match(lang));
        arena.add("dimension", dimension());
        arena.add("prestige", prestige());
        arena.add("chests", chests(registries, lang));
        arena.add("archetypes", archetypes(lang));
        arena.add("roster", roster());
        return arena;
    }

    private static JsonObject arenaAccess(OWWikiLang lang) {
        JsonObject access = new JsonObject();
        access.addProperty("min_badge", OWArena.ARENA_MIN_BADGE.name());
        access.addProperty("min_reputation", OWArena.ARENA_MIN_BADGE.threshold());
        access.addProperty("requires_chief_acceptance", true);
        access.addProperty("tribe_chest_min_badge", OWArena.TRIBE_CHEST_MIN_BADGE.name());
        access.addProperty("tribe_chest_min_reputation", OWArena.TRIBE_CHEST_MIN_BADGE.threshold());
        access.addProperty("chests_per_tribe_chest", OWArena.CHESTS_PER_TRIBE_CHEST);
        access.addProperty("challenge_ttl_ms", OWArenaChallenges.TTL_MS);
        access.addProperty("challenge_issued_by", "chief");
        access.addProperty("one_match_at_a_time", true);
        return access;
    }

    private static JsonArray terrains(OWWikiLang lang) {
        JsonArray terrains = new JsonArray();
        for (OWArena.Terrain terrain : OWArena.Terrain.values()) {
            JsonObject entry = new JsonObject();
            entry.addProperty("name", terrain.name());
            entry.addProperty("ordinal", terrain.ordinal());
            entry.addProperty("bit", terrain.bit());
            entry.addProperty("chosen_by", "challenger");
            text(entry, "label", lang, terrain.translationKey());
            text(entry, "unfit_message", lang, terrain.unfitKey());
            entry.addProperty("offset_x", OWArena.offsetXFor(terrain));
            entry.addProperty("spawn_y", OWArena.spawnY(terrain));
            entry.addProperty("fighter_z", OWArena.fighterZ(terrain));
            entry.addProperty("chief_z", OWArena.chiefZ(terrain));
            entry.addProperty("border_start", OWArena.borderStart(terrain));
            entry.addProperty("default_venue", OWArenaVenue.defaultFor(terrain).name());
            entry.addProperty("biome", terrain == OWArena.Terrain.AQUATIC
                    ? OWBiomes.ARENA_AQUATIC_BIOME.location().toString()
                    : OWBiomes.ARENA_BIOME.location().toString());
            terrains.add(entry);
        }
        return terrains;
    }

    private static JsonArray venues(OWWikiLang lang) {
        JsonArray venues = new JsonArray();
        for (OWArenaVenue venue : OWArenaVenue.values()) {
            JsonObject entry = new JsonObject();
            entry.addProperty("name", venue.name());
            entry.addProperty("id", venue.getId());
            entry.addProperty("terrain", venue.getTerrain().name());
            entry.addProperty("price", venue.getPrice());
            entry.addProperty("purchasable", venue.isPurchasable());
            entry.addProperty("unlock_bit", 1 << venue.getId());
            entry.addProperty("default_for_terrain", OWArenaVenue.defaultFor(venue.getTerrain()) == venue);
            entry.addProperty("texture", venue.getTexture().toString());
            entry.addProperty("display_texture", venue.getDisplayTexture().toString());
            entry.addProperty("texture_aspect", OWArenaVenue.TEXTURE_ASPECT);
            text(entry, "label", lang, venue.getTranslationKey());
            venues.add(entry);
        }
        return venues;
    }

    private static JsonObject match(OWWikiLang lang) {
        JsonObject match = new JsonObject();
        match.addProperty("max_fighters", OWArena.MAX_FIGHTERS);
        match.addProperty("distinct_archetypes_required", true);
        match.addProperty("fighter_snapshot_record", "OWArenaFighter");

        JsonArray phases = new JsonArray();
        for (OWArena.Phase phase : OWArena.Phase.values()) phases.add(phase.name());
        match.add("client_phases", phases);

        JsonArray states = new JsonArray();
        for (OWArenaMatch.State state : OWArenaMatch.State.values()) states.add(state.name());
        match.add("server_states", states);

        JsonArray results = new JsonArray();
        for (OWArena.Result result : OWArena.Result.values()) results.add(result.name());
        match.add("results", results);

        JsonObject timings = new JsonObject();
        timings.addProperty("selection_timeout_ms", OWArena.SELECTION_TIMEOUT_MS);
        timings.addProperty("opening_freeze_ms", OWArena.OPENING_FREEZE_MS);
        timings.addProperty("fight_grace_ms", OWArena.FIGHT_GRACE_MS);
        timings.addProperty("fight_timeout_ms", OWArena.FIGHT_TIMEOUT_MS);
        timings.addProperty("ended_linger_ms", OWArena.ENDED_LINGER_MS);
        timings.addProperty("missing_tolerance", OWArena.MISSING_TOLERANCE);
        match.add("timings", timings);

        JsonObject border = new JsonObject();
        border.addProperty("start", OWArena.BORDER_START);
        border.addProperty("start_aquatic", OWArena.ARENA_AQUATIC_BORDER_START);
        border.addProperty("end", OWArena.BORDER_END);
        border.addProperty("hold_ms", OWArena.BORDER_HOLD_MS);
        border.addProperty("shrink_ms", OWArena.BORDER_SHRINK_MS);
        match.add("border", border);

        JsonObject death = new JsonObject();
        death.addProperty("drops_soul", true);
        death.addProperty("offline_soul_storage", OWPendingSouls.DATA_NAME);
        text(death, "soul_returned", lang, LANG_PREFIX + "arena.soul_returned");
        match.add("death", death);
        return match;
    }

    private static JsonObject dimension() {
        JsonObject dimension = new JsonObject();
        dimension.addProperty("id", OWDimensions.ARENA.location().toString());
        dimension.addProperty("defined_by", "datapack");
        dimension.addProperty("radius", OWArena.ARENA_RADIUS);
        dimension.addProperty("y", OWArena.ARENA_Y);
        dimension.addProperty("floor_y", OWArena.ARENA_FLOOR_Y);
        dimension.addProperty("fighter_spread_x", OWArena.ARENA_FIGHTER_SPREAD);
        dimension.addProperty("aquatic_offset_x", OWArena.ARENA_AQUATIC_OFFSET_X);
        dimension.addProperty("aquatic_spawn_up", OWArena.ARENA_AQUATIC_SPAWN_UP);
        dimension.addProperty("terrain_both_mask", OWArena.TERRAIN_BOTH);
        dimension.addProperty("structure_naming_terrestrial", "arena_<col>_<row>_<layer>");
        dimension.addProperty("structure_naming_aquatic", "arena_aq_<col>_<row>_<layer>");
        dimension.addProperty("builder", "OWArenaBuilder");
        dimension.addProperty("protection", "OWArenaProtection");
        return dimension;
    }

    private static JsonObject prestige() {
        JsonObject prestige = new JsonObject();
        prestige.addProperty("shared_by_tribe", true);
        prestige.addProperty("per_chest", OWArena.PRESTIGE_PER_CHEST);
        prestige.addProperty("min_gain", OWArena.PRESTIGE_MIN);
        prestige.addProperty("max_gain", OWArena.PRESTIGE_MAX);
        prestige.addProperty("gain_formula",
                "clamp(round((reputation_adverse - reputation_propre) / 1000 * 2 * 40), 10, 300)");
        prestige.addProperty("consolation_formula", "max(1, round(gain_vainqueur * 0.25))");
        prestige.addProperty("reputation_min_gain", OWArena.MIN_REPUTATION_GAIN);
        prestige.addProperty("reputation_gain_formula", "max(100, reputation_perdant - reputation_gagnant)");
        prestige.addProperty("chests_claimed_per_member", true);

        JsonArray samples = new JsonArray();
        for (int delta : PRESTIGE_DELTA_SAMPLES) {
            int gain = OWArena.prestigeGain(0, delta);
            JsonObject entry = new JsonObject();
            entry.addProperty("reputation_delta", delta);
            entry.addProperty("winner_prestige", gain);
            entry.addProperty("loser_prestige", OWArena.prestigeConsolation(gain));
            entry.addProperty("winner_reputation", OWArena.reputationGain(0, Math.max(0, delta)));
            samples.add(entry);
        }
        prestige.add("samples", samples);

        JsonArray tiers = new JsonArray();
        for (int index = 1; index <= 10; index++) {
            JsonObject entry = new JsonObject();
            entry.addProperty("chest_index", index);
            entry.addProperty("prestige_required", index * OWArena.PRESTIGE_PER_CHEST);
            entry.addProperty("tribe_chests_earned", OWArena.pendingTribeChests(index, 0));
            tiers.add(entry);
        }
        prestige.add("tiers", tiers);
        return prestige;
    }

    private static JsonArray chests(HolderLookup.Provider registries, OWWikiLang lang) {
        Map<ResourceLocation, JsonObject> tables = chestLoot(registries);
        JsonArray chests = new JsonArray();
        for (OWArena.Chest chest : OWArena.Chest.values()) {
            JsonObject entry = new JsonObject();
            entry.addProperty("name", chest.name());
            entry.addProperty("ordinal", chest.ordinal());
            entry.addProperty("material", chest.material().name());
            entry.addProperty("size", chest.size().name());
            entry.addProperty("accent", hex(chest.accent()));
            entry.addProperty("min_coins", chest.minCoins());
            entry.addProperty("max_coins", chest.maxCoins());
            entry.addProperty("loot_table", chest.lootTable().location().toString());
            entry.addProperty("loot_path", chest.lootPath());
            text(entry, "label", lang, chest.translationKey());

            JsonArray badges = new JsonArray();
            for (OWReputation.Badge badge : OWReputation.Badge.values()) {
                if (badge != OWReputation.Badge.NONE && OWArena.chestFor(badge) == chest) badges.add(badge.name());
            }
            if (!badges.isEmpty()) entry.add("badges", badges);

            JsonObject table = tables.get(chest.lootTable().location());
            if (table != null) {
                JsonArray drops = OWWikiLoot.summarize(table);
                for (JsonElement element : drops) {
                    if (!element.isJsonObject()) continue;
                    JsonObject drop = element.getAsJsonObject();
                    Item item = itemOf(drop);
                    if (item != null) drop.addProperty("rare", OWArena.isRareReward(chest, item));
                }
                entry.add("loot", drops);
                if (table.has("pools")) entry.addProperty("pool_count", table.getAsJsonArray("pools").size());
            }
            chests.add(entry);
        }
        return chests;
    }

    private static Item itemOf(JsonObject drop) {
        if (!drop.has("item")) return null;
        try {
            ResourceLocation id = ResourceLocation.tryParse(drop.get("item").getAsString());
            return id == null ? null : BuiltInRegistries.ITEM.getOptional(id).orElse(null);
        } catch (Throwable ignored) {
            return null;
        }
    }

    private static Map<ResourceLocation, JsonObject> chestLoot(HolderLookup.Provider registries) {
        Map<ResourceLocation, JsonObject> tables = new LinkedHashMap<>();
        DynamicOps<JsonElement> ops = registries.createSerializationContext(JsonOps.INSTANCE);
        try {
            new OWArenaChestLootProvider(registries).generate((key, builder) -> {
                try {
                    LootTable table = builder.setParamSet(LootContextParamSets.CHEST).build();
                    LootTable.DIRECT_CODEC.encodeStart(ops, table)
                            .resultOrPartial(error -> {})
                            .ifPresent(json -> tables.put(key.location(), json.getAsJsonObject()));
                } catch (Throwable ignored) {
                }
            });
        } catch (Throwable ignored) {
        }
        return tables;
    }

    private static JsonArray archetypes(OWWikiLang lang) {
        JsonArray archetypes = new JsonArray();
        for (OWEntityConfig.Archetypes archetype : OWEntityConfig.Archetypes.values()) {
            JsonObject entry = new JsonObject();
            entry.addProperty("name", archetype.name());
            entry.addProperty("ordinal", archetype.ordinal());
            entry.addProperty("one_per_side", true);
            text(entry, "label", lang,
                    LANG_PREFIX + "arena.archetype." + archetype.name().toLowerCase(Locale.ROOT));
            archetypes.add(entry);
        }
        return archetypes;
    }

    private static JsonArray roster() {
        JsonArray roster = new JsonArray();
        for (OWWikiSpecies species : OWWikiSpecies.all()) {
            OWEntity probe = OWWikiReflect.probe(species.type(), species.implementation());
            JsonObject entry = new JsonObject();
            entry.addProperty("id", species.key().toString());
            entry.addProperty("path", species.id());
            entry.addProperty("probe_resolved", probe != null);

            Object mask = OWWikiReflect.call(probe, "arenaTerrainMask").orElse(null);
            if (mask instanceof Number number) {
                entry.addProperty("terrain_mask", number.intValue());
                JsonArray fits = new JsonArray();
                for (OWArena.Terrain terrain : OWArena.Terrain.values()) {
                    if (OWArena.fitsTerrain(number.intValue(), terrain)) fits.add(terrain.name());
                }
                entry.add("terrains", fits);
            }
            OWWikiReflect.putCall(entry, "fights_in_arena", probe, "canFightInArena");

            Object archetype = OWWikiReflect.call(probe, "getArchetype").orElse(null);
            if (archetype instanceof Enum<?> value) entry.addProperty("archetype", value.name());
            entry.addProperty("danger", OWReputation.dangerOf(species.id()));
            roster.add(entry);
        }
        return roster;
    }

    private static JsonObject economy() {
        JsonObject economy = new JsonObject();
        economy.addProperty("currency", "wild_coins");
        economy.addProperty("currency_key", OWCurrency.WILD_COINS_KEY);
        economy.addProperty("scope", "per_player");
        economy.addProperty("banner_unlock_key", OWBannerUnlocks.KEY);
        economy.addProperty("venue_unlock_key", OWArenaVenueUnlocks.KEY);

        JsonArray purchases = new JsonArray();
        for (OWTeamBannerShape shape : OWTeamBannerShape.values()) {
            if (!shape.isPurchasable()) continue;
            purchases.add(purchase("banner_shape", shape.name(), shape.getPrice(), shape.getDisplayName()));
        }
        for (OWArenaVenue venue : OWArenaVenue.values()) {
            if (!venue.isPurchasable()) continue;
            purchases.add(purchase("arena_venue", venue.name(), venue.getPrice(), venue.getTranslationKey()));
        }
        economy.add("purchases", purchases);

        JsonArray income = new JsonArray();
        for (OWArena.Chest chest : OWArena.Chest.values()) {
            JsonObject entry = new JsonObject();
            entry.addProperty("source", "arena_chest");
            entry.addProperty("chest", chest.name());
            entry.addProperty("min_coins", chest.minCoins());
            entry.addProperty("max_coins", chest.maxCoins());
            income.add(entry);
        }
        economy.add("income", income);
        return economy;
    }

    private static JsonObject purchase(String kind, String name, int price, String labelKey) {
        JsonObject entry = new JsonObject();
        entry.addProperty("kind", kind);
        entry.addProperty("name", name);
        entry.addProperty("price", price);
        entry.addProperty("label_key", labelKey);
        return entry;
    }

    private static JsonArray commands() {
        JsonArray commands = new JsonArray();
        command(commands, "owtribeaccept", "/owtribeaccept", 0, "player",
                "Accepter l'invitation de tribu en attente");
        command(commands, "owtribedecline", "/owtribedecline", 0, "player",
                "Refuser l'invitation de tribu en attente");
        command(commands, "owtribeapprove", "/owtribeapprove <joueur>", 0, "chief_or_deputy",
                "Valider une demande d'adhesion");
        command(commands, "owtribereject", "/owtribereject <joueur>", 0, "chief_or_deputy",
                "Refuser une demande d'adhesion");
        command(commands, "owteamaccept", "/owteamaccept", 0, "player",
                "Accepter une invitation de l'ancien systeme d'equipe");
        command(commands, "owteamdecline", "/owteamdecline", 0, "player",
                "Refuser une invitation de l'ancien systeme d'equipe");
        command(commands, "owtribetestmembers", "/owtribetestmembers", 0, "debug",
                "Peupler sa tribu de faux membres");
        command(commands, "owtriberemovetestmembers", "/owtriberemovetestmembers", 0, "debug",
                "Retirer les faux membres de test");
        command(commands, "owresetbannerunlocks", "/owresetbannerunlocks", 0, "debug",
                "Reinitialiser les achats de formes de banniere");
        command(commands, "owtribewipe", "/owtribewipe", 2, "admin",
                "Supprimer toutes les tribus du serveur");
        command(commands, "owtribereputation", "/owtribereputation set|clear <joueur> [montant]", 2, "admin",
                "Forcer ou liberer la reputation d'une tribu");
        command(commands, "owarenaprestige", "/owarenaprestige set|add [joueur] <montant>", 2, "admin",
                "Regler le prestige d'arene d'une tribu");
        command(commands, "owarenaspar", "/owarenaspar start [combattants] [terrestrial|aquatic]", 2, "admin",
                "Lancer un combat d'entrainement en arene");
        command(commands, "owarenarebuild", "/owarenarebuild", 2, "admin",
                "Reconstruire le decor de l'arene");
        command(commands, "owarenaexport", "/owarenaexport [terrestrial|aquatic]", 2, "admin",
                "Exporter le decor de l'arene en structures");
        return commands;
    }

    private static void command(JsonArray target, String name, String usage, int permission,
                                String scope, String summary) {
        JsonObject entry = new JsonObject();
        entry.addProperty("name", name);
        entry.addProperty("usage", usage);
        entry.addProperty("permission_level", permission);
        entry.addProperty("scope", scope);
        entry.addProperty("summary", summary);
        target.add(entry);
    }

    private static JsonObject persistence() {
        JsonObject persistence = new JsonObject();

        JsonArray saved = new JsonArray();
        saved.add(savedData(OWTribesSavedData.DATA_NAME, "OWTribesSavedData",
                "Registre de toutes les tribus du serveur"));
        saved.add(savedData(OWReputationData.DATA_NAME, "OWReputationData",
                "Creatures suivies et experience d'apprivoisement par joueur"));
        saved.add(savedData(OWPendingSouls.DATA_NAME, "OWPendingSouls",
                "Ames d'arene en attente de remise"));
        persistence.add("saved_data", saved);

        JsonArray playerKeys = new JsonArray();
        playerKeys.add(playerKey(OWCurrency.WILD_COINS_KEY, "int", "Porte-monnaie en Pieces Sauvages"));
        playerKeys.add(playerKey(OWBannerUnlocks.KEY, "bitmask", "Formes de banniere achetees"));
        playerKeys.add(playerKey(OWArenaVenueUnlocks.KEY, "bitmask", "Decors d'arene achetes"));
        persistence.add("player_persistent_keys", playerKeys);

        JsonArray stores = new JsonArray();
        stores.add("OWTribeInvites");
        stores.add("OWTribeJoinRequests");
        stores.add("OWArenaChallenges");
        stores.add("OWTeamInvites");
        persistence.add("transient_stores", stores);
        return persistence;
    }

    private static JsonObject savedData(String name, String type, String summary) {
        JsonObject entry = new JsonObject();
        entry.addProperty("name", name);
        entry.addProperty("class", type);
        entry.addProperty("level", "overworld");
        entry.addProperty("summary", summary);
        return entry;
    }

    private static JsonObject playerKey(String key, String type, String summary) {
        JsonObject entry = new JsonObject();
        entry.addProperty("key", key);
        entry.addProperty("type", type);
        entry.addProperty("summary", summary);
        return entry;
    }

    private static JsonObject networking(OWWikiSources sources) {
        JsonObject networking = new JsonObject();
        JsonArray toClient = filtered(sources.classNames(PACKET_TO_CLIENT_PACKAGE));
        JsonArray toServer = filtered(sources.classNames(PACKET_TO_SERVER_PACKAGE));
        if (toClient != null) networking.add("to_client", toClient);
        if (toServer != null) networking.add("to_server", toServer);
        return networking;
    }

    private static JsonArray filtered(JsonArray names) {
        if (names == null) return null;
        JsonArray kept = new JsonArray();
        for (JsonElement element : names) {
            String name = element.getAsString();
            for (String keyword : PACKET_KEYWORDS) {
                if (name.contains(keyword)) {
                    kept.add(name);
                    break;
                }
            }
        }
        return kept.isEmpty() ? null : kept;
    }

    private static JsonObject constants() {
        JsonObject constants = new JsonObject();
        constants.add("arena", OWWikiReflect.constants(OWArena.class, true));
        constants.add("arena_challenges", OWWikiReflect.constants(OWArenaChallenges.class, true));
        constants.add("arena_venue", OWWikiReflect.constants(OWArenaVenue.class, true));
        constants.add("badge", OWWikiReflect.constants(OWReputation.Badge.class, true));
        constants.add("banner_shape", OWWikiReflect.constants(OWTeamBannerShape.class, true));
        constants.add("banner_unlocks", OWWikiReflect.constants(OWBannerUnlocks.class, true));
        constants.add("champions", OWWikiReflect.constants(OWChampions.class, true));
        constants.add("currency", OWWikiReflect.constants(OWCurrency.class, true));
        constants.add("join_check", OWWikiReflect.constants(OWTribeJoinCheck.class, true));
        constants.add("mosaic_pattern", OWWikiReflect.constants(OWTeamMosaicPattern.class, true));
        constants.add("permissions", OWWikiReflect.constants(OWTribePermission.class, true));
        constants.add("reputation", OWWikiReflect.constants(OWReputation.class, true));
        constants.add("team", OWWikiReflect.constants(OWTeam.class, true));
        constants.add("tribe_invites", OWWikiReflect.constants(OWTribeInvites.class, true));
        constants.add("tribe_join_requests", OWWikiReflect.constants(OWTribeJoinRequests.class, true));
        constants.add("venue_unlocks", OWWikiReflect.constants(OWArenaVenueUnlocks.class, true));
        return constants;
    }

    private static JsonObject langDump(OWWikiLang lang) {
        JsonObject dump = new JsonObject();
        dump.addProperty("prefix", LANG_PREFIX);

        List<String> keys = new ArrayList<>(lang.keysWithPrefix(LANG_PREFIX));
        keys.sort(String::compareTo);
        dump.addProperty("count", keys.size());

        Map<String, JsonArray> groups = new LinkedHashMap<>();
        for (String key : keys) {
            JsonObject entry = lang.entry(key);
            if (entry == null) continue;
            groups.computeIfAbsent(groupOf(key), name -> new JsonArray()).add(entry);
        }
        JsonObject grouped = new JsonObject();
        groups.forEach(grouped::add);
        dump.add("groups", grouped);
        return dump;
    }

    private static String groupOf(String key) {
        String rest = key.substring(LANG_PREFIX.length());
        int dot = rest.indexOf('.');
        return dot > 0 ? rest.substring(0, dot) : "root";
    }

    private static OWTeam blankTeam() {
        return new OWTeam(0, "", null, 0xFFFFFF, new UUID[0], new OWEntity[0], "");
    }

    private static void text(JsonObject target, String property, OWWikiLang lang, String key) {
        JsonObject entry = lang.entry(key);
        if (entry != null) target.add(property, entry);
        else target.addProperty(property + "_key", key);
    }

    private static String hex(int color) {
        return String.format(Locale.ROOT, "#%06X", color & 0xFFFFFF);
    }
}
