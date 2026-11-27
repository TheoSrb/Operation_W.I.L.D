package net.tiew.operationWild.sound;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.item.JukeboxSong;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.tiew.operationWild.OperationWild;

public class OWSounds {
    public static final DeferredRegister<SoundEvent> SOUND_EVENTS =
            DeferredRegister.create(BuiltInRegistries.SOUND_EVENT, OperationWild.MOD_ID);

    public static final DeferredHolder<SoundEvent, SoundEvent> TAME_SUCCESS = registerSoundEvent("tame_success");

    public static final DeferredHolder<SoundEvent, SoundEvent> SPEAR_HIT = registerSoundEvent("spear_hurt");
    public static final DeferredHolder<SoundEvent, SoundEvent> SPEAR_LUNCH = registerSoundEvent("spear_lunch");

    public static final DeferredHolder<SoundEvent, SoundEvent> BLOWPIPE_LUNCH = registerSoundEvent("primitive_blowpipe_lunch");

    public static final DeferredHolder<SoundEvent, SoundEvent> SYRINGE_RIFLE_SHOOT = registerSoundEvent("syringe_rifle_shoot");
    public static final DeferredHolder<SoundEvent, SoundEvent> SYRINGE_RIFLE_CRACK = registerSoundEvent("syringe_rifle_crack");
    public static final DeferredHolder<SoundEvent, SoundEvent> SYRINGE_RIFLE_TRIGGER = registerSoundEvent("syringe_rifle_trigger");
    public static final DeferredHolder<SoundEvent, SoundEvent> SYRINGE_RIFLE_LOAD_START = registerSoundEvent("syringe_rifle_load_start");
    public static final DeferredHolder<SoundEvent, SoundEvent> SYRINGE_RIFLE_LOAD_MIDDLE = registerSoundEvent("syringe_rifle_load_middle");
    public static final DeferredHolder<SoundEvent, SoundEvent> SYRINGE_RIFLE_LOADED = registerSoundEvent("syringe_rifle_loaded");
    public static final DeferredHolder<SoundEvent, SoundEvent> TRANQUILIZER_DART_HIT = registerSoundEvent("tranquilizer_dart_hit");

    public static final DeferredHolder<SoundEvent, SoundEvent> BEAR_TRAP_CLOSED = registerSoundEvent("bear_trap_closed");

    public static final DeferredHolder<SoundEvent, SoundEvent> MINI_EARTHQUAKE = registerSoundEvent("mini_earthquake");

    public static final DeferredHolder<SoundEvent, SoundEvent> TIGER_ROAR = registerSoundEvent("tiger_roar");
    public static final DeferredHolder<SoundEvent, SoundEvent> TIGER_ROAR_ULTIMATE = registerSoundEvent("tiger_roar_ultimate");
    public static final DeferredHolder<SoundEvent, SoundEvent> TIGER_JUMP = registerSoundEvent("tiger_jump");
    public static final DeferredHolder<SoundEvent, SoundEvent> TIGER_IDLE = registerSoundEvent("tiger_idle");
    public static final DeferredHolder<SoundEvent, SoundEvent> TIGER_IDLE_2 = registerSoundEvent("tiger_idle_2");
    public static final DeferredHolder<SoundEvent, SoundEvent> TIGER_IDLE_3 = registerSoundEvent("tiger_idle_3");
    public static final DeferredHolder<SoundEvent, SoundEvent> TIGER_HURT = registerSoundEvent("tiger_hurt");
    public static final DeferredHolder<SoundEvent, SoundEvent> TIGER_HURTING = registerSoundEvent("tiger_hurting");
    public static final DeferredHolder<SoundEvent, SoundEvent> TIGER_SNIFFING = registerSoundEvent("tiger_sniff");
    public static final DeferredHolder<SoundEvent, SoundEvent> TIGER_3 = registerSoundEvent("tiger3");
    public static final DeferredHolder<SoundEvent, SoundEvent> TIGER_4 = registerSoundEvent("tiger4");
    public static final DeferredHolder<SoundEvent, SoundEvent> TIGER_SNORE_1 = registerSoundEvent("tiger_snore_1");
    public static final DeferredHolder<SoundEvent, SoundEvent> TIGER_SNORE_2 = registerSoundEvent("tiger_snore_2");
    public static final DeferredHolder<SoundEvent, SoundEvent> TIGER_SNORE_3 = registerSoundEvent("tiger_snore_3");

