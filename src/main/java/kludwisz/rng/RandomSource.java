package kludwisz.rng;

public interface RandomSource {

	void setSeed(long seed);

	void nextSeed();

	int nextBits(int bits);

	float nextFloat();

	double nextDouble();

	void skip(int states);
}
