package ch.speleo.scis.business.utils;

/** A range of integers. */
public class Range {
	
	private final int min;
	private final int max;
	
	public Range(int min, int max) {
		this.min = min;
		this.max = max;
	}
	
	public int getMin() {
		return min;
	}
	public int getMax() {
		return max;
	}
	
	public boolean contains(long value) {
		return value >= min && value <= max;
	}
	public boolean contains(double value) {
		return value >= min && value <= max;
	}
	
}
