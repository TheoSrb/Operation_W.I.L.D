package net.tiew.operationWild.gametest;

import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestGenerator;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.TestFunction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.animal.Cow;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import net.tiew.operationWild.entity.OWEntityRegistry;
import net.tiew.operationWild.entity.animals.terrestrial.LionEntity;
import net.tiew.operationWild.entity.animals.terrestrial.TigerEntity;
import net.tiew.operationWild.entity.clan.LionClanData;
import net.tiew.operationWild.entity.clan.LionHunt;
import net.tiew.operationWild.entity.clan.LionPride;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;

@GameTestHolder("ow")
@PrefixGameTestTemplate(false)
public class LionWildBench {

    private static final String TEMPLATE = "ow:lion_bench";

    private static TestFunction test(String id, int maxTicks, Consumer<GameTestHelper> body) {
        return new TestFunction(id, id, TEMPLATE, maxTicks, 0L, true, body);
    }

    @GameTestGenerator
    public static Collection<TestFunction> generate() {
        List<TestFunction> tests = new ArrayList<>();
        tests.add(test("lion_core_attack", 800, LionWildBench::coreAttack));
        tests.add(test("lion_warn_then_attack", 900, LionWildBench::warnThenAttack));
        tests.add(test("lion_heat_pile", 900, LionWildBench::heatPile));
        tests.add(test("lion_perch", 1400, LionWildBench::perch));
        tests.add(test("lion_hunt_starving", 3600, LionWildBench::huntStarving));
        tests.add(test("lion_feast_hierarchy", 1600, LionWildBench::feastHierarchy));
        tests.add(test("lion_chorus_war", 2400, LionWildBench::chorusWar));
        tests.add(test("lion_nomad_takeover", 2400, LionWildBench::nomadTakeover));
        tests.add(test("lion_rival_predator", 600, LionWildBench::rivalPredator));
        tests.add(test("lion_coalition", 1200, LionWildBench::coalition));
        tests.add(test("lion_watch_check", 100, LionWildBench::watchCheck));
        return tests;
    }

    private static BlockPos origin(GameTestHelper helper) {
        AABB bounds = helper.getBounds();
        return BlockPos.containing(bounds.minX, bounds.minY, bounds.minZ);
    }

    private static BlockPos center(GameTestHelper helper) {
        return origin(helper).offset(48, 1, 48);
    }

    private static void arena(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos o = origin(helper);
        for (int x = 0; x < 96; x++) {
            for (int z = 0; z < 96; z++) {
                level.setBlockAndUpdate(o.offset(x, 0, z), Blocks.GRASS_BLOCK.defaultBlockState());
                if (x == 0 || z == 0 || x == 95 || z == 95) {
                    for (int y = 1; y <= 8; y++) level.setBlockAndUpdate(o.offset(x, y, z), Blocks.BARRIER.defaultBlockState());
                }
            }
        }
    }

    private static void holdTime(GameTestHelper helper, long dayTime) {
        helper.getLevel().setDayTime(dayTime);
        helper.onEachTick(() -> helper.getLevel().setDayTime(dayTime));
    }