    public static final DeferredHolder<SoundEvent, SoundEvent> TIGER_ROAR_VIRUS = registerSoundEvent("tiger_roar_virus");
    public static final DeferredHolder<SoundEvent, SoundEvent> TIGER_ROAR_ULTIMATE_VIRUS = registerSoundEvent("tiger_roar_ultimate_virus");
    public static final DeferredHolder<SoundEvent, SoundEvent> TIGER_JUMP_VIRUS = registerSoundEvent("tiger_jump_virus");
    public static final DeferredHolder<SoundEvent, SoundEvent> TIGER_IDLE_VIRUS = registerSoundEvent("tiger_idle_virus");
    public static final DeferredHolder<SoundEvent, SoundEvent> TIGER_IDLE_2_VIRUS = registerSoundEvent("tiger_idle_2_virus");
    public static final DeferredHolder<SoundEvent, SoundEvent> TIGER_IDLE_3_VIRUS = registerSoundEvent("tiger_idle_3_virus");
    public static final DeferredHolder<SoundEvent, SoundEvent> TIGER_HURT_VIRUS = registerSoundEvent("tiger_hurt_virus");
    public static final DeferredHolder<SoundEvent, SoundEvent> TIGER_HURTING_VIRUS = registerSoundEvent("tiger_hurting_virus");
    public static final DeferredHolder<SoundEvent, SoundEvent> TIGER_SNIFFING_VIRUS = registerSoundEvent("tiger_sniff_virus");
    public static final DeferredHolder<SoundEvent, SoundEvent> TIGER_3_VIRUS = registerSoundEvent("tiger3_virus");
    public static final DeferredHolder<SoundEvent, SoundEvent> TIGER_4_VIRUS = registerSoundEvent("tiger4_virus");
    public static final DeferredHolder<SoundEvent, SoundEvent> TIGER_SNORE_1_VIRUS = registerSoundEvent("tiger_snore_1_virus");
    public static final DeferredHolder<SoundEvent, SoundEvent> TIGER_SNORE_2_VIRUS = registerSoundEvent("tiger_snore_2_virus");
    public static final DeferredHolder<SoundEvent, SoundEvent> TIGER_SNORE_3_VIRUS = registerSoundEvent("tiger_snore_3_virus");

    public static final DeferredHolder<SoundEvent, SoundEvent> BOA_IDLE_1 = registerSoundEvent("boa_idle_1");
    public static final DeferredHolder<SoundEvent, SoundEvent> BOA_IDLE_2 = registerSoundEvent("boa_idle_2");
    public static final DeferredHolder<SoundEvent, SoundEvent> BOA_IDLE_3 = registerSoundEvent("boa_idle_3");
    public static final DeferredHolder<SoundEvent, SoundEvent> BOA_IDLE_4 = registerSoundEvent("boa_idle_4");
    public static final DeferredHolder<SoundEvent, SoundEvent> BOA_HURT = registerSoundEvent("boa_hurt");
    public static final DeferredHolder<SoundEvent, SoundEvent> BOA_HITTING = registerSoundEvent("boa_hitting");
    public static final DeferredHolder<SoundEvent, SoundEvent> BOA_SLITHER = registerSoundEvent("boa_slither");

