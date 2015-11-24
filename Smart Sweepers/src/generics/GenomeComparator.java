package generics;

import java.util.Comparator;

public class GenomeComparator implements Comparator<Genome> {

	@Override
	public int compare(Genome o1, Genome o2) {
		return (int) (o1.fitness - o2.fitness);
	}

}