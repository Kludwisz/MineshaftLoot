package kludwisz.mineshafts;

import java.util.ArrayList;
import java.util.Iterator;

import com.seedfinding.mccore.util.block.BlockBox;
import com.seedfinding.mcseed.rand.JRand;
import kludwisz.mineshafts.util.Direction;

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

            structurePiece = var2.next();
        } while (structurePiece.boundingBox == null || !structurePiece.boundingBox.intersects(blockBox));
        return structurePiece;
    }

    public void translate(int x, int y, int z) {
        this.boundingBox.move(x, y, z);
    }
}