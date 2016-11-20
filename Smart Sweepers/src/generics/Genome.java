package generics;

import java.util.ArrayList;
import java.util.List;

public class Genome {
	private List<Double> weights;
	double fitness;

	public double getFitness() {
		return fitness;
	}

	public void setFitness(double fitness) {
		this.fitness = fitness;
	}

	public Genome() {
		fitness = 0;
		weights = new ArrayList<>();
	}

	public Genome(List<Double> weights, double fitness) {
		this.setWeights(weights);
		this.fitness = fitness;
	}

	public List<Double> getWeights() {
		return weights;
	}

	public void setWeights(List<Double> weights) {
		this.weights = weights;
	}

}
