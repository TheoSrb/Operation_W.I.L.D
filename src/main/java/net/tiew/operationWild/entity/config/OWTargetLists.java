package net.tiew.operationWild.entity.config;

import net.minecraft.world.entity.ambient.Bat;
import net.minecraft.world.entity.animal.Cat;
import net.minecraft.world.entity.animal.Chicken;
import net.minecraft.world.entity.animal.Cod;
import net.minecraft.world.entity.animal.Cow;
import net.minecraft.world.entity.animal.Dolphin;
import net.minecraft.world.entity.animal.Fox;
import net.minecraft.world.entity.animal.Ocelot;
import net.minecraft.world.entity.animal.Panda;
import net.minecraft.world.entity.animal.Parrot;
import net.minecraft.world.entity.animal.Pig;
import net.minecraft.world.entity.animal.PolarBear;
import net.minecraft.world.entity.animal.Pufferfish;
import net.minecraft.world.entity.animal.Rabbit;
import net.minecraft.world.entity.animal.Salmon;
import net.minecraft.world.entity.animal.Sheep;
import net.minecraft.world.entity.animal.Squid;
import net.minecraft.world.entity.animal.TropicalFish;
import net.minecraft.world.entity.animal.Turtle;
import net.minecraft.world.entity.animal.Wolf;
import net.minecraft.world.entity.animal.frog.Frog;
import net.minecraft.world.entity.animal.goat.Goat;
import net.minecraft.world.entity.animal.horse.Donkey;
import net.minecraft.world.entity.animal.horse.Horse;
import net.minecraft.world.entity.animal.horse.Llama;
import net.minecraft.world.entity.animal.horse.Mule;
import net.minecraft.world.entity.monster.Drowned;
import net.minecraft.world.entity.monster.ElderGuardian;
import net.minecraft.world.entity.monster.Guardian;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;
import net.tiew.operationWild.entity.animals.aquatic.CrocodileEntity;
import net.tiew.operationWild.entity.animals.terrestrial.BoaEntity;
import net.tiew.operationWild.entity.animals.terrestrial.KangarooEntity;
import net.tiew.operationWild.entity.animals.terrestrial.KodiakEntity;
import net.tiew.operationWild.entity.animals.terrestrial.TigerEntity;

import java.util.List;

/**
 * Tables de proies des créatures du mod.
 *
 * <p>Rassemblées ici plutôt qu'éparpillées dans chaque entité : ce sont des décisions de game
 * design, pas des détails d'implémentation, et elles ne se relisent qu'en les comparant les unes
 * aux autres.</p>
 *
 * <p><b>Ne jamais y remettre {@code Animal.class}.</b> C'est une classe fourre-tout dont héritent
 * aussi les six créatures du mod : elle faisait chasser l'orque par le tigre, et les vaches de la
 * berge par l'orque. Chaque proie se déclare nommément.</p>
 */
public final class OWTargetLists {

    private OWTargetLists() {}

    /**
     * Kodiak — omnivore opportuniste. Poisson d'abord, petit gibier ensuite, et deux concurrents
     * charognards avec qui l'ours se dispute réellement les carcasses.
     */
    public static final List<Class<?>> KODIAK = List.of(
            Salmon.class, Cod.class,
            Pig.class, Sheep.class, Goat.class, Rabbit.class, Chicken.class, Cow.class,
            Fox.class, Wolf.class
    );

    /**
     * Tigre — grand prédateur de jungle. Le panda partage sa forêt de bambous, le villageois et le
     * joueur rappellent le mangeur d'hommes, et les deux entrées du mod tiennent à un lieu précis :
     * le boa qu'il tue et mange, le crocodile qu'il croise dans les Sundarbans.
     */
    public static final List<Class<?>> TIGER = List.of(
            Player.class, Villager.class,
            Pig.class, Cow.class, Sheep.class, Goat.class, Chicken.class, Rabbit.class,
            Horse.class, Donkey.class, Mule.class, Llama.class,
            Panda.class, Parrot.class, Ocelot.class, Cat.class, Fox.class,
            BoaEntity.class, CrocodileEntity.class
    );

    /**
     * Boa — constricteur : il ne vise que ce qu'il peut avaler. D'où le retrait du tigre et du
     * cheval, qui inversaient le rapport de force ou dépassaient son gabarit.
     */
    public static final List<Class<?>> BOA = List.of(
            Player.class,
            Chicken.class, Rabbit.class, Parrot.class, Bat.class,
            Ocelot.class, Cat.class, Fox.class,
            Frog.class, Turtle.class
    );

    /**
     * Crocodile — embuscade au point d'eau : tout ce qui vient boire, plus le poisson de rivière.
     * Les seuls hostiles retenus sont ceux qui entrent réellement dans l'eau.
     */
    public static final List<Class<?>> CROCODILE = List.of(
            Player.class, Villager.class,
            Cow.class, Sheep.class, Pig.class, Goat.class, Chicken.class,
            Horse.class, Donkey.class, Mule.class,
            Salmon.class, Cod.class, TropicalFish.class,
            Turtle.class, Frog.class,
            Drowned.class, Zombie.class,
            KangarooEntity.class, BoaEntity.class, TigerEntity.class
    );

    /**
     * Orque — apex marin. Strictement aquatique : plus une seule proie terrestre. L'ours blanc et
     * le dauphin sont deux prédations documentées, les gardiens sont les seuls hostiles de son
     * élément.
     */
    public static final List<Class<?>> ORCA = List.of(
            Player.class,
            TigerEntity.class, KodiakEntity.class, BoaEntity.class,
            Squid.class, Cod.class, Salmon.class, TropicalFish.class, Pufferfish.class,
            Dolphin.class, Turtle.class, PolarBear.class,
            Drowned.class, Guardian.class, ElderGuardian.class
    );
}
