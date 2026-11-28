package net.tiew.operationWild.entity.platform;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;
import java.util.Optional;

public record OWPlatformBlocks(List<Entry> entries) {

    public static final OWPlatformBlocks EMPTY = new OWPlatformBlocks(List.of());

    public record Entry(int x, int y, int z, BlockState state, Optional<CompoundTag> data) {
        public static final Codec<Entry> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.INT.fieldOf("x").forGetter(Entry::x),
                Codec.INT.fieldOf("y").forGetter(Entry::y),
                Codec.INT.fieldOf("z").forGetter(Entry::z),
                BlockState.CODEC.fieldOf("state").forGetter(Entry::state),
                CompoundTag.CODEC.optionalFieldOf("data").forGetter(Entry::data)
        ).apply(instance, Entry::new));

        public static final StreamCodec<ByteBuf, Entry> STREAM_CODEC = StreamCodec.composite(
                ByteBufCodecs.VAR_INT, Entry::x,
                ByteBufCodecs.VAR_INT, Entry::y,
                ByteBufCodecs.VAR_INT, Entry::z,
                ByteBufCodecs.idMapper(Block.BLOCK_STATE_REGISTRY), Entry::state,
                ByteBufCodecs.OPTIONAL_COMPOUND_TAG, Entry::data,
                Entry::new);
    }

    public static final Codec<OWPlatformBlocks> CODEC = Entry.CODEC.listOf().xmap(OWPlatformBlocks::new, OWPlatformBlocks::entries);

    public static final StreamCodec<ByteBuf, OWPlatformBlocks> STREAM_CODEC =
            Entry.STREAM_CODEC.apply(ByteBufCodecs.list()).map(OWPlatformBlocks::new, OWPlatformBlocks::entries);

    public boolean isEmpty() {
        return entries.isEmpty();
    }
}
