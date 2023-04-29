package kludwisz.mineshafts;

import kaptainwutax.mcutils.util.block.BlockBox;
import kaptainwutax.mcutils.util.pos.BPos;

public class CoordinateTransformer 
{
	public static Direction facing = null;
	public static BlockBox boundingBox = null;
	
	/*
	 * @param _facing The Direction of the structurePiece
	 * @param _boundingBox The BlockBox of the structurePiece
	 */
	public static final void setParams(Direction _facing, BlockBox _boundingBox) {
		facing = _facing;
		boundingBox = _boundingBox;
	}
	
	public static final BPos getWorldPos(int relativeX, int relativeY, int relativeZ) {
		return new BPos(getWorldX(relativeX, relativeZ), getWorldY(relativeY), getWorldZ(relativeX, relativeZ));
	}
	
	public static final int getWorldX(int relativeX, int relativeZ) {
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

	public static final int getWorldY(int relativeY) {
        return facing == null ? relativeY : relativeY + boundingBox.minY;
    }

	public static final int getWorldZ(int relativeX, int relativeZ) {
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
	public static final boolean isSupportingBox(int relativeZ, BlockBox chunk) {
		for (int relativeX=0; relativeX<=2; relativeX++) {
			if (!chunk.contains(getWorldPos(relativeX, 3, relativeZ)))
				return false;
		}
		return true;
	}
}