    private static LionEntity lion(GameTestHelper helper, BlockPos pos, boolean male) {
        ServerLevel level = helper.getLevel();
        LionEntity lion = OWEntityRegistry.LION.get().create(level);
        lion.moveTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, level.random.nextFloat() * 360f, 0f);
        lion.finalizeSpawn(level, level.getCurrentDifficultyAt(pos), MobSpawnType.COMMAND, null);
        lion.setGender(male ? 1 : 0);
        try {
            Method method = LionEntity.class.getDeclaredMethod("applySexAttributes");
            method.setAccessible(true);
            method.invoke(lion);
        } catch (ReflectiveOperationException exception) {
            throw new RuntimeException(exception);
        }
        lion.setPersistenceRequired();
        level.addFreshEntity(lion);
        return lion;
    }

    private static ServerPlayer player(GameTestHelper helper, Vec3 at, float yaw) {
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        player.setGameMode(GameType.SURVIVAL);
        player.setInvulnerable(true);
        player.moveTo(at.x, at.y, at.z, yaw, 0f);
        player.setYHeadRot(yaw);
        return player;
    }

    private static void setSatiety(GameTestHelper helper, LionEntity member, float ratio) {
        helper.runAfterDelay(25, () -> {
            LionClanData.Clan clan = member.wildClan();
            if (clan != null) clan.satiety = (int) (LionClanData.MAX_SATIETY * ratio);
        });
    }

    private static void log(String id, String message) {
        System.out.println("[LION-BENCH] " + id + " | " + message);
    }

    private static String describe(LionEntity lion) {
        return String.format("%s pos=(%.1f,%.1f,%.1f) target=%s nap=%s sit=%s pose=%d clan=%s",
                lion.isMale() ? "M" : "F", lion.getX(), lion.getY(), lion.getZ(),
                lion.getTarget() == null ? "-" : lion.getTarget().getType().toShortString(),
                lion.isNapping(), lion.isSitting(), lion.getWildPose(),
                lion.getClanId() == null ? "-" : lion.getClanId().toString().substring(0, 6));
    }

    private static void coreAttack(GameTestHelper helper) {
        arena(helper);
        holdTime(helper, 11000L);
        BlockPos c = center(helper);
        LionEntity male = lion(helper, c, true);
        LionEntity a = lion(helper, c.offset(2, 0, 1), false);
        LionEntity b = lion(helper, c.offset(-2, 0, 1), false);
        setSatiety(helper, male, 0.4f);
        ServerPlayer[] intruder = new ServerPlayer[1];
        helper.runAfterDelay(60, () -> intruder[0] = player(helper, Vec3.atBottomCenterOf(c.offset(6, 0, 0)), 90f));
        helper.onEachTick(() -> {
            if (helper.getTick() % 100 == 0) {
                log("core_attack", "t=" + helper.getTick() + " " + describe(male) + " || " + describe(a) + " || " + describe(b));
                LionClanData.Clan clan = male.wildClan();
                if (clan != null && intruder[0] != null) log("core_attack", "gauge=" + clan.state.intrusion.get(intruder[0].getUUID()) + " home=" + clan.home);
            }
        });
        helper.succeedWhen(() -> {
            helper.assertTrue(intruder[0] != null, "pas encore de joueur");
            helper.assertTrue(a.getTarget() == intruder[0] || b.getTarget() == intruder[0], "aucune lionne ne cible l'intrus");
            log("core_attack", "OK au tick " + helper.getTick());
        });
    }

    private static void warnThenAttack(GameTestHelper helper) {
        arena(helper);
        holdTime(helper, 11000L);
        BlockPos c = center(helper);
        LionEntity male = lion(helper, c, true);
        LionEntity a = lion(helper, c.offset(1, 0, 2), false);
        LionEntity b = lion(helper, c.offset(-1, 0, 2), false);
        setSatiety(helper, male, 0.4f);
        ServerPlayer[] intruder = new ServerPlayer[1];
        boolean[] warned = new boolean[1];
        helper.runAfterDelay(60, () -> intruder[0] = player(helper, Vec3.atBottomCenterOf(c.offset(19, 0, 0)), 90f));
        helper.onEachTick(() -> {
            if (intruder[0] == null) return;
            for (LionEntity lion : List.of(male, a, b)) {
                if (lion.wild.warnTarget() == intruder[0] && lion.getTarget() == null) {
                    if (!warned[0]) log("warn", "avertissement au tick " + helper.getTick() + " pose=" + lion.getWildPose());
                    warned[0] = true;
                }
            }
            if (helper.getTick() % 100 == 0) {
                LionClanData.Clan clan = male.wildClan();
                log("warn", "t=" + helper.getTick() + " gauge=" + (clan == null ? "?" : clan.state.intrusion.get(intruder[0].getUUID()))
                        + " " + describe(a) + " || " + describe(b));
            }
        });
        helper.succeedWhen(() -> {
            helper.assertTrue(warned[0], "aucun avertissement");
            helper.assertTrue(intruder[0] != null && (a.getTarget() == intruder[0] || b.getTarget() == intruder[0]), "pas encore d'attaque");
            log("warn", "attaque au tick " + helper.getTick());
        });
    }

    private static void heatPile(GameTestHelper helper) {
        arena(helper);
        holdTime(helper, 6000L);
        BlockPos c = center(helper);
        LionEntity male = lion(helper, c, true);
        List<LionEntity> lionesses = List.of(lion(helper, c.offset(5, 0, 3), false), lion(helper, c.offset(-6, 0, 2), false),
                lion(helper, c.offset(3, 0, -7), false));
        setSatiety(helper, male, 0.9f);
        helper.onEachTick(() -> {
            if (helper.getTick() % 150 == 0) {
                StringBuilder line = new StringBuilder(describe(male));
                for (LionEntity lioness : lionesses) line.append(" || ").append(describe(lioness));
                log("heat_pile", "t=" + helper.getTick() + " " + line);
            }
        });
        helper.succeedWhen(() -> {
            LionClanData.Clan clan = male.wildClan();
            helper.assertTrue(clan != null && clan.home != null, "pas de territoire");
            Vec3 home = LionPride.homeCenter(clan);
            long resting = lionesses.stream().filter(l -> l.isNapping() && l.position().distanceTo(home) < 4.5).count();
            helper.assertTrue(resting >= 3 && male.isNapping(), "tas incomplet : " + resting);
            log("heat_pile", "tas formé au tick " + helper.getTick() + " (" + resting + " lionnes + mâle)");
        });
    }

    private static void perch(GameTestHelper helper) {
        arena(helper);
        holdTime(helper, 2000L);
        ServerLevel level = helper.getLevel();
        BlockPos c = center(helper);
        BlockPos rock = c.offset(9, 0, 0);
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                for (int y = 0; y < 3; y++) level.setBlockAndUpdate(rock.offset(dx, y, dz), Blocks.STONE.defaultBlockState());
            }
        }
        level.setBlockAndUpdate(rock.offset(-2, 0, 0), Blocks.STONE.defaultBlockState());
        level.setBlockAndUpdate(rock.offset(-3, 0, 0), Blocks.STONE.defaultBlockState());
        level.setBlockAndUpdate(rock.offset(-2, 1, 0), Blocks.STONE.defaultBlockState());
        LionEntity male = lion(helper, c, true);
        LionEntity a = lion(helper, c.offset(-3, 0, 2), false);
        setSatiety(helper, male, 0.5f);
        helper.onEachTick(() -> {
            if (helper.getTick() % 150 == 0) log("perch", "t=" + helper.getTick() + " " + describe(male) + " watch=" + male.wild.perchedWatch);
        });
        helper.succeedWhen(() -> {
            helper.assertTrue(male.isSitting() && male.wild.perchedWatch && male.getY() >= rock.getY() + 2.5, "pas sur le promontoire");
            log("perch", "perché au tick " + helper.getTick() + " y=" + male.getY());
        });
    }

    private static void huntStarving(GameTestHelper helper) {
        arena(helper);
        holdTime(helper, 15000L);
        ServerLevel level = helper.getLevel();
        BlockPos c = center(helper);
        LionEntity male = lion(helper, c, true);
        List<LionEntity> lionesses = List.of(lion(helper, c.offset(2, 0, 2), false), lion(helper, c.offset(-2, 0, 2), false),
                lion(helper, c.offset(0, 0, -3), false));
        setSatiety(helper, male, 0.1f);
        List<Cow> cows = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            Cow cow = EntityType.COW.create(level);
            cow.moveTo(c.getX() + 22 + i * 2, c.getY(), c.getZ() + 6 + i, 0f, 0f);
            level.addFreshEntity(cow);
            cows.add(cow);
        }
        int[] seen = new int[4];
        float[] satietyAtStart = new float[]{-1f};
        helper.onEachTick(() -> {
            LionClanData.Clan clan = male.wildClan();
            if (clan == null) return;
            if (satietyAtStart[0] < 0f && helper.getTick() > 30) satietyAtStart[0] = clan.satietyRatio();
            LionHunt hunt = clan.state.hunt;
            if (hunt != null && seen[0] == 0) {
                seen[0] = 1;
                log("hunt", "chasse planifiée au tick " + helper.getTick() + " roles=" + hunt.roles.values());
            }
            if (hunt != null && hunt.phase() == LionHunt.Phase.DRIVE && seen[1] == 0) {
                seen[1] = 1;
                log("hunt", "rabattage au tick " + helper.getTick());
            }
            if (hunt != null && hunt.phase() == LionHunt.Phase.STRIKE && seen[2] == 0) {
                seen[2] = 1;
                log("hunt", "assaut au tick " + helper.getTick());
            }
            for (LionEntity lioness : lionesses) {
                if (lioness.getWildPose() == LionEntity.WILD_POSE_STALK && lioness.getDeltaMovement().horizontalDistanceSqr() < 1.0E-5) seen[3]++;
            }
            if (helper.getTick() % 200 == 0) {
                StringBuilder line = new StringBuilder(describe(male));
                for (LionEntity lioness : lionesses) line.append(" || ").append(describe(lioness));
                log("hunt", "t=" + helper.getTick() + " satiety=" + String.format("%.2f", clan.satietyRatio())
                        + " cows=" + cows.stream().filter(Cow::isAlive).count() + " " + line);
            }
        });
        helper.succeedWhen(() -> {
            LionClanData.Clan clan = male.wildClan();
            helper.assertTrue(clan != null, "pas de clan");
            helper.assertTrue(cows.stream().anyMatch(cow -> !cow.isAlive()), "aucune vache tuée");
            long meat = level.getEntitiesOfClass(ItemEntity.class, new AABB(c).inflate(48), item -> item.getItem().is(Items.BEEF)).size();
            helper.assertTrue(clan.satietyRatio() > satietyAtStart[0] + 0.05f, "satiété pas remontée");
            helper.assertTrue(meat == 0 || clan.state.maleShare > 0, "viande pas mangée");
            log("hunt", "proie abattue, satiété " + String.format("%.2f -> %.2f", satietyAtStart[0], clan.satietyRatio())
                    + " partMale=" + clan.state.maleShare + " immobileTraque=" + seen[3] + " au tick " + helper.getTick());
        });
    }

    private static void feastHierarchy(GameTestHelper helper) {
        arena(helper);
        holdTime(helper, 2000L);
        ServerLevel level = helper.getLevel();
        BlockPos c = center(helper);
        LionEntity male = lion(helper, c.offset(-12, 0, 0), true);
        LionEntity lioness = lion(helper, c.offset(3, 0, 0), false);
        helper.runAfterDelay(25, () -> {
            LionClanData.Clan clan = male.wildClan();
            if (clan != null) {
                clan.satiety = (int) (LionClanData.MAX_SATIETY * 0.4f);
                clan.state.nextHuntTick = Long.MAX_VALUE;
            }
        });
        boolean[] violation = new boolean[1];
        int[] first = new int[2];
        helper.runAfterDelay(40, () -> {
            for (int i = 0; i < 5; i++) {
                ItemEntity item = new ItemEntity(level, c.getX() + 0.5 + i * 0.3, c.getY() + 0.2, c.getZ() + 0.5, new ItemStack(Items.BEEF));
                item.setPickUpDelay(32767);
                level.addFreshEntity(item);
            }
        });
        helper.onEachTick(() -> {
            LionClanData.Clan clan = male.wildClan();
            if (clan == null) return;
            if (male.wild.eating && first[0] == 0) first[0] = (int) helper.getTick();
            if (lioness.wild.eating && first[1] == 0) first[1] = (int) helper.getTick();
            if (lioness.wild.eating && clan.state.maleShare < 3 && male.distanceTo(lioness) < 40 && !violation[0]) {
                violation[0] = true;
                log("feast", "VIOLATION : la lionne mange avant la part du mâle (part=" + clan.state.maleShare + ")");
            }
            if (helper.getTick() % 100 == 0) log("feast", "t=" + helper.getTick() + " part=" + clan.state.maleShare + " " + describe(male) + " || " + describe(lioness));
        });
        helper.succeedWhen(() -> {
            helper.assertTrue(first[0] > 0 && first[1] > 0, "les deux n'ont pas encore mangé");
            helper.assertTrue(!violation[0], "hiérarchie non respectée");
            log("feast", "mâle mange à " + first[0] + ", lionne à " + first[1]);
        });
    }

    private static void chorusWar(GameTestHelper helper) {
        arena(helper);
        holdTime(helper, 13050L);
        BlockPos c = center(helper);
        LionEntity maleA = lion(helper, c.offset(-20, 0, 0), true);
        lion(helper, c.offset(-22, 0, 2), false);
        lion(helper, c.offset(-22, 0, -2), false);
        LionEntity maleB = lion(helper, c.offset(20, 0, 0), true);
        lion(helper, c.offset(22, 0, 2), false);
        lion(helper, c.offset(22, 0, -2), false);
        setSatiety(helper, maleA, 0.9f);
        setSatiety(helper, maleB, 0.9f);
        boolean[] chorus = new boolean[2];
        helper.onEachTick(() -> {
            LionClanData.Clan a = maleA.wildClan();
            LionClanData.Clan b = maleB.wildClan();
            if (a != null && a.state.lastChorusSlot >= 0 && !chorus[0]) {
                chorus[0] = true;
                log("chorus", "chœur A au tick " + helper.getTick() + " voix=" + a.state.lastVoices);
            }
            if (b != null && b.state.lastChorusSlot >= 0 && !chorus[1]) {
                chorus[1] = true;
                log("chorus", "chœur B au tick " + helper.getTick() + " voix=" + b.state.lastVoices);
            }
            if (helper.getTick() % 200 == 0 && a != null && b != null) {
                log("chorus", "t=" + helper.getTick() + " rivalA=" + (a.state.rival != null) + " rivalB=" + (b.state.rival != null)
                        + " " + describe(maleA) + " || " + describe(maleB) + " hpA=" + maleA.getHealth() + " hpB=" + maleB.getHealth());
            }
        });
        helper.succeedWhen(() -> {
            helper.assertTrue(chorus[0] || chorus[1], "pas de chœur");
            LionClanData.Clan a = maleA.wildClan();
            LionClanData.Clan b = maleB.wildClan();
            boolean war = a != null && b != null && a.state.rival != null && b.state.rival != null;
            boolean duel = maleA.getTarget() == maleB || maleB.getTarget() == maleA;
            boolean settled = maleA.wild.isConceding() || maleB.wild.isConceding() || !maleA.isAlive() || !maleB.isAlive();
            helper.assertTrue(settled || (war && duel), "pas de guerre ni de duel");
            log("chorus", "guerre=" + war + " duel=" + duel + " tranché=" + settled + " au tick " + helper.getTick());
        });
    }

    private static void nomadTakeover(GameTestHelper helper) {
        arena(helper);
        holdTime(helper, 11000L);
        BlockPos c = center(helper);
        LionEntity resident = lion(helper, c, true);
        LionEntity a = lion(helper, c.offset(2, 0, 2), false);
        LionEntity b = lion(helper, c.offset(-2, 0, 2), false);
        setSatiety(helper, resident, 0.9f);
        LionEntity[] nomad = new LionEntity[1];
        boolean[] watched = new boolean[1];
        helper.runAfterDelay(60, () -> {
            resident.setHealth(resident.getMaxHealth() * 0.32f);
            nomad[0] = lion(helper, c.offset(14, 0, 0), true);
        });
        helper.onEachTick(() -> {
            if (nomad[0] == null) return;
            if ((a.isGuardPosed() || b.isGuardPosed()) && !watched[0]) {
                watched[0] = true;
                log("nomad", "lionnes spectatrices au tick " + helper.getTick());
            }
            if (helper.getTick() % 100 == 0) {
                log("nomad", "t=" + helper.getTick() + " res=" + describe(resident) + " hp=" + resident.getHealth()
                        + " || nomad=" + describe(nomad[0]) + " || " + describe(a) + " || " + describe(b));
            }
        });
        helper.succeedWhen(() -> {
            helper.assertTrue(nomad[0] != null && nomad[0].getClanId() != null, "pas de nomade");
            helper.assertTrue(resident.wild.isConceding() || !resident.isAlive(), "le résident n'a pas cédé");
            helper.assertTrue(nomad[0].getClanId().equals(a.getClanId()) && nomad[0].getClanId().equals(b.getClanId()), "lionnes pas reprises");
            helper.assertTrue(a.getTarget() != nomad[0] && b.getTarget() != nomad[0], "les lionnes attaquent le nomade");
            log("nomad", "prise de pouvoir au tick " + helper.getTick() + " spectatrices=" + watched[0]);
        });
    }

    private static void rivalPredator(GameTestHelper helper) {
        arena(helper);
        holdTime(helper, 11000L);
        ServerLevel level = helper.getLevel();
        BlockPos c = center(helper);
        LionEntity male = lion(helper, c, true);
        LionEntity a = lion(helper, c.offset(2, 0, 2), false);
        LionEntity b = lion(helper, c.offset(-2, 0, 2), false);
        setSatiety(helper, male, 0.9f);
        TigerEntity[] tiger = new TigerEntity[1];
        helper.runAfterDelay(60, () -> {
            TigerEntity t = OWEntityRegistry.TIGER.get().create(level);
            t.moveTo(c.getX() + 10.5, c.getY(), c.getZ() + 0.5, 90f, 0f);
            t.finalizeSpawn(level, level.getCurrentDifficultyAt(c), MobSpawnType.COMMAND, null);
            level.addFreshEntity(t);
            tiger[0] = t;
        });
        helper.succeedWhen(() -> {
            helper.assertTrue(tiger[0] != null && (a.getTarget() == tiger[0] || b.getTarget() == tiger[0]), "le tigre n'est pas pris pour cible");
            log("predator", "tigre ciblé au tick " + helper.getTick());
        });
    }

    private static void coalition(GameTestHelper helper) {
        arena(helper);
        holdTime(helper, 11000L);
        BlockPos c = center(helper);
        LionEntity first = lion(helper, c, true);
        LionEntity second = lion(helper, c.offset(8, 0, 0), true);
        helper.succeedWhen(() -> {
            helper.assertTrue(first.getClanId() != null && first.getClanId().equals(second.getClanId()), "pas de coalition");
            LionClanData.Clan clan = first.wildClan();
            helper.assertTrue(clan != null && clan.maleCount() == 2 && clan.isNomad(), "coalition invalide");
            log("coalition", "coalition au tick " + helper.getTick());
        });
    }

    private static void watchCheck(GameTestHelper helper) {
        arena(helper);
        BlockPos c = center(helper);
        LionEntity lioness = lion(helper, c.offset(0, 0, 15), false);
        ServerPlayer facing = player(helper, Vec3.atBottomCenterOf(c), 0f);
        ServerPlayer away = player(helper, Vec3.atBottomCenterOf(c.offset(1, 0, 0)), 180f);
        helper.runAfterDelay(5, () -> {
            boolean seen = LionHunt.isWatchedBy(facing, lioness);
            boolean unseen = LionHunt.isWatchedBy(away, lioness);
            log("watch", "regard de face=" + seen + " dos tourné=" + unseen);
            if (seen && !unseen) helper.succeed();
            else helper.fail("détection du regard incorrecte");
        });
    }
}
