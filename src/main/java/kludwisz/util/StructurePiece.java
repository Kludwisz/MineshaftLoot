package kludwisz.util;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.seedfinding.mccore.util.block.BlockBox;
import com.seedfinding.mccore.util.block.BlockDirection;
import com.seedfinding.mccore.util.pos.BPos;
import com.seedfinding.mcseed.rand.JRand;

public abstract class StructurePiece {
    public List<BlockBox> airBoxes;
    public BlockBox boundingBox;
    public BlockDirection facing;
    public int length;

    protected StructurePiece(int length) {
        this.length = length;
    }

    public abstract void placeJigsaw(StructurePiece structurePiece, ArrayList<StructurePiece> list, JRand JRand);
    public abstract void calculateAirBoxes();

    public void translate(int x, int y, int z) {
        this.boundingBox.move(x, y, z);
    }

    public BPos getWorldPos(int relativeX, int relativeY, int relativeZ) {
        return new BPos(
                getWorldX(relativeX, relativeZ),
                getWorldY(relativeY),
                getWorldZ(relativeX, relativeZ)
        );
    }

    public int getWorldX(int relativeX, int relativeZ) {
        if (facing == null) {
            return relativeX;
        } else {
            switch(facing) {
                case NORTH:
                case SOUTH:
                    return boundingBox.minX + relativeX;
                case WEST:
                    return boundingBox.maxX - relativeZ;
                case EAST:
                    return boundingBox.minX + relativeZ;
                default:
                    return relativeX;
            }
        }
    }

    public int getWorldY(int relativeY) {
        return facing == null ? relativeY : relativeY + boundingBox.minY;
    }

    public int getWorldZ(int relativeX, int relativeZ) {
        if (facing == null) {
            return relativeZ;
        } else {
            switch(facing) {
                case NORTH:
                    return boundingBox.maxZ - relativeZ;
                case SOUTH:
                    return boundingBox.minZ + relativeZ;
                case WEST:
                case EAST:
                    return boundingBox.minZ + relativeX;
                default:
                    return relativeZ;
            }
        }
    }

    // --------------------------------------------------------------------------------------------------------

    public boolean airIntersectsBox(BlockBox box) {
        if (airBoxes == null)
            this.calculateAirBoxes();

        for (BlockBox airBox : airBoxes) {
            if (airBox.intersects(box)) {
                return true;
            }
        }
        return false;
    }

    public boolean airContainsPos(BPos pos) {
        if (airBoxes == null)
            this.calculateAirBoxes();

        for (BlockBox airBox : airBoxes) {
            if (airBox.contains(pos)) {
                return true;
            }
        }
        return false;
    }

    // --------------------------------------------------------------------------------------------------------

    public static StructurePiece getOverlappingPiece(ArrayList<StructurePiece> list, BlockBox blockBox) {
        Iterator<StructurePiece> var2 = list.iterator();
        StructurePiece structurePiece;
        do {
            if (!var2.hasNext()) {
                return null;
            }

            structurePiece = var2.next();
        } while (structurePiece.boundingBox == null || !structurePiece.boundingBox.intersects(blockBox));
        return structurePiece;
    }
}