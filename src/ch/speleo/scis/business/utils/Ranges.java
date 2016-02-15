package ch.speleo.scis.business.utils;

/** Ranges of integers, for several discontinuous {@link Range}s. Zero or one Range is also possible. */
public class Ranges {
	
	private final Range[] ranges;
	
	public Ranges(Range... ranges) {
		this.ranges = ranges;
	}
	
	public Range[] getRanges() {
		return ranges;
	}
	
	public boolean contains(long value) {
		for (Range range: ranges) {
			if (range.contains(value))
				return true;
		}
		return false;
	}
	public boolean contains(double value) {
		for (Range range: ranges) {
			if (range.contains(value))
				return true;
		}
		return false;
	}

}
