package net.tiew.operationWild.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.UUIDUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;

import java.util.UUID;

/**
 * Snapshot complet et générique d'un compagnon {@link net.tiew.operationWild.entity.OWEntity}
 * mort, porté par l'item Âme ({@link net.tiew.operationWild.item.custom.AnimalSoulItem}).
 *
 * <p>Contrairement à l'ancien système (un composant par caractéristique + switch codé en dur),
 * tout est ici concentré dans un seul record sérialisable, indépendant du type d'entité :
 * le type est stocké sous forme de {@link ResourceLocation}, ce qui rend la résurrection
 * automatiquement compatible avec n'importe quel OWEntity (hors véhicules) sans code dédié.</p>
 *
 * <p>L'{@link #originalUuid} est la clé de voûte : en ré-ancrant l'entité ressuscitée avec le
 * MÊME uuid, les skins débloqués ({@code OWDatasSave.purchasedSkins}) et la progression des
 * quêtes cosmétiques ({@code CosmeticsQuest}) — toutes deux indexées par UUID d'entité —
 * sont restaurées automatiquement.</p>
 */
public record SoulData(
        ResourceLocation entityType,
        UUID originalUuid,
        UUID ownerUuid,
        String ownerName,
        String nickname,
        boolean male,
        float maxHealth,
        float damage,
        float speed,
        float scale,
        int level,
        int variant,
        int skinIndex,
        CompoundTag teamTag
) {

    public static final UUID NO_UUID = new UUID(0L, 0L);

    /** Valeur "vide" utilisée comme défaut de composant et pour détecter une âme non initialisée. */
    public static final SoulData EMPTY = new SoulData(
            ResourceLocation.withDefaultNamespace("empty"),
            NO_UUID, NO_UUID, "", "",
            false, 1.0f, 1.0f, 0.25f, 1.0f,
            1, 0, 0, new CompoundTag());

    public static final Codec<SoulData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ResourceLocation.CODEC.fieldOf("entity_type").forGetter(SoulData::entityType),
            UUIDUtil.CODEC.fieldOf("original_uuid").forGetter(SoulData::originalUuid),
            UUIDUtil.CODEC.optionalFieldOf("owner_uuid", NO_UUID).forGetter(SoulData::ownerUuid),
            Codec.STRING.optionalFieldOf("owner_name", "").forGetter(SoulData::ownerName),
            Codec.STRING.optionalFieldOf("nickname", "").forGetter(SoulData::nickname),
            Codec.BOOL.optionalFieldOf("male", false).forGetter(SoulData::male),
            Codec.FLOAT.optionalFieldOf("max_health", 1.0f).forGetter(SoulData::maxHealth),
            Codec.FLOAT.optionalFieldOf("damage", 1.0f).forGetter(SoulData::damage),
            Codec.FLOAT.optionalFieldOf("speed", 0.25f).forGetter(SoulData::speed),
            Codec.FLOAT.optionalFieldOf("scale", 1.0f).forGetter(SoulData::scale),
            Codec.INT.optionalFieldOf("level", 1).forGetter(SoulData::level),
            Codec.INT.optionalFieldOf("variant", 0).forGetter(SoulData::variant),
            Codec.INT.optionalFieldOf("skin_index", 0).forGetter(SoulData::skinIndex),
            CompoundTag.CODEC.optionalFieldOf("team", new CompoundTag()).forGetter(SoulData::teamTag)
    ).apply(instance, SoulData::new));

    /** StreamCodec manuel : trop de champs pour {@code StreamCodec.composite}. */
    public static final StreamCodec<FriendlyByteBuf, SoulData> STREAM_CODEC = StreamCodec.of(
            (buf, data) -> {
                buf.writeResourceLocation(data.entityType);
                buf.writeUUID(data.originalUuid);
                buf.writeUUID(data.ownerUuid);
                buf.writeUtf(data.ownerName);
                buf.writeUtf(data.nickname);
                buf.writeBoolean(data.male);
                buf.writeFloat(data.maxHealth);
                buf.writeFloat(data.damage);
                buf.writeFloat(data.speed);
                buf.writeFloat(data.scale);
                buf.writeVarInt(data.level);
                buf.writeVarInt(data.variant);
                buf.writeVarInt(data.skinIndex);
                buf.writeNbt(data.teamTag);
            },
            buf -> {
                ResourceLocation entityType = buf.readResourceLocation();
                UUID originalUuid = buf.readUUID();
                UUID ownerUuid = buf.readUUID();
                String ownerName = buf.readUtf();
                String nickname = buf.readUtf();
                boolean male = buf.readBoolean();
                float maxHealth = buf.readFloat();
                float damage = buf.readFloat();
                float speed = buf.readFloat();
                float scale = buf.readFloat();
                int level = buf.readVarInt();
                int variant = buf.readVarInt();
                int skinIndex = buf.readVarInt();
                CompoundTag teamTag = buf.readNbt();
                return new SoulData(entityType, originalUuid, ownerUuid, ownerName, nickname,
                        male, maxHealth, damage, speed, scale, level, variant, skinIndex,
                        teamTag == null ? new CompoundTag() : teamTag);
            }
    );

    public boolean isEmpty() {
        return this.originalUuid.equals(NO_UUID) || this.entityType.getPath().equals("empty");
    }

    public boolean hasTeam() {
        return this.teamTag != null && !this.teamTag.isEmpty();
    }

    /** Coût en niveaux d'XP du rituel, croissant avec le niveau du compagnon. */
    public int xpLevelCost() {
        return 5 + this.level / 3;
    }

    /** Nombre de vagues de monstres à repousser, de ~3 (niv. 1) à ~10 (niv. 50). */
    public int waveCount() {
        return Math.min(10, 3 + this.level / 7);
    }
}
