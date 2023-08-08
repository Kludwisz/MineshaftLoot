package kludwisz.mineshafts;

import java.util.ArrayList;
import java.util.Iterator;

import com.seedfinding.mccore.util.block.BlockBox;
import com.seedfinding.mcseed.rand.JRand;

public abstract class StructurePiece {
    public BlockBox boundingBox;
    public Direction facing;
    public int length;

    protected StructurePiece(int length) {
        this.length = length;
    }

    public abstract void placeJigsaw(StructurePiece structurePiece, ArrayList<StructurePiece> list, JRand JRand);

    public static StructurePiece getOverlappingPiece(ArrayList<StructurePiece> list, BlockBox blockBox) {
        Iterator<StructurePiece> var2 = list.iterator();
        StructurePiece structurePiece;
        do {
            if (!var2.hasNext()) {
                return null;
            }

            structurePiece = (StructurePiece) var2.next();
        } while (structurePiece.boundingBox == null || !structurePiece.boundingBox.intersects(blockBox));
        return structurePiece;
    }

    protected int applyXTransform(int i, int j) {
        Direction direction = this.facing;
        if (direction == null) {
            return i;
        } else {
            switch(direction) {
                case NORTH:
                case SOUTH:
                    return this.boundingBox.minX + i;
                case WEST:
                    return this.boundingBox.maxX - j;
                case EAST:
                    return this.boundingBox.minX + j;
                default:
                    return i;
            }
        }
    }

    protected int applyYTransform(int i) {
        return this.facing == null ? i : i + this.boundingBox.minY;
    }

    protected int applyZTransform(int i, int j) {
        Direction direction = this.facing;
        if (direction == null) {
            return j;
        } else {
            switch(direction) {
                case NORTH:
                    return this.boundingBox.maxZ - j;
                case SOUTH:
                    return this.boundingBox.minZ + j;
                case WEST:
                case EAST:
                    return this.boundingBox.minZ + i;
                default:
                    return j;
            }
        }
    }

    public void translate(int x, int y, int z) {
        this.boundingBox.move(x, y, z);
    }
}