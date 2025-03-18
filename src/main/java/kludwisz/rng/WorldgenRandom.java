package kludwisz.rng;

public class WorldgenRandom {
	private final RandomSource source;

	public WorldgenRandom(Type type) {
		this.source = type == Type.JAVA ? new JavaRandomSource() : new XoroshiroRandomSource();
	}

	// ---------------------------------------------------
	// Seeding functions
	// ---------------------------------------------------

	public void setSeed(long seed) {
		this.source.setSeed(seed);
	}

	public long getPopulationSeed(long worldSeed, int chunkX, int chunkZ) {
		this.setSeed(worldSeed);
		long a = this.nextLong() | 1L;
		long b = this.nextLong() | 1L;
		return ((long)chunkX << 4) * a + ((long)chunkZ << 4) * b ^ worldSeed;
	}

	public void setPopulationSeed(long worldSeed, int chunkX, int chunkZ) {
		this.setSeed(this.getPopulationSeed(worldSeed, chunkX, chunkZ));
	}

	public void setDecoratorSeed(long worldSeed, int chunkX, int chunkZ, int index, int step) {
		this.setDecoratorSeed(worldSeed, chunkX, chunkZ, step * 10000 + index);
	}

	public void setDecoratorSeed(long worldSeed, int chunkX, int chunkZ, int salt) {
		long populationSeed = this.getPopulationSeed(worldSeed, chunkX, chunkZ);
		this.setSeed(populationSeed + salt);
	}

	// ---------------------------------------------------
	// RNG functions
	// ---------------------------------------------------

	public int nextInt(int bound) {
		int bits, val;
		final int m = bound - 1;

		if ((bound & m) == 0) {
			long x = bound * (long)source.nextBits(31);
			return (int) (x >> 31);
		}

		do {
			bits = source.nextBits(31);
			val = bits % bound;
		}
		while (bits - val + m < 0);

		return val;
	}

	public long nextLong() {
		return ((long) source.nextBits(32) << 32) + source.nextBits(32);
	}

	public float nextFloat() {
		return source.nextFloat();
	}

	public double nextDouble() {
		return source.nextDouble();
	}

	public void skip(int states) {
		source.skip(states);
	}

	public void nextSeed() {
		source.nextSeed();
	}

	public String getState() { // TODO remove
		if (source instanceof XoroshiroRandomSource xoroshiro) {
            return String.format("(%d, %d)", xoroshiro.lo, xoroshiro.hi);
		}
		else {
			return "N/A";
		}
	}

	// ---------------------------------------------------

	public enum Type {
		JAVA,
		XOROSHIRO
	}
}
