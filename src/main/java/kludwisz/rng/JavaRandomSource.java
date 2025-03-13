package kludwisz.rng;

import com.seedfinding.mcseed.lcg.LCG;

public class JavaRandomSource implements RandomSource {
	private static final long MULTIPLIER = 25214903917L;
	private static final long ADDEND = 11L;
	private static final long MODULUS = (1L << 48);
	private static final long MASK = MODULUS - 1;

	private static final int SKIP_TABLE_SIZE = 64;
	private final LCG[] skipTable = new LCG[SKIP_TABLE_SIZE];

	private long seed;

	public JavaRandomSource() {
		this.seed = 0L;
	}

	@Override
	public void setSeed(long seed) {
		this.seed = seed ^ MULTIPLIER & MASK;
	}

	@Override
	public void nextSeed() {
		this.seed = (this.seed * MULTIPLIER + ADDEND) & MASK;
	}

	@Override
	public int nextBits(int bits) {
		this.nextSeed();
		return (int)(this.seed >>> (48 - bits));
	}

	@Override
	public float nextFloat() {
		return this.nextBits(24) / ((float)(1 << 24));
	}

	@Override
	public double nextDouble() {
		return (((long)(nextBits(26)) << 27) + nextBits(27)) * 0x1.0p-53;
	}

	@Override
	public void skip(int states) {
		if (states >= 0 && states < SKIP_TABLE_SIZE) {
			// use cached skips for small step values
			if (skipTable[states] == null)
				skipTable[states] = LCG.JAVA.combine(states);
			this.seed = skipTable[states].nextSeed(this.seed);
		}
		else {
			// one-time long skip
			this.seed = LCG.JAVA.combine(states).nextSeed(this.seed);
		}
	}
}
