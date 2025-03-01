package kludwisz.mineshafts.util;

import com.seedfinding.mccore.util.block.BlockBox;
import com.seedfinding.mccore.util.pos.BPos;

public class CoordinateTransformer 
{
	public static Direction facing = null;
	public static BlockBox boundingBox = null;
	
	/*
	 * @param _facing The Direction of the structurePiece
	 * @param _boundingBox The BlockBox of the structurePiece
	 */
	public static void setParams(Direction _facing, BlockBox _boundingBox) {
		facing = _facing;
		boundingBox = _boundingBox;
	}
	
	public static BPos getWorldPos(int relativeX, int relativeY, int relativeZ) {
		return new BPos(getWorldX(relativeX, relativeZ), getWorldY(relativeY), getWorldZ(relativeX, relativeZ));
	}
	
	public static int getWorldX(int relativeX, int relativeZ) {
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

	public static int getWorldY(int relativeY) {
        return facing == null ? relativeY : relativeY + boundingBox.minY;
    }

	public static int getWorldZ(int relativeX, int relativeZ) {
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
	
	// mineshaft-specific, not for general use
	public static boolean isSupportingBox(int relativeZ, BlockBox chunk) {
		for (int relativeX=0; relativeX<=2; relativeX++) {
			if (!chunk.contains(getWorldPos(relativeX, 3, relativeZ)))
				return false;
		}
		return true;
	}
}