    public static final DeferredHolder<SoundEvent, SoundEvent> CHAMELEON_AMBIENT = registerSoundEvent("chameleon_ambient");
    public static final DeferredHolder<SoundEvent, SoundEvent> CHAMELEON_HURT = registerSoundEvent("chameleon_hurt");
    public static final DeferredHolder<SoundEvent, SoundEvent> CHAMELEON_DEATH = registerSoundEvent("chameleon_death");
    public static final DeferredHolder<SoundEvent, SoundEvent> CHAMELEON_HISS = registerSoundEvent("chameleon_hiss");
    public static final DeferredHolder<SoundEvent, SoundEvent> CHAMELEON_EAT = registerSoundEvent("chameleon_eat");
    public static final DeferredHolder<SoundEvent, SoundEvent> CHAMELEON_SCUTTLE = registerSoundEvent("chameleon_scuttle");
    public static final DeferredHolder<SoundEvent, SoundEvent> CHAMELEON_HOP = registerSoundEvent("chameleon_hop");
    public static final DeferredHolder<SoundEvent, SoundEvent> CHAMELEON_TONGUE_WINDUP = registerSoundEvent("chameleon_tongue_windup");
    public static final DeferredHolder<SoundEvent, SoundEvent> CHAMELEON_TONGUE_SHOT = registerSoundEvent("chameleon_tongue_shot");
    public static final DeferredHolder<SoundEvent, SoundEvent> CHAMELEON_TONGUE_IMPACT = registerSoundEvent("chameleon_tongue_impact");
    public static final DeferredHolder<SoundEvent, SoundEvent> CHAMELEON_TONGUE_STICK = registerSoundEvent("chameleon_tongue_stick");
    public static final DeferredHolder<SoundEvent, SoundEvent> CHAMELEON_TONGUE_UNSTICK = registerSoundEvent("chameleon_tongue_unstick");
    public static final DeferredHolder<SoundEvent, SoundEvent> CHAMELEON_TONGUE_MISS = registerSoundEvent("chameleon_tongue_miss");
    public static final DeferredHolder<SoundEvent, SoundEvent> CHAMELEON_CAMOUFLAGE = registerSoundEvent("chameleon_camouflage");
    public static final DeferredHolder<SoundEvent, SoundEvent> CHAMELEON_CAMOUFLAGE_BREAK = registerSoundEvent("chameleon_camouflage_break");
    public static final DeferredHolder<SoundEvent, SoundEvent> TARANTULA_AMBIENT = registerSoundEvent("tarantula_ambient");
    public static final DeferredHolder<SoundEvent, SoundEvent> TARANTULA_HURT = registerSoundEvent("tarantula_hurt");
    public static final DeferredHolder<SoundEvent, SoundEvent> TARANTULA_DEATH = registerSoundEvent("tarantula_death");
    public static final DeferredHolder<SoundEvent, SoundEvent> TARANTULA_SCUTTLE = registerSoundEvent("tarantula_scuttle");
    public static final DeferredHolder<SoundEvent, SoundEvent> TARANTULA_HOP = registerSoundEvent("tarantula_hop");
    public static final DeferredHolder<SoundEvent, SoundEvent> TARANTULA_WEB_SHOT = registerSoundEvent("tarantula_web_shot");
    public static final DeferredHolder<SoundEvent, SoundEvent> TARANTULA_WEB_STICK = registerSoundEvent("tarantula_web_stick");
    public static final DeferredHolder<SoundEvent, SoundEvent> TARANTULA_WEB_UNSTICK = registerSoundEvent("tarantula_web_unstick");
    public static final DeferredHolder<SoundEvent, SoundEvent> TARANTULA_WEB_REEL = registerSoundEvent("tarantula_web_reel");
    public static final DeferredHolder<SoundEvent, SoundEvent> TARANTULA_REFUSE = registerSoundEvent("tarantula_refuse");
    public static final DeferredHolder<SoundEvent, SoundEvent> TARANTULA_EAT = registerSoundEvent("tarantula_eat");

