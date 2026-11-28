package net.tiew.operationWild.entity.platform;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.List;

public record OWPlatformProfile(int width, int height, int length,
                                int deckX, int deckZ, int deckWidth, int deckLength,
                                double anchorUp, double anchorForward, double deckThickness,
                                @Nullable ResourceLocation deckTexture, List<Integer> gapColumns, List<BlockPos> reservedCells,
                                @Nullable Vec3 pilotSeat, double pilotSeatDrop) {

    public int volume() {
        return width * height * length;
    }

    public boolean contains(int x, int y, int z) {
        return x >= 0 && x < width && y >= 0 && y < height && z >= 0 && z < length;
    }

    public boolean onDeck(int x, int z) {
        return x >= deckX && x < deckX + deckWidth && z >= deckZ && z < deckZ + deckLength && !gapColumns.contains(x);
    }

    public int index(int x, int y, int z) {
        return (y * length + z) * width + x;
    }

    public boolean isReserved(int x, int y, int z) {
        if (gapColumns.contains(x) && z >= deckZ && z < deckZ + deckLength) return true;
        for (BlockPos pos : reservedCells) {
            if (pos.getX() == x && pos.getY() == y && pos.getZ() == z) return true;
        }
        return false;
    }

    public double centerX() {
        return deckX + deckWidth / 2.0;
    }

    public double centerZ() {
        return deckZ + deckLength / 2.0;
    }

    public double radius() {
        double dx = Math.max(centerX(), width - centerX());
        double dz = Math.max(centerZ(), length - centerZ());
        return Math.sqrt(dx * dx + dz * dz);
    }
}
