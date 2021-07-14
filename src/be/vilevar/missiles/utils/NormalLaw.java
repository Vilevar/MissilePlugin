package be.vilevar.missiles.utils;

import java.util.Random;

public class NormalLaw {

	private final Random random;
	private final double mean;
	private final double std;
	
	public NormalLaw(double mean, double std) {
		this(new Random(), mean, std);
	}
	
	public NormalLaw(Random random, double mean, double std) {
		this.random = random;
		this.mean = mean;
		this.std = std;
	}
	
	public double getMean() {
		return mean;
	}
	
	public double getStandardDeviation() {
		return std;
	}
	
	public double nextValue() {
		return (random.nextGaussian() * std) + mean;
	}
}