    public static final DeferredHolder<SoundEvent, SoundEvent> PEACOCK_IDLE = registerSoundEvent("peacock_idle");
    public static final DeferredHolder<SoundEvent, SoundEvent> PEACOCK_HURT = registerSoundEvent("peacock_hurt");
    public static final DeferredHolder<SoundEvent, SoundEvent> PEACOCK_DEATH = registerSoundEvent("peacock_death");
    public static final DeferredHolder<SoundEvent, SoundEvent> PEACOCK_CALL = registerSoundEvent("peacock_call");
    public static final DeferredHolder<SoundEvent, SoundEvent> PEACOCK_WING_GUST = registerSoundEvent("peacock_wing_gust");
    public static final DeferredHolder<SoundEvent, SoundEvent> PEACOCK_FAN = registerSoundEvent("peacock_fan");

    public static final DeferredHolder<SoundEvent, SoundEvent> TIGER_SHARK_CRUSH_MOUTH = registerSoundEvent("tiger_shark_crush_mouth");

    public static final DeferredHolder<SoundEvent, SoundEvent> JELLYFISH_ELECTRIFIED = registerSoundEvent("jellyfish_electrified");

    public static final DeferredHolder<SoundEvent, SoundEvent> ELEPHANT_FOOTSTEP = registerSoundEvent("elephant_footstep");
    public static final DeferredHolder<SoundEvent, SoundEvent> ELEPHANT_IDLE = registerSoundEvent("elephant_idle");
    public static final DeferredHolder<SoundEvent, SoundEvent> ELEPHANT_IDLE_2 = registerSoundEvent("elephant_idle_2");
    public static final DeferredHolder<SoundEvent, SoundEvent> ELEPHANT_IDLE_3 = registerSoundEvent("elephant_idle_3");
    public static final DeferredHolder<SoundEvent, SoundEvent> ELEPHANT_IDLE_4 = registerSoundEvent("elephant_idle_4");
    public static final DeferredHolder<SoundEvent, SoundEvent> ELEPHANT_HURT = registerSoundEvent("elephant_hurt");
    public static final DeferredHolder<SoundEvent, SoundEvent> ELEPHANT_HURTING = registerSoundEvent("elephant_hurting");
    public static final DeferredHolder<SoundEvent, SoundEvent> ELEPHANT_HURTING_2 = registerSoundEvent("elephant_hurting_2");
    public static final DeferredHolder<SoundEvent, SoundEvent> ELEPHANT_SCREAM = registerSoundEvent("elephant_scream");
    public static final DeferredHolder<SoundEvent, SoundEvent> ELEPHANT_CALL = registerSoundEvent("elephant_call");

    public static final DeferredHolder<SoundEvent, SoundEvent> KODIAK_IDLE_1 = registerSoundEvent("kodiak_idle");
    public static final DeferredHolder<SoundEvent, SoundEvent> KODIAK_IDLE_2 = registerSoundEvent("kodiak_idle_2");
    public static final DeferredHolder<SoundEvent, SoundEvent> KODIAK_IDLE_3 = registerSoundEvent("kodiak_idle_3");
    public static final DeferredHolder<SoundEvent, SoundEvent> KODIAK_HURTING = registerSoundEvent("kodiak_hurting");
    public static final DeferredHolder<SoundEvent, SoundEvent> KODIAK_HURTING_2 = registerSoundEvent("kodiak_hurting_2");
    public static final DeferredHolder<SoundEvent, SoundEvent> KODIAK_HURT = registerSoundEvent("kodiak_hurt");
    public static final DeferredHolder<SoundEvent, SoundEvent> KODIAK_MISC = registerSoundEvent("kodiak_misc");
    public static final DeferredHolder<SoundEvent, SoundEvent> LEG_HURT = registerSoundEvent("leg_hurt");
    public static final DeferredHolder<SoundEvent, SoundEvent> KODIAK_SNIFF_1 = registerSoundEvent("kodiak_sniff_1");
    public static final DeferredHolder<SoundEvent, SoundEvent> KODIAK_SNIFF_2 = registerSoundEvent("kodiak_sniff_2");
    public static final DeferredHolder<SoundEvent, SoundEvent> KODIAK_SNIFF_3 = registerSoundEvent("kodiak_sniff_3");

