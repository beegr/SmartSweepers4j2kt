import utils.Random;

public class Test {
	public static void main(String[] a) {
		Random rand = new Random();

		for (int i = 0; i < 100; i++) {
			int cp = rand.randomInt(0, 30);
			System.out.println(cp);
		}

	}
}
