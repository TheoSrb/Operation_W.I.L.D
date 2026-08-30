package net.tiew.operationWild.datagen.wiki;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.tiew.operationWild.entity.OWEntity;
import net.tiew.operationWild.entity.OWEntityRegistry;
import net.tiew.operationWild.entity.animals.aquatic.CrocodileEntity;
import net.tiew.operationWild.entity.animals.aquatic.OrcaEntity;
import net.tiew.operationWild.entity.animals.terrestrial.BoaEntity;
import net.tiew.operationWild.entity.animals.terrestrial.ElephantEntity;
import net.tiew.operationWild.entity.animals.terrestrial.GorillaEntity;
import net.tiew.operationWild.entity.animals.terrestrial.KangarooEntity;
import net.tiew.operationWild.entity.animals.terrestrial.KodiakEntity;
import net.tiew.operationWild.entity.animals.terrestrial.RedPandaEntity;
import net.tiew.operationWild.entity.animals.terrestrial.TigerEntity;
import net.tiew.operationWild.entity.attacks.OWAttacksConstants;
import net.tiew.operationWild.entity.bosses.PlantEmpressEntity;

import java.util.List;

public record OWWikiSpecies(EntityType<?> type, Class<? extends OWEntity> implementation) {

    private static final String VARIANTS_PACKAGE = "net.tiew.operationWild.entity.variants.";

    public static List<OWWikiSpecies> all() {
        return List.of(
                of(OWEntityRegistry.TIGER.get(), TigerEntity.class),
                of(OWEntityRegistry.KODIAK.get(), KodiakEntity.class),
                of(OWEntityRegistry.CROCODILE.get(), CrocodileEntity.class),
                of(OWEntityRegistry.BOA.get(), BoaEntity.class),
                of(OWEntityRegistry.ORCA.get(), OrcaEntity.class),
                of(OWEntityRegistry.ELEPHANT.get(), ElephantEntity.class),
                of(OWEntityRegistry.KANGAROO.get(), KangarooEntity.class),
                of(OWEntityRegistry.RED_PANDA.get(), RedPandaEntity.class),
                of(OWEntityRegistry.GORILLA.get(), GorillaEntity.class),
                of(OWEntityRegistry.PLANT_EMPRESS.get(), PlantEmpressEntity.class));
    }

    private static OWWikiSpecies of(EntityType<?> type, Class<? extends OWEntity> implementation) {
        return new OWWikiSpecies(type, implementation);
    }

    public ResourceLocation key() {
        return BuiltInRegistries.ENTITY_TYPE.getKey(type);
    }

    public String id() {
        return key().getPath();
    }

    public String stem() {
        String name = implementation.getSimpleName();
        return name.endsWith("Entity") ? name.substring(0, name.length() - "Entity".length()) : name;
    }

    public Class<?> variantEnum() {
        return OWWikiReflect.classOrNull(VARIANTS_PACKAGE + stem() + "Variant");
    }

    public Class<?> cosmeticEnum() {
        return OWWikiReflect.nestedOrNull(variantEnum(), "Cosmetics");
    }

    public Class<?> attackConstants() {
        return OWWikiReflect.nestedOrNull(OWAttacksConstants.class, stem());
    }
}