    public static final DeferredHolder<SoundEvent, SoundEvent> CROCODILE_IDLE_1 = registerSoundEvent("crocodile_idle_1");
    public static final DeferredHolder<SoundEvent, SoundEvent> CROCODILE_IDLE_2 = registerSoundEvent("crocodile_idle_2");
    public static final DeferredHolder<SoundEvent, SoundEvent> CROCODILE_IDLE_3 = registerSoundEvent("crocodile_idle_3");
    public static final DeferredHolder<SoundEvent, SoundEvent> CROCODILE_IDLE_4 = registerSoundEvent("crocodile_idle_4");
    public static final DeferredHolder<SoundEvent, SoundEvent> CROCODILE_MOUTH_CRUSH = registerSoundEvent("crocodile_mouth_crush");
    public static final DeferredHolder<SoundEvent, SoundEvent> CROCODILE_HURT = registerSoundEvent("crocodile_hurt");
    public static final DeferredHolder<SoundEvent, SoundEvent> CROCODILE_HIT_1 = registerSoundEvent("crocodile_hit_1");
    public static final DeferredHolder<SoundEvent, SoundEvent> CROCODILE_HIT_2 = registerSoundEvent("crocodile_hit_2");
    public static final DeferredHolder<SoundEvent, SoundEvent> CROCODILE_DEATH = registerSoundEvent("crocodile_death");



    public static final DeferredHolder<SoundEvent, SoundEvent> SUBMARINE_MOVE_LOOP = registerSoundEvent("submarine_move_loop");
    public static final DeferredHolder<SoundEvent, SoundEvent> SUBMARINE_SWITCH_LIGHT = registerSoundEvent("submarine_switch_light");
    public static final DeferredHolder<SoundEvent, SoundEvent> SUBMARINE_AMELIORATION = registerSoundEvent("submarine_amelioration");

