package utils;

public class Random {

	java.util.Random random;

	public Random() {
		random = new java.util.Random();
	}

	public int randomInt(int x, int y) {
		return random.nextInt(Integer.MAX_VALUE) % ((y - x + 1) + x);
	}

	public double randomFloat() {
		return random.nextDouble();
	}

	public boolean randomBoolean() {
		if (randomInt(0, 1) > 0.5) {
			return true;
		} else {
			return false;
		}
	}

	public double randomClamped() {
		return randomFloat() - randomFloat();
	}
}
