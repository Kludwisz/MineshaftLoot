package kludwisz.mineshafts;

public enum Direction
{
	NORTH(Axis.Z),
	EAST(Axis.X),
	SOUTH(Axis.Z),
	WEST(Axis.X);
	
	public Axis axis;

    Direction(Axis axis) {
        this.axis = axis;
    }

    public enum Axis {
        X,
        Z
    }
}
