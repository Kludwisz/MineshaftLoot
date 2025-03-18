package kludwisz.rng;

public class XoroshiroRandomSource implements RandomSource {
	private long lo;
	private long hi;

	public XoroshiroRandomSource() {
		this.lo = -7046029254386353131L;
		this.hi = 7640891576956012809L;
	}

	public static long mixStafford13(long l) {
		l = (l ^ l >>> 30) * -4658895280553007687L;
		l = (l ^ l >>> 27) * -7723592293110705685L;
		return l ^ l >>> 31;
	}

	@Override
	public void setSeed(long seed) {
		long a = seed ^ 0x6A09E667F3BCC909L;
		long b = a - 7046029254386353131L;
		this.lo = mixStafford13(a);
		this.hi = mixStafford13(b);
	}

	@Override
	public void nextSeed() {
		this.nextLongX();
	}

	@Override
	public int nextBits(int bits) {
		return (int)(this.nextLongX() >>> (64 - bits));
	}

	@Override
	public float nextFloat() {
		return (float)this.nextBits(24) * 5.9604645E-8F;
	}

	@Override
	public double nextDouble() {
		return (double)this.nextBits(53) * (double)1.110223E-16F;
	}

	@Override
	public void skip(int states) {
		for (int i = 0; i < states; i++)
			this.nextLongX();
	}

	// -----------------------------------------------------

	private long nextLongX() {
		long l = this.lo;
		long h = this.hi;
		long res = Long.rotateLeft(l + h, 17) + l;
		this.lo = Long.rotateLeft(l, 49) ^ (h ^= l) ^ h << 21;
		this.hi = Long.rotateLeft(h, 28);
		return res;
	}
}
