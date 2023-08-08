package kludwisz.mineshafts;

import com.seedfinding.mccore.util.block.BlockBox;

public class Corridor {
    public Direction direction;
    public int length;
    public int x,y,z;
    public BlockBox bb;
    public boolean hasCobwebs, hasRails;

    public Corridor(int x, int y, int z, int length, Direction direction, BlockBox bb2, boolean hasCobwebs, boolean hasRails) {
    	this.x = x;
    	this.y = y;
    	this.z = z;
        this.direction = direction;
        this.bb = bb2;
        this.length = length;
        this.hasCobwebs = hasCobwebs;
        this.hasRails = hasRails;
    }
    
    public Corridor(int length, Direction direction, BlockBox bb2, boolean hasCobwebs, boolean hasRails)
    {
    	this.direction = direction;
        this.bb = bb2;
        this.length = length;
        this.hasCobwebs = hasCobwebs;
        this.hasRails = hasRails;
    }
}
