package net.tiew.operationWild.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.UUIDUtil;

import java.util.UUID;

public record SyringeData(
        String entityType,
        UUID entityUUID,
        float maxHealth,
        float damage,
        float speed,
        boolean isFemale,
        float tamingExperience,
        int entityColor,
        int skinIndex
) {
    public static final Codec<SyringeData> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.STRING.fieldOf("entity_type").forGetter(SyringeData::entityType),
                    UUIDUtil.CODEC.fieldOf("entity_uuid").forGetter(SyringeData::entityUUID),
                    Codec.FLOAT.fieldOf("max_health").forGetter(SyringeData::maxHealth),
                    Codec.FLOAT.fieldOf("damage").forGetter(SyringeData::damage),
                    Codec.FLOAT.fieldOf("speed").forGetter(SyringeData::speed),
                    Codec.BOOL.fieldOf("is_female").forGetter(SyringeData::isFemale),
                    Codec.FLOAT.fieldOf("taming_experience").forGetter(SyringeData::tamingExperience),
                    Codec.INT.fieldOf("entity_color").forGetter(SyringeData::entityColor),
                    Codec.INT.optionalFieldOf("skin_index", 0).forGetter(SyringeData::skinIndex)
            ).apply(instance, SyringeData::new));
}