    public static final DeferredHolder<SoundEvent, SoundEvent> PLANT_EMPRESS_THEME = registerSoundEvent("plant_empress_theme");
    public static final DeferredHolder<SoundEvent, SoundEvent> PLANT_EMPRESS_THEME_LITE = registerSoundEvent("plant_empress_theme_lite");
    public static final ResourceKey<JukeboxSong> PLANT_EMPRESS_THEME_LITE_KEY = ResourceKey.create(Registries.JUKEBOX_SONG, ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "plant_empress_theme_lite"));
    public static final DeferredHolder<SoundEvent, SoundEvent> PLANT_EMPRESS_DEFEATED_THEME = registerSoundEvent("plant_empress_defeated");
    public static final DeferredHolder<SoundEvent, SoundEvent> PLANT_EMPRESS_RUMBLE = registerSoundEvent("plant_empress_rumble");
    public static final DeferredHolder<SoundEvent, SoundEvent> PLANT_EMPRESS_HEARTBEAT = registerSoundEvent("plant_empress_heartbeat");
    public static final DeferredHolder<SoundEvent, SoundEvent> PLANT_EMPRESS_BREATH = registerSoundEvent("plant_empress_breath");
    public static final DeferredHolder<SoundEvent, SoundEvent> PLANT_EMPRESS_BURST = registerSoundEvent("plant_empress_burst");
    public static final DeferredHolder<SoundEvent, SoundEvent> PLANT_EMPRESS_UNFURL = registerSoundEvent("plant_empress_unfurl");
    public static final DeferredHolder<SoundEvent, SoundEvent> PLANT_EMPRESS_ROAR = registerSoundEvent("plant_empress_roar");
    public static final DeferredHolder<SoundEvent, SoundEvent> PLANT_EMPRESS_AMBIENT = registerSoundEvent("plant_empress_ambient");
    public static final DeferredHolder<SoundEvent, SoundEvent> PLANT_EMPRESS_HURT = registerSoundEvent("plant_empress_hurt");
    public static final DeferredHolder<SoundEvent, SoundEvent> PLANT_EMPRESS_DEATH = registerSoundEvent("plant_empress_death");
    public static final DeferredHolder<SoundEvent, SoundEvent> PLANT_EMPRESS_HEAD_DEATH = registerSoundEvent("plant_empress_head_death");
    public static final DeferredHolder<SoundEvent, SoundEvent> PLANT_EMPRESS_PARRY = registerSoundEvent("plant_empress_parry");
    public static final DeferredHolder<SoundEvent, SoundEvent> PLANT_EMPRESS_SEALED = registerSoundEvent("plant_empress_sealed");
    public static final DeferredHolder<SoundEvent, SoundEvent> PLANT_EMPRESS_COUNTER = registerSoundEvent("plant_empress_counter");
    public static final DeferredHolder<SoundEvent, SoundEvent> PLANT_EMPRESS_STAGGER = registerSoundEvent("plant_empress_stagger");
    public static final DeferredHolder<SoundEvent, SoundEvent> PLANT_EMPRESS_DODGE = registerSoundEvent("plant_empress_dodge");
    public static final DeferredHolder<SoundEvent, SoundEvent> PLANT_EMPRESS_SNARE = registerSoundEvent("plant_empress_snare");
    public static final DeferredHolder<SoundEvent, SoundEvent> PLANT_EMPRESS_ROOTS = registerSoundEvent("plant_empress_roots");
    public static final DeferredHolder<SoundEvent, SoundEvent> PLANT_EMPRESS_SLAM = registerSoundEvent("plant_empress_slam");
    public static final DeferredHolder<SoundEvent, SoundEvent> PLANT_EMPRESS_LUNGE = registerSoundEvent("plant_empress_lunge");
    public static final DeferredHolder<SoundEvent, SoundEvent> PLANT_EMPRESS_BITE = registerSoundEvent("plant_empress_bite");
    public static final DeferredHolder<SoundEvent, SoundEvent> PLANT_EMPRESS_SPIT_CHARGE = registerSoundEvent("plant_empress_spit_charge");
    public static final DeferredHolder<SoundEvent, SoundEvent> PLANT_EMPRESS_SPIT = registerSoundEvent("plant_empress_spit");
    public static final DeferredHolder<SoundEvent, SoundEvent> PLANT_EMPRESS_SPORE_BURST = registerSoundEvent("plant_empress_spore_burst");
    public static final DeferredHolder<SoundEvent, SoundEvent> PLANT_EMPRESS_SEED_PLANT = registerSoundEvent("plant_empress_seed_plant");
    public static final DeferredHolder<SoundEvent, SoundEvent> PLANT_EMPRESS_WHIP = registerSoundEvent("plant_empress_whip");
    public static final DeferredHolder<SoundEvent, SoundEvent> PLANT_EMPRESS_QUAKE_RISE = registerSoundEvent("plant_empress_quake_rise");
    public static final DeferredHolder<SoundEvent, SoundEvent> PLANT_EMPRESS_QUAKE = registerSoundEvent("plant_empress_quake");
    public static final DeferredHolder<SoundEvent, SoundEvent> PLANT_EMPRESS_POLLEN = registerSoundEvent("plant_empress_pollen");

    /** Thème du menu principal (écran-titre), joué en boucle. */
    public static final DeferredHolder<SoundEvent, SoundEvent> MENU_MUSIC = registerSoundEvent("menu_music");

    /** Musiques du Rituel de Communion (résurrection) : intro → combat (boucle) → outro. */
    public static final DeferredHolder<SoundEvent, SoundEvent> RITUAL_INTRO = registerSoundEvent("ritual_intro");
    public static final DeferredHolder<SoundEvent, SoundEvent> RITUAL_THEME = registerSoundEvent("ritual_theme");
    public static final DeferredHolder<SoundEvent, SoundEvent> RITUAL_END = registerSoundEvent("ritual_end");

    private static DeferredHolder<SoundEvent, SoundEvent> registerSoundEvent(String name) {
        return SOUND_EVENTS.register(name, () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, name)));
    }

    public static void register(IEventBus eventBus) {
        SOUND_EVENTS.register(eventBus);
    }
}