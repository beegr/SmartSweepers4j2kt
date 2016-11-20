package generics;

import java.util.ArrayList;
import java.util.List;

import utils.Random;
import configuration.Parameters;

public class GenericAlgorithm {

	List<Genome> population;

	int populationSize;
	int chromosomeLength;
	double totalFitness;
	double bestFitness;
	double averageFitness;
	double worstFitness;
	int fittestGenome;
	double mutationRate;
	double crossoverRate;
	int generation;

	private Random rand;

	public GenericAlgorithm(int populationSize, double mutationRate, double crossoverRate, int numWeights) {
		super();
		this.populationSize = populationSize;
		this.chromosomeLength = numWeights;
		this.mutationRate = mutationRate;
		this.crossoverRate = crossoverRate;
		generation = 0;
		fittestGenome = 0;
		reset();

		population = new ArrayList<>();
		rand = new Random();
		for (int i = 0; i < populationSize; i++) {
			Genome genome = new Genome();
			population.add(genome);

			for (int numChromo = 0; numChromo < chromosomeLength; numChromo++) {
				genome.getWeights().add(rand.randomClamped());
			}
		}
	}

	public void mutate(List<Double> chromo) {
		for (int numChromo = 0; numChromo < chromo.size(); numChromo++) {
			if (rand.randomFloat() < mutationRate) {
				double chromoValue = chromo.get(numChromo);
				chromo.set(numChromo, chromoValue + (rand.randomClamped() * Parameters.dMaxPerturbation));
			}
		}
	}

	public Genome getChromoRoulette() {
		double slice = rand.randomFloat(0.2 * totalFitness, totalFitness);

		Genome theChosenOne = null;

		double fitnessSoFar = 0;

		for (int i = 0; i < populationSize; i++) {
			Genome currentGenome = population.get(i);
			fitnessSoFar += currentGenome.fitness;
			if (fitnessSoFar >= slice) {
				theChosenOne = currentGenome;
				break;
			}
		}
		return theChosenOne;
	}

	public void crossover(List<Double> mum, List<Double> dad, List<Double> baby1, List<Double> baby2) {
		if (rand.randomFloat() > crossoverRate || mum == dad) {
			for (int i = 0; i < mum.size(); i++) {
					baby1.add(rand.randomClamped());
					baby2.add(rand.randomClamped());
			}
			return;
		}

		for (int i = 0; i < mum.size(); i++) {
			if (rand.randomBoolean()) {
				baby1.add(mum.get(i));
			} else {
				baby1.add(dad.get(i));
			}

			if (rand.randomBoolean()) {
				baby2.add(mum.get(i));
			} else {
				baby2.add(dad.get(i));
			}
		}

	}

	public List<Genome> runEpoche(List<Genome> oldPopulation) {
		population = oldPopulation;

		reset();

		population.sort((o1, o2) -> (int) (o1.fitness - o2.fitness));

		calculateBestWorstAvTot();

		List<Genome> newPopulation = new ArrayList<>();

		if ((Parameters.iNumCopiesElite * Parameters.iNumElite) % 2 == 0) {
			grabNBest(Parameters.iNumElite, Parameters.iNumCopiesElite, newPopulation);
		}

		while (newPopulation.size() < populationSize) {
			Genome mum = getChromoRoulette();
			Genome dad = getChromoRoulette();

			List<Double> baby1 = new ArrayList<>();
			List<Double> baby2 = new ArrayList<>();

			crossover(mum.getWeights(), dad.getWeights(), baby1, baby2);

			mutate(baby1);
			mutate(baby2);

			newPopulation.add(new Genome(baby1, 0));
			newPopulation.add(new Genome(baby2, 0));
		}

		population = newPopulation;

		return population;
	}

	private void grabNBest(int nBest, int numCopies, List<Genome> population) {
		while (nBest > 0 && population.size() < populationSize) {
			nBest--;
			for (int i = 0; i < numCopies; i++) {
				population.add(this.population.get(populationSize - 1 - nBest));
			}
		}
	}

	private void calculateBestWorstAvTot() {
		totalFitness = 0;

		double highestSoFar = 0;
		double lowestSoFar = Double.MAX_VALUE;

		for (int i = 0; i < populationSize; i++) {
			double currentFitness = population.get(i).fitness;
			if (currentFitness > highestSoFar) {
				highestSoFar = currentFitness;
				fittestGenome = i;
				bestFitness = highestSoFar;
			}
			if (currentFitness < lowestSoFar) {
				lowestSoFar = currentFitness;
				worstFitness = lowestSoFar;
			}

			totalFitness += currentFitness;
		}

		averageFitness = totalFitness / populationSize;
	}

	private void reset() {
		totalFitness = 0;
		bestFitness = 0;
		worstFitness = Double.MAX_VALUE;
		averageFitness = 0;
	}

	public List<Genome> getChromos() {
		return population;
	}

	public double getAverageFitness() {
		return totalFitness / populationSize;
	}

	public double getBestFitness() {
		return bestFitness;
	}

}
